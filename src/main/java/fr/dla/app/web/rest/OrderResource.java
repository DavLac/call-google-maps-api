package fr.dla.app.web.rest;

import fr.dla.app.domain.Order;
import fr.dla.app.domain.OrderCoordinates;
import fr.dla.app.service.OrderService;
import fr.dla.app.service.dto.OrderCoordinatesDTO;
import fr.dla.app.service.mapper.OrderCoordinatesMapper;
import fr.dla.app.web.rest.errors.BadRequestException;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.util.ArrayUtils;

import java.util.Arrays;

import static fr.dla.app.config.Constants.ENTITY_DLAPP;

/**
 * Controller for view and managing Log Level at runtime.
 */
@RestController
@RequestMapping(
    path = "/orders",
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
)
public class OrderResource {

    private final Logger log = LoggerFactory.getLogger(OrderResource.class);

    private final OrderService orderService;
    private final OrderCoordinatesMapper orderCoordinatesMapper;

    public OrderResource(OrderService orderService, OrderCoordinatesMapper orderCoordinatesMapper) {
        this.orderService = orderService;
        this.orderCoordinatesMapper = orderCoordinatesMapper;
    }

    @PostMapping()
    @ApiOperation("Create an order")
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "Bad request"),
        @ApiResponse(code = 404, message = "Coordinates not found"),
        @ApiResponse(code = 500, message = "Internal server error")
    })
    public ResponseEntity<Order> createOrder(
        @ApiParam(value = "Order origin and destination coordinates") @RequestBody OrderCoordinates orderCoordinates
    ) {
        checkOrderCoordinatesBody(orderCoordinates);
        log.info("POST request to create an order. orderCoordinates = {}", orderCoordinates);
        OrderCoordinatesDTO orderCoordinatesDTO = orderCoordinatesMapper.toDto(orderCoordinates);
        return ResponseEntity.ok(orderService.createOrder(orderCoordinatesDTO));
    }

    private static void checkOrderCoordinatesBody(OrderCoordinates orderCoordinates) {
        if (orderCoordinates == null) {
            throw new BadRequestException("Body is null", ENTITY_DLAPP, "nullBodyError");
        }

        if (ArrayUtils.isEmpty(orderCoordinates.getDestination()) ||
            ArrayUtils.isEmpty(orderCoordinates.getOrigin())) {
            throw new BadRequestException("Parameters 'origin' and 'destination' must not be empty", ENTITY_DLAPP,
                "emptyObjectError");
        }

        if (isOrderCoordinatesHasTwoStringNotBlank(orderCoordinates)) {
            throw new BadRequestException("Parameters 'origin' and 'destination' must be an array of exactly two strings not blank",
                ENTITY_DLAPP, "badObjectError");
        }
    }

    private static boolean isOrderCoordinatesHasTwoStringNotBlank(OrderCoordinates orderCoordinates) {
        return (orderCoordinates.getDestination().length != 2 ||
            orderCoordinates.getOrigin().length != 2 ||
            Arrays.stream(orderCoordinates.getOrigin()).anyMatch(StringUtils::isBlank) ||
            Arrays.stream(orderCoordinates.getDestination()).anyMatch(StringUtils::isBlank));
    }
}
