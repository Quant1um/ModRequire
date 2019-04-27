package net.quantium.modrequire.checking;

import net.minecraft.network.NetHandlerPlayServer;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.network.handshake.NetworkDispatcher;
import net.quantium.modrequire.ModProvider;
import net.quantium.modrequire.configuration.ModConfiguration;
import net.quantium.modrequire.configuration.rules.Resolution;

public enum CheckHandler {
	INSTANCE;
	
	public static final EventBus EVENT_BUS = new EventBus();
	
	@SubscribeEvent
	public void onClientConnected(FMLNetworkEvent.ServerConnectionFromClientEvent event) {
		if(!(event.getHandler() instanceof NetHandlerPlayServer)) {
			throw new IllegalArgumentException("UwU (Please report this @quant1um)");
		}
		
		NetHandlerPlayServer netHandler = (NetHandlerPlayServer)event.getHandler();
	    NetworkDispatcher networkDispatcher = NetworkDispatcher.get(netHandler.netManager);
	    ModConfiguration conf = ModProvider.config();
	    
	    Resolution result = conf.getRules().resolve(networkDispatcher.getModList());

	    if(conf.getPlayers().getMode(netHandler.player).isPassable()) {
	    	EVENT_BUS.post(CheckEvent.pass(netHandler.player, result));
	    	return;
	    }	    
	    
	    EVENT_BUS.post(CheckEvent.create(netHandler.player, result));
	}
}
