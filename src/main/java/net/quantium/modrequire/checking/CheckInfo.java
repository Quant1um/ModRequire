package net.quantium.modrequire.checking;

import java.util.List;
import java.util.UUID;

import com.google.common.base.Preconditions;

import net.minecraft.entity.player.EntityPlayerMP;
import net.quantium.modrequire.configuration.rules.ModInfo;
import net.quantium.modrequire.configuration.rules.ModSelector;
import net.quantium.modrequire.configuration.rules.Resolution;

public class CheckInfo {
	private final EntityPlayerMP player;
	private final Resolution resolution;
	
	public CheckInfo(EntityPlayerMP player, Resolution resolution) {
		Preconditions.checkNotNull(player);
		Preconditions.checkNotNull(resolution);
		
		this.player = player;
		this.resolution = resolution;
	}

	public EntityPlayerMP getPlayer() {
		return this.player;
	}

	public Resolution getResolution() {
		return this.resolution;
	}
	
	public UUID getPlayerUUID() {
		return this.player.getGameProfile().getId();
	}
	
	public String getPlayerName() {
		return this.player.getGameProfile().getName();
	}
	
	public List<ModSelector> getRequired() {
		return this.resolution.getRequired();
	}
	
	public List<ModInfo> getForbidden() {
		return this.resolution.getForbidden();
	}
	
	public boolean hasRejections() {
		return this.resolution.hasRejections();
	}
	
	public boolean hasThisMod() {
		return this.resolution.hasThisMod();
	}
}
