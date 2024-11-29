
package dk.tandhjulet.image.utils;

import org.bukkit.Location;

public class LocationUtils {
	public static Location floorDecimals(Location location) {
		location.setX(Math.floor(location.getX()));
		location.setY(Math.floor(location.getY()));
		location.setZ(Math.floor(location.getZ()));

		return location;
	}

	public static boolean isAxisAligned(Location pos1, Location pos2) {
		return pos1.getBlockZ() - pos2.getBlockZ() == 0D
				|| pos1.getBlockX() - pos2.getBlockX() == 0D;
	}
}
