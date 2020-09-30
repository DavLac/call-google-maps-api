package fr.dla.app.client.googlemapsapi.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DistanceMatrixResponseEntity {
    private List<String> origin_addresses;
    private List<String> destination_addresses;
    private List<Row> rows;
    private GoogleApiTopLevelStatusEnum status;
}
