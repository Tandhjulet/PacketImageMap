package dk.tandhjulet.image.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import dk.tandhjulet.image.PacketImage;
import dk.tandhjulet.image.map.ImageMap;
import dk.tandhjulet.image.map.MapManager;
import dk.tandhjulet.image.objects.PlacementMetadata;
import dk.tandhjulet.image.utils.BlockUtils;
import dk.tandhjulet.image.utils.CuboidRegion;
import dk.tandhjulet.image.utils.LocationUtils;

public class InteractListener implements Listener {

	//
	// Looking for the code handling left and right click on item frames?
	// To best circumvent any other plugin having the final say in what happens to
	// the map, the code has been moved directly to this plugins own NMS
	// (ImageFrame.java) implementation of item frames.
	//

	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		Player player = e.getPlayer();
		if (!player.isOp())
			return;
		else if (player.getItemInHand().getType() != Material.STICK)
			return;
		else if (e.getAction() != Action.LEFT_CLICK_BLOCK && e.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;

		PlacementMetadata metadata = PlacementMetadata.get(player);
		if (metadata == null)
			return;

		e.setCancelled(true);

		Location location = e.getClickedBlock().getLocation().clone();
		if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
			updatePoint(player, location, metadata.getPos1(), new ItemStack(Material.EMERALD_BLOCK));
			metadata.setPos1(location);
			player.sendMessage("Set pos1 at " + LocationUtils.stringify(location));
		} else {
			updatePoint(player, location, metadata.getPos2(), new ItemStack(Material.REDSTONE_BLOCK));
			metadata.setPos2(location);
			player.sendMessage("Set pos2 at " + LocationUtils.stringify(location));
		}

		if (!metadata.isBothPointsSet())
			return;

		CuboidRegion region = metadata.getRegion();
		if (region.getWidth() == -1) {
			player.sendMessage("Your current selection is invalid.");
			return;
		}

		ImageMap map = MapManager.getImageMaps().get(metadata.getImageFileName());
		String ratioMessage = String.format(
				"Selected: %dx%d - Image ratio: %dx%d.", region.getWidth(), region.getHeight(), map.getWidth(),
				map.getHeight());

		if (map.hasSameDimensions(region.getWidth(), region.getHeight())) {
			ratioMessage += " Your image will not need to be scaled.";
		} else if (map.canScaleCleanly(region.getWidth(), region.getHeight())) {
			ratioMessage += " Your image will scale cleanly.";
		} else {
			ratioMessage += " Your image will become distorted as the dimensions don't add up cleanly.";
		}

		player.sendMessage(ratioMessage);

	}

	private static void updatePoint(Player player, Location newPoint, Location oldPoint, ItemStack displayBlock) {
		if (oldPoint != null) {
			try {
				BlockUtils.sendBlockUpdate(player, oldPoint.getBlock(), oldPoint, false);
			} catch (IllegalArgumentException | IllegalAccessException exception) {
			}
		}

		LocationUtils.floorDecimals(newPoint);

		try {
			BlockUtils.sendBlockUpdate(player, displayBlock, newPoint, true);
		} catch (IllegalArgumentException | IllegalAccessException exception) {
		}
	}

	public static void register() {
		Bukkit.getPluginManager().registerEvents(new InteractListener(), PacketImage.getInstance());
	}

}
