package dk.tandhjulet.image.config;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;

import dk.tandhjulet.image.objects.Direction;
import dk.tandhjulet.image.transformer.Transformer;
import eu.okaeri.configs.OkaeriConfig;
import lombok.Getter;

public class ImageConfig extends OkaeriConfig {
	@Getter
	private List<Image> images = new ArrayList<>();

	public class Image extends OkaeriConfig {
		@Getter
		private String imagePath;

		@Getter
		private Location pos1, pos2;

		@Getter
		private Direction frameDirection;

		@Getter
		private List<Transformer> transforms;
	}
}
