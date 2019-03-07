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
package net.pms.encoders;

import com.jgoodies.forms.builder.FormBuilder;
import com.jgoodies.forms.factories.Paddings;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import net.pms.Messages;
import net.pms.configuration.DeviceConfiguration;
import net.pms.configuration.FFmpegProgramInfo;
import net.pms.configuration.PmsConfiguration;
import net.pms.dlna.DLNAMediaInfo;
import net.pms.dlna.DLNAResource;
import net.pms.dlna.PartialSource;
import net.pms.formats.FormatType;
import net.pms.formats.WEB;
import net.pms.io.OutputParams;
import net.pms.io.ProcessWrapper;
import net.pms.io.ProcessWrapperImpl;
import net.pms.network.HTTPResource;
import net.pms.newgui.GuiUtil;

public class FFmpegAudio extends FFMpegVideo {
	public static final PlayerId ID = StandardPlayerId.FFMPEG_AUDIO;

	/** The {@link Configuration} key for the FFmpeg Audio executable type. */
	public static final String KEY_FFMPEG_AUDIO_EXECUTABLE_TYPE = "ffmpeg_audio_executable_type";
	public static final String NAME = "FFmpeg Audio";

	private JCheckBox noresample;

	// Not to be instantiated by anything but PlayerFactory
	FFmpegAudio() {
	}

	@Override
	public JComponent getConfigurationPanel() {
		FormLayout layout = new FormLayout(
			"left:pref, 0:grow",
			"p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, 0:grow"
		);
		FormBuilder builder = FormBuilder.create().layout(layout).border(Paddings.EMPTY).opaque(false);

		CellConstraints cc = new CellConstraints();

		noresample = new JCheckBox(Messages.getString("TrTab2.22"), configuration.isAudioResample());
		noresample.setContentAreaFilled(false);
		noresample.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				configuration.setAudioResample(e.getStateChange() == ItemEvent.SELECTED);
			}
		});
		builder.add(GuiUtil.getPreferredSizeComponent(noresample)).at(cc.xy(2, 3));

		return builder.getPanel();
	}

	@Override
	public int purpose() {
		return AUDIO_SIMPLEFILE_PLAYER;
	}

	@Override
	public PlayerId id() {
		return ID;
	}

	@Override
	public String getExecutableTypeKey() {
		return KEY_FFMPEG_AUDIO_EXECUTABLE_TYPE;
	}

	@Override
	public boolean isTimeSeekable() {
		return true;
	}

	@Override
	public boolean avisynth() {
		return false;
	}

	@Override
	public String name() {
		return NAME;
	}

	@Override
	public FormatType type() {
		return FormatType.AUDIO;
	}

	@Override
	@Deprecated
	public String[] args() {
		// unused: kept for backwards compatibility
		return new String[] {"-f", "s16be", "-ar", "48000"};
	}

	@Override
	public String mimeType() {
		return HTTPResource.AUDIO_TRANSCODE;
	}

	@Override
	public synchronized ProcessWrapper launchTranscode(
		DLNAResource dlna,
		DLNAMediaInfo media,
		OutputParams params
	) throws IOException {
		PmsConfiguration prev = configuration;
		// Use device-specific DMS conf
		configuration = (DeviceConfiguration)params.mediaRenderer;
		final String filename = dlna.getFileName();
		params.maxBufferSize = configuration.getMaxAudioBuffer();
		params.waitbeforestart = 2000;
		params.manageFastStart();

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

		List<String> cmdList = new ArrayList<>();

		cmdList.add(getExecutable());

		cmdList.add("-loglevel");
		cmdList.add(FFmpegProgramInfo.getFFmpegLogLevel());

		double start = Math.max(params.timeseek, 0.0);
		double end = params.timeend > 0 ? params.timeend : Double.POSITIVE_INFINITY;
		if (dlna instanceof PartialSource) {
			PartialSource partial = (PartialSource) dlna;
			if (partial.isPartialSource()) {
				start += partial.getClipStart();
				if (!Double.isInfinite(end)) {
					end += partial.getClipStart();
				}
				double clipEnd = partial.getClipEnd();
				if (clipEnd == 0.0) {
					clipEnd = Double.POSITIVE_INFINITY;
				}
				end = Math.min(end, clipEnd);
			}
		}

		if (start > 0.0) {
			cmdList.add("-ss");
			cmdList.add(String.valueOf(start));
		}

		if (!Double.isInfinite(end)) {
			double duration = end - start;
			cmdList.add("-t");
			cmdList.add(String.valueOf(duration));
		}

		// Decoder threads
		if (nThreads > 0) {
			cmdList.add("-threads");
			cmdList.add("" + nThreads);
		}

		cmdList.add("-i");
		cmdList.add(filename);

		// Make sure FFmpeg doesn't try to encode embedded images into the stream
		cmdList.add("-vn");
		cmdList.add("-dn");

		// Encoder threads
		if (nThreads > 0) {
			cmdList.add("-threads");
			cmdList.add("" + nThreads);
		}

		if (params.mediaRenderer.isTranscodeToMP3()) {
			cmdList.add("-f");
			cmdList.add("mp3");
			cmdList.add("-b:a");
			cmdList.add("320k");
			cmdList.add("-sample_fmt");
			cmdList.add("s16p");
		} else if (params.mediaRenderer.isTranscodeToWAV()) {
			cmdList.add("-f");
			cmdList.add("wav");
			cmdList.add("-c:a");
			cmdList.add("pcm_s16le");
		} else { // default: LPCM
			cmdList.add("-f");
			cmdList.add("s16be");
		}

		if (configuration.isAudioResample()) {
			if (params.mediaRenderer.isTranscodeAudioTo441()) {
				cmdList.add("-ar");
				cmdList.add("44100");
				cmdList.add("-ac");
				cmdList.add("2");
			} else {
				cmdList.add("-ar");
				cmdList.add("48000");
				cmdList.add("-ac");
				cmdList.add("2");
			}
		}

		cmdList.add("pipe:");

		String[] cmdArray = new String[ cmdList.size() ];
		cmdList.toArray(cmdArray);

		ProcessWrapperImpl pw = new ProcessWrapperImpl(cmdArray, params);
		pw.runInNewThread();

		configuration = prev;
		return pw;
	}

	@Override
	public boolean isCompatible(DLNAResource resource) {
		if (resource == null) {
			return false;
		}
		return resource.isAudio() && (isContainerCompatible(resource) || resource.getFormat() instanceof WEB);
	}
}
