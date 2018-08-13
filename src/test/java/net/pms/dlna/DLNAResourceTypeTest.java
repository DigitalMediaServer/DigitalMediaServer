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
package net.pms.dlna;

import ch.qos.logback.classic.LoggerContext;
import java.io.File;
import java.util.Random;
import net.pms.configuration.FormatConfiguration;
import net.pms.dlna.DLNAResource;
import net.pms.dlna.MediaType;
import net.pms.dlna.RealFile;
import net.pms.dlna.WebStream;
import net.pms.formats.Format;
import net.pms.formats.WEB;
import net.pms.util.FileUtil;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;

public class DLNAResourceTypeTest {
	private static DLNAResource video;
	private static DLNAResource audio;
	private static DLNAResource image;
	private static DLNAResource webImage;
	private static DLNAResource webVideo;
	private static DLNAResource webAudio;

	@BeforeClass
	public static void setUpClass() {
		image = new RealFile(getNonExistingFile("test.jpg"));
		image.isValid();
		audio = new RealFile(getNonExistingFile("test.mp3"));
		audio.isValid();
		video = new RealFile(getNonExistingFile("test.mpg"));
		video.isValid();
		webImage = new WebStream("", "http://example.com/test.jpg", "", MediaType.IMAGE);
		webImage.isValid();
		webAudio = new WebStream("", "http://example.com/test.mp3", "", MediaType.AUDIO);
		webAudio.isValid();
		webVideo = new WebStream("", "http://example.com/test.mpg", "", MediaType.VIDEO);
		webVideo.isValid();
	}

	@Before
	public void setUp() {
		// Silence all log messages from the DMS code that is being tested
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		context.reset();
	}

	@Test
	public void testFormat() {
		assertNotNull(image);
		assertNotNull(image.getFormat());
		assertEquals(Format.Identifier.JPG, image.getFormat().getIdentifier());

		assertNotNull(audio);
		assertNotNull(audio.getFormat());
		assertEquals(Format.Identifier.MP3, audio.getFormat().getIdentifier());

		assertNotNull(video);
		assertNotNull(video.getFormat());
		assertEquals(Format.Identifier.MPG, video.getFormat().getIdentifier());

		assertNotNull(webImage);
		assertNotNull(webImage.getFormat());
		assertEquals(Format.Identifier.WEB, webImage.getFormat().getIdentifier());

		assertNotNull(webAudio);
		assertNotNull(webAudio.getFormat());
		assertEquals(Format.Identifier.WEB, webAudio.getFormat().getIdentifier());

		assertNotNull(webVideo);
		assertNotNull(webVideo.getFormat());
		assertEquals(Format.Identifier.WEB, webVideo.getFormat().getIdentifier());
	}

	@Test
	public void testIsImage() {
		assertTrue(image.isImage());
		assertFalse(audio.isImage());
		assertFalse(video.isImage());
		assertTrue(webImage.isImage());
		assertFalse(webAudio.isImage());
		assertFalse(webVideo.isImage());
	}

	@Test
	public void testIsAudio() {
		assertFalse(image.isAudio());
		assertTrue(audio.isAudio());
		assertFalse(video.isAudio());
		assertFalse(webImage.isAudio());
		assertTrue(webAudio.isAudio());
		assertFalse(webVideo.isAudio());
	}

	@Test
	public void testIsVideo() {
		assertFalse(image.isVideo());
		assertFalse(audio.isVideo());
		assertTrue(video.isVideo());
		assertFalse(webImage.isVideo());
		assertFalse(webAudio.isVideo());
		assertTrue(webVideo.isVideo());
	}

	@Test
	public void testMatches() {
		// Kind of useless test since nothing is parsed as the files are imaginary
		assertFalse(image.matches(MediaType.VIDEO, FormatConfiguration.JPG));
		assertFalse(audio.matches(MediaType.VIDEO, FormatConfiguration.MP3));
		assertTrue(video.matches(MediaType.VIDEO));
		assertFalse(video.matches(MediaType.VIDEO, FormatConfiguration.JPG));
		assertFalse(webImage.matches(MediaType.VIDEO));
		assertTrue(webImage.matches(MediaType.IMAGE));
		assertFalse(webAudio.matches(MediaType.VIDEO));
		assertTrue(webAudio.matches(MediaType.AUDIO));
		assertTrue(webVideo.matches(MediaType.VIDEO) && webVideo.getFormat() instanceof WEB);
		assertFalse(webVideo.matches(MediaType.VIDEO, FormatConfiguration.MPEG1));
	}

	private static File getNonExistingFile(String initialname) {
		String basename = FileUtil.getFileNameWithoutExtension(initialname);
		String extension = FileUtil.getExtension(initialname);
		Random random = new Random();
		File result = new File(initialname);
		while (result.exists()) {
			result = new File(basename + random.nextInt() + "." + extension);
		}
		return result;
	}
}
