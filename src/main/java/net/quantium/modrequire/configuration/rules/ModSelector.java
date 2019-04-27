package net.quantium.modrequire.configuration.rules;

import java.util.Collections;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.NotImplementedException;

import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.versioning.ArtifactVersion;
import net.minecraftforge.fml.common.versioning.VersionRange;
import net.quantium.modrequire.ModProvider;
import net.quantium.modrequire.utils.RangeUtils;

public abstract class ModSelector implements Predicate<ModInfo> {
	public abstract ModSelector subtract(ModSelector subtrahend);
	public abstract String toReadableString();

	public static final ModSelector ANY = new Any();
	
	public static final ModSelector NONE = new None();
	
	public static ModSelector specificMod(String id) {
		return specificMod(id, RangeUtils.ANY);
	}
	
	public static ModSelector specificMod(String id, VersionRange range) {
		return specificMod(id, RangeUtils.fromRange(range));
	}
	
	public static ModSelector specificMod(String id, RangeSet<ArtifactVersion> range) {
		return new SpecificMod(id, range);
	}
	
	public static Iterable<ModSelector> loadedMods() {
		return NetworkRegistry.INSTANCE
				.registry()
				.entrySet()
				.stream()
				.map((entry) -> entry.getKey())
				.filter((mod) -> !mod.getModId().equals(ModProvider.MODID))
				.map((mod) -> specificMod(mod.getModId(), VersionRange.newRange(mod.getProcessedVersion(), Collections.EMPTY_LIST)))
				.collect(Collectors.toList());
	}
	
	public static Iterable<ModSelector> loadedModsAnyVersion() {
		return NetworkRegistry.INSTANCE
				.registry()
				.entrySet()
				.stream()
				.map((entry) -> entry.getKey())
				.filter((mod) -> !mod.getModId().equals(ModProvider.MODID))
				.map((mod) -> specificMod(mod.getModId()))
				.collect(Collectors.toList());
	}
	
	private static class SpecificMod extends ModSelector {	
		private final String modId;
		private final RangeSet<ArtifactVersion> acceptedVersions;
		
		public SpecificMod(String modId, VersionRange range) {
			this(modId, RangeUtils.fromRange(range));
		}
		
		public SpecificMod(String modId, RangeSet<ArtifactVersion> range) {
			if(range.isEmpty()) {
				throw new IllegalArgumentException("Range must not be empty!");
			}
			
			this.modId = modId.toLowerCase();
			this.acceptedVersions = range;
		}
		
		@Override
		public boolean test(ModInfo mod) {
			return mod.getModId().equalsIgnoreCase(this.modId) &&
					this.acceptedVersions.contains(mod.getVersion());
		}

		@Override
		public String toReadableString() {
			return String.format("%s %s", this.modId, this.acceptedVersions.toString());
		}

		@Override
		public ModSelector subtract(ModSelector subtrahend) {
			if(subtrahend == NONE) return this;
			if(subtrahend == ANY) return NONE;
			
			SpecificMod pb = (SpecificMod) subtrahend;
			
			if(this.modId.equalsIgnoreCase(pb.modId)) {
				RangeSet<ArtifactVersion> range = RangeUtils.subtract(this.acceptedVersions, pb.acceptedVersions);
				if(range.isEmpty())
					return NONE;
				return new SpecificMod(this.modId, range);
			}
			
			return this;
		}
	}

	private static class Any extends ModSelector {
		@Override
		public boolean test(ModInfo mod) {
			return true;
		}

		@Override
		public String toReadableString() {
			return "any mod";
		}

		@Override
		public ModSelector subtract(ModSelector subtrahend) {
			throw new NotImplementedException("Subtraction from ANY is not neccessary, hence not implemented");
		}
	}

	private static class None extends ModSelector {
		@Override
		public boolean test(ModInfo mod) {
			return false;
		}

		@Override
		public String toReadableString() {
			return "no mod";
		}

		@Override
		public ModSelector subtract(ModSelector subtrahend) {
			return NONE;
		}
	}
}
