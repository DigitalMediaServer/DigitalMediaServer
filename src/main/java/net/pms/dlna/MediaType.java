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
package net.pms.dlna;


/**
 * Defines the media types.
 *
 * @author Nadahar
 */
public enum MediaType {

	/** {@code UNKNOWN} for when the media type is unknown. */
	UNKNOWN(0),

	/** {@code AUDIO} for when the media type is audio. */
	AUDIO(1),

	/** {@code IMAGE} for when the media type is image. */
	IMAGE(2),

	/** {@code VIDEO} for when the media type is video. */
	VIDEO(3);

	private int value;
	private MediaType(int value) {
		this.value = value;
	}

	/**
	 * @param value {@link MediaType} integer value.
	 * @return The {@link MediaType} corresponding to the integer value or
	 *         {@code null} if invalid.
	 */
	public static MediaType typeOf(int value) {
		for (MediaType mediaType : MediaType.values()) {
			if (mediaType.value == value) {
				return mediaType;
			}
		}
		return null;
	}

	/**
	 * @return The integer value for this {@link MediaType}.
	 */
	public int getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "Media type " + this.name() + " (" + value + ")";
	}
}
