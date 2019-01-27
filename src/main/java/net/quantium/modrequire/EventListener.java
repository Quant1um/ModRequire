package net.quantium.modrequire;

import net.minecraft.network.NetHandlerPlayServer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.network.handshake.NetworkDispatcher;

public enum EventListener {
	INSTANCE;
	
	@SubscribeEvent
	public void onClientConnected(FMLNetworkEvent.ServerConnectionFromClientEvent event) {
		if(!(event.getHandler() instanceof NetHandlerPlayServer)) {
			return;
		}
		
		NetHandlerPlayServer netHandler = (NetHandlerPlayServer)event.getHandler();
	    NetworkDispatcher networkDispatcher = NetworkDispatcher.get(netHandler.netManager);
	    
	    ModResolver.Result result = ModResolver.resolve(networkDispatcher.getModList(), ModProvider.config().getRules());

	    if(result.hasRejections()) {
	    	netHandler.disconnect(MessageBuilder.build(ModProvider.config().getRejectionMessageInfo(), netHandler.player, result));
	    }
	}
}
