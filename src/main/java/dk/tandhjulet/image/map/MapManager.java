package dk.tandhjulet.image.map;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapView;

import dk.tandhjulet.image.PacketImage;
import dk.tandhjulet.image.objects.Axis;
import dk.tandhjulet.image.objects.Direction;
import dk.tandhjulet.image.objects.FileExtension;
import dk.tandhjulet.image.utils.LocationUtils;
import lombok.Getter;

public class MapManager {
	private static final File imageFolder = new File(PacketImage.getInstance().getDataFolder(), "images");
	@Getter
	private static final HashMap<String, ImageMap> imageMaps = new HashMap<>();

	// @Getter
	// private static final HashMap<> renderedImages = new HashMap<>();

	public static void load() throws IOException {
		for (final File file : imageFolder.listFiles()) {
			final FileExtension extension = FileExtension.getExtension(file.getName());
			if (extension == null)
				continue;

			final String slugifiedName = file.getName().replaceAll(" ", "_");

			if (imageMaps.put(slugifiedName, new ImageMap(file)) instanceof ImageMap) {
				Bukkit.getLogger().info("Image naming conflict for " + slugifiedName
						+ ". It is random what image will take precedence.");
			}
		}

		for (RenderableImageMap image : PacketImage.getImageConfig().getImages()) {
			Axis axis = Axis.getAxisAlignment(image.getRegion());
			Direction frameDirection = image.getFrameDirection(axis, true);
			if (frameDirection == null) {
				Bukkit.getLogger()
						.severe("Blocks are in the way and the image maps can therefor not be placed (Map at: "
								+ LocationUtils.stringify(image.getRegion().getPos1()) + "). Skipping this map...");
				continue;
			}
			image.renderUnsafely(frameDirection, false, null);
		}
	}

	public static ItemStack createMap(MapView view) {
		final ItemStack map = new ItemStack(Material.MAP);
		map.setDurability(getMapId(view));

		return map;
	}

	@SuppressWarnings("deprecation")
	public static short getMapId(MapView view) {
		return view.getId();
	}

	@SuppressWarnings("deprecation")
	public static MapView getMapFromId(short id) {
		return Bukkit.getMap(id);
	}

	static {
		imageFolder.mkdirs();
	}
}
