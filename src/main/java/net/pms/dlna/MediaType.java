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

import static org.apache.commons.lang3.StringUtils.isBlank;
import java.util.Locale;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;


/**
 * Defines the media types.
 *
 * @author Nadahar
 */
public enum MediaType {

	/** Audio media */
	AUDIO(1),

	/** Still image media */
	IMAGE(2),

	/** Video media */
	VIDEO(3);

	private final int value;

	private MediaType(int value) {
		this.value = value;
	}

	/**
	 * @return The integer value for this {@link MediaType}.
	 */
	public int getValue() {
		return value;
	}

	/**
	 * @return {@code true} if this is {@link #AUDIO}, {@code false} otherwise.
	 */
	public boolean isAudio() {
		return this == AUDIO;
	}

	/**
	 * @return {@code true} if this is {@link #IMAGE}, {@code false} otherwise.
	 */
	public boolean isImage() {
		return this == IMAGE;
	}

	/**
	 * @return {@code true} if this is {@link #VIDEO}, {@code false} otherwise.
	 */
	public boolean isVideo() {
		return this == VIDEO;
	}

	/**
	 * Determines if this is one of the specified {@link MediaType}s.
	 *
	 * @param types {@link MediaType}s.
	 * @return {@code true} if this is one of {@code types}, {@code false}
	 *         otherwise.
	 */
	public boolean isAmong(@Nullable MediaType... types) {
		if (types == null || types.length == 0) {
			return false;
		}
		for (MediaType type : types) {
			if (this == type) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return The lower-case "code" that represents this {@link MediaType}.
	 */
	@Nonnull
	public String getString() {
		return name().toLowerCase(Locale.ROOT);
	}

	@Override
	public String toString() {
		return "MediaType " + this.name() + " (" + value + ")";
	}

	/**
	 * @param value {@link MediaType} integer value.
	 * @return The {@link MediaType} corresponding to the integer value or
	 *         {@code null} if invalid.
	 */
	@Nullable
	public static MediaType typeOf(int value) {
		for (MediaType mediaType : MediaType.values()) {
			if (mediaType.value == value) {
				return mediaType;
			}
		}
		return null;
	}

	/**
	 * Determines if the specified {@link MediaType} is {@link #AUDIO}.
	 *
	 * @param type the {@link MediaType}.
	 * @return {@code true} if the specified {@link MediaType} is {@link #AUDIO},
	 *         {@code false} otherwise.
	 */
	public static boolean isAudio(@Nullable MediaType type) {
		return type == AUDIO;
	}

	/**
	 * Determines if the specified {@link MediaType} is {@link #IMAGE}.
	 *
	 * @param type the {@link MediaType}.
	 * @return {@code true} if the specified {@link MediaType} is {@link #IMAGE},
	 *         {@code false} otherwise.
	 */
	public static boolean isImage(@Nullable MediaType type) {
		return type == IMAGE;
	}

	/**
	 * Determines if the specified {@link MediaType} is {@link #VIDEO}.
	 *
	 * @param type the {@link MediaType}.
	 * @return {@code true} if the specified {@link MediaType} is {@link #VIDEO},
	 *         {@code false} otherwise.
	 */
	public static boolean isVideo(@Nullable MediaType type) {
		return type == VIDEO;
	}

	/**
	 * Determines if the specified {@link MediaType} is one of the specified
	 * {@link MediaType}s.
	 *
	 * @param type the {@link MediaType} to check.
	 * @param types the {@link MediaType}s to check among.
	 * @return {@code true} if {@code type} is one of {@code types},
	 *         {@code false} otherwise.
	 */
	public static boolean isOf(@Nullable MediaType type, @Nullable MediaType... types) {
		return type == null || types == null ? false : type.isAmong(types);
	}

	/**
	 * Attempts to parse the specified string to a {@link MediaType}.
	 *
	 * @param value the {@link String} to parse.
	 * @return The corresponding {@link MediaType} or {@code null}.
	 */
	@Nullable
	public static MediaType typeOf(@Nullable String value) {
		if (isBlank(value)) {
			return null;
		}

		switch (value.trim().toLowerCase(Locale.ROOT)) {
			case "audio":
				return AUDIO;
			case "image":
				return IMAGE;
			case "video":
				return VIDEO;
			default:
				return null;
		}
	}
}
