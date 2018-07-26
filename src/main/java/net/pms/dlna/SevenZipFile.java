/*
 * Universal Media Server
 * Copyright (C) 2008  SharkHunter
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import net.sf.sevenzipjbinding.simple.ISimpleInArchive;
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SevenZipFile extends DLNAResource {
	private static final Logger LOGGER = LoggerFactory.getLogger(SevenZipFile.class);
	private File file;
	private IInArchive archive;

	public SevenZipFile(File file) {
		this.file = file;
		setLastModified(this.file.lastModified());
		try {
			RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
			archive = SevenZip.openInArchive(null, new RandomAccessFileInStream(randomAccessFile));
			ISimpleInArchive simpleInArchive = archive.getSimpleInterface();

			for (ISimpleInArchiveItem item : simpleInArchive.getArchiveItems()) {
				LOGGER.debug("Found \"{}\" in archive \"{}\"", item.getPath(), this.file.getAbsolutePath());

				// Skip folders for now
				if (item.isFolder()) {
					continue;
				}
				addChild(new SevenZipEntry(file, item.getPath(), item.getSize()));
			}
		} catch (FileNotFoundException e) {
			LOGGER.error("An error occurred while trying to read archive file \"{}\": {}", this.file, e.getMessage());
			LOGGER.trace("", e);
		} catch (SevenZipException e) {
			LOGGER.error("An error occurred while reading archive file \"{}\": {}", this.file, e.getMessage());
			LOGGER.trace("", e);
		}
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return null;
	}

	@Override
	public String getName() {
		return file.getName();
	}

	@Override
	public long length() {
		return 0;
	}

	@Override
	public boolean isFolder() {
		return true;
	}

	@Override
	public String getSystemName() {
		return file.getAbsolutePath();
	}

	@Override
	public boolean isValid() {
		return file.exists();
	}
}
