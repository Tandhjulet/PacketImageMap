package dk.tandhjulet.image.map;

import java.awt.image.BufferedImage;
import java.util.Collection;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.util.Vector;

import dk.tandhjulet.image.PacketImage;
import dk.tandhjulet.image.objects.Axis;
import dk.tandhjulet.image.objects.Direction;
import dk.tandhjulet.image.utils.CuboidRegion;
import dk.tandhjulet.image.utils.LocationUtils;
import lombok.Getter;

public class ImageMap {
	public static final short MAP_WIDTH = 128;
	public static final short MAP_HEIGHT = 128;

	@Getter
	private final BufferedImage image;
	@Getter
	boolean hasFlushedImage = false;

	int origWidth, origHeight;

	@Getter
	int width, height;
	int insertX = 0, insertY = 0;

	BufferedImage[] cutImages = null;

	public ImageMap(BufferedImage image) {
		this.image = image;
		calculateImageDimensions();
	}

	public Location getMax(Location min, Axis axis) {
		Location max = min.clone();
		if (axis != Axis.Z) {
			max.setX(min.getX() + getWidth() - 1);
		}
		if (axis != Axis.X) {
			max.setZ(min.getZ() + getWidth() - 1);
		}
		max.setY(min.getY() + getHeight() - 1);
		return max;
	}

	public void calculateImageDimensions() {
		origWidth = image.getWidth();
		origHeight = image.getHeight();

		int undersizedX = origWidth % MAP_WIDTH;
		int undersizedY = origHeight % MAP_HEIGHT;

		width = (int) Math.ceil(origWidth / MAP_WIDTH);
		height = (int) Math.ceil(origHeight / MAP_HEIGHT);

		// If undersized add another column/row and recenter the image.
		if (undersizedX > 0) {
			width++;
			insertX = (undersizedX - MAP_WIDTH) / 2;
		}
		if (undersizedY > 0) {
			height++;
			insertY = (undersizedY - MAP_HEIGHT) / 2;
		}
	}

	public BufferedImage[] splitImages() {
		if (hasFlushedImage)
			return cutImages;

		cutImages = new BufferedImage[height * width];

		Bukkit.getLogger().info("Length: " + cutImages.length + " width: " + width +
				" height: " + height);

		int imageY = insertY;
		for (int y = 0; y < height; y++) {
			int imageX = insertX;
			for (int x = 0; x < width; x++) {
				cutImages[y * Math.max(height, width) + x] = createSubImageFromOriginal(imageX, imageY);
				imageX += 128;
			}
			imageY += 128;
		}

		hasFlushedImage = true;
		image.flush();
		return cutImages;
	}

	private BufferedImage createSubImageFromOriginal(int x, int y) {
		BufferedImage subImage = new BufferedImage(MAP_WIDTH, MAP_HEIGHT, BufferedImage.TYPE_INT_ARGB);
		subImage.getGraphics().drawImage(this.image, -x, -y, null);
		return subImage;
	}

	public boolean render(Location location, Direction frameDirection) {
		splitImages();
		if (cutImages.length == 0)
			return false;
		World world = location.getWorld();

		LocationUtils.floorDecimals(location);
		Location centerLocation = location.clone().add(width / 2, height / 2, 0);

		Collection<Entity> entities = world.getNearbyEntities(centerLocation, width / 2, height / 2, 1);

		boolean itemFramePresent = false;
		for (Entity entity : entities) {
			if (entity instanceof ItemFrame) {
				entity.remove();
				itemFramePresent = true;
			}
		}

		Bukkit.getScheduler().runTaskLater(PacketImage.getInstance(), () -> {
			CuboidRegion region = new CuboidRegion(location, getMax(location, frameDirection.getAxis()));
			Vector minPoint = region.getMin();

			region.forEachLocation((loc) -> {
				int id = (loc.getBlockY() - minPoint.getBlockY()) * Math.max(height, width);
				if (frameDirection.getAxis() == Axis.X)
					id += loc.getBlockX() - minPoint.getBlockX();
				else
					id += loc.getBlockZ() - minPoint.getBlockZ();

				MapView view = Bukkit.createMap(world);
				view.setWorld(world);

				for (MapRenderer renderer : view.getRenderers()) {
					view.removeRenderer(renderer);
				}

				ImageRenderer imageRenderer = new ImageRenderer(id);
				view.addRenderer(imageRenderer);

				ItemStack item = MapManager.createMap(view);
				ItemFrame itemFrame = (ItemFrame) world.spawnEntity(loc, EntityType.ITEM_FRAME);
				itemFrame.setItem(item);
				itemFrame.setFacingDirection(frameDirection.getBlockFace());
			});

			// Need to wait two ticks if item frames are present:
			// When marked for removal, it takes a tick before theyre actually removed.
			// If only one tick is waited, this code will run before then. Thus, we need to
			// wait two ticks before we can be sure that the item frames are removed.
		}, itemFramePresent ? 2L : 0L);

		return true;
	}

	@Nullable
	public Direction getFrameDirection(Location pos1, Location pos2, Axis axis, boolean excludeMinAndMax) {
		if (axis == Axis.MULT)
			return null;

		boolean isFilledOnRight = true,
				isFilledOnLeft = true;

		CuboidRegion region = new CuboidRegion(pos1, pos2);
		for (int x = region.getMinX(); x <= region.getMaxX(); x++) {
			for (int y = region.getMinY(); y <= region.getMaxY(); y++) {
				for (int z = region.getMinZ(); z <= region.getMaxZ(); z++) {
					Location loc = new Location(pos1.getWorld(), x, y, z);
					if (excludeMinAndMax && (loc.equals(pos1) || loc.equals(pos2)))
						continue;
					Material material = loc.getBlock().getType();

					LocationUtils.setRelative(loc, axis, 1);
					if (!loc.getBlock().getType().isSolid())
						isFilledOnRight = false;

					LocationUtils.setRelative(loc, axis, -2);
					if (!loc.getBlock().getType().isSolid())
						isFilledOnLeft = false;

					// Bukkit.getLogger().info(loc.toString() + " left: " + isFilledOnLeft + "
					// right: " + isFilledOnRight + " material: " + material);

					if (!isFilledOnLeft && !isFilledOnRight)
						return null;
					else if (material != Material.AIR)
						return null;
				}
			}
		}

		return axis.toDirection(isFilledOnLeft, isFilledOnRight);
	}

	public class ImageRenderer extends MapRenderer {
		int cutImageIndex;

		public ImageRenderer(int cutImageIndex) {
			super();

			this.cutImageIndex = cutImageIndex;
		}

		@Override
		public void render(MapView mapView, MapCanvas canvas, Player player) {
			if (cutImages[cutImageIndex] == null)
				return;

			canvas.drawImage(0, 0, cutImages[cutImageIndex]);
		}
	}
}
