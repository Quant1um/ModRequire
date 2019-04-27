package net.quantium.modrequire;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

public class ReloadCommand extends CommandBase {
	public static final ReloadCommand INSTANCE = new ReloadCommand();

	@Override
	public String getName() {
		return "reloadmrq";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/reloadmrq - Reloads ModRequire's config";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		ModProvider.config().load();
	}
	
	@Override
	public int getRequiredPermissionLevel() {
		return 4;
	}
}
