package net.quantium.modrequire;

import net.minecraft.network.NetHandlerPlayServer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.network.handshake.NetworkDispatcher;
import net.quantium.modrequire.checking.CheckEvent;
import net.quantium.modrequire.configuration.ModConfiguration;
import net.quantium.modrequire.configuration.message.MessageBuilder;
import net.quantium.modrequire.configuration.rules.Resolution;
import net.quantium.modrequire.log.RejectionLog;

public enum EventListener {
	INSTANCE;
	
	@SubscribeEvent
	public void kickPlayerOnRejection(CheckEvent.Rejected event) {
		event
			.getPlayer()
			.connection
			.disconnect(
					MessageBuilder.build(ModProvider.config().getRejectionMessageInfo(), 
					event.getPlayer(), 
					event.getResolution()));
	}
	
	@SubscribeEvent
	public void writeToLogOnRejection(CheckEvent.Rejected event) {
		RejectionLog.INSTANCE
			.appendRejection(event.getInfo());
	}
}
