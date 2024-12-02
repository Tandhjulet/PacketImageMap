package dk.tandhjulet.image.map;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class ImageMap {
	@Getter
	private final BufferedImage image;

	public RenderableImageMap getRenderable() {
		return new RenderableImageMap(cloneImage());
	}

	private BufferedImage cloneImage() {
		ColorModel model = image.getColorModel();
		boolean isAlphaPremult = model.isAlphaPremultiplied();
		WritableRaster raster = image.copyData(null);
		return new BufferedImage(model, raster, isAlphaPremult, null);
	}
}
