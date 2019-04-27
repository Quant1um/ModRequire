package net.quantium.modrequire.configuration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;

import org.apache.logging.log4j.Logger;

import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.config.Configuration;
import net.quantium.modrequire.ModProvider;
import net.quantium.modrequire.configuration.ComplexLoader.ErrorHandler;
import net.quantium.modrequire.configuration.message.MessageInfo;
import net.quantium.modrequire.configuration.parsing.ChunkedParser.ChunkedParserException;
import net.quantium.modrequire.configuration.players.PlayerRuleset;
import net.quantium.modrequire.configuration.players.PlayerRulesetParser;
import net.quantium.modrequire.configuration.rules.ModRuleset;
import net.quantium.modrequire.configuration.rules.ModRulesetParser;
import net.quantium.modrequire.configuration.watcher.DirectoryWatcher;
import net.quantium.modrequire.configuration.watcher.IWatcher;
import net.quantium.modrequire.configuration.watcher.NullWatcher;

public class ModConfiguration {

	private static final ComplexLoader<ModRuleset> MOD_RULESET_LOADER 
						= new ComplexLoader.Builder<ModRuleset>()
							.charset(StandardCharsets.UTF_8)
							.parser(ModRulesetParser.PARSER)
							.error(
								ErrorHandler.ifException(
									ErrorHandler.multiple(
										ErrorHandler.of((e) -> {
											ModProvider.logger().fatal("Error had occured while parsing rules!");
											ModProvider.logger().fatal("Default ruleset will be used.");
											printError(e, ModProvider.logger());
										})
									), 
									ModRuleset.InvalidRulesetException.class, 
									ChunkedParserException.class
								)
							)
							.error(
								ErrorHandler.ifException(
									ErrorHandler.thenWrite(ModRuleset.DEFAULT_DATA),
									NoSuchFileException.class
								)
							)
							.error(
								ErrorHandler.ifException(
									ErrorHandler.multiple(
										ErrorHandler.doBackup(),
										ErrorHandler.of((e) -> {
											ModProvider.logger().fatal("Failed to load requirement rules! Current file is backuped and default ruleset is used");
											printError(e, ModProvider.logger());
										})
									),
									IOException.class
								)
							)
							.error(
								ErrorHandler.of(
										(e) -> ModProvider.logger().fatal("Error occurred while loading mod ruleset!", e)
									)
							)
							.build();
	
	private static final ComplexLoader<PlayerRuleset> PLAYER_RULESET_LOADER 
						= new ComplexLoader.Builder<PlayerRuleset>()
							.charset(StandardCharsets.UTF_8)
							.parser(PlayerRulesetParser.PARSER)
							.error(
								ErrorHandler.ifException(
									ErrorHandler.multiple(
										ErrorHandler.of((e) -> {
											ModProvider.logger().fatal("Error had occured while parsing rules!");
											ModProvider.logger().fatal("Default ruleset will be used.");
											printError(e, ModProvider.logger());
										})
									), 
									PlayerRuleset.InvalidRulesetException.class, 
									ChunkedParserException.class
								)
							)
							.error(
								ErrorHandler.ifException(
									ErrorHandler.thenWrite(PlayerRuleset.DEFAULT_DATA),
									NoSuchFileException.class
								)
							)
							.error(
								ErrorHandler.ifException(
									ErrorHandler.multiple(
										ErrorHandler.doBackup(),
										ErrorHandler.of((e) -> {
											ModProvider.logger().fatal("Failed to load player ruleset! Current file is backuped and default ruleset is used");
											printError(e, ModProvider.logger());
										})
									),
									IOException.class
								)
							)
							.error(
									ErrorHandler.of(
											(e) -> ModProvider.logger().fatal("Error occurred while loading player ruleset!", e)
										)
							)
							.build();
	
	private static void printError(Throwable error, Logger logger) {
		if(error.getMessage() != null) logger.fatal(error.getMessage());
		error = error.getCause();
		
		int limit = 10;
		while(error != null && limit > 0) {
			if(error.getMessage() != null) logger.fatal("- {}", error.getMessage());
			error = error.getCause();
			limit--;
		}
	}
	
	private final Path configPath;
	private final Path rulesPath;
	private final Path playersPath;
	
	private Configuration forgeConfiguration;
	
	private MessageInfo msgInfo;
	private ModRuleset modRuleset;
	private PlayerRuleset playerRuleset;
	
	private IWatcher watcher;
	
	public ModConfiguration(Path dir) {
		try {
			Files.createDirectories(dir);
		} catch (IOException e) {
			ModProvider.logger().error("Failed to create config directory", e);
		}
		
		try {
			watcher = new DirectoryWatcher(dir, this::reload, 
					StandardWatchEventKinds.ENTRY_MODIFY, 
					StandardWatchEventKinds.ENTRY_CREATE, 
					StandardWatchEventKinds.ENTRY_DELETE);
		} catch (IOException e) {
			watcher = new NullWatcher();
			ModProvider.logger().warn("Failed to init directory watcher", e);
		}
		
		this.configPath = dir.resolve("config.cfg");
		this.rulesPath = dir.resolve("rules.txt");
		this.playersPath = dir.resolve("players.txt");
	}
	
	private void reload() {
		ModProvider.logger().info("Config has been automatically reloaded ^^");
		load();
	}
	
	public void load() {
		watcher.setAccept(false);
		
		try {
			if(this.forgeConfiguration == null)
				this.forgeConfiguration = new Configuration(this.configPath.toFile());
			this.forgeConfiguration.load();
			
			loadProperties();
			
			if(this.forgeConfiguration.hasChanged())
				this.forgeConfiguration.save();
			
			this.modRuleset = MOD_RULESET_LOADER
								.load(this.rulesPath)
								.orElse(ModRuleset.DEFAULT);
			
			this.playerRuleset = PLAYER_RULESET_LOADER
								.load(this.playersPath)
								.orElse(PlayerRuleset.DEFAULT);
			
			dumpModRuleset();
			dumpPlayerRuleset();
		} finally {
			watcher.setAccept(true);
		}
	}
	
	public void dumpModRuleset() {
		StringBuilder builder = new StringBuilder();
		builder.append("Loaded filter rules:").append("\n");
		
		if(this.modRuleset.isEmpty()) {
			builder.append("<none>");
		}else{
			builder.append(this.modRuleset.toReadableString());
		}
		
		ModProvider.logger().info(builder.toString());
	}
	
	public void dumpPlayerRuleset() {
		StringBuilder builder = new StringBuilder();
		builder.append("Loaded player rules:").append("\n");
		
		if(this.playerRuleset.isEmpty()) {
			builder.append("<none>");
		}else{
			builder.append(this.playerRuleset.toReadableString());
		}
		
		ModProvider.logger().info(builder.toString());
	}
	
	private void loadProperties() {
		Configuration cfg = this.forgeConfiguration;
		
		cfg.setCategoryComment("message", "Here you can customize your rejection message.");
		
		String header = cfg.getString("header", "message", "In order to connect to this server you need to do following:", "Header message (%s - nickname)");
		String colInstall = cfg.getString("columnInstall", "message", TextFormatting.GREEN + "Install %d mod(s):" + TextFormatting.RESET, "'Mods to install' column header (%d - amount of mods/predicates)");
		String colRemove = cfg.getString("columnRemove", "message", TextFormatting.RED + "Delete %d mod(s):" + TextFormatting.RESET, "'Mods to remove' column header (%d - amount of mods)");
		boolean headerOnly = cfg.getBoolean("headerOnly", "message", false, "Show only header text and hide all rejections from message");
		
		this.msgInfo = new MessageInfo(header, colRemove, colInstall, headerOnly);
	}
	
	public ModRuleset getRules() {
		return this.modRuleset;
	}

	public PlayerRuleset getPlayers() {
		return this.playerRuleset;
	}
	
	public MessageInfo getRejectionMessageInfo() {
		return this.msgInfo;
	}
}
