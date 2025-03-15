package dk.tandhjulet.image.map;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.map.MapView;

import dk.tandhjulet.image.PacketImage;
import dk.tandhjulet.image.objects.Direction;
import dk.tandhjulet.image.objects.FileExtension;
import dk.tandhjulet.image.utils.LocationUtils;
import lombok.Getter;

public class MapManager {
	private static final File imageFolder = new File(PacketImage.getInstance().getDataFolder(), "images");
	@Getter
	private static final HashMap<String, ImageMap> imageMaps = new HashMap<>();

	private static final HashMap<Short, RenderableImageMap> mapIdToImageMap = new HashMap<>();

	@Getter
	private static final HashSet<RenderableImageMap> renderedMaps = new HashSet<>();

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

		Bukkit.getLogger().info("---- RENDERING MAPS ----");

		for (RenderableImageMap image : PacketImage.getImageConfig().getImages()) {
			Direction frameDirection = RenderableImageMap.getFrameDirection(image.getRegion(), true);
			if (frameDirection == null) {
				Bukkit.getLogger()
						.warning(
								"Blocks are in the way (or missing) and the item frames can therefore not be placed (Map at: "
										+ LocationUtils.stringify(image.getRegion().getPos1())
										+ ")!");
			}

			image.render(null);
		}

		Bukkit.getLogger().info("---- DONE RENDERING ----");
	}

	public static void registerRegion(RenderableImageMap imageMap) {
		for (Short mapId : imageMap.getMapIds()) {
			mapIdToImageMap.put(mapId, imageMap);
		}
	}

	public static void unregisterRegion(RenderableImageMap imageMap) {
		for (Short mapId : imageMap.getMapIds()) {
			mapIdToImageMap.remove(mapId);
		}
	}

	public static void unregisterMapId(Short id) {
		mapIdToImageMap.remove(id);
	}

	public static void registerMapId(Short id, RenderableImageMap map) {
		mapIdToImageMap.put(id, map);
	}

	public static RenderableImageMap getRegisteredMap(Short id) {
		return mapIdToImageMap.get(id);
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
