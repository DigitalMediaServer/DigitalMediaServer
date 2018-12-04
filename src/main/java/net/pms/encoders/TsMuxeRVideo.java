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

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import com.jgoodies.forms.builder.FormBuilder;
import com.jgoodies.forms.factories.Paddings;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.sun.jna.Platform;
import java.awt.ComponentOrientation;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import net.pms.Messages;
import net.pms.PMS;
import net.pms.configuration.DeviceConfiguration;
import net.pms.configuration.ExecutableInfo;
import net.pms.configuration.ExecutableInfo.ExecutableInfoBuilder;
import net.pms.configuration.ExternalProgramInfo;
import net.pms.configuration.FFmpegExecutableInfo;
import net.pms.configuration.FFmpegExecutableInfo.Codec;
import net.pms.configuration.FFmpegExecutableInfo.CoderFlags;
import net.pms.configuration.FormatConfiguration;
import net.pms.configuration.PmsConfiguration;
import net.pms.configuration.RendererConfiguration;
import net.pms.dlna.*;
import net.pms.formats.Format;
import net.pms.formats.FormatType;
import net.pms.formats.v2.SubtitleType;
import net.pms.io.*;
import net.pms.media.VideoLevel;
import net.pms.newgui.GuiUtil;
import net.pms.platform.windows.NTStatus;
import net.pms.util.CodecUtil;
import net.pms.util.FormLayoutUtil;
import net.pms.util.Rational;
import net.pms.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TsMuxeRVideo extends Player {
	private static final Logger LOGGER = LoggerFactory.getLogger(TsMuxeRVideo.class);
	public static final PlayerId ID = StandardPlayerId.TSMUXER_VIDEO;

	/** The {@link Configuration} key for the custom tsMuxeR path. */
	public static final String KEY_TSMUXER_PATH     = "tsmuxer_path";

	/** The {@link Configuration} key for the tsMuxeR executable type. */
	public static final String KEY_TSMUXER_EXECUTABLE_TYPE = "tsmuxer_executable_type";
	public static final String NAME = "tsMuxeR Video";

	private static final String COL_SPEC = "left:pref, 0:grow";
	private static final String ROW_SPEC = "p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, 0:grow";

	// Not to be instantiated by anything but PlayerFactory
	TsMuxeRVideo() {
	}

	@Override
	public boolean excludeFormat(Format format) {
		String extension = format.getMatchedExtension();
		return extension != null
			&& !extension.equals("mp4")
			&& !extension.equals("mkv")
			&& !extension.equals("ts")
			&& !extension.equals("tp")
			&& !extension.equals("m2ts")
			&& !extension.equals("m2t")
			&& !extension.equals("mpg")
			&& !extension.equals("evo")
			&& !extension.equals("mpeg")
			&& !extension.equals("vob")
			&& !extension.equals("m2v")
			&& !extension.equals("mts")
			&& !extension.equals("mov")
			&& !extension.equals("srt")
			&& !extension.equals("sup");
	}

	@Override
	public int purpose() {
		return VIDEO_SIMPLEFILE_PLAYER;
	}

	@Override
	public PlayerId id() {
		return ID;
	}

	@Override
	public String getConfigurablePathKey() {
		return KEY_TSMUXER_PATH;
	}

	@Override
	public String getExecutableTypeKey() {
		return KEY_TSMUXER_EXECUTABLE_TYPE;
	}

	@Override
	public boolean isTimeSeekable() {
		return true;
	}

	@Override
	public String[] args() {
		return null;
	}

	@Override
	protected ExternalProgramInfo programInfo() {
		return configuration.getTsMuxeRPaths();
	}

	@Override
	public ProcessWrapper launchTranscode(
		DLNAResource dlna,
		DLNAMediaInfo media,
		OutputParams params
	) throws IOException {
		// Use device-specific DMS conf
		PmsConfiguration prev = configuration;
		configuration = (DeviceConfiguration) params.mediaRenderer;
		final String filename = dlna.getFileName();
		setAudioAndSubs(filename, media, params);

		PipeIPCProcess ffVideoPipe;
		ProcessWrapperImpl ffVideo;

		PipeIPCProcess ffAudioPipe[] = null;
		ProcessWrapperImpl ffAudio[] = null;

		String fps = media.getValidFps(false);

		String videoType = "V_MPEG4/ISO/AVC";
		if (FormatConfiguration.MPEG2.equals(media.getCodecV())) {
			videoType = "V_MPEG-2";
		} else if (FormatConfiguration.H265.equals(media.getCodecV())) {
			videoType = "V_MPEGH/ISO/HEVC";
		} else if (FormatConfiguration.VC1.equals(media.getCodecV())) {
			videoType = "V_MS/VFW/WVC1";
		}

		boolean aacTranscode = false;

		String[] ffmpegCommands;
		if (this instanceof TsMuxeRAudio && media.getFirstAudioTrack() != null) {
			ffVideoPipe = new PipeIPCProcess(System.currentTimeMillis() + "fakevideo", System.currentTimeMillis() + "videoout", false, true);

			String timeEndValue1 = "-t";
			String timeEndValue2 = "" + params.timeend;
			if (params.timeend < 1) {
				timeEndValue1 = "-y";
				timeEndValue2 = "-y";
			}

			ffmpegCommands = new String[] {
				PlayerFactory.getPlayerExecutable(StandardPlayerId.FFMPEG_VIDEO),
				timeEndValue1, timeEndValue2,
				"-loop", "1",
				"-i", "DummyInput.jpg",
				"-f", "h264",
				"-c:v", "libx264",
				"-level", "31",
				"-tune", "zerolatency",
				"-pix_fmt", "yuv420p",
				"-an",
				"-y",
				ffVideoPipe.getInputPipe()
			};

			OutputParams ffparams = new OutputParams(configuration);
			ffparams.maxBufferSize = 1;
			ffVideo = new ProcessWrapperImpl(ffmpegCommands, ffparams);

			if (
				filename.toLowerCase().endsWith(".flac") &&
				media.getFirstAudioTrack().getBitsPerSample() >= 24 &&
				media.getFirstAudioTrack().getSampleFrequency() % 48000 == 0
			) {
				ffAudioPipe = new PipeIPCProcess[1];
				ffAudioPipe[0] = new PipeIPCProcess(System.currentTimeMillis() + "flacaudio", System.currentTimeMillis() + "audioout", false, true);

				String[] flacCmd = new String[] {
					configuration.getFLACPath(),
					"--output-name=" + ffAudioPipe[0].getInputPipe(),
					"-d",
					"-f",
					"-F",
					filename
				};

				ffparams = new OutputParams(configuration);
				ffparams.maxBufferSize = 1;
				ffAudio = new ProcessWrapperImpl[1];
				ffAudio[0] = new ProcessWrapperImpl(flacCmd, ffparams);
			} else {
				ffAudioPipe = new PipeIPCProcess[1];
				ffAudioPipe[0] = new PipeIPCProcess(System.currentTimeMillis() + "mlpaudio", System.currentTimeMillis() + "audioout", false, true);
				String depth = "pcm_s16le";
				String rate = "48000";

				if (media.getFirstAudioTrack().getBitsPerSample() >= 24) {
					depth = "pcm_s24le";
				}

				if (media.getFirstAudioTrack().getSampleFrequency() > 48000) {
					rate = "" + media.getFirstAudioTrack().getSampleFrequency();
				}

				String[] flacCmd = new String[] {
					PlayerFactory.getPlayerExecutable(StandardPlayerId.FFMPEG_VIDEO),
					"-i", filename,
					"-vn",
					"-dn",
					"-ar", rate,
					"-f", "wav",
					"-acodec", depth,
					"-y",
					ffAudioPipe[0].getInputPipe()
				};

				ffparams = new OutputParams(configuration);
				ffparams.maxBufferSize = 1;
				ffAudio = new ProcessWrapperImpl[1];
				ffAudio[0] = new ProcessWrapperImpl(flacCmd, ffparams);
			}
		} else {
			params.waitbeforestart = 5000;
			params.manageFastStart();

			ffVideoPipe = new PipeIPCProcess(System.currentTimeMillis() + "ffmpegvideo", System.currentTimeMillis() + "videoout", false, true);

			ffmpegCommands = new String[] {
				PlayerFactory.getPlayerExecutable(StandardPlayerId.FFMPEG_VIDEO),
				"-ss", params.timeseek > 0 ? "" + params.timeseek : "0",
				"-i", filename,
				"-an",
				"-dn",
				"-c:v", "copy",
				"-f", media.getCodecV().startsWith("h264") ? "h264" : "mpeg2video",
				"-y",
				ffVideoPipe.getInputPipe()
			};

			InputFile newInput = new InputFile();
			newInput.setFilename(filename);
			newInput.setPush(params.stdin);

			// The code below is commented out until it can be fully understood what it's intended to do.
			// It seems to cause broken pipes as it is, and the Annex B header isn't usually parsed.

//			if (media.getH264AnnexB() != null && media.getH264AnnexB().length > 0) {
//				StreamModifier sm = new StreamModifier();
//				sm.setHeader(media.getH264AnnexB());
//				sm.setH264AnnexB(true);
//				ffVideoPipe.setModifier(sm);
//			}

			OutputParams ffparams = new OutputParams(configuration);
			ffparams.maxBufferSize = 1;
			ffparams.stdin = params.stdin;
			ffVideo = new ProcessWrapperImpl(ffmpegCommands, ffparams);

			int numAudioTracks = 1;

			if (media.getAudioTracksList() != null && media.getAudioTracksList().size() > 1 && configuration.isMuxAllAudioTracks()) {
				numAudioTracks = media.getAudioTracksList().size();
			}

			boolean singleMediaAudio = media.getAudioTracksList().size() <= 1;

			if (params.aid != null) {
				boolean ac3Remux;
				boolean dtsRemux;
				boolean encodedAudioPassthrough;
				boolean pcm;

				if (numAudioTracks <= 1) {
					ffAudioPipe = new PipeIPCProcess[numAudioTracks];
					ffAudioPipe[0] = new PipeIPCProcess(System.currentTimeMillis() + "ffmpegaudio01", System.currentTimeMillis() + "audioout", false, true);

					encodedAudioPassthrough = configuration.isEncodedAudioPassthrough() && params.aid.isNonPCMEncodedAudio() && params.mediaRenderer.isWrapEncodedAudioIntoPCM();
					ac3Remux = params.aid.isAC3() && configuration.isAudioRemuxAC3() && !encodedAudioPassthrough && !params.mediaRenderer.isTranscodeToAAC();
					dtsRemux = configuration.isAudioEmbedDtsInPcm() && (params.aid.isDTS()  || params.aid.isDTSHD()) && params.mediaRenderer.isDTSPlayable() && !encodedAudioPassthrough;

					pcm = configuration.isAudioUsePCM() &&
						media.isValidForLPCMTranscoding() &&
						(
							params.aid.isLossless() ||
							(params.aid.isDTS() && params.aid.getNumberOfChannels() <= 6) ||
							params.aid.isTrueHD() ||
							(
								!configuration.isMencoderUsePcmForHQAudioOnly() &&
								(
									params.aid.isAC3() ||
									params.aid.isMP3() ||
									params.aid.isAAC() ||
									params.aid.isVorbis() ||
									// params.aid.isWMA() ||
									params.aid.isMpegAudio()
								)
							)
						) && params.mediaRenderer.isLPCMPlayable();

					int channels;
					if (ac3Remux) {
						channels = params.aid.getNumberOfChannels(); // AC-3 remux
					} else if (dtsRemux || encodedAudioPassthrough) {
						channels = params.aid.getNumberOfChannels();
					} else if (pcm) {
						channels = params.aid.getNumberOfChannels();
					} else {
						channels = configuration.getAudioChannelCount(); // 5.1 max for AC-3 encoding
					}

					if (!ac3Remux && (dtsRemux || pcm || encodedAudioPassthrough)) {
						// DTS remux or LPCM
						StreamModifier sm = new StreamModifier();
						sm.setPcm(pcm);
						sm.setDtsEmbed(dtsRemux);
						sm.setEncodedAudioPassthrough(encodedAudioPassthrough);
						sm.setNbChannels(channels);
						sm.setSampleFrequency(params.aid.getSampleFrequency() < 48000 ? 48000 : params.aid.getSampleFrequency());
						sm.setBitsPerSample(16);

						ffmpegCommands = new String[] {
							PlayerFactory.getPlayerExecutable(StandardPlayerId.FFMPEG_VIDEO),
							"-ss", params.timeseek > 0 ? "" + params.timeseek : "0",
							"-i", filename,
							"-vn",
							"-dn",
							sm.isDtsEmbed() || sm.isEncodedAudioPassthrough() ? "" : "-ac",
							sm.isDtsEmbed() || sm.isEncodedAudioPassthrough() ? "" : "" + sm.getNbChannels(),
							"-f", "s16le",
							"-c:a", sm.isDtsEmbed() || sm.isEncodedAudioPassthrough() ? "copy" : "pcm_s16le",
							"-y",
							ffAudioPipe[0].getInputPipe()
						};

						// Use PCM trick when media renderer does not support DTS in MPEG
						if (!params.mediaRenderer.isMuxDTSToMpeg()) {
							ffAudioPipe[0].setModifier(sm);
						}
					} else if (!ac3Remux && params.mediaRenderer.isTranscodeToAAC()) {
						// AAC audio
						ffmpegCommands = new String[] {
							PlayerFactory.getPlayerExecutable(StandardPlayerId.FFMPEG_VIDEO),
							"-ss", params.timeseek > 0 ? "" + params.timeseek : "0",
							"-i", filename,
							"-vn",
							"-dn",
							"-ac", "" + channels,
							"-f", "adts",
							"-c:a", "aac",
							"-b:a", Math.min(configuration.getAudioBitrate(), 320) + "k",
							"-y",
							ffAudioPipe[0].getInputPipe()
						};
						aacTranscode = true;
					} else {
						// AC-3 audio
						ffmpegCommands = new String[] {
							PlayerFactory.getPlayerExecutable(StandardPlayerId.FFMPEG_VIDEO),
							"-ss", params.timeseek > 0 ? "" + params.timeseek : "0",
							"-i", filename,
							"-vn",
							"-dn",
							ac3Remux ? "" : "-ac",
							ac3Remux ? "" : "" + channels,
							"-f", "ac3",
							"-c:a", (ac3Remux) ? "copy" : "ac3",
							ac3Remux ? "" : "-b:a",
							ac3Remux ? "" : String.valueOf(CodecUtil.getAC3Bitrate(configuration, params.aid)) + "k",
							"-y",
							ffAudioPipe[0].getInputPipe()
						};
					}

					ffparams = new OutputParams(configuration);
					ffparams.maxBufferSize = 1;
					ffparams.stdin = params.stdin;
					ffAudio = new ProcessWrapperImpl[numAudioTracks];
					ffAudio[0] = new ProcessWrapperImpl(ffmpegCommands, ffparams);
				} else {
					ffAudioPipe = new PipeIPCProcess[numAudioTracks];
					ffAudio = new ProcessWrapperImpl[numAudioTracks];
					for (int i = 0; i < media.getAudioTracksList().size(); i++) {
						DLNAMediaAudio audio = media.getAudioTracksList().get(i);
						ffAudioPipe[i] = new PipeIPCProcess(System.currentTimeMillis() + "ffmpeg" + i, System.currentTimeMillis() + "audioout" + i, false, true);

						encodedAudioPassthrough = configuration.isEncodedAudioPassthrough() && params.aid.isNonPCMEncodedAudio() && params.mediaRenderer.isWrapEncodedAudioIntoPCM();
						ac3Remux = audio.isAC3() && configuration.isAudioRemuxAC3() && !encodedAudioPassthrough && !params.mediaRenderer.isTranscodeToAAC();
						dtsRemux = configuration.isAudioEmbedDtsInPcm() && (audio.isDTS() || audio.isDTSHD()) && params.mediaRenderer.isDTSPlayable() && !encodedAudioPassthrough;

						pcm = configuration.isAudioUsePCM() &&
							media.isValidForLPCMTranscoding() &&
							(
								audio.isLossless() ||
								(audio.isDTS() && audio.getNumberOfChannels() <= 6) ||
								audio.isTrueHD() ||
								(
									!configuration.isMencoderUsePcmForHQAudioOnly() &&
									(
										audio.isAC3() ||
										audio.isMP3() ||
										audio.isAAC() ||
										audio.isVorbis() ||
										// audio.isWMA() ||
										audio.isMpegAudio()
									)
								)
							) && params.mediaRenderer.isLPCMPlayable();

						int channels;
						if (ac3Remux) {
							channels = audio.getNumberOfChannels(); // AC-3 remux
						} else if (dtsRemux || encodedAudioPassthrough) {
							channels = params.aid.getNumberOfChannels();
						} else if (pcm) {
							channels = audio.getNumberOfChannels();
						} else {
							channels = configuration.getAudioChannelCount(); // 5.1 max for AC-3 encoding
						}

						if (!ac3Remux && (dtsRemux || pcm || encodedAudioPassthrough)) {
							// DTS remux or LPCM
							StreamModifier sm = new StreamModifier();
							sm.setPcm(pcm);
							sm.setDtsEmbed(dtsRemux);
							sm.setEncodedAudioPassthrough(encodedAudioPassthrough);
							sm.setNbChannels(channels);
							sm.setSampleFrequency(audio.getSampleFrequency() < 48000 ? 48000 : audio.getSampleFrequency());
							sm.setBitsPerSample(16);
							if (!params.mediaRenderer.isMuxDTSToMpeg()) {
								ffAudioPipe[i].setModifier(sm);
							}

							ffmpegCommands = new String[] {
								PlayerFactory.getPlayerExecutable(StandardPlayerId.FFMPEG_VIDEO),
								"-ss", params.timeseek > 0 ? "" + params.timeseek : "0",
								"-i", filename,
								"-vn",
								"-dn",
								sm.isDtsEmbed() || sm.isEncodedAudioPassthrough() ? "" : "-ac",
								sm.isDtsEmbed() || sm.isEncodedAudioPassthrough() ? "" : "" + sm.getNbChannels(),
								"-f", "s16le",
								singleMediaAudio ? "-y" : "-map", singleMediaAudio ? "-y" : ("0:a:" + (media.getAudioTracksList().indexOf(audio))),
								"-c:a", sm.isDtsEmbed() || sm.isEncodedAudioPassthrough() ? "copy" : "pcm_s16le",
								"-y",
								ffAudioPipe[i].getInputPipe()
							};
						} else if (!ac3Remux && params.mediaRenderer.isTranscodeToAAC()) {
							// AAC audio
							ArrayList<String> tempFFmpegCommands = new ArrayList<String>();
							Player ffmpeg = PlayerFactory.getPlayer(StandardPlayerId.FFMPEG_VIDEO, false, false);
							tempFFmpegCommands.add(ffmpeg.getExecutable());
							tempFFmpegCommands.add("-ss");
							tempFFmpegCommands.add(params.timeseek > 0 ? Double.toString(params.timeseek) : "0");
							tempFFmpegCommands.add("-i");
							tempFFmpegCommands.add(filename);
							tempFFmpegCommands.add("-ac");
							tempFFmpegCommands.add(Integer.toString(channels));
							tempFFmpegCommands.add("-f");
							tempFFmpegCommands.add("adts");
							if (!singleMediaAudio) {
								tempFFmpegCommands.add("-map");
								tempFFmpegCommands.add("0:a:" + media.getAudioTracksList().indexOf(audio));
							}
							tempFFmpegCommands.add("-c:a");
							tempFFmpegCommands.add("aac");
							ExecutableInfo executableInfo = ffmpeg.getExecutableInfo();
							if (executableInfo instanceof FFmpegExecutableInfo) {
								/*
								 * Check if the experimental flag is required for the "aac"
								 * encoder. This can also be determined by checking the
								 * libavcodec version (return true == experimental):
								 *
								 * if (isLibraryFFmpeg(libavcodecVersion)) {
								 *     // FFMpeg made it non-experimental in version libavcodec 57.16.101 (d9791a8656b5580756d5b7ecc315057e8cd4255e)
								 *     return libavcodecVersion.isLessThanOrEqualTo(new Version(57, 16, 101, 0));
								 * } // Libav has yet to make it non-experimental
								 * return true;
								 */
								Codec aac = ((FFmpegExecutableInfo) executableInfo).getCodecs().get("aac");
								if (aac.containsEncoderFlag(CoderFlags.EXPERIMENTAL)) {
									tempFFmpegCommands.add("-strict");
									tempFFmpegCommands.add("experimental");
								}
							}
							tempFFmpegCommands.add("-b:a");
							tempFFmpegCommands.add(Math.min(configuration.getAudioBitrate(), 320) + "k");
							tempFFmpegCommands.add("-y");
							tempFFmpegCommands.add(ffAudioPipe[i].getInputPipe());
							ffmpegCommands = tempFFmpegCommands.toArray(new String[tempFFmpegCommands.size()]);
							aacTranscode = true;
						} else {
							// AC-3 remux or encoding
							ffmpegCommands = new String[] {
								PlayerFactory.getPlayerExecutable(StandardPlayerId.FFMPEG_VIDEO),
								"-ss", params.timeseek > 0 ? "" + params.timeseek : "0",
								"-i", filename,
								"-vn",
								"-dn",
								ac3Remux ? "" : "-ac",
								ac3Remux ? "" : "" + channels,
								"-f", "ac3",
								singleMediaAudio ? "-y" : "-map", singleMediaAudio ? "-y" : ("0:a:" + (media.getAudioTracksList().indexOf(audio))),
								"-c:a", (ac3Remux) ? "copy" : "ac3",
								ac3Remux ? "" : "-b:a",
								ac3Remux ? "" : String.valueOf(CodecUtil.getAC3Bitrate(configuration, audio)) + "k",
								"-y",
								ffAudioPipe[i].getInputPipe()
							};
						}

						ffparams = new OutputParams(configuration);
						ffparams.maxBufferSize = 1;
						ffparams.stdin = params.stdin;
						ffAudio[i] = new ProcessWrapperImpl(ffmpegCommands, ffparams);
					}
				}
			}
		}

		File f = new File(configuration.getTempFolder(), "dms-tsmuxer.meta");
		params.log = false;
		try (PrintWriter pw = new PrintWriter(f)) {
			pw.println("MUXOPT --no-pcr-on-video-pid --new-audio-pes --vbr --vbv-len=500");

			String sei = "insertSEI";
			if (
				params.mediaRenderer.isPS3() &&
				media.isWebDl(filename, params)
			) {
				sei = "forceSEI";
			}
			String videoparams = sei + ", contSPS, track=" + "1"; //params.vid.getId()
			if (this instanceof TsMuxeRAudio) {
				videoparams = "track=224";
			}
			if (configuration.isFix25FPSAvMismatch()) {
				fps = "25";
			}
			pw.println(videoType + ", \"" + dlna.getFileName() + "\", " + (fps != null ? ("fps=" + fps + ", ") : "") + "video-width=" + media.getWidth() + ", video-height=" + media.getHeight() + ", " + videoparams);

			if (ffAudioPipe != null || params.aid != null) {
				if (ffAudioPipe.length == 1 || media.getAudioTracksList().size() < 2) {
					String timeshift = "";
					boolean ac3Remux;
					boolean dtsRemux;
					boolean encodedAudioPassthrough;
					boolean pcm;

					encodedAudioPassthrough = configuration.isEncodedAudioPassthrough() && params.aid.isNonPCMEncodedAudio() && params.mediaRenderer.isWrapEncodedAudioIntoPCM();
					ac3Remux = params.aid.isAC3() && configuration.isAudioRemuxAC3() && !encodedAudioPassthrough && !params.mediaRenderer.isTranscodeToAAC();
					dtsRemux = configuration.isAudioEmbedDtsInPcm() && (params.aid.isDTS()  || params.aid.isDTSHD()) && params.mediaRenderer.isDTSPlayable() && !encodedAudioPassthrough;
					pcm = configuration.isAudioUsePCM() &&
						media.isValidForLPCMTranscoding() &&
						(
							params.aid.isLossless() ||
							(params.aid.isDTS() && params.aid.getAudioProperties().getNumberOfChannels() <= 6) ||
							params.aid.isTrueHD() ||
							(
								!configuration.isMencoderUsePcmForHQAudioOnly() &&
								(
									params.aid.isAC3() ||
									params.aid.isMP3() ||
									params.aid.isAAC() ||
									params.aid.isVorbis() ||
									// params.aid.isWMA() ||
									params.aid.isMpegAudio()
								)
							)
						) && params.mediaRenderer.isLPCMPlayable();

					String audioType = "";
					if (ac3Remux) {
						// AC-3 remux takes priority
						audioType = "A_AC3";
					} else if (
						aacTranscode || //TODO: aacRemux
						params.mediaRenderer.isTranscodeToAAC() &&
						(
							params.aid.isAACLC() ||
							params.aid.isHEAAC()
						)
					) {
						audioType = "A_AAC";
					} else if (pcm || encodedAudioPassthrough || this instanceof TsMuxeRAudio) {
						audioType = "A_LPCM";
					} else if (dtsRemux) {
						audioType = "A_LPCM";
						if (params.mediaRenderer.isMuxDTSToMpeg()) {
							audioType = "A_DTS";
						}
					}

					if (
						params.aid != null &&
						params.aid.getAudioProperties().getAudioDelay() > 0 &&
						params.timeseek == 0
					) {
						timeshift = "timeshift=" + params.aid.getAudioProperties().getAudioDelay() + "ms, ";
					}
					if (audioType != "") {
						int audioTrack = 2;
						if (params.aid != null && params.aid.getId() == 0 && media.getAudioTracksList() != null) {
							audioTrack = media.getAudioTracksList().size() + 1;
						} else if (params.aid != null) {
							audioTrack = params.aid.getId();
						}
						pw.println(audioType + ", \"" + dlna.getFileName() + "\", " + timeshift + "track=" + audioTrack);
					}
				} else {
					for (int i = 0; i < media.getAudioTracksList().size(); i++) {
						DLNAMediaAudio lang = media.getAudioTracksList().get(i);
						String timeshift = "";
						boolean ac3Remux;
						boolean dtsRemux;
						boolean encodedAudioPassthrough;
						boolean pcm;

						encodedAudioPassthrough = configuration.isEncodedAudioPassthrough() && params.aid.isNonPCMEncodedAudio() && params.mediaRenderer.isWrapEncodedAudioIntoPCM();
						ac3Remux = params.aid.isAC3() && configuration.isAudioRemuxAC3() && !encodedAudioPassthrough;
						dtsRemux = configuration.isAudioEmbedDtsInPcm() && (params.aid.isDTS() || params.aid.isDTSHD()) && params.mediaRenderer.isDTSPlayable() && !encodedAudioPassthrough;

						pcm = configuration.isAudioUsePCM() &&
							media.isValidForLPCMTranscoding() &&
							(
								lang.isLossless() ||
								(lang.isDTS() && lang.getAudioProperties().getNumberOfChannels() <= 6) ||
								lang.isTrueHD() ||
								(
									!configuration.isMencoderUsePcmForHQAudioOnly() &&
									(
										params.aid.isAC3() ||
										params.aid.isMP3() ||
										params.aid.isAAC() ||
										params.aid.isVorbis() ||
										// params.aid.isWMA() ||
										params.aid.isMpegAudio()
									)
								)
							) && params.mediaRenderer.isLPCMPlayable();
						String audioType = "A_AC3";
						if (ac3Remux) {
							// AC-3 remux takes priority
							audioType = "A_AC3";
						} else if (
							aacTranscode || //TODO: aacRemux
							params.mediaRenderer.isTranscodeToAAC() &&
							(
								params.aid.isAACLC() ||
								params.aid.isHEAAC()
							)
						) {
							audioType = "A_AAC";
						} else {
							if (pcm) {
								audioType = "A_LPCM";
							}
							if (encodedAudioPassthrough) {
								audioType = "A_LPCM";
							}
							if (dtsRemux) {
								audioType = "A_LPCM";
								if (params.mediaRenderer.isMuxDTSToMpeg()) {
									audioType = "A_DTS";
								}
							}
						}
						if (lang.getAudioProperties().getAudioDelay() != 0 && params.timeseek == 0) {
							timeshift = "timeshift=" + lang.getAudioProperties().getAudioDelay() + "ms, ";
						}
						pw.println(audioType + ", \"" + ffAudioPipe[i].getOutputPipe() + "\", " + timeshift + "track=" + (2 + i));
					}
				}
			}

			DLNAMediaSubtitle subtitle = dlna.getMediaSubtitle();
			if (
				!configuration.isDisableSubtitles() &&
				params.sid != null &&
				(
					params.sid.getType() == SubtitleType.SUBRIP ||
					params.sid.getType() == SubtitleType.PGS
				)
			) {
				String subtitleType = "S_TEXT/UTF8";
				if (params.sid.getType() == SubtitleType.PGS) {
					subtitleType = "S_HDMV/PGS";
				}
				String fontName = "";
				if (isNotBlank(configuration.getFont())) {
					fontName = CodecUtil.isFontRegisteredInOS(configuration.getFont());
				}
				String subtitlePath = "";
				if (params.sid.isExternal()) {
					subtitlePath = params.sid.getExternalFile().getAbsolutePath();
				} else if (params.sid.isEmbedded()) {
					subtitlePath = dlna.getFileName();
				}
				int fontSize = 15 * Integer.parseInt(configuration.getAssScale());
				int subtitleBottomOffset = 0;
				if (media.getAspectRatioVideoTrack() != null) {
					if (media.getAspectRatioVideoTrack().compareTo(Rational.valueOf(51, 20)) >= 0) {
						subtitleBottomOffset = 172;
					} else if (media.getAspectRatioVideoTrack().compareTo(Rational.valueOf(12, 5)) >= 0) {
						subtitleBottomOffset = 154;
					} else if (media.getAspectRatioVideoTrack().compareTo(Rational.valueOf(37, 20)) >= 0) {
						subtitleBottomOffset = 48;
					} else if (media.getAspectRatioVideoTrack().compareTo(Rational.valueOf(4, 3)) >= 0) {
						subtitleBottomOffset = 36;
					} else {
						subtitleBottomOffset = Integer.valueOf(configuration.getAssMargin());
					}
				}
				String subColor = configuration.getSubsColor().getASSv4StylesHexValue().replaceFirst("&H", "0x");
				pw.println(subtitleType + ", \"" + subtitlePath + "\", " + (subtitleType == "S_TEXT/UTF8" ? ((fontName != null ? ("font-name=\"" + fontName + "\", ") : "") + (fontSize > 0 ? ("font-size=" + fontSize + ", ") : "") + (subColor != null ? ("font-color=" + subColor + ", ") : "") + (subtitleBottomOffset > 0 ? ("bottom-offset=" + subtitleBottomOffset + ", ") : "") + (configuration.getAssOutline() != null ? ("font-border=" + configuration.getAssOutline() + ", ") : "") + "text-align=center, ") : "")  + "fps=" + fps + ", video-width=" + media.getWidth() + ", video-height=" + media.getHeight() + ", track=" + params.sid.getId() + (subtitle.getLang() != null ? (", lang=" + subtitle.getLang()) : "")); //", mplsFile=00000"
			}
		}

		PipeProcess tsPipe = new PipeProcess(System.currentTimeMillis() + "tsmuxerout.ts");

		String[] cmdArray = new String[]{
			getExecutable(),
			f.getAbsolutePath(),
			tsPipe.getInputPipe()
		};

		ProcessWrapperImpl p = new ProcessWrapperImpl(cmdArray, params);
		params.maxBufferSize = 100;
		params.input_pipes[0] = tsPipe;
		params.stdin = null;
		ProcessWrapper pipe_process = tsPipe.getPipeProcess();
		p.attachProcess(pipe_process);
		pipe_process.runInNewThread();

		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
		}
		tsPipe.deleteLater();

		ProcessWrapper ff_pipe_process = ffVideoPipe.getPipeProcess();
		p.attachProcess(ff_pipe_process);
		ff_pipe_process.runInNewThread();
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
		}
		ffVideoPipe.deleteLater();

		p.attachProcess(ffVideo);
		ffVideo.runInNewThread();
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
		}

		if (ffAudioPipe != null && params.aid != null) {
			for (int i = 0; i < ffAudioPipe.length; i++) {
				ff_pipe_process = ffAudioPipe[i].getPipeProcess();
				p.attachProcess(ff_pipe_process);
				ff_pipe_process.runInNewThread();
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
				}
				ffAudioPipe[i].deleteLater();
				p.attachProcess(ffAudio[i]);
				ffAudio[i].runInNewThread();
			}
		}

		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
		}

		p.runInNewThread();
		configuration = prev;
		return p;
	}

	@Override
	public String mimeType() {
		return "video/mpeg";
	}

	@Override
	public String name() {
		return NAME;
	}

	@Override
	public FormatType type() {
		return FormatType.VIDEO;
	}
	private JCheckBox tsmuxerforcefps;
	private JCheckBox muxallaudiotracks;

	@Override
	public JComponent getConfigurationPanel() {
		// Apply the orientation for the locale
		ComponentOrientation orientation = ComponentOrientation.getOrientation(PMS.getLocale());
		String colSpec = FormLayoutUtil.getColSpec(COL_SPEC, orientation);
		FormLayout layout = new FormLayout(colSpec, ROW_SPEC);
		FormBuilder builder = FormBuilder.create().layout(layout).border(Paddings.EMPTY).opaque(false);

		CellConstraints cc = new CellConstraints();

		tsmuxerforcefps = new JCheckBox(Messages.getString("TsMuxeRVideo.2"), configuration.isTsmuxerForceFps());
		tsmuxerforcefps.setContentAreaFilled(false);
		tsmuxerforcefps.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				configuration.setTsmuxerForceFps(e.getStateChange() == ItemEvent.SELECTED);
			}
		});
		builder.add(GuiUtil.getPreferredSizeComponent(tsmuxerforcefps)).at(FormLayoutUtil.flip(cc.xy(2, 3), colSpec, orientation));

		muxallaudiotracks = new JCheckBox(Messages.getString("TsMuxeRVideo.19"), configuration.isMuxAllAudioTracks());
		muxallaudiotracks.setContentAreaFilled(false);
		muxallaudiotracks.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				configuration.setMuxAllAudioTracks(e.getStateChange() == ItemEvent.SELECTED);
			}
		});
		builder.add(GuiUtil.getPreferredSizeComponent(muxallaudiotracks)).at(FormLayoutUtil.flip(cc.xy(2, 5), colSpec, orientation));

		JPanel panel = builder.getPanel();

		// Apply the orientation to the panel and all components in it
		panel.applyComponentOrientation(orientation);

		return panel;
	}

	@Override
	public boolean isInternalSubtitlesSupported() {
		return false;
	}

	@Override
	public boolean isExternalSubtitlesSupported() {
		return false;
	}

	@Override
	public boolean isPlayerCompatible(RendererConfiguration mediaRenderer) {
		return mediaRenderer != null && (mediaRenderer.isMuxH264MpegTS() || mediaRenderer.isMuxH265MpegTS());
	}

	@Override
	public boolean isCompatible(DLNAResource resource) {
		DLNAMediaSubtitle subtitle = resource.getMediaSubtitle();
		DLNAMediaInfo media = resource.getMedia();
		DLNAMediaInfo mediaInfo = new DLNAMediaInfo();
//		VideoLevel videoLevelLimit = params.mediaRenderer.getVideoLevelLimit(mediaInfo.getVideoCodec());
//		VideoLevel videoLevel = mediaInfo.getVideoLevel();
		int width  = mediaInfo.getWidth();
		int height = mediaInfo.getHeight();
		if (width < 320 || height < 240) {
			return false;
		}

//		if (
//			videoLevelLimit != null &&
//			!videoLevelLimit.isGreaterThanOrEqualTo(videoLevel)
//		) {
//			return false;
//		}

		// Check whether the subtitle actually has a language defined,
		// uninitialized DLNAMediaSubtitle objects have a null language.
		// Check whether the subtitle is supported by tsMuxeR.
		if (
			!configuration.isDisableSubtitles() &&
			subtitle != null &&
			subtitle.getType() != SubtitleType.SUBRIP &&
			subtitle.getType() != SubtitleType.PGS
		) {
			return false;
		}

		if (
			media == null ||
			isBlank(media.getCodecV()) ||
			!resource.matches(
			MediaType.VIDEO,
			FormatConfiguration.MKV,
			FormatConfiguration.MOV,
			FormatConfiguration.MP4,
			FormatConfiguration.MPEG2,
			FormatConfiguration.MPEGPS,
			FormatConfiguration.MPEGTS
		)) {
			return false;
		}

		switch (media.getCodecV().trim().toLowerCase(Locale.ROOT)) {
			case FormatConfiguration.H264:
			case FormatConfiguration.H265:
			case FormatConfiguration.MPEG2:
			case FormatConfiguration.VC1:
				break;
			default:
				return false;
		}

		if (media.getAudioTracksList().size() > 0) {
			for (DLNAMediaAudio audio : media.getAudioTracksList()) {
				if (isBlank(audio.getCodecA())) {
					return false;
				}
				switch (audio.getCodecA()) {
					case FormatConfiguration.AAC_LC:
					case FormatConfiguration.AC3:
					case FormatConfiguration.DTS:
					case FormatConfiguration.DTSHD:
					case FormatConfiguration.EAC3:
					case FormatConfiguration.HE_AAC:
					case FormatConfiguration.LPCM:
					case FormatConfiguration.TRUEHD:
						break;
					default:
						return false;
				}
			}
		}
		return true;
	}

	@Override
	public @Nullable ExecutableInfo testExecutable(@Nonnull ExecutableInfo executableInfo) {
		executableInfo = testExecutableFile(executableInfo);
		if (Boolean.FALSE.equals(executableInfo.getAvailable())) {
			return executableInfo;
		}
		final String arg = "-v";
		ExecutableInfoBuilder result = executableInfo.modify();
		try {
			ListProcessWrapperResult output = SimpleProcessWrapper.runProcessListOutput(
				30000,
				1000,
				executableInfo.getPath().toString(),
				arg
			);
			if (output.getError() != null) {
				result.errorType(ExecutableErrorType.GENERAL);
				result.errorText(String.format(Messages.getString("Engine.Error"), this) + " \n" + output.getError().getMessage());
				result.available(Boolean.FALSE);
				LOGGER.debug("\"{} {}\" failed with error: {}", executableInfo.getPath(), arg, output.getError().getMessage());
				return result.build();
			}
			if (output.getExitCode() == 0) {
				if (output.getOutput() != null && output.getOutput().size() > 0) {
					Pattern pattern = Pattern.compile("tsMuxeR\\.\\s+Version\\s(\\S+)\\s+", Pattern.CASE_INSENSITIVE);
					Matcher matcher = pattern.matcher(output.getOutput().get(0));
					if (matcher.find() && isNotBlank(matcher.group(1))) {
						result.version(new Version(matcher.group(1)));
					}
				}
				result.available(Boolean.TRUE);
			} else {
				NTStatus ntStatus = Platform.isWindows() ? NTStatus.typeOf(output.getExitCode()) : null;
				if (ntStatus != null) {
					result.errorType(ExecutableErrorType.GENERAL);
					result.errorText(String.format(Messages.getString("Engine.Error"), this) + "\n\n" + ntStatus);
				} else {
					result.errorType(ExecutableErrorType.GENERAL);
					result.errorText(String.format(Messages.getString("Engine.ExitCode"), this, output.getExitCode()));
					if (Platform.isLinux() && Platform.is64Bit()) {
						result.errorType(ExecutableErrorType.GENERAL);
						result.errorText(result.errorText() + ". \n" + Messages.getString("Engine.tsMuxerErrorLinux"));
					}
					result.available(Boolean.FALSE);
				}
			}
		} catch (InterruptedException e) {
			return null;
		}
		return result.build();
	}

	@Override
	protected boolean isSpecificTest() {
		return false;
	}
}
