package dk.tandhjulet.image.config.serializers;

import dk.tandhjulet.image.objects.Direction;
import eu.okaeri.configs.schema.GenericsPair;
import eu.okaeri.configs.serdes.BidirectionalTransformer;
import eu.okaeri.configs.serdes.SerdesContext;
import lombok.NonNull;

public class DirectionSerializer extends BidirectionalTransformer<String, Direction> {

	@Override
	public GenericsPair<String, Direction> getPair() {
		return genericsPair(String.class, Direction.class);
	}

	@Override
	public Direction leftToRight(@NonNull String data, @NonNull SerdesContext serdesContext) {
		return Direction.from(data);
	}

	@Override
	public String rightToLeft(@NonNull Direction data, @NonNull SerdesContext serdesContext) {
		return data.name();
	}

}
