package fr.dla.app.service;

import fr.dla.app.service.dto.OrderCoordinatesDTO;
import fr.dla.app.web.rest.errors.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    private static final String START_LATITUDE = "START_LATITUDE";
    private static final String END_LATITUDE = "END_LATITUDE";
    private static final String START_LONGITUDE = "START_LONGITUDE";
    private static final String END_LONGITUDE = "END_LONGITUDE";
    private static final int ORDER_ID = 1;
    private static final int ORDER_DISTANCE = 10;
    private OrderCoordinatesDTO orderCoordinatesDTO = new OrderCoordinatesDTO();

    @InjectMocks
    private OrderService orderService;

    @Test
    void createOrder_withNullBody_shouldReturnBadRequest() {
        //test
        try {
            orderService.createOrder(null);
        } catch (BadRequestException ex) {
            //checks
            assertThat(ex.getErrorKey()).isEqualTo("nullBodyError");
        }
    }

    @Test
    void createOrder_withNullOrigin_shouldReturnBadRequest() {
        //inputs
        orderCoordinatesDTO.setOrigin(null);
        orderCoordinatesDTO.setDestination(new String[]{END_LATITUDE, END_LONGITUDE});

        //test
        try {
            orderService.createOrder(orderCoordinatesDTO);
        } catch (BadRequestException ex) {
            //checks
            assertThat(ex.getErrorKey()).isEqualTo("emptyObjectError");
        }
    }

    @Test
    void createOrder_withEmptyOrigin_shouldReturnBadRequest() {
        //inputs
        orderCoordinatesDTO.setOrigin(new String[]{});
        orderCoordinatesDTO.setDestination(new String[]{END_LATITUDE, END_LONGITUDE});

        //test
        try {
            orderService.createOrder(orderCoordinatesDTO);
        } catch (BadRequestException ex) {
            //checks
            assertThat(ex.getErrorKey()).isEqualTo("emptyObjectError");
        }
    }

    @Test
    void createOrder_withNullDestination_shouldReturnBadRequest() {
        //inputs
        orderCoordinatesDTO.setOrigin(new String[]{END_LATITUDE, END_LONGITUDE});
        orderCoordinatesDTO.setDestination(null);

        //test
        try {
            orderService.createOrder(orderCoordinatesDTO);
        } catch (BadRequestException ex) {
            //checks
            assertThat(ex.getErrorKey()).isEqualTo("emptyObjectError");
        }
    }

    @Test
    void createOrder_withEmptyDestination_shouldReturnBadRequest() {
        //inputs
        orderCoordinatesDTO.setOrigin(new String[]{END_LATITUDE, END_LONGITUDE});
        orderCoordinatesDTO.setDestination(new String[]{});

        //test
        try {
            orderService.createOrder(orderCoordinatesDTO);
        } catch (BadRequestException ex) {
            //checks
            assertThat(ex.getErrorKey()).isEqualTo("emptyObjectError");
        }
    }
}
