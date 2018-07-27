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

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;


/**
 * This interface defines a container to hold information used to look up
 * information about a music track. Implementations must be immutable.
 */
@Immutable
public interface AudioTagInfo {

	/**
	 * @return {@code true} if this {@link AudioTagInfo} has any information,
	 *         {@code false} if it is "blank".
	 */
	public boolean hasInfo();

	/**
	 * @return {@code true} if this {@link AudioTagInfo} has any useful
	 *         information for identifying the music track, {@code false}
	 *         otherwise.
	 */
	public boolean hasUsefulInfo();

	/**
	 * @return The album name or {@code null}.
	 */
	@Nullable
	public String getAlbum();

	/**
	 * @return The artist name or {@code null}.
	 */
	@Nullable
	public String getArtist();

	/**
	 * @return The track title or {@code null}.
	 */
	@Nullable
	public String getTitle();

	/**
	 * @return The release year or {@code -1}.
	 */
	public int getYear();

	/**
	 * @return The track number or {@code -1}.
	 */
	public int getTrackNumber();

	/**
	 * @return The total number of tracks or {@code -1}.
	 */
	public int getNumberOfTracks();
}
