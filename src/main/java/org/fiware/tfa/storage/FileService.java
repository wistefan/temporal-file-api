package org.fiware.tfa.storage;

public interface FileService {

	void startFile(String path, Object object);

	void appendFile(String path, Object object);
}
