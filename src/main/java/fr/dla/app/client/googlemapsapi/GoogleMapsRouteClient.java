package fr.dla.app.client.googlemapsapi;

import fr.dla.app.client.googlemapsapi.model.DistanceMatrixResponseEntity;

import java.util.List;

public interface GoogleMapsRouteClient {
    DistanceMatrixResponseEntity getDistanceDetailsBetweenTwoCoordinates(List<String> origin, List<String> destination);
}
