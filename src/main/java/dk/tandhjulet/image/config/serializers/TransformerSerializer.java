package dk.tandhjulet.image.config.serializers;

import dk.tandhjulet.image.transformer.Transformer;
import eu.okaeri.configs.schema.GenericsPair;
import eu.okaeri.configs.serdes.BidirectionalTransformer;
import eu.okaeri.configs.serdes.SerdesContext;
import lombok.NonNull;

public class TransformerSerializer extends BidirectionalTransformer<String, Transformer> {

	@Override
	public GenericsPair<String, Transformer> getPair() {
		return genericsPair(String.class, Transformer.class);
	}

	@Override
	public Transformer leftToRight(@NonNull String data, @NonNull SerdesContext serdesContext) {
		return Transformer.from(data);
	}

	@Override
	public String rightToLeft(@NonNull Transformer data, @NonNull SerdesContext serdesContext) {
		return data.name();
	}

}
