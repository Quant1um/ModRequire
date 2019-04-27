package net.quantium.modrequire.configuration.players;

import net.minecraft.entity.player.EntityPlayerMP;

public class PlayerRule {
	private final EnumMode mode;
	private final PlayerSelector selector;
	
	public PlayerRule(EnumMode mode, PlayerSelector selector) {
		this.mode = mode;
		this.selector = selector;
	}

	public EnumMode getMode() {
		return this.mode;
	}

	public PlayerSelector getSelector() {
		return this.selector;
	}
	
	public boolean test(EntityPlayerMP mod) {
		return this.selector.test(mod);
	}

	public String toReadableString() {
		return String.format("%s %s", this.mode.getDescription(), this.selector.toReadableString());
	}
}
