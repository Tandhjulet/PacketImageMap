package dk.tandhjulet.image.map;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;

import lombok.Getter;

public class ImageMap {
	@Getter
	private final BufferedImage image;

	@Getter
	private final int width, height;

	@Getter
	private final boolean undersized;

	public ImageMap(BufferedImage image) {
		this.image = image;

		boolean undersizedX = image.getWidth() % RenderableImageMap.MAP_WIDTH > 0;
		boolean undersizedY = image.getHeight() % RenderableImageMap.MAP_HEIGHT > 0;

		width = (int) Math.ceil(image.getWidth() / RenderableImageMap.MAP_WIDTH) + (undersizedX ? 1 : 0);
		height = (int) Math.ceil(image.getHeight() / RenderableImageMap.MAP_HEIGHT) + (undersizedY ? 1 : 0);

		this.undersized = (undersizedX || undersizedY);
	}

	public RenderableImageMap getRenderable() {
		return new RenderableImageMap(cloneImage());
	}

	private BufferedImage cloneImage() {
		ColorModel model = image.getColorModel();
		boolean isAlphaPremult = model.isAlphaPremultiplied();
		WritableRaster raster = image.copyData(null);
		return new BufferedImage(model, raster, isAlphaPremult, null);
	}

	public boolean hasSameDimensions(int width, int height) {
		return this.width == width && this.height == height;
	}

	public boolean canScaleCleanly(int width, int height) {
		return width / height == this.width / this.height;
	}
}
