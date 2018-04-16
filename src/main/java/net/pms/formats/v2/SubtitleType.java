/*
 * PS3 Media Server, for streaming any medias to your PS3.
 * Copyright (C) 2012  I. Sokolov
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
package net.pms.formats.v2;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Enum with possible types of subtitle tracks and methods for determining them
 * by file extension or libmediainfo output.
 */
public enum SubtitleType { //TODO: (Nad) Figure out how to handle untranscodable subtitles
	// MediaInfo database of codec signatures https://github.com/MediaArea/MediaInfoLib/blob/master/Source/Resource/Text/DataBase/Codec.csv

	//     int index, String description,        String shortName, List<String> fileExtensions, List<String> libMediaInfoCodecs,                         Category category
	UNKNOWN     (0,  "Unknown",                     "Unknown",     list(),             list("Unknown"),                                                  SubtitleCategory.UNDEFINED),
	SUBRIP      (1,  "SubRip",                      "SubRip",      list("srt"),        list("S_TEXT/UTF8", "S_UTF8", "UTF-8", "Subrip"),                 SubtitleCategory.TEXT),
	TEXT        (2,  "Text",                        "Text",        list("txt"),        list(),                                                           SubtitleCategory.TEXT),
	MICRODVD    (3,  "MicroDVD",                    "MicroDVD",    list("sub"),        list(),                                                           SubtitleCategory.TEXT),
	SAMI        (4,  "SAMI",                        "SAMI",        list("smi"),        list(),                                                           SubtitleCategory.TEXT),
	ASS         (5,  "(Advanced) SubStation Alpha", "ASS/SSA",     list("ass", "ssa"), list("S_TEXT/SSA", "S_TEXT/ASS", "S_SSA", "S_ASS", "SSA", "ASS"), SubtitleCategory.TEXT),
	VOBSUB      (6,  "VobSub",                      "VobSub",      list("idx"),        list("S_VOBSUB", "subp", "mp4s", "E0", "RLE"),                    SubtitleCategory.PICTURE), // "RLE" may also apply to other formats
	UNSUPPORTED (7,  "Unsupported",                 "Unsupported", list(),             list(),                                                           SubtitleCategory.UNDEFINED),
	USF         (8,  "Universal Subtitle Format",   "USF",         list(),             list("S_TEXT/USF", "S_USF", "USF"),                               SubtitleCategory.TEXT),
	BMP         (9,  "Bitmap",                      "BMP",         list(),             list("S_IMAGE/BMP"),                                              SubtitleCategory.PICTURE),
	DIVX        (10, "DIVX subtitles",              "XSUB",        list(),             list("DXSB"),                                                     SubtitleCategory.PICTURE),
	TX3G        (11, "Timed text (TX3G)",           "TX3G",        list(),             list("Timed Text", "tx3g"),                                       SubtitleCategory.TEXT),
	PGS         (12, "Blu-ray subtitles",           "PGS",         list("sup", "pgs"), list("S_HDMV/PGS", "PGS", "144"),                                 SubtitleCategory.PICTURE),
	WEBVTT      (13, "WebVTT",                      "WebVTT",      list("vtt"),        list("WebVTT", "S_TEXT/WEBVTT"),                                  SubtitleCategory.TEXT),
	TEXTST      (14, "HDMV Text",                   "HDMV",        list(),             list("S_HDMV/TEXTST"),                                            SubtitleCategory.TEXT),
	DVBSUB      (15, "DVB Subtitles",               "DVB",         list(),             list("S_DVBSUB", "DVB Subtitle", "6"),                            SubtitleCategory.PICTURE),
	EIA608      (16, "EIA-608",                     "EIA-608",     list(),             list("EIA-608", "c608"),                                          SubtitleCategory.TEXT),
	EIA708      (17, "EIA-708",                     "EIA-708",     list(),             list("EIA-708", "c708"),                                          SubtitleCategory.TEXT),
	KATE        (18, "Kate",                        "Kate",        list(),             list("Kate"),                                                     SubtitleCategory.TEXT),
	TELETEXT    (19, "Teletext subtitles",          "Teletext",    list(),             list("Teletext", "Teletext Subtitle"),                            SubtitleCategory.TEXT);

	private final int index;

	@Nonnull
	private final String description;

	@Nonnull
	private final String shortName;

	@Nonnull
	private final List<String> fileExtensions;

	@Nonnull
	private final List<String> libMediaInfoCodecs;
	private final SubtitleCategory category;

	private static final Map<Integer, SubtitleType> stableIndexToSubtitleTypeMap;
	private static final Map<String, SubtitleType> fileExtensionToSubtitleTypeMap;
	private static final Map<String, SubtitleType> libmediainfoCodecToSubtitleTypeMap;

	/**
	 * A constant {@link Set} of lower-case file extensions for supported
	 * subtitles types
	 */
	public final static Set<String> SUPPORTED_FILE_EXTENSIONS;
	private static List<String> list(String... args) {
		return Collections.unmodifiableList(Arrays.asList(args));
	}

	static {
		Map<Integer, SubtitleType> tempStableIndexToSubtitleTypeMap = new HashMap<>();
		Map<String, SubtitleType> tempFileExtensionToSubtitleTypeMap = new HashMap<>();
		Map<String, SubtitleType> tempLibmediainfoCodecToSubtitleTypeMap = new HashMap<>();
		for (SubtitleType subtitleType : values()) {
			tempStableIndexToSubtitleTypeMap.put(subtitleType.getStableIndex(), subtitleType);
			for (String fileExtension : subtitleType.fileExtensions) {
				tempFileExtensionToSubtitleTypeMap.put(fileExtension.toLowerCase(Locale.ROOT), subtitleType);
			}
			for (String codec : subtitleType.libMediaInfoCodecs) {
				tempLibmediainfoCodecToSubtitleTypeMap.put(codec.toLowerCase(Locale.ROOT), subtitleType);
			}
		}
		stableIndexToSubtitleTypeMap = Collections.unmodifiableMap(tempStableIndexToSubtitleTypeMap);
		fileExtensionToSubtitleTypeMap = Collections.unmodifiableMap(tempFileExtensionToSubtitleTypeMap);
		libmediainfoCodecToSubtitleTypeMap = Collections.unmodifiableMap(tempLibmediainfoCodecToSubtitleTypeMap);

		SUPPORTED_FILE_EXTENSIONS = Collections.unmodifiableSet(
			new LinkedHashSet<>(fileExtensionToSubtitleTypeMap.keySet())
		);
	}

	@Nonnull
	public static SubtitleType valueOfStableIndex(int stableIndex) {
		SubtitleType subtitleType = stableIndexToSubtitleTypeMap.get(stableIndex);
		return subtitleType == null ? UNKNOWN : subtitleType;
	}

	@Nonnull
	public static SubtitleType valueOfFileExtension(String fileExtension) {
		if (isBlank(fileExtension)) {
			return UNKNOWN;
		}
		SubtitleType subtitleType = fileExtensionToSubtitleTypeMap.get(fileExtension.trim().toLowerCase(Locale.ROOT));
		return subtitleType == null ? UNKNOWN : subtitleType;
	}

	@Nonnull
	public static SubtitleType valueOfMediaInfoValue(String value) {
		return valueOfMediaInfoValue(value, UNKNOWN);
	}

	public static SubtitleType valueOfMediaInfoValue(String value, SubtitleType defaultType) {
		if (isBlank(value)) {
			return defaultType;
		}
		SubtitleType subtitleType = libmediainfoCodecToSubtitleTypeMap.get(value.trim().toLowerCase(Locale.ROOT));
		return subtitleType == null ? defaultType : subtitleType;
	}

	@Nonnull
	public static Set<String> getSupportedFileExtensions() {
		return SUPPORTED_FILE_EXTENSIONS;
	}

	/**
	 * Creates a new instance with the specified parameters.
	 *
	 * @param index the subtitle type index.
	 * @param description the full description.
	 * @param shortName the abbreviated description.
	 * @param fileExtensions a {@link List} of possible file extensions.
	 * @param libMediaInfoCodecs a {@link List} of MediaInfo codec names.
	 * @param category the {@link SubtitleCategory} for the subtitle type.
	 */
	private SubtitleType(
		int index,
		@Nonnull String description,
		@Nonnull String shortName,
		@Nonnull List<String> fileExtensions,
		@Nonnull List<String> libMediaInfoCodecs,
		@Nonnull SubtitleCategory category
	) {
		this.index = index;
		this.description = description;
		this.shortName = shortName;
		this.fileExtensions = fileExtensions;
		this.libMediaInfoCodecs = libMediaInfoCodecs;
		this.category = category;
	}

	@Nonnull
	public String getDescription() {
		return description;
	}

	@Nonnull
	public String getShortName() {
		return shortName;
	}

	@Nonnull
	public String getExtension() {
		if (fileExtensions.isEmpty()) {
			return "";
		}
		return fileExtensions.get(0);
	}

	@Nonnull
	public List<String> getExtensions() {
		return fileExtensions;
	}

	public int getStableIndex() {
		return index;
	}

	public boolean isText() {
		return category == SubtitleCategory.TEXT;
	}

	public boolean isPicture() {
		return category == SubtitleCategory.PICTURE;
	}

	@Nonnull
	public SubtitleCategory getCategory() {
		return category;
	}

	/**
	 * An {@code enum} describing the basic type a {@link SubtitleType} is.
	 */
	public static enum SubtitleCategory {

		/** Text based subtitles */
		TEXT,

		/** Bitmap/image based subtitles */
		PICTURE,

		/** Undefined type of subtitles */
		UNDEFINED
	}
}
