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
package net.pms.formats;

import javax.annotation.Nullable;
import net.pms.dlna.MediaType;

/**
 * Defines the different "format types". This is an imprecise definition that
 * should be redefined in the future.
 *
 * @author Nadahar
 */
public enum FormatType {

	/** An audio format that isn't a container */
	AUDIO(1),

	/** An image format that isn't a container */
	IMAGE(2),

	/** A container format that can hold different types of content */
	CONTAINER(4),

	/** A video format that isn't a container */
	VIDEO(128),

	/** A playlist format */
	PLAYLIST(16),

	/** An ISO container */
	ISO(32),

	/** A subtitles format that isn't a container */
	SUBTITLES(64);

	/**
	 * This is only used for database compatibility with the old values, and can
	 * be removed if the database table is updated.
	 */
	private final int value;

	private FormatType(int value) {
		this.value = value;
	}

	/**
	 * @return The legacy integer value that corresponds to this
	 *         {@link FormatType}.
	 */
	public int getValue() {
		return value;
	}

	/**
	 * Although logically impossible, tries to map {@link FormatType} to
	 * {@link MediaType}.
	 *
	 * @return The "corresponding" {@link MediaType} or {@code null}.
	 */
	@Nullable
	public MediaType toMediaType() {
		switch (this) {
			case AUDIO:
				return MediaType.AUDIO;
			case IMAGE:
				return MediaType.IMAGE;
			case VIDEO:
				return MediaType.VIDEO;
			case CONTAINER:
				// An incorrect assumption
				return MediaType.VIDEO;
			default:
				return null;
		}
	}

	/**
	 * Although logically impossible, tries to map {@link FormatType} to
	 * {@link MediaType}.
	 *
	 * @param type the {@link FormatType} to convert from.
	 * @return The "corresponding" {@link MediaType} or {@code null}.
	 */
	@Nullable
	public static MediaType toMediaType(@Nullable FormatType type) {
		return type == null ? null : type.toMediaType();
	}

	/**
	 * Converts a legacy integer value to a {@link FormatType}.
	 *
	 * @param value the legacy integer value to convert.
	 * @return The corresponding {@link FormatType} or {@code null} if the
	 *         specified value is invalid.
	 */
	@Nullable
	public static FormatType typeOf(int value) {
		for (FormatType type : values()) {
			if (type.value == value) {
				return type;
			}
		}
		return null;
	}
}
