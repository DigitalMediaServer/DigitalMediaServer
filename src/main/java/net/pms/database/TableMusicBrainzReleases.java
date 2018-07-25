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
package net.pms.database;

import static net.pms.database.Tables.*;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.left;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.EnumSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import net.pms.image.thumbnail.CoverArtArchiveTagInfo;
import net.pms.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * This class is responsible for managing the MusicBrainz releases table. It
 * does everything from creating, checking and upgrading the table to
 * performing lookups, updates and inserts. All operations involving this table
 * shall be done with this class.
 *
 * @author Nadahar
 */

public final class TableMusicBrainzReleases extends Table {

	private static final Logger LOGGER = LoggerFactory.getLogger(TableMusicBrainzReleases.class);
	private static final TableId ID = TableId.MUSIC_BRAINZ_RELEASES;

	/**
	 * Table version must be increased every time a change is done to the table
	 * definition. Table upgrade SQL must also be added to
	 * {@link #upgradeTable()}
	 */
	private static final int TABLE_VERSION = 3;

	/**
	 * Should only be instantiated by {@link TableManager}.
	 *
	 * @param tableManager the {@link TableManager} to use.
	 */
	TableMusicBrainzReleases(@Nonnull TableManager tableManager) {
		super(tableManager);
	}

	@Override
	@Nonnull
	public TableId getTableId() {
		return ID;
	}

	@Override
	public int getTableVersion() {
		return TABLE_VERSION;
	}

	@Override
	@Nullable
	public EnumSet<TableId> getRelatedTables() {
		return null;
	}

	@Override
	protected void createTable(@Nonnull Connection connection) throws SQLException {
		LOGGER.debug("Creating database table \"{}\"", ID);
		try (Statement statement = connection.createStatement()) {
			statement.execute(
				"CREATE TABLE " + ID + "(" +
					"ID IDENTITY PRIMARY KEY, " +
					"EXPIRES DATETIME, " +
					"MBID VARCHAR(36), " +
					"ARTIST VARCHAR(1000), " +
					"ALBUM VARCHAR(1000), " +
					"TITLE VARCHAR(1000), " +
					"YEAR INT, " +
					"TRACK_NO INT, " +
					"NUM_TRACKS INT, " +
					"ARTIST_ID VARCHAR(36), " +
					"TRACK_ID VARCHAR(36)" +
				")");
			statement.execute("CREATE INDEX ARTIST_IDX ON " + ID + "(ARTIST)");
			statement.execute("CREATE INDEX ARTIST_ID_IDX ON " + ID + "(ARTIST_ID)"); //TODO: (Nad) Check indexes
		}
	}

	@Override
	protected void upgradeTable(@Nonnull Connection connection, int currentVersion) throws SQLException {
		LOGGER.info("Upgrading database table \"{}\" from version {} to {}", ID, currentVersion, TABLE_VERSION);
		if (currentVersion < 1) {
			currentVersion = 1;
		}
		connection.setAutoCommit(false);
		try (Statement statement = connection.createStatement()) {
			for (int version = currentVersion; version < TABLE_VERSION; version++) {
				LOGGER.trace("Upgrading table {} from version {} to {}", ID, version, version + 1);
				switch (version) {
					case 1:
						// Version 2 increases the size of ARTIST; ALBUM, TITLE and YEAR.
						statement.executeUpdate("ALTER TABLE " + ID + " ALTER COLUMN ARTIST VARCHAR(1000)");
						statement.executeUpdate("ALTER TABLE " + ID + " ALTER COLUMN ALBUM VARCHAR(1000)");
						statement.executeUpdate("ALTER TABLE " + ID + " ALTER COLUMN TITLE VARCHAR(1000)");
						statement.executeUpdate("ALTER TABLE " + ID + " ALTER COLUMN YEAR VARCHAR(20)");
						break;
					case 2:
						// Version 2 renames MODIFIED to EXPIRES and changes YEAR from VARCHAR(20) to INT
						statement.executeUpdate("ALTER TABLE " + ID + " ALTER COLUMN MODIFIED RENAME TO EXPIRES");
						statement.executeUpdate("ALTER TABLE " + ID + " ALTER COLUMN YEAR RENAME TO OLDYEAR");
						statement.executeUpdate("ALTER TABLE " + ID + " ADD COLUMN YEAR INT DEFAULT -1");
						try (Statement updateStatement = connection.createStatement(
								ResultSet.TYPE_SCROLL_SENSITIVE,
								ResultSet.CONCUR_UPDATABLE
						)) {
							try (
								ResultSet result = updateStatement.executeQuery(
									"SELECT ID, OLDYEAR, YEAR FROM " + ID + " WHERE OLDYEAR IS NOT NULL"
								)
							) {
								while (result.next()) {
									int year = StringUtil.getYear(result.getString("OLDYEAR"));
									if (year > 0) {
										result.updateInt("YEAR", year);
										result.updateRow();
									}
								}
							}
						}
						statement.executeUpdate("ALTER TABLE " + ID + " DROP COLUMN OLDYEAR");
						statement.executeUpdate("ALTER TABLE " + ID + " ADD COLUMN TRACK_NO INT DEFAULT -1");
						statement.executeUpdate("ALTER TABLE " + ID + " ADD COLUMN NUM_TRACKS INT DEFAULT -1");
						break;
					default:
						throw new IllegalStateException(
							"Table \"" + ID + "is missing table upgrade commands from version " +
							version + " to " + TABLE_VERSION
						);
				}
			}
			setTableVersion(connection, ID, TABLE_VERSION);
			connection.commit();
		} catch (SQLException e) {
			connection.rollback();
			throw e;
		} finally {
			connection.setAutoCommit(true);
		}
	}

	/**
	 * Stores the MBID with information from this {@link CoverArtArchiveTagInfo} in the
	 * database.
	 *
	 * @param mBID the MBID to store.
	 * @param tagInfo the {@link CoverArtArchiveTagInfo} who's information should be
	 *            associated with the given MBID.
	 * @param expires the milliseconds since January 1, 1970, 00:00:00 GMT on
	 *            which this entry expires.
	 */
	public void writeMBID(String mBID, CoverArtArchiveTagInfo tagInfo, long expires) {
		boolean trace = LOGGER.isTraceEnabled();

		try (Connection connection = getConnection()) {
			String query = "SELECT * FROM " + ID + constructTagWhere(tagInfo, true); //TODO: (Nad) Check if rows are properly selected/unique
			if (trace) {
				LOGGER.trace("Searching for release MBID with \"{}\" before update", query);
			}

			connection.setAutoCommit(false);
			try (Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
				try (ResultSet result = statement.executeQuery(query)) {
					if (result.next()) {
						if (isNotBlank(mBID) || isBlank(result.getString("MBID"))) {
							if (trace) {
								LOGGER.trace("Updating row {} to MBID \"{}\"", result.getInt("ID"), mBID);
							}
							result.updateTimestamp("EXPIRES", new Timestamp(expires));
							if (isNotBlank(mBID)) {
								result.updateString("MBID", mBID);
							} else {
								result.updateNull("MBID");
							}
							result.updateRow();
						} else if (trace) {
							LOGGER.trace(
								"Not updating row {} alone since the existing information seems better",
								result.getInt("ID")
							);
						}
					} else {
						if (trace) {
							LOGGER.trace(
								"Inserting new row for MBID \"{}\":\n" +
								"     Artist     \"{}\"\n" +
								"     Album      \"{}\"\n" +
								"     Title      \"{}\"\n" +
								"     Year       \"{}\"\n" +
								"     Track No   \"{}\"\n" +
								"     Num Tracks \"{}\"\n" +
								"     Artist ID  \"{}\"\n" +
								"     Track ID   \"{}\"\n",
								mBID, tagInfo.getArtist(), tagInfo.getAlbum(),
								tagInfo.getTitle(), tagInfo.getYear(),
								tagInfo.getTrackNumber(), tagInfo.getNumberOfTracks(),
								tagInfo.getArtistId(), tagInfo.getTrackId()
							);
						}

						result.moveToInsertRow();
						result.updateTimestamp("EXPIRES", new Timestamp(expires));
						if (isNotBlank(mBID)) {
							result.updateString("MBID", mBID);
						}
						if (isNotBlank(tagInfo.getAlbum())) {
							result.updateString("ALBUM", left(tagInfo.getAlbum(), 1000));
						}
						if (isNotBlank(tagInfo.getArtist())) {
							result.updateString("ARTIST", left(tagInfo.getArtist(), 1000));
						}
						if (isNotBlank(tagInfo.getTitle())) {
							result.updateString("TITLE", left(tagInfo.getTitle(), 1000));
						}
						if (tagInfo.getYear() > 0) {
							result.updateInt("YEAR", tagInfo.getYear());
						}
						if (tagInfo.getTrackNumber() > 0) {
							result.updateInt("TRACK_NO", tagInfo.getTrackNumber());
						}
						if (tagInfo.getNumberOfTracks() > 0) {
							result.updateInt("NUM_TRACKS", tagInfo.getNumberOfTracks());
						}
						if (isNotBlank(tagInfo.getArtistId())) {
							result.updateString("ARTIST_ID", tagInfo.getArtistId());
						}
						if (isNotBlank(tagInfo.getTrackId())) {
							result.updateString("TRACK_ID", tagInfo.getTrackId());
						}
						result.insertRow();
					}
					connection.commit();
				}
			} catch (SQLException e) {
				connection.rollback();
				throw e;
			} finally {
				connection.setAutoCommit(true);
			}
		} catch (SQLException e) {
			LOGGER.error(
				"Database error while writing Music Brainz ID \"{}\" for \"{}\": {}",
				mBID,
				tagInfo,
				e.getMessage()
			);
			LOGGER.trace("", e);
		}
	}

	/**
	 * Looks up the {@code MBID} in the table based on the specified
	 * {@link CoverArtArchiveTagInfo}.
	 *
	 * @param tagInfo the {@link CoverArtArchiveTagInfo} whose values should be
	 *            used in the search.
	 * @return The result of the search or {@code null}.
	 */
	@Nullable
	public MusicBrainzReleasesResult findMBID(CoverArtArchiveTagInfo tagInfo) {
		try (Connection connection = getConnection()) {
			String query = "SELECT MBID, EXPIRES FROM " + ID + constructTagWhere(tagInfo, false);

			LOGGER.trace("Searching for release MBID with \"{}\"", query);

			try (Statement statement = connection.createStatement()) {
				try (ResultSet resultSet = statement.executeQuery(query)) { //TODO: (Nad) Should a more sophisticated approach be used than simply picking the first?
					if (resultSet.next()) {
						return new MusicBrainzReleasesResult(resultSet.getTimestamp("EXPIRES"), resultSet.getString("MBID"));
					}
					return null;
				}
			}
		} catch (SQLException e) {
			LOGGER.error("Database error while looking up Music Brainz ID for \"{}\": {}", tagInfo, e.getMessage());
			LOGGER.trace("", e);
			return null;
		}
	}

	private static String constructTagWhere(CoverArtArchiveTagInfo tagInfo, boolean includeAll) {
		StringBuilder where = new StringBuilder(" WHERE ");
		final String and = " AND ";
		boolean added = false;

		if (includeAll || isNotBlank(tagInfo.getAlbum())) {
			where.append("ALBUM").append(sqlNullIfBlank(tagInfo.getAlbum(), true, false));
			added = true;
		}
		if (includeAll || isNotBlank(tagInfo.getArtistId())) {
			if (added) {
				where.append(and);
			}
			where.append("ARTIST_ID").append(sqlNullIfBlank(tagInfo.getArtistId(), true, false));
			added = true;
		}
		if (includeAll || (!isNotBlank(tagInfo.getArtistId()) && isNotBlank(tagInfo.getArtist()))) {
			if (added) {
				where.append(and);
			}
			where.append("ARTIST").append(sqlNullIfBlank(tagInfo.getArtist(), true, false));
			added = true;
		}

		if (
			includeAll || (
				isNotBlank(tagInfo.getTrackId()) && (
					!isNotBlank(tagInfo.getAlbum()) || !(
						isNotBlank(tagInfo.getArtist()) ||
						isNotBlank(tagInfo.getArtistId())
					)
				)
			)
		) {
			if (added) {
				where.append(and);
			}
			where.append("TRACK_ID").append(sqlNullIfBlank(tagInfo.getTrackId(), true, false));
			added = true;
		}
		if (
			includeAll || (
				!isNotBlank(tagInfo.getTrackId()) && (
					isNotBlank(tagInfo.getTitle()) && (
						!isNotBlank(tagInfo.getAlbum()) || !(
							isNotBlank(tagInfo.getArtist()) ||
							isNotBlank(tagInfo.getArtistId())
						)
					)
				)
			)
		) {
			if (added) {
				where.append(and);
			}
			where.append("TITLE").append(sqlNullIfBlank(tagInfo.getTitle(), true, false));
			added = true;
		}

		if (includeAll || tagInfo.getYear() > 0) {
			if (added) {
				where.append(and);
			}
			where.append("YEAR = ").append(tagInfo.getYear());
			added = true;
		}

		if (includeAll || tagInfo.getTrackNumber() > 0) {
			if (added) {
				where.append(and);
			}
			where.append("TRACK_NO = ").append(tagInfo.getTrackNumber());
			added = true;
		}

		if (includeAll || tagInfo.getNumberOfTracks() > 0) {
			if (added) {
				where.append(and);
			}
			where.append("NUM_TRACKS = ").append(tagInfo.getNumberOfTracks());
			added = true;
		}

		return where.toString();
	}

	/**
	 * A class for holding the results from a Music Brainz releases database
	 * lookup.
	 */
	@Immutable
	public static class MusicBrainzReleasesResult {

		private final Timestamp expires;
		private final String mBID;

		/**
		 * Creates a new instance holding the specified values.
		 *
		 * @param expires the expiration time {@link Timestamp}.
		 * @param mBID the {@code MBID}.
		 */
		@SuppressFBWarnings("EI_EXPOSE_REP2")
		public MusicBrainzReleasesResult(@Nullable Timestamp expires, @Nullable String mBID) {
			this.expires = expires;
			this.mBID = mBID;
		}

		/**
		 * @return The milliseconds since January 1, 1970, 00:00:00 GMT on which
		 *         this expires.
		 */
		@Nullable
		public long getExpires() {
			return expires == null ? 0 : expires.getTime();
		}

		/**
		 * @return The {@code MBID}.
		 */
		@Nullable
		public String getMBID() {
			return mBID;
		}
	}
}
