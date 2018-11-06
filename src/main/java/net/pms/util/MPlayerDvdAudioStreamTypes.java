/*
 * Digital Media Server, for streaming digital media to UPnP AV or DLNA
 * compatible devices based on PS3 Media Server and Universal Media Server.
 * Copyright (C) 2016 Digital Media Server developers.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see http://www.gnu.org/licenses/.
 */
package net.pms.util;

import static org.apache.commons.lang3.StringUtils.isBlank;
import net.pms.configuration.FormatConfiguration;


/**
 * This {@code enum} represents MPlayer's
 * {@code stream/stream_dvd_common.c dvd_audio_stream_types} array.
 *
 * @author Nadahar
 */
public enum MPlayerDvdAudioStreamTypes {

	/** AC-3 */
	AC3("ac3", FormatConfiguration.AC3),

	/** Unknown audio stream type */
	UNKNOWN("unknown", FormatConfiguration.UND),

	/** MPEG Audio */
	MPEG1("mpeg1", FormatConfiguration.MP2),

	/** MP2 */
	MPEG2EXT("mpeg2ext", FormatConfiguration.MP2),

	/** Linear PCM */
	LPCM("lpcm", FormatConfiguration.LPCM),

	/** DTS */
	DTS("dts", FormatConfiguration.DTS);

	/** DVD MPEG audio index lower bound */
	public static final int FIRST_MPEG_AID = 0;

	/** DVD AC-3 audio index lower bound */
	public static final int FIRST_AC3_AID = 128;

	/** DVD DTS audio index lower bound */
	public static final int FIRST_DTS_AID = 136;

	/** DVD LPCM audio index lower bound */
	public static final int FIRST_LPCM_AID = 160;

	private final String mPlayerCode;
	private final String formatConfigurationCode;

	private MPlayerDvdAudioStreamTypes(String mPlayerCode, String formatConfigurationCode) {
		this.mPlayerCode = mPlayerCode;
		this.formatConfigurationCode = formatConfigurationCode;
	}


	/**
	 * @return The MPlayer code for this DVD audio stream.
	 */
	public String getMPlayerCode() {
		return mPlayerCode;
	}


	/**
	 * @return The {@link FormatConfiguration} constant/codec for this DVD audio
	 *         stream type.
	 */
	public String getFormatConfigurationCode() {
		return formatConfigurationCode;
	}

	/**
	 * Returns the {@link MPlayerDvdAudioStreamTypes} type corresponding to the
	 * given MPlayer audio stream type output.
	 *
	 * @param mPlayerCode the MPlayer audio stream type string.
	 * @return The corresponding {@link MPlayerDvdAudioStreamTypes} or
	 *         {@code null}.
	 */
	public static MPlayerDvdAudioStreamTypes typeOf(String mPlayerCode) {
		if (isBlank(mPlayerCode)) {
			return null;
		}

		for (MPlayerDvdAudioStreamTypes type : MPlayerDvdAudioStreamTypes.values()) {
			if (mPlayerCode.equals(type.mPlayerCode)) {
				return type;
			}
		}
		return null;
	}

}
