package net.quantium.modrequire.checking;

import com.google.common.base.Preconditions;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.quantium.modrequire.configuration.rules.Resolution;

public abstract class CheckEvent extends Event {
	private final CheckInfo info;
	
	public CheckEvent(EntityPlayerMP player, Resolution resolution) {
		this(new CheckInfo(player, resolution));
	}

	public CheckEvent(CheckInfo info) {
		Preconditions.checkNotNull(info);
		this.info = info;
	}
	
	public EntityPlayerMP getPlayer() {
		return this.info.getPlayer();
	}

	public Resolution getResolution() {
		return this.info.getResolution();
	}
	
	public CheckInfo getInfo() {
		return this.info;
	}
	
	public static CheckEvent create(EntityPlayerMP player, Resolution resolution) {
		if(resolution.hasRejections()) {
			return new Rejected(player, resolution);
		} else {
			return new Passed(player, resolution);
		}
	}
	
	public static Event pass(EntityPlayerMP player, Resolution resolution) {
		return new Passed(player, resolution);
	}
	
	public static Event reject(EntityPlayerMP player, Resolution resolution) {
		return new Rejected(player, resolution);
	}
	
	public static class Passed extends CheckEvent {
		private Passed(EntityPlayerMP player, Resolution resolution) {
			super(player, resolution);
		}
	}
	
	public static class Rejected extends CheckEvent {
		private Rejected(EntityPlayerMP player, Resolution resolution) {
			super(player, resolution);
		}
	}
}
