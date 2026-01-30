package uk.ac.ed.acp.cw2;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ed.acp.cw2.dto.*;
import uk.ac.ed.acp.cw2.model.*;
import uk.ac.ed.acp.cw2.service.AvailabilityService;
import uk.ac.ed.acp.cw2.service.IlpClientService;
import uk.ac.ed.acp.cw2.service.PathPlanningService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * Comprehensive tests for PathPlanningService (CORRECTED).
 * Coverage:
 * Drone IDs are STRINGS
 * Flight path starts at service point
 * Hover represented as 2 identical coordinates
 * Hover counts as 1 move
 * Return flight is separate delivery (deliveryId = -1)
 * TotalMoves calculation includes hover
 * GeoJSON generation
 */
@ExtendWith(MockitoExtension.class)
class PathPlanningTests {

    @Mock
    private IlpClientService ilpClientService;

    @Mock
    private AvailabilityService availabilityService;

    @InjectMocks
    private PathPlanningService pathPlanningService;

    private List<Drone> testDrones;
    private List<ServicePoint> testServicePoints;
    private List<DroneForServicePoint> testDronesForServicePoints;
    private List<RestrictedArea> testRestrictedAreas;

    @BeforeEach
    void setUp() {
        setupTestData();
    }

    private void setupTestData() {
        // Creates drone with STRING IDs
        DroneCapability fullCapability = new DroneCapability();
        fullCapability.setCooling(true);
        fullCapability.setHeating(true);
        fullCapability.setCapacity(10.0);
        fullCapability.setMaxMoves(2000);
        fullCapability.setCostPerMove(0.01);
        fullCapability.setCostInitial(10.0);
        fullCapability.setCostFinal(5.0);

        Drone drone1 = new Drone();
        drone1.setId("DRONE-001");
        drone1.setName("Drone Alpha");
        drone1.setCapability(fullCapability);

        testDrones = Collections.singletonList(drone1);

        // Creates service points
        ServicePoint sp1 = new ServicePoint(1, "Appleton Tower",
                new LngLat(-3.186874, 55.944494));

        testServicePoints = Collections.singletonList(sp1);

        // Creates availability
        DayAvailability mondayAvail = new DayAvailability();
        mondayAvail.setDayOfWeek("MONDAY");
        mondayAvail.setFrom("09:00:00");
        mondayAvail.setUntil("17:00:00");

        DroneForServicePoint.DroneAvailability drone1Avail =
                new DroneForServicePoint.DroneAvailability();
        drone1Avail.setId("DRONE-001");
        drone1Avail.setAvailability(Collections.singletonList(mondayAvail));

        DroneForServicePoint dfsp1 = new DroneForServicePoint();
        dfsp1.setServicePointId(1);
        dfsp1.setDrones(Collections.singletonList(drone1Avail));

        testDronesForServicePoints = Collections.singletonList(dfsp1);

        // Creates no-fly zone
        List<LngLat> noFlyVertices = Arrays.asList(
                new LngLat(-3.190578, 55.944494),
                new LngLat(-3.192473, 55.942617),
                new LngLat(-3.188915, 55.942617),
                new LngLat(-3.190578, 55.944494)
        );

        RestrictedArea noFlyZone = new RestrictedArea();
        noFlyZone.setId(1);
        noFlyZone.setName("George Square No-Fly Zone");
        noFlyZone.setVertices(noFlyVertices);

        RestrictedArea.AltitudeLimits noFlyLimits = new RestrictedArea.AltitudeLimits();
        noFlyLimits.setLower(0);
        noFlyLimits.setUpper(-1);
        noFlyZone.setLimits(noFlyLimits);

        testRestrictedAreas = Collections.singletonList(noFlyZone);
    }

    @Test
    void testCalcDeliveryPath_SingleDispatch_CorrectStructure() {
        MedDispatchRec dispatch = createDispatch(123, "2025-01-06", "14:30",
                5.0, new LngLat(-3.187, 55.943));

        when(ilpClientService.getDrones()).thenReturn(testDrones);
        when(ilpClientService.getServicePoints()).thenReturn(testServicePoints);
        when(ilpClientService.getDronesForServicePoints()).thenReturn(testDronesForServicePoints);
        when(ilpClientService.getRestrictedAreas()).thenReturn(testRestrictedAreas);
        when(availabilityService.queryAvailableDrones(anyList()))
                .thenReturn(Collections.singletonList("DRONE-001"));  // STRING

        DeliveryPathResponse response = pathPlanningService.calcDeliveryPath(
                Collections.singletonList(dispatch));

        // Verifies response structure
        assertNotNull(response);
        assertEquals(1, response.getDronePaths().size());
        assertTrue(response.getTotalCost() > 0);
        assertTrue(response.getTotalMoves() > 0);

        DronePath dronePath = response.getDronePaths().get(0);
        assertEquals("DRONE-001", dronePath.getDroneId(), "Drone ID should be STRING");
        assertEquals(2, dronePath.getDeliveries().size(), "Should have outbound + return");
    }

    @Test
    void testCalcDeliveryPath_FlightPathStartsAtServicePoint() {
        MedDispatchRec dispatch = createDispatch(123, "2025-01-06", "14:30",
                5.0, new LngLat(-3.187, 55.943));

        when(ilpClientService.getDrones()).thenReturn(testDrones);
        when(ilpClientService.getServicePoints()).thenReturn(testServicePoints);
        when(ilpClientService.getDronesForServicePoints()).thenReturn(testDronesForServicePoints);
        when(ilpClientService.getRestrictedAreas()).thenReturn(testRestrictedAreas);
        when(availabilityService.queryAvailableDrones(anyList()))
                .thenReturn(Collections.singletonList("DRONE-001"));

        DeliveryPathResponse response = pathPlanningService.calcDeliveryPath(
                Collections.singletonList(dispatch));

        // First coordinate should be service point
        Delivery outbound = response.getDronePaths().get(0).getDeliveries().get(0);
        LngLat firstCoord = outbound.getFlightPath().get(0);
        LngLat servicePoint = testServicePoints.get(0).getLocation();

        assertEquals(servicePoint.getLng(), firstCoord.getLng(), 0.0001,
                "First coordinate should be service point lng");
        assertEquals(servicePoint.getLat(), firstCoord.getLat(), 0.0001,
                "First coordinate should be service point lat");
    }

    @Test
    void testCalcDeliveryPath_HoverRepresentedAsTwoIdenticalCoordinates() {
        MedDispatchRec dispatch = createDispatch(123, "2025-01-06", "14:30",
                5.0, new LngLat(-3.187, 55.943));

        when(ilpClientService.getDrones()).thenReturn(testDrones);
        when(ilpClientService.getServicePoints()).thenReturn(testServicePoints);
        when(ilpClientService.getDronesForServicePoints()).thenReturn(testDronesForServicePoints);
        when(ilpClientService.getRestrictedAreas()).thenReturn(testRestrictedAreas);
        when(availabilityService.queryAvailableDrones(anyList()))
                .thenReturn(Collections.singletonList("DRONE-001"));

        DeliveryPathResponse response = pathPlanningService.calcDeliveryPath(
                Collections.singletonList(dispatch));

        // Hover should be 2 identical coordinates
        Delivery outbound = response.getDronePaths().get(0).getDeliveries().get(0);
        List<LngLat> flightPath = outbound.getFlightPath();

        boolean hasHover = false;
        for (int i = 0; i < flightPath.size() - 1; i++) {
            LngLat coord1 = flightPath.get(i);
            LngLat coord2 = flightPath.get(i + 1);

            if (coord1.getLng().equals(coord2.getLng()) &&
                    coord1.getLat().equals(coord2.getLat())) {
                hasHover = true;
                break;
            }
        }

        assertTrue(hasHover, "Flight path should contain hover (2 identical coordinates)");
    }

    @Test
    void testCalcDeliveryPath_ReturnFlightIsSeparateDelivery() {
        MedDispatchRec dispatch = createDispatch(123, "2025-01-06", "14:30",
                5.0, new LngLat(-3.187, 55.943));

        when(ilpClientService.getDrones()).thenReturn(testDrones);
        when(ilpClientService.getServicePoints()).thenReturn(testServicePoints);
        when(ilpClientService.getDronesForServicePoints()).thenReturn(testDronesForServicePoints);
        when(ilpClientService.getRestrictedAreas()).thenReturn(testRestrictedAreas);
        when(availabilityService.queryAvailableDrones(anyList()))
                .thenReturn(Collections.singletonList("DRONE-001"));

        DeliveryPathResponse response = pathPlanningService.calcDeliveryPath(
                Collections.singletonList(dispatch));

        // Should have 2 deliveries
        List<Delivery> deliveries = response.getDronePaths().get(0).getDeliveries();
        assertEquals(2, deliveries.size(), "Should have outbound + return");

        // First delivery has actual deliveryId
        assertEquals(123, deliveries.get(0).getDeliveryId(),
                "First delivery should have actual ID");

        // Second delivery is return flight (using -1 for deliveryID)
        assertEquals(-1, deliveries.get(1).getDeliveryId(),
                "Return flight should have deliveryId = -1");
    }

    @Test
    void testCalcDeliveryPath_TotalMovesIncludesHover() {
        MedDispatchRec dispatch = createDispatch(123, "2025-01-06", "14:30",
                5.0, new LngLat(-3.187, 55.943));

        when(ilpClientService.getDrones()).thenReturn(testDrones);
        when(ilpClientService.getServicePoints()).thenReturn(testServicePoints);
        when(ilpClientService.getDronesForServicePoints()).thenReturn(testDronesForServicePoints);
        when(ilpClientService.getRestrictedAreas()).thenReturn(testRestrictedAreas);
        when(availabilityService.queryAvailableDrones(anyList()))
                .thenReturn(Collections.singletonList("DRONE-001"));

        DeliveryPathResponse response = pathPlanningService.calcDeliveryPath(
                Collections.singletonList(dispatch));

        // Total moves = coordinates - 1 (for each segment) + hover
        List<Delivery> deliveries = response.getDronePaths().get(0).getDeliveries();

        int expectedMoves = 0;
        for (Delivery delivery : deliveries) {
            expectedMoves += delivery.getFlightPath().size() - 1;
        }

        assertEquals(expectedMoves, response.getTotalMoves(),
                "Total moves should match sum of all segment moves including hover");
    }

    @Test
    void testCalcDeliveryPath_CostCalculation() {
        MedDispatchRec dispatch = createDispatch(123, "2025-01-06", "14:30",
                5.0, new LngLat(-3.187, 55.943));

        when(ilpClientService.getDrones()).thenReturn(testDrones);
        when(ilpClientService.getServicePoints()).thenReturn(testServicePoints);
        when(ilpClientService.getDronesForServicePoints()).thenReturn(testDronesForServicePoints);
        when(ilpClientService.getRestrictedAreas()).thenReturn(testRestrictedAreas);
        when(availabilityService.queryAvailableDrones(anyList()))
                .thenReturn(Collections.singletonList("DRONE-001"));

        DeliveryPathResponse response = pathPlanningService.calcDeliveryPath(
                Collections.singletonList(dispatch));

        // Cost should be initial + final + (moves * costPerMove)
        Drone drone = testDrones.getFirst();
        DroneCapability cap = drone.getCapability();

        double expectedCost = cap.getCostInitial() + cap.getCostFinal() +
                (response.getTotalMoves() * cap.getCostPerMove());

        assertEquals(expectedCost, response.getTotalCost(), 0.01,
                "Cost should equal initial + final + (moves * costPerMove)");
    }

    @Test
    void testCalcDeliveryPath_NullInput_ReturnsEmpty() {
        DeliveryPathResponse response = pathPlanningService.calcDeliveryPath(null);

        // Should return empty response
        assertNotNull(response);
        assertEquals(0.0, response.getTotalCost());
        assertEquals(0, response.getTotalMoves());
        assertTrue(response.getDronePaths().isEmpty());
    }

    @Test
    void testCalcDeliveryPath_EmptyInput_ReturnsEmpty() {
        DeliveryPathResponse response = pathPlanningService.calcDeliveryPath(new ArrayList<>());

        // Should return empty response
        assertNotNull(response);
        assertEquals(0.0, response.getTotalCost());
        assertEquals(0, response.getTotalMoves());
        assertTrue(response.getDronePaths().isEmpty());
    }

    @Test
    void testCalcDeliveryPath_NoAvailableDrone_ReturnsEmpty() {
        MedDispatchRec dispatch = createDispatch(123, "2025-01-06", "14:30",
                5.0, new LngLat(-3.187, 55.943));

        when(ilpClientService.getDrones()).thenReturn(testDrones);
        when(ilpClientService.getServicePoints()).thenReturn(testServicePoints);
        when(ilpClientService.getDronesForServicePoints()).thenReturn(testDronesForServicePoints);
        when(ilpClientService.getRestrictedAreas()).thenReturn(testRestrictedAreas);
        when(availabilityService.queryAvailableDrones(anyList()))
                .thenReturn(Collections.emptyList());  // No drones available

        DeliveryPathResponse response = pathPlanningService.calcDeliveryPath(
                Collections.singletonList(dispatch));

        // Should return empty paths
        assertNotNull(response);
        assertTrue(response.getDronePaths().isEmpty());
    }

    @Test
    void testCalcDeliveryPathAsGeoJson_ValidInput_ReturnsLineString() {
        MedDispatchRec dispatch = createDispatch(123, "2025-01-06", "14:30",
                5.0, new LngLat(-3.187, 55.943));

        when(ilpClientService.getDrones()).thenReturn(testDrones);
        when(ilpClientService.getServicePoints()).thenReturn(testServicePoints);
        when(ilpClientService.getDronesForServicePoints()).thenReturn(testDronesForServicePoints);
        when(ilpClientService.getRestrictedAreas()).thenReturn(testRestrictedAreas);
        when(availabilityService.queryAvailableDrones(anyList()))
                .thenReturn(Collections.singletonList("DRONE-001"));

        String geoJson = pathPlanningService.calcDeliveryPathAsGeoJson(
                Collections.singletonList(dispatch));

        // Should be valid GeoJSON LineString
        assertNotNull(geoJson);
        assertTrue(geoJson.contains("\"type\":\"LineString\""));
        assertTrue(geoJson.contains("\"coordinates\""));
        assertTrue(geoJson.startsWith("{"));
        assertTrue(geoJson.endsWith("}"));

        // Verifies coordinate format [lng,lat]
        assertTrue(geoJson.matches(".*\\[-?\\d+\\.\\d+,-?\\d+\\.\\d+].*"),
                "Should contain coordinate pairs in [lng,lat] format");
    }

    @Test
    void testCalcDeliveryPathAsGeoJson_EmptyInput_ReturnsEmptyFeatureCollection() {
        String geoJson = pathPlanningService.calcDeliveryPathAsGeoJson(new ArrayList<>());

        // Should return empty FeatureCollection
        assertNotNull(geoJson);
        assertTrue(geoJson.contains("\"type\":\"FeatureCollection\""));
        assertTrue(geoJson.contains("\"features\":[]"));
    }

    @Test
    void testCalcDeliveryPath_VerifyServiceCalls() {
        MedDispatchRec dispatch = createDispatch(123, "2025-01-06", "14:30",
                5.0, new LngLat(-3.187, 55.943));

        when(ilpClientService.getDrones()).thenReturn(testDrones);
        when(ilpClientService.getServicePoints()).thenReturn(testServicePoints);
        when(ilpClientService.getDronesForServicePoints()).thenReturn(testDronesForServicePoints);
        when(ilpClientService.getRestrictedAreas()).thenReturn(testRestrictedAreas);
        when(availabilityService.queryAvailableDrones(anyList()))
                .thenReturn(Collections.singletonList("DRONE-001"));

        pathPlanningService.calcDeliveryPath(Collections.singletonList(dispatch));

        // Verifies all required data was fetched
        verify(ilpClientService, times(1)).getDrones();
        verify(ilpClientService, times(1)).getServicePoints();
        verify(ilpClientService, times(1)).getDronesForServicePoints();
        verify(ilpClientService, times(1)).getRestrictedAreas();
        verify(availabilityService, times(1)).queryAvailableDrones(anyList());
    }

    // Helper method
    private MedDispatchRec createDispatch(int id, String date, String time,
                                          double capacity, LngLat delivery) {
        MedDispatchRec dispatch = new MedDispatchRec();
        dispatch.setId(id);
        dispatch.setDate(date);
        dispatch.setTime(time);

        DeliveryRequirements req = new DeliveryRequirements();
        req.setCapacity(capacity);
        dispatch.setRequirements(req);

        dispatch.setDelivery(delivery);

        return dispatch;
    }
}
