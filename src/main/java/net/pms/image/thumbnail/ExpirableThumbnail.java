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

import java.lang.ref.WeakReference;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import net.pms.dlna.DLNABinaryThumbnail;
import net.pms.dlna.DLNAThumbnail;
import net.pms.util.Expirable.BasicExpirable;
import net.pms.util.StringUtil;


/**
 * An abstract class for {@link DLNAThumbnail}s that can expire.
 *
 * @author Nadahar
 */
@Immutable
public abstract class ExpirableThumbnail extends BasicExpirable {

	/**
	 * Creates a new instance with the specified expiration time.
	 *
	 * @param expires the expiration time in milliseconds since midnight,
	 *            January 1, 1970.
	 */
	protected ExpirableThumbnail(long expires) {
		super(expires);
	}

	/**
	 * @return The {@link DLNAThumbnail} or {@code null}.
	 */
	@Nullable
	public abstract DLNAThumbnail getThumbnail();

	/**
	 * A class for {@link DLNABinaryThumbnail}s that can expire.
	 *
	 * @author Nadahar
	 */
	public static class ExpirableBinaryThumbnail extends ExpirableThumbnail {

		@Nullable
		private final DLNABinaryThumbnail thumbnail;

		/**
		 * Creates a new instance using the specified parameters.
		 *
		 * @param thumbnail the {@link DLNABinaryThumbnail} or {@code null}.
		 * @param expires the expiration time in milliseconds since midnight,
		 *            January 1, 1970.
		 */
		public ExpirableBinaryThumbnail(@Nullable DLNABinaryThumbnail thumbnail, long expires) {
			super(expires);
			this.thumbnail = thumbnail;
		}

		/**
		 * Creates a new instance with no {@link DLNABinaryThumbnail} that
		 * expires at the specified expiration time.
		 *
		 * @param expires the expiration time in milliseconds since midnight,
		 *            January 1, 1970.
		 */
		public ExpirableBinaryThumbnail(long expires) {
			super(expires);
			thumbnail = null;
		}

		@Override
		@Nullable
		public DLNABinaryThumbnail getThumbnail() {
			return thumbnail;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(", Thumbnail=").append(thumbnail == null ? "null" : thumbnail)
				.append(isExpired() ? ", Expired=" : ", Expires=")
				.append(StringUtil.formatDateTime(expires));
			return sb.toString();
		}

	}

	/**
	 * A class for weakly referenced {@link DLNABinaryThumbnail}s that can
	 * expire.
	 *
	 * @author Nadahar
	 */
	@Immutable
	public static class CachedThumbnail extends ExpirableThumbnail {
		private final WeakReference<DLNABinaryThumbnail> thumbnailReference;

		/**
		 * Creates a new instance using the specified parameters.
		 *
		 * @param thumbnail the {@link DLNABinaryThumbnail} or {@code null}.
		 * @param expires the expiration time in milliseconds since midnight,
		 *            January 1, 1970.
		 */
		public CachedThumbnail(@Nullable DLNABinaryThumbnail thumbnail, long expires) {
			super(expires);
			thumbnailReference = thumbnail == null ? null : new WeakReference<>(thumbnail);
		}

		/**
		 * Creates a new instance using the information from the specified
		 * {@link ExpirableBinaryThumbnail}.
		 *
		 * @param expirableBinaryThumbnail the {@link ExpirableBinaryThumbnail}.
		 * @throws NullPointerException if {@link ExpirableBinaryThumbnail} is
		 *             {@code null}.
		 */
		public CachedThumbnail(@Nonnull ExpirableBinaryThumbnail expirableBinaryThumbnail) {
			super(expirableBinaryThumbnail.getExpirationTime());
			DLNABinaryThumbnail thumbnail = expirableBinaryThumbnail.getThumbnail();
			thumbnailReference = thumbnail == null ? null : new WeakReference<>(thumbnail);
		}

		/**
		 * Creates a new instance without a referenced
		 * {@link DLNABinaryThumbnail} that expires at the specified expiration
		 * time.
		 *
		 * @param expires the expiration time in milliseconds since midnight,
		 *            January 1, 1970.
		 */
		public CachedThumbnail(long expires) {
			super(expires);
			thumbnailReference = null;
		}

		/**
		 * @return {@code true} if this instance can be disposed of, that is
		 *         that the referenced {@link DLNABinaryThumbnail} has either
		 *         been garbage collected or that it has no referenced
		 *         {@link DLNABinaryThumbnail} and the expiration time has
		 *         passed. Otherwise, {@code false} is returned.
		 */
		public boolean isDisposable() {
			return
				(
					thumbnailReference != null && thumbnailReference.get() == null
				) ||
				(
					thumbnailReference == null && isExpired()
				);
		}

		@Override
		@Nullable
		public DLNABinaryThumbnail getThumbnail() {
			return thumbnailReference == null ? null : thumbnailReference.get();
		}

		/**
		 * Creates a new {@link ExpirableBinaryThumbnail} using the information
		 * from this instance.
		 *
		 * @return The new {@link ExpirableBinaryThumbnail}.
		 */
		@Nonnull
		public ExpirableBinaryThumbnail toExpirableBinaryThumbnail() {
			return new ExpirableBinaryThumbnail(thumbnailReference == null ? null : thumbnailReference.get(), expires);
		}
	}
}
