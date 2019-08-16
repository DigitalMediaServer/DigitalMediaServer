package net.pms.dlna;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.pms.configuration.FormatConfiguration;
import net.pms.configuration.RendererConfiguration;
import net.pms.dlna.DLNAMediaInfo.RateMode;
import net.pms.dlna.MediaInfo.StreamType;
import net.pms.exception.UnknownFormatException;
import net.pms.formats.FormatType;
import net.pms.formats.v2.SubtitleType;
import net.pms.image.ImageFormat;
import net.pms.image.ImagesUtil;
import net.pms.image.ImagesUtil.ScaleType;
import net.pms.media.AV1Level;
import net.pms.media.H262Level;
import net.pms.media.H263Level;
import net.pms.media.H264Level;
import net.pms.media.H265Level;
import net.pms.media.MPEG4VisualLevel;
import net.pms.media.VC1Level;
import net.pms.media.VP9Level;
import net.pms.media.VideoCodec;
import net.pms.util.FileUtil;
import net.pms.util.StringUtil;
import net.pms.util.Version;
import org.apache.commons.codec.binary.Base64;
import static org.apache.commons.lang3.StringUtils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LibMediaInfoParser {
	private static final Logger LOGGER = LoggerFactory.getLogger(LibMediaInfoParser.class);

	// Regular expression to parse a 4 digit year number from a string
	private static final String YEAR_REGEX = ".*([\\d]{4}).*";

	// Pattern to parse the year from a string
	private static final Pattern yearPattern = Pattern.compile(YEAR_REGEX);

	private static final Pattern intPattern = Pattern.compile("([\\+-]?\\d+)([eE][\\+-]?\\d+)?");
	private static final Pattern lastIntPattern = Pattern.compile("([\\+-]?\\d+)([eE][\\+-]?\\d+)?\\s*$");

	/**
	 * The {@link Pattern} used to extract profile and level from a simple
	 * {@code profile@level} notation.
	 * <p>
	 * Examples values:
	 * <ul>
	 * <li>{@code MP@LL}</li>
	 * <li>{@code Advanced@High}</li>
	 * </ul>

	 */
	private static final Pattern SIMPLE_PROFILE_LEVEL_PATTERN = Pattern.compile(
		"^\\s*([^@]*[^@\\s])?\\s*@?\\s*(.*\\S)?\\s*$"
	);

	/**
	 * The {@link Pattern} used to extract profile and numeric level from a simple
	 * {@code profile@level} notation.
	 * <p>
	 * Examples values:
	 * <ul>
	 * <li>{@code Advanced@L1}</li>
	 * <li>{@code Simple@L3.2}</li>
	 * <li>{@code Complex@L2}</li>
	 * </ul>
	 */
	private static final Pattern SIMPLE_PROFILE_NUMERIC_LEVEL_PATTERN = Pattern.compile(
		"^\\s*([^@]*[^@\\s])?\\s*@?\\s*(?:L|LEVEL)?\\s*(\\d+(?:\\.\\d+|,\\d+)?)?\\s*$", Pattern.CASE_INSENSITIVE
	);

	/**
	 * The {@link Pattern} used to extract a H.264 profile and level from a
	 * {@code profile@level} notation.
	 * <p>
	 * Examples values:
	 * <ul>
	 * <li>{@code High@L3.0 }</li>
	 * <li>{@code High@L4.0}</li>
	 * <li>{@code High@L4.1}</li>
	 * <li>{@code High@L5.2@High}</li>
	 * <li>{@code Stereo High@L4.1 / High@L4.1}</li>
	 * </ul>
	 */
	private static final Pattern H264_PROFILE_LEVEL_PATTERN = Pattern.compile(
		"^\\s*([^@]*[^@\\s])?\\s*@?\\s*(?:L|LEVEL)?\\s*([\\db]+(?:\\.\\d+|,\\d+)?)?\\s*(?:@\\S.*\\S)?\\s*(?:/|$)", Pattern.CASE_INSENSITIVE
	);

	/**
	 * The {@link Pattern} used to extract a H.265 profile and level from a
	 * {@code profile@level@sub-profile} notation.
	 * <p>
	 * Examples values:
	 * <ul>
	 * <li>{@code Main@L4.1@High}</li>
	 * <li>{@code Main 10@L5@High}</li>
	 * <li>{@code High@L5.1@High}</li>
	 * <li>{@code High@L5.2@High}</li>
	 * <li>{@code High@L6@High}</li>
	 * <li>{@code Main 10@L6.1@High}</li>
	 * <li>{@code High@L6.2@High}</li>
	 * </ul>
	 */
	private static final Pattern H265_PROFILE_LEVEL_PATTERN = Pattern.compile(
		"^\\s*([^@]*[^@\\s])?\\s*@?\\s*(?:L|LEVEL)?\\s*(\\d+(?:\\.\\d+|,\\d+)?)?\\s*(?:@\\S.*\\S)?\\s*(?:/|$)", Pattern.CASE_INSENSITIVE
	);

	private static MediaInfo MI;
	private static final Version VERSION;

	static {
		MI = new MediaInfo();

		if (MI.isValid()) {
			MI.Option("Internet", "No"); // avoid MediaInfoLib to try to connect to an Internet server for availability of newer software, anonymous statistics and retrieving information about a file
			MI.Option("Complete", "1");
			MI.Option("Language", "raw");
			MI.Option("File_TestContinuousFileNames", "0");
			Matcher matcher = Pattern.compile("MediaInfoLib[\\s-]*(\\S+)", Pattern.CASE_INSENSITIVE).matcher(MI.Option("Info_Version"));
			if (matcher.find() && isNotBlank(matcher.group(1))) {
				VERSION = new Version(matcher.group(1));
			} else {
				VERSION = null;
			}
			if (VERSION != null && VERSION.isGreaterThan(18, 5)) {
				MI.Option("LegacyStreamDisplay", "1");
				MI.Option("File_HighestFormat", "0");
				MI.Option("File_ChannelLayout", "1");
				MI.Option("Legacy", "1");
			}
			LOGGER.debug("Option 'File_TestContinuousFileNames' is set to: " + MI.Option("File_TestContinuousFileNames_Get"));
			MI.Option("ParseSpeed", "0");
			LOGGER.debug("Option 'ParseSpeed' is set to: " + MI.Option("ParseSpeed_Get"));
//			LOGGER.debug(MI.Option("Info_Parameters_CSV")); // It can be used to export all current MediaInfo parameters
		} else {
			VERSION = null;
		}
	}

	public static boolean isValid() {
		return MI.isValid();
	}

	/**
	 * @return The {@code LibMediaInfo} {@link Version} or {@code null} if
	 *         unknown.
	 */
	@Nullable
	public static Version getVersion() {
		return VERSION;
	}

	/**
	 * Parse media via MediaInfo.
	 */
	public synchronized static void parse(DLNAMediaInfo media, InputFile inputFile, FormatType type, RendererConfiguration renderer) {
		File file = inputFile.getFile();
		ParseLogger parseLogger = LOGGER.isTraceEnabled() ? new ParseLogger() : null;
		if (!media.isMediaparsed() && file != null && MI.isValid() && MI.Open(file.getAbsolutePath()) > 0) {
			DLNAMediaAudio currentAudioTrack = new DLNAMediaAudio();
			DLNAMediaSubtitle currentSubTrack;
			media.setSize(file.length());
			String value;

			// Set General
			getFormat(StreamType.General, media, currentAudioTrack, MI.Get(StreamType.General, 0, "Format"), file);
			getFormat(StreamType.General, media, currentAudioTrack, MI.Get(StreamType.General, 0, "CodecID").trim(), file);
			media.setDuration(parseDuration(MI.Get(StreamType.General, 0, "Duration")));
			media.setBitRate(parseBitRate(MI.Get(StreamType.General, 0, "OverallBitRate"), false));
			media.setBitRateMode(parseBitRateMode(MI.Get(StreamType.General, 0, "OverallBitRate_Mode")));
			media.setStereoscopy(MI.Get(StreamType.General, 0, "StereoscopicLayout"));
			value = MI.Get(StreamType.General, 0, "Cover_Data");
			if (isNotBlank(value)) {
				try {
					media.setThumb(DLNABinaryThumbnail.toThumbnail(
						new Base64().decode(value.getBytes(StandardCharsets.US_ASCII)),
						640,
						480,
						ScaleType.MAX,
						ImageFormat.SOURCE,
						false
					));
				} catch (EOFException e) {
					LOGGER.debug(
						"Error reading \"{}\" thumbnail from MediaInfo: Unexpected end of stream, probably corrupt or read error.",
						file.getName()
					);
				} catch (UnknownFormatException e) {
					LOGGER.debug("Could not read \"{}\" thumbnail from MediaInfo: {}", file.getName(), e.getMessage());
				} catch (IOException e) {
					LOGGER.error("Error reading \"{}\" thumbnail from MediaInfo: {}", file.getName(), e.getMessage());
					LOGGER.trace("", e);
				}
			}

			value = MI.Get(StreamType.General, 0, "Title");
			if (isNotBlank(value)) {
				media.setFileTitleFromMetadata(value);
			}

			if (parseLogger != null) {
				parseLogger.logGeneralColumns(file);
			}

			// set Video
			media.setVideoTrackCount(MI.Count_Get(StreamType.Video));
			if (media.getVideoTrackCount() > 0) {
				for (int i = 0; i < media.getVideoTrackCount(); i++) {
					// check for DXSA and DXSB subtitles (subs in video format)
					if (MI.Get(StreamType.Video, i, "Title").startsWith("Subtitle")) {
						currentSubTrack = new DLNAMediaSubtitle();
						// First attempt to detect subtitle track format
						currentSubTrack.setType(SubtitleType.valueOfLibMediaInfoCodec(MI.Get(StreamType.Video, i, "Format")));
						// Second attempt to detect subtitle track format (CodecID usually is more accurate)
						currentSubTrack.setType(SubtitleType.valueOfLibMediaInfoCodec(MI.Get(StreamType.Video, i, "CodecID")));
						currentSubTrack.setId(media.getSubtitleTracksList().size());
						addSub(currentSubTrack, media);
						if (parseLogger != null) {
							parseLogger.logSubtitleTrackColumns(i, true);
						}
					} else {
						getFormat(StreamType.Video, media, currentAudioTrack, MI.Get(StreamType.Video, i, "Format"), file);
						getFormat(StreamType.Video, media, currentAudioTrack, MI.Get(StreamType.Video, i, "Format_Version"), file);
						value = MI.Get(StreamType.Video, i, "Format_Profile");
						getFormat(StreamType.Video, media, currentAudioTrack, value, file);
						getFormat(StreamType.Video, media, currentAudioTrack, MI.Get(StreamType.Video, i, "CodecID"), file);
						media.setWidth(getPixelValue(MI.Get(StreamType.Video, i, "Width")));
						media.setHeight(getPixelValue(MI.Get(StreamType.Video, i, "Height")));
						media.setMatrixCoefficients(MI.Get(StreamType.Video, i, "matrix_coefficients"));
						if (!media.is3d()) {
							media.setStereoscopy(MI.Get(StreamType.Video, i, "MultiView_Layout"));
						}

						media.setPixelAspectRatio(MI.Get(StreamType.Video, i, "PixelAspectRatio"));
						media.setScanType(MI.Get(StreamType.Video, i, "ScanType"));
						media.setScanOrder(MI.Get(StreamType.Video, i, "ScanOrder"));
						media.setAspectRatioContainer(MI.Get(StreamType.Video, i, "DisplayAspectRatio/String"));
						media.setAspectRatioVideoTrack(MI.Get(StreamType.Video, i, "DisplayAspectRatio_Original/String"));
						media.setFrameRate(getFPSValue(MI.Get(StreamType.Video, i, "FrameRate")));
						media.setFrameRateOriginal(MI.Get(StreamType.Video, i, "FrameRate_Original"));
						media.setFrameRateMode(getFrameRateModeValue(MI.Get(StreamType.Video, i, "FrameRate_Mode")));
						media.setFrameRateModeRaw(MI.Get(StreamType.Video, i, "FrameRate_Mode"));
						media.setReferenceFrameCount(getReferenceFrameCount(MI.Get(StreamType.Video, i, "Format_Settings_RefFrames")));
						media.setVideoTrackTitleFromMetadata(MI.Get(StreamType.Video, i, "Title"));

						if (isNotBlank(value) && media.getCodecV() != null) {
							setVideoProfileAndLevel(media, value);
						}

						value = MI.Get(StreamType.Video, i, "Format_Settings_QPel");
						if (isNotBlank(value)) {
							media.putExtra(FormatConfiguration.MI_QPEL, value);
						}

						value = MI.Get(StreamType.Video, i, "Format_Settings_GMC");
						if (isNotBlank(value)) {
							media.putExtra(FormatConfiguration.MI_GMC, value);
						}

						value = MI.Get(StreamType.Video, i, "Format_Settings_GOP");
						if (isNotBlank(value)) {
							media.putExtra(FormatConfiguration.MI_GOP, value);
						}

						media.setMuxingMode(MI.Get(StreamType.Video, i, "MuxingMode"));
						if (!media.isEncrypted()) {
							media.setEncrypted("encrypted".equals(MI.Get(StreamType.Video, i, "Encryption")));
						}

						value = MI.Get(StreamType.Video, i, "BitDepth");
						if (isNotBlank(value)) {
							try {
								media.setVideoBitDepth(Integer.parseInt(value));
							} catch (NumberFormatException nfe) {
								LOGGER.debug("Could not parse bits per sample \"" + value + "\"");
							}
						}
						if (parseLogger != null) {
							parseLogger.logVideoTrackColumns(i);
						}
					}
				}
			}

			// set Audio
			int audioTracks = MI.Count_Get(StreamType.Audio);
			if (audioTracks > 0) {
				for (int i = 0; i < audioTracks; i++) {
					currentAudioTrack = new DLNAMediaAudio();
					getFormat(StreamType.Audio, media, currentAudioTrack, MI.Get(StreamType.Audio, i, "Format"), file);
					getFormat(StreamType.Audio, media, currentAudioTrack, MI.Get(StreamType.Audio, i, "Format_Version"), file);
					getFormat(StreamType.Audio, media, currentAudioTrack, MI.Get(StreamType.Audio, i, "Format_Profile"), file);
					getFormat(StreamType.Audio, media, currentAudioTrack, MI.Get(StreamType.Audio, i, "CodecID"), file);
					value = MI.Get(StreamType.Audio, i, "CodecID_Description");
					if (isNotBlank(value) && value.startsWith("Windows Media Audio 10")) {
						currentAudioTrack.setCodecA(FormatConfiguration.WMA10);
					}
					currentAudioTrack.setLang(getLang(MI.Get(StreamType.Audio, i, "Language/String")));
					currentAudioTrack.setAudioTrackTitleFromMetadata(MI.Get(StreamType.Audio, i, "Title").trim());
					currentAudioTrack.setNumberOfChannels(parseNumberOfChannels(MI.Get(StreamType.Audio, i, "Channel(s)_Original")));
					if (currentAudioTrack.isNumberOfChannelsUnknown()) {
						currentAudioTrack.setNumberOfChannels(parseNumberOfChannels(MI.Get(StreamType.Audio, i, "Channel(s)")));
					}
					currentAudioTrack.setDelay(parseDelay(MI.Get(StreamType.Audio, i, "Video_Delay")));
					currentAudioTrack.setSampleFrequency(parseSamplingRate(MI.Get(StreamType.Audio, i, "SamplingRate")));
					currentAudioTrack.setBitRate(parseBitRate(MI.Get(StreamType.Audio, i, "BitRate"), false));
					currentAudioTrack.setBitRateMode(parseBitRateMode(MI.Get(StreamType.Audio, i, "BitRate_Mode")));
					currentAudioTrack.setSongname(MI.Get(StreamType.General, 0, "Track"));

					if (
						renderer.isPrependTrackNumbers() &&
						currentAudioTrack.getTrack() > 0 &&
						currentAudioTrack.getSongname() != null &&
						currentAudioTrack.getSongname().length() > 0
					) {
						currentAudioTrack.setSongname(currentAudioTrack.getTrack() + ": " + currentAudioTrack.getSongname());
					}

					currentAudioTrack.setAlbum(MI.Get(StreamType.General, 0, "Album"));
					currentAudioTrack.setArtist(MI.Get(StreamType.General, 0, "Performer"));
					currentAudioTrack.setGenre(MI.Get(StreamType.General, 0, "Genre"));
					// Try to parse the year from the stored date
					String recordedDate = MI.Get(StreamType.General, 0, "Recorded_Date");
					Matcher matcher = yearPattern.matcher(recordedDate);
					if (matcher.matches()) {
						try {
							currentAudioTrack.setYear(Integer.parseInt(matcher.group(1)));
						} catch (NumberFormatException nfe) {
							LOGGER.debug("Could not parse year from recorded date \"" + recordedDate + "\"");
						}
					}

					// Special check for OGM: MediaInfo reports specific Audio/Subs IDs (0xn) while mencoder does not
					value = MI.Get(StreamType.Audio, i, "ID/String");
					if (isNotBlank(value)) {
						if (value.contains("(0x") && !FormatConfiguration.OGG.equals(media.getContainer())) {
							currentAudioTrack.setId(getSpecificID(value));
						} else {
							currentAudioTrack.setId(media.getAudioTracksList().size());
						}
					}

					value = MI.Get(StreamType.General, i, "Track/Position");
					if (isNotBlank(value)) {
						try {
							currentAudioTrack.setTrack(Integer.parseInt(value));
						} catch (NumberFormatException nfe) {
							LOGGER.debug("Could not parse track \"" + value + "\"");
						}
					}

					value = MI.Get(StreamType.Audio, i, "BitDepth");
					if (isNotBlank(value)) {
						currentAudioTrack.setBitsPerSample(parseBitsperSample(value));
					}

					addAudio(currentAudioTrack, media);
					if (parseLogger != null) {
						parseLogger.logAudioTrackColumns(i);
					}
				}
			}

			// set Image
			media.setImageCount(MI.Count_Get(StreamType.Image));
			if (media.getImageCount() > 0 || type == FormatType.IMAGE) {
				boolean parseByMediainfo = false;
				// For images use our own parser instead of MediaInfo which doesn't provide enough information
				try {
					ImagesUtil.parseImage(file, media);
					// This is a little hack. MediaInfo only recognizes a few image formats
					// so that MI.Count_Get(image) might return 0 even if there is an image.
					if (media.getImageCount() == 0) {
						media.setImageCount(1);
					}
				} catch (IOException e) {
					if (media.getImageCount() > 0) {
						LOGGER.debug("Error parsing image ({}), switching to MediaInfo: {}", file.getAbsolutePath(), e.getMessage());
						LOGGER.trace("", e);
						parseByMediainfo = true;
					} else {
						LOGGER.warn("Image parsing for \"{}\" failed both with MediaInfo and internally: {}", file.getAbsolutePath(), e.getMessage());
						LOGGER.trace("", e);
						media.setImageCount(1);
					}
				}

				if (parseByMediainfo) {
					getFormat(StreamType.Image, media, currentAudioTrack, MI.Get(StreamType.Image, 0, "Format"), file);
					media.setWidth(getPixelValue(MI.Get(StreamType.Image, 0, "Width")));
					media.setHeight(getPixelValue(MI.Get(StreamType.Image, 0, "Height")));
				}
				if (parseLogger != null) {
					parseLogger.logImageColumns(0);
				}
			}

			// set Subs in text format
			int subTracks = MI.Count_Get(StreamType.Text);
			if (subTracks > 0) {
				for (int i = 0; i < subTracks; i++) {
					currentSubTrack = new DLNAMediaSubtitle();
					currentSubTrack.setType(SubtitleType.valueOfLibMediaInfoCodec(MI.Get(StreamType.Text, i, "Format")));
					currentSubTrack.setType(SubtitleType.valueOfLibMediaInfoCodec(MI.Get(StreamType.Text, i, "CodecID")));
					currentSubTrack.setLang(getLang(MI.Get(StreamType.Text, i, "Language/String")));
					currentSubTrack.setSubtitlesTrackTitleFromMetadata((MI.Get(StreamType.Text, i, "Title")).trim());
					// Special check for OGM: MediaInfo reports specific Audio/Subs IDs (0xn) while mencoder does not
					value = MI.Get(StreamType.Text, i, "ID/String");
					if (isNotBlank(value)) {
						if (value.contains("(0x") && !FormatConfiguration.OGG.equals(media.getContainer())) {
							currentSubTrack.setId(getSpecificID(value));
						} else {
							currentSubTrack.setId(media.getSubtitleTracksList().size());
						}
					}

					addSub(currentSubTrack, media);
					if (parseLogger != null) {
						parseLogger.logSubtitleTrackColumns(i, false);
					}
				}
			}

			/*
			 * Some container formats (like MP4/M4A) can represent both audio
			 * and video media. DMS initially recognized this as video, but this
			 * is corrected here if the content is only audio.
			 */
			if (media.isAudioOrVideoContainer() && media.isAudio()) {
				media.setContainer(media.getAudioVariantFormatConfigurationString());
			}

			// Separate ASF from WMV
			if (FormatConfiguration.WMV.equals(media.getContainer())) {
				if (
					media.getCodecV() != null &&
					!media.getCodecV().equals(FormatConfiguration.WMV) &&
					!media.getCodecV().equals(FormatConfiguration.VC1)
				) {
					media.setContainer(FormatConfiguration.ASF);
				} else {
					for (DLNAMediaAudio audioTrack : media.getAudioTracksList()) {
						if (
							audioTrack.getCodecA() != null &&
							!audioTrack.getCodecA().equals(FormatConfiguration.WMA) &&
							!audioTrack.getCodecA().equals(FormatConfiguration.WMAPRO) &&
							!audioTrack.getCodecA().equals(FormatConfiguration.WMALOSSLESS) &&
							!audioTrack.getCodecA().equals(FormatConfiguration.WMAVOICE) &&
							!audioTrack.getCodecA().equals(FormatConfiguration.WMA10) &&
							!audioTrack.getCodecA().equals(FormatConfiguration.MP3) // up to 128 kbit/s only (WMVSPML_MP3 profile)
						) {
							media.setContainer(FormatConfiguration.ASF);
							break;
						}
					}
				}
			}

			/*
			 * Recognize 3D layout from the filename.
			 *
			 * First we check for our custom naming convention, for which the filename
			 * either has to start with "3DSBSLF" or "3DSBSRF" for side-by-side layout
			 * or "3DOULF" or "3DOURF" for over-under layout.
			 * For anaglyph 3D video can be used following combination:
			 * 		3DARCG 	anaglyph_red_cyan_gray
			 *		3DARCH 	anaglyph_red_cyan_half_color
			 *		3DARCC 	anaglyph_red_cyan_color
			 *		3DARCD 	anaglyph_red_cyan_dubois
			 *		3DAGMG 	anaglyph_green_magenta_gray
			 *		3DAGMH 	anaglyph_green_magenta_half_color
			 *		3DAGMC 	anaglyph_green_magenta_color
			 *		3DAGMD 	anaglyph_green_magenta_dubois
			 *		3DAYBG 	anaglyph_yellow_blue_gray
			 *		3DAYBH 	anaglyph_yellow_blue_half_color
			 *		3DAYBC 	anaglyph_yellow_blue_color
			 *		3DAYBD 	anaglyph_yellow_blue_dubois
			 *
			 * Next we check for common naming conventions.
			 */
			if (!media.is3d()) {
				String upperCaseFileName = file.getName().toUpperCase();
				if (upperCaseFileName.startsWith("3DSBS")) {
					LOGGER.debug("3D format SBS detected for " + file.getName());
					media.setStereoscopy(file.getName().substring(2, 7));
				} else if (upperCaseFileName.startsWith("3DOU")) {
					LOGGER.debug("3D format OU detected for " + file.getName());
					media.setStereoscopy(file.getName().substring(2, 6));
				} else if (upperCaseFileName.startsWith("3DA")) {
					LOGGER.debug("3D format Anaglyph detected for " + file.getName());
					media.setStereoscopy(file.getName().substring(2, 6));
				} else if (upperCaseFileName.matches(".*[\\s\\.](H-|H|HALF-|HALF.)SBS[\\s\\.].*")) {
					LOGGER.debug("3D format HSBS detected for " + file.getName());
					media.setStereoscopy("half side by side (left eye first)");
				} else if (upperCaseFileName.matches(".*[\\s\\.](H-|H|HALF-|HALF.)(OU|TB)[\\s\\.].*")) {
					LOGGER.debug("3D format HOU detected for " + file.getName());
					media.setStereoscopy("half top-bottom (left eye first)");
				} else if (upperCaseFileName.matches(".*[\\s\\.]SBS[\\s\\.].*")) {
					if (media.getWidth() > 1920) {
						LOGGER.debug("3D format SBS detected for " + file.getName());
						media.setStereoscopy("side by side (left eye first)");
					} else {
						LOGGER.debug("3D format HSBS detected based on width for " + file.getName());
						media.setStereoscopy("half side by side (left eye first)");
					}
				} else if (upperCaseFileName.matches(".*[\\s\\.](OU|TB)[\\s\\.].*")) {
					if (media.getHeight() > 1080) {
						LOGGER.debug("3D format OU detected for " + file.getName());
						media.setStereoscopy("top-bottom (left eye first)");
					} else {
						LOGGER.debug("3D format HOU detected based on height for " + file.getName());
						media.setStereoscopy("half top-bottom (left eye first)");
					}
				}
			}

			media.postParse(inputFile);

			if (parseLogger != null) {
				LOGGER.trace("{}", parseLogger);
			}

			MI.Close();
			if (media.getContainer() == null) {
				media.setContainer(DLNAMediaLang.UND);
			}

			if (media.getCodecV() == null) {
				media.setCodecV(DLNAMediaLang.UND);
			}

			media.setMediaparsed(true);
		}
	}

	public static void addAudio(DLNAMediaAudio currentAudioTrack, DLNAMediaInfo media) {
		if (isBlank(currentAudioTrack.getLang())) {
			currentAudioTrack.setLang(DLNAMediaLang.UND);
		}

		if (isBlank(currentAudioTrack.getCodecA())) {
			currentAudioTrack.setCodecA(DLNAMediaLang.UND);
		}

		media.getAudioTracksList().add(currentAudioTrack);
	}

	public static void addSub(DLNAMediaSubtitle currentSubTrack, DLNAMediaInfo media) {
		if (currentSubTrack.getType() == SubtitleType.UNSUPPORTED) {
			return;
		}

		if (isBlank(currentSubTrack.getLang())) {
			currentSubTrack.setLang(DLNAMediaLang.UND);
		}

		media.getSubtitleTracksList().add(currentSubTrack);
	}

	/**
	 * Sets the correct information in media.setContainer(), media.setCodecV()
	 * or media.setCodecA, depending on streamType.
	 *
	 * Note: A lot of these are types of MPEG-4 Audio. A good resource to make
	 * sense of this is: <a href=
	 * "https://en.wikipedia.org/wiki/MPEG-4_Part_3#MPEG-4_Audio_Object_Types"
	 * >MPEG-4 Audio Object Types</a>
	 * <p>
	 * There are also free samples of most of them at <a
	 * href="http://fileformats.archiveteam.org/wiki/MPEG-4_SLS"
	 * >fileformats.archiveteam.org</a> and <a href=
	 * "ftp://mpaudconf:adif2mp4@ftp.iis.fhg.de/mpeg4audio-conformance/compressedMp4/"
	 * >ftp.iis.fhg.de</a>
	 *
	 * @param streamType
	 * @param media
	 * @param audio
	 * @param value
	 * @param file
	 *
	 * TODO: Refactor this pain of a method
	 */
	private static void getFormat(
		StreamType streamType,
		DLNAMediaInfo media,
		DLNAMediaAudio audio,
		String value,
		File file
	) {
		if (isBlank(value) || media == null) {
			return;
		}

		value = value.trim().toLowerCase(Locale.ROOT);
		String format = null;

		if (isBlank(value)) {
			return;
		} else if (value.startsWith("3g2")) {
			format = FormatConfiguration.THREEGPP2;
		} else if (value.startsWith("3gp")) {
			format = FormatConfiguration.THREEGPP;
		} else if (value.startsWith("matroska")) {
			format = FormatConfiguration.MKV;
		} else if (value.equals("avi") || value.equals("opendml")) {
			format = FormatConfiguration.AVI;
		} else if (value.startsWith("cinepa")) {
			format = FormatConfiguration.CINEPAK;
		} else if (value.startsWith("flash")) {
			format = FormatConfiguration.FLV;
		} else if (value.equals("webm")) {
			format = FormatConfiguration.WEBM;
		} else if (value.equals("mxf")) {
			format = FormatConfiguration.MXF;
		} else if (value.equals("qt") || value.equals("quicktime")) {
			format = FormatConfiguration.MOV;
		} else if ((
				value.contains("isom") ||
				streamType != StreamType.Audio && value.startsWith("mp4") && !value.startsWith("mp4a") ||
				value.equals("20") ||
				value.equals("isml") ||
				(value.startsWith("m4a") && !value.startsWith("m4ae")) ||
				value.startsWith("m4v") ||
				value.equals("mpeg-4 visual") ||
				value.equals("xavc")
			) &&
			!FormatConfiguration.MPEG4ASP.equals(media.getCodecV()) &&
			!FormatConfiguration.MPEG4SP.equals(media.getCodecV())
		) {
			format = FormatConfiguration.MP4;
		} else if (
			FormatConfiguration.MP4.equals(media.getCodecV()) &&
			value.startsWith("simple@l")
		) {
			media.setCodecV(FormatConfiguration.MPEG4SP);
		} else if (
			FormatConfiguration.MP4.equals(media.getCodecV()) &&
			value.startsWith("advanced simple@l")
		) {
			media.setCodecV(FormatConfiguration.MPEG4ASP);
		} else if (value.contains("mpeg-ps")) {
			format = FormatConfiguration.MPEGPS;
		} else if (value.contains("mpeg-ts") || value.equals("bdav")) {
			format = FormatConfiguration.MPEGTS;
		} else if (value.equals("mjp2")) {
			format = FormatConfiguration.MJP2;
		} else if (value.equals("caf")) {
			format = FormatConfiguration.CAF;
		} else if (value.contains("aiff")) {
			format = FormatConfiguration.AIFF;
		} else if (FormatConfiguration.AIFF.equals(media.getContainer())) {
			// Due to this bug: https://github.com/MediaArea/MediaInfoLib/issues/833
			if (!value.equals("pcm") && !value.startsWith("big") && !value.startsWith("little")) {
				media.setContainer(FormatConfiguration.AIFC);
				format = FormatConfiguration.ADPCM;
			} else {
				format = FormatConfiguration.LPCM;
			}
		} else if (value.contains("ogg")) {
			format = FormatConfiguration.OGG;
		} else if (value.contains("opus")) {
			format = FormatConfiguration.OPUS;
		} else if (value.contains("realmedia") || value.startsWith("rv")) {
			format = FormatConfiguration.RM;
		} else if (value.startsWith("theora")) {
			format = FormatConfiguration.THEORA;
		} else if (
			value.startsWith("windows media") ||
			value.equals("wmv1") ||
			value.equals("wmv2")
		) {
			format = FormatConfiguration.WMV;
		} else if (
			value.startsWith("dvr") &&
			media.getContainer().equals(FormatConfiguration.ASF)
		) {
			media.setContainer(FormatConfiguration.DVRMS);
		} else if (
			streamType == StreamType.Video && (
				value.contains("mjpg") ||
				value.contains("mjpeg") ||
				value.equals("mjpa") ||
				value.equals("mjpb") ||
				value.equals("jpeg") ||
				value.equals("jpeg2000")
			)
		) {
			format = FormatConfiguration.MJPEG;
		} else if (value.equals("h261")) {
			format = FormatConfiguration.H261;
		} else if (
			value.equals("h263") ||
			value.equals("s263") ||
			value.equals("u263")
		) {
			format = FormatConfiguration.H263;
		} else if (value.startsWith("avc") || value.startsWith("h264")) {
			format = FormatConfiguration.H264;
		} else if (value.startsWith("hevc")) {
			format = FormatConfiguration.H265;
		} else if (value.startsWith("sorenson")) {
			format = FormatConfiguration.SORENSON;
		} else if (value.startsWith("vp6")) {
			format = FormatConfiguration.VP6;
		} else if (value.startsWith("vp7")) {
			format = FormatConfiguration.VP7;
		} else if (value.startsWith("vp8")) {
			format = FormatConfiguration.VP8;
		} else if (value.startsWith("vp9")) {
			format = FormatConfiguration.VP9;
		} else if (value.startsWith("av1")) {
			format = FormatConfiguration.AV1;
		} else if (
			(
				value.startsWith("div") ||
				value.equals("dx50") ||
				value.equals("dvx1")
			) && (
				!FormatConfiguration.MPEG4ASP.equals(media.getCodecV()) &&
				!FormatConfiguration.MPEG4SP.equals(media.getCodecV())
			)
		) {
			format = FormatConfiguration.DIVX;
		} else if (value.startsWith("indeo")) { // Intel Indeo Video: IV31, IV32, IV41 and IV50
			format = FormatConfiguration.INDEO;
		} else if (streamType == StreamType.Video && value.equals("yuv")) {
			format = FormatConfiguration.YUV;
		} else if (streamType == StreamType.Video && (value.equals("rgb") || value.equals("rgba"))) {
			format = FormatConfiguration.RGB;
		} else if (streamType == StreamType.Video && value.equals("rle")) {
			format = FormatConfiguration.RLE;
		} else if (value.equals("mac3")) {
			format = FormatConfiguration.MACE3;
		} else if (value.equals("mac6")) {
			format = FormatConfiguration.MACE6;
		} else if (streamType == StreamType.Video && value.equals("tga")) {
			format = FormatConfiguration.TGA;
		} else if (value.equals("ffv1")) {
			format = FormatConfiguration.FFV1;
		} else if (value.equals("celp")) {
			format = FormatConfiguration.CELP;
		} else if (value.equals("qcelp")) {
			format = FormatConfiguration.QCELP;
		} else if (value.equals("suds")) {
			format = FormatConfiguration.SUDS;
		} else if (value.matches("(?i)(dv)|(cdv.?)|(dc25)|(dcap)|(dvc.?)|(dvs.?)|(dvrs)|(dv25)|(dv50)|(dvan)|(dvh.?)|(dvis)|(dvl.?)|(dvnm)|(dvp.?)|(mdvf)|(pdvc)|(r411)|(r420)|(sdcc)|(sl25)|(sl50)|(sldv)")) {
			format = FormatConfiguration.DV;
		} else if (value.contains("mpeg video")) {
			format = FormatConfiguration.MPEG2;
		} else if (value.startsWith("version 1")) {
			if (
				FormatConfiguration.MPEG2.equals(media.getCodecV()) &&
				audio.getCodecA() == null
			) {
				format = FormatConfiguration.MPEG1;
			}
		} else if (
			value.equals("vc-1") ||
			value.equals("wvc1") ||
			value.equals("wmv3") ||
			value.equals("wmvp") ||
			value.equals("wmva")
		) {
			format = FormatConfiguration.VC1;
		} else if (value.equals("vc-3") || value.startsWith("dnxhd")) {
			format = FormatConfiguration.VC3;
		} else if (value.startsWith("apr") || value.startsWith("prores")) {
			format = FormatConfiguration.PRORES;
		} else if (value.equals("au") || value.equals("uLaw/AU Audio File")) {
			format = FormatConfiguration.AU;
		} else if (value.equals("layer 3")) {
			if (audio.getCodecA() != null && audio.getCodecA().equals(FormatConfiguration.MPA)) {
				format = FormatConfiguration.MP3;
				// special case:
				if (FormatConfiguration.MPA.equals(media.getContainer())) {
					media.setContainer(FormatConfiguration.MP3);
				}
			}
		} else if (
			value.equals("layer 2") &&
			audio.getCodecA() != null &&
			media.getContainer() != null &&
			audio.getCodecA().equals(FormatConfiguration.MPA) &&
			media.getContainer().equals(FormatConfiguration.MPA)
		) {
			// only for audio files:
			format = FormatConfiguration.MP2;
			media.setContainer(FormatConfiguration.MP2);
		} else if (value.equals("dts")) {
				format = FormatConfiguration.DTS;
		} else if (
				value.equals("ma") ||
				value.equals("ma / core") ||
				value.equals("ma / es matrix / core") ||
				value.equals("ma / es discrete / core") ||
				value.equals("x / ma / core") ||
				value.equals("hra / core") ||
				value.equals("hra / es matrix / core") ||
				value.equals("hra / es discrete / core") ||
				value.equals("134")
			) {
			format = FormatConfiguration.DTSHD;
		} else if (value.equals("vorbis") || value.equals("a_vorbis")) {
			format = FormatConfiguration.VORBIS;
		} else if (value.equals("adts")) {
			format = FormatConfiguration.ADTS;
		} else if (value.startsWith("amr")) {
			format = FormatConfiguration.AMR;
		} else if (value.equals("dolby e")) {
			format = FormatConfiguration.DOLBYE;
		} else if (
			value.equals("ac-3") ||
			value.equals("a_ac3") ||
			value.equals("2000")
		) {
			format = FormatConfiguration.AC3;
		} else if (value.startsWith("e-ac-3") && !value.startsWith("e-ac-3 joc")) {
			format = FormatConfiguration.EAC3;
		} else if (value.equals("mlp")) {
			format = FormatConfiguration.MLP;
		} else if (value.contains("truehd") || value.contains("mlp fba") && !value.contains("mlp fba 16-ch")) {
			format = FormatConfiguration.TRUEHD;

		/*
		 * XXX: Atmos is disabled until multi-level parsing is implemented,
		 * because it's more useful to know the underlying codec.
		 */

		//} else if (value.contains("atmos") || value.startsWith("e-ac-3 joc") || value.contains("mlp fba 16-ch") || value.equals("131")) {
		//	format = FormatConfiguration.ATMOS;

		} else if (value.startsWith("cook")) {
			format = FormatConfiguration.COOK;
		} else if (value.startsWith("qdesign")) {
			format = FormatConfiguration.QDESIGN;
		} else if (value.equals("realaudio lossless")) {
			format = FormatConfiguration.RALF;
		} else if (value.equals("tta")) {
			format = FormatConfiguration.TTA;
		} else if (value.equals("55") || value.equals("a_mpeg/l3")) {
			format = FormatConfiguration.MP3;
		} else if (
			value.equals("lc") ||
			value.equals("aac lc") ||
			value.equals("00001000-0000-FF00-8000-00AA00389B71") ||
			(
				value.equals("aac") &&
				FormatConfiguration.AVI.equals(media.getContainer())
			)
		) {
			// mp4a-40-2, enca-67-2
			format = FormatConfiguration.AAC_LC; // v2 and v4
		} else if (value.equals("ltp") || value.equals("aac ltp")) {
			format = FormatConfiguration.AAC_LTP;
		} else if (value.contains("he-aac") || value.contains("he-aacv2") || value.equals("aac lc sbr") || value.equals("aac lc sbr ps")) {
			format = FormatConfiguration.HE_AAC; // v1 and v2
		} else if (
			value.equals("er bsac") ||
			value.equals("mp4a-40-22")
		) {
			format = FormatConfiguration.ER_BSAC;
		} else if (value.equals("main") || value.equals("aac main")) {
			format = FormatConfiguration.AAC_MAIN;
		} else if (value.equals("ssr") || value.equals("aac ssr")) {
			format = FormatConfiguration.AAC_SSR;
		} else if (value.startsWith("a_aac/")) {
			if (value.equals("a_aac/mpeg2/main")) {
				format = FormatConfiguration.AAC_MAIN;
			} else if (value.equals("a_aac/mpeg2/lc")) {
				format = FormatConfiguration.AAC_LC;
			} else if (value.equals("a_aac/mpeg2/lc/sbr")) {
				format = FormatConfiguration.HE_AAC;
			} else if (value.equals("a_aac/mpeg2/ssr")) {
				format = FormatConfiguration.AAC_SSR;
			} else if (value.equals("a_aac/mpeg4/main")) {
				format = FormatConfiguration.AAC_MAIN;
			} else if (value.equals("a_aac/mpeg4/lc")) {
				format = FormatConfiguration.AAC_LC;
			} else if (value.equals("a_aac/mpeg4/lc/sbr")) {
				format = FormatConfiguration.HE_AAC;
			} else if (value.equals("a_aac/mpeg4/lc/sbr/ps")) { // HE-AACv2
				format = FormatConfiguration.HE_AAC;
			} else if (value.equals("a_aac/mpeg4/ssr")) {
				format = FormatConfiguration.AAC_SSR;
			} else if (value.equals("a_aac/mpeg4/ltp")) {
				format = FormatConfiguration.AAC_LTP;
			} else {
				format = FormatConfiguration.AAC_MAIN;
			}
		} else if (value.startsWith("adpcm")) {
			format = FormatConfiguration.ADPCM;
		} else if (value.equals("pcm")) {
			format = FormatConfiguration.LPCM;
		} else if (value.equals("alac")) {
			format = FormatConfiguration.ALAC;
		} else if (value.equals("als")) {
			format = FormatConfiguration.ALS;
		} else if (value.equals("wave")) {
			format = FormatConfiguration.WAV;
		} else if (value.equals("shorten")) {
			format = FormatConfiguration.SHORTEN;
		} else if (value.equals("sls") || value.equals("SLS non-core")) {
			// m4ae-40-37 until this MediaInfo bug get fixed, or mp4a-40-38 for non-core
			format = FormatConfiguration.SLS;
		} else if (value.equals("acelp")) {
			format = FormatConfiguration.ACELP;
		} else if (value.equals("g.729") || value.equals("g.729a")) {
			format = FormatConfiguration.G729;
		} else if (value.equals("vselp")) {
			format = FormatConfiguration.REALAUDIO_14_4;
		} else if (value.equals("g.728")) {
			format = FormatConfiguration.REALAUDIO_28_8;
		} else if (value.equals("a_real/sipr") || value.equals("kevin")) {
			format = FormatConfiguration.SIPRO;
		} else if (
			(
				value.equals("dts") ||
				value.equals("a_dts") ||
				value.equals("8")
			) &&
			(
				audio.getCodecA() == null ||
				!audio.getCodecA().equals(FormatConfiguration.DTSHD)
			)
		) {
			format = FormatConfiguration.DTS;
		} else if (value.equals("mpeg audio")) {
			format = FormatConfiguration.MPA;
		} else if (value.equals("wma")) {
			format = FormatConfiguration.WMA;
			if (media.getCodecV() == null) {
				media.setContainer(format);
			}
		} else if (
			streamType == StreamType.Audio &&
			(
				FormatConfiguration.WMA.equals(media.getContainer()) ||
				FormatConfiguration.WMV.equals(media.getContainer())
			)
		) {
			if (value.equals("160") || value.equals("161")) {
				format = FormatConfiguration.WMA;
			} else if (value.equals("162")) {
				format = FormatConfiguration.WMAPRO;
			} else if (value.equals("163")) {
				format = FormatConfiguration.WMALOSSLESS;
			} else if (value.equalsIgnoreCase("A")) {
				format = FormatConfiguration.WMAVOICE;
			} else if (value.equals("wma10")) {
				format = FormatConfiguration.WMA10;
			}
		} else if (value.equals("flac") || "19d".equals(value)) { // https://github.com/MediaArea/MediaInfoLib/issues/594
			format = FormatConfiguration.FLAC;
		} else if (value.equals("monkey's audio")) {
			format = FormatConfiguration.MONKEYS_AUDIO;
		} else if (value.contains("musepack")) {
			format = FormatConfiguration.MPC;
		} else if (value.contains("wavpack")) {
			format = FormatConfiguration.WAVPACK;
		} else if (value.equals("openmg")) {
			format = FormatConfiguration.ATRAC;
		} else if (value.startsWith("atrac") || value.endsWith("-a119-fffa01e4ce62") || value.endsWith("-88fc-61654f8c836c")) {
			format = FormatConfiguration.ATRAC;
			if (streamType == StreamType.Audio && !FormatConfiguration.ATRAC.equals(media.getContainer())) {
				media.setContainer(FormatConfiguration.ATRAC);
			}
		} else if (value.equals("nellymoser")) {
			format = FormatConfiguration.NELLYMOSER;
		} else if (value.equals("jpeg")) {
			format = FormatConfiguration.JPG;
		} else if (value.equals("png")) {
			format = FormatConfiguration.PNG;
		} else if (value.equals("gif")) {
			format = FormatConfiguration.GIF;
		} else if (value.equals("bitmap")) {
			format = FormatConfiguration.BMP;
		} else if (value.equals("tiff")) {
			format = FormatConfiguration.TIFF;
		}

		if (format != null) {
			if (streamType == StreamType.General) {
				media.setContainer(format);
			} else if (streamType == StreamType.Video) {
				media.setCodecV(format);
			} else if (streamType == StreamType.Audio) {
				audio.setCodecA(format);
			}
		// format not found so set container type based on the file extension. It will be overwritten when the correct type will be found
		} else if (streamType == StreamType.General && media.getContainer() == null) {
			media.setContainer(FileUtil.getExtension(file.getAbsolutePath()).toLowerCase(Locale.ROOT));
		}
	}

	public static int getPixelValue(String value) {
		if (isBlank(value)) {
			return 0;
		}
		if (value.contains("pixel")) {
			value = value.substring(0, value.indexOf("pixel"));
		}

		value = value.trim();

		// Value can look like "512 / 512" at this point
		if (value.contains("/")) {
			value = value.substring(0, value.indexOf('/')).trim();
		}

		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			LOGGER.debug("Could not parse pixels \"{}\": {}", value, e.getMessage());
			LOGGER.trace("", e);
			return 0;
		}
	}

	/**
	 * Parses the reference frame count.
	 *
	 * @param value the {@code Format_Settings_RefFrames} value to parse.
	 * @return The reference frame count or {@code -1} if the parsing fails.
	 */
	public static int getReferenceFrameCount(String value) {
		if (isBlank(value)) {
			return -1;
		}

		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			LOGGER.debug("Could not parse ReferenceFrameCount \"{}\": {}", value, e.getMessage());
			LOGGER.trace("", e);
			return -1;
		}
	}

	public static int getSpecificID(String value) {
		// If ID is given as 'streamID-substreamID' use the second (which is hopefully unique).
		// For example in vob audio ID can be '189 (0xBD)-32 (0x80)' and text ID '189 (0xBD)-128 (0x20)'
		int end = value.lastIndexOf("(0x");
		if (end > -1) {
			int start = value.lastIndexOf('-') + 1;
			value = value.substring(start > end ? 0 : start, end);
		}

		value = value.trim();
		int id = Integer.parseInt(value);
		return id;
	}

	protected static int parseNumberOfChannels(String value) {
		if (isBlank(value)) {
			return -1;
		}

		// Examples of libmediainfo  (mediainfo --Full --Language=raw file):
		// Channel(s) : 2
		// Channel(s) : 6
		// Channel(s) : 2 channels / 1 channel / 1 channel
		// The first value is used

		Matcher intMatcher = intPattern.matcher(value);
		if (intMatcher.find()) {
			String matchResult = intMatcher.group();
			try {
				return Integer.parseInt(matchResult);
			} catch (NumberFormatException ex) {
				LOGGER.warn("NumberFormatException while parsing substring \"{}\" from value \"{}\"", matchResult, value);
			}
		}

		LOGGER.warn("Can't parse the number of channels from \"{}\", returning -1", value);
		return -1;
	}

	protected static int parseBitsperSample(String value) {
		if (isBlank(value)) {
			return -1;
		}

		// examples of libmediainfo  (mediainfo --Full --Language=raw file):
		// Bit depth : 16
		// Bit depth : 24
		// Bit depth : / 24 / 24
		// The last value is used

		Matcher intMatcher = lastIntPattern.matcher(value);
		if (intMatcher.find()) {
			String matchResult = intMatcher.group();
			try {
				return Integer.parseInt(matchResult);
			} catch (NumberFormatException ex) {
				LOGGER.warn("NumberFormatException while parsing substring \"{}\" from value \"{}\"", matchResult, value);
			}
		}

		LOGGER.warn("Can't parse bits per sample from \"{}\", returning -1", value);
		return -1;
	}

	protected static int parseSamplingRate(String value) {
		if (isBlank(value)) {
			return -1;
		}

		// Examples of libmediainfo output (mediainfo --Full --Language=raw file):
		// SamplingRate : 48000
		// SamplingRate : 44100 / 22050
		// SamplingRate : 48000 / 48000 / 24000
		// The first value is used

		Matcher intMatcher = intPattern.matcher(value);
		if (intMatcher.find()) {
			String matchResult = intMatcher.group();
			try {
				return Integer.parseInt(matchResult);
			} catch (NumberFormatException ex) {
				LOGGER.warn("NumberFormatException while parsing substring \"{}\" from value \"{}\"", matchResult, value);
			}
		}

		LOGGER.warn("Can't parse the sample rate from \"{}\", returning -1", value);
		return -1;
	}

	protected static int parseDelay(String value) {
		if (isBlank(value)) {
			return Integer.MIN_VALUE;
		}

		// Examples of libmediainfo output (mediainfo --Full --Language=raw file):
		// Video_Delay : -408

		Matcher intMatcher = intPattern.matcher(value);
		if (intMatcher.find()) {
			String matchResult = intMatcher.group();
			try {
				return Integer.parseInt(matchResult);
			} catch (NumberFormatException ex) {
				LOGGER.warn("NumberFormatException while parsing substring \"{}\" from value \"{}\"", matchResult, value);
			}
		}
		LOGGER.warn("Unable to parse delay from \"{}\"", value);
		return Integer.MIN_VALUE;
	}

	protected static int parseBitRate(String value, boolean video) {
		if (isBlank(value)) {
			return -1;
		}

		// examples of libmediainfo output (mediainfo --Full --Language=raw file):
		// BitRate : 1509000
		// BitRate : Unknown / Unknown / 1509000

		Matcher intMatcher = intPattern.matcher(value);
		if (intMatcher.find()) {
			String matchResult = intMatcher.group();
			try {
				return Integer.parseInt(matchResult);
			} catch (NumberFormatException ex) {
				LOGGER.warn("NumberFormatException during parsing substring \"{}\" from value \"{}\"", matchResult, value);
			}
		}

		LOGGER.warn("Can't parse {}bitrate \"{}\". Returning -1", video ? "video " : "", value);
		return -1;
	}

	@Nullable
	protected static RateMode parseBitRateMode(String value) {
		if (isBlank(value)) {
			return null;
		}

		// Examples of libmediainfo output (mediainfo --Full --Language=raw file):
		// Bit rate mode : Variable / Variable / Constant
		// Bit rate mode : Variable

		String[] values = value.split("/");
		for (String singleValue : values) {
			RateMode bitRateMode = RateMode.typeOf(singleValue);
			if (bitRateMode != null) {
				return bitRateMode;
			}
		}

		LOGGER.warn("Unable to parse bitrate mode from \"{}\", returning null", value);
		return null;
	}

	public static String getFPSValue(String value) {
		if (value.contains("fps")) {
			value = value.substring(0, value.indexOf("fps"));
		}

		value = value.trim();
		return value;
	}

	public static String getFrameRateModeValue(String value) {
		if (value.indexOf('/') > -1) {
			value = value.substring(0, value.indexOf('/'));
		}

		value = value.trim();
		return value;
	}

	public static String getLang(String value) {
		if (value.indexOf('(') > -1) {
			value = value.substring(0, value.indexOf('('));
		}

		if (value.indexOf('/') > -1) {
			value = value.substring(0, value.indexOf('/'));
		}

		value = value.trim();
		return value;
	}

	/**
	 * @deprecated use trim().
	 */
	@Deprecated
	public static String getFlavor(String value) {
		return value.trim();
	}

	/**
	 * Parses the "Duration" format.
	 */
	@Nullable
	private static Double parseDuration(String value) {
		if (isBlank(value)) {
			return null;
		}
		String[] parts = value.split("\\s*/\\s*");
		value = parts[parts.length - 1];
		int separator = value.indexOf(".");
		if (separator > 0) {
			value = value.substring(0, separator);
		}
		try {
			long longValue = Long.parseLong(value);
			return Double.valueOf(longValue / 1000.0);
		} catch (NumberFormatException e) {
			LOGGER.warn("Could not parse duration from \"{}\"", value);
			return null;
		}
	}

	public static void setVideoProfileAndLevel(@Nonnull DLNAMediaInfo media, @Nullable String value) {
		// Value can look like "Advanced@L1", "Complex@L2", "MP@LL" or "Simple@L3" with VC-1 or MPEG-4 Visual.
		if (isBlank(value)) {
			media.setVideoProfile(null);
			media.setVideoLevel(null);
			return;
		}

		VideoCodec codec = media.getVideoCodec();
		if (codec == null) {
			media.setVideoProfile(value);
			media.setVideoLevel(null);
			return;
		}

		// Specialized parsing depending on the codec and how LibMediaInfo formats this.
		Matcher matcher;
		switch (codec) {
			case AV1:
				matcher = SIMPLE_PROFILE_NUMERIC_LEVEL_PATTERN.matcher(value);
				if (matcher.find()) {
					media.setVideoProfile(matcher.group(1));
					media.setVideoLevel(AV1Level.typeOf(matcher.group(2)));
				} else {
					media.setVideoProfile(null);
					media.setVideoLevel(null);
				}
				break;
			case H262:
				matcher = SIMPLE_PROFILE_LEVEL_PATTERN.matcher(value);
				if (matcher.find()) {
					media.setVideoProfile(matcher.group(1));
					media.setVideoLevel(H262Level.typeOf(matcher.group(2)));
				} else {
					media.setVideoProfile(null);
					media.setVideoLevel(null);
				}
				break;
			case H263:
				matcher = SIMPLE_PROFILE_NUMERIC_LEVEL_PATTERN.matcher(value);
				if (matcher.find()) {
					media.setVideoProfile(matcher.group(1));
					media.setVideoLevel(H263Level.typeOf(matcher.group(2)));
				} else {
					media.setVideoProfile(null);
					media.setVideoLevel(null);
				}
				break;
			case H264:
				matcher = H264_PROFILE_LEVEL_PATTERN.matcher(value);
				if (matcher.find()) {
					media.setVideoProfile(matcher.group(1));
					media.setVideoLevel(H264Level.typeOf(matcher.group(2)));
				} else {
					media.setVideoProfile(null);
					media.setVideoLevel(null);
				}
				break;
			case H265:
				matcher = H265_PROFILE_LEVEL_PATTERN.matcher(value);
				if (matcher.find()) {
					media.setVideoProfile(matcher.group(1));
					media.setVideoLevel(H265Level.typeOf(matcher.group(2)));
				} else {
					media.setVideoProfile(null);
					media.setVideoLevel(null);
				}
				break;
			case MPEG4ASP:
			case MPEG4SP:
				matcher = SIMPLE_PROFILE_NUMERIC_LEVEL_PATTERN.matcher(value);
				if (matcher.find()) {
					media.setVideoProfile(matcher.group(1));
					media.setVideoLevel(MPEG4VisualLevel.typeOf(matcher.group(2)));
				} else {
					media.setVideoProfile(null);
					media.setVideoLevel(null);
				}
				break;
			case VC1:
				matcher = SIMPLE_PROFILE_LEVEL_PATTERN.matcher(value);
				if (matcher.find()) {
					media.setVideoProfile(matcher.group(1));
					media.setVideoLevel(VC1Level.typeOf(matcher.group(2)));
				} else {
					media.setVideoProfile(null);
					media.setVideoLevel(null);
				}
				break;
			case VP9:
				matcher = SIMPLE_PROFILE_NUMERIC_LEVEL_PATTERN.matcher(value);
				if (matcher.find()) {
					media.setVideoProfile(matcher.group(1));
					media.setVideoLevel(VP9Level.typeOf(matcher.group(2)));
				} else {
					media.setVideoProfile(null);
					media.setVideoLevel(null);
				}
				break;
			default:
				media.setVideoProfile(value);
				media.setVideoLevel(null);
				break;
		}
	}

	@SuppressWarnings("unused")
	protected static class ParseLogger {

		private final StringBuilder sb = new StringBuilder();
		private final Columns generalColumns = new Columns(false, 2, 32, 62, 92);
		private final Columns streamColumns = new Columns(false, 4, 34, 64, 94);

		/**
		 * Appends a label and value to the internal {@link StringBuilder} at
		 * the next column using the specified parameters.
		 *
		 * @param columns the {@link Columns} to use.
		 * @param label the label.
		 * @param value the value.
		 * @param quote if {@code true}, {@code value} is wrapped in double
		 *            quotes.
		 * @param notBlank if {@code true}, doesn't append anything if
		 *            {@code value} is {@code null} or only whitespace.
		 * @return {@code true} if something was appended, {@code false}
		 *         otherwise.
		 */
		private boolean appendStringNextColumn(
			Columns columns,
			String label,
			String value,
			boolean quote,
			boolean notBlank
		) {
			if (notBlank && isBlank(value)) {
				return false;
			}
			sb.append(columns.toNextColumnRelative(sb));
			appendString(label, value, true, quote, false);
			return true;
		}

		/**
		 * Appends a label and value to the internal {@link StringBuilder} at
		 * the specified column using the specified parameters.
		 *
		 * @param columns the {@link Columns} to use.
		 * @param column the column number.
		 * @param label the label.
		 * @param value the value.
		 * @param quote if {@code true}, {@code value} is wrapped in double
		 *            quotes.
		 * @param notBlank if {@code true}, doesn't append anything if
		 *            {@code value} is {@code null} or only whitespace.
		 * @return {@code true} if something was appended, {@code false}
		 *         otherwise.
		 */
		private boolean appendStringColumn(
			Columns columns,
			int column,
			String label,
			String value,
			boolean quote,
			boolean notBlank
		) {
			if (notBlank && isBlank(value)) {
				return false;
			}
			sb.append(columns.toColumn(sb, column));
			appendString(label, value, true, quote, false);
			return true;
		}

		/**
		 * Appends a label and value to the internal {@link StringBuilder} using
		 * the specified parameters.
		 *
		 * @param label the label.
		 * @param value the value.
		 * @param first if {@code false}, {@code ", "} is added first.
		 * @param quote if {@code true}, {@code value} is wrapped in double
		 *            quotes.
		 * @param notBlank if {@code true}, doesn't append anything if
		 *            {@code value} is {@code null} or only whitespace.
		 * @return {@code true} if something was appended, {@code false}
		 *         otherwise.
		 */
		private boolean appendString(String label, String value, boolean first, boolean quote, boolean notBlank) {
			if (notBlank && isBlank(value)) {
				return false;
			}
			if (!first) {
				sb.append(", ");
			}
			sb.append(label);
			if (quote) {
				sb.append(": \"");
			} else {
				sb.append(": ");
			}
			sb.append(quote ? value : value.trim());
			if (quote) {
				sb.append("\"");
			}
			return true;
		}

		/**
		 * Appends a label and a boolean value to the internal
		 * {@link StringBuilder} at the next column using the specified
		 * parameters. The boolean value will be {@code "False"} if
		 * {@code value} is {@code null} or only whitespace, {@code "True"}
		 * otherwise.
		 *
		 * @param columns the {@link Columns} to use.
		 * @param label the label.
		 * @param value the value to evaluate.
		 * @param booleanValues if {@code true}, {@code "True"} and
		 *            {@code "False"} will be used. If {@code false},
		 *            {@code "Yes"} and {@code "No"} will be used.
		 * @return Always {@code true}.
		 */
		private boolean appendExistsNextColumn(Columns columns, String label, String value, boolean booleanValues) {
			sb.append(columns.toNextColumnRelative(sb));
			appendExists(label, value, true, booleanValues);
			return true;
		}

		/**
		 * Appends a label and a boolean value to the internal
		 * {@link StringBuilder} at the specified column using the specified
		 * parameters. The boolean value will be {@code "False"} if
		 * {@code value} is {@code null} or only whitespace, {@code "True"}
		 * otherwise.
		 *
		 * @param columns the {@link Columns} to use.
		 * @param column the column number.
		 * @param label the label.
		 * @param value the value to evaluate.
		 * @param booleanValues if {@code true}, {@code "True"} and
		 *            {@code "False"} will be used. If {@code false},
		 *            {@code "Yes"} and {@code "No"} will be used.
		 * @return Always {@code true}.
		 */
		private boolean appendExistsColumn(
			Columns columns,
			int column,
			String label,
			String value,
			boolean booleanValues
		) {
			sb.append(columns.toColumn(sb, column));
			appendExists(label, value, true, booleanValues);
			return true;
		}

		/**
		 * Appends a label and a boolean value to the internal
		 * {@link StringBuilder} using the specified parameters. The boolean
		 * value will be {@code "False"} if {@code value} is {@code null} or
		 * only whitespace, {@code "True"} otherwise.
		 *
		 * @param label the label.
		 * @param value the value to evaluate.
		 * @param first if {@code false}, {@code ", "} is added first.
		 * @param booleanValues if {@code true}, {@code "True"} and
		 *            {@code "False"} will be used. If {@code false},
		 *            {@code "Yes"} and {@code "No"} will be used.
		 * @return Always {@code true}.
		 */
		private boolean appendExists(String label, String value, boolean first, boolean booleanValues) {
			if (!first) {
				sb.append(", ");
			}
			sb.append(label).append(": ");
			if (isBlank(value)) {
				sb.append(booleanValues ? "False" : "No");
			} else {
				sb.append(booleanValues ? "True" : "Yes");
			}
			return true;
		}

		public void logGeneral(File file) {
			if (file == null) {
				sb.append("MediaInfo parsing results for null:\n");
			} else {
				sb.append("MediaInfo parsing results for \"").append(file.getAbsolutePath()).append("\":\n");
			}
			if (MI == null) {
				sb.append("ERROR: LibMediaInfo instance is null");
				return;
			}
			if (!MI.isValid()) {
				sb.append("ERROR: LibMediaInfo instance not valid");
				return;
			}
			sb.append("  ");
			boolean first = true;
			first &= !appendString("Title", MI.Get(StreamType.General, 0, "Title"), first, true, true);
			first &= !appendString("Format", MI.Get(StreamType.General, 0, "Format"), first, true, false);
			first &= !appendString("CodecID", MI.Get(StreamType.General, 0, "CodecID"), first, true, true);
			Double durationSec = parseDuration(MI.Get(StreamType.General, 0, "Duration"));
			if (durationSec != null) {
				first &= !appendString("Duration", StringUtil.formatDLNADuration(durationSec), first, false, true);
			}
			first &= !appendString("Overall Bitrate Mode", MI.Get(StreamType.General, 0, "OverallBitRate_Mode"), first, false, true);
			first &= !appendString("Overall Bitrate", MI.Get(StreamType.General, 0, "OverallBitRate"), first, false, true);
			first &= !appendString("Overall Bitrate Nom.", MI.Get(StreamType.General, 0, "OverallBitRate_Nominal"), first, false, true);
			first &= !appendString("Overall Bitrate Max.", MI.Get(StreamType.General, 0, "OverallBitRate_Maximum"), first, false, true);
			first &= !appendString("Stereoscopic", MI.Get(StreamType.General, 0, "StereoscopicLayout"), first, true, true);
			appendExists("Cover", MI.Get(StreamType.General, 0, "Cover_Data"), first, false);
			first = false;
			appendString("FPS", MI.Get(StreamType.General, 0, "FrameRate"), first, false, true);
			appendString("Track", MI.Get(StreamType.General, 0, "Track"), first, true, true);
			appendString("Album", MI.Get(StreamType.General, 0, "Album"), first, true, true);
			appendString("Performer", MI.Get(StreamType.General, 0, "Performer"), first, true, true);
			appendString("Genre", MI.Get(StreamType.General, 0, "Genre"), first, true, true);
			appendString("Rec Date", MI.Get(StreamType.General, 0, "Recorded_Date"), first, true, true);
		}

		public void logGeneralColumns(File file) {
			if (file == null) {
				sb.append("MediaInfo parsing results for null:\n");
			} else {
				sb.append("MediaInfo parsing results for \"").append(file.getAbsolutePath()).append("\":\n");
			}
			if (MI == null) {
				sb.append("ERROR: LibMediaInfo instance is null");
				return;
			}
			if (!MI.isValid()) {
				sb.append("ERROR: LibMediaInfo instance not valid");
				return;
			}
			generalColumns.reset();
			appendStringNextColumn(generalColumns, "Title", MI.Get(StreamType.General, 0, "Title"), true, true);
			appendStringNextColumn(generalColumns, "Format", MI.Get(StreamType.General, 0, "Format"), true, false);
			appendStringNextColumn(generalColumns, "CodecID", MI.Get(StreamType.General, 0, "CodecID"), true, true);
			Double durationSec = parseDuration(MI.Get(StreamType.General, 0, "Duration"));
			if (durationSec != null) {
				appendStringNextColumn(generalColumns, "Duration", StringUtil.formatDLNADuration(durationSec), false, true);
			}
			appendStringNextColumn(generalColumns, "Overall Bitrate Mode", MI.Get(StreamType.General, 0, "OverallBitRate_Mode"), false, true);
			appendStringNextColumn(generalColumns, "Overall Bitrate", MI.Get(StreamType.General, 0, "OverallBitRate"), false, true);
			appendStringNextColumn(generalColumns, "Overall Bitrate Nom.", MI.Get(StreamType.General, 0, "OverallBitRate_Nominal"), false, true);
			appendStringNextColumn(generalColumns, "Overall Bitrate Max.", MI.Get(StreamType.General, 0, "OverallBitRate_Maximum"), false, true);
			appendStringNextColumn(generalColumns, "Stereoscopic", MI.Get(StreamType.General, 0, "StereoscopicLayout"), true, true);
			appendExistsNextColumn(generalColumns, "Cover", MI.Get(StreamType.General, 0, "Cover_Data"), false);
			appendStringNextColumn(generalColumns, "FPS", MI.Get(StreamType.General, 0, "FrameRate"), false, true);
			appendStringNextColumn(generalColumns, "Track", MI.Get(StreamType.General, 0, "Track"), true, true);
			appendStringNextColumn(generalColumns, "Album", MI.Get(StreamType.General, 0, "Album"), true, true);
			appendStringNextColumn(generalColumns, "Performer", MI.Get(StreamType.General, 0, "Performer"), true, true);
			appendStringNextColumn(generalColumns, "Genre", MI.Get(StreamType.General, 0, "Genre"), true, true);
			appendStringNextColumn(generalColumns, "Rec Date", MI.Get(StreamType.General, 0, "Recorded_Date"), true, true);
		}

		public void logVideoTrack(int idx) {
			if (MI == null || !MI.isValid()) {
				return;
			}

			sb.append("\n    - Video - ");
			boolean first = true;
			first &= !appendString("Format", MI.Get(StreamType.Video, idx, "Format"), first, true, true);
			first &= !appendString("Version", MI.Get(StreamType.Video, idx, "Format_Version"), first, true, true);
			first &= !appendString("Profile", MI.Get(StreamType.Video, idx, "Format_Profile"), first, true, true);
			first &= !appendString("ID", MI.Get(StreamType.Video, idx, "ID"), first, false, true);
			first &= !appendString("CodecID", MI.Get(StreamType.Video, idx, "CodecID"), first, true, true);
			Double durationSec = parseDuration(MI.Get(StreamType.Video, 0, "Duration"));
			if (durationSec != null) {
				first &= !appendString("Duration", StringUtil.formatDLNADuration(durationSec), first, false, true);
			}
			first &= !appendString("BitRate Mode", MI.Get(StreamType.Video, idx, "BitRate_Mode"), first, false, true);
			first &= !appendString("Bitrate", MI.Get(StreamType.Video, idx, "BitRate"), first, false, true);
			first &= !appendString("Bitrate Nominal", MI.Get(StreamType.Video, idx, "BitRate_Nominal"), first, false, true);
			first &= !appendString("BitRate Maximum", MI.Get(StreamType.Video, idx, "BitRate_Maximum"), first, false, true);
			first &= !appendString("Bitrate Encoded", MI.Get(StreamType.Video, idx, "BitRate_Encoded"), first, false, true);
			first &= !appendString("Width", MI.Get(StreamType.Video, idx, "Width"), first, false, true);
			first &= !appendString("Height", MI.Get(StreamType.Video, idx, "Height"), first, false, true);
			first &= !appendString("Colorimetry", MI.Get(StreamType.Video, idx, "Colorimetry"), first, false, true);
			first &= !appendString("Chroma", MI.Get(StreamType.Video, idx, "ChromaSubsampling"), first, false, true);
			first &= !appendString("Matrix Co", MI.Get(StreamType.Video, idx, "matrix_coefficients"), first, false, true);
			first &= !appendString("MultiView Layout", MI.Get(StreamType.Video, idx, "MultiView_Layout"), first, true, true);
			first &= !appendString("PAR", MI.Get(StreamType.Video, idx, "PixelAspectRatio"), first, false, true);
			first &= !appendString("DAR", MI.Get(StreamType.Video, idx, "DisplayAspectRatio/String"), first, false, true);
			first &= !appendString("DAR Orig", MI.Get(StreamType.Video, idx, "DisplayAspectRatio_Original/String"), first, false, true);
			first &= !appendString("Scan Type", MI.Get(StreamType.Video, idx, "ScanType"), first, false, true);
			first &= !appendString("Scan Order", MI.Get(StreamType.Video, idx, "ScanOrder"), first, false, true);
			first &= !appendString("FPS", MI.Get(StreamType.Video, idx, "FrameRate"), first, false, true);
			first &= !appendString("FPS Orig", MI.Get(StreamType.Video, idx, "FrameRate_Original"), first, false, true);
			first &= !appendString("Framerate Mode", MI.Get(StreamType.Video, idx, "FrameRate_Mode"), first, false, true);
			first &= !appendString("RefFrames", MI.Get(StreamType.Video, idx, "Format_Settings_RefFrames"), first, false, true);
			first &= !appendString("QPel", MI.Get(StreamType.Video, idx, "Format_Settings_QPel"), first, true, true);
			first &= !appendString("GMC", MI.Get(StreamType.Video, idx, "Format_Settings_GMC"), first, true, true);
			first &= !appendString("GOP", MI.Get(StreamType.Video, idx, "Format_Settings_GOP"), first, true, true);
			first &= !appendString("Muxing Mode", MI.Get(StreamType.Video, idx, "MuxingMode"), first, true, true);
			first &= !appendString("Encrypt", MI.Get(StreamType.Video, idx, "Encryption"), first, true, true);
			first &= !appendString("Bit Depth", MI.Get(StreamType.Video, idx, "BitDepth"), first, false, true);
			first &= !appendString("Delay", MI.Get(StreamType.Video, idx, "Delay"), first, false, true);
			first &= !appendString("Delay Source", MI.Get(StreamType.Video, idx, "Delay_Source"), first, false, true);
			first &= !appendString("Delay Original", MI.Get(StreamType.Video, idx, "Delay_Original"), first, false, true);
			first &= !appendString("Delay O. Source", MI.Get(StreamType.Video, idx, "Delay_Original_Source"), first, false, true);
			first &= !appendString("TimeStamp_FirstFrame", MI.Get(StreamType.Video, idx, "TimeStamp_FirstFrame"), first, false, true);
		}

		public void logVideoTrackColumns(int idx) {
			if (MI == null || !MI.isValid()) {
				return;
			}

			sb.append("\n  - Video track ");
			appendString("ID", MI.Get(StreamType.Video, idx, "ID"), true, false, false);
			streamColumns.reset();
			sb.append("\n");
			appendStringNextColumn(streamColumns, "Format", MI.Get(StreamType.Video, idx, "Format"), true, true);
			appendStringNextColumn(streamColumns, "Version", MI.Get(StreamType.Video, idx, "Format_Version"), true, true);
			appendStringNextColumn(streamColumns, "Profile", MI.Get(StreamType.Video, idx, "Format_Profile"), true, true);
			appendStringNextColumn(streamColumns, "CodecID", MI.Get(StreamType.Video, idx, "CodecID"), true, true);
			Double durationSec = parseDuration(MI.Get(StreamType.Video, 0, "Duration"));
			if (durationSec != null) {
				appendStringNextColumn(streamColumns, "Duration", StringUtil.formatDLNADuration(durationSec), false, true);
			}
			appendStringNextColumn(streamColumns, "BitRate Mode", MI.Get(StreamType.Video, idx, "BitRate_Mode"), false, true);
			appendStringNextColumn(streamColumns, "Bitrate", MI.Get(StreamType.Video, idx, "BitRate"), false, true);
			appendStringNextColumn(streamColumns, "Bitrate Nominal", MI.Get(StreamType.Video, idx, "BitRate_Nominal"), false, true);
			appendStringNextColumn(streamColumns, "BitRate Maximum", MI.Get(StreamType.Video, idx, "BitRate_Maximum"), false, true);
			appendStringNextColumn(streamColumns, "Bitrate Encoded", MI.Get(StreamType.Video, idx, "BitRate_Encoded"), false, true);
			appendStringNextColumn(streamColumns, "Width", MI.Get(StreamType.Video, idx, "Width"), false, true);
			appendStringNextColumn(streamColumns, "Height", MI.Get(StreamType.Video, idx, "Height"), false, true);
			appendStringNextColumn(streamColumns, "Colorimetry", MI.Get(StreamType.Video, idx, "Colorimetry"), false, true);
			appendStringNextColumn(streamColumns, "Chroma", MI.Get(StreamType.Video, idx, "ChromaSubsampling"), false, true);
			appendStringNextColumn(streamColumns, "Matrix Co", MI.Get(StreamType.Video, idx, "matrix_coefficients"), false, true);
			appendStringNextColumn(streamColumns, "MultiView Layout", MI.Get(StreamType.Video, idx, "MultiView_Layout"), true, true);
			appendStringNextColumn(streamColumns, "PAR", MI.Get(StreamType.Video, idx, "PixelAspectRatio"), false, true);
			appendStringNextColumn(streamColumns, "DAR", MI.Get(StreamType.Video, idx, "DisplayAspectRatio/String"), false, true);
			appendStringNextColumn(streamColumns, "DAR Orig", MI.Get(StreamType.Video, idx, "DisplayAspectRatio_Original/String"), false, true);
			appendStringNextColumn(streamColumns, "Scan Type", MI.Get(StreamType.Video, idx, "ScanType"), false, true);
			appendStringNextColumn(streamColumns, "Scan Order", MI.Get(StreamType.Video, idx, "ScanOrder"), false, true);
			appendStringNextColumn(streamColumns, "FPS", MI.Get(StreamType.Video, idx, "FrameRate"), false, true);
			appendStringNextColumn(streamColumns, "FPS Orig", MI.Get(StreamType.Video, idx, "FrameRate_Original"), false, true);
			appendStringNextColumn(streamColumns, "Framerate Mode", MI.Get(StreamType.Video, idx, "FrameRate_Mode"), false, true);
			appendStringNextColumn(streamColumns, "RefFrames", MI.Get(StreamType.Video, idx, "Format_Settings_RefFrames"), false, true);
			appendStringNextColumn(streamColumns, "QPel", MI.Get(StreamType.Video, idx, "Format_Settings_QPel"), true, true);
			appendStringNextColumn(streamColumns, "GMC", MI.Get(StreamType.Video, idx, "Format_Settings_GMC"), true, true);
			appendStringNextColumn(streamColumns, "GOP", MI.Get(StreamType.Video, idx, "Format_Settings_GOP"), true, true);
			appendStringNextColumn(streamColumns, "Muxing Mode", MI.Get(StreamType.Video, idx, "MuxingMode"), true, true);
			appendStringNextColumn(streamColumns, "Encrypt", MI.Get(StreamType.Video, idx, "Encryption"), true, true);
			appendStringNextColumn(streamColumns, "Bit Depth", MI.Get(StreamType.Video, idx, "BitDepth"), false, true);
			appendStringNextColumn(streamColumns, "Delay", MI.Get(StreamType.Video, idx, "Delay"), false, true);
			appendStringNextColumn(streamColumns, "Delay Source", MI.Get(StreamType.Video, idx, "Delay_Source"), false, true);
			appendStringNextColumn(streamColumns, "Delay Original", MI.Get(StreamType.Video, idx, "Delay_Original"), false, true);
			appendStringNextColumn(streamColumns, "Delay O. Source", MI.Get(StreamType.Video, idx, "Delay_Original_Source"), false, true);
			appendStringNextColumn(streamColumns, "TimeStamp_FirstFrame", MI.Get(StreamType.Video, idx, "TimeStamp_FirstFrame"), false, true);
		}

		public void logAudioTrack(int idx) {
			if (MI == null || !MI.isValid()) {
				return;
			}

			sb.append("\n    - Audio - ");
			boolean first = true;
			first &= !appendString("Title", MI.Get(StreamType.Audio, idx, "Title"), first, true, true);
			first &= !appendString("Format", MI.Get(StreamType.Audio, idx, "Format"), first, true, true);
			first &= !appendString("Version", MI.Get(StreamType.Audio, idx, "Format_Version"), first, true, true);
			first &= !appendString("Profile", MI.Get(StreamType.Audio, idx, "Format_Profile"), first, true, true);
			first &= !appendString("ID", MI.Get(StreamType.Audio, idx, "ID"), first, false, true);
			first &= !appendString("CodecID", MI.Get(StreamType.Audio, idx, "CodecID"), first, true, true);
			first &= !appendString("CodecID Desc", MI.Get(StreamType.Audio, idx, "CodecID_Description"), first, true, true);
			Double durationSec = parseDuration(MI.Get(StreamType.Audio, 0, "Duration"));
			if (durationSec != null) {
				first &= !appendString("Duration", StringUtil.formatDLNADuration(durationSec), first, false, true);
			}
			first &= !appendString("BitRate Mode", MI.Get(StreamType.Audio, idx, "BitRate_Mode"), first, false, true);
			first &= !appendString("Bitrate", MI.Get(StreamType.Audio, idx, "BitRate"), first, false, true);
			first &= !appendString("Bitrate Nominal", MI.Get(StreamType.Audio, idx, "BitRate_Nominal"), first, false, true);
			first &= !appendString("BitRate Maximum", MI.Get(StreamType.Audio, idx, "BitRate_Maximum"), first, false, true);
			first &= !appendString("Bitrate Encoded", MI.Get(StreamType.Audio, idx, "BitRate_Encoded"), first, false, true);
			first &= !appendString("Language", MI.Get(StreamType.Audio, idx, "Language"), first, true, true);
			first &= !appendString("Channel(s)", MI.Get(StreamType.Audio, idx, "Channel(s)_Original"), first, false, true);
			first &= !appendString("Samplerate", MI.Get(StreamType.Audio, idx, "SamplingRate"), first, false, true);
			first &= !appendString("Track", MI.Get(StreamType.General, idx, "Track/Position"), first, false, true);
			first &= !appendString("Bit Depth", MI.Get(StreamType.Audio, idx, "BitDepth"), first, false, true);
			first &= !appendString("Delay", MI.Get(StreamType.Audio, idx, "Delay"), first, false, true);
			first &= !appendString("Delay Source", MI.Get(StreamType.Audio, idx, "Delay_Source"), first, false, true);
			first &= !appendString("Delay Original", MI.Get(StreamType.Audio, idx, "Delay_Original"), first, false, true);
			first &= !appendString("Delay O. Source", MI.Get(StreamType.Audio, idx, "Delay_Original_Source"), first, false, true);
		}

		public void logAudioTrackColumns(int idx) {
			if (MI == null || !MI.isValid()) {
				return;
			}

			sb.append("\n  - Audio track ");
			appendString("ID", MI.Get(StreamType.Audio, idx, "ID"), true, false, false);
			appendString("Title", MI.Get(StreamType.Audio, idx, "Title"), false, true, true);
			streamColumns.reset();
			sb.append("\n");
			appendStringNextColumn(streamColumns, "Format", MI.Get(StreamType.Audio, idx, "Format"), true, true);
			appendStringNextColumn(streamColumns, "Version", MI.Get(StreamType.Audio, idx, "Format_Version"), true, true);
			appendStringNextColumn(streamColumns, "Profile", MI.Get(StreamType.Audio, idx, "Format_Profile"), true, true);
			appendStringNextColumn(streamColumns, "CodecID", MI.Get(StreamType.Audio, idx, "CodecID"), true, true);
			appendStringNextColumn(streamColumns, "CodecID Desc", MI.Get(StreamType.Audio, idx, "CodecID_Description"), true, true);
			Double durationSec = parseDuration(MI.Get(StreamType.Audio, 0, "Duration"));
			if (durationSec != null) {
				appendStringNextColumn(streamColumns, "Duration", StringUtil.formatDLNADuration(durationSec), false, true);
			}
			appendStringNextColumn(streamColumns, "BitRate Mode", MI.Get(StreamType.Audio, idx, "BitRate_Mode"), false, true);
			appendStringNextColumn(streamColumns, "Bitrate", MI.Get(StreamType.Audio, idx, "BitRate"), false, true);
			appendStringNextColumn(streamColumns, "Bitrate Nominal", MI.Get(StreamType.Audio, idx, "BitRate_Nominal"), false, true);
			appendStringNextColumn(streamColumns, "BitRate Maximum", MI.Get(StreamType.Audio, idx, "BitRate_Maximum"), false, true);
			appendStringNextColumn(streamColumns, "Bitrate Encoded", MI.Get(StreamType.Audio, idx, "BitRate_Encoded"), false, true);
			appendStringNextColumn(streamColumns, "Language", MI.Get(StreamType.Audio, idx, "Language"), true, true);
			appendStringNextColumn(streamColumns, "Channel(s)", MI.Get(StreamType.Audio, idx, "Channel(s)"), false, true);
			appendStringNextColumn(streamColumns, "Samplerate", MI.Get(StreamType.Audio, idx, "SamplingRate"), false, true);
			appendStringNextColumn(streamColumns, "Track", MI.Get(StreamType.General, idx, "Track/Position"), false, true);
			appendStringNextColumn(streamColumns, "Bit Depth", MI.Get(StreamType.Audio, idx, "BitDepth"), false, true);
			appendStringNextColumn(streamColumns, "Delay", MI.Get(StreamType.Audio, idx, "Delay"), false, true);
			appendStringNextColumn(streamColumns, "Delay Source", MI.Get(StreamType.Audio, idx, "Delay_Source"), false, true);
			appendStringNextColumn(streamColumns, "Delay Original", MI.Get(StreamType.Audio, idx, "Delay_Original"), false, true);
			appendStringNextColumn(streamColumns, "Delay O. Source", MI.Get(StreamType.Audio, idx, "Delay_Original_Source"), false, true);
		}

		public void logImage(int idx) {
			if (MI == null || !MI.isValid()) {
				return;
			}

			sb.append("\n    - Image - ");
			boolean first = true;
			first &= !appendString("Format", MI.Get(StreamType.Image, idx, "Format"), first, true, true);
			first &= !appendString("Version", MI.Get(StreamType.Image, idx, "Format_Version"), first, true, true);
			first &= !appendString("Profile", MI.Get(StreamType.Image, idx, "Format_Profile"), first, true, true);
			first &= !appendString("ID", MI.Get(StreamType.Image, idx, "ID"), first, false, true);
			first &= !appendString("Width", MI.Get(StreamType.Image, idx, "Width"), first, false, true);
			first &= !appendString("Height", MI.Get(StreamType.Image, idx, "Height"), first, false, true);
		}

		public void logImageColumns(int idx) {
			if (MI == null || !MI.isValid()) {
				return;
			}

			sb.append("\n  - Image ");
			appendString("ID", MI.Get(StreamType.Image, idx, "ID"), true, false, false);
			streamColumns.reset();
			sb.append("\n");
			appendStringNextColumn(streamColumns, "Format", MI.Get(StreamType.Image, idx, "Format"), true, true);
			appendStringNextColumn(streamColumns, "Version", MI.Get(StreamType.Image, idx, "Format_Version"), true, true);
			appendStringNextColumn(streamColumns, "Profile", MI.Get(StreamType.Image, idx, "Format_Profile"), true, true);
			appendStringNextColumn(streamColumns, "Width", MI.Get(StreamType.Image, idx, "Width"), false, true);
			appendStringNextColumn(streamColumns, "Height", MI.Get(StreamType.Image, idx, "Height"), false, true);
		}

		public void logSubtitleTrack(int idx, boolean videoSubtitle) {
			if (MI == null || !MI.isValid()) {
				return;
			}

			sb.append("\n    - Sub - ");
			boolean first = true;
			if (videoSubtitle) {
				first &= !appendString("Title", MI.Get(StreamType.Video, idx, "Title"), first, true, true);
				first &= !appendString("Format", MI.Get(StreamType.Video, idx, "Format"), first, true, true);
				first &= !appendString("Version", MI.Get(StreamType.Video, idx, "Format_Version"), first, true, true);
				first &= !appendString("Profile", MI.Get(StreamType.Video, idx, "Format_Profile"), first, true, true);
				first &= !appendString("ID", MI.Get(StreamType.Video, idx, "ID"), first, false, true);
			} else {
				first &= !appendString("Title", MI.Get(StreamType.Text, idx, "Title"), first, true, true);
				first &= !appendString("Format", MI.Get(StreamType.Text, idx, "Format"), first, true, true);
				first &= !appendString("Version", MI.Get(StreamType.Text, idx, "Format_Version"), first, true, true);
				first &= !appendString("Profile", MI.Get(StreamType.Text, idx, "Format_Profile"), first, true, true);
				first &= !appendString("ID", MI.Get(StreamType.Text, idx, "ID"), first, false, true);
				first &= !appendString("Language", MI.Get(StreamType.Text, idx, "Language"), first, true, true);
			}
		}

		public void logSubtitleTrackColumns(int idx, boolean videoSubtitle) {
			if (MI == null || !MI.isValid()) {
				return;
			}

			sb.append("\n  - Subtitle ");
			streamColumns.reset();
			if (videoSubtitle) {
				appendString("ID", MI.Get(StreamType.Video, idx, "ID"), true, false, false);
				appendString("Title", MI.Get(StreamType.Video, idx, "Title"), false, true, true);
				sb.append("\n");
				appendStringNextColumn(streamColumns, "Format", MI.Get(StreamType.Video, idx, "Format"), true, true);
				appendStringNextColumn(streamColumns, "Version", MI.Get(StreamType.Video, idx, "Format_Version"), true, true);
				appendStringNextColumn(streamColumns, "Profile", MI.Get(StreamType.Video, idx, "Format_Profile"), true, true);
			} else {
				appendString("ID", MI.Get(StreamType.Text, idx, "ID"), true, false, false);
				appendString("Title", MI.Get(StreamType.Text, idx, "Title"), false, true, true);
				sb.append("\n");
				appendStringNextColumn(streamColumns, "Format", MI.Get(StreamType.Text, idx, "Format"), true, true);
				appendStringNextColumn(streamColumns, "Version", MI.Get(StreamType.Text, idx, "Format_Version"), true, true);
				appendStringNextColumn(streamColumns, "Profile", MI.Get(StreamType.Text, idx, "Format_Profile"), true, true);
				appendStringNextColumn(streamColumns, "Language", MI.Get(StreamType.Text, idx, "Language"), true, true);
			}
		}

		@Override
		public String toString() {
			return sb.toString();
		}

		protected static class Columns {

			private final boolean includeZeroColumn;
			private final int[] columns;
			private int lastColumn = -1;

			public Columns(boolean includeZeroColumn, int... columns) {
				this.includeZeroColumn = includeZeroColumn;
				this.columns = columns;
			}

			public int lastColumn() {
				return lastColumn;
			}

			public int nextColumn() {
				if (lastColumn < 0 || lastColumn >= columns.length) {
					return includeZeroColumn ? 0 : 1;
				}
				return lastColumn + 1;
			}

			public void reset() {
				lastColumn = -1;
			}

			/**
			 * Returns the whitespace needed to jump to the next sequential
			 * column.
			 */
			public String toNextColumnAbsolute(StringBuilder sb) {
				if (sb == null) {
					return "";
				}

				boolean newLine = false;
				int next = nextColumn();
				if (next < lastColumn) {
					newLine = true;
				}
				return newLine ? "\n" + toColumn(0, nextColumn()) : toColumn(sb, nextColumn());
			}

			/**
			 * Returns the whitespace needed to jump to the next available
			 * column.
			 */
			public String toNextColumnRelative(StringBuilder sb) {
				if (sb == null) {
					return "";
				}

				boolean newLine = false;
				int linePosition = getLinePosition(sb);
				int column = -1;
				if (includeZeroColumn && linePosition == 0) {
					column = 0;
				} else {
					for (int i = 0; i < columns.length; i++) {
						if (columns[i] > linePosition) {
							column = i + 1;
							break;
						}
					}
				}
				if (column < 0) {
					column = includeZeroColumn ? 0 : 1;
					newLine = true;
				}
				return newLine ? "\n" + toColumn(0, column) : toColumn(linePosition, column);
			}

			public String toColumn(StringBuilder sb, int column) {
				if (sb == null || column > columns.length) {
					return "";
				}

				return toColumn(getLinePosition(sb), column);
			}

			public String toColumn(int linePosition, int column) {
				if (column > columns.length || linePosition < 0) {
					return "";
				}
				if (column < 1 ) {
					lastColumn = 0;
					return linePosition > 0 ? " " : "";
				}

				lastColumn = column;
				int fill = columns[column - 1] - linePosition;
				if (fill < 1 && linePosition > 0) {
					fill = 1;
				}
				return fill > 0 ? StringUtil.fillString(" ", fill) : "";
			}

			public static int getLinePosition(StringBuilder sb) {
				if (sb == null) {
					return 0;
				}
				int position = sb.lastIndexOf("\n");
				if (position < 0) {
					position = sb.length();
				} else {
					position = sb.length() - position - 1;
				}
				return position;
			}

			@Override
			public String toString() {
				StringBuilder sb = new StringBuilder("Columns: 0");
				for (int column : columns) {
					sb.append(", ").append(column);
				}
				return sb.toString();
			}
		}
	}
}
