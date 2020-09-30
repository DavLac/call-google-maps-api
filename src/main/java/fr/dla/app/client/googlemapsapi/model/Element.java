package fr.dla.app.client.googlemapsapi.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Element {
    private Distance distance;
    private Duration duration;
    private GoogleApiElementLevelStatusEnum status;
}
