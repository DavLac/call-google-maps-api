package fr.dla.app.client.googlemapsapi.impl;

import fr.dla.app.client.googlemapsapi.GoogleMapsRouteClient;
import fr.dla.app.client.googlemapsapi.model.DistanceMatrixResponseEntity;
import fr.dla.app.web.rest.errors.InternalServerErrorException;
import fr.dla.app.web.rest.errors.ProxyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.constraints.Size;

import static fr.dla.app.config.Constants.GOOGLE_API_ENTITY;

@Component
public class GoogleMapsApiDistanceClientImpl implements GoogleMapsRouteClient {

    private final Logger log = LoggerFactory.getLogger(GoogleMapsApiDistanceClientImpl.class);

    private static final String ORIGINS_PARAMETER = "origins";
    private static final String DESTINATIONS_PARAMETER = "destinations";
    private static final String KEY_PARAMETER = "key";
    private static final String GOOGLE_API_EXCEPTION_ERROR_KEY = "googleApiException";

    private final RestTemplate restTemplate;

    @Value("${application.google-maps-api.url}")
    private String endpointUrl;

    @Value("${application.google-maps-api.key}")
    private String apiKey;

    public GoogleMapsApiDistanceClientImpl(@Qualifier("vanillaRestTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public DistanceMatrixResponseEntity getDistanceDetailsBetweenTwoCoordinates(@Size(min = 2, max = 2) String[] origin,
                                                                                @Size(min = 2, max = 2) String[] destination) {

        log.info("Google maps API : get distance between two coordinates. origin={}, destination={}", origin, destination);

        UriComponents requestBuilder = UriComponentsBuilder.fromHttpUrl(endpointUrl)
            .queryParam(ORIGINS_PARAMETER, String.format("%s,%s", origin[0], origin[1]))
            .queryParam(DESTINATIONS_PARAMETER, String.format("%s,%s", destination[0], destination[1]))
            .queryParam(KEY_PARAMETER, apiKey)
            .build();

        log.info("GET request ---> {}", requestBuilder);

        ResponseEntity<DistanceMatrixResponseEntity> responseEntity;

        try {
            responseEntity = restTemplate.getForEntity(requestBuilder.toString(), DistanceMatrixResponseEntity.class);
        } catch (HttpClientErrorException ex) {
            throw new ProxyException(ex.getMessage(), GOOGLE_API_ENTITY, GOOGLE_API_EXCEPTION_ERROR_KEY);
        } catch (ResourceAccessException | HttpServerErrorException ex) {
            throw new InternalServerErrorException(ex.getMessage(), GOOGLE_API_ENTITY, GOOGLE_API_EXCEPTION_ERROR_KEY);
        }

        log.info("GET response <--- {}", responseEntity);

        return responseEntity.getBody();
    }
}
