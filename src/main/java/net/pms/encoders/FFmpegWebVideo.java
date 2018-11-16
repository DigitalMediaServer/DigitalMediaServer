/*
 * PS3 Media Server, for streaming any medias to your PS3.
 * Copyright (C) 2008-2012 A.Brochard
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
package net.pms.encoders;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JComponent;
import net.pms.configuration.DeviceConfiguration;
import net.pms.configuration.ExecutableInfo;
import net.pms.configuration.FFmpegExecutableInfo;
import net.pms.configuration.FFmpegExecutableInfo.ProtocolFlags;
import net.pms.configuration.FFmpegProgramInfo;
import net.pms.configuration.PmsConfiguration;
import net.pms.configuration.RendererConfiguration;
import net.pms.dlna.DLNAMediaInfo;
import net.pms.dlna.DLNAResource;
import net.pms.formats.WEB;
import net.pms.io.OutputParams;
import net.pms.io.OutputTextLogger;
import net.pms.io.PipeProcess;
import net.pms.io.ProcessWrapper;
import net.pms.io.ProcessWrapperImpl;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FFmpegWebVideo extends FFMpegVideo {
	private static final Logger LOGGER = LoggerFactory.getLogger(FFmpegWebVideo.class);
	public static final PlayerId ID = StandardPlayerId.FFMPEG_WEB_VIDEO;

	/** The {@link Configuration} key for the FFmpeg Audio executable type. */
	public static final String KEY_FFMPEG_WEB_EXECUTABLE_TYPE = "ffmpeg_web_executable_type";
	public static final String NAME = "FFmpeg Web Video";

	// Not to be instantiated by anything but PlayerFactory
	FFmpegWebVideo() {
	}

	@Override
	public JComponent getConfigurationPanel() {
		return null;
	}

	@Override
	public PlayerId id() {
		return ID;
	}

	@Override
	public String getExecutableTypeKey() {
		return KEY_FFMPEG_WEB_EXECUTABLE_TYPE;
	}

	@Override
	public int purpose() {
		return VIDEO_WEBSTREAM_PLAYER;
	}

	@Override
	public boolean isTimeSeekable() {
		return false;
	}

	@Override
	public synchronized ProcessWrapper launchTranscode(
		DLNAResource dlna,
		DLNAMediaInfo media,
		OutputParams params
	) throws IOException {
		params.minBufferSize = params.minFileSize;
		params.secondread_minsize = 100000;
		// Use device-specific DMS conf
		PmsConfiguration prev = configuration;
		configuration = (DeviceConfiguration) params.mediaRenderer;
		RendererConfiguration renderer = params.mediaRenderer;
		String filename = dlna.getFileName();
		setAudioAndSubs(filename, media, params);

		// Workaround an FFmpeg bug: http://ffmpeg.org/trac/ffmpeg/ticket/998
		// Also see static init
		if (filename.startsWith("mms:")) {
			filename = "mmsh:" + filename.substring(4);
		}

		FFmpegOptions customOptions = new FFmpegOptions();

		// Gather custom options from various sources in ascending priority:
		// - (http) header options
		if (params.header != null && params.header.length > 0) {
			String hdr = new String(params.header, StandardCharsets.ISO_8859_1);
			customOptions.addAll(parseOptions(hdr));
		}
		// - attached options
		String attached = (String) dlna.getAttachment(ID.toString());
		if (attached != null) {
			customOptions.addAll(parseOptions(attached));
		}
		// - renderer options
		String ffmpegOptions = renderer.getCustomFFmpegOptions();
		if (StringUtils.isNotEmpty(ffmpegOptions)) {
			customOptions.addAll(parseOptions(ffmpegOptions));
		}

		// Build the command line
		List<String> cmdList = new ArrayList<>();

		cmdList.add(getExecutable());

		// XXX squashed bug - without this, ffmpeg hangs waiting for a confirmation
		// that it can write to a file that already exists i.e. the named pipe
		cmdList.add("-y");

		cmdList.add("-loglevel");
		cmdList.add(FFmpegProgramInfo.getFFmpegLogLevel());

		/*
		 * FFmpeg uses multithreading by default, so provided that the
		 * user has not disabled FFmpeg multithreading and has not
		 * chosen to use more or less threads than are available, do not
		 * specify how many cores to use.
		 */
		int nThreads = 1;
		if (configuration.isFfmpegMultithreading()) {
			if (Runtime.getRuntime().availableProcessors() == configuration.getNumberOfCpuCores()) {
				nThreads = 0;
			} else {
				nThreads = configuration.getNumberOfCpuCores();
			}
		}

		// Decoder threads
		if (nThreads > 0) {
			cmdList.add("-threads");
			cmdList.add("" + nThreads);
		}

		// Add global and input-file custom options, if any
		if (!customOptions.isEmpty()) {
			customOptions.transferGlobals(cmdList);
			customOptions.transferInputFileOptions(cmdList);
		}

		if (params.timeseek > 0) {
			cmdList.add("-ss");
			cmdList.add("" + (int) params.timeseek);
		}

		cmdList.add("-i");
		cmdList.add(filename);

		cmdList.addAll(getVideoFilterOptions(dlna, media, params));

		// Encoder threads
		if (nThreads > 0) {
			cmdList.add("-threads");
			cmdList.add("" + nThreads);
		}

		// Add the output options (-f, -c:a, -c:v, etc.)

		// Now that inputs and filtering are complete, see if we should
		// give the renderer the final say on the command
		boolean override = false;
		if (renderer instanceof RendererConfiguration.OutputOverride) {
			override = ((RendererConfiguration.OutputOverride) renderer).getOutputOptions(cmdList, dlna, this, params);
		}

		if (!override) {
			cmdList.addAll(getVideoTranscodeOptions(dlna, media, params));

			// Add video bitrate options
			cmdList.addAll(getVideoBitrateOptions(dlna, media, params));

			// Add audio bitrate options
			cmdList.addAll(getAudioBitrateOptions(dlna, media, params));

			// Add any remaining custom options
			if (!customOptions.isEmpty()) {
				customOptions.transferAll(cmdList);
			}
		}

		// Set up the process

		// basename of the named pipe:
		String fifoName = String.format(
			"ffmpegwebvideo_%d_%d",
			Thread.currentThread().getId(),
			System.currentTimeMillis()
		);

		// This process wraps the command that creates the named pipe
		PipeProcess pipe = new PipeProcess(fifoName);
		pipe.deleteLater(); // delete the named pipe later; harmless if it isn't created
		ProcessWrapper mkfifo_process = pipe.getPipeProcess();

		/**
		 * It can take a long time for Windows to create a named pipe (and
		 * mkfifo can be slow if /tmp isn't memory-mapped), so run this in
		 * the current thread.
		 */
		mkfifo_process.runInSameThread();

		params.input_pipes[0] = pipe;

		// Output file
		cmdList.add(pipe.getInputPipe());

		// Convert the command list to an array
		String[] cmdArray = new String[cmdList.size()];
		cmdList.toArray(cmdArray);

		// Now launch FFmpeg
		ProcessWrapperImpl pw = new ProcessWrapperImpl(cmdArray, params);
		parseMediaInfo(filename, dlna, pw); // Better late than never
		pw.attachProcess(mkfifo_process); // Clean up the mkfifo process when the transcode ends

		// Give the mkfifo process a little time
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
			LOGGER.error("Thread interrupted while waiting for named pipe to be created", e);
		}

		// Launch the transcode command...
		pw.runInNewThread();
		// ...and wait briefly to allow it to start
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			LOGGER.error("Thread interrupted while waiting for transcode to start", e);
		}

		configuration = prev;
		return pw;
	}

	@Override
	public String name() {
		return NAME;
	}

	// TODO remove this when it's removed from Player
	@Deprecated
	@Override
	public String[] args() {
		return null;
	}

	@Override
	public boolean isCompatible(DLNAResource resource) {
		if (resource.isVideo() && resource.getFormat() instanceof WEB) {
			String url = resource.getFileName();

			ExecutableInfo executableInfo = programInfo.getExecutableInfo(currentExecutableType);
			if (executableInfo instanceof FFmpegExecutableInfo) {
				EnumSet<ProtocolFlags> flags = ((FFmpegExecutableInfo) executableInfo).getProtocols().get(url.split(":")[0]);
				if (flags == null || !flags.contains(ProtocolFlags.INPUT)) {
					return false;
				}
			} else {
				LOGGER.warn(
					"Couldn't check {} protocol compatibility for \"{}\", reporting as not compatible",
					getClass().getSimpleName(),
					url.split(":")[0]
				);
				return false;
			}
		}

		return false;
	}

	static final Matcher endOfHeader = Pattern.compile("Press \\[q\\]|A-V:|At least|Invalid").matcher("");

	/**
	 * Parse media info from ffmpeg headers during playback
	 */
	public void parseMediaInfo(String filename, final DLNAResource dlna, final ProcessWrapperImpl pw) {
		if (dlna.getMedia() == null) {
			dlna.setMedia(new DLNAMediaInfo());
		} else if (dlna.getMedia().isFFmpegparsed()) {
			return;
		}
		final ArrayList<String> lines = new ArrayList<>();
		final String input = filename.length() > 200 ? filename.substring(0, 199) : filename;
		OutputTextLogger ffParser = new OutputTextLogger(null) {
			@Override
			public boolean filter(String line) {
				if (endOfHeader.reset(line).find()) {
					dlna.getMedia().parseFFmpegInfo(lines, input);
					LOGGER.trace("[{}] parsed media from headers: {}", ID, dlna.getMedia());
					dlna.getParent().updateChild(dlna);
					return false; // done, stop filtering
				}
				lines.add(line);
				return true; // keep filtering
			}
		};
		ffParser.setFiltered(true);
		pw.setStderrConsumer(ffParser);
	}
}
