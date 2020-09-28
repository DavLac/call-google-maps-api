package fr.dla.app.client.googlemapsapi.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class Row {
    private List<Element> elements;
}
