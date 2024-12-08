package dk.tandhjulet.image.config.serializers;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import dk.tandhjulet.image.map.RenderableImageMap;
import dk.tandhjulet.image.transformer.Transformer;
import dk.tandhjulet.image.utils.CuboidRegion;
import dk.tandhjulet.image.utils.LocationUtils;
import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import lombok.NonNull;

public class ImageSerializer implements ObjectSerializer<RenderableImageMap> {

	@Override
	public boolean supports(@NonNull Class<? super RenderableImageMap> type) {
		return RenderableImageMap.class.isAssignableFrom(type);
	}

	@Override
	public void serialize(@NonNull RenderableImageMap image, @NonNull SerializationData data,
			@NonNull GenericsDeclaration generics) {
		data.add("pos1", image.getRegion().getPos1(), Location.class);
		data.add("pos2", image.getRegion().getPos2(), Location.class);

		data.add("image-file", image.getImageFile().getAbsolutePath());
		data.add("transforms", image.getTransforms());

		data.addArray("map-ids", image.getMapIds(), Short.class);
	}

	@Override
	public RenderableImageMap deserialize(@NonNull DeserializationData data, @NonNull GenericsDeclaration generics) {
		Location pos1 = data.get("pos1", Location.class);
		Location pos2 = data.get("pos2", Location.class);
		CuboidRegion region = new CuboidRegion(pos1, pos2);

		String imageFilePath = data.get("image-file", String.class);
		File imageFile = new File(imageFilePath);

		RenderableImageMap image;
		try {
			image = new RenderableImageMap(imageFile, region);

			List<Transformer> transforms = data.getAsList("transforms", Transformer.class);
			image.applyTransformers(transforms);
		} catch (IOException e) {
			Bukkit.getLogger().severe("Failed to load image at " + LocationUtils.stringify(pos1));

			e.printStackTrace();
			return null;
		}

		Short[] mapIds = data.getAsList("map-ids", Short.class)
				.toArray(new Short[image.getWidth() * image.getHeight()]);
		image.setMapIds(mapIds);

		return image;
	}

}
