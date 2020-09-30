package fr.dla.app.service;

import fr.dla.app.client.googlemapsapi.GoogleMapsRouteClient;
import fr.dla.app.client.googlemapsapi.model.Distance;
import fr.dla.app.client.googlemapsapi.model.DistanceMatrixResponseEntity;
import fr.dla.app.client.googlemapsapi.model.Element;
import fr.dla.app.client.googlemapsapi.model.GoogleApiElementLevelStatusEnum;
import fr.dla.app.client.googlemapsapi.model.GoogleApiTopLevelStatusEnum;
import fr.dla.app.client.googlemapsapi.model.Row;
import fr.dla.app.domain.Order;
import fr.dla.app.domain.OrderStatusEnum;
import fr.dla.app.domain.entities.OrderEntity;
import fr.dla.app.repository.OrderEntityRepository;
import fr.dla.app.service.mapper.OrderMapper;
import fr.dla.app.web.rest.errors.BadRequestException;
import fr.dla.app.web.rest.errors.InternalServerErrorException;
import fr.dla.app.web.rest.errors.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    private static final String START_LATITUDE = "START_LATITUDE";
    private static final String END_LATITUDE = "END_LATITUDE";
    private static final String START_LONGITUDE = "START_LONGITUDE";
    private static final String END_LONGITUDE = "END_LONGITUDE";
    private static final int ORDER_ID = 1;
    private static final int ORDER_DISTANCE = 10;

    @InjectMocks
    private OrderService orderService;

    @Mock
    private OrderEntityRepository orderEntityRepository;

    @Mock
    private GoogleMapsRouteClient googleMapsRouteClient;

    @Mock
    private OrderMapper orderMapper;

    @Test
    void createOrder_withGoodParameters_shouldCreateOrder() {
        //inputs
        List<String> origin = Arrays.asList(START_LATITUDE, START_LONGITUDE);
        List<String> destination = Arrays.asList(END_LATITUDE, END_LONGITUDE);

        DistanceMatrixResponseEntity distanceMatrixResponseEntity = DistanceMatrixResponseEntity.builder()
            .rows(
                Collections.singletonList(
                    Row.builder()
                        .elements(
                            Collections.singletonList(
                                Element.builder()
                                    .distance(new Distance(null, ORDER_DISTANCE))
                                    .status(GoogleApiElementLevelStatusEnum.OK)
                                    .build())).build()))
            .status(GoogleApiTopLevelStatusEnum.OK).build();
        Mockito.when(googleMapsRouteClient.getDistanceDetailsBetweenTwoCoordinates(origin, destination)).thenReturn(distanceMatrixResponseEntity);
        OrderEntity orderEntitySaved = new OrderEntity(ORDER_ID, ORDER_DISTANCE, OrderStatusEnum.UNASSIGNED);
        Mockito.when(orderEntityRepository.save(any(OrderEntity.class))).thenReturn(orderEntitySaved);
        Order order = new Order(ORDER_ID, ORDER_DISTANCE, OrderStatusEnum.UNASSIGNED);
        Mockito.when(orderMapper.toDto(orderEntitySaved)).thenReturn(order);

        //test
        Order orderResponse = orderService.createOrder(origin, destination);

        assertThat(orderResponse).isEqualToComparingFieldByField(order);

    }

    @Test
    void createOrder_withGoogleNotFoundError_shouldReturnNotFoundError() {
        //inputs
        List<String> origin = Arrays.asList(START_LATITUDE, START_LONGITUDE);
        List<String> destination = Arrays.asList(END_LATITUDE, END_LONGITUDE);

        DistanceMatrixResponseEntity distanceMatrixResponseEntity = DistanceMatrixResponseEntity.builder()
            .rows(
                Collections.singletonList(
                    Row.builder()
                        .elements(
                            Collections.singletonList(
                                Element.builder()
                                    .distance(null)
                                    .status(GoogleApiElementLevelStatusEnum.NOT_FOUND)
                                    .build())).build()))
            .status(GoogleApiTopLevelStatusEnum.OK).build();
        Mockito.when(googleMapsRouteClient.getDistanceDetailsBetweenTwoCoordinates(origin, destination)).thenReturn(distanceMatrixResponseEntity);

        //test
        try {
            orderService.createOrder(origin, destination);
        } catch (NotFoundException ex) {
            assertThat(ex.getErrorKey()).isEqualTo("notFoundError");
        }
    }

    @Test
    void createOrder_withGoogleThrowInvalidRequest_shouldReturnBadRequestError() {
        //inputs
        List<String> origin = Arrays.asList(START_LATITUDE, START_LONGITUDE);
        List<String> destination = Arrays.asList(END_LATITUDE, END_LONGITUDE);

        DistanceMatrixResponseEntity distanceMatrixResponseEntity = DistanceMatrixResponseEntity.builder()
            .rows(
                Collections.singletonList(
                    Row.builder()
                        .elements(
                            Collections.singletonList(
                                Element.builder()
                                    .distance(null)
                                    .status(GoogleApiElementLevelStatusEnum.NOT_FOUND)
                                    .build())).build()))
            .status(GoogleApiTopLevelStatusEnum.INVALID_REQUEST).build();
        Mockito.when(googleMapsRouteClient.getDistanceDetailsBetweenTwoCoordinates(origin, destination)).thenReturn(distanceMatrixResponseEntity);

        //test
        try {
            orderService.createOrder(origin, destination);
        } catch (BadRequestException ex) {
            assertThat(ex.getErrorKey()).isEqualTo("badRequestError");
        }
    }

    @Test
    void createOrder_withGoogleNullResponse_shouldReturnInternalServerError() {
        //inputs
        List<String> origin = Arrays.asList(START_LATITUDE, START_LONGITUDE);
        List<String> destination = Arrays.asList(END_LATITUDE, END_LONGITUDE);

        Mockito.when(googleMapsRouteClient.getDistanceDetailsBetweenTwoCoordinates(origin, destination)).thenReturn(null);

        //test
        try {
            orderService.createOrder(origin, destination);
        } catch (InternalServerErrorException ex) {
            assertThat(ex.getErrorKey()).isEqualTo("nullResponseError");
        }
    }
}
