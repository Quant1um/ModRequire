package net.quantium.modrequire.configuration.parsing;

import java.util.Collections;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Streams;

import net.quantium.modrequire.configuration.parsing.ChunkedParser.ChunkedParserException;
import net.quantium.modrequire.utils.Rethrow;

public class PrefixPredicateFormatParser<T, U, V, W> implements IParser<W>{
	private final Finisher<W, T> finisher;
	private final Combiner<T, U, V> combiner;
	private final Prefixer<U> prefixer;
	private final IParser<Iterable<V>> predicator;
	
	private final ChunkedParser<Iterable<T>> chunker;
	
	private PrefixPredicateFormatParser(Builder builder) {	
		Preconditions.checkNotNull(builder.combiner);
		Preconditions.checkNotNull(builder.prefixer);
		Preconditions.checkNotNull(builder.predicator);
		Preconditions.checkNotNull(builder.finisher);
		
		this.combiner = builder.combiner;
		this.prefixer = builder.prefixer;
		this.predicator = builder.predicator;
		this.finisher = builder.finisher;
		
		this.chunker = ChunkedParser.from(new PairParser(), Splitter.onPattern("(\r|\n|\r\n)"));
	}
	
	@Override
	public W tryParse(String string) throws ChunkedParserException {
		try {
			return this.finisher.finish(
					Streams.stream(this.chunker.tryParse(string))
						.flatMap((iterable) -> Streams.stream(iterable))
						.collect(Collectors.toList()));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public class PairParser implements IParser<Iterable<T>> {

		@Override
		public Iterable<T> tryParse(String string) throws ParserException {
			if(string.length() == 0) return Collections.emptyList();
			char pr = string.charAt(0);
			
			U prefix = PrefixPredicateFormatParser.this.prefixer.prefix(pr);
			if(prefix == null) return Collections.emptyList();
			
			return Streams.stream(PrefixPredicateFormatParser.this.predicator.tryParse(string.substring(1)))
					.map(Rethrow.function((predicate) -> PrefixPredicateFormatParser.this.combiner.combine(prefix, predicate)))
					.collect(Collectors.toList());
		}

	}
	
	public static class Builder<T, U, V, W> {
		
		private Finisher<W, T> finisher;
		private Combiner<T, U, V> combiner;
		private Prefixer<U> prefixer;
		private IParser<Iterable<V>> predicator;
		
		public Builder<T, U, V, W> finisher(Finisher<W, T> finisher) {
			this.finisher = finisher;
			return this;
		}
		
		public Builder<T, U, V, W> combiner(Combiner<T, U, V> combiner) {
			this.combiner = combiner;
			return this;
		}
		
		public Builder<T, U, V, W> prefixer(Prefixer<U> prefixer) {
			this.prefixer = prefixer;
			return this;
		}
		
		public Builder<T, U, V, W> predicator(IParser<Iterable<V>> predicator) {
			this.predicator = predicator;
			return this;
		}
		
		public PrefixPredicateFormatParser<T, U, V, W> build() {
			return new PrefixPredicateFormatParser<T, U, V, W>(this);
		}
	}
	
	@FunctionalInterface
	public static interface Prefixer<T> {
		T prefix(char c);
	}
	
	@FunctionalInterface
	public static interface Combiner<T, U, V> {
		T combine(U prefix, V predicate) throws Exception;
	}
	
	@FunctionalInterface
	public static interface Finisher<T, U> {
		T finish(Iterable<U> pairs) throws Exception;
	}
}
