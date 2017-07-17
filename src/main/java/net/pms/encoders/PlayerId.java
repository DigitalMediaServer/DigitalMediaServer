/*
 * Digital Media Server, for streaming digital media to DLNA compatible devices
 * based on www.ps3mediaserver.org and www.universalmediaserver.com.
 * Copyright (C) 2016 Digital Media Server developers.
 *
 * This program is a free software; you can redistribute it and/or
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


/**
 * Defines identifiers for {@link Player} subclasses
 * <p/>
 * This class is final and cannot be sub-classed.
 * </p>
 */
public class PlayerId {

	/*
	 * "AvSFFmpeg" "AviSynth/FFmpeg" 			-> "AviSynthFFmpeg"
	 * "AvSMEncoder" "AviSynth/MEncoder" 		-> "AviSynthMEncoder"
	 * "FFmpegAudio" "FFmpeg Audio"
	 * "FFmpegDVRMSRemux" "FFmpeg DVR-MS Remux"
	 * "FFmpegVideo" "FFmpeg"
	 * "FFmpegWebVideo" "FFmpeg Web Video"
	 * "MEncoder" "MEncoder"					-> "MEncoderVideo"
	 * "MEncoderWebVideo" "MEncoder Web"
	 * "RawThumbs" "dcraw Thumbnailer" 			-> "RAWThumbnailer"
	 * "TSMuxerAudio" "Audio High Fidelity" 	-> "tsMuxeRAudio"
	 * "TSMuxer" "tsMuxeR" 						-> tsMuxeRVideo
	 * "VLCAudio" "VLC Web Audio (Legacy)" 		-> "VLCAudioStreaming"
	 * "VLCVideo" "VLC Web Video (Legacy)" 		-> "VLCVideoStreaming"
	 * "VLCTranscoder" "VLC"					-> "VLCVideo"
	 * "VLCWebVideo" "VLC Web Video"
	 */

	public static final int UNKNOWN_INT = 0;
	public static final int AVI_SYNTH_FFMPEG_INT = 10;
	public static final int AVI_SYNTH_MENCODER_INT = 20;
	public static final int FFMPEG_AUDIO_INT = 30;
	public static final int FFMPEG_DVRMS_REMUX_INT = 40;
	public static final int FFMPEG_VIDEO_INT = 50;
	public static final int FFMPEG_WEB_VIDEO_INT = 60;
	public static final int MENCODER_VIDEO_INT = 70;
	public static final int MENCODER_WEB_VIDEO_INT = 80;
	public static final int RAW_THUMBNAILER_INT = 90;
	public static final int TSMUXER_AUDIO_INT = 100;
	public static final int TSMUXER_VIDEO_INT = 110;
	public static final int VLC_AUDIO_STREAMING_INT = 120;
	public static final int VLC_VIDEO_STREAMING_INT = 130;
	public static final int VLC_VIDEO_INT = 140;
	public static final int VLC_WEB_VIDEO_INT = 150;

	public static final Integer UNKNOWN_INTEGER = UNKNOWN_INT;
	public static final Integer AVI_SYNTH_FFMPEG_INTEGER = AVI_SYNTH_FFMPEG_INT;
	public static final Integer AVI_SYNTH_MENCODER_INTEGER = AVI_SYNTH_MENCODER_INT;
	public static final Integer FFMPEG_AUDIO_INTEGER = FFMPEG_AUDIO_INT;
	public static final Integer FFMPEG_DVRMS_REMUX_INTEGER = FFMPEG_DVRMS_REMUX_INT;
	public static final Integer FFMPEG_VIDEO_INTEGER = FFMPEG_VIDEO_INT;
	public static final Integer FFMPEG_WEB_VIDEO_INTEGER = FFMPEG_WEB_VIDEO_INT;
	public static final Integer MENCODER_VIDEO_INTEGER = MENCODER_VIDEO_INT;
	public static final Integer MENCODER_WEB_VIDEO_INTEGER = MENCODER_WEB_VIDEO_INT;
	public static final Integer RAW_THUMBNAILER_INTEGER = RAW_THUMBNAILER_INT;
	public static final Integer TSMUXER_AUDIO_INTEGER = TSMUXER_AUDIO_INT;
	public static final Integer TSMUXER_VIDEO_INTEGER = TSMUXER_VIDEO_INT;
	public static final Integer VLC_AUDIO_STREAMING_INTEGER = VLC_AUDIO_STREAMING_INT;
	public static final Integer VLC_VIDEO_STREAMING_INTEGER = VLC_VIDEO_STREAMING_INT;
	public static final Integer VLC_VIDEO_INTEGER = VLC_VIDEO_INT;
	public static final Integer VLC_WEB_VIDEO_INTEGER = VLC_WEB_VIDEO_INT;

	public static final PlayerId UNKNOWN = new PlayerId(UNKNOWN_INT, "Unknown", "Unknown");
	public static final PlayerId AVI_SYNTH_FFMPEG = new PlayerId(AVI_SYNTH_FFMPEG_INT, "AviSynthFFmpeg", "AviSynth/FFmpeg");
	public static final PlayerId AVI_SYNTH_MENCODER = new PlayerId(AVI_SYNTH_MENCODER_INT, "AviSynthMEncoder", "AviSynth/MEncoder");
	public static final PlayerId FFMPEG_AUDIO = new PlayerId(FFMPEG_AUDIO_INT, "FFmpegAudio", "FFmpeg Audio");
	public static final PlayerId FFMPEG_DVRMS_REMUX = new PlayerId(FFMPEG_DVRMS_REMUX_INT, "FFmpegDVRMSRemux", "FFmpeg DVR-MS Remux");
	public static final PlayerId FFMPEG_VIDEO = new PlayerId(FFMPEG_VIDEO_INT, "FFmpegVideo", "FFmpeg");
	public static final PlayerId FFMPEG_WEB_VIDEO = new PlayerId(FFMPEG_WEB_VIDEO_INT, "FFmpegWebVideo", "FFmpeg Web Video");
	public static final PlayerId MENCODER_VIDEO = new PlayerId(MENCODER_VIDEO_INT, "MEncoderVideo", "MEncoder Video");
	public static final PlayerId MENCODER_WEB_VIDEO = new PlayerId(MENCODER_WEB_VIDEO_INT, "MEncoderWebVideo", "MEncoder Web");
	public static final PlayerId RAW_THUMBNAILER = new PlayerId(RAW_THUMBNAILER_INT, "RAWThumbnailer", "DCRaw Thumbnailer");
	public static final PlayerId TSMUXER_AUDIO = new PlayerId(TSMUXER_AUDIO_INT, "tsMuxeRAudio", "tsMuxeR Audio");
	public static final PlayerId TSMUXER_VIDEO = new PlayerId(TSMUXER_VIDEO_INT, "tsMuxeRVideo", "tsMuxeR Video");
	public static final PlayerId VLC_AUDIO_STREAMING = new PlayerId(VLC_AUDIO_STREAMING_INT, "VLCAudioStreaming", "VLC Web Audio (Legacy)");
	public static final PlayerId VLC_VIDEO_STREAMING = new PlayerId(VLC_VIDEO_STREAMING_INT, "VLCVideoStreaming", "VLC Web Video (Legacy)");
	public static final PlayerId VLC_VIDEO = new PlayerId(VLC_VIDEO_INT, "VLCVideo", "VLC Video");
	public static final PlayerId VLC_WEB_VIDEO = new PlayerId(VLC_WEB_VIDEO_INT, "VLCWebVideo", "VLC Web Video");

	public final int playerIdInt;
	public final String playerIdStr;
	public final String playerName;

	/**
	 * Instantiate a {@link PlayerId} object.
	 */
	private PlayerId(int playerIdInt, String playerIdStr, String playerName) {
		this.playerIdInt = playerIdInt;
		this.playerIdStr = playerIdStr;
		this.playerName = playerName;
	}

	/**
	 * @return The string representation of this {@link PlayerId}.
	 */
	public String toString() {
		return playerIdStr;
	}

	/**
	 * @return The {@link Player} name
	 */
	public String name() {
		return playerName;
	}

	/**
	 * @return The integer representation of this {@link PlayerId}.
	 */
	public int toInt() {
		return playerIdInt;
	}

	/**
	 * Convert a {@link PlayerId} to an {@link Integer} object.
	 *
	 * @return This {@link PlayerId}'s Integer mapping.
	 */
	public Integer toInteger() {
		switch (playerIdInt) {
			case AVI_SYNTH_FFMPEG_INT:
				return AVI_SYNTH_FFMPEG_INTEGER;
			case AVI_SYNTH_MENCODER_INT:
				return AVI_SYNTH_MENCODER_INTEGER;
			case FFMPEG_AUDIO_INT:
				return FFMPEG_AUDIO_INTEGER;
			case FFMPEG_DVRMS_REMUX_INT:
				return FFMPEG_DVRMS_REMUX_INTEGER;
			case FFMPEG_VIDEO_INT:
				return FFMPEG_VIDEO_INTEGER;
			case FFMPEG_WEB_VIDEO_INT:
				return FFMPEG_WEB_VIDEO_INTEGER;
			case MENCODER_VIDEO_INT:
				return MENCODER_VIDEO_INTEGER;
			case MENCODER_WEB_VIDEO_INT:
				return MENCODER_WEB_VIDEO_INTEGER;
			case RAW_THUMBNAILER_INT:
				return RAW_THUMBNAILER_INTEGER;
			case TSMUXER_AUDIO_INT:
				return TSMUXER_AUDIO_INTEGER;
			case TSMUXER_VIDEO_INT:
				return TSMUXER_VIDEO_INTEGER;
			case VLC_AUDIO_STREAMING_INT:
				return VLC_AUDIO_STREAMING_INTEGER;
			case VLC_VIDEO_STREAMING_INT:
				return VLC_VIDEO_STREAMING_INTEGER;
			case VLC_VIDEO_INT:
				return VLC_VIDEO_INTEGER;
			case VLC_WEB_VIDEO_INT:
				return VLC_WEB_VIDEO_INTEGER;
			default:
				return UNKNOWN_INTEGER;
		}
	}

	/**
	 * Convert an integer passed as argument to a {@link PlayerId}. If the conversion
	 * fails, then this method returns {@link #UNKNOWN}.
	 */
	public static PlayerId toPlayerID(int val) {
		switch (val) {
			case AVI_SYNTH_FFMPEG_INT:
				return AVI_SYNTH_FFMPEG;
			case AVI_SYNTH_MENCODER_INT:
				return AVI_SYNTH_MENCODER;
			case FFMPEG_AUDIO_INT:
				return FFMPEG_AUDIO;
			case FFMPEG_DVRMS_REMUX_INT:
				return FFMPEG_DVRMS_REMUX;
			case FFMPEG_VIDEO_INT:
				return FFMPEG_VIDEO;
			case FFMPEG_WEB_VIDEO_INT:
				return FFMPEG_WEB_VIDEO;
			case MENCODER_VIDEO_INT:
				return MENCODER_VIDEO;
			case MENCODER_WEB_VIDEO_INT:
				return MENCODER_WEB_VIDEO;
			case RAW_THUMBNAILER_INT:
				return RAW_THUMBNAILER;
			case TSMUXER_AUDIO_INT:
				return TSMUXER_AUDIO;
			case TSMUXER_VIDEO_INT:
				return TSMUXER_VIDEO;
			case VLC_AUDIO_STREAMING_INT:
				return VLC_AUDIO_STREAMING;
			case VLC_VIDEO_STREAMING_INT:
				return VLC_VIDEO_STREAMING;
			case VLC_VIDEO_INT:
				return VLC_VIDEO;
			case VLC_WEB_VIDEO_INT:
				return VLC_WEB_VIDEO;
			default:
				return UNKNOWN;
		}
	}

	/**
	 * Convert the string passed as argument to a {@link PlayerId}. If the conversion
	 * fails, then this method returns {@link #UNKNOWN}.
	 */
	public static PlayerId toPlayerID(String sArg) {
		if ("AviSynthFFmpeg".equalsIgnoreCase(sArg)) {
			return PlayerId.AVI_SYNTH_FFMPEG;
		}
		if ("AviSynthMEncoder".equalsIgnoreCase(sArg)) {
			return PlayerId.AVI_SYNTH_MENCODER;
		}
		if ("FFmpegAudio".equalsIgnoreCase(sArg)) {
			return PlayerId.FFMPEG_AUDIO;
		}
		if ("FFmpegDVRMSRemux".equalsIgnoreCase(sArg)) {
			return PlayerId.FFMPEG_DVRMS_REMUX;
		}
		if ("FFmpegVideo".equalsIgnoreCase(sArg)) {
			return PlayerId.FFMPEG_VIDEO;
		}
		if ("FFmpegWebVideo".equalsIgnoreCase(sArg)) {
			return PlayerId.FFMPEG_WEB_VIDEO;
		}
		if ("MEncoderVideo".equalsIgnoreCase(sArg)) {
			return PlayerId.MENCODER_VIDEO;
		}
		if ("MEncoderWebVideo".equalsIgnoreCase(sArg)) {
			return PlayerId.MENCODER_WEB_VIDEO;
		}
		if ("RAWThumbnailer".equalsIgnoreCase(sArg)) {
			return PlayerId.RAW_THUMBNAILER;
		}
		if ("tsMuxeRAudio".equalsIgnoreCase(sArg)) {
			return PlayerId.TSMUXER_AUDIO;
		}
		if ("tsMuxeRVideo".equalsIgnoreCase(sArg)) {
			return PlayerId.TSMUXER_VIDEO;
		}
		if ("VLCAudioStreaming".equalsIgnoreCase(sArg)) {
			return PlayerId.VLC_AUDIO_STREAMING;
		}
		if ("VLCVideoStreaming".equalsIgnoreCase(sArg)) {
			return PlayerId.VLC_VIDEO_STREAMING;
		}
		if ("VLCVideo".equalsIgnoreCase(sArg)) {
			return PlayerId.VLC_VIDEO;
		}
		if ("VLCWebVideo".equalsIgnoreCase(sArg)) {
			return PlayerId.VLC_WEB_VIDEO;
		}

		return PlayerId.UNKNOWN;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + playerIdInt;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof PlayerId)) {
			return false;
		}
		PlayerId other = (PlayerId) obj;
		if (playerIdInt != other.playerIdInt) {
			return false;
		}
		return true;
	}
}

