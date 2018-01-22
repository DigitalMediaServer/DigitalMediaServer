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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import net.pms.PMS;
import net.pms.dlna.DLNAMediaDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class is the super class of all database table classes. It has the
 * responsibility to check or create the {@code TABLES} table, and to call
 * {@code checkTable()} for each database table implementation.
 *
 * This class also has some utility methods that's likely to be useful to most
 * child classes.
 *
 * @author Nadahar
 */
public class Tables {
	private static final Logger LOGGER = LoggerFactory.getLogger(Tables.class);
	private static final Object CHECK_TABLES_LOCK = new Object();

	/** The {@link DLNAMediaDatabase} instance */
	protected static final DLNAMediaDatabase DATABASE = PMS.get().getDatabase();
	private static boolean tablesChecked = false;
	private static final String ESCAPE_CHARACTER = "\\";

	/**
	 * Not to be instantiated.
	 */
	protected Tables() {
	}

	/**
	 * Checks all child tables for their existence and version and creates or
	 * upgrades as needed. Access to this method is serialized.
	 *
	 * @throws SQLException If an SQL error occurs during the operation.
	 */
	public static final void checkTables() throws SQLException {
		synchronized (CHECK_TABLES_LOCK) {
			if (tablesChecked) {
				LOGGER.debug("Database tables have already been checked, aborting check");
			} else {
				LOGGER.debug("Starting check of database tables");
				try (Connection connection = DATABASE.getConnection()) {
					if (!tableExists(connection, "TABLES")) {
						createTablesTable(connection);
					}

					TableMusicBrainzReleases.checkTable(connection);
					TableCoverArtArchive.checkTable(connection);
				}
				tablesChecked = true;
			}
		}
	}

	/**
	 * Checks if a named table exists.
	 *
	 * @param connection the {@link Connection} to use while performing the
	 *            check.
	 * @param tableName the name of the table to check for existence.
	 * @param tableSchema the table schema for the table to check for existence.
	 * @return {@code true} if a table with the given name in the given schema
	 *         exists, {@code false} otherwise.
	 * @throws SQLException If an SQL error occurs during the operation.
	 */
	protected static final boolean tableExists(
		final Connection connection,
		final String tableName,
		final String tableSchema
	) throws SQLException {
		LOGGER.trace("Checking if database table \"{}\" in schema \"{}\" exists", tableName, tableSchema);

		try (PreparedStatement statement = connection.prepareStatement(
			"SELECT * FROM INFORMATION_SCHEMA.TABLES " +
				"WHERE TABLE_SCHEMA = ? " +
				"AND  TABLE_NAME = ?"
		)) {
			statement.setString(1, tableSchema);
			statement.setString(2, tableName);
			try (ResultSet result = statement.executeQuery()) {
				if (result.next()) {
					LOGGER.trace("Database table \"{}\" found", tableName);
					return true;
				}
				LOGGER.trace("Database table \"{}\" not found", tableName);
				return false;
			}
		}
	}

	/**
	 * Checks if a named table exists in table schema {@code PUBLIC}.
	 *
	 * @param connection the {@link Connection} to use while performing the
	 *            check.
	 * @param tableName the name of the table to check for existence.
	 * @return {@code true} if a table with the given name in schema
	 *         {@code PUBLIC} exists, {@code false} otherwise.
	 * @throws SQLException If an SQL error occurs during the operation.
	 */
	protected static final boolean tableExists(final Connection connection, final String tableName) throws SQLException {
		return tableExists(connection, tableName, "PUBLIC");
	}

	/**
	 * Gets the version of a named table from the {@code TABLES} table.
	 *
	 * @param connection the {@link Connection} to use.
	 * @param tableName the name of the table for which to find the version.
	 * @return The version number if found or {@code null} if the table isn't
	 *         listed in {@code TABLES}.
	 * @throws SQLException If an SQL error occurs during the operation.
	 */
	protected static final Integer getTableVersion(final Connection connection, final String tableName) throws SQLException {
		try (PreparedStatement statement = connection.prepareStatement(
			"SELECT VERSION FROM TABLES " +
				"WHERE NAME = ?"
		)) {
			statement.setString(1, tableName);
			try (ResultSet result = statement.executeQuery()) {
				if (result.next()) {
					if (LOGGER.isTraceEnabled()) {
						LOGGER.trace("Table version for database table \"{}\" is {}", tableName, result.getInt("VERSION"));
					}
					return result.getInt("VERSION");
				}
				LOGGER.trace("Table version for database table \"{}\" not found", tableName);
				return null;
			}
		}
	}

	/**
	 * Sets the version of a named table in the {@code TABLES} table. Creates a
	 * row for the given table name if needed.
	 *
	 * @param connection the {@link Connection} to use.
	 * @param tableName the name of the table for which to set the version.
	 * @param version the version number to set.
	 * @throws SQLException If an SQL error occurs during the operation.
	 */
	protected static final void setTableVersion(
		final Connection connection,
		final String tableName,
		final int version
	) throws SQLException {
		try (PreparedStatement statement = connection.prepareStatement(
			"SELECT VERSION FROM TABLES WHERE NAME = ?"
		)) {
			statement.setString(1, tableName);
			try (ResultSet result = statement.executeQuery()) {
				if (result.next()) {
					int currentVersion = result.getInt("VERSION");
					if (version != currentVersion) {
						try (PreparedStatement updateStatement = connection.prepareStatement(
							"UPDATE TABLES SET VERSION = ? WHERE NAME = ?"
						)) {
							LOGGER.trace(
								"Updating table version for database table \"{}\" from {} to {}",
								tableName,
								currentVersion,
								version
							);
							updateStatement.setInt(1, version);
							updateStatement.setString(2, tableName);
							updateStatement.executeUpdate();
						}
					} else {
						LOGGER.trace("Table version for database table \"{}\" is already {}, aborting set", tableName, version);
					}
				} else {
					try (PreparedStatement insertStatement = connection.prepareStatement(
						"INSERT INTO TABLES VALUES(?, ?)"
					)) {
						LOGGER.trace("Setting table version for database table \"{}\" to {}", tableName, version);
						insertStatement.setString(1, tableName);
						insertStatement.setInt(2, version);
						insertStatement.executeUpdate();
					}
				}
			}
		}
	}

	/**
	 * Drops (deletes) the named table. <b>Use with caution</b>, there is no
	 * undo.
	 *
	 * @param connection the {@link Connection} to use.
	 * @param tableName the name of the table to delete.
	 * @throws SQLException If an SQL error occurs during the operation.
	 */
	protected static final void dropTable(final Connection connection, final String tableName) throws SQLException {
		LOGGER.debug("Dropping database table \"{}\"", tableName);
		try (Statement statement = connection.createStatement()) {
			statement.execute("DROP TABLE " + tableName);
		}
	}

	private static final void createTablesTable(final Connection connection) throws SQLException {
		LOGGER.debug("Creating database table \"TABLES\"");
		try (Statement statement = connection.createStatement()) {
			statement.execute("CREATE TABLE TABLES(NAME VARCHAR(50) PRIMARY KEY, VERSION INT NOT NULL)");
		}
	}

	/**
	 * Convenience method for handling SQL null values in {@code WHERE} or
	 * {@code HAVING} statements. SQL doesn't see null as a value, and thus
	 * {@code =} is illegal for {@code null}. Instead, {@code IS NULL} must be
	 * used.
	 * <p>
	 * Please note that the like-escaping is not applied, as that must be done
	 * before any wildcards are added.
	 *
	 * @param s the {@link String} to compare to.
	 * @param quote whether the result should be single quoted for use as a SQL
	 *            string or not.
	 * @param like whether {@code LIKE} should be used instead of {@code =}.
	 *            This implies quote.
	 * @return The SQL formatted string including the {@code =}, {@code LIKE} or
	 *         {@code IS} operator.
	 */
	public static final String sqlNullIfBlank(final String s, boolean quote, boolean like) {
		if (s == null || s.trim().isEmpty()) {
			return " IS NULL ";
		} else if (like) {
			return " LIKE " + sqlQuote(s);
		} else if (quote) {
			return " = " + sqlQuote(s);
		} else {
			return " = " + s;
		}
	}

	/**
	 * Surrounds the argument with single quotes and escapes any existing single
	 * quotes.
	 *
	 * @param s the {@link String} to escape and quote.
	 * @return The escaped and quoted {@link String}.
	 */
	public static final String sqlQuote(final String s) {
		return s == null ? null : "'" + s.replace("'", "''") + "'";
	}

	/**
	 * Escapes the argument with the default H2 escape character for the escape
	 * character itself and the two wildcard characters {@code %} and {@code _}.
	 * This escaping is only valid when using, {@code LIKE}, not when using
	 * {@code =}.
	 * <p>
	 * TODO: Escaping should be generalized so that any escape character could
	 * be used and that the class would set the correct escape character when
	 * opening the database.
	 *
	 * @param s the {@link String} to be SQL escaped.
	 * @return The escaped {@link String}.
	 */
	public static final String sqlLikeEscape(final String s) {
		return s == null ? null : s.
			replace(ESCAPE_CHARACTER, ESCAPE_CHARACTER + ESCAPE_CHARACTER).
			replace("%", ESCAPE_CHARACTER + "%").
			replace("_", ESCAPE_CHARACTER + "_");
	}
}
