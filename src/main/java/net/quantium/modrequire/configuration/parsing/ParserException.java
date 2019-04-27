package net.quantium.modrequire.configuration.parsing;

public abstract class ParserException extends Exception {

	public ParserException() {
		super();
	}

	public ParserException(String arg0, Throwable arg1, boolean arg2, boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

	public ParserException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public ParserException(String arg0) {
		super(arg0);
	}

	public ParserException(Throwable arg0) {
		super(arg0);
	}
}

