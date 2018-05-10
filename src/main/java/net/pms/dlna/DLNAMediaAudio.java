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

import java.util.Arrays;
import java.util.Locale;
import net.pms.configuration.FormatConfiguration;
import net.pms.formats.v2.AudioProperties;
import net.pms.util.BitRateMode;
import net.pms.util.UMSUtils;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class keeps track of the audio properties of media.
 *
 * TODO: Change all instance variables to private. For backwards compatibility
 * with external plugin code the variables have all been marked as deprecated
 * instead of changed to private, but this will surely change in the future.
 * When everything has been changed to private, the deprecated note can be
 * removed.
 */
@SuppressWarnings("deprecation")
public class DLNAMediaAudio extends DLNAMediaLang implements Cloneable {
	private static final Logger LOGGER = LoggerFactory.getLogger(DLNAMediaAudio.class);
	private AudioProperties audioProperties = new AudioProperties();

	/** The default bit rate used if the actual value is unknown */
	public static final int BITRATE_DEFAULT = 8000;

	/** The default number of audio channels used if the actual value is unknown */
	public static final int NUMBEROFCHANNELS_DEFAULT = 2;

	/** The default bits per sample used if the actual value is unknown */
	public static final int BITSPERSAMPLE_DEFAULT = 16;

	/** The default audio delay used if the actual value is unknown */
	public static final int AUDIODELAY_DEFAULT = 0;

	/** The default sample rate used if the actual value is unknown */
	public static final int SAMPLERATE_DEFAULT = 48000;

	/** The default bit rate mode used if the actual value is unknown */
	public static final BitRateMode BITRATEMODE_DEFAULT = BitRateMode.CBR;

	/**
	 * @deprecated Use standard getter and setter to access this variable.
	 */
	@Deprecated
	public int bitsperSample;
	private int[] bitsPerSampleArray;

	private int bitRate;
	private BitRateMode[] BitRateModes;

	/**
	 * @deprecated Use standard getter and setter to access this variable.
	 */
	@Deprecated
	public String sampleFrequency;

	private int[] sampleRatesArray;

	/**
	 * @deprecated Use standard getter and setter instead.
	 */
	@Deprecated
	public int nrAudioChannels;

	private int[] numberOfChannelsArray;

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

	/**
	 * @deprecated Use standard getter and setter to access this variable.
	 */
	@Deprecated
	public int delay;

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
		return FormatConfiguration.AAC_LC.equalsIgnoreCase(getCodecA());
	}

	/**
	 * @return True if the audio codec is AC-3.
	 */
	public boolean isAC3() {
		return FormatConfiguration.AC3.equalsIgnoreCase(getCodecA()) || getCodecA() != null && getCodecA().contains("a52");
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
	 * @return True if the audio codec is AIFF.
	 */
	public boolean isAIFF() {
		return FormatConfiguration.AIFF.equalsIgnoreCase(getCodecA());
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
	 * @return True if the audio codec is Atmos.
	 */
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
	 * @return True if the audio codec is DSF.
	 */
	public boolean isDSF() {
		return FormatConfiguration.DSF.equalsIgnoreCase(getCodecA());
	}

	/**
	 * @return True if the audio codec is DTS.
	 */
	public boolean isDTS() {
		return FormatConfiguration.DTS.equalsIgnoreCase(getCodecA()) || getCodecA() != null && getCodecA().contains("dca");
	}

	/**
	 * @return True if the audio codec is DTS HD.
	 */
	public boolean isDTSHD() {
		return FormatConfiguration.DTSHD.equalsIgnoreCase(getCodecA());
	}

	/**
	 * @return True if the audio codec is EAC-3.
	 */
	public boolean isEAC3() {
		return FormatConfiguration.EAC3.equalsIgnoreCase(getCodecA());
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
		return FormatConfiguration.HE_AAC.equalsIgnoreCase(getCodecA());
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
		return FormatConfiguration.MP3.equalsIgnoreCase(getCodecA());
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
		return FormatConfiguration.LPCM.equals(getCodecA()) || getCodecA() != null && getCodecA().startsWith("pcm");
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
	 * @return True if the audio codec is Wav.
	 */
	public boolean isWAV() {
		return FormatConfiguration.WAV.equalsIgnoreCase(getCodecA());
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
		return isAC3() || isAtmos() || isDTS() || isTrueHD() || isDTSHD();
	}

	/**
	 * @return True if the audio codec is lossless.
	 */
	public boolean isLossless() {
		return getCodecA() != null &&
			(
				isAIFF() || isALAC() || isALS() || isFLAC() || isMLP() ||
				isMonkeysAudio() || isPCM() || isRALF() || isShorten() ||
				isSLS() || isTrueHD() || isTTA() || isWAV() || isWavPack() ||
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
		} else if (isAIFF()) {
			return "AIFF";
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
		} else if (isWAV()) {
			return "WAV";
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
	public String toString() { //TODO: (Nad) Refactor
		StringBuilder result = new StringBuilder();
		if (getLang() != null && !getLang().equals("und")) {
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

		result.append(", Bitrate: ").append(getBitRate());
		if (getBitsperSample() != 16) {
			result.append(", Bits per Sample: ").append(getBitsperSample());
		}
		if (isBitRateModeKnown()) {
			if (BitRateModes.length > 1) {
				result.append(", Bitrate Modes: ").append(Arrays.toString(BitRateModes));
			} else {
				result.append(", Bitrate Mode: ").append(BitRateModes[0]);
			}
		}
		if (getAudioProperties() != null) {
			result.append(", ").append(getAudioProperties());
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
	protected DLNAMediaAudio clone() throws CloneNotSupportedException {
		DLNAMediaAudio clone = (DLNAMediaAudio) super.clone();
		if (audioProperties != null) {
			clone.audioProperties = audioProperties.clone();
		}
		clone.bitsPerSampleArray = ArrayUtils.clone(bitsPerSampleArray);
		clone.numberOfChannelsArray = ArrayUtils.clone(numberOfChannelsArray);
		clone.sampleRatesArray = ArrayUtils.clone(sampleRatesArray);
		//TODO: (Nad) Add new arrays
		return clone;
	}

	/**
	 * @return Whether bits per sample is an actual value or just a default.
	 */
	public boolean isBitsPerSampleKnown() {
		return
			bitsPerSampleArray != null && UMSUtils.getIntArrayMaxValue(bitsPerSampleArray, 0) > 0 ||
			bitsperSample > 0;
	}

	/**
	 * Returns the number of bits per sample for the audio.
	 *
	 * @return The number of bits per sample.
	 * @since 1.50
	 * @deprecated Use {@link #getBitsPerSample()} instead.
	 */
	@Deprecated
	public int getBitsperSample() {
		return getBitsPerSample();
	}

	/**
	 * Returns an array of bits per sample, with the "base" layer at index 0.
	 *
	 * @return The array of bits per sample or an array containing only
	 *         {@link #BITSPERSAMPLE_DEFAULT} if unknown.
	 */
	public int[] getBitsPerSampleArray() {
		if (
			bitsPerSampleArray != null &&
				bitsPerSampleArray.length > 0 &&
			UMSUtils.getIntArrayMaxValue(bitsPerSampleArray, 0) > 0
		) {
			int[] result = new int[bitsPerSampleArray.length];
			System.arraycopy(bitsPerSampleArray, 0, result, 0, bitsPerSampleArray.length);
			return result;
		}
		return new int[] {getBitsPerSample()};
	}

	/**
	 * Returns the highest bits per sample if there are multiple values,
	 * otherwise the single value.
	 *
	 * @return The bits per sample or {@link #BITSPERSAMPLE_DEFAULT} if unknown.
	 * @since 6.7.2
	 */
	public int getBitsPerSample() {
		if (bitsPerSampleArray != null && bitsPerSampleArray.length > 0) {
			int highest = UMSUtils.getIntArrayMaxValue(bitsPerSampleArray, 0);
			if (highest > 0) {
				return highest;
			}
		}
		if (bitsperSample > 0) {
			return bitsperSample;
		}
		return BITSPERSAMPLE_DEFAULT;
	}

	/**
	 * Sets the number of bits per sample for the audio.
	 *
	 * @param bitsperSample The number of bits per sample to set.
	 * @since 1.50
	 * @deprecated Use {@link #setBitsPerSample(int)} instead.
	 */
	@Deprecated
	public void setBitsperSample(int bitsperSample) {
		setBitsPerSample(bitsperSample);
	}

	/**
	 * Sets the bits per sample.
	 *
	 * @param bitsPerSample the bits per sample to set, use 0 if unknown.
	 * @since 6.7.2
	 */
	public void setBitsPerSample(int bitsPerSample) {
		setBitsPerSample(new int[] {bitsPerSample});
	}

	/**
	 * Sets an array of bits per sample.
	 *
	 * @param bitsPerSampleArray the array of bits per sample to set, use
	 *            {@code null} or an empty array if unknown.
	 * @since 6.7.2
	 */
	public void setBitsPerSample(int[] bitsPerSampleArray) {
		if (bitsPerSampleArray == null) {
			if (this.bitsPerSampleArray == null || this.bitsPerSampleArray.length > 0) {
				this.bitsPerSampleArray = new int[0];
			}
		} else {
			if (this.bitsPerSampleArray == null || this.bitsPerSampleArray.length != bitsPerSampleArray.length) {
				this.bitsPerSampleArray = new int[bitsPerSampleArray.length];
			}
			System.arraycopy(bitsPerSampleArray, 0, this.bitsPerSampleArray, 0, bitsPerSampleArray.length);
		}
		// Find the highest number to store in the deprecated field
		bitsperSample = UMSUtils.getIntArrayMaxValue(this.bitsPerSampleArray, 0);
	}

	/**
	 * Returns the bit rate for this audio track.
	 *
	 * @return The bit rate, or {@link #BITRATE_DEFAULT} if {@code bitRate} is
	 *         invalid.
	 */
	public int getBitRate() {
		return bitRate > 0 ? bitRate : BITRATE_DEFAULT;
	}

	/**
	 * Sets the audio bit rate.
	 *
	 * @param bitRate the audio bit rate to set.
	 */
	public void setBitRate(int bitRate) {
		this.bitRate = bitRate;
	}

	/**
	 * @return Whether bit rate mode is an actual value or just a default.
	 */
	public boolean isBitRateModeKnown() {
		return BitRateModes != null && BitRateModes.length > 0;
	}

	/**
	 * Gets the array of bit rate modes, with the "base" layer at index 0.
	 *
	 * @return The array of bit rate modes or an array containing only
	 *         {@link #BITRATEMODE_DEFAULT} if unknown.
	 */
	public BitRateMode[] getBitRateModes() {
		if (BitRateModes != null && BitRateModes.length > 0) {
			BitRateMode[] result = new BitRateMode[BitRateModes.length];
			System.arraycopy(BitRateModes, 0, result, 0, BitRateModes.length);
			return result;
		}
		return new BitRateMode[] {getBitRateMode()};
	}

	/**
	 * If known, returns {@link BitRateMode#VBR} if one of the bit rate mode
	 * values are {@link BitRateMode#VBR}, otherwise {@link BitRateMode#CBR}. If
	 * unknown, {@value #BITRATEMODE_DEFAULT} is returned.
	 *
	 * @return The bit rate mode with preference to {@link BitRateMode#VBR} or
	 *         {@link #BITRATEMODE_DEFAULT} if unknown.
	 */
	public BitRateMode getBitRateMode() {
		if (BitRateModes != null && BitRateModes.length > 0) {
			boolean isVariable = false;
			for (BitRateMode bitRateMode : BitRateModes) {
				if (bitRateMode == BitRateMode.VBR) {
					isVariable = true;
					break;
				}
			}
			return isVariable ? BitRateMode.VBR : BitRateMode.CBR;
		}
		return BITRATEMODE_DEFAULT;
	}

	/**
	 * Sets the bit rate mode.
	 *
	 * @param bitRateMode the bit rate mode to set, use {@code null} if unknown.
	 */
	public void setBitRateMode(BitRateMode bitRateMode) {
		if (bitRateMode == null && this.BitRateModes != null && this.BitRateModes.length > 0) {
			BitRateModes = new BitRateMode[0];
		} else if (bitRateMode != null) {
			setBitRateModes(new BitRateMode[] {bitRateMode});
		}
	}

	/**
	 * Sets an array of bit rate modes.
	 *
	 * @param bitRateModes the array of bit rate modes to set, use
	 *            {@code null} or an empty array if unknown.
	 * @since 6.7.2
	 */
	public void setBitRateModes(BitRateMode[] bitRateModes) {
		if (bitRateModes == null && this.BitRateModes != null && this.BitRateModes.length > 0) {
			BitRateModes = new BitRateMode[0];
		} else if (bitRateModes != null) {
			if (this.BitRateModes == null || this.BitRateModes.length != bitRateModes.length) {
				this.BitRateModes = new BitRateMode[bitRateModes.length];
			}
			System.arraycopy(bitRateModes, 0, this.BitRateModes, 0, bitRateModes.length);
		}
	}

	/**
	 * @return Whether sample rate is an actual value or just a default.
	 */
	public boolean isSampleRateKnown() {
		return
			sampleRatesArray != null && UMSUtils.getIntArrayMaxValue(sampleRatesArray, 0) > 0 ||
			audioProperties != null && audioProperties.getSampleFrequency(false) > 0;
	}

	/**
	 * Returns the sample frequency for the audio.
	 *
	 * @return The sample frequency.
	 * @since 1.50
	 * @deprecated Use {@link #getSampleRatesArray()} or
	 *             {@link #getSampleRate()} instead.
	 */
	@Deprecated
	public String getSampleFrequency() {
		return Integer.toString(getSampleRate());
	}

	/**
	 * Returns an array of sample rates, with the "base" layer at index 0.
	 *
	 * @return The array of sample rates or an array containing only
	 *         {@link #SAMPLERATE_DEFAULT} if unknown.
	 */
	public int[] getSampleRatesArray() {
		if (
			sampleRatesArray != null &&
				sampleRatesArray.length > 0 &&
			UMSUtils.getIntArrayMaxValue(sampleRatesArray, 0) > 0
		) {
			int[] result = new int[sampleRatesArray.length];
			System.arraycopy(sampleRatesArray, 0, result, 0, sampleRatesArray.length);
			return result;
		}
		return new int[] {getSampleRate()};
	}

	/**
	 * Returns the highest sample rate if there are multiple values, otherwise
	 * the single value.
	 *
	 * @return The sample rate or {@link #SAMPLERATE_DEFAULT} if unknown.
	 * @since 6.7.2
	 */
	public int getSampleRate() {
		if (sampleRatesArray != null && sampleRatesArray.length > 0) {
			int highest = UMSUtils.getIntArrayMaxValue(sampleRatesArray, 0);
			if (highest > 0) {
				return highest;
			}
		}
		if (!isBlank(sampleFrequency)) {
			try {
				return Integer.parseInt(sampleFrequency);
			} catch (NumberFormatException e) {
				// Just catch and move on
			}
		}
		if (audioProperties != null && audioProperties.getSampleFrequency() > 0) {
			return audioProperties.getSampleFrequency();
		}
		return SAMPLERATE_DEFAULT;
	}

	/**
	 * Sets the sample frequency for this audio.
	 *
	 * @param sampleFrequency The sample frequency to set.
	 * @since 1.50
	 * @deprecated Use {@link #setSampleRates(int[])} or
	 *             {@link #setSampleRate(int)} instead.
	 */
	@Deprecated
	public void setSampleFrequency(String sampleFrequency) {
		if (isNotBlank(sampleFrequency)) {
			try {
				setSampleRate(Integer.parseInt(sampleFrequency));
			} catch (NumberFormatException e) {
				LOGGER.warn("Audio sample frequency \"{}\" cannot be parsed, using (probably wrong) default", sampleFrequency);
			}
		}
	}

	/**
	 * Sets the sample rate.
	 *
	 * @param sampleRate the sample rate to set, use 0 if unknown.
	 * @since 6.7.2
	 */
	public void setSampleRate(int sampleRate) {
		setSampleRates(new int[] {sampleRate});
	}

	/**
	 * Sets an array of sample rates.
	 *
	 * @param sampleRatesArray the array of sample rates to set, use
	 *            {@code null} or an empty array if unknown.
	 * @since 6.7.2
	 */
	public void setSampleRates(int[] sampleRatesArray) {
		if (sampleRatesArray == null) {
			if (this.sampleRatesArray == null || this.sampleRatesArray.length > 0) {
				this.sampleRatesArray = new int[0];
			}
		} else {
			if (this.sampleRatesArray == null || this.sampleRatesArray.length != sampleRatesArray.length) {
				this.sampleRatesArray = new int[sampleRatesArray.length];
			}
			System.arraycopy(sampleRatesArray, 0, this.sampleRatesArray, 0, sampleRatesArray.length);
		}
		// Find the highest number to store in the deprecated fields
		int highest = UMSUtils.getIntArrayMaxValue(this.sampleRatesArray, 0);
		this.sampleFrequency = Integer.toString(highest);
		audioProperties.setSampleFrequency(highest);
	}

	/**
	 * @return Whether the number of channels is an actual value or just a default.
	 */
	public boolean isNumberOfChannelsKnown() {
		return
			numberOfChannelsArray != null && UMSUtils.getIntArrayMaxValue(numberOfChannelsArray, 0) > 0 ||
			nrAudioChannels > 0 ||
			audioProperties != null && audioProperties.getNumberOfChannels(false) > 0;
	}

	/**
	 * Returns the number of channels for the audio.
	 *
	 * @return The number of channels
	 * @since 1.50
	 * @deprecated Use {@link #getNumberOfChannels()} instead.
	 */
	@Deprecated
	public int getNrAudioChannels() {
		return getNumberOfChannels();
	}

	/**
	 * Returns an array with the number of audio channels, with the "base" layer
	 * at index 0.
	 *
	 * @return The array of audio channels or an array containing only
	 *         {@link #NUMBEROFCHANNELS_DEFAULT} if unknown.
	 */
	public int[] getNumberOfChannelsArray() {
		if (
			numberOfChannelsArray != null &&
			numberOfChannelsArray.length > 0 &&
			UMSUtils.getIntArrayMaxValue(numberOfChannelsArray, 0) > 0
		) {
			int[] result = new int[numberOfChannelsArray.length];
			System.arraycopy(numberOfChannelsArray, 0, result, 0, numberOfChannelsArray.length);
			return result;
		}
		return new int[] {getNumberOfChannels()};
	}

	/**
	 * Returns the highest number of audio channels if there are multiple
	 * values, otherwise the single value.
	 *
	 * @return The number of channels or {@link #NUMBEROFCHANNELS_DEFAULT} if
	 *         unknown.
	 */
	public int getNumberOfChannels() {
		if (numberOfChannelsArray != null && numberOfChannelsArray.length > 0) {
			int highest = UMSUtils.getIntArrayMaxValue(numberOfChannelsArray, 0);
			if (highest > 0) {
				return highest;
			}
		}
		if (nrAudioChannels > 0) {
			return nrAudioChannels;
		}
		if (audioProperties != null && audioProperties.getNumberOfChannels() > 0) {
			return audioProperties.getNumberOfChannels();
		}
		return NUMBEROFCHANNELS_DEFAULT;
	}

	/**
	 * Sets the number of channels for the audio.
	 *
	 * @param numberOfChannels The number of channels to set.
	 * @since 1.50
	 * @deprecated Use {@link #setNumberOfChannels(int)} instead.
	 */
	@Deprecated
	public void setNrAudioChannels(int numberOfChannels) {
		setNumberOfChannels(numberOfChannels);
	}

	/**
	 * Sets the number of audio channels.
	 *
	 * @param numberOfChannels the number of channels to set, use 0 if unknown.
	 */
	public void setNumberOfChannels(int numberOfChannels) {
		setNumberOfChannels(new int[] {numberOfChannels});
	}

	/**
	 * Sets an array of audio channels.
	 *
	 * @param numberOfChannelsArray the array of the number of channels to set,
	 *            use {@code null} or an empty array if unknown.
	 */
	public void setNumberOfChannels(int[] numberOfChannelsArray) {
		if (numberOfChannelsArray == null) {
			if (this.numberOfChannelsArray == null || this.numberOfChannelsArray.length > 0) {
				this.numberOfChannelsArray = new int[0];
			}
		} else {
			if (this.numberOfChannelsArray == null || this.numberOfChannelsArray.length != numberOfChannelsArray.length) {
				this.numberOfChannelsArray = new int[numberOfChannelsArray.length];
			}
			System.arraycopy(numberOfChannelsArray, 0, this.numberOfChannelsArray, 0, numberOfChannelsArray.length);
		}
		// Find the highest number to store in the deprecated fields
		this.nrAudioChannels = UMSUtils.getIntArrayMaxValue(this.numberOfChannelsArray, 0);
		audioProperties.setNumberOfChannels(this.nrAudioChannels);
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
		if (delay == 0 && audioProperties != null && audioProperties.getAudioDelay() != 0) {
			return audioProperties.getAudioDelay();
		}
		return delay;
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

	/**
	 * @deprecated Use the methods of this class to get the values instead.
	 */
	@Deprecated
	public AudioProperties getAudioProperties() {
		return audioProperties;
	}

	/**
	 * @param audioProperties the {@link AudioProperties} instance.
	 * @deprecated Use the methods of this class to get and set values instead.
	 */
	@Deprecated
	public void setAudioProperties(AudioProperties audioProperties) {
		if (audioProperties == null) {
			throw new IllegalArgumentException("Can't set null AudioProperties.");
		}
		this.audioProperties = audioProperties;
	}
}
