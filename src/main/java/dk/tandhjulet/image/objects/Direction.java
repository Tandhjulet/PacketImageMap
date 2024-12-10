package dk.tandhjulet.image.objects;

import org.apache.commons.lang3.EnumUtils;
import org.bukkit.block.BlockFace;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.server.v1_8_R3.EnumDirection;

@AllArgsConstructor
public enum Direction {
	NORTH(BlockFace.NORTH, Axis.X, EnumDirection.NORTH),
	SOUTH(BlockFace.SOUTH, Axis.X, EnumDirection.SOUTH),
	WEST(BlockFace.WEST, Axis.Z, EnumDirection.WEST),
	EAST(BlockFace.EAST, Axis.Z, EnumDirection.EAST);

	public static Direction from(String str) {
		boolean isValid = EnumUtils.isValidEnum(Direction.class, str.toUpperCase());
		if (!isValid)
			return null;

		return Direction.valueOf(str.toUpperCase());
	}

	@Override
	public String toString() {
		return name() + " (along " + getAxis().toString() + " axis)";
	}

	@Getter
	private final BlockFace blockFace;
	@Getter
	private final Axis axis;
	@Getter
	private final EnumDirection nmsDirection;
}
