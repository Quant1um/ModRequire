package net.quantium.modrequire.configuration;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;

import net.quantium.modrequire.configuration.parsing.IParser;

//2complex
public class ComplexLoader<T> {
	private final List<ErrorHandler> handlers;
	private final Charset charset;
	private final IParser<T> parser;
	
	private ComplexLoader(Builder<T> builder) {
		Preconditions.checkNotNull(builder.parser);
		Preconditions.checkNotNull(builder.charset);
		
		this.handlers = builder.handlers;
		this.charset = builder.charset;
		this.parser = builder.parser;
	}
	
	public Optional<T> load(Path path) {
		try {
			String content = 
					Files.readAllLines(path, this.charset)
						.stream()
						.collect(Collectors.joining("\n"));
			return Optional.of(this.parser.tryParse(content));
		} catch(Exception e) {
			ErrorContext context = new ErrorContext(path, this.charset);
			for(ErrorHandler handler : this.handlers) {
				try {
					if(handler.handleError(e, context)) {
						break;
					}
				}catch(Exception e1) {
					e1.printStackTrace();
				}
			}
			
			return Optional.empty();
		}
	}
	
	public static class ErrorContext {
		private final Path file;
		private final Charset charset;
		
		public ErrorContext(Path file, Charset charset) {
			this.file = file;
			this.charset = charset;
		}
		
		public Path getFile() {
			return this.file;
		}

		public Charset getCharset() {
			return this.charset;
		}
	}
	
	@FunctionalInterface
	public static interface ErrorHandler {
		boolean handleError(Exception e, ErrorContext context) throws Exception;
		
		public static ErrorHandler doBackup() {
			return (e, context) -> {
				Path path = context.getFile();
				Path pathBak = new File(path.toAbsolutePath().toString() + "_" +
		                new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".errored").toPath();
				Files.move(path, pathBak);
				return true;
			};
		}
		
		public static ErrorHandler thenWrite(String string) {
			return (e, context) -> {
				Path path = context.getFile();
				Charset charset = context.getCharset();
				
				Files.write(path, string.getBytes(charset));
				return true;
			};
		}
		
		public static ErrorHandler thenWrite(Iterable<String> lines) {
			return (e, context) -> {
				Path path = context.getFile();
				Charset charset = context.getCharset();
				
				Files.write(path, lines, charset);
				return true;
			};
		}
		
		public static ErrorHandler ifException(ErrorHandler handler, Class<? extends Exception>... classes) {
			return (e, context) -> {
				for(Class<? extends Exception> clazz : classes) {
					if(clazz.isInstance(e)) {
						handler.handleError(e, context);
						return true;
					}
				}
				return false;
			};
		}
		
		public static ErrorHandler multiple(ErrorHandler... handlers) {
			return (e, context) -> {
				for(ErrorHandler handler : handlers) {
					handler.handleError(e, context);
				}
				return true;
			};
		}
		
		public static ErrorHandler of(Consumer<Exception> consumer) {
			return (e, context) -> {
				consumer.accept(e);
				return true;
			};
		}
	}
	
	public static class Builder<T> {
		private final List<ErrorHandler> handlers = new ArrayList<ErrorHandler>();
		private Charset charset = StandardCharsets.UTF_8;
		private IParser<T> parser = null;
		
		public Builder<T> charset(Charset charset) {
			this.charset = charset;
			return this;
		}
		
		public Builder<T> parser(IParser<T> parser) {
			this.parser = parser;
			return this;
		}
		
		public Builder<T> error(ErrorHandler handler) {
			this.handlers.add(handler);
			return this;
		}
	
		public ComplexLoader<T> build() {
			return new ComplexLoader<T>(this);
		}
	}

}
