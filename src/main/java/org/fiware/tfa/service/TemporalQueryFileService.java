package org.fiware.tfa.service;

import com.fasterxml.jackson.annotation.OptBoolean;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.reactivex.functions.Function3;
import lombok.RequiredArgsConstructor;
import org.fiware.ngsi.api.TemporalApi;
import org.fiware.ngsi.api.TemporalApiClient;
import org.fiware.ngsi.model.QueryVO;
import org.fiware.ngsi.model.TemporalQueryVO;
import org.fiware.ngsi.model.TimerelVO;
import org.fiware.tfa.exception.ProblemDetails;
import org.fiware.tfa.storage.FileService;

import javax.annotation.Nullable;
import javax.inject.Singleton;
import java.net.URI;
import java.nio.file.LinkOption;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

@Singleton
@RequiredArgsConstructor
public class TemporalQueryFileService implements TemporalApi {

	private final TemporalApiClient temporalApiClient;
	private final FileService fileService;

	@Override
	public HttpResponse<Object> queryTemporalEntities(@Nullable String link, @Nullable String id, @Nullable String idPattern, @Nullable String type, @Nullable String attrs, @Nullable String q, @Nullable String georel, @Nullable String geometry, @Nullable String coordinates, @Nullable String geoproperty, @Nullable TimerelVO timerel, @Nullable String timeproperty, @Nullable Instant timeAt, @Nullable Instant endTimeAt, @Nullable String csf, @Nullable Integer pageSize, @Nullable URI pageAnchor, @Nullable Integer limit, @Nullable String options, @Nullable Integer lastN, @Nullable String ngSILDTenant, @Nullable Boolean fileResponse) {
		String requestId = UUID.randomUUID().toString();
		Optional<Instant> optionalTimeAt = Optional.ofNullable(timeAt);
		Optional<Instant> optionalEndTimeAt = Optional.ofNullable(endTimeAt);
		return queryInBatch((reqId, timeStart, timeEnd) -> temporalApiClient.queryTemporalEntities(link, id, idPattern, type, attrs, q, georel, geometry, coordinates, geoproperty, timerel, timeproperty, optionalTimeAt.orElse(null), optionalEndTimeAt.orElse(null), csf, pageSize, pageAnchor, limit, options, lastN, ngSILDTenant, null), requestId, optionalTimeAt.map(i -> LocalDateTime.ofInstant(timeAt, ZoneId.of("UTC"))), optionalEndTimeAt.map(i -> LocalDateTime.ofInstant(endTimeAt, ZoneId.of("UTC"))));
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

		String requestId = UUID.randomUUID().toString();

		Optional<LocalDateTime> optionalTimeAt = Optional.ofNullable(queryVO.getTemporalQ()).map(TemporalQueryVO::timeAt).map(i -> LocalDateTime.ofInstant(i, ZoneId.of("UTC")));
		Optional<LocalDateTime> optionalEndTimeAt = Optional.ofNullable(queryVO.getTemporalQ()).map(TemporalQueryVO::endTimeAt).map(i -> LocalDateTime.ofInstant(i, ZoneId.of("UTC")));

		return queryInBatch((reqId, timeStart, timeEnd) -> {
			TemporalQueryVO temporalQueryVO = Optional.ofNullable(queryVO.getTemporalQ()).orElse(new TemporalQueryVO());
			temporalQueryVO.timeAt(timeStart.map(ta -> ta.toInstant(ZoneOffset.UTC)).orElse(null));
			temporalQueryVO.endTimeAt(timeEnd.map(ta -> ta.toInstant(ZoneOffset.UTC)).orElse(null));
			queryVO.temporalQ(temporalQueryVO);
			return temporalApiClient.queryTemporalEntitiesOnPost(queryVO, link, pageSize, pageAnchor, limit, options, lastN, ngSILDTenant, null);
		}, requestId, optionalTimeAt, optionalEndTimeAt);
	}

	@Override
	public HttpResponse<Object> retrieveEntityTemporalById(URI entityId, @Nullable String link, @Nullable String attrs, @Nullable String options, @Nullable TimerelVO timerel, @Nullable String timeproperty, @Nullable Instant
			timeAt, @Nullable Instant endTimeAt, @Nullable Integer lastN, @Nullable String ngSILDTenant, @Nullable Boolean fileResponse) {

		String requestId = UUID.randomUUID().toString();
		Optional<Instant> optionalTimeAt = Optional.ofNullable(timeAt);
		Optional<Instant> optionalEndTimeAt = Optional.ofNullable(endTimeAt);

		return queryInBatch(
				(reqId, timeStart, timeEnd) -> temporalApiClient.retrieveEntityTemporalById(entityId, link, attrs, options, timerel, timeproperty, optionalTimeAt.orElse(null), optionalEndTimeAt.orElse(null), lastN, ngSILDTenant, null),
				requestId,
				optionalTimeAt.map(i -> LocalDateTime.ofInstant(timeAt, ZoneId.of("UTC"))),
				optionalEndTimeAt.map(i -> LocalDateTime.ofInstant(endTimeAt, ZoneId.of("UTC"))));
	}

	private HttpResponse<Object> queryInBatch(Function3<String, Optional<LocalDateTime>, Optional<LocalDateTime>, HttpResponse<Object>> queryFunction, String
			requestId, Optional<LocalDateTime> startTime, Optional<LocalDateTime> endTime) {
		try {
			HttpResponse<Object> temporalApiResponse = queryFunction.apply(requestId, startTime, endTime);

			switch (temporalApiResponse.getStatus()) {
				case OK -> {
					fileService.appendToFile(requestId, temporalApiResponse.body());
					return HttpResponse.noContent().header("Location", fileService.getLocation(requestId).toString());
				}
				case PARTIAL_CONTENT -> {
					fileService.appendToFile(requestId, temporalApiResponse.body());
					String contentRange = temporalApiResponse.getHeaders().get("Content-Range");
					Range newRange = getRangeFromHeader(contentRange);
					return queryInBatch(queryFunction, requestId, Optional.ofNullable(newRange.start()), Optional.ofNullable(newRange.end()));
				}
				default -> {
					ProblemDetails problemDetails = new ProblemDetails();
					problemDetails.setDetail("Was not able to get a valid response from the downstream api.");
					problemDetails.setTitle("Bad Gateway.");
					problemDetails.setStatus(HttpStatus.BAD_GATEWAY.getCode());
					problemDetails.setInstance(requestId);
					return HttpResponse.status(HttpStatus.BAD_GATEWAY).body(problemDetails);
				}
			}
		} catch (Exception e) {
			ProblemDetails problemDetails = new ProblemDetails(
					"https://uri.etsi.org/ngsi-ld/errors/InternalError",
					"There has been an error during the operation execution",
					500,
					e.getMessage(),
					requestId);
			return HttpResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetails);
		}
	}
}

