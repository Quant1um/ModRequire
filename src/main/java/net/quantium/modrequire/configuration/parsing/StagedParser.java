package net.quantium.modrequire.configuration.parsing;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class StagedParser<T> implements IParser<T> {
	private final List<Stage<T>> stages;
	
	private StagedParser(List<Stage<T>> stages) {
		this.stages = stages;
	}
	
	public T tryParse(String string) throws StageParsingException, UnknownTokenException {
		try {
			for(Stage<T> stage : this.stages) {
				T t = stage.tryParse(string);
				if(t != null) {
					return t;
				}
			}
		} catch(Exception e) {
			throw new StageParsingException(string, e);
		}
		
		throw new UnknownTokenException(string);
	}
	
	public static <T> StagedParser<T> of(Stage<T>... stages) {
		return new StagedParser<T>(Lists.newArrayList(stages));
	}
	
	public static class StageParsingException extends ParserException {
		public StageParsingException(String token, Exception e) {
			super("Error occured while parsing token: " + token, e);
		}
	}
	
	public static class UnknownTokenException extends ParserException {
		public UnknownTokenException(String value) {
			super("Unknown token: " + value);
		}
	}
	
	public static interface Stage<T> {
		T tryParse(String string) throws Exception;
	}
}
