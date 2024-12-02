package dk.tandhjulet.image.transformer.transformers;

import java.awt.geom.AffineTransform;

import dk.tandhjulet.image.map.RenderableImageMap;
import dk.tandhjulet.image.transformer.ImageTransformer;
import dk.tandhjulet.image.transformer.Transformer;

public class ImageInvertXTransformer extends ImageTransformer {

	@Override
	public void apply(AffineTransform transform, RenderableImageMap image) {

	}

	@Override
	public Transformer getCategory() {
		return Transformer.INVERT_X;
	}

}
