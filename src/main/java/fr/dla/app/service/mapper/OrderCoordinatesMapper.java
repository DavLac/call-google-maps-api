package fr.dla.app.service.mapper;

import fr.dla.app.domain.OrderCoordinates;
import fr.dla.app.service.dto.OrderCoordinatesDTO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderCoordinatesMapper extends EntityMapper<OrderCoordinatesDTO, OrderCoordinates> {
}
