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
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.pms.dlna.DLNABinaryThumbnail;
import net.pms.util.StringUtil;


/**
 * This class represents a cover or album art as stored in the database.
 *
 * @author Nadahar
 */
@Immutable
public class Cover {

	private final byte[] imageData;
	private final DLNABinaryThumbnail thumbnail;
	private final long expires;

	/**
	 * Creates a new instance using the specified parameters.
	 *
	 * @param imageData the image data for the cover itself.
	 * @param thumbnail the {@link DLNABinaryThumbnail} representing the cover.
	 * @param expires the expiration time in milliseconds since midnight,
	 *            January 1, 1970.
	 */
	@SuppressFBWarnings("EI_EXPOSE_REP2")
	public Cover(@Nullable byte[] imageData, @Nullable DLNABinaryThumbnail thumbnail, long expires) {
		this.imageData = imageData;
		this.thumbnail = thumbnail;
		this.expires = expires;
	}

	/**
	 * @return The image data for the cover or {@code null}.
	 */
	@Nullable
	@SuppressFBWarnings("EI_EXPOSE_REP")
	public byte[] getBytes() {
		return imageData;
	}

	/**
	 * @return The {@link DLNABinaryThumbnail} or {@code null}.
	 */
	@Nullable
	public DLNABinaryThumbnail getThumbnail() {
		return thumbnail;
	}

	/**
	 * @return The expiration time in milliseconds since midnight, January 1,
	 *         1970.
	 */
	public long getExpires() {
		return expires;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Image=");
		if (imageData == null) {
			sb.append("null");
		} else {
			sb.append(imageData.length).append(" bytes");
		}
		sb.append(", Thumbnail=").append(thumbnail == null ? "null" : thumbnail)
			.append(", Expires=").append(StringUtil.formatDateTime(expires))
			.append(System.currentTimeMillis() >= expires ? ", Expired=" : ", Expires=")
			.append(StringUtil.formatDateTime(expires));
		return sb.toString();
	}
}
