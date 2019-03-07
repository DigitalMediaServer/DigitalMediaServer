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
package net.pms.media;

import static org.apache.commons.lang3.StringUtils.isBlank;
import java.util.Locale;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.pms.configuration.FormatConfiguration;
import net.pms.dlna.MediaType;


/**
 * This {@code enum} represents video codecs.
 *
 * @author Nadahar
 */
public enum VideoCodec implements Codec {

	/** AOMedia Video 1 */
	AV1("AV1"),

	/** Cinepak */
	CINEPAK("Cinepak"),

	/**
	 * DivX (MPEG-4 Part 2), DivX Plus HD (H.264/MPEG-4 AVC) or DivX HEVC Ultra
	 * HD
	 */
	DIVX("DivX"),

	/** Digital Video */
	DV("Digital Video"),

	/** FF video codec 1 */
	FFV1("FFV1", true),

	/** H.261 */
	H261("H.261"),

	/** H.262/MPEG-2 Part 2 */
	H262("H.262"),

	/** H.263 */
	H263("H.263"),

	/** MPEG-4 Part 10, Advanced Video Coding (MPEG-4 AVC) */
	H264("H.264"),

	/** MPEG-H Part 2, High Efficiency Video Coding */
	H265("H.265"),

	/** Motion JPEG */
	MJPEG("M-JPEG"),

	/** A series of Motion JPEG 2000 images in some containers, for example AVI */
	MJP2("M-JPEG 2000"),

	/** MPEG-4 Part 2 */
	// XXX: Figure out how to deal with MPEG-4 and profiles
	MPEG4("MPEG-4"),

	/** MPEG-4 Part 2, MPEG-4 Visual Advanced Simple Profile (ASP) */
	MPEG4ASP("MPEG-4 ASP"),

	/** MPEG-4 Part 2, MPEG-4 Visual Simple Profile (SP) */
	MPEG4SP("MPEG-4 SP"),

	/**
	 * A series of Portable Network Graphics images in some containers, for
	 * example AVI
	 */
	PNG("PNG"),

	/** Apple ProRes */
	PRORES("ProRes"),

	/** A series of uncompressed RGB bitmaps in some containers, for example AVI */
	RGB("RGB"),

	/** Real Video */
	RV("RealVideo"),

	/**
	 * A series of SoftImage DS Uncompressed (PIC) images in some containers,
	 * for example AVI.
	 * <p>
	 * See image format reference <a href=
	 * "https://web.archive.org/web/20190211232700/http://softimage.wiki.softimage.com/index70ed.html?title=PIC_file_format#Channel_Info_Section"
	 * >here</a>.
	 */
	SUDS("SUDS"),

	/** Sorenson Video */
	SVQ1("Sorenson Video"),

	/** Sorenson Video 3 */
	SVQ3("Sorenson Video 3"),

	/** Sorenson Spark */
	FLV1("Sorenson Spark"),

	/** Indeo Video (Intel) */
	INDEO("INDEO"),

	/**
	 * A series of Run-Length Encoded bitmaps in some containers, for example
	 * AVI
	 */
	RLE("RLE"),

	/** A series of Truevision Targa images in some containers, for example AVI */
	TGA("TGA"),

	/** Theora */
	THEORA("Theora"),

	/** SMPTE 421M */
	VC1("VC-1"),

	/** SMPTE VC-3 */
	VC3("VC-3"),

	/** On2 TrueMotion VP6 */
	VP6("VP6"),

	/** On2 TrueMotion VP7 */
	VP7("VP7"),

	/** On2 TrueMotion VP8 */
	VP8("VP8"),

	/** VP9 */
	VP9("VP9"),

	/** Windows Media Video */
	WMV("Windows Media Video"),

	/**
	 * Used as a "video codec" when sequences of raw, uncompressed YUV is used
	 * as a video stream in AVI, MP4 or MOV files
	 */
	YUV("YUV");

	private final String codecName;
	private final boolean lossless;

	private VideoCodec(String name) {
		this.codecName = name;
		this.lossless = false;
	}

	private VideoCodec(String name, boolean lossless) {
		this.codecName = name;
		this.lossless = lossless;
	}

	@Override
	@Nonnull
	public String getCodecName() {
		return codecName;
	}

	@Override
	@Nonnull
	public MediaType getCodecType() {
		return MediaType.VIDEO;
	}

	@Override
	public boolean isLossless() {
		return lossless;
	}

	@Override
	public String toString() {
		return codecName;
	}

	@Nullable
	public String getLevelLimitKey() {
		StringBuilder sb;
		switch (this) {
			case MPEG4ASP:
			case MPEG4SP:
				sb = new StringBuilder().append("MPEG4-Visual");
				break;
			case AV1:
			case H262:
			case H263:
			case H264:
			case H265:
			case VC1:
			case VP9:
				sb = new StringBuilder().append(name());
				break;
			default:
				return null;
		}

		sb.append("LevelLimit");
		return sb.toString();
	}

	/**
	 * Tries to parse the specified string to a {@link VideoCodec}.
	 *
	 * @param value the {@link String} to parse.
	 * @return The corresponding {@link VideoCodec} or {@code null} if the
	 *         parsing failed.
	 */
	@Nullable
	public static VideoCodec typeOf(@Nullable String value) {
		if (isBlank(value)) {
			return null;
		}

		String valueLower = value.toLowerCase(Locale.ROOT);
		switch (valueLower) {
			case FormatConfiguration.CINEPAK:
				return CINEPAK;
			case "mpeg1":
				return H261;
			case "mpeg2":
				return H262;
			case FormatConfiguration.SORENSON:
				return SVQ1;
			default:
				String valueUpper = value.toUpperCase(Locale.ROOT);
				for (VideoCodec videoCodec : values()) {
					if (
						valueUpper.equals(videoCodec.name()) ||
						valueUpper.equals(videoCodec.codecName.toUpperCase(Locale.ROOT))
					) {
						return videoCodec;
					}
				}
				return null;
		}
	}
}
