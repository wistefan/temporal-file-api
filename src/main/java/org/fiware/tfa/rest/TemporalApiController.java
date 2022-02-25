package org.fiware.tfa.rest;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import lombok.RequiredArgsConstructor;
import org.fiware.ngsi.api.TemporalApi;
import org.fiware.ngsi.api.TemporalApiClient;
import org.fiware.ngsi.model.QueryVO;
import org.fiware.ngsi.model.TimerelVO;

import javax.annotation.Nullable;
import java.net.URI;
import java.time.Instant;

@Controller
@RequiredArgsConstructor
public class TemporalApiController implements TemporalApi {

	private final TemporalApiClient temporalApiClient;

	@Override
	public HttpResponse<Object> queryTemporalEntities(@Nullable String link, @Nullable String id, @Nullable String idPattern, @Nullable String type, @Nullable String attrs, @Nullable String q, @Nullable String georel, @Nullable String geometry, @Nullable String coordinates, @Nullable String geoproperty, @Nullable TimerelVO timerel, @Nullable String timeproperty, @Nullable Instant timeAt, @Nullable Instant endTimeAt, @Nullable String csf, @Nullable Integer pageSize, @Nullable URI pageAnchor, @Nullable Integer limit, @Nullable String options, @Nullable Integer lastN, @Nullable String ngSILDTenant, @Nullable Boolean fileResponse) {
		if (fileResponse) {
			return HttpResponse.noContent().header("Location", URI.create("").toString());
		} else {
			return temporalApiClient.queryTemporalEntities(link, id, idPattern, type, attrs, q, georel, geometry, coordinates, geoproperty, timerel, timeproperty, timeAt, endTimeAt, csf, pageSize, pageAnchor, limit, options, lastN, ngSILDTenant, null);
		}
	}

	@Override
	public HttpResponse<Object> queryTemporalEntitiesOnPost(QueryVO queryVO, @Nullable String link, @Nullable Integer pageSize, @Nullable URI pageAnchor, @Nullable Integer limit, @Nullable String options, @Nullable Integer lastN, @Nullable String ngSILDTenant, @Nullable Boolean fileResponse) {
		if (fileResponse) {
			return HttpResponse.noContent().header("Location", URI.create("").toString());
		} else {
			return temporalApiClient.queryTemporalEntitiesOnPost(queryVO, link, pageSize, pageAnchor, limit, options, lastN, ngSILDTenant, null);
		}
	}

	@Override
	public HttpResponse<Object> retrieveEntityTemporalById(URI entityId, @Nullable String link, @Nullable String attrs, @Nullable String options, @Nullable TimerelVO timerel, @Nullable String timeproperty, @Nullable Instant timeAt, @Nullable Instant endTimeAt, @Nullable Integer lastN, @Nullable String ngSILDTenant, @Nullable Boolean fileResponse) {
		if (fileResponse) {
			return HttpResponse.noContent().header("Location", URI.create("").toString());
		} else {
			return temporalApiClient.retrieveEntityTemporalById(entityId, link, attrs, options, timerel, timeproperty, timeAt, endTimeAt, lastN, ngSILDTenant, null);
		}
	}
}
