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
import static org.junit.Assert.assertNull;
import org.junit.Test;
import net.pms.media.H265Level;

public class H265LevelTest {

	@Test
	public void testStaticInstances() {
		assertNull(H265Level.typeOf("1b"));
		assertEquals(H265Level.L2, H265Level.typeOf("2"));
		assertEquals(H265Level.L2, H265Level.typeOf("2.0"));
		assertEquals(H265Level.L3, H265Level.typeOf("3,0"));
		assertEquals(H265Level.L4, H265Level.typeOf(" 4.0"));
		assertEquals(H265Level.L4_1, H265Level.typeOf("  4.1  "));
		assertEquals(H265Level.L5_1, H265Level.typeOf("5.1  "));
	}
}
