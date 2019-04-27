package net.quantium.modrequire.configuration.rules;

public class ModRule {
	private final EnumAction action;
	private final ModSelector selector;
	
	public ModRule(EnumAction action, ModSelector selector) throws InvalidRuleException {
		if(action == EnumAction.REQUIRE && selector == ModSelector.ANY) {
			throw new InvalidRuleException("Rule 'required any' doesn't make sense");
		}
		
		this.action = action;
		this.selector = selector;
	}

	public EnumAction getAction() {
		return this.action;
	}

	public ModSelector getSelector() {
		return this.selector;
	}
	
	public boolean test(ModInfo mod) {
		return this.selector.test(mod);
	}

	public String toReadableString() {
		return String.format("%s %s", this.action.getDescription(), this.selector.toReadableString());
	}
	
	public static class InvalidRuleException extends Exception {
		public InvalidRuleException(String string) {
			super(string);
		}
	}
}
