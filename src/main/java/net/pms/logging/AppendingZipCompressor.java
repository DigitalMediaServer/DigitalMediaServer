/*
 * Digital Media Server, for streaming digital media to DLNA compatible devices
 * based on www.ps3mediaserver.org and www.universalmediaserver.com.
 * Copyright (C) 2022 Digital Media Server developers.
 *
 * This program is a free software; you can redistribute it and/or
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
package net.pms.logging;

import ch.qos.logback.core.rolling.helper.CompressionMode;
import ch.qos.logback.core.rolling.helper.Compressor;
import ch.qos.logback.core.status.WarnStatus;
import net.pms.util.FileUtil;
import net.pms.util.Pair;
import net.pms.util.StringUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;


/**
 * A {@link Compressor} implementation that inserts the compressed file into an
 * existing ZIP archive if it already exists. If it doesn't exist, a new ZIP
 * archive will be created.
 * <p>
 * Can only handle {@link CompressionMode#ZIP}.
 */
public class AppendingZipCompressor extends Compressor {

	/** The buffer size */
	protected static final int BUFFER_SIZE = 65536;

	/**
	 * Creates a new instance.
	 */
	public AppendingZipCompressor() {
		super(CompressionMode.ZIP);
	}

	/**
	 * Compresses the specified file and stores it in the specified ZIP file,
	 * before finally deleting the specified file. There's no feedback if the
	 * operation fails.
	 *
	 * @param nameOfFile2Compress the filename to add to the ZIP file.
	 * @param nameOfCompressedFile the ZIP file to add the file to.
	 * @param innerEntryName the name of the file within the ZIP file.
	 */
	@Override
	public void compress(String nameOfFile2Compress, String nameOfCompressedFile, String innerEntryName) {
		zipCompress(nameOfFile2Compress, nameOfCompressedFile, innerEntryName);
	}

	private void zipCompress(String nameOfFile2zip, String nameOfZippedFile, String innerEntryName) {
		File file2zip = new File(nameOfFile2zip);

		if (!file2zip.exists()) {
			addStatus(new WarnStatus("The file to compress named [" + nameOfFile2zip + "] does not exist.", this));
			return;
		}

		if (innerEntryName == null) {
			addStatus(new WarnStatus("The innerEntryName parameter cannot be null", this));
			return;
		}

		if (!FileUtil.isExtension(nameOfZippedFile, false, "zip")) {
			nameOfZippedFile += ".zip";
		}
		long start = System.currentTimeMillis();
		File zippedFile = new File(nameOfZippedFile);
		File tmpZippedFile = new File(nameOfZippedFile + ".tmp");

		boolean result = ch.qos.logback.core.util.FileUtil.createMissingParentDirectories(zippedFile);
		if (!result) {
			addError("Failed to create parent folders for [" + zippedFile.getAbsolutePath() + "]");
			return;
		}

		if (tmpZippedFile.exists()) {
			if (!tmpZippedFile.delete()) {
				addWarn("Failed to delete old temporary file [" + tmpZippedFile + ']');
			}
		}
		boolean append = false;
		if (zippedFile.exists()) {
			if (zippedFile.renameTo(tmpZippedFile)) {
				append = true;
				addInfo("Appending to existing ZIP file [" + nameOfZippedFile + "]");
			} else {
				addWarn("Failed to rename [" + nameOfZippedFile + "] to [" + tmpZippedFile + "] - file locked?");
				return;
			}
		}
		addInfo("ZIP compressing [" + file2zip + ']' + (append ? " into " : " as ") + '[' + zippedFile + ']');

		byte[] buffer = new byte[BUFFER_SIZE];
		int n;
		int index = 0;
		try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(nameOfZippedFile))) {
			if (append) {
				// Copy contents from existing zip file
				try (ZipFile appendZip = new ZipFile(tmpZippedFile)) {
					Enumeration<? extends ZipEntry> entries = appendZip.entries();
					while (entries.hasMoreElements()) {
						ZipEntry zipEntry = entries.nextElement();
						zos.putNextEntry(zipEntry);
						if (!zipEntry.isDirectory()) {
							index++;
							InputStream zipEntryStream = appendZip.getInputStream(zipEntry);
							while ((n = zipEntryStream.read(buffer)) != -1) {
								zos.write(buffer, 0, n);
							}
						}
						zos.closeEntry();
					}
				} catch (ZipException e) {
					addError("Failed to preserve existing ZIP content: " + e.getMessage(), e);
				}
				if (!tmpZippedFile.delete()) {
					addWarn("Failed to delete [" + tmpZippedFile.getAbsolutePath() + ']');
				}
			}

			// Rename the requested file
			Pair<String, String> pair = FileUtil.detachExtension(innerEntryName);
			StringBuilder sb = new StringBuilder(innerEntryName.length() + 5);
			if (pair.getFirst() != null) {
				sb.append(pair.getFirst());
			}
			sb.append('.').append(String.format("%04d", index));
			if (pair.getSecond() != null) {
				sb.append('.').append(pair.getSecond());
			}

			// Add the requested file
			ZipEntry zipEntry = new ZipEntry(computeFileNameStrWithoutCompSuffix(sb.toString(), CompressionMode.ZIP));
			zos.putNextEntry(zipEntry);
			try (InputStream is = new FileInputStream(nameOfFile2zip)) {
				while ((n = is.read(buffer)) != -1) {
					zos.write(buffer, 0, n);
				}
			} catch (IOException e) {
				addError("An error occurred while compressing [" + nameOfFile2zip + "]: " + e.getMessage(), e);
			} finally {
				zos.closeEntry();
			}

			long end = System.currentTimeMillis();
			if (!file2zip.delete()) {
				addWarn("Failed to delete [" + nameOfFile2zip + ']');
			}
			addInfo(
				"Compressing [" + nameOfFile2zip + "] took " + StringUtil.formatDuration(end - start, true) + " seconds"
			);
		} catch (IOException e) {
			addError(
				"Error occurred while compressing [" + nameOfFile2zip + ']' + (append ? " into " : " as ") +
				'[' + nameOfZippedFile + "]: " + e.getMessage(),
				e
			);
		}
	}
}
