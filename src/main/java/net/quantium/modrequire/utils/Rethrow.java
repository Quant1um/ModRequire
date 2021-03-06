package net.quantium.modrequire.utils;

import java.util.function.Function;

//https://stackoverflow.com/questions/25643348/java-8-method-reference-unhandled-exception
public final class Rethrow {
	@FunctionalInterface
	public static interface FunctionWithExceptions<T, R, E extends Exception> {
	    R apply(T t) throws E;
	}
	
	public static <T, R, E extends Exception> Function<T, R> function(FunctionWithExceptions<T, R, E> func) {
		return (t) -> {
			try {
				return func.apply(t);
			} catch(Exception e) {
				throw new RuntimeException(e);
			}
		};
	}
}
