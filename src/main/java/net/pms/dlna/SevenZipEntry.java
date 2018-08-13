/*
 * Universal Media Server
 * Copyright (C) 2012  SharkHunter
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2
 * of the License only.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package net.pms.dlna;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import net.pms.formats.FormatType;
import net.pms.util.FileUtil;
import net.sf.sevenzipjbinding.ExtractOperationResult;
import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.ISequentialOutStream;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import net.sf.sevenzipjbinding.simple.ISimpleInArchive;
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class SevenZipEntry extends DLNAResource implements IPushOutput {
	private static final Logger LOGGER = LoggerFactory.getLogger(SevenZipEntry.class);
	private final  File file;
	private final String itemPath;
	private final long length;

	public SevenZipEntry(File file, String itemPath, long length) {
		this.itemPath = itemPath;
		this.file = file;
		this.length = length;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return null;
	}

	@Override
	public String getName() {
		return itemPath;
	}

	@Override
	public long length() {
		if (getPlayer() != null && getPlayer().type() != FormatType.IMAGE) {
			return DLNAMediaInfo.TRANS_SIZE;
		}

		return length;
	}

	@Override
	public boolean isFolder() {
		return false;
	}

	@Override
	public String getSystemName() {
		return FileUtil.getFileNameWithoutExtension(file.getAbsolutePath()) + "." + FileUtil.getExtension(itemPath);
	}

	@Override
	public boolean isValid() {
		resolveFormat();
		setHasExternalSubtitles(FileUtil.isSubtitlesExists(file, null));
		return getFormat() != null;
	}

	@Override
	public boolean isUnderlyingSeekSupported() {
		return length() < MAX_ARCHIVE_SIZE_SEEK;
	}

	@Override
	public void push(final OutputStream outputStream) throws IOException {
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				try (
					RandomAccessFile rf = new RandomAccessFile(file, "r");
					IInArchive archive = SevenZip.openInArchive(null, new RandomAccessFileInStream(rf));
					BufferedOutputStream bos = outputStream instanceof BufferedOutputStream ?
						(BufferedOutputStream) outputStream :
						new BufferedOutputStream(outputStream);
				){
					ISimpleInArchive simpleInArchive = archive.getSimpleInterface();
					ISimpleInArchiveItem realItem = null;

					for (ISimpleInArchiveItem item : simpleInArchive.getArchiveItems()) {
						if (item.getPath().equals(itemPath)) {
							realItem = item;
							break;
						}
					}

					if (realItem == null) {
						LOGGER.warn("Can't find \"{}\" in archive file \"{}\"", itemPath, file);
						return;
					}

					final LastException lastException = new LastException();
					ExtractOperationResult result = realItem.extractSlow(new ISequentialOutStream() {
						@Override
						public int write(byte[] data) throws SevenZipException {
							try {
								bos.write(data);
							} catch (IOException e) {
								lastException.exception = e;
							}
							return data.length;
						}
					});
					if (result != ExtractOperationResult.OK) {
						throw new SevenZipException(result.name(), lastException.exception);
					}

				} catch (FileNotFoundException e) {
					LOGGER.error("An error occurred while trying to read archive file \"{}\": {}", file, e.getMessage());
					LOGGER.trace("", e);
				} catch (SevenZipException e) {
					LOGGER.error("An error occurred while extracting \"{}\" from \"{}\": {}", itemPath, file, e.getMessage());
					LOGGER.trace("", e);
				} catch (IOException e) {
					LOGGER.error("An error occurred while closing archive file \"{}\": {}", file, e.getMessage());
					LOGGER.trace("", e);
				}
			}
		};

		new Thread(runnable, "7Zip Extractor for \"" + itemPath + "\"").start();
	}

	@Override
	public synchronized void resolve() {
		if (!isVideo()) {
			return;
		}

		boolean found = false;

		if (!found) {
			if (getMedia() == null) {
				setMedia(new DLNAMediaInfo());
			}

			found = !getMedia().isMediaparsed() && !getMedia().isParsing();

			if (getFormat() != null) {
				InputFile input = new InputFile();
				input.setPush(this);
				input.setSize(length());
				getFormat().parse(getMedia(), input, null);
				if (getMedia() != null && getMedia().isSLS()) {
					setFormat(getMedia().getAudioVariantFormat());
				}
			}
		}

		super.resolve();
	}

	@Override
	public DLNAThumbnailInputStream getThumbnailInputStream() throws IOException {
		if (getMedia() != null && getMedia().getThumb() != null) {
			return getMedia().getThumbnailInputStream();
		}
		return super.getThumbnailInputStream();
	}

	@Override
	protected String getThumbnailURL(DLNAImageProfile profile) {
		if (!isVideo()) {
			// no thumbnail support for now for zipped videos
			return null;
		}

		return super.getThumbnailURL(profile);
	}

	@Override
	public String write() {
		return getName() + ">" + file.getAbsolutePath() + ">" + length;
	}

	/**
	 * This class is used as a hack to hide "The pipe is being closed"
	 * exceptions that are being thrown during extraction. The real cause for
	 * this is unknown, but can very well be because of missing synchronization.
	 *
	 * @author Nadahar
	 */
	@SuppressFBWarnings("NM_CLASS_NOT_EXCEPTION")
	private static class LastException {
		private IOException exception;
	}
}
