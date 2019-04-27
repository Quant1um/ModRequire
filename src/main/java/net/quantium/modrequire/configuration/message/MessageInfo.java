package net.quantium.modrequire.configuration.message;

public class MessageInfo {
	private final String header;
	private final String columnRemove;
	private final String columnInstall;
	
	private final boolean headerOnly;

	public MessageInfo(String header, String columnRemove, String columnInstall, boolean headerOnly) {
		this.header = header;
		this.columnRemove = columnRemove;
		this.columnInstall = columnInstall;
		this.headerOnly = headerOnly;
	}
	
	public String getHeader() {
		return this.header;
	}

	public String getColumnRemove() {
		return this.columnRemove;
	}

	public String getColumnInstall() {
		return this.columnInstall;
	}	
	
	public boolean isHeaderOnly() {
		return this.headerOnly;
	}
}
