package fr.dla.app.client.googlemapsapi;

import fr.dla.app.client.googlemapsapi.model.DistanceMatrixResponseEntity;

public interface GoogleMapsRouteClient {
    DistanceMatrixResponseEntity getDistanceDetailsBetweenTwoCoordinates(String[] origin, String[] destination);
}
