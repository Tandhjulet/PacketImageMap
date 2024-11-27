package dk.tandhjulet.image.objects;

import org.apache.commons.lang3.EnumUtils;

public enum FileExtension {
	PNG;

	public static FileExtension getExtension(String fileName) {
		if (fileName == null)
			return null;

		String[] splitByDot = fileName.split("\\.");
		String extension = splitByDot[splitByDot.length - 1].toUpperCase();
		if (EnumUtils.isValidEnum(FileExtension.class, extension))
			return FileExtension.valueOf(extension);
		return null;
	}
}
