package dk.tandhjulet.image.utils;

import java.lang.reflect.Field;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import dk.tandhjulet.image.PacketImage;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.IBlockData;
import net.minecraft.server.v1_8_R3.PacketPlayOutBlockChange;

public class BlockUtils {
	private static Field blockPositionField;

	public static void addRelative(BlockFace blockFace, Location location) {
		switch (blockFace) {
			case UP:
			case DOWN:
			case SELF:
				break;
			case EAST:
			case EAST_NORTH_EAST:
			case EAST_SOUTH_EAST:
				location.add(1, 0, 0);
				break;
			case NORTH:
			case NORTH_EAST:
			case NORTH_NORTH_EAST:
			case NORTH_NORTH_WEST:
			case NORTH_WEST:
				location.add(0, 0, -1);
				break;
			case SOUTH:
			case SOUTH_EAST:
			case SOUTH_SOUTH_EAST:
			case SOUTH_SOUTH_WEST:
			case SOUTH_WEST:
				location.add(0, 0, 1);
				break;
			case WEST:
			case WEST_NORTH_WEST:
			case WEST_SOUTH_WEST:
				location.add(-1, 0, 0);
				break;

		}
	}

	public static void sendBlockUpdate(Player player, ItemStack block, Location loc, boolean waitTick)
			throws IllegalArgumentException, IllegalAccessException {
		sendBlockUpdate(player, getCombinedId(block), loc, waitTick);
	}

	public static void sendBlockUpdate(Player player, Block block, Location loc, boolean waitTick)
			throws IllegalArgumentException, IllegalAccessException {
		sendBlockUpdate(player, getCombinedId(block), loc, waitTick);
	}

	public static void sendBlockUpdate(Player player, int combinedId, Location loc, boolean waitTick)
			throws IllegalArgumentException, IllegalAccessException {

		IBlockData blockData = net.minecraft.server.v1_8_R3.Block.getByCombinedId(combinedId);
		PacketPlayOutBlockChange packet = new PacketPlayOutBlockChange();
		packet.block = blockData;

		BlockPosition blockPosition = new BlockPosition(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
		blockPositionField.set(packet, blockPosition);

		if (waitTick) {
			Bukkit.getScheduler().runTask(PacketImage.getInstance(), () -> {
				((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
			});
		} else {
			((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
		}
	}

	@SuppressWarnings("deprecation")
	public static int getCombinedId(Block block) {
		return block.getTypeId() + (block.getData() << 12);
	}

	@SuppressWarnings("deprecation")
	public static int getCombinedId(ItemStack block) {
		return block.getTypeId() + (block.getData().getData() << 12);
	}

	static {
		try {
			blockPositionField = PacketPlayOutBlockChange.class.getDeclaredField("a");
			blockPositionField.setAccessible(true);
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
	}
}
