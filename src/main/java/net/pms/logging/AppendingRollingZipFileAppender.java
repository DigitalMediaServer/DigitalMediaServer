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

import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.RollingPolicy;
import static org.apache.commons.lang3.StringUtils.isBlank;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;


/**
 * This implementation will compress and rename the previous log file if used
 * with {@link AppendingZipRollingPolicy}. If used with another
 * {@link RollingPolicy}, it will behave exactly like
 * {@link RollingFileAppender}.
 *
 * @param <E> the event type.
 */
public class AppendingRollingZipFileAppender<E> extends RollingFileAppender<E> {

	@Override
	public void start() {
		if (getRollingPolicy() instanceof AppendingZipRollingPolicy) {
			savePreviousFile();
		}
		super.start();
	}

	private void savePreviousFile() {
		String filename = getFile();
		if (isBlank(filename)) {
			return;
		}
		Path current, currentZip, previousZip;
		try {
			current = Paths.get(filename);
			currentZip = current.resolveSibling(filename + ".zip");
			previousZip = current.resolveSibling(filename + ".prev.zip");
		} catch (InvalidPathException e) {
			addWarn(
				"Failed to save the previous log file [" + filename + "] because the path is invalid: " + e.getMessage(),
				e
			);
			return;
		}
		if (Files.isRegularFile(current)) {
			addInfo("The previous log file will be compressed to [" + currentZip.toAbsolutePath().toString() + "].");
			AppendingZipCompressor compressor = new AppendingZipCompressor();
			compressor.setContext(getContext());
			Path entryName = current.getFileName();
			if (entryName == null) {
				addWarn(
					"Failed to save the previous log file [" + filename + "] because something very strange has happened"
				);
				return;
			}
			compressor.compress(
				current.toAbsolutePath().toString(),
				currentZip.toAbsolutePath().toString(),
				entryName.toString()
			);
			if (Files.exists(current)) {
				addWarn("Failed to save the previous log file [" + filename + ']');
				return;
			}
		}
		if (Files.isRegularFile(currentZip)) {
			addInfo(
				"The compressed previous log file will be renamed to [" + previousZip.toAbsolutePath().toString() + ']'
			);
			try {
				Files.move(currentZip, previousZip, StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				addWarn(
					"Failed to rename the compressed previous log file to [" + previousZip.toAbsolutePath().toString() +
					"]: " + e.getMessage(),
					e
				);
			}
		}
	}
}
