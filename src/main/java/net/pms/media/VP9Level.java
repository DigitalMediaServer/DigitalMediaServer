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
import javax.annotation.Nullable;


/**
 * This represent the predefined VP9 levels.
 *
 * @author Nadahar
 */
public enum VP9Level implements VideoLevel {

	/** Level 1 */
	L1,

	/** Level 1.1 */
	L1_1,

	/** Level 2 */
	L2,

	/** Level 2.1 */
	L2_1,

	/** Level 3 */
	L3,

	/** Level 3.1 */
	L3_1,

	/** Level 4 */
	L4,

	/** Level 4.1 */
	L4_1,

	/** Level 5 */
	L5,

	/** Level 5.1 */
	L5_1,

	/** Level 5.2 */
	L5_2,

	/** Level 6 */
	L6,

	/** Level 6.1 */
	L6_1,

	/** Level 6.2 */
	L6_2;

	@Override
	public boolean isGreaterThanOrEqualTo(@Nullable VideoLevel other) {
		return other instanceof VP9Level ? compareTo((VP9Level) other) >= 0 : false;
	}

	@Override
	public boolean isGreaterThan(@Nullable VideoLevel other) {
		return other instanceof VP9Level ? compareTo((VP9Level) other) > 0 : false;
	}

	@Override
	public boolean isLessThanOrEqualTo(@Nullable VideoLevel other) {
		return other instanceof VP9Level ? compareTo((VP9Level) other) <= 0 : false;
	}

	@Override
	public boolean isLessThan(@Nullable VideoLevel other) {
		return other instanceof VP9Level ? compareTo((VP9Level) other) < 0 : false;
	}

	/**
	 * Tries to convert {@code value} into a {@link VP9Level}. Returns
	 * {@code null} if the conversion fails.
	 *
	 * @param value the {@link String} describing a VP9 level.
	 * @return The {@link VP9Level} corresponding to {@code value} or
	 *         {@code null}.
	 */
	@Nullable
	public static VP9Level typeOf(@Nullable String value) {
		if (isBlank(value)) {
			return null;
		}

		value = value.replaceAll(",", "\\.").trim();
		switch (value) {
			case "1":
			case "1.0":
				return L1;
			case "1.1":
				return L1_1;
			case "2":
			case "2.0":
				return L2;
			case "2.1":
				return L2_1;
			case "3":
			case "3.0":
				return L3;
			case "3.1":
				return L3_1;
			case "4":
			case "4.0":
				return L4;
			case "4.1":
				return L4_1;
			case "5":
			case "5.0":
				return L5;
			case "5.1":
				return L5_1;
			case "5.2":
				return L5_2;
			case "6":
			case "6.0":
				return L6;
			case "6.1":
				return L6_1;
			case "6.2":
				return L6_2;
			default:
				return null;
		}
	}

	/**
	 * Tries to convert {@code value} into an {@link VP9Level}. Returns
	 * {@code null} if the conversion fails.
	 *
	 * @param value the {@code int} describing an VP9 level in either one or two
	 *            digit notation.
	 * @return The {@link VP9Level} corresponding to {@code value} or
	 *         {@code null}.
	 */
	@Nullable
	public static VP9Level typeOf(int value) {
		switch (value) {
			case 1:
			case 10:
				return L1;
			case 11:
				return L1_1;
			case 2:
			case 20:
				return L2;
			case 21:
				return L2_1;
			case 22:
			case 3:
			case 30:
				return L3;
			case 31:
				return L3_1;
			case 4:
			case 40:
				return L4;
			case 41:
				return L4_1;
			case 5:
			case 50:
				return L5;
			case 51:
				return L5_1;
			case 52:
				return L5_2;
			case 6:
			case 60:
				return L6;
			case 61:
				return L6_1;
			case 62:
				return L6_2;
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
		StringBuilder sb = full ? new StringBuilder(9).append("Level ") : new StringBuilder(3);
		switch (this) {
			case L1:
				return sb.append("1").toString();
			case L1_1:
				return sb.append("1.1").toString();
			case L2:
				return sb.append("2").toString();
			case L2_1:
				return sb.append("2.1").toString();
			case L3:
				return sb.append("3").toString();
			case L3_1:
				return sb.append("3.1").toString();
			case L4:
				return sb.append("4").toString();
			case L4_1:
				return sb.append("4.1").toString();
			case L5:
				return sb.append("5").toString();
			case L5_1:
				return sb.append("5.1").toString();
			case L5_2:
				return sb.append("5.2").toString();
			case L6:
				return sb.append("6").toString();
			case L6_1:
				return sb.append("6.1").toString();
			case L6_2:
				return sb.append("6.2").toString();
			default:
				throw new IllegalStateException("Unimplemented enum value: " + super.toString());
		}
	}
}
