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

import static net.pms.util.StringUtil.urlEncode;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import fm.last.musicbrainz.coverart.CoverArt;
import fm.last.musicbrainz.coverart.CoverArtException;
import fm.last.musicbrainz.coverart.CoverArtImage;
import fm.last.musicbrainz.coverart.impl.DefaultCoverArtArchiveClient;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import net.pms.database.TableCoverArtArchive;
import net.pms.database.TableCoverArtArchive.CoverArtArchiveEntry;
import net.pms.database.TableManager;
import net.pms.database.TableMusicBrainzReleases;
import net.pms.database.TableMusicBrainzReleases.MusicBrainzReleasesResult;
import net.pms.dlna.DLNABinaryThumbnail;
import net.pms.dlna.DLNAThumbnail;
import net.pms.image.ImageFormat;
import net.pms.image.ImagesUtil.ScaleType;
import net.pms.image.thumbnail.ExpirableThumbnail.CachedThumbnail;
import net.pms.image.thumbnail.ExpirableThumbnail.ExpirableBinaryThumbnail;
import net.pms.service.Services;
import net.pms.util.SafeDocumentBuilderFactory;
import net.pms.util.StringUtil;
import net.pms.util.TimePeriod;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.HttpResponseException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


/**
 * This class handles audio covers from Cover Art Archive. It handles database
 * caching and HTTP lookup of both MusicBrainz ID's (MBIDs) and binary cover
 * data from Cover Art Archive.
 *
 * @author Nadahar
 */
public class CoverArtArchiveUtil extends CoverUtil {
	private static final Logger LOGGER = LoggerFactory.getLogger(CoverArtArchiveUtil.class);

	/** A point in time in the very distant future */
	private static final long NO_NETWORK_EXPIRATION_TIME = Long.MAX_VALUE;

	/** An expiration period that expires almost immediately */
	private static final TimePeriod IMMEDIATE_EXPIRATION_PERIOD = new TimePeriod(100);

	/** An expiration period used when errors occur, 4 ± 1 minutes */
	private static final TimePeriod ERROR_EXPIRATION_PERIOD = new TimePeriod(4 * 60 * 1000, 2 * 60 * 1000);

	/** An expiration period used when not found, 3 ± 0.5 days */
	private static final TimePeriod NOT_FOUND_EXPIRATION_PERIOD = new TimePeriod(3 * 24 * 60 * 60 * 1000, 24 * 60 * 60 * 1000);

	/** An expiration period used when found, 14 ± 3 days */
	private static final TimePeriod FOUND_EXPIRATION_PERIOD = new TimePeriod(14 * 24 * 60 * 60 * 1000, 6 * 24 * 60 * 60 * 1000);

	/** The time to wait for a ticket in seconds */
	private static final long WAIT_TIMEOUT_SECONDS = 10;
	private static final SafeDocumentBuilderFactory DOCUMENT_BUILDER_FACTORY = SafeDocumentBuilderFactory.newInstance();

	/** How often the thumbnail cache should be cleaned automatically */
	private static final long THUMBNAIL_CACHE_CLEAN_INTERVAL = 2 * 60 * 1000;

	/** The thread queue for a given {@link CoverArtArchiveTagInfo} */
	private final Map<CoverArtArchiveTagInfo, ReentrantLock> tagQueue = new HashMap<>();

	/** The thread queue for a given {@code MBID} */
	private final Map<String, ReentrantLock> mbIDQueue = new HashMap<>();

	/** The weakly referenced thumbnail cache */
	private final Map<String, CachedThumbnail> thumbnailCache = new HashMap<>();

	/** The time of the last thumbnail cache cleaning */
	private long lastThumbnailCacheClean;

	/**
	 * Do not instantiate this class, use {@link CoverUtil#get()}.
	 */
	protected CoverArtArchiveUtil() {
	}

	/**
	 * Creates a {@link DLNABinaryThumbnail} from a byte array containing a
	 * supported image format. The maximum thumbnail size to store can be tuned
	 * here.
	 *
	 * @param bytes the image data.
	 * @return The {@link DLNABinaryThumbnail} or {@code null};
	 */
	@Nullable
	protected static DLNABinaryThumbnail createThumbnail(@Nullable byte[] bytes) {
		if (bytes == null) {
			return null;
		}
		try {
			return DLNABinaryThumbnail.toThumbnail(
				bytes,
				640,
				480,
				ScaleType.MAX,
				ImageFormat.SOURCE,
				false
			);
		} catch (IOException e) {
			LOGGER.error("Couldn't convert image to DLNABinaryThumbnail: {}", e.getMessage());
			LOGGER.trace("", e);
			return null;
		}
	}

	@Nonnull
	private ExpirableBinaryThumbnail retrieveThumbnail(@Nonnull TableManager tableManager, @Nonnull String mbID, boolean externalNetwork) {
		if (mbID == null) {
			throw new IllegalArgumentException("mbID cannot be null");
		}
		mbID = mbID.intern();
		boolean trace = LOGGER.isTraceEnabled();

		if (trace) {
			LOGGER.trace("Trying to retrieve thumbnail for MBID \"{}\"", mbID);
		}

		TableCoverArtArchive tableCoverArtArchive = tableManager.getTableCoverArtArchive();
		if (tableCoverArtArchive == null) {
			throw new IllegalStateException(
				"Can't find thumbnail from Cover Art Archive since the table instance doesn't exist"
			);
		}

		// Check if the thumbnail is cached
		ReentrantLock ticket;
		synchronized (thumbnailCache) {
			cleanCache(false);
			CachedThumbnail cacheEntry = thumbnailCache.get(mbID);
			if (cacheEntry != null) {
				// Don't "optimize", we need to hold a reference to the thumbnail before we test it to avoid GC'ing
				ExpirableBinaryThumbnail result = cacheEntry.toExpirableBinaryThumbnail();
				if (!cacheEntry.isDisposable()) {
					if (trace) {
						if (result.getThumbnail() == null) {
							LOGGER.trace(
								"Found a cached entry without a thumbnail that expires {}, no thumbnail is currently available",
								StringUtil.formatDateTimeAuto(result.getExpirationTime())
							);
						} else {
							LOGGER.trace("Found a cached thumbnail: {}", result.getThumbnail());
						}
					}
					return result;
				}

				// It is cached but expired
				cleanCache(true);
			}

			// It's not cached, get or create a ticket.
			synchronized (mbIDQueue) {
				ticket = mbIDQueue.get(mbID);
				if (ticket == null) {
					ticket = new ReentrantLock();
					mbIDQueue.put(mbID, ticket);
				}
			}
		}

		// Queue on the ticket
		try {
			if (!ticket.tryLock(WAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
				// Lock acquisition timed out, return nothing
				long expirationTime = IMMEDIATE_EXPIRATION_PERIOD.getTime();
				if (trace) {
					LOGGER.trace(
						"Timed out while waiting for ticket acquisition, returning a null thumbnail that expires {}",
						StringUtil.formatDateTimeAuto(expirationTime)
					);
				}
				return new ExpirableBinaryThumbnail(System.currentTimeMillis() + 100);
			}
		} catch (InterruptedException e) {
			LOGGER.debug(
				"CoverArtAchiveThumbnail.retrieveThumbnail() was interrupted while waiting for a ticket for MBID \"{}\"",
				mbID
			);
			return new ExpirableBinaryThumbnail(System.currentTimeMillis() + 100);
		}
		try {
			// Done queuing, check if another thread has retrieved the thumbnail while queuing
			synchronized (thumbnailCache) {
				CachedThumbnail cacheEntry = thumbnailCache.get(mbID);
				if (cacheEntry != null) {
					// Don't "optimize", we need to hold a reference to the thumbnail before we test it to avoid GC'ing
					ExpirableBinaryThumbnail result = cacheEntry.toExpirableBinaryThumbnail();
					if (!cacheEntry.isDisposable()) {
						if (trace) {
							if (result.getThumbnail() == null) {
								LOGGER.trace(
									"Found a cached entry without a thumbnail that expires {}, no thumbnail is currently available",
									StringUtil.formatDateTimeAuto(result.getExpirationTime())
								);
							} else {
								LOGGER.trace("Found a cached thumbnail: {}", result.getThumbnail());
							}
						}
						return result;
					}
				}
			}

			// Check if it's in the database
			ExpirableBinaryThumbnail result;
			CoverArtArchiveEntry tableEntry = tableCoverArtArchive.findMBID(mbID);
			if (
				!tableEntry.isFound() ||
				(
					tableEntry.isFound() && tableEntry.getCover() == null &&
					System.currentTimeMillis() >= tableEntry.getExpires()
				)
			) {
				// It's not in the database or its empty and expired - try to retrieve it
				if (externalNetwork) {
					Cover cover = downloadCover(tableCoverArtArchive, mbID, externalNetwork);
					if (cover.getThumbnail() == null && cover.getBytes() != null) {
						result = new ExpirableBinaryThumbnail(createThumbnail(cover.getBytes()), cover.getExpires());
					} else {
						result = new ExpirableBinaryThumbnail(cover.getThumbnail(), cover.getExpires());
					}
				} else {
					// External network is disabled, never expire
					result = new ExpirableBinaryThumbnail(NO_NETWORK_EXPIRATION_TIME);
				}
			} else if (tableEntry.getThumbnail() != null) {
				// We have the thumbnail
				result = new ExpirableBinaryThumbnail(tableEntry.getThumbnail(), tableEntry.getExpires());
			} else if (tableEntry.getCover() != null) {
				// We have the cover, generate the thumbnail
				result = new ExpirableBinaryThumbnail(createThumbnail(tableEntry.getCover()), tableEntry.getExpires());

				// Update the database with the generated thumbnail
				try {
					tableCoverArtArchive.updateThumbnail(mbID, result.getThumbnail());
				} catch (SQLException e) {
					LOGGER.error(
						"Could not update thumbnail for MBID \"{}\" because of an SQL error: {}",
						mbID,
						e.getMessage()
					);
					LOGGER.trace("", e);
				}
			} else {
				// The entry is cached as unavailable and isn't expired
				result = new ExpirableBinaryThumbnail(tableEntry.getExpires());
			}

			// If we have a result..? How to deal with null...
			// Update the cache with the generated thumbnail
			synchronized (thumbnailCache) {
				thumbnailCache.put(mbID, new CachedThumbnail(result));
			}

			if (trace) {
				if (result.getThumbnail() == null) {
					LOGGER.trace(
						"Didn't find a thumbnail, caching and returning a null thumbnail that expires {}",
						StringUtil.formatDateTimeAuto(result.getExpirationTime())
					);
				} else {
					LOGGER.trace("Found a thumbnail: {}", result.getThumbnail());
				}
			}
			return result;
		} finally {
			ticket.unlock();

			// Remove the ticked from the queue
			synchronized (mbIDQueue) {
				mbIDQueue.remove(mbID);
			}
		}
	}

	/**
	 * Downloads the cover from CoverArtArchive and store it in the database.
	 * Must only be called with a lock on a ticket from the same {@code MBID}.
	 * No queuing is done here.
	 *
	 * @param tableCoverArtArchive the {@link TableCoverArtArchive} instance to
	 *            use.
	 * @param mbID the {@code MBID} for which to retrieve the cover art.
	 * @return The cover art image data or {@code null}.
	 */
	@Nonnull
	private static Cover downloadCover(@Nonnull TableCoverArtArchive tableCoverArtArchive, @Nonnull String mbID, boolean externalNetwork) {
		if (!externalNetwork) {
			LOGGER.warn(
				"Can't download cover art for MBID \"{}\" from Cover Art Archive since external network access is disabled",
				mbID
			);
			LOGGER.info("Either enable external network or disable cover art downloads");
			return new Cover(null, null, NO_NETWORK_EXPIRATION_TIME);
		}

		DefaultCoverArtArchiveClient client = new DefaultCoverArtArchiveClient();

		CoverArt coverArt;
		try {
			coverArt = client.getByMbid(UUID.fromString(mbID));
		} catch (CoverArtException e) {
			LOGGER.debug("Couldn't get cover with MBID \"{}\": {}", mbID, e.getMessage());
			LOGGER.trace("", e);
			return new Cover(null, null, ERROR_EXPIRATION_PERIOD.getTime());
		}
		if (coverArt == null || coverArt.getImages().isEmpty()) {
			LOGGER.debug("MBID \"{}\" has no cover at CoverArtArchive", mbID);
			long expiration = NOT_FOUND_EXPIRATION_PERIOD.getTime();
			tableCoverArtArchive.writeMBID(mbID, null, null, expiration);
			return new Cover(null, null, expiration);
		}
		CoverArtImage image = coverArt.getFrontImage();
		if (image == null) {
			image = coverArt.getImages().get(0);
		}
		byte[] cover = null;
		try {
			try (InputStream is = image.getLargeThumbnail()) {
				cover = IOUtils.toByteArray(is);
			} catch (HttpResponseException e) {
				// Use the default image if the large thumbnail is not available
				try (InputStream is = image.getImage()) {
					cover = IOUtils.toByteArray(is);
				}
			}
			DLNABinaryThumbnail thumbnail = cover == null ? null : createThumbnail(cover);
			long expiration = FOUND_EXPIRATION_PERIOD.getTime();
			tableCoverArtArchive.writeMBID(mbID, cover, thumbnail, expiration);
			return new Cover(cover, thumbnail, expiration);
		} catch (HttpResponseException e) {
			if (e.getStatusCode() == 404) {
				LOGGER.debug("Cover art for MBID \"{}\" was not found at CoverArtArchive", mbID);
				long expiration = NOT_FOUND_EXPIRATION_PERIOD.getTime();
				tableCoverArtArchive.writeMBID(mbID, null, null, expiration);
				return new Cover(null, null, expiration);
			}
			LOGGER.warn(
				"Got HTTP status code {} while trying to download cover art for MBID \"{}\" from CoverArtArchive: {}",
				e.getStatusCode(),
				mbID,
				e.getMessage()
			);
			return new Cover(null, null, ERROR_EXPIRATION_PERIOD.getTime());
		} catch (IOException e) {
			LOGGER.error("An error occurred while downloading cover art for MBID \"{}\": {}", mbID, e.getMessage());
			LOGGER.trace("", e);
			return new Cover(null, null, ERROR_EXPIRATION_PERIOD.getTime());
		}
	}

	/**
	 * Attempts to return an {@link ExpirableBinaryThumbnail} for the specified
	 * {@code MBID}. Only returns {@code null} if the specified {@code MBID} is
	 * blank.
	 *
	 * @param mbID the MBID whose thumbnail to find.
	 * @param externalNetwork {@code true} if the use of external networks is
	 *            allowed, {@code false} otherwise.
	 * @return The {@link ExpirableBinaryThumbnail} with or without an actual
	 *         thumbnail or {@code null} if the specified {@code MBID} is blank.
	 */
	@Nullable
	public ExpirableBinaryThumbnail getThumbnail(@Nullable String mbID, boolean externalNetwork) {
		if (isBlank(mbID)) {
			return null;
		}
		TableManager tableManager = Services.tableManager();
		if (tableManager == null) {
			LOGGER.error("Can't find cover from Cover Art Archive since TableManager doesn't exist");
			return null;
		}

		return retrieveThumbnail(tableManager, mbID, externalNetwork);
	}

	@Override
	@Nullable
	protected DLNAThumbnail doGetThumbnail(@Nullable AudioTagInfo tagInfo, boolean externalNetwork) {
		if (!(tagInfo instanceof CoverArtArchiveTagInfo)) {
			return null;
		}
		CoverArtArchiveTagInfo caaTagInfo = (CoverArtArchiveTagInfo) tagInfo;

		TableManager tableManager = Services.tableManager();
		if (tableManager == null) {
			LOGGER.error("Can't download cover from Cover Art Archive since TableManager doesn't exist");
			return null;
		}

		LOGGER.trace("Trying to find MBID for \"{}\"", caaTagInfo);
		String mbID = getMBID(tableManager, caaTagInfo, externalNetwork);
		if (mbID == null) {
			LOGGER.trace("Failed to find MBID, returning null");
			return null;
		}

		LOGGER.trace("Found MBID \"{}\", trying to find cover", mbID);
		return new CoverArtAchiveThumbnail(mbID, retrieveThumbnail(tableManager, mbID, externalNetwork));
	}

	@Override
	public AudioTagInfo createAudioTagInfo(@Nonnull Tag tag) {
		return new CoverArtArchiveTagInfo(tag);
	}

	@Override
	public AudioTagInfo createAudioTagInfo(MP3File mp3File) {
		return new CoverArtArchiveTagInfo(mp3File);
	}

	@Override
	public AudioTagInfo createAudioTagInfo(
		@Nullable String album,
		@Nullable String artist,
		@Nullable String title,
		int year,
		int trackNo,
		int numTracks
	) {
		return new CoverArtArchiveTagInfo(album, artist, null, title, year, trackNo, numTracks, null, null);
	}

	/**
	 * Creates a new {@link AudioTagInfo} of the correct type using the
	 * specified values.
	 *
	 * @param album the album name or {@code null}.
	 * @param artist the artist name or {@code null}.
	 * @param artistId the MusicBrainz artist ID or {@code null}.
	 * @param title the song title or {@code null}.
	 * @param year the release year or {@code -1}.
	 * @param trackNo the track number or {@code -1}.
	 * @param numTracks the total number of tracks or {@code -1}.
	 * @param trackId the MusicBrainz track ID or {@code null}.
	 * @param releaseId the MusicBrainz release ID or {@code null}.
	 * @return The new {@link AudioTagInfo} instance.
	 */
	public CoverArtArchiveTagInfo createAudioTagInfo(
		@Nullable String album,
		@Nullable String artist,
		@Nullable String artistId,
		@Nullable String title,
		int year,
		int trackNo,
		int numTracks,
		@Nullable String trackId,
		@Nullable String releaseId
	) {
		return new CoverArtArchiveTagInfo(album, artist, artistId, title, year, trackNo, numTracks, trackId, releaseId);
	}

	private static String fuzzString(String s) {
		String[] words = s.split(" ");
		StringBuilder sb = new StringBuilder("(");
		for (String word : words) {
			sb.append(StringUtil.luceneEscape(word)).append("~ ");
		}
		sb.append(')');
		return sb.toString();
	}

	private static String buildMBReleaseQuery(@Nonnull CoverArtArchiveTagInfo tagInfo, final boolean fuzzy) {
		final String and = urlEncode(" AND ");
		StringBuilder query = new StringBuilder("release/?query=");
		boolean added = false;

		if (isNotBlank(tagInfo.getAlbum())) {
			if (fuzzy) {
				query.append(urlEncode(fuzzString(tagInfo.getAlbum())));
			} else {
				query.append(urlEncode("\"" + StringUtil.luceneEscape(tagInfo.getAlbum()) + "\""));
			}
			added = true;
		}

		/*
		 * Release (album) artist is usually the music director of the album.
		 * Track (Recording) artist is usually the singer. Searching release
		 * with artist here is likely to return no result.
		 */

		if (
			isNotBlank(tagInfo.getTrackId()) &&
			(
				isBlank(tagInfo.getAlbum()) ||
				!(
					isNotBlank(tagInfo.getArtist()) ||
					isNotBlank(tagInfo.getArtistId())
				)
			)
		) {
			if (added) {
				query.append(and);
			}
			query.append("tid:").append(tagInfo.getTrackId());
			added = true;
		} else if (
			isNotBlank(tagInfo.getTitle()) &&
			(
				isBlank(tagInfo.getAlbum()) ||
				!(
					isNotBlank(tagInfo.getArtist()) ||
					isNotBlank(tagInfo.getArtistId())
				)
			)
		) {
			if (added) {
				query.append(and);
			}
			query.append("recording:");
			if (fuzzy) {
				query.append(urlEncode(fuzzString(tagInfo.getTitle())));
			} else {
				query.append(urlEncode("\"" + StringUtil.luceneEscape(tagInfo.getTitle()) + "\""));
			}
			added = true;
		}

		if (!fuzzy && tagInfo.getYear() > 0) {
			if (added) {
				query.append(and);
			}
			query.append("date:").append(tagInfo.getYear()).append('*');
			added = true;
		}
		return query.toString();
	}

	private static String buildMBRecordingQuery(CoverArtArchiveTagInfo tagInfo, final boolean fuzzy) {
		final String and = urlEncode(" AND ");
		StringBuilder query = new StringBuilder("recording/?query=");
		boolean added = false;

		if (isNotBlank(tagInfo.getTitle())) {
			if (fuzzy) {
				query.append(urlEncode(fuzzString(tagInfo.getTitle())));
			} else {
				query.append(urlEncode("\"" + StringUtil.luceneEscape(tagInfo.getTitle()) + "\""));
			}
			added = true;
		}

		if (isNotBlank(tagInfo.getTrackId())) {
			if (added) {
				query.append(and);
			}
			query.append("tid:").append(tagInfo.getTrackId());
			added = true;
		}

		if (isNotBlank(tagInfo.getArtistId())) {
			if (added) {
				query.append(and);
			}
			query.append("arid:").append(tagInfo.getArtistId());
			added = true;
		} else if (isNotBlank(tagInfo.getArtist())) {
			if (added) {
				query.append(and);
			}
			query.append("artistname:");
			if (fuzzy) {
				query.append(urlEncode(fuzzString(tagInfo.getArtist())));
			} else {
				query.append(urlEncode("\"" + StringUtil.luceneEscape(tagInfo.getArtist()) + "\""));
			}
		}

		if (!fuzzy && tagInfo.getYear() > 0) {
			if (added) {
				query.append(and);
			}
			query.append("date:").append(tagInfo.getYear()).append('*');
			added = true;
		}
		return query.toString();
	}

	@SuppressWarnings("null")
	@Nullable
	private String getMBID(@Nonnull TableManager tableManager, @Nullable CoverArtArchiveTagInfo tagInfo, boolean externalNetwork) {
		if (tagInfo == null) {
			return null;
		}
		boolean trace = LOGGER.isTraceEnabled();
		if (trace) {
			LOGGER.trace("Trying to resolve MBID for tag: {}", tagInfo);
		}

		TableMusicBrainzReleases tableMusicBrainzReleases = tableManager.getTableMusicBrainzReleases();
		if (tableMusicBrainzReleases == null) {
			LOGGER.error("Can't look up cover MBID from MusicBrainz since the table instance doesn't exist");
			return null;
		}

		if (!tagInfo.hasUsefulInfo()) {
			if (trace && tagInfo.hasInfo()) {
				LOGGER.trace("Tag has no useful information - aborting MBID search");
			} else if (trace) {
				LOGGER.trace("Tag has no information - aborting MBID search");
			}
			return null;
		}

		// No need to look up MBID if it's already in the tagInfo
		if (isNotBlank(tagInfo.getReleaseId()) && !"n/a".equals(tagInfo.getReleaseId().toLowerCase(Locale.ROOT))) {
			if (trace) {
				LOGGER.trace("Found MBID {} embedded in the metadata", tagInfo.getReleaseId());
			}
			return tagInfo.getReleaseId().intern();
		}

		// Check if it's in the database first
		MusicBrainzReleasesResult result = tableMusicBrainzReleases.findMBID(tagInfo);
		if (result != null) {
			if (isNotBlank(result.getMBID())) {
				if (trace) {
					LOGGER.trace("Found cached MBID \"{}\"", result.getMBID());
				}
				return result.getMBID().intern();
			} else if (System.currentTimeMillis() < result.getExpires()) {
				// If a lookup has been done within expireTime and no result,
				// return null. Do another lookup after expireTime has passed
				if (trace) {
					LOGGER.trace(
						"Found a cached entry without a MBID that expires {}, no MBID found",
						StringUtil.formatDateTimeAuto(result.getExpires())
					);
				}
				return null;
			}
		}

		if (!externalNetwork) {
			LOGGER.warn("Can't look up MBID from MusicBrainz since external network is disabled");
			LOGGER.info("Either enable external network or disable cover download");
			return null;
		}

		// It's not in the database, get or create a ticket.
		ReentrantLock ticket;
		synchronized (tagQueue) {
			ticket = tagQueue.get(tagInfo);
			if (ticket == null) {
				ticket = new ReentrantLock();
				tagQueue.put(tagInfo, ticket);
			}
		}

		// Queue on the ticket
		try {
			if (!ticket.tryLock(WAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
				// Lock acquisition timed out, return nothing
				if (trace) {
					LOGGER.trace("Timed out while waiting for ticket acquisition, returning an empty MBID");
				}
				return null;
			}
		} catch (InterruptedException e) {
			LOGGER.debug(
				"CoverArtAchiveThumbnail.getMBID() was interrupted while waiting for a ticket for tag {}",
				tagInfo
			);
			return null;
		}
		try {
			DocumentBuilder builder = null;
			try {
				builder = DOCUMENT_BUILDER_FACTORY.newDocumentBuilder();
			} catch (ParserConfigurationException e) {
				LOGGER.error("Error initializing XML parser: {}", e.getMessage());
				LOGGER.trace("", e);
				return null;
			}

			/*
			 * Rounds are defined as this:
			 *
			 *   1 - Exact release search
			 *   2 - Fuzzy release search
			 *   3 - Exact track search
			 *   4 - Fuzzy track search
			 *   5 - Give up
			 */

			int round;
			if (
				isNotBlank(tagInfo.getAlbum()) ||
				isNotBlank(tagInfo.getArtist()) ||
				isNotBlank(tagInfo.getArtistId())
			) {
				round = 1;
			} else {
				round = 3;
			}

			String mbID = null;
			while (round < 5 && isBlank(mbID)) {
				String query;

				if (round < 3) {
					query = buildMBReleaseQuery(tagInfo, round > 1);
				} else {
					query = buildMBRecordingQuery(tagInfo, round > 3);
				}

				if (query != null) {
					final String url = "http://musicbrainz.org/ws/2/" + query + "&fmt=xml";
					if (LOGGER.isTraceEnabled()) {
						LOGGER.trace("Performing release MBID lookup at musicbrainz: \"{}\"", url);
					}

					try {
						HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
						connection.setRequestProperty("Accept-Charset", StandardCharsets.UTF_8.name());
						int status = connection.getResponseCode();
						if (status != 200) {
							LOGGER.error(
								"Could not lookup audio cover for \"{}\": musicbrainz.org replied with status code {}",
								tagInfo.getTitle(),
								status
							);
							return null;
						}

						Document document;
						try {
							document = builder.parse(connection.getInputStream());
						} catch (SAXException e) {
							LOGGER.error("Failed to parse XML for \"{}\": {}", url, e.getMessage());
							LOGGER.trace("", e);
							return null;
						} finally {
							connection.getInputStream().close();
						}

						ArrayList<ReleaseRecord> releaseList;
						if (round < 3) {
							releaseList = parseRelease(document);
						} else {
							releaseList = parseRecording(document);
						}

						if (releaseList != null && !releaseList.isEmpty()) {
							// Try to find the best match - this logic can be refined if
							// matching quality turns out to be to low
							int maxScore = 0;
							for (ReleaseRecord release : releaseList) {
								boolean found = false;
								if (isNotBlank(tagInfo.getArtist())) {
									String[] tagArtists = tagInfo.getArtist().split("(?i),|&|\\sand\\s");
									for (String artist : release.artists) {
										for (String tagArtist : tagArtists) {
											if (StringUtil.isEqual(tagArtist, artist, false, true, true, null)) {
												release.score += 30;
												found = true;
												break;
											}
										}
									}
								}
								if (isNotBlank(tagInfo.getAlbum())) {
									if (StringUtil.isEqual(tagInfo.getAlbum(), release.album, false, true, true, null)) {
											release.score += 30;
											found = true;
									}
								}
								if (isNotBlank(tagInfo.getTitle())) {
									if (StringUtil.isEqual(tagInfo.getTitle(), release.title, false, true, true, null)) {
										release.score += 40;
										found = true;
									}
								}
								if (tagInfo.getYear() > 0 && tagInfo.getYear() == release.year) {
									release.score += 20;
								}
								// Prefer Single > Album > Compilation
								if (found) {
									if (release.type == ReleaseType.Single) {
										release.score += 20;
									} else if (release.type == null || release.type == ReleaseType.Album) {
										release.score += 10;
									}
								}
								maxScore = Math.max(maxScore, release.score);
							}

							for (ReleaseRecord release : releaseList) {
								if (release.score == maxScore) {
									mbID = release.id;
									break;
								}
							}
						}

						if (isNotBlank(mbID)) {
							mbID = mbID.intern();
							if (trace) {
								LOGGER.debug("MusicBrainz release ID \"{}\" found for \"{}\" with \"{}\"", mbID, tagInfo, url);
							} else {
								LOGGER.debug("MusicBrainz release ID \"{}\" found for \"{}\"", mbID, tagInfo);
							}
							tableMusicBrainzReleases.writeMBID(mbID, tagInfo, FOUND_EXPIRATION_PERIOD.getTime());
							return mbID;
						}
						if (trace) {
							LOGGER.trace("No music release found with \"{}\"", url);
						}
					} catch (IOException e) {
						LOGGER.debug("Failed to find MBID for \"{}\": {}", query, e.getMessage());
						LOGGER.trace("", e);
						return null;
					}
				}
				round++;
			}
			LOGGER.debug("No MusicBrainz release found for \"{}\"", tagInfo);
			tableMusicBrainzReleases.writeMBID(null, tagInfo, NOT_FOUND_EXPIRATION_PERIOD.getTime());
			return null;
		} finally {
			ticket.unlock();

			// Remove the ticked from the queue
			synchronized (tagQueue) {
				tagQueue.remove(tagInfo);
			}
		}
	}

	private ArrayList<ReleaseRecord> parseRelease(final Document document) {
		NodeList nodeList = document.getDocumentElement().getElementsByTagName("release-list");
		if (nodeList.getLength() < 1) {
			return null;
		}
		Element listElement = (Element) nodeList.item(0); // release-list
		nodeList = listElement.getElementsByTagName("release");
		if (nodeList.getLength() < 1) {
			return null;
		}

		int nodeListLength = nodeList.getLength();
		ArrayList<ReleaseRecord> releaseList = new ArrayList<>(nodeListLength);
		for (int i = 0; i < nodeListLength; i++) {
			if (nodeList.item(i) instanceof Element) {
				Element releaseElement = (Element) nodeList.item(i);
				ReleaseRecord release = new ReleaseRecord();
				release.id = releaseElement.getAttribute("id");
				try {
					release.score = Integer.parseInt(releaseElement.getAttribute("ext:score"));
				} catch (NumberFormatException e) {
					release.score = 0;
				}
				try {
					release.album = getChildElement(releaseElement, "title").getTextContent();
				} catch (NullPointerException e) {
					release.album = null;
				}
				Element releaseGroup = getChildElement(releaseElement, "release-group");
				if (releaseGroup != null) {
					try {
						release.type = ReleaseType.valueOf(getChildElement(releaseGroup, "primary-type").getTextContent());
					} catch (IllegalArgumentException | NullPointerException e) {
						release.type = null;
					}
				}
				Element releaseYear = getChildElement(releaseElement, "date");
				if (releaseYear != null) {
					release.year = StringUtil.getYear(releaseYear.getTextContent());
				} else {
					release.year = -1;
				}
				Element artists = getChildElement(releaseElement, "artist-credit");
				if (artists != null && artists.getChildNodes().getLength() > 0) {
					NodeList artistList = artists.getChildNodes();
					for (int j = 0; j < artistList.getLength(); j++) {
						Node node = artistList.item(j);
						if (
							node.getNodeType() == Node.ELEMENT_NODE &&
							node.getNodeName().equals("name-credit") &&
							node instanceof Element
						) {
							Element artistElement = getChildElement((Element) node, "artist");
							if (artistElement != null) {
								Element artistNameElement = getChildElement(artistElement, "name");
								if (artistNameElement != null) {
									release.artists.add(artistNameElement.getTextContent());
								}
							}

						}
					}
				}
				if (isNotBlank(release.id)) {
					releaseList.add(release);
				}
			}
		}
		return releaseList;
	}

	private ArrayList<ReleaseRecord> parseRecording(final Document document) {
		NodeList nodeList = document.getDocumentElement().getElementsByTagName("recording-list");
		if (nodeList.getLength() < 1) {
			return null;
		}
		Element listElement = (Element) nodeList.item(0); // recording-list
		nodeList = listElement.getElementsByTagName("recording");
		if (nodeList.getLength() < 1) {
			return null;
		}

		int nodeListLength = nodeList.getLength();
		ArrayList<ReleaseRecord> releaseList = new ArrayList<>(nodeListLength);
		for (int i = 0; i < nodeListLength; i++) {
			if (nodeList.item(i) instanceof Element) {
				Element recordingElement = (Element) nodeList.item(i);
				ReleaseRecord releaseTemplate = new ReleaseRecord();

				try {
					releaseTemplate.score = Integer.parseInt(recordingElement.getAttribute("ext:score"));
				} catch (NumberFormatException e) {
					releaseTemplate.score = 0;
				}

				try {
					releaseTemplate.title = getChildElement(recordingElement, "title").getTextContent();
				} catch (NullPointerException e) {
					releaseTemplate.title = null;
				}

				Element artists = getChildElement(recordingElement, "artist-credit");
				if (artists != null && artists.getChildNodes().getLength() > 0) {
					NodeList artistList = artists.getChildNodes();
					for (int j = 0; j < artistList.getLength(); j++) {
						Node node = artistList.item(j);
						if (
							node.getNodeType() == Node.ELEMENT_NODE &&
							node.getNodeName().equals("name-credit") &&
							node instanceof Element
						) {
							Element artistElement = getChildElement((Element) node, "artist");
							if (artistElement != null) {
								Element artistNameElement = getChildElement(artistElement, "name");
								if (artistNameElement != null) {
									releaseTemplate.artists.add(artistNameElement.getTextContent());
								}
							}

						}
					}
				}

				Element releaseListElement = getChildElement(recordingElement, "release-list");
				if (releaseListElement != null) {
					NodeList releaseNodeList = releaseListElement.getElementsByTagName("release");
					int releaseNodeListLength = releaseNodeList.getLength();
					for (int j = 0; j < releaseNodeListLength; j++) {
						ReleaseRecord release = new ReleaseRecord(releaseTemplate);
						Element releaseElement = (Element) releaseNodeList.item(j);
						release.id = releaseElement.getAttribute("id");
						Element releaseGroup = getChildElement(releaseElement, "release-group");
						if (releaseGroup != null) {
							try {
								release.type = ReleaseType.valueOf(getChildElement(releaseGroup, "primary-type").getTextContent());
							} catch (IllegalArgumentException | NullPointerException e) {
								release.type = null;
							}
						}
						try {
							release.album = getChildElement(releaseElement, "title").getTextContent();
						} catch (NullPointerException e) {
							release.album = null;
						}
						Element releaseYear = getChildElement(releaseElement, "date");
						if (releaseYear != null) {
							release.year = StringUtil.getYear(releaseYear.getTextContent());
						} else {
							release.year = -1;
						}

						if (isNotBlank(release.id)) {
							releaseList.add(release);
						}
					}
				}
			}
		}
		return releaseList;
	}

	private void cleanCache(boolean force) {
		synchronized (thumbnailCache) {
			if (force || System.currentTimeMillis() >= lastThumbnailCacheClean + THUMBNAIL_CACHE_CLEAN_INTERVAL) {
				for (
					Iterator<Entry<String, CachedThumbnail>> iterator = thumbnailCache.entrySet().iterator();
					iterator.hasNext();
				) {
					Entry<String, CachedThumbnail> entry = iterator.next();
					if (entry.getValue().isDisposable()) {
						iterator.remove();
					}
				}
				lastThumbnailCacheClean = System.currentTimeMillis();
			}
		}
	}

	private static enum ReleaseType {
		Single,
		Album,
		EP,
		Broadcast,
		Other
	}

	@SuppressWarnings("checkstyle:VisibilityModifier")
	private static class ReleaseRecord {

		/** The release ID */
		String id;

		/** The score */
		int score;

		/** The song title */
		String title;

		/** The album name */
		String album;

		/** The {@link List} of artists */
		final List<String> artists = new ArrayList<>();

		/** The {@link ReleaseType} */
		ReleaseType type;

		/** The release year */
		int year;

		public ReleaseRecord() {
		}

		public ReleaseRecord(ReleaseRecord source) {
			id = source.id;
			score = source.score;
			title = source.title;
			album = source.album;
			type = source.type;
			year = source.year;
			for (String artist : source.artists) {
				artists.add(artist);
			}
		}
	}
}
