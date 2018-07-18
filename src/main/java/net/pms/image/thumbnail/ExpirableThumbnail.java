package net.pms.image.thumbnail;

import java.lang.ref.WeakReference;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import net.pms.dlna.DLNABinaryThumbnail;
import net.pms.dlna.DLNAThumbnail;
import net.pms.util.Expirable.AbstractExpirable;
import net.pms.util.StringUtil;


@Immutable
public abstract class ExpirableThumbnail extends AbstractExpirable {

	protected ExpirableThumbnail(long expires) {
		super(expires);
	}

	public abstract DLNAThumbnail getThumbnail();

	public static class ExpirableBinaryThumbnail extends ExpirableThumbnail {
		private final DLNABinaryThumbnail thumbnail;

		public ExpirableBinaryThumbnail(DLNABinaryThumbnail thumbnail, long expires) {
			super(expires);
			this.thumbnail = thumbnail;
		}

		public ExpirableBinaryThumbnail(long expires) {
			super(expires);
			thumbnail = null;
		}

		@Override
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

	@Immutable
	public static class CachedThumbnail extends ExpirableThumbnail {
		private final WeakReference<DLNABinaryThumbnail> thumbnailReference;

		public CachedThumbnail(DLNABinaryThumbnail thumbnail, long expires) {
			super(expires);
			thumbnailReference = new WeakReference<>(thumbnail);
		}

		public CachedThumbnail(@Nonnull ExpirableBinaryThumbnail expirableBinaryThumbnail) {
			super(expirableBinaryThumbnail.getExpiryTime());
			thumbnailReference = new WeakReference<>(expirableBinaryThumbnail.getThumbnail());
		}

		public CachedThumbnail(long expires) {
			super(expires);
			thumbnailReference = null;
		}

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
		public DLNABinaryThumbnail getThumbnail() {
			return thumbnailReference == null ? null : thumbnailReference.get();
		}

		public ExpirableBinaryThumbnail toExpirableBinaryThumbnail() {
			return new ExpirableBinaryThumbnail(thumbnailReference == null ? null : thumbnailReference.get(), expires);
		}
	}
}
