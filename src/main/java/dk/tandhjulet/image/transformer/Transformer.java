package dk.tandhjulet.image.transformer;

import java.awt.geom.AffineTransform;
import java.util.ArrayList;

import org.apache.commons.lang3.EnumUtils;

import dk.tandhjulet.image.map.RenderableImageMap;
import dk.tandhjulet.image.transformer.transformers.ImageCoverTransformer;
import dk.tandhjulet.image.transformer.transformers.ImageInvertXTransformer;
import dk.tandhjulet.image.transformer.transformers.ImageInvertYTransformer;
import dk.tandhjulet.image.transformer.transformers.ImageStretchTransformer;
import lombok.NonNull;

public enum Transformer {
	INVERT_X,
	INVERT_Y,

	STRETCH,
	COVER;

	private ArrayList<ImageTransformer> imageTransformers = new ArrayList<>();

	public AffineTransform apply(@NonNull AffineTransform transform, @NonNull RenderableImageMap image) {
		if (image.isImagesSplit())
			return transform;

		imageTransformers.forEach((transformer) -> {
			transformer.apply(transform, image);
		});

		return transform;
	}

	public static Transformer from(String str) {
		boolean isValid = EnumUtils.isValidEnum(Transformer.class, str.toUpperCase());
		if (!isValid)
			return null;

		return Transformer.valueOf(str.toUpperCase());
	}

	public void register(ImageTransformer transformer) {
		imageTransformers.add(transformer);
	}

	static {
		new ImageCoverTransformer().register();
		new ImageStretchTransformer().register();
		new ImageInvertXTransformer().register();
		new ImageInvertYTransformer().register();
	}
}
