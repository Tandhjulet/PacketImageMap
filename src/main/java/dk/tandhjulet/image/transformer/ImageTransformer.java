package dk.tandhjulet.image.transformer;

import java.awt.geom.AffineTransform;

import dk.tandhjulet.image.map.RenderableImageMap;

public abstract class ImageTransformer {
	public abstract void apply(AffineTransform transform, RenderableImageMap image);

	public abstract Transformer getCategory();

	public void register() {
		getCategory().register(this);
	}
}
