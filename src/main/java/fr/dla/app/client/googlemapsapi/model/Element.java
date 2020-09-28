package fr.dla.app.client.googlemapsapi.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Element {
    private Distance distance;
    private Duration duration;
    private GoogleApiElementLevelStatusEnum status;
}
