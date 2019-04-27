package net.quantium.modrequire.configuration.rules.predicates;

import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

import net.minecraftforge.fml.common.versioning.ArtifactVersion;
import net.minecraftforge.fml.common.versioning.VersionRange;
import net.quantium.modrequire.configuration.ModInfo;
import net.quantium.modrequire.utils.RangeUtils;

public class PredicateBasic extends Predicate {
	
	private final String modId;
	private final RangeSet<ArtifactVersion> acceptedVersions;
	
	public PredicateBasic(String modId, VersionRange range) {
		this(modId, RangeUtils.fromRange(range));
	}
	
	public PredicateBasic(String modId, RangeSet<ArtifactVersion> range) {
		if(range.isEmpty()) {
			throw new IllegalArgumentException("Range must not be empty!");
		}
		
		this.modId = modId.toLowerCase();
		this.acceptedVersions = range;
	}
	
	@Override
	public boolean matches(ModInfo mod) {
		return mod.getModId().equalsIgnoreCase(this.modId) &&
				this.acceptedVersions.contains(mod.getVersion());
	}

	@Override
	public String toReadableString() {
		return this.modId + " " + this.acceptedVersions.toString();
	}

	@Override
	public Predicate subtract(Predicate subtrahend) {
		if(subtrahend == EMPTY) return this;
		if(subtrahend == ANY) return EMPTY;
		
		PredicateBasic pb = (PredicateBasic) subtrahend;
		
		if(this.modId.equalsIgnoreCase(pb.modId)) {
			RangeSet<ArtifactVersion> range = RangeUtils.subtract(this.acceptedVersions, pb.acceptedVersions);
			if(range.isEmpty())
				return EMPTY;
			return new PredicateBasic(this.modId, range);
		}
		
		return this;
	}
}
