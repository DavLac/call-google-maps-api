package fr.dla.app.client.googlemapsapi.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class DistanceMatrixResponseEntity {
    private List<String> origin_addresses;
    private List<String> destination_addresses;
    private List<Row> rows;
    private GoogleApiTopLevelStatusEnum status;
}
