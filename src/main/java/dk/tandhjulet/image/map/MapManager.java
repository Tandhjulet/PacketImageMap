package dk.tandhjulet.image.map;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.imageio.ImageIO;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapView;

import dk.tandhjulet.image.PacketImage;
import dk.tandhjulet.image.objects.FileExtension;
import lombok.Getter;

public class MapManager {
	private static final File imageFolder = new File(PacketImage.getInstance().getDataFolder(), "images");
	@Getter
	private static final HashMap<String, ImageMap> imageMaps = new HashMap<>();

	public static void load() throws IOException {
		for (final File file : imageFolder.listFiles()) {
			final FileExtension extension = FileExtension.getExtension(file.getName());
			if (extension == null)
				continue;

			final BufferedImage image = ImageIO.read(file);
			final String slugifiedName = file.getName().replaceAll(" ", "_");

			if (imageMaps.put(slugifiedName, new ImageMap(image)) instanceof ImageMap) {
				Bukkit.getLogger().info("Image naming conflict for " + slugifiedName
						+ ". It is random what image will take precedence.");
			}
		}
	}

	@SuppressWarnings("deprecation")
	public static ItemStack createMap(MapView view) {
		final ItemStack map = new ItemStack(Material.MAP);
		map.setDurability(view.getId());

		return map;
	}

	static {
		imageFolder.mkdirs();
	}
}
