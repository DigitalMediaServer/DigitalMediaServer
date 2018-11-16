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
package net.pms.logging;

import static org.apache.commons.lang3.StringUtils.isBlank;
import java.util.Locale;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import net.pms.Messages;


/**
 * This {@code enum} represents the level of detail used for logging.
 *
 * @author Nadahar
 */
public enum LogLevel {

	/** Logging is turned off */
	OFF,

	/** Only log errors */
	ERROR,

	/** Only log errors and warnings */
	WARN,

	/** Only log errors, warnings and information */
	INFO,

	/** Log everything except "trace" information */
	DEBUG,

	/** Log everything */
	TRACE,

	/** Log everything */
	ALL;

	/**
	 * @return The {@link ch.qos.logback.classic.Level} that corresponds to this
	 *         {@link LogLevel}.
	 */
	@Nonnull
	public ch.qos.logback.classic.Level getLogbackLevel() {
		switch (this) {
			case OFF:
				return ch.qos.logback.classic.Level.OFF;
			case ERROR:
				return ch.qos.logback.classic.Level.ERROR;
			case WARN:
				return ch.qos.logback.classic.Level.WARN;
			case INFO:
				return ch.qos.logback.classic.Level.INFO;
			case DEBUG:
				return ch.qos.logback.classic.Level.DEBUG;
			case TRACE:
				return ch.qos.logback.classic.Level.TRACE;
			case ALL:
				return ch.qos.logback.classic.Level.ALL;
			default:
				throw new AssertionError("Unimplemented LogLevel \"" + name() + "\"");
		}
	}

	/**
	 * @return The {@link ch.qos.logback.classic.Level#levelInt} that
	 *         corresponds to this {@link LogLevel}.
	 */
	public int getLogBackIntValue() {
		return getLogbackLevel().levelInt;
	}

	/**
	 * @return The non-localized textual representation of this {@link LogLevel}.
	 */
	@Override
	public String toString() {
		return toString(false);
	}

	/**
	 * Returns the textual representation of this {@link LogLevel}.
	 *
	 * @param localized if {@code true} the representation is localized, if
	 *            {@code false} it is not.
	 * @return The textual representation of this {@link LogLevel}.
	 */
	@Nonnull
	public String toString(boolean localized) {
		switch (this) {
			case OFF:
				return localized ? Messages.getString("TracesTab.16") : "Off";
			case ERROR:
				return localized ? Messages.getString("TracesTab.6") : "Error";
			case WARN:
				return localized ? Messages.getString("TracesTab.7") : "Warning";
			case INFO:
				return localized ? Messages.getString("TracesTab.8") : "Info";
			case DEBUG:
				return localized ? Messages.getString("TracesTab.9") : "Debug";
			case TRACE:
				return localized ? Messages.getString("TracesTab.10") : "Trace";
			case ALL:
				return localized ? Messages.getString("TracesTab.15") : "All";
			default:
				throw new AssertionError("Unimplemented LogLevel \"" + name() + "\"");
		}
	};

	/**
	 * Attempts to convert the specified integer value to a {@link LogLevel}. If
	 * the conversion fails, {@code null} is returned.
	 * <p>
	 * This method supports {@link ch.qos.logback.classic.Level},
	 * {@link java.util.logging.Level} and
	 * {@link org.slf4j.spi.LocationAwareLogger} integer values.
	 *
	 * @param value the value to convert.
	 * @return The corresponding {@link LogLevel} or {@code null}.
	 */
	@Nullable
	public static LogLevel typeOf(int value) {
		return typeOf(value, null);
	}

	/**
	 * Attempts to convert the specified integer value to a {@link LogLevel}. If
	 * the conversion fails, {@code defaultLevel} is returned.
	 * <p>
	 * This method supports {@link ch.qos.logback.classic.Level},
	 * {@link java.util.logging.Level} and
	 * {@link org.slf4j.spi.LocationAwareLogger} integer values.
	 *
	 * @param value the value to convert.
	 * @param defaultLevel the {@link LogLevel} to return if the conversion
	 *            fails.
	 * @return The corresponding {@link LogLevel} or {@code defaultLevel}.
	 */
	@Nullable
	public static LogLevel typeOf(int value, @Nullable LogLevel defaultLevel) {
		switch (value) {
			case Integer.MAX_VALUE:
				return OFF;
			case 40000:
			case 40:
			case 1000:
				return ERROR;
			case 30000:
			case 30:
			case 900:
				return WARN;
			case 20000:
			case 20:
			case 800:
				return INFO;
			case 10000:
			case 10:
			case 700:
			case 500:
				return DEBUG;
			case 5000:
			case 0:
			case 400:
			case 300:
				return TRACE;
			case Integer.MIN_VALUE:
				return ALL;
			default:
				return defaultLevel;
		}

	}

	/**
	 * Converts the specified {@link java.util.logging.Level} to a
	 * {@link LogLevel}. If the conversion fails, {@code null} is returned.
	 *
	 * @param level the {@link Level} to convert.
	 * @return The corresponding {@link LogLevel} or {@code null}.
	 */
	@Nullable
	public static LogLevel typeOf(@Nullable Level level) {
		if (level == null) {
			return null;
		}
		return typeOf(level.intValue());
	}

	/**
	 * Converts the specified {@link ch.qos.logback.classic.Level} to a
	 * {@link LogLevel}. If the conversion fails, {@code null} is returned.
	 *
	 * @param level the {@link ch.qos.logback.classic.Level} to convert.
	 * @return The corresponding {@link LogLevel} or {@code null}.
	 */
	@Nullable
	public static LogLevel typeOf(@Nullable ch.qos.logback.classic.Level level) {
		if (level == null) {
			return null;
		}
		return typeOf(level.toInt());
	}

	/**
	 * Converts the specified {@link org.slf4j.event.Level} to a
	 * {@link LogLevel}. If the conversion fails, {@code null} is returned.
	 *
	 * @param level the {@link org.slf4j.event.Level} to convert.
	 * @return The corresponding {@link LogLevel} or {@code null}.
	 */
	@Nullable
	public static LogLevel typeOf(@Nullable org.slf4j.event.Level level) {
		if (level == null) {
			return null;
		}
		return typeOf(level.toInt());
	}

	/**
	 * Attempts to parse the specified string to a {@link LogLevel}. If the
	 * parsing fails, {@code null} is returned.
	 * <p>
	 * This method supports {@link ch.qos.logback.classic.Level},
	 * {@link java.util.logging.Level} and {@link org.slf4j.event.Level} string
	 * values.
	 *
	 * @param value the {@link String} to parse.
	 * @return The corresponding {@link LogLevel} or {@code null}.
	 */
	@Nullable
	public static LogLevel typeOf(@Nullable String value) {
		return isBlank(value) ? null : typeOf(value, null);
	}

	/**
	 * Attempts to parse the specified string to a {@link LogLevel}. If the
	 * parsing fails, {@code defaultLevel} is returned.
	 * <p>
	 * This method supports {@link ch.qos.logback.classic.Level},
	 * {@link java.util.logging.Level} and {@link org.slf4j.event.Level} string
	 * values.
	 *
	 * @param value the {@link String} to parse.
	 * @param defaultLevel the {@link LogLevel} to return if the parsing fails.
	 * @return The corresponding {@link LogLevel} or {@code defaultLevel}.
	 */
	@Nullable
	public static LogLevel typeOf(@Nullable String value, @Nullable LogLevel defaultLevel) {
		if (isBlank(value)) {
			return defaultLevel;
		}

		value = value.trim().toUpperCase(Locale.ROOT);
		// Look for custom strings
		switch (value) {
			case "SEVERE":
				return ERROR;
			case "WARNING":
				return WARN;
			case "INFORMATION":
			case "NORMAL":
			case "STANDARD":
			case "DEFAULT":
				return INFO;
			case "CONFIG":
			case "FINE":
				return DEBUG;
			case "FINER":
			case "FINEST":
				return TRACE;
			default:
				break;
		}

		// Look for standard strings
		for (LogLevel level : values()) {
			if (value.equals(level.name())) {
				return level;
			}
		}

		// Look for localized strings
		for (LogLevel level : values()) {
			if (value.equals(level.toString(true).toLowerCase(Locale.ROOT))) {
				return level;
			}
		}

		// Give up
		return defaultLevel;
	}

	/**
	 * Returns the effective {@link LogLevel} for the specified
	 * {@link org.slf4j.Logger} or {@code null} if {@code logger} is
	 * {@code null}.
	 *
	 * @param logger the {@link org.slf4j.Logger} whose {@link LogLevel} to get.
	 * @return The effective {@link LogLevel} or {@code null} if {@code logger}
	 *         is {@code null}.
	 */
	@Nullable
	public static LogLevel getLogLevel(@Nullable Logger logger) {
		if (logger == null) {
			return null;
		}

		if (logger instanceof ch.qos.logback.classic.Logger) {
			ch.qos.logback.classic.Level level = ((ch.qos.logback.classic.Logger) logger).getLevel();
			if (level != null) {
				return typeOf(level);
			}
		}

		if (logger.isTraceEnabled()) {
			return TRACE;
		}
		if (logger.isDebugEnabled()) {
			return DEBUG;
		}
		if (logger.isInfoEnabled()) {
			return INFO;
		}
		if (logger.isWarnEnabled()) {
			return WARN;
		}
		if (logger.isErrorEnabled()) {
			return ERROR;
		}
		return OFF;
	}

	/**
	 * Returns the effective {@link LogLevel} for the specified
	 * {@link java.util.logging.Logger} or {@code null} if {@code logger} is
	 * {@code null}.
	 *
	 * @param logger the {@link java.util.logging.Logger} whose {@link LogLevel}
	 *            to get.
	 * @return The effective {@link LogLevel} or {@code null} if {@code logger}
	 *         is {@code null}.
	 */
	@Nullable
	public static LogLevel getLogLevel(@Nullable java.util.logging.Logger logger) {
		if (logger == null) {
			return null;
		}

		Level level = logger.getLevel();
		if (level != null) {
			return typeOf(level);
		}

		if (logger.isLoggable(Level.FINER)) {
			return TRACE;
		}
		if (logger.isLoggable(Level.FINE) || logger.isLoggable(Level.CONFIG)) {
			return DEBUG;
		}
		if (logger.isLoggable(Level.INFO)) {
			return INFO;
		}
		if (logger.isLoggable(Level.WARNING)) {
			return WARN;
		}
		if (logger.isLoggable(Level.SEVERE)) {
			return ERROR;
		}
		return OFF;
	}
}
