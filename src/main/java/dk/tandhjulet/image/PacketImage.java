package dk.tandhjulet.image;

import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import dk.tandhjulet.image.commands.CommandSetMap;
import dk.tandhjulet.image.map.MapManager;
import lombok.Getter;

public class PacketImage extends JavaPlugin {
	@Getter
	private static PacketImage instance;

	@Override
	public void onEnable() {
		instance = this;

		try {
			MapManager.load();
		} catch (IOException e) {
			Bukkit.getLogger().severe("Failed to load MapManager.");
			e.printStackTrace();
		}
		CommandSetMap.register();
	}
}