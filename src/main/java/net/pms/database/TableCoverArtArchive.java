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
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.EnumSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import org.h2.api.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.pms.dlna.DLNABinaryThumbnail;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;


/**
 * This class is responsible for managing the Cover Art Archive table. It
 * does everything from creating, checking and upgrading the table to
 * performing lookups, updates and inserts. All operations involving this table
 * shall be done with this class.
 *
 * @author Nadahar
 */
public final class TableCoverArtArchive extends Table {

	private static final Logger LOGGER = LoggerFactory.getLogger(TableCoverArtArchive.class);
	private static final TableId ID = TableId.COVER_ART_ARCHIVE;

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
	TableCoverArtArchive(@Nonnull TableManager tableManager) {
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
					"COVER BLOB, " +
					"THUMBNAIL OTHER" +
				")");
			statement.execute("CREATE INDEX MBID_IDX ON " + ID + "(MBID)");
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
						// Version 2 adds field THUMBNAIL and renames MODIFIED to EXPIRES.
						statement.executeUpdate("ALTER TABLE " + ID + " ADD COLUMN THUMBNAIL OTHER");
						statement.executeUpdate("ALTER TABLE " + ID + " ALTER COLUMN MODIFIED RENAME TO EXPIRES");
						break;
					default:
						throw new IllegalStateException(
							"Table \"" + ID + "is missing table upgrade commands from version " +
							version + " to " + TABLE_VERSION
						);
				}
			}
			setTableVersion(connection, ID, TABLE_VERSION);
		} catch (SQLException e) {
			connection.rollback();
			throw e;
		} finally {
			connection.setAutoCommit(true);
		}
	}

	/**
	 * Stores the cover {@link Blob} with the given {@code MBID} in the
	 * database.
	 *
	 * @param mBID the {@code MBID} to store.
	 * @param cover the cover as a byte array.
	 * @param thumbnail the {@link DLNABinaryThumbnail}.
	 * @param expires the milliseconds since January 1, 1970, 00:00:00 GMT on
	 *            which this entry expires.
	 * @throws IllegalArgumentException If {@code mBID} is blank.
	 */
	public void writeMBID(@Nonnull String mBID, @Nullable byte[] cover, @Nullable DLNABinaryThumbnail thumbnail, long expires) {
		if (isBlank(mBID)) {
			throw new IllegalArgumentException("mBID cannot be blank");
		}
		boolean trace = LOGGER.isTraceEnabled();

		try (Connection connection = getConnection()) {
			String query = "SELECT * FROM " + ID + contructMBIDWhere(mBID);
			if (trace) {
				LOGGER.trace("Searching for Cover Art Archive cover with \"{}\" before update", query);
			}

			connection.setAutoCommit(false);
			try (Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
				try (ResultSet result = statement.executeQuery(query)) {
					if (result.next()) {
						if (cover != null || result.getBlob("COVER") == null) {
							if (trace) {
								LOGGER.trace("Updating cover for MBID \"{}\"", mBID);
							}
							result.updateTimestamp("EXPIRES", new Timestamp(expires));
							if (cover != null) {
								result.updateBytes("COVER", cover);
							} else {
								result.updateNull("COVER");
							}
							if (thumbnail == null) {
								result.updateNull("THUMBNAIL");
							} else {
								result.updateObject("THUMBNAIL", thumbnail);
							}
							result.updateRow();
						} else if (trace) {
							LOGGER.trace("Leaving row {} alone since the previous information seems better", result.getInt("ID"));
						}
					} else {
						if (trace) {
							LOGGER.trace(
								"Inserting new {}cover for MBID \"{}\"",
								cover == null || cover.length == 0 ? "empty " : "",
								mBID
							);
						}

						result.moveToInsertRow();
						result.updateTimestamp("EXPIRES", new Timestamp(expires));
						result.updateString("MBID", mBID);
						if (cover != null) {
							result.updateBytes("COVER", cover);
						}
						if (thumbnail != null) {
							result.updateObject("THUMBNAIL", thumbnail);
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
			LOGGER.error("Database error while writing Cover Art Archive cover for MBID \"{}\": {}", mBID, e.getMessage());
			LOGGER.trace("", e);
		}
	}

	/**
	 * Looks up cover in the table based on the given {@code MBID} and returns
	 * the result as a {@link CoverArtArchiveEntry}.
	 *
	 * @param mBID the {@code MBID} to search for.
	 *
	 * @return The resulting {@link CoverArtArchiveEntry}.
	 */
	@Nonnull
	public CoverArtArchiveEntry findMBID(@Nullable String mBID) {
		boolean trace = LOGGER.isTraceEnabled();
		CoverArtArchiveEntry result;

		try (Connection connection = getConnection()) {
			String query = "SELECT COVER, EXPIRES, THUMBNAIL FROM " + ID + contructMBIDWhere(mBID);

			if (trace) {
				LOGGER.trace("Searching for cover with \"{}\"", query);
			}

			try (Statement statement = connection.createStatement()) {
				try (ResultSet resultSet = statement.executeQuery(query)) {
					if (resultSet.next()) {
						DLNABinaryThumbnail thumbnail;
						try {
							thumbnail = (DLNABinaryThumbnail) resultSet.getObject("THUMBNAIL");
						} catch (Exception e) {
							thumbnail = null;
							if (trace) {
								LOGGER.trace(
									"Deserialization failed for MBID \"{}\", returning null: {}",
									mBID,
									e.getMessage()
								);
							}
						}
						result = new CoverArtArchiveEntry(
							true,
							resultSet.getTimestamp("EXPIRES"),
							resultSet.getBytes("COVER"),
							thumbnail
						);
					} else {
						result = new CoverArtArchiveEntry(false, null, null, null);
					}
				}
			}
		} catch (SQLException e) {
			LOGGER.error(
				"Database error while looking up Cover Art Archive cover for MBID \"{}\": {}",
				mBID,
				e.getMessage()
			);
			LOGGER.trace("", e);
			result = new CoverArtArchiveEntry(false, null, null, null);
		}

		return result;
	}

	/**
	 * Looks up thumbnail with the specified {@code MBID}.
	 *
	 * @param mBID the MBID to look up.
	 *
	 * @return The {@link DLNABinaryThumbnail} or {@code null}.
	 * @throws SQLException If a SQL error occurs during the operation.
	 */
	public DLNABinaryThumbnail getThumbnail(String mBID) throws SQLException {
		if (mBID == null) {
			return null;
		}
		boolean trace = LOGGER.isTraceEnabled();

		try (Connection connection = getConnection()) {
			String query = "SELECT THUMBNAIL FROM " + ID + " WHERE MBID = " + sqlQuote(mBID);

			if (trace) {
				LOGGER.trace("Looking up thumbnail for MBID \"{}\" with \"{}\"", mBID, query);
			}

			try (
				Statement statement = connection.createStatement();
				ResultSet resultSet = statement.executeQuery(query);
			) {
				if (resultSet.next()) {
					try {
						return (DLNABinaryThumbnail) resultSet.getObject("THUMBNAIL");
					} catch (Exception e) {
						if (trace) {
							LOGGER.trace(
								"Deserialization failed for MBID \"{}\", returning null: {}",
								mBID,
								e.getMessage()
							);
						}
						return null;
					}
				}
				if (trace) {
					LOGGER.trace("No thumbnail found for MBID \"{}\"", mBID);
				}
				return null;
			}
		}
	}

	/**
	 * Looks up thumbnail with the specified {@code MBID}.
	 *
	 * @param mBID the MBID to look up.
	 * @param thumbnail the {@link DLNABinaryThumbnail} to store.
	 *
	 * @throws SQLException If a SQL error occurs during the operation.
	 * @throws IllegalArgumentException If {@code mBID} is {@code null}.
	 */
	public void updateThumbnail(String mBID, DLNABinaryThumbnail thumbnail) throws SQLException {
		if (mBID == null) {
			throw new IllegalArgumentException("mBID cannot be null");
		}
		boolean trace = LOGGER.isTraceEnabled();

		try (Connection connection = getConnection()) {
			String query = "SELECT * FROM " + ID + " WHERE MBID = " + sqlQuote(mBID);
			if (trace) {
				LOGGER.trace("Searching for Cover Art Archive cover with \"{}\" before update", query);
			}

			connection.setAutoCommit(false);
			try (Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
				try (ResultSet result = statement.executeQuery(query)) {
					if (result.next()) {
						if (thumbnail == null) {
							result.updateNull("THUMBNAIL");
						} else {
							result.updateObject("THUMBNAIL", thumbnail);
						}
						result.updateRow();
					} else {
						throw new SQLException("Row for MBID \"" + mBID + "\" not found", "02000", ErrorCode.NO_DATA_AVAILABLE);
					}
				}
				connection.commit();
			} catch (SQLException e) {
				connection.rollback();
				throw e;
			} finally {
				connection.setAutoCommit(true);
			}
		}
	}

	private static String contructMBIDWhere(String mBID) {
		return " WHERE MBID" + sqlNullIfBlank(mBID, true, false);
	}

	/**
	 * A class for holding the results from a Cover Art Archive database lookup.
	 */
	@Immutable
	public static class CoverArtArchiveEntry {

		private final boolean found;
		private final Timestamp expires;
		private final byte[] cover;
		private final DLNABinaryThumbnail thumbnail;

		/**
		 * Creates a new instance holding the specified values.
		 *
		 * @param found {@code true} if found, {@code false} otherwise.
		 * @param expires the expiration time {@link Timestamp}.
		 * @param cover the cover byte array.
		 * @param thumbnail the {@link DLNABinaryThumbnail}.
		 */
		@SuppressFBWarnings("EI_EXPOSE_REP2")
		public CoverArtArchiveEntry(
			boolean found,
			@Nullable Timestamp expires,
			@Nullable byte[] cover,
			@Nullable DLNABinaryThumbnail thumbnail
		) {
			this.found = found;
			this.expires = expires;
			this.cover = cover;
			this.thumbnail = thumbnail;
		}

		/**
		 * @return {@code true} if found, {@code false} otherwise.
		 */
		public boolean isFound() {
			return found;
		}

		/**
		 * @return The milliseconds since January 1, 1970, 00:00:00 GMT on which
		 *         this entry expires.
		 */
		@Nullable
		public long getExpires() {
			return expires == null ? 0 : expires.getTime();
		}

		/**
		 * @return The cover byte array.
		 */
		@Nullable
		@SuppressFBWarnings("EI_EXPOSE_REP")
		public byte[] getCover() {
			return cover;
		}

		/**
		 * @return The cover {@link DLNABinaryThumbnail}.
		 */
		@Nullable
		public DLNABinaryThumbnail getThumbnail() {
			return thumbnail;
		}
	}
}
