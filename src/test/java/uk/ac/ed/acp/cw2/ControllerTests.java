package uk.ac.ed.acp.cw2;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.ac.ed.acp.cw2.controller.CoreRestController;
import uk.ac.ed.acp.cw2.controller.DroneDeliveryController;
import uk.ac.ed.acp.cw2.controller.QueryController;
import uk.ac.ed.acp.cw2.dto.*;
import uk.ac.ed.acp.cw2.model.*;
import uk.ac.ed.acp.cw2.service.AvailabilityService;
import uk.ac.ed.acp.cw2.service.PathPlanningService;
import uk.ac.ed.acp.cw2.service.QueryService;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for REST controllers
 * Coverage:
 * All endpoints with proper request/response
 * Drone IDs are STRINGS
 * Validation and error cases
 */

@WebMvcTest
class ControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private QueryService queryService;

    @MockBean
    private AvailabilityService availabilityService;

    @MockBean
    private PathPlanningService pathPlanningService;

    // CoreRestController tests

    @Test
    void testGetUid_ReturnsStudentId() throws Exception {
        mockMvc.perform(get("/api/v1/uid"))
                .andExpect(status().isOk())
                .andExpect(content().string("s2581854"));
    }

    @Test
    void testDistanceTo_ValidRequest_ReturnsDistance() throws Exception {
        DistanceRequest request = new DistanceRequest();
        request.setPosition1(new LngLat(0.0, 0.0));
        request.setPosition2(new LngLat(3.0, 4.0));

        mockMvc.perform(post("/api/v1/distanceTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("5.0"));
    }

    @Test
    void testIsCloseTo_Close_ReturnsTrue() throws Exception {
        DistanceRequest request = new DistanceRequest();
        request.setPosition1(new LngLat(0.0, 0.0));
        request.setPosition2(new LngLat(0.0001, 0.0001));

        mockMvc.perform(post("/api/v1/isCloseTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void testNextPosition_ValidRequest_ReturnsNewPosition() throws Exception {
        NextPositionRequest request = new NextPositionRequest();
        request.setStart(new LngLat(0.0, 0.0));
        request.setAngle(0.0);

        mockMvc.perform(post("/api/v1/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lng").value(0.00015))
                .andExpect(jsonPath("$.lat").value(0.0));
    }

    @Test
    void testIsInRegion_PointInside_ReturnsTrue() throws Exception {
        List<LngLat> vertices = Arrays.asList(
                new LngLat(0.0, 0.0),
                new LngLat(4.0, 0.0),
                new LngLat(4.0, 4.0),
                new LngLat(0.0, 4.0),
                new LngLat(0.0, 0.0)
        );
        Region region = new Region("Square", vertices);

        IsInRegionRequest request = new IsInRegionRequest();
        request.setPosition(new LngLat(2.0, 2.0));
        request.setRegion(region);

        mockMvc.perform(post("/api/v1/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    // QueryController tests

    @Test
    void testDronesWithCooling_True_ReturnsStringIds() throws Exception {
        when(queryService.getDronesWithCooling(true))
                .thenReturn(Arrays.asList("DRONE-001", "DRONE-002"));

        mockMvc.perform(get("/api/v1/dronesWithCooling/true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("DRONE-001"))
                .andExpect(jsonPath("$[1]").value("DRONE-002"));
    }

    @Test
    void testDronesWithCooling_False_ReturnsStringIds() throws Exception {
        when(queryService.getDronesWithCooling(false))
                .thenReturn(Arrays.asList("DRONE-003", "DRONE-004"));

        mockMvc.perform(get("/api/v1/dronesWithCooling/false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("DRONE-003"))
                .andExpect(jsonPath("$[1]").value("DRONE-004"));
    }

    @Test
    void testDroneDetails_ValidStringId_ReturnsDrone() throws Exception {
        DroneCapability cap = new DroneCapability();
        cap.setCooling(true);
        cap.setCapacity(10.0);

        Drone drone = new Drone();
        drone.setId("DRONE-001");  // STRING
        drone.setName("Test Drone");
        drone.setCapability(cap);

        when(queryService.getDroneById("DRONE-001")).thenReturn(drone);

        mockMvc.perform(get("/api/v1/droneDetails/DRONE-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("DRONE-001"))
                .andExpect(jsonPath("$.name").value("Test Drone"))
                .andExpect(jsonPath("$.capability.cooling").value(true));
    }

    @Test
    void testDroneDetails_InvalidId_ReturnsNotFound() throws Exception {
        when(queryService.getDroneById("DRONE-999")).thenReturn(null);

        mockMvc.perform(get("/api/v1/droneDetails/DRONE-999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testQueryAsPath_ReturnsStringIds() throws Exception {
        when(queryService.queryByPath("capacity", "10.0"))
                .thenReturn(Collections.singletonList("DRONE-001"));

        mockMvc.perform(get("/api/v1/queryAsPath/capacity/10.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("DRONE-001"));
    }

    @Test
    void testQuery_MultipleConditions_ReturnsStringIds() throws Exception {
        QueryAttribute qa1 = new QueryAttribute();
        qa1.setAttribute("cooling");
        qa1.setOperator("=");
        qa1.setValue("true");

        QueryAttribute qa2 = new QueryAttribute();
        qa2.setAttribute("capacity");
        qa2.setOperator(">");
        qa2.setValue("5.0");

        List<QueryAttribute> queryAttributes = Arrays.asList(qa1, qa2);

        when(queryService.query(anyList()))
                .thenReturn(Arrays.asList("DRONE-001", "DRONE-002"));

        mockMvc.perform(post("/api/v1/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(queryAttributes)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("DRONE-001"))
                .andExpect(jsonPath("$[1]").value("DRONE-002"));
    }

    // DroneDeliveryController tests

    @Test
    void testQueryAvailableDrones_ReturnsStringIds() throws Exception {
        MedDispatchRec dispatch = new MedDispatchRec();
        dispatch.setId(1);
        dispatch.setDate("2025-01-06");
        dispatch.setTime("10:00");

        DeliveryRequirements req = new DeliveryRequirements();
        req.setCapacity(5.0);
        dispatch.setRequirements(req);

        when(availabilityService.queryAvailableDrones(anyList()))
                .thenReturn(Arrays.asList("DRONE-001", "DRONE-002"));

        mockMvc.perform(post("/api/v1/queryAvailableDrones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Collections.singletonList(dispatch))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("DRONE-001"))
                .andExpect(jsonPath("$[1]").value("DRONE-002"));
    }

    @Test
    void testQueryAvailableDrones_EmptyInput_ReturnsEmpty() throws Exception {
        mockMvc.perform(post("/api/v1/queryAvailableDrones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void testCalcDeliveryPath_ReturnsResponseWithStringDroneId() throws Exception {
        MedDispatchRec dispatch = new MedDispatchRec();
        dispatch.setId(1);
        dispatch.setDate("2025-01-06");
        dispatch.setTime("10:00");

        DeliveryRequirements req = new DeliveryRequirements();
        req.setCapacity(5.0);
        dispatch.setRequirements(req);
        dispatch.setDelivery(new LngLat(-3.187, 55.943));

        // Creates response with String drone ID
        DronePath dronePath = new DronePath();
        dronePath.setDroneId("DRONE-001");
        dronePath.setDeliveries(new ArrayList<>());

        DeliveryPathResponse mockResponse = new DeliveryPathResponse();
        mockResponse.setTotalCost(25.5);
        mockResponse.setTotalMoves(100);
        mockResponse.setDronePaths(Collections.singletonList(dronePath));

        when(pathPlanningService.calcDeliveryPath(anyList()))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/calcDeliveryPath")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Collections.singletonList(dispatch))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCost").value(25.5))
                .andExpect(jsonPath("$.totalMoves").value(100))
                .andExpect(jsonPath("$.dronePaths[0].droneId").value("DRONE-001"));
    }

    @Test
    void testCalcDeliveryPathAsGeoJson_ReturnsGeoJson() throws Exception {
        MedDispatchRec dispatch = new MedDispatchRec();
        dispatch.setId(1);
        dispatch.setDate("2025-01-06");
        dispatch.setTime("10:00");

        DeliveryRequirements req = new DeliveryRequirements();
        req.setCapacity(5.0);
        dispatch.setRequirements(req);
        dispatch.setDelivery(new LngLat(-3.187, 55.943));

        String mockGeoJson = "{\"type\":\"LineString\",\"coordinates\":[[-3.186,55.944]]}";

        when(pathPlanningService.calcDeliveryPathAsGeoJson(anyList()))
                .thenReturn(mockGeoJson);

        mockMvc.perform(post("/api/v1/calcDeliveryPathAsGeoJson")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Collections.singletonList(dispatch))))
                .andExpect(status().isOk())
                .andExpect(content().json(mockGeoJson));
    }

    @Test
    void testCalcDeliveryPath_OptionalFields_HandledGracefully() throws Exception {
        // Test with minimal required fields only
        String minimalDispatch = """
            [{
                "id": 1,
                "requirements": {
                    "capacity": 5.0
                },
                "delivery": {
                    "lng": -3.187,
                    "lat": 55.943
                }
            }]
            """;

        DeliveryPathResponse mockResponse = new DeliveryPathResponse();
        mockResponse.setTotalCost(15.0);
        mockResponse.setTotalMoves(50);
        mockResponse.setDronePaths(new ArrayList<>());

        when(pathPlanningService.calcDeliveryPath(anyList()))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/calcDeliveryPath")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(minimalDispatch))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCost").value(15.0));
    }

    @Test
    void testQueryAvailableDrones_CoolingAndHeating_BothRequired() throws Exception {
        String dispatchWithBoth = """
            [{
                "id": 1,
                "date": "2025-01-06",
                "time": "10:00",
                "requirements": {
                    "capacity": 5.0,
                    "cooling": true,
                    "heating": true
                },
                "delivery": {
                    "lng": -3.187,
                    "lat": 55.943
                }
            }]
            """;

        // Only drones with both capabilities
        when(availabilityService.queryAvailableDrones(anyList()))
                .thenReturn(Collections.singletonList("DRONE-001"));

        mockMvc.perform(post("/api/v1/queryAvailableDrones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dispatchWithBoth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("DRONE-001"));
    }

    @Test
    void testQueryAvailableDrones_WithMaxCost() throws Exception {
        String dispatchWithMaxCost = """
            [{
                "id": 1,
                "date": "2025-01-06",
                "time": "10:00",
                "requirements": {
                    "capacity": 5.0,
                    "maxCost": 50.0
                },
                "delivery": {
                    "lng": -3.187,
                    "lat": 55.943
                }
            }]
            """;

        when(availabilityService.queryAvailableDrones(anyList()))
                .thenReturn(Collections.singletonList("DRONE-001"));

        mockMvc.perform(post("/api/v1/queryAvailableDrones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dispatchWithMaxCost))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("DRONE-001"));
    }
}
