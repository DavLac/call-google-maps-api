package fr.dla.app.web.rest;

import fr.dla.app.domain.Order;
import fr.dla.app.domain.OrderCoordinates;
import fr.dla.app.domain.OrderStatusEnum;
import fr.dla.app.service.OrderService;
import fr.dla.app.service.dto.OrderCoordinatesDTO;
import fr.dla.app.service.mapper.OrderCoordinatesMapper;
import fr.dla.app.web.rest.errors.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class OrderResourceTest {

    private static final String START_LATITUDE = "START_LATITUDE";
    private static final String END_LATITUDE = "END_LATITUDE";
    private static final String START_LONGITUDE = "START_LONGITUDE";
    private static final String END_LONGITUDE = "END_LONGITUDE";
    private static final int ORDER_ID = 1;
    private static final int ORDER_DISTANCE = 10;
    private static final String NULL_BODY_ERROR_ERROR_KEY = "nullBodyError";

    private OrderCoordinates orderCoordinates = new OrderCoordinates();
    private OrderCoordinatesDTO orderCoordinatesDTO = new OrderCoordinatesDTO();

    @InjectMocks
    private OrderResource orderResource;

    @Mock
    private OrderService orderService;

    @Mock
    private OrderCoordinatesMapper orderCoordinatesMapper;

    @Test
    void createOrder_withFullValidParameters_shouldReturnOkResponse() {
        //inputs
        orderCoordinates.setOrigin(Arrays.asList(START_LATITUDE, START_LONGITUDE));
        orderCoordinates.setDestination(Arrays.asList(END_LATITUDE, END_LONGITUDE));

        orderCoordinatesDTO.setOrigin(orderCoordinatesDTO.getOrigin());
        orderCoordinatesDTO.setDestination(orderCoordinatesDTO.getDestination());
        Mockito.when(orderCoordinatesMapper.toDto(orderCoordinates)).thenReturn(orderCoordinatesDTO);

        Order order = new Order();
        order.setId(ORDER_ID);
        order.setDistance(ORDER_DISTANCE);
        order.setStatus(OrderStatusEnum.UNASSIGNED);
        Mockito.when(orderService.createOrder(orderCoordinatesDTO.getOrigin(), orderCoordinatesDTO.getDestination())).thenReturn(order);

        //test
        ResponseEntity<Order> orderResponseEntity = orderResource.createOrder(orderCoordinates);

        //checks
        assertThat(orderResponseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(orderResponseEntity.getBody()).isEqualToComparingFieldByField(order);
    }

    @Test
    void createOrder_withServiceThrowAnException_shouldReturnTheException() {
        //inputs
        orderCoordinates.setOrigin(Arrays.asList(START_LATITUDE, START_LONGITUDE));
        orderCoordinates.setDestination(Arrays.asList(END_LATITUDE, END_LONGITUDE));

        orderCoordinatesDTO.setOrigin(orderCoordinatesDTO.getOrigin());
        orderCoordinatesDTO.setDestination(orderCoordinatesDTO.getDestination());
        Mockito.when(orderCoordinatesMapper.toDto(orderCoordinates)).thenReturn(orderCoordinatesDTO);

        //test
        try {
            orderResource.createOrder(orderCoordinates);
        } catch (BadRequestException ex) {
            //checks
            assertThat(ex.getErrorKey()).isEqualTo(NULL_BODY_ERROR_ERROR_KEY);
        }
    }
}
