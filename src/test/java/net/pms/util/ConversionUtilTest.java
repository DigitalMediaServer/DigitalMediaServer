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
	public void formatBytesTest() {
		assertEquals("0 bytes", formatBytes(0, false));
		assertEquals("0 bytes", formatBytes(0, true));
		assertEquals("1 byte", formatBytes(1, false));
		assertEquals("1 byte", formatBytes(1, true));
		assertEquals("2 bytes", formatBytes(2, false));
		assertEquals("2 bytes", formatBytes(2, true));
		assertEquals("100 bytes", formatBytes(100, false));
		assertEquals("100 bytes", formatBytes(100, true));
		assertEquals("1 kB", formatBytes(1000, false));
		assertEquals("1000 bytes", formatBytes(1000, true));
		assertEquals("1.0 kB", formatBytes(1024, false));
		assertEquals("1,0 kB", formatBytes(1024, false, Locale.GERMAN));
		assertEquals("1 KiB", formatBytes(1024, true));
		assertEquals("4.1 kB", formatBytes(4097, false));
		assertEquals("4,1 kB", formatBytes(4097, false, Locale.GERMAN));
		assertEquals("4.0 KiB", formatBytes(4097, true));
		assertEquals("4,0 KiB", formatBytes(4097, true, Locale.GERMAN));
		assertEquals("-4.1 kB", formatBytes(-4097, false));
		assertEquals("-4,1 kB", formatBytes(-4097, false, Locale.GERMAN));
		assertEquals("-4.0 KiB", formatBytes(-4097, true));
		assertEquals("-4,0 KiB", formatBytes(-4097, true, Locale.GERMAN));
		assertEquals("153.4 kB", formatBytes(153442, false));
		assertEquals("153,4 kB", formatBytes(153442, false, Locale.GERMAN));
		assertEquals("149.8 KiB", formatBytes(153442, true));
		assertEquals("149,8 KiB", formatBytes(153442, true, Locale.GERMAN));
		assertEquals("-153.4 kB", formatBytes(-153442, false));
		assertEquals("-153,4 kB", formatBytes(-153442, false, Locale.GERMAN));
		assertEquals("-149.8 KiB", formatBytes(-153442, true));
		assertEquals("-149,8 KiB", formatBytes(-153442, true, Locale.GERMAN));
		assertEquals("634.2 MB", formatBytes(634153442, false));
		assertEquals("634,2 MB", formatBytes(634153442, false, Locale.GERMAN));
		assertEquals("604.8 MiB", formatBytes(634153442, true));
		assertEquals("604,8 MiB", formatBytes(634153442, true, Locale.GERMAN));
		assertEquals("634 MB", formatBytes(634000000, false));
		assertEquals("600 MiB", formatBytes(600 * 1L << 20, true));
		assertEquals("-600 MiB", formatBytes(-600 * 1L << 20, true));
		assertEquals("1.4 GB", formatBytes(1426453442, false));
		assertEquals("1,4 GB", formatBytes(1426453442, false, Locale.GERMAN));
		assertEquals("1.3 GiB", formatBytes(1426453442, true));
		assertEquals("1,3 GiB", formatBytes(1426453442, true, Locale.GERMAN));
		assertEquals("2.1 GB", formatBytes(Integer.MAX_VALUE, false));
		assertEquals("2,1 GB", formatBytes(Integer.MAX_VALUE, false, Locale.GERMAN));
		assertEquals("2.0 GiB", formatBytes(Integer.MAX_VALUE, true));
		assertEquals("2,0 GiB", formatBytes(Integer.MAX_VALUE, true, Locale.GERMAN));
		assertEquals("2 GiB", formatBytes(Integer.MAX_VALUE + 1L, true));
		assertEquals("-2.1 GB", formatBytes(Integer.MIN_VALUE, false));
		assertEquals("-2,1 GB", formatBytes(Integer.MIN_VALUE, false, Locale.GERMAN));
		assertEquals("-2 GiB", formatBytes(Integer.MIN_VALUE, true));
		assertEquals("568.4 GB", formatBytes(568426453442L, false));
		assertEquals("568,4 GB", formatBytes(568426453442L, false, Locale.GERMAN));
		assertEquals("529.4 GiB", formatBytes(568426453442L, true));
		assertEquals("529,4 GiB", formatBytes(568426453442L, true, Locale.GERMAN));
		assertEquals("8.6 TB", formatBytes(8568426453442L, false));
		assertEquals("8,6 TB", formatBytes(8568426453442L, false, Locale.GERMAN));
		assertEquals("7.8 TiB", formatBytes(8568426453442L, true));
		assertEquals("7,8 TiB", formatBytes(8568426453442L, true, Locale.GERMAN));
		assertEquals("328.6 TB", formatBytes(328568426453442L, false));
		assertEquals("328,6 TB", formatBytes(328568426453442L, false, Locale.GERMAN));
		assertEquals("298.8 TiB", formatBytes(328568426453442L, true));
		assertEquals("298,8 TiB", formatBytes(328568426453442L, true, Locale.GERMAN));
		assertEquals("62.3 PB", formatBytes(62328568426453442L, false));
		assertEquals("62,3 PB", formatBytes(62328568426453442L, false, Locale.GERMAN));
		assertEquals("55.4 PiB", formatBytes(62328568426453442L, true));
		assertEquals("55,4 PiB", formatBytes(62328568426453442L, true, Locale.GERMAN));
		assertEquals("962.3 PB", formatBytes(962328568426453442L, false));
		assertEquals("962,3 PB", formatBytes(962328568426453442L, false, Locale.GERMAN));
		assertEquals("854.7 PiB", formatBytes(962328568426453442L, true));
		assertEquals("854,7 PiB", formatBytes(962328568426453442L, true, Locale.GERMAN));
		assertEquals("9.2 EB", formatBytes(Long.MAX_VALUE, false));
		assertEquals("9,2 EB", formatBytes(Long.MAX_VALUE, false, Locale.GERMAN));
		assertEquals("8.0 EiB", formatBytes(Long.MAX_VALUE, true));
		assertEquals("8,0 EiB", formatBytes(Long.MAX_VALUE, true, Locale.GERMAN));
		assertEquals("-9.2 EB", formatBytes(Long.MIN_VALUE + 1L, false));
		assertEquals("-9,2 EB", formatBytes(Long.MIN_VALUE + 1L, false, Locale.GERMAN));
		assertEquals("-8.0 EiB", formatBytes(Long.MIN_VALUE + 1L, true));
		assertEquals("-8,0 EiB", formatBytes(Long.MIN_VALUE + 1L, true, Locale.GERMAN));
		assertEquals("-9.2 EB", formatBytes(Long.MIN_VALUE, false));
		assertEquals("-9,2 EB", formatBytes(Long.MIN_VALUE, false, Locale.GERMAN));
		assertEquals("-8 EiB", formatBytes(Long.MIN_VALUE, true));
	}

	@Test
	public void parseNumberWithUnitNoRoundingOverloadTest() {
		// Invalid
		assertNull(parseNumberWithUnit(null, null, null, null, null, false, false, false));
		assertNull(parseNumberWithUnit("", null, null, null, null, false, false, false));
		assertNull(parseNumberWithUnit(" \t ", null, null, null, null, false, false, false));
		assertNull(parseNumberWithUnit("NotANumber", null, null, null, null, false, false, false));
		assertNull(parseNumberWithUnit("Not a number", null, null, null, null, false, false, false));
		assertNull(parseNumberWithUnit("KeineNummer", Locale.GERMAN, null, null, null, false, false, false));
		assertNull(parseNumberWithUnit("Keine Nummer", Locale.GERMAN, null, null, null, false, false, false));
		assertNull(parseNumberWithUnit("Keine,Nummer", Locale.GERMAN, null, null, null, false, false, false));
		assertNull(parseNumberWithUnit("Keine.Nummer", Locale.GERMAN, null, null, null, false, false, false));

		// Without unit
		assertEquals(new Pair<Number, String>(Integer.valueOf(4), null), parseNumberWithUnit("4", null, null, null, null, false, false, false));

		// Percent
		assertNull(parseNumberWithUnit("46%", null, null, null, null, false, false, false));
		assertEquals(new Pair<Number, String>(Integer.valueOf(46), "%"), parseNumberWithUnit("46%", null, null, null, null, true, false, false));
		assertEquals(new Pair<Number, String>(Rational.valueOf(93, 10), "%"), parseNumberWithUnit("9,3%", Locale.GERMAN, null, null, null, true, false, false));
		assertNull(parseNumberWithUnit("9,3k%", Locale.GERMAN, null, null, null, true, false, false));
		assertNull(parseNumberWithUnit("9,3 Mi%", Locale.GERMAN, null, null, null, true, false, false));
		assertEquals(new Pair<Number, String>(Double.valueOf(130.125), "%"), parseNumberWithUnit("130,125%", Locale.GERMAN, null, null, null, true, false, false));
		assertEquals(new Pair<Number, String>(Integer.valueOf(0x2f), "%"), parseNumberWithUnit("0x2f%", Locale.GERMAN, null, null, null, true, false, false));
		assertEquals(new Pair<Number, String>(Double.valueOf(47.88671875), "%"), parseNumberWithUnit("0x2f,e3%", Locale.GERMAN, null, null, null, true, false, false));

		// Required unit
		assertEquals(new Pair<Number, String>(Rational.valueOf(37632, 5), "B"), parseNumberWithUnit(" 7.35kiB ", null, "B", null, null, false, false, false));
		assertEquals(new Pair<Number, String>(Rational.valueOf(37632, 5), "b"), parseNumberWithUnit(" 7.35kib ", null, "B", null, null, false, false, false));
		assertNull(parseNumberWithUnit(" 7.35ki ", null, "B", null, null, false, false, false));
		assertEquals(new Pair<Number, String>(Integer.valueOf(700), "m"), parseNumberWithUnit("700m", null, "m", null, null, false, false, false));
		assertEquals(new Pair<Number, String>(Integer.valueOf(700), "m"), parseNumberWithUnit(" 700 m ", null, "M", null, null, false, false, false));

		// Default unit
		assertEquals(new Pair<Number, String>(Integer.valueOf(7350), "b"), parseNumberWithUnit(" 7.35k ", null, "B", null, "b", false, false, false));
		assertEquals(new Pair<Number, String>(Integer.valueOf(7350), "b"), parseNumberWithUnit(" 7.35k ", null, "b", null, "b", false, false, false));
		assertEquals(new Pair<Number, String>(Integer.valueOf(564), "b"), parseNumberWithUnit(" 0x234 ", null, "B", null, "b", false, false, false));
		assertEquals(new Pair<Number, String>(Integer.valueOf(564), "b"), parseNumberWithUnit(" 0x234 ", null, "b", null, "b", false, false, false));
		assertEquals(new Pair<Number, String>(Integer.valueOf(1), "b"), parseNumberWithUnit(" 0x1 ", null, null, null, "b", false, false, false));

		// Default unit prefix
		assertNull(parseNumberWithUnit(" 7.35", null, null, UnitPrefix.CENTI, null, false, false, false));
		assertEquals(new Pair<Number, String>(Rational.valueOf(147, 2000), null), parseNumberWithUnit(" 7.35", null, null, UnitPrefix.CENTI, null, false, false, true));
		assertEquals(new Pair<Number, String>(Integer.valueOf(1048576), "b"), parseNumberWithUnit(" 0x1 ", null, null, UnitPrefix.MEBI, "b", false, false, false));
		assertEquals(new Pair<Number, String>(Integer.valueOf(1024), "b"), parseNumberWithUnit(" 0x1 KI", null, null, UnitPrefix.MEBI, "b", false, false, false));
		assertEquals(new Pair<Number, String>(Rational.valueOf(25165824, 5), "B"), parseNumberWithUnit(" 4,8 ", Locale.GERMAN, "B", UnitPrefix.MEBI, "B", false, false, false));

		// Fractional units
		assertEquals(new Pair<Number, String>(Rational.valueOf(147, 20), "nB"), parseNumberWithUnit(" 7.35nB ", null, null, null, null, false, false, false));
		assertEquals(new Pair<Number, String>(Rational.valueOf(147L, 20000000000L), "B"), parseNumberWithUnit(" 7.35nB ", null, null, null, null, false, false, true));
		assertEquals(new Pair<Number, String>(Integer.valueOf(7350000), "B"), parseNumberWithUnit(" 7.35MB ", null, null, null, null, false, false, true));
		assertEquals(new Pair<Number, String>(Rational.valueOf(147, 20000), "B"), parseNumberWithUnit(" 7.35mB ", null, null, null, null, false, false, true));
		assertEquals(new Pair<Number, String>(Long.valueOf(735000000000L), "B"), parseNumberWithUnit(" 0.000735PB ", null, null, null, null, false, false, true));
		assertEquals(new Pair<Number, String>(Rational.valueOf(147, 2000000), "B"), parseNumberWithUnit(" 73500000pB ", null, null, null, null, false, false, true));
		assertEquals(new Pair<Number, String>(Integer.valueOf(735), "B"), parseNumberWithUnit(" 0.000000000000000000735 ZB ", null, null, null, null, false, false, true));
		assertEquals(new Pair<Number, String>(Rational.valueOf(147, 200), "B"), parseNumberWithUnit(" 735000000000000000000zB ", null, null, null, null, false, false, true));
		assertEquals(new Pair<Number, String>(Integer.valueOf(735000), "B"), parseNumberWithUnit(" 0.000000000000000000735 YB ", null, null, null, null, false, false, true));
		assertEquals(new Pair<Number, String>(Rational.valueOf(147, 200000), "B"), parseNumberWithUnit(" 735000000000000000000yB ", null, null, null, null, false, false, true));

		// Combinations
		assertEquals(new Pair<Number, String>(Integer.valueOf(400), null), parseNumberWithUnit("4h", null, null, null, null, false, false, false));
		assertEquals(new Pair<Number, String>(Integer.valueOf(4096), "h"), parseNumberWithUnit("4Kih", null, null, null, null, false, false, false));
		assertEquals(new Pair<Number, String>(Long.valueOf(4000000000000L), "b"), parseNumberWithUnit("4 tb  ", null, null, null, null, false, false, false));
		assertEquals(new Pair<Number, String>(Long.valueOf(4398046511104L), "B"), parseNumberWithUnit("  4tiB ", null, null, null, null, false, false, false));
		assertEquals(new Pair<Number, String>(Integer.valueOf(-3960), "B"), parseNumberWithUnit(" -3.96kB ", null, null, null, null, false, false, false));
		assertEquals(new Pair<Number, String>(Integer.valueOf(7350000), "B"), parseNumberWithUnit(" 7.35MB ", null, null, null, null, false, false, false));
		assertEquals(new Pair<Number, String>(Integer.valueOf(7350000), "B"), parseNumberWithUnit(" 7.35mB ", null, null, null, null, false, false, false));
		assertEquals(new Pair<Number, String>(Rational.valueOf(147, 20), "mB"), parseNumberWithUnit(" 7.35mB ", null, null, null, null, false, true, false));
		assertEquals(new Pair<Number, String>(Rational.valueOf(38535168, 5), "B"), parseNumberWithUnit(" 7.35MiB ", null, null, null, null, false, false, false));
		assertEquals(new Pair<Number, String>(Rational.valueOf(38535168, 5), "B"), parseNumberWithUnit(" 7.35MiB ", null, null, null, null, false, false, false));
		assertEquals(new Pair<Number, String>(Rational.valueOf(38535168, 5), "B"), parseNumberWithUnit(" 7.35Mi ", null, null, null, "B", false, false, false));
		assertEquals(new Pair<Number, String>(Integer.valueOf(23), "L"), parseNumberWithUnit(" 2300 cL ", null, null, null, null, false, false, true));
	}

	@Test
	public void parseNumberWithUnitMathContextOverloadTest() {
		// Invalid
		assertNull(parseNumberWithUnit(null, null, null, null, null, null, false, false, false));
		assertNull(parseNumberWithUnit("", null, null, null, null, null, false, false, false));
		assertNull(parseNumberWithUnit(" \t ", null, null, null, null, null, false, false, false));
		assertNull(parseNumberWithUnit("NotANumber", null, null, null, null, null, false, false, false));
		assertNull(parseNumberWithUnit("Not a number", null, null, null, null, null, false, false, false));
		assertNull(parseNumberWithUnit("KeineNummer", Locale.GERMAN, null, null, null, null, false, false, false));
		assertNull(parseNumberWithUnit("Keine Nummer", Locale.GERMAN, null, null, null, null, false, false, false));
		assertNull(parseNumberWithUnit("Keine,Nummer", Locale.GERMAN, null, null, null, null, false, false, false));
		assertNull(parseNumberWithUnit("Keine.Nummer", Locale.GERMAN, null, null, null, null, false, false, false));

		// Without unit
		assertEquals(new Pair<Number, String>(Integer.valueOf(4), null), parseNumberWithUnit("4", null, null, null, null, null, false, false, false));

		// Percent
		assertNull(parseNumberWithUnit("46%", null, null, null, null, null, false, false, false));
		assertEquals(new Pair<Number, String>(Integer.valueOf(46), "%"), parseNumberWithUnit("46%", null, null, null, null, null, true, false, false));
		assertEquals(new Pair<Number, String>(Rational.valueOf(93, 10), "%"), parseNumberWithUnit("9,3%", Locale.GERMAN, null, null, null, null, true, false, false));
		assertNull(parseNumberWithUnit("9,3k%", Locale.GERMAN, null, null, null, null, true, false, false));
		assertNull(parseNumberWithUnit("9,3 Mi%", Locale.GERMAN, null, null, null, null, true, false, false));
		assertEquals(new Pair<Number, String>(Double.valueOf(130.125), "%"), parseNumberWithUnit("130,125%", Locale.GERMAN, null, null, null, null, true, false, false));
		assertEquals(new Pair<Number, String>(Integer.valueOf(0x2f), "%"), parseNumberWithUnit("0x2f%", Locale.GERMAN, null, null, null, null, true, false, false));
		assertEquals(new Pair<Number, String>(Double.valueOf(47.88671875), "%"), parseNumberWithUnit("0x2f,e3%", Locale.GERMAN, null, null, null, null, true, false, false));

		// Required unit
		assertEquals(new Pair<Number, String>(Integer.valueOf(7526), "B"), parseNumberWithUnit(" 7.35kiB ", null, new MathContext(4, RoundingMode.HALF_EVEN), "B", null, null, false, false, false));
		assertEquals(new Pair<Number, String>(Integer.valueOf(7526), "b"), parseNumberWithUnit(" 7.35kib ", null, new MathContext(4, RoundingMode.HALF_EVEN), "B", null, null, false, false, false));
		assertNull(parseNumberWithUnit(" 7.35ki ", null, new MathContext(5, RoundingMode.HALF_EVEN), "B", null, null, false, false, false));
		assertEquals(new Pair<Number, String>(Integer.valueOf(700), "m"), parseNumberWithUnit("700m", null, new MathContext(3, RoundingMode.HALF_EVEN), "m", null, null, false, false, false));
		assertEquals(new Pair<Number, String>(Integer.valueOf(700), "m"), parseNumberWithUnit(" 700 m ", null, new MathContext(3, RoundingMode.HALF_EVEN), "M", null, null, false, false, false));

		// Default unit
		assertEquals(new Pair<Number, String>(Integer.valueOf(7350), "b"), parseNumberWithUnit(" 7.35k ", null, null, "B", null, "b", false, false, false));
		assertEquals(new Pair<Number, String>(Integer.valueOf(7350), "b"), parseNumberWithUnit(" 7.35k ", null, null, "b", null, "b", false, false, false));
		assertEquals(new Pair<Number, String>(Integer.valueOf(564), "b"), parseNumberWithUnit(" 0x234 ", null, null, "B", null, "b", false, false, false));
		assertEquals(new Pair<Number, String>(Integer.valueOf(564), "b"), parseNumberWithUnit(" 0x234 ", null, null, "b", null, "b", false, false, false));
		assertEquals(new Pair<Number, String>(Integer.valueOf(1), "b"), parseNumberWithUnit(" 0x1 ", null, null, null, null, "b", false, false, false));

		// Default unit prefix
		assertNull(parseNumberWithUnit(" 7.35", null, new MathContext(5, RoundingMode.HALF_EVEN), null, UnitPrefix.CENTI, null, false, false, false));
		assertEquals(new Pair<Number, String>(Rational.valueOf(7, 100), null), parseNumberWithUnit(" 7.35", null, new MathContext(1, RoundingMode.HALF_EVEN), null, UnitPrefix.CENTI, null, false, false, true));
		assertEquals(new Pair<Number, String>(Integer.valueOf(1048576), "b"), parseNumberWithUnit(" 0x1 ", null, null, null, UnitPrefix.MEBI, "b", false, false, false));
		assertEquals(new Pair<Number, String>(Integer.valueOf(1024), "b"), parseNumberWithUnit(" 0x1 KI", null, null, null, UnitPrefix.MEBI, "b", false, false, false));
		assertEquals(new Pair<Number, String>(Integer.valueOf(5033165), "B"), parseNumberWithUnit(" 4,8 ", Locale.GERMAN, new MathContext(7), "B", UnitPrefix.MEBI, "B", false, false, false));

		// Fractional units
		assertEquals(new Pair<Number, String>(Rational.valueOf(147, 20), "nB"), parseNumberWithUnit(" 7.35nB ", null, null, null, null, null, false, false, false));
		assertEquals(new Pair<Number, String>(Rational.valueOf(147L, 20000000000L), "B"), parseNumberWithUnit(" 7.35nB ", null, null, null, null, null, false, false, true));
		assertEquals(new Pair<Number, String>(Integer.valueOf(7350000), "B"), parseNumberWithUnit(" 7.35MB ", null, null, null, null, null, false, false, true));
		assertEquals(new Pair<Number, String>(Rational.valueOf(147, 20000), "B"), parseNumberWithUnit(" 7.35mB ", null, null, null, null, null, false, false, true));
		assertEquals(new Pair<Number, String>(Long.valueOf(735000000000L), "B"), parseNumberWithUnit(" 0.000735PB ", null, null, null, null, null, false, false, true));
		assertEquals(new Pair<Number, String>(Rational.valueOf(147, 2000000), "B"), parseNumberWithUnit(" 73500000pB ", null, null, null, null, null, false, false, true));
		assertEquals(new Pair<Number, String>(Integer.valueOf(735), "B"), parseNumberWithUnit(" 0.000000000000000000735 ZB ", null, null, null, null, null, false, false, true));
		assertEquals(new Pair<Number, String>(Rational.valueOf(147, 200), "B"), parseNumberWithUnit(" 735000000000000000000zB ", null, null, null, null, null, false, false, true));
		assertEquals(new Pair<Number, String>(Integer.valueOf(735000), "B"), parseNumberWithUnit(" 0.000000000000000000735 YB ", null, null, null, null, null, false, false, true));
		assertEquals(new Pair<Number, String>(Rational.valueOf(147, 200000), "B"), parseNumberWithUnit(" 735000000000000000000yB ", null, null, null, null, null, false, false, true));

		// Combinations
		assertEquals(new Pair<Number, String>(Integer.valueOf(400), null), parseNumberWithUnit("4h", null, null, null, null, null, false, false, false));
		assertEquals(new Pair<Number, String>(Integer.valueOf(4096), "h"), parseNumberWithUnit("4Kih", null, null, null, null, null, false, false, false));
		assertEquals(new Pair<Number, String>(Long.valueOf(4000000000000L), "b"), parseNumberWithUnit("4 tb  ", null, null, null, null, null, false, false, false));
		assertEquals(new Pair<Number, String>(Long.valueOf(4398046511104L), "B"), parseNumberWithUnit("  4tiB ", null, null, null, null, null, false, false, false));
		assertEquals(new Pair<Number, String>(Integer.valueOf(-3960), "B"), parseNumberWithUnit(" -3.96kB ", null, null, null, null, null, false, false, false));
		assertEquals(new Pair<Number, String>(Integer.valueOf(7350000), "B"), parseNumberWithUnit(" 7.35MB ", null, null, null, null, null, false, false, false));
		assertEquals(new Pair<Number, String>(Integer.valueOf(7350000), "B"), parseNumberWithUnit(" 7.35mB ", null, null, null, null, null, false, false, false));
		assertEquals(new Pair<Number, String>(Rational.valueOf(147, 20), "mB"), parseNumberWithUnit(" 7.35mB ", null, null, null, null, null, false, true, false));
		assertEquals(new Pair<Number, String>(Rational.valueOf(38535168, 5), "B"), parseNumberWithUnit(" 7.35MiB ", null, null, null, null, null, false, false, false));
		assertEquals(new Pair<Number, String>(Integer.valueOf(7707034), "B"), parseNumberWithUnit(" 7.35MiB ", null, new MathContext(7, RoundingMode.HALF_EVEN), null, null, null, false, false, false));
		assertEquals(new Pair<Number, String>(Integer.valueOf(7707034), "B"), parseNumberWithUnit(" 7.35Mi ", null, new MathContext(7, RoundingMode.HALF_EVEN), null, null, "B", false, false, false));
		assertEquals(new Pair<Number, String>(Integer.valueOf(23), "L"), parseNumberWithUnit(" 2300 cL ", null, new MathContext(5, RoundingMode.HALF_EVEN), null, null, null, false, false, true));
	}

	@Test
	public void parseNumberWithUnitRoundingOverloadTest() {
		// Invalid
		assertNull(parseNumberWithUnit(null, null, 0, null, null, null, null, false, false, false));
		assertNull(parseNumberWithUnit("", null, 0, null, null, null, null, false, false, false));
		assertNull(parseNumberWithUnit(" \t ", null, 0, null, null, null, null, false, false, false));
		assertNull(parseNumberWithUnit("NotANumber", null, 0, null, null, null, null, false, false, false));
		assertNull(parseNumberWithUnit("Not a number", null, 0, null, null, null, null, false, false, false));
		assertNull(parseNumberWithUnit("KeineNummer", Locale.GERMAN, 0, null, null, null, null, false, false, false));
		assertNull(parseNumberWithUnit("Keine Nummer", Locale.GERMAN, 0, null, null, null, null, false, false, false));
		assertNull(parseNumberWithUnit("Keine,Nummer", Locale.GERMAN, 0, null, null, null, null, false, false, false));
		assertNull(parseNumberWithUnit("Keine.Nummer", Locale.GERMAN, 0, null, null, null, null, false, false, false));

		// Without unit
		assertEquals(new Pair<Number, String>(Integer.valueOf(4), null), parseNumberWithUnit("4", null, 0, null, null, null, null, false, false, false));

		// Percent
		assertNull(parseNumberWithUnit("46%", null, 0, null, null, null, null, false, false, false));
		assertEquals(new Pair<Number, String>(Integer.valueOf(46), "%"), parseNumberWithUnit("46%", null, 0, null, null, null, null, true, false, false));
		assertEquals(new Pair<Number, String>(Rational.valueOf(93, 10), "%"), parseNumberWithUnit("9,3%", Locale.GERMAN, 0, null, null, null, null, true, false, false));
		assertNull(parseNumberWithUnit("9,3k%", Locale.GERMAN, 0, null, null, null, null, true, false, false));
		assertNull(parseNumberWithUnit("9,3 Mi%", Locale.GERMAN, 0, null, null, null, null, true, false, false));
		assertEquals(new Pair<Number, String>(Double.valueOf(130.125), "%"), parseNumberWithUnit("130,125%", Locale.GERMAN, 0, null, null, null, null, true, false, false));
		assertEquals(new Pair<Number, String>(Integer.valueOf(0x2f), "%"), parseNumberWithUnit("0x2f%", Locale.GERMAN, 0, null, null, null, null, true, false, false));
		assertEquals(new Pair<Number, String>(Double.valueOf(47.88671875), "%"), parseNumberWithUnit("0x2f,e3%", Locale.GERMAN, 0, null, null, null, null, true, false, false));

		// Required unit
		assertEquals(new Pair<Number, String>(Integer.valueOf(7526), "B"), parseNumberWithUnit(" 7.35kiB ", null, 0, RoundingMode.HALF_EVEN, "B", null, null, false, false, false));
		assertEquals(new Pair<Number, String>(Integer.valueOf(7526), "b"), parseNumberWithUnit(" 7.35kib ", null, 0, RoundingMode.HALF_EVEN, "B", null, null, false, false, false));
		assertNull(parseNumberWithUnit(" 7.35ki ", null, 0, RoundingMode.HALF_EVEN, "B", null, null, false, false, false));
		assertEquals(new Pair<Number, String>(Integer.valueOf(700), "m"), parseNumberWithUnit("700m", null, 0, RoundingMode.HALF_EVEN, "m", null, null, false, false, false));
		assertEquals(new Pair<Number, String>(Integer.valueOf(700), "m"), parseNumberWithUnit(" 700 m ", null, 0, RoundingMode.HALF_EVEN, "M", null, null, false, false, false));

		// Default unit
		assertEquals(new Pair<Number, String>(Integer.valueOf(7350), "b"), parseNumberWithUnit(" 7.35k ", null, 0, null, "B", null, "b", false, false, false));
		assertEquals(new Pair<Number, String>(Integer.valueOf(7350), "b"), parseNumberWithUnit(" 7.35k ", null, 0, null, "b", null, "b", false, false, false));
		assertEquals(new Pair<Number, String>(Integer.valueOf(564), "b"), parseNumberWithUnit(" 0x234 ", null, 0, null, "B", null, "b", false, false, false));
		assertEquals(new Pair<Number, String>(Integer.valueOf(564), "b"), parseNumberWithUnit(" 0x234 ", null, 0, null, "b", null, "b", false, false, false));
		assertEquals(new Pair<Number, String>(Integer.valueOf(1), "b"), parseNumberWithUnit(" 0x1 ", null, 0, null, null, null, "b", false, false, false));

		// Default unit prefix
		assertNull(parseNumberWithUnit(" 7.35", null, 0, RoundingMode.HALF_EVEN, null, UnitPrefix.CENTI, null, false, false, false));
		assertEquals(new Pair<Number, String>(Rational.valueOf(7, 100), null), parseNumberWithUnit(" 7.35", null, 2, RoundingMode.HALF_EVEN, null, UnitPrefix.CENTI, null, false, false, true));
		assertEquals(new Pair<Number, String>(Integer.valueOf(1048576), "b"), parseNumberWithUnit(" 0x1 ", null, 0, null, null, UnitPrefix.MEBI, "b", false, false, false));
		assertEquals(new Pair<Number, String>(Integer.valueOf(1024), "b"), parseNumberWithUnit(" 0x1 KI", null, 0, null, null, UnitPrefix.MEBI, "b", false, false, false));

		// Fractional units
		assertEquals(new Pair<Number, String>(Rational.valueOf(147, 20), "nB"), parseNumberWithUnit(" 7.35nB ", null, 0, null, null, null, null, false, false, false));
		assertEquals(new Pair<Number, String>(Rational.valueOf(147L, 20000000000L), "B"), parseNumberWithUnit(" 7.35nB ", null, 0, null, null, null, null, false, false, true));
		assertEquals(new Pair<Number, String>(Integer.valueOf(7350000), "B"), parseNumberWithUnit(" 7.35MB ", null, 0, null, null, null, null, false, false, true));
		assertEquals(new Pair<Number, String>(Rational.valueOf(147, 20000), "B"), parseNumberWithUnit(" 7.35mB ", null, 0, null, null, null, null, false, false, true));
		assertEquals(new Pair<Number, String>(Long.valueOf(735000000000L), "B"), parseNumberWithUnit(" 0.000735PB ", null, 0, null, null, null, null, false, false, true));
		assertEquals(new Pair<Number, String>(Rational.valueOf(147, 2000000), "B"), parseNumberWithUnit(" 73500000pB ", null, 0, null, null, null, null, false, false, true));
		assertEquals(new Pair<Number, String>(Integer.valueOf(735), "B"), parseNumberWithUnit(" 0.000000000000000000735 ZB ", null, 0, null, null, null, null, false, false, true));
		assertEquals(new Pair<Number, String>(Rational.valueOf(147, 200), "B"), parseNumberWithUnit(" 735000000000000000000zB ", null, 0, null, null, null, null, false, false, true));
		assertEquals(new Pair<Number, String>(Integer.valueOf(735000), "B"), parseNumberWithUnit(" 0.000000000000000000735 YB ", null, 0, null, null, null, null, false, false, true));
		assertEquals(new Pair<Number, String>(Rational.valueOf(147, 200000), "B"), parseNumberWithUnit(" 735000000000000000000yB ", null, 0, null, null, null, null, false, false, true));

		// Combinations
		assertEquals(new Pair<Number, String>(Integer.valueOf(400), null), parseNumberWithUnit("4h", null, 0, null, null, null, null, false, false, false));
		assertEquals(new Pair<Number, String>(Integer.valueOf(4096), "h"), parseNumberWithUnit("4Kih", null, 0, null, null, null, null, false, false, false));
		assertEquals(new Pair<Number, String>(Long.valueOf(4000000000000L), "b"), parseNumberWithUnit("4 tb  ", null, 0, null, null, null, null, false, false, false));
		assertEquals(new Pair<Number, String>(Long.valueOf(4398046511104L), "B"), parseNumberWithUnit("  4tiB ", null, 0, null, null, null, null, false, false, false));
		assertEquals(new Pair<Number, String>(Integer.valueOf(-3960), "B"), parseNumberWithUnit(" -3.96kB ", null, 0, null, null, null, null, false, false, false));
		assertEquals(new Pair<Number, String>(Integer.valueOf(7350000), "B"), parseNumberWithUnit(" 7.35MB ", null, 0, null, null, null, null, false, false, false));
		assertEquals(new Pair<Number, String>(Integer.valueOf(7350000), "B"), parseNumberWithUnit(" 7.35mB ", null, 0, null, null, null, null, false, false, false));
		assertEquals(new Pair<Number, String>(Rational.valueOf(147, 20), "mB"), parseNumberWithUnit(" 7.35mB ", null, 0, null, null, null, null, false, true, false));
		assertEquals(new Pair<Number, String>(Rational.valueOf(38535168, 5), "B"), parseNumberWithUnit(" 7.35MiB ", null, 0, null, null, null, null, false, false, false));
		assertEquals(new Pair<Number, String>(Integer.valueOf(7707034), "B"), parseNumberWithUnit(" 7.35MiB ", null, 0, RoundingMode.HALF_EVEN, null, null, null, false, false, false));
		assertEquals(new Pair<Number, String>(Integer.valueOf(7707034), "B"), parseNumberWithUnit(" 7.35Mi ", null, 0, RoundingMode.HALF_EVEN, null, null, "B", false, false, false));
		assertEquals(new Pair<Number, String>(Integer.valueOf(23), "L"), parseNumberWithUnit(" 2300 cL ", null, 0, RoundingMode.HALF_EVEN, null, null, null, false, false, true));
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
		assertEquals(new Pair<Number, String>(Rational.valueOf(93, 10), "%"), parseNumberWithUnit("9,3%", Locale.GERMAN, null, 0, null, "B", null, null, true, false, false));
		assertEquals(new Pair<Number, String>(Rational.valueOf(93, 10), "%"), parseNumberWithUnit("9,3%", Locale.GERMAN, null, 0, null, null, UnitPrefix.MEBI, null, true, false, false));
		assertEquals(new Pair<Number, String>(Rational.valueOf(93, 10), "%"), parseNumberWithUnit("9,3%", Locale.GERMAN, null, 0, null, null, null, "B", true, false, false));
		assertEquals(new Pair<Number, String>(Rational.valueOf(93, 10), "%"), parseNumberWithUnit("9,3%", Locale.GERMAN, null, 0, null, "b", null, "B", true, false, false));
		assertNull(parseNumberWithUnit("9,3k%", Locale.GERMAN, null, 0, null, null, null, null, true, false, false));
		assertNull(parseNumberWithUnit("9,3 Mi%", Locale.GERMAN, null, 0, null, null, null, null, true, false, false));
		assertEquals(new Pair<Number, String>(Double.valueOf(130.125), "%"), parseNumberWithUnit("130,125%", Locale.GERMAN, null, 0, null, null, null, null, true, false, false));
		assertEquals(new Pair<Number, String>(Integer.valueOf(0x2f), "%"), parseNumberWithUnit("0x2f%", Locale.GERMAN, null, 0, null, null, null, null, true, false, false));
		assertEquals(new Pair<Number, String>(Double.valueOf(47.88671875), "%"), parseNumberWithUnit("0x2f,e3%", Locale.GERMAN, null, 0, null, null, null, null, true, false, false));

		// Required unit
		assertEquals(new Pair<Number, String>(Integer.valueOf(7526), "B"), parseNumberWithUnit(" 7.35kiB ", null, null, 0, RoundingMode.HALF_EVEN, "B", null, null, false, false, false));
		assertEquals(new Pair<Number, String>(Integer.valueOf(7526), "b"), parseNumberWithUnit(" 7.35kib ", null, null, 0, RoundingMode.HALF_EVEN, "B", null, null, false, false, false));
		assertNull(parseNumberWithUnit(" 7.35ki ", null, null, 0, RoundingMode.HALF_EVEN, "B", null, null, false, false, false));
		assertEquals(new Pair<Number, String>(Integer.valueOf(700), "m"), parseNumberWithUnit("700m", null, null, 0, RoundingMode.HALF_EVEN, "m", null, null, false, false, false));
		assertEquals(new Pair<Number, String>(Integer.valueOf(700), "m"), parseNumberWithUnit(" 700 m ", null, null, 0, RoundingMode.HALF_EVEN, "M", null, null, false, false, false));

		// Default unit
		assertEquals(new Pair<Number, String>(Integer.valueOf(7350), "b"), parseNumberWithUnit(" 7.35k ", null, null, 0, null, "B", null, "b", false, false, false));
		assertEquals(new Pair<Number, String>(Integer.valueOf(7350), "b"), parseNumberWithUnit(" 7.35k ", null, null, 0, null, "b", null, "b", false, false, false));
		assertEquals(new Pair<Number, String>(Integer.valueOf(564), "b"), parseNumberWithUnit(" 0x234 ", null, null, 0, null, "B", null, "b", false, false, false));
		assertEquals(new Pair<Number, String>(Integer.valueOf(564), "b"), parseNumberWithUnit(" 0x234 ", null, null, 0, null, "b", null, "b", false, false, false));
		assertEquals(new Pair<Number, String>(Integer.valueOf(1), "b"), parseNumberWithUnit(" 0x1 ", null, null, 0, null, null, null, "b", false, false, false));

		// Default unit prefix
		assertNull(parseNumberWithUnit(" 7.35", null, null, 0, RoundingMode.HALF_EVEN, null, UnitPrefix.CENTI, null, false, false, false));
		assertEquals(new Pair<Number, String>(Rational.valueOf(7, 100), null), parseNumberWithUnit(" 7.35", null, null, 2, RoundingMode.HALF_EVEN, null, UnitPrefix.CENTI, null, false, false, true));
		assertEquals(new Pair<Number, String>(Integer.valueOf(1048576), "b"), parseNumberWithUnit(" 0x1 ", null, null, 0, null, null, UnitPrefix.MEBI, "b", false, false, false));
		assertEquals(new Pair<Number, String>(Integer.valueOf(1024), "b"), parseNumberWithUnit(" 0x1 KI", null, null, 0, null, null, UnitPrefix.MEBI, "b", false, false, false));
		assertEquals(new Pair<Number, String>(Integer.valueOf(5033165), "B"), parseNumberWithUnit(" 4,8 ", Locale.GERMAN, new MathContext(7), 0, null, "B", UnitPrefix.MEBI, "B", false, false, false));
		assertEquals(new Pair<Number, String>(Integer.valueOf(5033165), "B"), parseNumberWithUnit(" 4,8 ", Locale.GERMAN, new MathContext(7), 0, null, null, UnitPrefix.MEBI, "B", false, false, false));
		assertEquals(new Pair<Number, String>(Integer.valueOf(5033165), null), parseNumberWithUnit(" 4,8 ", Locale.GERMAN, new MathContext(7), 0, null, null, UnitPrefix.MEBI, null, false, false, false));
		assertEquals(new Pair<Number, String>(Rational.valueOf(24, 5), "B"), parseNumberWithUnit(" 4,8 B", Locale.GERMAN, new MathContext(7), 0, null, null, UnitPrefix.MEBI, null, false, false, false));

		// Fractional units
		assertEquals(new Pair<Number, String>(Rational.valueOf(147, 20), "nB"), parseNumberWithUnit(" 7.35nB ", null, null, 0, null, null, null, null, false, false, false));
		assertEquals(new Pair<Number, String>(Rational.valueOf(147L, 20000000000L), "B"), parseNumberWithUnit(" 7.35nB ", null, null, 0, null, null, null, null, false, false, true));
		assertEquals(new Pair<Number, String>(Integer.valueOf(7350000), "B"), parseNumberWithUnit(" 7.35MB ", null, null, 0, null, null, null, null, false, false, true));
		assertEquals(new Pair<Number, String>(Rational.valueOf(147, 20000), "B"), parseNumberWithUnit(" 7.35mB ", null, null, 0, null, null, null, null, false, false, true));
		assertEquals(new Pair<Number, String>(Long.valueOf(735000000000L), "B"), parseNumberWithUnit(" 0.000735PB ", null, null, 0, null, null, null, null, false, false, true));
		assertEquals(new Pair<Number, String>(Rational.valueOf(147, 2000000), "B"), parseNumberWithUnit(" 73500000pB ", null, null, 0, null, null, null, null, false, false, true));
		assertEquals(new Pair<Number, String>(Integer.valueOf(735), "B"), parseNumberWithUnit(" 0.000000000000000000735 ZB ", null, null, 0, null, null, null, null, false, false, true));
		assertEquals(new Pair<Number, String>(Rational.valueOf(147, 200), "B"), parseNumberWithUnit(" 735000000000000000000zB ", null, null, 0, null, null, null, null, false, false, true));
		assertEquals(new Pair<Number, String>(Integer.valueOf(735000), "B"), parseNumberWithUnit(" 0.000000000000000000735 YB ", null, null, 0, null, null, null, null, false, false, true));
		assertEquals(new Pair<Number, String>(Rational.valueOf(147, 200000), "B"), parseNumberWithUnit(" 735000000000000000000yB ", null, null, 0, null, null, null, null, false, false, true));

		// Combinations
		assertEquals(new Pair<Number, String>(Integer.valueOf(400), null), parseNumberWithUnit("4h", null, null, 0, null, null, null, null, false, false, false));
		assertEquals(new Pair<Number, String>(Integer.valueOf(4096), "h"), parseNumberWithUnit("4Kih", null, null, 0, null, null, null, null, false, false, false));
		assertEquals(new Pair<Number, String>(Long.valueOf(4000000000000L), "b"), parseNumberWithUnit("4 tb  ", null, null, 0, null, null, null, null, false, false, false));
		assertEquals(new Pair<Number, String>(Long.valueOf(4398046511104L), "B"), parseNumberWithUnit("  4tiB ", null, null, 0, null, null, null, null, false, false, false));
		assertEquals(new Pair<Number, String>(Integer.valueOf(-3960), "B"), parseNumberWithUnit(" -3.96kB ", null, null, 0, null, null, null, null, false, false, false));
		assertEquals(new Pair<Number, String>(Integer.valueOf(7350000), "B"), parseNumberWithUnit(" 7.35MB ", null, null, 0, null, null, null, null, false, false, false));
		assertEquals(new Pair<Number, String>(Integer.valueOf(7350000), "B"), parseNumberWithUnit(" 7.35mB ", null, null, 0, null, null, null, null, false, false, false));
		assertEquals(new Pair<Number, String>(Rational.valueOf(147, 20), "mB"), parseNumberWithUnit(" 7.35mB ", null, null, 0, null, null, null, null, false, true, false));
		assertEquals(new Pair<Number, String>(Rational.valueOf(38535168, 5), "B"), parseNumberWithUnit(" 7.35MiB ", null, null, 0, null, null, null, null, false, false, false));
		assertEquals(new Pair<Number, String>(Integer.valueOf(7707034), "B"), parseNumberWithUnit(" 7.35MiB ", null, null, 0, RoundingMode.HALF_EVEN, null, null, null, false, false, false));
		assertEquals(new Pair<Number, String>(Integer.valueOf(7707034), "B"), parseNumberWithUnit(" 7.35Mi ", null, null, 0, RoundingMode.HALF_EVEN, null, null, "B", false, false, false));
		assertEquals(new Pair<Number, String>(Integer.valueOf(23), "L"), parseNumberWithUnit(" 2300 cL ", null, null, 0, RoundingMode.HALF_EVEN, null, null, null, false, false, true));
	}

	@Test
	public void parseIntNoRoundingTest() {
		// Invalid
		assertEquals(-666, parseInt(null, -666));
		assertEquals(-666, parseInt(LoggerFactory.getILoggerFactory(), -666));
		assertEquals(-666, parseInt(new StringBuilder("foobar"), -666));
		assertEquals(-666, parseInt("foo 2 bar", -666));
		assertEquals(-666, parseInt(" \t", -666));
		assertEquals(-666, parseInt(" ", -666));
		assertEquals(-666, parseInt("", -666));

		// Number
		assertEquals(Byte.MAX_VALUE, parseInt(Integer.valueOf(Byte.MAX_VALUE), -666));
		assertEquals(Byte.MAX_VALUE, parseInt(Long.valueOf(Byte.MAX_VALUE), -666));
		assertEquals(Byte.MAX_VALUE, parseInt(Byte.valueOf(Byte.MAX_VALUE), -666));
		assertEquals(Byte.MIN_VALUE, parseInt(Integer.valueOf(Byte.MIN_VALUE), -666));
		assertEquals(Byte.MIN_VALUE, parseInt(Long.valueOf(Byte.MIN_VALUE), -666));
		assertEquals(Byte.MIN_VALUE, parseInt(Byte.valueOf(Byte.MIN_VALUE), -666));

		assertEquals(Short.MAX_VALUE, parseInt(Integer.valueOf(Short.MAX_VALUE), -666));
		assertEquals(Short.MAX_VALUE, parseInt(Long.valueOf(Short.MAX_VALUE), -666));
		assertEquals(Short.MAX_VALUE, parseInt(Short.valueOf(Short.MAX_VALUE), -666));
		assertEquals(Short.MIN_VALUE, parseInt(Integer.valueOf(Short.MIN_VALUE), -666));
		assertEquals(Short.MIN_VALUE, parseInt(Long.valueOf(Short.MIN_VALUE), -666));
		assertEquals(Short.MIN_VALUE, parseInt(Short.valueOf(Short.MIN_VALUE), -666));

		assertEquals(Integer.MAX_VALUE, parseInt(Integer.valueOf(Integer.MAX_VALUE), -666));
		assertEquals(Integer.MAX_VALUE, parseInt(Long.valueOf(Integer.MAX_VALUE), -666));
		assertEquals(Integer.MIN_VALUE, parseInt(Integer.valueOf(Integer.MIN_VALUE), -666));
		assertEquals(Integer.MIN_VALUE, parseInt(Long.valueOf(Integer.MIN_VALUE), -666));

		assertEquals(-666, parseInt(Long.valueOf(Long.MAX_VALUE), -666));
		assertEquals(-666, parseInt(BigInteger.valueOf(Long.MAX_VALUE), -666));
		assertEquals(-666, parseInt(Long.valueOf(Long.MIN_VALUE), -666));
		assertEquals(-666, parseInt(BigInteger.valueOf(Long.MIN_VALUE), -666));

		assertEquals(Integer.MAX_VALUE, parseInt(Long.valueOf(Integer.MAX_VALUE), -666));
		assertEquals(Integer.MIN_VALUE, parseInt(Long.valueOf(Integer.MIN_VALUE), -666));

		assertEquals(-666, parseInt(Long.valueOf(Long.MAX_VALUE), -666));
		assertEquals(-666, parseInt(Long.valueOf(Long.MIN_VALUE), -666));

		assertEquals(-666, parseInt(BigInteger.valueOf(Long.MAX_VALUE).multiply(BigInteger.TEN), -666));
		assertEquals(-666, parseInt(BigInteger.valueOf(Long.MIN_VALUE).multiply(BigInteger.TEN), -666));

		assertEquals(-666, parseInt(Double.valueOf(Double.MAX_VALUE), -666));
		assertEquals(-666, parseInt(Double.valueOf(Double.MIN_VALUE), -666));

		assertEquals(-666, parseInt(BigDecimal.valueOf(Long.MAX_VALUE, -10), -666));
		assertEquals(-666, parseInt(BigDecimal.valueOf(Long.MIN_VALUE, -10), -666));

		assertEquals(-666, parseInt(Rational.valueOf(BigDecimal.valueOf(Long.MAX_VALUE, -10)), -666));
		assertEquals(-666, parseInt(Rational.valueOf(BigDecimal.valueOf(Long.MIN_VALUE, -10)), -666));

		assertEquals(-666, parseInt(Rational.valueOf(Long.MAX_VALUE), -666));
		assertEquals(-666, parseInt(Rational.valueOf(Long.MIN_VALUE), -666));

		assertEquals(Integer.MAX_VALUE, parseInt(Rational.valueOf(Integer.MAX_VALUE), -666));
		assertEquals(Integer.MIN_VALUE, parseInt(Rational.valueOf(Integer.MIN_VALUE), -666));

		assertEquals(Short.MAX_VALUE, parseInt(Rational.valueOf(Short.MAX_VALUE), -666));
		assertEquals(Short.MIN_VALUE, parseInt(Rational.valueOf(Short.MIN_VALUE), -666));

		assertEquals(Byte.MAX_VALUE, parseInt(Rational.valueOf(Byte.MAX_VALUE), -666));
		assertEquals(Byte.MIN_VALUE, parseInt(Rational.valueOf(Byte.MIN_VALUE), -666));

		assertEquals(-666, parseInt(Rational.valueOf(3, 4), -666));

		// String
		assertEquals(0, parseInt("0", -666));
		assertEquals(1, parseInt("1", -666));
		assertEquals(-1, parseInt("-1", -666));

		assertEquals(0, parseInt("0.00 E14", -666));
		assertEquals(1, parseInt("0.01 E2", -666));
		assertEquals(-1, parseInt("-0.01 E2", -666));

		assertEquals(0, parseInt("0x0.00 E-14", -666));
		assertEquals(0, parseInt("0x0.00 P-14", -666));
		assertEquals(1, parseInt("0x0.01 E2", -666));
		assertEquals(1, parseInt("0x10 E-1", -666));
		assertEquals(1, parseInt("0x10 P-4", -666));
		assertEquals(-1, parseInt("-0x0.01 E2", -666));
		assertEquals(-1, parseInt("-0x10 E-1", -666));
		assertEquals(-1, parseInt("-0x10 P-4", -666));

		assertEquals(10, parseInt("0x0.0a E2", -666));
		assertEquals(10, parseInt("0xa0 E-1", -666));
		assertEquals(10, parseInt("0xa0 P-4", -666));
		assertEquals(-10, parseInt("-0x0.0a E2", -666));
		assertEquals(-10, parseInt("-0xa0 E-1", -666));
		assertEquals(-10, parseInt("-0xa0p-4", -666));

		assertEquals(-666, parseInt("  34.45", -666));
		assertEquals(-666, parseInt("34.45 E1  ", -666));
		assertEquals(3445, parseInt("34.45E2", -666));
		assertEquals(3445, parseInt("34.45 E+2", -666));
		assertEquals(-666, parseInt("  34.45  E+2 ", -666));

		assertEquals(-666, parseInt(" 12.25 ", -666));
		assertEquals(-666, parseInt(" 12 .25 ", -666));
		assertEquals(-666, parseInt(" 12. 25 ", -666));

		assertEquals(-666, parseInt("  0x265712.2094325 P-5", -666));
		assertEquals(-666, parseInt("  0x265712.2094325 E2", -666));
		assertEquals(-666, parseInt("  0x0.2657122094325 EF", -666));
	}

	@Test
	public void parseIntMathContextOverloadTest() {
		// Invalid
		assertEquals(-666, parseInt(null, null, null, -666));
		assertEquals(-666, parseInt(LoggerFactory.getILoggerFactory(), null, null, -666));
		assertEquals(-666, parseInt(new StringBuilder("foobar"), null, null, -666));
		assertEquals(-666, parseInt("foo 2 bar", null, null, -666));
		assertEquals(-666, parseInt(" \t", null, null, -666));
		assertEquals(-666, parseInt(" ", null, null, -666));
		assertEquals(-666, parseInt("", null, null, -666));

		// Number
		assertEquals(Byte.MAX_VALUE, parseInt(Integer.valueOf(Byte.MAX_VALUE), null, null, -666));
		assertEquals(Byte.MAX_VALUE, parseInt(Long.valueOf(Byte.MAX_VALUE), null, null, -666));
		assertEquals(Byte.MAX_VALUE, parseInt(Byte.valueOf(Byte.MAX_VALUE), null, null, -666));
		assertEquals(Byte.MIN_VALUE, parseInt(Integer.valueOf(Byte.MIN_VALUE), null, null, -666));
		assertEquals(Byte.MIN_VALUE, parseInt(Long.valueOf(Byte.MIN_VALUE), null, null, -666));
		assertEquals(Byte.MIN_VALUE, parseInt(Byte.valueOf(Byte.MIN_VALUE), null, null, -666));

		assertEquals(Short.MAX_VALUE, parseInt(Integer.valueOf(Short.MAX_VALUE), null, null, -666));
		assertEquals(Short.MAX_VALUE, parseInt(Long.valueOf(Short.MAX_VALUE), null, null, -666));
		assertEquals(Short.MAX_VALUE, parseInt(Short.valueOf(Short.MAX_VALUE), null, null, -666));
		assertEquals(Short.MIN_VALUE, parseInt(Integer.valueOf(Short.MIN_VALUE), null, null, -666));
		assertEquals(Short.MIN_VALUE, parseInt(Long.valueOf(Short.MIN_VALUE), null, null, -666));
		assertEquals(Short.MIN_VALUE, parseInt(Short.valueOf(Short.MIN_VALUE), null, null, -666));

		assertEquals(Integer.MAX_VALUE, parseInt(Integer.valueOf(Integer.MAX_VALUE), null, null, -666));
		assertEquals(Integer.MAX_VALUE, parseInt(Long.valueOf(Integer.MAX_VALUE), null, null, -666));
		assertEquals(Integer.MIN_VALUE, parseInt(Integer.valueOf(Integer.MIN_VALUE), null, null, -666));
		assertEquals(Integer.MIN_VALUE, parseInt(Long.valueOf(Integer.MIN_VALUE), null, null, -666));

		assertEquals(-666, parseInt(Long.valueOf(Long.MAX_VALUE), null, null, -666));
		assertEquals(-666, parseInt(BigInteger.valueOf(Long.MAX_VALUE), null, null, -666));
		assertEquals(-666, parseInt(Long.valueOf(Long.MIN_VALUE), null, null, -666));
		assertEquals(-666, parseInt(BigInteger.valueOf(Long.MIN_VALUE), null, null, -666));

		assertEquals(Integer.MAX_VALUE, parseInt(Long.valueOf(Integer.MAX_VALUE), null, MathContext.DECIMAL64, -666));
		assertEquals(Integer.MIN_VALUE, parseInt(Long.valueOf(Integer.MIN_VALUE), null, MathContext.DECIMAL64, -666));

		assertEquals(-666, parseInt(Long.valueOf(Long.MAX_VALUE), null, MathContext.DECIMAL128, -666));
		assertEquals(-666, parseInt(Long.valueOf(Long.MIN_VALUE), null, MathContext.DECIMAL128, -666));

		assertEquals(-666, parseInt(BigInteger.valueOf(Long.MAX_VALUE).multiply(BigInteger.TEN), null, MathContext.DECIMAL128, -666));
		assertEquals(-666, parseInt(BigInteger.valueOf(Long.MIN_VALUE).multiply(BigInteger.TEN), null, MathContext.DECIMAL128, -666));

		assertEquals(-666, parseInt(Double.valueOf(Double.MAX_VALUE), null, MathContext.DECIMAL128, -666));
		assertEquals(-666, parseInt(Double.valueOf(Double.MIN_VALUE), null, MathContext.DECIMAL128, -666));

		assertEquals(-666, parseInt(BigDecimal.valueOf(Long.MAX_VALUE, -10), null, MathContext.DECIMAL128, -666));
		assertEquals(-666, parseInt(BigDecimal.valueOf(Long.MIN_VALUE, -10), null, MathContext.DECIMAL128, -666));

		assertEquals(-666, parseInt(Rational.valueOf(BigDecimal.valueOf(Long.MAX_VALUE, -10)), null, MathContext.DECIMAL128, -666));
		assertEquals(-666, parseInt(Rational.valueOf(BigDecimal.valueOf(Long.MIN_VALUE, -10)), null, MathContext.DECIMAL128, -666));

		assertEquals(-666, parseInt(Rational.valueOf(Long.MAX_VALUE), null, MathContext.DECIMAL128, -666));
		assertEquals(-666, parseInt(Rational.valueOf(Long.MIN_VALUE), null, MathContext.DECIMAL128, -666));

		assertEquals(Integer.MAX_VALUE, parseInt(Rational.valueOf(Integer.MAX_VALUE), null, MathContext.DECIMAL128, -666));
		assertEquals(Integer.MIN_VALUE, parseInt(Rational.valueOf(Integer.MIN_VALUE), null, MathContext.DECIMAL128, -666));

		assertEquals(Short.MAX_VALUE, parseInt(Rational.valueOf(Short.MAX_VALUE), null, MathContext.DECIMAL128, -666));
		assertEquals(Short.MIN_VALUE, parseInt(Rational.valueOf(Short.MIN_VALUE), null, MathContext.DECIMAL128, -666));

		assertEquals(Byte.MAX_VALUE, parseInt(Rational.valueOf(Byte.MAX_VALUE), null, MathContext.DECIMAL128, -666));
		assertEquals(Byte.MIN_VALUE, parseInt(Rational.valueOf(Byte.MIN_VALUE), null, MathContext.DECIMAL128, -666));

		assertEquals(-666, parseInt(Rational.valueOf(3, 4), null, null, -666));

		// String
		assertEquals(0, parseInt("0", null, null, -666));
		assertEquals(1, parseInt("1", null, null, -666));
		assertEquals(-1, parseInt("-1", null, null, -666));

		assertEquals(0, parseInt("0.00 E14", null, null, -666));
		assertEquals(1, parseInt("0.01 E2", null, null, -666));
		assertEquals(-1, parseInt("-0.01 E2", null, null, -666));

		assertEquals(0, parseInt("0x0.00 E-14", null, null, -666));
		assertEquals(0, parseInt("0x0.00 P-14", null, null, -666));
		assertEquals(1, parseInt("0x0.01 E2", null, null, -666));
		assertEquals(1, parseInt("0x10 E-1", null, null, -666));
		assertEquals(1, parseInt("0x10 P-4", null, null, -666));
		assertEquals(-1, parseInt("-0x0.01 E2", null, null, -666));
		assertEquals(-1, parseInt("-0x10 E-1", null, null, -666));
		assertEquals(-1, parseInt("-0x10 P-4", null, null, -666));

		assertEquals(10, parseInt("0x0.0a E2", null, null, -666));
		assertEquals(10, parseInt("0xa0 E-1", null, null, -666));
		assertEquals(10, parseInt("0xa0 P-4", null, null, -666));
		assertEquals(-10, parseInt("-0x0.0a E2", null, null, -666));
		assertEquals(-10, parseInt("-0x0,0a E2", Locale.GERMAN, null, -666));
		assertEquals(-10, parseInt("-0xa0 E-1", null, null, -666));
		assertEquals(-10, parseInt("-0xa0p-4", null, null, -666));

		assertEquals(-666, parseInt("  34.45", null, null, -666));
		assertEquals(-666, parseInt("34.45 E1  ", null, null, -666));
		assertEquals(3445, parseInt("34.45E2", null, null, -666));
		assertEquals(3445, parseInt("34.45 E+2", null, null, -666));
		assertEquals(-666, parseInt("  34.45  E+2 ", null, null, -666));

		assertEquals(-666, parseInt("  34.45", Locale.GERMAN, null, -666));
		assertEquals(-666, parseInt("34.45 E1  ", Locale.GERMAN, null, -666));
		assertEquals(3445, parseInt("34.45E2", Locale.GERMAN, null, -666));
		assertEquals(3445, parseInt("34.45 E+2", Locale.GERMAN, null, -666));
		assertEquals(-666, parseInt("  34.45  E+2 ", Locale.GERMAN, null, -666));

		assertEquals(-666, parseInt("  34,45", Locale.GERMAN, null, -666));
		assertEquals(-666, parseInt("34,45 E1  ", Locale.GERMAN, null, -666));
		assertEquals(3445, parseInt("34,45E2", Locale.GERMAN, null, -666));
		assertEquals(3445, parseInt("34,45 E+2", Locale.GERMAN, null, -666));
		assertEquals(-666, parseInt("  34,45  E+2 ", Locale.GERMAN, null, -666));

		assertEquals(-666, parseInt(" 12.25 ", null, null, -666));
		assertEquals(-666, parseInt(" 12 .25 ", null, null, -666));
		assertEquals(-666, parseInt(" 12. 25 ", null, null, -666));

		assertEquals(-666, parseInt("  0x265712.2094325 P-5", null, null, -666));
		assertEquals(78521, parseInt("  0x265712.2094325 P-5", null, new MathContext(5, RoundingMode.HALF_EVEN), -666));
		assertEquals(643240000, parseInt("  0x265712.2094325 E2", null, new MathContext(5, RoundingMode.HALF_EVEN), -666));
		assertEquals(-666, parseInt("  0x0.2657122094325 EF", null, new MathContext(5, RoundingMode.DOWN), -666));
		assertEquals(-666, parseInt("  0x0.2657122094325 EF", null, new MathContext(5, RoundingMode.HALF_EVEN), -666));
		assertEquals(-666, parseInt("  0x0.2657122094325 EF", null, null, -666));
	}

	@Test
	public void parseIntRoundingOverloadTest() {
		// Invalid
		assertEquals(-666, parseInt(null, null, 0, null, -666));
		assertEquals(-666, parseInt(LoggerFactory.getILoggerFactory(), null, 0, null, -666));
		assertEquals(-666, parseInt(new StringBuilder("foobar"), null, 0, null, -666));
		assertEquals(-666, parseInt("foo 2 bar", null, 0, null, -666));
		assertEquals(-666, parseInt(" \t", null, 0, null, -666));
		assertEquals(-666, parseInt(" ", null, 0, null, -666));
		assertEquals(-666, parseInt("", null, 0, null, -666));

		// Number
		assertEquals(Byte.MAX_VALUE, parseInt(Integer.valueOf(Byte.MAX_VALUE), null, 0, null, -666));
		assertEquals(Byte.MAX_VALUE, parseInt(Long.valueOf(Byte.MAX_VALUE), null, 0, null, -666));
		assertEquals(Byte.MAX_VALUE, parseInt(Byte.valueOf(Byte.MAX_VALUE), null, 0, null, -666));
		assertEquals(Byte.MIN_VALUE, parseInt(Integer.valueOf(Byte.MIN_VALUE), null, 0, null, -666));
		assertEquals(Byte.MIN_VALUE, parseInt(Long.valueOf(Byte.MIN_VALUE), null, 0, null, -666));
		assertEquals(Byte.MIN_VALUE, parseInt(Byte.valueOf(Byte.MIN_VALUE), null, 0, null, -666));

		assertEquals(Short.MAX_VALUE, parseInt(Integer.valueOf(Short.MAX_VALUE), null, 0, null, -666));
		assertEquals(Short.MAX_VALUE, parseInt(Long.valueOf(Short.MAX_VALUE), null, 0, null, -666));
		assertEquals(Short.MAX_VALUE, parseInt(Short.valueOf(Short.MAX_VALUE), null, 0, null, -666));
		assertEquals(Short.MIN_VALUE, parseInt(Integer.valueOf(Short.MIN_VALUE), null, 0, null, -666));
		assertEquals(Short.MIN_VALUE, parseInt(Long.valueOf(Short.MIN_VALUE), null, 0, null, -666));
		assertEquals(Short.MIN_VALUE, parseInt(Short.valueOf(Short.MIN_VALUE), null, 0, null, -666));

		assertEquals(Integer.MAX_VALUE, parseInt(Integer.valueOf(Integer.MAX_VALUE), null, 0, null, -666));
		assertEquals(Integer.MAX_VALUE, parseInt(Long.valueOf(Integer.MAX_VALUE), null, 0, null, -666));
		assertEquals(Integer.MIN_VALUE, parseInt(Integer.valueOf(Integer.MIN_VALUE), null, 0, null, -666));
		assertEquals(Integer.MIN_VALUE, parseInt(Long.valueOf(Integer.MIN_VALUE), null, 0, null, -666));

		assertEquals(-666, parseInt(Long.valueOf(Long.MAX_VALUE), null, 0, null, -666));
		assertEquals(-666, parseInt(BigInteger.valueOf(Long.MAX_VALUE), null, 0, null, -666));
		assertEquals(-666, parseInt(Long.valueOf(Long.MIN_VALUE), null, 0, null, -666));
		assertEquals(-666, parseInt(BigInteger.valueOf(Long.MIN_VALUE), null, 0, null, -666));

		assertEquals(Integer.MAX_VALUE, parseInt(Long.valueOf(Integer.MAX_VALUE), null, 0, RoundingMode.HALF_UP, -666));
		assertEquals(Integer.MIN_VALUE, parseInt(Long.valueOf(Integer.MIN_VALUE), null, 0, RoundingMode.HALF_UP, -666));

		assertEquals(-666, parseInt(Long.valueOf(Long.MAX_VALUE), null, 0, RoundingMode.CEILING, -666));
		assertEquals(-666, parseInt(Long.valueOf(Long.MIN_VALUE), null, 0, RoundingMode.CEILING, -666));

		assertEquals(-666, parseInt(BigInteger.valueOf(Long.MAX_VALUE).multiply(BigInteger.TEN), null, 0, RoundingMode.CEILING, -666));
		assertEquals(-666, parseInt(BigInteger.valueOf(Long.MIN_VALUE).multiply(BigInteger.TEN), null, 0, RoundingMode.CEILING, -666));

		assertEquals(-666, parseInt(Double.valueOf(Double.MAX_VALUE), null, 0, RoundingMode.CEILING, -666));
		assertEquals(1, parseInt(Double.valueOf(Double.MIN_VALUE), null, 0, RoundingMode.CEILING, -666));
		assertEquals(0, parseInt(Double.valueOf(Double.MIN_VALUE), null, 0, RoundingMode.DOWN, -666));
		assertEquals(0, parseInt(Double.valueOf(Double.MIN_VALUE), null, 0, RoundingMode.FLOOR, -666));
		assertEquals(0, parseInt(Double.valueOf(Double.MIN_VALUE), null, 0, RoundingMode.HALF_DOWN, -666));
		assertEquals(0, parseInt(Double.valueOf(Double.MIN_VALUE), null, 0, RoundingMode.HALF_EVEN, -666));
		assertEquals(0, parseInt(Double.valueOf(Double.MIN_VALUE), null, 0, RoundingMode.HALF_UP, -666));
		assertEquals(1, parseInt(Double.valueOf(Double.MIN_VALUE), null, 0, RoundingMode.UP, -666));

		assertEquals(-666, parseInt(BigDecimal.valueOf(Long.MAX_VALUE, -10), null, 0, RoundingMode.CEILING, -666));
		assertEquals(-666, parseInt(BigDecimal.valueOf(Long.MIN_VALUE, -10), null, 0, RoundingMode.CEILING, -666));

		assertEquals(-666, parseInt(Rational.valueOf(BigDecimal.valueOf(Long.MAX_VALUE, -10)), null, 0, RoundingMode.CEILING, -666));
		assertEquals(-666, parseInt(Rational.valueOf(BigDecimal.valueOf(Long.MIN_VALUE, -10)), null, 0, RoundingMode.CEILING, -666));

		assertEquals(-666, parseInt(Rational.valueOf(Long.MAX_VALUE), null, 0, RoundingMode.CEILING, -666));
		assertEquals(-666, parseInt(Rational.valueOf(Long.MIN_VALUE), null, 0, RoundingMode.CEILING, -666));

		assertEquals(Integer.MAX_VALUE, parseInt(Rational.valueOf(Integer.MAX_VALUE), null, 0, RoundingMode.CEILING, -666));
		assertEquals(Integer.MIN_VALUE, parseInt(Rational.valueOf(Integer.MIN_VALUE), null, 0, RoundingMode.CEILING, -666));

		assertEquals(Short.MAX_VALUE, parseInt(Rational.valueOf(Short.MAX_VALUE), null, 0, RoundingMode.CEILING, -666));
		assertEquals(Short.MIN_VALUE, parseInt(Rational.valueOf(Short.MIN_VALUE), null, 0, RoundingMode.CEILING, -666));

		assertEquals(Byte.MAX_VALUE, parseInt(Rational.valueOf(Byte.MAX_VALUE), null, 0, RoundingMode.CEILING, -666));
		assertEquals(Byte.MIN_VALUE, parseInt(Rational.valueOf(Byte.MIN_VALUE), null, 0, RoundingMode.CEILING, -666));

		assertEquals(-666, parseInt(Rational.valueOf(3, 4), null, 0, null, -666));
		assertEquals(-666, parseInt(Rational.valueOf(3, 4), null, 1, RoundingMode.HALF_EVEN, -666));

		// String
		assertEquals(0, parseInt("0", null, 0, null, -666));
		assertEquals(1, parseInt("1", null, 0, null, -666));
		assertEquals(-1, parseInt("-1", null, 0, null, -666));

		assertEquals(0, parseInt("0.00 E14", null, 0, null, -666));
		assertEquals(1, parseInt("0.01 E2", null, 0, null, -666));
		assertEquals(-1, parseInt("-0.01 E2", null, 0, null, -666));

		assertEquals(0, parseInt("0x0.00 E-14", null, 0, null, -666));
		assertEquals(0, parseInt("0x0.00 P-14", null, 0, null, -666));
		assertEquals(1, parseInt("0x0.01 E2", null, 0, null, -666));
		assertEquals(1, parseInt("0x10 E-1", null, 0, null, -666));
		assertEquals(1, parseInt("0x10 P-4", null, 0, null, -666));
		assertEquals(-1, parseInt("-0x0.01 E2", null, 0, null, -666));
		assertEquals(-1, parseInt("-0x10 E-1", null, 0, null, -666));
		assertEquals(-1, parseInt("-0x10 P-4", null, 0, null, -666));

		assertEquals(10, parseInt("0x0.0a E2", null, 0, null, -666));
		assertEquals(10, parseInt("0xa0 E-1", null, 0, null, -666));
		assertEquals(10, parseInt("0xa0 P-4", null, 0, null, -666));
		assertEquals(-10, parseInt("-0x0.0a E2", null, 0, null, -666));
		assertEquals(-10, parseInt("-0x0,0a E2", Locale.GERMAN, 0, null, -666));
		assertEquals(-10, parseInt("-0xa0 E-1", null, 0, null, -666));
		assertEquals(-10, parseInt("-0xa0p-4", null, 0, null, -666));

		assertEquals(-666, parseInt("  34.45", null, 0, null, -666));
		assertEquals(-666, parseInt("34.45 E1  ", null, 0, null, -666));
		assertEquals(3445, parseInt("34.45E2", null, 0, null, -666));
		assertEquals(3445, parseInt("34.45 E+2", null, 0, null, -666));
		assertEquals(-666, parseInt("  34.45  E+2 ", null, 0, null, -666));

		assertEquals(-666, parseInt("  34.45", Locale.GERMAN, 0, null, -666));
		assertEquals(-666, parseInt("34.45 E1  ", Locale.GERMAN, 0, null, -666));
		assertEquals(3445, parseInt("34.45E2", Locale.GERMAN, 0, null, -666));
		assertEquals(3445, parseInt("34.45 E+2", Locale.GERMAN, 0, null, -666));
		assertEquals(-666, parseInt("  34.45  E+2 ", Locale.GERMAN, 0, null, -666));

		assertEquals(-666, parseInt("  34,45", Locale.GERMAN, 0, null, -666));
		assertEquals(-666, parseInt("34,45 E1  ", Locale.GERMAN, 0, null, -666));
		assertEquals(3445, parseInt("34,45E2", Locale.GERMAN, 0, null, -666));
		assertEquals(3445, parseInt("34,45 E+2", Locale.GERMAN, 0, null, -666));
		assertEquals(-666, parseInt("  34,45  E+2 ", Locale.GERMAN, 0, null, -666));

		assertEquals(-666, parseInt(" 12.25 ", null, 0, null, -666));
		assertEquals(-666, parseInt(" 12 .25 ", null, 0, null, -666));
		assertEquals(-666, parseInt(" 12. 25 ", null, 0, null, -666));

		assertEquals(-666, parseInt("  0x265712.2094325 P-5", null, 0, null, -666));
		assertEquals(-666, parseInt("  0x0.2657122094325 EF", null, 0, null, -666));
		assertEquals(-666, parseInt("  0x0.2657122094325 EF", null, -3, RoundingMode.HALF_UP, -666));
		assertEquals(-666, parseInt("  0x0.2657122094325 EF", null, -3, RoundingMode.HALF_EVEN, -666));
	}

	@Test
	public void parseLongNoRoundingTest() {
		// Invalid
		assertEquals(-666L, parseLong(null, -666L));
		assertEquals(-666L, parseLong(LoggerFactory.getILoggerFactory(), -666L));
		assertEquals(-666L, parseLong(new StringBuilder("foobar"), -666L));
		assertEquals(-666L, parseLong("foo 2 bar", -666L));
		assertEquals(-666L, parseLong(" \t", -666L));
		assertEquals(-666L, parseLong(" ", -666L));
		assertEquals(-666L, parseLong("", -666L));

		// Number
		assertEquals(Byte.MAX_VALUE, parseLong(Integer.valueOf(Byte.MAX_VALUE), -666L));
		assertEquals(Byte.MAX_VALUE, parseLong(Long.valueOf(Byte.MAX_VALUE), -666L));
		assertEquals(Byte.MAX_VALUE, parseLong(Byte.valueOf(Byte.MAX_VALUE), -666L));
		assertEquals(Byte.MIN_VALUE, parseLong(Integer.valueOf(Byte.MIN_VALUE), -666L));
		assertEquals(Byte.MIN_VALUE, parseLong(Long.valueOf(Byte.MIN_VALUE), -666L));
		assertEquals(Byte.MIN_VALUE, parseLong(Byte.valueOf(Byte.MIN_VALUE), -666L));

		assertEquals(Short.MAX_VALUE, parseLong(Integer.valueOf(Short.MAX_VALUE), -666L));
		assertEquals(Short.MAX_VALUE, parseLong(Long.valueOf(Short.MAX_VALUE), -666L));
		assertEquals(Short.MAX_VALUE, parseLong(Short.valueOf(Short.MAX_VALUE), -666L));
		assertEquals(Short.MIN_VALUE, parseLong(Integer.valueOf(Short.MIN_VALUE), -666L));
		assertEquals(Short.MIN_VALUE, parseLong(Long.valueOf(Short.MIN_VALUE), -666L));
		assertEquals(Short.MIN_VALUE, parseLong(Short.valueOf(Short.MIN_VALUE), -666L));

		assertEquals(Integer.MAX_VALUE, parseLong(Integer.valueOf(Integer.MAX_VALUE), -666L));
		assertEquals(Integer.MAX_VALUE, parseLong(Long.valueOf(Integer.MAX_VALUE), -666L));
		assertEquals(Integer.MIN_VALUE, parseLong(Integer.valueOf(Integer.MIN_VALUE), -666L));
		assertEquals(Integer.MIN_VALUE, parseLong(Long.valueOf(Integer.MIN_VALUE), -666L));

		assertEquals(Long.MAX_VALUE, parseLong(Long.valueOf(Long.MAX_VALUE), -666L));
		assertEquals(Long.MAX_VALUE, parseLong(BigInteger.valueOf(Long.MAX_VALUE), -666L));
		assertEquals(Long.MIN_VALUE, parseLong(Long.valueOf(Long.MIN_VALUE), -666L));
		assertEquals(Long.MIN_VALUE, parseLong(BigInteger.valueOf(Long.MIN_VALUE), -666L));

		assertEquals(Integer.MAX_VALUE, parseLong(Long.valueOf(Integer.MAX_VALUE), -666L));
		assertEquals(Integer.MIN_VALUE, parseLong(Long.valueOf(Integer.MIN_VALUE), -666L));

		assertEquals(Long.MAX_VALUE, parseLong(Long.valueOf(Long.MAX_VALUE), -666L));
		assertEquals(Long.MIN_VALUE, parseLong(Long.valueOf(Long.MIN_VALUE), -666L));

		assertEquals(-666L, parseLong(BigInteger.valueOf(Long.MAX_VALUE).multiply(BigInteger.TEN), -666L));
		assertEquals(-666L, parseLong(BigInteger.valueOf(Long.MIN_VALUE).multiply(BigInteger.TEN), -666L));

		assertEquals(-666L, parseLong(Double.valueOf(Double.MAX_VALUE), -666L));
		assertEquals(-666L, parseLong(Double.valueOf(Double.MIN_VALUE), -666L));

		assertEquals(-666L, parseLong(BigDecimal.valueOf(Long.MAX_VALUE, -10), -666L));
		assertEquals(-666L, parseLong(BigDecimal.valueOf(Long.MIN_VALUE, -10), -666L));

		assertEquals(-666L, parseLong(Rational.valueOf(BigDecimal.valueOf(Long.MAX_VALUE, -10)), -666L));
		assertEquals(-666L, parseLong(Rational.valueOf(BigDecimal.valueOf(Long.MIN_VALUE, -10)), -666L));

		assertEquals(Long.MAX_VALUE, parseLong(Rational.valueOf(Long.MAX_VALUE), -666L));
		assertEquals(Long.MIN_VALUE, parseLong(Rational.valueOf(Long.MIN_VALUE), -666L));

		assertEquals(Integer.MAX_VALUE, parseLong(Rational.valueOf(Integer.MAX_VALUE), -666L));
		assertEquals(Integer.MIN_VALUE, parseLong(Rational.valueOf(Integer.MIN_VALUE), -666L));

		assertEquals(Short.MAX_VALUE, parseLong(Rational.valueOf(Short.MAX_VALUE), -666L));
		assertEquals(Short.MIN_VALUE, parseLong(Rational.valueOf(Short.MIN_VALUE), -666L));

		assertEquals(Byte.MAX_VALUE, parseLong(Rational.valueOf(Byte.MAX_VALUE), -666L));
		assertEquals(Byte.MIN_VALUE, parseLong(Rational.valueOf(Byte.MIN_VALUE), -666L));

		assertEquals(-666L, parseLong(Rational.valueOf(3, 4), -666L));

		// String
		assertEquals(0L, parseLong("0", -666L));
		assertEquals(1L, parseLong("1", -666L));
		assertEquals(-1L, parseLong("-1", -666L));

		assertEquals(0L, parseLong("0.00 E14", -666L));
		assertEquals(1L, parseLong("0.01 E2", -666L));
		assertEquals(-1L, parseLong("-0.01 E2", -666L));

		assertEquals(0L, parseLong("0x0.00 E-14", -666L));
		assertEquals(0L, parseLong("0x0.00 P-14", -666L));
		assertEquals(1L, parseLong("0x0.01 E2", -666L));
		assertEquals(1L, parseLong("0x10 E-1", -666L));
		assertEquals(1L, parseLong("0x10 P-4", -666L));
		assertEquals(-1L, parseLong("-0x0.01 E2", -666L));
		assertEquals(-1L, parseLong("-0x10 E-1", -666L));
		assertEquals(-1L, parseLong("-0x10 P-4", -666L));

		assertEquals(10L, parseLong("0x0.0a E2", -666L));
		assertEquals(10L, parseLong("0xa0 E-1", -666L));
		assertEquals(10L, parseLong("0xa0 P-4", -666L));
		assertEquals(-10L, parseLong("-0x0.0a E2", -666L));
		assertEquals(-10L, parseLong("-0xa0 E-1", -666L));
		assertEquals(-10L, parseLong("-0xa0p-4", -666L));

		assertEquals(-666L, parseLong("  34.45", -666L));
		assertEquals(-666L, parseLong("34.45 E1  ", -666L));
		assertEquals(3445L, parseLong("34.45E2", -666L));
		assertEquals(3445L, parseLong("34.45 E+2", -666L));
		assertEquals(-666L, parseLong("  34.45  E+2 ", -666L));

		assertEquals(-666L, parseLong(" 12.25 ", -666L));
		assertEquals(-666L, parseLong(" 12 .25 ", -666L));
		assertEquals(-666L, parseLong(" 12. 25 ", -666L));

		assertEquals(-666L, parseLong("  0x265712.2094325 P-5", -666L));
		assertEquals(-666L, parseLong("  0x265712.2094325 E2", -666L));
		assertEquals(172668551721854208L, parseLong("  0x0.2657122094325 EF", -666L));
	}

	@Test
	public void parseLongMathContextOverloadTest() {
		// Invalid
		assertEquals(-666L, parseLong(null, null, null, -666L));
		assertEquals(-666L, parseLong(LoggerFactory.getILoggerFactory(), null, null, -666L));
		assertEquals(-666L, parseLong(new StringBuilder("foobar"), null, null, -666L));
		assertEquals(-666L, parseLong("foo 2 bar", null, null, -666L));
		assertEquals(-666L, parseLong(" \t", null, null, -666L));
		assertEquals(-666L, parseLong(" ", null, null, -666L));
		assertEquals(-666L, parseLong("", null, null, -666L));

		// Number
		assertEquals(Byte.MAX_VALUE, parseLong(Integer.valueOf(Byte.MAX_VALUE), null, null, -666L));
		assertEquals(Byte.MAX_VALUE, parseLong(Long.valueOf(Byte.MAX_VALUE), null, null, -666L));
		assertEquals(Byte.MAX_VALUE, parseLong(Byte.valueOf(Byte.MAX_VALUE), null, null, -666L));
		assertEquals(Byte.MIN_VALUE, parseLong(Integer.valueOf(Byte.MIN_VALUE), null, null, -666L));
		assertEquals(Byte.MIN_VALUE, parseLong(Long.valueOf(Byte.MIN_VALUE), null, null, -666L));
		assertEquals(Byte.MIN_VALUE, parseLong(Byte.valueOf(Byte.MIN_VALUE), null, null, -666L));

		assertEquals(Short.MAX_VALUE, parseLong(Integer.valueOf(Short.MAX_VALUE), null, null, -666L));
		assertEquals(Short.MAX_VALUE, parseLong(Long.valueOf(Short.MAX_VALUE), null, null, -666L));
		assertEquals(Short.MAX_VALUE, parseLong(Short.valueOf(Short.MAX_VALUE), null, null, -666L));
		assertEquals(Short.MIN_VALUE, parseLong(Integer.valueOf(Short.MIN_VALUE), null, null, -666L));
		assertEquals(Short.MIN_VALUE, parseLong(Long.valueOf(Short.MIN_VALUE), null, null, -666L));
		assertEquals(Short.MIN_VALUE, parseLong(Short.valueOf(Short.MIN_VALUE), null, null, -666L));

		assertEquals(Integer.MAX_VALUE, parseLong(Integer.valueOf(Integer.MAX_VALUE), null, null, -666L));
		assertEquals(Integer.MAX_VALUE, parseLong(Long.valueOf(Integer.MAX_VALUE), null, null, -666L));
		assertEquals(Integer.MIN_VALUE, parseLong(Integer.valueOf(Integer.MIN_VALUE), null, null, -666L));
		assertEquals(Integer.MIN_VALUE, parseLong(Long.valueOf(Integer.MIN_VALUE), null, null, -666L));

		assertEquals(Long.MAX_VALUE, parseLong(Long.valueOf(Long.MAX_VALUE), null, null, -666L));
		assertEquals(Long.MAX_VALUE, parseLong(BigInteger.valueOf(Long.MAX_VALUE), null, null, -666L));
		assertEquals(Long.MIN_VALUE, parseLong(Long.valueOf(Long.MIN_VALUE), null, null, -666L));
		assertEquals(Long.MIN_VALUE, parseLong(BigInteger.valueOf(Long.MIN_VALUE), null, null, -666L));

		assertEquals(Integer.MAX_VALUE, parseLong(Long.valueOf(Integer.MAX_VALUE), null, MathContext.DECIMAL64, -666L));
		assertEquals(Integer.MIN_VALUE, parseLong(Long.valueOf(Integer.MIN_VALUE), null, MathContext.DECIMAL64, -666L));

		assertEquals(Long.MAX_VALUE, parseLong(Long.valueOf(Long.MAX_VALUE), null, MathContext.DECIMAL128, -666L));
		assertEquals(Long.MIN_VALUE, parseLong(Long.valueOf(Long.MIN_VALUE), null, MathContext.DECIMAL128, -666L));

		assertEquals(-666L, parseLong(BigInteger.valueOf(Long.MAX_VALUE).multiply(BigInteger.TEN), null, MathContext.DECIMAL128, -666L));
		assertEquals(-666L, parseLong(BigInteger.valueOf(Long.MIN_VALUE).multiply(BigInteger.TEN), null, MathContext.DECIMAL128, -666L));

		assertEquals(-666L, parseLong(Double.valueOf(Double.MAX_VALUE), null, MathContext.DECIMAL128, -666L));
		assertEquals(-666L, parseLong(Double.valueOf(Double.MIN_VALUE), null, MathContext.DECIMAL128, -666L));

		assertEquals(-666L, parseLong(BigDecimal.valueOf(Long.MAX_VALUE, -10), null, MathContext.DECIMAL128, -666L));
		assertEquals(-666L, parseLong(BigDecimal.valueOf(Long.MIN_VALUE, -10), null, MathContext.DECIMAL128, -666L));

		assertEquals(-666L, parseLong(Rational.valueOf(BigDecimal.valueOf(Long.MAX_VALUE, -10)), null, MathContext.DECIMAL128, -666L));
		assertEquals(-666L, parseLong(Rational.valueOf(BigDecimal.valueOf(Long.MIN_VALUE, -10)), null, MathContext.DECIMAL128, -666L));

		assertEquals(Long.MAX_VALUE, parseLong(Rational.valueOf(Long.MAX_VALUE), null, MathContext.DECIMAL128, -666L));
		assertEquals(Long.MIN_VALUE, parseLong(Rational.valueOf(Long.MIN_VALUE), null, MathContext.DECIMAL128, -666L));

		assertEquals(Integer.MAX_VALUE, parseLong(Rational.valueOf(Integer.MAX_VALUE), null, MathContext.DECIMAL128, -666L));
		assertEquals(Integer.MIN_VALUE, parseLong(Rational.valueOf(Integer.MIN_VALUE), null, MathContext.DECIMAL128, -666L));

		assertEquals(Short.MAX_VALUE, parseLong(Rational.valueOf(Short.MAX_VALUE), null, MathContext.DECIMAL128, -666L));
		assertEquals(Short.MIN_VALUE, parseLong(Rational.valueOf(Short.MIN_VALUE), null, MathContext.DECIMAL128, -666L));

		assertEquals(Byte.MAX_VALUE, parseLong(Rational.valueOf(Byte.MAX_VALUE), null, MathContext.DECIMAL128, -666L));
		assertEquals(Byte.MIN_VALUE, parseLong(Rational.valueOf(Byte.MIN_VALUE), null, MathContext.DECIMAL128, -666L));

		assertEquals(-666L, parseLong(Rational.valueOf(3, 4), null, null, -666L));

		// String
		assertEquals(0L, parseLong("0", null, null, -666L));
		assertEquals(1L, parseLong("1", null, null, -666L));
		assertEquals(-1L, parseLong("-1", null, null, -666L));

		assertEquals(0L, parseLong("0.00 E14", null, null, -666L));
		assertEquals(1L, parseLong("0.01 E2", null, null, -666L));
		assertEquals(-1L, parseLong("-0.01 E2", null, null, -666L));

		assertEquals(0L, parseLong("0x0.00 E-14", null, null, -666L));
		assertEquals(0L, parseLong("0x0.00 P-14", null, null, -666L));
		assertEquals(1L, parseLong("0x0.01 E2", null, null, -666L));
		assertEquals(1L, parseLong("0x10 E-1", null, null, -666L));
		assertEquals(1L, parseLong("0x10 P-4", null, null, -666L));
		assertEquals(-1L, parseLong("-0x0.01 E2", null, null, -666L));
		assertEquals(-1L, parseLong("-0x10 E-1", null, null, -666L));
		assertEquals(-1L, parseLong("-0x10 P-4", null, null, -666L));

		assertEquals(10L, parseLong("0x0.0a E2", null, null, -666L));
		assertEquals(10L, parseLong("0xa0 E-1", null, null, -666L));
		assertEquals(10L, parseLong("0xa0 P-4", null, null, -666L));
		assertEquals(-10L, parseLong("-0x0.0a E2", null, null, -666L));
		assertEquals(-10L, parseLong("-0x0,0a E2", Locale.GERMAN, null, -666L));
		assertEquals(-10L, parseLong("-0xa0 E-1", null, null, -666L));
		assertEquals(-10L, parseLong("-0xa0p-4", null, null, -666L));

		assertEquals(-666L, parseLong("  34.45", null, null, -666L));
		assertEquals(-666L, parseLong("34.45 E1  ", null, null, -666L));
		assertEquals(3445L, parseLong("34.45E2", null, null, -666L));
		assertEquals(3445L, parseLong("34.45 E+2", null, null, -666L));
		assertEquals(-666L, parseLong("  34.45  E+2 ", null, null, -666L));

		assertEquals(-666L, parseLong("  34.45", Locale.GERMAN, null, -666L));
		assertEquals(-666L, parseLong("34.45 E1  ", Locale.GERMAN, null, -666L));
		assertEquals(3445L, parseLong("34.45E2", Locale.GERMAN, null, -666L));
		assertEquals(3445L, parseLong("34.45 E+2", Locale.GERMAN, null, -666L));
		assertEquals(-666L, parseLong("  34.45  E+2 ", Locale.GERMAN, null, -666L));

		assertEquals(-666L, parseLong("  34,45", Locale.GERMAN, null, -666L));
		assertEquals(-666L, parseLong("34,45 E1  ", Locale.GERMAN, null, -666L));
		assertEquals(3445L, parseLong("34,45E2", Locale.GERMAN, null, -666L));
		assertEquals(3445L, parseLong("34,45 E+2", Locale.GERMAN, null, -666L));
		assertEquals(-666L, parseLong("  34,45  E+2 ", Locale.GERMAN, null, -666L));

		assertEquals(-666L, parseLong(" 12.25 ", null, null, -666L));
		assertEquals(-666L, parseLong(" 12 .25 ", null, null, -666L));
		assertEquals(-666L, parseLong(" 12. 25 ", null, null, -666L));

		assertEquals(-666L, parseLong("  0x265712.2094325 P-5", null, null, -666L));
		assertEquals(78521L, parseLong("  0x265712.2094325 P-5", null, new MathContext(5, RoundingMode.HALF_EVEN), -666L));
		assertEquals(643240000L, parseLong("  0x265712.2094325 E2", null, new MathContext(5, RoundingMode.HALF_EVEN), -666L));
		assertEquals(172660000000000000L, parseLong("  0x0.2657122094325 EF", null, new MathContext(5, RoundingMode.DOWN), -666L));
		assertEquals(172670000000000000L, parseLong("  0x0.2657122094325 EF", null, new MathContext(5, RoundingMode.HALF_EVEN), -666L));
		assertEquals(172668551721854208L, parseLong("  0x0.2657122094325 EF", null, null, -666L));
	}

	@Test
	public void parseLongRoundingOverloadTest() {
		// Invalid
		assertEquals(-666L, parseLong(null, null, 0, null, -666L));
		assertEquals(-666L, parseLong(LoggerFactory.getILoggerFactory(), null, 0, null, -666L));
		assertEquals(-666L, parseLong(new StringBuilder("foobar"), null, 0, null, -666L));
		assertEquals(-666L, parseLong("foo 2 bar", null, 0, null, -666L));
		assertEquals(-666L, parseLong(" \t", null, 0, null, -666L));
		assertEquals(-666L, parseLong(" ", null, 0, null, -666L));
		assertEquals(-666L, parseLong("", null, 0, null, -666L));

		// Number
		assertEquals(Byte.MAX_VALUE, parseLong(Integer.valueOf(Byte.MAX_VALUE), null, 0, null, -666L));
		assertEquals(Byte.MAX_VALUE, parseLong(Long.valueOf(Byte.MAX_VALUE), null, 0, null, -666L));
		assertEquals(Byte.MAX_VALUE, parseLong(Byte.valueOf(Byte.MAX_VALUE), null, 0, null, -666L));
		assertEquals(Byte.MIN_VALUE, parseLong(Integer.valueOf(Byte.MIN_VALUE), null, 0, null, -666L));
		assertEquals(Byte.MIN_VALUE, parseLong(Long.valueOf(Byte.MIN_VALUE), null, 0, null, -666L));
		assertEquals(Byte.MIN_VALUE, parseLong(Byte.valueOf(Byte.MIN_VALUE), null, 0, null, -666L));

		assertEquals(Short.MAX_VALUE, parseLong(Integer.valueOf(Short.MAX_VALUE), null, 0, null, -666L));
		assertEquals(Short.MAX_VALUE, parseLong(Long.valueOf(Short.MAX_VALUE), null, 0, null, -666L));
		assertEquals(Short.MAX_VALUE, parseLong(Short.valueOf(Short.MAX_VALUE), null, 0, null, -666L));
		assertEquals(Short.MIN_VALUE, parseLong(Integer.valueOf(Short.MIN_VALUE), null, 0, null, -666L));
		assertEquals(Short.MIN_VALUE, parseLong(Long.valueOf(Short.MIN_VALUE), null, 0, null, -666L));
		assertEquals(Short.MIN_VALUE, parseLong(Short.valueOf(Short.MIN_VALUE), null, 0, null, -666L));

		assertEquals(Integer.MAX_VALUE, parseLong(Integer.valueOf(Integer.MAX_VALUE), null, 0, null, -666L));
		assertEquals(Integer.MAX_VALUE, parseLong(Long.valueOf(Integer.MAX_VALUE), null, 0, null, -666L));
		assertEquals(Integer.MIN_VALUE, parseLong(Integer.valueOf(Integer.MIN_VALUE), null, 0, null, -666L));
		assertEquals(Integer.MIN_VALUE, parseLong(Long.valueOf(Integer.MIN_VALUE), null, 0, null, -666L));

		assertEquals(Long.MAX_VALUE, parseLong(Long.valueOf(Long.MAX_VALUE), null, 0, null, -666L));
		assertEquals(Long.MAX_VALUE, parseLong(BigInteger.valueOf(Long.MAX_VALUE), null, 0, null, -666L));
		assertEquals(Long.MIN_VALUE, parseLong(Long.valueOf(Long.MIN_VALUE), null, 0, null, -666L));
		assertEquals(Long.MIN_VALUE, parseLong(BigInteger.valueOf(Long.MIN_VALUE), null, 0, null, -666L));

		assertEquals(Integer.MAX_VALUE, parseLong(Long.valueOf(Integer.MAX_VALUE), null, 0, RoundingMode.HALF_UP, -666L));
		assertEquals(Integer.MIN_VALUE, parseLong(Long.valueOf(Integer.MIN_VALUE), null, 0, RoundingMode.HALF_UP, -666L));

		assertEquals(Long.MAX_VALUE, parseLong(Long.valueOf(Long.MAX_VALUE), null, 0, RoundingMode.CEILING, -666L));
		assertEquals(Long.MIN_VALUE, parseLong(Long.valueOf(Long.MIN_VALUE), null, 0, RoundingMode.CEILING, -666L));

		assertEquals(-666L, parseLong(BigInteger.valueOf(Long.MAX_VALUE).multiply(BigInteger.TEN), null, 0, RoundingMode.CEILING, -666L));
		assertEquals(-666L, parseLong(BigInteger.valueOf(Long.MIN_VALUE).multiply(BigInteger.TEN), null, 0, RoundingMode.CEILING, -666L));

		assertEquals(-666L, parseLong(Double.valueOf(Double.MAX_VALUE), null, 0, RoundingMode.CEILING, -666L));
		assertEquals(1L, parseLong(Double.valueOf(Double.MIN_VALUE), null, 0, RoundingMode.CEILING, -666L));
		assertEquals(0L, parseLong(Double.valueOf(Double.MIN_VALUE), null, 0, RoundingMode.DOWN, -666L));
		assertEquals(0L, parseLong(Double.valueOf(Double.MIN_VALUE), null, 0, RoundingMode.FLOOR, -666L));
		assertEquals(0L, parseLong(Double.valueOf(Double.MIN_VALUE), null, 0, RoundingMode.HALF_DOWN, -666L));
		assertEquals(0L, parseLong(Double.valueOf(Double.MIN_VALUE), null, 0, RoundingMode.HALF_EVEN, -666L));
		assertEquals(0L, parseLong(Double.valueOf(Double.MIN_VALUE), null, 0, RoundingMode.HALF_UP, -666L));
		assertEquals(1L, parseLong(Double.valueOf(Double.MIN_VALUE), null, 0, RoundingMode.UP, -666L));

		assertEquals(-666L, parseLong(BigDecimal.valueOf(Long.MAX_VALUE, -10), null, 0, RoundingMode.CEILING, -666L));
		assertEquals(-666L, parseLong(BigDecimal.valueOf(Long.MIN_VALUE, -10), null, 0, RoundingMode.CEILING, -666L));

		assertEquals(-666L, parseLong(Rational.valueOf(BigDecimal.valueOf(Long.MAX_VALUE, -10)), null, 0, RoundingMode.CEILING, -666L));
		assertEquals(-666L, parseLong(Rational.valueOf(BigDecimal.valueOf(Long.MIN_VALUE, -10)), null, 0, RoundingMode.CEILING, -666L));

		assertEquals(Long.MAX_VALUE, parseLong(Rational.valueOf(Long.MAX_VALUE), null, 0, RoundingMode.CEILING, -666L));
		assertEquals(Long.MIN_VALUE, parseLong(Rational.valueOf(Long.MIN_VALUE), null, 0, RoundingMode.CEILING, -666L));

		assertEquals(Integer.MAX_VALUE, parseLong(Rational.valueOf(Integer.MAX_VALUE), null, 0, RoundingMode.CEILING, -666L));
		assertEquals(Integer.MIN_VALUE, parseLong(Rational.valueOf(Integer.MIN_VALUE), null, 0, RoundingMode.CEILING, -666L));

		assertEquals(Short.MAX_VALUE, parseLong(Rational.valueOf(Short.MAX_VALUE), null, 0, RoundingMode.CEILING, -666L));
		assertEquals(Short.MIN_VALUE, parseLong(Rational.valueOf(Short.MIN_VALUE), null, 0, RoundingMode.CEILING, -666L));

		assertEquals(Byte.MAX_VALUE, parseLong(Rational.valueOf(Byte.MAX_VALUE), null, 0, RoundingMode.CEILING, -666L));
		assertEquals(Byte.MIN_VALUE, parseLong(Rational.valueOf(Byte.MIN_VALUE), null, 0, RoundingMode.CEILING, -666L));

		assertEquals(-666L, parseLong(Rational.valueOf(3, 4), null, 0, null, -666L));
		assertEquals(-666L, parseLong(Rational.valueOf(3, 4), null, 1, RoundingMode.HALF_EVEN, -666L));

		// String
		assertEquals(0L, parseLong("0", null, 0, null, -666L));
		assertEquals(1L, parseLong("1", null, 0, null, -666L));
		assertEquals(-1L, parseLong("-1", null, 0, null, -666L));

		assertEquals(0L, parseLong("0.00 E14", null, 0, null, -666L));
		assertEquals(1L, parseLong("0.01 E2", null, 0, null, -666L));
		assertEquals(-1L, parseLong("-0.01 E2", null, 0, null, -666L));

		assertEquals(0L, parseLong("0x0.00 E-14", null, 0, null, -666L));
		assertEquals(0L, parseLong("0x0.00 P-14", null, 0, null, -666L));
		assertEquals(1L, parseLong("0x0.01 E2", null, 0, null, -666L));
		assertEquals(1L, parseLong("0x10 E-1", null, 0, null, -666L));
		assertEquals(1L, parseLong("0x10 P-4", null, 0, null, -666L));
		assertEquals(-1L, parseLong("-0x0.01 E2", null, 0, null, -666L));
		assertEquals(-1L, parseLong("-0x10 E-1", null, 0, null, -666L));
		assertEquals(-1L, parseLong("-0x10 P-4", null, 0, null, -666L));

		assertEquals(10L, parseLong("0x0.0a E2", null, 0, null, -666L));
		assertEquals(10L, parseLong("0xa0 E-1", null, 0, null, -666L));
		assertEquals(10L, parseLong("0xa0 P-4", null, 0, null, -666L));
		assertEquals(-10L, parseLong("-0x0.0a E2", null, 0, null, -666L));
		assertEquals(-10L, parseLong("-0x0,0a E2", Locale.GERMAN, 0, null, -666L));
		assertEquals(-10L, parseLong("-0xa0 E-1", null, 0, null, -666L));
		assertEquals(-10L, parseLong("-0xa0p-4", null, 0, null, -666L));

		assertEquals(-666L, parseLong("  34.45", null, 0, null, -666L));
		assertEquals(-666L, parseLong("34.45 E1  ", null, 0, null, -666L));
		assertEquals(3445L, parseLong("34.45E2", null, 0, null, -666L));
		assertEquals(3445L, parseLong("34.45 E+2", null, 0, null, -666L));
		assertEquals(-666L, parseLong("  34.45  E+2 ", null, 0, null, -666L));

		assertEquals(-666L, parseLong("  34.45", Locale.GERMAN, 0, null, -666L));
		assertEquals(-666L, parseLong("34.45 E1  ", Locale.GERMAN, 0, null, -666L));
		assertEquals(3445L, parseLong("34.45E2", Locale.GERMAN, 0, null, -666L));
		assertEquals(3445L, parseLong("34.45 E+2", Locale.GERMAN, 0, null, -666L));
		assertEquals(-666L, parseLong("  34.45  E+2 ", Locale.GERMAN, 0, null, -666L));

		assertEquals(-666L, parseLong("  34,45", Locale.GERMAN, 0, null, -666L));
		assertEquals(-666L, parseLong("34,45 E1  ", Locale.GERMAN, 0, null, -666L));
		assertEquals(3445L, parseLong("34,45E2", Locale.GERMAN, 0, null, -666L));
		assertEquals(3445L, parseLong("34,45 E+2", Locale.GERMAN, 0, null, -666L));
		assertEquals(-666L, parseLong("  34,45  E+2 ", Locale.GERMAN, 0, null, -666L));

		assertEquals(-666L, parseLong(" 12.25 ", null, 0, null, -666L));
		assertEquals(-666L, parseLong(" 12 .25 ", null, 0, null, -666L));
		assertEquals(-666L, parseLong(" 12. 25 ", null, 0, null, -666L));

		assertEquals(-666L, parseLong("  0x265712.2094325 P-5", null, 0, null, -666L));
		assertEquals(172668551721854208L, parseLong("  0x0.2657122094325 EF", null, 0, null, -666L));
		assertEquals(172668551721854000L, parseLong("  0x0.2657122094325 EF", null, -3, RoundingMode.HALF_UP, -666L));
		assertEquals(172668551721854000L, parseLong("  0x0.2657122094325 EF", null, -3, RoundingMode.HALF_EVEN, -666L));
	}

	@Test
	public void parseDoubleNoRoundingTest() {
		// Invalid
		assertEquals(Double.NaN, parseDouble(null, null, Double.NaN), 0.0);
		assertEquals(Double.NaN, parseDouble(LoggerFactory.getILoggerFactory(), null, Double.NaN), 0.0);
		assertEquals(Double.NaN, parseDouble(new StringBuilder("foobar"), null, Double.NaN), 0.0);
		assertEquals(Double.NaN, parseDouble("foo 2 bar", null, Double.NaN), 0.0);
		assertEquals(Double.NaN, parseDouble(" \t", null, Double.NaN), 0.0);
		assertEquals(Double.NaN, parseDouble(" ", null, Double.NaN), 0.0);
		assertEquals(Double.NaN, parseDouble("", null, Double.NaN), 0.0);

		// Number
		assertEquals(Byte.MAX_VALUE, parseDouble(Integer.valueOf(Byte.MAX_VALUE), null, Double.NaN), 0.0);
		assertEquals(Byte.MAX_VALUE, parseDouble(Long.valueOf(Byte.MAX_VALUE), null, Double.NaN), 0.0);
		assertEquals(Byte.MAX_VALUE, parseDouble(Byte.valueOf(Byte.MAX_VALUE), null, Double.NaN), 0.0);
		assertEquals(Byte.MIN_VALUE, parseDouble(Integer.valueOf(Byte.MIN_VALUE), null, Double.NaN), 0.0);
		assertEquals(Byte.MIN_VALUE, parseDouble(Long.valueOf(Byte.MIN_VALUE), null, Double.NaN), 0.0);
		assertEquals(Byte.MIN_VALUE, parseDouble(Byte.valueOf(Byte.MIN_VALUE), null, Double.NaN), 0.0);

		assertEquals(Short.MAX_VALUE, parseDouble(Integer.valueOf(Short.MAX_VALUE), null, Double.NaN), 0.0);
		assertEquals(Short.MAX_VALUE, parseDouble(Long.valueOf(Short.MAX_VALUE), null, Double.NaN), 0.0);
		assertEquals(Short.MAX_VALUE, parseDouble(Short.valueOf(Short.MAX_VALUE), null, Double.NaN), 0.0);
		assertEquals(Short.MIN_VALUE, parseDouble(Integer.valueOf(Short.MIN_VALUE), null, Double.NaN), 0.0);
		assertEquals(Short.MIN_VALUE, parseDouble(Long.valueOf(Short.MIN_VALUE), null, Double.NaN), 0.0);
		assertEquals(Short.MIN_VALUE, parseDouble(Short.valueOf(Short.MIN_VALUE), null, Double.NaN), 0.0);

		assertEquals(Integer.MAX_VALUE, parseDouble(Integer.valueOf(Integer.MAX_VALUE), null, Double.NaN), 0.0);
		assertEquals(Integer.MAX_VALUE, parseDouble(Long.valueOf(Integer.MAX_VALUE), null, Double.NaN), 0.0);
		assertEquals(Integer.MIN_VALUE, parseDouble(Integer.valueOf(Integer.MIN_VALUE), null, Double.NaN), 0.0);
		assertEquals(Integer.MIN_VALUE, parseDouble(Long.valueOf(Integer.MIN_VALUE), null, Double.NaN), 0.0);

		assertEquals(Long.MAX_VALUE, parseDouble(Long.valueOf(Long.MAX_VALUE), null, Double.NaN), 0.0);
		assertEquals(Long.MAX_VALUE, parseDouble(BigInteger.valueOf(Long.MAX_VALUE), null, Double.NaN), 0.0);
		assertEquals(Long.MIN_VALUE, parseDouble(Long.valueOf(Long.MIN_VALUE), null, Double.NaN), 0.0);
		assertEquals(Long.MIN_VALUE, parseDouble(BigInteger.valueOf(Long.MIN_VALUE), null, Double.NaN), 0.0);

		assertEquals(Integer.MAX_VALUE, parseDouble(Long.valueOf(Integer.MAX_VALUE), null, Double.NaN), 0.0);
		assertEquals(Integer.MIN_VALUE, parseDouble(Long.valueOf(Integer.MIN_VALUE), null, Double.NaN), 0.0);

		assertEquals(Long.MAX_VALUE, parseDouble(Long.valueOf(Long.MAX_VALUE), null, Double.NaN), 0.0);
		assertEquals(Long.MIN_VALUE, parseDouble(Long.valueOf(Long.MIN_VALUE), null, Double.NaN), 0.0);

		assertEquals(9.223372036854776E19, parseDouble(BigInteger.valueOf(Long.MAX_VALUE).multiply(BigInteger.TEN), null, Double.NaN), 0.0);
		assertEquals(-9.223372036854776E19, parseDouble(BigInteger.valueOf(Long.MIN_VALUE).multiply(BigInteger.TEN), null, Double.NaN), 0.0);

		assertEquals(Double.MAX_VALUE, parseDouble(Double.valueOf(Double.MAX_VALUE), null, Double.NaN), 0.0);
		assertEquals(Double.MIN_VALUE, parseDouble(Double.valueOf(Double.MIN_VALUE), null, Double.NaN), 0.0);

		assertEquals(9.223372036854776E28, parseDouble(BigDecimal.valueOf(Long.MAX_VALUE, -10), null, Double.NaN), 0.0);
		assertEquals(-9.223372036854776E28, parseDouble(BigDecimal.valueOf(Long.MIN_VALUE, -10), null, Double.NaN), 0.0);

		assertEquals(9.223372036854776E28, parseDouble(Rational.valueOf(BigDecimal.valueOf(Long.MAX_VALUE, -10)), null, Double.NaN), 0.0);
		assertEquals(-9.223372036854776E28, parseDouble(Rational.valueOf(BigDecimal.valueOf(Long.MIN_VALUE, -10)), null, Double.NaN), 0.0);

		assertEquals(Long.MAX_VALUE, parseDouble(Rational.valueOf(Long.MAX_VALUE), null, Double.NaN), 0.0);
		assertEquals(Long.MIN_VALUE, parseDouble(Rational.valueOf(Long.MIN_VALUE), null, Double.NaN), 0.0);

		assertEquals(Integer.MAX_VALUE, parseDouble(Rational.valueOf(Integer.MAX_VALUE), null, Double.NaN), 0.0);
		assertEquals(Integer.MIN_VALUE, parseDouble(Rational.valueOf(Integer.MIN_VALUE), null, Double.NaN), 0.0);

		assertEquals(Short.MAX_VALUE, parseDouble(Rational.valueOf(Short.MAX_VALUE), null, Double.NaN), 0.0);
		assertEquals(Short.MIN_VALUE, parseDouble(Rational.valueOf(Short.MIN_VALUE), null, Double.NaN), 0.0);

		assertEquals(Byte.MAX_VALUE, parseDouble(Rational.valueOf(Byte.MAX_VALUE), null, Double.NaN), 0.0);
		assertEquals(Byte.MIN_VALUE, parseDouble(Rational.valueOf(Byte.MIN_VALUE), null, Double.NaN), 0.0);

		assertEquals(0.75, parseDouble(Rational.valueOf(3, 4), null, Double.NaN), 0.0);

		// String
		assertEquals(0, parseDouble("0", null, Double.NaN), 0.0);
		assertEquals(1, parseDouble("1", null, Double.NaN), 0.0);
		assertEquals(-1, parseDouble("-1", null, Double.NaN), 0.0);

		assertEquals(0, parseDouble("0.00 E14", null, Double.NaN), 0.0);
		assertEquals(1, parseDouble("0.01 E2", null, Double.NaN), 0.0);
		assertEquals(-1, parseDouble("-0.01 E2", null, Double.NaN), 0.0);

		assertEquals(0, parseDouble("0x0.00 E-14", null, Double.NaN), 0.0);
		assertEquals(0, parseDouble("0x0.00 P-14", null, Double.NaN), 0.0);
		assertEquals(1, parseDouble("0x0.01 E2", null, Double.NaN), 0.0);
		assertEquals(1, parseDouble("0x10 E-1", null, Double.NaN), 0.0);
		assertEquals(1, parseDouble("0x10 P-4", null, Double.NaN), 0.0);
		assertEquals(-1, parseDouble("-0x0.01 E2", null, Double.NaN), 0.0);
		assertEquals(-1, parseDouble("-0x10 E-1", null, Double.NaN), 0.0);
		assertEquals(-1, parseDouble("-0x10 P-4", null, Double.NaN), 0.0);

		assertEquals(10, parseDouble("0x0.0a E2", null, Double.NaN), 0.0);
		assertEquals(10, parseDouble("0xa0 E-1", null, Double.NaN), 0.0);
		assertEquals(10, parseDouble("0xa0 P-4", null, Double.NaN), 0.0);
		assertEquals(-10, parseDouble("-0x0.0a E2", null, Double.NaN), 0.0);
		assertEquals(-10, parseDouble("-0x0,0a E2", Locale.GERMAN, Double.NaN), 0.0);
		assertEquals(-10, parseDouble("-0xa0 E-1", null, Double.NaN), 0.0);
		assertEquals(-10, parseDouble("-0xa0p-4", null, Double.NaN), 0.0);

		assertEquals(34.45, parseDouble("  34.45", null, Double.NaN), 0.0);
		assertEquals(344.5, parseDouble("34.45 E1  ", null, Double.NaN), 0.0);
		assertEquals(3445, parseDouble("34.45E2", null, Double.NaN), 0.0);
		assertEquals(3445, parseDouble("34.45 E+2", null, Double.NaN), 0.0);
		assertEquals(Double.NaN, parseDouble("  34.45  E+2 ", null, Double.NaN), 0.0);

		assertEquals(34.45, parseDouble("  34.45", Locale.GERMAN, Double.NaN), 0.0);
		assertEquals(344.5, parseDouble("34.45 E1  ", Locale.GERMAN, Double.NaN), 0.0);
		assertEquals(3445, parseDouble("34.45E2", Locale.GERMAN, Double.NaN), 0.0);
		assertEquals(3445, parseDouble("34.45 E+2", Locale.GERMAN, Double.NaN), 0.0);
		assertEquals(Double.NaN, parseDouble("  34.45  E+2 ", Locale.GERMAN, Double.NaN), 0.0);

		assertEquals(34.45, parseDouble("  34,45", Locale.GERMAN, Double.NaN), 0.0);
		assertEquals(344.5, parseDouble("34,45 E1  ", Locale.GERMAN, Double.NaN), 0.0);
		assertEquals(3445, parseDouble("34,45E2", Locale.GERMAN, Double.NaN), 0.0);
		assertEquals(3445, parseDouble("34,45 E+2", Locale.GERMAN, Double.NaN), 0.0);
		assertEquals(Double.NaN, parseDouble("  34,45  E+2 ", Locale.GERMAN, Double.NaN), 0.0);

		assertEquals(12.25, parseDouble(" 12.25 ", null, Double.NaN), 0.0);
		assertEquals(Double.NaN, parseDouble(" 12 .25 ", null, Double.NaN), 0.0);
		assertEquals(Double.NaN, parseDouble(" 12. 25 ", null, Double.NaN), 0.0);

		assertEquals(78520.56647691561, parseDouble("  0x265712.2094325 P-5", null, Double.NaN), 0.0);
		assertEquals(6.432404805788927E8, parseDouble("  0x265712.2094325 E2", null, Double.NaN), 0.0);
		assertEquals(172668551721854208.0, parseDouble("  0x0.2657122094325 EF", null, Double.NaN), 0.0);
	}

	@Test
	public void parseDoubleMathContextOverloadTest() {
		// Invalid
		assertEquals(Double.NaN, parseDouble(null, null, null, Double.NaN), 0.0);
		assertEquals(Double.NaN, parseDouble(LoggerFactory.getILoggerFactory(), null, null, Double.NaN), 0.0);
		assertEquals(Double.NaN, parseDouble(new StringBuilder("foobar"), null, null, Double.NaN), 0.0);
		assertEquals(Double.NaN, parseDouble("foo 2 bar", null, null, Double.NaN), 0.0);
		assertEquals(Double.NaN, parseDouble(" \t", null, null, Double.NaN), 0.0);
		assertEquals(Double.NaN, parseDouble(" ", null, null, Double.NaN), 0.0);
		assertEquals(Double.NaN, parseDouble("", null, null, Double.NaN), 0.0);

		// Number
		assertEquals(Byte.MAX_VALUE, parseDouble(Integer.valueOf(Byte.MAX_VALUE), null, null, Double.NaN), 0.0);
		assertEquals(Byte.MAX_VALUE, parseDouble(Long.valueOf(Byte.MAX_VALUE), null, null, Double.NaN), 0.0);
		assertEquals(Byte.MAX_VALUE, parseDouble(Byte.valueOf(Byte.MAX_VALUE), null, null, Double.NaN), 0.0);
		assertEquals(Byte.MIN_VALUE, parseDouble(Integer.valueOf(Byte.MIN_VALUE), null, null, Double.NaN), 0.0);
		assertEquals(Byte.MIN_VALUE, parseDouble(Long.valueOf(Byte.MIN_VALUE), null, null, Double.NaN), 0.0);
		assertEquals(Byte.MIN_VALUE, parseDouble(Byte.valueOf(Byte.MIN_VALUE), null, null, Double.NaN), 0.0);

		assertEquals(Short.MAX_VALUE, parseDouble(Integer.valueOf(Short.MAX_VALUE), null, null, Double.NaN), 0.0);
		assertEquals(Short.MAX_VALUE, parseDouble(Long.valueOf(Short.MAX_VALUE), null, null, Double.NaN), 0.0);
		assertEquals(Short.MAX_VALUE, parseDouble(Short.valueOf(Short.MAX_VALUE), null, null, Double.NaN), 0.0);
		assertEquals(Short.MIN_VALUE, parseDouble(Integer.valueOf(Short.MIN_VALUE), null, null, Double.NaN), 0.0);
		assertEquals(Short.MIN_VALUE, parseDouble(Long.valueOf(Short.MIN_VALUE), null, null, Double.NaN), 0.0);
		assertEquals(Short.MIN_VALUE, parseDouble(Short.valueOf(Short.MIN_VALUE), null, null, Double.NaN), 0.0);

		assertEquals(Integer.MAX_VALUE, parseDouble(Integer.valueOf(Integer.MAX_VALUE), null, null, Double.NaN), 0.0);
		assertEquals(Integer.MAX_VALUE, parseDouble(Long.valueOf(Integer.MAX_VALUE), null, null, Double.NaN), 0.0);
		assertEquals(Integer.MIN_VALUE, parseDouble(Integer.valueOf(Integer.MIN_VALUE), null, null, Double.NaN), 0.0);
		assertEquals(Integer.MIN_VALUE, parseDouble(Long.valueOf(Integer.MIN_VALUE), null, null, Double.NaN), 0.0);

		assertEquals(Long.MAX_VALUE, parseDouble(Long.valueOf(Long.MAX_VALUE), null, null, Double.NaN), 0.0);
		assertEquals(Long.MAX_VALUE, parseDouble(BigInteger.valueOf(Long.MAX_VALUE), null, null, Double.NaN), 0.0);
		assertEquals(Long.MIN_VALUE, parseDouble(Long.valueOf(Long.MIN_VALUE), null, null, Double.NaN), 0.0);
		assertEquals(Long.MIN_VALUE, parseDouble(BigInteger.valueOf(Long.MIN_VALUE), null, null, Double.NaN), 0.0);

		assertEquals(Integer.MAX_VALUE, parseDouble(Long.valueOf(Integer.MAX_VALUE), null, MathContext.DECIMAL64, Double.NaN), 0.0);
		assertEquals(Integer.MIN_VALUE, parseDouble(Long.valueOf(Integer.MIN_VALUE), null, MathContext.DECIMAL64, Double.NaN), 0.0);

		assertEquals(Long.MAX_VALUE, parseDouble(Long.valueOf(Long.MAX_VALUE), null, MathContext.DECIMAL128, Double.NaN), 0.0);
		assertEquals(Long.MIN_VALUE, parseDouble(Long.valueOf(Long.MIN_VALUE), null, MathContext.DECIMAL128, Double.NaN), 0.0);

		assertEquals(9.223372036854776E19, parseDouble(BigInteger.valueOf(Long.MAX_VALUE).multiply(BigInteger.TEN), null, MathContext.DECIMAL128, Double.NaN), 0.0);
		assertEquals(-9.223372036854776E19, parseDouble(BigInteger.valueOf(Long.MIN_VALUE).multiply(BigInteger.TEN), null, MathContext.DECIMAL128, Double.NaN), 0.0);

		assertEquals(Double.POSITIVE_INFINITY, parseDouble(Double.valueOf(Double.MAX_VALUE), null, MathContext.DECIMAL128, Double.NaN), 0.0);
		assertEquals(Double.MIN_VALUE, parseDouble(Double.valueOf(Double.MIN_VALUE), null, MathContext.DECIMAL128, Double.NaN), 0.0);

		assertEquals(9.223372036854776E28, parseDouble(BigDecimal.valueOf(Long.MAX_VALUE, -10), null, MathContext.DECIMAL128, Double.NaN), 0.0);
		assertEquals(-9.223372036854776E28, parseDouble(BigDecimal.valueOf(Long.MIN_VALUE, -10), null, MathContext.DECIMAL128, Double.NaN), 0.0);

		assertEquals(9.223372036854776E28, parseDouble(Rational.valueOf(BigDecimal.valueOf(Long.MAX_VALUE, -10)), null, MathContext.DECIMAL128, Double.NaN), 0.0);
		assertEquals(-9.223372036854776E28, parseDouble(Rational.valueOf(BigDecimal.valueOf(Long.MIN_VALUE, -10)), null, MathContext.DECIMAL128, Double.NaN), 0.0);

		assertEquals(Long.MAX_VALUE, parseDouble(Rational.valueOf(Long.MAX_VALUE), null, MathContext.DECIMAL128, Double.NaN), 0.0);
		assertEquals(Long.MIN_VALUE, parseDouble(Rational.valueOf(Long.MIN_VALUE), null, MathContext.DECIMAL128, Double.NaN), 0.0);

		assertEquals(Integer.MAX_VALUE, parseDouble(Rational.valueOf(Integer.MAX_VALUE), null, MathContext.DECIMAL128, Double.NaN), 0.0);
		assertEquals(Integer.MIN_VALUE, parseDouble(Rational.valueOf(Integer.MIN_VALUE), null, MathContext.DECIMAL128, Double.NaN), 0.0);

		assertEquals(Short.MAX_VALUE, parseDouble(Rational.valueOf(Short.MAX_VALUE), null, MathContext.DECIMAL128, Double.NaN), 0.0);
		assertEquals(Short.MIN_VALUE, parseDouble(Rational.valueOf(Short.MIN_VALUE), null, MathContext.DECIMAL128, Double.NaN), 0.0);

		assertEquals(Byte.MAX_VALUE, parseDouble(Rational.valueOf(Byte.MAX_VALUE), null, MathContext.DECIMAL128, Double.NaN), 0.0);
		assertEquals(Byte.MIN_VALUE, parseDouble(Rational.valueOf(Byte.MIN_VALUE), null, MathContext.DECIMAL128, Double.NaN), 0.0);

		assertEquals(0.75, parseDouble(Rational.valueOf(3, 4), null, null, Double.NaN), 0.0);

		// String
		assertEquals(0, parseDouble("0", null, null, Double.NaN), 0.0);
		assertEquals(1, parseDouble("1", null, null, Double.NaN), 0.0);
		assertEquals(-1, parseDouble("-1", null, null, Double.NaN), 0.0);

		assertEquals(0, parseDouble("0.00 E14", null, null, Double.NaN), 0.0);
		assertEquals(1, parseDouble("0.01 E2", null, null, Double.NaN), 0.0);
		assertEquals(-1, parseDouble("-0.01 E2", null, null, Double.NaN), 0.0);

		assertEquals(0, parseDouble("0x0.00 E-14", null, null, Double.NaN), 0.0);
		assertEquals(0, parseDouble("0x0.00 P-14", null, null, Double.NaN), 0.0);
		assertEquals(1, parseDouble("0x0.01 E2", null, null, Double.NaN), 0.0);
		assertEquals(1, parseDouble("0x10 E-1", null, null, Double.NaN), 0.0);
		assertEquals(1, parseDouble("0x10 P-4", null, null, Double.NaN), 0.0);
		assertEquals(-1, parseDouble("-0x0.01 E2", null, null, Double.NaN), 0.0);
		assertEquals(-1, parseDouble("-0x10 E-1", null, null, Double.NaN), 0.0);
		assertEquals(-1, parseDouble("-0x10 P-4", null, null, Double.NaN), 0.0);

		assertEquals(10, parseDouble("0x0.0a E2", null, null, Double.NaN), 0.0);
		assertEquals(10, parseDouble("0xa0 E-1", null, null, Double.NaN), 0.0);
		assertEquals(10, parseDouble("0xa0 P-4", null, null, Double.NaN), 0.0);
		assertEquals(-10, parseDouble("-0x0.0a E2", null, null, Double.NaN), 0.0);
		assertEquals(-10, parseDouble("-0x0,0a E2", Locale.GERMAN, null, Double.NaN), 0.0);
		assertEquals(-10, parseDouble("-0xa0 E-1", null, null, Double.NaN), 0.0);
		assertEquals(-10, parseDouble("-0xa0p-4", null, null, Double.NaN), 0.0);

		assertEquals(34.45, parseDouble("  34.45", null, null, Double.NaN), 0.0);
		assertEquals(344.5, parseDouble("34.45 E1  ", null, null, Double.NaN), 0.0);
		assertEquals(3445, parseDouble("34.45E2", null, null, Double.NaN), 0.0);
		assertEquals(3445, parseDouble("34.45 E+2", null, null, Double.NaN), 0.0);
		assertEquals(Double.NaN, parseDouble("  34.45  E+2 ", null, null, Double.NaN), 0.0);

		assertEquals(34.45, parseDouble("  34.45", Locale.GERMAN, null, Double.NaN), 0.0);
		assertEquals(344.5, parseDouble("34.45 E1  ", Locale.GERMAN, null, Double.NaN), 0.0);
		assertEquals(3445, parseDouble("34.45E2", Locale.GERMAN, null, Double.NaN), 0.0);
		assertEquals(3445, parseDouble("34.45 E+2", Locale.GERMAN, null, Double.NaN), 0.0);
		assertEquals(Double.NaN, parseDouble("  34.45  E+2 ", Locale.GERMAN, null, Double.NaN), 0.0);

		assertEquals(34.45, parseDouble("  34,45", Locale.GERMAN, null, Double.NaN), 0.0);
		assertEquals(344.5, parseDouble("34,45 E1  ", Locale.GERMAN, null, Double.NaN), 0.0);
		assertEquals(3445, parseDouble("34,45E2", Locale.GERMAN, null, Double.NaN), 0.0);
		assertEquals(3445, parseDouble("34,45 E+2", Locale.GERMAN, null, Double.NaN), 0.0);
		assertEquals(Double.NaN, parseDouble("  34,45  E+2 ", Locale.GERMAN, null, Double.NaN), 0.0);

		assertEquals(12.25, parseDouble(" 12.25 ", null, null, Double.NaN), 0.0);
		assertEquals(Double.NaN, parseDouble(" 12 .25 ", null, null, Double.NaN), 0.0);
		assertEquals(Double.NaN, parseDouble(" 12. 25 ", null, null, Double.NaN), 0.0);

		assertEquals(78520.56647691561, parseDouble("  0x265712.2094325 P-5", null, null, Double.NaN), 0.0);
		assertEquals(172660000000000000.0, parseDouble("  0x0.2657122094325 EF", null, new MathContext(5, RoundingMode.DOWN), Double.NaN), 0.0);
		assertEquals(172670000000000000.0, parseDouble("  0x0.2657122094325 EF", null, new MathContext(5, RoundingMode.HALF_EVEN), Double.NaN), 0.0);
		assertEquals(172668551721854208.0, parseDouble("  0x0.2657122094325 EF", null, null, Double.NaN), 0.0);
	}

	@Test
	public void parseDoubleRoundingOverloadTest() {
		// Invalid
		assertEquals(Double.NaN, parseDouble(null, null, 0, null, Double.NaN), 0.0);
		assertEquals(Double.NaN, parseDouble(LoggerFactory.getILoggerFactory(), null, 0, null, Double.NaN), 0.0);
		assertEquals(Double.NaN, parseDouble(new StringBuilder("foobar"), null, 0, null, Double.NaN), 0.0);
		assertEquals(Double.NaN, parseDouble("foo 2 bar", null, 0, null, Double.NaN), 0.0);
		assertEquals(Double.NaN, parseDouble(" \t", null, 0, null, Double.NaN), 0.0);
		assertEquals(Double.NaN, parseDouble(" ", null, 0, null, Double.NaN), 0.0);
		assertEquals(Double.NaN, parseDouble("", null, 0, null, Double.NaN), 0.0);

		// Number
		assertEquals(Byte.MAX_VALUE, parseDouble(Integer.valueOf(Byte.MAX_VALUE), null, 0, null, Double.NaN), 0.0);
		assertEquals(Byte.MAX_VALUE, parseDouble(Long.valueOf(Byte.MAX_VALUE), null, 0, null, Double.NaN), 0.0);
		assertEquals(Byte.MAX_VALUE, parseDouble(Byte.valueOf(Byte.MAX_VALUE), null, 0, null, Double.NaN), 0.0);
		assertEquals(Byte.MIN_VALUE, parseDouble(Integer.valueOf(Byte.MIN_VALUE), null, 0, null, Double.NaN), 0.0);
		assertEquals(Byte.MIN_VALUE, parseDouble(Long.valueOf(Byte.MIN_VALUE), null, 0, null, Double.NaN), 0.0);
		assertEquals(Byte.MIN_VALUE, parseDouble(Byte.valueOf(Byte.MIN_VALUE), null, 0, null, Double.NaN), 0.0);

		assertEquals(Short.MAX_VALUE, parseDouble(Integer.valueOf(Short.MAX_VALUE), null, 0, null, Double.NaN), 0.0);
		assertEquals(Short.MAX_VALUE, parseDouble(Long.valueOf(Short.MAX_VALUE), null, 0, null, Double.NaN), 0.0);
		assertEquals(Short.MAX_VALUE, parseDouble(Short.valueOf(Short.MAX_VALUE), null, 0, null, Double.NaN), 0.0);
		assertEquals(Short.MIN_VALUE, parseDouble(Integer.valueOf(Short.MIN_VALUE), null, 0, null, Double.NaN), 0.0);
		assertEquals(Short.MIN_VALUE, parseDouble(Long.valueOf(Short.MIN_VALUE), null, 0, null, Double.NaN), 0.0);
		assertEquals(Short.MIN_VALUE, parseDouble(Short.valueOf(Short.MIN_VALUE), null, 0, null, Double.NaN), 0.0);

		assertEquals(Integer.MAX_VALUE, parseDouble(Integer.valueOf(Integer.MAX_VALUE), null, 0, null, Double.NaN), 0.0);
		assertEquals(Integer.MAX_VALUE, parseDouble(Long.valueOf(Integer.MAX_VALUE), null, 0, null, Double.NaN), 0.0);
		assertEquals(Integer.MIN_VALUE, parseDouble(Integer.valueOf(Integer.MIN_VALUE), null, 0, null, Double.NaN), 0.0);
		assertEquals(Integer.MIN_VALUE, parseDouble(Long.valueOf(Integer.MIN_VALUE), null, 0, null, Double.NaN), 0.0);

		assertEquals(Long.MAX_VALUE, parseDouble(Long.valueOf(Long.MAX_VALUE), null, 0, null, Double.NaN), 0.0);
		assertEquals(Long.MAX_VALUE, parseDouble(BigInteger.valueOf(Long.MAX_VALUE), null, 0, null, Double.NaN), 0.0);
		assertEquals(Long.MIN_VALUE, parseDouble(Long.valueOf(Long.MIN_VALUE), null, 0, null, Double.NaN), 0.0);
		assertEquals(Long.MIN_VALUE, parseDouble(BigInteger.valueOf(Long.MIN_VALUE), null, 0, null, Double.NaN), 0.0);

		assertEquals(Integer.MAX_VALUE, parseDouble(Long.valueOf(Integer.MAX_VALUE), null, 0, RoundingMode.HALF_UP, Double.NaN), 0.0);
		assertEquals(Integer.MIN_VALUE, parseDouble(Long.valueOf(Integer.MIN_VALUE), null, 0, RoundingMode.HALF_UP, Double.NaN), 0.0);

		assertEquals(Long.MAX_VALUE, parseDouble(Long.valueOf(Long.MAX_VALUE), null, 0, RoundingMode.CEILING, Double.NaN), 0.0);
		assertEquals(Long.MIN_VALUE, parseDouble(Long.valueOf(Long.MIN_VALUE), null, 0, RoundingMode.CEILING, Double.NaN), 0.0);

		assertEquals(9.223372036854776E19, parseDouble(BigInteger.valueOf(Long.MAX_VALUE).multiply(BigInteger.TEN), null, 0, RoundingMode.CEILING, Double.NaN), 0.0);
		assertEquals(-9.223372036854776E19, parseDouble(BigInteger.valueOf(Long.MIN_VALUE).multiply(BigInteger.TEN), null, 0, RoundingMode.CEILING, Double.NaN), 0.0);

		assertEquals(Double.POSITIVE_INFINITY, parseDouble(Double.valueOf(Double.MAX_VALUE), null, 0, RoundingMode.CEILING, Double.NaN), 0.0);
		assertEquals(1.0, parseDouble(Double.valueOf(Double.MIN_VALUE), null, 0, RoundingMode.CEILING, Double.NaN), 0.0);
		assertEquals(0.0, parseDouble(Double.valueOf(Double.MIN_VALUE), null, 0, RoundingMode.DOWN, Double.NaN), 0.0);
		assertEquals(0.0, parseDouble(Double.valueOf(Double.MIN_VALUE), null, 0, RoundingMode.FLOOR, Double.NaN), 0.0);
		assertEquals(0.0, parseDouble(Double.valueOf(Double.MIN_VALUE), null, 0, RoundingMode.HALF_DOWN, Double.NaN), 0.0);
		assertEquals(0.0, parseDouble(Double.valueOf(Double.MIN_VALUE), null, 0, RoundingMode.HALF_EVEN, Double.NaN), 0.0);
		assertEquals(0.0, parseDouble(Double.valueOf(Double.MIN_VALUE), null, 0, RoundingMode.HALF_UP, Double.NaN), 0.0);
		assertEquals(1.0, parseDouble(Double.valueOf(Double.MIN_VALUE), null, 0, RoundingMode.UP, Double.NaN), 0.0);

		assertEquals(9.223372036854776E28, parseDouble(BigDecimal.valueOf(Long.MAX_VALUE, -10), null, 0, RoundingMode.CEILING, Double.NaN), 0.0);
		assertEquals(-9.223372036854776E28, parseDouble(BigDecimal.valueOf(Long.MIN_VALUE, -10), null, 0, RoundingMode.CEILING, Double.NaN), 0.0);

		assertEquals(9.223372036854776E28, parseDouble(Rational.valueOf(BigDecimal.valueOf(Long.MAX_VALUE, -10)), null, 0, RoundingMode.CEILING, Double.NaN), 0.0);
		assertEquals(-9.223372036854776E28, parseDouble(Rational.valueOf(BigDecimal.valueOf(Long.MIN_VALUE, -10)), null, 0, RoundingMode.CEILING, Double.NaN), 0.0);

		assertEquals(Long.MAX_VALUE, parseDouble(Rational.valueOf(Long.MAX_VALUE), null, 0, RoundingMode.CEILING, Double.NaN), 0.0);
		assertEquals(Long.MIN_VALUE, parseDouble(Rational.valueOf(Long.MIN_VALUE), null, 0, RoundingMode.CEILING, Double.NaN), 0.0);

		assertEquals(Integer.MAX_VALUE, parseDouble(Rational.valueOf(Integer.MAX_VALUE), null, 0, RoundingMode.CEILING, Double.NaN), 0.0);
		assertEquals(Integer.MIN_VALUE, parseDouble(Rational.valueOf(Integer.MIN_VALUE), null, 0, RoundingMode.CEILING, Double.NaN), 0.0);

		assertEquals(Short.MAX_VALUE, parseDouble(Rational.valueOf(Short.MAX_VALUE), null, 0, RoundingMode.CEILING, Double.NaN), 0.0);
		assertEquals(Short.MIN_VALUE, parseDouble(Rational.valueOf(Short.MIN_VALUE), null, 0, RoundingMode.CEILING, Double.NaN), 0.0);

		assertEquals(Byte.MAX_VALUE, parseDouble(Rational.valueOf(Byte.MAX_VALUE), null, 0, RoundingMode.CEILING, Double.NaN), 0.0);
		assertEquals(Byte.MIN_VALUE, parseDouble(Rational.valueOf(Byte.MIN_VALUE), null, 0, RoundingMode.CEILING, Double.NaN), 0.0);

		assertEquals(0.75, parseDouble(Rational.valueOf(3, 4), null, 0, null, Double.NaN), 0.0);
		assertEquals(0.8, parseDouble(Rational.valueOf(3, 4), null, 1, RoundingMode.HALF_EVEN, Double.NaN), 0.0);

		// String
		assertEquals(0, parseDouble("0", null, 0, null, Double.NaN), 0.0);
		assertEquals(1, parseDouble("1", null, 0, null, Double.NaN), 0.0);
		assertEquals(-1, parseDouble("-1", null, 0, null, Double.NaN), 0.0);

		assertEquals(0, parseDouble("0.00 E14", null, 0, null, Double.NaN), 0.0);
		assertEquals(1, parseDouble("0.01 E2", null, 0, null, Double.NaN), 0.0);
		assertEquals(-1, parseDouble("-0.01 E2", null, 0, null, Double.NaN), 0.0);

		assertEquals(0, parseDouble("0x0.00 E-14", null, 0, null, Double.NaN), 0.0);
		assertEquals(0, parseDouble("0x0.00 P-14", null, 0, null, Double.NaN), 0.0);
		assertEquals(1, parseDouble("0x0.01 E2", null, 0, null, Double.NaN), 0.0);
		assertEquals(1, parseDouble("0x10 E-1", null, 0, null, Double.NaN), 0.0);
		assertEquals(1, parseDouble("0x10 P-4", null, 0, null, Double.NaN), 0.0);
		assertEquals(-1, parseDouble("-0x0.01 E2", null, 0, null, Double.NaN), 0.0);
		assertEquals(-1, parseDouble("-0x10 E-1", null, 0, null, Double.NaN), 0.0);
		assertEquals(-1, parseDouble("-0x10 P-4", null, 0, null, Double.NaN), 0.0);

		assertEquals(10, parseDouble("0x0.0a E2", null, 0, null, Double.NaN), 0.0);
		assertEquals(10, parseDouble("0xa0 E-1", null, 0, null, Double.NaN), 0.0);
		assertEquals(10, parseDouble("0xa0 P-4", null, 0, null, Double.NaN), 0.0);
		assertEquals(-10, parseDouble("-0x0.0a E2", null, 0, null, Double.NaN), 0.0);
		assertEquals(-10, parseDouble("-0x0,0a E2", Locale.GERMAN, 0, null, Double.NaN), 0.0);
		assertEquals(-10, parseDouble("-0xa0 E-1", null, 0, null, Double.NaN), 0.0);
		assertEquals(-10, parseDouble("-0xa0p-4", null, 0, null, Double.NaN), 0.0);

		assertEquals(34.45, parseDouble("  34.45", null, 0, null, Double.NaN), 0.0);
		assertEquals(344.5, parseDouble("34.45 E1  ", null, 0, null, Double.NaN), 0.0);
		assertEquals(3445, parseDouble("34.45E2", null, 0, null, Double.NaN), 0.0);
		assertEquals(3445, parseDouble("34.45 E+2", null, 0, null, Double.NaN), 0.0);
		assertEquals(Double.NaN, parseDouble("  34.45  E+2 ", null, 0, null, Double.NaN), 0.0);

		assertEquals(34.45, parseDouble("  34.45", Locale.GERMAN, 0, null, Double.NaN), 0.0);
		assertEquals(344.5, parseDouble("34.45 E1  ", Locale.GERMAN, 0, null, Double.NaN), 0.0);
		assertEquals(3445, parseDouble("34.45E2", Locale.GERMAN, 0, null, Double.NaN), 0.0);
		assertEquals(3445, parseDouble("34.45 E+2", Locale.GERMAN, 0, null, Double.NaN), 0.0);
		assertEquals(Double.NaN, parseDouble("  34.45  E+2 ", Locale.GERMAN, 0, null, Double.NaN), 0.0);

		assertEquals(34.45, parseDouble("  34,45", Locale.GERMAN, 0, null, Double.NaN), 0.0);
		assertEquals(344.5, parseDouble("34,45 E1  ", Locale.GERMAN, 0, null, Double.NaN), 0.0);
		assertEquals(3445, parseDouble("34,45E2", Locale.GERMAN, 0, null, Double.NaN), 0.0);
		assertEquals(3445, parseDouble("34,45 E+2", Locale.GERMAN, 0, null, Double.NaN), 0.0);
		assertEquals(Double.NaN, parseDouble("  34,45  E+2 ", Locale.GERMAN, 0, null, Double.NaN), 0.0);

		assertEquals(12.25, parseDouble(" 12.25 ", null, 0, null, Double.NaN), 0.0);
		assertEquals(Double.NaN, parseDouble(" 12 .25 ", null, 0, null, Double.NaN), 0.0);
		assertEquals(Double.NaN, parseDouble(" 12. 25 ", null, 0, null, Double.NaN), 0.0);

		assertEquals(78520.56647691561, parseDouble("  0x265712.2094325 P-5", null, 0, null, Double.NaN), 0.0);
		assertEquals(172668551721854208.0, parseDouble("  0x0.2657122094325 EF", null, 0, null, Double.NaN), 0.0);
		assertEquals(172668551721854000.0, parseDouble("  0x0.2657122094325 EF", null, -3, RoundingMode.HALF_UP, Double.NaN), 0.0);
		assertEquals(172668551721854000.0, parseDouble("  0x0.2657122094325 EF", null, -3, RoundingMode.HALF_EVEN, Double.NaN), 0.0);
	}

	@Test
	public void parseNumberNoRoundingOverloadTest() {
		// Invalid
		assertNull(parseNumber(null, null, false));
		assertNull(parseNumber(LoggerFactory.getILoggerFactory(), null, false));
		assertNull(parseNumber(new StringBuilder("foobar"), null, false));
		assertNull(parseNumber("foo 2 bar", null, false));
		assertNull(parseNumber(" \t", null, false));
		assertNull(parseNumber(" ", null, false));
		assertNull(parseNumber("", null, false));

		// Number
		assertEquals(Integer.valueOf(Byte.MAX_VALUE), parseNumber(Integer.valueOf(Byte.MAX_VALUE), null, false));
		assertEquals(Long.valueOf(Byte.MAX_VALUE), parseNumber(Long.valueOf(Byte.MAX_VALUE), null, false));
		assertEquals(Integer.valueOf(Byte.MAX_VALUE), parseNumber(Long.valueOf(Byte.MAX_VALUE), null, true));
		assertEquals(Integer.valueOf(Byte.MAX_VALUE), parseNumber(Byte.valueOf(Byte.MAX_VALUE), null, false));
		assertEquals(Integer.valueOf(Byte.MAX_VALUE), parseNumber(Byte.valueOf(Byte.MAX_VALUE), null, true));
		assertEquals(Integer.valueOf(Byte.MIN_VALUE), parseNumber(Integer.valueOf(Byte.MIN_VALUE), null, false));
		assertEquals(Long.valueOf(Byte.MIN_VALUE), parseNumber(Long.valueOf(Byte.MIN_VALUE), null, false));
		assertEquals(Integer.valueOf(Byte.MIN_VALUE), parseNumber(Long.valueOf(Byte.MIN_VALUE), null, true));
		assertEquals(Integer.valueOf(Byte.MIN_VALUE), parseNumber(Byte.valueOf(Byte.MIN_VALUE), null, false));
		assertEquals(Integer.valueOf(Byte.MIN_VALUE), parseNumber(Byte.valueOf(Byte.MIN_VALUE), null, true));

		assertEquals(Integer.valueOf(Short.MAX_VALUE), parseNumber(Integer.valueOf(Short.MAX_VALUE), null, false));
		assertEquals(Long.valueOf(Short.MAX_VALUE), parseNumber(Long.valueOf(Short.MAX_VALUE), null, false));
		assertEquals(Integer.valueOf(Short.MAX_VALUE), parseNumber(Long.valueOf(Short.MAX_VALUE), null, true));
		assertEquals(Integer.valueOf(Short.MAX_VALUE), parseNumber(Short.valueOf(Short.MAX_VALUE), null, false));
		assertEquals(Integer.valueOf(Short.MAX_VALUE), parseNumber(Short.valueOf(Short.MAX_VALUE), null, true));
		assertEquals(Integer.valueOf(Short.MIN_VALUE), parseNumber(Integer.valueOf(Short.MIN_VALUE), null, false));
		assertEquals(Long.valueOf(Short.MIN_VALUE), parseNumber(Long.valueOf(Short.MIN_VALUE), null, false));
		assertEquals(Integer.valueOf(Short.MIN_VALUE), parseNumber(Long.valueOf(Short.MIN_VALUE), null, true));
		assertEquals(Integer.valueOf(Short.MIN_VALUE), parseNumber(Short.valueOf(Short.MIN_VALUE), null, false));
		assertEquals(Integer.valueOf(Short.MIN_VALUE), parseNumber(Short.valueOf(Short.MIN_VALUE), null, true));

		assertEquals(Integer.valueOf(Integer.MAX_VALUE), parseNumber(Integer.valueOf(Integer.MAX_VALUE), null, false));
		assertEquals(Long.valueOf(Integer.MAX_VALUE), parseNumber(Long.valueOf(Integer.MAX_VALUE), null, false));
		assertEquals(Integer.valueOf(Integer.MAX_VALUE), parseNumber(Long.valueOf(Integer.MAX_VALUE), null, true));
		assertEquals(Integer.valueOf(Integer.MIN_VALUE), parseNumber(Integer.valueOf(Integer.MIN_VALUE), null, false));
		assertEquals(Long.valueOf(Integer.MIN_VALUE), parseNumber(Long.valueOf(Integer.MIN_VALUE), null, false));
		assertEquals(Integer.valueOf(Integer.MIN_VALUE), parseNumber(Long.valueOf(Integer.MIN_VALUE), null, true));

		assertEquals(Long.valueOf(Long.MAX_VALUE), parseNumber(Long.valueOf(Long.MAX_VALUE), null, false));
		assertEquals(BigInteger.valueOf(Long.MAX_VALUE), parseNumber(BigInteger.valueOf(Long.MAX_VALUE), null, false));
		assertEquals(Long.valueOf(Long.MAX_VALUE), parseNumber(BigInteger.valueOf(Long.MAX_VALUE), null, true));
		assertEquals(Long.valueOf(Long.MIN_VALUE), parseNumber(Long.valueOf(Long.MIN_VALUE), null, false));
		assertEquals(BigInteger.valueOf(Long.MIN_VALUE), parseNumber(BigInteger.valueOf(Long.MIN_VALUE), null, false));
		assertEquals(Long.valueOf(Long.MIN_VALUE), parseNumber(BigInteger.valueOf(Long.MIN_VALUE), null, true));

		assertEquals(Long.valueOf(Integer.MAX_VALUE), parseNumber(Long.valueOf(Integer.MAX_VALUE), null, false));
		assertEquals(Integer.valueOf(Integer.MAX_VALUE), parseNumber(Long.valueOf(Integer.MAX_VALUE), null, true));
		assertEquals(Long.valueOf(Integer.MIN_VALUE), parseNumber(Long.valueOf(Integer.MIN_VALUE), null, false));
		assertEquals(Integer.valueOf(Integer.MIN_VALUE), parseNumber(Long.valueOf(Integer.MIN_VALUE), null, true));

		assertEquals(Long.valueOf(Long.MAX_VALUE), parseNumber(Long.valueOf(Long.MAX_VALUE), null, false));
		assertEquals(Long.valueOf(Long.MAX_VALUE), parseNumber(Long.valueOf(Long.MAX_VALUE), null, true));
		assertEquals(Long.valueOf(Long.MIN_VALUE), parseNumber(Long.valueOf(Long.MIN_VALUE), null, false));
		assertEquals(Long.valueOf(Long.MIN_VALUE), parseNumber(Long.valueOf(Long.MIN_VALUE), null, true));

		BigInteger bigInteger = BigInteger.valueOf(Long.MAX_VALUE).multiply(BigInteger.TEN);
		assertEquals(bigInteger, parseNumber(bigInteger, null, false));
		assertEquals(bigInteger, parseNumber(bigInteger, null, true));
		bigInteger = BigInteger.valueOf(Long.MIN_VALUE).multiply(BigInteger.TEN);
		assertEquals(bigInteger, parseNumber(bigInteger, null, false));
		assertEquals(bigInteger, parseNumber(bigInteger, null, true));

		assertEquals(Double.valueOf(Double.MAX_VALUE), parseNumber(Double.valueOf(Double.MAX_VALUE), null, false));
		assertEquals(Double.valueOf(Double.MAX_VALUE), parseNumber(Double.valueOf(Double.MAX_VALUE), null, true));
		assertEquals(Double.valueOf(Double.MIN_VALUE), parseNumber(Double.valueOf(Double.MIN_VALUE), null, false));
		assertEquals(Double.valueOf(Double.MIN_VALUE), parseNumber(Double.valueOf(Double.MIN_VALUE), null, true));

		BigDecimal bigDecimal = BigDecimal.valueOf(Long.MAX_VALUE, -10);
		assertEquals(Rational.valueOf(bigDecimal), parseNumber(bigDecimal, null, false));
		assertEquals(bigDecimal.toBigIntegerExact(), parseNumber(bigDecimal, null, true));
		bigDecimal = BigDecimal.valueOf(Long.MIN_VALUE, -10);
		assertEquals(Rational.valueOf(bigDecimal), parseNumber(bigDecimal, null, false));
		assertEquals(bigDecimal.toBigIntegerExact(), parseNumber(bigDecimal, null, true));

		bigInteger = BigDecimal.valueOf(Long.MAX_VALUE, -10).toBigIntegerExact();
		Rational rational = Rational.valueOf(BigDecimal.valueOf(Long.MAX_VALUE, -10));
		assertEquals(rational, parseNumber(rational, null, false));
		assertEquals(bigInteger, parseNumber(rational, null, true));
		bigInteger = BigDecimal.valueOf(Long.MIN_VALUE, -10).toBigIntegerExact();
		rational = Rational.valueOf(BigDecimal.valueOf(Long.MIN_VALUE, -10));
		assertEquals(rational, parseNumber(rational, null, false));
		assertEquals(bigInteger, parseNumber(rational, null, true));

		rational = Rational.valueOf(Long.MAX_VALUE);
		assertEquals(rational, parseNumber(rational, null, false));
		assertEquals(Long.valueOf(Long.MAX_VALUE), parseNumber(rational, null, true));
		rational = Rational.valueOf(Long.MIN_VALUE);
		assertEquals(rational, parseNumber(rational, null, false));
		assertEquals(Long.valueOf(Long.MIN_VALUE), parseNumber(rational, null, true));

		rational = Rational.valueOf(Integer.MAX_VALUE);
		assertEquals(rational, parseNumber(rational, null, false));
		assertEquals(Integer.valueOf(Integer.MAX_VALUE), parseNumber(rational, null, true));
		rational = Rational.valueOf(Integer.MIN_VALUE);
		assertEquals(rational, parseNumber(rational, null, false));
		assertEquals(Integer.valueOf(Integer.MIN_VALUE), parseNumber(rational, null, true));

		rational = Rational.valueOf(Short.MAX_VALUE);
		assertEquals(rational, parseNumber(rational, null, false));
		assertEquals(Integer.valueOf(Short.MAX_VALUE), parseNumber(rational, null, true));
		rational = Rational.valueOf(Short.MIN_VALUE);
		assertEquals(rational, parseNumber(rational, null, false));
		assertEquals(Integer.valueOf(Short.MIN_VALUE), parseNumber(rational, null, true));

		rational = Rational.valueOf(Byte.MAX_VALUE);
		assertEquals(rational, parseNumber(rational, null, false));
		assertEquals(Integer.valueOf(Byte.MAX_VALUE), parseNumber(rational, null, true));
		rational = Rational.valueOf(Byte.MIN_VALUE);
		assertEquals(rational, parseNumber(rational, null, false));
		assertEquals(Integer.valueOf(Byte.MIN_VALUE), parseNumber(rational, null, true));

		rational = Rational.valueOf(3, 4);
		assertEquals(rational, parseNumber(rational, null, false));
		assertEquals(Double.valueOf(0.75), parseNumber(rational, null, true));

		// String
		assertEquals(BigInteger.ZERO, parseNumber("0", null, false));
		assertEquals(Integer.valueOf(0), parseNumber("0", null, true));
		assertEquals(BigInteger.ONE, parseNumber("1", null, false));
		assertEquals(Integer.valueOf(1), parseNumber("1", null, true));
		assertEquals(BigInteger.ONE.negate(), parseNumber("-1", null, false));
		assertEquals(Integer.valueOf(-1), parseNumber("-1", null, true));

		assertEquals(BigInteger.ZERO, parseNumber("0.00 E14", null, false));
		assertEquals(Integer.valueOf(0), parseNumber("0.00 E14", null, true));
		assertEquals(BigInteger.ONE, parseNumber("0.01 E2", null, false));
		assertEquals(Integer.valueOf(1), parseNumber("0.01 E2", null, true));
		assertEquals(BigInteger.ONE.negate(), parseNumber("-0.01 E2", null, false));
		assertEquals(Integer.valueOf(-1), parseNumber("-0.01 E2", null, true));

		assertEquals(BigInteger.ZERO, parseNumber("0x0.00 E-14", null, false));
		assertEquals(Integer.valueOf(0), parseNumber("0x0.00 E-14", null, true));
		assertEquals(BigInteger.ZERO, parseNumber("0x0.00 P-14", null, false));
		assertEquals(Integer.valueOf(0), parseNumber("0x0.00 P-14", null, true));
		assertEquals(BigInteger.ONE, parseNumber("0x0.01 E2", null, false));
		assertEquals(Integer.valueOf(1), parseNumber("0x0.01 E2", null, true));
		assertEquals(BigInteger.ONE, parseNumber("0x10 E-1", null, false));
		assertEquals(Integer.valueOf(1), parseNumber("0x10 E-1", null, true));
		assertEquals(BigInteger.ONE, parseNumber("0x10 P-4", null, false));
		assertEquals(Integer.valueOf(1), parseNumber("0x10 P-4", null, true));
		assertEquals(BigInteger.ONE.negate(), parseNumber("-0x0.01 E2", null, false));
		assertEquals(Integer.valueOf(-1), parseNumber("-0x0.01 E2", null, true));
		assertEquals(BigInteger.ONE.negate(), parseNumber("-0x10 E-1", null, false));
		assertEquals(Integer.valueOf(-1), parseNumber("-0x10 E-1", null, true));
		assertEquals(BigInteger.ONE.negate(), parseNumber("-0x10 P-4", null, false));
		assertEquals(Integer.valueOf(-1), parseNumber("-0x10 P-4", null, true));

		assertEquals(BigInteger.TEN, parseNumber("0x0.0a E2", null, false));
		assertEquals(Integer.valueOf(10), parseNumber("0x0.0a E2", null, true));
		assertEquals(BigInteger.TEN, parseNumber("0xa0 E-1", null, false));
		assertEquals(Integer.valueOf(10), parseNumber("0xa0 E-1", null, true));
		assertEquals(BigInteger.TEN, parseNumber("0xa0 P-4", null, false));
		assertEquals(Integer.valueOf(10), parseNumber("0xa0 P-4", null, true));
		assertEquals(BigInteger.TEN.negate(), parseNumber("-0x0.0a E2", null, false));
		assertEquals(Integer.valueOf(-10), parseNumber("-0x0.0a E2", null, true));
		assertEquals(BigInteger.TEN.negate(), parseNumber("-0x0,0a E2", Locale.GERMAN, false));
		assertEquals(Integer.valueOf(-10), parseNumber("-0x0,0a E2", Locale.GERMAN, true));
		assertEquals(BigInteger.TEN.negate(), parseNumber("-0xa0 E-1", null, false));
		assertEquals(Integer.valueOf(-10), parseNumber("-0xa0 E-1", null, true));
		assertEquals(BigInteger.TEN.negate(), parseNumber("-0xa0p-4", null, false));
		assertEquals(Integer.valueOf(-10), parseNumber("-0xa0p-4", null, true));

		assertEquals(Rational.valueOf(34.45), parseNumber("  34.45", null, true));
		assertEquals(Double.valueOf(344.5), parseNumber("34.45 E1  ", null, true));
		assertEquals(Integer.valueOf(3445), parseNumber("34.45E2", null, true));
		assertEquals(BigInteger.valueOf(3445), parseNumber("34.45 E+2", null, false));
		assertNull(parseNumber("  34.45  E+2 ", null, false));

		assertEquals(Rational.valueOf(34.45), parseNumber("  34.45", Locale.GERMAN, true));
		assertEquals(Double.valueOf(344.5), parseNumber("34.45 E1  ", Locale.GERMAN, true));
		assertEquals(Integer.valueOf(3445), parseNumber("34.45E2", Locale.GERMAN, true));
		assertEquals(BigInteger.valueOf(3445), parseNumber("34.45 E+2", Locale.GERMAN, false));
		assertNull(parseNumber("  34.45  E+2 ", Locale.GERMAN, false));

		assertEquals(Rational.valueOf(34.45), parseNumber("  34,45", Locale.GERMAN, true));
		assertEquals(Double.valueOf(344.5), parseNumber("34,45 E1  ", Locale.GERMAN, true));
		assertEquals(Integer.valueOf(3445), parseNumber("34,45E2", Locale.GERMAN, true));
		assertEquals(BigInteger.valueOf(3445), parseNumber("34,45 E+2", Locale.GERMAN, false));
		assertNull(parseNumber("  34,45  E+2 ", Locale.GERMAN, false));

		assertEquals(Rational.valueOf(49, 4), parseNumber("  12.25", null, false));
		assertEquals(Double.valueOf(12.25), parseNumber(" 12.25 ", null, true));
		assertNull(parseNumber(" 12 .25 ", null, true));
		assertNull(parseNumber(" 12. 25 ", null, true));

		assertEquals(Rational.valueOf("674486530163493/8589934592"), parseNumber("  0x265712.2094325 P-5", null, false));
		assertEquals(BigInteger.valueOf(172668551721854208L), parseNumber("  0x0.2657122094325 EF", null, false));
		assertEquals(Long.valueOf(172668551721854208L), parseNumber("  0x0.2657122094325 EF", null, true));
		assertEquals(BigInteger.valueOf(172668551721854208L), parseNumber("  0x0.2657122094325 EF", null, false));
		assertEquals(Long.valueOf(172668551721854208L), parseNumber("  0x0.2657122094325 EF", null, true));
	}

	@Test
	public void parseNumberMathContextOverloadTest() {
		// Invalid
		assertNull(parseNumber(null, null, null, false));
		assertNull(parseNumber(LoggerFactory.getILoggerFactory(), null, null, false));
		assertNull(parseNumber(new StringBuilder("foobar"), null, null, false));
		assertNull(parseNumber("foo 2 bar", null, null, false));
		assertNull(parseNumber(" \t", null, null, false));
		assertNull(parseNumber(" ", null, null, false));
		assertNull(parseNumber("", null, null, false));

		// Number
		assertEquals(Integer.valueOf(Byte.MAX_VALUE), parseNumber(Integer.valueOf(Byte.MAX_VALUE), null, null, false));
		assertEquals(Long.valueOf(Byte.MAX_VALUE), parseNumber(Long.valueOf(Byte.MAX_VALUE), null, null, false));
		assertEquals(Integer.valueOf(Byte.MAX_VALUE), parseNumber(Long.valueOf(Byte.MAX_VALUE), null, null, true));
		assertEquals(Integer.valueOf(Byte.MAX_VALUE), parseNumber(Byte.valueOf(Byte.MAX_VALUE), null, null, false));
		assertEquals(Integer.valueOf(Byte.MAX_VALUE), parseNumber(Byte.valueOf(Byte.MAX_VALUE), null, null, true));
		assertEquals(Integer.valueOf(Byte.MIN_VALUE), parseNumber(Integer.valueOf(Byte.MIN_VALUE), null, null, false));
		assertEquals(Long.valueOf(Byte.MIN_VALUE), parseNumber(Long.valueOf(Byte.MIN_VALUE), null, null, false));
		assertEquals(Integer.valueOf(Byte.MIN_VALUE), parseNumber(Long.valueOf(Byte.MIN_VALUE), null, null, true));
		assertEquals(Integer.valueOf(Byte.MIN_VALUE), parseNumber(Byte.valueOf(Byte.MIN_VALUE), null, null, false));
		assertEquals(Integer.valueOf(Byte.MIN_VALUE), parseNumber(Byte.valueOf(Byte.MIN_VALUE), null, null, true));

		assertEquals(Integer.valueOf(Short.MAX_VALUE), parseNumber(Integer.valueOf(Short.MAX_VALUE), null, null, false));
		assertEquals(Long.valueOf(Short.MAX_VALUE), parseNumber(Long.valueOf(Short.MAX_VALUE), null, null, false));
		assertEquals(Integer.valueOf(Short.MAX_VALUE), parseNumber(Long.valueOf(Short.MAX_VALUE), null, null, true));
		assertEquals(Integer.valueOf(Short.MAX_VALUE), parseNumber(Short.valueOf(Short.MAX_VALUE), null, null, false));
		assertEquals(Integer.valueOf(Short.MAX_VALUE), parseNumber(Short.valueOf(Short.MAX_VALUE), null, null, true));
		assertEquals(Integer.valueOf(Short.MIN_VALUE), parseNumber(Integer.valueOf(Short.MIN_VALUE), null, null, false));
		assertEquals(Long.valueOf(Short.MIN_VALUE), parseNumber(Long.valueOf(Short.MIN_VALUE), null, null, false));
		assertEquals(Integer.valueOf(Short.MIN_VALUE), parseNumber(Long.valueOf(Short.MIN_VALUE), null, null, true));
		assertEquals(Integer.valueOf(Short.MIN_VALUE), parseNumber(Short.valueOf(Short.MIN_VALUE), null, null, false));
		assertEquals(Integer.valueOf(Short.MIN_VALUE), parseNumber(Short.valueOf(Short.MIN_VALUE), null, null, true));

		assertEquals(Integer.valueOf(Integer.MAX_VALUE), parseNumber(Integer.valueOf(Integer.MAX_VALUE), null, null, false));
		assertEquals(Long.valueOf(Integer.MAX_VALUE), parseNumber(Long.valueOf(Integer.MAX_VALUE), null, null, false));
		assertEquals(Integer.valueOf(Integer.MAX_VALUE), parseNumber(Long.valueOf(Integer.MAX_VALUE), null, null, true));
		assertEquals(Integer.valueOf(Integer.MIN_VALUE), parseNumber(Integer.valueOf(Integer.MIN_VALUE), null, null, false));
		assertEquals(Long.valueOf(Integer.MIN_VALUE), parseNumber(Long.valueOf(Integer.MIN_VALUE), null, null, false));
		assertEquals(Integer.valueOf(Integer.MIN_VALUE), parseNumber(Long.valueOf(Integer.MIN_VALUE), null, null, true));

		assertEquals(Long.valueOf(Long.MAX_VALUE), parseNumber(Long.valueOf(Long.MAX_VALUE), null, null, false));
		assertEquals(BigInteger.valueOf(Long.MAX_VALUE), parseNumber(BigInteger.valueOf(Long.MAX_VALUE), null, null, false));
		assertEquals(Long.valueOf(Long.MAX_VALUE), parseNumber(BigInteger.valueOf(Long.MAX_VALUE), null, null, true));
		assertEquals(Long.valueOf(Long.MIN_VALUE), parseNumber(Long.valueOf(Long.MIN_VALUE), null, null, false));
		assertEquals(BigInteger.valueOf(Long.MIN_VALUE), parseNumber(BigInteger.valueOf(Long.MIN_VALUE), null, null, false));
		assertEquals(Long.valueOf(Long.MIN_VALUE), parseNumber(BigInteger.valueOf(Long.MIN_VALUE), null, null, true));

		assertEquals(Rational.valueOf(Integer.MAX_VALUE), parseNumber(Long.valueOf(Integer.MAX_VALUE), null, MathContext.DECIMAL64, false));
		assertEquals(Integer.valueOf(Integer.MAX_VALUE), parseNumber(Long.valueOf(Integer.MAX_VALUE), null, MathContext.DECIMAL64, true));
		assertEquals(Rational.valueOf(Integer.MIN_VALUE), parseNumber(Long.valueOf(Integer.MIN_VALUE), null, MathContext.DECIMAL64, false));
		assertEquals(Integer.valueOf(Integer.MIN_VALUE), parseNumber(Long.valueOf(Integer.MIN_VALUE), null, MathContext.DECIMAL64, true));

		assertEquals(Rational.valueOf(Long.MAX_VALUE), parseNumber(Long.valueOf(Long.MAX_VALUE), null, MathContext.DECIMAL128, false));
		assertEquals(Long.valueOf(Long.MAX_VALUE), parseNumber(Long.valueOf(Long.MAX_VALUE), null, MathContext.DECIMAL128, true));
		assertEquals(Rational.valueOf(Long.MIN_VALUE), parseNumber(Long.valueOf(Long.MIN_VALUE), null, MathContext.DECIMAL128, false));
		assertEquals(Long.valueOf(Long.MIN_VALUE), parseNumber(Long.valueOf(Long.MIN_VALUE), null, MathContext.DECIMAL128, true));

		BigInteger bigInteger = BigInteger.valueOf(Long.MAX_VALUE).multiply(BigInteger.TEN);
		assertEquals(Rational.valueOf(bigInteger), parseNumber(bigInteger, null, MathContext.DECIMAL128, false));
		assertEquals(bigInteger, parseNumber(bigInteger, null, MathContext.DECIMAL128, true));
		bigInteger = BigInteger.valueOf(Long.MIN_VALUE).multiply(BigInteger.TEN);
		assertEquals(Rational.valueOf(bigInteger), parseNumber(bigInteger, null, MathContext.DECIMAL128, false));
		assertEquals(bigInteger, parseNumber(bigInteger, null, MathContext.DECIMAL128, true));

		assertEquals(Rational.valueOf(Double.MAX_VALUE), parseNumber(Double.valueOf(Double.MAX_VALUE), null, MathContext.DECIMAL128, false));
		assertEquals(BigDecimal.valueOf(Double.MAX_VALUE).toBigIntegerExact(), parseNumber(Double.valueOf(Double.MAX_VALUE), null, MathContext.DECIMAL128, true));
		assertEquals(Rational.valueOf(Double.MIN_VALUE), parseNumber(Double.valueOf(Double.MIN_VALUE), null, MathContext.DECIMAL128, false));
		assertEquals(Rational.valueOf(Double.MIN_VALUE), parseNumber(Double.valueOf(Double.MIN_VALUE), null, MathContext.DECIMAL128, true));

		BigDecimal bigDecimal = BigDecimal.valueOf(Long.MAX_VALUE, -10);
		assertEquals(Rational.valueOf(bigDecimal), parseNumber(bigDecimal, null, MathContext.DECIMAL128, false));
		assertEquals(bigDecimal.toBigIntegerExact(), parseNumber(bigDecimal, null, MathContext.DECIMAL128, true));
		bigDecimal = BigDecimal.valueOf(Long.MIN_VALUE, -10);
		assertEquals(Rational.valueOf(bigDecimal), parseNumber(bigDecimal, null, MathContext.DECIMAL128, false));
		assertEquals(bigDecimal.toBigIntegerExact(), parseNumber(bigDecimal, null, MathContext.DECIMAL128, true));

		bigInteger = BigDecimal.valueOf(Long.MAX_VALUE, -10).toBigIntegerExact();
		Rational rational = Rational.valueOf(BigDecimal.valueOf(Long.MAX_VALUE, -10));
		assertEquals(rational, parseNumber(rational, null, MathContext.DECIMAL128, false));
		assertEquals(bigInteger, parseNumber(rational, null, MathContext.DECIMAL128, true));
		bigInteger = BigDecimal.valueOf(Long.MIN_VALUE, -10).toBigIntegerExact();
		rational = Rational.valueOf(BigDecimal.valueOf(Long.MIN_VALUE, -10));
		assertEquals(rational, parseNumber(rational, null, MathContext.DECIMAL128, false));
		assertEquals(bigInteger, parseNumber(rational, null, MathContext.DECIMAL128, true));

		rational = Rational.valueOf(Long.MAX_VALUE);
		assertEquals(rational, parseNumber(rational, null, MathContext.DECIMAL128, false));
		assertEquals(Long.valueOf(Long.MAX_VALUE), parseNumber(rational, null, MathContext.DECIMAL128, true));
		rational = Rational.valueOf(Long.MIN_VALUE);
		assertEquals(rational, parseNumber(rational, null, MathContext.DECIMAL128, false));
		assertEquals(Long.valueOf(Long.MIN_VALUE), parseNumber(rational, null, MathContext.DECIMAL128, true));

		rational = Rational.valueOf(Integer.MAX_VALUE);
		assertEquals(rational, parseNumber(rational, null, MathContext.DECIMAL128, false));
		assertEquals(Integer.valueOf(Integer.MAX_VALUE), parseNumber(rational, null, MathContext.DECIMAL128, true));
		rational = Rational.valueOf(Integer.MIN_VALUE);
		assertEquals(rational, parseNumber(rational, null, MathContext.DECIMAL128, false));
		assertEquals(Integer.valueOf(Integer.MIN_VALUE), parseNumber(rational, null, MathContext.DECIMAL128, true));

		rational = Rational.valueOf(Short.MAX_VALUE);
		assertEquals(rational, parseNumber(rational, null, MathContext.DECIMAL128, false));
		assertEquals(Integer.valueOf(Short.MAX_VALUE), parseNumber(rational, null, MathContext.DECIMAL128, true));
		rational = Rational.valueOf(Short.MIN_VALUE);
		assertEquals(rational, parseNumber(rational, null, MathContext.DECIMAL128, false));
		assertEquals(Integer.valueOf(Short.MIN_VALUE), parseNumber(rational, null, MathContext.DECIMAL128, true));

		rational = Rational.valueOf(Byte.MAX_VALUE);
		assertEquals(rational, parseNumber(rational, null, MathContext.DECIMAL128, false));
		assertEquals(Integer.valueOf(Byte.MAX_VALUE), parseNumber(rational, null, MathContext.DECIMAL128, true));
		rational = Rational.valueOf(Byte.MIN_VALUE);
		assertEquals(rational, parseNumber(rational, null, MathContext.DECIMAL128, false));
		assertEquals(Integer.valueOf(Byte.MIN_VALUE), parseNumber(rational, null, MathContext.DECIMAL128, true));

		rational = Rational.valueOf(3, 4);
		assertEquals(rational, parseNumber(rational, null, null, false));
		assertEquals(Double.valueOf(0.75), parseNumber(rational, null, null, true));

		// String
		assertEquals(BigInteger.ZERO, parseNumber("0", null, null, false));
		assertEquals(Integer.valueOf(0), parseNumber("0", null, null, true));
		assertEquals(BigInteger.ONE, parseNumber("1", null, null, false));
		assertEquals(Integer.valueOf(1), parseNumber("1", null, null, true));
		assertEquals(BigInteger.ONE.negate(), parseNumber("-1", null, null, false));
		assertEquals(Integer.valueOf(-1), parseNumber("-1", null, null, true));

		assertEquals(BigInteger.ZERO, parseNumber("0.00 E14", null, null, false));
		assertEquals(Integer.valueOf(0), parseNumber("0.00 E14", null, null, true));
		assertEquals(BigInteger.ONE, parseNumber("0.01 E2", null, null, false));
		assertEquals(Integer.valueOf(1), parseNumber("0.01 E2", null, null, true));
		assertEquals(BigInteger.ONE.negate(), parseNumber("-0.01 E2", null, null, false));
		assertEquals(Integer.valueOf(-1), parseNumber("-0.01 E2", null, null, true));

		assertEquals(BigInteger.ZERO, parseNumber("0x0.00 E-14", null, null, false));
		assertEquals(Integer.valueOf(0), parseNumber("0x0.00 E-14", null, null, true));
		assertEquals(BigInteger.ZERO, parseNumber("0x0.00 P-14", null, null, false));
		assertEquals(Integer.valueOf(0), parseNumber("0x0.00 P-14", null, null, true));
		assertEquals(BigInteger.ONE, parseNumber("0x0.01 E2", null, null, false));
		assertEquals(Integer.valueOf(1), parseNumber("0x0.01 E2", null, null, true));
		assertEquals(BigInteger.ONE, parseNumber("0x10 E-1", null, null, false));
		assertEquals(Integer.valueOf(1), parseNumber("0x10 E-1", null, null, true));
		assertEquals(BigInteger.ONE, parseNumber("0x10 P-4", null, null, false));
		assertEquals(Integer.valueOf(1), parseNumber("0x10 P-4", null, null, true));
		assertEquals(BigInteger.ONE.negate(), parseNumber("-0x0.01 E2", null, null, false));
		assertEquals(Integer.valueOf(-1), parseNumber("-0x0.01 E2", null, null, true));
		assertEquals(BigInteger.ONE.negate(), parseNumber("-0x10 E-1", null, null, false));
		assertEquals(Integer.valueOf(-1), parseNumber("-0x10 E-1", null, null, true));
		assertEquals(BigInteger.ONE.negate(), parseNumber("-0x10 P-4", null, null, false));
		assertEquals(Integer.valueOf(-1), parseNumber("-0x10 P-4", null, null, true));

		assertEquals(BigInteger.TEN, parseNumber("0x0.0a E2", null, null, false));
		assertEquals(Integer.valueOf(10), parseNumber("0x0.0a E2", null, null, true));
		assertEquals(BigInteger.TEN, parseNumber("0xa0 E-1", null, null, false));
		assertEquals(Integer.valueOf(10), parseNumber("0xa0 E-1", null, null, true));
		assertEquals(BigInteger.TEN, parseNumber("0xa0 P-4", null, null, false));
		assertEquals(Integer.valueOf(10), parseNumber("0xa0 P-4", null, null, true));
		assertEquals(BigInteger.TEN.negate(), parseNumber("-0x0.0a E2", null, null, false));
		assertEquals(Integer.valueOf(-10), parseNumber("-0x0.0a E2", null, null, true));
		assertEquals(BigInteger.TEN.negate(), parseNumber("-0x0,0a E2", Locale.GERMAN, null, false));
		assertEquals(Integer.valueOf(-10), parseNumber("-0x0,0a E2", Locale.GERMAN, null, true));
		assertEquals(BigInteger.TEN.negate(), parseNumber("-0xa0 E-1", null, null, false));
		assertEquals(Integer.valueOf(-10), parseNumber("-0xa0 E-1", null, null, true));
		assertEquals(BigInteger.TEN.negate(), parseNumber("-0xa0p-4", null, null, false));
		assertEquals(Integer.valueOf(-10), parseNumber("-0xa0p-4", null, null, true));

		assertEquals(Rational.valueOf(34.45), parseNumber("  34.45", null, null, true));
		assertEquals(Double.valueOf(344.5), parseNumber("34.45 E1  ", null, null, true));
		assertEquals(Integer.valueOf(3445), parseNumber("34.45E2", null, null, true));
		assertEquals(BigInteger.valueOf(3445), parseNumber("34.45 E+2", null, null, false));
		assertNull(parseNumber("  34.45  E+2 ", null, null, false));

		assertEquals(Rational.valueOf(34.45), parseNumber("  34.45", Locale.GERMAN, null, true));
		assertEquals(Double.valueOf(344.5), parseNumber("34.45 E1  ", Locale.GERMAN, null, true));
		assertEquals(Integer.valueOf(3445), parseNumber("34.45E2", Locale.GERMAN, null, true));
		assertEquals(BigInteger.valueOf(3445), parseNumber("34.45 E+2", Locale.GERMAN, null, false));
		assertNull(parseNumber("  34.45  E+2 ", Locale.GERMAN, null, false));

		assertEquals(Rational.valueOf(34.45), parseNumber("  34,45", Locale.GERMAN, null, true));
		assertEquals(Double.valueOf(344.5), parseNumber("34,45 E1  ", Locale.GERMAN, null, true));
		assertEquals(Integer.valueOf(3445), parseNumber("34,45E2", Locale.GERMAN, null, true));
		assertEquals(BigInteger.valueOf(3445), parseNumber("34,45 E+2", Locale.GERMAN, null, false));
		assertNull(parseNumber("  34,45  E+2 ", Locale.GERMAN, null, false));

		assertEquals(Rational.valueOf(49, 4), parseNumber("  12.25", null, null, false));
		assertEquals(Double.valueOf(12.25), parseNumber(" 12.25 ", null, null, true));
		assertNull(parseNumber(" 12 .25 ", null, null, true));
		assertNull(parseNumber(" 12. 25 ", null, null, true));

		assertEquals(Rational.valueOf("674486530163493/8589934592"), parseNumber("  0x265712.2094325 P-5", null, null, false));
		assertEquals(BigInteger.valueOf(78521), parseNumber("  0x265712.2094325 P-5", null, new MathContext(5, RoundingMode.HALF_EVEN), false));
		assertEquals(Integer.valueOf(78521), parseNumber("  0x265712.2094325 P-5", null, new MathContext(5, RoundingMode.HALF_EVEN), true));
		assertEquals(BigInteger.valueOf(643240000), parseNumber("  0x265712.2094325 E2", null, new MathContext(5, RoundingMode.HALF_EVEN), false));
		assertEquals(Integer.valueOf(643240000), parseNumber("  0x265712.2094325 E2", null, new MathContext(5, RoundingMode.HALF_EVEN), true));
		assertEquals(BigInteger.valueOf(172660000000000000L), parseNumber("  0x0.2657122094325 EF", null, new MathContext(5, RoundingMode.DOWN), false));
		assertEquals(Long.valueOf(172670000000000000L), parseNumber("  0x0.2657122094325 EF", null, new MathContext(5, RoundingMode.HALF_EVEN), true));
		assertEquals(BigInteger.valueOf(172668551721854208L), parseNumber("  0x0.2657122094325 EF", null, null, false));
		assertEquals(Long.valueOf(172668551721854208L), parseNumber("  0x0.2657122094325 EF", null, null, true));
	}

	@Test
	public void parseNumberRoundingOverloadTest() {
		// Invalid
		assertNull(parseNumber(null, null, 0, null, false));
		assertNull(parseNumber(LoggerFactory.getILoggerFactory(), null, 0, null, false));
		assertNull(parseNumber(new StringBuilder("foobar"), null, 0, null, false));
		assertNull(parseNumber("foo 2 bar", null, 0, null, false));
		assertNull(parseNumber(" \t", null, 0, null, false));
		assertNull(parseNumber(" ", null, 0, null, false));
		assertNull(parseNumber("", null, 0, null, false));

		// Number
		assertEquals(Integer.valueOf(Byte.MAX_VALUE), parseNumber(Integer.valueOf(Byte.MAX_VALUE), null, 0, null, false));
		assertEquals(Long.valueOf(Byte.MAX_VALUE), parseNumber(Long.valueOf(Byte.MAX_VALUE), null, 0, null, false));
		assertEquals(Integer.valueOf(Byte.MAX_VALUE), parseNumber(Long.valueOf(Byte.MAX_VALUE), null, 0, null, true));
		assertEquals(Integer.valueOf(Byte.MAX_VALUE), parseNumber(Byte.valueOf(Byte.MAX_VALUE), null, 0, null, false));
		assertEquals(Integer.valueOf(Byte.MAX_VALUE), parseNumber(Byte.valueOf(Byte.MAX_VALUE), null, 0, null, true));
		assertEquals(Integer.valueOf(Byte.MIN_VALUE), parseNumber(Integer.valueOf(Byte.MIN_VALUE), null, 0, null, false));
		assertEquals(Long.valueOf(Byte.MIN_VALUE), parseNumber(Long.valueOf(Byte.MIN_VALUE), null, 0, null, false));
		assertEquals(Integer.valueOf(Byte.MIN_VALUE), parseNumber(Long.valueOf(Byte.MIN_VALUE), null, 0, null, true));
		assertEquals(Integer.valueOf(Byte.MIN_VALUE), parseNumber(Byte.valueOf(Byte.MIN_VALUE), null, 0, null, false));
		assertEquals(Integer.valueOf(Byte.MIN_VALUE), parseNumber(Byte.valueOf(Byte.MIN_VALUE), null, 0, null, true));

		assertEquals(Integer.valueOf(Short.MAX_VALUE), parseNumber(Integer.valueOf(Short.MAX_VALUE), null, 0, null, false));
		assertEquals(Long.valueOf(Short.MAX_VALUE), parseNumber(Long.valueOf(Short.MAX_VALUE), null, 0, null, false));
		assertEquals(Integer.valueOf(Short.MAX_VALUE), parseNumber(Long.valueOf(Short.MAX_VALUE), null, 0, null, true));
		assertEquals(Integer.valueOf(Short.MAX_VALUE), parseNumber(Short.valueOf(Short.MAX_VALUE), null, 0, null, false));
		assertEquals(Integer.valueOf(Short.MAX_VALUE), parseNumber(Short.valueOf(Short.MAX_VALUE), null, 0, null, true));
		assertEquals(Integer.valueOf(Short.MIN_VALUE), parseNumber(Integer.valueOf(Short.MIN_VALUE), null, 0, null, false));
		assertEquals(Long.valueOf(Short.MIN_VALUE), parseNumber(Long.valueOf(Short.MIN_VALUE), null, 0, null, false));
		assertEquals(Integer.valueOf(Short.MIN_VALUE), parseNumber(Long.valueOf(Short.MIN_VALUE), null, 0, null, true));
		assertEquals(Integer.valueOf(Short.MIN_VALUE), parseNumber(Short.valueOf(Short.MIN_VALUE), null, 0, null, false));
		assertEquals(Integer.valueOf(Short.MIN_VALUE), parseNumber(Short.valueOf(Short.MIN_VALUE), null, 0, null, true));

		assertEquals(Integer.valueOf(Integer.MAX_VALUE), parseNumber(Integer.valueOf(Integer.MAX_VALUE), null, 0, null, false));
		assertEquals(Long.valueOf(Integer.MAX_VALUE), parseNumber(Long.valueOf(Integer.MAX_VALUE), null, 0, null, false));
		assertEquals(Integer.valueOf(Integer.MAX_VALUE), parseNumber(Long.valueOf(Integer.MAX_VALUE), null, 0, null, true));
		assertEquals(Integer.valueOf(Integer.MIN_VALUE), parseNumber(Integer.valueOf(Integer.MIN_VALUE), null, 0, null, false));
		assertEquals(Long.valueOf(Integer.MIN_VALUE), parseNumber(Long.valueOf(Integer.MIN_VALUE), null, 0, null, false));
		assertEquals(Integer.valueOf(Integer.MIN_VALUE), parseNumber(Long.valueOf(Integer.MIN_VALUE), null, 0, null, true));

		assertEquals(Long.valueOf(Long.MAX_VALUE), parseNumber(Long.valueOf(Long.MAX_VALUE), null, 0, null, false));
		assertEquals(BigInteger.valueOf(Long.MAX_VALUE), parseNumber(BigInteger.valueOf(Long.MAX_VALUE), null, 0, null, false));
		assertEquals(Long.valueOf(Long.MAX_VALUE), parseNumber(BigInteger.valueOf(Long.MAX_VALUE), null, 0, null, true));
		assertEquals(Long.valueOf(Long.MIN_VALUE), parseNumber(Long.valueOf(Long.MIN_VALUE), null, 0, null, false));
		assertEquals(BigInteger.valueOf(Long.MIN_VALUE), parseNumber(BigInteger.valueOf(Long.MIN_VALUE), null, 0, null, false));
		assertEquals(Long.valueOf(Long.MIN_VALUE), parseNumber(BigInteger.valueOf(Long.MIN_VALUE), null, 0, null, true));

		assertEquals(Rational.valueOf(Integer.MAX_VALUE), parseNumber(Long.valueOf(Integer.MAX_VALUE), null, 0, RoundingMode.HALF_UP, false));
		assertEquals(Integer.valueOf(Integer.MAX_VALUE), parseNumber(Long.valueOf(Integer.MAX_VALUE), null, 0, RoundingMode.HALF_UP, true));
		assertEquals(Rational.valueOf(Integer.MIN_VALUE), parseNumber(Long.valueOf(Integer.MIN_VALUE), null, 0, RoundingMode.HALF_UP, false));
		assertEquals(Integer.valueOf(Integer.MIN_VALUE), parseNumber(Long.valueOf(Integer.MIN_VALUE), null, 0, RoundingMode.HALF_UP, true));

		assertEquals(Rational.valueOf(Long.MAX_VALUE), parseNumber(Long.valueOf(Long.MAX_VALUE), null, 0, RoundingMode.CEILING, false));
		assertEquals(Long.valueOf(Long.MAX_VALUE), parseNumber(Long.valueOf(Long.MAX_VALUE), null, 0, RoundingMode.CEILING, true));
		assertEquals(Rational.valueOf(Long.MIN_VALUE), parseNumber(Long.valueOf(Long.MIN_VALUE), null, 0, RoundingMode.CEILING, false));
		assertEquals(Long.valueOf(Long.MIN_VALUE), parseNumber(Long.valueOf(Long.MIN_VALUE), null, 0, RoundingMode.CEILING, true));

		BigInteger bigInteger = BigInteger.valueOf(Long.MAX_VALUE).multiply(BigInteger.TEN);
		assertEquals(Rational.valueOf(bigInteger), parseNumber(bigInteger, null, 0, RoundingMode.CEILING, false));
		assertEquals(bigInteger, parseNumber(bigInteger, null, 0, RoundingMode.CEILING, true));
		bigInteger = BigInteger.valueOf(Long.MIN_VALUE).multiply(BigInteger.TEN);
		assertEquals(Rational.valueOf(bigInteger), parseNumber(bigInteger, null, 0, RoundingMode.CEILING, false));
		assertEquals(bigInteger, parseNumber(bigInteger, null, 0, RoundingMode.CEILING, true));

		assertEquals(Rational.valueOf(Double.MAX_VALUE), parseNumber(Double.valueOf(Double.MAX_VALUE), null, 0, RoundingMode.CEILING, false));
		assertEquals(BigDecimal.valueOf(Double.MAX_VALUE).toBigIntegerExact(), parseNumber(Double.valueOf(Double.MAX_VALUE), null, 0, RoundingMode.CEILING, true));
		assertEquals(Rational.ONE, parseNumber(Double.valueOf(Double.MIN_VALUE), null, 0, RoundingMode.CEILING, false));
		assertEquals(Integer.valueOf(1), parseNumber(Double.valueOf(Double.MIN_VALUE), null, 0, RoundingMode.CEILING, true));
		assertEquals(Integer.valueOf(0), parseNumber(Double.valueOf(Double.MIN_VALUE), null, 0, RoundingMode.DOWN, true));
		assertEquals(Integer.valueOf(0), parseNumber(Double.valueOf(Double.MIN_VALUE), null, 0, RoundingMode.FLOOR, true));
		assertEquals(Integer.valueOf(0), parseNumber(Double.valueOf(Double.MIN_VALUE), null, 0, RoundingMode.HALF_DOWN, true));
		assertEquals(Integer.valueOf(0), parseNumber(Double.valueOf(Double.MIN_VALUE), null, 0, RoundingMode.HALF_EVEN, true));
		assertEquals(Integer.valueOf(0), parseNumber(Double.valueOf(Double.MIN_VALUE), null, 0, RoundingMode.HALF_UP, true));
		assertEquals(Integer.valueOf(1), parseNumber(Double.valueOf(Double.MIN_VALUE), null, 0, RoundingMode.UP, true));

		BigDecimal bigDecimal = BigDecimal.valueOf(Long.MAX_VALUE, -10);
		assertEquals(Rational.valueOf(bigDecimal), parseNumber(bigDecimal, null, 0, RoundingMode.CEILING, false));
		assertEquals(bigDecimal.toBigIntegerExact(), parseNumber(bigDecimal, null, 0, RoundingMode.CEILING, true));
		bigDecimal = BigDecimal.valueOf(Long.MIN_VALUE, -10);
		assertEquals(Rational.valueOf(bigDecimal), parseNumber(bigDecimal, null, 0, RoundingMode.CEILING, false));
		assertEquals(bigDecimal.toBigIntegerExact(), parseNumber(bigDecimal, null, 0, RoundingMode.CEILING, true));

		bigInteger = BigDecimal.valueOf(Long.MAX_VALUE, -10).toBigIntegerExact();
		Rational rational = Rational.valueOf(BigDecimal.valueOf(Long.MAX_VALUE, -10));
		assertEquals(rational, parseNumber(rational, null, 0, RoundingMode.CEILING, false));
		assertEquals(bigInteger, parseNumber(rational, null, 0, RoundingMode.CEILING, true));
		bigInteger = BigDecimal.valueOf(Long.MIN_VALUE, -10).toBigIntegerExact();
		rational = Rational.valueOf(BigDecimal.valueOf(Long.MIN_VALUE, -10));
		assertEquals(rational, parseNumber(rational, null, 0, RoundingMode.CEILING, false));
		assertEquals(bigInteger, parseNumber(rational, null, 0, RoundingMode.CEILING, true));

		rational = Rational.valueOf(Long.MAX_VALUE);
		assertEquals(rational, parseNumber(rational, null, 0, RoundingMode.CEILING, false));
		assertEquals(Long.valueOf(Long.MAX_VALUE), parseNumber(rational, null, 0, RoundingMode.CEILING, true));
		rational = Rational.valueOf(Long.MIN_VALUE);
		assertEquals(rational, parseNumber(rational, null, 0, RoundingMode.CEILING, false));
		assertEquals(Long.valueOf(Long.MIN_VALUE), parseNumber(rational, null, 0, RoundingMode.CEILING, true));

		rational = Rational.valueOf(Integer.MAX_VALUE);
		assertEquals(rational, parseNumber(rational, null, 0, RoundingMode.CEILING, false));
		assertEquals(Integer.valueOf(Integer.MAX_VALUE), parseNumber(rational, null, 0, RoundingMode.CEILING, true));
		rational = Rational.valueOf(Integer.MIN_VALUE);
		assertEquals(rational, parseNumber(rational, null, 0, RoundingMode.CEILING, false));
		assertEquals(Integer.valueOf(Integer.MIN_VALUE), parseNumber(rational, null, 0, RoundingMode.CEILING, true));

		rational = Rational.valueOf(Short.MAX_VALUE);
		assertEquals(rational, parseNumber(rational, null, 0, RoundingMode.CEILING, false));
		assertEquals(Integer.valueOf(Short.MAX_VALUE), parseNumber(rational, null, 0, RoundingMode.CEILING, true));
		rational = Rational.valueOf(Short.MIN_VALUE);
		assertEquals(rational, parseNumber(rational, null, 0, RoundingMode.CEILING, false));
		assertEquals(Integer.valueOf(Short.MIN_VALUE), parseNumber(rational, null, 0, RoundingMode.CEILING, true));

		rational = Rational.valueOf(Byte.MAX_VALUE);
		assertEquals(rational, parseNumber(rational, null, 0, RoundingMode.CEILING, false));
		assertEquals(Integer.valueOf(Byte.MAX_VALUE), parseNumber(rational, null, 0, RoundingMode.CEILING, true));
		rational = Rational.valueOf(Byte.MIN_VALUE);
		assertEquals(rational, parseNumber(rational, null, 0, RoundingMode.CEILING, false));
		assertEquals(Integer.valueOf(Byte.MIN_VALUE), parseNumber(rational, null, 0, RoundingMode.CEILING, true));

		rational = Rational.valueOf(3, 4);
		assertEquals(rational, parseNumber(rational, null, 0, null, false));
		assertEquals(Double.valueOf(0.75), parseNumber(rational, null, 0, null, true));
		assertEquals(Rational.valueOf(4, 5), parseNumber(rational, null, 1, RoundingMode.HALF_EVEN, false));
		assertEquals(Rational.valueOf(4, 5), parseNumber(rational, null, 1, RoundingMode.HALF_EVEN, true));

		// String
		assertEquals(BigInteger.ZERO, parseNumber("0", null, 0, null, false));
		assertEquals(Integer.valueOf(0), parseNumber("0", null, 0, null, true));
		assertEquals(BigInteger.ONE, parseNumber("1", null, 0, null, false));
		assertEquals(Integer.valueOf(1), parseNumber("1", null, 0, null, true));
		assertEquals(BigInteger.ONE.negate(), parseNumber("-1", null, 0, null, false));
		assertEquals(Integer.valueOf(-1), parseNumber("-1", null, 0, null, true));

		assertEquals(BigInteger.ZERO, parseNumber("0.00 E14", null, 0, null, false));
		assertEquals(Integer.valueOf(0), parseNumber("0.00 E14", null, 0, null, true));
		assertEquals(BigInteger.ONE, parseNumber("0.01 E2", null, 0, null, false));
		assertEquals(Integer.valueOf(1), parseNumber("0.01 E2", null, 0, null, true));
		assertEquals(BigInteger.ONE.negate(), parseNumber("-0.01 E2", null, 0, null, false));
		assertEquals(Integer.valueOf(-1), parseNumber("-0.01 E2", null, 0, null, true));

		assertEquals(BigInteger.ZERO, parseNumber("0x0.00 E-14", null, 0, null, false));
		assertEquals(Integer.valueOf(0), parseNumber("0x0.00 E-14", null, 0, null, true));
		assertEquals(BigInteger.ZERO, parseNumber("0x0.00 P-14", null, 0, null, false));
		assertEquals(Integer.valueOf(0), parseNumber("0x0.00 P-14", null, 0, null, true));
		assertEquals(BigInteger.ONE, parseNumber("0x0.01 E2", null, 0, null, false));
		assertEquals(Integer.valueOf(1), parseNumber("0x0.01 E2", null, 0, null, true));
		assertEquals(BigInteger.ONE, parseNumber("0x10 E-1", null, 0, null, false));
		assertEquals(Integer.valueOf(1), parseNumber("0x10 E-1", null, 0, null, true));
		assertEquals(BigInteger.ONE, parseNumber("0x10 P-4", null, 0, null, false));
		assertEquals(Integer.valueOf(1), parseNumber("0x10 P-4", null, 0, null, true));
		assertEquals(BigInteger.ONE.negate(), parseNumber("-0x0.01 E2", null, 0, null, false));
		assertEquals(Integer.valueOf(-1), parseNumber("-0x0.01 E2", null, 0, null, true));
		assertEquals(BigInteger.ONE.negate(), parseNumber("-0x10 E-1", null, 0, null, false));
		assertEquals(Integer.valueOf(-1), parseNumber("-0x10 E-1", null, 0, null, true));
		assertEquals(BigInteger.ONE.negate(), parseNumber("-0x10 P-4", null, 0, null, false));
		assertEquals(Integer.valueOf(-1), parseNumber("-0x10 P-4", null, 0, null, true));

		assertEquals(BigInteger.TEN, parseNumber("0x0.0a E2", null, 0, null, false));
		assertEquals(Integer.valueOf(10), parseNumber("0x0.0a E2", null, 0, null, true));
		assertEquals(BigInteger.TEN, parseNumber("0xa0 E-1", null, 0, null, false));
		assertEquals(Integer.valueOf(10), parseNumber("0xa0 E-1", null, 0, null, true));
		assertEquals(BigInteger.TEN, parseNumber("0xa0 P-4", null, 0, null, false));
		assertEquals(Integer.valueOf(10), parseNumber("0xa0 P-4", null, 0, null, true));
		assertEquals(BigInteger.TEN.negate(), parseNumber("-0x0.0a E2", null, 0, null, false));
		assertEquals(Integer.valueOf(-10), parseNumber("-0x0.0a E2", null, 0, null, true));
		assertEquals(BigInteger.TEN.negate(), parseNumber("-0x0,0a E2", Locale.GERMAN, 0, null, false));
		assertEquals(Integer.valueOf(-10), parseNumber("-0x0,0a E2", Locale.GERMAN, 0, null, true));
		assertEquals(BigInteger.TEN.negate(), parseNumber("-0xa0 E-1", null, 0, null, false));
		assertEquals(Integer.valueOf(-10), parseNumber("-0xa0 E-1", null, 0, null, true));
		assertEquals(BigInteger.TEN.negate(), parseNumber("-0xa0p-4", null, 0, null, false));
		assertEquals(Integer.valueOf(-10), parseNumber("-0xa0p-4", null, 0, null, true));

		assertEquals(Rational.valueOf(34.45), parseNumber("  34.45", null, 0, null, true));
		assertEquals(Double.valueOf(344.5), parseNumber("34.45 E1  ", null, 0, null, true));
		assertEquals(Integer.valueOf(3445), parseNumber("34.45E2", null, 0, null, true));
		assertEquals(BigInteger.valueOf(3445), parseNumber("34.45 E+2", null, 0, null, false));
		assertNull(parseNumber("  34.45  E+2 ", null, 0, null, false));

		assertEquals(Rational.valueOf(34.45), parseNumber("  34.45", Locale.GERMAN, 0, null, true));
		assertEquals(Double.valueOf(344.5), parseNumber("34.45 E1  ", Locale.GERMAN, 0, null, true));
		assertEquals(Integer.valueOf(3445), parseNumber("34.45E2", Locale.GERMAN, 0, null, true));
		assertEquals(BigInteger.valueOf(3445), parseNumber("34.45 E+2", Locale.GERMAN, 0, null, false));
		assertNull(parseNumber("  34.45  E+2 ", Locale.GERMAN, 0, null, false));

		assertEquals(Rational.valueOf(34.45), parseNumber("  34,45", Locale.GERMAN, 0, null, true));
		assertEquals(Double.valueOf(344.5), parseNumber("34,45 E1  ", Locale.GERMAN, 0, null, true));
		assertEquals(Integer.valueOf(3445), parseNumber("34,45E2", Locale.GERMAN, 0, null, true));
		assertEquals(BigInteger.valueOf(3445), parseNumber("34,45 E+2", Locale.GERMAN, 0, null, false));
		assertNull(parseNumber("  34,45  E+2 ", Locale.GERMAN, 0, null, false));

		assertEquals(Rational.valueOf(49, 4), parseNumber("  12.25", null, 0, null, false));
		assertEquals(Double.valueOf(12.25), parseNumber(" 12.25 ", null, 0, null, true));
		assertNull(parseNumber(" 12 .25 ", null, 0, null, true));
		assertNull(parseNumber(" 12. 25 ", null, 0, null, true));

		assertEquals(Rational.valueOf("674486530163493/8589934592"), parseNumber("  0x265712.2094325 P-5", null, 0, null, false));
		assertEquals(BigInteger.valueOf(172668551721854208L), parseNumber("  0x0.2657122094325 EF", null, 0, null, false));
		assertEquals(Long.valueOf(172668551721854208L), parseNumber("  0x0.2657122094325 EF", null, 0, null, true));
		assertEquals(BigInteger.valueOf(172668551721854000L), parseNumber("  0x0.2657122094325 EF", null, -3, RoundingMode.HALF_UP, false));
		assertEquals(Long.valueOf(172668551721854000L), parseNumber("  0x0.2657122094325 EF", null, -3, RoundingMode.HALF_EVEN, true));
	}

	@Test
	public void parseNumberTest() {
		// Invalid
		assertNull(parseNumber(null, null, null, 0, null, false));
		assertNull(parseNumber(LoggerFactory.getILoggerFactory(), null, null, 0, null, false));
		assertNull(parseNumber(new StringBuilder("foobar"), null, null, 0, null, false));
		assertNull(parseNumber("foo 2 bar", null, null, 0, null, false));
		assertNull(parseNumber(" \t", null, null, 0, null, false));
		assertNull(parseNumber(" ", null, null, 0, null, false));
		assertNull(parseNumber("", null, null, 0, null, false));

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

	@Test
	public void isDoubleValueExactTest() {
		// BigDecimal
		assertFalse(isDoubleValueExact((BigDecimal) null));
		assertTrue(isDoubleValueExact(BigDecimal.valueOf(5.25)));
		assertFalse(isDoubleValueExact(BigDecimal.valueOf(5.2)));

		// Rational
		assertFalse(isDoubleValueExact((Rational) null));
		assertTrue(isDoubleValueExact(Rational.valueOf(5.25)));
		assertFalse(isDoubleValueExact(Rational.valueOf(5.2)));
		assertTrue(isDoubleValueExact(Rational.valueOf(Double.NaN)));
		assertTrue(isDoubleValueExact(Rational.valueOf(Double.POSITIVE_INFINITY)));
		assertTrue(isDoubleValueExact(Rational.valueOf(Double.NEGATIVE_INFINITY)));
	}
}
