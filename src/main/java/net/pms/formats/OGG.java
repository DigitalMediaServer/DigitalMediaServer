package net.pms.formats;

import net.pms.dlna.protocolinfo.MimeType;
import net.pms.dlna.protocolinfo.KnownMimeTypes;

public class OGG extends Format {

	@Override
	public Identifier getIdentifier() {
		return Identifier.OGG;
	}

	@Override
	public boolean transcodable() {
		return true;
	}

	public OGG() {
		type = VIDEO;
	}

	@Override
	public String[] getSupportedExtensions() {
		return new String[] {
			"ogg",
			"ogm",
			"ogv"
		};
	}

	@Override
	public MimeType mimeType() {
		return KnownMimeTypes.OGG;
	}
}
