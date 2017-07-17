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
package net.pms.util.jna;


/**
 * This interface provides the possibility for automatic conversion between
 * {@link Enum} and C-style integer enums.
 *
 * @param <T> the Enum type
 */
public interface JnaIntEnum<T> {

	/**
	 * @return This constant's {@code int} value
	 */
	int getValue();

	/**
	 * Tries to find a constant corresponding to {@code value}.
	 *
	 * @param value the integer value to look for.
	 * @return The corresponding constant or {@code null} if not found.
	 */
	T typeForValue(int value);
}
