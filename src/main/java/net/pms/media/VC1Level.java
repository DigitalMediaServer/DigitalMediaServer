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
 * This represent the predefined SMPTE 421M/VC-1 levels.
 *
 * @author Nadahar
 */
public enum VC1Level implements VideoLevel {

	/** Low Level */
	LOW,

	/** Level 0 */
	L0,

	/** Medium Level */
	MEDIUM,

	/** Level 1 */
	L1,

	/** High Level */
	HIGH,

	/** Level 2 */
	L2,

	/** Level 3 */
	L3,

	/** Level 4 */
	L4;

	@Override
	public boolean isGreaterThanOrEqualTo(@Nullable VideoLevel other) {
		return other instanceof VC1Level ? compareTo((VC1Level) other) >= 0 : false;
	}

	@Override
	public boolean isGreaterThan(@Nullable VideoLevel other) {
		return other instanceof VC1Level ? compareTo((VC1Level) other) > 0 : false;
	}

	@Override
	public boolean isLessThanOrEqualTo(@Nullable VideoLevel other) {
		return other instanceof VC1Level ? compareTo((VC1Level) other) <= 0 : false;
	}

	@Override
	public boolean isLessThan(@Nullable VideoLevel other) {
		return other instanceof VC1Level ? compareTo((VC1Level) other) < 0 : false;
	}

	/**
	 * Tries to convert {@code value} into a {@link VC1Level}. Returns
	 * {@code null} if the conversion fails.
	 *
	 * @param value the {@link String} describing a SMPTE 421M/VC-1 level.
	 * @return The {@link VC1Level} corresponding to {@code value} or
	 *         {@code null}.
	 */
	@Nullable
	public static VC1Level typeOf(@Nullable String value) {
		if (isBlank(value)) {
			return null;
		}

		value = value.replaceAll(",", "\\.").trim().toUpperCase(Locale.ROOT);
		switch (value) {
			case "L":
			case "LL":
				return LOW;
			case "0":
			case "L0":
				return L0;
			case "M":
			case "ML":
				return MEDIUM;
			case "1":
			case "L1":
			case "1.0":
			case "L1.0":
				return L1;
			case "H":
			case "HL":
				return HIGH;
			case "2":
			case "L2":
			case "2.0":
			case "L2.0":
				return L2;
			case "3":
			case "L3":
			case "3.0":
			case "L3.0":
				return L3;
			case "4":
			case "L4":
			case "4.0":
			case "L4.0":
				return L4;
			default:
				for (VC1Level level : values()) {
					if (value.equals(level.name()) || value.equals(level.name() + " LEVEL")) {
						return level;
					}
				}

				return null;
		}
	}

	/**
	 * Tries to convert {@code value} into a {@link VC1Level}. Returns
	 * {@code null} if the conversion fails.
	 *
	 * @param value the {@code int} describing a SMPTE 421M/VC-1 level in either
	 *            one or two digit notation.
	 * @return The {@link VC1Level} corresponding to {@code value} or
	 *         {@code null}.
	 */
	@Nullable
	public static VC1Level typeOf(int value) {
		switch (value) {
			case 0:
				return L0;
			case 1:
			case 10:
				return L1;
			case 2:
			case 20:
				return L2;
			case 3:
			case 30:
				return L3;
			case 4:
				return L4;
			default:
				return null;
		}
	}

	@Override
	public String toString() {
		return toString(true);
	}

	@Override
	public String toString(boolean full) {
		switch (this) {
			case LOW:
				return full ? "Low Level" : "LL";
			case L0:
				return full ? "Level 0" : "0";
			case MEDIUM:
				return full ? "Medium Level" : "ML";
			case L1:
				return full ? "Level 1" : "1";
			case HIGH:
				return full ? "High Level" : "HL";
			case L2:
				return full ? "Level 2" : "2";
			case L3:
				return full ? "Level 3" : "3";
			case L4:
				return full ? "Level 4" : "4";
			default:
				throw new IllegalStateException("Unimplemented enum value: " + super.toString());
		}
	}
}
