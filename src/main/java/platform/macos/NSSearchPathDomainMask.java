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
package platform.macos;

/**
 * These constants represents the {@code NS_OPTIONS} with the same name.
 */
@SuppressWarnings("checkstyle:ConstantName")
public class NSSearchPathDomainMask {

	/** Not to be instantiated. */
	private NSSearchPathDomainMask() {
	}

	/**
	 * User's home directory --- place to install user's personal items
	 * ({@code ~})
	 */
	public static final long NSUserDomainMask = 1L;

	/**
	 * Local to the current machine --- place to install items available to
	 * everyone on this machine ({@code /Library})
	 */
	public static final long NSLocalDomainMask = 2L;

	/**
	 * Publicly available location in the local area network --- place to
	 * install items available on the network ({@code /Network})
	 */
	public static final long NSNetworkDomainMask = 4L;

	/** Provided by Apple, unmodifiable ({@code /System}) */
	public static final long NSSystemDomainMask = 8L;

	/** All domains: all of the above and future items */
	public static final long NSAllDomainsMask = 0x0ffffL;
}
