package dk.tandhjulet.image.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;

import dk.tandhjulet.image.PacketImage;
import dk.tandhjulet.image.objects.PlacementMetadata;
import dk.tandhjulet.image.utils.BlockUtils;
import dk.tandhjulet.image.utils.LocationUtils;

public class InteractListener implements Listener {

	@EventHandler
	public void onPlayerInteract(PlayerInteractEntityEvent e) {
		Player player = e.getPlayer();
		if (!player.isOp())
			return;
		else if (player.getItemInHand().getType() != Material.STICK)
			return;
		e.setCancelled(true);

		Entity entity = e.getRightClicked();
		PlacementMetadata metadata = PlacementMetadata.get(player);
		if (metadata != PlacementMetadata.ACTIVE)
			return;

		Location location = entity.getLocation().clone();
		LocationUtils.floorDecimals(location);

		if (location.equals(metadata.getPos1()) || location.equals(metadata.getPos2())) {
			try {
				BlockUtils.sendBlockUpdate(player, location.getBlock(), location);
			} catch (IllegalArgumentException | IllegalAccessException e1) {
				// Pretty bare bones, but hey. Let's hope that the function doesn't error out.
				// After all, this plugin is only designed to support 1.8, so support for other
				// versions should not be a guarantee.
				player.sendMessage("Removed position at " + location.toString());
			}

			if (location.equals(metadata.getPos1())) {
				metadata.setPos1(null);
			} else {
				metadata.setPos2(null);
			}
			return;
		}

		final ItemStack block;
		if (metadata.getPos1() == null) {
			block = new ItemStack(Material.EMERALD_BLOCK);
			metadata.setPos1(location);
		} else {
			block = new ItemStack(Material.REDSTONE_BLOCK);
			metadata.setPos2(location);
		}

		try {
			BlockUtils.sendBlockUpdate(player, block, entity.getLocation());
		} catch (IllegalArgumentException | IllegalAccessException e1) {
			player.sendMessage("Set point at " + location);
		}
	}

	public static void register() {
		Bukkit.getPluginManager().registerEvents(new InteractListener(), PacketImage.getInstance());
	}

}
