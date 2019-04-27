package net.quantium.modrequire.configuration.rules;

public enum EnumAction {
	REQUIRE("require", '!', true),
	ALLOW("allow", '+', true),
	FORBID("forbid", '-', false);
	
	private final String description;
	private final char prefix;
	private final boolean pass;
	
	private EnumAction(String description, char prefix, boolean pass) {
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
	
	public static EnumAction fromPrefix(char prefix) {
		for(EnumAction mode : EnumAction.values())
			if(mode.getPrefix() == prefix)
				return mode;
		return null;
	}
}
