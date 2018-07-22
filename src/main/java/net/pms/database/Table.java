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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.EnumSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This abstract class contains the basic functionality needed to represent a database table
 * that can be handled by {@link Tables} and {@link TableManager}.
 *
 * @author Nadahar
 */
public abstract class Table {

	private static final Logger LOGGER = LoggerFactory.getLogger(Table.class);

	/** The {@link TableManager} managing this {@link Table} */
	@Nonnull
	protected final TableManager tableManager;

	/**
	 * Creates a new instance using the specified {@link TableManager}.
	 *
	 * @param tableManager the {@link TableManager} to use;
	 */
	public Table(@Nonnull TableManager tableManager) {
		if (tableManager == null) {
			throw new IllegalArgumentException("tableManager cannot be null");
		}
		this.tableManager = tableManager;
	}

	/**
	 * @return The {@link TableId} of this {@link Table}.
	 */
	@Nonnull
	public abstract TableId getTableId();

	/**
	 * @return The name of this {@link Table}.
	 */
	@Nonnull
	public String getName() {
		return getTableId().getName();
	}

	@Override
	public String toString() {
		return getName();
	}

	/**
	 * @return An {@link EnumSet} of related tables that will have to be dropped
	 *         if this table is dropped or {@code null}.
	 */
	@Nullable
	public abstract EnumSet<TableId> getRelatedTables();

	/**
	 * Gets a new {@link Connection}.
	 * <p>
	 * <b>Note:</b> The {@link Connection} <b>MUST</b> be closed after use. This
	 * call is blocking and will have to wait if no {@link Connection} is
	 * available from the connection pool.
	 *
	 * @return The new {@link Connection} or {@code null} if no
	 *         {@link Connection} could be acquired. If {@code null}, check
	 *         {@link TableManager#getError()}.
	 * @throws SQLException If {@link TableManager} is disconnected.
	 */
	@Nullable
	protected Connection getConnection() throws SQLException {
		Connection connection = tableManager.getConnection();
		if (connection == null) {
			throw new SQLException("Can't get database connection since TableManager is disconnected");
		}
		return connection;
	}

	/**
	 * @return the {@link TableManager} for this {@link Table}.
	 */
	@Nonnull
	protected TableManager getTableManager() {
		return tableManager;
	}

	/**
	 * @return The current version of this table.
	 */
	public abstract int getTableVersion();

	/**
	 * Checks and creates or upgrades the table as needed.
	 *
	 * @param connection the {@link Connection} to use
	 * @throws SQLException If an SQL error occurs during the operation.
	 */
	void checkTable(@Nonnull Connection connection) throws SQLException {
		if (Tables.tableExists(connection, getTableId())) {
			int databaseVersion = Tables.getTableVersion(connection, getTableId());
			if (databaseVersion > 0) {
				if (databaseVersion < getTableVersion()) {
					upgradeTable(connection, databaseVersion);
				} else if (databaseVersion > getTableVersion()) {
					throw new SQLException(
						"Database table \"" + getTableId() +
						"\" is from a newer version of DMS. Please move, rename or delete database file \"" +
						getTableManager().getDatabaseFilepath(true) +
						"\" before starting DMS"
					);
				}
			} else {
				LOGGER.warn(
					"Database table \"{}\" has an unknown version and cannot be used. Dropping and recreating table",
					getTableId()
				);
				Tables.dropTable(connection, getTableId());
				createTable(connection);
				Tables.setTableVersion(connection, getTableId(), getTableVersion());
			}
		} else {
			createTable(connection);
			Tables.setTableVersion(connection, getTableId(), getTableVersion());
		}
	}

	/**
	 * This method <b>MUST</b> be updated if the table definitions are altered.
	 * The changes for each version in the form of {@code ALTER TABLE} must be
	 * implemented here.
	 *
	 * @param connection the {@link Connection} to use.
	 * @param currentVersion the version to upgrade <b>from</b>.
	 * @throws SQLException If an SQL error occurs during the operation.
	 */
	abstract void upgradeTable(@Nonnull Connection connection, int currentVersion) throws SQLException;

	/**
	 * Creates this table in the database. The operation will fail if the table
	 * already exists.
	 *
	 * @param connection the {@link Connection} to use.
	 * @throws SQLException If a SQL error occurs during the operation.
	 */
	protected abstract void createTable(@Nonnull Connection connection) throws SQLException;
}
