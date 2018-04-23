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

import static org.apache.commons.lang3.StringUtils.isBlank;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import org.h2.api.ErrorCode;
import org.h2.engine.Constants;
import org.h2.jdbcx.JdbcConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.pms.Messages;
import net.pms.PMS;
import net.pms.configuration.PmsConfiguration;
import net.pms.service.Service;
import net.pms.util.StringUtil;


/**
 * This {@link Service} provides access to the database {@link Tables} and
 * {@link Table} implementation singleton instances.
 *
 * @author Nadahar
 */
public class TableManager implements Service {

	private static final Logger LOGGER = LoggerFactory.getLogger(TableManager.class);

	/** The default database name */
	private static final String DEFAULT_NAME = "DMS";

	private final String databaseName;
	private final String databaseFilename;
	private final String url;
	private final Map<TableId, Table> tables;

	@Nullable
	private final TableCoverArtArchive tableCoverArtArchive;

	@Nullable
	private final TableMusicBrainzReleases tableMusicBrainzReleases;

	@GuardedBy("this")
	private JdbcConnectionPool connectionPool;

	@GuardedBy("this")
	private boolean connected;

	@GuardedBy("this")
	private Exception lastException;

	@GuardedBy("this")
	private Set<Table> futureTables;

	/**
	 * Creates a new instance using {@value #DEFAULT_NAME} as the database name.
	 */
	public TableManager() {
		this(null);
	}

	/**
	 * Creates a new instance using the specified database name.
	 *
	 * @param databaseName the database name to use or {@code null} to use
	 *            {@value #DEFAULT_NAME}.
	 */
	public TableManager(@Nullable String databaseName) {
		this.databaseName = isBlank(databaseName) ? DEFAULT_NAME : databaseName;
		PmsConfiguration configuration = PMS.getConfiguration();
		this.databaseFilename = determineDBFilename(this.databaseName, configuration);
		this.url = buildURL(databaseFilename, configuration);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(
				"Using database file \"{}\" with URL: {}",
				this.databaseFilename + Constants.SUFFIX_MV_FILE,
				this.url
			);
		} else {
			LOGGER.info("Using database file \"{}\"", this.databaseFilename + Constants.SUFFIX_MV_FILE);
		}
		TableId[] tableIds = TableId.values();
		HashMap<TableId, Table> tempTables = new HashMap<>(tableIds.length - 1);
		for (TableId tableId : TableId.values()) {
			if (tableId != TableId.TABLES) {
				tempTables.put(tableId, tableId.newInstance(this));
			}
		}
		this.tables = Collections.unmodifiableMap(tempTables);

		// Set constants to frequently used table instances
		this.tableCoverArtArchive = (TableCoverArtArchive) tables.get(TableId.COVER_ART_ARCHIVE);
		this.tableMusicBrainzReleases = (TableMusicBrainzReleases) tables.get(TableId.MUSIC_BRAINZ_RELEASES);

		start();
	}

	/**
	 * Starts this {@link TableManager}. This will be called automatically by the
	 * constructor, and need only be called if the previous call failed or
	 * {@link #stop()} has been called.
	 */
	@Override
	public synchronized void start() {
		if (connected) {
			LOGGER.debug("TableManager is already started, ignoring start request");
			return;
		}
		LOGGER.debug("Starting check of tables for database {}", databaseName);
		lastException = null;
		futureTables = null;

		try {
			Class.forName("org.h2.Driver");
		} catch (ClassNotFoundException e) {
			lastException = e;
			return;
		}

		createConnectionPool();
		try (Connection connection = connectionPool.getConnection()) {
			Tables tablesInstance = new Tables(this);
			tablesInstance.checkTable(connection);

			futureTables = checkFutureTableVersions(connection);
			if (futureTables.isEmpty()) {
				for (Table table : tables.values()) {
					table.checkTable(connection);
				}
				connected = true;
			} else {
				LOGGER.debug("The database has too new database tables");
				clearConnectionPool();
			}
		} catch (SQLException e) {
			LOGGER.error("Database tables check failed with: {}", e.getMessage());
			LOGGER.trace("", e);
			lastException = e;
			clearConnectionPool();
		}
	}

	/**
	 * Stops this {@link TableManager}. This will cause the connection pool to
	 * shut down and this {@link TableManager} to go into a disconnected state.
	 * Existing connections will continue to work until they are closed.
	 */
	@Override
	public synchronized void stop() {
		clearConnectionPool();
		connected = false;
	}

	@Override
	public synchronized boolean isAlive() {
		return connected;
	}

	/**
	 * @return {@code true} if an error was registered during the last call to
	 *         {@link #start()}, {@code false} otherwise.
	 */
	public synchronized boolean isError() {
		return lastException != null || futureTables != null && !futureTables.isEmpty();
	}

	/**
	 * @return {@code true} if there is an error and that error is
	 *         {@link ErrorCode#DATABASE_ALREADY_OPEN_1}, {@code false}
	 *         otherwise.
	 */
	public synchronized boolean isAlreadyOpenError() {
		return
			lastException instanceof SQLException &&
			((SQLException) lastException).getErrorCode() == ErrorCode.DATABASE_ALREADY_OPEN_1;
	}

	/**
	 * @return {@code true} if there is an error and that error is
	 *         {@link ErrorCode#FILE_VERSION_ERROR_1}, {@code false}
	 *         otherwise.
	 */
	public synchronized boolean isWrongVersionOrCorrupt() {
		return
			lastException instanceof SQLException &&
			((SQLException) lastException).getErrorCode() == ErrorCode.FILE_VERSION_ERROR_1;
	}

	/**
	 * @return {@code true} if tables of too new version was registered during
	 *         the last call to {@link #start()}, {@code false} otherwise.
	 */
	public synchronized boolean hasFutureTables() {
		return futureTables != null && !futureTables.isEmpty();
	}

	/**
	 * Generates a sorted, readable string representation of the registered
	 * "future" tables.
	 *
	 * @param includeRelated if {@code true} related tables of the "future"
	 *            tables are also included in the result.
	 * @param rootLanguage {@code true} to generate the string using the "root"
	 *            language (untranslated), {@code false} to generate the string
	 *            using translated components.
	 * @return The sorted, readable {@link String}.
	 */
	@Nonnull
	public String getFutureTablesString(boolean includeRelated, boolean rootLanguage) {
		List<TableId> tableIds = getFutureTables(includeRelated, true, rootLanguage);
		if (tableIds.isEmpty()) {
			return rootLanguage ? Messages.getRootString("Generic.None") : Messages.getString("Generic.None");
		}
		String and = rootLanguage ? Messages.getRootString("Generic.And") : Messages.getString("Generic.And");
		ArrayList<String> names = new ArrayList<String>(tableIds.size());
		for (TableId tableId : tableIds) {
			names.add(rootLanguage ? tableId.getRootDisplayName() : tableId.getDisplayName());
		}
		return StringUtil.createReadableCombinedString(names, ",", and);
	}

	/**
	 * Generates a {@link List} of "future" {@link Table}s.
	 *
	 * @param includeRelated {@code true} to include related tables.
	 * @param sort {@code true} to sort the results alphabetically.
	 * @param rootLanguage {@code true} to use the "root" language names when
	 *            sorting.
	 * @return The {@link List} of "future" {@link Table}s.
	 */
	@Nonnull
	public List<TableId> getFutureTables(boolean includeRelated, boolean sort, final boolean rootLanguage) {
		ArrayList<TableId> result = new ArrayList<>();
		ArrayList<Table> tempTables;
		synchronized (this) {
			if (futureTables == null || futureTables.isEmpty()) {
				return result;
			}
			tempTables = new ArrayList<>(futureTables);
		}

		for (Table table : tempTables) {
			result.add(table.getTableId());
		}
		if (includeRelated) {
			result.addAll(getRelatedTableIds(tempTables));
		}

		if (sort) {
			Collections.sort(result, new Comparator<TableId>() {
				@Override
				public int compare(TableId o1, TableId o2) {
					if (o1 == null && o2 == null) {
						return 0;
					}
					if (o1 == null) {
						return 1;
					}
					if (o2 == null) {
						return -1;
					}
					if (rootLanguage) {
						return o1.getRootDisplayName().compareTo(o2.getRootDisplayName());
					}
					return o1.getDisplayName().compareTo(o2.getDisplayName());
				}
			});
		}

		return result;
	}

	/**
	 * @return {@code true} if there are "future" tables that have relations to
	 *         "non-future" tables, {@code false} otherwise.
	 */
	public boolean hasFutureTablesRelated() {
		ArrayList<Table> tempTables;
		synchronized (this) {
			if (futureTables == null || futureTables.isEmpty()) {
				return false;
			}
			tempTables = new ArrayList<>(futureTables);
		}

		return !getRelatedTableIds(tempTables).isEmpty();
	}

	/**
	 * @return The {@link Exception} registered during the last call to
	 *         {@link #start()} or {@code null} of no error was registered.
	 */
	@Nullable
	public synchronized Exception getError() {
		return lastException;
	}

	/**
	 * Gets a {@link Connection} from the connection pool if one is available.
	 * <b>Blocks until one is available it all connections are currently in
	 * use</b>. If this {@link TableManager} isn't connected to a database,
	 * {@code null} is returned.
	 *
	 * @return The {@link Connection} or {@code null} if this
	 *         {@link TableManager} isn't connected to a database.
	 */
	@Nullable
	public synchronized Connection getConnection() {
		if (!connected) {
			if (lastException != null) {
				LOGGER.debug(
					"Rejecting request for database connection due to previous error: {}",
					lastException.getMessage()
				);
				return null;
			}
			LOGGER.debug("Rejecting request for database connection because TableManager is disconnected");
			return null;
		}
		try {
			return connectionPool.getConnection();
		} catch (SQLException e) {
			LOGGER.error("Unable to acquire database connection: {}", e.getMessage());
			LOGGER.trace("", e);
			lastException = e;
			connected = false;
			clearConnectionPool();
			return null;
		}
	}

	/**
	 * Creates a single {@link Connection} for maintenance/repair use if this
	 * {@link TableManager} is disconnected.
	 *
	 * @return The new {@link Connection}.
	 * @throws SQLException If a SQL error occurs during the operation.
	 * @throws IllegalStateException If this {@link TableManager} is connected.
	 */
	@Nonnull
	@SuppressFBWarnings("DMI_EMPTY_DB_PASSWORD")
	public synchronized Connection getMaintenanceConnection() throws SQLException {
		if (connected) {
			throw new IllegalStateException("TableManager is not in a disconnected state");
		}
		return DriverManager.getConnection(url, "sa", "");
	}

	/**
	 * @return The database name.
	 */
	@Nonnull
	public String getDatabaseName() {
		return databaseName;
	}

	/**
	 * Gets the database file path. Since the database can consist of multiple
	 * files, this is the file path including the database name but without the
	 * extensions (which may vary). If {@code withExtension} is {@code true},
	 * the extension for the "main" database file is appended.
	 *
	 * @param withExtension if {@code true} {@link Constants#SUFFIX_MV_FILE} is
	 *            appended to the "base" file name.
	 *
	 * @return The database file path.
	 */
	@Nonnull
	public String getDatabaseFilepath(boolean withExtension) {
		return withExtension ? databaseFilename + Constants.SUFFIX_MV_FILE : databaseFilename;
	}

	/**
	 * Returns the {@link Table} with the specified {@link TableId} if
	 * registered.
	 *
	 * @param tableId the {@link TableId} for the {@link Table} to return.
	 * @return The {@link Table} or {@code null}.
	 */
	@Nullable
	public Table getTable(TableId tableId) {
		return tables.get(tableId);
	}

	/**
	 * @return The registered {@link TableCoverArtArchive} or {@code null}.
	 */
	@Nullable
	public TableCoverArtArchive getTableCoverArtArchive() {
		return tableCoverArtArchive;
	}

	/**
	 * @return The registered {@link TableMusicBrainzReleases} or {@code null}.
	 */
	@Nullable
	public TableMusicBrainzReleases getTableMusicBrainzReleases() {
		return tableMusicBrainzReleases;
	}

	/**
	 * @return The registered {@link Table} instances.
	 */
	@Nonnull
	public Map<TableId, Table> getTables() {
		return tables;
	}

	@Override
	protected void finalize() throws Throwable {
		clearConnectionPool();
		super.finalize();
	}

	private void createConnectionPool() {
		if (connectionPool == null) {
			connectionPool = JdbcConnectionPool.create(url, "sa", "");
		}
	}

	private void clearConnectionPool() {
		if (connectionPool != null) {
			connectionPool.dispose();
			connectionPool = null;
		}
	}

	@Nonnull
	private Set<Table> checkFutureTableVersions(@Nonnull Connection connection) throws SQLException {
		HashSet<Table> result = new HashSet<>();
		for (Table table : tables.values()) {
			if (table.getTableId() == TableId.TABLES) {
				// TABLES isn't versioned
				continue;
			}

			int databaseVersion = Tables.getTableVersion(connection, table.getTableId());
			if (databaseVersion > table.getTableVersion()) {
				if (Tables.tableExists(connection, table.getTableId())) {
					// We have a problem, add it to the result
					result.add(table);
				} else {
					// The table doesn't actually exist, reset the table
					// version number and let it be created normally.
					Tables.setTableVersion(connection, table.getTableId(), 0);
				}
			}
		}
		return result;
	}

	/**
	 * Gets {@link TableId}s related to the specified {@link List} of
	 * {@link Table}s excluding the {@link TableId}s from the {@link List}'s
	 * {@link Table} themselves.
	 *
	 * @param tables the {@link List} of {@link Table}s for which to find
	 *            related {@link TableId}s.
	 * @return The related tables.
	 */
	@Nonnull
	protected static EnumSet<TableId> getRelatedTableIds(@Nullable List<Table> tables) {
		EnumSet<TableId> result = EnumSet.noneOf(TableId.class);
		if (tables == null) {
			return result;
		}
		for (Table table : tables) {
			EnumSet<TableId> related = table.getRelatedTables();
			if (related != null) {
				result.addAll(related);
			}
		}
		if (result.isEmpty()) {
			return result;
		}
		for (Iterator<TableId> iterator = result.iterator(); iterator.hasNext();) {
			TableId tableId = iterator.next();
			for (Table table : tables) {
				if (table.getTableId() == tableId) {
					iterator.remove();
					break;
				}
			}
		}
		return result;
	}

	private static String determineDBFilename(@Nonnull String databaseName, @Nullable PmsConfiguration configuration) {
		File databaseFile = null;
		if (configuration != null && configuration.getProfileFolder() != null) {
			File folder = new File(configuration.getProfileFolder());
			if (folder.isDirectory()) {
				folder = new File(folder, "database");
				databaseFile = new File(folder, databaseName);
			}
		}
		if (databaseFile == null) {
			databaseFile = new File(databaseName);
		}
		return databaseFile.getAbsolutePath();
	}

	private static String buildURL(@Nonnull String databaseFilename, @Nullable PmsConfiguration configuration) {
		StringBuilder sb = new StringBuilder(Constants.START_URL)
			.append("nio:")
			.append(databaseFilename)
			.append(";MULTI_THREADED=1");
		if (configuration != null) {
			if (configuration.getDatabaseLogging()) {
				sb.append(";TRACE_LEVEL_FILE=4");
				LOGGER.info("Database logging is enabled");
			}
		}
		return sb.toString();
	}
}
