package org.fiware.tfa.exception;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;

@Slf4j
@Produces
@Singleton
@Requires(classes = {StorageException.class, ExceptionHandler.class})
public class StorageExceptionHandler implements ExceptionHandler<StorageException, HttpResponse<ProblemDetails>> {

	@Override
	public HttpResponse<ProblemDetails> handle(HttpRequest request, StorageException exception) {
		log.warn("Error when accessing storage: {} - {}.", exception.getStorageObjectId(), exception.getMessage());
		log.debug("Full stack trace for: {}.", exception.getStorageObjectId(), exception);
		return HttpResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(
						new ProblemDetails(
								"https://uri.etsi.org/ngsi-ld/errors/InternalError",
								"There has been an error during the operation execution",
								500,
								exception.getMessage(),
								exception.getStorageObjectId()));
	}
}
