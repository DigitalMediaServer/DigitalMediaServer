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
package net.pms.util;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A utility class for conversions and parsers.
 *
 * @author Nadahar
 */
public class ConversionUtil {
	private static final Logger LOGGER = LoggerFactory.getLogger(ConversionUtil.class);

	/**
	 * A {@link Pattern} that matches decimal and hexadecimal numbers with
	 * trailing unit/text. Group 1 is the number part, group 2 is the unit/text
	 * part or {@code null}.
	 */
	public static final Pattern NUMBER_UNIT = Pattern.compile(
		"^\\s*([-+]?(?:(?:\\p{Digit}+|\\p{Digit}*\\.\\p{Digit}+)(?:\\s?[Ee][-+]?\\p{Digit}+)?|0[Xx](?:\\p{XDigit}+|" +
		"\\p{XDigit}*\\.\\p{XDigit}+)(?:\\s?[Ee][-+]?\\p{XDigit}+|\\s?[Pp][-+]?\\p{Digit}+)?))\\s?(\\p{L}\\w*|%)?\\s*$",
		Pattern.UNICODE_CHARACTER_CLASS
	);

	/**
	 * A {@link Pattern} that matches decimal and hexadecimal numbers. If the
	 * number is decimal, group 3 and 4 are always {@code null}.
	 * <p>
	 * If the number is hexadecimal, group 1 and 2 are always {@code null}.
	 * Group 1 (dec) or 3 (hexa) contains the significand. Group 2 (dec)
	 * contains the exponent without the prefix {@code "e"} or {@code "E"},
	 * while group 4 (hexa) contains the exponent including the prefix which is
	 * one of {@code "e"}, {@code "E"}, {@code "p"} or {@code "P"}.
	 */
	public static final Pattern NUMBER = Pattern.compile(
		"^\\s*(?:([-+]?(?:\\p{Digit}+|\\p{Digit}*\\.\\p{Digit}+))(?:\\s?[Ee]([-+]?\\p{Digit}+))?|" +
		"([-+]?0[Xx](?:\\p{XDigit}+|\\p{XDigit}*\\.\\p{XDigit}+))(?:\\s?([Ee][-+]?\\p{XDigit}+|[Pp][-+]?\\p{Digit}+))?)\\s*$",
		Pattern.UNICODE_CHARACTER_CLASS
	);

	/**
	 * Not to be instantiated.
	 */
	private ConversionUtil() {
	}

	/**
	 * Formats bytes into a rounded {@link String} representation in either
	 * binary/power of 2 or SI notation using {@link Locale#ROOT}.
	 *
	 * @param bytes the value to format.
	 * @param binary whether the representation should be binary/power of 2 or
	 *            SI/metric.
	 * @return The formatted byte value and unit.
	 */
	public static String formatBytes(long bytes, boolean binary) {
		return formatBytes(bytes, binary, Locale.ROOT);
	}

	/**
	 * Formats bytes into a rounded {@link String} representation in either
	 * binary/power of 2 or SI notation.
	 *
	 * @param bytes the value to format.
	 * @param binary whether the representation should be binary/power of 2 or
	 *            SI/metric.
	 * @param locale the {@link Locale} to use when formatting.
	 * @return The formatted byte value and unit.
	 */
	public static String formatBytes(long bytes, boolean binary, Locale locale) {
		if ((binary && bytes < 1L << 10) || bytes < UnitPrefix.KILO.getFactorLong()) {
			return String.format("%d %s", bytes, bytes == 1L ? "byte" : "bytes");
		}

		UnitPrefix unitPrefix;
		if ((binary && bytes < UnitPrefix.MEBI.getFactorLong()) || bytes < UnitPrefix.MEGA.getFactorLong()) { // kibi/kilo
			unitPrefix = binary ? UnitPrefix.KIBI : UnitPrefix.KILO;
		} else if ((binary && bytes < UnitPrefix.GIBI.getFactorLong()) || bytes < UnitPrefix.GIGA.getFactorLong()) { // mebi/mega
			unitPrefix = binary ? UnitPrefix.MEBI : UnitPrefix.MEGA;
		} else if ((binary && bytes < UnitPrefix.TEBI.getFactorLong()) || bytes < UnitPrefix.TERA.getFactorLong()) { // gibi/giga
			unitPrefix = binary ? UnitPrefix.GIBI : UnitPrefix.GIGA;
		} else if ((binary && bytes < UnitPrefix.PEBI.getFactorLong()) || bytes < UnitPrefix.PETA.getFactorLong()) { // tebi/tera
			unitPrefix = binary ? UnitPrefix.TEBI : UnitPrefix.TERA;
		} else if ((binary && bytes < UnitPrefix.EXBI.getFactorLong()) || bytes < UnitPrefix.EXA.getFactorLong()) { // pebi/peta
			unitPrefix = binary ? UnitPrefix.PEBI : UnitPrefix.PETA;
		} else { // exbi/exa
			unitPrefix = binary ? UnitPrefix.EXBI : UnitPrefix.EXA;
		}
		if (bytes % unitPrefix.getFactorLong() == 0) {
			return String.format(locale, "%d %s%s", bytes / unitPrefix.getFactorLong(), unitPrefix.getSymbol(), "B");
		}
		return String.format(locale, "%.1f %s%s", (double) bytes / unitPrefix.getFactorLong(), unitPrefix.getSymbol(), "B");
	}

	@Nullable
	public static Pair<Number, String> parseNumberWithUnit( //TODO: (Nad) Here
		@Nullable String value,
		@Nullable Locale locale,
		@Nullable MathContext mathContext,
		int roundingScale,
		@Nullable RoundingMode roundingMode,
		@Nullable String requiredUnit,
		@Nullable UnitPrefix defaultUnitPrefix,
		@Nullable String defaultUnit,
		boolean allowPercent,
		boolean caseSensitivePrefixes,
		boolean allowFractionalPrefixes
	) {
		if (isBlank(value)) {
			return null;
		}
		if (locale != null) {
			char c = DecimalFormatSymbols.getInstance(locale).getDecimalSeparator();
			if (c != '.' && value.indexOf(c) >= 0) {
				value = value.replace(".", "").replace(c, '.');
			}
		}

		Matcher matcher = NUMBER_UNIT.matcher(value);
		if (!matcher.find()) {
			LOGGER.warn("Unable to parse a numeric value from \"{}\"", value);
			return null;
		}

		Locale caseLocale = locale != null ? locale : Locale.ROOT;
		String lowerRequiredUnit = requiredUnit == null ? null : requiredUnit.toLowerCase(caseLocale);
		String unit;
		String lowerUnit;
		UnitPrefix unitPrefix = null;
		if (matcher.groupCount() < 2 || matcher.group(2) == null) {
			// No prefix or unit specified
			unit = defaultUnit;
			lowerUnit = defaultUnit == null ? null : defaultUnit.toLowerCase(caseLocale);
			unitPrefix = defaultUnitPrefix;
		} else {
			// Prefix and/or unit specified
			unit = matcher.group(2);
			if ("%".equals(unit)) {
				if (allowPercent) {
					Number number = parseNumber(matcher.group(1), locale, mathContext, roundingScale, roundingMode, true);
					if (number == null) {
						return null;
					}
					return new Pair<>(number, "%");
				}
				LOGGER.warn("Illegal unit \"%\" in \"{}\"", value);
				return null;
			}

			if (unit.equals(requiredUnit)) {
				Number number = parseNumber(matcher.group(1), locale, mathContext, roundingScale, roundingMode, true);
				if (number == null) {
					return null;
				}
				return new Pair<>(number, unit);
			}

			lowerUnit = unit.toLowerCase(caseLocale);
			if (lowerUnit.equals(lowerRequiredUnit)) {
				Number number = parseNumber(matcher.group(1), locale, mathContext, roundingScale, roundingMode, true);
				if (number == null) {
					return null;
				}
				return new Pair<>(number, unit);
			}

			String matchPrefix = caseSensitivePrefixes ? unit : lowerUnit;
			for (UnitPrefix prefix : UnitPrefix.values()) {
				if (!allowFractionalPrefixes && prefix.isFractional()) {
					continue;
				}
				String prefixSymbol = caseSensitivePrefixes ? prefix.getSymbol() : prefix.getSymbol().toLowerCase(caseLocale);
				boolean forceCaseSensitive =
					!caseSensitivePrefixes &&
					allowFractionalPrefixes && (
						"m".equals(prefixSymbol) ||
						"p".equals(prefixSymbol)
					);
				if (
					forceCaseSensitive && unit.startsWith(prefix.getSymbol()) ||
					!forceCaseSensitive && matchPrefix.startsWith(prefixSymbol)
				) {
					unitPrefix = prefix;
					unit = unit.substring(prefixSymbol.length());
					lowerUnit = lowerUnit.substring(prefixSymbol.length());
					break;
				}
			}
			if (isBlank(unit)) {
				unit = defaultUnit;
				lowerUnit = defaultUnit == null ? null : defaultUnit.toLowerCase(caseLocale);
			}
		}

		// Required unit
		if (lowerRequiredUnit != null && !lowerRequiredUnit.equals(lowerUnit)) {
			LOGGER.error("Invalid unit \"{}\" instead of \"{}\" in \"{}\"", unit, requiredUnit, value);
			return null;
		}

		if (unitPrefix == null) {
			// Without prefix
			Number number = parseNumber(matcher.group(1), locale, mathContext, roundingScale, roundingMode, true);
			if (number == null) {
				return null;
			}
			return new Pair<>(number, unit);
		}

		// With prefix
		Number number = parseNumber(matcher.group(1), locale, null, 0, null, false);
		if (number == null) {
			return null;
		}
		if (number instanceof Rational) {
			number = ((Rational) number).multiply(unitPrefix.getFactorRational());
		} else if (number instanceof BigInteger) {
			if (unitPrefix.isAsBigIntegerValid()) {
				number = ((BigInteger) number).multiply(unitPrefix.getFactorBigInteger());
			} else {
				number = Rational.valueOf((BigInteger) number).multiply(unitPrefix.getFactorRational());
			}
		}
		number = parseNumber(number, null, mathContext, roundingScale, roundingMode, true);
		return new Pair<>(number, unit);
	}

	/**
	 * Attempts to convert an object into an {@code int}. If the object is an
	 * integer {@link Number}, {@link Number#intValue()} is returned. If the
	 * object is {@code null} or a fractional number, {@code nullValue} is
	 * returned. Otherwise, an {@code int} is attempted parsed from
	 * {@link Object#toString()}. If the parsing fails, {@code nullValue} is
	 * returned.
	 *
	 * @param object the {@link Object} to convert to an {@code int}.
	 * @param nullValue the value to return if {@code object} is {@code null} or
	 *            the parsing fails.
	 * @return The parsed {@code int} or {@code nullValue}.
	 */
	public static int parseInt( //TODO: (Nad) Test
		@Nullable Object object,
		int nullValue
	) {
		return parseInt(object, null, null, nullValue);
	}

	/**
	 * Attempts to convert an object into an {@code int} after rounding. If the
	 * object is an integer {@link Number}, {@link Number#intValue()} is
	 * returned. If the object is {@code null} or a fractional number,
	 * {@code nullValue} is returned. Otherwise, an {@code int} is attempted
	 * parsed from {@link Object#toString()}. If the parsing fails,
	 * {@code nullValue} is returned.
	 *
	 * @param object the {@link Object} to convert to an {@code int}.
	 * @param locale the {@link Locale} to use for decimal numbers. If
	 *            {@code null}, {@code "."} is used as a decimal separator.
	 * @param mathContext the {@link MathContext} to use for rounding or
	 *            {@code null} for no rounding.
	 * @param nullValue the value to return if {@code object} is {@code null} or
	 *            the parsing fails.
	 * @return The parsed {@code int} or {@code nullValue}.
	 */
	public static int parseInt( //TODO: (Nad) Test
		@Nullable Object object,
		@Nullable Locale locale,
		@Nullable MathContext mathContext,
		int nullValue
	) {
		Number number = parseNumber(object, locale, mathContext, 0, null, true);
		return number instanceof Integer ? number.intValue() : nullValue;
	}

	/**
	 * Attempts to convert an object into an {@code int} after rounding. If the
	 * object is an integer {@link Number}, {@link Number#intValue()} is
	 * returned. If the object is {@code null} or a fractional number,
	 * {@code nullValue} is returned. Otherwise, an {@code int} is attempted
	 * parsed from {@link Object#toString()}. If the parsing fails,
	 * {@code nullValue} is returned.
	 *
	 * @param object the {@link Object} to convert to an {@code int}.
	 * @param locale the {@link Locale} to use for decimal numbers. If
	 *            {@code null}, {@code "."} is used as a decimal separator.
	 * @param roundingScale the number of digits to keep after the
	 *            decimal-separator if positive or the number of digits to
	 *            discard before the decimal-separator. Ignored if
	 *            {@code roundingMode} is {@code null}.
	 * @param roundingMode The {@link RoundingMode} to use. Use with scale
	 *            {@code 0} to round to an integer.
	 * @param nullValue the value to return if {@code object} is {@code null} or
	 *            the parsing fails.
	 * @return The parsed {@code int} or {@code nullValue}.
	 */
	public static int parseInt( //TODO: (Nad) Test
		@Nullable Object object,
		@Nullable Locale locale,
		int roundingScale,
		@Nullable RoundingMode roundingMode,
		int nullValue
	) {
		Number number = parseNumber(object, locale, null, roundingScale, roundingMode, true);
		return number instanceof Integer ? number.intValue() : nullValue;
	}

	/**
	 * Attempts to convert an object into a {@code long}. If the object is an
	 * integer {@link Number}, {@link Number#longValue()} is returned. If the
	 * object is {@code null} or a fractional number, {@code nullValue} is
	 * returned. Otherwise, a {@code long} is attempted parsed from
	 * {@link Object#toString()}. If the parsing fails, {@code nullValue} is
	 * returned.
	 *
	 * @param object the {@link Object} to convert to a {@code long}.
	 * @param nullValue the value to return if {@code object} is {@code null} or
	 *            the parsing fails.
	 * @return The parsed {@code long} or {@code nullValue}.
	 */
	public static long parseLong( //TODO: (Nad) Test
		@Nullable Object object,
		long nullValue
	) {
		return parseLong(object, null, null, nullValue);
	}

	/**
	 * Attempts to convert an object into a {@code long} after rounding. If the
	 * object is an integer {@link Number}, {@link Number#longValue()} is
	 * returned. If the object is {@code null} or a fractional number,
	 * {@code nullValue} is returned. Otherwise, a {@code long} is attempted
	 * parsed from {@link Object#toString()}. If the parsing fails,
	 * {@code nullValue} is returned.
	 *
	 * @param object the {@link Object} to convert to a {@code long}.
	 * @param locale the {@link Locale} to use for decimal numbers. If
	 *            {@code null}, {@code "."} is used as a decimal separator.
	 * @param mathContext the {@link MathContext} to use for rounding or
	 *            {@code null} for no rounding.
	 * @param nullValue the value to return if {@code object} is {@code null} or
	 *            the parsing fails.
	 * @return The parsed {@code long} or {@code nullValue}.
	 */
	public static long parseLong( //TODO: (Nad) Test
		@Nullable Object object,
		@Nullable Locale locale,
		@Nullable MathContext mathContext,
		long nullValue
	) {
		Number number = parseNumber(object, locale, mathContext, 0, null, true);
		return number instanceof Integer || number instanceof Long ? number.longValue() : nullValue;
	}

	/**
	 * Attempts to convert an object into a {@code long} after rounding. If the
	 * object is an integer {@link Number}, {@link Number#longValue()} is
	 * returned. If the object is {@code null} or a fractional number,
	 * {@code nullValue} is returned. Otherwise, a {@code long} is attempted
	 * parsed from {@link Object#toString()}. If the parsing fails,
	 * {@code nullValue} is returned.
	 *
	 * @param object the {@link Object} to convert to a {@code long}.
	 * @param locale the {@link Locale} to use for decimal numbers. If
	 *            {@code null}, {@code "."} is used as a decimal separator.
	 * @param roundingScale the number of digits to keep after the
	 *            decimal-separator if positive or the number of digits to
	 *            discard before the decimal-separator. Ignored if
	 *            {@code roundingMode} is {@code null}.
	 * @param roundingMode The {@link RoundingMode} to use. Use with scale
	 *            {@code 0} to round to an integer.
	 * @param nullValue the value to return if {@code object} is {@code null} or
	 *            the parsing fails.
	 * @return The parsed {@code long} or {@code nullValue}.
	 */
	public static long parseLong( //TODO: (Nad) Test
		@Nullable Object object,
		@Nullable Locale locale,
		int roundingScale,
		@Nullable RoundingMode roundingMode,
		long nullValue
	) {
		Number number = parseNumber(object, locale, null, roundingScale, roundingMode, true);
		return number instanceof Integer || number instanceof Long ? number.longValue() : nullValue;
	}

	/**
	 * Attempts to convert an object into a {@code double}. If the object is a
	 * {@link Number}, {@link Number#doubleValue()} is returned. If the object
	 * is {@code null}, {@code nullValue} is returned. Otherwise, a
	 * {@code double} is attempted parsed from {@link Object#toString()}. If the
	 * parsing fails, {@code nullValue} is returned.
	 *
	 * @param object the {@link Object} to convert to a {@code double}.
	 * @param locale the {@link Locale} to use for decimal numbers. If
	 *            {@code null}, {@code "."} is used as a decimal separator.
	 * @param mathContext the {@link MathContext} to use for rounding or
	 *            {@code null} for no rounding.
	 * @param nullValue the value to return if {@code object} is {@code null} or
	 *            the parsing fails.
	 * @return The parsed {@code double} or {@code nullValue}.
	 */
	public static double parseDouble( //TODO: (Nad) Test
		@Nullable Object object,
		@Nullable Locale locale,
		@Nullable MathContext mathContext,
		double nullValue
	) {
		Number number = parseNumber(object, locale, mathContext, 0, null, false);
		return number != null ? number.doubleValue() : nullValue;
	}

	/**
	 * Attempts to convert an object into a {@code double}. If the object is a
	 * {@link Number}, {@link Number#doubleValue()} is returned. If the object
	 * is {@code null}, {@code nullValue} is returned. Otherwise, a
	 * {@code double} is attempted parsed from {@link Object#toString()}. If the
	 * parsing fails, {@code nullValue} is returned.
	 *
	 * @param object the {@link Object} to convert to a {@code double}.
	 * @param locale the {@link Locale} to use for decimal numbers. If
	 *            {@code null}, {@code "."} is used as a decimal separator.
	 * @param roundingScale the number of digits to keep after the
	 *            decimal-separator if positive or the number of digits to
	 *            discard before the decimal-separator. Ignored if
	 *            {@code roundingMode} is {@code null}.
	 * @param roundingMode The {@link RoundingMode} to use. Use with scale
	 *            {@code 0} to round to an integer.
	 * @param nullValue the value to return if {@code object} is {@code null} or
	 *            the parsing fails.
	 * @return The parsed {@code double} or {@code nullValue}.
	 */
	public static double parseDouble( //TODO: (Nad) Test
		@Nullable Object object,
		@Nullable Locale locale,
		int roundingScale,
		@Nullable RoundingMode roundingMode,
		double nullValue
	) {
		Number number = parseNumber(object, locale, null, roundingScale, roundingMode, false);
		return number != null ? number.doubleValue() : nullValue;
	}

	/**
	 * Attempts to convert an object into a {@link Number}. If the object is a
	 * {@link Number}, the object itself is returned. If the object is
	 * {@code null}, {@code null} is returned. Otherwise, a {@link Number} is
	 * attempted parsed from {@link Object#toString()}. If the parsing fails,
	 * {@code null} is returned. No rounding will be applied.
	 * <p>
	 * The {@link Number} implementations that might be returned are
	 * {@link Rational}, {@link BigInteger}, {@link Integer}, {@link Long} or
	 * {@link Double}.
	 *
	 * @param object the {@link Object} to convert to a {@link Number}.
	 * @param locale the {@link Locale} to use for decimal numbers. If
	 *            {@code null}, {@code "."} is used as a decimal separator.
	 * @param reduceType if {@code true} the return type is the "smallest"
	 *            possible, if {@code false} the type that is used internally is
	 *            returned. "Smallest" means that {@link Integer} is preferred
	 *            over {@link Long}, which is preferred over {@link BigInteger}.
	 *            For fractional values {@link Double} is preferred over
	 *            {@link Rational}.
	 * @return The {@link Number} or {@code null}.
	 */
	@Nullable
	public static Number parseNumber(
		@Nullable Object object,
		@Nullable Locale locale,
		boolean reduceType
	) {
		return parseNumber(object, locale, null, 0, null, reduceType);
	}

	/**
	 * Attempts to convert an object into a {@link Number}. If the object is a
	 * {@link Number}, the object itself is returned. If the object is
	 * {@code null}, {@code null} is returned. Otherwise, a {@link Number} is
	 * attempted parsed from {@link Object#toString()}. If the parsing fails,
	 * {@code null} is returned.
	 * <p>
	 * The {@link Number} implementations that might be returned are
	 * {@link Rational}, {@link BigInteger}, {@link Integer}, {@link Long} or
	 * {@link Double}.
	 *
	 * @param object the {@link Object} to convert to a {@link Number}.
	 * @param locale the {@link Locale} to use for decimal numbers. If
	 *            {@code null}, {@code "."} is used as a decimal separator.
	 * @param mathContext the {@link MathContext} to use for rounding or
	 *            {@code null} for no rounding.
	 * @param reduceType if {@code true} the return type is the "smallest"
	 *            possible, if {@code false} the type that is used internally is
	 *            returned. "Smallest" means that {@link Integer} is preferred
	 *            over {@link Long}, which is preferred over {@link BigInteger}.
	 *            For fractional values {@link Double} is preferred over
	 *            {@link Rational}.
	 * @return The {@link Number} or {@code null}.
	 */
	@Nullable
	public static Number parseNumber(
		@Nullable Object object,
		@Nullable Locale locale,
		@Nullable MathContext mathContext,
		boolean reduceType
	) {
		return parseNumber(object, locale, mathContext, 0, null, reduceType);
	}

	/**
	 * /** Attempts to convert an object into a {@link Number}. If the object is
	 * a {@link Number}, the object itself is returned. If the object is
	 * {@code null}, {@code null} is returned. Otherwise, a {@link Number} is
	 * attempted parsed from {@link Object#toString()}. If the parsing fails,
	 * {@code null} is returned.
	 * <p>
	 * The {@link Number} implementations that might be returned are
	 * {@link Rational}, {@link BigInteger}, {@link Integer}, {@link Long} or
	 * {@link Double}.
	 *
	 * @param object the {@link Object} to convert to a {@link Number}.
	 * @param locale the {@link Locale} to use for decimal numbers. If
	 *            {@code null}, {@code "."} is used as a decimal separator.
	 * @param roundingScale the number of digits to keep after the
	 *            decimal-separator if positive or the number of digits to
	 *            discard before the decimal-separator. Ignored if
	 *            {@code roundingMode} is {@code null}.
	 * @param roundingMode The {@link RoundingMode} to use. Use with scale
	 *            {@code 0} to round to an integer.
	 * @param reduceType if {@code true} the return type is the "smallest"
	 *            possible, if {@code false} the type that is used internally is
	 *            returned. "Smallest" means that {@link Integer} is preferred
	 *            over {@link Long}, which is preferred over {@link BigInteger}.
	 *            For fractional values {@link Double} is preferred over
	 *            {@link Rational}.
	 * @return The {@link Number} or {@code null}.
	 */
	@Nullable
	public static Number parseNumber(
		@Nullable Object object,
		@Nullable Locale locale,
		int roundingScale,
		@Nullable RoundingMode roundingMode,
		boolean reduceType
	) {
		return parseNumber(object, locale, null, roundingScale, roundingMode, reduceType);
	}

	/**
	 * Attempts to convert an object into a {@link Number}. If the object is a
	 * {@link Number}, the object itself is returned. If the object is
	 * {@code null}, {@code null} is returned. Otherwise, a {@link Number} is
	 * attempted parsed from {@link Object#toString()}. If the parsing fails,
	 * {@code null} is returned.
	 * <p>
	 * The {@link Number} implementations that might be returned are
	 * {@link Rational}, {@link BigInteger}, {@link Integer}, {@link Long} or
	 * {@link Double}.
	 *
	 * @param object the {@link Object} to convert to a {@link Number}.
	 * @param locale the {@link Locale} to use for decimal numbers. If
	 *            {@code null}, {@code "."} is used as a decimal separator.
	 * @param mathContext the {@link MathContext} to use for rounding or
	 *            {@code null} for no rounding. This overrides {@code scale} and
	 *            {@code roundingMode} if non-{@code null}.
	 * @param roundingScale the number of digits to keep after the
	 *            decimal-separator if positive or the number of digits to
	 *            discard before the decimal-separator. Ignored if
	 *            {@code mathContext} is non-{@code null} or
	 *            {@code roundingMode} is {@code null}.
	 * @param roundingMode The {@link RoundingMode} to use. Use with scale
	 *            {@code 0} to round to an integer. Ignored if
	 *            {@code mathContext} is non-{@code null}.
	 * @param reduceType if {@code true} the return type is the "smallest"
	 *            possible, if {@code false} the type that is used internally is
	 *            returned. "Smallest" means that {@link Integer} is preferred
	 *            over {@link Long}, which is preferred over {@link BigInteger}.
	 *            For fractional values {@link Double} is preferred over
	 *            {@link Rational}.
	 * @return The {@link Number} or {@code null}.
	 */
	@Nullable
	protected static Number parseNumber(
		@Nullable Object object,
		@Nullable Locale locale,
		@Nullable MathContext mathContext,
		int roundingScale,
		@Nullable RoundingMode roundingMode,
		boolean reduceType
	) {
		if (object == null) {
			return null;
		}
		if (object instanceof Number) {
			if (mathContext == null && (roundingMode == null || roundingMode == RoundingMode.UNNECESSARY)) {
				// No rounding
				if (object instanceof Double || object instanceof Integer) {
					return (Number) object;
				}
				if (object instanceof Short || object instanceof Byte) {
					return Integer.valueOf(((Number) object).intValue());
				}
				if (
					!reduceType && (
						object instanceof Rational ||
						object instanceof BigInteger ||
						object instanceof Long
					)
				) {
					return (Number) object;
				}
			}
			Rational rational = Rational.valueOf((Number) object);
			if (mathContext != null || (roundingMode != null && roundingMode != RoundingMode.UNNECESSARY)) {
				// Rounding
				if (mathContext != null) {
					rational = Rational.valueOf(rational.bigDecimalValue(mathContext).stripTrailingZeros());
				} else if (roundingScale != 0 || !rational.isInteger()) {
					rational = Rational.valueOf(rational.bigDecimalValue(roundingScale, roundingMode).stripTrailingZeros());
				}
			}

			// Return type
			if (!reduceType) {
				return rational;
			}
			if (!rational.isInteger()) {
				// Fractional return type
				if (isDoubleValueExact(rational)) {
					// Return Double
					return Double.valueOf(rational.doubleValue());
				}
				// Return Rational
				return rational;
			}
			// Integer return type
			BigInteger bigInteger = rational.bigIntegerValue();

			if (
				bigInteger.compareTo(BigInteger.valueOf(Long.MIN_VALUE)) < 0 ||
				bigInteger.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0
			) {
				// Return BigInteger
				return bigInteger;
			}
			long l = bigInteger.longValue();
			if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
				// Return Long
				return Long.valueOf(l);
			}
			// Return Integer
			return Integer.valueOf((int) l);
		}
		String s = object.toString().trim();
		if (isBlank(s)) {
			return null;
		}
		if (locale != null) {
			char c = DecimalFormatSymbols.getInstance(locale).getDecimalSeparator();
			if (c != '.' && s.indexOf(c) >= 0) {
				s = s.replace(".", "").replace(c, '.');
			}
		}
		try {
			Matcher matcher = NUMBER.matcher(s);
			if (!matcher.find()) {
				LOGGER.trace("Failed to parse a number from \"{}\"", s);
				return null;
			}
			int radix;
			String significand;
			String exponent;
			if (matcher.group(1) != null) {
				// Decimal
				radix = 10;
				significand = matcher.group(1);
				exponent = matcher.group(2);
			} else {
				// Hexadecimal
				radix = 16;
				significand = matcher.group(3).replaceFirst("0[Xx]", "");
				exponent = matcher.group(4);
			}

			BigInteger bigInteger;
			int dot = significand.indexOf('.');
			if (dot >= 0 || exponent != null) {
				// Fractional or exponential form
				int scale;
				if (dot >= 0) {
					scale = significand.substring(dot + 1).length();
					significand = significand.replace(".", "");
				} else {
					scale = 0;
				}
				int power = 0;
				if (radix == 10 && exponent != null) {
					// Decimal
					scale = scale - Integer.parseInt(exponent);
				} else if (radix == 16 && exponent != null) {
					// Hexadecimal
					if (exponent.toLowerCase(Locale.ROOT).startsWith("p")) {
						power = Integer.parseInt(exponent.substring(1));
					} else {
						scale = scale - Integer.parseInt(exponent.substring(1), radix);
					}
				}
				Rational rational = Rational.valueOf(new BigInteger(significand, radix));
				if (scale != 0) {
					rational = rational.multiply(Rational.valueOf(radix).pow(-scale));
				}
				if (power != 0) {
					rational = rational.multiply(Rational.valueOf(2).pow(power));
				}

				// Rounding
				if (mathContext != null || (roundingMode != null && roundingMode != RoundingMode.UNNECESSARY)) {
					if (mathContext != null) {
						rational = Rational.valueOf(rational.bigDecimalValue(mathContext).stripTrailingZeros());
					} else if (roundingScale != 0 || !rational.isInteger()) {
						rational = Rational.valueOf(rational.bigDecimalValue(roundingScale, roundingMode).stripTrailingZeros());
					}
				}

				// Return type
				if (!rational.isInteger()) {
					// Fractional return type
					if (reduceType && isDoubleValueExact(rational)) {
						// Return Double
						return Double.valueOf(rational.doubleValue());
					}
					// Return Rational
					return rational;
				}
				// Integer value
				bigInteger = rational.bigIntegerValue();
			} else {
				// Integer form;
				bigInteger = new BigInteger(significand, radix);
			}

			// Integer return type
			if (
				!reduceType ||
				bigInteger.compareTo(BigInteger.valueOf(Long.MIN_VALUE)) < 0 ||
				bigInteger.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0
			) {
				// Return BigInteger
				return bigInteger;
			}
			long l = bigInteger.longValue();
			if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
				// Return Long
				return Long.valueOf(l);
			}
			// Return Integer
			return Integer.valueOf((int) l);
		} catch (NumberFormatException | ArithmeticException e) {
			if (isNotBlank(e.getMessage())) {
				LOGGER.trace("Failed to parse a number from \"{}\": {}", s, e.getMessage());
			} else {
				LOGGER.trace("Failed to parse a number from \"{}\"", s);
			}
			return null;
		}
	}

	/**
	 * Checks if {@link BigDecimal#doubleValue()} is an exact representation of
	 * the specified {@link BigDecimal}.
	 *
	 * @param value the {@link BigDecimal} to check.
	 * @return {@code true} if {@link BigDecimal#doubleValue()} is an exact
	 *         representation of {@code value}, {@code false} if the conversion
	 *         leads to a small inaccuracy.
	 */
	public static boolean isDoubleValueExact(@Nullable BigDecimal value) {
		if (value == null) {
			return false;
		}
		return value.compareTo(new BigDecimal(value.doubleValue())) == 0;
	}

	/**
	 * Checks if {@link Rational#doubleValue()} is an exact representation of
	 * the specified {@link Rational}.
	 *
	 * @param value the {@link Rational} to check.
	 * @return {@code true} if {@link Rational#doubleValue()} is an exact
	 *         representation of {@code value}, {@code false} if the conversion
	 *         leads to a small inaccuracy.
	 */
	public static boolean isDoubleValueExact(@Nullable Rational value) {
		if (value == null) {
			return false;
		}
		if (value.isInfinite() || value.isNaN()) {
			return true;
		}
		return value.compareTo(new BigDecimal(value.doubleValue())) == 0;
	}

	/**
	 * This {@code enum} represents the positive unit prefixes that is usable
	 * with a 64-bit integer (long).
	 */
	public static enum UnitPrefix {
		KIBI(1L << 10, "Ki"),
		MEBI(1L << 20, "Mi"),
		GIBI(1L << 30, "Gi"),
		TEBI(1L << 40, "Ti"),
		PEBI(1L << 50, "Pi"),
		EXBI(1L << 60, "Ei"),
		ZEBI(BigInteger.valueOf(1024).pow(7), "Zi"),
		YOBI(BigInteger.valueOf(1024).pow(8), "Yi"),
		ATTO(Rational.TEN.pow(-18), "a"),
		FEMTO(Rational.TEN.pow(-15), "f"),
		PICO(Rational.TEN.pow(-12), "p"),
		NANO(Rational.TEN.pow(-9), "n"),
		MICRO(Rational.TEN.pow(-6), "μ"),
		MILLI(Rational.TEN.pow(-3), "m"),
		CENTI(Rational.TEN.pow(-2), "c"),
		DECI(Rational.TEN.pow(-1), "d"),
		DECA(10L, "da"),
		HECTO(100L, "h"),
		KILO(1000L, "k"),
		MEGA(1000000L, "M"),
		GIGA(1000000000L, "G"),
		TERA(1000000000000L, "T"),
		PETA(1000000000000000L, "P"),
		EXA(1000000000000000000L, "E");

		private final long factorL;
		private final BigInteger factorBI;
		private final Rational factorR;
		private final boolean asLongValid;
		private final boolean asBigIntegerValid;
		private final boolean fractional;

		@Nonnull
		private final String symbol;

		private UnitPrefix(long factor, @Nonnull String symbol) {
			this.factorL = factor;
			this.asLongValid = true;
			this.factorBI = BigInteger.valueOf(factor);
			this.asBigIntegerValid = true;
			this.factorR = Rational.valueOf(factor);
			this.symbol = symbol;
			this.fractional = false;
		}

		private UnitPrefix(BigInteger factor, @Nonnull String symbol) {
			this.factorBI = factor;
			this.asBigIntegerValid = true;
			if (
				factor.compareTo(BigInteger.valueOf(Long.MIN_VALUE)) < 0 ||
				factor.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0
			) {
				this.factorL = -1;
				this.asLongValid = false;
			} else {
				this.factorL = factor.longValue();
				this.asLongValid = true;
			}
			this.factorR = Rational.valueOf(factor);
			this.symbol = symbol;
			this.fractional = false;
		}

		private UnitPrefix(Rational factor, @Nonnull String symbol) {
			this.factorR = factor;
			if (factor.isInteger()) {
				if (
					factor.compareTo(BigInteger.valueOf(Long.MIN_VALUE)) < 0 ||
					factor.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0
				) {
					this.factorL = -1;
					this.asLongValid = false;
				} else {
					this.factorL = factor.longValue();
					this.asLongValid = true;
				}
				this.factorBI = factor.bigIntegerValue();
				this.asBigIntegerValid = true;
			} else {
				this.factorL = -1;
				this.asLongValid = false;
				this.factorBI = null;
				this.asBigIntegerValid = false;
			}
			this.fractional = factor.compareTo(Rational.ONE) < 0;
			this.symbol = symbol;
		}

		/**
		 * @return {@code true} if the factor can be expressed as a {@code long},
		 *         {@code false} otherwise.
		 */
		public boolean isAsLongValid() {
			return asLongValid;
		}

		/**
		 * @return The {@code long} factor.
		 */
		public long getFactorLong() {
			return factorL;
		}

		/**
		 * @return {@code true} if the factor can be expressed as a
		 *         {@link BigInteger}, {@code false} otherwise.
		 */
		public boolean isAsBigIntegerValid() {
			return asBigIntegerValid;
		}

		/**
		 * @return The {@link BigInteger} factor.
		 */
		public BigInteger getFactorBigInteger() {
			return factorBI;
		}

		/**
		 * @return The {@link Rational} factor.
		 */
		public Rational getFactorRational() {
			return factorR;
		}

		/**
		 * @return {@code true} if the factor is less than {@code 1},
		 *         {@code false} otherwise.
		 */
		public boolean isFractional() {
			return fractional;
		}

		/**
		 * @return The symbol.
		 */
		@Nonnull
		public String getSymbol() {
			return symbol;
		}
	}
}
