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
package net.pms.platform.macos;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import net.pms.util.jna.macos.corefoundation.CoreFoundation;
import net.pms.util.jna.macos.corefoundation.CoreFoundation.CFStringBuiltInEncodings;
import net.pms.util.jna.macos.corefoundation.CoreFoundation.CFStringRef;

/**
 * Partial JNA mapping of macOS SystemConfiguration.
 *
 * @author Nadahar
 */
@SuppressWarnings("checkstyle:MethodName")
public interface SystemConfiguration extends Library {

	/**
	 * The instance of this interface.
	 */
	SystemConfiguration INSTANCE = Native.loadLibrary("SystemConfiguration", SystemConfiguration.class);

	/**
	 * Returns the current computer name.
	 *
	 * @param store the dynamic store session that should be used for
	 *            communication with the server. Pass {@code null} to use a
	 *            temporary session.
	 * @param nameEncoding a pointer to memory that, on output, is filled with
	 *            the encoding associated with the computer or host name, if it
	 *            is non-{@code null}. Use
	 *            {@link CFStringBuiltInEncodings#typeOf(int)} to translate the
	 *            value to a {@link CFStringBuiltInEncodings} value if the
	 *            returned integer is corresponds to a valid
	 *            {@link CFStringBuiltInEncodings}.
	 * @return The current computer name, or {@code null} if the name has not
	 *         been set or if an error occurred. You must release the return
	 *         value with
	 *         {@link CoreFoundation#CFRelease(CoreFoundation.CFTypeRef)}.
	 * @since macOS 10.1
	 */
	CFStringRef SCDynamicStoreCopyComputerName(Pointer store, IntByReference nameEncoding);
}
