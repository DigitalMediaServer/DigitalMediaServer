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

import java.awt.color.ColorSpace;
import java.io.Serializable;
import javax.annotation.concurrent.ThreadSafe;
import net.pms.image.ColorSpaceType;
import net.pms.image.ImageFormat;
import net.pms.image.ImageInfo;


/**
 * An interface for DLNA compliant thumbnails with image metadata information
 * and the {@link DLNAImageProfile}.
 * <p>
 * All implementation must be threadsafe/immutable.
 *
 * @author Nadahar
 */
@ThreadSafe
public interface DLNAThumbnail extends Serializable {

	/**
	 * @return The {@link DLNAImageProfile} this {@link DLNAThumbnail} adheres to.
	 */
	public DLNAImageProfile getDLNAImageProfile();

	/**
	 * @param copy whether or not a new array or a reference to the underlying
	 *             buffer should be returned. If a reference is returned,
	 *             <b>NO MODIFICATIONS must be done to the array!</b>
	 * @return The bytes of this image.
	 */
	public byte[] getBytes(boolean copy);

	/**
	 * @return The {@link ImageInfo} for this {@link DLNAThumbnail}.
	 */
	public ImageInfo getImageInfo();

	/**
	 * @return The width of this {@link DLNAThumbnail}.
	 */
	public int getWidth();

	/**
	 * @return The height of this {@link DLNAThumbnail}.
	 */
	public int getHeight();

	/**
	 * @return The {@link ImageFormat} for this {@link DLNAThumbnail}.
	 */
	public ImageFormat getFormat();

	/**
	 * @return The {@link ColorSpace} for this {@link DLNAThumbnail}.
	 */
	public ColorSpace getColorSpace();

	/**
	 * @return The {@link ColorSpaceType} for this {@link DLNAThumbnail}.
	 */
	public ColorSpaceType getColorSpaceType();

	/**
	 * @return The bits per pixel for this {@link DLNAThumbnail}.
	 *
	 * @see #getBitDepth()
	 */
	public int getBitPerPixel();

	/**
	 * The number of color components describe how many "channels" the color
	 * model has. A grayscale image without alpha has 1, a RGB image without
	 * alpha has 3, a RGB image with alpha has 4 etc.
	 *
	 * @return The number of color components for this {@link DLNAThumbnail}.
	 */
	public int getNumComponents();

	/**
	 * @return The number of bits per color "channel" for this
	 *         {@link DLNAThumbnail}.
	 *
	 * @see #getBitPerPixel()
	 * @see #getNumColorComponents()
	 */
	public int getBitDepth();

	/**
	 * @return Whether or not {@link ImageIO} can read/parse this
	 *         {@link DLNAThumbnail}.
	 */
	public boolean isImageIOSupported();

}
