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

import net.pms.util.ISO639;
import static org.junit.Assert.*;
import org.junit.Test;


public class ISO639Test {

	/**
	 * Test the {@link ISO639} {@code enum} to verify the defined languages.
	 */
	@Test
	public void testCodes() {
		assertNull("No language found for ISO code null", ISO639.getFirstName(null));

		// Reserved keyword DLNAMediaLang.UND should match "Undetermined"
		assertEquals("ISO code \"und\" returns \"Undetermined\"",
				"Undetermined", ISO639.getFirstName("und"));

		assertEquals("ISO code \"en\" returns \"English\"", "English", ISO639.getFirstName("en"));
		assertEquals("ISO code \"eng\" returns \"English\"", "English", ISO639.getFirstName("eng"));
		assertEquals("ISO code \"EnG\" returns \"English\"", "English", ISO639.getFirstName("EnG"));
		assertNull("Language name \"Czech language\" returns null", ISO639.getFirstName("Czech language"));
		assertEquals("Czech", ISO639.getFirstName("Czech language", true));
		assertEquals("French", ISO639.getFirstName("The French don't like other languages", true));

		// Test code lookup
		assertNotNull("ISO code \"en\" is valid", ISO639.getCode("en"));
		assertNotNull("ISO code \"EN\" is valid", ISO639.getCode("EN"));
		assertNotNull("ISO code \"vie\" is valid", ISO639.getCode("vie"));
		assertNotNull("ISO code \"vIe\" is valid", ISO639.getCode("vIe"));
		assertNull("ISO code \"en-uk\" is invalid", ISO639.getCode("en-uk"));
		assertNull("ISO code \"\" is invalid", ISO639.getCode(""));
		assertNull("ISO code null is invalid", ISO639.getCode(null));

		// Test general lookup
		assertNotNull("ISO code \"en\" is valid", ISO639.get("en"));
		assertNotNull("ISO code \"EN\" is valid", ISO639.get("EN"));
		assertNotNull("ISO code \"vie\" is valid", ISO639.get("vie"));
		assertNotNull("ISO code \"vIe\" is valid", ISO639.get("vIe"));
		assertNull("ISO code \"en-uk\" is invalid", ISO639.get("en-uk"));
		assertNull("ISO code \"\" is invalid", ISO639.get(""));
		assertNull("ISO code null is invalid", ISO639.get(null));
		assertNotNull(ISO639.get("ENGLISH"));
		assertNotNull(ISO639.get("Burmese"));
		assertNotNull(ISO639.get("telugu"));

		// Test getISO639Part2Code()
		assertEquals("ISO code \"en\" returns \"eng\"", "eng", ISO639.getISO639Part2Code("en"));
		assertEquals("ISO code \"eng\" returns \"eng\"", "eng", ISO639.getISO639Part2Code("eng"));
		assertNull("ISO code \"\" returns null", ISO639.getISO639Part2Code(""));
		assertNull("ISO code null returns null", ISO639.getISO639Part2Code(null));
		assertEquals("Language name \"English\" returns ISO code \"eng\"", "eng", ISO639.getISO639Part2Code("English"));
		assertEquals("Language name \"english\" returns null", "eng", ISO639.getISO639Part2Code("english"));
		assertEquals("Language name \"Czech\" returns ISO code \"cze\"", "cze", ISO639.getISO639Part2Code("Czech"));
		assertNull("Language name \"Czech language\" returns null", ISO639.getISO639Part2Code("Czech language"));
		assertEquals("cze", ISO639.getISO639Part2Code("Czech language", true));
		assertEquals("fre", ISO639.getISO639Part2Code("The French don't like other languages", true));
		assertEquals("swe", ISO639.getISO639Part2Code("Does anyone speak sweedish?", true));
		assertEquals("nor", ISO639.getISO639Part2Code("Norweigan"));

		// Test isCodeMatching()
		assertTrue("ISO code \"ful\" matches language \"Fulah\"", ISO639.isCodeMatching("Fulah", "ful"));
		assertTrue("ISO code \"gd\" matches language \"Gaelic (Scots)\"", ISO639.isCodeMatching("Gaelic", "gd"));
		assertTrue("ISO code \"gla\" matches language \"Gaelic (Scots)\"", ISO639.isCodeMatching("Gaelic", "gla"));
		assertFalse("ISO code \"eng\" doesn't match language \"Gaelic (Scots)\"", ISO639.isCodeMatching("Gaelic", "eng"));
		assertTrue("ISO code \"gla\" matches ISO code \"gd\"", ISO639.isCodesMatching("gla", "gd"));
		assertTrue("ISO code \"ice\" matches ISO code \"is\"", ISO639.isCodesMatching("ice", "is"));
		assertTrue("ISO code \"isl\" matches ISO code \"ice\"", ISO639.isCodesMatching("isl", "ice"));
		assertFalse("ISO code \"lav\" doesn't match ISO code \"en\"", ISO639.isCodesMatching("lav", "en"));

		// Test getISOCode()
		assertEquals("ISO code \"eng\" returns ISO code \"en\"", ISO639.getISOCode("eng"), "en");
		assertEquals("ISO code \"ell\" returns ISO code \"el\"", ISO639.getISOCode("ell"), "el");
		assertEquals("ISO code \"gre\" returns ISO code \"el\"", ISO639.getISOCode("gre"), "el");
		assertEquals("ISO code \"gay\" returns ISO code \"gay\"", ISO639.getISOCode("gay"), "gay"); // No pun intended
		assertNull("Language name \"Czech language\" returns null", ISO639.getISOCode("Czech language"));
		assertEquals(ISO639.getISOCode("Czech language", true), "cs");
		assertEquals(ISO639.getISOCode("The French don't like other languages", true), "fr");
		assertEquals(ISO639.getISOCode("Where do they speak Choctaw?", true), "cho");
		assertEquals(ISO639.getISOCode("Where do they speak madureese?", true), "mad");
		assertEquals(ISO639.getISOCode("Where do they speak philipine?", true), "phi");
		assertEquals(ISO639.getISOCode("Where do they speak portugese?", true), "pt");
		assertEquals(ISO639.getISOCode("Where do they speak sinhaleese?", true), "si");
		assertEquals(ISO639.getISOCode("Does anyone speak sweedish?", true), "sv");

		// Test multiple language names
		ISO639 entry1 = ISO639.get("Imperial Aramaic");
		ISO639 entry2 = ISO639.get("Official Aramaic");
		ISO639 entry3 = ISO639.get("arc");
		assertEquals("Imperial Aramaic (700-300 BCE)", entry1.getFirstName());
		assertEquals(entry1, entry2);
		assertEquals(entry1, entry3);
		assertEquals("Official Aramaic (700-300 BCE)", entry3.getNames().get(1));
		assertEquals("Official Aramaic (700-300 BCE)", entry3.getNames().get(1));
		assertEquals(3, ISO639.get("SEPEDI").getNames().size());
	}
}
