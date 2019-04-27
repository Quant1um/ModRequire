package net.quantium.modrequire.configuration.rules;

import java.util.List;

public class Resolution {
	private final boolean hasThisMod;
	private final List<ModSelector> posRejected;
	private final List<ModInfo> negRejected;
	
	public Resolution(List<ModSelector> posRejected, List<ModInfo> negRejected, boolean thisMod) {
		this.posRejected = posRejected;
		this.negRejected = negRejected;
		this.hasThisMod = thisMod;
	}
	
	public List<ModSelector> getRequired() {
		return this.posRejected;
	}
	
	public List<ModInfo> getForbidden() {
		return this.negRejected;
	}
	
	public boolean hasRejections() {
		return !this.posRejected.isEmpty() || !this.negRejected.isEmpty();
	}
	
	public boolean hasThisMod() {
		return this.hasThisMod;
	}
}
