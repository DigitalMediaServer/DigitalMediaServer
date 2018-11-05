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
import net.pms.formats.Format;
import net.pms.formats.FormatFactory;
import net.pms.formats.FormatType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

/**
 * Test basic functionality of {@link Format}.
 */
public class FormatFactoryTest {
	/**
	 * Set up testing conditions before running the tests.
	 */
	@Before
	public final void setUp() {
		// Silence all log messages from the DMS code that is being tested
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		context.reset();
	}

	/**
	 * Test edge cases for {@link FormatFactory#getAssociatedExtension(String)}.
	 */
	@Test
	public final void testFormatFactoryEdgeCases() {
		// Null string
		Format result = FormatFactory.getAssociatedFormat(null);
		assertNull("Null string matches no format", result);

		// Empty string
		result = FormatFactory.getAssociatedFormat("");
		assertNull("Empty string matches no extension", result);

		// Unsupported extension
		result = FormatFactory.getAssociatedFormat(
			"test.bogus"
		);
		assertNull(
			"Unsupported extension: \"test.bogus\" matches no format",
			result
		);

		// Confirm the protocol (e.g. WEB) is checked before the extension
		testSingleFormat("http://example.com/test.mp3", "WEB", null);
		testSingleFormat("http://example.com/test.asf?format=.wmv", "WEB", null);

		// confirm that the WEB format is assigned for arbitrary protocols
		testSingleFormat("svn+ssh://example.com/example.test", "WEB", null);
		testSingleFormat("bogus://example.com/test.test", "WEB", null);
		testSingleFormat("fake://example.com/test.test", "WEB", null);
		testSingleFormat("dms://example", "WEB", null);
	}

	/**
	 * Test whether {@link FormatFactory#getAssociatedExtension(String)} manages
	 * to retrieve the correct format.
	 */
	@Test
	public final void testFormatRetrieval() {
		testSingleFormat("test.ac3", "AC3", FormatType.AUDIO);
		testSingleFormat("test.act", "ADPCM", FormatType.AUDIO);
		testSingleFormat("test.aac", "ADTS", FormatType.AUDIO);
		testSingleFormat("test.aif", "AIFF", FormatType.AUDIO);
		testSingleFormat("test.aiff", "AIFF", FormatType.AUDIO);
		testSingleFormat("test.aifc", "AIFF", FormatType.AUDIO);
		testSingleFormat("test.ass", "ASS", FormatType.SUBTITLES);
		testSingleFormat("test.ssa", "ASS", FormatType.SUBTITLES);
		testSingleFormat("test.aa3", "ATRAC", FormatType.AUDIO);
		testSingleFormat("test.at3", "ATRAC", FormatType.AUDIO);
		testSingleFormat("test.at9", "ATRAC", FormatType.AUDIO);
		testSingleFormat("test.atrac", "ATRAC", FormatType.AUDIO);
		testSingleFormat("test.msa", "ATRAC", FormatType.AUDIO);
		testSingleFormat("test.oma", "ATRAC", FormatType.AUDIO);
		testSingleFormat("test.omg", "ATRAC", FormatType.AUDIO);
		testSingleFormat("test.au", "AU", FormatType.AUDIO);
		testSingleFormat("test.snd", "AU", FormatType.AUDIO);
		testSingleFormat("test.dff", "DFF", FormatType.AUDIO);
		testSingleFormat("test.dsf", "DSF", FormatType.AUDIO);
		testSingleFormat("test.dts", "DTS", FormatType.AUDIO);
		testSingleFormat("test.eac3", "EAC3", FormatType.AUDIO);
		testSingleFormat("test.fla", "FLAC", FormatType.AUDIO);
		testSingleFormat("test.flac", "FLAC", FormatType.AUDIO);
		testSingleFormat("test.gif", "GIF", FormatType.IMAGE);
		testSingleFormat("test.idx", "IDX", FormatType.SUBTITLES);
		testSingleFormat("test.img", "ISO", FormatType.ISO);
		testSingleFormat("test.iso", "ISO", FormatType.ISO);
		testSingleFormat("test.jpe", "JPG", FormatType.IMAGE);
		testSingleFormat("test.jpeg", "JPG", FormatType.IMAGE);
		testSingleFormat("test.jpg", "JPG", FormatType.IMAGE);
		testSingleFormat("test.mpo", "JPG", FormatType.IMAGE);
		testSingleFormat("test.m4a", "M4A", FormatType.AUDIO);
		testSingleFormat("test.m4b", "M4A", FormatType.AUDIO);
		testSingleFormat("test.m4r", "M4A", FormatType.AUDIO);
		testSingleFormat("test.3g2", "MKV", FormatType.CONTAINER);
		testSingleFormat("test.3gp", "MKV", FormatType.CONTAINER);
		testSingleFormat("test.3gp2", "MKV", FormatType.CONTAINER);
		testSingleFormat("test.asf", "MKV", FormatType.CONTAINER);
		testSingleFormat("test.asx", "MKV", FormatType.CONTAINER);
		testSingleFormat("test.dv", "MKV", FormatType.CONTAINER);
		testSingleFormat("test.evo", "MKV", FormatType.CONTAINER);
		testSingleFormat("test.flv", "MKV", FormatType.CONTAINER);
		testSingleFormat("test.hdmov", "MKV", FormatType.CONTAINER);
		testSingleFormat("test.hdm", "MKV", FormatType.CONTAINER);
		testSingleFormat("test.m2v", "MKV", FormatType.CONTAINER);
		testSingleFormat("test.mk3d", "MKV", FormatType.CONTAINER);
		testSingleFormat("test.mkv", "MKV", FormatType.CONTAINER);
		testSingleFormat("test.mov", "MKV", FormatType.CONTAINER);
		testSingleFormat("test.ogm", "OGG", FormatType.CONTAINER);
		testSingleFormat("test.ogv", "OGG", FormatType.CONTAINER);
		testSingleFormat("test.rmv", "MKV", FormatType.CONTAINER);
		testSingleFormat("test.rmvb", "MKV", FormatType.CONTAINER);
		testSingleFormat("test.rm", "MKV", FormatType.CONTAINER);
		testSingleFormat("test.mxf", "MKV", FormatType.CONTAINER);
		testSingleFormat("test.webm", "MKV", FormatType.CONTAINER);
		testSingleFormat("test.265", "MKV", FormatType.CONTAINER);
		testSingleFormat("test.h265", "MKV", FormatType.CONTAINER);
		testSingleFormat("test.mlp", "MLP", FormatType.AUDIO);
		testSingleFormat("test.mp3", "MP3", FormatType.AUDIO);
		testSingleFormat("test.mpc", "MPC", FormatType.AUDIO);
		testSingleFormat("test.mp+", "MPC", FormatType.AUDIO);
		testSingleFormat("test.mpp", "MPC", FormatType.AUDIO);
		testSingleFormat("test.avi", "MPG", FormatType.CONTAINER);
		testSingleFormat("test.div", "MPG", FormatType.CONTAINER);
		testSingleFormat("test.divx", "MPG", FormatType.CONTAINER);
		testSingleFormat("test.dvr", "DVRMS", FormatType.CONTAINER);
		testSingleFormat("test.dvr-ms", "DVRMS", FormatType.CONTAINER);
		testSingleFormat("test.m2p", "MPG", FormatType.CONTAINER);
		testSingleFormat("test.m2t", "MPG", FormatType.CONTAINER);
		testSingleFormat("test.m2ts", "MPG", FormatType.CONTAINER);
		testSingleFormat("test.m4v", "MPG", FormatType.CONTAINER);
		testSingleFormat("test.mj2", "MPG", FormatType.CONTAINER);
		testSingleFormat("test.mjp2", "MPG", FormatType.CONTAINER);
		testSingleFormat("test.mod", "MPG", FormatType.CONTAINER);
		testSingleFormat("test.mp4", "MPG", FormatType.CONTAINER);
		testSingleFormat("test.mpe", "MPG", FormatType.CONTAINER);
		testSingleFormat("test.mpeg", "MPG", FormatType.CONTAINER);
		testSingleFormat("test.mpg", "MPG", FormatType.CONTAINER);
		testSingleFormat("test.mts", "MPG", FormatType.CONTAINER);
		testSingleFormat("test.s4ud", "MPG", FormatType.CONTAINER);
		testSingleFormat("test.tivo", "MPG", FormatType.CONTAINER);
		testSingleFormat("test.tmf", "MPG", FormatType.CONTAINER);
		testSingleFormat("test.tp", "MPG", FormatType.CONTAINER);
		testSingleFormat("test.ts", "MPG", FormatType.CONTAINER);
		testSingleFormat("test.ty", "MPG", FormatType.CONTAINER);
		testSingleFormat("test.vdr", "MPG", FormatType.CONTAINER);
		testSingleFormat("test.vob", "MPG", FormatType.CONTAINER);
		testSingleFormat("test.vro", "MPG", FormatType.CONTAINER);
		testSingleFormat("test.wm", "MPG", FormatType.CONTAINER);
		testSingleFormat("test.wmv", "MPG", FormatType.CONTAINER);
		testSingleFormat("test.wtv", "MPG", FormatType.CONTAINER);
		testSingleFormat("test.mpa", "MPA", FormatType.AUDIO);
		testSingleFormat("test.m1a", "MPA", FormatType.AUDIO);
		testSingleFormat("test.m2a", "MPA", FormatType.AUDIO);
		testSingleFormat("test.mp1", "MPA", FormatType.AUDIO);
		testSingleFormat("test.mp2", "MPA", FormatType.AUDIO);
		testSingleFormat("test.sub", "MicroDVD", FormatType.SUBTITLES);
		testSingleFormat("test.ape", "MonkeysAudio", FormatType.AUDIO);
		testSingleFormat("test.oga", "OGA", FormatType.AUDIO);
		testSingleFormat("test.ogg", "OGG", FormatType.CONTAINER);
		testSingleFormat("test.spx", "OGA", FormatType.AUDIO);
		testSingleFormat("test.opus", "OGA", FormatType.AUDIO);
		testSingleFormat("test.pls", "PLAYLIST", FormatType.PLAYLIST);
		testSingleFormat("test.m3u", "PLAYLIST", FormatType.PLAYLIST);
		testSingleFormat("test.m3u8", "PLAYLIST", FormatType.PLAYLIST);
		testSingleFormat("test.cue", "PLAYLIST", FormatType.PLAYLIST);
		testSingleFormat("test.ups", "PLAYLIST", FormatType.PLAYLIST);
		testSingleFormat("test.ra", "RA", FormatType.AUDIO);
		testSingleFormat("test.png", "PNG", FormatType.IMAGE);
		testSingleFormat("test.3fr", "RAW", FormatType.IMAGE);
		testSingleFormat("test.ari", "RAW", FormatType.IMAGE);
		testSingleFormat("test.arw", "RAW", FormatType.IMAGE);
		testSingleFormat("test.bay", "RAW", FormatType.IMAGE);
		testSingleFormat("test.cap", "RAW", FormatType.IMAGE);
		testSingleFormat("test.cr2", "RAW", FormatType.IMAGE);
		testSingleFormat("test.crw", "RAW", FormatType.IMAGE);
		testSingleFormat("test.dcr", "RAW", FormatType.IMAGE);
		testSingleFormat("test.dcs", "RAW", FormatType.IMAGE);
		testSingleFormat("test.dng", "RAW", FormatType.IMAGE);
		testSingleFormat("test.drf", "RAW", FormatType.IMAGE);
		testSingleFormat("test.eip", "RAW", FormatType.IMAGE);
		testSingleFormat("test.erf", "RAW", FormatType.IMAGE);
		testSingleFormat("test.fff", "RAW", FormatType.IMAGE);
		testSingleFormat("test.iiq", "RAW", FormatType.IMAGE);
		testSingleFormat("test.k25", "RAW", FormatType.IMAGE);
		testSingleFormat("test.kdc", "RAW", FormatType.IMAGE);
		testSingleFormat("test.mdc", "RAW", FormatType.IMAGE);
		testSingleFormat("test.mef", "RAW", FormatType.IMAGE);
		testSingleFormat("test.mos", "RAW", FormatType.IMAGE);
		testSingleFormat("test.mrw", "RAW", FormatType.IMAGE);
		testSingleFormat("test.nef", "RAW", FormatType.IMAGE);
		testSingleFormat("test.nrw", "RAW", FormatType.IMAGE);
		testSingleFormat("test.obm", "RAW", FormatType.IMAGE);
		testSingleFormat("test.orf", "RAW", FormatType.IMAGE);
		testSingleFormat("test.pef", "RAW", FormatType.IMAGE);
		testSingleFormat("test.ptx", "RAW", FormatType.IMAGE);
		testSingleFormat("test.pxn", "RAW", FormatType.IMAGE);
		testSingleFormat("test.r3d", "RAW", FormatType.IMAGE);
		testSingleFormat("test.raf", "RAW", FormatType.IMAGE);
		testSingleFormat("test.raw", "RAW", FormatType.IMAGE);
		testSingleFormat("test.rwl", "RAW", FormatType.IMAGE);
		testSingleFormat("test.rw2", "RAW", FormatType.IMAGE);
		testSingleFormat("test.rwz", "RAW", FormatType.IMAGE);
		testSingleFormat("test.sr2", "RAW", FormatType.IMAGE);
		testSingleFormat("test.srf", "RAW", FormatType.IMAGE);
		testSingleFormat("test.srw", "RAW", FormatType.IMAGE);
		testSingleFormat("test.x3f", "RAW", FormatType.IMAGE);
		testSingleFormat("test.smi", "SAMI", FormatType.SUBTITLES);
		testSingleFormat("test.shn", "SHN", FormatType.AUDIO);
		testSingleFormat("test.sup", "SUP", FormatType.SUBTITLES);
		testSingleFormat("test.srt", "SubRip", FormatType.SUBTITLES);
		testSingleFormat("test.3g2a", "THREEG2A", FormatType.AUDIO);
		testSingleFormat("test.3ga", "THREEGA", FormatType.AUDIO);
		testSingleFormat("test.amr", "THREEGA", FormatType.AUDIO);
		testSingleFormat("test.3gpa", "THREEGA", FormatType.AUDIO);
		testSingleFormat("test.tta", "TTA", FormatType.AUDIO);
		testSingleFormat("test.txt", "TXT", FormatType.SUBTITLES);
		testSingleFormat("test.thd", "TrueHD", FormatType.AUDIO);
		testSingleFormat("test.tif", "TIFF", FormatType.IMAGE);
		testSingleFormat("test.tiff", "TIFF", FormatType.IMAGE);
		testSingleFormat("test.wav", "WAV", FormatType.AUDIO);
		testSingleFormat("test.wave", "WAV", FormatType.AUDIO);
		testSingleFormat("test.wv", "WavPack", FormatType.AUDIO);
		testSingleFormat("test.wvp", "WavPack", FormatType.AUDIO);
		testSingleFormat("test.vtt", "WebVTT", FormatType.SUBTITLES);
		testSingleFormat("test.wma", "WMA", FormatType.AUDIO);
		testSingleFormat("http://example.com/", "WEB", null);
	}


	/**
	 * Verify if a filename is recognized as a given FormatType. Use
	 * <code>null</code> as formatName when no match is expected.
	 *
	 * @param filename
	 *            The filename to verify.
	 * @param formatName
	 *            The name of the expected format.
	 */
	private static void testSingleFormat(final String filename, final String formatName, FormatType type) {
		Format result = FormatFactory.getAssociatedFormat(filename);

		if (result != null) {
			assertEquals("\"" + filename + "\" is expected to match",
					formatName, result.toString());
			assertEquals("\"" + filename + "\" is expected to be of type " + type, type, result.getType());
		} else {
			assertNull("\"" + filename + "\" is expected to match nothing", formatName);
		}
	}
}
