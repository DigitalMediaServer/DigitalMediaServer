/*
 * Digital Media Server, for streaming digital media to DLNA compatible devices
 * based on www.ps3mediaserver.org and www.universalmediaserver.com. Copyright
 * (C) 2016 Digital Media Server developers.
 *
 * This program is a free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; version 2 of the License only.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package net.pms.dlna;

import static org.junit.Assert.*;
import org.junit.Test;
import net.pms.util.Rational;

public class DLNAMediaInfoTest {

	@Test
	public void testAspectRatioContainer() {
		DLNAMediaInfo media = new DLNAMediaInfo();

		media.setAspectRatioContainer("4:3");
		assertEquals(Rational.valueOf(4, 3), media.getAspectRatioContainer());

		media.setAspectRatioContainer("4/3");
		assertEquals(Rational.valueOf(4, 3), media.getAspectRatioContainer());

		media.setAspectRatioContainer("16:12");
		assertEquals(Rational.valueOf(4, 3), media.getAspectRatioContainer());

		media.setAspectRatioContainer("1.25");
		assertEquals(Rational.valueOf(5, 4), media.getAspectRatioContainer());

		media.setAspectRatioContainer((String) null);
		assertNull(media.getAspectRatioContainer());

		media.setAspectRatioContainer(Rational.valueOf(16, 12));
		assertEquals(Rational.valueOf(4, 3), media.getAspectRatioContainer());

		media.setAspectRatioContainer(Rational.valueOf("4:3"));
		assertEquals(Rational.valueOf(4, 3), media.getAspectRatioContainer());

		media.setAspectRatioContainer(Rational.valueOf("1.25"));
		assertEquals(Rational.valueOf(5, 4), media.getAspectRatioContainer());

		media.setAspectRatioContainer(Rational.valueOf(16, 0));
		assertEquals(Rational.POSITIVE_INFINITY, media.getAspectRatioContainer());

		media.setAspectRatioContainer(Rational.valueOf(0, 16));
		assertNull(media.getAspectRatioContainer());

		media.setAspectRatioContainer((Rational) null);
		assertNull(media.getAspectRatioContainer());
	}

	@Test
	public void testAspectRatioDvdIso() {
		DLNAMediaInfo media = new DLNAMediaInfo();

		media.setAspectRatioDvdIso("4:3");
		assertEquals(Rational.valueOf(4, 3), media.getAspectRatioDvdIso());

		media.setAspectRatioDvdIso("4/3");
		assertEquals(Rational.valueOf(4, 3), media.getAspectRatioDvdIso());

		media.setAspectRatioDvdIso("16:12");
		assertEquals(Rational.valueOf(4, 3), media.getAspectRatioDvdIso());

		media.setAspectRatioDvdIso("1.25");
		assertEquals(Rational.valueOf(5, 4), media.getAspectRatioDvdIso());

		media.setAspectRatioDvdIso((String) null);
		assertNull(media.getAspectRatioDvdIso());

		media.setAspectRatioDvdIso(Rational.valueOf(16, 12));
		assertEquals(Rational.valueOf(4, 3), media.getAspectRatioDvdIso());

		media.setAspectRatioDvdIso(Rational.valueOf("4:3"));
		assertEquals(Rational.valueOf(4, 3), media.getAspectRatioDvdIso());

		media.setAspectRatioDvdIso(Rational.valueOf("1.25"));
		assertEquals(Rational.valueOf(5, 4), media.getAspectRatioDvdIso());

		media.setAspectRatioDvdIso(Rational.valueOf(16, 0));
		assertEquals(Rational.POSITIVE_INFINITY, media.getAspectRatioDvdIso());

		media.setAspectRatioDvdIso(Rational.valueOf(0, 16));
		assertNull(media.getAspectRatioDvdIso());

		media.setAspectRatioDvdIso((Rational) null);
		assertNull(media.getAspectRatioDvdIso());
	}

	@Test
	public void testAspectRatioVideoTrack() {
		DLNAMediaInfo media = new DLNAMediaInfo();

		media.setAspectRatioVideoTrack("4:3");
		assertEquals(Rational.valueOf(4, 3), media.getAspectRatioVideoTrack());

		media.setAspectRatioVideoTrack("4/3");
		assertEquals(Rational.valueOf(4, 3), media.getAspectRatioVideoTrack());

		media.setAspectRatioVideoTrack("16:12");
		assertEquals(Rational.valueOf(4, 3), media.getAspectRatioVideoTrack());

		media.setAspectRatioVideoTrack("1.25");
		assertEquals(Rational.valueOf(5, 4), media.getAspectRatioVideoTrack());

		media.setAspectRatioVideoTrack((String) null);
		assertNull(media.getAspectRatioVideoTrack());

		media.setAspectRatioVideoTrack(Rational.valueOf(16, 12));
		assertEquals(Rational.valueOf(4, 3), media.getAspectRatioVideoTrack());

		media.setAspectRatioVideoTrack(Rational.valueOf("4:3"));
		assertEquals(Rational.valueOf(4, 3), media.getAspectRatioVideoTrack());

		media.setAspectRatioVideoTrack(Rational.valueOf("1.25"));
		assertEquals(Rational.valueOf(5, 4), media.getAspectRatioVideoTrack());

		media.setAspectRatioVideoTrack(Rational.valueOf(16, 0));
		assertEquals(Rational.POSITIVE_INFINITY, media.getAspectRatioVideoTrack());

		media.setAspectRatioVideoTrack(Rational.valueOf(0, 16));
		assertNull(media.getAspectRatioVideoTrack());

		media.setAspectRatioVideoTrack((Rational) null);
		assertNull(media.getAspectRatioVideoTrack());
	}
}
