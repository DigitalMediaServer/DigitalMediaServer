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

import java.util.Locale;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.pms.configuration.FormatConfiguration;
import net.pms.dlna.DLNAMediaInfo.RateMode;
import net.pms.formats.v2.AudioProperties;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * This class keeps track of the audio properties of media.
 *
 * TODO: Change all instance variables to private. For backwards compatibility
 * with external plugin code the variables have all been marked as deprecated
 * instead of changed to private, but this will surely change in the future.
 * When everything has been changed to private, the deprecated note can be
 * removed.
 */
public class DLNAMediaAudio extends DLNAMediaLang implements Cloneable {

	public static final int BITRATE_DEFAULT = 8000;
	public static final int NUMBEROFCHANNELS_DEFAULT = 2;
	public static final int BITSPERSAMPLE_DEFAULT = 16;
	public static final int AUDIODELAY_DEFAULT = 0;
	public static final int SAMPLEFREQUENCY_DEFAULT = 48000;
	public static final RateMode BITRATEMODE_DEFAULT = RateMode.CONSTANT;

	private AudioProperties audioProperties = new AudioProperties();

	private int bitsPerSample = -1;

	private int bitRate = -1;

	private RateMode bitRateMode;

	private int sampleFrequency = -1;

	private int numberOfChannels = -1;

	/**
	 * @deprecated Use standard getter and setter to access this variable.
	 */
	@Deprecated
	public String codecA;

	/**
	 * @deprecated Use standard getter and setter to access this variable.
	 */
	@Deprecated
	public String album;

	/**
	 * @deprecated Use standard getter and setter to access this variable.
	 */
	@Deprecated
	public String artist;

	/**
	 * @deprecated Use standard getter and setter to access this variable.
	 */
	@Deprecated
	public String songname;

	/**
	 * @deprecated Use standard getter and setter to access this variable.
	 */
	@Deprecated
	public String genre;

	/**
	 * @deprecated Use standard getter and setter to access this variable.
	 */
	@Deprecated
	public int year;

	/**
	 * @deprecated Use standard getter and setter to access this variable.
	 */
	@Deprecated
	public int track;

	private int delay = Integer.MIN_VALUE;

	/**
	 * @deprecated Use standard getter and setter to access this variable.
	 */
	@Deprecated
	public String audioTrackTitleFromMetadata;

	/**
	 * @deprecated Use standard getter and setter to access this variable.
	 */
	@Deprecated
	public String muxingModeAudio;

	/**
	 * @return True if the audio codec is one of the AAC variants.
	 */
	public boolean isAAC() {
		return isAACLC() || isHEAAC();
	}

	/**
	 * @return True if the audio codec is AAC-LC.
	 */
	public boolean isAACLC() {
		return FormatConfiguration.AAC_LC.equalsIgnoreCase(getCodecA()) ||
			getCodecA() != null && getCodecA().contains("aac (lc)");
	}

	/**
	 * @return True if the audio codec is AC-3.
	 */
	public boolean isAC3() {
		return
			FormatConfiguration.AC3.equalsIgnoreCase(getCodecA()) ||
			getCodecA() != null && ((
					getCodecA().contains("ac3") &&
					!getCodecA().contains("mac3") &&
					!getCodecA().contains("eac3")
				) ||
				getCodecA().contains("a52")
			);
	}

	/**
	 * @return True if the audio codec is ACELP.
	 */
	public boolean isACELP() {
		return FormatConfiguration.ACELP.equalsIgnoreCase(getCodecA());
	}

	/**
	 * @return True if the audio codec is ADPCM.
	 */
	public boolean isADPCM() {
		return FormatConfiguration.ADPCM.equalsIgnoreCase(getCodecA());
	}

	/**
	 * @return True if the audio codec is ALAC.
	 */
	public boolean isALAC() {
		return FormatConfiguration.ALAC.equalsIgnoreCase(getCodecA());
	}

	/**
	 * @return True if the audio codec is ALS.
	 */
	public boolean isALS() {
		return FormatConfiguration.ALS.equalsIgnoreCase(getCodecA());
	}

	/**
	 * @return {@code true} if the audio codec is Atmos.
	 *
	 * @deprecated Not in use until multi-level parsing is implemented
	 */
	@Deprecated
	public boolean isAtmos() {
		return FormatConfiguration.ATMOS.equalsIgnoreCase(getCodecA());
	}

	/**
	 * @return True if the audio codec is ATRAC.
	 */
	public boolean isATRAC() {
		return FormatConfiguration.ATRAC.equalsIgnoreCase(getCodecA());
	}

	/**
	 * @return True if the audio codec is Cook.
	 */
	public boolean isCook() {
		return FormatConfiguration.COOK.equalsIgnoreCase(getCodecA());
	}

	/**
	 * @return True if the audio codec is DFF.
	 */
	public boolean isDFF() {
		return FormatConfiguration.DFF.equalsIgnoreCase(getCodecA());
	}

	/**
	 * @return True if the audio codec is Dolby E.
	 */
	public boolean isDOLBYE() {
		return FormatConfiguration.DOLBYE.equalsIgnoreCase(getCodecA());
	}

	/**
	 * @return True if the audio codec is DSF.
	 */
	public boolean isDSF() {
		return FormatConfiguration.DSF.equalsIgnoreCase(getCodecA());
	}

	/**
	 * @return True if the audio codec is DTS.
	 */
	public boolean isDTS() {
		return
			FormatConfiguration.DTS.equalsIgnoreCase(getCodecA()) ||
			getCodecA() != null && (
				getCodecA().contains("dts") ||
				getCodecA().contains("dca")
			);
	}

	/**
	 * @return True if the audio codec is DTS-HD.
	 */
	public boolean isDTSHD() {
		return FormatConfiguration.DTSHD.equalsIgnoreCase(getCodecA());
	}

	/**
	 * @return True if the audio codec is EAC-3.
	 */
	public boolean isEAC3() {
		return FormatConfiguration.EAC3.equalsIgnoreCase(getCodecA()) ||
			getCodecA() != null && getCodecA().contains("eac3");
	}

	/**
	 * @return whether the audio codec is ER BSAC.
	 */
	public boolean isERBSAC() {
		return FormatConfiguration.ER_BSAC.equalsIgnoreCase(getCodecA());
	}

	/**
	 * @return True if the audio codec is FLAC.
	 */
	public boolean isFLAC() {
		return FormatConfiguration.FLAC.equalsIgnoreCase(getCodecA());
	}

	/**
	 * @return True if the audio codec is G729.
	 */
	public boolean isG729() {
		return FormatConfiguration.G729.equalsIgnoreCase(getCodecA());
	}

	/**
	 * @return True if the audio codec is HE-AAC.
	 */
	public boolean isHEAAC() {
		return FormatConfiguration.HE_AAC.equalsIgnoreCase(getCodecA()) ||
			getCodecA() != null && getCodecA().contains("aac (he-aac)");
	}

	/**
	 * @return True if the audio codec is MLP.
	 */
	public boolean isMLP() {
		return FormatConfiguration.MLP.equalsIgnoreCase(getCodecA());
	}

	/**
	 * @return True if the audio codec is MonkeysAudio.
	 */
	public boolean isMonkeysAudio() {
		return FormatConfiguration.MONKEYS_AUDIO.equalsIgnoreCase(getCodecA());
	}

	/**
	 * @return True if the audio codec is MP3.
	 */
	public boolean isMP3() {
		return FormatConfiguration.MP3.equalsIgnoreCase(getCodecA()) || getCodecA() != null && getCodecA().contains("mp3");
	}

	/**
	 * @return True if the audio codec is MPEG-1/MPEG-2.
	 */
	public boolean isMpegAudio() {
		return FormatConfiguration.MP2.equalsIgnoreCase(getCodecA()) || FormatConfiguration.MPA.equalsIgnoreCase(getCodecA());
	}

	/**
	 * @return True if the audio codec is MPC.
	 */
	public boolean isMPC() {
		return FormatConfiguration.MPC.equalsIgnoreCase(getCodecA());
	}

	/**
	 * @return True if the audio codec is OPUS.
	 */
	public boolean isOpus() {
		return FormatConfiguration.OPUS.equalsIgnoreCase(getCodecA());
	}

	/**
	 * @return True if the audio codec is PCM.
	 */
	public boolean isPCM() {
		return FormatConfiguration.LPCM.equals(getCodecA()) || getCodecA() != null && getCodecA().startsWith("pcm_");
	}

	/**
	 * @return True if the audio codec is QDesign.
	 */
	public boolean isQDesign() {
		return FormatConfiguration.QDESIGN.equalsIgnoreCase(getCodecA());
	}

	/**
	 * @return True if the audio codec is RealAudio Lossless.
	 */
	public boolean isRALF() {
		return FormatConfiguration.RALF.equalsIgnoreCase(getCodecA());
	}

	/**
	 * @return True if the audio codec is RealAudio 14.4.
	 */
	public boolean isRealAudio14_4() {
		return FormatConfiguration.REALAUDIO_14_4.equalsIgnoreCase(getCodecA());
	}

	/**
	 * @return True if the audio codec is RealAudio 28.8.
	 */
	public boolean isRealAudio28_8() {
		return FormatConfiguration.REALAUDIO_28_8.equalsIgnoreCase(getCodecA());
	}

	/**
	 * @return True if the audio codec is Shorten.
	 */
	public boolean isShorten() {
		return FormatConfiguration.SHORTEN.equalsIgnoreCase(getCodecA());
	}

	/**
	 * @return True if the audio codec is Sipro Lab Telecom Audio Codec.
	 */
	public boolean isSipro() {
		return FormatConfiguration.SIPRO.equalsIgnoreCase(getCodecA());
	}

	/**
	 * @return True if the audio codec is SLS.
	 */
	public boolean isSLS() {
		return FormatConfiguration.SLS.equalsIgnoreCase(getCodecA());
	}

	/**
	 * @return True if the audio codec is TrueHD.
	 */
	public boolean isTrueHD() {
		return FormatConfiguration.TRUEHD.equalsIgnoreCase(getCodecA());
	}

	/**
	 * @return True if the audio codec is TTA.
	 */
	public boolean isTTA() {
		return FormatConfiguration.TTA.equalsIgnoreCase(getCodecA());
	}

	/**
	 * @return True if the audio codec is Vorbis.
	 */
	public boolean isVorbis() {
		return FormatConfiguration.VORBIS.equalsIgnoreCase(getCodecA());
	}

	/**
	 * @return True if the audio codec is WavPack.
	 */
	public boolean isWavPack() {
		return FormatConfiguration.WAVPACK.equalsIgnoreCase(getCodecA());
	}

	/**
	 * @return True if the audio codec is WMA.
	 */
	public boolean isWMA() {
		return FormatConfiguration.WMA.equalsIgnoreCase(getCodecA());
	}

	/**
	 * @return True if the audio codec is WMA10.
	 */
	public boolean isWMA10() {
		return FormatConfiguration.WMA10.equalsIgnoreCase(getCodecA());
	}

	/**
	 * @return True if the audio codec is WMA Lossless.
	 */
	public boolean isWMALossless() {
		return FormatConfiguration.WMALOSSLESS.equalsIgnoreCase(getCodecA());
	}

	/**
	 * @return True if the audio codec is WMA Pro.
	 */
	public boolean isWMAPro() {
		return FormatConfiguration.WMAPRO.equalsIgnoreCase(getCodecA());
	}

	/**
	 * @return True if the audio codec is WMA Voice.
	 */
	public boolean isWMAVoice() {
		return FormatConfiguration.WMAVOICE.equalsIgnoreCase(getCodecA());
	}

	/**
	 * @return True if the audio codec is AC-3, DTS, DTS-HD or TrueHD.
	 */
	public boolean isNonPCMEncodedAudio() {
		return isAC3() || isAtmos() || isDTS() || isTrueHD() || isDTSHD(); // isAAC() || isEAC3()
	}

	/**
	 * @return True if the audio codec is lossless.
	 */
	public boolean isLossless() {
		return getCodecA() != null &&
			(
				isALAC() || isALS() || isFLAC() || isMLP() ||
				isMonkeysAudio() || isPCM() || isRALF() || isShorten() ||
				isSLS() || isTrueHD() || isTTA() || isWavPack() ||
				isWMALossless()
			);
	}

	/**
	 * Returns a standardized name for the audio codec that is used.
	 *
	 * @return The standardized name.
	 */
	public String getAudioCodec() {
		if (isAACLC()) {
			return "AAC-LC";
		} else if (isAC3()) {
			return "AC3";
		} else if (isACELP()) {
			return "ACELP";
		} else if (isADPCM()) {
			return "ADPCM";
		} else if (isALAC()) {
			return "ALAC";
		} else if (isALS()) {
			return "ALS";
		} else if (isAtmos()) {
			return "Atmos";
		} else if (isATRAC()) {
			return "ATRAC";
		} else if (isCook()) {
			return "Cook";
		} else if (isDFF()) {
			return "DFF";
		} else if (isDOLBYE()) {
			return "Dolby E";
		} else if (isDSF()) {
			return "DSF";
		} else if (isDTS()) {
			return "DTS";
		} else if (isDTSHD()) {
			return "DTS-HD";
		} else if (isEAC3()) {
			return "Enhanced AC-3";
		} else if (isERBSAC()) {
			return "ER BSAC";
		} else if (isFLAC()) {
			return "FLAC";
		} else if (isG729()) {
			return "G.729";
		} else if (isHEAAC()) {
			return "HE-AAC";
		} else if (isMLP()) {
			return "MLP";
		} else if (isMonkeysAudio()) {
			return "Monkey's Audio";
		} else if (isMP3()) {
			return "MP3";
		} else if (isMpegAudio()) {
			return "Mpeg Audio";
		} else if (isMPC()) {
			return "Musepack";
		} else if (isOpus()) {
			return "Opus";
		} else if (isPCM()) {
			return "LPCM";
		} else if (isQDesign()) {
			return "QDesign";
		} else if (isRealAudio14_4()) {
			return "RealAudio 14.4";
		} else if (isRealAudio28_8()) {
			return "RealAudio 28.8";
		} else if (isRALF()) {
			return "RealAudio Lossless";
		} else if (isShorten()) {
			return "Shorten";
		} else if (isSipro()) {
			return "Sipro";
		} else if (isSLS()) {
			return "SLS";
		} else if (isTrueHD()) {
			return "TrueHD";
		} else if (isTTA()) {
			return "TTA";
		} else if (isVorbis()) {
			return "Vorbis";
		} else if (isWavPack()) {
			return "WavPack";
		} else if (isWMA()) {
			return "WMA";
		} else if (isWMA10()) {
			return "WMA 10";
		} else if (isWMALossless()) {
			return "WMA Lossless";
		} else if (isWMAPro()) {
			return "WMA Pro";
		} else if (isWMAVoice()) {
			return "WMA Voice";
		}
		return getCodecA() != null ? getCodecA() : "-";
	}

	/**
	 * Returns a string containing all identifying audio properties.
	 *
	 * @return The properties string.
	 */
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		if (isNotBlank(getLang()) && !getLang().equals("und")) {
			result.append("Id: ").append(getId());
			result.append(", Language Code: ").append(getLang());
		}

		if (isNotBlank(getAudioTrackTitleFromMetadata())) {
			if (result.length() > 0) {
				result.append(", ");
			}
			result.append("Audio Track Title From Metadata: ").append(getAudioTrackTitleFromMetadata());
		}

		if (result.length() > 0) {
			result.append(", ");
		}
		result.append("Audio Codec: ").append(getAudioCodec());

		if (!isBitRateUnknown()) {
			result.append(", Bitrate: ").append(getBitRate());
		}
		if (!isBitRateModeUnknown()) {
			result.append(", Bitrate Mode: ").append(getBitRateMode());
		}
		if (!isBitsPerSampleUnknown()) {
			result.append(", Bits per Sample: ").append(getBitsPerSample());
		}
		if (!isNumberOfChannelsUnknown()) {
			if (getNumberOfChannels() == 1) {
				result.append(", Channel: ").append(getNumberOfChannels());
			} else {
				result.append(", Channels: ").append(getNumberOfChannels());
			}
		}
		if (!isSampleFrequencyUnknown()) {
			result.append(", Sample Frequency: ").append(getSampleFrequency()).append(" Hz");
		}
		if (!isDelayUnknown()) {
			result.append(", Delay: ").append(getDelay()).append(" ms");
		}

		if (isNotBlank(getArtist())) {
			result.append(", Artist: ").append(getArtist());
		}
		if (isNotBlank(getAlbum())) {
			result.append(", Album: ").append(getAlbum());
		}
		if (isNotBlank(getSongname())) {
			result.append(", Track Name: ").append(getSongname());
		}
		if (getYear() != 0) {
			result.append(", Year: ").append(getYear());
		}
		if (getTrack() != 0) {
			result.append(", Track: ").append(getTrack());
		}
		if (isNotBlank(getGenre())) {
			result.append(", Genre: ").append(getGenre());
		}

		if (isNotBlank(getMuxingModeAudio())) {
			result.append(", Muxing Mode: ").append(getMuxingModeAudio());
		}

		return result.toString();
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	/**
	 * Returns the number of bits per sample for the audio.
	 *
	 * @return The number of bits per sample.
	 * @since 1.50
	 */
	public int getBitsPerSample() {
		return bitsPerSample > 0 ? bitsPerSample : BITSPERSAMPLE_DEFAULT;
	}

	/**
	 * Returns the number of bits per sample for the audio without using
	 * default.
	 *
	 * @return The raw number of bits per sample.
	 */
	public int getBitsPerSampleRaw() {
		return bitsPerSample;
	}

	/**
	 * @return {@code true} if bits per sample is unknown and a default value is
	 *         used, {@code false} otherwise.
	 */
	public boolean isBitsPerSampleUnknown() {
		return bitsPerSample <= 0;
	}

	/**
	 * Sets the number of bits per sample for the audio.
	 *
	 * @param bitsPerSample The number of bits per sample to set.
	 * @since 1.50
	 */
	public void setBitsPerSample(int bitsPerSample) {
		this.bitsPerSample = bitsPerSample;
	}

	/**
	 * Returns the bitrate for this audio track.
	 *
	 * @return The bitrate.
	 */
	public int getBitRate() {
		return bitRate > 0 ? bitRate : BITRATE_DEFAULT;
	}

	/**
	 * Returns the bitrate for this audio track without using default.
	 *
	 * @return The raw bitrate value.
	 */
	public int getBitRateRaw() {
		return bitRate;
	}

	/**
	 * @return {@code true} if the bitrate is unknown and a default value is
	 *         used, {@code false} otherwise.
	 */
	public boolean isBitRateUnknown() {
		return bitRate <= 0;
	}

	/**
	 * Sets the audio bitrate.
	 *
	 * @param bitRate the audio bitrate to set.
	 */
	public void setBitRate(int bitRate) {
		this.bitRate = bitRate;
	}

	/**
	 * @return the bitrate mode.
	 */
	@Nonnull
	public RateMode getBitRateMode() {
		return bitRateMode == null ? BITRATEMODE_DEFAULT : bitRateMode;
	}

	/**
	 * @return the bitrate mode without using default.
	 */
	@Nullable
	public RateMode getBitRateModeRaw() {
		return bitRateMode;
	}

	/**
	 * @return {@code true} if the bitrate mode is unknown and a default value
	 *         is used, {@code false} otherwise.
	 */
	public boolean isBitRateModeUnknown() {
		return bitRateMode == null;
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
	 * Returns the sample frequency for the audio.
	 *
	 * @return The sample frequency.
	 * @since 1.50
	 */
	public int getSampleFrequency() {
		return sampleFrequency > 0 ? sampleFrequency : SAMPLEFREQUENCY_DEFAULT;
	}

	/**
	 * Returns the sample frequency for the audio without using default.
	 *
	 * @return The raw sample frequency.
	 */
	public int getSampleFrequencyRaw() {
		return sampleFrequency;
	}

	/**
	 * @return {@code true} if the sample frequency is unknown and a default
	 *         value is used, {@code false} otherwise.
	 */
	public boolean isSampleFrequencyUnknown() {
		return sampleFrequency <= 0;
	}

	/**
	 * Sets the sample frequency for the audio.
	 *
	 * @param sampleFrequency The sample frequency to set.
	 * @since 1.50
	 */
	public void setSampleFrequency(int sampleFrequency) {
			this.sampleFrequency = sampleFrequency;
			audioProperties.setSampleFrequency(sampleFrequency);
	}

	/**
	 * Returns the number of audio channels.
	 *
	 * @return The number of channels
	 * @since 1.50
	 */
	public int getNumberOfChannels() {
		return numberOfChannels > 0 ? numberOfChannels : NUMBEROFCHANNELS_DEFAULT;
	}

	/**
	 * Returns the number of audio channels without using default.
	 *
	 * @return The raw number of channels.
	 */
	public int getNumberOfChannelsRaw() {
		return numberOfChannels;
	}

	/**
	 * @return {@code true} if the number of channels is unknown and a default
	 *         value is used, {@code false} otherwise.
	 */
	public boolean isNumberOfChannelsUnknown() {
		return numberOfChannels <= 0;
	}

	/**
	 * Sets the number of channels for the audio.
	 *
	 * @param numberOfChannels The number of channels to set.
	 */
	public void setNumberOfChannels(int numberOfChannels) {
		this.numberOfChannels = numberOfChannels;
		audioProperties.setNumberOfChannels(numberOfChannels);
	}

	/**
	 * Returns the name of the audio codec that is being used.
	 *
	 * @return The name of the audio codec.
	 * @since 1.50
	 */
	public String getCodecA() {
		return codecA;
	}

	/**
	 * Sets the name of the audio codec that is being used.
	 *
	 * @param codecA The name of the audio codec to set.
	 * @since 1.50
	 */
	public void setCodecA(String codecA) {
		this.codecA = codecA != null ? codecA.toLowerCase(Locale.ROOT) : null;
	}

	/**
	 * Returns the name of the album to which an audio track belongs.
	 *
	 * @return The album name.
	 * @since 1.50
	 */
	public String getAlbum() {
		return album;
	}

	/**
	 * Sets the name of the album to which an audio track belongs.
	 *
	 * @param album The name of the album to set.
	 * @since 1.50
	 */
	public void setAlbum(String album) {
		this.album = album;
	}

	/**
	 * Returns the name of the artist performing the audio track.
	 *
	 * @return The artist name.
	 * @since 1.50
	 */
	public String getArtist() {
		return artist;
	}

	/**
	 * Sets the name of the artist performing the audio track.
	 *
	 * @param artist The artist name to set.
	 * @since 1.50
	 */
	public void setArtist(String artist) {
		this.artist = artist;
	}

	/**
	 * Returns the name of the song for the audio track.
	 *
	 * @return The song name.
	 * @since 1.50
	 */
	public String getSongname() {
		return songname;
	}

	/**
	 * Sets the name of the song for the audio track.
	 *
	 * @param songname The song name to set.
	 * @since 1.50
	 */
	public void setSongname(String songname) {
		this.songname = songname;
	}

	/**
	 * Returns the name of the genre for the audio track.
	 *
	 * @return The genre name.
	 * @since 1.50
	 */
	public String getGenre() {
		return genre;
	}

	/**
	 * Sets the name of the genre for the audio track.
	 *
	 * @param genre The name of the genre to set.
	 * @since 1.50
	 */
	public void setGenre(String genre) {
		this.genre = genre;
	}

	/**
	 * Returns the year of inception for the audio track.
	 *
	 * @return The year.
	 * @since 1.50
	 */
	public int getYear() {
		return year;
	}

	/**
	 * Sets the year of inception for the audio track.
	 *
	 * @param year The year to set.
	 * @since 1.50
	 */
	public void setYear(int year) {
		this.year = year;
	}

	/**
	 * Returns the track number within an album for the audio.
	 *
	 * @return The track number.
	 * @since 1.50
	 */
	public int getTrack() {
		return track;
	}

	/**
	 * Sets the track number within an album for the audio.
	 *
	 * @param track The track number to set.
	 * @since 1.50
	 */
	public void setTrack(int track) {
		this.track = track;
	}

	/**
	 * Returns the delay for the audio in milliseconds.
	 *
	 * @return The delay.
	 * @since 1.50
	 */
	public int getDelay() {
		return delay > Integer.MIN_VALUE ? delay : AUDIODELAY_DEFAULT;
	}

	/**
	 * Returns the delay for the audio in milliseconds without using default.
	 *
	 * @return The raw delay.
	 */
	public int getDelayRaw() {
		return delay;
	}

	/**
	 * @return {@code true} if the audio delay is unknown and a default value is
	 *         used, {@code false} otherwise.
	 */
	public boolean isDelayUnknown() {
		return delay == Integer.MIN_VALUE;
	}

	/**
	 * Sets the delay for the audio.
	 *
	 * @param audioDelay The delay to set.
	 * @since 1.50
	 */
	public void setDelay(int audioDelay) {
		this.delay = audioDelay;
		audioProperties.setAudioDelay(audioDelay);
	}

	/**
	 * @deprecated use getAudioTrackTitleFromMetadata()
	 */
	@Deprecated
	public String getFlavor() {
		return getAudioTrackTitleFromMetadata();
	}

	/**
	 * @deprecated use setAudioTrackTitleFromMetadata()
	 */
	@Deprecated
	public void setFlavor(String value) {
		setAudioTrackTitleFromMetadata(value);
	}

	public String getAudioTrackTitleFromMetadata() {
		return audioTrackTitleFromMetadata;
	}

	public void setAudioTrackTitleFromMetadata(String value) {
		this.audioTrackTitleFromMetadata = value;
	}

	/**
	 * Returns the audio codec to use for muxing.
	 *
	 * @return The audio codec to use.
	 * @since 1.50
	 */
	public String getMuxingModeAudio() {
		return muxingModeAudio;
	}

	/**
	 * Sets the audio codec to use for muxing.
	 *
	 * @param muxingModeAudio The audio codec to use.
	 * @since 1.50
	 */
	public void setMuxingModeAudio(String muxingModeAudio) {
		this.muxingModeAudio = muxingModeAudio;
	}

	public AudioProperties getAudioProperties() {
		return audioProperties;
	}

	public void setAudioProperties(AudioProperties audioProperties) {
		if (audioProperties == null) {
			throw new IllegalArgumentException("Can't set null AudioProperties.");
		}
		this.audioProperties = audioProperties;
	}
}
