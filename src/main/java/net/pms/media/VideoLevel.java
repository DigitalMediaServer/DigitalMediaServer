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

import javax.annotation.Nullable;

/**
 * This interface represents predefined video codec profile levels.
 *
 * @author Nadahar
 */
public interface VideoLevel {

	/**
	 * @param other the {@link VideoLevel} to compare to.
	 * @return {@code true} if this level is of the same type as and is equal to
	 *         or greater than ({@code >=}) {@code other}, {@code false}
	 *         otherwise.
	 */
	public boolean isGreaterThanOrEqualTo(@Nullable VideoLevel other);

	/**
	 * @param other the {@link VideoLevel} to compare to.
	 * @return {@code true} if this level is of the same type as and is greater
	 *         than ({@code >}) {@code other}, {@code false} otherwise.
	 */
	public boolean isGreaterThan(@Nullable VideoLevel other);

	/**
	 * @param other the {@link VideoLevel} to compare to.
	 * @return {@code true} if this level is of the same type as and is equal to
	 *         or less than ({@code <=}) {@code other}, {@code false} otherwise.
	 */
	public boolean isLessThanOrEqualTo(@Nullable VideoLevel other);

	/**
	 * @param other the {@link VideoLevel} to compare to.
	 * @return {@code true} if this level is of the same type as and is less
	 *         than ({@code <}) {@code other}, {@code false} otherwise.
	 */
	public boolean isLessThan(@Nullable VideoLevel other);

	/**
	 * Returns the {@link String} representation of this {@link VideoLevel}.
	 * <p>
	 * <b>Note: </b>This interface requires that {@link #toString()} produce the
	 * same output as {@link #toString(boolean)} when {@code full} is
	 * {@code true}.
	 *
	 * @param full if {@code true}, a human readable string is returned. If
	 *            {@code false}, a short form/code is returned.
	 * @return The {@link String} representation.
	 */
	public String toString(boolean full);
}
