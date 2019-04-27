package net.quantium.modrequire.configuration.parsing;

public interface IParser<T> {
	T tryParse(String string) throws ParserException;
}
