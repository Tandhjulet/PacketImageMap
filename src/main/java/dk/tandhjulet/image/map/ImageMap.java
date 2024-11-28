package dk.tandhjulet.image.map;

import java.awt.image.BufferedImage;
import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import dk.tandhjulet.image.PacketImage;
import lombok.Getter;

public class ImageMap {
	public static final short MAP_WIDTH = 128;
	public static final short MAP_HEIGHT = 128;

	private final BufferedImage image;

	int origWidth, origHeight;

	@Getter
	int width, height;
	int insertX, insertY = 0;

	BufferedImage[] cutImages = null;

	public ImageMap(BufferedImage image) {

		this.image = image;
		calculateImageDimensions();
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
		cutImages = new BufferedImage[height * width];

		Bukkit.getLogger().info("Length: " + cutImages.length + " width: " + width + " height: " + height);

		int imageY = insertY;
		for (int y = 0; y < height; y++) {
			int imageX = insertX;
			for (int x = 0; x < width; x++) {
				cutImages[y * Math.max(height, width) + x] = createSubImageFromOriginal(imageX, imageY);
				imageX += 128;
			}
			imageY += 128;
		}

		return cutImages;
	}

	private BufferedImage createSubImageFromOriginal(int x, int y) {
		BufferedImage subImage = new BufferedImage(MAP_WIDTH, MAP_HEIGHT, BufferedImage.TYPE_INT_ARGB);
		subImage.getGraphics().drawImage(this.image, -x, -y, null);
		return subImage;
	}

	public boolean render(Location location) {
		splitImages();
		if (cutImages.length == 0)
			return false;
		World world = location.getWorld();

		location.setYaw(0);
		location.setPitch(0);
		location.setY(Math.floor(location.getY()));
		location.setX(Math.floor(location.getX()));

		Location centerLocation = location.clone().add(width / 2, height / 2, 0);
		Bukkit.getLogger()
				.info("x: " + centerLocation.getX() + " y: " + centerLocation.getY() + " z: " + centerLocation.getZ());

		Collection<Entity> entities = world.getNearbyEntities(centerLocation, width / 2 + 1, height / 2 + 1, 1);

		boolean itemFramePresent = false;
		for (Entity entity : entities) {
			if (entity instanceof ItemFrame) {
				entity.remove();
				itemFramePresent = true;
			}
		}

		Bukkit.getScheduler().runTaskLater(PacketImage.getInstance(), () -> {
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					MapView view = Bukkit.createMap(world);
					view.setWorld(world);

					for (MapRenderer renderer : view.getRenderers()) {
						view.removeRenderer(renderer);
					}

					ImageRenderer imageRenderer = new ImageRenderer(y * Math.max(height, width) + x);
					view.addRenderer(imageRenderer);

					ItemStack item = MapManager.createMap(view);
					Location relLoc = location.clone().add(x, height - y - 1, 0);

					// Bukkit.getLogger()
					// .info("x: " + relLoc.getX() + " y: " + relLoc.getY() + " z: " + relLoc.getZ()
					// + " using id: " + (y * Math.max(height, width) + x));

					// Spawn item frame

					ItemFrame itemFrame = (ItemFrame) world.spawnEntity(relLoc, EntityType.ITEM_FRAME);
					itemFrame.setItem(item);
					itemFrame.setFacingDirection(BlockFace.SOUTH);
				}
			}

			// Need to wait two ticks if item frames are present:
			// When marked for removal, it takes a tick before theyre actually removed.
			// If only one tick is waited, this code will run before then. Thus, we need to
			// wait two ticks before we can be sure that the item frames are removed.
		}, itemFramePresent ? 2L : 0L);

		return true;
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
			cutImages[cutImageIndex] = null;
		}
	}
}
