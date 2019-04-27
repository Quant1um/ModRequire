package net.quantium.modrequire.configuration.message;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.quantium.modrequire.configuration.rules.Resolution;
import net.quantium.modrequire.configuration.rules.ModInfo;
import net.quantium.modrequire.configuration.rules.ModSelector;

public final class MessageBuilder {
	private static final char NEW_LINE = '\n';
	
	private static final int MAX_ENTRIES = 16;
	private static final int ENTRY_LENGTH = 24;
	
	private static final String PADDING = "    ";
	private static final String ELLIPSIS = "...";
	
	public static ITextComponent build(MessageInfo info, EntityPlayerMP player, Resolution result) {
		String nickname = player.getName();
		int removeCount = result.getForbidden().size();
		int installCount = result.getRequired().size();

		List<String> removeColumn = result.getForbidden()
				.stream()
				.limit(MAX_ENTRIES)
				.map(ModInfo::toString)
				.sorted()
				.collect(Collectors.toList());
		if(removeCount > MAX_ENTRIES) removeColumn.add(ELLIPSIS);
		
		List<String> installColumn = result.getRequired()
				.stream()
				.limit(MAX_ENTRIES)
				.map(ModSelector::toReadableString)
				.sorted()
				.collect(Collectors.toList());
		if(installCount > MAX_ENTRIES) installColumn.add(ELLIPSIS);
		
		String removeColumnHeader = String.format(info.getColumnRemove(), removeCount);
		String installColumnHeader = String.format(info.getColumnInstall(), installCount);
		String header = String.format(info.getHeader(), nickname);
		
		StringBuilder builder = new StringBuilder();
		
		builder.append(header).append(NEW_LINE);
		
		if(removeCount > 0 && installCount > 0) {
			buildDualTable(builder, installColumnHeader, installColumn, removeColumnHeader, removeColumn);
		}else {
			if(removeCount > 0) {
				buildSingleTable(builder, removeColumnHeader, removeColumn);
			}else if(installCount > 0) {
				buildSingleTable(builder, installColumnHeader, installColumn);
			}
		}
		
		return new TextComponentString(builder.toString());
	}

	private static void buildDualTable(StringBuilder builder, 
			String header1, List<String> column1,
			String header2, List<String> column2) {
		column1.add(0, header1);
		column2.add(0, header2);
		
		for(int i = 0; i < Math.max(column1.size(), column2.size()); i++) {
			String str1 = i >= column1.size() ? "" : column1.get(i);
			String str2 = i >= column2.size() ? "" : column2.get(i);
			
			str1 = StringUtils.abbreviate(str1, ENTRY_LENGTH);
			str2 = StringUtils.abbreviate(str2, ENTRY_LENGTH);
			
			str1 = StringUtils.leftPad(str1, ENTRY_LENGTH);
			str2 = StringUtils.rightPad(str2, ENTRY_LENGTH);
			
			builder
				.append(str1)
				.append(PADDING)
				.append(str2)
				.append(NEW_LINE);
		}
	}

	private static void buildSingleTable(StringBuilder builder, String header, List<String> column) {
		builder.append(header).append(NEW_LINE);
		for(String entry : column)
			builder.append(entry).append(NEW_LINE);
	}
}
