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
		int x = Math.max(pos1.getBlockX(), pos2.getBlockX());
		int y = Math.max(pos1.getBlockY(), pos2.getBlockY());
		int z = Math.max(pos1.getBlockZ(), pos2.getBlockZ());
		return new Vector(x, y, z);
	}

	public Vector getMin() {
		int x = Math.min(pos1.getBlockX(), pos2.getBlockX());
		int y = Math.min(pos1.getBlockY(), pos2.getBlockY());
		int z = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
		return new Vector(x, y, z);
	}
}
