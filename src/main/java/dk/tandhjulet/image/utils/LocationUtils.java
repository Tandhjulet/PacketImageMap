
package dk.tandhjulet.image.utils;

import org.bukkit.Location;

import dk.tandhjulet.image.objects.Axis;

public class LocationUtils {
	public static Location floorDecimals(Location location) {
		location.setX(Math.floor(location.getX()));
		location.setY(Math.floor(location.getY()));
		location.setZ(Math.floor(location.getZ()));

		return location;
	}

	public static void setRelative(Location toSet, Axis alongAxis, int relativeValue) {
		if (alongAxis == Axis.X) {
			toSet.add(relativeValue, 0, 0);
		} else if (alongAxis == Axis.Z) {
			toSet.add(0, 0, relativeValue);
		} else if (alongAxis == Axis.MULT) {
			toSet.add(relativeValue, 0, relativeValue);
		}
	}
}
