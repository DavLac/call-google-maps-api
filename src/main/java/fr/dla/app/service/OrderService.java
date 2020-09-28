package fr.dla.app.service;

import fr.dla.app.client.googlemapsapi.GoogleMapsRouteClient;
import fr.dla.app.client.googlemapsapi.model.DistanceMatrixResponseEntity;
import fr.dla.app.domain.Order;
import fr.dla.app.domain.OrderStatus;
import fr.dla.app.domain.entities.OrderEntity;
import fr.dla.app.repository.OrderEntityRepository;
import fr.dla.app.service.dto.OrderCoordinatesDTO;
import fr.dla.app.service.mapper.OrderMapper;
import fr.dla.app.web.rest.errors.BadRequestException;
import fr.dla.app.web.rest.errors.InternalServerErrorException;
import fr.dla.app.web.rest.errors.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

import static fr.dla.app.config.Constants.ENTITY_DLAPP;
import static fr.dla.app.config.Constants.GOOGLE_API_ENTITY;

@Service
@Transactional
public class OrderService {

    private final Logger log = LoggerFactory.getLogger(OrderService.class);

    private static final String BAD_REQUEST_ERROR_KEY = "badRequestError";

    private final OrderEntityRepository orderEntityRepository;
    private final GoogleMapsRouteClient googleMapsRouteClient;
    private final OrderMapper orderMapper;

    public OrderService(OrderEntityRepository orderEntityRepository,
                        GoogleMapsRouteClient googleMapsRouteClient,
                        OrderMapper orderMapper) {
        this.orderEntityRepository = orderEntityRepository;
        this.googleMapsRouteClient = googleMapsRouteClient;
        this.orderMapper = orderMapper;
    }

    //region public method
    public Order createOrder(@NotNull OrderCoordinatesDTO orderCoordinatesDTO) {
        DistanceMatrixResponseEntity distanceMatrixResponseEntity =
            googleMapsRouteClient.getDistanceDetailsBetweenTwoCoordinates(
                orderCoordinatesDTO.getOrigin(),
                orderCoordinatesDTO.getDestination()
            );

        handleDistanceMatrixResponseEntityResponse(distanceMatrixResponseEntity);

        OrderEntity orderEntity = new OrderEntity(distanceMatrixResponseEntity.getRows().get(0).getElements().get(0)
            .getDistance().getValue(), OrderStatus.UNASSIGNED);
        log.info("Creating order in database with order entity = {}", orderEntity);
        OrderEntity orderEntitySaved = orderEntityRepository.save(orderEntity);

        return orderMapper.toDto(orderEntitySaved);
    }

    @Transactional(readOnly = true)
    public List<Order> getOrders(Integer page, Integer limit) {
        log.info("Get orders with page = {} and limit = {}", page, limit);

        checkParamMinimumSize(page, "Page");
        checkParamMinimumSize(limit, "Limit");

        Page<OrderEntity> orderEntities = orderEntityRepository.findAll(PageRequest.of(page - 1, limit));

        log.info("Fetched {} orders", orderEntities.getTotalElements());

        return orderEntities.getContent().stream()
            .map(orderMapper::toDto)
            .collect(Collectors.toList());
    }
    //endregion public method

    //region private method
    private static void handleDistanceMatrixResponseEntityResponse(DistanceMatrixResponseEntity distanceMatrixResponseEntity) {
        if (distanceMatrixResponseEntity == null) {
            throw new InternalServerErrorException("Google maps API return a null response", GOOGLE_API_ENTITY, "nullBodyError");
        }

        switch (distanceMatrixResponseEntity.getStatus()) {
            case OK:
                break;
            case INVALID_REQUEST:
            case MAX_ELEMENTS_EXCEEDED:
                throw new BadRequestException(String.format("Google maps API return a bad request error : %s",
                    distanceMatrixResponseEntity.getStatus()), GOOGLE_API_ENTITY, BAD_REQUEST_ERROR_KEY);
            case UNKNOWN_ERROR:
            case OVER_DAILY_LIMIT:
            case OVER_QUERY_LIMIT:
            case REQUEST_DENIED:
                throw new InternalServerErrorException(String.format("Google maps API return a client error response : %s",
                    distanceMatrixResponseEntity.getStatus()), GOOGLE_API_ENTITY, "clientErrorResponse");
            default:
                throw new InternalServerErrorException(String.format("Google maps API return unknown response error : %s",
                    distanceMatrixResponseEntity.getStatus()), GOOGLE_API_ENTITY, "unknownErrorResponse");
        }

        if (CollectionUtils.isEmpty(distanceMatrixResponseEntity.getRows())) {
            throw new InternalServerErrorException("Google maps API return empty rows result", GOOGLE_API_ENTITY, "emptyRowsError");
        }

        if (CollectionUtils.isEmpty(distanceMatrixResponseEntity.getRows().get(0).getElements())) {
            throw new InternalServerErrorException("Google maps API return empty elements result", GOOGLE_API_ENTITY, "emptyElementsError");
        }

        switch (distanceMatrixResponseEntity.getRows().get(0).getElements().get(0).getStatus()) {
            case OK:
                break;
            case MAX_ROUTE_LENGTH_EXCEEDED:
                throw new BadRequestException(String.format("Google maps API return a bad request error : %s",
                    distanceMatrixResponseEntity.getRows().get(0).getElements().get(0).getStatus()), GOOGLE_API_ENTITY, BAD_REQUEST_ERROR_KEY);
            case NOT_FOUND:
            case ZERO_RESULTS:
                throw new NotFoundException(String.format("Google maps API return a not found error : %s",
                    distanceMatrixResponseEntity.getRows().get(0).getElements().get(0).getStatus()), GOOGLE_API_ENTITY, "notFoundError");
            default:
                throw new InternalServerErrorException(String.format("Google maps API return unknown response error : %s",
                    distanceMatrixResponseEntity.getRows().get(0).getElements().get(0).getStatus()), GOOGLE_API_ENTITY, "unknownErrorResponse");
        }

        if (distanceMatrixResponseEntity.getRows().get(0).getElements().get(0).getDistance() == null) {
            throw new InternalServerErrorException("Google maps API return null distance result", GOOGLE_API_ENTITY, "nullDistanceError");
        }
    }

    private void checkParamMinimumSize(Integer param, String paramName) {
        if (param == null || param < 1) {
            throw new BadRequestException(String.format("%s minimum size = 1", paramName), ENTITY_DLAPP, BAD_REQUEST_ERROR_KEY);
        }
    }
    //endregion private method
}