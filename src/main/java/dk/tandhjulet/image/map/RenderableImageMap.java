package dk.tandhjulet.image.map;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.util.Vector;

import dk.tandhjulet.image.PacketImage;
import dk.tandhjulet.image.config.ImageConfig;
import dk.tandhjulet.image.itemframe.CraftImageFrame;
import dk.tandhjulet.image.itemframe.ImageFrame;
import dk.tandhjulet.image.objects.Axis;
import dk.tandhjulet.image.objects.Direction;
import dk.tandhjulet.image.transformer.Transformer;
import dk.tandhjulet.image.utils.CuboidRegion;
import dk.tandhjulet.image.utils.LocationUtils;
import lombok.Getter;
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

	private final @Getter Direction frameDirection;

	private final @Getter HashSet<ImageRenderer> imageRenderers = new HashSet<>();

	@Getter
	private List<Transformer> transforms = new ArrayList<>();

	public RenderableImageMap(File imageFile, CuboidRegion region) throws IOException {
		this(imageFile, region, getFrameDirection(region, true));
	}

	public RenderableImageMap(File imageFile, CuboidRegion region, Direction frameDirection) throws IOException {
		this.frameDirection = frameDirection;
		this.region = region;
		this.imageFile = imageFile;
		this.image = ImageIO.read(imageFile);

		origWidth = image.getWidth();
		origHeight = image.getHeight();

		height = region.getHeight();
		width = region.get2DWidth();
		mapIds = new Short[height * width];

		double mapsNeededX = Math.ceil((double) origWidth / MAP_WIDTH);
		double mapsNeededY = Math.ceil((double) origHeight / MAP_HEIGHT);

		double scaleX = region.get2DWidth() / mapsNeededX;
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

		MapManager.getRenderedMaps().add(this);
	}

	public void replace(File imageFile) throws IOException {
		BufferedImage newImage = ImageIO.read(imageFile);
		int imageWidth = newImage.getWidth();
		int imageHeight = newImage.getHeight();
		double mapsNeededX = Math.ceil((double) imageWidth / MAP_WIDTH);
		double mapsNeededY = Math.ceil((double) imageHeight / MAP_HEIGHT);
		if (height != mapsNeededY || width != mapsNeededX)
			throw new IOException("Images are not the same map-size - cannot replace");

		this.image = newImage;
		this.imageFile = imageFile;
		this.imagesSplit = false;

		splitImages();

		for (ImageRenderer renderer : imageRenderers) {
			renderer.rendered = false;
		}
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

	public BufferedImage[] splitImages() {
		if (this.frameDirection == null)
			throw new IllegalArgumentException("No frame direction provided!");

		if (imagesSplit)
			return cutImages;

		cutImages = new BufferedImage[height * width];

		// Bukkit.getLogger().info("Length: " + cutImages.length + " width: " + width +
		// " height: " + height);

		// North and east facing maps should be in reverse sequence along x/z axis.
		final boolean isInverted = !(frameDirection.equals(Direction.WEST) || frameDirection.equals(Direction.SOUTH));

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

			if (!(ent instanceof CraftImageFrame))
				continue;
			ItemFrame frame = (ItemFrame) ent;
			if (frame.getItem() != null && frame.getItem().getType() == Material.MAP) {
				short mapId = frame.getItem().getDurability();
				if (MapManager.getRegisteredMap(mapId) != this) {
					return null;
				}
			}

			Location loc = ent.getLocation();
			LocationUtils.floorDecimals(loc);
			locToFrame.put(loc.hashCode(), (ItemFrame) ent);
		}

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

	public boolean render(Runnable callback) {
		splitImages();
		if (cutImages.length == 0)
			return false;

		final Collection<Entity> entities = region.getEntities();
		if (entities.size() == 0)
			return renderUnsafely(callback, entities);

		boolean itemframePresent = false;
		for (Entity ent : entities) {
			if (!(ent instanceof ItemFrame))
				continue;

			// Remove call will be ignored on CraftImageFrames, so we only clear
			// regular item frames here.
			ent.remove();

			if (!(ent instanceof CraftImageFrame))
				itemframePresent = true;
		}

		if (!itemframePresent)
			return renderUnsafely(callback, entities);

		Bukkit.getScheduler().runTaskLater(PacketImage.getInstance(), () -> {
			renderUnsafely(callback, entities);
		}, 2L);
		return true;
	}

	private boolean renderUnsafely(Runnable callback, Collection<Entity> entities) {
		World world = region.getWorld();

		try {
			final ItemFrame[][] itemframesPresent = getItemFrames(entities);
			if (itemframesPresent == null)
				return false;

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

				Short mapId = mapIds[id];
				MapView view = mapId == null ? null : MapManager.getMapFromId(mapId);
				if (view == null || mapId == null) {
					view = Bukkit.createMap(world);
					mapIds[id] = MapManager.getMapId(view);
				}

				if (view.getRenderers().size() != 1 || !(view.getRenderers().get(0) instanceof ImageRenderer)) {
					view.setWorld(world);

					for (MapRenderer renderer : view.getRenderers()) {
						view.removeRenderer(renderer);
					}

					ImageRenderer imageRenderer = new ImageRenderer(id, view);
					view.addRenderer(imageRenderer);
				}

				ItemFrame frame = itemframesPresent[locWidth][locHeight];
				ItemStack item = null;
				if (frame != null && frame.getItem() != null && frame.getItem().getType() == Material.MAP) {
					item = frame.getItem();
				}

				if (frame == null || item == null || item.getDurability() != mapIds[id]) {
					final ItemStack map = new ItemStack(Material.MAP);
					map.setDurability(MapManager.getMapId(view));

					if (frame == null) {
						ImageFrame nmsItemFrame = new ImageFrame(((CraftWorld) region.getWorld()).getHandle(),
								LocationUtils.toBlockPosition(loc), frameDirection.getNmsDirection());

						frame = new CraftImageFrame((CraftServer) Bukkit.getServer(), nmsItemFrame);

						// frame = (ItemFrame) world.spawnEntity(region, EntityType.ITEM_FRAME);
					}
					frame.setItem(map);
					frame.setFacingDirection(frameDirection.getBlockFace());
				}
			});
		} catch (Exception e) {
			Bukkit.getLogger()
					.severe("------- SEVERE ERROR OCCURED WHILST PLACING MAP -------");
			Bukkit.getLogger().severe("map at " + LocationUtils.stringify(region.getPos1()));
			e.printStackTrace();
		}

		MapManager.registerRegion(this);
		if (callback != null)
			callback.run();

		return true;
	}

	public void remove() {
		MapManager.unregisterRegion(this);

		region.getEntities().forEach((entity) -> {
			if (entity instanceof CraftImageFrame)
				((CraftImageFrame) entity).removeFrame();
		});

		for (BufferedImage image : cutImages) {
			image.flush();
		}

		MapManager.getRenderedMaps().remove(this);

		PacketImage.getImageConfig().getImages().remove(this);
		PacketImage.getImageConfig().save();
	}

	@Nullable
	public static Direction getFrameDirection(CuboidRegion region, boolean excludeMinAndMax) {
		Axis axis = region.getAxis();
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
		public boolean rendered = false;
		@Getter
		private final MapView view;
		private final int cutImageIndex;

		public ImageRenderer(int cutImageIndex, MapView view) {
			this.cutImageIndex = cutImageIndex;
			this.view = view;

			imageRenderers.add(this);
		}

		@Override
		public void render(MapView mapView, MapCanvas canvas, Player player) {
			if (cutImages[cutImageIndex] == null)
				return;

			if (rendered)
				return;

			canvas.drawImage(0, 0, cutImages[cutImageIndex]);
			rendered = true;
		}
	}
}
