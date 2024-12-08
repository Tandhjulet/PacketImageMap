package dk.tandhjulet.image.objects;

import javax.annotation.Nullable;

import org.bukkit.Location;

import dk.tandhjulet.image.utils.CuboidRegion;

public enum Axis {
	MULT,
	X,
	Z;

	public static Axis getAxisAlignment(CuboidRegion region) {
		return getAxisAlignment(region.getPos1(), region.getPos2());
	}

	public static Axis getAxisAlignment(Location pos1, Location pos2) {
		if (pos1.getBlockZ() - pos2.getBlockZ() == 0D)
			return Axis.X;

		if (pos1.getBlockX() - pos2.getBlockX() == 0D)
			return Axis.Z;

		return Axis.MULT;
	}

	@Nullable
	public Direction toDirection(boolean filledLeft, boolean filledRight) {
		if (this == Axis.MULT)
			return null;
		else if (this == Axis.X) {
			if (filledLeft)
				return Direction.SOUTH;
			else if (filledRight)
				return Direction.NORTH;
		} else if (this == Axis.Z) {
			if (filledLeft) {
				return Direction.EAST;
			} else if (filledRight)
				return Direction.WEST;
		}

		return null;
	}
}
