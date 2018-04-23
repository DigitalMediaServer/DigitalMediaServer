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
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

	/**
	 * tableLock is used to synchronize database access on table level.
	 * H2 calls are thread safe, but the database's multithreading support is
	 * described as experimental. This lock therefore used in addition to SQL
	 * transaction locks. All access to this table must be guarded with this
	 * lock. The lock allows parallel reads.
	 */
	private static final ReadWriteLock tableLock = new ReentrantReadWriteLock();
	private static final Logger LOGGER = LoggerFactory.getLogger(TableCoverArtArchive.class);
	private static final TableId ID = TableId.COVER_ART_ARCHIVE;

	/**
	 * Table version must be increased every time a change is done to the table
	 * definition. Table upgrade SQL must also be added to
	 * {@link #upgradeTable()}
	 */
	private static final int TABLE_VERSION = 1;

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
					"MODIFIED DATETIME, " +
					"MBID VARCHAR(36), " +
					"COVER BLOB, " +
				")");
			statement.execute("CREATE INDEX MBID_IDX ON " + ID + "(MBID)");
		}
	}

	/**
	 * This method <b>MUST</b> be updated if the table definition are altered.
	 * The changes for each version in the form of {@code ALTER TABLE} must be
	 * implemented here.
	 * <p>
	 * For an implementation example see
	 * {@link TableMusicBrainzReleases#upgradeTable(Connection, int)}.
	 */
	@SuppressWarnings("unused")
	@Override
	protected void upgradeTable(@Nonnull Connection connection, int currentVersion) throws SQLException {
		LOGGER.info("Upgrading database table \"{}\" from version {} to {}", ID, currentVersion, TABLE_VERSION);
		if (currentVersion < 1) {
			currentVersion = 1;
		}
		tableLock.writeLock().lock();
		try {
			for (int version = currentVersion; version < TABLE_VERSION; version++) {
				LOGGER.trace("Upgrading table {} from version {} to {}", ID, version, version + 1);
				switch (version) {
					//case 1: Alter table to version 2
					default:
						throw new IllegalStateException(
							"Table \"" + ID + "is missing table upgrade commands from version " +
							version + " to " + TABLE_VERSION
						);
				}
			}
			setTableVersion(connection, ID, TABLE_VERSION);
		} finally {
			tableLock.writeLock().unlock();
		}
	}

	/**
	 * Stores the cover {@link Blob} with the given {@code MBID} in the
	 * database.
	 *
	 * @param mBID the {@code MBID} to store.
	 * @param cover the cover as a {@link Blob}.
	 * @throws IllegalArgumentException If {@code mBID} is blank.
	 */
	public void writeMBID(@Nonnull String mBID, @Nullable byte[] cover) {
		if (isBlank(mBID)) {
			throw new IllegalArgumentException("mBID cannot be blank");
		}
		boolean trace = LOGGER.isTraceEnabled();

		try (Connection connection = getConnection()) {
			String query = "SELECT * FROM " + ID + contructMBIDWhere(mBID);
			if (trace) {
				LOGGER.trace("Searching for Cover Art Archive cover with \"{}\" before update", query);
			}

			tableLock.writeLock().lock();
			try (Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
				connection.setAutoCommit(false);
				try (ResultSet result = statement.executeQuery(query)) {
					if (result.next()) {
						if (cover != null || result.getBlob("COVER") == null) {
							if (trace) {
								LOGGER.trace("Updating cover for MBID \"{}\"", mBID);
							}
							result.updateTimestamp("MODIFIED", new Timestamp(System.currentTimeMillis()));
							if (cover != null) {
								result.updateBytes("COVER", cover);
							} else {
								result.updateNull("COVER");
							}
							result.updateRow();
						} else if (trace) {
							LOGGER.trace("Leaving row {} alone since previous information seems better", result.getInt("ID"));
						}
					} else {
						if (trace) {
							LOGGER.trace("Inserting new cover for MBID \"{}\"", mBID);
						}

						result.moveToInsertRow();
						result.updateTimestamp("MODIFIED", new Timestamp(System.currentTimeMillis()));
						result.updateString("MBID", mBID);
						if (cover != null) {
							result.updateBytes("COVER", cover);
						}
						result.insertRow();
					}
				} finally {
					connection.commit();
				}
			} finally {
				tableLock.writeLock().unlock();
			}
		} catch (SQLException e) {
			LOGGER.error("Database error while writing Cover Art Archive cover for MBID \"{}\": {}", mBID, e.getMessage());
			LOGGER.trace("", e);
		}
	}

	/**
	 * Looks up cover in the table based on the given {@code MBID} and returns
	 * the result as a {@link CoverArtArchiveResult}.
	 *
	 * @param mBID the {@code MBID} to search for.
	 *
	 * @return The resulting {@link CoverArtArchiveResult}.
	 */
	@Nonnull
	public CoverArtArchiveResult findMBID(@Nullable String mBID) {
		boolean trace = LOGGER.isTraceEnabled();
		CoverArtArchiveResult result;

		try (Connection connection = getConnection()) {
			String query = "SELECT COVER, MODIFIED FROM " + ID + contructMBIDWhere(mBID);

			if (trace) {
				LOGGER.trace("Searching for cover with \"{}\"", query);
			}

			tableLock.readLock().lock();
			try (Statement statement = connection.createStatement()) {
				try (ResultSet resultSet = statement.executeQuery(query)) {
					if (resultSet.next()) {
						result = new CoverArtArchiveResult(true, resultSet.getTimestamp("MODIFIED"), resultSet.getBytes("COVER"));
					} else {
						result = new CoverArtArchiveResult(false, null, null);
					}
				}
			} finally {
				tableLock.readLock().unlock();
			}
		} catch (SQLException e) {
			LOGGER.error(
				"Database error while looking up Cover Art Archive cover for MBID \"{}\": {}",
				mBID,
				e.getMessage()
			);
			LOGGER.trace("", e);
			result = new CoverArtArchiveResult(false, null, null);
		}

		return result;
	}

	private static String contructMBIDWhere(String mBID) {
		return " WHERE MBID" + sqlNullIfBlank(mBID, true, false);
	}

	/**
	 * A class for holding the results from a Cover Art Archive database lookup.
	 */
	@Immutable
	public static class CoverArtArchiveResult {

		private final boolean found;
		private final Timestamp modified;
		private final byte[] cover;

		/**
		 * Creates a new instance holding the specified values.
		 *
		 * @param found {@code true} if found, {@code false} otherwise.
		 * @param modified the modified {@link Timestamp}.
		 * @param cover the cover byte array.
		 */
		@SuppressFBWarnings("EI_EXPOSE_REP2")
		public CoverArtArchiveResult(boolean found, @Nullable Timestamp modified, @Nullable byte[] cover) {
			this.found = found;
			this.modified = modified;
			this.cover = cover;
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
		 * @return The cover byte array.
		 */
		@Nullable
		@SuppressFBWarnings("EI_EXPOSE_REP")
		public byte[] getCover() {
			return cover;
		}
	}
}
