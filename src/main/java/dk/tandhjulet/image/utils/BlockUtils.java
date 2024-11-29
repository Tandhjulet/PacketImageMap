package dk.tandhjulet.image.utils;

import java.lang.reflect.Field;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.IBlockData;
import net.minecraft.server.v1_8_R3.PacketPlayOutBlockChange;

public class BlockUtils {
	private static Field blockPositionField;

	public static void sendBlockUpdate(Player player, ItemStack block, Location loc)
			throws IllegalArgumentException, IllegalAccessException {
		sendBlockUpdate(player, getCombinedId(block), loc);
	}

	public static void sendBlockUpdate(Player player, Block block, Location loc)
			throws IllegalArgumentException, IllegalAccessException {
		sendBlockUpdate(player, getCombinedId(block), loc);
	}

	public static void sendBlockUpdate(Player player, int combinedId, Location loc)
			throws IllegalArgumentException, IllegalAccessException {

		IBlockData blockData = net.minecraft.server.v1_8_R3.Block.getByCombinedId(combinedId);
		PacketPlayOutBlockChange packet = new PacketPlayOutBlockChange();
		packet.block = blockData;

		BlockPosition blockPosition = new BlockPosition(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
		blockPositionField.set(packet, blockPosition);

		((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
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
