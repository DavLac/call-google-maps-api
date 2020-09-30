package fr.dla.app.web.rest;

import fr.dla.app.DlappApp;
import fr.dla.app.domain.OrderCoordinates;
import fr.dla.app.domain.OrderStatusEnum;
import fr.dla.app.domain.entities.OrderEntity;
import fr.dla.app.repository.OrderEntityRepository;
import fr.dla.app.web.rest.errors.DlappExceptionHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static fr.dla.app.web.rest.OrderIntTest.CoordinatesEnum.DISNEYLAND_PARIS;
import static fr.dla.app.web.rest.OrderIntTest.CoordinatesEnum.LALAMOVE_HONG_KONG_OFFICE;
import static fr.dla.app.web.rest.OrderIntTest.CoordinatesEnum.MALDIVES_ISLAND;
import static fr.dla.app.web.rest.OrderIntTest.CoordinatesEnum.PARIS_EIFFEL_TOWER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {DlappApp.class})
class OrderIntTest {

    private static final int DISTANCE_BETWEEN_EIFFEL_TOWER_AND_DISNEYLAND_PARIS = 51231;

    enum CoordinatesEnum {
        PARIS_EIFFEL_TOWER("48.858245", "2.294642"),
        DISNEYLAND_PARIS("48.868480", "2.781909"),
        LALAMOVE_HONG_KONG_OFFICE("22.339320", "114.147315"),
        MALDIVES_ISLAND("-0.611764", "73.093789");

        private String latitude;
        private String longitude;

        CoordinatesEnum(String latitude, String longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }

    private OrderCoordinates orderCoordinates = new OrderCoordinates();

    @Autowired
    private OrderResource controller;

    @Autowired
    private DlappExceptionHandler exceptionTranslator;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private OrderEntityRepository orderEntityRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(exceptionTranslator)
            .setMessageConverters(jacksonMessageConverter)
            .build();
    }

    @AfterEach
    void clean() {
        orderEntityRepository.deleteAll();
    }

    @Test
    @Transactional
    void createOrder_withFullValidParameters_shouldReturnCreatedOrder() throws Exception {
        orderCoordinates.setOrigin(Arrays.asList(PARIS_EIFFEL_TOWER.latitude, PARIS_EIFFEL_TOWER.longitude));
        orderCoordinates.setDestination(Arrays.asList(DISNEYLAND_PARIS.latitude, DISNEYLAND_PARIS.longitude));

        int databaseSizeBeforeCreate = orderEntityRepository.findAll().size();

        // Create order
        mockMvc.perform(post("/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(orderCoordinates)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").isNotEmpty())
            .andExpect(jsonPath("$.distance").value(DISTANCE_BETWEEN_EIFFEL_TOWER_AND_DISNEYLAND_PARIS))
            .andExpect(jsonPath("$.status").value(OrderStatusEnum.UNASSIGNED.name()));

        // Validate the Order in the database
        List<OrderEntity> orderEntityList = orderEntityRepository.findAll();
        assertThat(orderEntityList).hasSize(databaseSizeBeforeCreate + 1);

        OrderEntity orderEntity = orderEntityList.get(orderEntityList.size() - 1);
        assertThat(orderEntity.getId()).isNotNull();
        assertThat(orderEntity.getId()).isNotNegative();
        assertThat(orderEntity.getDistance()).isEqualTo(DISTANCE_BETWEEN_EIFFEL_TOWER_AND_DISNEYLAND_PARIS);
        assertThat(orderEntity.getStatus()).isEqualTo(OrderStatusEnum.UNASSIGNED);
    }

    @Test
    @Transactional
    void createOrder_withBetweenTwoUnreachableCoordinates_shouldReturnZeroResultsError() throws Exception {
        orderCoordinates.setOrigin(Arrays.asList(LALAMOVE_HONG_KONG_OFFICE.latitude, LALAMOVE_HONG_KONG_OFFICE.longitude));
        orderCoordinates.setDestination(Arrays.asList(MALDIVES_ISLAND.latitude, MALDIVES_ISLAND.longitude));

        int databaseSizeBeforeCreate = orderEntityRepository.findAll().size();

        // Create order
        mockMvc.perform(post("/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(orderCoordinates)))
            .andExpect(status().isPreconditionFailed())
            .andExpect(jsonPath("$.error").value("Google maps API return zero results"));

        // Validate database size
        int databaseSizeAfterCreate = orderEntityRepository.findAll().size();
        assertThat(databaseSizeAfterCreate).isEqualTo(databaseSizeBeforeCreate);
    }

    @Test
    void createOrder_withDestinationBadSize_shouldReturnBadRequest() throws Exception {
        orderCoordinates.setOrigin(Arrays.asList(LALAMOVE_HONG_KONG_OFFICE.latitude, LALAMOVE_HONG_KONG_OFFICE.longitude));
        orderCoordinates.setDestination(Collections.singletonList(MALDIVES_ISLAND.latitude));

        mockMvc.perform(post("/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(orderCoordinates)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value(containsString("Field error in object 'orderCoordinates' on field 'destination'")));
    }

    @Test
    void createOrder_withDestinationBlank_shouldReturnBadRequest() throws Exception {
        orderCoordinates.setOrigin(Arrays.asList(LALAMOVE_HONG_KONG_OFFICE.latitude, LALAMOVE_HONG_KONG_OFFICE.longitude));
        orderCoordinates.setDestination(Arrays.asList(MALDIVES_ISLAND.latitude, " "));

        mockMvc.perform(post("/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(orderCoordinates)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value(containsString("[destination[1]]]; default message [must not be blank]")));
    }

    @Test
    void createOrder_withOriginBlank_shouldReturnBadRequest() throws Exception {
        orderCoordinates.setOrigin(Arrays.asList(" ", LALAMOVE_HONG_KONG_OFFICE.longitude));
        orderCoordinates.setDestination(Arrays.asList(MALDIVES_ISLAND.latitude, MALDIVES_ISLAND.longitude));

        mockMvc.perform(post("/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(orderCoordinates)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value(containsString("[origin[0]]]; default message [must not be blank]")));
    }

    @Test
    void createOrder_withOriginBadSize_shouldReturnBadRequest() throws Exception {
        orderCoordinates.setOrigin(Collections.singletonList(LALAMOVE_HONG_KONG_OFFICE.latitude));
        orderCoordinates.setDestination(Arrays.asList(MALDIVES_ISLAND.latitude, MALDIVES_ISLAND.longitude));

        mockMvc.perform(post("/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(orderCoordinates)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value(containsString("Field error in object 'orderCoordinates' on field 'origin'")));
    }

    @Test
    void createOrder_withOriginEmpty_shouldReturnBadRequest() throws Exception {
        orderCoordinates.setOrigin(new ArrayList<>());
        orderCoordinates.setDestination(Arrays.asList(MALDIVES_ISLAND.latitude, MALDIVES_ISLAND.longitude));

        mockMvc.perform(post("/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(orderCoordinates)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value(containsString("Field error in object 'orderCoordinates' on field 'origin'")));
    }

    @Test
    void createOrder_withOriginNull_shouldReturnBadRequest() throws Exception {
        orderCoordinates.setOrigin(null);
        orderCoordinates.setDestination(Arrays.asList(MALDIVES_ISLAND.latitude, MALDIVES_ISLAND.longitude));

        mockMvc.perform(post("/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(orderCoordinates)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error")
                .value(containsString("Field error in object 'orderCoordinates' on field 'origin': rejected value [null]")));
    }

    @Test
    void createOrder_withNullBody_shouldReturnZeroResultsError() throws Exception {
        mockMvc.perform(post("/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(null)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Required request body is missing: public " +
                "org.springframework.http.ResponseEntity<fr.dla.app.domain.Order> fr.dla.app.web.rest.OrderResource." +
                "createOrder(fr.dla.app.domain.OrderCoordinates)"));
    }

    @Test
    void createOrder_withNullBody_shouldReturnBadRequestError() throws Exception {
        mockMvc.perform(post("/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(null)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Required request body is missing: public " +
                "org.springframework.http.ResponseEntity<fr.dla.app.domain.Order> fr.dla.app.web.rest.OrderResource" +
                ".createOrder(fr.dla.app.domain.OrderCoordinates)"));
    }

    @Test
    void createOrder_withBadMediaType_shouldReturnBadMediaTypeError() throws Exception {
        mockMvc.perform(post("/orders")
            .contentType(MediaType.APPLICATION_XML)
            .content(TestUtil.convertObjectToJsonBytes(orderCoordinates)))
            .andExpect(status().isUnsupportedMediaType())
            .andExpect(jsonPath("$.error").value("Content type 'application/xml' not supported"));
    }

    @Test
    void createOrder_withBadMethodType_shouldReturnMethodNotAllowed() throws Exception {
        mockMvc.perform(delete("/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(orderCoordinates)))
            .andExpect(status().isMethodNotAllowed())
            .andExpect(jsonPath("$.error").value("Request method 'DELETE' not supported"));
    }

    @Test
    void createOrder_withUnknownController_shouldReturnNotFoundMethod() throws Exception {
        mockMvc.perform(post("/unknown")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(null)))
            .andExpect(status().isNotFound());
    }
}
