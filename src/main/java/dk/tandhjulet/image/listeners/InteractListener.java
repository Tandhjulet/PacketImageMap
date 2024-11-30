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
import dk.tandhjulet.image.objects.PlacementMetadata;
import dk.tandhjulet.image.utils.BlockUtils;
import dk.tandhjulet.image.utils.LocationUtils;

public class InteractListener implements Listener {

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
		if (metadata != PlacementMetadata.ACTIVE)
			return;

		e.setCancelled(true);

		Location location = e.getClickedBlock().getLocation().clone();
		if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
			updatePoint(player, location, metadata.getPos1(), new ItemStack(Material.EMERALD_BLOCK));
			metadata.setPos1(location);
			player.sendMessage("Set pos1");
		} else {
			updatePoint(player, location, metadata.getPos2(), new ItemStack(Material.REDSTONE_BLOCK));
			metadata.setPos2(location);
			player.sendMessage("Set pos2");
		}
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
