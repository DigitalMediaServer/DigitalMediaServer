package net.pms.formats.audio;

import net.pms.formats.AudioAsVideo;

public class MLP extends AudioBase {

	@Override
	public Identifier getIdentifier() {
		return Identifier.MLP;
	}

	public MLP() {
		secondaryFormat = new AudioAsVideo();
	}

	@Override
	public String[] getSupportedExtensions() {
		return new String[] { "mlp" };
	}
}
