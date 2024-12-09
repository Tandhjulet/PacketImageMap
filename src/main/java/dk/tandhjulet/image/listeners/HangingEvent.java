package dk.tandhjulet.image.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingBreakEvent;

import dk.tandhjulet.image.PacketImage;
import dk.tandhjulet.image.map.MapManager;

public class HangingEvent implements Listener {

	// Will be called every 5 seconds for items with no wall to hang on.
	// A more performative aproach would be to override the tick counter in
	// EntityHanging.class, however, this will probably do for now...
	@EventHandler
	public void onHangingBreak(HangingBreakEvent e) {
		if (!(e.getEntity() instanceof ItemFrame))
			return;

		ItemFrame frame = (ItemFrame) e.getEntity();
		if (frame.getItem() == null)
			return;

		if (frame.getItem().getType() != Material.MAP)
			return;

		short mapId = frame.getItem().getDurability();
		if (MapManager.getMapFromId(mapId) == null)
			return;

		e.setCancelled(true);
	}

	public static void register() {
		Bukkit.getPluginManager().registerEvents(new HangingEvent(), PacketImage.getInstance());
	}
}
