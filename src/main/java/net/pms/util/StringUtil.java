/*
 * Universal Media Server, for streaming any media to DLNA
 * compatible renderers based on the http://www.ps3mediaserver.org.
 * Copyright (C) 2012 UMS developers.
 *
 * This program is a free software; you can redistribute it and/or
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

package net.pms.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.JEditorPane;
import javax.swing.JTextPane;
import javax.swing.text.html.HTMLEditorKit;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import static org.apache.commons.lang3.StringUtils.isBlank;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.text.translate.UnicodeUnescaper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class StringUtil {
	private static final Logger LOGGER = LoggerFactory.getLogger(StringUtil.class);
	private static final int[] MULTIPLIER = new int[] {3600, 60, 1};
	public static final String SEC_TIME_FORMAT = "%02d:%02d:%02.0f";
	public static final String DURATION_TIME_FORMAT = "%02d:%02d:%05.2f";
	public static final String DLNA_DURATION_FORMAT = "%01d:%02d:%06.3f";
	public static final String NEWLINE_CHARACTER = System.getProperty("line.separator");

	/** A {@link Pattern} that matches lower-case characters */
	public static final Pattern LOWER = Pattern.compile("[\\p{Lower}]+", Pattern.UNICODE_CHARACTER_CLASS);

	/** A {@link Pattern} that matches upper-case characters */
	public static final Pattern UPPER = Pattern.compile("[\\p{Upper}]+", Pattern.UNICODE_CHARACTER_CLASS);

	/** A {@link Pattern} that matches new-lines */
	public static final Pattern NEWLINE = Pattern.compile("\\r\\n|\\r|\\n");

	/** A {@link Pattern} that matches whitespace */
	public static final Pattern WHITESPACE = Pattern.compile("\\s+", Pattern.UNICODE_CHARACTER_CLASS);

	/**
	 * Appends "&lt;<u>tag</u> " to the StringBuilder. This is a typical HTML/DIDL/XML tag opening.
	 *
	 * @param sb String to append the tag beginning to.
	 * @param tag String that represents the tag
	 */
	public static void openTag(StringBuilder sb, String tag) {
		sb.append("&lt;");
		sb.append(tag);
	}

	/**
	 * Appends the closing symbol &gt; to the StringBuilder. This is a typical HTML/DIDL/XML tag closing.
	 *
	 * @param sb String to append the ending character of a tag.
	 */
	public static void endTag(StringBuilder sb) {
		sb.append("&gt;");
	}

	/**
	 * Appends "&lt;/<u>tag</u>&gt;" to the StringBuilder. This is a typical closing HTML/DIDL/XML tag.
	 *
	 * @param sb
	 * @param tag
	 */
	public static void closeTag(StringBuilder sb, String tag) {
		sb.append("&lt;/");
		sb.append(tag);
		sb.append("&gt;");
	}

	public static void addAttribute(StringBuilder sb, String attribute, Object value) {
		sb.append(' ');
		sb.append(attribute);
		sb.append("=\"");
		sb.append(value);
		sb.append("\"");
	}

	public static void addXMLTagAndAttribute(StringBuilder sb, String tag, Object value) {
		sb.append("&lt;");
		sb.append(tag);
		sb.append("&gt;");
		sb.append(value);
		sb.append("&lt;/");
		sb.append(tag);
		sb.append("&gt;");
	}

	/**
	 * Does double transformations between &<> characters and their XML representation with ampersands.
	 *
	 * @param s String to be encoded
	 * @return Encoded String
	 */
	public static String encodeXML(String s) {
		s = s.replace("&", "&amp;");
		s = s.replace("<", "&lt;");
		s = s.replace(">", "&gt;");
		/* Skip encoding/escaping ' and " for compatibility with some renderers
		 * This might need to be made into a renderer option if some renderers require them to be encoded
		 * s = s.replace("\"", "&quot;");
		 * s = s.replace("'", "&apos;");
		 */

		// The second encoding/escaping of & is not a bug, it's what effectively adds the second layer of encoding/escaping
		s = s.replace("&", "&amp;");
		return s;
	}

	/**
	 * Removes xml character representations.
	 *
	 * @param s String to be cleaned
	 * @return Encoded String
	 */
	public static String unEncodeXML(String s) {
		// Note: ampersand substitution must be first in order to undo double transformations
		// TODO: support ' and " if/when required, see encodeXML() above
		return s.replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">");
	}

	/**
	 * Converts a URL string to a more canonical form
	 *
	 * @param url String to be converted
	 * @return Converted String.
	 */
	public static String convertURLToFileName(String url) {
		url = url.replace('/', '\u00b5');
		url = url.replace('\\', '\u00b5');
		url = url.replace(':', '\u00b5');
		url = url.replace('?', '\u00b5');
		url = url.replace('*', '\u00b5');
		url = url.replace('|', '\u00b5');
		url = url.replace('<', '\u00b5');
		url = url.replace('>', '\u00b5');
		return url;
	}

	/**
	 * Translates the specified string into
	 * {@code application/x-www-form-urlencoded} format using {@code UTF-8}.
	 *
	 * @param s the {@link String} to encode.
	 * @return The encoded {@link String}.
	 */
	public static String urlEncode(String s) {
		try {
			return URLEncoder.encode(s, StandardCharsets.UTF_8.name());
		} catch (UnsupportedEncodingException e) {
			throw new AssertionError("UTF-8 is unsupported");
		}
	}

	/**
	 * Decodes an {@code application/x-www-form-urlencoded} string using
	 * {@code UTF-8}.
	 *
	 * @param s the {@link String} to decode.
	 * @return The decoded {@link String}.
	 */
	public static String urlDecode(String s) {
		try {
			return URLDecoder.decode(s, StandardCharsets.UTF_8.name());
		} catch (UnsupportedEncodingException e) {
			throw new AssertionError("UTF-8 is unsupported");
		}
	}

	/**
	 * Parse as double, or if it's not just one number, handles {hour}:{minute}:{seconds}
	 *
	 * @param time
	 * @return
	 */
	public static double convertStringToTime(String time) throws IllegalArgumentException {
		if (isBlank(time)) {
			throw new IllegalArgumentException("time String should not be blank.");
		}

		try {
			return Double.parseDouble(time);
		} catch (NumberFormatException e) {
			String[] arguments = time.split(":");
			double sum = 0;
			int i = 0;
			for (String argument : arguments) {
				sum += Double.parseDouble(argument.replace(",", ".")) * MULTIPLIER[i];
				i++;
			}

			return sum;
		}
	}

	/**
	 * Converts time to string.
	 *
	 * @param d the time in seconds.
	 * @param timeFormat Format string e.g. "%02d:%02d:%02f" or use the
	 *            predefined constants {@link #SEC_TIME_FORMAT},
	 *            {@link #DURATION_TIME_FORMAT}.
	 *
	 * @return The converted {@link String}.
	 */
	public static String convertTimeToString(double d, String timeFormat) {
		StringBuilder sb = new StringBuilder();
		try (Formatter formatter = new Formatter(sb, Locale.US)) {
			double s = d % 60;
			int h = (int) (d / 3600);
			int m = ((int) (d / 60)) % 60;
			formatter.format(timeFormat, h, m, s);
		}

		return sb.toString();
	}

	/**
	 * Converts a duration in seconds to the DIDL-Lite specified duration
	 * format.
	 *
	 * @param duration the duration in seconds.
	 * @return The formatted duration.
	 */
	@Nonnull
	public static String formatDLNADuration(double duration) {
		double seconds;
		int hours;
		int minutes;
		if (duration < 0) {
			seconds = 0.0;
			hours = 0;
			minutes = 0;
		} else {
			seconds = duration % 60;
			hours = (int) (duration / 3600);
			minutes = ((int) (duration / 60)) % 60;
		}
		if (hours > 99999) {
			// As per DLNA standard
			hours = 99999;
		}
		StringBuilder sb = new StringBuilder();
		try (Formatter formatter = new Formatter(sb, Locale.ROOT)) {
			formatter.format(DLNA_DURATION_FORMAT, hours, minutes, seconds);
		}
		return sb.toString();
	}

	/**
	 * Returns a formatted duration string in the form
	 * {@code y years d days HH:mm:ss[.SSS]} where the specified {@code double}
	 * value is interpreted as the number of seconds. If years or days are zero
	 * they are omitted, the same applies to hours and minutes unless a larger
	 * element is already included.
	 *
	 * @param duration the duration in seconds.
	 * @param includeMilliseconds if {@code true} the number of milliseconds
	 *            will be included, if {@code false} the second will be rounded
	 *            to the closest integer value.
	 * @return The formatted duration.
	 */
	public static String formatDuration(double duration, boolean includeMilliseconds) {
		return formatDuration((long) (duration * 1000), includeMilliseconds);
	}

	/**
	 * Returns a formatted duration string in the form
	 * {@code y years d days HH:mm:ss[.SSS]} where the specified {@code long}
	 * value is interpreted as the number of milliseconds. If years or days are
	 * zero they are omitted, the same applies to hours and minutes unless a
	 * larger element is already included.
	 *
	 * @param duration the duration in milliseconds.
	 * @param includeMilliseconds if {@code true} the number of milliseconds
	 *            will be included, if {@code false} the second will be rounded
	 *            to the closest integer value.
	 * @return The formatted duration.
	 */
	public static String formatDuration(long duration, boolean includeMilliseconds) {
		long delta;
		int hours;
		int minutes;
		int seconds;
		long ms = duration < 0 ? -duration : duration;
		StringBuilder sb = new StringBuilder();
		delta = ms / 31536000000L; // 365 days
		if (delta > 0) {
			sb.append(delta).append(delta == 1 ? " year" : " years");
			ms = ms % 31536000000L;
		}
		delta = ms / 86400000L; // 24 hours
		if (delta > 0) {
			if (sb.length() > 0) {
				sb.append(" ");
			}
			sb.append(delta).append(delta == 1 ? " day" : " days");
			ms = ms % 86400000L;
		}

		delta = ms / 1000;
		if (delta > 0 || (includeMilliseconds && ms > 0)) {
			if (sb.length() > 0) {
				sb.append(" ");
			}
			boolean includeAll = ms < duration;
			hours = (int) (delta / 3600);
			minutes = ((int) (delta / 60)) % 60;
			seconds = includeMilliseconds ? (int) (delta % 60) : (int) Math.round((ms % 60000) / 1000d);
			ms = ms % 1000;
			if (includeAll || hours > 0) {
				if (includeAll && hours < 10) {
					sb.append(String.format((Locale) null, "%02d:", hours));
				} else {
					sb.append(hours).append(":");
				}
			}
			if (includeAll || hours > 0) {
				sb.append(String.format((Locale) null, "%02d:", minutes));
			} else if (minutes > 0) {
				sb.append(minutes).append(":");
			}
			if (includeAll || hours > 0 || minutes > 0) {
				sb.append(String.format((Locale) null, "%02d", seconds));
			} else {
				sb.append(seconds);
			}
			if (includeMilliseconds) {
				sb.append(".").append(String.format((Locale) null, "%03d", ms));
			}
		}
		return sb.toString();
	}

	/**
	 * Returns an unlocalized, formatted time string in the form
	 * {@code HH:mm:ss} for the specified {@link Calendar}.
	 *
	 * @param calendar the {@link Calendar}.
	 * @return The formatted {@link String}.
	 * @throws IllegalArgumentException If {@code calendar} is {@code null}.
	 */
	@Nonnull
	public static String formatTime(@Nonnull Calendar calendar) {
		if (calendar == null) {
			throw new IllegalArgumentException("calendar cannot be null");
		}
		return formatDateTime(calendar.getTime().getTime(), false);
	}

	/**
	 * Returns an unlocalized, formatted time string in the form
	 * {@code HH:mm:ss} for the specified {@link Date}.
	 *
	 * @param date the {@link Date}.
	 * @return The formatted {@link String}.
	 * @throws IllegalArgumentException If {@code date} is {@code null}.
	 */
	@Nonnull
	public static String formatTime(@Nonnull Date date) {
		if (date == null) {
			throw new IllegalArgumentException("date cannot be null");
		}
		return formatDateTime(date.getTime(), false);
	}

	/**
	 * Returns an unlocalized, formatted time string in the form
	 * {@code HH:mm:ss} for the specified {@link Timestamp}.
	 *
	 * @param timestamp the {@link Timestamp}.
	 * @return The formatted {@link String}.
	 * @throws IllegalArgumentException If {@code timestamp} is {@code null}.
	 */
	@Nonnull
	public static String formatTime(@Nonnull Timestamp timestamp) {
		if (timestamp == null) {
			throw new IllegalArgumentException("timestamp cannot be null");
		}
		return formatDateTime(timestamp.getTime(), false);
	}

	/**
	 * Returns an unlocalized, formatted time string in the form
	 * {@code HH:mm:ss} where the specified long value is interpreted as the
	 * number of milliseconds since January 1, 1970, 00:00:00 GMT (epoch).
	 *
	 * @param time the number of milliseconds since January 1, 1970, 00:00:00
	 *            GMT.
	 * @return The formatted {@link String}.
	 */
	@Nonnull
	public static String formatTime(long time) {
		return formatDateTime(time, false);
	}

	/**
	 * Returns an unlocalized, formatted date and time string in the form
	 * {@code yyyy-MM-dd HH:mm:ss} for the specified {@link Date}.
	 *
	 * @param date the {@link Date}.
	 * @return The formatted {@link String}.
	 * @throws IllegalArgumentException If {@code date} is {@code null}.
	 */
	@Nonnull
	public static String formatDateTime(@Nonnull Date date) {
		if (date == null) {
			throw new IllegalArgumentException("date cannot be null");
		}
		return formatDateTime(date.getTime(), true);
	}

	/**
	 * Returns an unlocalized, formatted date and time string in the form
	 * {@code yyyy-MM-dd HH:mm:ss} for the specified {@link Timestamp}.
	 *
	 * @param timestamp the {@link Timestamp}.
	 * @return The formatted {@link String}.
	 * @throws IllegalArgumentException If {@code timestamp} is {@code null}.
	 */
	@Nonnull
	public static String formatDateTime(@Nonnull Timestamp timestamp) {
		if (timestamp == null) {
			throw new IllegalArgumentException("timestamp cannot be null");
		}
		return formatDateTime(timestamp.getTime(), true);
	}

	/**
	 * Returns an unlocalized, formatted date and time string in the form
	 * {@code yyyy-MM-dd HH:mm:ss} where the specified long value is interpreted
	 * as the number of milliseconds since January 1, 1970, 00:00:00 GMT
	 * (epoch).
	 *
	 * @param time the number of milliseconds since January 1, 1970, 00:00:00
	 *            GMT.
	 * @return The formatted {@link String}.
	 */
	@Nonnull
	public static String formatDateTime(long time) {
		return formatDateTime(time, true);
	}

	/**
	 * Returns an unlocalized, formatted date and time string in the form
	 * {@code yyyy-MM-dd HH:mm:ss} for the specified {@link Calendar}.
	 *
	 * @param calendar the {@link Calendar}.
	 * @return The formatted {@link String}.
	 * @throws IllegalArgumentException If {@code calendar} is {@code null}.
	 */
	@Nonnull
	public static String formatDateTime(@Nonnull Calendar calendar) {
		if (calendar == null) {
			throw new IllegalArgumentException("calendar cannot be null");
		}
		return formatDateTime(calendar, true);
	}

	/**
	 * Returns an unlocalized, formatted date and time string in the form
	 * {@code HH:mm:ss} or {@code yyyy-MM-dd HH:mm:ss} where the specified long
	 * value is interpreted as the number of milliseconds since January 1, 1970,
	 * 00:00:00 GMT (epoch). The date part is only included if the date is
	 * different from the current date.
	 *
	 * @param time the number of milliseconds since January 1, 1970, 00:00:00
	 *            GMT.
	 * @return The formatted {@link String}.
	 */
	@Nonnull
	public static String formatDateTimeAuto(long time) {
		long now = System.currentTimeMillis();
		TimeZone localTimeZone = TimeZone.getDefault();
		now += localTimeZone.getOffset(now);
		long localTime = time + localTimeZone.getOffset(time);
		return formatDateTime(time, now / 86400000L != localTime / 86400000L);
	}

	/**
	 * Returns an unlocalized, formatted date and time string in the form
	 * {@code HH:mm:ss} or {@code yyyy-MM-dd HH:mm:ss} where the specified long
	 * value is interpreted as the number of milliseconds since January 1, 1970,
	 * 00:00:00 GMT (epoch).
	 *
	 * @param time the number of milliseconds since January 1, 1970, 00:00:00
	 *            GMT.
	 * @param includeDate {@code true} to include the date part, {@code false}
	 *            to only show the time.
	 * @return The formatted {@link String}.
	 */
	@Nonnull
	public static String formatDateTime(long time, boolean includeDate) {
		return time == Long.MAX_VALUE ?
			"Never" :
			String.format(Locale.ROOT, includeDate ? "%tY-%<tm-%<td %<tH:%<tM:%<tS" : "%tH:%<tM:%<tS", time);
	}

	/**
	 * Returns an unlocalized, formatted date and time string in the form
	 * {@code HH:mm:ss} or {@code yyyy-MM-dd HH:mm:ss} for the specified
	 * {@link Calendar}.
	 *
	 * @param calendar the {@link Calendar}.
	 * @param includeDate {@code true} to include the date part, {@code false}
	 *            to only show the time.
	 * @return The formatted {@link String}.
	 * @throws IllegalArgumentException If {@code calendar} is {@code null}.
	 */
	@Nonnull
	public static String formatDateTime(@Nonnull Calendar calendar, boolean includeDate) {
		if (calendar == null) {
			throw new IllegalArgumentException("calendar cannot be null");
		}
		return String.format(Locale.ROOT, includeDate ? "%tY-%<tm-%<td %<tH:%<tM:%<tS" : "%tH:%<tM:%<tS", calendar);
	}

	/**
	 * Removes leading zeros up to the nth char of an hh:mm:ss time string,
	 * normalizing it first if necessary.
	 *
	 * @param time the time string.
	 * @param n position to stop checking
	 *
	 * @return the Shortened String.
	 */
	@Nonnull
	public static String shortTime(@Nullable String time, int n) {
		n = n < 8 ? n : 8;
		if (!isBlank(time)) {
			if (time.startsWith("NOT_IMPLEMENTED")) {
				return time.length() > 15 ? time.substring(15) : " ";
			}
			int i = time.indexOf('.');
			// Throw out the decimal portion, if any
			if (i > -1) {
				time = time.substring(0, i);
			}
			int l = time.length();
			// Normalize if necessary
			if (l < 8) {
				time = "00:00:00".substring(0, 8 - l) + time;
			} else if (l > 8) {
				time = time.substring(l - 8);
			}
			for (i = 0; i < n; i++) {
				if (time.charAt(i) != "00:00:00".charAt(i)) {
					break;
				}
			}
			return time.substring(i);
		}
		return "00:00:00".substring(n);
	}

	public static boolean isZeroTime(@Nullable String t) {
		return isBlank(t) || "00:00:00.000".contains(t);
	}

	/**
	 * Returns the first four digit number that can look like a year from the
	 * specified {@link CharSequence}.
	 *
	 * @param date the {@link CharSequence} from which to extract the year.
	 * @return The extracted year or {@code -1} if no valid year is found.
	 */
	public static int getYear(@Nullable CharSequence date) {
		if (isBlank(date)) {
			return -1;
		}
		Pattern pattern = Pattern.compile("\\b\\d{4}\\b");
		Matcher matcher = pattern.matcher(date);
		while (matcher.find()) {
			int result = Integer.parseInt(matcher.group());
			if (result > 1600 && result < 2100) {
				return result;
			}
		}
		return -1;
	}

	/**
	 * Extracts the first four digit number that can look like a year from both
	 * {@link CharSequence}s and compares them.
	 *
	 * @param firstDate the first {@link CharSequence} containing a year.
	 * @param secondDate the second {@link CharSequence} containing a year.
	 * @return {@code true} if a year can be extracted from both
	 *         {@link CharSequence}s and they have the same numerical value,
	 *         {@code false} otherwise.
	 */
	public static boolean isSameYear(@Nullable CharSequence firstDate, @Nullable CharSequence secondDate) {
		int first = getYear(firstDate);
		if (first < 0) {
			return false;
		}
		int second = getYear(secondDate);
		if (second < 0) {
			return false;
		}
		return first == second;
	}

	/**
	 * Compares the specified {@link String}s for equality. Either
	 * {@link String} can be {@code null}, and they are considered equal if both
	 * are {@code null}.
	 *
	 * @param first the first {@link String} to compare.
	 * @param second the second {@link String} to compare.
	 * @return {@code true} if the two {@link String}s are equal, {@code false}
	 *         otherwise.
	 * @throws IllegalArgumentException if an invalid combination of parameters
	 *             is specified.
	 * @throws IndexOutOfBoundsException if {@code fromIdx} or {@code toIdx} is
	 *             positive and outside the bounds of {@code first} or
	 *             {@code second}.
	 */
	public static boolean isEqual(
		@Nullable String first,
		@Nullable String second
	) {
		return isEqual(first, second, false, false, false, null, false, 0, -1, -1);
	}

	/**
	 * Compares the specified {@link String}s for equality. Either
	 * {@link String} can be {@code null}, and they are considered equal if both
	 * are {@code null}.
	 *
	 * @param first the first {@link String} to compare.
	 * @param second the second {@link String} to compare.
	 * @param blankIsNull if {@code true} a blank {@link String} will be equal
	 *            to any other blank {@link String} or {@code null}.
	 * @return {@code true} if the two {@link String}s are equal according to
	 *         the rules set by the parameters, {@code false} otherwise.
	 * @throws IllegalArgumentException if an invalid combination of parameters
	 *             is specified.
	 * @throws IndexOutOfBoundsException if {@code fromIdx} or {@code toIdx} is
	 *             positive and outside the bounds of {@code first} or
	 *             {@code second}.
	 */
	public static boolean isEqual(
		@Nullable String first,
		@Nullable String second,
		boolean blankIsNull
	) {
		return isEqual(first, second, blankIsNull, false, false, null, false, 0, -1, -1);
	}

	/**
	 * Compares the specified {@link String}s for equality according to the
	 * rules set by the parameters. Either {@link String} can be {@code null},
	 * and they are considered equal if both are {@code null}.
	 *
	 * @param first the first {@link String} to compare.
	 * @param second the second {@link String} to compare.
	 * @param blankIsNull if {@code true} a blank {@link String} will be equal
	 *            to any other blank {@link String} or {@code null}.
	 * @param trim {@code true} to {@link String#trim()} both {@link String}s
	 *            before comparison, {@code false} otherwise.
	 * @param ignoreCase {@code true} to convert both {@link String}s to
	 *            lower-case using the specified {@link Locale} before
	 *            comparison, {@code false} otherwise.
	 * @param locale the {@link Locale} to use when converting both
	 *            {@link String}s to lower-case if {@code ignoreCase} is
	 *            {@code true}. Ignored if {@code ignoreCase} is {@code false}.
	 *            If {@code null}, {@link Locale#ROOT} will be used.
	 * @return {@code true} if the two {@link String}s are equal according to
	 *         the rules set by the parameters, {@code false} otherwise.
	 */
	public static boolean isEqual(
		@Nullable String first,
		@Nullable String second,
		boolean blankIsNull,
		boolean trim,
		boolean ignoreCase,
		@Nullable Locale locale
	) {
		return isEqual(first, second, blankIsNull, trim, ignoreCase, locale, false, 0, -1, -1);
	}

	/**
	 * Compares the specified {@link String}s for equality according to the
	 * rules set by the parameters. Either {@link String} can be {@code null},
	 * and they are considered equal if both are {@code null}.
	 *
	 * @param first the first {@link String} to compare.
	 * @param second the second {@link String} to compare.
	 * @param blankIsNull if {@code true} a blank {@link String} will be equal
	 *            to any other blank {@link String} or {@code null}.
	 * @param trim {@code true} to {@link String#trim()} both {@link String}s
	 *            before comparison, {@code false} otherwise.
	 * @param ignoreCase {@code true} to convert both {@link String}s to
	 *            lower-case using the specified {@link Locale} before
	 *            comparison, {@code false} otherwise.
	 * @param locale the {@link Locale} to use when converting both
	 *            {@link String}s to lower-case if {@code ignoreCase} is
	 *            {@code true}. Ignored if {@code ignoreCase} is {@code false}.
	 *            If {@code null}, {@link Locale#ROOT} will be used.
	 * @param shortest {@code true} to only compare the length of the shortest
	 *            of the two {@link String}s.
	 * @param minLength the minimum length to compare if {@code shortest} is
	 *            true. If this is zero, an empty {@link String} will equal any
	 *            {@link String}.
	 * @return {@code true} if the two {@link String}s are equal according to
	 *         the rules set by the parameters, {@code false} otherwise.
	 */
	public static boolean isEqual(
		@Nullable String first,
		@Nullable String second,
		boolean blankIsNull,
		boolean trim,
		boolean ignoreCase,
		@Nullable Locale locale,
		boolean shortest,
		int minLength
	) {
		return isEqual(first, second, blankIsNull, trim, ignoreCase, locale, shortest, minLength, -1, -1);
	}

	/**
	 * Compares the specified {@link String}s for equality according to the
	 * rules set by the parameters. Either {@link String} can be {@code null},
	 * and they are considered equal if both are {@code null}.
	 *
	 * @param first the first {@link String} to compare.
	 * @param second the second {@link String} to compare.
	 * @param blankIsNull if {@code true} a blank {@link String} will be equal
	 *            to any other blank {@link String} or {@code null}.
	 * @param ignoreCase {@code true} to convert both {@link String}s to
	 *            lower-case using the specified {@link Locale} before
	 *            comparison, {@code false} otherwise.
	 * @param locale the {@link Locale} to use when converting both
	 *            {@link String}s to lower-case if {@code ignoreCase} is
	 *            {@code true}. Ignored if {@code ignoreCase} is {@code false}.
	 *            If {@code null}, {@link Locale#ROOT} will be used.
	 * @param fromIdx compare only from the character of this index.
	 * @return {@code true} if the two {@link String}s are equal according to
	 *         the rules set by the parameters, {@code false} otherwise.
	 * @throws IllegalArgumentException {@code toIdx} is positive and is smaller
	 *             than {@code fromIdx}.
	 * @throws IndexOutOfBoundsException if {@code fromIdx} or {@code toIdx} is
	 *             positive and outside the bounds of {@code first} or
	 *             {@code second}.
	 */
	public static boolean isEqualFrom(
		@Nullable String first,
		@Nullable String second,
		boolean blankIsNull,
		boolean ignoreCase,
		@Nullable Locale locale,
		int fromIdx
	) {
		return isEqual(first, second, blankIsNull, false, ignoreCase, locale, false, 0, fromIdx, -1);
	}

	/**
	 * Compares the specified {@link String}s for equality according to the
	 * rules set by the parameters. Either {@link String} can be {@code null},
	 * and they are considered equal if both are {@code null}.
	 *
	 * @param first the first {@link String} to compare.
	 * @param second the second {@link String} to compare.
	 * @param blankIsNull if {@code true} a blank {@link String} will be equal
	 *            to any other blank {@link String} or {@code null}.
	 * @param ignoreCase {@code true} to convert both {@link String}s to
	 *            lower-case using the specified {@link Locale} before
	 *            comparison, {@code false} otherwise.
	 * @param locale the {@link Locale} to use when converting both
	 *            {@link String}s to lower-case if {@code ignoreCase} is
	 *            {@code true}. Ignored if {@code ignoreCase} is {@code false}.
	 *            If {@code null}, {@link Locale#ROOT} will be used.
	 * @param toIdx compare only to (not including) the character of this index.
	 *            To compare to the end of the {@link String}, use {@code -1} or
	 *            the index position after the last character (the same as the
	 *            length).
	 * @return {@code true} if the two {@link String}s are equal according to
	 *         the rules set by the parameters, {@code false} otherwise.
	 * @throws IllegalArgumentException {@code toIdx} is positive and is smaller
	 *             than {@code fromIdx}.
	 * @throws IndexOutOfBoundsException if {@code fromIdx} or {@code toIdx} is
	 *             positive and outside the bounds of {@code first} or
	 *             {@code second}.
	 */
	public static boolean isEqualTo(
		@Nullable String first,
		@Nullable String second,
		boolean blankIsNull,
		boolean ignoreCase,
		@Nullable Locale locale,
		int toIdx
	) {
		return isEqual(first, second, blankIsNull, false, ignoreCase, locale, false, 0, -1, toIdx);
	}


	/**
	 * Compares the specified {@link String}s for equality according to the
	 * rules set by the parameters. Either {@link String} can be {@code null},
	 * and they are considered equal if both are {@code null}.
	 *
	 * @param first the first {@link String} to compare.
	 * @param second the second {@link String} to compare.
	 * @param blankIsNull if {@code true} a blank {@link String} will be equal
	 *            to any other blank {@link String} or {@code null}.
	 * @param ignoreCase {@code true} to convert both {@link String}s to
	 *            lower-case using the specified {@link Locale} before
	 *            comparison, {@code false} otherwise.
	 * @param locale the {@link Locale} to use when converting both
	 *            {@link String}s to lower-case if {@code ignoreCase} is
	 *            {@code true}. Ignored if {@code ignoreCase} is {@code false}.
	 *            If {@code null}, {@link Locale#ROOT} will be used.
	 * @param fromIdx compare only from the character of this index.
	 * @param toIdx compare only to (not including) the character of this index.
	 *            To compare to the end of the {@link String}, use {@code -1} or
	 *            the index position after the last character (the same as the
	 *            length).
	 * @return {@code true} if the two {@link String}s are equal according to
	 *         the rules set by the parameters, {@code false} otherwise.
	 * @throws IllegalArgumentException {@code toIdx} is positive and is smaller
	 *             than {@code fromIdx}.
	 * @throws IndexOutOfBoundsException if {@code fromIdx} or {@code toIdx} is
	 *             positive and outside the bounds of {@code first} or
	 *             {@code second}.
	 */
	public static boolean isEqual(
		@Nullable String first,
		@Nullable String second,
		boolean blankIsNull,
		boolean ignoreCase,
		@Nullable Locale locale,
		int fromIdx,
		int toIdx
	) {
		return isEqual(first, second, blankIsNull, false, ignoreCase, locale, false, 0, fromIdx, toIdx);
	}

	/**
	 * Compares the specified {@link String}s for equality according to the
	 * rules set by the parameters. Either {@link String} can be {@code null},
	 * and they are considered equal if both are {@code null}.
	 *
	 * @param first the first {@link String} to compare.
	 * @param second the second {@link String} to compare.
	 * @param blankIsNull if {@code true} a blank {@link String} will be equal
	 *            to any other blank {@link String} or {@code null}.
	 * @param trim {@code true} to {@link String#trim()} both {@link String}s
	 *            before comparison, {@code false} otherwise. Cannot be used
	 *            together with {@code fromIdx} or {@code toIdx}.
	 * @param ignoreCase {@code true} to convert both {@link String}s to
	 *            lower-case using the specified {@link Locale} before
	 *            comparison, {@code false} otherwise.
	 * @param locale the {@link Locale} to use when converting both
	 *            {@link String}s to lower-case if {@code ignoreCase} is
	 *            {@code true}. Ignored if {@code ignoreCase} is {@code false}.
	 *            If {@code null}, {@link Locale#ROOT} will be used.
	 * @param shortest {@code true} to only compare the length of the shortest
	 *            of the two {@link String}s. Cannot be used together with
	 *            {@code fromIdx} or {@code toIdx}.
	 * @param minLength the minimum length to compare if {@code shortest} is
	 *            true. If this is zero, an empty {@link String} will equal any
	 *            {@link String}.
	 * @param fromIdx compare only from the character of this index. Cannot be
	 *            used together with {@code trim} or {@code shortest}.
	 * @param toIdx compare only to (not including) the character of this index.
	 *            To compare to the end of the {@link String}, use {@code -1} or
	 *            the index position after the last character (the same as the
	 *            length). Cannot be used together with {@code trim} or
	 *            {@code shortest}.
	 * @return {@code true} if the two {@link String}s are equal according to
	 *         the rules set by the parameters, {@code false} otherwise.
	 * @throws IllegalArgumentException if an invalid combination of parameters
	 *             is specified.
	 * @throws IndexOutOfBoundsException if {@code fromIdx} or {@code toIdx} is
	 *             positive and outside the bounds of {@code first} or
	 *             {@code second}.
	 */
	protected static boolean isEqual(
		@Nullable String first,
		@Nullable String second,
		boolean blankIsNull,
		boolean trim,
		boolean ignoreCase,
		@Nullable Locale locale,
		boolean shortest,
		int minLength,
		int fromIdx,
		int toIdx
	) {
		if ((trim || shortest) && (fromIdx >= 0 || toIdx >= 0)) {
			throw new IllegalArgumentException("trim or shortest and index range can't be used together");
		}
		if (blankIsNull) {
			if (first == null) {
				first = "";
			}
			if (second == null) {
				second = "";
			}
		} else {
			if (first == null || second == null) {
				return first == null && second == null;
			}
		}
		// No null after this point

		if (trim) {
			first = first.trim();
			second = second.trim();
		}

		if (ignoreCase) {
			if (locale == null) {
				locale = Locale.ROOT;
			}
			first = first.toLowerCase(locale);
			second = second.toLowerCase(locale);
		}

		if (shortest) {
			if (first.length() != second.length()) {
				int shortestIdx = Math.max(Math.min(first.length(), second.length()), minLength);
				first = first.substring(0, Math.min(shortestIdx, first.length()));
				second = second.substring(0, Math.min(shortestIdx, second.length()));
			}
		} else if (fromIdx >= 0 || toIdx >= 0) {
			if (fromIdx == toIdx) {
				return true;
			}
			if (fromIdx > toIdx && toIdx >= 0) {
				throw new IllegalArgumentException("fromIdx (" + fromIdx + ") > toIdx (" + toIdx + ")");
			}
			if (fromIdx >= first.length() || fromIdx >= second.length()) {
				throw new IndexOutOfBoundsException(
					"fromIdx=" + fromIdx + ", first length=" + first.length() + ", second length=" + second.length()
				);
			}
			if (toIdx > first.length() || toIdx > second.length()) {
				throw new IndexOutOfBoundsException(
					"toIdx=" + fromIdx + ", first length=" + first.length() + ", second length=" + second.length()
				);
			}
			if (fromIdx < 0) {
				fromIdx = 0;
			}
			if (toIdx < 0) {
				first = first.substring(fromIdx);
				second = second.substring(fromIdx);
			} else {
				first = first.substring(fromIdx, toIdx);
				second = second.substring(fromIdx, toIdx);
			}
		}

		if (blankIsNull && (isBlank(first) || isBlank(second))) {
			return isBlank(first) && isBlank(second);
		}

		return first.equals(second);
	}

	/**
	 * A unicode unescaper that translates unicode escapes, e.g. {@code \u005c},
	 * while leaving intact any sequences that can't be interpreted as escaped
	 * unicode.
	 */
	public static class LaxUnicodeUnescaper extends UnicodeUnescaper {
		@Override
		public int translate(CharSequence input, int index, Writer out) throws IOException {
			try {
				return super.translate(input, index, out);
			} catch (IllegalArgumentException e) {
				// Leave it alone and continue
			}
			return 0;
		}
	}

	/**
	 * Returns the argument string surrounded with quotes if it contains a space,
	 * otherwise returns the string as is.
	 *
	 * @param arg The argument string
	 * @return The string, optionally in quotes.
	 */
	public static String quoteArg(String arg) {
		if (arg != null && arg.indexOf(' ') > -1) {
			return "\"" + arg + "\"";
		}

		return arg;
	}

	/**
	 * Fill a string in a unicode safe way.
	 *
	 * @param subString The <code>String</code> to be filled with
	 * @param count The number of times to repeat the <code>String</code>
	 * @return The filled string
	 */
	public static String fillString(String subString, int count) {
		StringBuilder sb = new StringBuilder(subString.length() * count);
		for (int i = 0; i < count; i++) {
			sb.append(subString);
		}
		return sb.toString();
	}

	/**
	 * Fill a string in a unicode safe way provided that the char array contains
	 * a valid unicode sequence.
	 *
	 * @param chars The <code>char[]</code> to be filled with
	 * @param count The number of times to repeat the <code>char[]</code>
	 * @return The filled string
	 */
	public static String fillString(char[] chars, int count) {
		StringBuilder sb = new StringBuilder(chars.length * count);
		for (int i = 0; i < count; i++) {
			sb.append(chars);
		}
		return sb.toString();
	}

	/**
	 * Fill a string in a unicode safe way. 8 bit (&lt; 256) code points
	 * equals ISO 8859-1 codes.
	 *
	 * @param codePoint The unicode code point to be filled with
	 * @param count The number of times to repeat the unicode code point
	 * @return The filled string
	 */
	public static String fillString(int codePoint, int count) {
		return fillString(Character.toChars(codePoint), count);
	}

	/**
	 * Returns the <code>body</code> of a HTML {@link String} formatted by
	 * {@link HTMLEditorKit} as typically used by {@link JEditorPane} and
	 * {@link JTextPane} stripped for tags, newline, indentation and with
	 * <code>&lt;br&gt;</code> tags converted to newline.<br>
	 * <br>
	 * <strong>Note: This is not a universal or sophisticated HTML stripping
	 * method, but is purpose built for these circumstances.</strong>
	 *
	 * @param html the HTML formatted text as described above
	 * @return The "deHTMLified" text
	 */
	public static String stripHTML(String html) {
		Pattern pattern = Pattern.compile("<body>(.*)</body>", Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
		Matcher matcher = pattern.matcher(html);
		if (matcher.find()) {
			return matcher.group(1).replaceAll("\n    ", "").trim().replaceAll("(?i)<br>", "\n").replaceAll("<.*?>", "");
		}
		throw new IllegalArgumentException("HTML text not as expected, must have <body> section");
	}

	/**
	 * Replaces a case-insensitive substring with the same substring in its
	 * provided capitalization. Used to make sure a given part of a
	 * {@link CharSequence} is capitalized in a defined manner.
	 *
	 * @param target the {@link CharSequence} to replace occurrences in.
	 * @param subString the {@link String} in its correctly capitalized form.
	 * @return The resulting {@link String}.
	 */
	public static String caseReplace(CharSequence target, String subString) {
		return Pattern.compile(subString, Pattern.CASE_INSENSITIVE + Pattern.LITERAL).matcher(target).replaceAll(subString);
	}

	/**
	 * Evaluates whether all the alphabetic characters are in upper-case in the
	 * specified {@link CharSequence}.
	 *
	 * @param cs the {@link CharSequence} to evaluate.
	 * @return {@code true} if all the alphabetic characters in {@code cs} are
	 *         in upper-case, {@code false} otherwise.
	 */
	public static boolean isUpperCase(CharSequence cs) {
		return !LOWER.matcher(cs).find();
	}

	/**
	 * Evaluates whether all the alphabetic characters are in lower-case in the
	 * specified {@link CharSequence}.
	 *
	 * @param cs the {@link CharSequence} to evaluate.
	 * @return {@code true} if all the alphabetic characters in {@code cs} are
	 *         in lower-case, {@code false} otherwise.
	 */
	public static boolean isLowerCase(CharSequence cs) {
		return !UPPER.matcher(cs).find();
	}

	/**
	 * Evaluates whether all the alphabetic characters are in the same case
	 * (either upper or lower) in the specified {@link CharSequence}.
	 *
	 * @param cs the {@link CharSequence} to evaluate.
	 * @return {@code true} if all the alphabetic characters in {@code cs} are
	 *         in the same case (either upper or lower), {@code false}
	 *         otherwise.
	 */
	public static boolean isSameCase(CharSequence cs) {
		return isLowerCase(cs) || isUpperCase(cs);
	}

	/**
	 * Escapes {@link org.apache.lucene} special characters with backslash.
	 *
	 * @param s the {@link String} to evaluate.
	 * @return The converted String.
	 */
	@SuppressFBWarnings("SF_SWITCH_NO_DEFAULT")
	public static String luceneEscape(final String s) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			char ch = s.charAt(i);
			switch (ch) {
				case '+':
				case '-':
				case '&':
				case '|':
				case '!':
				case '(':
				case ')':
				case '{':
				case '}':
				case '[':
				case ']':
				case '^':
				case '\"':
				case '~':
				case '*':
				case '?':
				case ':':
				case '\\':
				case '/':
					sb.append("\\");
				default:
					sb.append(ch);
			}
		}

		return sb.toString();
	}

	/**
	 * Escapes special characters with backslashes for FFmpeg subtitles.
	 *
	 * @param s the {@link String} to evaluate.
	 * @return The converted String.
	 */
	public static String ffmpegEscape(String s) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			char ch = s.charAt(i);
			switch (ch) {
				case '\'':
					sb.append("\\\\\\'");
					break;
				case ':':
					sb.append("\\\\:");
					break;
				case '\\':
					sb.append("/");
					break;
				case ']':
				case '[':
				case ',':
				case ';':
					sb.append("\\");
					// Fall through
				default:
					sb.append(ch);
					break;
			}
		}

		return sb.toString();
	}

	/**
	 * Formats a XML string to be easier to read with newlines and indentations.
	 *
	 * @param xml the {@link String} to "prettify".
	 * @param indentWidth the width of one indentation in number of characters.
	 * @return The "prettified" {@link String}.
	 * @throws SAXException If a parsing error occurs.
	 * @throws ParserConfigurationException If a parsing error occurs.
	 * @throws XPathExpressionException If a parsing error occurs.
	 * @throws TransformerException If a parsing error occurs.
	 */
	public static String prettifyXML(
		String xml,
		int indentWidth
	) throws SAXException, ParserConfigurationException, XPathExpressionException, TransformerException {
		try {
			// Turn XML string into a document
			Document xmlDocument =
				SafeDocumentBuilderFactory.newInstance().
				newDocumentBuilder().
				parse(new InputSource(new ByteArrayInputStream(xml.getBytes("utf-8"))));

			// Remove whitespaces outside tags
			xmlDocument.normalize();
			XPath xPath = XPathFactory.newInstance().newXPath();
			NodeList nodeList = (NodeList) xPath.evaluate(
				"//text()[normalize-space()='']",
				xmlDocument,
				XPathConstants.NODESET
			);

			for (int i = 0; i < nodeList.getLength(); ++i) {
				Node node = nodeList.item(i);
				node.getParentNode().removeChild(node);
			}

			// Setup pretty print options
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			transformerFactory.setAttribute("indent-number", indentWidth);
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");

			// Return pretty print XML string
			StringWriter stringWriter = new StringWriter();
			transformer.transform(new DOMSource(xmlDocument), new StreamResult(stringWriter));
			return stringWriter.toString();
		} catch (IOException e) {
			LOGGER.warn("Failed to read XML document, returning the source document: {}", e.getMessage());
			LOGGER.trace("", e);
			return xml;
		}
	}

	/**
	 * Creates a "readable" string by combining the objects in {@code elements}
	 * while inserting "{@code ,}" and "{@code and}" as appropriate. The
	 * resulting {@link String} is in the form
	 * {@code "element 1, element2 and element3"}.
	 *
	 * @param elements the {@link Collection} of {@link String} to combine.
	 * @return The combined "readable" {@link String}.
	 */
	public static String createReadableCombinedString(Collection<? extends Object> elements) {
		return createReadableCombinedString(elements, false, null, null);
	}

	/**
	 * Creates a "readable" string by combining the objects in {@code elements}
	 * while inserting "{@code ,}" and "{@code and}" as appropriate. The
	 * resulting {@link String} is in the form
	 * {@code "element 1, element2 and element3"}.
	 *
	 * @param elements the {@link Collection} of {@link Object} to combine.
	 * @param quote if {@code true}, all elements will be quoted in
	 *            double-quotes.
	 * @return The combined "readable" {@link String}.
	 */
	public static String createReadableCombinedString(Collection<? extends Object> elements, boolean quote) {
		return createReadableCombinedString(elements, quote, null, null);
	}

	/**
	 * Creates a "readable" string by combining the objects in {@code elements}
	 * while inserting {@code separator} and {@code lastSeparator} as
	 * appropriate. The resulting {@link String} is in the form
	 * {@code "element 1<separator> element2 <lastSeparator> element3"}.
	 *
	 * @param elements the {@link Collection} of {@link Object} to combine.
	 * @param separator the "normal" separator used everywhere except between
	 *            the last two elements.
	 * @param lastSeparator the separator used between the last two elements.
	 * @return The combined "readable" {@link String}.
	 */
	public static String createReadableCombinedString(Collection<? extends Object> elements, String separator, String lastSeparator) {
		if (elements == null || elements.isEmpty()) {
			return "";
		}
		return createReadableCombinedString(elements.toArray(new Object[elements.size()]), false, separator, lastSeparator);
	}

	/**
	 * Creates a "readable" string by combining the objects in {@code elements}
	 * while inserting {@code separator} and {@code lastSeparator} as
	 * appropriate. The resulting {@link String} is in the form
	 * {@code "element 1<separator> element2 <lastSeparator> element"}.
	 *
	 * @param elements the {@link Collection} of {@link Object} to combine.
	 * @param quote if {@code true}, all elements will be quoted in
	 *            double-quotes.
	 * @param separator the "normal" separator used everywhere except between
	 *            the last two elements.
	 * @param lastSeparator the separator used between the last two elements.
	 * @return The combined "readable" {@link String}.
	 */
	public static String createReadableCombinedString(
		Collection<? extends Object> elements,
		boolean quote,
		String separator,
		String lastSeparator
	) {
		if (elements == null || elements.isEmpty()) {
			return "";
		}
		return createReadableCombinedString(elements.toArray(new Object[elements.size()]), quote, separator, lastSeparator);
	}

	/**
	 * Creates a "readable" string by combining the objects in {@code elements}
	 * while inserting "{@code ,}" and "{@code and}" as appropriate. The
	 * resulting {@link String} is in the form
	 * {@code "element 1, element2 and element3}.
	 *
	 * @param elements the elements to combine.
	 * @return The combined "readable" {@link String}.
	 */
	@Nonnull
	public static String createReadableCombinedString(@Nullable Object[] elements) {
		return createReadableCombinedString(elements, false, null, null);
	}

	/**
	 * Creates a "readable" string by combining the objects in {@code elements}
	 * while inserting "{@code ,}" and "{@code and}" as appropriate. The
	 * resulting {@link String} is in the form
	 * {@code "element 1, element2 and element3"}.
	 *
	 * @param elements the elements to combine.
	 * @param quote if {@code true}, all elements will be quoted in
	 *            double-quotes.
	 * @return The combined "readable" {@link String}.
	 */
	public static String createReadableCombinedString(@Nullable Object[] elements, boolean quote) {
		return createReadableCombinedString(elements, quote, null, null);
	}

	/**
	 * Creates a "readable" string by combining the objects in {@code elements}
	 * while inserting {@code separator} and {@code lastSeparator} as
	 * appropriate. The resulting {@link String} is in the form
	 * {@code "element 1<separator> element2 <lastSeparator> element3"}.
	 *
	 * @param elements the elements to combine.
	 * @param separator the "normal" separator used everywhere except between
	 *            the last two elements.
	 * @param lastSeparator the separator used between the last two elements.
	 * @return The combined "readable" {@link String}.
	 */
	@Nonnull
	public static String createReadableCombinedString(
		@Nullable Object[] elements,
		@Nullable String separator,
		@Nullable String lastSeparator
	) {
		return createReadableCombinedString(elements, false, separator, lastSeparator);
	}

	/**
	 * Creates a "readable" string by combining the objects in {@code elements}
	 * while inserting {@code separator} and {@code lastSeparator} as
	 * appropriate. The resulting {@link String} is in the form
	 * {@code "element 1<separator> element2 <lastSeparator> element3"}.
	 *
	 * @param elements the elements to combine.
	 * @param quote if {@code true}, all elements will be quoted in
	 *            double-quotes.
	 * @param separator the "normal" separator used everywhere except between
	 *            the last two elements.
	 * @param lastSeparator the separator used between the last two elements.
	 * @return The combined "readable" {@link String}.
	 */
	@Nonnull
	public static String createReadableCombinedString(
		@Nullable Object[] elements,
		boolean quote,
		@Nullable String separator,
		@Nullable String lastSeparator
	) {
		if (elements == null || elements.length == 0) {
			return "";
		}
		if (elements.length == 1 && elements[0] instanceof Collection<?>) {
			// This method will catch a Collection<?> argument as well, convert it to an array
			elements = ((Collection<?>) elements[0]).toArray();
		}
		if (separator == null) {
			separator = ", ";
		} else {
			separator += " ";
		}
		if (isBlank(lastSeparator)) {
			lastSeparator = " and ";
		} else {
			if (!lastSeparator.substring(0, 1).equals(" ")) {
				lastSeparator = " " + lastSeparator;
			}
			if (!lastSeparator.substring(lastSeparator.length() - 1).equals(" ")) {
				lastSeparator += " ";
			}
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < elements.length; i++) {
			if (i > 0) {
				if (i == elements.length - 1) {
					sb.append(lastSeparator);
				} else {
					sb.append(separator);
				}
			}
			if (quote) {
				sb.append("\"").append(elements[i]).append("\"");
			} else {
				sb.append(elements[i]);
			}
		}
		return sb.toString();
	}

	/**
	 * An {@code enum} representing letter cases.
	 */
	public enum LetterCase {

		/** Upper-case, uppercase, capital or majuscule */
		UPPER,

		/** Lower-case, lowercase or minuscule */
		LOWER
	}
}
