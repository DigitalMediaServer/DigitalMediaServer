/*
 * PS3 Media Server, for streaming any medias to your PS3.
 * Copyright (C) 2008  A.Brochard
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

package net.pms.test.formats;

import ch.qos.logback.classic.LoggerContext;
import net.pms.formats.*;
import net.pms.formats.audio.AC3;
import net.pms.formats.audio.ADPCM;
import net.pms.formats.audio.ADTS;
import net.pms.formats.audio.AIFF;
import net.pms.formats.audio.ATRAC;
import net.pms.formats.audio.AU;
import net.pms.formats.audio.DFF;
import net.pms.formats.audio.DSF;
import net.pms.formats.audio.DTS;
import net.pms.formats.audio.EAC3;
import net.pms.formats.audio.FLAC;
import net.pms.formats.audio.M4A;
import net.pms.formats.audio.MKA;
import net.pms.formats.audio.MLP;
import net.pms.formats.audio.MonkeysAudio;
import net.pms.formats.audio.MP3;
import net.pms.formats.audio.MPA;
import net.pms.formats.audio.MPC;
import net.pms.formats.audio.OGA;
import net.pms.formats.audio.RA;
import net.pms.formats.audio.SHN;
import net.pms.formats.audio.THREEGA;
import net.pms.formats.audio.THREEG2A;
import net.pms.formats.audio.TrueHD;
import net.pms.formats.audio.TTA;
import net.pms.formats.audio.WAV;
import net.pms.formats.audio.WavPack;
import net.pms.formats.audio.WMA;
import net.pms.formats.image.GIF;
import net.pms.formats.image.JPG;
import net.pms.formats.image.PNG;
import net.pms.formats.image.RAW;
import net.pms.formats.image.TIFF;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

/**
 * Test basic functionality of {@link Format}.
 */
public class FormatTest {
	@Before
	public void setUp() {
		// Silence all log messages from the DMS code that is being tested
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        context.reset();
	}

    /**
     * Test edge cases for {@link Format#match(String)}.
     */
    @Test
	public void testFormatEdgeCases() {
    	// Empty string
		assertEquals("MP3 does not match \"\"", false, new MP3().match(""));

    	// Null string
		assertEquals("MP3 does not match null", false, new MP3().match(null));

		// Mixed case
		assertEquals("TIFF matches \"tEsT.TiFf\"", true, new TIFF().match("tEsT.TiFf"));

		// Starting with identifier instead of ending
		assertEquals("TIFF does not match \"tiff.test\"", false, new TIFF().match("tiff.test"));

		// Substring
		assertEquals("TIFF does not match \"not.tiff.but.mp3\"", false, new TIFF().match("not.tiff.but.mp3"));
    }

    /**
     * Test if {@link Format#match(String)} manages to match the identifiers
     * specified in each format with getId().
     */
    @Test
	public void testFormatIdentifiers() {
		// Identifier tests based on the identifiers defined in getId() of each class
		assertEquals("AC3 matches \"test.ac3\"", true, new AC3().match("test.ac3"));
		assertEquals("ADPCM matches \"test.act\"", true, new ADPCM().match("test.act"));
		assertEquals("ADTS matches \"test.aac\"", true, new ADTS().match("test.aac"));
		assertEquals("AIFF matches \"test.aiff\"", true, new AIFF().match("test.aiff"));
		assertEquals("MonkeysAudio matches \"test.ape\"", true, new MonkeysAudio().match("test.ape"));
		assertEquals("ATRAC matches \"test.aa3\"", true, new ATRAC().match("test.aa3"));
		assertEquals("AU matches \"test.au\"", true, new AU().match("test.au"));
		assertEquals("DFF matches \"test.dff\"", true, new DFF().match("test.dff"));
		assertEquals("DSF matches \"test.dsf\"", true, new DSF().match("test.dsf"));
		assertEquals("DTS matches \"test.dts\"", true, new DTS().match("test.dts"));
		assertEquals("DVRMS matches \"test.dvr-ms\"", true, new DVRMS().match("test.dvr-ms"));
		assertEquals("EAC3 matches \"test.ec3\"", true, new EAC3().match("test.ec3"));
		assertEquals("FLAC matches \"test.flac\"", true, new FLAC().match("test.flac"));
		assertEquals("GIF matches \"test.gif\"", true, new GIF().match("test.gif"));
		assertEquals("ISO matches \"test.iso\"", true, new ISO().match("test.iso"));
		assertEquals("JPG matches \"test.jpg\"", true, new JPG().match("test.jpg"));
		assertEquals("M4A matches \"test.m4a\"", true, new M4A().match("test.m4a"));
		assertEquals("MKA matches \"test.mka\"", true, new MKA().match("test.mka"));
		assertEquals("MKV matches \"test.mkv\"", true, new MKV().match("test.mkv"));
		assertEquals("MLP matches \"test.mlp\"", true, new MLP().match("test.mlp"));
		assertEquals("MP3 matches \"test.mp3\"", true, new MP3().match("test.mp3"));
		assertEquals("MPC matches \"test.mpc\"", true, new MPC().match("test.mpc"));
		assertEquals("MPA matches \"test.mp2\"", true, new MPA().match("test.mp2"));
		assertEquals("MPG matches \"test.mpg\"", true, new MPG().match("test.mpg"));
		assertEquals("OGA matches \"test.oga\"", true, new OGA().match("test.oga"));
		assertEquals("OGG matches \"test.ogg\"", true, new OGG().match("test.ogg"));
		assertEquals("PNG matches \"test.png\"", true, new PNG().match("test.png"));
		assertEquals("RA matches \"test.ra\"", true, new RA().match("test.ra"));
		assertEquals("RAW matches \"test.arw\"", true, new RAW().match("test.arw"));
		assertEquals("SHN matches \"test.shn\"", true, new SHN().match("test.shn"));
		assertEquals("THREEGA matches \"test.3ga\"", true, new THREEGA().match("test.3ga"));
		assertEquals("THREEG2A matches \"test.3g2a\"", true, new THREEG2A().match("test.3g2a"));
		assertEquals("TIF matches \"test.tiff\"", true, new TIFF().match("test.tiff"));
		assertEquals("TrueHD matches \"test.thd\"", true, new TrueHD().match("test.thd"));
		assertEquals("TTA matches \"test.tta\"", true, new TTA().match("test.tta"));
		assertEquals("WAV matches \"test.wav\"", true, new WAV().match("test.wav"));
		assertEquals("WavPack matches \"test.wv\"", true, new WavPack().match("test.wv"));
		assertEquals("WEB matches \"http\"", true, new WEB().match("http://test.org/"));
		assertEquals("WMA matches \"test.wma\"", true, new WMA().match("test.wma"));
	}
}
