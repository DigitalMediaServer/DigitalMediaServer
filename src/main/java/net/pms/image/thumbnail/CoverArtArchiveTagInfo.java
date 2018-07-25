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

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import java.util.Locale;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.id3.AbstractID3v2Tag;
import org.jaudiotagger.tag.id3.ID3v1Tag;
import net.pms.util.AudioUtils;
import net.pms.util.StringUtil;


/**
 * This class is a container to hold information used by
 * {@link CoverArtArchiveUtil} to look up covers.
 */
@Immutable
public class CoverArtArchiveTagInfo implements AudioTagInfo {

	/** The album name */
	protected final String album;

	/** The artist name */
	protected final String artist;

	/** The song title */
	protected final String title;

	/** The release year */
	protected final int year;

	/** The track number */
	protected final int trackNo;

	/** The total number of tracks */
	protected final int numTracks;

	/** The MusicBrainz artist ID */
	protected final String artistId;

	/** The MusicBrainz track ID */
	protected final String trackId;

	/** The MusicBrainz release ID */
	protected final String releaseId;

	/**
	 * Creates a new instance based on the specified {@link Tag}.
	 *
	 * @param tag the {@link Tag} to get the information from.
	 */
	public CoverArtArchiveTagInfo(@Nonnull Tag tag) {
		album = AudioUtils.tagGetFieldSafe(tag, FieldKey.ALBUM);
		artist = AudioUtils.tagGetFieldSafe(tag, FieldKey.ARTIST);
		String tempArtistId = AudioUtils.tagGetFieldSafe(tag, FieldKey.MUSICBRAINZ_ARTISTID);
		if (isBlank(tempArtistId)) {
			tempArtistId = AudioUtils.tagGetFieldSafe(tag, FieldKey.MUSICBRAINZ_RELEASEARTISTID);
		}
		artistId = tempArtistId;
		title = AudioUtils.tagGetFieldSafe(tag, FieldKey.TITLE);
		year = StringUtil.getYear(AudioUtils.tagGetFieldSafe(tag, FieldKey.YEAR));
		int tempTrack;
		try {
			tempTrack = Integer.parseInt(AudioUtils.tagGetFieldSafe(tag, FieldKey.TRACK));
		} catch (NumberFormatException e) {
			tempTrack = -1;
		}
		trackNo = tempTrack;
		try {
			tempTrack = Integer.parseInt(AudioUtils.tagGetFieldSafe(tag, FieldKey.TRACK_TOTAL));
		} catch (NumberFormatException e) {
			tempTrack = -1;
		}
		numTracks = tempTrack;
		trackId = AudioUtils.tagGetFieldSafe(tag, FieldKey.MUSICBRAINZ_TRACK_ID);
		releaseId = AudioUtils.tagGetFieldSafe(tag, FieldKey.MUSICBRAINZ_RELEASEID);
	}

	/**
	 * Creates a new instance based on the specified {@link MP3File}. This
	 * method is used specifically for MP3 files to extract information from
	 * both {@code IDv1} and {@code IDv2} tags.
	 *
	 * @param mp3File the {@link MP3File} to get the information from.
	 */
	public CoverArtArchiveTagInfo(@Nonnull MP3File mp3File) {
		ID3v1Tag v1tag = mp3File.getID3v1Tag();
		AbstractID3v2Tag v2tag = mp3File.getID3v2Tag();
		if (v1tag != null && v2tag != null) {
			String tempString = AudioUtils.tagGetFieldSafe(v2tag, FieldKey.ALBUM);
			if (isBlank(tempString)) {
				tempString = AudioUtils.tagGetFieldSafe(v1tag, FieldKey.ALBUM);
			}
			album = tempString;
			tempString = AudioUtils.tagGetFieldSafe(v2tag, FieldKey.ARTIST);
			if (isBlank(tempString)) {
				tempString = AudioUtils.tagGetFieldSafe(v1tag, FieldKey.ARTIST);
			}
			artist = tempString;
			String tempArtistId = AudioUtils.tagGetFieldSafe(v2tag, FieldKey.MUSICBRAINZ_ARTISTID);
			if (isBlank(tempArtistId)) {
				tempArtistId = AudioUtils.tagGetFieldSafe(v2tag, FieldKey.MUSICBRAINZ_RELEASEARTISTID);
			}
			artistId = tempArtistId;
			tempString = AudioUtils.tagGetFieldSafe(v2tag, FieldKey.TITLE);
			if (isBlank(tempString)) {
				tempString = AudioUtils.tagGetFieldSafe(v1tag, FieldKey.TITLE);
			}
			title = tempString;
			int tempYear = StringUtil.getYear(AudioUtils.tagGetFieldSafe(v2tag, FieldKey.YEAR));
			if (tempYear < 1) {
				tempYear = StringUtil.getYear(AudioUtils.tagGetFieldSafe(v1tag, FieldKey.YEAR));
			}
			year = tempYear;
			int tempTrack;
			try {
				tempTrack = Integer.parseInt(AudioUtils.tagGetFieldSafe(v2tag, FieldKey.TRACK));
			} catch (NumberFormatException e) {
				try {
					tempTrack = Integer.parseInt(AudioUtils.tagGetFieldSafe(v1tag, FieldKey.TRACK));
				} catch (NumberFormatException e2) {
					tempTrack = -1;
				}
			}
			trackNo = tempTrack;
			try {
				tempTrack = Integer.parseInt(AudioUtils.tagGetFieldSafe(v2tag, FieldKey.TRACK_TOTAL));
			} catch (NumberFormatException e) {
				try {
					tempTrack = Integer.parseInt(AudioUtils.tagGetFieldSafe(v1tag, FieldKey.TRACK_TOTAL));
				} catch (NumberFormatException e2) {
					tempTrack = -1;
				}
			}
			numTracks = tempTrack;
			trackId = AudioUtils.tagGetFieldSafe(v2tag, FieldKey.MUSICBRAINZ_TRACK_ID);
			releaseId = AudioUtils.tagGetFieldSafe(v2tag, FieldKey.MUSICBRAINZ_RELEASEID);
		} else if (v1tag != null || v2tag != null) {
			Tag tag = v1tag != null ? v1tag : v2tag;
			album = AudioUtils.tagGetFieldSafe(tag, FieldKey.ALBUM);
			artist = AudioUtils.tagGetFieldSafe(tag, FieldKey.ARTIST);
			String tempArtistId = AudioUtils.tagGetFieldSafe(tag, FieldKey.MUSICBRAINZ_ARTISTID);
			if (isBlank(tempArtistId)) {
				tempArtistId = AudioUtils.tagGetFieldSafe(tag, FieldKey.MUSICBRAINZ_RELEASEARTISTID);
			}
			artistId = tempArtistId;
			title = AudioUtils.tagGetFieldSafe(tag, FieldKey.TITLE);
			year = StringUtil.getYear(AudioUtils.tagGetFieldSafe(tag, FieldKey.YEAR));
			int tempTrack;
			try {
				tempTrack = Integer.parseInt(AudioUtils.tagGetFieldSafe(tag, FieldKey.TRACK));
			} catch (NumberFormatException e) {
				tempTrack = -1;
			}
			trackNo = tempTrack;
			try {
				tempTrack = Integer.parseInt(AudioUtils.tagGetFieldSafe(tag, FieldKey.TRACK_TOTAL));
			} catch (NumberFormatException e) {
				tempTrack = -1;
			}
			numTracks = tempTrack;
			trackId = AudioUtils.tagGetFieldSafe(tag, FieldKey.MUSICBRAINZ_TRACK_ID);
			releaseId = AudioUtils.tagGetFieldSafe(tag, FieldKey.MUSICBRAINZ_RELEASEID);
		} else {
			album = null;
			artist = null;
			artistId = null;
			title = null;
			year = -1;
			trackNo = -1;
			numTracks = -1;
			trackId = null;
			releaseId = null;
		}
	}

	/**
	 * @return {@code true} if this {@link CoverArtArchiveTagInfo} has any
	 *         information, {@code false} if it is "blank".
	 */
	@Override
	public boolean hasInfo() {
		return
			isNotBlank(album) && !"n/a".equals(album.toLowerCase(Locale.ROOT)) ||
			isNotBlank(artist) && !"n/a".equals(artist.toLowerCase(Locale.ROOT)) ||
			isNotBlank(title) && !"n/a".equals(title.toLowerCase(Locale.ROOT)) ||
			year > 0 ||
			trackNo > 0 ||
			isNotBlank(artistId) && !"n/a".equals(artistId.toLowerCase(Locale.ROOT)) ||
			isNotBlank(trackId) && !"n/a".equals(trackId.toLowerCase(Locale.ROOT)) ||
			isNotBlank(releaseId) && !"n/a".equals(releaseId.toLowerCase(Locale.ROOT));
	}

	/**
	 * @return {@code true} if this {@link CoverArtArchiveTagInfo} has any
	 *         useful information for searching for a {@code MBID},
	 *         {@code false} otherwise.
	 */
	@Override
	public boolean hasUsefulInfo() {
		return
			isNotBlank(album) && !"n/a".equals(album.toLowerCase(Locale.ROOT)) ||
			isNotBlank(title) && !"n/a".equals(title.toLowerCase(Locale.ROOT)) ||
			isNotBlank(trackId) && !"n/a".equals(trackId.toLowerCase(Locale.ROOT)) ||
			isNotBlank(releaseId) && !"n/a".equals(releaseId.toLowerCase(Locale.ROOT));
	}

	@Override
	public String getAlbum() {
		return album;
	}

	@Override
	public String getArtist() {
		return artist;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public int getYear() {
		return year;
	}

	@Override
	public int getTrackNumber() {
		return trackNo;
	}

	@Override
	public int getNumberOfTracks() {
		return numTracks;
	}

	/**
	 * @return The MusicBrainz artist ID.
	 */
	@Nullable
	public String getArtistId() {
		return artistId;
	}

	/**
	 * @return The MusicBrainz track ID.
	 */
	@Nullable
	public String getTrackId() {
		return trackId;
	}

	/**
	 * @return The MusicBrainz release ID.
	 */
	@Nullable
	public String getReleaseId() {
		return releaseId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((album == null) ? 0 : album.hashCode());
		result = prime * result + ((artist == null) ? 0 : artist.hashCode());
		result = prime * result + ((artistId == null) ? 0 : artistId.hashCode());
		result = prime * result + numTracks;
		result = prime * result + ((releaseId == null) ? 0 : releaseId.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		result = prime * result + ((trackId == null) ? 0 : trackId.hashCode());
		result = prime * result + trackNo;
		result = prime * result + year;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof CoverArtArchiveTagInfo)) {
			return false;
		}
		CoverArtArchiveTagInfo other = (CoverArtArchiveTagInfo) obj;
		if (album == null) {
			if (other.album != null) {
				return false;
			}
		} else if (!album.equals(other.album)) {
			return false;
		}
		if (artist == null) {
			if (other.artist != null) {
				return false;
			}
		} else if (!artist.equals(other.artist)) {
			return false;
		}
		if (artistId == null) {
			if (other.artistId != null) {
				return false;
			}
		} else if (!artistId.equals(other.artistId)) {
			return false;
		}
		if (numTracks != other.numTracks) {
			return false;
		}
		if (releaseId == null) {
			if (other.releaseId != null) {
				return false;
			}
		} else if (!releaseId.equals(other.releaseId)) {
			return false;
		}
		if (title == null) {
			if (other.title != null) {
				return false;
			}
		} else if (!title.equals(other.title)) {
			return false;
		}
		if (trackId == null) {
			if (other.trackId != null) {
				return false;
			}
		} else if (!trackId.equals(other.trackId)) {
			return false;
		}
		if (trackNo != other.trackNo) {
			return false;
		}
		if (year != other.year) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		if (isNotBlank(artist)) {
			result.append(artist);
		}
		if (isNotBlank(artistId)) {
			if (result.length() > 0) {
				result.append(" (").append(artistId).append(')');
			} else {
				result.append(artistId);
			}
		}
		if (
			result.length() > 0 &&
			(
				isNotBlank(title) ||
				isNotBlank(album) ||
				isNotBlank(trackId)
			)
		) {
			result.append(" - ");
		}
		if (isNotBlank(album)) {
			result.append(album);
			if (isNotBlank(title) || isNotBlank(trackId)) {
				result.append(": ");
			}
		}
		if (isNotBlank(title)) {
			result.append(title);
			if (isNotBlank(trackId)) {
				result.append(" (").append(trackId).append(')');
			}
		} else if (isNotBlank(trackId)) {
			result.append(trackId);
		}
		if (year > 0) {
			if (result.length() > 0) {
				result.append(" (").append(year).append(')');
			} else {
				result.append(year);
			}
		}
		if (trackNo > 0 && numTracks > 0) {
			if (result.length() > 0) {
				result.append(", ");
			}
			result.append("track=").append(trackNo).append("/").append(numTracks);
		}
		if (isNotBlank(trackId)) {
			if (result.length() > 0) {
				result.append(", ");
			}
			result.append("trackID=").append(trackId);
		}
		if (isNotBlank(releaseId)) {
			if (result.length() > 0) {
				result.append(", ");
			}
			result.append("releaseId=").append(releaseId);
		}
		return result.toString();
	}
}
