
package dk.tandhjulet.image.utils;

import org.bukkit.Location;

import dk.tandhjulet.image.objects.Axis;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.World;

public class LocationUtils {
	public static Location floorDecimals(Location location) {
		location.setX(Math.floor(location.getX()));
		location.setY(Math.floor(location.getY()));
		location.setZ(Math.floor(location.getZ()));

		return location;
	}

	public static void setRelative(Location toSet, Axis alongAxis, int relativeValue) {
		if (alongAxis == Axis.X) {
			toSet.add(0, 0, relativeValue);
		} else if (alongAxis == Axis.Z) {
			toSet.add(relativeValue, 0, 0);
		} else if (alongAxis == Axis.MULT) {
			toSet.add(relativeValue, 0, relativeValue);
		}
	}

	public static String stringify(Location loc) {
		return "x: " + loc.getBlockX() + " y: " + loc.getBlockY() + " z: " + loc.getBlockZ();
	}

	public static Location center(Location pos1, Location pos2) {
		if (pos1.getWorld() != pos2.getWorld())
			throw new IllegalArgumentException("Positions are not in the same world.");

		double avgX = Math.ceil((double) (pos1.getBlockX() + pos2.getBlockX()) / 2);
		double avgY = Math.ceil((double) (pos1.getBlockY() + pos2.getBlockY()) / 2);
		double avgZ = Math.ceil((double) (pos1.getBlockZ() + pos2.getBlockZ()) / 2);
		return new Location(pos1.getWorld(), avgX, avgY, avgZ);
	}

	public static BlockPosition toBlockPosition(Location loc) {
		return new BlockPosition(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
	}

	public static Location from(BlockPosition position, World world) {
		return new Location(world.getWorld(), position.getX(), position.getY(), position.getZ());
	}
}
