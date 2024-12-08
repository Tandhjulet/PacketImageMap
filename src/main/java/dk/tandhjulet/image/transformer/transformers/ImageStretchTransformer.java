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

		double widthRatio = (double) newWidth / image.getImageWidth();
		double heightRatio = (double) newHeight / image.getImageHeight();

		transform.scale(widthRatio, heightRatio);

		image.setInsertY(0);
		image.setInsertX(0);

		image.setScaledHeight(newHeight);
		image.setScaledWidth(newWidth);
	}

	@Override
	public Transformer getCategory() {
		return Transformer.STRETCH;
	}

}
