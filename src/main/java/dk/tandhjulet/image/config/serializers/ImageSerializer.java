package dk.tandhjulet.image.config.serializers;

import org.bukkit.Location;

import dk.tandhjulet.image.map.RenderableImageMap;
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

	}

	@Override
	public RenderableImageMap deserialize(@NonNull DeserializationData data, @NonNull GenericsDeclaration generics) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'deserialize'");
	}

}
