package dk.tandhjulet.image.transformer.transformers;

import java.awt.geom.AffineTransform;

import dk.tandhjulet.image.map.RenderableImageMap;
import dk.tandhjulet.image.transformer.ImageTransformer;
import dk.tandhjulet.image.transformer.Transformer;

public class ImageStretchTransformer extends ImageTransformer {

	@Override
	public void apply(AffineTransform transform, RenderableImageMap image) {
		int newWidth = image.getWidth() * RenderableImageMap.MAP_WIDTH;
		int newHeight = image.getHeight() * RenderableImageMap.MAP_HEIGHT;

		transform.scale(newWidth / image.getImageWidth(), newHeight / image.getImageHeight());
	}

	@Override
	public Transformer getCategory() {
		return Transformer.STRETCH;
	}

}
