package dk.tandhjulet.image.commands;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

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
import dk.tandhjulet.image.transformer.Transformer;
import dk.tandhjulet.image.utils.CuboidRegion;

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

		if (!MapManager.getImageMaps().containsKey(args[0])) {
			commandSender.sendMessage("Det billede eksisterer ikke.");
			return true;
		}

		Player player = (Player) commandSender;

		PlacementMetadata placement = PlacementMetadata.get(player);
		if (placement == null) {
			PlacementMetadata.create(player, args[0]);

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

		CuboidRegion region = placement.getRegion();

		Direction frameDirection = RenderableImageMap.getFrameDirection(region, true);
		if (frameDirection == null) {
			player.sendMessage("Please ensure that there are no blocks in the way and that the back wall is filled.");
			return true;
		}

		RenderableImageMap map;
		try {
			map = MapManager.getImageMaps().get(placement.getImageFileName())
					.getRenderable(region, frameDirection);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		map.applyTransformers(parseTransformers(args));

		placement.getPos1().getBlock().setType(Material.AIR);
		placement.getPos2().getBlock().setType(Material.AIR);

		if (map.render(() -> {
			map.save();
		})) {
			player.sendMessage("Successfully placed image map.");
			PlacementMetadata.remove(player);
		} else {
			player.sendMessage("An error occured.");
		}

		return true;
	}

	private List<Transformer> parseTransformers(String[] args) {
		List<Transformer> toApply = new LinkedList<>();
		for (String arg : args) {
			Transformer transformer = Transformer.from(arg);
			if (transformer == null)
				continue;

			toApply.add(transformer);
		}
		return toApply;
	}

	public static void register() {
		Bukkit.getPluginCommand("setmap").setExecutor(new CommandSetMap());
	}

}
