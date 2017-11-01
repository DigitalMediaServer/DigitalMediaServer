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
package net.pms.formats.audio;

import net.pms.dlna.protocolinfo.MimeType;
import net.pms.dlna.protocolinfo.KnownMimeTypes;

public class MP3 extends AudioBase {

	@Override
	public Identifier getIdentifier() {
		return Identifier.MP3;
	}

	@Override
	public String[] getSupportedExtensions() {
		return new String[] { "mp3" };
	}

	@Override
	public boolean transcodable() {
		return false;
	}

	@Override
	public MimeType mimeType() {
		return KnownMimeTypes.MP3;
	}
}
