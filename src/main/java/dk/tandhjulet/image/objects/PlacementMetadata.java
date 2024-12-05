package dk.tandhjulet.image.objects;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import dk.tandhjulet.image.utils.CuboidRegion;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
public class PlacementMetadata {
	private static HashMap<UUID, PlacementMetadata> placementData = new HashMap<>();

	@Getter
	@Setter
	private Location pos1, pos2;

	@Getter
	private final String imageFileName;

	public CuboidRegion getRegion() {
		if (!isBothPointsSet())
			return null;
		return new CuboidRegion(pos1, pos2);
	}

	public boolean isBothPointsSet() {
		return pos1 != null && pos2 != null;
	}

	public static PlacementMetadata get(Player player) {
		return placementData.get(player.getUniqueId());
	}

	public static PlacementMetadata set(Player player, PlacementMetadata to) {
		return placementData.put(player.getUniqueId(), to);
	}

	public static PlacementMetadata remove(Player player) {
		return placementData.remove(player.getUniqueId());
	}

	public static boolean isActive(Player player) {
		return placementData.containsKey(player.getUniqueId());
	}

	public static PlacementMetadata create(Player player, String imageFileName) {
		return set(player, new PlacementMetadata(imageFileName));
	}
}
