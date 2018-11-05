/*
 * PS3 Media Server, for streaming any medias to your PS3.
 * Copyright (C) 2013  I. Sokolov
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
package net.pms.dlna;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.LoggerContext;
import net.pms.PMS;
import net.pms.configuration.FormatConfiguration;
import net.pms.media.H264Level;
import net.pms.media.H265Level;
import net.pms.media.VC1Level;
import net.pms.media.VP9Level;

public class LibMediaInfoParserTest {

	@BeforeClass
	public static void SetUPClass() {
		PMS.configureJNA();
	}

	/**
	 * Set up testing conditions before running the tests.
	 */
	@Before
	public void setUp() {
		// Silence all log messages from the DMS code that is being tested
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		context.reset();
	}

	@Test
	public void testGetReferenceFrameCount() throws Exception {
		assertThat(LibMediaInfoParser.getReferenceFrameCount("-5 6")).isEqualTo(-1);
		assertThat(LibMediaInfoParser.getReferenceFrameCount("7")).isEqualTo(7);
		assertThat(LibMediaInfoParser.getReferenceFrameCount("2 frame2")).isEqualTo(-1);
		assertThat(LibMediaInfoParser.getReferenceFrameCount("-16 frame3")).isEqualTo(-1);
		assertThat(LibMediaInfoParser.getReferenceFrameCount("")).isEqualTo(-1);
		assertThat(LibMediaInfoParser.getReferenceFrameCount("strange1")).isEqualTo(-1);
		assertThat(LibMediaInfoParser.getReferenceFrameCount("6ref")).isEqualTo(-1);
	}

	@Test
	public void testGetBitrate() throws Exception {
		assertThat(LibMediaInfoParser.parseBitRate("256", false)).isEqualTo(256);
		assertThat(LibMediaInfoParser.parseBitRate("128/192", false)).isEqualTo(128);
	}

	@Test
	public void testGetBitrateInvalidInput() throws Exception {
		assertThat(LibMediaInfoParser.parseBitRate("", false)).isEqualTo(-1);
		assertThat(LibMediaInfoParser.parseBitRate("asfd", false)).isEqualTo(-1);
	}

	@Test
	public void testGetSpecificID() throws Exception {
		assertThat(LibMediaInfoParser.getSpecificID("256")).isEqualTo(256);
		assertThat(LibMediaInfoParser.getSpecificID("189 (0xBD)-32 (0x80)")).isEqualTo(32);
		assertThat(LibMediaInfoParser.getSpecificID("189 (0xBD)")).isEqualTo(189);
		assertThat(LibMediaInfoParser.getSpecificID("189 (0xBD)-")).isEqualTo(189);
	}

	@Test
	public void testGetSampleFrequency() throws Exception {
		assertThat(LibMediaInfoParser.parseSamplingRate("44100")).isEqualTo(44100);
		assertThat(LibMediaInfoParser.parseSamplingRate("24000khz")).isEqualTo(24000);
		assertThat(LibMediaInfoParser.parseSamplingRate("48000 / 44100")).isEqualTo(48000);
	}

	@Test
	public void testGetFPSValue() throws Exception {
		assertThat(LibMediaInfoParser.getFPSValue("30")).isEqualTo("30");
		assertThat(LibMediaInfoParser.getFPSValue("30fps")).isEqualTo("30");
	}

	@Test
	public void testGetFrameRateModeValue() throws Exception {
		assertThat(LibMediaInfoParser.getFrameRateModeValue("VBR")).isEqualTo("VBR");
		assertThat(LibMediaInfoParser.getFrameRateModeValue("CBR/VBR")).isEqualTo("CBR");
	}

	@Test
	public void testGetLang() throws Exception {
		assertThat(LibMediaInfoParser.getLang("enUS")).isEqualTo("enUS");
		assertThat(LibMediaInfoParser.getLang("ptBR (Brazil)")).isEqualTo("ptBR");
		assertThat(LibMediaInfoParser.getLang("enUS/GB")).isEqualTo("enUS");
	}

	@Test
	public void testVideoLevelH264() {
		DLNAMediaInfo media = new DLNAMediaInfo();
		media.setCodecV(FormatConfiguration.H264);

		LibMediaInfoParser.setVideoProfileAndLevel(media, "");
		assertNull(media.getVideoLevel());
		assertNull(media.getVideoProfile());

		LibMediaInfoParser.setVideoProfileAndLevel(media, "level");
		assertNull(media.getVideoLevel());
		assertEquals("level", media.getVideoProfile());

		LibMediaInfoParser.setVideoProfileAndLevel(media, "low@L1b");
		assertEquals(H264Level.L1b, media.getVideoLevel());
		assertEquals("low", media.getVideoProfile());

		LibMediaInfoParser.setVideoProfileAndLevel(media, "Main@L2.0");
		assertEquals(H264Level.L2, media.getVideoLevel());
		assertEquals("Main", media.getVideoProfile());

		LibMediaInfoParser.setVideoProfileAndLevel(media, "High@L3.0");
		assertEquals(H264Level.L3, media.getVideoLevel());
		assertEquals("High", media.getVideoProfile());

		LibMediaInfoParser.setVideoProfileAndLevel(media, "high@l4,0");
		assertEquals(H264Level.L4, media.getVideoLevel());
		assertEquals("high", media.getVideoProfile());

		LibMediaInfoParser.setVideoProfileAndLevel(media, "hIgH@L4.1");
		assertEquals(H264Level.L4_1, media.getVideoLevel());
		assertEquals("hIgH", media.getVideoProfile());

		LibMediaInfoParser.setVideoProfileAndLevel(media, "5");
		assertNull(media.getVideoLevel());
		assertEquals("5", media.getVideoProfile());

		LibMediaInfoParser.setVideoProfileAndLevel(media, "LEVEL 5.1");
		assertNull(media.getVideoLevel());
		assertEquals("LEVEL 5.1", media.getVideoProfile());

		LibMediaInfoParser.setVideoProfileAndLevel(media, "level5,2");
		assertNull(media.getVideoLevel());
		assertEquals("level5,2", media.getVideoProfile());
	}

	@Test
	public void testVideoLevelH265() {
		DLNAMediaInfo media = new DLNAMediaInfo();
		media.setCodecV(FormatConfiguration.H265);

		LibMediaInfoParser.setVideoProfileAndLevel(media, "");
		assertNull(media.getVideoLevel());
		assertNull(media.getVideoProfile());

		LibMediaInfoParser.setVideoProfileAndLevel(media, "level");
		assertNull(media.getVideoLevel());
		assertEquals("level", media.getVideoProfile());

		LibMediaInfoParser.setVideoProfileAndLevel(media, "low@L1b");
		assertNull(media.getVideoLevel());
		assertEquals("low", media.getVideoProfile());

		LibMediaInfoParser.setVideoProfileAndLevel(media, "Main@L2.0@High");
		assertEquals(H265Level.L2, media.getVideoLevel());
		assertEquals("Main", media.getVideoProfile());

		LibMediaInfoParser.setVideoProfileAndLevel(media, "High@L3.0");
		assertEquals(H265Level.L3, media.getVideoLevel());
		assertEquals("High", media.getVideoProfile());

		LibMediaInfoParser.setVideoProfileAndLevel(media, "mAin@l4,0@maIN");
		assertEquals(H265Level.L4, media.getVideoLevel());
		assertEquals("mAin", media.getVideoProfile());

		LibMediaInfoParser.setVideoProfileAndLevel(media, "hIgH@L4.1");
		assertEquals(H265Level.L4_1, media.getVideoLevel());
		assertEquals("hIgH", media.getVideoProfile());

		LibMediaInfoParser.setVideoProfileAndLevel(media, "hIgH@L4.2@loW");
		assertNull(media.getVideoLevel());
		assertEquals("hIgH", media.getVideoProfile());

		LibMediaInfoParser.setVideoProfileAndLevel(media, "5");
		assertNull(media.getVideoLevel());
		assertEquals("5", media.getVideoProfile());

		LibMediaInfoParser.setVideoProfileAndLevel(media, "LEVEL 5.1");
		assertNull(media.getVideoLevel());
		assertEquals("LEVEL 5.1", media.getVideoProfile());

		LibMediaInfoParser.setVideoProfileAndLevel(media, "level5,2");
		assertNull(media.getVideoLevel());
		assertEquals("level5,2", media.getVideoProfile());

		LibMediaInfoParser.setVideoProfileAndLevel(media, "@l2.0");
		assertEquals(H265Level.L2, media.getVideoLevel());
		assertNull(media.getVideoProfile());

		LibMediaInfoParser.setVideoProfileAndLevel(media, "@6,1");
		assertEquals(H265Level.L6_1, media.getVideoLevel());
		assertNull(media.getVideoProfile());
	}

	@Test
	public void testVideoLevelVP9() {
		DLNAMediaInfo media = new DLNAMediaInfo();
		media.setCodecV(FormatConfiguration.VP9);

		LibMediaInfoParser.setVideoProfileAndLevel(media, "");
		assertNull(media.getVideoLevel());
		assertNull(media.getVideoProfile());

		LibMediaInfoParser.setVideoProfileAndLevel(media, "level");
		assertNull(media.getVideoLevel());
		assertEquals("level", media.getVideoProfile());

		LibMediaInfoParser.setVideoProfileAndLevel(media, "low@L1b");
		assertNull(media.getVideoLevel());
		assertNull(media.getVideoProfile());

		LibMediaInfoParser.setVideoProfileAndLevel(media, "Main@L2.0@High");
		assertNull(media.getVideoLevel());
		assertNull(media.getVideoProfile());

		LibMediaInfoParser.setVideoProfileAndLevel(media, "High@L3.0");
		assertEquals(VP9Level.L3, media.getVideoLevel());
		assertEquals("High", media.getVideoProfile());

		LibMediaInfoParser.setVideoProfileAndLevel(media, "mAin@l4,0@maIN");
		assertNull(media.getVideoLevel());
		assertNull(media.getVideoProfile());

		LibMediaInfoParser.setVideoProfileAndLevel(media, "hIgH@L4.1");
		assertEquals(VP9Level.L4_1, media.getVideoLevel());
		assertEquals("hIgH", media.getVideoProfile());

		LibMediaInfoParser.setVideoProfileAndLevel(media, "hIgH@L4.2@loW");
		assertNull(media.getVideoLevel());
		assertNull(media.getVideoProfile());

		LibMediaInfoParser.setVideoProfileAndLevel(media, "5");
		assertNull(media.getVideoLevel());
		assertEquals("5", media.getVideoProfile());

		LibMediaInfoParser.setVideoProfileAndLevel(media, "LEVEL 5.1");
		assertNull(media.getVideoLevel());
		assertEquals("LEVEL 5.1", media.getVideoProfile());

		LibMediaInfoParser.setVideoProfileAndLevel(media, "level5,2");
		assertNull(media.getVideoLevel());
		assertEquals("level5,2", media.getVideoProfile());

		LibMediaInfoParser.setVideoProfileAndLevel(media, "@l2.0");
		assertEquals(VP9Level.L2, media.getVideoLevel());
		assertNull(media.getVideoProfile());

		LibMediaInfoParser.setVideoProfileAndLevel(media, "@6,2");
		assertEquals(VP9Level.L6_2, media.getVideoLevel());
		assertNull(media.getVideoProfile());
	}

	@Test
	public void testVideoLevelVC1() {
		DLNAMediaInfo media = new DLNAMediaInfo();
		media.setCodecV(FormatConfiguration.VC1);

		LibMediaInfoParser.setVideoProfileAndLevel(media, "");
		assertNull(media.getVideoLevel());
		assertNull(media.getVideoProfile());

		LibMediaInfoParser.setVideoProfileAndLevel(media, "level");
		assertNull(media.getVideoLevel());
		assertEquals("level", media.getVideoProfile());

		LibMediaInfoParser.setVideoProfileAndLevel(media, "low@L1b");
		assertNull(media.getVideoLevel());
		assertEquals("low", media.getVideoProfile());

		LibMediaInfoParser.setVideoProfileAndLevel(media, "Main@L2.0@High");
		assertNull(media.getVideoLevel());
		assertEquals("Main", media.getVideoProfile());

		LibMediaInfoParser.setVideoProfileAndLevel(media, "High@L3.0");
		assertEquals(VC1Level.L3, media.getVideoLevel());
		assertEquals("High", media.getVideoProfile());

		LibMediaInfoParser.setVideoProfileAndLevel(media, "mAin@l4,0@maIN");
		assertNull(media.getVideoLevel());
		assertEquals("mAin", media.getVideoProfile());

		LibMediaInfoParser.setVideoProfileAndLevel(media, "hIgH@Ll");
		assertEquals(VC1Level.LOW, media.getVideoLevel());
		assertEquals("hIgH", media.getVideoProfile());

		LibMediaInfoParser.setVideoProfileAndLevel(media, "hIgH@L4.2@loW");
		assertNull(media.getVideoLevel());
		assertEquals("hIgH", media.getVideoProfile());

		LibMediaInfoParser.setVideoProfileAndLevel(media, "5");
		assertNull(media.getVideoLevel());
		assertEquals("5", media.getVideoProfile());

		LibMediaInfoParser.setVideoProfileAndLevel(media, "LEVEL 5.1");
		assertNull(media.getVideoLevel());
		assertEquals("LEVEL 5.1", media.getVideoProfile());

		LibMediaInfoParser.setVideoProfileAndLevel(media, "level5,2");
		assertNull(media.getVideoLevel());
		assertEquals("level5,2", media.getVideoProfile());

		LibMediaInfoParser.setVideoProfileAndLevel(media, "@l2.0");
		assertEquals(VC1Level.L2, media.getVideoLevel());
		assertNull(media.getVideoProfile());

		LibMediaInfoParser.setVideoProfileAndLevel(media, "@hl");
		assertEquals(VC1Level.HIGH, media.getVideoLevel());
		assertNull(media.getVideoProfile());
	}
}
