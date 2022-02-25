package org.fiware.tfa.service;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import lombok.RequiredArgsConstructor;
import org.fiware.ngsi.api.TemporalApi;
import org.fiware.ngsi.api.TemporalApiClient;
import org.fiware.ngsi.model.QueryVO;
import org.fiware.ngsi.model.TimerelVO;
import org.fiware.tfa.exception.ProblemDetails;
import org.fiware.tfa.storage.LocalStorageFileService;

import javax.annotation.Nullable;
import javax.inject.Singleton;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Singleton
@RequiredArgsConstructor
public class TemporalQueryFileService implements TemporalApi {

	private final TemporalApiClient temporalApiClient;
	private final LocalStorageFileService localStorageFileService;

	@Override
	public HttpResponse<Object> queryTemporalEntities(@Nullable String link, @Nullable String id, @Nullable String idPattern, @Nullable String type, @Nullable String attrs, @Nullable String q, @Nullable String georel, @Nullable String geometry, @Nullable String coordinates, @Nullable String geoproperty, @Nullable TimerelVO timerel, @Nullable String timeproperty, @Nullable Instant timeAt, @Nullable Instant endTimeAt, @Nullable String csf, @Nullable Integer pageSize, @Nullable URI pageAnchor, @Nullable Integer limit, @Nullable String options, @Nullable Integer lastN, @Nullable String ngSILDTenant, @Nullable Boolean fileResponse) {
		String requestId = UUID.randomUUID().toString();

		HttpResponse<Object> temporalApiResponse = temporalApiClient.queryTemporalEntities(link, id, idPattern, type, attrs, q, georel, geometry, coordinates, geoproperty, timerel, timeproperty, timeAt, endTimeAt, csf, pageSize, pageAnchor, limit, options, lastN, ngSILDTenant, null);
		switch (temporalApiResponse.getStatus()) {
			case OK -> {
				localStorageFileService.startFile(requestId, temporalApiResponse.body());
				return HttpResponse.noContent().header("Location", requestId);
			}
			case PARTIAL_CONTENT -> {
				localStorageFileService.startFile(requestId, temporalApiResponse.body());
				String contentRange = temporalApiResponse.getHeaders().get("Content-Range");
				Range newRange = getRangeFromHeader(contentRange);
				return queryTemporalEntitiesNextBatch(requestId, link, id, idPattern, type, attrs, q, georel, geometry, coordinates, geoproperty, timerel, timeproperty, newRange.start().toInstant(ZoneOffset.UTC), newRange.end().toInstant(ZoneOffset.UTC), csf, pageSize, pageAnchor, limit, options, lastN, ngSILDTenant, null);
			}
			default -> {
				ProblemDetails problemDetails = new ProblemDetails();
				problemDetails.setDetail("Was not able to get a valid response from the downstream api.");
				problemDetails.setTitle("Bad Gateway.");
				problemDetails.setStatus(HttpStatus.BAD_GATEWAY.getCode());
				problemDetails.setInstance(id);
				return HttpResponse.status(HttpStatus.BAD_GATEWAY).body(problemDetails);
			}
		}
	}

	private HttpResponse<Object> queryTemporalEntitiesNextBatch(String requestId, @Nullable String link, @Nullable String id, @Nullable String idPattern, @Nullable String type, @Nullable String attrs, @Nullable String q, @Nullable String georel, @Nullable String geometry, @Nullable String coordinates, @Nullable String geoproperty, @Nullable TimerelVO timerel, @Nullable String timeproperty, @Nullable Instant timeAt, @Nullable Instant endTimeAt, @Nullable String csf, @Nullable Integer pageSize, @Nullable URI pageAnchor, @Nullable Integer limit, @Nullable String options, @Nullable Integer lastN, @Nullable String ngSILDTenant, @Nullable Boolean fileResponse) {

		HttpResponse<Object> temporalApiResponse = temporalApiClient.queryTemporalEntities(link, id, idPattern, type, attrs, q, georel, geometry, coordinates, geoproperty, timerel, timeproperty, timeAt, endTimeAt, csf, pageSize, pageAnchor, limit, options, lastN, ngSILDTenant, null);
		localStorageFileService.appendFile(requestId, temporalApiResponse.getBody());
		switch (temporalApiResponse.getStatus()) {
			case OK -> {
				localStorageFileService.appendFile(requestId, temporalApiResponse.body());
				return HttpResponse.noContent().header("Location", requestId);
			}
			case PARTIAL_CONTENT -> {
				localStorageFileService.appendFile(requestId, temporalApiResponse.body());
				String contentRange = temporalApiResponse.getHeaders().get("Content-Range");
				Range newRange = getRangeFromHeader(contentRange);
				return queryTemporalEntitiesNextBatch(requestId, link, id, idPattern, type, attrs, q, georel, geometry, coordinates, geoproperty, timerel, timeproperty, newRange.start().toInstant(ZoneOffset.UTC), newRange.end().toInstant(ZoneOffset.UTC), csf, pageSize, pageAnchor, limit, options, lastN, ngSILDTenant, null);
			}
			default -> {
				ProblemDetails problemDetails = new ProblemDetails();
				problemDetails.setDetail("Was not able to get a valid response from the downstream api.");
				problemDetails.setTitle("Bad Gateway.");
				problemDetails.setStatus(HttpStatus.BAD_GATEWAY.getCode());
				problemDetails.setInstance(id);
				return HttpResponse.status(HttpStatus.BAD_GATEWAY).body(problemDetails);
			}
		}
	}

	private Range getRangeFromHeader(String rangeHeader) {
		if (rangeHeader == null) {
			throw new RuntimeException("Did not receive the expected range header.");
		}
		String[] firstSplit = rangeHeader.split(" ");
		if (firstSplit.length != 2) {
			throw new RuntimeException("Did not receive a valid range header.");
		}
		String[] secondSplit = firstSplit[1].split("/");
		if (secondSplit.length != 2) {
			throw new RuntimeException("Did not receive a valid range header.");
		}
		String[] lastSplit = secondSplit[0].split("-");
		// YYYY-MM-DD....-YYYY-MM-DD....
		if (secondSplit.length != 6) {
			throw new RuntimeException("Did not receive a valid range header.");
		}
		String rangeStart = String.format("%s-%s-%s", lastSplit[0], lastSplit[1], lastSplit[2]);
		String rangeEnd = String.format("%s-%s-%s", lastSplit[3], lastSplit[4], lastSplit[5]);
		LocalDateTime startTime = LocalDateTime.parse(rangeStart);
		LocalDateTime endTime = LocalDateTime.parse(rangeEnd);
		return new Range(startTime, endTime);
	}

	@Override
	public HttpResponse<Object> queryTemporalEntitiesOnPost(QueryVO queryVO, @Nullable String link, @Nullable Integer pageSize, @Nullable URI pageAnchor, @Nullable Integer limit, @Nullable String options, @Nullable Integer
			lastN, @Nullable String ngSILDTenant, @Nullable Boolean fileResponse) {
		return null;
	}

	@Override
	public HttpResponse<Object> retrieveEntityTemporalById(URI entityId, @Nullable String link, @Nullable String attrs, @Nullable String options, @Nullable TimerelVO timerel, @Nullable String timeproperty, @Nullable Instant
			timeAt, @Nullable Instant endTimeAt, @Nullable Integer lastN, @Nullable String ngSILDTenant, @Nullable Boolean fileResponse) {
		return null;
	}
}

