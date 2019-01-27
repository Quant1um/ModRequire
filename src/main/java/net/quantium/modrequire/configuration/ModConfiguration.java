package net.quantium.modrequire.configuration;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.config.Configuration;
import net.quantium.modrequire.ModProvider;
import net.quantium.modrequire.configuration.rules.RequirementRules;
import net.quantium.modrequire.configuration.rules.RequirementRules.RequirementParsingException;
import net.quantium.modrequire.configuration.rules.predicates.PredicateParser.PredicateParserException;

public class ModConfiguration {

	private final File configPath;
	private final File rulesPath;
	
	private Configuration forgeConfiguration;
	
	private boolean doDumpRules;
	private RequirementRules reqRules;
	private MessageInfo msgInfo;
	
	public ModConfiguration(File dir) {
		dir.mkdirs();
		
		this.configPath = new File(dir, "config.cfg");
		this.rulesPath = new File(dir, "rules.txt");
	}
	
	public void load() {
		if(this.forgeConfiguration == null)
			this.forgeConfiguration = new Configuration(this.configPath);
		this.forgeConfiguration.load();
		
		loadProperties();
		
		if(this.forgeConfiguration.hasChanged())
			this.forgeConfiguration.save();
		
		Path path = this.rulesPath.toPath();
		try {
			if(!Files.exists(path)) {
				this.reqRules = RequirementRules.DEFAULT;
				Files.write(path, RequirementRules.DEFAULT_DATA);
			} else {
				this.reqRules = RequirementRules.parse(Files.readAllLines(this.rulesPath.toPath()));
			}
			
			
			if(doDumpRules()) {
				ModProvider.logger().info("Loaded rules:");
				if(this.reqRules.isEmpty()) {
					ModProvider.logger().info("<EMPTY>");
				}else{
					ModProvider.logger().info(this.reqRules.toReadableString());
				}
			}	
		} catch(RequirementParsingException p) {
			this.reqRules = RequirementRules.DEFAULT;
			ModProvider.logger().fatal("Error had occured while parsing rules: {}", p.getMessage());
			ModProvider.logger().fatal("Default ruleset will be used");
		} catch (Throwable t) {
			doBackup();
			ModProvider.logger().fatal("Failed to load requirement rules! Current file is backuped and default ruleset is used", t);
		}
	}
	
	private void loadProperties() {
		Configuration cfg = this.forgeConfiguration;
		
		cfg.setCategoryComment("message", "Here you can customize your rejection message.");
		
		String header = cfg.getString("header", "message", "In order to connect to this server you need to do following:", "Header message (%s - nickname)");
		String colInstall = cfg.getString("columnInstall", "message", TextFormatting.GREEN + "Install %d mod(s):" + TextFormatting.RESET, "'Mods to install' column header (%d - amount of mods/predicates)");
		String colRemove = cfg.getString("columnRemove", "message", TextFormatting.RED + "Delete %d mod(s):" + TextFormatting.RESET, "'Mods to remove' column header (%d - amount of mods)");
		boolean headerOnly = cfg.getBoolean("headerOnly", "message", false, "Show only header text and hide all rejections from message");
		
		this.msgInfo = new MessageInfo(header, colRemove, colInstall, headerOnly);
	
		this.doDumpRules = cfg.getBoolean("doDumpRules", "general", true, "Print rules to console when (re)loaded");
	}
	
	private void doBackup() {
		File fileBak = new File(this.rulesPath.getAbsolutePath() + "_" +
                new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".errored");
		this.rulesPath.renameTo(fileBak);
		
		this.reqRules = RequirementRules.DEFAULT;
		try{
			Files.write(this.rulesPath.toPath(), RequirementRules.DEFAULT_DATA);
		}catch(Throwable t2) {
			ModProvider.logger().fatal("Failed to write fallback default ruleset!", t2);
		}
	}
	
	public RequirementRules getRules() {
		return this.reqRules;
	}

	public MessageInfo getRejectionMessageInfo() {
		return this.msgInfo;
	}
	
	public boolean doDumpRules() {
		return this.doDumpRules;
	}
}
