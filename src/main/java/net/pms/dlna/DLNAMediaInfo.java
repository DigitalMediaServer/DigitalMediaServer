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
package net.pms.dlna;

import java.io.*;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.pms.PMS;
import net.pms.configuration.FFmpegProgramInfo;
import net.pms.configuration.FormatConfiguration;
import net.pms.configuration.PmsConfiguration;
import net.pms.configuration.RendererConfiguration;
import net.pms.encoders.PlayerFactory;
import net.pms.encoders.StandardPlayerId;
import net.pms.exception.UnknownFormatException;
import net.pms.formats.AudioAsVideo;
import net.pms.formats.Format;
import net.pms.formats.Format.Identifier;
import net.pms.formats.FormatType;
import net.pms.formats.audio.*;
import net.pms.formats.v2.SubtitleType;
import net.pms.image.ExifInfo;
import net.pms.image.ExifOrientation;
import net.pms.image.ImageFormat;
import net.pms.image.ImageInfo;
import net.pms.image.ImagesUtil;
import net.pms.image.ImagesUtil.ScaleType;
import net.pms.image.thumbnail.CoverUtil;
import net.pms.io.OutputParams;
import net.pms.io.ProcessWrapperImpl;
import net.pms.media.VideoCodec;
import net.pms.media.VideoLevel;
import net.pms.network.HTTPResource;
import net.pms.util.FileUtil;
import net.pms.util.MpegUtil;
import net.pms.util.ProcessUtil;
import net.pms.util.Rational;
import static net.pms.util.StringUtil.*;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.logging.ErrorMessage;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.KeyNotFoundException;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class keeps track of media file metadata scanned by the MediaInfo library.
 *
 * TODO: Change all instance variables to private. For backwards compatibility
 * with external plugin code the variables have all been marked as deprecated
 * instead of changed to private, but this will surely change in the future.
 * When everything has been changed to private, the deprecated note can be
 * removed.
 */
public class DLNAMediaInfo implements Cloneable {
	private static final Logger LOGGER = LoggerFactory.getLogger(DLNAMediaInfo.class);
	private static final PmsConfiguration configuration = PMS.getConfiguration();

	public static final long ENDFILE_POS = 99999475712L;

	/**
	 * Maximum size of a stream, taking into account that some renderers (like
	 * the PS3) will convert this <code>long</code> to <code>int</code>.
	 * Truncating this value will still return the maximum value that an
	 * <code>int</code> can contain.
	 */
	public static final long TRANS_SIZE = Long.MAX_VALUE - Integer.MAX_VALUE - 1;

	/**
	 * Containers that can represent audio or video media is by default
	 * considered to be video. This {@link Map} maps such containers to the type
	 * to use if they represent audio media.
	 */
	protected static final Map<String, AudioVariantInfo> audioOrVideoContainers;

	static {
		Map<String, AudioVariantInfo> mutableAudioOrVideoContainers = new HashMap<>();

		// Map container formats to their "audio variant".
		mutableAudioOrVideoContainers.put(FormatConfiguration.MP4, new AudioVariantInfo(new M4A(), FormatConfiguration.M4A));
		mutableAudioOrVideoContainers.put(FormatConfiguration.MKV, new AudioVariantInfo(new MKA(), FormatConfiguration.MKA));
		mutableAudioOrVideoContainers.put(FormatConfiguration.OGG, new AudioVariantInfo(new OGA(), FormatConfiguration.OGA));
		mutableAudioOrVideoContainers.put(FormatConfiguration.RM, new AudioVariantInfo(new RA(), FormatConfiguration.RA));
		// XXX Not technically correct, but should work until MPA is implemented
		mutableAudioOrVideoContainers.put(FormatConfiguration.MPEG1, new AudioVariantInfo(new MP3(), FormatConfiguration.MPA));
		// XXX Not technically correct, but should work until MPA is implemented
		mutableAudioOrVideoContainers.put(FormatConfiguration.MPEG2, new AudioVariantInfo(new MP3(), FormatConfiguration.MPA));
		mutableAudioOrVideoContainers.put(FormatConfiguration.THREEGPP, new AudioVariantInfo(new THREEGA(), FormatConfiguration.THREEGA));
		mutableAudioOrVideoContainers.put(FormatConfiguration.THREEGPP2, new AudioVariantInfo(new THREEG2A(), FormatConfiguration.THREEGA));
		// XXX WEBM Audio is NOT MKA, but it will have to stay this way until WEBM Audio is implemented.
		mutableAudioOrVideoContainers.put(FormatConfiguration.WEBM, new AudioVariantInfo(new MKA(), FormatConfiguration.WEBA));
		mutableAudioOrVideoContainers.put(FormatConfiguration.WMV, new AudioVariantInfo(new WMA(), FormatConfiguration.WMA));

		audioOrVideoContainers = Collections.unmodifiableMap(mutableAudioOrVideoContainers);
	}

	// Stored in database
	@Nullable
	private Double durationSec;

	private int bitRate;

	@Nullable
	private RateMode bitRateMode;

	/**
	 * @deprecated Use standard getter and setter to access this variable.
	 */
	@Deprecated
	public int width;

	/**
	 * @deprecated Use standard getter and setter to access this variable.
	 */
	@Deprecated
	public int height;

	/**
	 * @deprecated Use standard getter and setter to access this variable.
	 */
	@Deprecated
	public long size;

	/**
	 * @deprecated Use standard getter and setter to access this variable.
	 */
	@Deprecated
	public String codecV;

	/**
	 * @deprecated Use standard getter and setter to access this variable.
	 */
	@Deprecated
	public String frameRate;

	private String frameRateMode;
	private Rational pixelAspectRatio;
	private ScanType scanType;
	private ScanOrder scanOrder;

	/**
	 * The framerate mode as read from the parser.
	 */
	private String frameRateModeRaw;
	private String frameRateOriginal;
	private Rational aspectRatioDvdIso;
	private Rational aspectRatioContainer;
	private Rational aspectRatioVideoTrack;
	private int videoBitDepth = 8;

	private volatile DLNAThumbnail thumb = null;

	private volatile ImageInfo imageInfo = null;

	/**
	 * @deprecated Use standard getter and setter to access this variable.
	 */
	@Deprecated
	public String mimeType;

	/**
	 * @deprecated Use standard getter and setter to access this variable.
	 */
	@Deprecated
	public int bitsPerPixel;

	private int referenceFrameCount = -1;

	private VideoLevel videoLevel;
	private String videoProfile;

	private List<DLNAMediaAudio> audioTracks = new ArrayList<>();
	private List<DLNAMediaSubtitle> subtitleTracks = new ArrayList<>();

	private boolean externalSubsExist = false;

	public void setExternalSubsExist(boolean exist) {
		this.externalSubsExist = exist;
	}

	public boolean isExternalSubsExist() {
		return externalSubsExist;
	}

	private boolean externalSubsParsed = false;

	public void setExternalSubsParsed(boolean parsed) {
		this.externalSubsParsed = parsed;
	}

	public boolean isExternalSubsParsed() {
		return externalSubsParsed;
	}

	/**
	 * @deprecated Use standard getter and setter to access this variable.
	 */
	@Deprecated
	public String muxingMode;

	/**
	 * @deprecated Use standard getter and setter to access this variable.
	 */
	@Deprecated
	public String muxingModeAudio;

	/**
	 * @deprecated Use standard getter and setter to access this variable.
	 */
	@Deprecated
	public String container;

	private final Object h264_annexBLock = new Object();
	private byte[] h264_annexB;

	/**
	 * Not stored in database.
	 */
	private volatile boolean mediaparsed;

	public boolean ffmpegparsed;

	/**
	 * isUseMediaInfo-related, used to manage thumbnail management separated
	 * from the main parsing process.
	 *
	 * @deprecated Use standard getter and setter to access this variable.
	 */
	@Deprecated
	public volatile boolean thumbready;

	/**
	 * @deprecated Use standard getter and setter to access this variable.
	 */
	@Deprecated
	public int dvdtrack;

	/**
	 * @deprecated Use standard getter and setter to access this variable.
	 */
	@Deprecated
	public boolean secondaryFormatValid = true;

	private final Object parsingLock = new Object();
	private boolean parsing = false;

	private final Object ffmpeg_failureLock = new Object();
	private boolean ffmpeg_failure = false;

	private Map<String, String> extras;

	/**
	 * @deprecated Use standard getter and setter to access this variable.
	 */
	@Deprecated
	public boolean encrypted;

	/**
	 * @deprecated Use standard getter and setter to access this variable.
	 */
	@Deprecated
	public String matrixCoefficients;

	/**
	 * @deprecated Use standard getter and setter to access this variable.
	 */
	@Deprecated
	public String stereoscopy;

	/**
	 * @deprecated Use standard getter and setter to access this variable.
	 */
	@Deprecated
	public String fileTitleFromMetadata;

	/**
	 * @deprecated Use standard getter and setter to access this variable.
	 */
	@Deprecated
	public String videoTrackTitleFromMetadata;

	private int videoTrackCount = 0;
	private int imageCount = 0;

	public int getVideoTrackCount() {
		return videoTrackCount;
	}

	public void setVideoTrackCount(int value) {
		videoTrackCount = value;
	}

	public int getAudioTrackCount() {
		return audioTracks.size();
	}

	public int getImageCount() {
		return imageCount;
	}

	public void setImageCount(int value) {
		imageCount = value;
	}

	public int getSubTrackCount() {
		return subtitleTracks.size();
	}

	public boolean isVideo() {
		return MediaType.VIDEO == getMediaType();
	}

	public boolean isAudio() {
		return MediaType.AUDIO == getMediaType();
	}

	public boolean hasAudio() {
		return audioTracks.size() > 0;
	}

	/**
	 * Determines whether this media "is" MPEG-4 SLS.
	 * <p>
	 * SLS is MPEG-4's hybrid lossless audio codec. It uses a standard MPEG-4 GA
	 * core layer. Valid cores include AAC-LC, AAC Scalable (without LTP), ER
	 * AAC LC, ER AAC Scalable, and ER BSAC.
	 * <p>
	 * DMS currently only implements AAC-LC and ER BSAC among the valid core
	 * layer codecs, so only those are "approved" by this test. If further
	 * codecs are added in the future, this test should be modified accordingly.
	 *
	 * @return {@code true} if this {@link DLNAMediaInfo} instance has two audio
	 *         tracks where the first has an approved AAC codec and the second
	 *         has codec SLS, {@code false} otherwise.
	 */
	public boolean isSLS() {
		if (audioTracks.size() != 2) {
			return false;
		}

		return
			(
				audioTracks.get(0).isAACLC() ||
				audioTracks.get(0).isERBSAC()
			) &&
			audioTracks.get(1).isSLS();
	}

	/**
	 * Determines the {@link MediaType} from the parsed information as it is at
	 * the moment. The results may change if the parsed information changes.
	 *
	 * @return The {@link MediaType} or {@code null} if none could be
	 *         determined.
	 */
	@Nullable
	public MediaType getMediaType() {
		if (videoTrackCount > 0) {
			return MediaType.VIDEO;
		}
		if (audioTracks != null && !audioTracks.isEmpty()) {
			return MediaType.AUDIO;
		}

		return imageCount > 0 ? MediaType.IMAGE : null;
	}

	/**
	 * @return true when there are subtitle tracks embedded in the media file.
	 */
	public boolean hasSubtitles() {
		return subtitleTracks.size() > 0;
	}

	public boolean isImage() {
		return MediaType.IMAGE == getMediaType();
	}

	/**
	 * Used to determine whether tsMuxeR can mux the file to the renderer
	 * instead of transcoding.
	 * Also used by DLNAResource to help determine the DLNA.ORG_PN (file type)
	 * value to send to the renderer.
	 *
	 * TODO: Now that FFmpeg is muxing without tsMuxeR, we should make a separate
	 *       function for that, or even better, re-think this whole approach.
	 *
	 * @param mediaRenderer The renderer we might mux to.
	 *
	 * @return
	 */
	public boolean isMuxable(RendererConfiguration mediaRenderer) {
		// Make sure the file is H.264 video
		if (!isH264()) {
			return false;
		}

		// Check if the renderer supports the resolution of the video
		if (
			(
				mediaRenderer.isMaximumResolutionSpecified() &&
				(
					width > mediaRenderer.getMaxVideoWidth() ||
					height > mediaRenderer.getMaxVideoHeight()
				)
			) ||
			(
				!mediaRenderer.isMuxNonMod4Resolution() &&
				!isMod4()
			)
		) {
			return false;
		}

		// Temporary fix: MediaInfo support will take care of this in the future
		// For now, http://ps3mediaserver.org/forum/viewtopic.php?f=11&t=6361&start=0
		// Bravia does not support AVC video at less than 288px high
		if (mediaRenderer.isBRAVIA() && height < 288) {
			return false;
		}

		return true;
	}

	/**
	 * Whether a file is a WEB-DL release.
	 *
	 * It's important for some devices like PS3 because WEB-DL files often have
	 * some difference (possibly not starting on a keyframe or something to do with
	 * SEI output from MEncoder, possibly something else) that makes the PS3 not
	 * accept them when output from tsMuxeR via MEncoder.
	 *
	 * The above statement may not be applicable when using tsMuxeR via FFmpeg
	 * so we should reappraise the situation if we make that change.
	 *
	 * It is unlikely it will return false-positives but it will return
	 * false-negatives.
	 *
	 * @param filename the filename.
	 * @param params the file properties.
	 *
	 * @return Whether a file is a WEB-DL release.
	 */
	public boolean isWebDl(String filename, OutputParams params) {
		// Check the filename
		if (filename.toLowerCase().replaceAll("\\-", "").contains("webdl")) {
			return true;
		}

		// Check the metadata
		if (
			(
				getFileTitleFromMetadata() != null &&
				getFileTitleFromMetadata().toLowerCase().replaceAll("\\-", "").contains("webdl")
			) ||
			(
				getVideoTrackTitleFromMetadata() != null &&
				getVideoTrackTitleFromMetadata().toLowerCase().replaceAll("\\-", "").contains("webdl")
			) ||
			(
				params.aid != null &&
				params.aid.getAudioTrackTitleFromMetadata() != null &&
				params.aid.getAudioTrackTitleFromMetadata().toLowerCase().replaceAll("\\-", "").contains("webdl")
			) ||
			(
				params.sid != null &&
				params.sid.getSubtitlesTrackTitleFromMetadata() != null &&
				params.sid.getSubtitlesTrackTitleFromMetadata().toLowerCase().replaceAll("\\-", "").contains("webdl")
			)
		) {
			return true;
		}

		return false;
	}

	public Map<String, String> getExtras() {
		return extras;
	}

	public void putExtra(String key, String value) {
		if (extras == null) {
			extras = new HashMap<>();
		}

		extras.put(key, value);
	}

	public String getExtrasAsString() {
		if (extras == null) {
			return null;
		}

		StringBuilder sb = new StringBuilder();

		for (Map.Entry<String, String> entry : extras.entrySet()) {
			sb.append(entry.getKey());
			sb.append('|');
			sb.append(entry.getValue());
			sb.append('|');
		}

		return sb.toString();
	}

	public void setExtrasAsString(String value) {
		if (value != null) {
			StringTokenizer st = new StringTokenizer(value, "|");

			while (st.hasMoreTokens()) {
				try {
					putExtra(st.nextToken(), st.nextToken());
				} catch (NoSuchElementException nsee) {
					LOGGER.debug("Caught exception", nsee);
				}
			}
		}
	}

	public void generateThumbnail(
		InputFile input,
		Format ext,
		double seekPosition,
		boolean resume,
		RendererConfiguration renderer
	) {
		DLNAMediaInfo forThumbnail = new DLNAMediaInfo();
		forThumbnail.setMediaparsed(mediaparsed);  // check if file was already parsed by MediaInfo
		forThumbnail.setImageInfo(imageInfo);
		forThumbnail.durationSec = getDuration();
		forThumbnail.audioTracks = audioTracks;
		forThumbnail.imageCount = imageCount;
		forThumbnail.videoTrackCount = videoTrackCount;
		if (forThumbnail.durationSec == null) {
			forThumbnail.durationSec = Double.valueOf(0d);
		} else if (seekPosition <= forThumbnail.durationSec.doubleValue()) {
			forThumbnail.durationSec = Double.valueOf(seekPosition);
		} else {
			forThumbnail.durationSec /= 2;
		}

		forThumbnail.parse(input, ext, true, resume, renderer);
		thumb = forThumbnail.thumb;
		thumbready = true;
	}

	private ProcessWrapperImpl getFFmpegThumbnail(InputFile media, boolean resume) {
		/*
		 * Note: The text output from FFmpeg is used by renderers that do
		 * not use MediaInfo, so do not make any changes that remove or
		 * minimize the amount of text given by FFmpeg here
		 */
		ArrayList<String> args = new ArrayList<>();
		boolean generateThumbnail = configuration.isThumbnailGenerationEnabled() && !configuration.isUseMplayerForVideoThumbs();

		args.add(PlayerFactory.getPlayerExecutable(StandardPlayerId.FFMPEG_VIDEO));
		if (args.get(0) == null) {
			LOGGER.warn("Cannot generate thumbnail for \"{}\" since the FFmpeg executable is undefined", media.getFilename());
			return null;
		}

		if (generateThumbnail) {
			args.add("-ss");
			if (resume) {
				args.add(Integer.toString((int) getDurationInSeconds()));
			} else {
				args.add(Integer.toString((int) Math.min(configuration.getThumbnailSeekPos(), getDurationInSeconds())));
			}
		}

		args.add("-loglevel");
		args.add(FFmpegProgramInfo.getFFmpegLogLevel());
		args.add("-hide_banner");
		args.add("-i");

		if (media.getFile() != null) {
			args.add(ProcessUtil.getShortFileNameIfWideChars(media.getFile().getAbsolutePath()));
		} else {
			args.add("-");
		}

		args.add("-map");
		args.add("0:V");
		args.add("-an");
		args.add("-dn");
		args.add("-sn");
		if (generateThumbnail) {
			args.add("-c:v");
			args.add("mjpeg");
			args.add("-pix_fmt");
			args.add("yuvj420p");
			args.add("-q:v");
			args.add("2");
			args.add("-vf");
			args.add("select='eq(pict_type,PICT_TYPE_I)',scale='if(gte(iw,ih),320,trunc(iw*sar*oh/ih))':'if(gte(iw,ih),trunc(ih*ow/iw/sar),320)'");
			args.add("-vsync");
			args.add("vfr");
			args.add("-vframes");
			args.add("1");
			args.add("-f");
			args.add("image2");
			args.add("pipe:");
		}

		OutputParams params = new OutputParams(configuration);
		params.maxBufferSize = 1;
		params.stdin = media.getPush();
		params.noexitcheck = true; // not serious if anything happens during the thumbnailer

		// true: consume stderr on behalf of the caller i.e. parse()
		final ProcessWrapperImpl pw = new ProcessWrapperImpl(args.toArray(new String[args.size()]), true, params, false, true);

		// FAILSAFE
		synchronized (parsingLock) {
			parsing = true;
		}
		Runnable r = new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(10000);
					synchronized (ffmpeg_failureLock) {
						ffmpeg_failure = true;
					}
				} catch (InterruptedException e) { }

				pw.stopProcess();
				synchronized (parsingLock) {
					parsing = false;
				}
			}
		};

		Thread failsafe = new Thread(r, "FFmpeg Thumbnail Failsafe");
		failsafe.start();
		pw.runInSameThread();
		synchronized (parsingLock) {
			parsing = false;
		}
		return pw;
	}

	private ProcessWrapperImpl getMplayerThumbnail(InputFile media, boolean resume) throws IOException {
		File file = media.getFile();
		String args[] = new String[14];
		args[0] = configuration.getMPlayerPath();
		args[1] = "-ss";
		if (resume) {
			args[2] = "" + (int) getDurationInSeconds();
		} else {
			args[2] = "" + configuration.getThumbnailSeekPos();
		}

		args[3] = "-quiet";

		if (file != null) {
			args[4] = ProcessUtil.getShortFileNameIfWideChars(file.getAbsolutePath());
		} else {
			args[4] = "-";
		}

		args[5] = "-msglevel";
		args[6] = "all=4";
		args[7] = "-vf";
		args[8] = "scale=320:-2";
		args[9] = "-frames";
		args[10] = "1";
		args[11] = "-vo";
		String frameName = "" + media.hashCode();
		frameName = "mplayer_thumbs:subdirs=\"" + frameName + "\"";
		frameName = frameName.replace(',', '_');
		args[12] = "jpeg:outdir=" + frameName;
		args[13] = "-nosound";
		OutputParams params = new OutputParams(configuration);
		params.workDir = configuration.getTempFolder();
		params.maxBufferSize = 1;
		params.stdin = media.getPush();
		params.log = true;
		params.noexitcheck = true; // not serious if anything happens during the thumbnailer
		final ProcessWrapperImpl pw = new ProcessWrapperImpl(args, true, params);

		// FAILSAFE
		synchronized (parsingLock) {
			parsing = true;
		}
		Runnable r = new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) { }

				pw.stopProcess();
				synchronized (parsingLock) {
					parsing = false;
				}
			}
		};

		Thread failsafe = new Thread(r, "MPlayer Thumbnail Failsafe");
		failsafe.start();
		pw.runInSameThread();
		synchronized (parsingLock) {
			parsing = false;
		}
		return pw;
	}

	/**
	 * Parse media without using MediaInfo.
	 */
	public void parse(InputFile inputFile, Format ext, boolean thumbOnly, boolean resume, RendererConfiguration renderer) {
		int i = 0;

		while (isParsing()) {
			if (i == 5) {
				mediaparsed = true;
				break;
			}

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) { }

			i++;
		}

		if (isMediaparsed() && !thumbOnly) { // file could be already parsed by MediaInfo and we need only thumbnail
			return;
		}

		FormatType type = ext == null ? null : ext.getType();

		if (inputFile != null) {
			File file = inputFile.getFile();
			if (file != null) {
				size = file.length();
			} else {
				size = inputFile.getSize();
			}

			ProcessWrapperImpl pw = null;
			boolean ffmpeg_parsing = true;

			if (type == FormatType.AUDIO || ext instanceof AudioAsVideo) {
				ffmpeg_parsing = false;
				DLNAMediaAudio audio = new DLNAMediaAudio();

				if (file != null) {
					try {
						AudioFile af;
						if ("mp2".equals(FileUtil.getExtension(file).toLowerCase(Locale.ROOT))) {
							af = AudioFileIO.readAs(file, "mp3");
						} else {
							// Read using file extension
							af = AudioFileIO.read(file);
						}
						AudioHeader ah = af.getAudioHeader();

						if (ah != null && !thumbOnly) {
							int length = ah.getTrackLength();
							int rate = ah.getSampleRateAsNumber();

							if (ah.getEncodingType() != null && ah.getEncodingType().toLowerCase().contains("flac 24")) {
								audio.setBitsPerSample(24);
							}

							audio.setSampleFrequency(rate);
							durationSec = Integer.valueOf(length).doubleValue();
							bitRate = (int) ah.getBitRateAsNumber();

							String channels = ah.getChannels().trim().toLowerCase(Locale.ROOT);
							if (isNotBlank(channels)) {
								if (channels.equals("1") || channels.contains("mono")) {
									audio.setNumberOfChannels(1);
								} else if (channels.equals("2") || channels.contains("stereo")) {
									audio.setNumberOfChannels(2);
								} else {
									try {
										audio.setNumberOfChannels(Integer.parseInt(channels));
									} catch (IllegalArgumentException e) { // Includes NumberFormatException
										LOGGER.error(
											"Couldn't parse the number of audio channels ({}) for file: \"{}\"",
											channels,
											af.getFile().getName()
										);
									}
								}
							}
							if (audio.isNumberOfChannelsUnknown()) {
								LOGGER.error(
									"Invalid number of audio channels parsed ({}) for file \"{}\", defaulting to stereo",
									audio.getNumberOfChannelsRaw(),
									af.getFile().getName()
								);
								audio.setNumberOfChannels(DLNAMediaAudio.NUMBEROFCHANNELS_DEFAULT); // set default number of channels
							}

							if (isNotBlank(ah.getEncodingType())) {
								audio.setCodecA(ah.getEncodingType());
							}

							if (audio.getCodecA() != null && audio.getCodecA().contains("(windows media")) {
								audio.setCodecA(audio.getCodecA().substring(0, audio.getCodecA().indexOf("(windows media")).trim());
							}
						}

						Tag tag = af.getTag();

						if (tag != null) {
							CoverUtil coverUtil = CoverUtil.get();
							if (tag.getArtworkList().size() > 0) {
								thumb = DLNABinaryThumbnail.toThumbnail(
									tag.getArtworkList().get(0).getBinaryData(),
									640,
									480,
									ScaleType.MAX,
									ImageFormat.SOURCE,
									false
								);
							} else if (coverUtil != null) {
								thumb = coverUtil.getThumbnail(
									af instanceof MP3File ?
										coverUtil.createAudioTagInfo((MP3File) af) :
										coverUtil.createAudioTagInfo(tag)
								);
							}
							if (thumb != null) {
								thumbready = true;
							}

							if (!thumbOnly) {
								audio.setAlbum(tag.getFirst(FieldKey.ALBUM));
								audio.setArtist(tag.getFirst(FieldKey.ARTIST));
								audio.setSongname(tag.getFirst(FieldKey.TITLE));
								String y = tag.getFirst(FieldKey.YEAR);

								try {
									if (y.length() > 4) {
										y = y.substring(0, 4);
									}
									audio.setYear(Integer.parseInt(((y != null && y.length() > 0) ? y : "0")));
									y = tag.getFirst(FieldKey.TRACK);
									audio.setTrack(Integer.parseInt(((y != null && y.length() > 0) ? y : "1")));
									audio.setGenre(tag.getFirst(FieldKey.GENRE));
								} catch (NumberFormatException | KeyNotFoundException e) {
									LOGGER.debug("Error parsing unimportant metadata: " + e.getMessage());
								}
							}
						}
					} catch (CannotReadException e) {
						if (e.getMessage().startsWith(
							ErrorMessage.NO_READER_FOR_THIS_FORMAT.getMsg().substring(0, ErrorMessage.NO_READER_FOR_THIS_FORMAT.getMsg().indexOf("{"))
						)) {
							LOGGER.debug("No audio tag support for audio file \"{}\"", file.getName());
						} else {
							LOGGER.error("Error reading audio tag for \"{}\": {}", file.getName(), e.getMessage());
							LOGGER.trace("", e);
						}
					} catch (IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException | NumberFormatException | KeyNotFoundException e) {
						LOGGER.debug("Error parsing audio file tag for \"{}\": {}", file.getName(), e.getMessage());
						LOGGER.trace("", e);
						ffmpeg_parsing = false;
					}

					// Set container for formats that the normal parsing fails to do from Format
					if (isBlank(container) && ext != null) {
						if (ext.getIdentifier() == Identifier.ADPCM) {
							audio.setCodecA(FormatConfiguration.ADPCM);
						} else if (ext.getIdentifier() == Identifier.DSF) {
							audio.setCodecA(FormatConfiguration.DSF);
						} else if (ext.getIdentifier() == Identifier.DFF) {
							audio.setCodecA(FormatConfiguration.DFF);
						}
					}

					if (isNotBlank(audio.getSongname())) {
						if (renderer != null && renderer.isPrependTrackNumbers() && audio.getTrack() > 0) {
							audio.setSongname(audio.getTrack() + ": " + audio.getSongname());
						}
					} else {
						audio.setSongname(file.getName());
					}

					if (!ffmpeg_parsing) {
						audioTracks.add(audio);
					}
				}
				if (isBlank(container)) {
					container = audio.getCodecA();
				}
			}

			if (type == FormatType.IMAGE && file != null) {
				if (!thumbOnly) {
					try {
						ffmpeg_parsing = false;
						ImagesUtil.parseImage(file, this);
						imageCount++;
					} catch (IOException e) {
						LOGGER.debug("Error parsing image \"{}\", switching to FFmpeg: {}", file.getAbsolutePath(), e.getMessage());
						LOGGER.trace("", e);
						ffmpeg_parsing = true;
					}
				}

				if (thumbOnly && configuration.isThumbnailGenerationEnabled() && configuration.getImageThumbnailsEnabled()) {
					LOGGER.trace("Creating thumbnail for \"{}\"", file.getName());

					// Create the thumbnail image
					try {
						if (imageInfo instanceof ExifInfo && ((ExifInfo) imageInfo).hasExifThumbnail() && !imageInfo.isImageIOSupported()) {
							/*
							 * XXX Extraction of thumbnails was removed in version
							 * 2.10.0 of metadata-extractor because of a bug in
							 * related code. This section is deactivated while
							 * waiting for this to be made available again.
							 *
							 * Images supported by ImageIO or DCRaw aren't affected,
							 * so this only applied to very few images anyway.
							 * It could extract thumbnails for some "raw" images
							 * if DCRaw was disabled.
							 *
							// ImageIO can't read the file, try to get the embedded Exif thumbnail if it's there.
							Metadata metadata;
							try {
								metadata = ImagesUtil.getMetadata(new FileInputStream(file), imageInfo.getFormat());
							} catch (ImageProcessingException e) {
								metadata = null;
								LOGGER.debug("Unexpected error reading metadata for \"{}\": {}", file.getName(), e.getMessage());
								LOGGER.trace("", e);
							}
							thumb = DLNAThumbnail.toThumbnail(
								ImagesUtil.getThumbnailFromMetadata(file, metadata),
								320,
								320,
								ScaleType.MAX,
								ImageFormat.SOURCE,
								false
							);
							if (thumb == null && LOGGER.isTraceEnabled()) {
								LOGGER.trace("Exif thumbnail extraction failed, no thumbnail will be generated for \"{}\"", file.getName());
							}*/
						} else {
							// This will fail with UnknownFormatException for any image formats not supported by ImageIO
							thumb = DLNABinaryThumbnail.toThumbnail(
								Files.newInputStream(file.toPath()),
								320,
								320,
								ScaleType.MAX,
								ImageFormat.SOURCE,
								false
							);
						}
						thumbready = true;
					} catch (EOFException e) {
						LOGGER.debug(
							"Error generating thumbnail for \"{}\": Unexpected end of file, probably corrupt file or read error.",
							file.getName()
						);
					} catch (UnknownFormatException e) {
						LOGGER.debug("Could not generate thumbnail for \"{}\" because the format is unknown: {}", file.getName(), e.getMessage());
					} catch (IOException e) {
						LOGGER.debug("Error generating thumbnail for \"{}\": {}", file.getName(), e.getMessage());
						LOGGER.trace("", e);
					}
				}
			}

			if (ffmpeg_parsing) {
				if (
					!thumbOnly || (
						mediaparsed && getMediaType() == MediaType.VIDEO
					) || (
						!mediaparsed && (
							type == FormatType.VIDEO ||
							type == FormatType.CONTAINER
						) &&
						!configuration.isUseMplayerForVideoThumbs()
					)
				) {
					pw = getFFmpegThumbnail(inputFile, resume);
				}

				String input = "-";

				if (file != null) {
					input = ProcessUtil.getShortFileNameIfWideChars(file.getAbsolutePath());
				}

				synchronized (ffmpeg_failureLock) {
					if (pw != null && !ffmpeg_failure && !thumbOnly) {
						parseFFmpegInfo(pw.getResults(), input);
					}
				}

				if (
					!thumbOnly &&
					container != null &&
					file != null &&
					container.equals("mpegts") &&
					isH264() &&
					getDurationInSeconds() == 0
				) {
					// Parse the duration
					try {
						int length = MpegUtil.getDurationFromMpeg(file);
						if (length > 0) {
							durationSec = Integer.valueOf(length).doubleValue();
						}
					} catch (IOException e) {
						LOGGER.trace("Error retrieving length: " + e.getMessage());
					}
				}

				if (configuration.isUseMplayerForVideoThumbs() && (type == FormatType.VIDEO || type == FormatType.CONTAINER)) {
					try {
						getMplayerThumbnail(inputFile, resume);
						String frameName = "" + inputFile.hashCode();
						frameName = configuration.getTempFolder() + "/mplayer_thumbs/" + frameName + "00000001/00000001.jpg";
						frameName = frameName.replace(',', '_');
						File jpg = new File(frameName);

						if (jpg.exists()) {
							try (InputStream is = new FileInputStream(jpg)) {
								int sz = is.available();

								if (sz > 0) {
									byte[] bytes = new byte[sz];
									is.read(bytes);
									thumb = DLNABinaryThumbnail.toThumbnail(
										bytes,
										640,
										480,
										ScaleType.MAX,
										ImageFormat.SOURCE,
										false
									);
									thumbready = true;
								}
							}

							if (!jpg.delete()) {
								jpg.deleteOnExit();
							}

							// Try and retry
							if (!jpg.getParentFile().delete() && !jpg.getParentFile().delete()) {
								LOGGER.debug("Failed to delete \"" + jpg.getParentFile().getAbsolutePath() + "\"");
							}
						}
					} catch (IOException e) {
						LOGGER.debug("Caught exception", e);
					}
				}

				if (
					pw != null &&
					thumb == null &&
					(type == FormatType.VIDEO || type == FormatType.CONTAINER) &&
					pw.getOutputByteArray() != null
				) {
					byte[] bytes = pw.getOutputByteArray().toByteArray();
					if (bytes != null && bytes.length > 0) {
						try {
							thumb = DLNABinaryThumbnail.toThumbnail(bytes);
						} catch (IOException e) {
							LOGGER.debug("Error while decoding thumbnail: " + e.getMessage());
							LOGGER.trace("", e);
						}
						thumbready = true;
					}
				}
			}

			postParse(inputFile);
			mediaparsed = true;
		}
	}

	/**
	 * Parses media info from FFmpeg's stderr output.
	 *
	 * @param lines the stderr output.
	 * @param input the FFmpeg input (-i) argument used.
	 */
	public void parseFFmpegInfo(List<String> lines, String input) {
		if (lines != null) {
			if ("-".equals(input)) {
				input = "pipe:";
			}

			boolean matches = false;
			int langId = 0;
			int subId = 0;
			ListIterator<String> FFmpegMetaData = lines.listIterator();

			for (String line : lines) {
				FFmpegMetaData.next();
				line = line.trim();
				if (line.startsWith("Output")) {
					matches = false;
				} else if (line.startsWith("Input")) {
					if (line.contains(input)) {
						matches = true;
						container = line.substring(10, line.indexOf(',', 11)).trim();

						/**
						 * This method is very inaccurate because the Input line in the FFmpeg output
						 * returns "mov,mp4,m4a,3gp,3g2,mj2" for all 6 of those formats, meaning that
						 * we think they are all "mov".
						 *
						 * Here we workaround it by using the file extension, but the best idea is to
						 * prevent using this method by using MediaInfo=true in renderer configs.
						 */
						if ("mov".equals(container)) {
							container = line.substring(line.lastIndexOf('.') + 1, line.lastIndexOf('\'')).trim();
							LOGGER.trace("Setting container to " + container + " from the filename. To prevent false-positives, use MediaInfo=true in the renderer config.");
						}
					} else {
						matches = false;
					}
				} else if (matches) {
					if (line.contains("Duration")) {
						StringTokenizer st = new StringTokenizer(line, ",");
						while (st.hasMoreTokens()) {
							String token = st.nextToken().trim();
							if (token.startsWith("Duration: ")) {
								String durationStr = token.substring(10);
								int l = durationStr.substring(durationStr.indexOf('.') + 1).length();
								if (l < 4) {
									durationStr += "00".substring(0, 3 - l);
								}
								if (durationStr.contains("N/A")) {
									durationSec = null;
								} else {
									durationSec = parseDurationString(durationStr);
								}
							} else if (token.startsWith("bitrate: ")) {
								String bitr = token.substring(9);
								int spacepos = bitr.indexOf(' ');
								if (spacepos > -1) {
									String value = bitr.substring(0, spacepos);
									String unit = bitr.substring(spacepos + 1);
									bitRate = Integer.parseInt(value);
									if (unit.equals("kb/s")) {
										bitRate = 1024 * bitRate;
									}
									if (unit.equals("mb/s")) {
										bitRate = 1048576 * bitRate;
									}
								}
							}
						}
					} else if (line.contains("Audio:")) {
						StringTokenizer st = new StringTokenizer(line, ",");
						int a = line.indexOf('(');
						int b = line.indexOf("):", a);
						DLNAMediaAudio audio = new DLNAMediaAudio();
						audio.setId(langId++);
						if (a > -1 && b > a) {
							audio.setLang(line.substring(a + 1, b));
						} else {
							audio.setLang(DLNAMediaLang.UND);
						}

						// Get TS IDs
						a = line.indexOf("[0x");
						b = line.indexOf(']', a);
						if (a > -1 && b > a + 3) {
							String idString = line.substring(a + 3, b);
							try {
								audio.setId(Integer.parseInt(idString, 16));
							} catch (NumberFormatException nfe) {
								LOGGER.debug("Error parsing Stream ID: " + idString);
							}
						}

						while (st.hasMoreTokens()) {
							String token = st.nextToken().trim();
							if (token.startsWith("Stream")) {
								audio.setCodecA(token.substring(token.indexOf("Audio: ") + 7));
							} else if (token.endsWith("Hz")) {
								try {
									audio.setSampleFrequency(Integer.parseInt(token.substring(0, token.indexOf("Hz")).trim()));
								} catch (NumberFormatException e) {
									LOGGER.warn("Can't parse the sample rate from \"{}\"", token);
								}
							} else if (token.equals("mono")) {
								audio.setNumberOfChannels(1);
							} else if (token.equals("stereo")) {
								audio.setNumberOfChannels(2);
							} else if (token.equals("5:1") || token.equals("5.1") || token.equals("6 channels")) {
								audio.setNumberOfChannels(6);
							} else if (token.equals("5 channels")) {
								audio.setNumberOfChannels(5);
							} else if (token.equals("4 channels")) {
								audio.setNumberOfChannels(4);
							} else if (token.equals("2 channels")) {
								audio.setNumberOfChannels(2);
							} else if (token.equals("s32")) {
								audio.setBitsPerSample(32);
							} else if (token.equals("s24")) {
								audio.setBitsPerSample(24);
							} else if (token.equals("s16")) {
								audio.setBitsPerSample(16);
							}
						}
						int FFmpegMetaDataNr = FFmpegMetaData.nextIndex();

						if (FFmpegMetaDataNr > -1) {
							line = lines.get(FFmpegMetaDataNr);
						}

						if (line.contains("Metadata:")) {
							FFmpegMetaDataNr += 1;
							line = lines.get(FFmpegMetaDataNr);
							while (line.indexOf("      ") == 0) {
								if (line.toLowerCase().contains("title           :")) {
									int aa = line.indexOf(": ");
									int bb = line.length();
									if (aa > -1 && bb > aa) {
										audio.setAudioTrackTitleFromMetadata(line.substring(aa + 2, bb));
										break;
									}
								} else {
									FFmpegMetaDataNr += 1;
									line = lines.get(FFmpegMetaDataNr);
								}
							}
						}

						audioTracks.add(audio);
					} else if (line.contains("Video:")) {
						StringTokenizer st = new StringTokenizer(line, ",");
						while (st.hasMoreTokens()) {
							String token = st.nextToken().trim();
							if (token.startsWith("Stream")) {
								codecV = token.substring(token.indexOf("Video: ") + 7);
								videoTrackCount++;
							} else if ((token.contains("tbc") || token.contains("tb(c)"))) {
								// A/V sync issues with newest FFmpeg, due to the new tbr/tbn/tbc outputs
								// Priority to tb(c)
								String frameRateDoubleString = token.substring(0, token.indexOf("tb")).trim();
								try {
									if (!frameRateDoubleString.equals(frameRate)) {// tbc taken into account only if different than tbr
										double frameRateDouble = Double.valueOf(frameRateDoubleString);
										frameRate = String.format(Locale.ENGLISH, "%.2f", frameRateDouble / 2);
									}
								} catch (NumberFormatException nfe) {
									// Could happen if tbc is "1k" or something like that, no big deal
									LOGGER.debug("Could not parse framerate \"" + frameRateDoubleString + "\"");
								}

							} else if ((token.contains("tbr") || token.contains("tb(r)")) && frameRate == null) {
								frameRate = token.substring(0, token.indexOf("tb")).trim();
							} else if ((token.contains("fps") || token.contains("fps(r)")) && frameRate == null) { // dvr-ms ?
								frameRate = token.substring(0, token.indexOf("fps")).trim();
							} else if (token.indexOf('x') > -1 && !token.contains("max")) {
								String resolution = token.trim();
								if (resolution.contains(" [")) {
									resolution = resolution.substring(0, resolution.indexOf(" ["));
								}
								try {
									width = Integer.parseInt(resolution.substring(0, resolution.indexOf('x')));
								} catch (NumberFormatException nfe) {
									LOGGER.debug("Could not parse width from \"" + resolution.substring(0, resolution.indexOf('x')) + "\"");
								}
								try {
									height = Integer.parseInt(resolution.substring(resolution.indexOf('x') + 1));
								} catch (NumberFormatException nfe) {
									LOGGER.debug("Could not parse height from \"" + resolution.substring(resolution.indexOf('x') + 1) + "\"");
								}
							}
						}
					} else if (line.contains("Subtitle:")) {
						DLNAMediaSubtitle lang = new DLNAMediaSubtitle();

						// $ ffmpeg -codecs | grep "^...S"
						// ..S... = Subtitle codec
						// DES... ass                  ASS (Advanced SSA) subtitle
						// DES... dvb_subtitle         DVB subtitles (decoders: dvbsub ) (encoders: dvbsub )
						// ..S... dvb_teletext         DVB teletext
						// DES... dvd_subtitle         DVD subtitles (decoders: dvdsub ) (encoders: dvdsub )
						// ..S... eia_608              EIA-608 closed captions
						// D.S... hdmv_pgs_subtitle    HDMV Presentation Graphic Stream subtitles (decoders: pgssub )
						// D.S... jacosub              JACOsub subtitle
						// D.S... microdvd             MicroDVD subtitle
						// DES... mov_text             MOV text
						// D.S... mpl2                 MPL2 subtitle
						// D.S... pjs                  PJS (Phoenix Japanimation Society) subtitle
						// D.S... realtext             RealText subtitle
						// D.S... sami                 SAMI subtitle
						// DES... srt                  SubRip subtitle with embedded timing
						// DES... ssa                  SSA (SubStation Alpha) subtitle
						// DES... subrip               SubRip subtitle
						// D.S... subviewer            SubViewer subtitle
						// D.S... subviewer1           SubViewer v1 subtitle
						// D.S... text                 raw UTF-8 text
						// D.S... vplayer              VPlayer subtitle
						// D.S... webvtt               WebVTT subtitle
						// DES... xsub                 XSUB

						if (line.contains("srt") || line.contains("subrip")) {
							lang.setType(SubtitleType.SUBRIP);
						} else if (line.contains(" text")) {
							// excludes dvb_teletext, mov_text, realtext
							lang.setType(SubtitleType.TEXT);
						} else if (line.contains("microdvd")) {
							lang.setType(SubtitleType.MICRODVD);
						} else if (line.contains("sami")) {
							lang.setType(SubtitleType.SAMI);
						} else if (line.contains("ass") || line.contains("ssa")) {
							lang.setType(SubtitleType.ASS);
						} else if (line.contains("dvd_subtitle")) {
							lang.setType(SubtitleType.VOBSUB);
						} else if (line.contains("xsub")) {
							lang.setType(SubtitleType.DIVX);
						} else if (line.contains("mov_text")) {
							lang.setType(SubtitleType.TX3G);
						} else if (line.contains("webvtt")) {
							lang.setType(SubtitleType.WEBVTT);
						} else {
							lang.setType(SubtitleType.UNKNOWN);
						}

						int a = line.indexOf('(');
						int b = line.indexOf("):", a);
						if (a > -1 && b > a) {
							lang.setLang(line.substring(a + 1, b));
						} else {
							lang.setLang(DLNAMediaLang.UND);
						}

						lang.setId(subId++);
						int FFmpegMetaDataNr = FFmpegMetaData.nextIndex();

						if (FFmpegMetaDataNr > -1) {
							line = lines.get(FFmpegMetaDataNr);
						}

						if (line.contains("Metadata:")) {
							FFmpegMetaDataNr += 1;
							line = lines.get(FFmpegMetaDataNr);

							while (line.indexOf("      ") == 0) {
								if (line.toLowerCase().contains("title           :")) {
									int aa = line.indexOf(": ");
									int bb = line.length();
									if (aa > -1 && bb > aa) {
										lang.setSubtitlesTrackTitleFromMetadata(line.substring(aa + 2, bb));
										break;
									}
								} else {
									FFmpegMetaDataNr += 1;
									line = lines.get(FFmpegMetaDataNr);
								}
							}
						}
						subtitleTracks.add(lang);
					}
				}
			}
		}
		ffmpegparsed = true;
	}

	/**
	 * Returns whether this media contains a H.264 (AVC) video.
	 *
	 * @return {@code true} if this media contains a H.264 video stream,
	 *         {@code false} otherwise.
	 */
	public boolean isH264() {
		return FormatConfiguration.H264.equals(codecV);
	}

	/**
	 * Returns whether this media contains a H.265 (HEVC) video.
	 *
	 * @return {@code true} if this media contains a H.265 video stream,
	 *         {@code false} otherwise.
	 */
	public boolean isH265() {
		return FormatConfiguration.H265.equals(codecV) || codecV != null && codecV.startsWith("hevc");
	}

	/**
	 * Returns whether this media contains a MPEG-4 Visual video.
	 *
	 * @return {@code true} if this media contains a MPEG-4 Visual video stream,
	 *         {@code false} otherwise.
	 */
	public boolean isMPEG4() {
		return FormatConfiguration.MPEG4ASP.equals(codecV) || FormatConfiguration.MPEG4SP.equals(codecV);
	}

	/**
	 * Disable LPCM transcoding for MP4 container with non-H264 video as
	 * a workaround for MEncoder's A/V sync bug.
	 */
	public boolean isValidForLPCMTranscoding() {
		if (container != null) {
			if (container.equals("mp4")) {
				return isH264();
			}
			return true;
		}

		return false;
	}

	public int getFrameNumbers() {
		double fr = Double.parseDouble(frameRate);
		return (int) (getDurationInSeconds() * fr);
	}

	public void setDuration(@Nullable Double durationSec) {
		this.durationSec = durationSec;
	}

	/**
	 * Returns the duration in seconds as a {@link Double} or {@code null} if
	 * unknown.
	 * <p>
	 * <b>To avoid the possibility of a {@code null} value</b>, use
	 * {@link #getDurationInSeconds()} instead.
	 *
	 * @return The duration in seconds or {@code null}.
	 */
	@Nullable
	public Double getDuration() {
		return durationSec;
	}

	/**
	 * Returns the duration in seconds as a {@code double} or {@code 0} if
	 * unknown.
	 * <p>
	 * To get {@code null} if unknown, use {@link #getDuration()}.
	 *
	 * @return The duration in seconds or {@code 0}.
	 */
	public double getDurationInSeconds() {
		return durationSec != null ? durationSec.doubleValue() : 0d;
	}

	@Nullable
	public String getDurationString() {
		return durationSec != null ? formatDLNADuration(durationSec) : null;
	}

	/**
	 * @deprecated Use {@link StringUtil#formatDLNADuration} instead.
	 */
	@Deprecated
	public static String getDurationString(double d) {
		return formatDLNADuration(d);
	}

	@Nullable
	public static Double parseDurationString(@Nullable String duration) {
		return duration != null ? convertStringToTime(duration) : null;
	}

	public void postParse(InputFile f) {
		if (container != null) {
			switch (container) {
				case FormatConfiguration.THREEGPP:
					mimeType = HTTPResource.THREEGPP_TYPEMIME;
					break;
				case FormatConfiguration.THREEGPP2:
					mimeType = HTTPResource.THREEGPP2_TYPEMIME;
					break;
				case FormatConfiguration.AVI:
					mimeType = HTTPResource.AVI_TYPEMIME;
					break;
				case FormatConfiguration.ASF:
				case FormatConfiguration.DVRMS:
					mimeType = HTTPResource.ASF_TYPEMIME;
					break;
				case FormatConfiguration.DIVX:
					mimeType = HTTPResource.DIVX_TYPEMIME;
					break;
				case FormatConfiguration.FLV:
					mimeType = HTTPResource.FLV_TYPEMIME;
					break;
				case FormatConfiguration.MJP2:
					mimeType = HTTPResource.MJP2_TYPEMIME;
					break;
				case FormatConfiguration.MKV:
					mimeType = HTTPResource.MATROSKA_TYPEMIME;
					break;
				case FormatConfiguration.MOV:
					mimeType = HTTPResource.MOV_TYPEMIME;
					break;
				case FormatConfiguration.MP4:
					mimeType = HTTPResource.MP4_TYPEMIME;
					break;
				case FormatConfiguration.MPEGPS:
				case FormatConfiguration.MPEG1:
				case FormatConfiguration.MPEG2:
					mimeType = HTTPResource.MPEG_TYPEMIME;
					break;
				case FormatConfiguration.MPEGTS:
					mimeType = HTTPResource.MPEGTS_TYPEMIME;
					break;
				case FormatConfiguration.MXF:
					mimeType = HTTPResource.MXF_TYPEMIME;
					break;
				case FormatConfiguration.OGG:
					mimeType = HTTPResource.OGG_TYPEMIME;
					break;
				case FormatConfiguration.RM:
					mimeType = HTTPResource.RM_TYPEMIME;
					break;
				case FormatConfiguration.WEBM:
					mimeType = HTTPResource.AUDIO_WEBM_TYPEMIME;
					break;
				case FormatConfiguration.WMV:
					mimeType = HTTPResource.WMV_TYPEMIME;
					break;
				case FormatConfiguration.AC3:
					mimeType = HTTPResource.AUDIO_AC3_TYPEMIME;
					break;
				case FormatConfiguration.ADPCM:
					mimeType = HTTPResource.AUDIO_ADPCM_TYPEMIME;
					break;
				case FormatConfiguration.ADTS:
					mimeType = HTTPResource.AUDIO_ADTS_TYPEMIME;
					break;
				case FormatConfiguration.AIFF:
					mimeType = HTTPResource.AUDIO_AIFF_TYPEMIME;
					break;
				case FormatConfiguration.AIFC:
					mimeType = HTTPResource.AUDIO_AIFC_TYPEMIME;
					break;
				case FormatConfiguration.AMR:
					mimeType = HTTPResource.AUDIO_AMR_TYPEMIME;
					break;
				case FormatConfiguration.ATRAC:
					mimeType = HTTPResource.AUDIO_ATRAC_TYPEMIME;
					break;
				case FormatConfiguration.AU:
					mimeType = HTTPResource.AUDIO_AU_TYPEMIME;
					break;
				case FormatConfiguration.CAF:
					mimeType = HTTPResource.AUDIO_CAF_TYPEMIME;
					break;
				case FormatConfiguration.DFF:
					mimeType = HTTPResource.AUDIO_DFF_TYPEMIME;
					break;
				case FormatConfiguration.DSF:
					mimeType = HTTPResource.AUDIO_DSF_TYPEMIME;
					break;
				case FormatConfiguration.DTS:
					mimeType = HTTPResource.AUDIO_DTS_TYPEMIME;
					break;
				case FormatConfiguration.DTSHD:
					mimeType = HTTPResource.AUDIO_DTSHD_TYPEMIME;
					break;
				case FormatConfiguration.EAC3:
					mimeType = HTTPResource.AUDIO_EAC3_TYPEMIME;
					break;
				case FormatConfiguration.FLAC:
					mimeType = HTTPResource.AUDIO_FLAC_TYPEMIME;
					break;
				case FormatConfiguration.LPCM:
					mimeType = HTTPResource.AUDIO_LPCM_TYPEMIME;
					break;
				case FormatConfiguration.M4A:
					mimeType = HTTPResource.AUDIO_M4A_TYPEMIME;
					break;
				case FormatConfiguration.MPA:
				case FormatConfiguration.MP1:
				case FormatConfiguration.MP2:
					mimeType = HTTPResource.AUDIO_MPA_TYPEMIME;
					break;
				case FormatConfiguration.MP3:
					mimeType = HTTPResource.AUDIO_MP3_TYPEMIME;
					break;
				case FormatConfiguration.MKA:
					mimeType = HTTPResource.AUDIO_MKA_TYPEMIME;
					break;
				case FormatConfiguration.MLP:
					mimeType = HTTPResource.AUDIO_MLP_TYPEMIME;
					break;
				case FormatConfiguration.MONKEYS_AUDIO:
					mimeType = HTTPResource.AUDIO_APE_TYPEMIME;
					break;
				case FormatConfiguration.MPC:
					mimeType = HTTPResource.AUDIO_MPC_TYPEMIME;
					break;
				case FormatConfiguration.OGA:
					mimeType = HTTPResource.AUDIO_OGA_TYPEMIME;
					break;
				case FormatConfiguration.RA:
					mimeType = HTTPResource.AUDIO_RA_TYPEMIME;
					break;
				case FormatConfiguration.SHORTEN:
					mimeType = HTTPResource.AUDIO_SHN_TYPEMIME;
					break;
				case FormatConfiguration.THREEGA:
					mimeType = HTTPResource.AUDIO_THREEGPPA_TYPEMIME;
					break;
				case FormatConfiguration.THREEG2A:
					mimeType = HTTPResource.AUDIO_THREEGPP2A_TYPEMIME;
					break;
				case FormatConfiguration.TRUEHD:
					mimeType = HTTPResource.AUDIO_TRUEHD_TYPEMIME;
					break;
				case FormatConfiguration.TTA:
					mimeType = HTTPResource.AUDIO_TTA_TYPEMIME;
					break;
				case FormatConfiguration.WAV:
					mimeType = HTTPResource.AUDIO_WAV_TYPEMIME;
					break;
				case FormatConfiguration.WAVPACK:
					mimeType = HTTPResource.AUDIO_WV_TYPEMIME;
					break;
				case FormatConfiguration.WEBA:
					mimeType = HTTPResource.AUDIO_WEBM_TYPEMIME;
					break;
				case FormatConfiguration.WMA:
				case FormatConfiguration.WMA10:
					mimeType = HTTPResource.AUDIO_WMA_TYPEMIME;
					break;
				case FormatConfiguration.BMP:
					mimeType = HTTPResource.BMP_TYPEMIME;
					break;
				case FormatConfiguration.GIF:
					mimeType = HTTPResource.GIF_TYPEMIME;
					break;
				case FormatConfiguration.JPG:
					mimeType = HTTPResource.JPEG_TYPEMIME;
					break;
				case FormatConfiguration.PNG:
					mimeType = HTTPResource.PNG_TYPEMIME;
					break;
				case FormatConfiguration.TIFF:
					mimeType = HTTPResource.TIFF_TYPEMIME;
					break;
				case FormatConfiguration.WEBP:
					mimeType = HTTPResource.WEBP_TYPEMIME;
					break;
			}
		}

		if (mimeType == null) {
			mimeType = HTTPResource.getDefaultMimeType(getMediaType());
		}

		MediaType mediaType = getMediaType();

		if (
			getFirstAudioTrack() == null || !(
				mediaType == MediaType.AUDIO &&
				getFirstAudioTrack().getBitsPerSample() == 24 &&
				getFirstAudioTrack().getSampleFrequency() > 48000
		)) {
			secondaryFormatValid = false;
		}

		// Check for external subs here
		if (f.getFile() != null && mediaType == MediaType.VIDEO && configuration.isAutoloadExternalSubtitles()) {
			FileUtil.isSubtitlesExists(f.getFile(), this);
		}
	}

	public boolean isLossless(String codecA) {
		return
			codecA != null && (
				codecA.startsWith("pcm") ||
				codecA.startsWith("dts") ||
				codecA.startsWith("a_dts") ||
				codecA.equals("dca") ||
				codecA.contains("flac") ||
				"19d".equals(codecA)
			) &&
			!codecA.contains("pcm_u8") &&
			!codecA.contains("pcm_s8");
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		if (getContainer() != null) {
			result.append("Container: ").append(getContainer().toUpperCase(Locale.ROOT)).append(", ");
		}
		result.append("Size: ").append(getSize());
		if (isVideo()) {
			result.append(", Video Bitrate: ").append(getBitRate());
			if (bitRateMode != null) {
				result.append(", Video Bitrate Mode: ").append(bitRateMode);
			}
			result.append(", Video Tracks: ").append(getVideoTrackCount());
			result.append(", codecV: ").append(getCodecV());
			result.append(", Video Codec: ").append(getVideoCodec());
			if (isNotBlank(videoProfile)) {
				result.append(", Video Format Profile: ").append(videoProfile);
			}
			if (videoLevel != null) {
				result.append(", Video Level: ").append(videoLevel.toString(false));
			}
			result.append(", Duration: ").append(getDurationString());
			result.append(", Video Resolution: ").append(getWidth()).append(" x ").append(getHeight());
			if (aspectRatioContainer != null) {
				result.append(", Display Aspect Ratio: ").append(aspectRatioContainer.toAspectRatio());
			}
			if (pixelAspectRatio != null && !Rational.ONE.equals(pixelAspectRatio)) {
				result.append(", Pixel Aspect Ratio: ");
				if (pixelAspectRatio.isInteger()) {
					result.append(pixelAspectRatio.toDebugString());
				} else {
					result.append(pixelAspectRatio.toDecimalString(
						new DecimalFormat("#0.##", DecimalFormatSymbols.getInstance(Locale.ROOT))
					));
					result.append(" (").append(pixelAspectRatio.toString()).append(")");
				}
			}
			if (scanType != null) {
				result.append(", Scan Type: ").append(getScanType());
			}
			if (scanOrder != null) {
				result.append(", Scan Order: ").append(getScanOrder());
			}
			if (isNotBlank(getFrameRate())) {
				result.append(", Framerate: ").append(getFrameRate());
			}
			if (isNotBlank(getFrameRateOriginal())) {
				result.append(", Original Framerate: ").append(getFrameRateOriginal());
			}
			if (isNotBlank(getFrameRateMode())) {
				result.append(", Framerate Mode: ").append(getFrameRateMode());
			}
			if (isNotBlank(getFrameRateModeRaw()) && !getFrameRateModeRaw().equals(getFrameRateMode())) {
				result.append(", Raw Framerate Mode: ").append(getFrameRateModeRaw());
			}
			if (isNotBlank(getMuxingMode())) {
				result.append(", Muxing Mode: ").append(getMuxingMode());
			}
			if (isNotBlank(getMatrixCoefficients())) {
				result.append(", Matrix Coefficients: ").append(getMatrixCoefficients());
			}
			if (getVideoBitDepth() != 8) {
				result.append(", Video Bit Depth: ").append(getVideoBitDepth());
			}
			if (isNotBlank(getFileTitleFromMetadata())) {
				result.append(", File Title from Metadata: ").append(getFileTitleFromMetadata());
			}
			if (isNotBlank(getVideoTrackTitleFromMetadata())) {
				result.append(", Video Track Title from Metadata: ").append(getVideoTrackTitleFromMetadata());
			}

			if (getAudioTrackCount() > 0) {
				appendAudioTracks(result);
			}

			if (hasSubtitles()) {
				appendSubtitleTracks(result);
			}

		} else if (getAudioTrackCount() > 0) {
			result.append(", Bitrate: ").append(getBitRate());
			if (bitRateMode != null) {
				result.append(", Bitrate Mode: ").append(bitRateMode);
			}
			result.append(", Duration: ").append(getDurationString());
			appendAudioTracks(result);
		}
		if (getImageCount() > 0) {
			if (getImageCount() > 1) {
				result.append(", Images: ").append(getImageCount());
			}
			if (getImageInfo() != null) {
				result.append(", ").append(getImageInfo());
			} else {
				result.append(", Image Width: ").append(getWidth());
				result.append(", Image Height: ").append(getHeight());
			}
		}

		if (getThumb() != null) {
			result.append(", ").append(getThumb());
		}

		result.append(", Mime Type: ").append(getMimeType());

		return result.toString();
	}

	public void appendAudioTracks(StringBuilder sb) {
		sb.append(", Audio Tracks: ").append(getAudioTrackCount());
		for (DLNAMediaAudio audio : audioTracks) {
			if (!audio.equals(audioTracks.get(0))) {
				sb.append(",");
			}
			sb.append(" [").append(audio).append("]");
		}
	}

	public void appendSubtitleTracks(StringBuilder sb) {
		sb.append(", Subtitle Tracks: ").append(getSubTrackCount());
		for (DLNAMediaSubtitle subtitleTrack : subtitleTracks) {
			if (!subtitleTrack.equals(subtitleTracks.get(0))) {
				sb.append(",");
			}
			sb.append(" [").append(subtitleTrack).append("]");
		}
	}

	public DLNAThumbnailInputStream getThumbnailInputStream() {
		return thumb != null && thumb.getBytes(false) != null ? new DLNAThumbnailInputStream(thumb) : null;
	}

	public String getValidFps(boolean ratios) {
		String validFrameRate = null;

		if (frameRate != null && frameRate.length() > 0) {
			try {
				double fr = Double.parseDouble(frameRate.replace(',', '.'));

				if (fr >= 14.99 && fr < 15.1) {
					validFrameRate = "15";
				} else if (fr > 23.9 && fr < 23.99) {
					validFrameRate = ratios ? "24000/1001" : "23.976";
				} else if (fr > 23.99 && fr < 24.1) {
					validFrameRate = "24";
				} else if (fr >= 24.99 && fr < 25.1) {
					validFrameRate = "25";
				} else if (fr > 29.9 && fr < 29.99) {
					validFrameRate = ratios ? "30000/1001" : "29.97";
				} else if (fr >= 29.99 && fr < 30.1) {
					validFrameRate = "30";
				} else if (fr > 47.9 && fr < 47.99) {
					validFrameRate = ratios ? "48000/1001" : "47.952";
				} else if (fr > 49.9 && fr < 50.1) {
					validFrameRate = "50";
				} else if (fr > 59.8 && fr < 59.99) {
					validFrameRate = ratios ? "60000/1001" : "59.94";
				} else if (fr >= 59.99 && fr < 60.1) {
					validFrameRate = "60";
				}
			} catch (NumberFormatException nfe) {
				LOGGER.error(null, nfe);
			}
		}

		return validFrameRate;
	}

	public DLNAMediaAudio getFirstAudioTrack() {
		if (audioTracks.size() > 0) {
			return audioTracks.get(0);
		}
		return null;
	}

	/**
	 * @deprecated use getAspectRatioMencoderMpegopts() for the original
	 * functionality of this method, or use getAspectRatioContainer() for a
	 * better default method to get aspect ratios.
	 */
	@Deprecated
	public String getValidAspect(boolean ratios) {
		return getAspectRatioMencoderMpegopts(ratios);
	}

	/**
	 * Converts the result of getAspectRatioDvdIso() to provide
	 * MEncoderVideo with a valid value for the "vaspect" option in the
	 * "-mpegopts" command.
	 *
	 * Note: Our code never uses a false value for "ratios", so unless any
	 * plugins rely on it we can simplify things by removing that parameter.
	 *
	 * @param ratios
	 * @return
	 */
	public String getAspectRatioMencoderMpegopts(boolean ratios) {
		String a = null;

		if (aspectRatioDvdIso != null) {
			double aspectRatio = aspectRatioDvdIso.doubleValue();

			if (aspectRatio > 1.7 && aspectRatio < 1.8) {
				a = ratios ? "16/9" : "1.777777777777777";
			}

			if (aspectRatio > 1.3 && aspectRatio < 1.4) {
				a = ratios ? "4/3" : "1.333333333333333";
			}
		}

		return a;
	}

	public String getResolution() {
		if (width > 0 && height > 0) {
			return width + "x" + height;
		}

		return null;
	}

	public int getRealVideoBitRate() {
		if (bitRate > 0) {
			return (bitRate / 8);
		}

		int realBitRate = 10000000;

		if (getDurationInSeconds() > 0) {
			realBitRate = (int) (size / getDurationInSeconds());
		}

		return realBitRate;
	}

	public boolean isHDVideo() {
		return (width > 864 || height > 576);
	}

	public boolean isMpegTS() {
		return container != null && container.equals("mpegts");
	}

	@Override
	protected DLNAMediaInfo clone() throws CloneNotSupportedException {
		DLNAMediaInfo mediaCloned = (DLNAMediaInfo) super.clone();
		mediaCloned.setAudioTracksList(new ArrayList<DLNAMediaAudio>());
		for (DLNAMediaAudio audio : audioTracks) {
			mediaCloned.getAudioTracksList().add((DLNAMediaAudio) audio.clone());
		}

		mediaCloned.setSubtitleTracksList(new ArrayList<DLNAMediaSubtitle>());
		for (DLNAMediaSubtitle sub : subtitleTracks) {
			mediaCloned.getSubtitleTracksList().add((DLNAMediaSubtitle) sub.clone());
		}

		return mediaCloned;
	}

	/**
	 * Returns the bitrate for this media.
	 *
	 * @return The bitrate.
	 * @since 1.50.0
	 */
	public int getBitRate() {
		return bitRate;
	}

	/**
	 * @param bitRate the bitrate to set.
	 * @since 1.50.0
	 */
	public void setBitRate(int bitRate) {
		this.bitRate = bitRate;
	}

	/**
	 * @return the bitrate mode.
	 */
	@Nullable
	public RateMode getBitRateMode() {
		return bitRateMode;
	}

	/**
	 * Sets the bitrate mode.
	 *
	 * @param bitRateMode the bitrate mode to set.
	 */
	public void setBitRateMode(@Nullable RateMode bitRateMode) {
		this.bitRateMode = bitRateMode;
	}

	/**
	 * @return The width.
	 * @since 1.50.0
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * @param width the width to set.
	 * @since 1.50.0
	 */
	public void setWidth(int width) {
		this.width = width;
	}

	/**
	 * @return The height.
	 * @since 1.50.0
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * @param height the height to set.
	 * @since 1.50.0
	 */
	public void setHeight(int height) {
		this.height = height;
	}

	/**
	 * @return The size.
	 * @since 1.50.0
	 */
	public long getSize() {
		return size;
	}

	/**
	 * @param size the size to set.
	 * @since 1.50.0
	 */
	public void setSize(long size) {
		this.size = size;
	}

	/**
	 * @return the codecV
	 * @since 1.50.0
	 */
	public String getCodecV() {
		return codecV;
	}

	/**
	 * Gets the {@link VideoCodec} for this media.
	 * <p>
	 * <b>Note:</b> This is currently a "transitional method" which parses the
	 * {@link VideoCodec} based on the old {@link String}-based video codec
	 * value. If the parsing fails, {@code null} will be returned. That doesn't
	 * necessarily mean that no {@code codecV} is set.
	 *
	 * @return The {@link VideoCodec} or {@code null}.
	 */
	@Nullable
	public VideoCodec getVideoCodec() {
		return codecV == null ? null : VideoCodec.typeOf(codecV);
	}

	/**
	 * @param codecV the codecV to set.
	 * @since 1.50.0
	 */
	public void setCodecV(String codecV) {
		this.codecV = codecV != null ? codecV.toLowerCase(Locale.ROOT) : null ;
	}

	/**
	 * @return The framerate.
	 * @since 1.50.0
	 */
	public String getFrameRate() {
		return frameRate;
	}

	/**
	 * @param frameRate the framerate to set.
	 * @since 1.50.0
	 */
	public void setFrameRate(String frameRate) {
		this.frameRate = frameRate;
	}

	/**
	 * @return The original framerate.
	 */
	public String getFrameRateOriginal() {
		return frameRateOriginal;
	}

	/**
	 * @param frameRateOriginal the frameRateOriginal to set.
	 */
	public void setFrameRateOriginal(String frameRateOriginal) {
		this.frameRateOriginal = frameRateOriginal;
	}

	/**
	 * @return The framerate mode.
	 * @since 1.55.0
	 */
	public String getFrameRateMode() {
		return frameRateMode;
	}

	/**
	 * @param frameRateMode the framerate mode to set.
	 * @since 1.55.0
	 */
	public void setFrameRateMode(String frameRateMode) {
		this.frameRateMode = frameRateMode;
	}

	/**
	 * @return The unaltered framerate mode.
	 */
	public String getFrameRateModeRaw() {
		return frameRateModeRaw;
	}

	/**
	 * @param frameRateModeRaw the unaltered framerate mode to set.
	 */
	public void setFrameRateModeRaw(String frameRateModeRaw) {
		this.frameRateModeRaw = frameRateModeRaw;
	}

	/**
	 * @return The video bit depth.
	 */
	public int getVideoBitDepth() {
		return videoBitDepth;
	}

	/**
	 * @param value the video bit depth to set.
	 */
	public void setVideoBitDepth(int value) {
		this.videoBitDepth = value;
	}

	/**
	 * @return The pixel aspect ratio.
	 */
	public Rational getPixelAspectRatio() {
		return pixelAspectRatio;
	}

	/**
	 * Sets the pixel aspect ratio by parsing the specified {@link String}.
	 *
	 * @param pixelAspectRatio the pixel aspect ratio to set.
	 * @throws NumberFormatException If {@code pixelAspectRatio} cannot be
	 *             parsed.
	 */
	public void setPixelAspectRatio(String pixelAspectRatio) {
		setPixelAspectRatio(Rational.valueOf(pixelAspectRatio));
	}

	/**
	 * Sets the pixel aspect ratio.
	 *
	 * @param pixelAspectRatio the pixel aspect ratio to set.
	 */
	public void setPixelAspectRatio(Rational pixelAspectRatio) {
		if (Rational.isNotBlank(pixelAspectRatio)) {
			this.pixelAspectRatio = pixelAspectRatio;
		} else {
			this.pixelAspectRatio = null;
		}
	}

	/**
	 * @return the {@link ScanType}.
	 */
	@Nullable
	public ScanType getScanType() {
		return scanType;
	}

	/**
	 * Sets the {@link ScanType}.
	 *
	 * @param scanType the {@link ScanType} to set.
	 */
	public void setScanType(@Nullable ScanType scanType) {
		this.scanType = scanType;
	}

	/**
	 * Sets the {@link ScanType} by parsing the specified {@link String}.
	 *
	 * @param scanType the {@link String} to parse.
	 */
	public void setScanType(@Nullable String scanType) {
		this.scanType = ScanType.typeOf(scanType);
	}

	/**
	 * @return the {@link ScanOrder}.
	 */
	@Nullable
	public ScanOrder getScanOrder() {
		return scanOrder;
	}

	/**
	 * Sets the {@link ScanOrder}.
	 *
	 * @param scanOrder the {@link ScanOrder} to set.
	 */
	public void setScanOrder(@Nullable ScanOrder scanOrder) {
		this.scanOrder = scanOrder;
	}

	/**
	 * Sets the {@link ScanOrder} by parsing the specified {@link String}.
	 *
	 * @param scanOrder the {@link String} to parse.
	 */
	public void setScanOrder(@Nullable String scanOrder) {
		this.scanOrder = ScanOrder.typeOf(scanOrder);
	}

	/**
	 * @deprecated use getAspectRatioDvdIso() for the original.
	 * functionality of this method, or use getAspectRatioContainer() for a
	 * better default method to get aspect ratios.
	 */
	@Deprecated
	public String getAspect() {
		return getAspectRatioDvdIso().toAspectRatio();
	}

	/**
	 * The aspect ratio for a {@code DVD ISO} video track.
	 *
	 * @return The aspect ratio.
	 * @since 1.50.0
	 */
	public Rational getAspectRatioDvdIso() {
		return aspectRatioDvdIso;
	}

	/**
	 * Sets the aspect ratio for a {@code DVD ISO} video track by parsing the
	 * specified {@link String}.
	 *
	 * @param aspectRatio the aspect ratio to set.
	 * @throws NumberFormatException If {@code aspectRatio} cannot be parsed.
	 */
	public void setAspectRatioDvdIso(String aspectRatio) {
		if (isBlank(aspectRatio)) {
			aspectRatioDvdIso = null;
			return;
		}
		if (Rational.RATIONAL_SEPARATOR.matcher(aspectRatio).find()) {
			setAspectRatioDvdIso(Rational.valueOf(aspectRatio));
			return;
		}
		try {
			setAspectRatioDvdIso(Rational.aspectRatioValueOf(Double.parseDouble(aspectRatio)));
		} catch (NumberFormatException e) {
			setAspectRatioDvdIso(Rational.valueOf(aspectRatio));
		}
	}

	/**
	 * Sets the aspect ratio for a {@code DVD ISO} video track.
	 *
	 * @param aspectRatio the aspect ratio to set.
	 * @since 1.50.0
	 */
	public void setAspectRatioDvdIso(Rational aspectRatio) {
		if (Rational.isNotBlank(aspectRatio)) {
			aspectRatioDvdIso = aspectRatio;
		} else {
			aspectRatioDvdIso = null;
		}
	}

	/**
	 * Get the aspect ratio reported by the file/container. This is the aspect
	 * ratio that the renderer should display the video at, and is usually the
	 * same as the video track aspect ratio.
	 *
	 * @return The aspect ratio reported by the file/container.
	 */
	public Rational getAspectRatioContainer() {
		return aspectRatioContainer;
	}

	/**
	 * Sets the aspect ratio reported by the file/container by parsing the
	 * specified {@link String}.
	 *
	 * @param aspectRatio the aspect ratio to set.
	 * @throws NumberFormatException If {@code aspectRatio} cannot be parsed.
	 */
	public void setAspectRatioContainer(String aspectRatio) {
		if (isBlank(aspectRatio)) {
			aspectRatioContainer = null;
			return;
		}
		if (Rational.RATIONAL_SEPARATOR.matcher(aspectRatio).find()) {
			setAspectRatioContainer(Rational.valueOf(aspectRatio));
			return;
		}
		try {
			setAspectRatioContainer(Rational.aspectRatioValueOf(Double.parseDouble(aspectRatio)));
		} catch (NumberFormatException e) {
			setAspectRatioContainer(Rational.valueOf(aspectRatio));
		}
	}

	/**
	 * Sets the aspect ratio reported by the file/container.
	 *
	 * @param aspectRatio the aspect ratio to set.
	 */
	public void setAspectRatioContainer(Rational aspectRatio) {
		if (Rational.isNotBlank(aspectRatio)) {
			aspectRatioContainer = aspectRatio;
		} else {
			aspectRatioContainer = null;
		}
	}

	/**
	 * Get the aspect ratio of the video track. This is the actual aspect ratio
	 * of the pixels, which is not always the aspect ratio that the renderer
	 * should display or that we should output; that is
	 * {@link #getAspectRatioContainer()}.
	 *
	 * @return the aspect ratio of the video track
	 */
	public Rational getAspectRatioVideoTrack() {
		return aspectRatioVideoTrack;
	}

	/**
	 * Sets the aspect ratio reported by the video track by parsing the
	 * specified {@link String}.
	 *
	 * @param aspectRatio the aspect ratio to set.
	 * @throws NumberFormatException If {@code aspectRatio} cannot be parsed.
	 */
	public void setAspectRatioVideoTrack(String aspectRatio) {
		if (isBlank(aspectRatio)) {
			aspectRatioVideoTrack = null;
			return;
		}
		if (Rational.RATIONAL_SEPARATOR.matcher(aspectRatio).find()) {
			setAspectRatioVideoTrack(Rational.valueOf(aspectRatio));
			return;
		}
		try {
			setAspectRatioVideoTrack(Rational.aspectRatioValueOf(Double.parseDouble(aspectRatio)));
		} catch (NumberFormatException e) {
			setAspectRatioVideoTrack(Rational.valueOf(aspectRatio));
		}
	}

	/**
	 * Sets the aspect ratio reported by the video track.
	 *
	 * @param aspectRatio the aspect ratio to set
	 */
	public void setAspectRatioVideoTrack(Rational aspectRatio) {
		if (Rational.isNotBlank(aspectRatio)) {
			aspectRatioVideoTrack = aspectRatio;
		} else {
			aspectRatioVideoTrack = null;
		}
	}

	/**
	 * @return The {@link DLNAThumbnail}.
	 */
	public DLNAThumbnail getThumb() {
		return thumb;
	}

	/**
	 * @param thumb the thumb to set.
	 * @since 1.50.0
	 * @deprecated Use {@link #setThumb(DLNAThumbnail)} instead.
	 */
	@Deprecated
	public void setThumb(byte[] thumb) {
		try {
			this.thumb = DLNABinaryThumbnail.toThumbnail(
				thumb,
				640,
				480,
				ScaleType.MAX,
				ImageFormat.SOURCE,
				false
			);
			if (this.thumb != null) {
				thumbready = true;
			}
		} catch (IOException e) {
			LOGGER.error("An error occurred while trying to store thumbnail: {}", e.getMessage());
			LOGGER.trace("", e);
		}
	}

	/**
	 * Sets the {@link DLNAThumbnail} instance to use for this
	 * {@link DLNAMediaInfo} instance.
	 *
	 * @param thumbnail the {@link DLNAThumbnail} to set.
	 */
	public void setThumb(DLNAThumbnail thumbnail) {
		this.thumb = thumbnail;
		if (thumbnail != null) {
			thumbready = true;
		}
	}

	/**
	 * @return The mimeType.
	 * @since 1.50.0
	 */
	public String getMimeType() {
		return mimeType;
	}

	/**
	 * @param mimeType the mimeType to set.
	 * @since 1.50.0
	 */
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public String getMatrixCoefficients() {
		return matrixCoefficients;
	}

	public void setMatrixCoefficients(String matrixCoefficients) {
		this.matrixCoefficients = matrixCoefficients;
	}

	public String getFileTitleFromMetadata() {
		return fileTitleFromMetadata;
	}

	public void setFileTitleFromMetadata(String value) {
		this.fileTitleFromMetadata = value;
	}

	public String getVideoTrackTitleFromMetadata() {
		return videoTrackTitleFromMetadata;
	}

	public void setVideoTrackTitleFromMetadata(String value) {
		this.videoTrackTitleFromMetadata = value;
	}

	/**
	 * @return The {@link ImageInfo} for this media or {@code null}.
	 */
	public ImageInfo getImageInfo() {
		return imageInfo;
	}

	/**
	 * Sets the {@link ImageInfo} for this media.
	 *
	 * @param imageInfo the {@link ImageInfo}.
	 */
	public void setImageInfo(ImageInfo imageInfo) {
		this.imageInfo = imageInfo;
		if (imageInfo != null && imageInfo.getWidth() > 0 && imageInfo.getHeight() > 0) {
			setWidth(imageInfo.getWidth());
			setHeight(imageInfo.getHeight());
		}
	}

	/**
	 * @return The reference frame count for the video stream or {@code -1}.
	 */
	public int getReferenceFrameCount() {
		return referenceFrameCount;
	}

	/**
	 * Sets the reference frame count for video stream.
	 *
	 * @param referenceFrameCount the reference frame count.
	 */
	public void setReferenceFrameCount(int referenceFrameCount) {
		this.referenceFrameCount = referenceFrameCount < -1 ? -1 : referenceFrameCount;
	}

	/**
	 * @return The video level for the video stream or {@code null} if not
	 *         parsed, known or relevant.
	 */
	@Nullable
	public VideoLevel getVideoLevel() {
		return videoLevel;
	}

	/**
	 * Sets the {@link VideoLevel} for the current video codec. Make sure that
	 * the correct {@link VideoLevel} type/implementation for the
	 * {@link VideoCodec} is used.
	 *
	 * @param videoLevel the {@link VideoLevel} to set or {@code null} if
	 *            unknown.
	 */
	public void setVideoLevel(@Nullable VideoLevel videoLevel) {
		this.videoLevel = videoLevel;
	}

	/**
	 * Gets the video profile for the video stream or {@code null} if not
	 * parsed/relevant.
	 *
	 * @return The video profile {@link String}.
	 */
	@Nullable
	public String getVideoProfile() {
		return videoProfile;
	}

	/**
	 * Sets video profile for the video stream.
	 *
	 * @param videoProfile the video profile.
	 */
	public void setVideoProfile(@Nullable String videoProfile) {
		this.videoProfile = videoProfile == null ? null : videoProfile.trim();
	}

	/**
	 * @return The {@link List} of audio tracks.
	 * @since 1.60.0
	 */
	// TODO (breaking change): rename to getAudioTracks
	public List<DLNAMediaAudio> getAudioTracksList() {
		return audioTracks;
	}

	/**
	 * @return The {@link ArrayList} of audio tracks.
	 *
	 * @deprecated use getAudioTracksList() instead.
	 */
	@Deprecated
	public ArrayList<DLNAMediaAudio> getAudioCodes() {
		if (audioTracks instanceof ArrayList) {
			return (ArrayList<DLNAMediaAudio>) audioTracks;
		}
		return new ArrayList<>();
	}

	/**
	 * @param audioTracks the {@link List} of audio tracks to set
	 * @since 1.60.0
	 */
	// TODO (breaking change): rename to setAudioTracks
	public void setAudioTracksList(List<DLNAMediaAudio> audioTracks) {
		this.audioTracks = audioTracks;
	}

	/**
	 * @param audioTracks the {@link List} of audio tracks to set.
	 *
	 * @deprecated use setAudioTracksList(ArrayList<DLNAMediaAudio> audioTracks) instead.
	 */
	@Deprecated
	public void setAudioCodes(List<DLNAMediaAudio> audioTracks) {
		setAudioTracksList(audioTracks);
	}

	/**
	 * @return The {@link List} of subtitles tracks.
	 * @since 1.60.0
	 */
	// TODO (breaking change): rename to getSubtitleTracks
	public List<DLNAMediaSubtitle> getSubtitleTracksList() {
		return subtitleTracks;
	}

	/**
	 * @return The {@link ArrayList} of subtitles tracks.
	 * @deprecated use getSubtitleTracksList() instead.
	 */
	@Deprecated
	public ArrayList<DLNAMediaSubtitle> getSubtitlesCodes() {
		if (subtitleTracks instanceof ArrayList) {
			return (ArrayList<DLNAMediaSubtitle>) subtitleTracks;
		}
		return new ArrayList<>();
	}

	/**
	 * @param subtitleTracks the {@link List} of subtitles tracks to set.
	 * @since 1.60.0
	 */
	// TODO (breaking change): rename to setSubtitleTracks
	public void setSubtitleTracksList(List<DLNAMediaSubtitle> subtitleTracks) {
		this.subtitleTracks = subtitleTracks;
	}

	/**
	 * @param subtitleTracks the {@link List} of subtitles tracks to set.
	 * @deprecated use setSubtitleTracksList(List<DLNAMediaSubtitle> subtitleTracks) instead.
	 */
	@Deprecated
	public void setSubtitlesCodes(List<DLNAMediaSubtitle> subtitleTracks) {
		setSubtitleTracksList(subtitleTracks);
	}

	/**
	 * @return The Exif orientation or {@link ExifOrientation#TOP_LEFT} if
	 *         unknown.
	 * @since 1.50.0
	 */
	public ExifOrientation getExifOrientation() {
		return imageInfo != null ? imageInfo.getExifOrientation() : ExifOrientation.TOP_LEFT;
	}

	/**
	 * @return The muxingMode.
	 * @since 1.50.0
	 */
	public String getMuxingMode() {
		return muxingMode;
	}

	/**
	 * @param muxingMode the muxingMode to set.
	 * @since 1.50.0
	 */
	public void setMuxingMode(String muxingMode) {
		this.muxingMode = muxingMode;
	}

	/**
	 * @return The muxingModeAudio.
	 * @since 1.50.0
	 */
	public String getMuxingModeAudio() {
		return muxingModeAudio;
	}

	/**
	 * @param muxingModeAudio the muxingModeAudio to set.
	 * @since 1.50.0
	 */
	public void setMuxingModeAudio(String muxingModeAudio) {
		this.muxingModeAudio = muxingModeAudio;
	}

	/**
	 * @return The container.
	 * @since 1.50.0
	 */
	public String getContainer() {
		return container;
	}

	/**
	 * @param container the container to set.
	 * @since 1.50.0
	 */
	public void setContainer(String container) {
		this.container = container;
	}

	/**
	 * @return The h264_annexB.
	 * @since 1.50.0
	 */
	public byte[] getH264AnnexB() {
		synchronized (h264_annexBLock) {
			if (h264_annexB == null) {
				return null;
			}
			byte[] result = new byte[h264_annexB.length];
			System.arraycopy(h264_annexB, 0, result, 0, h264_annexB.length);
			return result;
		}
	}

	/**
	 * @param h264AnnexB the h264_annexB to set.
	 * @since 1.50.0
	 */
	public void setH264AnnexB(byte[] h264AnnexB) {
		synchronized (h264_annexBLock) {
			if (h264AnnexB == null) {
				this.h264_annexB = null;
			} else {
				this.h264_annexB = new byte[h264AnnexB.length];
				System.arraycopy(h264AnnexB, 0, this.h264_annexB, 0, h264AnnexB.length);
			}
		}
	}

	/**
	 * @return The media parsed status.
	 * @since 1.50.0
	 */
	public boolean isMediaparsed() {
		return mediaparsed;
	}

	/**
	 * @param mediaparsed the media parsed status to set.
	 * @since 1.50.0
	 */
	public void setMediaparsed(boolean mediaparsed) {
		this.mediaparsed = mediaparsed;
	}

	public boolean isFFmpegparsed() {
		return ffmpegparsed;
	}

	/**
	 * @return the thumbready
	 * @since 1.50.0
	 */
	public boolean isThumbready() {
		return thumbready;
	}

	/**
	 * @param thumbready the thumbready to set.
	 * @since 1.50.0
	 */
	public void setThumbready(boolean thumbready) {
		this.thumbready = thumbready;
	}

	/**
	 * @return The DVD track number.
	 * @since 1.50.0
	 */
	public int getDvdtrack() {
		return dvdtrack;
	}

	/**
	 * @param dvdtrack the DVD track number to set.
	 * @since 1.50.0
	 */
	public void setDvdtrack(int dvdtrack) {
		this.dvdtrack = dvdtrack;
	}

	/**
	 * @return The secondaryFormatValid.
	 * @since 1.50.0
	 */
	public boolean isSecondaryFormatValid() {
		return secondaryFormatValid;
	}

	/**
	 * @param secondaryFormatValid the secondaryFormatValid to set.
	 * @since 1.50.0
	 */
	public void setSecondaryFormatValid(boolean secondaryFormatValid) {
		this.secondaryFormatValid = secondaryFormatValid;
	}

	/**
	 * @return Whether parsing is currently ongoing.
	 * @since 1.50.0
	 */
	public boolean isParsing() {
		synchronized (parsingLock) {
			return parsing;
		}
	}

	/**
	 * @param parsing the parsing to set.
	 * @since 1.50.0
	 */
	public void setParsing(boolean parsing) {
		synchronized (parsingLock) {
			this.parsing = parsing;
		}
	}

	/**
	 * @return The encrypted.
	 * @since 1.50.0
	 */
	public boolean isEncrypted() {
		return encrypted;
	}

	/**
	 * @param encrypted the encrypted to set.
	 * @since 1.50.0
	 */
	public void setEncrypted(boolean encrypted) {
		this.encrypted = encrypted;
	}

	public boolean isMod4() {
		if (
			height % 4 != 0 ||
			width % 4 != 0
		) {
			return false;
		}

		return true;
	}

	/**
	 * Note: This is based on a flag in Matroska files, and as such it is
	 * unreliable; it will be unlikely to find a false-positive but there
	 * will be false-negatives, similar to language flags.
	 *
	 * @return Whether the video track is 3D.
	 */
	public boolean is3d() {
		return isNotBlank(stereoscopy);
	}

	/**
	 * The significance of this is that the aspect ratio should not be kept
	 * in this case when transcoding.
	 * Example: 3840x1080 should be resized to 1920x1080, not 1920x540.
	 *
	 * @return Whether the video track is full SBS or OU 3D.
	 */
	public boolean is3dFullSbsOrOu() {
		if (!is3d()) {
			return false;
		}

		switch (stereoscopy.toLowerCase()) {
			case "overunderrt":
			case "oulf":
			case "ourf":
			case "sbslf":
			case "sbsrf":
			case "top-bottom (left eye first)":
			case "top-bottom (right eye first)":
			case "side by side (left eye first)":
			case "side by side (right eye first)":
				return true;
		}

		return false;
	}

	/**
	 * Note: This is based on a flag in Matroska files, and as such it is
	 * unreliable; it will be unlikely to find a false-positive but there
	 * will be false-negatives, similar to language flags.
	 *
	 * @return The type of stereoscopy (3D) of the video track.
	 */
	public String getStereoscopy() {
		return stereoscopy;
	}

	/**
	 * Sets the type of stereoscopy (3D) of the video track.
	 *
	 * Note: This is based on a flag in Matroska files, and as such it is
	 * unreliable; it will be unlikely to find a false-positive but there
	 * will be false-negatives, similar to language flags.
	 *
	 * @param stereoscopy the type of stereoscopy (3D) of the video track.
	 */
	public void setStereoscopy(String stereoscopy) {
		this.stereoscopy = stereoscopy;
	}

	/**
	 * Used by FFmpeg for 3D video format naming.
	 */
	public enum Mode3D {
		ML,
		MR,
		SBSL,
		SBSR,
		SBS2L,
		SBS2R,
		ABL,
		ABR,
		AB2L,
		AB2R,
		ARCG,
		ARCH,
		ARCC,
		ARCD,
		AGMG,
		AGMH,
		AGMC,
		AGMD,
		AYBG,
		AYBH,
		AYBC,
		AYBD
	}

	public Mode3D get3DLayout() {
		if (!is3d()) {
			return null;
		}

		isAnaglyph = true;
		switch (stereoscopy.toLowerCase()) {
			case "overunderrt":
			case "oulf":
			case "top-bottom (left eye first)":
				isAnaglyph = false;
				return Mode3D.ABL;
			case "ourf":
			case "top-bottom (right eye first)":
				isAnaglyph = false;
				return Mode3D.ABR;
			case "sbslf":
			case "side by side (left eye first)":
				isAnaglyph = false;
				return Mode3D.SBSL;
			case "sbsrf":
			case "side by side (right eye first)":
				isAnaglyph = false;
				return Mode3D.SBSR;
			case "half top-bottom (left eye first)":
				isAnaglyph = false;
				return Mode3D.AB2L;
			case "half side by side (left eye first)":
				isAnaglyph = false;
				return Mode3D.SBS2L;
			case "arcg":
				return Mode3D.ARCG;
			case "arch":
				return Mode3D.ARCH;
			case "arcc":
				return Mode3D.ARCC;
			case "arcd":
				return Mode3D.ARCD;
			case "agmg":
				return Mode3D.AGMG;
			case "agmh":
				return Mode3D.AGMH;
			case "agmc":
				return Mode3D.AGMC;
			case "agmd":
				return Mode3D.AGMD;
			case "aybg":
				return Mode3D.AYBG;
			case "aybh":
				return Mode3D.AYBH;
			case "aybc":
				return Mode3D.AYBC;
			case "aybd":
				return Mode3D.AYBD;
		}

		return null;
	}

	private boolean isAnaglyph;

	public boolean stereoscopyIsAnaglyph() {
		get3DLayout();
		return isAnaglyph;
	}

	public boolean isDVDResolution() {
		return (width == 720 && height == 576) || (width == 720 && height == 480);
	}

	/**
	 * Determines if this {@link DLNAMediaInfo} instance has a container that is
	 * used both for audio and video media.
	 *
	 * @return {@code true} if the currently set {@code container} can be either
	 *         audio or video, {@code false} otherwise.
	 */
	public boolean isAudioOrVideoContainer() {
		if (isBlank(container)) {
			return false;
		}
		for (Entry<String, AudioVariantInfo> entry : audioOrVideoContainers.entrySet()) {
			if (
				container.equals(entry.getKey()) ||
				container.equals(entry.getValue().getFormatConfiguration())
			) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the {@link Format} to use if this {@link DLNAMediaInfo} instance
	 * represent an audio media wrapped in a container that can represent both
	 * audio and video media. This returns {@code null} unless
	 * {@link #isAudioOrVideoContainer} is {@code true}.
	 *
	 * @see #isAudioOrVideoContainer()
	 *
	 * @return The "audio variant" {@link Format} for this container, or
	 *         {@code null} if it doesn't apply.
	 */
	public Format getAudioVariantFormat() {
		if (isBlank(container)) {
			return null;
		}
		for (Entry<String, AudioVariantInfo> entry : audioOrVideoContainers.entrySet()) {
			if (
				container.equals(entry.getKey()) ||
				container.equals(entry.getValue().getFormatConfiguration())
			) {
				return entry.getValue().getFormat();
			}
		}
		return null;
	}

	/**
	 * Returns the {@link FormatConfiguration} {@link String} constant to use if
	 * this {@link DLNAMediaInfo} instance represent an audio media wrapped in a
	 * container that can represent both audio and video media. This returns
	 * {@code null} unless {@link #isAudioOrVideoContainer} is {@code true}.
	 *
	 * @see #isAudioOrVideoContainer()
	 *
	 * @return The "audio variant" {@link FormatConfiguration} {@link String}
	 *         constant for this container, or {@code null} if it doesn't apply.
	 */
	public String getAudioVariantFormatConfigurationString() {
		if (isBlank(container)) {
			return null;
		}
		for (Entry<String, AudioVariantInfo> entry : audioOrVideoContainers.entrySet()) {
			if (
				container.equals(entry.getKey()) ||
				container.equals(entry.getValue().getFormatConfiguration())
			) {
				return entry.getValue().getFormatConfiguration();
			}
		}
		return null;
	}

	/**
	 * Returns the {@link AudioVariantInfo} to use if this {@link DLNAMediaInfo}
	 * instance represent an audio media wrapped in a container that can
	 * represent both audio and video media. This returns {@code null} unless
	 * {@link #isAudioOrVideoContainer} is {@code true}.
	 *
	 * @see #isAudioOrVideoContainer()
	 *
	 * @return The {@link AudioVariantInfo} for this container, or {@code null}
	 *         if it doesn't apply.
	 */
	public AudioVariantInfo getAudioVariant() {
		if (isBlank(container)) {
			return null;
		}
		for (Entry<String, AudioVariantInfo> entry : audioOrVideoContainers.entrySet()) {
			if (
				container.equals(entry.getKey()) ||
				container.equals(entry.getValue().getFormatConfiguration())
			) {
				return entry.getValue();
			}
		}
		return null;
	}

	/**
	 * An immutable struct/record for hold information for a particular audio
	 * variant for containers that can constitute multiple "media types".
	 */
	public static class AudioVariantInfo {

		protected final Format format;
		protected final String formatConfiguration;

		/**
		 * Creates a new instance.
		 *
		 * @param format the {@link Format} for this {@link AudioVariantInfo}.
		 * @param formatConfiguration the {@link FormatConfiguration}
		 *            {@link String} constant for this {@link AudioVariantInfo}.
		 */
		public AudioVariantInfo(Format format, String formatConfiguration) {
			this.format = format;
			this.formatConfiguration = formatConfiguration;
		}

		/**
		 * @return the {@link Format}.
		 */
		public Format getFormat() {
			return format;
		}

		/**
		 * @return the {@link FormatConfiguration} {@link String} constant.
		 */
		public String getFormatConfiguration() {
			return formatConfiguration;
		}
	}

	/**
	 * This {@code enum} represents the different video "scan types".
	 */
	public enum ScanType {

		/** Interlaced scan, any sub-type */
		INTERLACED,

		/** Mixed scan */
		MIXED,

		/** Progressive scan */
		PROGRESSIVE;

		@Override
		public String toString() {
			switch (this) {
				case INTERLACED:
					return "Interlaced";
				case MIXED:
					return "Mixed";
				case PROGRESSIVE:
					return "Progressive";
				default:
					return name();
			}
		}

		public static ScanType typeOf(String scanType) {
			if (isBlank(scanType)) {
				return null;
			}
			scanType = scanType.trim().toLowerCase(Locale.ROOT);
			switch (scanType) {
				case "interlaced" :
					return INTERLACED;
				case "mixed" :
					return MIXED;
				case "progressive" :
					return PROGRESSIVE;
				default:
					LOGGER.debug("Warning: Unrecognized ScanType \"{}\"", scanType);
					return null;
			}
		}
	}

	/**
	 * This {@code enum} represents the video scan order.
	 */
	public enum ScanOrder {

		/** Bottom Field First */
		BFF,

		/** Bottom Field Only */
		BFO,

		/** Pulldown */
		PULLDOWN,

		/** 2:2:2:2:2:2:2:2:2:2:2:3 Pulldown */
		PULLDOWN_2_2_2_2_2_2_2_2_2_2_2_3,

		/** 2:3 Pulldown */
		PULLDOWN_2_3,

		/** Top Field First */
		TFF,

		/** Top Field Only */
		TFO;

		@Override
		public String toString() {
			switch (this) {
				case BFF:
					return "Bottom Field First";
				case BFO:
					return "Bottom Field Only";
				case PULLDOWN:
					return "Pulldown";
				case PULLDOWN_2_2_2_2_2_2_2_2_2_2_2_3:
					return "2:2:2:2:2:2:2:2:2:2:2:3 Pulldown";
				case PULLDOWN_2_3:
					return "2:3 Pulldown";
				case TFF:
					return "Top Field First";
				case TFO:
					return "Top Field Only";
				default:
					return name();
			}
		}

		public static ScanOrder typeOf(String scanOrder) {
			if (isBlank(scanOrder)) {
				return null;
			}
			scanOrder = scanOrder.trim().toLowerCase(Locale.ROOT);
			switch (scanOrder) {
				case "bff" :
				case "bottom field first":
					return BFF;
				case "bfo":
				case "bottom field only":
					return BFO;
				case "pulldown":
					return PULLDOWN;
				case "2:2:2:2:2:2:2:2:2:2:2:3 pulldown":
					return PULLDOWN_2_2_2_2_2_2_2_2_2_2_2_3;
				case "2:3 pulldown":
					return PULLDOWN_2_3;
				case "tff":
				case "top field first":
					return TFF;
				case "tfo":
				case "top field only":
					return TFO;
				default:
					LOGGER.debug("Warning: Unrecognized ScanOrder \"{}\"", scanOrder);
					if (scanOrder.contains("pulldown")) {
						return PULLDOWN;
					}
					return null;
			}
		}
	}

	/**
	 * This {@code enum} represents constant or variable rate modes, for example
	 * for bitrate of framerate.
	 */
	public enum RateMode {
		CONSTANT,
		VARIABLE;

		@Nullable
		public static RateMode typeOf(@Nullable String value) {
			if (isBlank(value)) {
				return null;
			}

			switch (value.trim().toLowerCase(Locale.ROOT)) {
				case "c":
				case "cbr":
				case "cfr":
				case "const":
				case "constant":
					return CONSTANT;
				case "v":
				case "var":
				case "vbr":
				case "vfr":
				case "variable":
					return VARIABLE;
				default:
					return null;
			}
		}
	}

}
