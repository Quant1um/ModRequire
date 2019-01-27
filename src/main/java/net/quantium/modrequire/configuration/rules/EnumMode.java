package net.quantium.modrequire.configuration.rules;

public enum EnumMode {
	REQUIRED("required", '!', true),
	OPTIONAL("optional", '+', true),
	BLACKLISTED("forbidden", '-', false);
	
	private final String desc;
	private final char prefix;
	private final boolean pass;
	
	private EnumMode(String desc, char prefix, boolean pass) {
		this.desc = desc;
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
		return this.desc;
	}
	
	public static EnumMode getByPrefix(char prefix) {
		for(EnumMode mode : EnumMode.values())
			if(mode.getPrefix() == prefix)
				return mode;
		return null;
	}
}
