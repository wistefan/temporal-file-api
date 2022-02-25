package org.fiware.tfa.configuration;

import io.micronaut.context.annotation.ConfigurationProperties;
import lombok.Data;

/**
 * Use the local disk as storage. Not recommended for production use.
 */
@ConfigurationProperties("local")
@Data
public class LocalStorageProperties {

	/**
	 * Should local storage be enabled.
	 */
	private boolean enabled = false;
	/**
	 * The folder to store the files at.
	 */
	private String baseFolder = "/responses";

}

