package fr.dla.app.domain.entities;

import fr.dla.app.domain.OrderStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Version;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
public class OrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer distance;

    private OrderStatusEnum status;

    @Version
    private Long version = 0L;

    public OrderEntity(Integer id, Integer distance, OrderStatusEnum status) {
        this.id = id;
        this.distance = distance;
        this.status = status;
    }

    public OrderEntity(Integer distance, OrderStatusEnum status) {
        this.distance = distance;
        this.status = status;
    }
}
