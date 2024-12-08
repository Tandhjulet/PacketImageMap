package dk.tandhjulet.image.config;

import java.util.ArrayList;
import java.util.List;

import dk.tandhjulet.image.map.RenderableImageMap;
import eu.okaeri.configs.OkaeriConfig;
import lombok.Getter;

public class ImageConfig extends OkaeriConfig {
	@Getter
	private List<RenderableImageMap> images = new ArrayList<>();
}
