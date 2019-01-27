package net.quantium.modrequire.configuration.rules.predicates;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.RangeSet;

import net.minecraftforge.fml.common.versioning.ArtifactVersion;
import net.minecraftforge.fml.common.versioning.InvalidVersionSpecificationException;
import net.minecraftforge.fml.common.versioning.Restriction;
import net.minecraftforge.fml.common.versioning.VersionRange;
import net.quantium.modrequire.utils.RangeUtils;

public final class PredicateParser {
	
	public static Iterable<Predicate> parse(String string) throws PredicateParserException {
		string = StringUtils.trim(string);
		
		for(EnumToken potentialToken : EnumToken.values()) {
			try{
				Iterable<Predicate> parsed = potentialToken.parse(string);
				if(parsed != null)
					return parsed;
			}catch(Exception e) {
				throw new PredicateParserException("Error occured while parsing token: " + string, e);
			}
		}
		throw new PredicateParserException("Undefined token: " + string);
	}
	
	public static class PredicateParserException extends Exception {
		public PredicateParserException(String string) {
			super(string);
		}
		
		public PredicateParserException(String string, Exception e) {
			super(string, e);
		}
	}
	
	private static enum EnumToken {
		ANY {
			@Override
			public Iterable<Predicate> parse(String str) {
				if(!str.equals("*"))
					return null;
				return Lists.newArrayList(Predicate.ANY);
			}
		},
		
		GROUP_LOADED {
			@Override
			public Iterable<Predicate> parse(String str) {
				if(!str.equalsIgnoreCase("*loaded"))
					return null;
				return Predicate.getLoadedMods();
			}
		},
		
		GROUP_LOADED_ANY {
			@Override
			public Iterable<Predicate> parse(String str) {
				if(!str.equalsIgnoreCase("*loaded-any"))
					return null;
				return Predicate.getLoadedModsAnyVersion();
			}
		},
		
		DEFAULT {
			private final Pattern PATTERN = Pattern.compile("^([a-z0-9]{1,64})(@.+)?$");

			@Override
			public Iterable<Predicate> parse(String str) throws InvalidVersionSpecificationException {
				Matcher matcher = PATTERN.matcher(str);
				
				if(!matcher.matches())
					return null;
				
				String modId = matcher.group(1);
				VersionRange versionRange = parseVersion(matcher.group(2));
				RangeSet<ArtifactVersion> range = RangeUtils.fromRange(versionRange);
				
				if(range.isEmpty())
					return Lists.newArrayList(Predicate.EMPTY);
				return Lists.newArrayList(new PredicateBasic(modId, range));
			}
			
			private VersionRange parseVersion(String str) throws InvalidVersionSpecificationException {
				return str == null ? RangeUtils.ANY : VersionRange.createFromVersionSpec(str);
			}
		};
		
		public abstract Iterable<Predicate> parse(String str) throws Exception;
	}
}
