package dk.tandhjulet.image.listeners;

import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import dk.tandhjulet.image.PacketImage;
import dk.tandhjulet.image.map.MapManager;
import dk.tandhjulet.image.map.RenderableImageMap;

public class ConnectionListener implements Listener {

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		Player player = e.getPlayer();

		HashSet<RenderableImageMap> imageMaps = MapManager.getRenderedMaps();
		for (RenderableImageMap map : imageMaps) {
			map.send(player);
		}
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		Player player = e.getPlayer();

		HashSet<RenderableImageMap> imageMaps = MapManager.getRenderedMaps();
		for (RenderableImageMap map : imageMaps) {
			map.getSentTo().remove(player);
		}
	}

	public static void register() {
		Bukkit.getPluginManager().registerEvents(new ConnectionListener(), PacketImage.getInstance());
	}

}
