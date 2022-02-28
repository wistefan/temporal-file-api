package org.fiware.tfa.rest;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Produces;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fiware.tfa.exception.ProblemDetails;
import org.fiware.tfa.storage.LocalStorageFileService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Controller
@RequiredArgsConstructor
@Requires(property = "local.enabled", value = "true")
public class FileDownloadController {

	private final LocalStorageFileService localStorageFileService;

	@Get("/file/{fileId}")
	@Produces({MediaType.MULTIPART_FORM_DATA})
	public HttpResponse<Object> getFileById(@PathVariable(name = "fileId") String fileId) {
		Path localPath = localStorageFileService.getFilePath(fileId);
		if (!Files.exists(localPath)) {
			return HttpResponse.notFound().contentType(MediaType.APPLICATION_JSON).body(new ProblemDetails(
					"https://uri.etsi.org/ngsi-ld/errors/ResourceNotFound",
					"The referred resource has not been found",
					404,
					String.format("No file exists at %s", localPath.toString()),
					fileId));
		}

		try {
			return HttpResponse.ok().contentType(MediaType.MULTIPART_FORM_DATA).body(Files.readAllBytes(localPath));
		} catch (IOException e) {
			log.warn("Was not able to read file {}.", localPath, e);
			return HttpResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(
					new ProblemDetails(
							"https://uri.etsi.org/ngsi-ld/errors/InternalError",
							"There has been an error during the operation execution",
							500,
							String.format("Was not able to read file %s.", localPath.toString()),
							fileId)
			);
		}
	}
}
