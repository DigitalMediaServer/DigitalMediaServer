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
 * This represent the predefined AV1 levels.
 *
 * @author Nadahar
 */
public enum AV1Level implements VideoLevel {

	/** Level 2.0 */
	L2,

	/** Level 2.1 */
	L2_1,

	/** Level 2.2 */
	L2_2,

	/** Level 2.3 */
	L2_3,

	/** Level 3.0 */
	L3,

	/** Level 3.1 */
	L3_1,

	/** Level 3.2 */
	L3_2,

	/** Level 3.3 */
	L3_3,

	/** Level 4.0 */
	L4,

	/** Level 4.1 */
	L4_1,

	/** Level 4.2 */
	L4_2,

	/** Level 4.3 */
	L4_3,

	/** Level 5.0 */
	L5,

	/** Level 5.1 */
	L5_1,

	/** Level 5.2 */
	L5_2,

	/** Level 5.3 */
	L5_3,

	/** Level 6.0 */
	L6,

	/** Level 6.1 */
	L6_1,

	/** Level 6.2 */
	L6_2,

	/** Level 6.3 */
	L6_3,

	/** Level 7.0 */
	L7,

	/** Level 7.1 */
	L7_1,

	/** Level 7.2 */
	L7_2,

	/** Level 7.3 */
	L7_3;

	@Override
	public boolean isGreaterThanOrEqualTo(@Nullable VideoLevel other) {
		return other instanceof AV1Level ? compareTo((AV1Level) other) >= 0 : false;
	}

	@Override
	public boolean isGreaterThan(@Nullable VideoLevel other) {
		return other instanceof AV1Level ? compareTo((AV1Level) other) > 0 : false;
	}

	@Override
	public boolean isLessThanOrEqualTo(@Nullable VideoLevel other) {
		return other instanceof AV1Level ? compareTo((AV1Level) other) <= 0 : false;
	}

	@Override
	public boolean isLessThan(@Nullable VideoLevel other) {
		return other instanceof AV1Level ? compareTo((AV1Level) other) < 0 : false;
	}

	/**
	 * Tries to convert {@code value} into a {@link AV1Level}. Returns
	 * {@code null} if the conversion fails.
	 *
	 * @param value the {@link String} describing a AV1 level.
	 * @return The {@link AV1Level} corresponding to {@code value} or
	 *         {@code null}.
	 */
	@Nullable
	public static AV1Level typeOf(@Nullable String value) {
		if (isBlank(value)) {
			return null;
		}

		value = value.trim();
		switch (value) {
			case "2":
			case "2.0":
				return L2;
			case "2.1":
				return L2_1;
			case "2.2":
				return L2_2;
			case "2.3":
				return L2_3;
			case "3":
			case "3.0":
				return L3;
			case "3.1":
				return L3_1;
			case "3.2":
				return L3_2;
			case "3.3":
				return L3_3;
			case "4":
			case "4.0":
				return L4;
			case "4.1":
				return L4_1;
			case "4.2":
				return L4_2;
			case "4.3":
				return L4_3;
			case "5":
			case "5.0":
				return L5;
			case "5.1":
				return L5_1;
			case "5.2":
				return L5_2;
			case "5.3":
				return L5_3;
			case "6":
			case "6.0":
				return L6;
			case "6.1":
				return L6_1;
			case "6.2":
				return L6_2;
			case "6.3":
				return L6_3;
			case "7":
			case "7.0":
				return L7;
			case "7.1":
				return L7_1;
			case "7.2":
				return L7_2;
			case "7.3":
				return L7_3;
			default:
				return null;
		}
	}

	/**
	 * Tries to convert {@code value} into an {@link AV1Level}. Returns
	 * {@code null} if the conversion fails.
	 *
	 * @param value the {@code int} describing an AV1 level in either one or two
	 *            digit notation.
	 * @return The {@link AV1Level} corresponding to {@code value} or
	 *         {@code null}.
	 */
	@Nullable
	public static AV1Level typeOf(int value) {
		switch (value) {
			case 2:
			case 20:
				return L2;
			case 21:
				return L2_1;
			case 22:
				return L2_2;
			case 23:
				return L2_3;
			case 3:
			case 30:
				return L3;
			case 31:
				return L3_1;
			case 32:
				return L3_2;
			case 33:
				return L3_3;
			case 4:
			case 40:
				return L4;
			case 41:
				return L4_1;
			case 42:
				return L4_2;
			case 43:
				return L4_3;
			case 5:
			case 50:
				return L5;
			case 51:
				return L5_1;
			case 52:
				return L5_2;
			case 53:
				return L5_3;
			case 6:
			case 60:
				return L6;
			case 61:
				return L6_1;
			case 62:
				return L6_2;
			case 63:
				return L6_3;
			case 7:
			case 70:
				return L7;
			case 71:
				return L7_1;
			case 72:
				return L7_2;
			case 73:
				return L7_3;
			default:
				return null;
		}
	}

	@Override
	public String toString() {
		switch (this) {
			case L2:
				return "Level 2.0";
			case L2_1:
				return "Level 2.1";
			case L2_2:
				return "Level 2.2";
			case L2_3:
				return "Level 2.3";
			case L3:
				return "Level 3.0";
			case L3_1:
				return "Level 3.1";
			case L3_2:
				return "Level 3.2";
			case L3_3:
				return "Level 3.3";
			case L4:
				return "Level 4.0";
			case L4_1:
				return "Level 4.1";
			case L4_2:
				return "Level 4.2";
			case L4_3:
				return "Level 4.3";
			case L5:
				return "Level 5.0";
			case L5_1:
				return "Level 5.1";
			case L5_2:
				return "Level 5.2";
			case L5_3:
				return "Level 5.3";
			case L6:
				return "Level 6.0";
			case L6_1:
				return "Level 6.1";
			case L6_2:
				return "Level 6.2";
			case L6_3:
				return "Level 6.3";
			case L7:
				return "Level 7.0";
			case L7_1:
				return "Level 7.1";
			case L7_2:
				return "Level 7.2";
			case L7_3:
				return "Level 7.3";
			default:
				throw new IllegalStateException("Unimplemented enum value: " + super.toString());
		}
	}
}
