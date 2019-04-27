package net.quantium.modrequire.configuration.rules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.google.common.collect.Streams;

import net.quantium.modrequire.ModProvider;
import net.quantium.modrequire.configuration.parsing.ChunkedParser.ChunkedParserException;

public class ModRuleset implements Iterable<ModRule> {

	public static final Iterable<String> DEFAULT_DATA = Lists.newArrayList(
			"Use +<predicate> to make selected mod(s) optional",
			"Use -<predicate> to make selected mod(s) forbidden",
			"Use !<predicate> to make selected mod(s) required",
			"",
			"Where <predicate> is a simple mod selector",
			"You can use predicate <modid>[@versions] to select specified mod,",
			"                      *                  to select all mods",
			"                      *loaded[-any]      to select current mods installed on the server (use [-any] to select installed mods but do not check their version)",
			"                                                                                        (also this predicate does NOT select ModRequire mod itself because it is server-side mod)",
			"",
			"Any line that starts with +, - or ! (leftmost whitespaces are NOT ignored) is a rule, if line starts with any other symbol - it's a comment",
			"",
			"Rules are read from top to bottom, this means that rules at the top have higher priority in comparison to lower ones",
			"By default (if mod haven't matched any rule) mods are allowed but not required",
			"WARNING! This mod does not override forge mod checking behaviour, therefore you cannot allow clients to connect with some of mods missing!",
			"",
			"Examples:",
			"# +*loaded",
			"# -*",
			"This ruleset will disallow any mods that doesn't exists on the server",
			"",
			"# -optifine",
			"This ruleset will disallow usage of optifine");
	
	public static final ModRuleset DEFAULT = new ModRuleset(Collections.EMPTY_LIST);
	
	private final List<ModSelector> required;
	private final List<ModRule> rules;
	
	private ModRuleset(Iterable<ModRule> rules) {
		this.rules = reduce(rules);
		this.required = buildRequired(this.rules);
	}
	
	public String toReadableString() {
		return this.rules.stream()
					.map((rule) -> rule.toReadableString())
					.collect(Collectors.joining("\n"));
				
	}

	public boolean isEmpty() {
		return this.rules.isEmpty();
	}
	
	public EnumAction getAction(ModInfo mod) {
		return this.rules.stream()
					.filter((rule) -> rule.test(mod))
					.findFirst()
					.map((rule) -> rule.getAction())
					.orElse(EnumAction.ALLOW);
	}
	
	public Iterable<ModSelector> getRequired() {
		return this.required;
	}
	
	@Override
	public Iterator<ModRule> iterator() {
		return this.rules.iterator();
	}
	
	public Resolution resolve(Map<String, String> mods) {
		return resolve(mods.entrySet()
				.stream()
				.map((entry) -> new ModInfo(entry.getKey(), entry.getValue()))
				.collect(Collectors.toList()));
	}
	
	public Resolution resolve(Iterable<ModInfo> mods) {
		List<ModSelector> posRejected = new ArrayList<ModSelector>();
		List<ModInfo> negRejected = new ArrayList<ModInfo>();
		boolean hasModRequire = false;
		
		for(ModInfo mod : mods) {
			if(mod.getModId().equals(ModProvider.MODID)) {
				hasModRequire = true;
			}
			
			if(!getAction(mod).isPassable()) {
				negRejected.add(mod);
			}
		}
		
		for(ModSelector predicate : getRequired()) {
			if(!Streams.stream(mods).anyMatch((mod) -> predicate.test(mod))) {
				posRejected.add(predicate);
			}
		}
		
		return new Resolution(posRejected, negRejected, hasModRequire);
	}
	
	public static ModRuleset parse(String string) throws ChunkedParserException, InvalidRulesetException {
		return ModRulesetParser.PARSER.tryParse(string);
	}
	
	public static ModRuleset from(Iterable<ModRule> rules) throws InvalidRulesetException {
		List<ModRule> list = Lists.newArrayList(rules);
		if(list.isEmpty()) {
			return empty();
		}
		
		for(int i = 0; i < list.size() - 1; i++) {
			if(list.get(i).getSelector() == ModSelector.ANY) {
				throw new InvalidRulesetException(String.format("Rule with predicate '*' must be last in the ruleset! [line %d]", i + 1));
			}
		}
		
		return new ModRuleset(list);
	}
	
	public static ModRuleset empty() {
		return new ModRuleset(Collections.emptyList());
	}
	
	private static List<ModRule> reduce(Iterable<ModRule> rules) {
		return Streams.stream(rules)
				.filter((rule) -> rule.getSelector() != ModSelector.NONE)
				.collect(Collectors.toList());
	}
	
	private static List<ModSelector> buildRequired(Iterable<ModRule> rules) {
		List<ModSelector> result = new ArrayList<ModSelector>();	
		List<ModSelector> exclude = new ArrayList<ModSelector>();
		
		for(ModRule rule : rules) {
			if(rule.getAction() == EnumAction.REQUIRE) {
				ModSelector predicate = rule.getSelector();
				for(ModSelector excluded : exclude) {
					if(predicate == ModSelector.NONE) 
						break;
					predicate = predicate.subtract(excluded);
				}
				
				if(predicate != ModSelector.NONE) 
					result.add(predicate);
			}
			
			exclude.add(rule.getSelector());
		}
		
		return result;
	}
	
	public static class InvalidRulesetException extends Exception {
		public InvalidRulesetException(String string) {
			super(string);
		}
	}
}
