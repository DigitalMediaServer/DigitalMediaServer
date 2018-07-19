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
package net.pms.dlna;

import static org.apache.commons.lang3.StringUtils.isBlank;
import java.awt.color.ColorSpace;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.pms.PMS;
import net.pms.image.ColorSpaceType;
import net.pms.image.ImageFormat;
import net.pms.image.ImageInfo;
import net.pms.image.thumbnail.ExpirableThumbnail.ExpirableBinaryThumbnail;
import net.pms.util.CoverArtArchiveUtil;
import net.pms.util.CoverSupplier;
import net.pms.util.CoverUtil;


public class CoverArtAchiveThumbnail implements DLNAThumbnail {

	private static final long serialVersionUID = 1L;

	/** The {@code MBID} */
	@Nonnull
	@GuardedBy("this")
	protected String mbid;

	@SuppressFBWarnings("SE_TRANSIENT_FIELD_NOT_RESTORED")
	@GuardedBy("this")
	protected transient boolean updated;

	/** The {@link DLNABinaryThumbnail} instance */
	@Nullable
	@GuardedBy("this")
	protected transient ExpirableBinaryThumbnail delegate;

	public CoverArtAchiveThumbnail(@Nonnull String mbid, @Nonnull ExpirableBinaryThumbnail delegate) {
		if (isBlank(mbid)) {
			throw new IllegalArgumentException("mbid cannot be blank");
		}
		if (delegate == null) {
			throw new IllegalArgumentException("delegate cannot be null");
		}
		this.mbid = mbid.intern();
		this.delegate = delegate;
		this.updated = true;
	}

	@Override
	public synchronized DLNAImageProfile getDLNAImageProfile() {
		update();
		return delegate != null && delegate.getThumbnail() != null ? delegate.getThumbnail().getDLNAImageProfile() : null;
	}

	@Override
	public synchronized byte[] getBytes(boolean copy) {
		update();
		return delegate != null && delegate.getThumbnail() != null ? delegate.getThumbnail().getBytes(copy) : null;
	}

	@Override
	public synchronized ImageInfo getImageInfo() {
		update();
		return delegate != null && delegate.getThumbnail() != null ? delegate.getThumbnail().getImageInfo() : null;
	}

	@Override
	public synchronized int getWidth() {
		update();
		return delegate != null && delegate.getThumbnail() != null ? delegate.getThumbnail().getWidth() : -1;
	}

	@Override
	public synchronized int getHeight() {
		update();
		return delegate != null && delegate.getThumbnail() != null ? delegate.getThumbnail().getHeight() : -1;
	}

	@Override
	public synchronized ImageFormat getFormat() {
		update();
		return delegate != null && delegate.getThumbnail() != null ? delegate.getThumbnail().getFormat() : null;
	}

	@Override
	public synchronized ColorSpace getColorSpace() {
		update();
		return delegate != null && delegate.getThumbnail() != null ? delegate.getThumbnail().getColorSpace() : null;
	}

	@Override
	public synchronized ColorSpaceType getColorSpaceType() {
		update();
		return delegate != null && delegate.getThumbnail() != null ? delegate.getThumbnail().getColorSpaceType() : null;
	}

	@Override
	public synchronized int getBitPerPixel() {
		update();
		return delegate != null && delegate.getThumbnail() != null ? delegate.getThumbnail().getBitPerPixel() : -1;
	}

	@Override
	public synchronized int getNumComponents() {
		update();
		return delegate != null && delegate.getThumbnail() != null ? delegate.getThumbnail().getNumComponents() : -1;
	}

	@Override
	public synchronized int getBitDepth() {
		update();
		return delegate != null && delegate.getThumbnail() != null ? delegate.getThumbnail().getBitDepth() : -1;
	}

	@Override
	public synchronized boolean isImageIOSupported() {
		update();
		return delegate != null && delegate.getThumbnail() != null ? delegate.getThumbnail().isImageIOSupported() : false;
	}

	protected synchronized void update() {
		if (!updated) {
			// After deserialization
			mbid = mbid.intern();
			updated = true;
		}
		if (delegate == null || delegate.isExpired()) {
			CoverArtArchiveUtil util = (CoverArtArchiveUtil) CoverUtil.get(CoverSupplier.COVER_ART_ARCHIVE);
			if (util == null) {
				throw new AssertionError("Error in CoverUtil.get()");
			}
			delegate = util.getThumbnail(mbid, PMS.getConfiguration().getExternalNetwork());
		}
	}
}
