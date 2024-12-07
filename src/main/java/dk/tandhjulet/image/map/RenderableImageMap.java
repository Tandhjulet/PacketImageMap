package dk.tandhjulet.image.map;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;

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
import dk.tandhjulet.image.config.ImageConfig;
import dk.tandhjulet.image.objects.Axis;
import dk.tandhjulet.image.objects.Direction;
import dk.tandhjulet.image.transformer.Transformer;
import dk.tandhjulet.image.utils.CuboidRegion;
import dk.tandhjulet.image.utils.LocationUtils;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

public class RenderableImageMap {
	public static final short MAP_WIDTH = 128;
	public static final short MAP_HEIGHT = 128;

	private @Getter File imageFile;
	private @Getter BufferedImage image;
	private @Getter boolean imagesSplit = false;

	private @Getter final int origWidth, origHeight;
	private @Getter @Setter int scaledWidth, scaledHeight;

	private @Getter final int width, height;

	private @Getter @Setter int insertX = 0, insertY = 0;

	private @Getter CuboidRegion region;

	private BufferedImage[] cutImages = null;
	private @Getter Short[] mapIds;

	@Getter
	private List<Transformer> transforms = new ArrayList<>();

	public RenderableImageMap(File imageFile, CuboidRegion region) throws IOException {
		this.region = region;
		this.imageFile = imageFile;
		this.image = ImageIO.read(imageFile);

		origWidth = image.getWidth();
		origHeight = image.getHeight();

		height = region.getHeight();
		width = region.getWidth();
		mapIds = new Short[height * width];

		double mapsNeededX = Math.ceil((double) origWidth / MAP_WIDTH);
		double mapsNeededY = Math.ceil((double) origHeight / MAP_HEIGHT);

		double scaleX = region.getWidth() / mapsNeededX;
		double scaleY = region.getHeight() / mapsNeededY;

		// Bukkit.getLogger().info("Maps needed " + mapsNeededX + " x " + mapsNeededY);
		// Bukkit.getLogger().info("Scaled up by " + scaleX + " x " + scaleY);

		scaledWidth = (int) Math.floor(image.getWidth() * scaleX);
		scaledHeight = (int) Math.floor(image.getHeight() * scaleY);

		insertX = -(width * MAP_WIDTH - scaledWidth) / 2;
		insertY = -(height * MAP_HEIGHT - scaledHeight) / 2;

		AffineTransform transform = new AffineTransform();
		transform.scale(scaleX, scaleY);

		applyAffineTransform(transform);
	}

	public void setMapIds(Short[] mapIds) {
		for (Short mapId : this.mapIds) {
			MapManager.unregisterMapId(mapId);
		}
		this.mapIds = mapIds;
		MapManager.registerRegion(this);
	}

	public void applyTransformers(List<Transformer> transforms) {
		AffineTransform transform = new AffineTransform();
		transforms.forEach((transformer) -> {
			transformer.apply(transform, this);
		});
		applyAffineTransform(transform);
		this.transforms.addAll(transforms);
	}

	private void applyAffineTransform(AffineTransform transform) {
		AffineTransformOp transformOp = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
		BufferedImage newImage = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_ARGB);

		BufferedImage temp = this.image;
		this.image = transformOp.filter(image, newImage);
		temp.flush();
	}

	public int getImageHeight() {
		return image.getHeight();
	}

	public int getImageWidth() {
		return image.getWidth();
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

	public BufferedImage[] splitImages(@NonNull Direction direction) {
		if (imagesSplit)
			return cutImages;

		cutImages = new BufferedImage[height * width];

		// Bukkit.getLogger().info("Length: " + cutImages.length + " width: " + width +
		// " height: " + height);

		// North and east facing maps should be in reverse sequence along x/z axis.
		final boolean isInverted = !(direction.equals(Direction.WEST) || direction.equals(Direction.SOUTH));

		int imageY = insertY;
		for (int y = 0; y < height; y++) {
			int imageX = insertX;
			for (int x = 0; x < width; x++) {
				// Bukkit.getLogger().info("y: " + y + " x: " + x);

				final int id;
				if (height == 1) {
					id = isInverted ? (width - x - 1) : x;
				} else if (width == 1) {
					id = (height - y - 1);
				} else {
					id = (height - y - 1) * Math.max(height, width) + (isInverted ? (width - x - 1) : x);
				}

				cutImages[id] = createSubImageFromOriginal(imageX, imageY);
				imageX += 128;
			}
			imageY += 128;
		}

		imagesSplit = true;
		image.flush();
		return cutImages;
	}

	private BufferedImage createSubImageFromOriginal(int x, int y) {
		BufferedImage subImage = new BufferedImage(MAP_WIDTH, MAP_HEIGHT, BufferedImage.TYPE_INT_ARGB);
		subImage.getGraphics().drawImage(this.image, -x, -y, null);
		return subImage;
	}

	private Collection<Entity> getEntities(Location pos1, Location pos2, Direction frameDirection) {
		World world = pos1.getWorld();
		LocationUtils.floorDecimals(pos1);
		LocationUtils.floorDecimals(pos2);
		Location centerLocation = LocationUtils.center(pos1, pos2);

		Collection<Entity> entities;
		if (frameDirection.getAxis() == Axis.X) {
			entities = world.getNearbyEntities(centerLocation, Math.ceil(width / 2D), Math.ceil(height / 2D), 1);
		} else if (frameDirection.getAxis() == Axis.Z) {
			entities = world.getNearbyEntities(centerLocation, 1, Math.ceil(height / 2D), Math.ceil(width / 2D));
		} else {
			entities = world.getNearbyEntities(centerLocation, width / 2D + 1, height / 2D + 1, width / 2D + 1);
		}
		return entities;
	}

	private boolean removeEntities(Location pos1, Location pos2, Direction frameDirection) {
		Collection<Entity> entities = getEntities(pos1, pos2, frameDirection);

		boolean itemFramePresent = false;
		for (Entity entity : entities) {
			if (entity instanceof ItemFrame) {
				entity.remove();
				itemFramePresent = true;
			}
		}

		return itemFramePresent;
	}

	public boolean renderUnsafely(Direction frameDirection, boolean createMaps, Runnable callback) {
		splitImages(frameDirection);
		if (cutImages.length == 0)
			return false;

		World world = region.getWorld();

		boolean itemFramePresent;
		if (createMaps)
			itemFramePresent = removeEntities(region.getPos1(), region.getPos2(), frameDirection);
		else
			itemFramePresent = false;

		Bukkit.getScheduler().runTaskLater(PacketImage.getInstance(), () -> {
			Vector minPoint = region.getMin();

			region.forEachLocation((loc) -> {
				int id = 0;
				if (width > 1) {
					if (height > 1)
						id = (loc.getBlockY() - minPoint.getBlockY()) * Math.max(width, height);
					if (frameDirection.getAxis() == Axis.X)
						id += loc.getBlockX() - minPoint.getBlockX();
					else
						id += loc.getBlockZ() - minPoint.getBlockZ();
				} else {
					id = (loc.getBlockY() - minPoint.getBlockY());
				}

				MapView view;
				if (createMaps) {
					view = Bukkit.createMap(world);
					mapIds[id] = MapManager.getMapId(view);
				} else {
					view = MapManager.getMapFromId(mapIds[id]);
				}

				view.setWorld(world);

				for (MapRenderer renderer : view.getRenderers()) {
					view.removeRenderer(renderer);
				}

				ImageRenderer imageRenderer = new ImageRenderer(id);
				view.addRenderer(imageRenderer);

				if (createMaps) {
					final ItemStack map = new ItemStack(Material.MAP);
					map.setDurability(MapManager.getMapId(view));
					ItemFrame itemFrame = (ItemFrame) world.spawnEntity(loc, EntityType.ITEM_FRAME);
					itemFrame.setItem(map);
					itemFrame.setFacingDirection(frameDirection.getBlockFace());
				}
			});

			MapManager.registerRegion(this);
			if (callback != null)
				callback.run();

			// Need to wait two ticks if item frames are present:
			// When marked for removal, it takes a tick before theyre actually removed.
			// If only one tick is waited, this code will run before then. Thus, we need to
			// wait two ticks before we can be sure that the item frames are removed.
		}, itemFramePresent ? 2L : 0L);

		return true;
	}

	public void remove() {
		Axis axis = Axis.getAxisAlignment(region);
		Direction frameDirection = getFrameDirection(axis, false);
		getEntities(region.getPos1(), region.getPos2(), frameDirection).forEach((entity) -> {
			entity.remove();
		});

		for (BufferedImage image : cutImages) {
			image.flush();
		}

		PacketImage.getImageConfig().getImages().remove(this);
		PacketImage.getImageConfig().save();
	}

	@Nullable
	public Direction getFrameDirection(@NonNull Axis axis, boolean excludeMinAndMax) {
		if (axis == Axis.MULT)
			return null;

		boolean isFilledOnRight = true,
				isFilledOnLeft = true;

		for (int x = region.getMinX(); x <= region.getMaxX(); x++) {
			for (int y = region.getMinY(); y <= region.getMaxY(); y++) {
				for (int z = region.getMinZ(); z <= region.getMaxZ(); z++) {
					Location loc = new Location(region.getWorld(), x, y, z);
					if (excludeMinAndMax && (loc.equals(region.getPos1()) || loc.equals(region.getPos2())))
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

	public void save() {
		ImageConfig conf = PacketImage.getImageConfig();
		conf.getImages().add(this);
		conf.save();
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
