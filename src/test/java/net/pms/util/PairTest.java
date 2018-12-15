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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.junit.Test;


public class PairTest {

	@Test
	public void getTest() {
		Pair<String, Double> pair = new Pair<>("Pair", Double.valueOf(99.9));
		Pair<String, Double> pair2 = new Pair<>("Another pair", Double.valueOf(99.9));
		Pair<String, Double> pair3 = new Pair<>("Pair", Double.valueOf(10d));

		assertEquals("Pair", pair.first);
		assertEquals("Pair", pair.getFirst());
		assertEquals("Pair", pair.getKey());
		assertEquals("Pair", pair.getLeft());
		assertEquals(Double.valueOf(99.9), pair.second);
		assertEquals(Double.valueOf(99.9), pair.getSecond());
		assertEquals(Double.valueOf(99.9), pair.getValue());
		assertEquals(Double.valueOf(99.9), pair.getRight());
		assertEquals(Double.valueOf(99.9), pair.getLast());
		assertEquals("Another pair", pair2.first);
		assertEquals("Another pair", pair2.getFirst());
		assertEquals("Another pair", pair2.getKey());
		assertEquals("Another pair", pair2.getLeft());
		assertEquals(Double.valueOf(99.9), pair2.second);
		assertEquals(Double.valueOf(99.9), pair2.getSecond());
		assertEquals(Double.valueOf(99.9), pair2.getValue());
		assertEquals(Double.valueOf(99.9), pair2.getRight());
		assertEquals(Double.valueOf(99.9), pair2.getLast());
		assertEquals("Pair", pair3.first);
		assertEquals("Pair", pair3.getFirst());
		assertEquals("Pair", pair3.getKey());
		assertEquals("Pair", pair3.getLeft());
		assertEquals(Double.valueOf(10.0), pair3.second);
		assertEquals(Double.valueOf(10.0), pair3.getSecond());
		assertEquals(Double.valueOf(10.0), pair3.getValue());
		assertEquals(Double.valueOf(10.0), pair3.getRight());
		assertEquals(Double.valueOf(10.0), pair3.getLast());
	}

	@Test
	public void hashCodeTest() {
		Integer key = Integer.valueOf(3);
		String value = "value";

		assertEquals(key.hashCode(), new Pair<>(key, null).hashCode());
		assertEquals(value.hashCode(), new Pair<>(null, value).hashCode());
		assertEquals(key.hashCode() ^ value.hashCode(), new Pair<>(key, value).hashCode());
	}

	@Test
	public void equalsTest() {
		Pair<String, Double> pair = new Pair<>("Pair", Double.valueOf(99.9));
		Pair<String, Double> pair2 = new Pair<>("Another pair", Double.valueOf(99.9));
		Pair<String, Double> pair3 = new Pair<>("Pair", Double.valueOf(10d));

		assertFalse(pair.equals(null));
		assertFalse(pair.equals("null"));
		assertFalse(pair.equals(pair2));
		assertFalse(pair.equals(pair3));
		assertFalse(pair2.equals(pair));
		assertFalse(pair2.equals(pair3));
		assertFalse(pair3.equals(pair));
		assertFalse(pair3.equals(pair2));
		assertTrue(pair.equals(pair));
		assertTrue(pair2.equals(pair2));
		assertTrue(pair3.equals(pair3));
		assertTrue(pair.equals(new Pair<String, Double>("Pair", Double.valueOf(99.9))));
		assertFalse(pair.equals(new Pair<String, Float>("Pair", Float.valueOf(99.9f))));
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Test
	public void compareToTest() {
		Pair<String, Double> pair = new Pair<>("Pair", Double.valueOf(99.9));
		Pair<String, Double> pair2 = new Pair<>("Another pair", Double.valueOf(99.9));
		Pair<String, Double> pair3 = new Pair<>("Pair", Double.valueOf(10d));

		List<Pair<String, Double>> list = new ArrayList<>();
		list.add(pair);
		list.add(pair2);
		list.add(pair3);
		Collections.sort(list);
		assertEquals(pair2, list.get(0));
		assertEquals(pair3, list.get(1));
		assertEquals(pair, list.get(2));

		assertEquals(0, new Pair<String, Double>(null, null).compareTo(new Pair<String, Double>(null, null)));
		assertEquals(-1, pair.compareTo(new Pair<String, Double>(null, null)));
		assertEquals(1, new Pair<String, Double>(null, null).compareTo(pair));
		assertEquals(-1, pair.compareTo(new Pair<String, Double>("Pair", null)));
		assertEquals(1, new Pair<String, Double>("Pair", null).compareTo(pair));

		try {
			new Pair<Object, Double>(new Object(), null).compareTo(new Pair<Object, Double>(new Object(), null));
			fail("Should have thrown UnsupportedOperationException");
		} catch (UnsupportedOperationException e) {
			assertEquals("K must implement Comparable", e.getMessage());
		}
		try {
			new Pair<Object, Object>(null, new Object()).compareTo(new Pair<Object, Object>(null, new Object()));
			fail("Should have thrown UnsupportedOperationException");
		} catch (UnsupportedOperationException e) {
			assertEquals("V must implement Comparable", e.getMessage());
		}

		try {
			new Pair("", null).compareTo(new Pair<Object, Double>(new Object(), null));
			fail("Should have thrown UnsupportedOperationException");
		} catch (UnsupportedOperationException e) {
			assertEquals("K must implement Comparable", e.getMessage());
		}
		try {
			new Pair(null, "").compareTo(new Pair<Object, Object>(null, new Object()));
			fail("Should have thrown UnsupportedOperationException");
		} catch (UnsupportedOperationException e) {
			assertEquals("V must implement Comparable", e.getMessage());
		}
	}

	@Test
	public void toStringTest() {
		Pair<String, Double> pair = new Pair<>("Pair", Double.valueOf(99.9));
		Pair<String, Double> pair2 = new Pair<>("Another pair", Double.valueOf(99.9));
		Pair<String, Double> pair3 = new Pair<>("Pair", Double.valueOf(10d));

		assertEquals("Pair[\"Pair\", 99.9]", pair.toString());
		assertEquals("Pair[\"Another pair\", 99.9]", pair2.toString());
		assertEquals("Pair[\"Pair\", 10.0]", pair3.toString());
		assertEquals("Pair[null, 10.0]", new Pair<String, Double>(null, Double.valueOf(10d)).toString());
		assertEquals("Pair[\"Pair\", null]", new Pair<String, Double>("Pair", null).toString());
		assertEquals("Pair[null, null]", new Pair<String, Double>(null, null).toString());

		assertEquals("Pair[2147483647, \"MAX_VALUE\"]", new Pair<Integer, CharSequence>(Integer.MAX_VALUE, new StringBuilder("MAX_VALUE")).toString());
	}

	@Test
	public void testMapEntry() {
		Pair<String, Double> pair = new Pair<>("Pair", Double.valueOf(99.9));

		Map<String, Double> map = new HashMap<>();
		map.put("Pair", Double.valueOf(99.9));
		for (Entry<String, Double> entry : map.entrySet()) {
			assertTrue(pair.equals(entry));
			assertTrue(entry.equals(pair));
		}
	}

	@Test
	public void testImmutable() {
		Pair<String, Double> pair = new Pair<>("Pair", Double.valueOf(99.9));
		try {
			pair.setValue(Double.valueOf(20d));
			fail("Should have thrown UnsupportedOperationException");
		} catch (UnsupportedOperationException e) {
			assertEquals("Pair is immutable", e.getMessage());
		}
	}
}
