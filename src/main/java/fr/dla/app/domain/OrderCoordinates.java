package fr.dla.app.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.Size;

@Getter
@Setter
@ToString
public class OrderCoordinates {

    @Size(min = 2, max = 2)
    private String[] origin;

    @Size(min = 2, max = 2)
    private String[] destination;
}
