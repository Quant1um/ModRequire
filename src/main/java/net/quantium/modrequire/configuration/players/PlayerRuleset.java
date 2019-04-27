package net.quantium.modrequire.configuration.players;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.google.common.collect.Streams;

import net.minecraft.entity.player.EntityPlayerMP;
import net.quantium.modrequire.configuration.parsing.ChunkedParser.ChunkedParserException;

public class PlayerRuleset implements Iterable<PlayerRule> {

	public static final Iterable<String> DEFAULT_DATA = Lists.newArrayList(
			"Use +<predicate> to make selected player(s) subject to checking",
			"Use -<predicate> to skip selected player(s) (this means that selected players can join the server regardless of mod rejections)",
			"",
			"Where <predicate> is a simple player selector",
			"You can use predicate <nickname>           to select player by nickname,",
			"                      <uuid>               to select player by uuid",
			"                      {<permission>}       to select all players that have given permission",
			"                      *ops [or *operators] to select all operators",
			"                      *                    to select all players",
			"",
			"",
			"Any line that starts with + or - (leftmost whitespaces are NOT ignored) is a rule, if line starts with any other symbol - it's a comment",
			"",
			"Rules are read from top to bottom, this means that rules at the top have higher priority in comparison to lower ones",
			"By default (if player haven't matched any rule) players are subject to checking",
			"",
			"Examples:",
			"# -*ops",
			"This ruleset will whitelist all operators",
			"",
			"# -MiyuChan",
			"# -80e439e0-2e44-4383-8bd5-a17c168065a2",
			"This ruleset will skip 'MiyuChat' and player with UUID '80e439e0-2e44-4383-8bd5-a17c168065a2'");
	
	public static final PlayerRuleset DEFAULT = new PlayerRuleset(Collections.EMPTY_LIST);
	
	private final List<PlayerRule> rules;
	
	private PlayerRuleset(Iterable<PlayerRule> rules) {
		this.rules = reduce(rules);
	}
	
	public String toReadableString() {
		return this.rules.stream()
					.map((rule) -> rule.toReadableString())
					.collect(Collectors.joining("\n"));
				
	}

	public boolean isEmpty() {
		return this.rules.isEmpty();
	}
	
	public EnumMode getMode(EntityPlayerMP ply) {
		return this.rules.stream()
					.filter((rule) -> rule.test(ply))
					.findFirst()
					.map((rule) -> rule.getMode())
					.orElse(EnumMode.CHECK);
	}
	
	@Override
	public Iterator<PlayerRule> iterator() {
		return this.rules.iterator();
	}
	
	public static PlayerRuleset parse(String string) throws ChunkedParserException, InvalidRulesetException {
		return PlayerRulesetParser.PARSER.tryParse(string);
	}
	
	public static PlayerRuleset from(Iterable<PlayerRule> rules) throws InvalidRulesetException {
		List<PlayerRule> list = Lists.newArrayList(rules);
		if(list.isEmpty()) {
			return empty();
		}
		
		for(int i = 0; i < list.size() - 1; i++) {
			if(list.get(i).getSelector() == PlayerSelector.ANYONE) {
				throw new InvalidRulesetException(String.format("Rule with predicate '*' must be last in the ruleset! [line %d]", i + 1));
			}
		}
		
		return new PlayerRuleset(list);
	}
	
	public static PlayerRuleset empty() {
		return new PlayerRuleset(Collections.emptyList());
	}
	
	private static List<PlayerRule> reduce(Iterable<PlayerRule> rules) {
		return Streams.stream(rules)
				.filter((rule) -> rule.getSelector() != PlayerSelector.NOONE)
				.collect(Collectors.toList());
	}
	
	public static class InvalidRulesetException extends Exception {
		public InvalidRulesetException(String string) {
			super(string);
		}
	}
}
