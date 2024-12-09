package dk.tandhjulet.image.map;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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

	private @Getter Collection<ItemFrame> itemFrames;

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

	private ItemFrame[][] getItemFrames(Collection<Entity> entities) {
		if (entities == null)
			entities = region.getEntities();

		ItemFrame[][] itemframePresent = new ItemFrame[width][height];
		HashMap<Integer, ItemFrame> locToFrame = new HashMap<>();

		Vector min = region.getMin();

		for (Entity ent : entities) {
			if (!(ent instanceof ItemFrame))
				continue;

			Location loc = ent.getLocation();
			LocationUtils.floorDecimals(loc);
			locToFrame.put(loc.hashCode(), (ItemFrame) ent);
		}

		// Bukkit.getLogger().info("locToFrame size: " + locToFrame.size());

		region.forEachLocation((loc) -> {
			int width;
			if (region.getAxis() == Axis.X)
				width = loc.getBlockX() - min.getBlockX();
			else if (region.getAxis() == Axis.Z)
				width = loc.getBlockZ() - min.getBlockZ();
			else
				throw new RuntimeException("cuboid region is not axis aligned");

			int height = loc.getBlockY() - min.getBlockY();

			LocationUtils.floorDecimals(loc);

			Integer hash = loc.hashCode();
			itemframePresent[width][height] = locToFrame.get(hash);
		});

		return itemframePresent;
	}

	public boolean renderUnsafely(Direction frameDirection, boolean createMaps, Runnable callback) {
		splitImages(frameDirection);
		if (cutImages.length == 0)
			return false;

		World world = region.getWorld();

		final Collection<Entity> entities = region.getEntities();
		final ItemFrame[][] itemframesPresent = getItemFrames(entities);

		// Bukkit.getLogger().info("ents size: " + entities.size());
		// Bukkit.getLogger().info("item frames: ");
		// Bukkit.getLogger().info(Arrays.toString(itemframesPresent[0]));
		// Bukkit.getLogger().info(Arrays.toString(itemframesPresent[1]));

		Bukkit.getScheduler().runTaskLater(PacketImage.getInstance(), () -> {
			Vector minPoint = region.getMin();

			region.forEachLocation((loc) -> {
				int id = 0;
				int locWidth = 0;
				int locHeight = loc.getBlockY() - minPoint.getBlockY();
				if (width > 1) {
					if (height > 1)
						id = locHeight * Math.max(width, height);
					if (frameDirection.getAxis() == Axis.X)
						locWidth = loc.getBlockX() - minPoint.getBlockX();
					else
						locWidth = loc.getBlockZ() - minPoint.getBlockZ();
					id += locWidth;
				} else {
					id = locHeight;
				}

				MapView view;
				if (createMaps) {
					view = Bukkit.createMap(world);
					mapIds[id] = MapManager.getMapId(view);
				} else {
					view = MapManager.getMapFromId(mapIds[id]);
				}

				if (view.getRenderers().size() != 1 || !(view.getRenderers().get(0) instanceof ImageRenderer)) {
					view.setWorld(world);

					for (MapRenderer renderer : view.getRenderers()) {
						view.removeRenderer(renderer);
					}

					ImageRenderer imageRenderer = new ImageRenderer(id);
					view.addRenderer(imageRenderer);
				}

				ItemFrame frame = itemframesPresent[locWidth][locHeight];
				ItemStack item = null;
				if (frame != null && frame.getItem() != null && frame.getItem().getType() == Material.MAP) {
					item = frame.getItem();
				}

				if (createMaps || frame == null || item.getDurability() != mapIds[id]) {
					final ItemStack map = new ItemStack(Material.MAP);
					map.setDurability(MapManager.getMapId(view));

					if (frame == null) {
						frame = (ItemFrame) world.spawnEntity(loc, EntityType.ITEM_FRAME);
					}
					frame.setItem(map);
					frame.setFacingDirection(frameDirection.getBlockFace());
				}
			});

			MapManager.registerRegion(this);
			if (callback != null)
				callback.run();

			// Need to wait two ticks if item frames are present:
			// When marked for removal, it takes a tick before theyre actually removed.
			// If only one tick is waited, this code will run before then. Thus, we need to
			// wait two ticks before we can be sure that the item frames are removed.
		}, 2L);

		return true;
	}

	public void remove() {
		region.getEntities().forEach((entity) -> {
			if (entity instanceof ItemFrame)
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
