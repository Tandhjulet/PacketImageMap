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

		double widthRatio = (double) newWidth / image.getImageWidth();
		double heightRatio = (double) newHeight / image.getImageHeight();

		// Bukkit.getLogger().info("Width: " + widthRatio + " height: " + heightRatio);

		double maxRatio = Math.max(widthRatio, heightRatio);
		double translateX = 0, translateY = 0;
		if (maxRatio == heightRatio)
			translateX = -((image.getImageWidth() * maxRatio) - image.getImageWidth()) / 2;
		if (maxRatio == widthRatio)
			translateY = -((image.getImageHeight() * maxRatio) - image.getImageHeight()) / 2;

		transform.translate(translateX, translateY);
		transform.scale(maxRatio, maxRatio);

		// Bukkit.getLogger().info("translatedX: " + translateX + " translatedY: " +
		// translateY);

		image.setInsertY(0);
		image.setInsertX(0);

		image.setScaledHeight(newHeight);
		image.setScaledWidth(newWidth);
	}

	@Override
	public Transformer getCategory() {
		return Transformer.COVER;
	}

}
