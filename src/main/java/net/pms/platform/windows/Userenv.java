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

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;


/**
 * Partial mapping of {@code Userenv.dll}.
 *
 * @author Nadahar
 */
@SuppressWarnings("checkstyle:MethodName")
public interface Userenv extends StdCallLibrary {

	/**
	 * The instance of this interface.
	 */
	Userenv INSTANCE = Native.loadLibrary("Userenv", Userenv.class, W32APIOptions.DEFAULT_OPTIONS);

	/**
	 * Retrieves the path to the root of the directory that contains program
	 * data shared by all users.
	 *
	 * @param profileDir a buffer that, when this function returns successfully,
	 *            receives the path. Set this value to {@code null} to determine
	 *            the required size of the buffer, including the terminating
	 *            null character.
	 * @param chSize a {@link Pointer} to the size of the {@code profileDir}
	 *            buffer, in chars. If the buffer specified by
	 *            {@code profileDir} is not large enough or {@code profileDir}
	 *            is {@code null}, the function fails and this parameter
	 *            receives the necessary buffer size, including the terminating
	 *            null character.
	 * @return {@code true} if successful; otherwise, {@code false}. To get
	 *         extended error information, call {@link Kernel32#GetLastError()}.
	 */
	boolean GetAllUsersProfileDirectoryW(char[] profileDir, IntByReference chSize);

	/**
	 * Retrieves the path to the root of the default user's profile.
	 *
	 * @param profileDir a buffer that, when this function returns successfully,
	 *            receives the path to the default user's profile directory. Set
	 *            this value to {@code null} to determine the required size of
	 *            the buffer.
	 * @param chSize the size of the {@code profileDir} buffer, in chars. If the
	 *            buffer specified by {@code profileDir} is not large enough or
	 *            {@code profileDir} is {@code null}, the function fails and
	 *            this parameter receives the necessary buffer size, including
	 *            the terminating null character.
	 * @return {@code true} if successful; otherwise, {@code false}. To get
	 *         extended error information, call {@link Kernel32#GetLastError()}.
	 */
	boolean GetDefaultUserProfileDirectoryW(char[] profileDir, IntByReference chSize);
}
