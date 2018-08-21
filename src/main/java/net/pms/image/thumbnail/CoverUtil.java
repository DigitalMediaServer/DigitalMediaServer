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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.pms.PMS;
import net.pms.dlna.DLNAThumbnail;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.Tag;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * This class is the superclass of all cover utility implementations.
 * Cover utilities are responsible for getting media covers based
 * on information given by the caller.
 *
 * @author Nadahar
 */
public abstract class CoverUtil {

	private static Object instanceLock = new Object();
	private static CoverArtArchiveUtil coverArtArchiveInstance;

	/**
	 * Do not instantiate this class, use {@link #get()}.
	 */
	protected CoverUtil() {
	}

	/**
	 * Returns the static instance of the configured {@link CoverSupplier} type,
	 * or {@code null} if no {@link CoverSupplier} is configured.
	 *
	 * @return The {@link CoverUtil} or {@code null}.
	 */
	@Nullable
	public static CoverUtil get() {
		return get(PMS.getConfiguration().getAudioThumbnailMethod());
	}

	/**
	 * Returns the static instance of the specified {@link CoverSupplier} type,
	 * or {@code null} if no valid {@link CoverSupplier} is specified.
	 *
	 * @param supplier the {@link CoverSupplier} whose instance to get.
	 * @return The {@link CoverUtil} or {@code null}.
	 */
	@Nullable
	public static CoverUtil get(CoverSupplier supplier) {
		synchronized (instanceLock) {
			switch (supplier) {
				case COVER_ART_ARCHIVE:
					if (coverArtArchiveInstance == null) {
						coverArtArchiveInstance = new CoverArtArchiveUtil();
					}
					return coverArtArchiveInstance;
				default:
					return null;
			}
		}
	}

	/**
	 * Convenience method to find the first child {@link Element} of the given
	 * name.
	 *
	 * @param element the {@link Element} to search
	 * @param name the name of the child {@link Element}
	 * @return The found {@link Element} or null if not found
	 */
	protected Element getChildElement(Element element, String name) {
		NodeList list = element.getElementsByTagName(name);
		int listLength = list.getLength();
		for (int i = 0; i < listLength; i++) {
			Node node = list.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equals(name) && node instanceof Element) {
				return (Element) node;
			}
		}
		return null;
	}

	/**
	 * Gets a thumbnail from the configured cover utility based on the specified
	 * {@link AudioTagInfo}.
	 *
	 * @param tagInfo the {@link AudioTagInfo} to use while searching for a
	 *            cover.
	 * @return The thumbnail or {@code null} if none was found.
	 */
	@Nullable
	public final DLNAThumbnail getThumbnail(AudioTagInfo tagInfo) {
		boolean externalNetwork = PMS.getConfiguration().getExternalNetwork();
		return doGetThumbnail(tagInfo, externalNetwork);
	}

	/**
	 * Gets a {@link DLNAThumbnail} from the configured cover utility based on
	 * the specified {@link AudioTagInfo}.
	 *
	 * @param tagInfo the {@link AudioTagInfo} to use while searching for a
	 *            cover.
	 * @param externalNetwork {@code true} if the use of external networks
	 *            (Internet) is allowed, {@code false} otherwise.
	 * @return The thumbnail or {@code null} if none was found.
	 */
	@Nullable
	protected abstract DLNAThumbnail doGetThumbnail(AudioTagInfo tagInfo, boolean externalNetwork);

	/**
	 * Creates a new {@link AudioTagInfo} of the correct type based on the
	 * specified {@link Tag}.
	 *
	 * @param tag the {@link Tag} to get the information from.
	 * @return The new {@link AudioTagInfo} instance.
	 */
	public abstract AudioTagInfo createAudioTagInfo(@Nonnull Tag tag);

	/**
	 * Creates a new {@link AudioTagInfo} of the correct type based on the
	 * specified {@link MP3File}. This method is used specifically for MP3 files
	 * to extract information from both {@code IDv1} and {@code IDv2} tags.
	 *
	 * @param mp3File the {@link MP3File} to get the information from.
	 * @return The new {@link AudioTagInfo} instance.
	 */
	public abstract AudioTagInfo createAudioTagInfo(@Nonnull MP3File mp3File);

	/**
	 * Creates a new {@link AudioTagInfo} of the correct type using the
	 * specified values.
	 *
	 * @param album the album name or {@code null}.
	 * @param artist the artist name or {@code null}.
	 * @param title the song title or {@code null}.
	 * @param year the release year or {@code -1}.
	 * @param trackNo the track number or {@code -1}.
	 * @param numTracks the total number of tracks or {@code -1}.
	 * @return The new {@link AudioTagInfo} instance.
	 */
	public abstract AudioTagInfo createAudioTagInfo(
		@Nullable String album,
		@Nullable String artist,
		@Nullable String title,
		int year,
		int trackNo,
		int numTracks
	);
}
