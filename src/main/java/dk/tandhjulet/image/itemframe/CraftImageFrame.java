package dk.tandhjulet.image.itemframe;

import java.lang.reflect.Field;

import org.bukkit.Rotation;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftItemFrame;

import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.EntityHanging;
import net.minecraft.server.v1_8_R3.EntityItemFrame;
import net.minecraft.server.v1_8_R3.EnumDirection;
import net.minecraft.server.v1_8_R3.ItemStack;
import net.minecraft.server.v1_8_R3.WorldServer;

public class CraftImageFrame extends CraftItemFrame {
	private static Field bukkitEntityField;

	public CraftImageFrame(CraftServer server, ImageFrame entity) {
		super(server, entity);
		entity.setBukkitEntity(this);
	}

	@Override
	public boolean setFacingDirection(BlockFace face, boolean force) {
		EntityHanging hanging = this.getHandle();
		EnumDirection dir = hanging.direction;
		switch (face) {
			case NORTH:
				this.getHandle().setDirection(EnumDirection.NORTH);
				break;
			case EAST:
				this.getHandle().setDirection(EnumDirection.EAST);
				break;
			case SOUTH:
			default:
				this.getHandle().setDirection(EnumDirection.SOUTH);
				break;
			case WEST:
				this.getHandle().setDirection(EnumDirection.WEST);
		}

		if (!force && !hanging.survives()) {
			hanging.setDirection(dir);
			return false;
		}

		this.update();
		return true;
	}

	private void update() {
		EntityItemFrame old = this.getHandle();
		WorldServer world = ((CraftWorld) this.getWorld()).getHandle();
		BlockPosition position = old.getBlockPosition();
		EnumDirection direction = old.getDirection();
		ItemStack item = old.getItem() != null ? old.getItem().cloneItemStack() : null;
		old.die();

		ImageFrame frame = new ImageFrame(world, position, direction);
		frame.setItem(item);
		frame.setBukkitEntity(this);

		world.addEntity(frame);
		this.entity = frame;
	}

	@Override
	public void remove() {
	}

	public void removeFrame() {
		this.entity.dead = true;
	}

	@Override
	public void setRotation(Rotation rotation) {
	}

	static {
		try {
			bukkitEntityField = Entity.class.getDeclaredField("bukkitEntity");
			bukkitEntityField.setAccessible(true);
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
	}
}
