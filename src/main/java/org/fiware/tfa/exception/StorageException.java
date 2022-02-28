package org.fiware.tfa.exception;

import lombok.Getter;

public class StorageException extends RuntimeException{

	@Getter
	public final String storageObjectId;


	public StorageException(String message, String storageObjectId) {
		super(message);
		this.storageObjectId = storageObjectId;
	}

	public StorageException(String message, Throwable cause, String storageObjectId) {
		super(message, cause);
		this.storageObjectId = storageObjectId;
	}
}
