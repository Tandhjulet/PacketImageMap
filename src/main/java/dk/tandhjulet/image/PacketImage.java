package dk.tandhjulet.image;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import dk.tandhjulet.image.commands.CommandSetMap;
import dk.tandhjulet.image.config.ImageConfig;
import dk.tandhjulet.image.listeners.InteractListener;
import dk.tandhjulet.image.map.MapManager;
import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.yaml.bukkit.YamlBukkitConfigurer;
import eu.okaeri.configs.yaml.bukkit.serdes.SerdesBukkit;
import lombok.Getter;

public class PacketImage extends JavaPlugin {
	@Getter
	private static PacketImage instance;

	@Getter
	private static ImageConfig imageConfig;

	@Override
	public void onEnable() {
		instance = this;

		imageConfig = ConfigManager.create(ImageConfig.class, (conf) -> {
			conf.withConfigurer(new YamlBukkitConfigurer(), new SerdesBukkit());
			conf.withBindFile(new File(this.getDataFolder(), "images.yml"));
			conf.saveDefaults();
			conf.load(true);
		});

		try {
			MapManager.load();
		} catch (IOException e) {
			Bukkit.getLogger().severe("Failed to load MapManager.");
			e.printStackTrace();
		}

		CommandSetMap.register();
		InteractListener.register();
	}
}