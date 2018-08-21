/*
 * Digital Media Server, for streaming digital media to UPnP AV or DLNA
 * compatible devices based on PS3 Media Server and Universal Media Server.
 * Copyright (C) 2018 Digital Media Server developers.
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
package net.pms.dlna;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import java.io.File;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;


/**
 * This class represents entries in a cue sheet.
 *
 * @author Nadahar
 */
public class CueSheetEntry extends RealFile implements PartialSource {

	private final double clipStart;
	private final double clipEnd;

	public CueSheetEntry(@Nonnull File file, double clipStart, double clipEnd) {
		super(file);
		this.clipStart = clipStart;
		this.clipEnd = clipEnd;
	}

	public CueSheetEntry(@Nonnull File file, @Nullable String name, double clipStart, double clipEnd) {
		super(file, name);
		this.clipStart = clipStart;
		this.clipEnd = clipEnd;
	}

	@Override
	public double getClipStart() {
		return clipStart;
	}

	@Override
	public double getClipEnd() {
		return clipEnd;
	}

	@Override
	public boolean isPartialSource() {
		return clipStart > 0.0 || clipEnd != 0.0 && !Double.isInfinite(clipEnd);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void checkThumbnail() {
		if (media == null || media.isThumbready()) {
			return;
		}

		if (parent instanceof CueFolder) {
			CueFolder cueFolder = (CueFolder) parent;
			DLNAThumbnail thumbnail = cueFolder.getThumbnail();
			if (thumbnail != null && thumbnail.getBytes(false) != null) {
				media.setThumb(thumbnail);
				media.setThumbready(true);
				return;
			}
		}

		super.checkThumbnail();
	}

	@Override
	public String write() {
		StringBuilder sb = new StringBuilder()
			.append(getConf().getFiles().get(0)).append(">")
			.append(clipStart).append(">")
			.append(clipEnd);
		if (isNotBlank(getConf().getName())) {
			sb.append(">").append(getConf().getName());
		}
		return sb.toString();
	}

	@Nullable
	public static CueSheetEntry read(@Nullable String data) {
		if (data == null || data.indexOf('>') == -1) {
			return null;
		}
		String[] parameters = data.split(">");
		switch (parameters.length) {
			case 3:
				return new CueSheetEntry(
					new File(parameters[0]),
					Double.parseDouble(parameters[1]),
					Double.parseDouble(parameters[2])
				);
			case 4:
				return new CueSheetEntry(
					new File(parameters[0]),
					parameters[3],
					Double.parseDouble(parameters[1]),
					Double.parseDouble(parameters[2])
				);
			default:
				return null;
		}
	}
}
