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
import com.sun.jna.Platform;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import net.pms.Messages;
import net.pms.configuration.DeviceConfiguration;
import net.pms.configuration.ExecutableInfo;
import net.pms.configuration.ExecutableInfo.ExecutableInfoBuilder;
import net.pms.configuration.ExternalProgramInfo;
import net.pms.configuration.FFmpegExecutableInfo;
import net.pms.configuration.FFmpegExecutableInfo.FFmpegExecutableInfoBuilder;
import net.pms.configuration.FFmpegProgramInfo;
import net.pms.configuration.FormatConfiguration;
import net.pms.configuration.PmsConfiguration;
import net.pms.configuration.RendererConfiguration;
import net.pms.dlna.DLNAMediaInfo;
import net.pms.dlna.DLNAMediaSubtitle;
import net.pms.dlna.DLNAResource;
import net.pms.dlna.FileTranscodeVirtualFolder;
import net.pms.dlna.InputFile;
import net.pms.dlna.PartialSource;
import net.pms.formats.Format;
import net.pms.formats.FormatType;
import net.pms.formats.PLAYLIST;
import net.pms.formats.v2.SubtitleType;
import net.pms.io.*;
import net.pms.media.H264Level;
import net.pms.media.VideoCodec;
import net.pms.media.VideoLevel;
import net.pms.network.HTTPResource;
import net.pms.newgui.GuiUtil;
import net.pms.platform.windows.NTStatus;
import net.pms.util.CodecUtil;
import net.pms.util.ProcessUtil;
import net.pms.util.Rational;
import net.pms.util.StringUtil;
import net.pms.util.SubtitleUtils;
import org.apache.commons.lang3.StringUtils;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Pure FFmpeg video player.
 *
 * Design note:
 *
 * Helper methods that return lists of <code>String</code>s representing
 * options are public to facilitate composition e.g. a custom engine (plugin)
 * that uses tsMuxeR for videos without subtitles and FFmpeg otherwise needs to
 * compose and call methods on both players.
 *
 * To avoid API churn, and to provide wiggle room for future functionality, all
 * of these methods take the same arguments as launchTranscode (and the same
 * first four arguments as finalizeTranscoderArgs) even if one or more of the
 * parameters are unused e.g.:
 *
 *     public List<String> getAudioBitrateOptions(
 *         String filename,
 *         DLNAResource dlna,
 *         DLNAMediaInfo media,
 *         OutputParams params
 *     )
 */
public class FFMpegVideo extends Player {
	private static final Logger LOGGER = LoggerFactory.getLogger(FFMpegVideo.class);
	public static final PlayerId ID = StandardPlayerId.FFMPEG_VIDEO;
	private static Rational ASPECT_16_9 = Rational.valueOf(16, 9);

	/** The {@link Configuration} key for the custom FFmpeg path. */
	public static final String KEY_FFMPEG_PATH = "ffmpeg_path";

	/** The {@link Configuration} key for the FFmpeg executable type. */
	public static final String KEY_FFMPEG_EXECUTABLE_TYPE = "ffmpeg_executable_type";
	public static final String NAME = "FFmpeg Video";
	private static final String DEFAULT_QSCALE = "3";

	// Not to be instantiated by anything but PlayerFactory
	FFMpegVideo() {
	}

	/**
	 * Returns a list of strings representing the rescale options for this transcode i.e. the ffmpeg -vf
	 * options used to show subtitles in either SSA/ASS or picture-based format and resize a video that's too wide and/or high for the specified renderer.
	 * If the renderer has no size limits, or there's no media metadata, or the video is within the renderer's
	 * size limits, an empty list is returned.
	 *
	 * @param dlna
	 * @param media metadata for the DLNA resource which is being transcoded
	 * @param params
	 * @return a {@link List} of <code>String</code>s representing the rescale options for this video,
	 * or an empty list if the video doesn't need to be resized.
	 * @throws java.io.IOException
	 */
	public List<String> getVideoFilterOptions(DLNAResource dlna, DLNAMediaInfo media, OutputParams params) throws IOException {
		List<String> videoFilterOptions = new ArrayList<>();
		ArrayList<String> filterChain = new ArrayList<>();
		ArrayList<String> scalePadFilterChain = new ArrayList<>();
		final RendererConfiguration renderer = params.mediaRenderer;

		boolean isMediaValid = media.isMediaparsed() && media.getHeight() != 0;
		boolean isResolutionTooHighForRenderer = isMediaValid && !params.mediaRenderer.isResolutionCompatibleWithRenderer(media.getWidth(), media.getHeight());

		int scaleWidth = 0;
		int scaleHeight = 0;
		if (media.getWidth() > 0 && media.getHeight() > 0) {
			scaleWidth = media.getWidth();
			scaleHeight = media.getHeight();
		}

		boolean is3D = media.is3d() && !media.stereoscopyIsAnaglyph();

		// Make sure the aspect ratio is 16/9 if the renderer needs it.
		boolean keepAR = (renderer.isKeepAspectRatio() || renderer.isKeepAspectRatioTranscoding()) &&
				!media.is3dFullSbsOrOu() &&
				!ASPECT_16_9.equals(media.getAspectRatioContainer());

		// Scale and pad the video if necessary
		if (isResolutionTooHighForRenderer || (!renderer.isRescaleByRenderer() && renderer.isMaximumResolutionSpecified() && media.getWidth() < 720)) { // Do not rescale for SD video and higher
			if (media.is3dFullSbsOrOu()) {
				scalePadFilterChain.add(String.format("scale=%1$d:%2$d", renderer.getMaxVideoWidth(), renderer.getMaxVideoHeight()));
			} else {
				scalePadFilterChain.add(String.format("scale=iw*min(%1$d/iw\\,%2$d/ih):ih*min(%1$d/iw\\,%2$d/ih)", renderer.getMaxVideoWidth(), renderer.getMaxVideoHeight()));

				if (keepAR) {
					scalePadFilterChain.add(String.format("pad=%1$d:%2$d:(%1$d-iw)/2:(%2$d-ih)/2", renderer.getMaxVideoWidth(), renderer.getMaxVideoHeight()));
				}
			}
		} else if (keepAR && isMediaValid) {
			if ((media.getWidth() / (double) media.getHeight()) >= (16 / (double) 9)) {
				scalePadFilterChain.add("pad=iw:iw/(16/9):0:(oh-ih)/2");
				scaleHeight = (int) Math.round(scaleWidth / (16 / (double) 9));
			} else {
				scalePadFilterChain.add("pad=ih*(16/9):ih:(ow-iw)/2:0");
				scaleWidth = (int) Math.round(scaleHeight * (16 / (double) 9));
			}

			scaleWidth  = convertToModX(scaleWidth, 4);
			scaleHeight = convertToModX(scaleHeight, 4);

			// Make sure we didn't exceed the renderer's maximum resolution.
			if (
				scaleHeight > renderer.getMaxVideoHeight() ||
				scaleWidth  > renderer.getMaxVideoWidth()
			) {
				scaleHeight = renderer.getMaxVideoHeight();
				scaleWidth  = renderer.getMaxVideoWidth();
			}

			scalePadFilterChain.add("scale=" + scaleWidth + ":" + scaleHeight);
		}

		filterChain.addAll(scalePadFilterChain);

		boolean override = true;
		if (renderer instanceof RendererConfiguration.OutputOverride) {
			RendererConfiguration.OutputOverride or = (RendererConfiguration.OutputOverride)renderer;
			override = or.addSubtitles();
		}

		if (!isDisableSubtitles(params) && override) {
			boolean isSubsManualTiming = true;
			DLNAMediaSubtitle convertedSubs = dlna.getMediaSubtitle();
			StringBuilder subsFilter = new StringBuilder();
			if (params.sid != null && params.sid.getType().isText()) {
				boolean isSubsASS = params.sid.getType() == SubtitleType.ASS;
				String originalSubsFilename = null;
				if (is3D) {
					if (convertedSubs != null && convertedSubs.getConvertedFile() != null) { // subs are already converted to 3D so use them
						originalSubsFilename = convertedSubs.getConvertedFile().getAbsolutePath();
					} else if (!isSubsASS) { // When subs are not converted and they are not in the ASS format and video is 3D then subs need conversion to 3D
						originalSubsFilename = SubtitleUtils.getSubtitles(dlna, media, params, configuration, SubtitleType.ASS).getAbsolutePath();
					} else {
						originalSubsFilename = params.sid.getExternalFile().getAbsolutePath();
					}
				} else if (params.sid.isExternal()) {
					if (params.sid.isStreamable() && renderer.streamSubsForTranscodedVideo()) { // when subs are streamable do not transcode them
						originalSubsFilename = null;
					} else {
						originalSubsFilename = params.sid.getExternalFile().getAbsolutePath();
					}
				} else if (params.sid.isEmbedded()) {
					originalSubsFilename = dlna.getFileName();
				}

				if (originalSubsFilename != null) {
					subsFilter.append("subtitles=").append(StringUtil.ffmpegEscape(originalSubsFilename));
					if (params.sid.isEmbedded()) {
						subsFilter.append(":si=").append(params.sid.getId());
					}

					// Set the input subtitles character encoding if not UTF-8
					if (!params.sid.isSubsUtf8()) {
						if (isNotBlank(configuration.getSubtitlesCodepage())) {
							subsFilter.append(":charenc=").append(configuration.getSubtitlesCodepage());
						} else if (params.sid.getSubCharacterSet() != null) {
							subsFilter.append(":charenc=").append(params.sid.getSubCharacterSet());
						}
					}

					// If the FFmpeg font config is enabled than we need to add settings to the filter. TODO there could be also changed the font type. See http://ffmpeg.org/ffmpeg-filters.html#subtitles-1
					if (configuration.isFFmpegFontConfig() && !is3D && !isSubsASS) { // Do not force style for 3D videos and ASS subtitles
						subsFilter.append(":force_style=");
						subsFilter.append("'");
						String fontName = configuration.getFont();
						if (isNotBlank(fontName)) {
							String font = CodecUtil.isFontRegisteredInOS(fontName);
							if (font != null) {
								subsFilter.append("Fontname=").append(font);
							}
						}

						// XXX (valib) If the font size is not acceptable it could be calculated better taking in to account the original video size. Unfortunately I don't know how to do that.
						subsFilter.append(",Fontsize=").append(15 * Double.parseDouble(configuration.getAssScale()));
						subsFilter.append(",PrimaryColour=").append(configuration.getSubsColor().getASSv4StylesHexValue());
						subsFilter.append(",Outline=").append(configuration.getAssOutline());
						subsFilter.append(",Shadow=").append(configuration.getAssShadow());
						subsFilter.append(",MarginV=").append(configuration.getAssMargin());
						subsFilter.append("'");
					}
				}
			} else if (params.sid.getType().isPicture()) {
				if (params.sid.getId() < 100) {
					// Embedded
					subsFilter.append("[0:v][0:s:").append(media.getSubtitleTracksList().indexOf(params.sid)).append("]overlay");
					isSubsManualTiming = false;
				} else {
					// External
					videoFilterOptions.add("-i");
					videoFilterOptions.add(params.sid.getExternalFile().getAbsolutePath());
					subsFilter.append("[0:v][1:s]overlay"); // this assumes the sub file is single-language
				}
			}
			if (isNotBlank(subsFilter)) {
				if (params.timeseek > 0 && isSubsManualTiming) {
					filterChain.add("setpts=PTS+" + params.timeseek + "/TB"); // based on https://trac.ffmpeg.org/ticket/2067
				}

				filterChain.add(subsFilter.toString());
				if (params.timeseek > 0 && isSubsManualTiming) {
					filterChain.add("setpts=PTS-STARTPTS"); // based on https://trac.ffmpeg.org/ticket/2067
				}
			}
		}

		String overrideVF = renderer.getFFmpegVideoFilterOverride();
		if (StringUtils.isNotEmpty(overrideVF)) {
			filterChain.add(overrideVF);
		}

		// Convert 3D video to the other output 3D format or to 2D using "Output3DFormat = ml" or "Output3DFormat = mr" in the renderer conf
		String stereoLayout = null;
		String renderer3DOutputFormat = null;
		if (media.get3DLayout() != null) {
			stereoLayout = media.get3DLayout().toString().toLowerCase(Locale.ROOT);
			renderer3DOutputFormat = params.mediaRenderer.getOutput3DFormat();
		}

		if (
			is3D &&
			stereoLayout != null &&
			isNotBlank(renderer3DOutputFormat) &&
			!stereoLayout.equals(renderer3DOutputFormat)
		) {
			filterChain.add("stereo3d=" + stereoLayout + ":" + renderer3DOutputFormat);
		}

		if (filterChain.size() > 0) {
			videoFilterOptions.add("-filter_complex");
			videoFilterOptions.add(StringUtils.join(filterChain, ","));
		}

		return videoFilterOptions;
	}

	/**
	 * Returns a list of <code>String</code>s representing ffmpeg output
	 * options (i.e. options that define the output file's video codec,
	 * audio codec and container) compatible with the renderer's
	 * <code>TranscodeVideo</code> profile.
	 *
	 * @param dlna
	 * @param media the media metadata for the video being streamed. May contain unset/null values (e.g. for web videos).
	 * @param params output parameters
	 *
	 * @return a {@link List} of <code>String</code>s representing the FFmpeg output parameters for the renderer according
	 * to its <code>TranscodeVideo</code> profile.
	 */
	public synchronized List<String> getVideoTranscodeOptions(DLNAResource dlna, DLNAMediaInfo media, OutputParams params) {
		List<String> transcodeOptions = new ArrayList<>();
		final String filename = dlna.getFileName();
		final RendererConfiguration renderer = params.mediaRenderer;
		String customFFmpegOptions = renderer.getCustomFFmpegOptions();

		if (
			(
				renderer.isTranscodeToWMV() &&
				!renderer.isXbox360()
			) ||
			(
				renderer.isXboxOne() &&
				purpose() == VIDEO_WEBSTREAM_PLAYER
			)
		) { // WMV
			transcodeOptions.add("-c:v");
			transcodeOptions.add("wmv2");

			if (!Pattern.compile("-(?:c:a|codec:a|acodec)\\b").matcher(customFFmpegOptions).find()) {
				transcodeOptions.add("-c:a");
				transcodeOptions.add("wmav2");
			}

			transcodeOptions.add("-f");
			transcodeOptions.add("asf");
		} else { // MPEGPSMPEG2AC3, MPEGTSMPEG2AC3, MPEGTSH264AC3 or MPEGTSH264AAC
			final boolean isTsMuxeRVideoEngineActive = PlayerFactory.isPlayerActive(TsMuxeRVideo.ID);

			// Output audio codec
			dtsRemux = isTsMuxeRVideoEngineActive &&
				configuration.isAudioEmbedDtsInPcm() &&
				params.aid != null &&
				params.aid.isDTS() &&
				!avisynth() &&
				renderer.isDTSPlayable();

			boolean isSubtitlesAndTimeseek = !isDisableSubtitles(params) && params.timeseek > 0;

			if (configuration.isAudioRemuxAC3() && params.aid != null && params.aid.isAC3() && !avisynth() && renderer.isTranscodeToAC3() && !isSubtitlesAndTimeseek) {
				// AC-3 remux
				if (!Pattern.compile("-(?:c:a|codec:a|acodec)\\b").matcher(customFFmpegOptions).find()) {
					transcodeOptions.add("-c:a");
					transcodeOptions.add("copy");
				}
			} else {
				if (dtsRemux) {
					// Audio is added in a separate process later
					transcodeOptions.add("-an");
				} else if (type() == FormatType.AUDIO) {
					// Skip
				} else if (!Pattern.compile("-(?:c:a|codec:a|acodec)\\b").matcher(customFFmpegOptions).find()) {
					if (renderer.isTranscodeToAAC()) {
						transcodeOptions.add("-c:a");
						transcodeOptions.add("aac");
					} else {
						transcodeOptions.add("-c:a");
						transcodeOptions.add("ac3");
					}
				}
			}

			InputFile newInput = null;
			if (filename != null) {
				newInput = new InputFile();
				newInput.setFilename(filename);
				newInput.setPush(params.stdin);
			}

			// Output video codec
			if (renderer.isTranscodeToH264() || renderer.isTranscodeToH265()) {
				if (!Pattern.compile("-(?:c:v|codec:v|vcodec)\\b").matcher(customFFmpegOptions).find()) {
					transcodeOptions.add("-c:v");
					if (renderer.isTranscodeToH264()) {
						transcodeOptions.add("libx264");
					} else {
						transcodeOptions.add("libx265");
					}
					if (!customFFmpegOptions.contains("-tune")) {
						transcodeOptions.add("-tune");
						transcodeOptions.add("zerolatency");
					}
					if (!Pattern.compile("(?:\\bpreset(?!:a)|-vpre)\\b").matcher(customFFmpegOptions).find()) {
						transcodeOptions.add("-preset");
						transcodeOptions.add("ultrafast");
					}
				}
				if (!Pattern.compile("\\blevel(?!:a)\\b").matcher(customFFmpegOptions).find()) {
					VideoLevel level = renderer.getVideoLevelLimit(
						renderer.isTranscodeToH264() ? VideoCodec.H264 : VideoCodec.H265
					);
					if (level != null) {
						transcodeOptions.add("-level");
						transcodeOptions.add(level.toString(false));
					}
				}
				if (!Pattern.compile("-pix_fmt\\b|\\bformat=").matcher(customFFmpegOptions).find()) {
					transcodeOptions.add("-pix_fmt");
					transcodeOptions.add("yuv420p");
				}
			} else if (!dtsRemux) {
				if (!Pattern.compile("-(?:c:v|codec:v|vcodec)\\b").matcher(customFFmpegOptions).find()) {
					transcodeOptions.add("-c:v");
					transcodeOptions.add("mpeg2video");
				}
			}

			if (!Pattern.compile("-f\\b").matcher(customFFmpegOptions).find()) {
				// Output file format
				transcodeOptions.add("-f");
				if (dtsRemux) {
					transcodeOptions.add("mpeg2video");
				} else if (renderer.isTranscodeToMPEGTS()) {
					transcodeOptions.add("mpegts");
				} else {
					transcodeOptions.add("vob");
				}
			}
		}

		return transcodeOptions;
	}

	/**
	 * Returns the video bitrate spec for the current transcode according
	 * to the limits/requirements of the renderer and the user's settings.
	 *
	 * @param dlna
	 * @param media the media metadata for the video being streamed. May contain unset/null values (e.g. for web videos).
	 * @param params
	 * @return a {@link List} of <code>String</code>s representing the video bitrate options for this transcode
	 */
	public List<String> getVideoBitrateOptions(DLNAResource dlna, DLNAMediaInfo media, OutputParams params) {
		List<String> videoBitrateOptions = new ArrayList<>();
		boolean low = false;

		int defaultMaxBitrates[] = getVideoBitrateConfig(configuration.getMaximumBitrate());
		int rendererMaxBitrates[] = new int[2];

		if (isNotBlank(params.mediaRenderer.getMaxVideoBitrate())) {
			rendererMaxBitrates = getVideoBitrateConfig(params.mediaRenderer.getMaxVideoBitrate());
		}

		// Give priority to the renderer's maximum bitrate setting over the user's setting
		if (rendererMaxBitrates[0] > 0 && rendererMaxBitrates[0] < defaultMaxBitrates[0]) {
			LOGGER.trace(
				"Using video bitrate limit from {} configuration ({} Mb/s) because " +
				"it is lower than the general configuration bitrate limit ({} Mb/s)",
				params.mediaRenderer.getRendererName(),
				rendererMaxBitrates[0],
				defaultMaxBitrates[0]
			);
			defaultMaxBitrates = rendererMaxBitrates;
		} else {
			LOGGER.trace(
				"Using video bitrate limit from the general configuration ({} Mb/s)",
				defaultMaxBitrates[0]
			);
		}

		boolean isXboxOneWebVideo = params.mediaRenderer.isXboxOne() && purpose() == VIDEO_WEBSTREAM_PLAYER;
		int maximumBitrate = defaultMaxBitrates[0];

		if (params.mediaRenderer.getCBRVideoBitrate() < 1 && params.timeend == 0) {
			if (rendererMaxBitrates[0] < 0) {
				// odd special case here
				// this is -1 so we guess that 3000 kbps is good
				defaultMaxBitrates[0] = 3000;
				low = true;
			} else {
				// Convert value from Mb to Kb
				defaultMaxBitrates[0] = 1000 * defaultMaxBitrates[0];
			}

			if (params.mediaRenderer.isHalveBitrate()) {
				defaultMaxBitrates[0] /= 2;
				LOGGER.trace("Halving the video bitrate limit to {} kb/s", defaultMaxBitrates[0]);
			}

			int bufSize = 1835;
			boolean bitrateLevel41Limited = false;

			/**
			 * Although the maximum bitrate for H.264 Level 4.1 is
			 * officially 50,000 kbit/s, some 4.1-capable renderers
			 * like the PS3 stutter when video exceeds roughly 31,250
			 * kbit/s.
			 *
			 * We also apply the correct buffer size in this section.
			 */
			if (!isXboxOneWebVideo && params.mediaRenderer.isTranscodeToH264()) {
				if (
					params.mediaRenderer.getVideoLevelLimit(VideoCodec.H264) == H264Level.L4_1 &&
					defaultMaxBitrates[0] > 31250
				) {
					defaultMaxBitrates[0] = 31250;
					bitrateLevel41Limited = true;
					LOGGER.trace("Adjusting the video bitrate limit to the H.264 Level 4.1-safe value of 31250 kb/s");
				}
				bufSize = defaultMaxBitrates[0];
			} else {
				if (media.isHDVideo()) {
					bufSize = defaultMaxBitrates[0] / 3;
				}

				if (bufSize > 7000) {
					bufSize = 7000;
				}

				if (defaultMaxBitrates[1] > 0) {
					bufSize = defaultMaxBitrates[1];
				}

				if (params.mediaRenderer.isDefaultVBVSize() && rendererMaxBitrates[1] == 0) {
					bufSize = 1835;
				}
			}

			if (!bitrateLevel41Limited) {
				// Make room for audio
				if (dtsRemux) {
					defaultMaxBitrates[0] -= 1510;
				} else {
					defaultMaxBitrates[0] -= configuration.getAudioBitrate();
				}

				// Round down to the nearest Mb
				defaultMaxBitrates[0] = defaultMaxBitrates[0] / 1000 * 1000;
				if (low) {
					defaultMaxBitrates[0] = 3000;
				}

				LOGGER.trace(
					"Adjusting the video bitrate limit to {} kb/s to make room for audio",
					defaultMaxBitrates[0]
				);
			}

			videoBitrateOptions.add("-bufsize");
			videoBitrateOptions.add(String.valueOf(bufSize) + "k");

			if (defaultMaxBitrates[0] > 0) {
				videoBitrateOptions.add("-maxrate");
				videoBitrateOptions.add(String.valueOf(defaultMaxBitrates[0]) + "k");
			}
		}

		if (isXboxOneWebVideo || !params.mediaRenderer.isTranscodeToH264()) {
			// Add MPEG-2 quality settings
			String mpeg2Options = configuration.getMPEG2MainSettingsFFmpeg();
			String mpeg2OptionsRenderer = params.mediaRenderer.getCustomFFmpegMPEG2Options();

			// Renderer settings take priority over user settings
			if (isNotBlank(mpeg2OptionsRenderer)) {
				mpeg2Options = mpeg2OptionsRenderer;
			} else if (mpeg2Options.contains("Automatic")) {
				boolean isWireless = mpeg2Options.contains("Wireless");
				mpeg2Options = "-g 5 -q:v 1 -qmin 2 -qmax 3";

				// It has been reported that non-PS3 renderers prefer keyint 5 but prefer it for PS3 because it lowers the average bitrate
				if (params.mediaRenderer.isPS3()) {
					mpeg2Options = "-g 25 -q:v 1 -qmin 2 -qmax 3";
				}

				if (isWireless || maximumBitrate < 70) {
					// Lower quality for 720p+ content
					if (media.getWidth() > 1280) {
						mpeg2Options = "-g 25 -qmax 7 -qmin 2";
					} else if (media.getWidth() > 720) {
						mpeg2Options = "-g 25 -qmax 5 -qmin 2";
					}
				}
			}
			String[] customOptions = StringUtils.split(mpeg2Options);
			videoBitrateOptions.addAll(new ArrayList<>(Arrays.asList(customOptions)));
		} else {
			// Add x264 quality settings
			if (
				!Pattern.compile("-(?:crf|b|b:v|qp)\\b|(?:(?:x264opts|x264-params)\\s[^-]*(?:crf|bitrate|qp)=)")
				.matcher(params.mediaRenderer.getCustomFFmpegOptions()).find()
			) {
				String x264CRF = configuration.getx264ConstantRateFactor();

				// Remove comment from the value
				if (x264CRF.contains("/*")) {
					x264CRF = x264CRF.substring(x264CRF.indexOf("/*"));
				}

				if (x264CRF.contains("Automatic")) {
					if (x264CRF.contains("Wireless") || maximumBitrate < 70) {
						x264CRF = "19";
						// Lower quality for 720p+ content
						if (media.getWidth() > 1280) {
							x264CRF = "23";
						} else if (media.getWidth() > 720) {
							x264CRF = "22";
						}
					} else {
						x264CRF = "16";

						// Lower quality for 720p+ content
						if (media.getWidth() > 720) {
							x264CRF = "19";
						}
					}
				}
				if (isNotBlank(x264CRF) && !params.mediaRenderer.nox264()) {
					videoBitrateOptions.add("-crf");
					videoBitrateOptions.add(x264CRF);
				}
			}
		}

		return videoBitrateOptions;
	}

	/**
	 * Returns the audio bitrate spec for the current transcode according
	 * to the limits/requirements of the renderer.
	 *
	 * @param dlna
	 * @param media the media metadata for the video being streamed. May contain unset/null values (e.g. for web videos).
	 * @param params
	 * @return a {@link List} of <code>String</code>s representing the audio bitrate options for this transcode
	 */
	public List<String> getAudioBitrateOptions(DLNAResource dlna, DLNAMediaInfo media, OutputParams params) {
		List<String> audioBitrateOptions = new ArrayList<>();

		audioBitrateOptions.add("-q:a");
		audioBitrateOptions.add(DEFAULT_QSCALE);

		audioBitrateOptions.add("-ar");
		audioBitrateOptions.add("" + params.mediaRenderer.getTranscodedVideoAudioSampleRate());

		return audioBitrateOptions;
	}

	protected boolean dtsRemux;
	protected boolean ac3Remux;

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
		return KEY_FFMPEG_PATH;
	}

	@Override
	public String getExecutableTypeKey() {
		return KEY_FFMPEG_EXECUTABLE_TYPE;
	}

	@Override
	public boolean isTimeSeekable() {
		return true;
	}

	@Override
	public boolean avisynth() {
		return false;
	}

	public String initialString() {
		String threads = " -threads 1";
		if (configuration.isFfmpegMultithreading()) {
			if (Runtime.getRuntime().availableProcessors() == configuration.getNumberOfCpuCores()) {
				threads = "";
			} else {
				threads = " -threads " + configuration.getNumberOfCpuCores();
			}
		}
		return threads;
	}

	@Override
	public String name() {
		return NAME;
	}

	@Override
	public FormatType type() {
		return FormatType.VIDEO;
	}

	private static int[] getVideoBitrateConfig(String bitrate) {
		int bitrates[] = new int[2];

		if (bitrate.contains("(") && bitrate.contains(")")) {
			bitrates[1] = Integer.parseInt(bitrate.substring(bitrate.indexOf('(') + 1, bitrate.indexOf(')')));
		}

		if (bitrate.contains("(")) {
			bitrate = bitrate.substring(0, bitrate.indexOf('(')).trim();
		}

		if (isBlank(bitrate)) {
			bitrate = "0";
		}

		bitrates[0] = (int) Double.parseDouble(bitrate);

		return bitrates;
	}

	@Override
	@Deprecated
	public String[] args() {
		return null;
	}

	@Override
	public String mimeType() {
		return HTTPResource.VIDEO_TRANSCODE;
	}

	@Override
	protected ExternalProgramInfo programInfo() {
		return configuration.getFFmpegPaths();
	}

	@Override
	public boolean isGPUAccelerationReady() {
		return false;
	}

	@Override
	public synchronized ProcessWrapper launchTranscode(
		DLNAResource dlna,
		DLNAMediaInfo media,
		OutputParams params
	) throws IOException {
		final String filename = dlna.getFileName();
		InputFile newInput = new InputFile();
		newInput.setFilename(filename);
		newInput.setPush(params.stdin);
		// Use device-specific DMS conf
		PmsConfiguration prev = configuration;
		configuration = (DeviceConfiguration) params.mediaRenderer;
		RendererConfiguration renderer = params.mediaRenderer;

		/*
		 * Check if the video track and the container report different aspect ratios
		 */
		boolean aspectRatiosMatch = true;
		if (
			media.getAspectRatioContainer() != null &&
			media.getAspectRatioVideoTrack() != null &&
			!media.getAspectRatioContainer().equals(media.getAspectRatioVideoTrack())
		) {
			aspectRatiosMatch = false;
		}

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
		boolean avisynth = avisynth();
		if (params.timeseek > 0) {
			params.waitbeforestart = 1;
		} else if (renderer.isTranscodeFastStart()){
			params.manageFastStart();
		} else {
			params.waitbeforestart = 1;
		}

		setAudioAndSubs(filename, media, params);
		dlna.setMediaSubtitle(params.sid);
		cmdList.add(getExecutable());

		// Prevent FFmpeg timeout
		cmdList.add("-y");

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
			cmdList.add(String.valueOf(nThreads));
		}

		final boolean isTsMuxeRVideoEngineActive = PlayerFactory.isPlayerActive(TsMuxeRVideo.ID);
		final boolean isXboxOneWebVideo = params.mediaRenderer.isXboxOne() && purpose() == VIDEO_WEBSTREAM_PLAYER;

		ac3Remux = false;
		dtsRemux = false;

		if (
			configuration.isAudioRemuxAC3() &&
			params.aid != null &&
			params.aid.isAC3() &&
			!avisynth() &&
			renderer.isTranscodeToAC3() &&
			!isXboxOneWebVideo &&
			params.aid.getNumberOfChannels() <= configuration.getAudioChannelCount()
		) {
			// AC-3 remux takes priority
			ac3Remux = true;
		} else {
			// Now check for DTS remux and LPCM streaming
			dtsRemux = isTsMuxeRVideoEngineActive &&
				configuration.isAudioEmbedDtsInPcm() &&
				params.aid != null &&
				params.aid.isDTS() &&
				!avisynth() &&
				params.mediaRenderer.isDTSPlayable();
		}

		String frameRateRatio = media.getValidFps(true);
		String frameRateNumber = media.getValidFps(false);

		// Input filename
		cmdList.add("-i");
		if (avisynth && !filename.toLowerCase().endsWith(".iso")) {
			File avsFile = AviSynthFFmpeg.getAVSScript(filename, params.sid, params.fromFrame, params.toFrame, frameRateRatio, frameRateNumber, configuration);
			cmdList.add(ProcessUtil.getShortFileNameIfWideChars(avsFile.getAbsolutePath()));
		} else {
			if (params.stdin != null) {
				cmdList.add("pipe:");
			} else {
				cmdList.add(filename);
			}
		}

		/**
		 * Defer to MEncoder for subtitles if:
		 * - MEncoder is enabled and available
		 * - The setting is enabled
		 * - There are subtitles to transcode
		 * - The file is not being played via the transcode folder
		 */
		if (
			PlayerFactory.isPlayerActive(MEncoderVideo.ID) &&
			!(renderer instanceof RendererConfiguration.OutputOverride) &&
			params.sid != null &&
			!(
				!configuration.getHideTranscodeEnabled() &&
				dlna.isNoName() &&
				(dlna.getParent() instanceof FileTranscodeVirtualFolder)
			) &&
			configuration.isFFmpegDeferToMEncoderForProblematicSubtitles() &&
			params.sid.isEmbedded() &&
			(
				(
					params.sid.getType().isText() &&
					params.sid.getType() != SubtitleType.ASS
				) ||
				params.sid.getType() == SubtitleType.VOBSUB
			)
		) {
			LOGGER.trace("Switching from FFmpeg to MEncoder to transcode subtitles because the user setting is enabled.");
			MEncoderVideo mv = (MEncoderVideo) PlayerFactory.getPlayer(MEncoderVideo.ID,  false, false);
			return mv.launchTranscode(dlna, media, params);
		}

		// Decide whether to defer to tsMuxeR or continue to use FFmpeg
		if (
			!(renderer instanceof RendererConfiguration.OutputOverride) &&
			configuration.isFFmpegMuxWithTsMuxerWhenCompatible()
		) {
			// Decide whether to defer to tsMuxeR or continue to use FFmpeg
			boolean deferToTsmuxer = PlayerFactory.isPlayerActive(TsMuxeRVideo.ID);
			String prependTraceReason = "Not muxing the video stream with tsMuxeR via FFmpeg because ";
			if (!deferToTsmuxer) {
				LOGGER.warn(
					prependTraceReason + "tsMuxeR {}",
					PlayerFactory.isPlayerAvailable(TsMuxeRVideo.ID) ? "is disabled" : "isn't available"
				);
			}
			VideoLevel videoLevelLimit = params.mediaRenderer.getVideoLevelLimit(media.getVideoCodec());
			if (
				deferToTsmuxer &&
				!configuration.getHideTranscodeEnabled() &&
				dlna.isNoName() &&
				dlna.getParent() instanceof FileTranscodeVirtualFolder
			) {
				deferToTsmuxer = false;
				LOGGER.trace(prependTraceReason + "the file is being played via a FFmpeg entry in the transcode folder.");
			}
			if (deferToTsmuxer && !params.mediaRenderer.isMuxH264MpegTS()) {
				deferToTsmuxer = false;
				LOGGER.trace(prependTraceReason + "the renderer does not support H.264 inside MPEG-TS.");
			}
			if (deferToTsmuxer && params.sid != null) {
				deferToTsmuxer = false;
				LOGGER.trace(prependTraceReason + "we need to burn subtitles.");
			}
			if (deferToTsmuxer && avisynth()) {
				deferToTsmuxer = false;
				LOGGER.trace(prependTraceReason + "we are using AviSynth.");
			}
			if (
				deferToTsmuxer &&
				videoLevelLimit != null &&
				!videoLevelLimit.isGreaterThanOrEqualTo(media.getVideoLevel())
			) {
				deferToTsmuxer = false;
				if (LOGGER.isTraceEnabled()) {
					VideoLevel level = media.getVideoLevel();
					if (level == null) {
						LOGGER.trace(prependTraceReason + "the {} level is unknown", media.getVideoCodec());
					} else {
						LOGGER.trace(
							prependTraceReason + "the {} level ({}) is above the limit ({}) for this renderer",
							media.getVideoCodec(),
							level.toString(false),
							videoLevelLimit.toString(false)
						);
					}
				}
			}
			if (deferToTsmuxer && !media.isMuxable(params.mediaRenderer)) {
				deferToTsmuxer = false;
				LOGGER.trace(prependTraceReason + "the video stream is not muxable to this renderer");
			}
			if (deferToTsmuxer && !aspectRatiosMatch) {
				deferToTsmuxer = false;
				LOGGER.trace(prependTraceReason + "we need to transcode to apply the correct aspect ratio.");
			}
			if (
				deferToTsmuxer &&
				!params.mediaRenderer.isPS3() &&
				media.isWebDl(filename, params)
			) {
				deferToTsmuxer = false;
				LOGGER.trace(prependTraceReason + "the version of tsMuxeR supported by this renderer does not support WEB-DL files.");
			}
			if (deferToTsmuxer && "bt.601".equals(media.getMatrixCoefficients())) {
				deferToTsmuxer = false;
				LOGGER.trace(prependTraceReason + "the colorspace probably isn't supported by the renderer.");
			}
			if (
				deferToTsmuxer && (
					params.mediaRenderer.isKeepAspectRatio() ||
					params.mediaRenderer.isKeepAspectRatioTranscoding()
				) &&
				!ASPECT_16_9.equals(media.getAspectRatioContainer())
			) {
				deferToTsmuxer = false;
				LOGGER.trace(prependTraceReason + "the renderer needs us to add borders so it displays the correct aspect ratio of " + media.getAspectRatioContainer() + ".");
			}
			if (deferToTsmuxer && !params.mediaRenderer.isResolutionCompatibleWithRenderer(media.getWidth(), media.getHeight())) {
				deferToTsmuxer = false;
				LOGGER.trace(prependTraceReason + "the resolution is incompatible with the renderer.");
			}
			if (deferToTsmuxer) {
				TsMuxeRVideo tv = (TsMuxeRVideo) PlayerFactory.getPlayer(StandardPlayerId.TSMUXER_VIDEO, false, true);
				params.forceFps = media.getValidFps(false);

				if (media.getCodecV() != null) {
					if (media.isH264()) {
						params.forceType = "V_MPEG4/ISO/AVC";
					} else if (media.getCodecV().startsWith("mpeg2")) {
						params.forceType = "V_MPEG-2";
					} else if (media.getCodecV().equals("vc1")) {
						params.forceType = "V_MS/VFW/WVC1";
					}
				}

				return tv.launchTranscode(dlna, media, params);
			}
		}

		// Apply any video filters and associated options. These should go
		// after video input is specified and before output streams are mapped.
		cmdList.addAll(getVideoFilterOptions(dlna, media, params));

		// Map the output streams if necessary
		if (media.getAudioTracksList().size() > 1) {
			// Set the video stream
			cmdList.add("-map");
			cmdList.add("0:v");

			// Set the proper audio stream
			cmdList.add("-map");
			cmdList.add("0:a:" + (media.getAudioTracksList().indexOf(params.aid)));
		}

		// Now configure the output streams

		// Encoder threads
		if (nThreads > 0) {
			cmdList.add("-threads");
			cmdList.add(String.valueOf(nThreads));
		}

		// Add the output options (-f, -c:a, -c:v, etc.)

		// Now that inputs and filtering are complete, see if we should
		// give the renderer the final say on the command
		boolean override = false;
		if (renderer instanceof RendererConfiguration.OutputOverride) {
			override = ((RendererConfiguration.OutputOverride)renderer).getOutputOptions(cmdList, dlna, this, params);
		}

		if (!override) {
			cmdList.addAll(getVideoBitrateOptions(dlna, media, params));

			String customFFmpegOptions = renderer.getCustomFFmpegOptions();

			// Audio channels, bitrate and sampling rate
			if (!ac3Remux && !dtsRemux && !(type() == FormatType.AUDIO)) {
				int channels = 0;
				if (
					(
						renderer.isTranscodeToWMV() &&
						!renderer.isXbox360()
					) ||
					(
						renderer.isXboxOne() &&
						purpose() == VIDEO_WEBSTREAM_PLAYER
					)
				) {
					channels = 2;
				} else if (params.aid != null && params.aid.getNumberOfChannels() > configuration.getAudioChannelCount()) {
					channels = configuration.getAudioChannelCount();
				}

				if (channels > 0 && !Pattern.compile("-ac\\b").matcher(customFFmpegOptions).find()) {
					cmdList.add("-ac");
					cmdList.add(String.valueOf(channels));
				}

				if (!Pattern.compile("-(?:ab|b:a|q:a|qscale:a)\\b").matcher(customFFmpegOptions).find()) {
					cmdList.add("-b:a");
					if (renderer.isTranscodeToAAC()) {
						cmdList.add(Math.min(configuration.getAudioBitrate(), 320) + "k");
					} else {
						cmdList.add(String.valueOf(CodecUtil.getAC3Bitrate(configuration, params.aid)) + "k");
					}
				}

				if (
					params.aid != null &&
					params.aid.getSampleFrequency() != params.mediaRenderer.getTranscodedVideoAudioSampleRate() &&
					!Pattern.compile("-ar\\b").matcher(customFFmpegOptions).find()
				) {
					cmdList.add("-ar");
					cmdList.add(String.valueOf(params.mediaRenderer.getTranscodedVideoAudioSampleRate()));
				}
			}

			// Add the output options (-f, -c:a, -c:v, etc.)
			cmdList.addAll(getVideoTranscodeOptions(dlna, media, params));

			// Add custom options
			if (StringUtils.isNotEmpty(customFFmpegOptions)) {
				parseOptions(customFFmpegOptions, cmdList);
			}
		}


		// Set up the process
		PipeProcess pipe = null;

		if (!dtsRemux) {
//			cmdList.add("pipe:");

			// basename of the named pipe:
			String fifoName = String.format(
				"ffmpegvideo_%d_%d",
				Thread.currentThread().getId(),
				System.currentTimeMillis()
			);

			// This process wraps the command that creates the named pipe
			pipe = new PipeProcess(fifoName);
			pipe.deleteLater(); // delete the named pipe later; harmless if it isn't created

			params.input_pipes[0] = pipe;

			// Output file
			cmdList.add(pipe.getInputPipe());
		}

		String[] cmdArray = new String[cmdList.size()];
		cmdList.toArray(cmdArray);

		ProcessWrapperImpl pw = new ProcessWrapperImpl(cmdArray, params);

		setOutputParsing(dlna, pw, false);

		if (!dtsRemux) {
			ProcessWrapper mkfifo_process = pipe.getPipeProcess();

			/**
			 * It can take a long time for Windows to create a named pipe (and
			 * mkfifo can be slow if /tmp isn't memory-mapped), so run this in
			 * the current thread.
			 */
			mkfifo_process.runInSameThread();
			pw.attachProcess(mkfifo_process); // Clean up the mkfifo process when the transcode ends

			// Give the mkfifo process a little time
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				LOGGER.error("Thread interrupted while waiting for named pipe to be created", e);
			}
		} else {
			pipe = new PipeProcess(System.currentTimeMillis() + "tsmuxerout.ts");

			TsMuxeRVideo ts = (TsMuxeRVideo) PlayerFactory.getPlayer(StandardPlayerId.TSMUXER_VIDEO, false, true);
			File f = new File(configuration.getTempFolder(), "dms-tsmuxer.meta");
			String cmd[] = new String[]{ ts.getExecutable(), f.getAbsolutePath(), pipe.getInputPipe() };
			pw = new ProcessWrapperImpl(cmd, params);

			PipeIPCProcess ffVideoPipe = new PipeIPCProcess(System.currentTimeMillis() + "ffmpegvideo", System.currentTimeMillis() + "videoout", false, true);

			cmdList.add(ffVideoPipe.getInputPipe());

			OutputParams ffparams = new OutputParams(configuration);
			ffparams.maxBufferSize = 1;
			ffparams.stdin = params.stdin;

			String[] cmdArrayDts = new String[cmdList.size()];
			cmdList.toArray(cmdArrayDts);

			ProcessWrapperImpl ffVideo = new ProcessWrapperImpl(cmdArrayDts, ffparams);

			ProcessWrapper ff_video_pipe_process = ffVideoPipe.getPipeProcess();
			pw.attachProcess(ff_video_pipe_process);
			ff_video_pipe_process.runInNewThread();
			ffVideoPipe.deleteLater();

			pw.attachProcess(ffVideo);
			ffVideo.runInNewThread();

			PipeIPCProcess ffAudioPipe = new PipeIPCProcess(System.currentTimeMillis() + "ffmpegaudio01", System.currentTimeMillis() + "audioout", false, true);
			StreamModifier sm = new StreamModifier();
			sm.setPcm(false);
			sm.setDtsEmbed(dtsRemux);
			sm.setSampleFrequency(48000);
			sm.setBitsPerSample(16);
			sm.setNbChannels(2);

			List<String> cmdListDTS = new ArrayList<>();
			cmdListDTS.add(getExecutable());
			cmdListDTS.add("-y");
			cmdListDTS.add("-ss");

			if (params.timeseek > 0) {
				cmdListDTS.add(String.valueOf(params.timeseek));
			} else {
				cmdListDTS.add("0");
			}

			if (params.stdin == null) {
				cmdListDTS.add("-i");
			} else {
				cmdListDTS.add("-");
			}
			cmdListDTS.add(filename);

			if (params.timeseek > 0) {
				cmdListDTS.add("-copypriorss");
				cmdListDTS.add("0");
				cmdListDTS.add("-avoid_negative_ts");
				cmdListDTS.add("1");
			}

			cmdListDTS.add("-ac");
			cmdListDTS.add("2");

			cmdListDTS.add("-f");
			cmdListDTS.add("dts");

			cmdListDTS.add("-c:a");
			cmdListDTS.add("copy");

			cmdListDTS.add(ffAudioPipe.getInputPipe());

			String[] cmdArrayDTS = new String[cmdListDTS.size()];
			cmdListDTS.toArray(cmdArrayDTS);

			if (!params.mediaRenderer.isMuxDTSToMpeg()) { // No need to use the PCM trick when media renderer supports DTS
				ffAudioPipe.setModifier(sm);
			}

			OutputParams ffaudioparams = new OutputParams(configuration);
			ffaudioparams.maxBufferSize = 1;
			ffaudioparams.stdin = params.stdin;
			ProcessWrapperImpl ffAudio = new ProcessWrapperImpl(cmdArrayDTS, ffaudioparams);

			params.stdin = null;
			try (PrintWriter pwMux = new PrintWriter(f)) {
				pwMux.println("MUXOPT --no-pcr-on-video-pid --no-asyncio --new-audio-pes --vbr --vbv-len=500");
				String videoType = "V_MPEG-2";

				if (renderer.isTranscodeToH264()) {
					videoType = "V_MPEG4/ISO/AVC";
				}

				if (params.no_videoencode && params.forceType != null) {
					videoType = params.forceType;
				}

				StringBuilder fps = new StringBuilder();
				fps.append("");
				if (params.forceFps != null) {
					fps.append("fps=").append(params.forceFps).append(", ");
				}

				String audioType = "A_AC3";
				if (dtsRemux) {
					if (params.mediaRenderer.isMuxDTSToMpeg()) {
						// Renderer can play proper DTS track
						audioType = "A_DTS";
					} else {
						// DTS padded in LPCM trick
						audioType = "A_LPCM";
					}
				}

				// XXX This is questionable, it's unclear of the codec is always H.264
				// and what the consequence of omitting the "level" parameter is
				VideoLevel level = params.mediaRenderer.getVideoLevelLimit(VideoCodec.H264);
				pwMux.println(
					videoType + ", \"" + ffVideoPipe.getOutputPipe() + "\", " + fps +
					(level != null ? "level=" + level.toString(false) + ", " : "") +
					"insertSEI, contSPS, track=1"
				);
				pwMux.println(audioType + ", \"" + ffAudioPipe.getOutputPipe() + "\", track=2");
			}

			ProcessWrapper pipe_process = pipe.getPipeProcess();
			pw.attachProcess(pipe_process);
			pipe_process.runInNewThread();

			try {
				wait(50);
			} catch (InterruptedException e) {
			}

			pipe.deleteLater();
			params.input_pipes[0] = pipe;

			ProcessWrapper ff_pipe_process = ffAudioPipe.getPipeProcess();
			pw.attachProcess(ff_pipe_process);
			ff_pipe_process.runInNewThread();

			try {
				wait(50);
			} catch (InterruptedException e) {
			}

			ffAudioPipe.deleteLater();
			pw.attachProcess(ffAudio);
			ffAudio.runInNewThread();
		}

		// Launch the transcode command...
		pw.runInNewThread();
		// ...and wait briefly to allow it to start
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			LOGGER.error("Thread interrupted while waiting for transcode to start");
			LOGGER.trace("", e);
		}
		configuration = prev;
		return pw;
	}

	private JCheckBox multithreading;
	private JCheckBox videoRemuxTsMuxer;
	private JCheckBox fc;
	private JCheckBox deferToMEncoderForSubtitles;

	@Override
	public JComponent getConfigurationPanel() {
		FormLayout layout = new FormLayout(
			"left:pref, 0:grow",
			"p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p"
		);
		FormBuilder builder = FormBuilder.create().layout(layout).border(Paddings.EMPTY).opaque(false);
		CellConstraints cc = new CellConstraints();

		multithreading = new JCheckBox(Messages.getString("MEncoderVideo.35"), configuration.isFfmpegMultithreading());
		multithreading.setContentAreaFilled(false);
		multithreading.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				configuration.setFfmpegMultithreading(e.getStateChange() == ItemEvent.SELECTED);
			}
		});
		builder.add(GuiUtil.getPreferredSizeComponent(multithreading)).at(cc.xy(2, 3));

		videoRemuxTsMuxer = new JCheckBox(Messages.getString("MEncoderVideo.38"), configuration.isFFmpegMuxWithTsMuxerWhenCompatible());
		videoRemuxTsMuxer.setContentAreaFilled(false);
		videoRemuxTsMuxer.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				configuration.setFFmpegMuxWithTsMuxerWhenCompatible(e.getStateChange() == ItemEvent.SELECTED);
			}
		});
		builder.add(GuiUtil.getPreferredSizeComponent(videoRemuxTsMuxer)).at(cc.xy(2, 5));

		fc = new JCheckBox(Messages.getString("FFmpeg.3"), configuration.isFFmpegFontConfig());
		fc.setContentAreaFilled(false);
		fc.setToolTipText(Messages.getString("FFmpeg.0"));
		fc.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				configuration.setFFmpegFontConfig(e.getStateChange() == ItemEvent.SELECTED);
			}
		});
		builder.add(GuiUtil.getPreferredSizeComponent(fc)).at(cc.xy(2, 7));

		deferToMEncoderForSubtitles = new JCheckBox(Messages.getString("FFmpeg.1"), configuration.isFFmpegDeferToMEncoderForProblematicSubtitles());
		deferToMEncoderForSubtitles.setContentAreaFilled(false);
		deferToMEncoderForSubtitles.setToolTipText(Messages.getString("FFmpeg.2"));
		deferToMEncoderForSubtitles.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				configuration.setFFmpegDeferToMEncoderForProblematicSubtitles(e.getStateChange() == ItemEvent.SELECTED);
			}
		});
		builder.add(GuiUtil.getPreferredSizeComponent(deferToMEncoderForSubtitles)).at(cc.xy(2, 9));

		return builder.getPanel();
	}

	/**
	 * A simple arg parser with basic quote comprehension
	 */
	protected static List<String> parseOptions(String str) {
		return str == null ? null : parseOptions(str, new ArrayList<String>());
	}

	protected static List<String> parseOptions(String str, List<String> cmdList) {
		int start, pos = 0, len = str.length();
		while (pos < len) {
			// New arg
			if (str.charAt(pos) == '\"') {
				start = pos + 1;
				// Find next quote. No support for escaped quotes here, and
				// -1 means no matching quote but be lax and accept the fragment anyway
				pos = str.indexOf('"', start);
			} else {
				start = pos;
				// Find next space
				pos = str.indexOf(' ', start);
			}
			if (pos == -1) {
				// We're done
				pos = len;
			}
			// Add the arg, if any
			if (pos - start > 0) {
				cmdList.add(str.substring(start, pos).trim());
			}
			pos++;
			// Advance to next non-space char
			while (pos < len && str.charAt(pos) == ' ') {
				pos++;
			}
		}
		return cmdList;
	}



	/**
	 * Collapse the multiple internal ways of saying "subtitles are disabled" into a single method
	 * which returns true if any of the following are true:
	 *
	 *     1) configuration.isDisableSubtitles()
	 *     2) params.sid == null
	 *     3) avisynth()
	 * @param params
	 * @return
	 */
	public boolean isDisableSubtitles(OutputParams params) {
		return configuration.isDisableSubtitles() || (params.sid == null) || avisynth();
	}

	@Override
	public boolean isCompatible(DLNAResource resource) {
		return resource.isVideo() && isContainerCompatible(resource) || resource.getFormat() instanceof PLAYLIST;
	}

	/**
	 * Checks the compatibility for FFmpeg itself. This is common for all
	 * "FFmpeg engines" and shouldn't have restrictions that only apply to one
	 * of them.
	 *
	 * @param resource the {@link DLNAResource} to check for compatibility.
	 * @return The result.
	 */
	@SuppressWarnings("null")
	protected boolean isContainerCompatible(@Nonnull DLNAResource resource) {
		DLNAMediaInfo media = resource.getMedia();
		String container = media == null ? null : media.getContainer();
		if (isBlank(container)) {
			return false;
		}
		switch (container) {
			case FormatConfiguration.MCF:
				return false;
			default:
				return true;
		}
	}

	// matches 'Duration: 00:17:17.00' but not 'Duration: N/A'
	static final Matcher reDuration = Pattern.compile("Duration:\\s+([\\d:.]+),").matcher("");

	/**
	 * Set up a filter to parse ffmpeg's stderr output for info
	 * (e.g. duration) if required.
	 */
	public void setOutputParsing(final DLNAResource dlna, ProcessWrapperImpl pw, boolean force) {
		if (configuration.isResumeEnabled() && dlna.getMedia() != null) {
			long duration = force ? 0 : (long) dlna.getMedia().getDurationInSeconds();
			if (duration == 0 || duration == DLNAMediaInfo.TRANS_SIZE) {
				OutputTextLogger ffParser = new OutputTextLogger(null) {
					@Override
					public boolean filter(String line) {
						if (reDuration.reset(line).find()) {
							String d = reDuration.group(1);
							LOGGER.trace("[{}] setting duration: {}", ID, d);
							dlna.getMedia().setDuration(StringUtil.convertStringToTime(d));
							return false; // done, stop filtering
						}
						return true; // keep filtering
					}
				};
				ffParser.setFiltered(true);
				pw.setStderrConsumer(ffParser);
			}
		}
	}

	private static void setSubtitlesResolution(String subtitles, int subtitlesWidth, int subtitlesHeight) throws IOException {
		BufferedReader input = new BufferedReader(new InputStreamReader(new FileInputStream(new File(subtitles))));
		String line;
		boolean resolved = false;
		while ((line = input.readLine()) != null) {
			if (line.contains("[Script Info]")) {
				while ((line = input.readLine()) != null) {
					if (isNotBlank(line)) {
						if (line.contains("PlayResX:")) {
							subtitlesWidth = Integer.parseInt(line.substring(9).trim());
						} else if (line.contains("PlayResY:")) {
							subtitlesHeight = Integer.parseInt(line.substring(9).trim());
						}
					} else {
						resolved = true;
						break;
					}
				}
			}
			if (resolved) {
				input.close();
				break;
			}
		}
	}

	@Override
	public boolean excludeFormat(Format extension) {
		return false;
	}

	@Override
	public boolean isPlayerCompatible(RendererConfiguration renderer) {
		return true;
	}

	@Override
	@Nullable
	public ExecutableInfo testExecutable(@Nonnull ExecutableInfo executableInfo) {
		executableInfo = testExecutableFile(executableInfo);
		if (Boolean.FALSE.equals(executableInfo.getAvailable())) {
			return executableInfo;
		}
		final String arg = "-version";
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
				result.available(Boolean.TRUE);
				if (result instanceof FFmpegExecutableInfoBuilder) {
					FFmpegExecutableInfoBuilder builder = (FFmpegExecutableInfoBuilder) result;
					FFmpegExecutableInfo.parseVersions(builder, output);

					FFmpegExecutableInfo.determineProtocols(builder);
					if (builder.protocols() == null || builder.protocols().size() == 0) {
						LOGGER.warn("Couldn't parse any supported protocols for \"{}\"", executableInfo.getPath());
					} else if (LOGGER.isDebugEnabled()) {
						LOGGER.debug(
							"{} supported protocols: {}",
							executableInfo.getPath(),
							FFmpegExecutableInfo.toProtocolsString(builder.protocols())
						);
					}

					FFmpegExecutableInfo.determineFormats(builder);
					if (builder.formats() == null || builder.formats().size() == 0) {
						LOGGER.warn("Couldn't parse any supported formats for \"{}\"", executableInfo.getPath());
					} else if (LOGGER.isDebugEnabled()) {
						LOGGER.debug(
							"{} supported formats: {}",
							executableInfo.getPath(),
							FFmpegExecutableInfo.toFormatsString(builder.formats())
						);
					}

					FFmpegExecutableInfo.determineCodecs(builder);
					if (builder.codecs() == null || builder.codecs().size() == 0) {
						LOGGER.warn("Couldn't parse any supported codecs for \"{}\"", executableInfo.getPath());
					} else if (LOGGER.isDebugEnabled()) {
						LOGGER.debug(
							"{} supported codecs: {}",
							executableInfo.getPath(),
							FFmpegExecutableInfo.toCodecsBuilderString(builder.codecs())
						);
					}

					FFmpegExecutableInfo.determineBitstreamFilters(builder);
					if (LOGGER.isDebugEnabled()) {
						if (builder.bitstreamFilters() == null || builder.bitstreamFilters().isEmpty()) {
							LOGGER.debug("No bitstream filters parsed for \"{}\"", executableInfo.getPath());
						} else {
							LOGGER.debug(
								"{} supported bitstream filters: {}",
								executableInfo.getPath(),
								FFmpegExecutableInfo.toBitstreamFiltersString(builder.bitstreamFilters())
							);
						}
					}
				} else {
					LOGGER.error("Could not determine FFmpeg details because of an internal error");
				}
			} else {
				NTStatus ntStatus = Platform.isWindows() ? NTStatus.typeOf(output.getExitCode()) : null;
				if (ntStatus != null) {
					result.errorType(ExecutableErrorType.GENERAL);
					result.errorText(String.format(Messages.getString("Engine.Error"), this) + "\n\n" + ntStatus);
				} else {
					if (output.getOutput().size() > 2) {
						result.errorType(ExecutableErrorType.GENERAL);
						result.errorText(
							String.format(Messages.getString("Engine.Error"), this) + " \n" +
							output.getOutput().get(output.getOutput().size() - 2) + " " +
							output.getOutput().get(output.getOutput().size() - 1)
						);
					} else if (output.getOutput().size() > 1) {
						result.errorType(ExecutableErrorType.GENERAL);
						result.errorText(
							String.format(Messages.getString("Engine.Error"), this) + " \n" +
							output.getOutput().get(output.getOutput().size() - 1)
						);
					} else {
						result.errorType(ExecutableErrorType.GENERAL);
						result.errorText(String.format(Messages.getString("Engine.Error"), this) + Messages.getString("General.3"));
					}
				}
				result.available(Boolean.FALSE);
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
