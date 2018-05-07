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
package net.pms.formats.image;

import net.pms.dlna.protocolinfo.MimeType;

/**
 * A representation of the Truevision Targa Graphic file format.
 *
 * @author Nadahar
 */
public class TGA extends ImageBase {

	@Override
	public Identifier getIdentifier() {
		return Identifier.TGA;
	}

	@Override
	public String[] getSupportedExtensions() {
		return new String[] {
			"tga",
			"icb",
			"vda",
			"vstrle"
		};
	}

	@Override
	public MimeType getStandardMimeType() {
		/*
		 * application/tga,
		 * application/x-tga,
		 * application/x-targa,
		 * image/tga,
		 * image/x-tga,
		 * image/targa,
		 * image/x-targa
		 */
		return MimeType.FACTORY.createMimeType("image","x-tga");
	}

}
