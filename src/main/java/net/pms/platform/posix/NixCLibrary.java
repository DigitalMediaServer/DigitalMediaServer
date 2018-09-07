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
package net.pms.platform.posix;

import com.sun.jna.Library;
import com.sun.jna.Native;

/**
 * Partial mapping of the *NIX standard C library.
 *
 * @author Nadahar
 */
public interface NixCLibrary extends Library {

	/**
	 * The instance of this interface.
	 */
	NixCLibrary INSTANCE = Native.loadLibrary("c", NixCLibrary.class);

	/**
	 * Returns the null-terminated hostname in the character array name, which
	 * has a size of {@code bufferSize} bytes. If the null-terminated hostname
	 * is too large to fit, then the name is truncated, and no error is returned
	 * except for in older libraries. POSIX.1-2001 says that if such truncation
	 * occurs, then it is unspecified whether the returned buffer includes a
	 * terminating null byte.
	 *
	 * @param hostname the byte array to fill with the hostname.
	 * @param bufferSize the size of the byte array.
	 * @return {@code 0} on success, {@code -1} on error.
	 */
	public int gethostname(byte[] hostname, int bufferSize);
}
