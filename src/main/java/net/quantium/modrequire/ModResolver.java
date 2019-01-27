package net.quantium.modrequire;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.google.common.collect.Streams;

import net.quantium.modrequire.ModResolver.Result;
import net.quantium.modrequire.configuration.ModInfo;
import net.quantium.modrequire.configuration.rules.RequirementRules;
import net.quantium.modrequire.configuration.rules.Rule;
import net.quantium.modrequire.configuration.rules.predicates.Predicate;

public final class ModResolver {

	public static Result resolve(Map<String, String> mods, RequirementRules rules) {
		return resolve(mods.entrySet()
				.stream()
				.map((entry) -> new ModInfo(entry.getKey(), entry.getValue()))
				.collect(Collectors.toList()), 
				rules);
	}
	
	public static Result resolve(Iterable<ModInfo> mods, RequirementRules rules) {
		List<Predicate> posRejected = new ArrayList<Predicate>();
		List<ModInfo> negRejected = new ArrayList<ModInfo>();
		boolean hasModRequire = false;
		
		for(ModInfo mod : mods) {
			if(mod.getModId().equals(ModProvider.MODID)) {
				hasModRequire = true;
			}
			
			if(!rules.getMode(mod).isPassable()) {
				negRejected.add(mod);
			}
		}
		
		for(Predicate predicate : rules.getRequired()) {
			if(!Streams.stream(mods).anyMatch((mod) -> predicate.matches(mod))) {
				posRejected.add(predicate);
			}
		}
		
		return new Result(posRejected, negRejected, hasModRequire);
	}
	
	public static class Result {

		private final boolean hasModRequire;
		private final List<Predicate> posRejected;
		private final List<ModInfo> negRejected;
		
		private Result(List<Predicate> posRejected, List<ModInfo> negRejected, boolean hasModRequire) {
			this.posRejected = posRejected;
			this.negRejected = negRejected;
			this.hasModRequire = hasModRequire;
		}
		
		public List<Predicate> getRequired() {
			return this.posRejected;
		}
		
		public List<ModInfo> getForbidden() {
			return this.negRejected;
		}
		
		public boolean hasRejections() {
			return !this.posRejected.isEmpty() || !this.negRejected.isEmpty();
		}
		
		public boolean hasModRequire() {
			return this.hasModRequire;
		}
	}
}
