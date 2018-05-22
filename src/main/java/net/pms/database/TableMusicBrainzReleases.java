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
import net.pms.util.CoverArtArchiveUtil.CoverArtArchiveTagInfo;
import org.jaudiotagger.tag.Tag;
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
	private static final int TABLE_VERSION = 2;

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
					"MODIFIED DATETIME, " +
					"MBID VARCHAR(36), " +
					"ARTIST VARCHAR(1000), " +
					"ALBUM VARCHAR(1000), " +
					"TITLE VARCHAR(1000), " +
					"YEAR VARCHAR(20), " +
					"ARTIST_ID VARCHAR(36), " +
					"TRACK_ID VARCHAR(36)" +
				")");
			statement.execute("CREATE INDEX ARTIST_IDX ON " + ID + "(ARTIST)");
			statement.execute("CREATE INDEX ARTIST_ID_IDX ON " + ID + "(ARTIST_ID)");
		}
	}

	/**
	 * This method <b>MUST</b> be updated if the table definition are altered.
	 * The changes for each version in the form of {@code ALTER TABLE} must be
	 * implemented here.
	 */
	@Override
	protected void upgradeTable(@Nonnull Connection connection, int currentVersion) throws SQLException {
		LOGGER.info("Upgrading database table \"{}\" from version {} to {}", ID, currentVersion, TABLE_VERSION);
		if (currentVersion < 1) {
			currentVersion = 1;
		}
		connection.setAutoCommit(false);
		try {
			for (int version = currentVersion; version < TABLE_VERSION; version++) {
				LOGGER.trace("Upgrading table {} from version {} to {}", ID, version, version + 1);
				switch (version) {
					case 1:
						// Version 2 increases the size of ARTIST; ALBUM, TITLE and YEAR.
						Statement statement = connection.createStatement();
						statement.executeUpdate("ALTER TABLE " + ID + " ALTER COLUMN ARTIST VARCHAR(1000)");
						statement.executeUpdate("ALTER TABLE " + ID + " ALTER COLUMN ALBUM VARCHAR(1000)");
						statement.executeUpdate("ALTER TABLE " + ID + " ALTER COLUMN TITLE VARCHAR(1000)");
						statement.executeUpdate("ALTER TABLE " + ID + " ALTER COLUMN YEAR VARCHAR(20)");
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
	 * Stores the MBID with information from this {@link Tag} in the database.
	 *
	 * @param mBID the MBID to store.
	 * @param tagInfo the {@link Tag} who's information should be associated
	 *            with the given MBID.
	 */
	public void writeMBID(String mBID, CoverArtArchiveTagInfo tagInfo) {
		boolean trace = LOGGER.isTraceEnabled();

		try (Connection connection = getConnection()) {
			String query = "SELECT * FROM " + ID + constructTagWhere(tagInfo, true);
			if (trace) {
				LOGGER.trace("Searching for release MBID with \"{}\" before update", query);
			}

			connection.setAutoCommit(false);
			try (Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
				try (ResultSet result = statement.executeQuery(query)) {
					if (result.next()) {
						if (isNotBlank(mBID) || !isNotBlank(result.getString("MBID"))) {
							if (trace) {
								LOGGER.trace("Updating row {} to MBID \"{}\"", result.getInt("ID"), mBID);
							}
							result.updateTimestamp("MODIFIED", new Timestamp(System.currentTimeMillis()));
							if (isNotBlank(mBID)) {
								result.updateString("MBID", mBID);
							} else {
								result.updateNull("MBID");
							}
							result.updateRow();
						} else if (trace) {
							LOGGER.trace("Leaving row {} alone since previous information seems better", result.getInt("ID"));
						}
					} else {
						if (trace) {
							LOGGER.trace(
								"Inserting new row for MBID \"{}\":\n" +
								"     Artist    \"{}\"\n" +
								"     Album     \"{}\"\n" +
								"     Title     \"{}\"\n" +
								"     Year      \"{}\"\n" +
								"     Artist ID \"{}\"\n" +
								"     Track ID  \"{}\"\n",
								mBID, tagInfo.artist, tagInfo.album,
								tagInfo.title, tagInfo.year,
								tagInfo.artistId, tagInfo.trackId
							);
						}

						result.moveToInsertRow();
						result.updateTimestamp("MODIFIED", new Timestamp(System.currentTimeMillis()));
						if (isNotBlank(mBID)) {
							result.updateString("MBID", mBID);
						}
						if (isNotBlank(tagInfo.album)) {
							result.updateString("ALBUM", left(tagInfo.album, 1000));
						}
						if (isNotBlank(tagInfo.artist)) {
							result.updateString("ARTIST", left(tagInfo.artist, 1000));
						}
						if (isNotBlank(tagInfo.title)) {
							result.updateString("TITLE", left(tagInfo.title, 1000));
						}
						if (isNotBlank(tagInfo.year)) {
							result.updateString("YEAR", left(tagInfo.year, 20));
						}
						if (isNotBlank(tagInfo.artistId)) {
							result.updateString("ARTIST_ID", tagInfo.artistId);
						}
						if (isNotBlank(tagInfo.trackId)) {
							result.updateString("TRACK_ID", tagInfo.trackId);
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
	 * Looks up MBID in the table based on the given {@link Tag}.
	 *
	 * @param tagInfo the {@link Tag} for whose values should be used in the
	 *            search.
	 * @return The result of the search.
	 */
	@Nonnull
	public MusicBrainzReleasesResult findMBID(CoverArtArchiveTagInfo tagInfo) {
		boolean trace = LOGGER.isTraceEnabled();
		MusicBrainzReleasesResult result;

		try (Connection connection = getConnection()) {
			String query = "SELECT MBID, MODIFIED FROM " + ID + constructTagWhere(tagInfo, false);

			if (trace) {
				LOGGER.trace("Searching for release MBID with \"{}\"", query);
			}

			try (Statement statement = connection.createStatement()) {
				try (ResultSet resultSet = statement.executeQuery(query)) {
					if (resultSet.next()) {
						result = new MusicBrainzReleasesResult(true, resultSet.getTimestamp("MODIFIED"), resultSet.getString("MBID"));
					} else {
						result = new MusicBrainzReleasesResult(false, null, null);
					}
				}
			}
		} catch (SQLException e) {
			LOGGER.error("Database error while looking up Music Brainz ID for \"{}\": {}", tagInfo, e.getMessage());
			LOGGER.trace("", e);
			result = new MusicBrainzReleasesResult(false, null, null);
		}

		return result;
	}

	private static String constructTagWhere(CoverArtArchiveTagInfo tagInfo, boolean includeAll) {
		StringBuilder where = new StringBuilder(" WHERE ");
		final String and = " AND ";
		boolean added = false;

		if (includeAll || isNotBlank(tagInfo.album)) {
			where.append("ALBUM").append(sqlNullIfBlank(tagInfo.album, true, false));
			added = true;
		}
		if (includeAll || isNotBlank(tagInfo.artistId)) {
			if (added) {
				where.append(and);
			}
			where.append("ARTIST_ID").append(sqlNullIfBlank(tagInfo.artistId, true, false));
			added = true;
		}
		if (includeAll || (!isNotBlank(tagInfo.artistId) && isNotBlank(tagInfo.artist))) {
			if (added) {
				where.append(and);
			}
			where.append("ARTIST").append(sqlNullIfBlank(tagInfo.artist, true, false));
			added = true;
		}

		if (
			includeAll || (
				isNotBlank(tagInfo.trackId) && (
					!isNotBlank(tagInfo.album) || !(
						isNotBlank(tagInfo.artist) ||
						isNotBlank(tagInfo.artistId)
					)
				)
			)
		) {
			if (added) {
				where.append(and);
			}
			where.append("TRACK_ID").append(sqlNullIfBlank(tagInfo.trackId, true, false));
			added = true;
		}
		if (
			includeAll || (
				!isNotBlank(tagInfo.trackId) && (
					isNotBlank(tagInfo.title) && (
						!isNotBlank(tagInfo.album) || !(
							isNotBlank(tagInfo.artist) ||
							isNotBlank(tagInfo.artistId)
						)
					)
				)
			)
		) {
			if (added) {
				where.append(and);
			}
			where.append("TITLE").append(sqlNullIfBlank(tagInfo.title, true, false));
			added = true;
		}

		if (isNotBlank(tagInfo.year)) {
			if (added) {
				where.append(and);
			}
			where.append("YEAR").append(sqlNullIfBlank(tagInfo.year, true, false));
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

		private final boolean found;
		private final Timestamp modified;
		private final String mBID;

		/**
		 * Creates a new instance holding the specified values.
		 *
		 * @param found {@code true} if found, {@code false} otherwise.
		 * @param modified the modified {@link Timestamp}.
		 * @param mBID the {@code MBID}.
		 */
		@SuppressFBWarnings("EI_EXPOSE_REP2")
		public MusicBrainzReleasesResult(boolean found, @Nullable Timestamp modified, @Nullable String mBID) {
			this.found = found;
			this.modified = modified;
			this.mBID = mBID;
		}

		/**
		 * @return {@code true} if found, {@code false} otherwise.
		 */
		public boolean isFound() {
			return found;
		}

		/**
		 * @return The modified {@link Timestamp}.
		 */
		@Nullable
		@SuppressFBWarnings("EI_EXPOSE_REP")
		public Timestamp getModified() {
			return modified;
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
