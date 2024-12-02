package dk.tandhjulet.image.transformer;

import java.awt.geom.AffineTransform;
import java.util.ArrayList;

import dk.tandhjulet.image.map.RenderableImageMap;
import dk.tandhjulet.image.transformer.transformers.ImageCoverTransformer;
import dk.tandhjulet.image.transformer.transformers.ImageInvertXTransformer;
import dk.tandhjulet.image.transformer.transformers.ImageInvertYTransformer;
import dk.tandhjulet.image.transformer.transformers.ImageStretchTransformer;

public enum Transformer {
	INVERT_X,
	INVERT_Y,

	STRETCH,
	COVER;

	private ArrayList<ImageTransformer> imageTransformers = new ArrayList<>();

	public boolean apply(RenderableImageMap image) {
		if (image.isImagesSplit())
			return false;

		AffineTransform transform = new AffineTransform();

		imageTransformers.forEach((transformer) -> {
			transformer.apply(transform, image);
		});

		return true;
	}

	public void register(ImageTransformer transformer) {
		imageTransformers.add(transformer);
	}

	public static void register() {
		COVER.register(new ImageCoverTransformer());
		STRETCH.register(new ImageStretchTransformer());
		INVERT_X.register(new ImageInvertXTransformer());
		INVERT_Y.register(new ImageInvertYTransformer());
	}
}
