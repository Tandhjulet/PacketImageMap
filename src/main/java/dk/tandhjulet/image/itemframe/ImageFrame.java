package dk.tandhjulet.image.itemframe;

import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftHumanEntity;

import dk.tandhjulet.image.map.MapManager;
import dk.tandhjulet.image.map.RenderableImageMap;
import dk.tandhjulet.image.objects.ClickType;
import dk.tandhjulet.image.objects.Direction;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.DamageSource;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.EntityHuman;
import net.minecraft.server.v1_8_R3.EntityItemFrame;
import net.minecraft.server.v1_8_R3.EnumDirection;
import net.minecraft.server.v1_8_R3.World;

public class ImageFrame extends EntityItemFrame {

	public ImageFrame(World world) {
		super(world);
	}

	public ImageFrame(World world, BlockPosition bp, Direction direction) {
		super(world, bp, direction.getNmsDirection());
	}

	public ImageFrame(World world, BlockPosition bp, EnumDirection direction) {
		super(world, bp, direction);
	}

	public void setBukkitEntity(CraftEntity entity) {
		this.bukkitEntity = entity;
	}

	@Override
	public void setRotation(int i) {
	}

	@Override
	// On damage/on left click
	public boolean damageEntity(DamageSource source, float f) {
		handleClick(source.getEntity(), ClickType.LEFT);
		return true;
	}

	@Override
	// normally drops the item inside the frame and optionally (flag) the frame too.
	public void a(Entity entity, boolean flag) {
		return;
	}

	@Override
	// Right click on map handler
	public boolean e(EntityHuman human) {
		handleClick(human, ClickType.RIGHT);
		return true;
	}

	public void handleClick(Entity entity, ClickType clickType) {
		if (!(entity instanceof EntityHuman))
			return;

		CraftHumanEntity player = ((EntityHuman) entity).getBukkitEntity();
		if (!player.isOp())
			return;

		if (player.getItemInHand().getType() != Material.STICK || clickType != ClickType.LEFT) {
			player.sendMessage("Left click with a stick in hand to remove this image!");
			return;
		}

		short data = (short) getItem().getData();
		RenderableImageMap map = MapManager.getRegisteredMap(data);
		if (map == null) {
			player.sendMessage("Could not find image map at this location in memory... Please contact the developer.");
			return;
		}

		map.remove();
	}

	@Override
	// Called every tick. Overriden so that maps dont drop after 5 seconds.
	public void t_() {
		this.lastX = this.locX;
		this.lastY = this.locY;
		this.lastZ = this.locZ;
	}
}
