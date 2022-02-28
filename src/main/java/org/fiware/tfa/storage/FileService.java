package org.fiware.tfa.storage;

import java.net.URL;

public interface FileService {

	void appendToFile(String path, Object object);

	URL getLocation(String id);

}
