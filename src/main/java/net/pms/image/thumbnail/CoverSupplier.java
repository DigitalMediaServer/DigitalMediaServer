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
package net.pms.image.thumbnail;

import java.util.Locale;
import javax.annotation.Nullable;

/**
 * Defines the suppliers of covers/album art.
 *
 * @author Nadahar
 */
public enum CoverSupplier {

	/** No cover/album art supplier */
	NONE,

	/** Cover Art Archive */
	COVER_ART_ARCHIVE;

	/**
	 * Tries to parse a {@link String} to a {@link CoverSupplier}. If no match
	 * can be found, {@code null} is returned.
	 *
	 * @param string the {@link String} to parse.
	 * @return The {@link CoverSupplier} or {@code null}.
	 */
	@Nullable
	public static CoverSupplier typeOf(@Nullable String string) {
		if (string == null) {
			return null;
		}
		switch (string.trim().toLowerCase(Locale.ROOT)) {
			case "":
			case "none":
				return NONE;
			case "coverartarchive":
			case "coverartarchive.org":
			case "cover art archive":
				return COVER_ART_ARCHIVE;
			default:
				return null;
		}
	}

	/**
	 * Returns the {@link CoverSupplier} with the specified ordinal value.
	 *
	 * @param ordinal the ordinal value.
	 * @return The {@link CoverSupplier} or {@code null}.
	 */
	@Nullable
	public static CoverSupplier typeOf(int ordinal) {
		return typeOf(ordinal, null);
	}

	/**
	 * Returns the {@link CoverSupplier} with the specified ordinal value. If no
	 * match can be found, the specified default {@link CoverSupplier} is
	 * returned.
	 *
	 * @param ordinal the ordinal value.
	 * @param defaultSupplier the default {@link CoverSupplier}.
	 * @return The {@link CoverSupplier} or {@code null}.
	 */
	@Nullable
	public static CoverSupplier typeOf(int ordinal, @Nullable CoverSupplier defaultSupplier) {
		for (CoverSupplier supplier : values()) {
			if (supplier.ordinal() == ordinal) {
				return supplier;
			}
		}
		return defaultSupplier;
	}
}
