package fr.dla.app.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Getter
@Setter
@ToString
public class OrderCoordinates {

    @NotNull
    @Size(min = 2, max = 2)
    private List<@NotBlank String> origin;

    @NotNull
    @Size(min = 2, max = 2)
    private List<@NotBlank String> destination;
}
