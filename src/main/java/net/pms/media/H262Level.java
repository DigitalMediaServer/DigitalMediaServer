/*
 * Digital Media Server, for streaming digital media to DLNA compatible devices
 * based on www.ps3mediaserver.org and www.universalmediaserver.com. Copyright
 * (C) 2016 Digital Media Server developers.
 *
 * This program is a free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; version 2 of the License only.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package net.pms.media;

import static org.apache.commons.lang3.StringUtils.isBlank;
import java.util.Locale;
import javax.annotation.Nullable;


/**
 * This represent the predefined H.262 levels.
 *
 * @author Nadahar
 */
public enum H262Level implements VideoLevel {

	/** Low Level */
	LL,

	/** Main Level */
	ML,

	/** High 1440 */
	H_14,

	/** High Level */
	HL;

	@Override
	public boolean isGreaterThanOrEqualTo(@Nullable VideoLevel other) {
		return other instanceof H262Level ? compareTo((H262Level) other) >= 0 : false;
	}

	@Override
	public boolean isGreaterThan(@Nullable VideoLevel other) {
		return other instanceof H262Level ? compareTo((H262Level) other) > 0 : false;
	}

	@Override
	public boolean isLessThanOrEqualTo(@Nullable VideoLevel other) {
		return other instanceof H262Level ? compareTo((H262Level) other) <= 0 : false;
	}

	@Override
	public boolean isLessThan(@Nullable VideoLevel other) {
		return other instanceof H262Level ? compareTo((H262Level) other) < 0 : false;
	}

	/**
	 * Tries to convert {@code value} into an {@link H262Level}. Returns
	 * {@code null} if the conversion fails.
	 *
	 * @param value the {@link String} describing an H.262 level.
	 * @return The {@link H262Level} corresponding to {@code value} or
	 *         {@code null}.
	 */
	@Nullable
	public static H262Level typeOf(@Nullable String value) {
		if (isBlank(value)) {
			return null;
		}
		value = value.trim().toUpperCase(Locale.ROOT);
		if ("H-14".equals(value) || "H14".equals(value)) {
			return H_14;
		}
		for (H262Level level : values()) {
			if (level.name().equals(value)) {
				return level;
			}
		}
		return null;
	}

	@Override
	public String toString() {
		switch (this) {
			case LL:
				return "Low Level";
			case ML:
				return "Main Level";
			case H_14:
				return "High 1440";
			case HL:
				return "High Level";
			default:
				throw new IllegalStateException("Unimplemented enum value: " + super.toString());
		}
	}
}
