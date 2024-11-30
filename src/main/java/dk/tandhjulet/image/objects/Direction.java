package dk.tandhjulet.image.objects;

import org.bukkit.block.BlockFace;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum Direction {
	NORTH(BlockFace.NORTH, Axis.X),
	SOUTH(BlockFace.SOUTH, Axis.X),
	WEST(BlockFace.WEST, Axis.Z),
	EAST(BlockFace.EAST, Axis.Z);

	@Getter
	private final BlockFace blockFace;
	@Getter
	private final Axis axis;
}
