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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.EnumSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This {@link Table} implementation is responsible for the special
 * {@code TABLES} {@link Table}, which contains version information about all
 * the other {@link Table}s.
 * <p>
 * This class also functions as a generic database utility class.
 *
 * @author Nadahar
 */
public class Tables extends Table {
	private static final Logger LOGGER = LoggerFactory.getLogger(Tables.class);

	/** The {@link TableId} for this {@link Table} */
	public static final TableId ID = TableId.TABLES;

	private static final String DEFAULT_ESCAPE_CHARACTER = "\\";

	/**
	 * Should only be instantiated by {@link TableManager}.
	 *
	 * @param tableManager the {@link TableManager} to use.
	 */
	Tables(@Nonnull TableManager tableManager) {
		super(tableManager);
	}

	@Override
	@Nonnull
	public TableId getTableId() {
		return ID;
	}

	/**
	 * {@link Tables} isn't versioned.
	 */
	@Override
	public int getTableVersion() {
		return 0;
	}

	@Override
	@Nullable
	public EnumSet<TableId> getRelatedTables() {
		return null;
	}

	/**
	 * {@link Tables} isn't versioned.
	 */
	@Override
	void upgradeTable(@Nonnull Connection connection, int currentVersion) throws SQLException {
	}

	@Override
	void checkTable(@Nonnull Connection connection) throws SQLException {
		if (!tableExists(connection, ID)) {
			createTable(connection);
		}
	}

	@Override
	protected void createTable(@Nonnull Connection connection) throws SQLException {
		LOGGER.debug("Creating database table \"{}\"", ID.getName());
		try (Statement statement = connection.createStatement()) {
			statement.execute("CREATE TABLE TABLES(NAME VARCHAR(50) PRIMARY KEY, VERSION INT NOT NULL)");
		}
	}

	/**
	 * Checks if a named table exists in table schema {@code PUBLIC}.
	 *
	 * @param connection the {@link Connection} to use while performing the
	 *            check.
	 * @param tableId the {@link TableId} of the {@link Table} to check for
	 *            existence.
	 * @return {@code true} if the {@link Table} in schema {@code PUBLIC}
	 *         exists, {@code false} otherwise.
	 * @throws SQLException If an SQL error occurs during the operation.
	 */
	protected static boolean tableExists(
		@Nonnull Connection connection,
		@Nullable TableId tableId
	) throws SQLException {
		return tableExists(connection, tableId, null);
	}

	/**
	 * Checks if a named table exists. If {@code tableSchema} is blank, schema
	 * {@code PUBLIC} is used.
	 *
	 * @param connection the {@link Connection} to use while performing the
	 *            check.
	 * @param tableId the {@link TableId} of the {@link Table} to check for
	 *            existence.
	 * @param tableSchema the table schema for the table to check for existence.
	 * @return {@code true} if the {@link Table} exists in the given schema,
	 *         {@code false} otherwise.
	 * @throws SQLException If an SQL error occurs during the operation.
	 */
	protected static boolean tableExists(
		@Nonnull Connection connection,
		@Nullable TableId tableId,
		@Nullable String tableSchema
	) throws SQLException {
		if (tableId == null) {
			return false;
		}
		if (isBlank(tableSchema)) {
			tableSchema = "PUBLIC";
		}
		LOGGER.trace("Checking if database table \"{}\" in schema \"{}\" exists", tableId, tableSchema);

		try (PreparedStatement statement = connection.prepareStatement(
			"SELECT * FROM INFORMATION_SCHEMA.TABLES " +
				"WHERE TABLE_SCHEMA = ? " +
				"AND  TABLE_NAME = ?"
		)) {
			statement.setString(1, tableSchema);
			statement.setString(2, tableId.getName());
			try (ResultSet result = statement.executeQuery()) {
				if (result.next()) {
					LOGGER.trace("Database table \"{}\" found", tableId);
					return true;
				}
				LOGGER.trace("Database table \"{}\" not found", tableId);
				return false;
			}
		}
	}

	/**
	 * Gets the version of a {@link Table} from {@link Tables}.
	 *
	 * @param connection the {@link Connection} to use.
	 * @param tableId the {@link TableId} of the {@link Table} for which to find
	 *            the version.
	 * @return The version number if found or {@code -1} if the table isn't
	 *         listed in {@link Tables}.
	 * @throws SQLException If an SQL error occurs during the operation.
	 */
	protected static int getTableVersion(
		@Nonnull Connection connection,
		@Nullable TableId tableId
	) throws SQLException {
		if (tableId == null) {
			return -1;
		}
		try (PreparedStatement statement = connection.prepareStatement(
			"SELECT VERSION FROM TABLES " +
				"WHERE NAME = ?"
		)) {
			statement.setString(1, tableId.getName());
			try (ResultSet result = statement.executeQuery()) {
				if (result.next()) {
					if (LOGGER.isTraceEnabled()) {
						LOGGER.trace("Table version for database table \"{}\" is {}", tableId, result.getInt("VERSION"));
					}
					return result.getInt("VERSION");
				}
				LOGGER.trace("Table version for database table \"{}\" not found", tableId);
				return -1;
			}
		}
	}

	/**
	 * Sets the version of a {@link Table} in {@link Tables}. Creates a row for
	 * the given {@link Table} if needed.
	 *
	 * @param connection the {@link Connection} to use.
	 * @param tableId the {@link TableId} of the {@link Table} for which to set
	 *            the version.
	 * @param version the version number to set.
	 * @throws SQLException If an SQL error occurs during the operation.
	 * @throws IllegalArgumentException If {@code tableId} is {@code null}.
	 */
	protected static void setTableVersion(
		@Nonnull Connection connection,
		@Nonnull TableId tableId,
		int version
	) throws SQLException {
		if (tableId == null) {
			throw new IllegalArgumentException("tableId cannot be null");
		}
		try (PreparedStatement statement = connection.prepareStatement(
			"SELECT VERSION FROM TABLES WHERE NAME = ?"
		)) {
			statement.setString(1, tableId.getName());
			try (ResultSet result = statement.executeQuery()) {
				if (result.next()) {
					int currentVersion = result.getInt("VERSION");
					if (version != currentVersion) {
						try (PreparedStatement updateStatement = connection.prepareStatement(
							"UPDATE TABLES SET VERSION = ? WHERE NAME = ?"
						)) {
							LOGGER.trace(
								"Updating table version for database table \"{}\" from {} to {}",
								tableId,
								currentVersion,
								version
							);
							updateStatement.setInt(1, version);
							updateStatement.setString(2, tableId.getName());
							updateStatement.executeUpdate();
						}
					} else {
						LOGGER.trace("Table version for database table \"{}\" is already {}, aborting set", tableId, version);
					}
				} else {
					try (PreparedStatement insertStatement = connection.prepareStatement(
						"INSERT INTO TABLES VALUES(?, ?)"
					)) {
						LOGGER.trace("Setting table version for database table \"{}\" to {}", tableId, version);
						insertStatement.setString(1, tableId.getName());
						insertStatement.setInt(2, version);
						insertStatement.executeUpdate();
					}
				}
			}
		}
	}

	/**
	 * Removes the specified {@link Table} and its version from {@link Tables}.
	 *
	 * @param connection the {@link Connection} to use.
	 * @param tableId the {@link TableId} of the {@link Table} to remove.
	 * @throws SQLException If an SQL error occurs during the operation.
	 * @throws IllegalArgumentException If {@code tableId} is {@code null}.
	 */
	protected static void removeTableVersion(
		@Nonnull Connection connection,
		@Nonnull TableId tableId
	) throws SQLException {
		if (tableId == null) {
			throw new IllegalArgumentException("tableId cannot be null");
		}
		try (Statement statement = connection.createStatement()) {
			int result = statement.executeUpdate("DELETE FROM TABLES WHERE NAME = " + sqlQuote(tableId.toString()));
			if (result < 0) {
				LOGGER.trace("Table version for database table \"{}\" isn't set, nothing to remove");
			} else if (result > 1) {
				throw new AssertionError(
					"Tables.removeTableVersion removed more than one row with the name \"" + tableId + "\""
				);
			} else {
				LOGGER.trace("Removed table version for database table \"{}\"", tableId);
			}
		}
	}

	/**
	 * Drops (deletes) a {@link Table}. <b>Use with caution</b>, there is no
	 * undo. This also removes its version entry from {@link Tables}.
	 *
	 * @param connection the {@link Connection} to use.
	 * @param tableId the {@link TableId} of the {@link Table} to delete.
	 * @throws SQLException If an SQL error occurs during the operation.
	 * @throws IllegalArgumentException If {@code tableId} is {@code null}.
	 */
	protected static void dropTable(
		@Nonnull Connection connection,
		@Nonnull TableId tableId
	) throws SQLException {
		if (tableId == null) {
			throw new IllegalArgumentException("tableId cannot be null");
		}
		LOGGER.debug("Dropping database table \"{}\"", tableId);
		try (Statement statement = connection.createStatement()) {
			statement.execute("DROP TABLE " + tableId);
			removeTableVersion(connection, tableId);
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
	@Nonnull
	public static String sqlNullIfBlank(@Nullable String s, boolean quote, boolean like) {
		if (isBlank(s)) {
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
	 *
	 * @see #sqlEscape(String)
	 */
	@Nullable
	public static String sqlQuote(@Nullable String s) {
		return s == null ? null : "'" + s.replace("'", "''") + "'";
	}

	/**
	 * Escapes any existing single quotes in the argument but doesn't quote it.
	 *
	 * @param s the {@link String} to escape.
	 * @return The escaped {@code s}.
	 *
	 * @see #sqlQuote(String)
	 */
	@Nullable
	public static String sqlEscape(@Nullable String s) {
		return s == null ? null : s.replace("'", "''");
	}

	/**
	 * Escapes the argument using the default H2 escape character {@code "\"}
	 * for the escape character itself and the two wildcard characters {@code %}
	 * and {@code _}. This escaping is only valid when using, {@code LIKE}, not
	 * when using {@code =}.
	 *
	 * @param s the {@link String} to be SQL escaped.
	 * @return The escaped {@link String}.
	 */
	@Nullable
	public static String sqlLikeEscape(@Nullable String s) {
		return sqlLikeEscape(s, null);
	}

	/**
	 * Escapes the argument with the specified escape character for the escape
	 * character itself and the two wildcard characters {@code %} and {@code _}.
	 * This escaping is only valid when using, {@code LIKE}, not when using
	 * {@code =}.
	 * <p>
	 * <b>NOTE: When using a non-default escape character</b>, the operand must
	 * be followed by {@code "ESCAPE '<escape character>'"}.
	 *
	 * @param s the {@link String} to be SQL escaped.
	 * @param escapeCharacter the escape character or {@code null} to use the
	 *            default.
	 * @return The escaped {@link String}.
	 */
	@Nullable
	public static String sqlLikeEscape(@Nullable String s, @Nullable String escapeCharacter) {
		if (escapeCharacter == null) {
			// Blank escape character is valid
			escapeCharacter = DEFAULT_ESCAPE_CHARACTER;
		} else if (escapeCharacter.length() > 1) {
			LOGGER.debug("Ignoring invalid escape character \"{}\" in sqlLikeEscape()", escapeCharacter);
			escapeCharacter = DEFAULT_ESCAPE_CHARACTER;
		}
		return s == null ? null : s.
			replace(escapeCharacter, escapeCharacter + escapeCharacter).
			replace("%", escapeCharacter + "%").
			replace("_", escapeCharacter + "_");
	}
}
