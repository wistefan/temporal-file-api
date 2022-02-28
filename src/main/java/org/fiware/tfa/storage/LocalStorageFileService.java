package org.fiware.tfa.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.annotation.Requires;
import lombok.RequiredArgsConstructor;
import org.fiware.tfa.configuration.LocalStorageProperties;
import org.fiware.tfa.exception.StorageException;

import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

@Singleton
@RequiredArgsConstructor
@Requires(property = "local.enabled", value = "true")
public class LocalStorageFileService implements FileService {


	public static final String FILENAME_TEMPLATE = "%s/%s";
	private static final String LOCATION_TEMPLATE = "%sfile/%s";

	private final LocalStorageProperties localStorageProperties;
	private final ObjectMapper objectMapper;

	@Override
	public void appendToFile(String fileName, Object object) {
		if (Files.exists(getFilePath(fileName))) {
			try {
				Object currentFile = objectMapper.readValue(getFilePath(fileName).toFile(), Object.class);
				Object updatedObject = objectMapper.readerForUpdating(currentFile).readValue(objectMapper.writeValueAsString(object));
				objectMapper.writeValue(getFilePath(fileName).toFile(), updatedObject);
			} catch (IOException e) {
				throw new StorageException(String.format("Was not able to merge object into local file %s.", fileName), e, fileName);
			}
		} else {
			try {
				Files.createFile(getFilePath(fileName));
				objectMapper.writeValue(getFilePath(fileName).toFile(), object);
			} catch (IOException e) {
				throw new StorageException(String.format("Was not able to start file at %s in local storage.", fileName), e, fileName);
			}
		}

	}

	@Override
	public URL getLocation(String id) {
		try {
			return new URL(String.format(LOCATION_TEMPLATE, localStorageProperties.getBaseAddress(), id));
		} catch (MalformedURLException e) {
			throw new StorageException("Was not able to create download address.", e, id);
		}
	}

	public Path getFilePath(String filename) {
		return Path.of(String.format(FILENAME_TEMPLATE, localStorageProperties.getBaseFolder(), filename));
	}
}

