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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Locale;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import org.apache.commons.configuration.ConfigurationException;
import static net.pms.util.ConversionUtil.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNull;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;


public class ConversionUtilTest {

	@Before
	public void setUp() throws ConfigurationException {
		// Silence all log messages from the DMS code that is being tested
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		context.getLogger(Logger.ROOT_LOGGER_NAME).setLevel(Level.OFF);
	}

	@Test
	public void parseNumberWithUnitTest() {
		// Invalid
		assertNull(parseNumberWithUnit(null, null, null, 0, null, null, null, null, false, false, false));
		assertNull(parseNumberWithUnit("", null, null, 0, null, null, null, null, false, false, false));
		assertNull(parseNumberWithUnit(" \t ", null, null, 0, null, null, null, null, false, false, false));
		assertNull(parseNumberWithUnit("NotANumber", null, null, 0, null, null, null, null, false, false, false));
		assertNull(parseNumberWithUnit("Not a number", null, null, 0, null, null, null, null, false, false, false));
		assertNull(parseNumberWithUnit("KeineNummer", Locale.GERMAN, null, 0, null, null, null, null, false, false, false));
		assertNull(parseNumberWithUnit("Keine Nummer", Locale.GERMAN, null, 0, null, null, null, null, false, false, false));
		assertNull(parseNumberWithUnit("Keine,Nummer", Locale.GERMAN, null, 0, null, null, null, null, false, false, false));
		assertNull(parseNumberWithUnit("Keine.Nummer", Locale.GERMAN, null, 0, null, null, null, null, false, false, false));

		// Without unit
		assertEquals(new Pair<Number, String>(Integer.valueOf(4), null), parseNumberWithUnit("4", null, null, 0, null, null, null, null, false, false, false));

		// Percent
		assertNull(parseNumberWithUnit("46%", null, null, 0, null, null, null, null, false, false, false));
		assertEquals(new Pair<Number, String>(Integer.valueOf(46), "%"), parseNumberWithUnit("46%", null, null, 0, null, null, null, null, true, false, false));
		assertEquals(new Pair<Number, String>(Rational.valueOf(93, 10), "%"), parseNumberWithUnit("9,3%", Locale.GERMAN, null, 0, null, null, null, null, true, false, false));
		assertEquals(new Pair<Number, String>(Double.valueOf(130.125), "%"), parseNumberWithUnit("130,125%", Locale.GERMAN, null, 0, null, null, null, null, true, false, false));
		assertEquals(new Pair<Number, String>(Integer.valueOf(0x2f), "%"), parseNumberWithUnit("0x2f%", Locale.GERMAN, null, 0, null, null, null, null, true, false, false));
		assertEquals(new Pair<Number, String>(Double.valueOf(47.88671875), "%"), parseNumberWithUnit("0x2f,e3%", Locale.GERMAN, null, 0, null, null, null, null, true, false, false));


		assertEquals(new Pair<Number, String>(Integer.valueOf(400), null), parseNumberWithUnit("4h", null, null, 0, null, null, null, null, false, false, false));
		assertEquals(new Pair<Number, String>(Integer.valueOf(4096), "h"), parseNumberWithUnit("4Kih", null, null, 0, null, null, null, null, false, false, false));
		assertEquals(new Pair<Number, String>(Long.valueOf(4000000000000L), "b"), parseNumberWithUnit("4 tb  ", null, null, 0, null, null, null, null, false, false, false));
		assertEquals(new Pair<Number, String>(Long.valueOf(4398046511104L), "B"), parseNumberWithUnit("  4tiB ", null, null, 0, null, null, null, null, false, false, false));
		assertEquals(new Pair<Number, String>(Integer.valueOf(-3960), "B"), parseNumberWithUnit(" -3.96kB ", null, null, 0, null, null, null, null, false, false, false));
		assertEquals(new Pair<Number, String>(Rational.valueOf(147, 20), "nB"), parseNumberWithUnit(" 7.35nB ", null, null, 0, null, null, null, null, false, false, false));
		assertEquals(new Pair<Number, String>(Rational.valueOf(147L, 20000000000L), "B"), parseNumberWithUnit(" 7.35nB ", null, null, 0, null, null, null, null, false, false, true));
		assertEquals(new Pair<Number, String>(Integer.valueOf(7350000), "B"), parseNumberWithUnit(" 7.35MB ", null, null, 0, null, null, null, null, false, false, true));
		assertEquals(new Pair<Number, String>(Rational.valueOf(147, 20000), "B"), parseNumberWithUnit(" 7.35mB ", null, null, 0, null, null, null, null, false, false, true));
		assertEquals(new Pair<Number, String>(Rational.valueOf(38535168, 5), "B"), parseNumberWithUnit(" 7.35MiB ", null, null, 0, null, null, null, null, false, false, false));
		assertEquals(new Pair<Number, String>(Integer.valueOf(7707034), "B"), parseNumberWithUnit(" 7.35MiB ", null, null, 0, RoundingMode.HALF_EVEN, null, null, null, false, false, false));
		assertEquals(new Pair<Number, String>(Integer.valueOf(7707034), "B"), parseNumberWithUnit(" 7.35Mi ", null, null, 0, RoundingMode.HALF_EVEN, null, null, "B", false, false, false));

		// Required Unit
		assertEquals(new Pair<Number, String>(Integer.valueOf(7526), "B"), parseNumberWithUnit(" 7.35kiB ", null, null, 0, RoundingMode.HALF_EVEN, "B", null, null, false, false, false));
		assertEquals(new Pair<Number, String>(Integer.valueOf(7526), "b"), parseNumberWithUnit(" 7.35kib ", null, null, 0, RoundingMode.HALF_EVEN, "B", null, null, false, false, false));
		assertNull(parseNumberWithUnit(" 7.35ki ", null, null, 0, RoundingMode.HALF_EVEN, "B", null, null, false, false, false));
		assertEquals(new Pair<Number, String>(Integer.valueOf(700), "m"), parseNumberWithUnit("700m", null, null, 0, RoundingMode.HALF_EVEN, "m", null, null, false, false, false));
		assertEquals(new Pair<Number, String>(Integer.valueOf(700), "m"), parseNumberWithUnit(" 700 m ", null, null, 0, RoundingMode.HALF_EVEN, "M", null, null, false, false, false));

		// Default Unit
		assertEquals(new Pair<Number, String>(Integer.valueOf(7350), "b"), parseNumberWithUnit(" 7.35k ", null, null, 0, null, "B", null, "b", false, false, false));
		assertEquals(new Pair<Number, String>(Integer.valueOf(7350), "b"), parseNumberWithUnit(" 7.35k ", null, null, 0, null, "b", null, "b", false, false, false));
		assertEquals(new Pair<Number, String>(Integer.valueOf(7526), "b"), parseNumberWithUnit(" 0x234 ", null, null, 0, null, "b", null, "b", false, false, false));
	}

	@Test
	public void parseNumberTest() {
		// Invalid
		assertNull(parseNumber(null, null, 0, null, false));
		assertNull(parseNumber(LoggerFactory.getILoggerFactory(), null, 0, null, false));
		assertNull(parseNumber(new StringBuilder("foobar"), null, 0, null, false));
		assertNull(parseNumber("foo 2 bar", null, 0, null, false));
		assertNull(parseNumber(" \t", null, 0, null, false));
		assertNull(parseNumber(" ", null, 0, null, false));
		assertNull(parseNumber("", null, 0, null, false));

		// Number
		assertEquals(Integer.valueOf(Byte.MAX_VALUE), parseNumber(Integer.valueOf(Byte.MAX_VALUE), null, null, 0, null, false));
		assertEquals(Long.valueOf(Byte.MAX_VALUE), parseNumber(Long.valueOf(Byte.MAX_VALUE), null, null, 0, null, false));
		assertEquals(Integer.valueOf(Byte.MAX_VALUE), parseNumber(Long.valueOf(Byte.MAX_VALUE), null, null, 0, null, true));
		assertEquals(Integer.valueOf(Byte.MAX_VALUE), parseNumber(Byte.valueOf(Byte.MAX_VALUE), null, null, 0, null, false));
		assertEquals(Integer.valueOf(Byte.MAX_VALUE), parseNumber(Byte.valueOf(Byte.MAX_VALUE), null, null, 0, null, true));
		assertEquals(Integer.valueOf(Byte.MIN_VALUE), parseNumber(Integer.valueOf(Byte.MIN_VALUE), null, null, 0, null, false));
		assertEquals(Long.valueOf(Byte.MIN_VALUE), parseNumber(Long.valueOf(Byte.MIN_VALUE), null, null, 0, null, false));
		assertEquals(Integer.valueOf(Byte.MIN_VALUE), parseNumber(Long.valueOf(Byte.MIN_VALUE), null, null, 0, null, true));
		assertEquals(Integer.valueOf(Byte.MIN_VALUE), parseNumber(Byte.valueOf(Byte.MIN_VALUE), null, null, 0, null, false));
		assertEquals(Integer.valueOf(Byte.MIN_VALUE), parseNumber(Byte.valueOf(Byte.MIN_VALUE), null, null, 0, null, true));

		assertEquals(Integer.valueOf(Short.MAX_VALUE), parseNumber(Integer.valueOf(Short.MAX_VALUE), null, null, 0, null, false));
		assertEquals(Long.valueOf(Short.MAX_VALUE), parseNumber(Long.valueOf(Short.MAX_VALUE), null, null, 0, null, false));
		assertEquals(Integer.valueOf(Short.MAX_VALUE), parseNumber(Long.valueOf(Short.MAX_VALUE), null, null, 0, null, true));
		assertEquals(Integer.valueOf(Short.MAX_VALUE), parseNumber(Short.valueOf(Short.MAX_VALUE), null, null, 0, null, false));
		assertEquals(Integer.valueOf(Short.MAX_VALUE), parseNumber(Short.valueOf(Short.MAX_VALUE), null, null, 0, null, true));
		assertEquals(Integer.valueOf(Short.MIN_VALUE), parseNumber(Integer.valueOf(Short.MIN_VALUE), null, null, 0, null, false));
		assertEquals(Long.valueOf(Short.MIN_VALUE), parseNumber(Long.valueOf(Short.MIN_VALUE), null, null, 0, null, false));
		assertEquals(Integer.valueOf(Short.MIN_VALUE), parseNumber(Long.valueOf(Short.MIN_VALUE), null, null, 0, null, true));
		assertEquals(Integer.valueOf(Short.MIN_VALUE), parseNumber(Short.valueOf(Short.MIN_VALUE), null, null, 0, null, false));
		assertEquals(Integer.valueOf(Short.MIN_VALUE), parseNumber(Short.valueOf(Short.MIN_VALUE), null, null, 0, null, true));

		assertEquals(Integer.valueOf(Integer.MAX_VALUE), parseNumber(Integer.valueOf(Integer.MAX_VALUE), null, null, 0, null, false));
		assertEquals(Long.valueOf(Integer.MAX_VALUE), parseNumber(Long.valueOf(Integer.MAX_VALUE), null, null, 0, null, false));
		assertEquals(Integer.valueOf(Integer.MAX_VALUE), parseNumber(Long.valueOf(Integer.MAX_VALUE), null, null, 0, null, true));
		assertEquals(Integer.valueOf(Integer.MIN_VALUE), parseNumber(Integer.valueOf(Integer.MIN_VALUE), null, null, 0, null, false));
		assertEquals(Long.valueOf(Integer.MIN_VALUE), parseNumber(Long.valueOf(Integer.MIN_VALUE), null, null, 0, null, false));
		assertEquals(Integer.valueOf(Integer.MIN_VALUE), parseNumber(Long.valueOf(Integer.MIN_VALUE), null, null, 0, null, true));

		assertEquals(Long.valueOf(Long.MAX_VALUE), parseNumber(Long.valueOf(Long.MAX_VALUE), null, null, 0, null, false));
		assertEquals(BigInteger.valueOf(Long.MAX_VALUE), parseNumber(BigInteger.valueOf(Long.MAX_VALUE), null, null, 0, null, false));
		assertEquals(Long.valueOf(Long.MAX_VALUE), parseNumber(BigInteger.valueOf(Long.MAX_VALUE), null, null, 0, null, true));
		assertEquals(Long.valueOf(Long.MIN_VALUE), parseNumber(Long.valueOf(Long.MIN_VALUE), null, null, 0, null, false));
		assertEquals(BigInteger.valueOf(Long.MIN_VALUE), parseNumber(BigInteger.valueOf(Long.MIN_VALUE), null, null, 0, null, false));
		assertEquals(Long.valueOf(Long.MIN_VALUE), parseNumber(BigInteger.valueOf(Long.MIN_VALUE), null, null, 0, null, true));

		assertEquals(Rational.valueOf(Integer.MAX_VALUE), parseNumber(Long.valueOf(Integer.MAX_VALUE), null, null, 0, RoundingMode.HALF_UP, false));
		assertEquals(Integer.valueOf(Integer.MAX_VALUE), parseNumber(Long.valueOf(Integer.MAX_VALUE), null, null, 0, RoundingMode.HALF_UP, true));
		assertEquals(Rational.valueOf(Integer.MAX_VALUE), parseNumber(Long.valueOf(Integer.MAX_VALUE), null, MathContext.DECIMAL64, 0, null, false));
		assertEquals(Integer.valueOf(Integer.MAX_VALUE), parseNumber(Long.valueOf(Integer.MAX_VALUE), null, MathContext.DECIMAL64, 0, null, true));
		assertEquals(Rational.valueOf(Integer.MIN_VALUE), parseNumber(Long.valueOf(Integer.MIN_VALUE), null, null, 0, RoundingMode.HALF_UP, false));
		assertEquals(Integer.valueOf(Integer.MIN_VALUE), parseNumber(Long.valueOf(Integer.MIN_VALUE), null, null, 0, RoundingMode.HALF_UP, true));
		assertEquals(Rational.valueOf(Integer.MIN_VALUE), parseNumber(Long.valueOf(Integer.MIN_VALUE), null, MathContext.DECIMAL64, 0, null, false));
		assertEquals(Integer.valueOf(Integer.MIN_VALUE), parseNumber(Long.valueOf(Integer.MIN_VALUE), null, MathContext.DECIMAL64, 0, null, true));

		assertEquals(Rational.valueOf(Long.MAX_VALUE), parseNumber(Long.valueOf(Long.MAX_VALUE), null, null, 0, RoundingMode.CEILING, false));
		assertEquals(Long.valueOf(Long.MAX_VALUE), parseNumber(Long.valueOf(Long.MAX_VALUE), null, null, 0, RoundingMode.CEILING, true));
		assertEquals(Rational.valueOf(Long.MAX_VALUE), parseNumber(Long.valueOf(Long.MAX_VALUE), null, MathContext.DECIMAL128, 0, null, false));
		assertEquals(Long.valueOf(Long.MAX_VALUE), parseNumber(Long.valueOf(Long.MAX_VALUE), null, MathContext.DECIMAL128, 0, null, true));
		assertEquals(Rational.valueOf(Long.MIN_VALUE), parseNumber(Long.valueOf(Long.MIN_VALUE), null, null, 0, RoundingMode.CEILING, false));
		assertEquals(Long.valueOf(Long.MIN_VALUE), parseNumber(Long.valueOf(Long.MIN_VALUE), null, null, 0, RoundingMode.CEILING, true));
		assertEquals(Rational.valueOf(Long.MIN_VALUE), parseNumber(Long.valueOf(Long.MIN_VALUE), null, MathContext.DECIMAL128, 0, null, false));
		assertEquals(Long.valueOf(Long.MIN_VALUE), parseNumber(Long.valueOf(Long.MIN_VALUE), null, MathContext.DECIMAL128, 0, null, true));

		BigInteger bigInteger = BigInteger.valueOf(Long.MAX_VALUE).multiply(BigInteger.TEN);
		assertEquals(Rational.valueOf(bigInteger), parseNumber(bigInteger, null, null, 0, RoundingMode.CEILING, false));
		assertEquals(bigInteger, parseNumber(bigInteger, null, null, 0, RoundingMode.CEILING, true));
		assertEquals(Rational.valueOf(bigInteger), parseNumber(bigInteger, null, MathContext.DECIMAL128, 0, null, false));
		assertEquals(bigInteger, parseNumber(bigInteger, null, MathContext.DECIMAL128, 0, null, true));
		bigInteger = BigInteger.valueOf(Long.MIN_VALUE).multiply(BigInteger.TEN);
		assertEquals(Rational.valueOf(bigInteger), parseNumber(bigInteger, null, null, 0, RoundingMode.CEILING, false));
		assertEquals(bigInteger, parseNumber(bigInteger, null, null, 0, RoundingMode.CEILING, true));
		assertEquals(Rational.valueOf(bigInteger), parseNumber(bigInteger, null, MathContext.DECIMAL128, 0, null, false));
		assertEquals(bigInteger, parseNumber(bigInteger, null, MathContext.DECIMAL128, 0, null, true));

		assertEquals(Rational.valueOf(Double.MAX_VALUE), parseNumber(Double.valueOf(Double.MAX_VALUE), null, null, 0, RoundingMode.CEILING, false));
		assertEquals(BigDecimal.valueOf(Double.MAX_VALUE).toBigIntegerExact(), parseNumber(Double.valueOf(Double.MAX_VALUE), null, null, 0, RoundingMode.CEILING, true));
		assertEquals(Rational.valueOf(Double.MAX_VALUE), parseNumber(Double.valueOf(Double.MAX_VALUE), null, MathContext.DECIMAL128, 0, null, false));
		assertEquals(BigDecimal.valueOf(Double.MAX_VALUE).toBigIntegerExact(), parseNumber(Double.valueOf(Double.MAX_VALUE), null, MathContext.DECIMAL128, 0, null, true));
		assertEquals(Rational.ONE, parseNumber(Double.valueOf(Double.MIN_VALUE), null, null, 0, RoundingMode.CEILING, false));
		assertEquals(Integer.valueOf(1), parseNumber(Double.valueOf(Double.MIN_VALUE), null, null, 0, RoundingMode.CEILING, true));
		assertEquals(Integer.valueOf(0), parseNumber(Double.valueOf(Double.MIN_VALUE), null, null, 0, RoundingMode.DOWN, true));
		assertEquals(Integer.valueOf(0), parseNumber(Double.valueOf(Double.MIN_VALUE), null, null, 0, RoundingMode.FLOOR, true));
		assertEquals(Integer.valueOf(0), parseNumber(Double.valueOf(Double.MIN_VALUE), null, null, 0, RoundingMode.HALF_DOWN, true));
		assertEquals(Integer.valueOf(0), parseNumber(Double.valueOf(Double.MIN_VALUE), null, null, 0, RoundingMode.HALF_EVEN, true));
		assertEquals(Integer.valueOf(0), parseNumber(Double.valueOf(Double.MIN_VALUE), null, null, 0, RoundingMode.HALF_UP, true));
		assertEquals(Integer.valueOf(1), parseNumber(Double.valueOf(Double.MIN_VALUE), null, null, 0, RoundingMode.UP, true));
		assertEquals(Rational.valueOf(Double.MIN_VALUE), parseNumber(Double.valueOf(Double.MIN_VALUE), null, MathContext.DECIMAL128, 0, null, false));
		assertEquals(Rational.valueOf(Double.MIN_VALUE), parseNumber(Double.valueOf(Double.MIN_VALUE), null, MathContext.DECIMAL128, 0, null, true));

		BigDecimal bigDecimal = BigDecimal.valueOf(Long.MAX_VALUE, -10);
		assertEquals(Rational.valueOf(bigDecimal), parseNumber(bigDecimal, null, null, 0, RoundingMode.CEILING, false));
		assertEquals(bigDecimal.toBigIntegerExact(), parseNumber(bigDecimal, null, null, 0, RoundingMode.CEILING, true));
		assertEquals(Rational.valueOf(bigDecimal), parseNumber(bigDecimal, null, MathContext.DECIMAL128, 0, null, false));
		assertEquals(bigDecimal.toBigIntegerExact(), parseNumber(bigDecimal, null, MathContext.DECIMAL128, 0, null, true));
		bigDecimal = BigDecimal.valueOf(Long.MIN_VALUE, -10);
		assertEquals(Rational.valueOf(bigDecimal), parseNumber(bigDecimal, null, null, 0, RoundingMode.CEILING, false));
		assertEquals(bigDecimal.toBigIntegerExact(), parseNumber(bigDecimal, null, null, 0, RoundingMode.CEILING, true));
		assertEquals(Rational.valueOf(bigDecimal), parseNumber(bigDecimal, null, MathContext.DECIMAL128, 0, null, false));
		assertEquals(bigDecimal.toBigIntegerExact(), parseNumber(bigDecimal, null, MathContext.DECIMAL128, 0, null, true));

		bigInteger = BigDecimal.valueOf(Long.MAX_VALUE, -10).toBigIntegerExact();
		Rational rational = Rational.valueOf(BigDecimal.valueOf(Long.MAX_VALUE, -10));
		assertEquals(rational, parseNumber(rational, null, null, 0, RoundingMode.CEILING, false));
		assertEquals(bigInteger, parseNumber(rational, null, null, 0, RoundingMode.CEILING, true));
		assertEquals(rational, parseNumber(rational, null, MathContext.DECIMAL128, 0, null, false));
		assertEquals(bigInteger, parseNumber(rational, null, MathContext.DECIMAL128, 0, null, true));
		bigInteger = BigDecimal.valueOf(Long.MIN_VALUE, -10).toBigIntegerExact();
		rational = Rational.valueOf(BigDecimal.valueOf(Long.MIN_VALUE, -10));
		assertEquals(rational, parseNumber(rational, null, null, 0, RoundingMode.CEILING, false));
		assertEquals(bigInteger, parseNumber(rational, null, null, 0, RoundingMode.CEILING, true));
		assertEquals(rational, parseNumber(rational, null, MathContext.DECIMAL128, 0, null, false));
		assertEquals(bigInteger, parseNumber(rational, null, MathContext.DECIMAL128, 0, null, true));

		rational = Rational.valueOf(Long.MAX_VALUE);
		assertEquals(rational, parseNumber(rational, null, null, 0, RoundingMode.CEILING, false));
		assertEquals(Long.valueOf(Long.MAX_VALUE), parseNumber(rational, null, null, 0, RoundingMode.CEILING, true));
		assertEquals(rational, parseNumber(rational, null, MathContext.DECIMAL128, 0, null, false));
		assertEquals(Long.valueOf(Long.MAX_VALUE), parseNumber(rational, null, MathContext.DECIMAL128, 0, null, true));
		rational = Rational.valueOf(Long.MIN_VALUE);
		assertEquals(rational, parseNumber(rational, null, null, 0, RoundingMode.CEILING, false));
		assertEquals(Long.valueOf(Long.MIN_VALUE), parseNumber(rational, null, null, 0, RoundingMode.CEILING, true));
		assertEquals(rational, parseNumber(rational, null, MathContext.DECIMAL128, 0, null, false));
		assertEquals(Long.valueOf(Long.MIN_VALUE), parseNumber(rational, null, MathContext.DECIMAL128, 0, null, true));

		rational = Rational.valueOf(Integer.MAX_VALUE);
		assertEquals(rational, parseNumber(rational, null, null, 0, RoundingMode.CEILING, false));
		assertEquals(Integer.valueOf(Integer.MAX_VALUE), parseNumber(rational, null, null, 0, RoundingMode.CEILING, true));
		assertEquals(rational, parseNumber(rational, null, MathContext.DECIMAL128, 0, null, false));
		assertEquals(Integer.valueOf(Integer.MAX_VALUE), parseNumber(rational, null, MathContext.DECIMAL128, 0, null, true));
		rational = Rational.valueOf(Integer.MIN_VALUE);
		assertEquals(rational, parseNumber(rational, null, null, 0, RoundingMode.CEILING, false));
		assertEquals(Integer.valueOf(Integer.MIN_VALUE), parseNumber(rational, null, null, 0, RoundingMode.CEILING, true));
		assertEquals(rational, parseNumber(rational, null, MathContext.DECIMAL128, 0, null, false));
		assertEquals(Integer.valueOf(Integer.MIN_VALUE), parseNumber(rational, null, MathContext.DECIMAL128, 0, null, true));

		rational = Rational.valueOf(Short.MAX_VALUE);
		assertEquals(rational, parseNumber(rational, null, null, 0, RoundingMode.CEILING, false));
		assertEquals(Integer.valueOf(Short.MAX_VALUE), parseNumber(rational, null, null, 0, RoundingMode.CEILING, true));
		assertEquals(rational, parseNumber(rational, null, MathContext.DECIMAL128, 0, null, false));
		assertEquals(Integer.valueOf(Short.MAX_VALUE), parseNumber(rational, null, MathContext.DECIMAL128, 0, null, true));
		rational = Rational.valueOf(Short.MIN_VALUE);
		assertEquals(rational, parseNumber(rational, null, null, 0, RoundingMode.CEILING, false));
		assertEquals(Integer.valueOf(Short.MIN_VALUE), parseNumber(rational, null, null, 0, RoundingMode.CEILING, true));
		assertEquals(rational, parseNumber(rational, null, MathContext.DECIMAL128, 0, null, false));
		assertEquals(Integer.valueOf(Short.MIN_VALUE), parseNumber(rational, null, MathContext.DECIMAL128, 0, null, true));

		rational = Rational.valueOf(Byte.MAX_VALUE);
		assertEquals(rational, parseNumber(rational, null, null, 0, RoundingMode.CEILING, false));
		assertEquals(Integer.valueOf(Byte.MAX_VALUE), parseNumber(rational, null, null, 0, RoundingMode.CEILING, true));
		assertEquals(rational, parseNumber(rational, null, MathContext.DECIMAL128, 0, null, false));
		assertEquals(Integer.valueOf(Byte.MAX_VALUE), parseNumber(rational, null, MathContext.DECIMAL128, 0, null, true));
		rational = Rational.valueOf(Byte.MIN_VALUE);
		assertEquals(rational, parseNumber(rational, null, null, 0, RoundingMode.CEILING, false));
		assertEquals(Integer.valueOf(Byte.MIN_VALUE), parseNumber(rational, null, null, 0, RoundingMode.CEILING, true));
		assertEquals(rational, parseNumber(rational, null, MathContext.DECIMAL128, 0, null, false));
		assertEquals(Integer.valueOf(Byte.MIN_VALUE), parseNumber(rational, null, MathContext.DECIMAL128, 0, null, true));

		rational = Rational.valueOf(3, 4);
		assertEquals(rational, parseNumber(rational, null, null, 0, null, false));
		assertEquals(Double.valueOf(0.75), parseNumber(rational, null, null, 0, null, true));
		assertEquals(Rational.valueOf(4, 5), parseNumber(rational, null, null, 1, RoundingMode.HALF_EVEN, false));
		assertEquals(Rational.valueOf(4, 5), parseNumber(rational, null, null, 1, RoundingMode.HALF_EVEN, true));

		// String
		assertEquals(BigInteger.ZERO, parseNumber("0", null, null, 0, null, false));
		assertEquals(Integer.valueOf(0), parseNumber("0", null, null, 0, null, true));
		assertEquals(BigInteger.ONE, parseNumber("1", null, null, 0, null, false));
		assertEquals(Integer.valueOf(1), parseNumber("1", null, null, 0, null, true));
		assertEquals(BigInteger.ONE.negate(), parseNumber("-1", null, null, 0, null, false));
		assertEquals(Integer.valueOf(-1), parseNumber("-1", null, null, 0, null, true));

		assertEquals(BigInteger.ZERO, parseNumber("0.00 E14", null, null, 0, null, false));
		assertEquals(Integer.valueOf(0), parseNumber("0.00 E14", null, null, 0, null, true));
		assertEquals(BigInteger.ONE, parseNumber("0.01 E2", null, null, 0, null, false));
		assertEquals(Integer.valueOf(1), parseNumber("0.01 E2", null, null, 0, null, true));
		assertEquals(BigInteger.ONE.negate(), parseNumber("-0.01 E2", null, null, 0, null, false));
		assertEquals(Integer.valueOf(-1), parseNumber("-0.01 E2", null, null, 0, null, true));

		assertEquals(BigInteger.ZERO, parseNumber("0x0.00 E-14", null, null, 0, null, false));
		assertEquals(Integer.valueOf(0), parseNumber("0x0.00 E-14", null, null, 0, null, true));
		assertEquals(BigInteger.ZERO, parseNumber("0x0.00 P-14", null, null, 0, null, false));
		assertEquals(Integer.valueOf(0), parseNumber("0x0.00 P-14", null, null, 0, null, true));
		assertEquals(BigInteger.ONE, parseNumber("0x0.01 E2", null, null, 0, null, false));
		assertEquals(Integer.valueOf(1), parseNumber("0x0.01 E2", null, null, 0, null, true));
		assertEquals(BigInteger.ONE, parseNumber("0x10 E-1", null, null, 0, null, false));
		assertEquals(Integer.valueOf(1), parseNumber("0x10 E-1", null, null, 0, null, true));
		assertEquals(BigInteger.ONE, parseNumber("0x10 P-4", null, null, 0, null, false));
		assertEquals(Integer.valueOf(1), parseNumber("0x10 P-4", null, null, 0, null, true));
		assertEquals(BigInteger.ONE.negate(), parseNumber("-0x0.01 E2", null, null, 0, null, false));
		assertEquals(Integer.valueOf(-1), parseNumber("-0x0.01 E2", null, null, 0, null, true));
		assertEquals(BigInteger.ONE.negate(), parseNumber("-0x10 E-1", null, null, 0, null, false));
		assertEquals(Integer.valueOf(-1), parseNumber("-0x10 E-1", null, null, 0, null, true));
		assertEquals(BigInteger.ONE.negate(), parseNumber("-0x10 P-4", null, null, 0, null, false));
		assertEquals(Integer.valueOf(-1), parseNumber("-0x10 P-4", null, null, 0, null, true));

		assertEquals(BigInteger.TEN, parseNumber("0x0.0a E2", null, null, 0, null, false));
		assertEquals(Integer.valueOf(10), parseNumber("0x0.0a E2", null, null, 0, null, true));
		assertEquals(BigInteger.TEN, parseNumber("0xa0 E-1", null, null, 0, null, false));
		assertEquals(Integer.valueOf(10), parseNumber("0xa0 E-1", null, null, 0, null, true));
		assertEquals(BigInteger.TEN, parseNumber("0xa0 P-4", null, null, 0, null, false));
		assertEquals(Integer.valueOf(10), parseNumber("0xa0 P-4", null, null, 0, null, true));
		assertEquals(BigInteger.TEN.negate(), parseNumber("-0x0.0a E2", null, null, 0, null, false));
		assertEquals(Integer.valueOf(-10), parseNumber("-0x0.0a E2", null, null, 0, null, true));
		assertEquals(BigInteger.TEN.negate(), parseNumber("-0x0,0a E2", Locale.GERMAN, null, 0, null, false));
		assertEquals(Integer.valueOf(-10), parseNumber("-0x0,0a E2", Locale.GERMAN, null, 0, null, true));
		assertEquals(BigInteger.TEN.negate(), parseNumber("-0xa0 E-1", null, null, 0, null, false));
		assertEquals(Integer.valueOf(-10), parseNumber("-0xa0 E-1", null, null, 0, null, true));
		assertEquals(BigInteger.TEN.negate(), parseNumber("-0xa0p-4", null, null, 0, null, false));
		assertEquals(Integer.valueOf(-10), parseNumber("-0xa0p-4", null, null, 0, null, true));

		assertEquals(Rational.valueOf(34.45), parseNumber("  34.45", null, null, 0, null, true));
		assertEquals(Double.valueOf(344.5), parseNumber("34.45 E1  ", null, null, 0, null, true));
		assertEquals(Integer.valueOf(3445), parseNumber("34.45E2", null, null, 0, null, true));
		assertEquals(BigInteger.valueOf(3445), parseNumber("34.45 E+2", null, null, 0, null, false));
		assertNull(parseNumber("  34.45  E+2 ", null, null, 0, null, false));

		assertEquals(Rational.valueOf(34.45), parseNumber("  34.45", Locale.GERMAN, null, 0, null, true));
		assertEquals(Double.valueOf(344.5), parseNumber("34.45 E1  ", Locale.GERMAN, null, 0, null, true));
		assertEquals(Integer.valueOf(3445), parseNumber("34.45E2", Locale.GERMAN, null, 0, null, true));
		assertEquals(BigInteger.valueOf(3445), parseNumber("34.45 E+2", Locale.GERMAN, null, 0, null, false));
		assertNull(parseNumber("  34.45  E+2 ", Locale.GERMAN, null, 0, null, false));

		assertEquals(Rational.valueOf(34.45), parseNumber("  34,45", Locale.GERMAN, null, 0, null, true));
		assertEquals(Double.valueOf(344.5), parseNumber("34,45 E1  ", Locale.GERMAN, null, 0, null, true));
		assertEquals(Integer.valueOf(3445), parseNumber("34,45E2", Locale.GERMAN, null, 0, null, true));
		assertEquals(BigInteger.valueOf(3445), parseNumber("34,45 E+2", Locale.GERMAN, null, 0, null, false));
		assertNull(parseNumber("  34,45  E+2 ", Locale.GERMAN, null, 0, null, false));

		assertEquals(Rational.valueOf(49, 4), parseNumber("  12.25", null, null, 0, null, false));
		assertEquals(Double.valueOf(12.25), parseNumber(" 12.25 ", null, null, 0, null, true));
		assertNull(parseNumber(" 12 .25 ", null, null, 0, null, true));
		assertNull(parseNumber(" 12. 25 ", null, null, 0, null, true));

		assertEquals(Rational.valueOf("674486530163493/8589934592"), parseNumber("  0x265712.2094325 P-5", null, null, 0, null, false));
		assertEquals(BigInteger.valueOf(78521), parseNumber("  0x265712.2094325 P-5", null, new MathContext(5, RoundingMode.HALF_EVEN), 0, null, false));
		assertEquals(Integer.valueOf(78521), parseNumber("  0x265712.2094325 P-5", null, new MathContext(5, RoundingMode.HALF_EVEN), 0, null, true));
		assertEquals(BigInteger.valueOf(643240000), parseNumber("  0x265712.2094325 E2", null, new MathContext(5, RoundingMode.HALF_EVEN), 0, null, false));
		assertEquals(Integer.valueOf(643240000), parseNumber("  0x265712.2094325 E2", null, new MathContext(5, RoundingMode.HALF_EVEN), 0, null, true));
		assertEquals(BigInteger.valueOf(172660000000000000L), parseNumber("  0x0.2657122094325 EF", null, new MathContext(5, RoundingMode.DOWN), 0, null, false));
		assertEquals(Long.valueOf(172670000000000000L), parseNumber("  0x0.2657122094325 EF", null, new MathContext(5, RoundingMode.HALF_EVEN), 0, null, true));
		assertEquals(BigInteger.valueOf(172668551721854208L), parseNumber("  0x0.2657122094325 EF", null, null, 0, null, false));
		assertEquals(Long.valueOf(172668551721854208L), parseNumber("  0x0.2657122094325 EF", null, null, 0, null, true));
		assertEquals(BigInteger.valueOf(172668551721854000L), parseNumber("  0x0.2657122094325 EF", null, null, -3, RoundingMode.HALF_UP, false));
		assertEquals(Long.valueOf(172668551721854000L), parseNumber("  0x0.2657122094325 EF", null, null, -3, RoundingMode.HALF_EVEN, true));
	}
}
