/*
 * PS3 Media Server, for streaming media to your PS3.
 * Copyright (C) 2008-2013 A. Brochard.
 *
 * This program is free software; you can redistribute it and/or
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

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertArrayEquals;
import org.junit.Test;

public class VersionTest {
	private final static Version version(String version) {
		return new Version(version);
	}

	private final static Version version(String version, boolean hex) {
		return new Version(version, hex);
	}

	private final static Version version(int major) {
		return new Version(major);
	}

	private final static Version version(int major, int minor) {
		return new Version(major, minor);
	}

	private final static Version version(int major, int minor, int revision) {
		return new Version(major, minor, revision);
	}

	private final static Version version(int major, int minor, int revision, int build) {
		return new Version(major, minor, revision, build);
	}

	private static void assertVersionEquals(Version v1, Version v2) {
		// non-nullity
		assertFalse(v1 == null);
		assertFalse(v2 == null);

		// symmetry (and equality)
		assertTrue(v1.equals(v2));
		assertTrue(v2.equals(v1));

		// reflexivity
		assertTrue(v1.equals(v1));
		assertTrue(v2.equals(v2));

		// consistency
		assertTrue(v1.equals(v2));
		assertTrue(v2.equals(v1));

		assertThat(v1.hashCode()).isEqualTo(v1.hashCode());
		assertThat(v2.hashCode()).isEqualTo(v2.hashCode());
		assertThat(v1.hashCode()).isEqualTo(v2.hashCode());
		assertThat(v2.hashCode()).isEqualTo(v1.hashCode());

		assertFalse(v1.isGreaterThan(v1));
		assertFalse(v2.isGreaterThan(v2));
		assertFalse(v1.isGreaterThan(v2));
		assertFalse(v2.isGreaterThan(v1));

		assertFalse(v1.isLessThan(v1));
		assertFalse(v2.isLessThan(v2));
		assertFalse(v1.isLessThan(v2));
		assertFalse(v2.isLessThan(v1));

		assertTrue(v1.isGreaterThanOrEqualTo(v1));
		assertTrue(v2.isGreaterThanOrEqualTo(v2));
		assertTrue(v1.isGreaterThanOrEqualTo(v2));
		assertTrue(v2.isGreaterThanOrEqualTo(v1));

		assertTrue(v1.isLessThanOrEqualTo(v1));
		assertTrue(v2.isLessThanOrEqualTo(v2));
		assertTrue(v1.isLessThanOrEqualTo(v2));
		assertTrue(v2.isLessThanOrEqualTo(v1));
	}

	private static void assertVersionEqualsDetailed(Version v1, Version v2) {
		// symmetry (and equality)
		assertTrue(v1.equalsDetailed(v2));
		assertTrue(v2.equalsDetailed(v1));

		// reflexivity
		assertTrue(v1.equalsDetailed(v1));
		assertTrue(v2.equalsDetailed(v2));

		// consistency
		assertTrue(v1.equalsDetailed(v2));
		assertTrue(v2.equalsDetailed(v1));

		assertFalse(v1.isGreaterThan(v1, true));
		assertFalse(v2.isGreaterThan(v2, true));
		assertFalse(v1.isGreaterThan(v2, true));
		assertFalse(v2.isGreaterThan(v1, true));

		assertFalse(v1.isLessThan(v1, true));
		assertFalse(v2.isLessThan(v2, true));
		assertFalse(v1.isLessThan(v2, true));
		assertFalse(v2.isLessThan(v1, true));

		assertTrue(v1.isGreaterThanOrEqualTo(v1, true));
		assertTrue(v2.isGreaterThanOrEqualTo(v2, true));
		assertTrue(v1.isGreaterThanOrEqualTo(v2, true));
		assertTrue(v2.isGreaterThanOrEqualTo(v1, true));

		assertTrue(v1.isLessThanOrEqualTo(v1, true));
		assertTrue(v2.isLessThanOrEqualTo(v2, true));
		assertTrue(v1.isLessThanOrEqualTo(v2, true));
		assertTrue(v2.isLessThanOrEqualTo(v1, true));
	}

	private static boolean versionEqualsExact(Version v1, Version v2, boolean caseSensitive) {
		return
			// symmetry (and equality)
			v1.equalsExcact(v2, caseSensitive) &&
			v2.equalsExcact(v1, caseSensitive) &&

			// reflexivity
			v1.equalsExcact(v1, caseSensitive) &&
			v2.equalsExcact(v2, caseSensitive) &&

			// consistency
			v1.equalsExcact(v2, caseSensitive) &&
			v2.equalsExcact(v1, caseSensitive) &&

			(
				!caseSensitive ||
				!v1.isGreaterThan(v1, true) &&
				!v2.isGreaterThan(v2, true) &&
				!v1.isGreaterThan(v2, true) &&
				!v2.isGreaterThan(v1, true) &&

				!v1.isLessThan(v1, true) &&
				!v2.isLessThan(v2, true) &&
				!v1.isLessThan(v2, true) &&
				!v2.isLessThan(v1, true) &&

				v1.isGreaterThanOrEqualTo(v1, true) &&
				v2.isGreaterThanOrEqualTo(v2, true) &&
				v1.isGreaterThanOrEqualTo(v2, true) &&
				v2.isGreaterThanOrEqualTo(v1, true) &&

				v1.isLessThanOrEqualTo(v1, true) &&
				v2.isLessThanOrEqualTo(v2, true) &&
				v1.isLessThanOrEqualTo(v2, true) &&
				v2.isLessThanOrEqualTo(v1, true)
			);
	}

	private static void assertVersionIsGreaterThan(Version v1, Version v2) {
		assertTrue(v1.isGreaterThan(v2));
		assertFalse(v2.isGreaterThan(v1));

		assertTrue(v2.isLessThan(v1));
		assertFalse(v1.isLessThan(v2));

		assertTrue(v1.isGreaterThanOrEqualTo(v2));
		assertFalse(v2.isGreaterThanOrEqualTo(v1));

		assertTrue(v2.isLessThanOrEqualTo(v1));
		assertFalse(v1.isLessThanOrEqualTo(v2));

		assertFalse(v1.equals(v2));
		assertFalse(v2.equals(v1));

		assertThat(v1.hashCode()).isNotEqualTo(v2.hashCode());
		assertThat(v2.hashCode()).isNotEqualTo(v1.hashCode());
	}

	private static void assertVersionIsGreaterThanSuffix(Version v1, Version v2) {
		assertTrue(v1.isGreaterThan(v2, true));
		assertFalse(v2.isGreaterThan(v1, true));

		assertTrue(v2.isLessThan(v1, true));
		assertFalse(v1.isLessThan(v2, true));

		assertTrue(v1.isGreaterThanOrEqualTo(v2, true));
		assertFalse(v2.isGreaterThanOrEqualTo(v1, true));

		assertTrue(v2.isLessThanOrEqualTo(v1, true));
		assertFalse(v1.isLessThanOrEqualTo(v2, true));

		assertFalse(v1.equalsDetailed(v2));
		assertFalse(v2.equalsDetailed(v1));
	}

	private static void assertVersionToStringEquals(Version v, String s) {
		assertThat(v.toString()).isEqualTo(s);
	}

	@Test
	public void testTransitivity() {
		Version v1 = version("1.1.1");
		Version v2 = version("2.2.2");
		Version v3 = version("3.3.3");
		Version v4 = version(4, 5, 6, 7);
		Version v5 = version(5, 6, 7);
		Version v6 = version(6, 7);
		Version v7 = version(7);

		assertVersionIsGreaterThan(v2, v1);
		assertVersionIsGreaterThan(v3, v2);
		assertVersionIsGreaterThan(v3, v1);
		assertVersionIsGreaterThan(v4, v1);
		assertVersionIsGreaterThan(v4, v2);
		assertVersionIsGreaterThan(v4, v3);
		assertVersionIsGreaterThan(v5, v1);
		assertVersionIsGreaterThan(v5, v2);
		assertVersionIsGreaterThan(v5, v3);
		assertVersionIsGreaterThan(v5, v4);
		assertVersionIsGreaterThan(v6, v1);
		assertVersionIsGreaterThan(v6, v2);
		assertVersionIsGreaterThan(v6, v3);
		assertVersionIsGreaterThan(v6, v4);
		assertVersionIsGreaterThan(v6, v5);
		assertVersionIsGreaterThan(v7, v1);
		assertVersionIsGreaterThan(v7, v2);
		assertVersionIsGreaterThan(v7, v3);
		assertVersionIsGreaterThan(v7, v4);
		assertVersionIsGreaterThan(v7, v5);
		assertVersionIsGreaterThan(v7, v6);

		Version v8 = version("1.1.1-beta");
		Version v9 = version("2.2.2-snapshot");
		Version v10 = version("3.3.3_b34");
		Version v11 = version("3.3.3_b34", true);

		assertVersionIsGreaterThanSuffix(v2, v1);
		assertVersionIsGreaterThanSuffix(v3, v2);
		assertVersionIsGreaterThanSuffix(v3, v1);
		assertVersionIsGreaterThanSuffix(v5, v1);
		assertVersionIsGreaterThanSuffix(v8, v1);
		assertVersionIsGreaterThanSuffix(v6, v8);
		assertVersionIsGreaterThanSuffix(v7, v11);
		assertVersionIsGreaterThanSuffix(v9, v1);
		assertVersionIsGreaterThanSuffix(v10, v1);
		assertVersionIsGreaterThanSuffix(v11, v1);
		assertVersionIsGreaterThanSuffix(v2, v8);
		assertVersionIsGreaterThanSuffix(v9, v2);
		assertVersionIsGreaterThanSuffix(v10, v2);
		assertVersionIsGreaterThanSuffix(v11, v2);
		assertVersionIsGreaterThanSuffix(v3, v8);
		assertVersionIsGreaterThanSuffix(v3, v9);
		assertVersionIsGreaterThanSuffix(v10, v3);
		assertVersionIsGreaterThanSuffix(v11, v3);
		assertVersionIsGreaterThanSuffix(v11, v10);
		assertVersionIsGreaterThanSuffix(v11, v9);
		assertVersionIsGreaterThanSuffix(v11, v8);
		assertVersionIsGreaterThanSuffix(v10, v9);
		assertVersionIsGreaterThanSuffix(v10, v8);
		assertVersionIsGreaterThanSuffix(v9, v8);

		Version va = version("2.2.2");
		Version vb = version("2.2.2");
		Version vc = version(2, 2, 2);

		assertVersionEquals(vb, vc);
		assertVersionEquals(va, vb);
		assertVersionEquals(va, vc);
	}

	@Test
	public void testToString() {
		assertVersionToStringEquals(version(""), "");
		assertVersionToStringEquals(version("foo"), "foo");
	}

	@Test
	public void testEquals() {
		assertVersionEquals(version(""), version(""));
		assertVersionEquals(version("0"), version("0"));
		assertVersionEquals(version("0"), version("00"));
		assertVersionEquals(version("0"), version("00.00"));
		assertVersionEquals(version("0"), version("0.0.0"));
		assertVersionEquals(version("0"), version("00.00.00"));
		assertVersionEquals(version("0"), version("0.0.0.0"));
		assertVersionEquals(version("0"), version("00.00.00.00"));
		assertVersionEquals(version("0"), version(0, 0, 0, 0));
		assertVersionEquals(version("0"), version(0, 0, 0));
		assertVersionEquals(version("0"), version(0, 0));
		assertVersionEquals(version("0"), version(0));

		assertVersionEquals(version("0foo"), version("0"));
		assertVersionEquals(version("0foo"), version("00"));
		assertVersionEquals(version("0foo"), version("0.0"));
		assertVersionEquals(version("0foo"), version("00.00"));
		assertVersionEquals(version("0foo"), version("0.0.0"));
		assertVersionEquals(version("0foo"), version("00.00.00"));
		assertVersionEquals(version("0foo"), version("0.0.0.0"));
		assertVersionEquals(version("0.foo"), version("00.00.00.00"));
		assertVersionEquals(version("0foo"), version(0, 0, 0, 0));
		assertVersionEquals(version("0.foo"), version(0, 0, 0));
		assertVersionEquals(version("0foo"), version(0, 0));
		assertVersionEquals(version("0.foo"), version(0));

		assertVersionEquals(version("1foo2"), version("1"));
		assertVersionEquals(version("1foo2"), version("01"));
		assertVersionEquals(version("1foo2"), version("1.0"));
		assertVersionEquals(version("1foo2"), version("01.00"));
		assertVersionEquals(version("1foo2"), version("1.0.0"));
		assertVersionEquals(version("1foo2"), version("01.00.00"));
		assertVersionEquals(version("1foo2"), version("1.0.0.0"));
		assertVersionEquals(version("1foo2"), version("01.00.00.00"));
		assertVersionEquals(version("1foo"), version(1, 0, 0, 0));
		assertVersionEquals(version("1.foo3"), version(1, 0, 0));
		assertVersionEquals(version("1foo2"), version(1, 0));
		assertVersionEquals(version("1.foo"), version(1));

		assertVersionEquals(version("2"), version("2"));
		assertVersionEquals(version("2"), version("02"));
		assertVersionEquals(version("2"), version("2.0"));
		assertVersionEquals(version("2"), version("02.00"));
		assertVersionEquals(version("2"), version("2.0.0"));
		assertVersionEquals(version("2"), version("02.00.00"));
		assertVersionEquals(version("2"), version("2.0.0.0"));
		assertVersionEquals(version("2"), version("02.00.00.00"));
		assertVersionEquals(version("2"), version(2, 0, 0, 0));
		assertVersionEquals(version("2"), version(2, 0, 0));
		assertVersionEquals(version("2"), version(2, 0));
		assertVersionEquals(version("2"), version(2));

		assertVersionEquals(version("2.2"), version("2.2"));
		assertVersionEquals(version("2.2"), version("02.02"));
		assertVersionEquals(version("2.2"), version("2.2.0"));
		assertVersionEquals(version("2.2"), version("02.02.00"));
		assertVersionEquals(version("2.2"), version("2.2.0.0"));
		assertVersionEquals(version("2.2"), version("02.02.00.00"));
		assertVersionEquals(version("2.2"), version(2, 2, 0, 0));
		assertVersionEquals(version("2.2"), version(2, 2, 0));
		assertVersionEquals(version("2.2"), version(2, 2));

		assertVersionEquals(version("2.2.2"), version("2.2.2"));
		assertVersionEquals(version("2.2.2"), version("02.02.02"));
		assertVersionEquals(version("2.2.2"), version("2.2.2.0"));
		assertVersionEquals(version("2.2.2"), version("02.02.02.00"));
		assertVersionEquals(version("2.2.2"), version(2, 2, 2, 0));
		assertVersionEquals(version("2.2.2"), version(2, 2, 2));

		assertVersionEquals(version("2.2.2.2"), version("2.2.2.2"));
		assertVersionEquals(version("2.2.2.2"), version("02.02.02.02"));
		assertVersionEquals(version("2.2.2.2"), version("02,02,02,02"));
		assertVersionEquals(version("2.2.2.2"), version(2, 2, 2, 2));
	}

	@Test
	public void testEqualsDetailed() {
		assertVersionEqualsDetailed(version(""), version(""));
		assertVersionEqualsDetailed(version("0"), version("0"));
		assertVersionEqualsDetailed(version("0"), version("00"));
		assertVersionEqualsDetailed(version("0"), version("00.00"));
		assertVersionEqualsDetailed(version("0"), version("0.0.0"));
		assertVersionEqualsDetailed(version("0"), version("00.00.00"));
		assertVersionEqualsDetailed(version("0"), version("0.0.0.0"));
		assertVersionEqualsDetailed(version("0"), version("00.00.00.00"));
		assertVersionEqualsDetailed(version("0"), version(0, 0, 0, 0));
		assertVersionEqualsDetailed(version("0"), version(0, 0, 0));
		assertVersionEqualsDetailed(version("0"), version(0, 0));
		assertVersionEqualsDetailed(version("0"), version(0));

		assertVersionEqualsDetailed(version("foo"), version("foo"));
		assertVersionEqualsDetailed(version("0foo"), version("0foo"));
		assertVersionEqualsDetailed(version("0foo"), version("00foo"));
		assertVersionEqualsDetailed(version("0foo"), version("0.0foo"));
		assertVersionEqualsDetailed(version("0foo"), version("00.00foo"));
		assertVersionEqualsDetailed(version("0foo"), version("0.0.0foo"));
		assertVersionEqualsDetailed(version("0foo"), version("00.00.00foo"));
		assertVersionEqualsDetailed(version("0foo"), version("0.0.0.0foo"));
		assertVersionEqualsDetailed(version("0foo"), version("00.00.00.00foo"));
		assertVersionEqualsDetailed(version("0"), version(0, 0, 0, 0));
		assertVersionEqualsDetailed(version("0"), version(0, 0, 0));
		assertVersionEqualsDetailed(version("0"), version(0, 0));
		assertVersionEqualsDetailed(version("0"), version(0));

		assertVersionEqualsDetailed(version("1foo2"), version("1foo2"));
		assertVersionEqualsDetailed(version("1foo2"), version("01foo2"));
		assertVersionEqualsDetailed(version("1foo2"), version("1.0foo2"));
		assertVersionEqualsDetailed(version("1foo2"), version("01.00foo2"));
		assertVersionEqualsDetailed(version("1foo2"), version("1.0.0foo2"));
		assertVersionEqualsDetailed(version("1foo2"), version("01.00.00foo2"));
		assertVersionEqualsDetailed(version("1foo2"), version("1.0.0.0foo2"));
		assertVersionEqualsDetailed(version("1foo2"), version("01.00.00.00foo2"));
		assertVersionEqualsDetailed(version("1"), version(1, 0, 0, 0));
		assertVersionEqualsDetailed(version("1"), version(1, 0, 0));
		assertVersionEqualsDetailed(version("1"), version(1, 0));
		assertVersionEqualsDetailed(version("1"), version(1));

		assertVersionEqualsDetailed(version("2"), version("2"));
		assertVersionEqualsDetailed(version("2"), version("02"));
		assertVersionEqualsDetailed(version("2"), version("2.0"));
		assertVersionEqualsDetailed(version("2"), version("02.00"));
		assertVersionEqualsDetailed(version("2"), version("2.0.0"));
		assertVersionEqualsDetailed(version("2"), version("02.00.00"));
		assertVersionEqualsDetailed(version("2"), version("2.0.0.0"));
		assertVersionEqualsDetailed(version("2"), version("02.00.00.00"));
		assertVersionEqualsDetailed(version("2"), version(2, 0, 0, 0));
		assertVersionEqualsDetailed(version("2"), version(2, 0, 0));
		assertVersionEqualsDetailed(version("2"), version(2, 0));
		assertVersionEqualsDetailed(version("2"), version(2));

		assertVersionEqualsDetailed(version("2.2"), version("2.2"));
		assertVersionEqualsDetailed(version("2.2"), version("02.02"));
		assertVersionEqualsDetailed(version("2.2"), version("2.2.0"));
		assertVersionEqualsDetailed(version("2.2"), version("02.02.00"));
		assertVersionEqualsDetailed(version("2.2"), version("2.2.0.0"));
		assertVersionEqualsDetailed(version("2.2"), version("02.02.00.00"));
		assertVersionEqualsDetailed(version("2.2"), version(2, 2, 0, 0));
		assertVersionEqualsDetailed(version("2.2"), version(2, 2, 0));
		assertVersionEqualsDetailed(version("2.2"), version(2, 2));

		assertVersionEqualsDetailed(version("2.2.2"), version("2.2.2"));
		assertVersionEqualsDetailed(version("2.2.2"), version("02.02.02"));
		assertVersionEqualsDetailed(version("2.2.2"), version("2.2.2.0"));
		assertVersionEqualsDetailed(version("2.2.2"), version("02.02.02.00"));
		assertVersionEqualsDetailed(version("2.2.2"), version(2, 2, 2, 0));
		assertVersionEqualsDetailed(version("2.2.2"), version(2, 2, 2));

		assertVersionEqualsDetailed(version("2.2.2.2"), version("2.2.2.2"));
		assertVersionEqualsDetailed(version("2.2.2.2"), version("02.02.02.02"));
		assertVersionEqualsDetailed(version("2.2.2.2"), version(2, 2, 2, 2));

		assertVersionEqualsDetailed(version("-b34x"), version("-b34x"));
		assertVersionEqualsDetailed(version("0-b34x"), version("0-b34x"));
		assertVersionEqualsDetailed(version("0-b34x"), version("00-b34x"));
		assertVersionEqualsDetailed(version("0-b34x"), version("00.00-b34x"));
		assertVersionEqualsDetailed(version("0-b34x"), version("0.0.0-b34x"));
		assertVersionEqualsDetailed(version("0-b34x"), version("00.00.00-b34x"));
		assertVersionEqualsDetailed(version("0-b34x"), version("0.0.0.0-b34x"));
		assertVersionEqualsDetailed(version("0-b34x"), version("00.00.00.00-b34x"));
	}

	@Test
	public void testEqualsExact() {
		assertTrue(versionEqualsExact(version(""), version(""), true));
		assertFalse(versionEqualsExact(version(""), version("0"), false));
		assertFalse(versionEqualsExact(version(""), version("00"), false));
		assertFalse(versionEqualsExact(version(""), version("00.00"), false));
		assertFalse(versionEqualsExact(version(""), version("0.0.0"), false));
		assertFalse(versionEqualsExact(version(""), version("00.00.00"), false));
		assertFalse(versionEqualsExact(version(""), version("0.0.0.0"), false));
		assertFalse(versionEqualsExact(version(""), version("00.00.00.00"), false));
		assertFalse(versionEqualsExact(version(""), version(0, 0, 0, 0), false));
		assertFalse(versionEqualsExact(version(""), version(0, 0, 0), false));
		assertFalse(versionEqualsExact(version(""), version(0, 0), false));
		assertFalse(versionEqualsExact(version(""), version(0), false));

		assertFalse(versionEqualsExact(version("foo"), version("0foo"), false));
		assertFalse(versionEqualsExact(version("foo"), version("00foo"), false));
		assertFalse(versionEqualsExact(version("foo"), version("0.0foo"), false));
		assertFalse(versionEqualsExact(version("foo"), version("00.00foo"), false));
		assertFalse(versionEqualsExact(version("foo"), version("0.0.0foo"), false));
		assertFalse(versionEqualsExact(version("foo"), version("00.00.00foo"), false));
		assertFalse(versionEqualsExact(version("foo"), version("0.0.0.0foo"), false));
		assertFalse(versionEqualsExact(version("foo"), version("00.00.00.00foo"), false));
		assertFalse(versionEqualsExact(version("foo"), version(0, 0, 0, 0), false));
		assertFalse(versionEqualsExact(version("foo"), version(0, 0, 0), false));
		assertFalse(versionEqualsExact(version("foo"), version(0, 0), false));
		assertFalse(versionEqualsExact(version("foo"), version(0), false));

		assertTrue(versionEqualsExact(version("1foo2"), version("1foo2"), true));
		assertTrue(versionEqualsExact(version("1Foo2"), version("1foo2"), false));
		assertFalse(versionEqualsExact(version("1Foo2"), version("1foo2"), true));
		assertFalse(versionEqualsExact(version("1foo2"), version("01foo2"), false));
		assertFalse(versionEqualsExact(version("1foo2"), version("1.0foo2"), false));
		assertFalse(versionEqualsExact(version("1foo2"), version("01.00foo2"), false));
		assertFalse(versionEqualsExact(version("1foo2"), version("1.0.0foo2"), false));
		assertFalse(versionEqualsExact(version("1foo2"), version("01.00.00foo2"), false));
		assertFalse(versionEqualsExact(version("1foo2"), version("1.0.0.0foo2"), false));
		assertFalse(versionEqualsExact(version("1foo2"), version("01.00.00.00foo2"), false));

		assertTrue(versionEqualsExact(version("2"), version("2"), false));
		assertTrue(versionEqualsExact(version("2"), version("2"), true));
		assertFalse(versionEqualsExact(version("2"), version("02"), false));
		assertFalse(versionEqualsExact(version("2"), version("2.0"), false));
		assertFalse(versionEqualsExact(version("2"), version("02.00"), false));
		assertFalse(versionEqualsExact(version("2"), version("2.0.0"), false));
		assertFalse(versionEqualsExact(version("2"), version("02.00.00"), false));
		assertFalse(versionEqualsExact(version("2"), version("2.0.0.0"), false));
		assertFalse(versionEqualsExact(version("2"), version("02.00.00.00"), false));
		assertFalse(versionEqualsExact(version("2"), version(2, 0, 0, 0), false));
		assertFalse(versionEqualsExact(version("2"), version(2, 0, 0), false));
		assertFalse(versionEqualsExact(version("2"), version(2, 0), false));
		assertTrue(versionEqualsExact(version("2"), version(2), false));

		assertTrue(versionEqualsExact(version("2.2"), version("2.2"), false));
		assertTrue(versionEqualsExact(version("2.2"), version("2.2"), true));
		assertFalse(versionEqualsExact(version("2.2"), version("02.02"), false));
		assertFalse(versionEqualsExact(version("2.2"), version("2.2.0"), false));
		assertFalse(versionEqualsExact(version("2.2"), version("02.02.00"), false));
		assertFalse(versionEqualsExact(version("2.2"), version("2.2.0.0"), false));
		assertFalse(versionEqualsExact(version("2.2"), version("02.02.00.00"), false));
		assertFalse(versionEqualsExact(version("2.2"), version(2, 2, 0, 0), false));
		assertFalse(versionEqualsExact(version("2.2"), version(2, 2, 0), false));
		assertTrue(versionEqualsExact(version("2.2"), version(2, 2), false));

		assertTrue(versionEqualsExact(version("2.2.2"), version("2.2.2"), false));
		assertTrue(versionEqualsExact(version("2.2.2"), version("2.2.2"), true));
		assertFalse(versionEqualsExact(version("2.2.2"), version("02.02.02"), false));
		assertFalse(versionEqualsExact(version("2.2.2"), version("2.2.2.0"), false));
		assertFalse(versionEqualsExact(version("2.2.2"), version("02.02.02.00"), false));
		assertFalse(versionEqualsExact(version("2.2.2"), version(2, 2, 2, 0), false));
		assertTrue(versionEqualsExact(version("2.2.2"), version(2, 2, 2), false));

		assertTrue(versionEqualsExact(version("2.2.2.2"), version("2.2.2.2"), false));
		assertTrue(versionEqualsExact(version("2.2.2.2"), version("2.2.2.2"), true));
		assertFalse(versionEqualsExact(version("2.2.2.2"), version("02.02.02.02"), false));

		assertTrue(versionEqualsExact(version("-b34x"), version("-b34x"), false));
		assertTrue(versionEqualsExact(version("-b34x"), version("-b34x"), true));
		assertTrue(versionEqualsExact(version("-B34x"), version("-b34x"), false));
		assertTrue(versionEqualsExact(version("-B34X"), version("-b34x"), false));
		assertTrue(versionEqualsExact(version("-b34X"), version("-b34x"), false));
		assertFalse(versionEqualsExact(version("-B34x"), version("-b34x"), true));
		assertFalse(versionEqualsExact(version("-B34X"), version("-b34x"), true));
		assertFalse(versionEqualsExact(version("-b34X"), version("-b34x"), true));
		assertTrue(versionEqualsExact(version("0-b34x"), version("0-b34x"), false));
		assertTrue(versionEqualsExact(version("0-b34x"), version("0-b34x"), true));
		assertFalse(versionEqualsExact(version("0-b34x"), version("00-b34x"), false));
		assertFalse(versionEqualsExact(version("0-b34x"), version("00.00-b34x"), false));
		assertFalse(versionEqualsExact(version("0-b34x"), version("0.0.0-b34x"), false));
		assertFalse(versionEqualsExact(version("0-b34x"), version("00.00.00-b34x"), false));
		assertFalse(versionEqualsExact(version("0-b34x"), version("0.0.0.0-b34x"), false));
		assertFalse(versionEqualsExact(version("0-b34x"), version("00.00.00.00-b34x"), false));
	}

	@Test
	public void testIsGreaterThan() {
		assertVersionIsGreaterThan(version("2"), version("1"));
		assertVersionIsGreaterThan(version("2"), version("01"));
		assertVersionIsGreaterThan(version("2"), version("1.0"));
		assertVersionIsGreaterThan(version("2"), version("01.00"));
		assertVersionIsGreaterThan(version("2"), version("1.0.0"));
		assertVersionIsGreaterThan(version("2"), version("01.00.00"));
		assertVersionIsGreaterThan(version("2"), version("1.0.0.0"));
		assertVersionIsGreaterThan(version("2"), version("01.00.00.00"));
		assertVersionIsGreaterThan(version("2"), version("01.00.00.00"));
		assertTrue(version("2").isGreaterThanOrEqualTo(1, 0, 0, 0));
		assertTrue(version("2").isGreaterThanOrEqualTo(1, 0, 0));
		assertTrue(version("2").isGreaterThanOrEqualTo(1, 0));
		assertTrue(version("2").isGreaterThanOrEqualTo(2));
		assertTrue(version("2").isGreaterThan(1, 0, 0, 0));
		assertTrue(version("2").isGreaterThan(1, 0, 0));
		assertTrue(version("2").isGreaterThan(1, 0));
		assertTrue(version("2").isGreaterThan(1));
		assertFalse(version("2").isLessThanOrEqualTo(1, 0, 0, 0));
		assertFalse(version("2").isLessThanOrEqualTo(1, 0, 0));
		assertFalse(version("2").isLessThanOrEqualTo(1, 0));
		assertTrue(version("2").isLessThanOrEqualTo(2));
		assertFalse(version("2").isLessThan(1, 0, 0, 0));
		assertFalse(version("2").isLessThan(1, 0, 0));
		assertFalse(version("2").isLessThan(1, 0));
		assertFalse(version("2").isLessThan(1));

		assertVersionIsGreaterThan(version("2.2"), version("2"));
		assertVersionIsGreaterThan(version("2.2"), version("02"));
		assertVersionIsGreaterThan(version("2.2"), version("2.0"));
		assertVersionIsGreaterThan(version("2.2"), version("02.00"));
		assertVersionIsGreaterThan(version("2.2"), version("2.0.0"));
		assertVersionIsGreaterThan(version("2.2"), version("02.00.00"));
		assertVersionIsGreaterThan(version("2.2"), version("2.0.0.0"));
		assertVersionIsGreaterThan(version("2.2"), version("02.00.00.00"));
		assertTrue(version("2.2").isGreaterThanOrEqualTo(2, 0, 0, 0));
		assertTrue(version("2.2").isGreaterThanOrEqualTo(2, 0, 0));
		assertTrue(version("2.2").isGreaterThanOrEqualTo(2, 2));
		assertTrue(version("2.2").isGreaterThanOrEqualTo(2));
		assertTrue(version("2.2").isGreaterThan(2, 0, 0, 0));
		assertTrue(version("2.2").isGreaterThan(2, 0, 0));
		assertTrue(version("2.2").isGreaterThan(2, 0));
		assertTrue(version("2.2").isGreaterThan(2));
		assertFalse(version("2.2").isLessThanOrEqualTo(2, 0, 0, 0));
		assertFalse(version("2.2").isLessThanOrEqualTo(2, 0, 0));
		assertTrue(version("2.2").isLessThanOrEqualTo(2, 2));
		assertFalse(version("2.2").isLessThanOrEqualTo(2));
		assertFalse(version("2.2").isLessThan(2, 0, 0, 0));
		assertFalse(version("2.2").isLessThan(2, 0, 0));
		assertFalse(version("2.2").isLessThan(2, 0));
		assertFalse(version("2.2").isLessThan(2));

		assertVersionIsGreaterThan(version("2.2.2"), version("2"));
		assertVersionIsGreaterThan(version("2.2.2"), version("02"));
		assertVersionIsGreaterThan(version("2.2.2"), version("2.0"));
		assertVersionIsGreaterThan(version("2.2.2"), version("02.00"));
		assertVersionIsGreaterThan(version("2.2.2"), version("2.0.0"));
		assertVersionIsGreaterThan(version("2.2.2"), version("02.00.00"));
		assertVersionIsGreaterThan(version("2.2.2"), version("2.0.0.0"));
		assertVersionIsGreaterThan(version("2.2.2"), version("02.00.00.00"));
		assertTrue(version("2.2.2").isGreaterThanOrEqualTo(2, 2, 2, 0));
		assertTrue(version("2.2.2").isGreaterThanOrEqualTo(2, 2, 2));
		assertTrue(version("2.2.2").isGreaterThanOrEqualTo(2, 2));
		assertTrue(version("2.2.2").isGreaterThanOrEqualTo(2));
		assertTrue(version("2.2.2").isGreaterThan(2, 2, 0, 0));
		assertTrue(version("2.2.2").isGreaterThan(2, 2, 0));
		assertTrue(version("2.2.2").isGreaterThan(2, 2));
		assertTrue(version("2.2.2").isGreaterThan(2));
		assertTrue(version("2.2.2").isLessThanOrEqualTo(2, 2, 2, 0));
		assertTrue(version("2.2.2").isLessThanOrEqualTo(2, 2, 2));
		assertFalse(version("2.2.2").isLessThanOrEqualTo(2, 2));
		assertFalse(version("2.2.2").isLessThanOrEqualTo(2));
		assertFalse(version("2.2.2").isLessThan(2, 2, 0, 0));
		assertFalse(version("2.2.2").isLessThan(2, 2, 0));
		assertFalse(version("2.2.2").isLessThan(2, 2));
		assertFalse(version("2.2.2").isLessThan(2));

		assertVersionIsGreaterThan(version("2.2.2.2"), version("2"));
		assertVersionIsGreaterThan(version("2.2.2.2"), version("02"));
		assertVersionIsGreaterThan(version("2.2.2.2"), version("2.0"));
		assertVersionIsGreaterThan(version("2.2.2.2"), version("02.00"));
		assertVersionIsGreaterThan(version("2.2.2.2"), version("2.0.0"));
		assertVersionIsGreaterThan(version("2.2.2.2"), version("02.00.00"));
		assertVersionIsGreaterThan(version("2.2.2.2"), version("2.0.0.0"));
		assertVersionIsGreaterThan(version("2.2.2.2"), version("02.00.00.00"));
		assertTrue(version("2.2.2.2").isGreaterThanOrEqualTo(2, 2, 2, 2));
		assertTrue(version("2.2.2.2").isGreaterThanOrEqualTo(2, 2, 2));
		assertTrue(version("2.2.2.2").isGreaterThanOrEqualTo(2, 2));
		assertTrue(version("2.2.2.2").isGreaterThanOrEqualTo(2));
		assertTrue(version("2.2.2.2").isGreaterThan(2, 2, 2, 0));
		assertTrue(version("2.2.2.2").isGreaterThan(2, 2, 2));
		assertTrue(version("2.2.2.2").isGreaterThan(2, 2));
		assertTrue(version("2.2.2.2").isGreaterThan(2));
		assertTrue(version("2.2.2.2").isLessThanOrEqualTo(2, 2, 2, 2));
		assertFalse(version("2.2.2.2").isLessThanOrEqualTo(2, 2, 2));
		assertFalse(version("2.2.2.2").isLessThanOrEqualTo(2, 2));
		assertFalse(version("2.2.2.2").isLessThanOrEqualTo(2));
		assertFalse(version("2.2.2.2").isLessThan(2, 2, 2, 0));
		assertFalse(version("2.2.2.2").isLessThan(2, 2, 2));
		assertFalse(version("2.2.2.2").isLessThan(2, 2));
		assertFalse(version("2.2.2.2").isLessThan(2));
	}

	@Test
	public void testMajor() {
		assertThat(version("0").getMajor()).isEqualTo(0);
		assertThat(version("0.1").getMajor()).isEqualTo(0);
		assertThat(version("0.1.2").getMajor()).isEqualTo(0);
		assertThat(version("0.1.2.3").getMajor()).isEqualTo(0);

		assertThat(version("1").getMajor()).isEqualTo(1);
		assertThat(version("1.2").getMajor()).isEqualTo(1);
		assertThat(version("1.2.3").getMajor()).isEqualTo(1);
		assertThat(version("1.2.3.4").getMajor()).isEqualTo(1);

		assertEquals("v18", version("v18.3.a3").getMajorString());
		assertEquals("v18", version("v18.3.a3", true).getMajorString());
		assertEquals("b3", version("b3.3.9").getMajorString());
		assertEquals("b3", version("b3.3.9", true).getMajorString());
	}

	@Test
	public void testMinor() {
		assertThat(version("0").getMinor()).isEqualTo(0);
		assertThat(version("0.1").getMinor()).isEqualTo(1);
		assertThat(version("0.1.2").getMinor()).isEqualTo(1);
		assertThat(version("0.1.2.3").getMinor()).isEqualTo(1);

		assertThat(version("1").getMinor()).isEqualTo(0);
		assertThat(version("1.2").getMinor()).isEqualTo(2);
		assertThat(version("1.2.3").getMinor()).isEqualTo(2);
		assertThat(version("1.2.3.4").getMinor()).isEqualTo(2);

		assertNull(version("v18.b3.a3").getMinorString());
		assertEquals("b3", version("v18.b3.a3", true).getMinorString());
	}

	@Test
	public void testRevision() {
		assertThat(version("0").getRevision()).isEqualTo(0);
		assertThat(version("0.1").getRevision()).isEqualTo(0);
		assertThat(version("0.1.2").getRevision()).isEqualTo(2);
		assertThat(version("0.1.2.3").getRevision()).isEqualTo(2);

		assertThat(version("1").getRevision()).isEqualTo(0);
		assertThat(version("1.2").getRevision()).isEqualTo(0);
		assertThat(version("1.2.3").getRevision()).isEqualTo(3);
		assertThat(version("1.2.3.4").getRevision()).isEqualTo(3);

		assertNull(version("v18.3.a3").getRevisionString());
		assertEquals("a3", version("v18.3.a3", true).getRevisionString());
	}

	@Test
	public void testBuild() {
		assertThat(version("0").getBuild()).isEqualTo(0);
		assertThat(version("0.1").getBuild()).isEqualTo(0);
		assertThat(version("0.1.2").getBuild()).isEqualTo(0);
		assertThat(version("0.1.2.3").getBuild()).isEqualTo(3);

		assertThat(version("1").getBuild()).isEqualTo(0);
		assertThat(version("1.2").getBuild()).isEqualTo(0);
		assertThat(version("1.2.3").getBuild()).isEqualTo(0);
		assertThat(version("1.2.3.4").getBuild()).isEqualTo(4);

		assertNull(version("v18.3.a3").getBuildString());
	}

	@Test
	public void testParsing() {
		// Real version numbers found in Windows registry of miscellaneous formats
		assertEquals(version(2, 6, 0, 0), version("2.6.0 MT"));
		assertEquals(version(2204, 0, 0, 0), version("2204"));
		assertEquals(version(3, 0, 0, 0), version("3.0"));
		assertEquals(version(17, 9, 20044, 0), version("17.009.20044.0"));
		assertEquals(version(9, 0, 0, 2), version("9.0.0.2", true));
		assertEquals(version(9, 64, 108, 3899), version("9.40.6C.F3b", true));
		assertNotEquals(version(0, 0, 0, 0), version("DC"));
		assertEquals(version(3, 0, 5, 0), version("3.0.5"));
		assertEquals(version(2, 6, 3, 8518), version("2.6.3.8518"));
		assertEquals(version(2, 6, 3, 34072), version("2.6.3.8518", true));
		assertEquals(version(1, 0, 3705, 0), version("v1.0.3705"));
		assertEquals(1, version("v1.0.3705").getMajor());
		assertEquals("v1", version("v1.0.3705").getMajorString());
		assertEquals(version(1, 0, 14085, 0), version("v1.0.3705", true));
		assertEquals(version(1, 8, 477, 0), version("001.008.00477"));
		assertEquals(version(1, 8, 1143, 0), version("001.008.00477", true));
		assertEquals(version(1, 8, 0, 131), version("1.8.0_131-b11"));
		assertEquals("-b11", version("1.8.0_131-b11").getTextSuffix());
		assertEquals(version(13, 0, 0, 206), version("13,0,0,206"));
		assertEquals(version(2012, 12, 14, 11), version("v2012.12.14.11"));
		assertEquals("v2012", version("v2012.12.14.11").getMajorString());
		assertEquals(version(12, 0, 7601, 23517), version("12,0,7601,23517"));
		assertEquals(version(54, 0, 1, 0), version("54.0.1 (x86 nb-NO)"));
		assertEquals("(x86 nb-NO)", version("54.0.1 (x86 nb-NO)").getTextSuffix());
		assertEquals(version(23, 169, 192756, 0), version("17.0a9.2f0f4.0-SNAPSHOT", true));
		assertEquals("-SNAPSHOT", version("17.0a9.2f0f4.0-SNAPSHOT", true).getTextSuffix());
	}

	@Test
	public void testElements() {
		assertArrayEquals(new int[] {2, 6, 0}, version("2.6.0 MT").getElementValues());
		assertArrayEquals(new int[] {2, 6}, version("2.6.0 MT").getCanonicalElements());
		assertArrayEquals(new String[] {"2", "6", "0"}, version("2.6.0 MT").getElements());
		assertArrayEquals(new String[] {"2", "4", "1"}, version(2, 4, 1).getElements());
		assertArrayEquals(new int[] {17, 9, 20044, 0}, version("17.009.20044.0").getElementValues());
		assertArrayEquals(new int[] {17, 9, 20044}, version("17.009.20044.0").getCanonicalElements());
		assertArrayEquals(new String[] {"17", "009", "20044", "0"}, version("17.009.20044.0").getElements());
		assertArrayEquals(new int[] {17, 9, 20044, 0, 5, 9, 17}, version("v17.009.20044.0.5,9,17").getElementValues());
		assertArrayEquals(new int[] {17, 9, 20044, 0, 5, 9, 17}, version("v17.009.20044.0.5,9,17").getCanonicalElements());
		assertArrayEquals(new int[] {123, 29, 0, 24}, version(123, 29, 0, 24).getCanonicalElements());
		assertArrayEquals(new String[] {"v17", "009", "20044", "0", "5", "9", "17"}, version("v17.009.20044.0.5,9,17").getElements());
		assertArrayEquals(new int[] {9, 64, 108, 3899}, version("9.40.6C.F3b", true).getElementValues());
		assertArrayEquals(new int[] {64, 108}, version(64, 108).getElementValues());
		assertArrayEquals(new int[] {9, 64, 108, 3899}, version("9.40.6C.F3b", true).getCanonicalElements());
		assertArrayEquals(new String[] {"9", "40", "6C", "F3b"}, version("9.40.6C.F3b", true).getElements());
		assertArrayEquals(new int[] {9, 64}, version("9.40.0.0.0", true).getCanonicalElements());
		assertArrayEquals(new int[] {0}, version(0).getCanonicalElements());
	}
}
