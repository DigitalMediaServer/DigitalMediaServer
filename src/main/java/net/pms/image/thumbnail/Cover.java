package net.pms.image.thumbnail;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import net.pms.dlna.DLNABinaryThumbnail;
import net.pms.util.StringUtil;


@Immutable
public class Cover {

	private final byte[] imageData;
	private final DLNABinaryThumbnail thumbnail;
	private final long expires;

	public Cover(@Nullable byte[] imageData, @Nullable DLNABinaryThumbnail thumbnail, long expires) {
		this.imageData = imageData;
		this.thumbnail = thumbnail;
		this.expires = expires;
	}

	@Nullable
	public byte[] getBytes() {
		return imageData;
	}

	@Nullable
	public DLNABinaryThumbnail getThumbnail() {
		return thumbnail;
	}

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
