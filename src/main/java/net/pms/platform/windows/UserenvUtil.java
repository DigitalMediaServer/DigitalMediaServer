/*
 * Digital Media Server, for streaming digital media to UPnP AV or DLNA
 * compatible devices based on PS3 Media Server and Universal Media Server.
 * Copyright (C) 2016 Digital Media Server developers.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see http://www.gnu.org/licenses/.
 */
package net.pms.platform.windows;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.ptr.IntByReference;


/**
 * This utility class implements functions from Windows {@code Userenv.dll}.
 *
 * @author Nadahar
 */
public class UserenvUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserenvUtil.class);

	/**
	 * Not to be instantiated.
	 */
	private UserenvUtil() {
	}

	/**
	 * @return The {@link Path} to the root of the directory that contains
	 *         program data shared by all users, or {@code null} if the
	 *         operation fails.
	 */
	@Nullable
	public static Path getAllUsersProfileDirectory() {
		IntByReference size = new IntByReference();
		Userenv.INSTANCE.GetAllUsersProfileDirectoryW(null, size);
		char[] buffer = new char[size.getValue()];
		if (Userenv.INSTANCE.GetAllUsersProfileDirectoryW(buffer, size)) {
			try {
				return Paths.get(Native.toString(buffer));
			} catch (InvalidPathException e) {
				LOGGER.error(
					"Failed to retrieve the all-users profile folder with error: {}",
					e.getMessage()
				);
				LOGGER.trace("", e);
				return null;
			}
		}
		LOGGER.error(
			"Failed to retrieve the all-users profile folder with error code: {}",
			Kernel32.INSTANCE.GetLastError()
		);
		return null;
	}

	/**
	 * @return The {@link Path} to the root of the default user's profile, or
	 *         {@code null} if the operation fails.
	 */
	@Nullable
	public static Path getDefaultUserProfileDirectory() {
		IntByReference size = new IntByReference();
		Userenv.INSTANCE.GetDefaultUserProfileDirectoryW(null, size);
		char[] buffer = new char[size.getValue()];
		if (Userenv.INSTANCE.GetDefaultUserProfileDirectoryW(buffer, size)) {
			try {
				return Paths.get(Native.toString(buffer));
			} catch (InvalidPathException e) {
				LOGGER.error(
					"Failed to retrieve the default user profile folder with error: {}",
					e.getMessage()
				);
				LOGGER.trace("", e);
				return null;
			}
		}
		LOGGER.error(
			"Failed to retrieve the default user profile folder with error code: {}",
			Kernel32.INSTANCE.GetLastError()
		);
		return null;
	}
}
