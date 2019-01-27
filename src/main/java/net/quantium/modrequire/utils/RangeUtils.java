package net.quantium.modrequire.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.BoundType;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

import net.minecraftforge.fml.common.versioning.ArtifactVersion;
import net.minecraftforge.fml.common.versioning.Restriction;
import net.minecraftforge.fml.common.versioning.VersionRange;

public final class RangeUtils {
	
	public static final VersionRange ANY = VersionRange.newRange(null, Lists.newArrayList(Restriction.EVERYTHING));

	public static <T extends Comparable> RangeSet<T> subtract(RangeSet<T> r1, RangeSet<T> r2) {
		RangeSet<T> clone = TreeRangeSet.create(r1);
		clone.removeAll(r2);
		return clone;
	}
	
	public static RangeSet<ArtifactVersion> fromRange(VersionRange range) {
		RangeSet<ArtifactVersion> set = TreeRangeSet.create();
		
		if(range.hasRestrictions()) {
			for(Restriction restriction : range.getRestrictions())
				set.add(fromRestriction(restriction));
		} else if(range.getRecommendedVersion() != null) {
			set.add(Range.singleton(range.getRecommendedVersion()));
		}

		return set;
	}
	
	public static Range<ArtifactVersion> fromRestriction(Restriction restriction) {
		BoundType upperBound = restriction.isUpperBoundInclusive() ? BoundType.CLOSED : BoundType.OPEN;
		BoundType lowerBound = restriction.isLowerBoundInclusive() ? BoundType.CLOSED : BoundType.OPEN;
		boolean isUpUnbounded = restriction.getUpperBound() == null;
		boolean isDownUnbounded = restriction.getLowerBound() == null;
		
		if(isUpUnbounded && isDownUnbounded) 
			return Range.all();
		else if(isUpUnbounded) 
			return Range.downTo(restriction.getLowerBound(), lowerBound);
		else if(isDownUnbounded) 
			return Range.upTo(restriction.getUpperBound(), upperBound);	
		else 
			return Range.range(restriction.getLowerBound(), lowerBound, 
					restriction.getUpperBound(), upperBound);
	}
}
