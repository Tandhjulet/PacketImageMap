package dk.tandhjulet.image.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import dk.tandhjulet.image.map.ImageMap;
import dk.tandhjulet.image.map.MapManager;
import dk.tandhjulet.image.objects.PlacementMetadata;
import dk.tandhjulet.image.utils.BlockUtils;
import dk.tandhjulet.image.utils.LocationUtils;

public class CommandSetMap implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
		if (commandSender instanceof ConsoleCommandSender) {
			Bukkit.getLogger().warning("This command can only be used by players.");
			return true;
		} else if (args.length == 0) {
			return false;
		} else if (!commandSender.isOp()) {
			commandSender.sendMessage("Permission denied.");
			return true;
		}

		Player player = (Player) commandSender;
		PlacementMetadata placement = PlacementMetadata.get(player);
		if (placement == null) {
			PlacementMetadata.set(player, PlacementMetadata.ACTIVE);

			player.sendMessage("Please right click on the two points using a stick.");
			player.sendMessage("When you finished selecting the display area, run this command again.");
			return true;
		}

		else if (placement.getPos1() == null || placement.getPos2() == null) {
			player.sendMessage("Please select both corner points.");
			return true;
		}

		else if (!LocationUtils.isAxisAligned(placement.getPos1(), placement.getPos2())) {
			player.sendMessage("Your selection has a depth greater than one.");
			player.sendMessage("Please redefine your selection.");
			return true;
		}

		ImageMap map = MapManager.getImageMaps().get(args[0]);
		if (map.render(placement.getPos1())) {
			player.sendMessage("Successfully placed image map.");

			PlacementMetadata removed = PlacementMetadata.remove(player);
			if (removed == null)
				return true;

			try {
				BlockUtils.sendBlockUpdate(player, removed.getPos1().getBlock(), removed.getPos1());
				BlockUtils.sendBlockUpdate(player, removed.getPos2().getBlock(), removed.getPos2());
			} catch (IllegalArgumentException | IllegalAccessException e) {
				player.sendMessage(
						"Placement indication blocks not removed? Please relog. View logs for more information.");
				e.printStackTrace();
			}

		} else {
			player.sendMessage("An error occured.");
		}

		return true;
	}

	public static void register() {
		Bukkit.getPluginCommand("setmap").setExecutor(new CommandSetMap());
	}

}
