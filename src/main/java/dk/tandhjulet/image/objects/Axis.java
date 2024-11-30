package dk.tandhjulet.image.objects;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public enum Axis {
	MULT,
	X,
	Z;

	public static Axis getAxisAlignment(Location pos1, Location pos2) {
		if (pos1.getBlockZ() - pos2.getBlockZ() == 0D)
			return Axis.X;

		if (pos1.getBlockX() - pos2.getBlockX() == 0D)
			return Axis.Z;

		return Axis.MULT;
	}

	@Nullable
	public Direction toDirection(boolean filledLeft, boolean filledRight) {
		Bukkit.getLogger().info(toString());

		if (this == Axis.MULT)
			return null;
		else if (this == Axis.X) {
			if (filledLeft && filledRight)
				return Direction.SOUTH;
			else if (filledLeft)
				return Direction.SOUTH;
			else if (filledRight)
				return Direction.NORTH;
		} else if (this == Axis.Z) {
			if (filledLeft && filledRight)
				return Direction.WEST;
			else if (filledLeft) {
				return Direction.EAST;
			} else if (filledRight)
				return Direction.WEST;
		}

		return null;
	}
}
