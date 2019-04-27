package net.quantium.modrequire.configuration.players;

public enum EnumMode {
	CHECK("check", '+', false),
	SKIP("skip", '-', true);
	
	private final String description;
	private final char prefix;
	private final boolean pass;
	
	private EnumMode(String description, char prefix, boolean pass) {
		this.description = description;
		this.prefix = prefix;
		this.pass = pass;
	}
	
	public boolean isPassable() {
		return this.pass;
	}
	
	public char getPrefix() {
		return this.prefix;
	}
	
	public String getDescription() {
		return this.description;
	}
	
	public static EnumMode fromPrefix(char prefix) {
		for(EnumMode mode : EnumMode.values())
			if(mode.getPrefix() == prefix)
				return mode;
		return null;
	}
}
