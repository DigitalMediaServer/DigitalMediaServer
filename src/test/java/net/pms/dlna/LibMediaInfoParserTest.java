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

import org.junit.BeforeClass;
import org.junit.Test;

import net.pms.PMS;

public class LibMediaInfoParserTest {
	@BeforeClass
	public static void SetUPClass() {
		PMS.configureJNA();
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
	public void testGetAvcLevel() throws Exception {
		assertThat(LibMediaInfoParser.getAvcLevel("Main@L2.0")).isEqualTo("2.0");
		assertThat(LibMediaInfoParser.getAvcLevel("High@L3.0")).isEqualTo("3.0");
		assertThat(LibMediaInfoParser.getAvcLevel("high@l4.0")).isEqualTo("4.0");
		assertThat(LibMediaInfoParser.getAvcLevel("hIgH@L4.1")).isEqualTo("4.1");
		assertThat(LibMediaInfoParser.getAvcLevel("5.1")).isNull();
		assertThat(LibMediaInfoParser.getAvcLevel("level5")).isNull();
	}

	@Test
	public void testGetAvcProfile() throws Exception {
		assertThat(LibMediaInfoParser.getAvcProfile("Main@L2.0")).isEqualTo("main");
		assertThat(LibMediaInfoParser.getAvcProfile("High@L3.0")).isEqualTo("high");
		assertThat(LibMediaInfoParser.getAvcProfile("high@l4.0")).isEqualTo("high");
		assertThat(LibMediaInfoParser.getAvcProfile("hIgH@L4.1")).isEqualTo("high");
		assertThat(LibMediaInfoParser.getAvcProfile("LOW@L4.1")).isEqualTo("low");
	}

	@Test
	public void testGetAvcProfileInvalidInput() throws Exception {
		assertThat(LibMediaInfoParser.getAvcProfile("@L2.0")).isNull();
		assertThat(LibMediaInfoParser.getAvcProfile("@l2.0")).isNull();
	}

	@Test
	public void testGetBitrate() throws Exception {
		assertThat(LibMediaInfoParser.parseBitRate("256")).isEqualTo(256);
		assertThat(LibMediaInfoParser.parseBitRate("128/192")).isEqualTo(128);
	}

	@Test
	public void testGetBitrateInvalidInput() throws Exception {
		assertThat(LibMediaInfoParser.parseBitRate("")).isEqualTo(0);
		assertThat(LibMediaInfoParser.parseBitRate("asfd")).isEqualTo(0);
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
		assertThat(LibMediaInfoParser.parseSampleRate("44100")).isEqualTo("44100");
		assertThat(LibMediaInfoParser.parseSampleRate("24000khz")).isEqualTo("24000");
		assertThat(LibMediaInfoParser.parseSampleRate("48000 / 44100")).isEqualTo("48000");
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
}
