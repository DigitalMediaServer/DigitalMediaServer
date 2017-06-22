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
package net.pms.util;

import static org.apache.commons.lang3.StringUtils.isBlank;
import java.util.Locale;

/**
 * This {@code enum} represents the possible bit rate "modes".
 */
public enum BitRateMode {

	/** Constant bit rate mode*/
	CBR,

	/** Variable bit rate mode */
	VBR;

	@Override
	public String toString() {
		switch (this) {
			case CBR:
				return "Constant";
			case VBR:
				return "Variable";
			default:
				return super.toString();
		}
	}

	/**
	 * Tries to parse {@code value} and return the corresponding
	 * {@link BitRateMode}. Returns {@code null} if the parsing fails.
	 *
	 * @param value the {@link String} to parse.
	 * @return The corresponding {@link BitRateMode} or {@code null}.
	 */
	public static BitRateMode typeOf(String value) {
		if (isBlank(value)) {
			return null;
		}
		value = value.toUpperCase(Locale.ROOT).trim();
		switch (value) {
			case "CBR":
			case "CONSTANT":
				return CBR;
			case "VBR":
			case "VARIABLE":
				return VBR;
			default:
				return null;
		}
	}
}
