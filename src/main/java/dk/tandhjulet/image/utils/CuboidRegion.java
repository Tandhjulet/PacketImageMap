package dk.tandhjulet.image.utils;

import java.util.function.Consumer;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import lombok.Getter;

public class CuboidRegion {

	private final Location pos1, pos2;

	@Getter
	private final int maxX, maxY, maxZ;
	@Getter
	private final int minX, minY, minZ;

	private Vector min, max;

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
	 * Gets the width of the current selection.
	 * 
	 * @return width along x or z axis. if selection is not axis aligned, -1 is
	 *         returned.
	 */
	public int getWidth() {
		int x = maxX - minX;
		int z = maxZ - minZ;

		if (x > 0 && z > 0)
			return -1;
		return x + z + 1;
	}

	public int getHeight() {
		return maxY - minY + 1;
	}
}
