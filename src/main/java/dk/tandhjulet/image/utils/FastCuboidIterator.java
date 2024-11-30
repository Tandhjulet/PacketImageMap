package dk.tandhjulet.image.utils;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.bukkit.Location;
import org.bukkit.util.Vector;

public class FastCuboidIterator implements Iterator<Location> {

	private final Location pos1, pos2;

	private final int maxX, maxY, maxZ;
	private int nextX, nextY, nextZ;

	private Vector min, max;

	private boolean hasNext = true;

	public FastCuboidIterator(Location pos1, Location pos2) {
		if (pos1.getWorld() != pos2.getWorld())
			throw new IllegalArgumentException("Positions are not in the same world");

		this.pos1 = pos1;
		this.pos2 = pos2;

		max = getMax();
		maxX = max.getBlockX();
		maxY = max.getBlockY();
		maxZ = max.getBlockZ();

		min = getMin();
		nextX = min.getBlockX();
		nextY = min.getBlockY();
		nextZ = min.getBlockZ();
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

	@Override
	public boolean hasNext() {
		return this.hasNext;
	}

	@Override
	public Location next() {
		if (!hasNext) {
			throw new NoSuchElementException();
		}

		Location location = new Location(pos1.getWorld(), nextX, nextY, maxZ);
		setNextLocation();

		return location;
	}

	private void setNextLocation() {
		if (++nextX <= maxX)
			return;
		nextX = getMin().getBlockX();

		if (++nextY <= maxY)
			return;
		nextY = getMin().getBlockY();

		if (++nextZ <= maxZ)
			return;
		hasNext = false;
	}
}
