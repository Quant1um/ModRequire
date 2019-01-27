package net.quantium.modrequire.configuration.rules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Streams;

import net.quantium.modrequire.ModProvider;
import net.quantium.modrequire.configuration.ModInfo;
import net.quantium.modrequire.configuration.rules.Rule.InvalidRuleException;
import net.quantium.modrequire.configuration.rules.predicates.Predicate;
import net.quantium.modrequire.configuration.rules.predicates.PredicateParser.PredicateParserException;

public class RequirementRules implements Iterable<Rule> {
	public static final Iterable<String> DEFAULT_DATA = Lists.newArrayList(
			"Use +<predicate> to make selected mod(s) optional",
			"Use -<predicate> to make selected mod(s) forbidden",
			"Use !<predicate> to make selected mod(s) required",
			"",
			"Where <predicate> is a simple mod selector",
			"You can use predicate <modid>[@versions] to select specified mod,",
			"                      *                  to select all mods",
			"                      *loaded[-any]      to select current mods installed on the server (use [-any] to select installed mods but do not check their version)",
			"",
			"",
			"Any line that starts with +, - or ! (leftmost whitespaces are ignored) is a rule, if line starts with any other symbol - it's a comment",
			"",
			"Rules are read from top to bottom, this means that rules at the top have higher priority in comparison to lower ones",
			"WARNING! This mod does not override forge mod checking behaviour, therefore you cannot allow clients to connect with some of mods missing!",
			"",
			"Examples:",
			"# +*loaded",
			"# -*",
			"This ruleset will disallow any mods that doesn't exists on the server",
			"",
			"# -optifine",
			"# +*",
			"This ruleset will disallow usage of optifine");
	
	public static final RequirementRules DEFAULT = new RequirementRules(Collections.EMPTY_LIST);
	
	private final List<Predicate> required;
	private final List<Rule> rules;
	
	public RequirementRules(Iterable<Rule> rules) {
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
	
	public EnumMode getMode(ModInfo mod) {
		return this.rules.stream()
					.filter((rule) -> rule.matches(mod))
					.findFirst()
					.map((rule) -> rule.getMode())
					.orElse(EnumMode.OPTIONAL);
	}
	
	public Iterable<Predicate> getRequired() {
		return this.required;
	}
	
	@Override
	public Iterator<Rule> iterator() {
		return this.rules.iterator();
	}
	
	private static List<Rule> reduce(Iterable<Rule> rules) {
		return Streams.stream(rules)
				.filter((rule) -> rule.getPredicate() != Predicate.EMPTY)
				.collect(Collectors.toList());
	}
	
	private static List<Predicate> buildRequired(Iterable<Rule> rules) {
		List<Predicate> result = new ArrayList<Predicate>();	
		List<Predicate> exclude = new ArrayList<Predicate>();
		
		for(Rule rule : rules) {
			if(rule.getMode() == EnumMode.REQUIRED) {
				Predicate predicate = rule.getPredicate();
				for(Predicate excluded : exclude) {
					if(predicate == Predicate.EMPTY) 
						break;
					predicate = predicate.subtract(excluded);
				}
				
				if(predicate != Predicate.EMPTY) 
					result.add(predicate);
			}
			
			exclude.add(rule.getPredicate());
		}
		
		return result;
	}
	
	public static RequirementRules parse(Iterable<String> lines) throws RequirementParsingException {
		List<Rule> rules = new ArrayList<Rule>();
		
		int lineNumber = 0;
		for(String line : lines) {
			lineNumber++;
			
			try {
				String trimmed = StringUtils.trim(line);
				Iterable<Rule> parsed = Rule.parse(line);
				if(parsed != null) {
					for(Rule rule : parsed)
						rules.add(rule);
				}
			}catch(PredicateParserException | InvalidRuleException p) {
				throw new RequirementParsingException(lineNumber, line, p);
			}
		}
		
		return new RequirementRules(rules);
	}
	
	public static class RequirementParsingException extends Exception {

		public RequirementParsingException(int lineNumber, String lineContent, Exception e) {
			super(String.format("%s (at %d: %s)", e.getMessage(), lineNumber, lineContent), e);
		}
		
	}
}
