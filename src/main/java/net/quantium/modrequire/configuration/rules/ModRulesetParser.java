package net.quantium.modrequire.configuration.rules;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Lists;
import com.google.common.collect.RangeSet;

import net.minecraftforge.fml.common.versioning.ArtifactVersion;
import net.minecraftforge.fml.common.versioning.InvalidVersionSpecificationException;
import net.minecraftforge.fml.common.versioning.VersionRange;
import net.quantium.modrequire.configuration.parsing.IParser;
import net.quantium.modrequire.configuration.parsing.PrefixPredicateFormatParser;
import net.quantium.modrequire.configuration.parsing.StagedParser;
import net.quantium.modrequire.utils.RangeUtils;

public class ModRulesetParser {
	
	private static enum EnumSelectorParsingStage implements StagedParser.Stage<Iterable<ModSelector>> {
		ANY {
			@Override
			public Iterable<ModSelector> tryParse(String str) {
				if(!str.equals("*"))
					return null;
				return Lists.newArrayList(ModSelector.ANY);
			}
		},
		
		GROUP_LOADED {
			@Override
			public Iterable<ModSelector> tryParse(String str) {
				if(!str.equalsIgnoreCase("*loaded"))
					return null;
				return ModSelector.loadedMods();
			}
		},
		
		GROUP_LOADED_ANY {
			@Override
			public Iterable<ModSelector> tryParse(String str) {
				if(!str.equalsIgnoreCase("*loaded-any"))
					return null;
				return ModSelector.loadedModsAnyVersion();
			}
		},
		
		DEFAULT {
			private final Pattern PATTERN = Pattern.compile("^(.{0,64}?)(@.+)?$");

			@Override
			public Iterable<ModSelector> tryParse(String str) throws InvalidVersionSpecificationException {
				Matcher matcher = PATTERN.matcher(str);
				
				if(!matcher.matches())
					return null;
				
				String modId = matcher.group(1);
				String versionString = matcher.group(2);
				VersionRange versionRange = parseVersion(versionString);
				RangeSet<ArtifactVersion> range = RangeUtils.fromRange(versionRange);
				
				if(range.isEmpty())
					return Lists.newArrayList(ModSelector.NONE);
				return Lists.newArrayList(ModSelector.specificMod(modId, range));
			}
			
			private VersionRange parseVersion(String str) throws InvalidVersionSpecificationException {
				return str == null ? RangeUtils.ANY : VersionRange.createFromVersionSpec(str.substring(1));
			}
		};
	}
	
	public static final PrefixPredicateFormatParser<ModRule, EnumAction, ModSelector, ModRuleset> PARSER = 
			new PrefixPredicateFormatParser.Builder<ModRule, EnumAction, ModSelector, ModRuleset>()
				.predicator(StagedParser.of(
							EnumSelectorParsingStage.ANY,
							EnumSelectorParsingStage.GROUP_LOADED,
							EnumSelectorParsingStage.GROUP_LOADED_ANY,
							EnumSelectorParsingStage.DEFAULT
						))
				.prefixer(EnumAction::fromPrefix)
				.combiner(ModRule::new)
				.finisher(ModRuleset::from)
				.build();
	
}
