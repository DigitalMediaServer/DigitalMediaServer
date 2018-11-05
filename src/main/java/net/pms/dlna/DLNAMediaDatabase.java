/*
 * PS3 Media Server, for streaming any medias to your PS3.
 * Copyright (C) 2008  A.Brochard
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2
 * of the License only.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package net.pms.dlna;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import net.pms.Messages;
import net.pms.PMS;
import net.pms.dlna.DLNAMediaInfo.RateMode;
import net.pms.formats.Format;
import net.pms.formats.FormatType;
import net.pms.formats.v2.SubtitleType;
import net.pms.image.ImageInfo;
import net.pms.media.VideoLevel;
import net.pms.service.Services;
import net.pms.util.Rational;
import static org.apache.commons.lang3.StringUtils.*;
import org.h2.api.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * This class provides methods for creating and maintaining the database where
 * media information is stored. Scanning media and interpreting the data is
 * intensive, so the database is used to cache scanned information to be reused
 * later.
 */
public class DLNAMediaDatabase implements Runnable {
	private static final Logger LOGGER = LoggerFactory.getLogger(DLNAMediaDatabase.class);

	public static final String NONAME = "###";
	private Thread scanner;
	private int dbCount;

	/**
	 * The database version should be incremented when we change anything to
	 * do with the database since the last released version.
	 */
	private final String latestVersion = "12";

	// Database column sizes
	private final int SIZE_CODECV = 32;
	private final int SIZE_FRAMERATE = 32;
	private final int SIZE_VIDEO_PROFILE = 40;
	private final int SIZE_CONTAINER = 32;
	private final int SIZE_MATRIX_COEFFICIENTS = 16;
	private final int SIZE_MUXINGMODE = 32;
	private final int SIZE_FRAMERATE_MODE = 16;
	private final int SIZE_STEREOSCOPY = 255;
	private final int SIZE_LANG = 3;
	private final int SIZE_TITLE = 255;
	private final int SIZE_CODECA = 32;
	private final int SIZE_ALBUM = 255;
	private final int SIZE_ARTIST = 255;
	private final int SIZE_SONGNAME = 255;
	private final int SIZE_GENRE = 64;

	/**
	 * Initializes the database for use, performing checks and creating a new
	 * database if necessary.
	 *
	 * @param force whether to recreate the database even if it isn't necessary.
	 */
	@SuppressFBWarnings("SQL_NONCONSTANT_STRING_PASSED_TO_EXECUTE")
	public synchronized void init(boolean force) {
		dbCount = -1;
		String version = null;
		boolean trace = LOGGER.isTraceEnabled();

		try (Connection connection = Services.tableManager().getConnection()) {
			if (connection == null) {
				LOGGER.error("Can't initialize database since TableManager isn't connected");
				return;
			}

			try (
				Statement statement = connection.createStatement();
				ResultSet resultSet = statement.executeQuery("SELECT count(*) FROM FILES");
			) {
				if (resultSet.next()) {
					dbCount = resultSet.getInt(1);
				}
			}

			try (
				Statement statement = connection.createStatement();
				ResultSet resultSet = statement.executeQuery("SELECT VALUE FROM METADATA WHERE KEY = 'VERSION'");
			) {
				if (resultSet.next()) {
					version = resultSet.getString(1);
				}
			}
		} catch (SQLException se) {
			if (se.getErrorCode() != ErrorCode.TABLE_OR_VIEW_NOT_FOUND_1) { // Don't log exception "Table "FILES" not found" which will be corrected in following step
				LOGGER.error("Database initialization error: {}", se.getMessage());
				LOGGER.trace("", se);
			}
		}

		// Recreate database if it is not the latest version.
		boolean force_reinit = !latestVersion.equals(version);
		if (force || dbCount == -1 || force_reinit) {
			LOGGER.debug("Database will be (re)initialized");
			try (Connection connection = Services.tableManager().getConnection()) {
				if (connection == null) {
					LOGGER.error("Can't initialize database since TableManager isn't connected");
					return;
				}

				try {
					LOGGER.trace("DROPPING TABLE FILES");
					executeUpdate(connection, "DROP TABLE FILES");
					LOGGER.trace("DROPPING TABLE METADATA");
					executeUpdate(connection, "DROP TABLE METADATA");
					LOGGER.trace("DROPPING TABLE REGEXP_RULES");
					executeUpdate(connection, "DROP TABLE REGEXP_RULES");
					LOGGER.trace("DROPPING TABLE AUDIOTRACKS");
					executeUpdate(connection, "DROP TABLE AUDIOTRACKS");
					LOGGER.trace("DROPPING TABLE SUBTRACKS");
					executeUpdate(connection, "DROP TABLE SUBTRACKS");
				} catch (SQLException se) {
					if (se.getErrorCode() != ErrorCode.TABLE_OR_VIEW_NOT_FOUND_1) { // Don't log exception "Table "FILES" not found" which will be corrected in following step
						LOGGER.error("SQL error while dropping tables: {}", se.getMessage());
						LOGGER.trace("", se);
					}
				}

				try {
					StringBuilder sb = new StringBuilder();
					sb.append("CREATE TABLE FILES (");
					sb.append("  ID                      INT AUTO_INCREMENT");
					sb.append(", FILENAME                VARCHAR2(1024)   NOT NULL");
					sb.append(", MODIFIED                TIMESTAMP        NOT NULL");
					sb.append(", TYPE                    INT");
					sb.append(", DURATION                DOUBLE");
					sb.append(", BITRATE                 INT");
					sb.append(", BITRATEMODE             OTHER");
					sb.append(", WIDTH                   INT");
					sb.append(", HEIGHT                  INT");
					sb.append(", SIZE                    NUMERIC");
					sb.append(", CODECV                  VARCHAR2(").append(SIZE_CODECV).append(')');
					sb.append(", FRAMERATE               VARCHAR2(").append(SIZE_FRAMERATE).append(')');
					sb.append(", ASPECTRATIODVD          OTHER");
					sb.append(", ASPECTRATIOCONTAINER    OTHER");
					sb.append(", ASPECTRATIOVIDEOTRACK   OTHER");
					sb.append(", REFRAMES                INT");
					sb.append(", VIDEOPROFILE            VARCHAR2(").append(SIZE_VIDEO_PROFILE).append(')');
					sb.append(", VIDEOLEVEL              OTHER");
					sb.append(", IMAGEINFO               OTHER");
					sb.append(", THUMB                   OTHER");
					sb.append(", CONTAINER               VARCHAR2(").append(SIZE_CONTAINER).append(')');
					sb.append(", MUXINGMODE              VARCHAR2(").append(SIZE_MUXINGMODE).append(')');
					sb.append(", FRAMERATEMODE           VARCHAR2(").append(SIZE_FRAMERATE_MODE).append(')');
					sb.append(", STEREOSCOPY             VARCHAR2(").append(SIZE_STEREOSCOPY).append(')');
					sb.append(", MATRIXCOEFFICIENTS      VARCHAR2(").append(SIZE_MATRIX_COEFFICIENTS).append(')');
					sb.append(", TITLECONTAINER          VARCHAR2(").append(SIZE_TITLE).append(')');
					sb.append(", TITLEVIDEOTRACK         VARCHAR2(").append(SIZE_TITLE).append(')');
					sb.append(", VIDEOTRACKCOUNT         INT");
					sb.append(", IMAGECOUNT              INT");
					sb.append(", BITDEPTH                INT");
					sb.append(", PIXELASPECTRATIO        OTHER");
					sb.append(", SCANTYPE                OTHER");
					sb.append(", SCANORDER               OTHER");
					sb.append(", constraint PK1 primary key (FILENAME, MODIFIED, ID))");
					if (trace) {
						LOGGER.trace("Creating table FILES with:\n\n{}\n", sb.toString());
					}
					executeUpdate(connection, sb.toString());

					sb = new StringBuilder();
					sb.append("CREATE TABLE AUDIOTRACKS (");
					sb.append("  FILEID            INT              NOT NULL");
					sb.append(", ID                INT              NOT NULL");
					sb.append(", LANG              VARCHAR2(").append(SIZE_LANG).append(')');
					sb.append(", TITLE             VARCHAR2(").append(SIZE_TITLE).append(')');
					sb.append(", NRAUDIOCHANNELS   NUMERIC");
					sb.append(", SAMPLEFREQ        INT");
					sb.append(", CODECA            VARCHAR2(").append(SIZE_CODECA).append(')');
					sb.append(", BITSPERSAMPLE     INT");
					sb.append(", ALBUM             VARCHAR2(").append(SIZE_ALBUM).append(')');
					sb.append(", ARTIST            VARCHAR2(").append(SIZE_ARTIST).append(')');
					sb.append(", SONGNAME          VARCHAR2(").append(SIZE_SONGNAME).append(')');
					sb.append(", GENRE             VARCHAR2(").append(SIZE_GENRE).append(')');
					sb.append(", YEAR              INT");
					sb.append(", TRACK             INT");
					sb.append(", DELAY             INT");
					sb.append(", MUXINGMODE        VARCHAR2(").append(SIZE_MUXINGMODE).append(')');
					sb.append(", BITRATE           INT");
					sb.append(", BITRATEMODE       OTHER");
					sb.append(", constraint PKAUDIO primary key (FILEID, ID))");
					if (trace) {
						LOGGER.trace("Creating table AUDIOTRACKS with:\n\n{}\n", sb.toString());
					}
					executeUpdate(connection, sb.toString());

					sb = new StringBuilder();
					sb.append("CREATE TABLE SUBTRACKS (");
					sb.append("  FILEID   INT              NOT NULL");
					sb.append(", ID       INT              NOT NULL");
					sb.append(", LANG     VARCHAR2(").append(SIZE_LANG).append(')');
					sb.append(", TITLE    VARCHAR2(").append(SIZE_TITLE).append(')');
					sb.append(", TYPE     INT");
					sb.append(", constraint PKSUB primary key (FILEID, ID))");
					if (trace) {
						LOGGER.trace("Creating table SUBTRACKS with:\n\n{}\n", sb.toString());
					}
					executeUpdate(connection, sb.toString());

					LOGGER.trace("Creating table METADATA");
					executeUpdate(connection, "CREATE TABLE METADATA (KEY VARCHAR2(255) NOT NULL, VALUE VARCHAR2(255) NOT NULL)");
					executeUpdate(connection, "INSERT INTO METADATA VALUES ('VERSION', '" + latestVersion + "')");

					LOGGER.trace("Creating index IDXARTIST");
					executeUpdate(connection, "CREATE INDEX IDXARTIST on AUDIOTRACKS (ARTIST asc);");

					LOGGER.trace("Creating index IDXALBUM");
					executeUpdate(connection, "CREATE INDEX IDXALBUM on AUDIOTRACKS (ALBUM asc);");

					LOGGER.trace("Creating index IDXGENRE");
					executeUpdate(connection, "CREATE INDEX IDXGENRE on AUDIOTRACKS (GENRE asc);");

					LOGGER.trace("Creating index IDXYEAR");
					executeUpdate(connection, "CREATE INDEX IDXYEAR on AUDIOTRACKS (YEAR asc);");

					LOGGER.trace("Creating table REGEXP_RULES");
					executeUpdate(connection, "CREATE TABLE REGEXP_RULES ( ID VARCHAR2(255) PRIMARY KEY, RULE VARCHAR2(255), ORDR NUMERIC);");
					executeUpdate(connection, "INSERT INTO REGEXP_RULES VALUES ( '###', '(?i)^\\W.+', 0 );");
					executeUpdate(connection, "INSERT INTO REGEXP_RULES VALUES ( '0-9', '(?i)^\\d.+', 1 );");

					// Retrieve the alphabet property value and split it
					String[] chars = Messages.getString("DLNAMediaDatabase.1").split(",");

					for (int i = 0; i < chars.length; i++) {
						// Create regexp rules for characters with a sort order based on the property value
						executeUpdate(connection, "INSERT INTO REGEXP_RULES VALUES ( '" + chars[i] + "', '(?i)^" + chars[i] + ".+', " + (i + 2) + " );");
					}

					LOGGER.debug("Database initialized");
				} catch (SQLException se) {
					LOGGER.error("Error creating tables: {}", se.getMessage());
					LOGGER.trace("", se);
				}
			} catch (SQLException e) {
				LOGGER.error("Error closing database connection: {}", e.getMessage());
				LOGGER.trace("", e);
			}
		} else {
			LOGGER.debug("Database file count: {}", dbCount);
			LOGGER.debug("Database version: {}", latestVersion);
		}
	}

	private static void executeUpdate(Connection conn, String sql) throws SQLException {
		if (conn != null) {
			try (Statement stmt = conn.createStatement()) {
				stmt.executeUpdate(sql);
			}
		}
	}

	/**
	 * Checks whether a row representing a {@link DLNAMediaInfo} instance for
	 * the given media exists in the database.
	 *
	 * @param name the full path of the media.
	 * @param modified the current {@code lastModified} value of the media file.
	 * @return {@code true} if the data exists for this media, {@code false}
	 *         otherwise.
	 */
	public synchronized boolean isDataExists(String name, long modified) {
		boolean found = false;
		try (
			Connection connection = Services.tableManager().getConnection();
			PreparedStatement stmt = connection == null ? null : connection.prepareStatement(
				"SELECT * FROM FILES WHERE FILENAME = ? AND MODIFIED = ?"
			);
		) {
			if (connection == null || stmt == null) {
				LOGGER.error("Can't check for data existence since TableManager isn't connected");
				return false;
			}
			stmt.setString(1, name);
			stmt.setTimestamp(2, new Timestamp(modified));
			try (
				ResultSet rs = stmt.executeQuery();
			) {
				while (rs.next()) {
					found = true;
				}
			}
		} catch (SQLException se) {
			LOGGER.error("An SQL error occurred when trying to check if data exists for \"{}\": {}", name, se.getMessage());
			LOGGER.trace("", se);
			return false;
		}
		return found;
	}

	/**
	 * Gets rows of {@link DLNAMediaDatabase} from the database and returns them
	 * as a {@link List} of {@link DLNAMediaInfo} instances.
	 *
	 * @param name the full path of the media.
	 * @param modified the current {@code lastModified} value of the media file.
	 * @return The {@link List} of {@link DLNAMediaInfo} instances matching
	 *         {@code name} and {@code modified}.
	 * @throws SQLException if an SQL error occurs during the operation.
	 * @throws IOException if an IO error occurs during the operation.
	 */
	public synchronized ArrayList<DLNAMediaInfo> getData(String name, long modified) throws IOException, SQLException {
		ArrayList<DLNAMediaInfo> list = new ArrayList<>();
		try (
			Connection connection = Services.tableManager().getConnection();
			PreparedStatement stmt = connection == null ? null : connection.prepareStatement(
				"SELECT * FROM FILES WHERE FILENAME = ? AND MODIFIED = ?"
			);
		) {
			if (connection == null || stmt == null) {
				LOGGER.error("Can't get data since TableManager isn't connected");
				return list;
			}
			stmt.setString(1, name);
			stmt.setTimestamp(2, new Timestamp(modified));
			try (
				ResultSet rs = stmt.executeQuery();
				PreparedStatement audios = connection.prepareStatement("SELECT * FROM AUDIOTRACKS WHERE FILEID = ?");
				PreparedStatement subs = connection.prepareStatement("SELECT * FROM SUBTRACKS WHERE FILEID = ?")
			) {
				while (rs.next()) {
					DLNAMediaInfo media = new DLNAMediaInfo();
					int id = rs.getInt("ID");
					media.setDuration(toDouble(rs, "DURATION"));
					media.setBitRate(rs.getInt("BITRATE"));
					media.setBitRateMode((RateMode) rs.getObject("BITRATEMODE"));
					media.setWidth(rs.getInt("WIDTH"));
					media.setHeight(rs.getInt("HEIGHT"));
					media.setSize(rs.getLong("SIZE"));
					media.setCodecV(rs.getString("CODECV"));
					media.setFrameRate(rs.getString("FRAMERATE"));
					media.setAspectRatioDvdIso((Rational) rs.getObject("ASPECTRATIODVD"));
					media.setAspectRatioContainer((Rational) rs.getObject("ASPECTRATIOCONTAINER"));
					media.setAspectRatioVideoTrack((Rational) rs.getObject("ASPECTRATIOVIDEOTRACK"));
					media.setReferenceFrameCount(rs.getInt("REFRAMES"));
					media.setVideoProfile(rs.getString("VIDEOPROFILE"));
					media.setVideoLevel((VideoLevel) rs.getObject("VIDEOLEVEL"));
					media.setImageInfo((ImageInfo) rs.getObject("IMAGEINFO"));
					media.setThumb((DLNAThumbnail) rs.getObject("THUMB"));
					media.setContainer(rs.getString("CONTAINER"));
					media.setMuxingMode(rs.getString("MUXINGMODE"));
					media.setFrameRateMode(rs.getString("FRAMERATEMODE"));
					media.setStereoscopy(rs.getString("STEREOSCOPY"));
					media.setMatrixCoefficients(rs.getString("MATRIXCOEFFICIENTS"));
					media.setFileTitleFromMetadata(rs.getString("TITLECONTAINER"));
					media.setVideoTrackTitleFromMetadata(rs.getString("TITLEVIDEOTRACK"));
					media.setVideoTrackCount(rs.getInt("VIDEOTRACKCOUNT"));
					media.setImageCount(rs.getInt("IMAGECOUNT"));
					media.setVideoBitDepth(rs.getInt("BITDEPTH"));
					media.setPixelAspectRatio((Rational) rs.getObject("PIXELASPECTRATIO"));
					media.setScanType((DLNAMediaInfo.ScanType) rs.getObject("SCANTYPE"));
					media.setScanOrder((DLNAMediaInfo.ScanOrder) rs.getObject("SCANORDER"));
					media.setMediaparsed(true);

					audios.setInt(1, id);
					try (ResultSet elements = audios.executeQuery()) {
						while (elements.next()) {
							DLNAMediaAudio audio = new DLNAMediaAudio();
							audio.setId(elements.getInt("ID"));
							audio.setLang(elements.getString("LANG"));
							audio.setAudioTrackTitleFromMetadata(elements.getString("TITLE"));
							audio.setNumberOfChannels(elements.getInt("NRAUDIOCHANNELS"));
							audio.setSampleFrequency(elements.getInt("SAMPLEFREQ"));
							audio.setCodecA(elements.getString("CODECA"));
							audio.setBitsPerSample(elements.getInt("BITSPERSAMPLE"));
							audio.setAlbum(elements.getString("ALBUM"));
							audio.setArtist(elements.getString("ARTIST"));
							audio.setSongname(elements.getString("SONGNAME"));
							audio.setGenre(elements.getString("GENRE"));
							audio.setYear(elements.getInt("YEAR"));
							audio.setTrack(elements.getInt("TRACK"));
							audio.setDelay(elements.getInt("DELAY"));
							audio.setMuxingModeAudio(elements.getString("MUXINGMODE"));
							audio.setBitRate(elements.getInt("BITRATE"));
							audio.setBitRateMode((RateMode) rs.getObject("BITRATEMODE"));
							media.getAudioTracksList().add(audio);
						}
					}
					subs.setInt(1, id);
					try (ResultSet elements = subs.executeQuery()) {
						while (elements.next()) {
							DLNAMediaSubtitle sub = new DLNAMediaSubtitle();
							sub.setId(elements.getInt("ID"));
							sub.setLang(elements.getString("LANG"));
							sub.setSubtitlesTrackTitleFromMetadata(elements.getString("TITLE"));
							sub.setType(SubtitleType.valueOfStableIndex(elements.getInt("TYPE")));
							media.getSubtitleTracksList().add(sub);
						}
					}

					list.add(media);
				}
			}
		} catch (SQLException se) {
			if (se.getCause() != null && se.getCause() instanceof IOException) {
				throw (IOException) se.getCause();
			}
			throw se;
		}
		return list;
	}

	private static Double toDouble(ResultSet rs, String column) throws SQLException {
		Object obj = rs.getObject(column);
		if (obj instanceof Double) {
			return (Double) obj;
		}
		return null;
	}

	private void insertOrUpdateSubtitleTracks(Connection connection, int fileId, DLNAMediaInfo media) throws SQLException {
		if (connection == null || fileId < 0 || media == null || media.getSubTrackCount() < 1) {
			return;
		}

		/* XXX This is flawed, multiple subtitle tracks with the same language will
		 * overwrite each other.
		 */
		try (
			PreparedStatement updateStatment = connection.prepareStatement(
				"SELECT " +
					"FILEID, ID, LANG, TITLE, TYPE " +
				"FROM SUBTRACKS " +
				"WHERE " +
					"FILEID = ? AND ID = ?",
				ResultSet.TYPE_FORWARD_ONLY,
				ResultSet.CONCUR_UPDATABLE
			);
			PreparedStatement insertStatement = connection.prepareStatement(
				"INSERT INTO SUBTRACKS (" +
					"FILEID, ID, LANG, TITLE, TYPE " +
				") VALUES (" +
					"?, ?, ?, ?, ?" +
				")"
			);
		) {
			for (DLNAMediaSubtitle subtitleTrack : media.getSubtitleTracksList()) {
				updateStatment.setInt(1, fileId);
				updateStatment.setInt(2, subtitleTrack.getId());
				try (ResultSet rs = updateStatment.executeQuery()) {
					if (rs.next()) {
						rs.updateString("LANG", left(subtitleTrack.getLang(), SIZE_LANG));
						rs.updateString("TITLE", left(subtitleTrack.getSubtitlesTrackTitleFromMetadata(), SIZE_TITLE));
						rs.updateInt("TYPE", subtitleTrack.getType().getStableIndex());
						rs.updateRow();
					} else {
						insertStatement.clearParameters();
						insertStatement.setInt(1, fileId);
						insertStatement.setInt(2, subtitleTrack.getId());
						insertStatement.setString(3, left(subtitleTrack.getLang(), SIZE_LANG));
						insertStatement.setString(4, left(subtitleTrack.getSubtitlesTrackTitleFromMetadata(), SIZE_TITLE));
						insertStatement.setInt(5, subtitleTrack.getType().getStableIndex());
						insertStatement.executeUpdate();
					}
				}
			}
		} catch (SQLException e) {
			connection.rollback();
			throw e;
		}
	}

	private void insertOrUpdateAudioTracks(Connection connection, int fileId, DLNAMediaInfo media) throws SQLException {
		if (connection == null || fileId < 0 || media == null || media.getAudioTrackCount() < 1) {
			return;
		}

		/* XXX This is flawed, multiple audio tracks with the same language will
		 * overwrite each other.
		 */
		try (
			PreparedStatement updateStatment = connection.prepareStatement(
				"SELECT " +
					"FILEID, ID, LANG, TITLE, NRAUDIOCHANNELS, SAMPLEFREQ, CODECA, " +
					"BITSPERSAMPLE, ALBUM, ARTIST, SONGNAME, GENRE, YEAR, TRACK, " +
					"DELAY, MUXINGMODE, BITRATE, BITRATEMODE " +
				"FROM AUDIOTRACKS " +
				"WHERE " +
					"FILEID = ? AND ID = ?",
				ResultSet.TYPE_FORWARD_ONLY,
				ResultSet.CONCUR_UPDATABLE
			);
			PreparedStatement insertStatement = connection.prepareStatement(
				"INSERT INTO AUDIOTRACKS (" +
					"FILEID, ID, LANG, TITLE, NRAUDIOCHANNELS, SAMPLEFREQ, CODECA, BITSPERSAMPLE, " +
					"ALBUM, ARTIST, SONGNAME, GENRE, YEAR, TRACK, DELAY, MUXINGMODE, BITRATE, BITRATEMODE" +
				") VALUES (" +
					"?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?" +
				")"
			);
		) {
			for (DLNAMediaAudio audioTrack : media.getAudioTracksList()) {
				updateStatment.setInt(1, fileId);
				updateStatment.setInt(2, audioTrack.getId());
				try (ResultSet rs = updateStatment.executeQuery()) {
					if (rs.next()) {
						rs.updateString("LANG", left(audioTrack.getLang(), SIZE_LANG));
						rs.updateString("TITLE", left(audioTrack.getAudioTrackTitleFromMetadata(), SIZE_TITLE));
						rs.updateInt("NRAUDIOCHANNELS", audioTrack.getNumberOfChannelsRaw());
						rs.updateInt("SAMPLEFREQ", audioTrack.getSampleFrequencyRaw());
						rs.updateString("CODECA", left(audioTrack.getCodecA(), SIZE_CODECA));
						rs.updateInt("BITSPERSAMPLE", audioTrack.getBitsPerSampleRaw());
						rs.updateString("ALBUM", left(trimToEmpty(audioTrack.getAlbum()), SIZE_ALBUM));
						rs.updateString("ARTIST", left(trimToEmpty(audioTrack.getArtist()), SIZE_ARTIST));
						rs.updateString("SONGNAME", left(trimToEmpty(audioTrack.getSongname()), SIZE_SONGNAME));
						rs.updateString("GENRE", left(trimToEmpty(audioTrack.getGenre()), SIZE_GENRE));
						rs.updateInt("YEAR", audioTrack.getYear());
						rs.updateInt("TRACK", audioTrack.getTrack());
						rs.updateInt("DELAY", audioTrack.getDelayRaw());
						rs.updateString("MUXINGMODE", left(trimToEmpty(audioTrack.getMuxingModeAudio()), SIZE_MUXINGMODE));
						rs.updateInt("BITRATE", audioTrack.getBitRateRaw());
						updateSerialized(rs, audioTrack.getBitRateModeRaw(), "BITRATEMODE");
						rs.updateRow();
					} else {
						insertStatement.clearParameters();
						insertStatement.setInt(1, fileId);
						insertStatement.setInt(2, audioTrack.getId());
						insertStatement.setString(3, left(audioTrack.getLang(), SIZE_LANG));
						insertStatement.setString(4, left(audioTrack.getAudioTrackTitleFromMetadata(), SIZE_TITLE));
						insertStatement.setInt(5, audioTrack.getNumberOfChannelsRaw());
						insertStatement.setInt(6, audioTrack.getSampleFrequencyRaw());
						insertStatement.setString(7, left(audioTrack.getCodecA(), SIZE_CODECA));
						insertStatement.setInt(8, audioTrack.getBitsPerSampleRaw());
						insertStatement.setString(9, left(trimToEmpty(audioTrack.getAlbum()), SIZE_ALBUM));
						insertStatement.setString(10, left(trimToEmpty(audioTrack.getArtist()), SIZE_ARTIST));
						insertStatement.setString(11, left(trimToEmpty(audioTrack.getSongname()), SIZE_SONGNAME));
						insertStatement.setString(12, left(trimToEmpty(audioTrack.getGenre()), SIZE_GENRE));
						insertStatement.setInt(13, audioTrack.getYear());
						insertStatement.setInt(14, audioTrack.getTrack());
						insertStatement.setInt(15, audioTrack.getDelayRaw());
						insertStatement.setString(16, left(trimToEmpty(audioTrack.getMuxingModeAudio()), SIZE_MUXINGMODE));
						insertStatement.setInt(17, audioTrack.getBitRateRaw());
						insertSerialized(insertStatement, audioTrack.getBitRateModeRaw(), 18);
						insertStatement.executeUpdate();
					}
				}
			}
		} catch (SQLException e) {
			connection.rollback();
			throw e;
		}
	}

	protected void updateSerialized(ResultSet rs, Object x, String columnLabel) throws SQLException {
		if (x != null) {
			rs.updateObject(columnLabel, x);
		} else {
			rs.updateNull(columnLabel);
		}
	}

	protected void insertSerialized(PreparedStatement ps, Object x, int parameterIndex) throws SQLException {
		if (x != null) {
			ps.setObject(parameterIndex, x);
		} else {
			ps.setNull(parameterIndex, Types.OTHER);
		}
	}

	/**
	 * Inserts or updates a database row representing an {@link DLNAMediaInfo}
	 * instance. If the row already exists, it will be updated with the
	 * information given in {@code media}. If it doesn't exist, a new will row
	 * be created using the same information.
	 *
	 * @param name the full path of the media.
	 * @param modified the current {@code lastModified} value of the media file.
	 * @param type the integer constant from {@link Format} indicating the type
	 *            of media.
	 * @param media the {@link DLNAMediaInfo} row to update.
	 * @throws SQLException if an SQL error occurs during the operation.
	 */
	public synchronized void insertOrUpdateData(String name, long modified, FormatType type, DLNAMediaInfo media) throws SQLException {
		try (Connection connection = Services.tableManager().getConnection()) {
			if (connection == null) {
				LOGGER.error("Can't insert or update data since TableManager isn't connected");
				return;
			}
			connection.setAutoCommit(false);
			int fileId = -1;
			try (PreparedStatement ps = connection.prepareStatement(
				"SELECT " +
					"ID, FILENAME, MODIFIED, TYPE, DURATION, BITRATE, BITRATEMODE, WIDTH, HEIGHT, SIZE, CODECV, " +
					"FRAMERATE, ASPECTRATIODVD, ASPECTRATIOCONTAINER, ASPECTRATIOVIDEOTRACK, REFRAMES, VIDEOPROFILE, " +
					"VIDEOLEVEL, IMAGEINFO, THUMB, CONTAINER, MUXINGMODE, FRAMERATEMODE, STEREOSCOPY, MATRIXCOEFFICIENTS, " +
					"TITLECONTAINER, TITLEVIDEOTRACK, VIDEOTRACKCOUNT, IMAGECOUNT, BITDEPTH, PIXELASPECTRATIO, " +
					"SCANTYPE, SCANORDER " +
				"FROM FILES " +
				"WHERE " +
					"FILENAME = ?",
				ResultSet.TYPE_FORWARD_ONLY,
				ResultSet.CONCUR_UPDATABLE
			)) {
				ps.setString(1, name);
				try (ResultSet rs = ps.executeQuery()) {
					if (rs.next()) {
						fileId = rs.getInt("ID");
						rs.updateTimestamp("MODIFIED", new Timestamp(modified));
						rs.updateInt("TYPE", type == null ? 0 : type.getValue());
						if (media != null) {
							if (media.getDuration() != null) {
								rs.updateDouble("DURATION", media.getDurationInSeconds());
							} else {
								rs.updateNull("DURATION");
							}

							rs.updateInt("BITRATE", media.getMediaType() == MediaType.IMAGE ? 0 : media.getBitRate());
							updateSerialized(rs, media.getBitRateMode(), "BITRATEMODE");
							rs.updateInt("WIDTH", media.getWidth());
							rs.updateInt("HEIGHT", media.getHeight());
							rs.updateLong("SIZE", media.getSize());
							rs.updateString("CODECV", left(media.getCodecV(), SIZE_CODECV));
							rs.updateString("FRAMERATE", left(media.getFrameRate(), SIZE_FRAMERATE));
							updateSerialized(rs, media.getAspectRatioDvdIso(), "ASPECTRATIODVD");
							updateSerialized(rs, media.getAspectRatioContainer(), "ASPECTRATIOCONTAINER");
							updateSerialized(rs, media.getAspectRatioVideoTrack(), "ASPECTRATIOVIDEOTRACK");
							rs.updateInt("REFRAMES", media.getReferenceFrameCount());
							rs.updateString("VIDEOPROFILE", left(media.getVideoProfile(), SIZE_VIDEO_PROFILE));
							updateSerialized(rs, media.getVideoLevel(), "VIDEOLEVEL");
							updateSerialized(rs, media.getImageInfo(), "IMAGEINFO");
							updateSerialized(rs, media.getThumb(), "THUMB");
							rs.updateString("CONTAINER", left(media.getContainer(), SIZE_CONTAINER));
							rs.updateString("MUXINGMODE", left(media.getMuxingModeAudio(), SIZE_MUXINGMODE));
							rs.updateString("FRAMERATEMODE", left(media.getFrameRateMode(), SIZE_FRAMERATE_MODE));
							rs.updateString("STEREOSCOPY", left(media.getStereoscopy(), SIZE_STEREOSCOPY));
							rs.updateString("MATRIXCOEFFICIENTS", left(media.getMatrixCoefficients(), SIZE_MATRIX_COEFFICIENTS));
							rs.updateString("TITLECONTAINER", left(media.getFileTitleFromMetadata(), SIZE_TITLE));
							rs.updateString("TITLEVIDEOTRACK", left(media.getVideoTrackTitleFromMetadata(), SIZE_TITLE));
							rs.updateInt("VIDEOTRACKCOUNT", media.getVideoTrackCount());
							rs.updateInt("IMAGECOUNT", media.getImageCount());
							rs.updateInt("BITDEPTH", media.getVideoBitDepth());
							updateSerialized(rs, media.getPixelAspectRatio(), "PIXELASPECTRATIO");
							updateSerialized(rs, media.getScanType(), "SCANTYPE");
							updateSerialized(rs, media.getScanOrder(), "SCANORDER");
						}
						rs.updateRow();
					}
				}
			} catch (SQLException e) {
				connection.rollback();
				throw e;
			}
			if (fileId < 0) {
				// No fileId means it didn't exist
				try (
					PreparedStatement ps = connection.prepareStatement(
						"INSERT INTO FILES (FILENAME, MODIFIED, TYPE, DURATION, BITRATE, BITRATEMODE, WIDTH, HEIGHT, SIZE, CODECV, " +
						"FRAMERATE, ASPECTRATIODVD, ASPECTRATIOCONTAINER, ASPECTRATIOVIDEOTRACK, REFRAMES, VIDEOPROFILE, VIDEOLEVEL, " +
						"IMAGEINFO, THUMB, CONTAINER, MUXINGMODE, FRAMERATEMODE, STEREOSCOPY, MATRIXCOEFFICIENTS, TITLECONTAINER, " +
						"TITLEVIDEOTRACK, VIDEOTRACKCOUNT, IMAGECOUNT, BITDEPTH, PIXELASPECTRATIO, SCANTYPE, SCANORDER) VALUES " +
						"(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")
				) {
					ps.setString(1, name);
					ps.setTimestamp(2, new Timestamp(modified));
					ps.setInt(3, type == null ? 0 : type.getValue());
					if (media != null) {
						if (media.getDuration() != null) {
							ps.setDouble(4, media.getDurationInSeconds());
						} else {
							ps.setNull(4, Types.DOUBLE);
						}

						ps.setInt(5, media.getMediaType() == MediaType.IMAGE ? 0 : media.getBitRate());
						insertSerialized(ps, media.getBitRateMode(), 6);
						ps.setInt(7, media.getWidth());
						ps.setInt(8, media.getHeight());
						ps.setLong(9, media.getSize());
						ps.setString(10, left(media.getCodecV(), SIZE_CODECV));
						ps.setString(11, left(media.getFrameRate(), SIZE_FRAMERATE));
						insertSerialized(ps, media.getAspectRatioDvdIso(), 12);
						insertSerialized(ps, media.getAspectRatioContainer(), 13);
						insertSerialized(ps, media.getAspectRatioVideoTrack(), 14);
						ps.setInt(15, media.getReferenceFrameCount());
						ps.setString(16, left(media.getVideoProfile(), SIZE_VIDEO_PROFILE));
						insertSerialized(ps, media.getVideoLevel(), 17);
						insertSerialized(ps, media.getImageInfo(), 18);
						insertSerialized(ps, media.getThumb(), 19);
						ps.setString(20, left(media.getContainer(), SIZE_CONTAINER));
						ps.setString(21, left(media.getMuxingModeAudio(), SIZE_MUXINGMODE));
						ps.setString(22, left(media.getFrameRateMode(), SIZE_FRAMERATE_MODE));
						ps.setString(23, left(media.getStereoscopy(), SIZE_STEREOSCOPY));
						ps.setString(24, left(media.getMatrixCoefficients(), SIZE_MATRIX_COEFFICIENTS));
						ps.setString(25, left(media.getFileTitleFromMetadata(), SIZE_TITLE));
						ps.setString(26, left(media.getVideoTrackTitleFromMetadata(), SIZE_TITLE));
						ps.setInt(27, media.getVideoTrackCount());
						ps.setInt(28, media.getImageCount());
						ps.setInt(29, media.getVideoBitDepth());
						insertSerialized(ps, media.getPixelAspectRatio(), 30);
						insertSerialized(ps, media.getScanType(), 31);
						insertSerialized(ps, media.getScanOrder(), 32);
					} else {
						ps.setString(4, null);
						ps.setInt(5, 0);
						ps.setNull(6, Types.OTHER);
						ps.setInt(7, 0);
						ps.setInt(8, 0);
						ps.setLong(9, 0);
						ps.setNull(10, Types.VARCHAR);
						ps.setNull(11, Types.VARCHAR);
						ps.setNull(12, Types.VARCHAR);
						ps.setNull(13, Types.VARCHAR);
						ps.setNull(14, Types.VARCHAR);
						ps.setByte(15, (byte) -1);
						ps.setNull(16, Types.VARCHAR);
						ps.setNull(17, Types.OTHER);
						ps.setNull(18, Types.OTHER);
						ps.setNull(19, Types.OTHER);
						ps.setNull(20, Types.VARCHAR);
						ps.setNull(21, Types.VARCHAR);
						ps.setNull(22, Types.VARCHAR);
						ps.setNull(23, Types.VARCHAR);
						ps.setNull(24, Types.VARCHAR);
						ps.setNull(25, Types.VARCHAR);
						ps.setNull(26, Types.VARCHAR);
						ps.setInt(27, 0);
						ps.setInt(28, 0);
						ps.setInt(29, 0);
						ps.setNull(30, Types.OTHER);
						ps.setNull(31, Types.OTHER);
						ps.setNull(32, Types.OTHER);
					}
					ps.executeUpdate();
					try (ResultSet rs = ps.getGeneratedKeys()) {
						if (rs.next()) {
							fileId = rs.getInt(1);
						}
					}
				} catch (SQLException e) {
					connection.rollback();
					throw e;
				}
			}

			if (media != null && fileId > -1) {
				insertOrUpdateAudioTracks(connection, fileId, media);
				insertOrUpdateSubtitleTracks(connection, fileId, media);
			}

			connection.commit();
			connection.setAutoCommit(true);
		} catch (SQLException se) {
			if (se.getErrorCode() == 23505) {
				throw new SQLException(String.format(
					"Duplicate key while adding \"%s\" to the cache: %s",
					name,
					se.getMessage()
				), se);
			}
			throw se;
		}
	}

	public synchronized void deleteThumbnails() {
		try (
			Connection connection = Services.tableManager().getConnection();
			PreparedStatement preparedStatement = connection == null ? null : connection.prepareStatement(
				"UPDATE FILES SET THUMB = ?"
			);
		) {
			if (connection == null || preparedStatement == null) {
				LOGGER.error("Can't delete thumbnails since TableManager isn't connected");
				return;
			}
			preparedStatement.setNull(1, Types.OTHER);
			preparedStatement.executeUpdate();
		} catch (SQLException se) {
			LOGGER.error("Error deleting cached thumbnails: {}", se.getMessage());
			LOGGER.trace("", se);
		}
	}

	public synchronized void updateThumbnail(String name, long modified, DLNAMediaInfo media) {
		try (
			Connection connection = Services.tableManager().getConnection();
			PreparedStatement preparedStatement = connection == null ? null : connection.prepareStatement(
				"UPDATE FILES SET THUMB = ? WHERE FILENAME = ? AND MODIFIED = ?"
			);
		) {
			if (connection == null || preparedStatement == null) {
				LOGGER.error("Can't update thumbnail since TableManager isn't connected");
				return;
			}
			preparedStatement.setString(2, name);
			preparedStatement.setTimestamp(3, new Timestamp(modified));
			if (media != null && media.getThumb() != null) {
				preparedStatement.setObject(1, media.getThumb());
			} else {
				preparedStatement.setNull(1, Types.OTHER);
			}
			preparedStatement.executeUpdate();
		} catch (SQLException se) {
			LOGGER.error("Error updating cached thumbnail for \"{}\": {}", media, se.getMessage());
			LOGGER.trace("", se);
		}
	}

	public synchronized ArrayList<String> getStrings(String sql) {
		ArrayList<String> list = new ArrayList<>();
		try (Connection connection = Services.tableManager().getConnection()) {
			if (connection == null) {
				LOGGER.error("Can't get strings since TableManager isn't connected");
				return null;
			}
			try (
				PreparedStatement preparedStatement = connection.prepareStatement(sql);
				ResultSet resultSet = preparedStatement.executeQuery();
			) {
				while (resultSet.next()) {
					String str = resultSet.getString(1);
					if (isBlank(str)) {
						if (!list.contains(NONAME)) {
							list.add(NONAME);
						}
					} else if (!list.contains(str)) {
						list.add(str);
					}
				}
			}
		} catch (SQLException se) {
			LOGGER.error("An error occured while getting strings: {}", se.getMessage());
			LOGGER.trace("", se);
			return null;
		}
		return list;
	}

	public synchronized void cleanup() {
		try (Connection connection = Services.tableManager().getConnection()) {
			if (connection == null) {
				LOGGER.error("Can't cleanup database since TableManager isn't connected");
				return;
			}
			try (
				PreparedStatement preparedStatement = connection.prepareStatement("SELECT COUNT(*) FROM FILES");
				ResultSet resultSet = preparedStatement.executeQuery();
			) {
				dbCount = 0;

				if (resultSet.next()) {
					dbCount = resultSet.getInt(1);
				}
			}

			PMS.get().getFrame().setStatusLine(Messages.getString("DLNAMediaDatabase.2") + " 0%");
			int i = 0;
			int oldpercent = 0;

			if (dbCount > 0) {
				try (
					PreparedStatement preparedStatement = connection.prepareStatement(
						"SELECT FILENAME, MODIFIED, ID FROM FILES",
						ResultSet.TYPE_FORWARD_ONLY,
						ResultSet.CONCUR_UPDATABLE
					);
					ResultSet resultSet = preparedStatement.executeQuery();
				) {
					while (resultSet.next()) {
						String filename = resultSet.getString("FILENAME");
						long modified = resultSet.getTimestamp("MODIFIED").getTime();
						File file = new File(filename);
						if (!file.exists() || file.lastModified() != modified) {
							resultSet.deleteRow();
						}
						i++;
						int newpercent = i * 100 / dbCount;
						if (newpercent > oldpercent) {
							PMS.get().getFrame().setStatusLine(Messages.getString("DLNAMediaDatabase.2") + newpercent + "%");
							oldpercent = newpercent;
						}
					}
				}
			}
		} catch (SQLException se) {
			LOGGER.error("An error occured while cleaning up the database: {}", se.getMessage());
			LOGGER.trace("", se);
		}
	}

	public synchronized ArrayList<File> getFiles(String sql) {
		ArrayList<File> list = new ArrayList<>();
		try (Connection connection = Services.tableManager().getConnection()) {
			if (connection == null) {
				LOGGER.error("Can't get files since TableManager isn't connected");
				return null;
			}

			try (
				PreparedStatement preparedStatement = connection.prepareStatement(
					sql.toLowerCase().startsWith("select") ? sql : ("SELECT FILENAME, MODIFIED FROM FILES WHERE " + sql)
				);
				ResultSet resultSet = preparedStatement.executeQuery();
			) {
				while (resultSet.next()) {
					String filename = resultSet.getString("FILENAME");
					long modified = resultSet.getTimestamp("MODIFIED").getTime();
					File file = new File(filename);
					if (file.exists() && file.lastModified() == modified) {
						list.add(file);
					}
				}
			}
		} catch (SQLException se) {
			LOGGER.error("An error occured while getting files: {}", se.getMessage());
			LOGGER.trace("", se);
			return null;
		}
		return list;
	}

	public boolean isScanLibraryRunning() {
		return scanner != null && scanner.isAlive();
	}

	public void scanLibrary() {
		if (isScanLibraryRunning()) {
			LOGGER.info("Cannot start library scanner: A scan is already in progress");
		} else {
			scanner = new Thread(this, "Library Scanner");
			scanner.start();
		}
	}

	public void stopScanLibrary() {
		if (isScanLibraryRunning()) {
			PMS.get().getRootFolder(null).stopScan();
		}
	}

	@Override
	public void run() {
		try {
			PMS.get().getRootFolder(null).scan();
		} catch (Exception e) {
			LOGGER.error("Unhandled exception during library scan: {}", e.getMessage());
			LOGGER.trace("", e);
		}
	}
}
