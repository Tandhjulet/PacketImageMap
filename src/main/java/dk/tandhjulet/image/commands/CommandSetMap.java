package dk.tandhjulet.image.commands;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import dk.tandhjulet.image.map.RenderableImageMap;
import dk.tandhjulet.image.map.MapManager;
import dk.tandhjulet.image.objects.Axis;
import dk.tandhjulet.image.objects.Direction;
import dk.tandhjulet.image.objects.PlacementMetadata;

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
		} else if (placement.getPos1() == null || placement.getPos2() == null) {
			player.sendMessage("Please select both corner points.");
			return true;
		}

		Axis axis = Axis.getAxisAlignment(placement.getPos1(), placement.getPos2());
		if (axis == Axis.MULT) {
			player.sendMessage("Your selection has a depth greater than one.");
			player.sendMessage("Please redefine your selection.");
			return true;
		}

		RenderableImageMap map = MapManager.getImageMaps().get(args[0]).getRenderable();
		Direction frameDirection = map.getFrameDirection(placement.getPos1(), placement.getPos2(), axis, true);
		if (frameDirection == null) {
			player.sendMessage("Please ensure that there are no blocks in the way and that the back wall is filled.");
			return true;
		}
		Bukkit.getLogger().info(frameDirection.toString());

		placement.getPos1().getBlock().setType(Material.AIR);
		placement.getPos2().getBlock().setType(Material.AIR);

		if (map.renderUnsafely(placement.getPos1(), placement.getPos2(), frameDirection)) {
			player.sendMessage("Successfully placed image map.");
			PlacementMetadata.remove(player);
		} else {
			player.sendMessage("An error occured.");
		}

		return true;
	}

	public static void register() {
		Bukkit.getPluginCommand("setmap").setExecutor(new CommandSetMap());
	}

}
