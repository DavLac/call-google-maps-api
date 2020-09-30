package fr.dla.app.web.rest;

import fr.dla.app.domain.Order;
import fr.dla.app.domain.OrderCoordinates;
import fr.dla.app.domain.OrderStatus;
import fr.dla.app.domain.PatchOrderResponse;
import fr.dla.app.service.OrderService;
import fr.dla.app.service.dto.OrderCoordinatesDTO;
import fr.dla.app.service.mapper.OrderCoordinatesMapper;
import fr.dla.app.web.rest.errors.BadRequestException;
import fr.dla.app.web.rest.errors.DlappErrorResponse;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static fr.dla.app.config.Constants.ENTITY_DLAPP;

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

    /**
     * Create an order
     *
     * @param orderCoordinates origin and destination with a start and end latitude/longitude
     * @return the created order
     */
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
        log.info("POST request to create an order. orderCoordinates = {}", orderCoordinates);
        OrderCoordinatesDTO orderCoordinatesDTO = orderCoordinatesMapper.toDto(orderCoordinates);
        return ResponseEntity.ok(orderService.createOrder(orderCoordinatesDTO));
    }

    /**
     * List orders
     *
     * @param page  Page number
     * @param limit Page size of orders to display
     * @return Order list by page
     */
    @GetMapping()
    @ApiOperation("Get orders")
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "Bad request"),
        @ApiResponse(code = 500, message = "Internal server error")
    })
    public ResponseEntity<List<Order>> getOrders(
        @ApiParam("Page number of the requested page") @RequestParam(required = false) Integer page,
        @ApiParam("The size of the requested page") @RequestParam(required = false) Integer limit
    ) {
        log.info("GET request to get orders. page = {}, limit = {}", page, limit);
        return ResponseEntity.ok(orderService.getOrders(page, limit));
    }

    /**
     * Take an order, status = TAKEN
     *
     * @param id          Order id to take
     * @param orderStatus Order status to update
     * @return 'SUCCESS' response status if order was updated
     */
    @PatchMapping("/{id}")
    @ApiOperation("Take an order")
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "Bad request"),
        @ApiResponse(code = 404, message = "Order not found"),
        @ApiResponse(code = 412, message = "Order already taken"),
        @ApiResponse(code = 500, message = "Internal server error")
    })
    public ResponseEntity<PatchOrderResponse> takeOrder(
        @ApiParam(value = "Order ID to take") @PathVariable(value = "id") Integer id,
        @ApiParam(value = "Order status") @RequestBody OrderStatus orderStatus
    ) {
        log.info("PATCH request to take an order. Order id = {}, order status = {}", id, orderStatus);

        if (orderStatus == null) {
            throw new BadRequestException("Status parameter is null", ENTITY_DLAPP, "nullObjectError");
        }

        return ResponseEntity.ok(orderService.takeOrder(id, orderStatus.getStatus()));
    }
}
