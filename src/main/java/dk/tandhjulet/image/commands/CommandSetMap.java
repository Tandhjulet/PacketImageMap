package dk.tandhjulet.image.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import dk.tandhjulet.image.map.ImageMap;
import dk.tandhjulet.image.map.MapManager;

public class CommandSetMap implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
		if (commandSender instanceof ConsoleCommandSender) {
			Bukkit.getLogger().warning("This command can only be used by players.");
			return true;
		} else if (args.length == 0) {
			return false;
		}

		Player player = (Player) commandSender;

		ImageMap map = MapManager.getImageMaps().get(args[0]);
		if (map == null) {
			player.sendMessage("There doesnt exist a map with name: " + args[0] + ". Did you forget the extension (?)");
			return true;
		}
		if (map.render(player.getLocation())) {
			player.sendMessage("Maps successfully pasted.");
		} else {
			player.sendMessage("Could not paste maps.");
		}

		return true;
	}

	public static void register() {
		Bukkit.getPluginCommand("setmap").setExecutor(new CommandSetMap());
	}

}
