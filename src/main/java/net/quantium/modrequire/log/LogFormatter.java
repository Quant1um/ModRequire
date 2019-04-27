package net.quantium.modrequire.log;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;

import net.minecraft.entity.player.EntityPlayerMP;
import net.quantium.modrequire.checking.CheckInfo;
import net.quantium.modrequire.configuration.rules.Resolution;

public class LogFormatter {

	public static String format(CheckInfo rejection) {
		final String DATE_FORMAT = "yyyy.MM.dd HH:mm:ss";
		final int PREFIX_LENGTH = DATE_FORMAT.length();
		final String POSTPREFIX = " | ";
		
		StringBuilder builder = new StringBuilder();

		builder
			.append(new SimpleDateFormat(DATE_FORMAT).format(new Date()))
			.append(POSTPREFIX)
			.append("Player ")
			.append(rejection.getPlayerName())
			.append(" was trying to join the server with following mod rejections:");
		
		String prefix = Strings.repeat(" ", PREFIX_LENGTH);
		
		if(!rejection.getForbidden().isEmpty()) {
			builder
				.append("\n")
				.append(prefix)
				.append(POSTPREFIX)
				.append("Forbidden mods: ")
				.append(Joiner.on(", ").join(
						rejection
							.getForbidden()
							.stream()
							.map((a) -> a.toString())
							.iterator()));
		}

		if(!rejection.getRequired().isEmpty()) {
			builder
				.append("\n")
				.append(prefix)
				.append(POSTPREFIX)
				.append("Absent mods: ")
				.append(Joiner.on(", ").join(
						rejection
							.getRequired()
							.stream()
							.map((a) -> a.toReadableString())
							.iterator()));
		}
		
		builder.append("\n");
		return builder.toString();
	}
}
