package net.quantium.modrequire.log;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import com.google.common.io.MoreFiles;

import net.quantium.modrequire.ModProvider;
import net.quantium.modrequire.checking.CheckInfo;

public enum RejectionLog {
	INSTANCE(getLogPath(), StandardCharsets.UTF_8);
	
	private static Path getLogPath() {
		return Paths.get("mod-rejections.log").toAbsolutePath();
	}
	
	private final Path file;
	private final Charset charset;
	
	private RejectionLog(Path file, Charset charset) {
		this.file = file;
		this.charset = charset;
	}

	public void appendRejection(CheckInfo rejection) {
		try {
			MoreFiles.asCharSink(this.file, this.charset, 
					StandardOpenOption.CREATE, 
					StandardOpenOption.APPEND, 
					StandardOpenOption.WRITE).write(LogFormatter.format(rejection));
		} catch (IOException e) {
			ModProvider.logger().error("Error occurred while writing rejection to log file", e);
		}
	}
}
