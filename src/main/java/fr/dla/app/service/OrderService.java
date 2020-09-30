package fr.dla.app.service;

import fr.dla.app.client.googlemapsapi.GoogleMapsRouteClient;
import fr.dla.app.client.googlemapsapi.model.DistanceMatrixResponseEntity;
import fr.dla.app.client.googlemapsapi.model.GoogleApiElementLevelStatusEnum;
import fr.dla.app.client.googlemapsapi.model.GoogleApiTopLevelStatusEnum;
import fr.dla.app.domain.Order;
import fr.dla.app.domain.OrderStatusEnum;
import fr.dla.app.domain.PatchOrderResponse;
import fr.dla.app.domain.ResponseStatusEnum;
import fr.dla.app.domain.entities.OrderEntity;
import fr.dla.app.repository.OrderEntityRepository;
import fr.dla.app.service.mapper.OrderMapper;
import fr.dla.app.web.rest.errors.BadRequestException;
import fr.dla.app.web.rest.errors.InternalServerErrorException;
import fr.dla.app.web.rest.errors.NotFoundException;
import fr.dla.app.web.rest.errors.PreconditionFailedException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import javax.validation.constraints.Min;
import java.util.List;
import java.util.stream.Collectors;

import static fr.dla.app.config.Constants.ENTITY_DLAPP;
import static fr.dla.app.config.Constants.GOOGLE_API_ENTITY;

@Slf4j
@Service
@Transactional
public class OrderService {

    private static final String BAD_REQUEST_ERROR_KEY = "badRequestError";

    private final OrderEntityRepository orderEntityRepository;
    private final GoogleMapsRouteClient googleMapsRouteClient;
    private final OrderMapper orderMapper;

    @PersistenceContext
    private EntityManager entityManager;

    public OrderService(OrderEntityRepository orderEntityRepository,
                        GoogleMapsRouteClient googleMapsRouteClient,
                        OrderMapper orderMapper) {
        this.orderEntityRepository = orderEntityRepository;
        this.googleMapsRouteClient = googleMapsRouteClient;
        this.orderMapper = orderMapper;
    }

    //region public method
    public Order createOrder(List<String> origin, List<String> destination) {

        DistanceMatrixResponseEntity distanceMatrixResponseEntity = googleMapsRouteClient.getDistanceDetailsBetweenTwoCoordinates(origin, destination);

        handleDistanceMatrixResponseEntityResponse(distanceMatrixResponseEntity);

        final Integer distanceResult = distanceMatrixResponseEntity.getRows().get(0).getElements().get(0).getDistance().getValue();
        OrderEntity orderEntity = new OrderEntity(distanceResult, OrderStatusEnum.UNASSIGNED);
        log.info("Creating order in database with order entity = {}", orderEntity);
        OrderEntity orderEntitySaved = orderEntityRepository.save(orderEntity);

        return orderMapper.toDto(orderEntitySaved);
    }

    @Transactional(readOnly = true)
    public List<Order> getOrders(@Min(1) final int page, @Min(1) final int limit) {
        log.info("Get orders with page = {} and limit = {}", page, limit);

        Page<OrderEntity> orderEntities = orderEntityRepository.findAll(PageRequest.of(page - 1, limit));

        log.info("Fetched {} orders", orderEntities.getTotalElements());

        return orderEntities.getContent().stream()
            .map(orderMapper::toDto)
            .collect(Collectors.toList());
    }

    public PatchOrderResponse takeOrder(final int orderId, final String orderStatus) {
        log.info("Take an order with order id = {}", orderId);

        if (!StringUtils.equals(orderStatus, OrderStatusEnum.TAKEN.name())) {
            throw new BadRequestException(String.format("Status parameter is not equal to '%s'", OrderStatusEnum.TAKEN.name()),
                ENTITY_DLAPP, BAD_REQUEST_ERROR_KEY);
        }

        OrderEntity orderEntity = entityManager.find(OrderEntity.class, orderId, LockModeType.WRITE);

        if (orderEntity == null) {
            throw new NotFoundException("Order not found", ENTITY_DLAPP, "orderNotFound");
        }

        if (orderEntity.getStatus() == OrderStatusEnum.TAKEN) {
            throw new PreconditionFailedException("Order already taken", ENTITY_DLAPP, "orderAlreadyTaken");
        }

        orderEntity.setStatus(OrderStatusEnum.TAKEN);
        entityManager.merge(orderEntity);

        log.info("Order updated to status = {}", OrderStatusEnum.TAKEN);

        return new PatchOrderResponse(ResponseStatusEnum.SUCCESS);
    }
    //endregion public method

    //region private method
    private static void handleDistanceMatrixResponseEntityResponse(DistanceMatrixResponseEntity distanceMatrixResponseEntity) {
        if (distanceMatrixResponseEntity == null) {
            throw new InternalServerErrorException("Google maps API return a null response", GOOGLE_API_ENTITY, "nullResponseError");
        }

        GoogleApiTopLevelStatusEnum topLevelResponseStatus = distanceMatrixResponseEntity.getStatus();
        switch (topLevelResponseStatus) {
            case OK:
                break;
            case INVALID_REQUEST:
            case MAX_ELEMENTS_EXCEEDED:
                throw new BadRequestException(String.format("Google maps API return a bad request error : %s",
                    topLevelResponseStatus), GOOGLE_API_ENTITY, BAD_REQUEST_ERROR_KEY);
            case UNKNOWN_ERROR:
            case OVER_DAILY_LIMIT:
            case OVER_QUERY_LIMIT:
            case REQUEST_DENIED:
                throw new InternalServerErrorException(String.format("Google maps API return a client error response : %s",
                    topLevelResponseStatus), GOOGLE_API_ENTITY, "clientErrorResponse");
            default:
                throw new InternalServerErrorException(String.format("Google maps API return unknown response error : %s",
                    topLevelResponseStatus), GOOGLE_API_ENTITY, "unknownErrorResponse");
        }

        if (CollectionUtils.isEmpty(distanceMatrixResponseEntity.getRows())) {
            throw new InternalServerErrorException("Google maps API return empty rows result",
                GOOGLE_API_ENTITY, "emptyRowsError");
        }

        if (CollectionUtils.isEmpty(distanceMatrixResponseEntity.getRows().get(0).getElements())) {
            throw new InternalServerErrorException("Google maps API return empty elements result",
                GOOGLE_API_ENTITY, "emptyElementsError");
        }

        GoogleApiElementLevelStatusEnum elementLevelResponseStatus = distanceMatrixResponseEntity.getRows().get(0)
            .getElements().get(0).getStatus();
        switch (elementLevelResponseStatus) {
            case OK:
                break;
            case MAX_ROUTE_LENGTH_EXCEEDED:
                throw new BadRequestException(String.format("Google maps API return a bad request error : %s",
                    elementLevelResponseStatus), GOOGLE_API_ENTITY, BAD_REQUEST_ERROR_KEY);
            case NOT_FOUND:
                throw new NotFoundException("Google maps API return a not found error", GOOGLE_API_ENTITY, "notFoundError");
            case ZERO_RESULTS:
                throw new PreconditionFailedException("Google maps API return zero results", GOOGLE_API_ENTITY, "zeroResultsError");
            default:
                throw new InternalServerErrorException(String.format("Google maps API return unknown response error : %s",
                    elementLevelResponseStatus), GOOGLE_API_ENTITY, "unknownErrorResponse");
        }

        if (distanceMatrixResponseEntity.getRows().get(0).getElements().get(0).getDistance() == null) {
            throw new InternalServerErrorException("Google maps API return null distance result", GOOGLE_API_ENTITY, "nullDistanceError");
        }
    }
    //endregion private method
}
