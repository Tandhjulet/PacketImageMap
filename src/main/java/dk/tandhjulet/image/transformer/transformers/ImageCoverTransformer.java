package dk.tandhjulet.image.transformer.transformers;

import java.awt.geom.AffineTransform;

import dk.tandhjulet.image.map.RenderableImageMap;
import dk.tandhjulet.image.transformer.ImageTransformer;
import dk.tandhjulet.image.transformer.Transformer;

public class ImageCoverTransformer extends ImageTransformer {

	@Override
	public void apply(AffineTransform transform, RenderableImageMap image) {
		int newWidth = image.getWidth() * RenderableImageMap.MAP_WIDTH;
		int newHeight = image.getHeight() * RenderableImageMap.MAP_HEIGHT;

		int widthRatio = newWidth / image.getImageWidth();
		int heightRatio = newHeight / image.getImageHeight();
		int maxRatio = Math.max(widthRatio, heightRatio);

		transform.scale(maxRatio, maxRatio);

		image.setInsertX((newWidth - image.getImageWidth()) / 2);
		image.setInsertY((newHeight - image.getImageHeight()) / 2);
	}

	@Override
	public Transformer getCategory() {
		return Transformer.COVER;
	}

}
