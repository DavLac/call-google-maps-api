package fr.dla.app.service.mapper;

import fr.dla.app.domain.Order;
import fr.dla.app.domain.entities.OrderEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderMapper extends EntityMapper<Order, OrderEntity> {
}
