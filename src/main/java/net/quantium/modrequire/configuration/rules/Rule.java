package net.quantium.modrequire.configuration.rules;

import java.util.stream.Collectors;

import org.codehaus.plexus.util.StringUtils;

import com.google.common.collect.Streams;

import net.quantium.modrequire.configuration.ModInfo;
import net.quantium.modrequire.configuration.rules.predicates.Predicate;
import net.quantium.modrequire.configuration.rules.predicates.PredicateParser;
import net.quantium.modrequire.configuration.rules.predicates.PredicateParser.PredicateParserException;
import net.quantium.modrequire.utils.Rethrow;

public class Rule {
	private final EnumMode mode;
	private final Predicate predicate;
	
	public Rule(EnumMode mode, Predicate predicate) throws InvalidRuleException {
		if(mode == EnumMode.REQUIRED && predicate == Predicate.ANY) {
			throw new InvalidRuleException("Rule 'required any' doesn't make sense");
		}
		
		this.mode = mode;
		this.predicate = predicate;
	}

	public EnumMode getMode() {
		return this.mode;
	}

	public Predicate getPredicate() {
		return this.predicate;
	}
	
	public boolean matches(ModInfo mod) {
		return this.predicate.matches(mod);
	}

	public String toReadableString() {
		return String.format("%s: %s", this.mode.getDescription(), this.predicate.toReadableString());
	}
	
	public static Iterable<Rule> parse(String str) throws PredicateParserException, InvalidRuleException {
		if(StringUtils.isBlank(str))
			return null;
		
		char prefix = str.charAt(0);
		EnumMode mode = EnumMode.getByPrefix(prefix);
		
		if(mode == null)
			return null;
		
		String predicateStr = str.substring(1);
		
		return Streams.stream(PredicateParser.parse(predicateStr))
				.map(Rethrow.<Predicate, Rule, InvalidRuleException>function((predicate) -> new Rule(mode, predicate)))
				.collect(Collectors.toList());
	}
	
	public static class InvalidRuleException extends Exception {
		public InvalidRuleException(String string) {
			super(string);
		}
	}
}
