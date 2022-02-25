package org.fiware.tfa.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.fiware.tfa.configuration.LocalStorageProperties;
import org.fiware.tfa.exception.StorageException;

import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@RequiredArgsConstructor
@Singleton
public class LocalStorageFileService implements FileService {


	private static final String FILENAME_TEMPLATE = "%s/%s";

	private final LocalStorageProperties localStorageProperties;
	private final ObjectMapper objectMapper;

	@Override
	public void startFile(String fileName, Object object) {
		try {
			Files.createFile(Path.of(localStorageProperties.getBaseFolder() + fileName));
			objectMapper.writeValue(new File(localStorageProperties.getBaseFolder() + fileName), object);
		} catch (IOException e) {
			throw new StorageException(String.format("Was not able to start file at %s in local storage.", fileName), e);
		}
	}

	@Override
	public void appendFile(String fileName, Object object) {
		try {
			Object currentFile = objectMapper.readValue(new File(getFilePath(fileName).toUri()), Object.class);
			Object updatedObject = objectMapper.readerForUpdating(currentFile).readValue(objectMapper.writeValueAsString(object));
			objectMapper.writeValue(new File(getFilePath(fileName).toUri()), updatedObject);
		} catch (IOException e) {
			throw new StorageException(String.format("Was not able to merge object into local file %s.", fileName), e);
		}
	}

	private Path getFilePath(String filename) {
		return Path.of(String.format(localStorageProperties.getBaseFolder(), filename));
	}
}

