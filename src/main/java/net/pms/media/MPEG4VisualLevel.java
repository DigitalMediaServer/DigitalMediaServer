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
 * This represent the predefined MPEG-4 Visual levels.
 *
 * @author Nadahar
 */
public enum MPEG4VisualLevel implements VideoLevel {

	/** Level 1 */
	L1,

	/** Level 2 */
	L2,

	/** Level 3 */
	L3,

	/** Level 4 */
	L4;

	@Override
	public boolean isGreaterThanOrEqualTo(@Nullable VideoLevel other) {
		return other instanceof MPEG4VisualLevel ? compareTo((MPEG4VisualLevel) other) >= 0 : false;
	}

	@Override
	public boolean isGreaterThan(@Nullable VideoLevel other) {
		return other instanceof MPEG4VisualLevel ? compareTo((MPEG4VisualLevel) other) > 0 : false;
	}

	@Override
	public boolean isLessThanOrEqualTo(@Nullable VideoLevel other) {
		return other instanceof MPEG4VisualLevel ? compareTo((MPEG4VisualLevel) other) <= 0 : false;
	}

	@Override
	public boolean isLessThan(@Nullable VideoLevel other) {
		return other instanceof MPEG4VisualLevel ? compareTo((MPEG4VisualLevel) other) < 0 : false;
	}

	/**
	 * Tries to convert {@code value} into a {@link MPEG4VisualLevel}. Returns
	 * {@code null} if the conversion fails.
	 *
	 * @param value the {@link String} describing a MPEG-4 Visual level.
	 * @return The {@link MPEG4VisualLevel} corresponding to {@code value} or
	 *         {@code null}.
	 */
	@Nullable
	public static MPEG4VisualLevel typeOf(@Nullable String value) {
		if (isBlank(value)) {
			return null;
		}

		value = value.trim();
		switch (value) {
			case "1":
			case "1.0":
				return L1;
			case "2":
			case "2.0":
				return L2;
			case "3":
			case "3.0":
				return L3;
			case "4":
			case "4.0":
				return L4;
			default:
				return null;
		}
	}

	/**
	 * Tries to convert {@code value} into a {@link MPEG4VisualLevel}. Returns
	 * {@code null} if the conversion fails.
	 *
	 * @param value the {@code int} describing a MPEG-4 Visual level in either
	 *            one or two digit notation.
	 * @return The {@link MPEG4VisualLevel} corresponding to {@code value} or
	 *         {@code null}.
	 */
	@Nullable
	public static MPEG4VisualLevel typeOf(int value) {
		switch (value) {
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
			case 40:
				return L4;
			default:
				return null;
		}
	}

	@Override
	public String toString() {
		switch (this) {
			case L1:
				return "Level 1";
			case L2:
				return "Level 2";
			case L3:
				return "Level 3";
			case L4:
				return "Level 4";
			default:
				throw new IllegalStateException("Unimplemented enum value: " + super.toString());
		}
	}
}
