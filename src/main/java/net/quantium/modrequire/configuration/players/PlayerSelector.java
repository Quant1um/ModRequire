package net.quantium.modrequire.configuration.players;

import java.util.UUID;
import java.util.function.Predicate;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.server.permission.PermissionAPI;

public abstract class PlayerSelector implements Predicate<EntityPlayerMP> {
	public abstract String toReadableString();
	
	public static final PlayerSelector ANYONE = new Anyone();
	public static final PlayerSelector NOONE = new Noone();
	public static final PlayerSelector OPERATORS = new Operators();

	public static PlayerSelector byNickname(String nickname) {
		return new ByNickname(nickname);
	}
	
	public static PlayerSelector byUUID(UUID uuid) {
		return new ByUUID(uuid);
	}
	
	public static PlayerSelector withPermission(String permissionNode) {
		return new WithPermission(permissionNode);
	}
	
	private static class ByNickname extends PlayerSelector{
		private final String nickname;
		
		public ByNickname(String nickname) {
			this.nickname = nickname;
		}
		
		@Override
		public boolean test(EntityPlayerMP player) {
			return this.nickname
					.equals(player
							.getGameProfile()
							.getName());
		}

		@Override
		public String toReadableString() {
			return String.format("players with nickname '%s'", this.nickname);
		}
	}
	
	private static class ByUUID extends PlayerSelector{
		private final UUID uuid;
		
		public ByUUID(UUID uuid) {
			this.uuid = uuid;
		}
		
		@Override
		public boolean test(EntityPlayerMP player) {
			return this.uuid
					.equals(player
							.getGameProfile()
							.getId());
		}

		@Override
		public String toReadableString() {
			return String.format("players with uuid '%s'", this.uuid.toString());
		}
	}
	
	private static class WithPermission extends PlayerSelector{
		private final String permissionNode;
		
		public WithPermission(String permissionNode) {
			this.permissionNode = permissionNode;
		}
		
		@Override
		public boolean test(EntityPlayerMP player) {
			return PermissionAPI.hasPermission(player, this.permissionNode);
		}

		@Override
		public String toReadableString() {
			return String.format("players with permission '%s'", this.permissionNode);
		}
	}
	
	private static class Anyone extends PlayerSelector {
		
		@Override
		public boolean test(EntityPlayerMP mod) {
			return true;
		}

		@Override
		public String toReadableString() {
			return "anyone";
		}
	}
	
	private static class Noone extends PlayerSelector {
		@Override
		public boolean test(EntityPlayerMP mod) {
			return false;
		}

		@Override
		public String toReadableString() {
			return "no one";
		}
	}
	
	private static class Operators extends PlayerSelector {
		@Override
		public boolean test(EntityPlayerMP player) {
			return FMLCommonHandler.instance()
					.getMinecraftServerInstance()
					.getPlayerList()
					.getOppedPlayers()
					.getEntry(player.getGameProfile()) != null;
		}

		@Override
		public String toReadableString() {
			return "operators";
		}
	}
}
