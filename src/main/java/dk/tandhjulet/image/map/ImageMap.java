package dk.tandhjulet.image.map;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import dk.tandhjulet.image.transformer.Transformer;
import dk.tandhjulet.image.utils.CuboidRegion;
import lombok.Getter;

public class ImageMap {
	@Getter
	private final File imageFile;

	@Getter
	private final int width, height;

	@Getter
	private final boolean undersized;

	public ImageMap(File imageFile) throws IOException {
		this.imageFile = imageFile;

		// Optimize-able, but this generally only runs on load so it's pretty low
		// priority.
		BufferedImage image = ImageIO.read(imageFile);

		boolean undersizedX = image.getWidth() % RenderableImageMap.MAP_WIDTH > 0;
		boolean undersizedY = image.getHeight() % RenderableImageMap.MAP_HEIGHT > 0;

		width = (int) Math.ceil(image.getWidth() / RenderableImageMap.MAP_WIDTH) + (undersizedX ? 1 : 0);
		height = (int) Math.ceil(image.getHeight() / RenderableImageMap.MAP_HEIGHT) + (undersizedY ? 1 : 0);

		this.undersized = (undersizedX || undersizedY);

		image.flush();
	}

	public RenderableImageMap getRenderable(CuboidRegion region) throws IOException {
		return new RenderableImageMap(getImageFile(), region);
	}

	public RenderableImageMap getRenderable(CuboidRegion region, List<Transformer> transformers) throws IOException {
		RenderableImageMap map = getRenderable(region);
		map.applyTransformers(transformers);

		return map;
	}

	public boolean hasSameDimensions(int width, int height) {
		return this.width == width && this.height == height;
	}

	public boolean canScaleCleanly(int width, int height) {
		return (double) width / height == (double) this.width / this.height;
	}
}
