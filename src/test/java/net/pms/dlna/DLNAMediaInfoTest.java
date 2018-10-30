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
import net.pms.media.H264Level;
import net.pms.media.H265Level;

public class DLNAMediaInfoTest {

	@Test
	public void testH264() throws Exception {
		DLNAMediaInfo media = new DLNAMediaInfo();

		media.setVideoProfile("low@L1b");
		assertEquals(H264Level.L1b, media.getVideoLevel());
		assertEquals("low", media.getH264Profile());

		media.setVideoProfile("Main@L2.0");
		assertEquals(H264Level.L2, media.getVideoLevel());
		assertEquals("main", media.getH264Profile());

		media.setVideoProfile("High@L3.0");
		assertEquals(H264Level.L3, media.getVideoLevel());
		assertEquals("high", media.getH264Profile());

		media.setVideoProfile("high@l4.0");
		assertEquals(H264Level.L4, media.getVideoLevel());
		assertEquals("high", media.getH264Profile());

		media.setVideoProfile("hIgH@L4.1");
		assertEquals(H264Level.L4_1, media.getVideoLevel());
		assertEquals("high", media.getH264Profile());

		media.setVideoProfile("5");
		assertEquals(H264Level.L5, media.getVideoLevel());
		assertEquals("5", media.getH264Profile());

		media.setVideoProfile("LEVEL 5.1");
		assertEquals(H264Level.L5_1, media.getVideoLevel());
		assertEquals("level 5.1", media.getH264Profile());

		media.setVideoProfile("level5,2");
		assertEquals(H264Level.L5_2, media.getVideoLevel());
		assertEquals("level5,2", media.getH264Profile());

		media.setVideoProfile("level");
		assertNull(media.getVideoLevel());
		assertEquals("level", media.getH264Profile());
	}

	@Test
	public void testH265() throws Exception {
		DLNAMediaInfo media = new DLNAMediaInfo();

		media.setVideoProfile("Main@L2.0@High");
		assertEquals(H265Level.L2, media.getVideoLevel());
		assertEquals("main", media.getH265Profile());

		media.setVideoProfile("High@L3.0");
		assertEquals(H265Level.L3, media.getVideoLevel());
		assertEquals("high", media.getH265Profile());

		media.setVideoProfile("mAin@l4.0@maIN");
		assertEquals(H265Level.L4, media.getVideoLevel());
		assertEquals("main", media.getH265Profile());

		media.setVideoProfile("hIgH@L4.1");
		assertEquals(H265Level.L4_1, media.getVideoLevel());
		assertEquals("high", media.getH265Profile());

		media.setVideoProfile("hIgH@L4.2@loW");
		assertNull(media.getVideoLevel());
		assertEquals("high", media.getH265Profile());

		media.setVideoProfile("5");
		assertEquals(H265Level.L5, media.getVideoLevel());
		assertEquals("5", media.getH265Profile());

		media.setVideoProfile("LEVEL 5.1");
		assertEquals(H265Level.L5_1, media.getVideoLevel());
		assertEquals("level 5.1", media.getH265Profile());

		media.setVideoProfile("level5,2");
		assertEquals(H265Level.L5_2, media.getVideoLevel());
		assertEquals("level5,2", media.getH265Profile());

		media.setVideoProfile("level");
		assertNull(media.getVideoLevel());
		assertEquals("level", media.getH265Profile());

		media.setVideoProfile("@l2.0");
		assertEquals(H265Level.L2, media.getVideoLevel());
		assertNull(media.getH265Profile());

		media.setVideoProfile("@2");
		assertEquals(H265Level.L2, media.getVideoLevel());
		assertNull(media.getH265Profile());
	}
}
