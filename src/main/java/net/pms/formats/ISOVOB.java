package net.pms.formats;


public class ISOVOB extends MPG {

	@Override
	public Identifier getIdentifier() {
		return Identifier.ISOVOB;
	}

	@Override
	public String[] getSupportedExtensions() {
		return new String[0];
	}
}
