package net.quantium.modrequire.configuration.parsing;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Splitter;

public class ChunkedParser<T> implements IParser<Iterable<T>> {
	private final Splitter splitter;
	private final IParser<T> parser;
	
	private ChunkedParser(IParser<T> parser, Splitter splitter) {
		this.parser = parser;
		this.splitter = splitter;
	}

	@Override
	public Iterable<T> tryParse(String string) throws ChunkedParserException {
		List<T> list = new ArrayList<T>();
		
		int chunkId = 0;
		String chunkStr = "";
		try{
			for(String chunk : this.splitter.split(string)) {
				list.add(this.parser.tryParse(chunk));
				chunkId++;
				chunkStr = chunk;
			}
		}catch(ParserException e) {
			throw new ChunkedParserException(chunkId, chunkStr, e);
		}
		
		return list;
	}
	
	public static <T> ChunkedParser <T> from(IParser<T> parser, Splitter splitter) {
		return new ChunkedParser<T>(parser, splitter);
	}
	
	public static class ChunkedParserException extends ParserException {
		private final int chunkId;
		private final String chunk;
		
		public ChunkedParserException(int chunkId, String chunk, ParserException e) {
			super(String.format("Chunked parser threw an exception at line %d (%s): %s", chunkId, chunk, e == null ? "null" : e.getMessage()), e);
			this.chunkId = chunkId;
			this.chunk = chunk;
		}

		public int getChunkId() {
			return this.chunkId;
		}

		public String getChunk() {
			return this.chunk;
		}
	}
}

