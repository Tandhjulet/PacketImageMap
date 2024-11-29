package dk.tandhjulet.image.objects;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import lombok.Getter;
import lombok.Setter;

public enum PlacementMetadata {
	INACTIVE,
	ACTIVE;

	private static HashMap<UUID, PlacementMetadata> metadatas = new HashMap<>();

	@Getter
	@Setter
	private Location pos1, pos2;

	public static PlacementMetadata get(Player player) {
		return metadatas.get(player.getUniqueId());
	}

	public static PlacementMetadata set(Player player, PlacementMetadata to) {
		return metadatas.put(player.getUniqueId(), to);
	}

	public static PlacementMetadata remove(Player player) {
		return metadatas.remove(player.getUniqueId());
	}
}
