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

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static net.pms.util.TitleCase.*;
import org.junit.Test;

public class TitleCaseTest {

	@Test
	public void testIrregularCase() {
		assertTrue(IRREGULAR_CASE.matcher("fooBar").find());
		assertTrue(IRREGULAR_CASE.matcher(".ooBar").find());
		assertFalse(IRREGULAR_CASE.matcher(".0Bar").find());
		assertFalse(IRREGULAR_CASE.matcher("foobar").find());
		assertFalse(IRREGULAR_CASE.matcher("Foobar").find());
		assertFalse(IRREGULAR_CASE.matcher("FOOBAR").find());
		assertTrue(IRREGULAR_CASE.matcher("FooBar").find());
		assertTrue(IRREGULAR_CASE.matcher("FOOBaR").find());
	}

	@Test
	public void testPunct() {
		assertTrue(PUNCT.matcher(".ooBar").find());
		assertFalse(PUNCT.matcher("FooBar").find());
		assertTrue(PUNCT.matcher("foo(bar)").find());
		assertTrue(PUNCT.matcher("foo@bar").find());
		assertTrue(PUNCT.matcher("foo\u061Bbar").find());
	}

	@Test
	public void testConvert() {
		assertNull(convert(null, null));
		assertEquals("", convert("", null));
		Locale turkish = Locale.forLanguageTag("tr");

		String s1 = "foobar fooBar FooBar .foobar foo, bar foo:bar,";
		String s2 = " v foo and bar  v f'obaro i v";
		String s3 = "foo\nfoo@bar\rfoo_BAR\r\nf`bar foo-bar-fo FOO/BAR /foobar\nbar";

		// convert(String, Locale)
		assertEquals("Foobar fooBar FooBar .Foobar Foo, Bar Foo:bar,", convert(s1, Locale.ROOT));
		assertEquals("V Foo and Bar v F'Obaro I V", convert(s2, Locale.ROOT));
		assertEquals("Foo\nfoo@bar\nfoo_BAR\nF`Bar Foo-Bar-Fo Foo/Bar /foobar\nBar", convert(s3, Locale.ROOT));
		assertEquals("Foobar fooBar FooBar .Foobar Foo, Bar Foo:bar,", convert(s1, turkish));
		assertEquals("V Foo and Bar v F'Obaro İ V", convert(s2, turkish));
		assertEquals("Foo\nfoo@bar\nfoo_BAR\nF`Bar Foo-Bar-Fo Foo/Bar /foobar\nBar", convert(s3, turkish));

		// convert(String, Locale, keepLower)
		List<String> lower = Arrays.asList("bar", "foobar");
		assertEquals("Foobar fooBar FooBar .Foobar Foo, Bar Foo:bar,", convert(s1, Locale.ROOT, null));
		assertEquals("V Foo and Bar v F'Obaro I V", convert(s2, Locale.ROOT, null));
		assertEquals("Foo\nfoo@bar\nfoo_BAR\nF`Bar Foo-Bar-Fo Foo/Bar /foobar\nBar", convert(s3, Locale.ROOT, null));
		assertEquals("Foobar fooBar FooBar .Foobar Foo, Bar Foo:bar,", convert(s1, turkish, null));
		assertEquals("V Foo and Bar v F'Obaro İ V", convert(s2, turkish, null));
		assertEquals("Foo\nfoo@bar\nfoo_BAR\nF`Bar Foo-Bar-Fo Foo/Bar /foobar\nBar", convert(s3, turkish, null));
		assertEquals("Foobar fooBar FooBar .Foobar Foo, bar Foo:bar,", convert(s1, Locale.ROOT, lower));
		assertEquals("V Foo And bar V F'Obaro I V", convert(s2, Locale.ROOT, lower));
		assertEquals("Foo\nfoo@bar\nfoo_BAR\nF`Bar Foo-bar-Fo Foo/bar /foobar\nBar", convert(s3, Locale.ROOT, lower));
		assertEquals("Foobar fooBar FooBar .Foobar Foo, bar Foo:bar,", convert(s1, turkish, lower));
		assertEquals("V Foo And bar V F'Obaro İ V", convert(s2, turkish, lower));
		assertEquals("Foo\nfoo@bar\nfoo_BAR\nF`Bar Foo-bar-Fo Foo/bar /foobar\nBar", convert(s3, turkish, lower));

		// convert(String, boolean, Locale, keepLower)
		assertEquals("Foobar fooBar FooBar .Foobar Foo, Bar Foo:bar,", convert(s1, false, Locale.ROOT, null));
		assertEquals("V Foo and Bar v F'Obaro I V", convert(s2, false, Locale.ROOT, null));
		assertEquals("Foo\nfoo@bar\nfoo_BAR\nF`Bar Foo-Bar-Fo Foo/Bar /foobar\nBar", convert(s3, false, Locale.ROOT, null));
		assertEquals("Foobar fooBar FooBar .Foobar Foo, Bar Foo:bar,", convert(s1, false, turkish, null));
		assertEquals("V Foo and Bar v F'Obaro İ V", convert(s2, false, turkish, null));
		assertEquals("Foo\nfoo@bar\nfoo_BAR\nF`Bar Foo-Bar-Fo Foo/Bar /foobar\nBar", convert(s3, false, turkish, null));
		assertEquals("Foobar fooBar FooBar .Foobar Foo, bar Foo:bar,", convert(s1, false, Locale.ROOT, lower));
		assertEquals("V Foo And bar V F'Obaro I V", convert(s2, false, Locale.ROOT, lower));
		assertEquals("Foo\nfoo@bar\nfoo_BAR\nF`Bar Foo-bar-Fo Foo/bar /foobar\nBar", convert(s3, false, Locale.ROOT, lower));
		assertEquals("Foobar fooBar FooBar .Foobar Foo, bar Foo:bar,", convert(s1, false, turkish, lower));
		assertEquals("V Foo And bar V F'Obaro İ V", convert(s2, false, turkish, lower));
		assertEquals("Foo\nfoo@bar\nfoo_BAR\nF`Bar Foo-bar-Fo Foo/bar /foobar\nBar", convert(s3, false, turkish, lower));

		assertEquals("Foobar fooBar FooBar .Foobar Foo, Bar Foo:bar,", convert(s1, true, Locale.ROOT, null));
		assertEquals("V Foo and Bar v F'Obaro I V", convert(s2, true, Locale.ROOT, null));
		assertEquals("Foo foo@bar foo_BAR F`Bar Foo-Bar-Fo Foo/Bar /foobar Bar", convert(s3, true, Locale.ROOT, null));
		assertEquals("Foobar fooBar FooBar .Foobar Foo, Bar Foo:bar,", convert(s1, true, turkish, null));
		assertEquals("V Foo and Bar v F'Obaro İ V", convert(s2, true, turkish, null));
		assertEquals("Foo foo@bar foo_BAR F`Bar Foo-Bar-Fo Foo/Bar /foobar Bar", convert(s3, true, turkish, null));
		assertEquals("Foobar fooBar FooBar .Foobar Foo, bar Foo:bar,", convert(s1, true, Locale.ROOT, lower));
		assertEquals("V Foo And bar V F'Obaro I V", convert(s2, true, Locale.ROOT, lower));
		assertEquals("Foo foo@bar foo_BAR F`Bar Foo-bar-Fo Foo/bar /foobar Bar", convert(s3, true, Locale.ROOT, lower));
		assertEquals("Foobar fooBar FooBar .Foobar Foo, bar Foo:bar,", convert(s1, true, turkish, lower));
		assertEquals("V Foo And bar V F'Obaro İ V", convert(s2, true, turkish, lower));
		assertEquals("Foo foo@bar foo_BAR F`Bar Foo-bar-Fo Foo/bar /foobar Bar", convert(s3, true, turkish, lower));
	}
}
