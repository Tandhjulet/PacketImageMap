package dk.tandhjulet.image.map;

import java.awt.image.BufferedImage;

import dk.tandhjulet.image.utils.CuboidRegion;
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

	public RenderableImageMap getRenderable(CuboidRegion region) {
		return new RenderableImageMap(getImage(), region);
	}

	public boolean hasSameDimensions(int width, int height) {
		return this.width == width && this.height == height;
	}

	public boolean canScaleCleanly(int width, int height) {
		return (double) width / height == (double) this.width / this.height;
	}
}
