package net.quantium.modrequire;

import java.io.File;

import org.apache.logging.log4j.Logger;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.quantium.modrequire.ModProvider.NotAllowedOnClientException;
import net.quantium.modrequire.checking.CheckHandler;
import net.quantium.modrequire.configuration.ModConfiguration;

@Mod(modid = ModProvider.MODID, name = ModProvider.NAME, version = ModProvider.VERSION, acceptableRemoteVersions = "*")
public class ModProvider
{
	public static final String MODID = "qmodrequire";
    public static final String NAME = "Mod Require";
    public static final String VERSION = "0.3";

    @Instance
    private static ModProvider instance;
    
    private Logger logger;
    private ModConfiguration config;
    private CheckHandler handler;

    public static Logger logger() {
    	return instance.logger;
    }
    
    public static ModConfiguration config() {
    	return instance.config;
    }
    
    public static ModProvider instance() {
    	return instance;
    }
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
    	checkSide();
    	
        logger = event.getModLog();
        config = new ModConfiguration(new File(event.getSuggestedConfigurationFile().getParentFile(), "ModRequire").toPath());
        config.load();
        
        MinecraftForge.EVENT_BUS.register(CheckHandler.INSTANCE);
        CheckHandler.EVENT_BUS.register(EventListener.INSTANCE);
    }
    
    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
    	event.registerServerCommand(ReloadCommand.INSTANCE);
    }
    
    private void checkSide() {
    	if(FMLCommonHandler.instance().getSide().isClient())
    		throw new NotAllowedOnClientException();
    }
    
    public class NotAllowedOnClientException extends RuntimeException {
    	
    	public NotAllowedOnClientException() {
    		super("ModRequire mod is server-side only!");
    	}
	}
}
