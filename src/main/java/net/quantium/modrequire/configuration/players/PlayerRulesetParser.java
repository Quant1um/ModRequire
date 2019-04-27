package net.quantium.modrequire.configuration.players;

import java.util.UUID;
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

public class PlayerRulesetParser {
	
	private static enum EnumSelectorParsingStage implements StagedParser.Stage<Iterable<PlayerSelector>> {
		ANY {
			@Override
			public Iterable<PlayerSelector> tryParse(String str) {
				if(!str.equals("*"))
					return null;
				return Lists.newArrayList(PlayerSelector.ANYONE);
			}
		},
		
		OPERATORS {
			@Override
			public Iterable<PlayerSelector> tryParse(String str) {
				if(!str.equalsIgnoreCase("*ops") && !str.equalsIgnoreCase("*operators"))
					return null;
				return Lists.newArrayList(PlayerSelector.OPERATORS);
			}
		},
		
		WITH_PERMISSION {
			private final Pattern PATTERN = Pattern.compile("^\\{(.+)\\}$");
			
			@Override
			public Iterable<PlayerSelector> tryParse(String str) {
				Matcher matcher = PATTERN.matcher(str);
				
				if(!matcher.matches())
					return null;
				
				return Lists.newArrayList(PlayerSelector.withPermission(matcher.group(1)));
			}
		},
		
		BY_UUID {
			private final Pattern PATTERN = Pattern.compile("^[0-9a-fA-F]{8}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{12}$");
			
			@Override
			public Iterable<PlayerSelector> tryParse(String str) {
				Matcher matcher = PATTERN.matcher(str);
				if(!matcher.matches())
					return null;
				
				return Lists.newArrayList(PlayerSelector.byUUID(UUID.fromString(str.toLowerCase())));
			}
		},
		
		BY_NICKNAME {
			@Override
			public Iterable<PlayerSelector> tryParse(String str) {
				return Lists.newArrayList(PlayerSelector.byNickname(str));
			}
		};
	}
	
	public static final PrefixPredicateFormatParser<PlayerRule, EnumMode, PlayerSelector, PlayerRuleset> PARSER = 
			new PrefixPredicateFormatParser.Builder<PlayerRule, EnumMode, PlayerSelector, PlayerRuleset>()
				.predicator(StagedParser.of(
							EnumSelectorParsingStage.ANY,
							EnumSelectorParsingStage.OPERATORS,
							EnumSelectorParsingStage.BY_UUID,
							EnumSelectorParsingStage.BY_NICKNAME
						))
				.prefixer(EnumMode::fromPrefix)
				.combiner(PlayerRule::new)
				.finisher(PlayerRuleset::from)
				.build();
	
}
