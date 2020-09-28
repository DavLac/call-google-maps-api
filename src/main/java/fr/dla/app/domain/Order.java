package fr.dla.app.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Order {
    private Integer id;
    private Integer distance;
    private OrderStatus status;
}
