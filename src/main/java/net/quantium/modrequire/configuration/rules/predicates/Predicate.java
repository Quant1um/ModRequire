package net.quantium.modrequire.configuration.rules.predicates;

import java.util.Collections;
import java.util.stream.Collectors;

import org.apache.commons.lang3.NotImplementedException;

import com.google.common.collect.TreeRangeSet;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.versioning.VersionRange;
import net.quantium.modrequire.configuration.ModInfo;
import net.quantium.modrequire.utils.RangeUtils;

public abstract class Predicate {
	public static final Predicate ANY = new Predicate() {
		@Override
		public boolean matches(ModInfo mod) {
			return true;
		}

		@Override
		public String toReadableString() {
			return "any";
		}

		@Override
		public Predicate subtract(Predicate subtrahend) {
			throw new NotImplementedException("Subtraction from ANY is not neccessary, hence not implemented");
		}
	};
	
	public static final Predicate EMPTY = new Predicate() {
		@Override
		public boolean matches(ModInfo mod) {
			return false;
		}

		@Override
		public String toReadableString() {
			return "empty";
		}

		@Override
		public Predicate subtract(Predicate subtrahend) {
			return EMPTY;
		}
	};
	
	public abstract boolean matches(ModInfo mod);
	public abstract Predicate subtract(Predicate subtrahend);
	public abstract String toReadableString();
	
	public static Iterable<Predicate> getLoadedMods() {
		return NetworkRegistry.INSTANCE
				.registry()
				.entrySet()
				.stream()
				.map((entry) -> entry.getKey())
				.map((mod) -> new PredicateBasic(mod.getModId(), VersionRange.newRange(mod.getProcessedVersion(), Collections.EMPTY_LIST)))
				.collect(Collectors.toList());
	}
	
	public static Iterable<Predicate> getLoadedModsAnyVersion() {
		return NetworkRegistry.INSTANCE
				.registry()
				.entrySet()
				.stream()
				.map((entry) -> entry.getKey())
				.map((mod) -> new PredicateBasic(mod.getModId(), RangeUtils.ANY))
				.collect(Collectors.toList());
	}
}
