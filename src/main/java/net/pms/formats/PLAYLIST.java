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
package net.pms.formats;

import net.pms.configuration.RendererConfiguration;
import net.pms.dlna.DLNAMediaInfo;
import net.pms.dlna.protocolinfo.MimeType;
import net.pms.dlna.protocolinfo.KnownMimeTypes;

public class PLAYLIST extends Format {

	@Override
	public Identifier getIdentifier() {
		return Identifier.PLAYLIST;
	}

	@Override
	public boolean transcodable() {
		return false;
	}

	public PLAYLIST() {
		type = PLAYLIST;
	}

	@Override
	public boolean isCompatible(DLNAMediaInfo media, RendererConfiguration renderer) {
		// TODO: manage via renderer conf setting
		return false;
	}

	@Override
	public String[] getSupportedExtensions() {
		return new String[] {
			"pls",
			"m3u",
			"m3u8",
			"cue",
			"ups"
		};
	}

	@Override
	public MimeType mimeType() {
		return KnownMimeTypes.M3U;
	}
}
