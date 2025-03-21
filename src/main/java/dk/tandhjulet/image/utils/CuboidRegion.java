package dk.tandhjulet.image.utils;

import java.util.Collection;
import java.util.function.Consumer;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import dk.tandhjulet.image.objects.Axis;
import lombok.Getter;

public class CuboidRegion {

	@Getter
	private final Location pos1, pos2;

	@Getter
	private final int maxX, maxY, maxZ;
	@Getter
	private final int minX, minY, minZ;

	private Vector min, max;

	private @Getter Axis axis;

	public CuboidRegion(Location pos1, Location pos2) {
		if (pos1.getWorld() != pos2.getWorld())
			throw new IllegalArgumentException("Positions are not in the same world");

		this.pos1 = pos1;
		this.pos2 = pos2;

		max = getMax();
		maxX = max.getBlockX();
		maxY = max.getBlockY();
		maxZ = max.getBlockZ();

		min = getMin();
		minX = min.getBlockX();
		minY = min.getBlockY();
		minZ = min.getBlockZ();

		this.axis = Axis.getAxisAlignment(this);
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof CuboidRegion))
			return false;
		CuboidRegion otherRegion = (CuboidRegion) other;
		return (getMin().equals(otherRegion.getMin())) && (getMax().equals(otherRegion.getMax()));
	}

	public void forEachLocation(Consumer<Location> consumer) {
		for (int x = minX; x <= maxX; x++) {
			for (int y = minY; y <= maxY; y++) {
				for (int z = minZ; z <= maxZ; z++) {
					consumer.accept(new Location(pos1.getWorld(), x, y, z));
				}
			}
		}
	}

	public Vector getMax() {
		if (max != null)
			return max;

		int x = Math.max(pos1.getBlockX(), pos2.getBlockX());
		int y = Math.max(pos1.getBlockY(), pos2.getBlockY());
		int z = Math.max(pos1.getBlockZ(), pos2.getBlockZ());
		return new Vector(x, y, z);
	}

	public Vector getMin() {
		if (min != null)
			return min;

		int x = Math.min(pos1.getBlockX(), pos2.getBlockX());
		int y = Math.min(pos1.getBlockY(), pos2.getBlockY());
		int z = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
		return new Vector(x, y, z);
	}

	/**
	 * Gets the two-dimensional width of the current selection.
	 * 
	 * @return width along x or z axis. if selection is not axis aligned, -1 is
	 *         returned.
	 */
	public int get2DWidth() {
		int x = maxX - minX;
		int z = maxZ - minZ;

		if (x > 0 && z > 0)
			return -1;
		return x + z + 1;
	}

	public int getWidth() {
		return maxX - minX + 1;
	}

	public int getDepth() {
		return maxZ - minZ + 1;
	}

	public int getHeight() {
		return maxY - minY + 1;
	}

	public World getWorld() {
		return pos1.getWorld();
	}

	public Collection<Entity> getEntities() {
		int width = get2DWidth();
		int height = getHeight();

		World world = pos1.getWorld();
		LocationUtils.floorDecimals(pos1);
		LocationUtils.floorDecimals(pos2);
		Location centerLocation = LocationUtils.center(pos1, pos2);

		Collection<Entity> entities;
		if (axis == Axis.X) {
			entities = world.getNearbyEntities(centerLocation, Math.ceil(width / 2D), Math.ceil(height / 2D), 1);
		} else if (axis == Axis.Z) {
			entities = world.getNearbyEntities(centerLocation, 1, Math.ceil(height / 2D), Math.ceil(width / 2D));
		} else {
			entities = world.getNearbyEntities(centerLocation, width / 2D + 1, height / 2D + 1, width / 2D + 1);
		}
		return entities;
	}

	public String toString() {
		return "{min:{" + min + "},max:{" + max + "}";
	}
}
