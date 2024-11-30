package dk.tandhjulet.image.objects;

import org.bukkit.Location;

public enum Axis {
	MULT,
	X,
	Z;

	public static Axis getAxisAlignment(Location pos1, Location pos2) {
		if (pos1.getBlockZ() - pos2.getBlockZ() == 0D)
			return Axis.Z;

		if (pos1.getBlockX() - pos2.getBlockX() == 0D)
			return Axis.X;

		return Axis.MULT;
	}
}
