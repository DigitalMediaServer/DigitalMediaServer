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
package net.pms.util.jna.macos.kernreturn;


/**
 * Common interface for all {@code kern_return_t} values, which maps return
 * codes to {@link KernReturnT} instances.
 *
 * @author Nadahar
 */
@SuppressWarnings("checkstyle:JavadocStyle")
public interface KernReturnT {

	/**
	 * @return The integer value for this instance.
	 */
	int getValue();

	/**
	 * Returns a standardized string representation of a {@link KernReturnT}
	 * return code in the form {@code "<name> (0x<hexcode>)"}.
	 * <p>
	 * Subclasses should use {@link Long#toHexString()} since
	 * {@code kern_return_t} is unsigned. Subclasses may also reroute
	 * {@link #toString()} to {@link #toStandardString()}. Example implementation:
	 *
	 * <pre>
	 * {@code return "<name> (0x" +  Long.toHexString(<code> & 0xFFFFFFFFL) + ")";}
	 * </pre>
	 *
	 * @return The formatted {@link String}.
	 */
	String toStandardString();
}
