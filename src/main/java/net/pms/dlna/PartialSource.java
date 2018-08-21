/*
 * Digital Media Server, for streaming digital media to UPnP AV or DLNA
 * compatible devices based on PS3 Media Server and Universal Media Server.
 * Copyright (C) 2018 Digital Media Server developers.
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


/**
 * This interface represents and {@link DLNAResource} that is restricted to only
 * a part of the original source.
 *
 * @author Nadahar
 */
public interface PartialSource {

	/**
	 * @return The start time of the clip in seconds.
	 */
	public double getClipStart();

	/**
	 * @return The end time of the clip in seconds or
	 *         {@link Double#POSITIVE_INFINITY}.
	 */
	public double getClipEnd();

	/**
	 * @return {@code true} if this only represents a part of the source,
	 *         {@code false} otherwise.
	 */
	public boolean isPartialSource();
}
