package net.pms.dlna.protocolinfo;

import static org.apache.commons.lang3.StringUtils.isBlank;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import javax.annotation.Nonnull;


/**
 * This {@code enum} contains pre-defined {@link MimeType}s that can be used
 * without having to explicitly create a new instance.
 *
 * @author Nadahar
 */
public enum KnownMimeTypes implements MimeType {

	/** Dolby Digital Audio Codec */
	AC3("audio", "vnd.dolby.dd-raw"),

	/** Adaptive Differential Pulse-Code Modulation (audio) */
	ADPCM("audio", "x-adpcm"),

	/** MPEG-4 Audio Data Transport Stream */
	ADTS("audio", "vnd.dlna.adts"),

	/** Audio Interchange File Format */
	AIFF("audio", "aiff"),

	/** Monkey's Audio */
	APE("audio", "x-ape"),

	/** Advances Systems Format (video) */
	ASF("video", "x-ms-asf"),

	/** Adaptive Transform Acoustic Coding (audio) */
	ATRAC("audio", "x-sony-oma"),

	/** Sun Au file format (audio) */
	AU("audio", "basic"),

	/** Audio Video Interleave */
	AVI("video", "avi"),

	/** Bitmap Image File/DIB */
	BMP("image", "bmp"),

	/** Direct Stream Digital multichannel (audio) */
	DFF("audio", "x-dff"),

	/** DivX */
	DIVX("video", "divx"),

	/** Direct Stream Digital stereo (audio) */
	DSF("audio", "x-dsf"),

	/** Digital Theater Systems (audio) */
	DTS("audio", "vnd.dts"),

	/** Digital Theater Systems High Resolution Audio */
	DTS_HD("audio", "vnd.dts.hd"),

	/** Microsoft Digital Video Recording */
	DVR_MS("video", "x-ms-dvr"),

	/** Dolby Digital Plus/Enhanced AC-3 (audio) */
	EAC3("audio", "eac3"),

	/** Free Lossless Audio Codec */
	FLAC("audio", "x-flac"),

	/** Flash Video */
	FLASH("video", "flash"),

	/** Flash Video */
	FLV("video", "x-flv"),

	/** Graphics Interchange Format (image) */
	GIF("image", "gif"),

	/** Joint Photographic Experts Group (image) */
	JPEG("image", "jpeg"),

	/** Linear Pulse-Code Modulation */
	LPCM("audio", "L16"),

	/** Blu-ray Disc Audio-Video MPEG-2 Transport Stream */
	M2TS("video", "video/mp2t"),

	/** M3U and M3U8 playlist */
	M3U("application", "mpegurl"),

	/** Audio-only MPEG-4 */
	M4A("audio", "mp4"),

	/** Matroska Multimedia Container */
	MATROSKA("video", "x-matroska"),

	/** Matroska Audio */
	MKA("audio", "x-matroska"),

	/** Meridian Lossless Packing (audio) */
	MLP("audio", "vnd.dolby.mlp"),

	/** QuickTime File Format */
	MOV("video", "quicktime"),

	/** MPEG-1 Audio Layer II */
	MP2("audio", "mpeg"),

	/** MPEG-1 or MPEG-2 Audio Layer III */
	MP3("audio", "mpeg"),

	/** MPEG-4 Part 14 */
	MP4("video", "mp4"),

	/** MPEG-1 Audio Layer I */
	MPA("audio", "mpeg"),

	/** Musepack (audio) */
	MPC("audio", "x-musepack"),

	/** Moving Picture Experts Group */
	MPEG("video", "mpeg"),

	/** MPEG Transport Stream */
	MPEG_TS("video", "vnd.dlna.mpeg-tts"),

	/** JPEG Multi-Picture Format */
	MPO("image", "mpo"),

	/** Ogg Audio */
	OGA("audio", "ogg"),

	/** Ogg */
	OGG("video", "ogg"),

	/** Portable Network Graphics */
	PNG("image", "png"),

	/** RealAudio */
	RA("audio", "vnd.rn-realaudio"),

	/** Generic "raw" image file */
	RAW("image", "x-raw"),

	/** RealMedia */
	RM("application", "vnd.rn-realmedia"),

	/** Shorten (audio) */
	SHN("audio", "x-shn"),

	/** 3rd Generation Partnership Project */
	THREEGPP("video", "3gpp"),

	/** 3rd Generation Partnership Project 2 */
	THREEGPP2("video", "3gpp2"),

	/** 3rd Generation Partnership Project 2 Audio */
	THREEGPP2A("audio", "3gpp2"),

	/** 3rd Generation Partnership Project Audio */
	THREEGPPA("audio", "3gpp"),

	/** Tagged Image File Format */
	TIFF("image", "tiff"),

	/** Dolby TrueHD (audio) */
	TRUE_HD("audio", "vnd.dolby.mlp"),

	/** True Audio */
	TTA("audio", "x-tta"),

	/** SMPTE 421M (video) */
	VC1("video", "mpeg"),

	/** DVD Video Object */
	VOB("video", "mpeg"),

	/** Waveform Audio File Format */
	WAV("audio", "wav"),

	/** WebM */
	WEBM("video", "webm"),

	/** WebM Audio */
	WEBM_AUDIO("audio", "webm"),

	/** Windows Media Audio */
	WMA("audio", "x-ms-wma"),

	/** Windows Media Video */
	WMV("video", "x-ms-wmv"),

	/** WavPack (audio) */
	WV("audio", "x-wavpack");

	/** The cached {@link #toString()} value */
	protected final String stringValue;

	/** The subtype. */
	private final String subtype;

	/** The type. */
	private final String type;

	private KnownMimeTypes(String type, String subtype) {
		this.type = type;
		this.subtype = subtype;
		this.stringValue = type + "/" + subtype;
	}

	@Override
	public boolean equalBaseValue(MimeType other) {
		return evaluateEquals(other, false);
	}

	@Override
	public boolean equalValue(MimeType other) {
		return evaluateEquals(other, true);
	}

	/**
	 * Evaluates the {@link MimeType} specific "equals" implementations.
	 *
	 * @param other the {@link MimeType} to compare with.
	 * @param equalParameters whether the parameters should be compared.
	 * @return {@code true} if the two are considered equal, {@code false}
	 *         otherwise.
	 */
	protected boolean evaluateEquals(MimeType other, boolean equalParameters) {
		if (this == other) {
			return true;
		}
		if (other == null) {
			return false;
		}
		if (equalParameters && !other.getParameters().isEmpty()) {
			return false;
		}
		if (subtype == null) {
			if (other.getSubtype() != null) {
				return false;
			}
		} else if (other.getSubtype() == null) {
			return false;
		} else if (!subtype.toLowerCase(Locale.ROOT).equals(other.getSubtype().toLowerCase(Locale.ROOT))) {
			return false;
		}
		if (type == null) {
			if (other.getType() != null) {
				return false;
			}
		} else if (other.getType() == null) {
			return false;
		} else if (!type.toLowerCase(Locale.ROOT).equals(other.getType().toLowerCase(Locale.ROOT))) {
			return false;
		}
		return true;
	}

	@Override
	@Nonnull
	public Map<String, String> getParameters() {
		return Collections.EMPTY_MAP;
	}

	@Override
	public String getSubtype() {
		return subtype;
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public boolean isAnySubtype() {
		return ANY.equals(subtype);
	}

	@Override
	public boolean isAnyType() {
		return isBlank(type) || ANY.equals(type);
	}

	@Override
	public boolean isCompatible(MimeType other) {
		if (other == null) {
			return false;
		}
		if (
			(isBlank(type) || ANY.equals(type)) &&
			isBlank(subtype) ||
			(isBlank(other.getType()) || ANY.equals(other.getType())) &&
			isBlank(other.getSubtype())
		) {
			return true;
		} else if (isBlank(type) || (isBlank(other.getType()))) {
			return
				isBlank(subtype) ||
				isBlank(other.getSubtype()) ||
				ANY.equals(subtype) ||
				ANY.equals(other.getSubtype()) ||
				subtype.toLowerCase(Locale.ROOT).equals(other.getSubtype().toLowerCase(Locale.ROOT));
		} else if (
			type.toLowerCase(Locale.ROOT).equals(other.getType().toLowerCase(Locale.ROOT)) &&
			(
				isBlank(subtype) ||
				ANY.equals(subtype) ||
				isBlank(other.getSubtype()) ||
				ANY.equals(other.getSubtype())
			)
		) {
			return true;
		} else if (isBlank(subtype) || isBlank(other.getSubtype())) {
			return false;
		} else {
			return
				type.toLowerCase(Locale.ROOT).equals(other.getType().toLowerCase(Locale.ROOT)) &&
				subtype.toLowerCase(Locale.ROOT).equals(other.getSubtype().toLowerCase(Locale.ROOT));
		}
	}

	@Override
	public boolean isDRM() {
		return isDTCP();
	}

	@Override
	public boolean isDTCP() {
		return "application".equalsIgnoreCase(type) && "x-dtcp1".equalsIgnoreCase(subtype);
	}

	@Override
	public org.seamless.util.MimeType toSeamlessMimeType() {
		return new org.seamless.util.MimeType(type, subtype);
	}

	@Override
	public String toString() {
		return stringValue;
	}

	@Override
	public String toStringWithoutParameters() {
		return stringValue;
	}
}
