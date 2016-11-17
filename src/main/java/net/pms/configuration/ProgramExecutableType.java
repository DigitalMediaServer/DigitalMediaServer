/*
 * Digital Media Server, for streaming digital media to DLNA compatible devices
 * based on www.ps3mediaserver.org and www.universalmediaserver.com.
 * Copyright (C) 2016 Digital Media Server developers.
 *
 * This program is a free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2
 * of the License only.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package net.pms.configuration;

import static org.apache.commons.lang3.StringUtils.isBlank;
import java.util.Locale;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;


/**
 * Defines executable types for external programs.
 *
 * @author Nadahar
 */
public enum ProgramExecutableType {

	/** An executable bundled with DMS. */
	BUNDLED,

	/** An executable reachable via the OS path. */
	INSTALLED,

	/** A configured/custom executable path. */
	CUSTOM;

	/**
	 * Returns the string representation of this {@link ProgramExecutableType}.
	 */
	@Override
	public String toString() {
		switch (this) {
			case BUNDLED:
				return "Bundled";
			case CUSTOM:
				return "Custom";
			case INSTALLED:
				return "Installed";
			default:
				throw new IllegalStateException("ProgramExecutableType " + super.toString() + "isn't implemented");
		}
	}

	/**
	 * Converts the string passed as an argument to a
	 * {@link ProgramExecutableType}. If the conversion fails, the
	 * {@code defaultExecutableType} is returned.
	 *
	 * @param executableType the {@link String} to convert.
	 * @param defaultExecutableType the default to return if the conversion
	 *            fails.
	 * @return The corresponding {@link ProgramExecutableType} or {@code null}.
	 */
	@Nonnull
	public static ProgramExecutableType toProgramExecutableType(
		@Nullable String executableType,
		@Nonnull ProgramExecutableType defaultExecutableType
	) {
		if (defaultExecutableType == null) {
			throw new IllegalArgumentException("defaultExecutableType cannot be null");
		}
		ProgramExecutableType result = toProgramExecutableType(executableType);
		return result != null ? result : defaultExecutableType;
	}

	/**
	 * Converts the string passed as an argument to a
	 * {@link ProgramExecutableType}. If the conversion fails, {@code null} is
	 * returned.
	 *
	 * @param executableType the {@link String} to convert.
	 * @return The corresponding {@link ProgramExecutableType} or {@code null}.
	 */
	@Nullable
	public static ProgramExecutableType toProgramExecutableType(String executableType) {
		if (isBlank(executableType)) {
			return null;
		}
		switch (executableType.toLowerCase(Locale.ROOT)) {
			case "bundled":
				return BUNDLED;
			case "installed":
				return INSTALLED;
			case "custom":
				return CUSTOM;
			default:
				return null;
		}
	}
}
