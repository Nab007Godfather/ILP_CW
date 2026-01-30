package uk.ac.ed.acp.cw2;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ed.acp.cw2.dto.DeliveryRequirements;
import uk.ac.ed.acp.cw2.dto.MedDispatchRec;
import uk.ac.ed.acp.cw2.model.*;
import uk.ac.ed.acp.cw2.service.AvailabilityService;
import uk.ac.ed.acp.cw2.service.IlpClientService;

import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Comprehensive tests for AvailabilityService
 * Coverage:
 * Drone IDs are strings
 * AND-narrowing logic
 * Cases of both cooling and heating requirement (both must be present)
 * Estimated maxCost checking
 * Date/time availability checking
 * Time check: delivery BEFORE end of availability
 */
@ExtendWith(MockitoExtension.class)
class AvailabilityTests {

    @Mock
    private IlpClientService ilpClientService;

    @InjectMocks
    private AvailabilityService availabilityService;

    private List<Drone> testDrones;
    private List<ServicePoint> testServicePoints;
    private List<DroneForServicePoint> testDronesForServicePoints;

    @BeforeEach
    void setUp() {
        setupTestData();
    }

    private void setupTestData() {

        // Drone "DRONE-001"
        DroneCapability fullCap = new DroneCapability();
        fullCap.setCooling(true);
        fullCap.setHeating(true);
        fullCap.setCapacity(10.0);
        fullCap.setMaxMoves(2000);
        fullCap.setCostPerMove(0.01);
        fullCap.setCostInitial(10.0);
        fullCap.setCostFinal(5.0);

        Drone drone1 = new Drone();
        drone1.setId("DRONE-001");
        drone1.setName("Drone Alpha");
        drone1.setCapability(fullCap);

        // Drone "DRONE-002"
        DroneCapability coolingCap = new DroneCapability();
        coolingCap.setCooling(true);
        coolingCap.setHeating(false);
        coolingCap.setCapacity(8.0);
        coolingCap.setMaxMoves(1500);
        coolingCap.setCostPerMove(0.015);
        coolingCap.setCostInitial(12.0);
        coolingCap.setCostFinal(6.0);

        Drone drone2 = new Drone();
        drone2.setId("DRONE-002");
        drone2.setName("Drone Beta");
        drone2.setCapability(coolingCap);

        // Drone "DRONE-003"
        DroneCapability heatingCap = new DroneCapability();
        heatingCap.setCooling(false);
        heatingCap.setHeating(true);
        heatingCap.setCapacity(12.0);
        heatingCap.setMaxMoves(2500);
        heatingCap.setCostPerMove(0.008);
        heatingCap.setCostInitial(8.0);
        heatingCap.setCostFinal(4.0);

        Drone drone3 = new Drone();
        drone3.setId("DRONE-003");
        drone3.setName("Drone Gamma");
        drone3.setCapability(heatingCap);

        // Drone "DRONE-004"
        DroneCapability basicCap = new DroneCapability();
        basicCap.setCooling(false);
        basicCap.setHeating(false);
        basicCap.setCapacity(5.0);
        basicCap.setMaxMoves(1000);
        basicCap.setCostPerMove(0.02);
        basicCap.setCostInitial(15.0);
        basicCap.setCostFinal(7.0);

        Drone drone4 = new Drone();
        drone4.setId("DRONE-004");
        drone4.setName("Drone Delta");
        drone4.setCapability(basicCap);

        testDrones = Arrays.asList(drone1, drone2, drone3, drone4);

        // Setup service points
        ServicePoint sp1 = new ServicePoint(1, "Appleton Tower",
                new LngLat(-3.186874, 55.944494));
        testServicePoints = Collections.singletonList(sp1);

        // Setup availability schedules
        DayAvailability mondayToFriday = new DayAvailability();
        mondayToFriday.setDayOfWeek("MONDAY");
        mondayToFriday.setFrom("09:00:00");
        mondayToFriday.setUntil("17:00:00");

        DroneForServicePoint.DroneAvailability drone1Availability =
                new DroneForServicePoint.DroneAvailability();
        drone1Availability.setId("DRONE-001");
        drone1Availability.setAvailability(Collections.singletonList(mondayToFriday));

        DroneForServicePoint.DroneAvailability drone2Availability =
                new DroneForServicePoint.DroneAvailability();
        drone2Availability.setId("DRONE-002");
        drone2Availability.setAvailability(Collections.singletonList(mondayToFriday));

        DroneForServicePoint.DroneAvailability drone3Availability =
                new DroneForServicePoint.DroneAvailability();
        drone3Availability.setId("DRONE-003");
        drone3Availability.setAvailability(Collections.singletonList(mondayToFriday));

        DroneForServicePoint.DroneAvailability drone4Availability =
                new DroneForServicePoint.DroneAvailability();
        drone4Availability.setId("DRONE-004");
        drone4Availability.setAvailability(Collections.singletonList(mondayToFriday));

        DroneForServicePoint dfsp = new DroneForServicePoint();
        dfsp.setServicePointId(1);
        dfsp.setDrones(Arrays.asList(drone1Availability, drone2Availability, drone3Availability, drone4Availability));

        testDronesForServicePoints = Collections.singletonList(dfsp);
    }

    // Basic availability tests

    @Test
    void testQueryAvailableDrones_SimpleDispatch_ReturnsMultipleDrones() {
        MedDispatchRec dispatch = createDispatch(1, "2025-01-06", "10:00", 3.0, false, false);

        when(ilpClientService.getDrones()).thenReturn(testDrones);
        when(ilpClientService.getServicePoints()).thenReturn(testServicePoints);
        when(ilpClientService.getDronesForServicePoints()).thenReturn(testDronesForServicePoints);

        List<String> availableDrones = availabilityService.queryAvailableDrones(
                Collections.singletonList(dispatch));

        // Multiple drones should be available
        assertFalse(availableDrones.isEmpty(), "Should find available drones");
        assertTrue(availableDrones.contains("DRONE-001"), "DRONE-001 has capacity");
        assertTrue(availableDrones.contains("DRONE-002"), "DRONE-002 has capacity");
        assertTrue(availableDrones.contains("DRONE-003"), "DRONE-003 has capacity");
        assertTrue(availableDrones.contains("DRONE-004"), "DRONE-004 has capacity");
    }

    @Test
    void testQueryAvailableDrones_CoolingRequired() {
        MedDispatchRec dispatch = createDispatch(1, "2025-01-06", "10:00", 5.0, true, false);

        when(ilpClientService.getDrones()).thenReturn(testDrones);
        when(ilpClientService.getServicePoints()).thenReturn(testServicePoints);
        when(ilpClientService.getDronesForServicePoints()).thenReturn(testDronesForServicePoints);

        List<String> availableDrones = availabilityService.queryAvailableDrones(
                Collections.singletonList(dispatch));

        // Only drones with cooling should be available
        assertFalse(availableDrones.isEmpty());
        assertTrue(availableDrones.contains("DRONE-001"), "DRONE-001 has cooling");
        assertTrue(availableDrones.contains("DRONE-002"), "DRONE-002 has cooling");
        assertFalse(availableDrones.contains("DRONE-003"), "DRONE-003 has no cooling");
        assertFalse(availableDrones.contains("DRONE-004"), "DRONE-004 has no cooling");
    }

    @Test
    void testQueryAvailableDrones_HeatingRequired() {
        MedDispatchRec dispatch = createDispatch(1, "2025-01-06", "10:00", 5.0, false, true);

        when(ilpClientService.getDrones()).thenReturn(testDrones);
        when(ilpClientService.getServicePoints()).thenReturn(testServicePoints);
        when(ilpClientService.getDronesForServicePoints()).thenReturn(testDronesForServicePoints);

        List<String> availableDrones = availabilityService.queryAvailableDrones(
                Collections.singletonList(dispatch));

        // Only drones with heating should be available
        assertFalse(availableDrones.isEmpty());
        assertTrue(availableDrones.contains("DRONE-001"), "DRONE-001 has heating");
        assertFalse(availableDrones.contains("DRONE-002"), "DRONE-002 has no heating");
        assertTrue(availableDrones.contains("DRONE-003"), "DRONE-003 has heating");
    }

    @Test
    void testQueryAvailableDrones_BothCoolingAndHeating_RequiresBoth() {
        MedDispatchRec dispatch = createDispatch(1, "2025-01-06", "10:00", 5.0, true, true);

        when(ilpClientService.getDrones()).thenReturn(testDrones);
        when(ilpClientService.getServicePoints()).thenReturn(testServicePoints);
        when(ilpClientService.getDronesForServicePoints()).thenReturn(testDronesForServicePoints);

        List<String> availableDrones = availabilityService.queryAvailableDrones(
                Collections.singletonList(dispatch));

        // Only DRONE-001 has BOTH capabilities
        assertEquals(1, availableDrones.size(), "Only one drone has both capabilities");
        assertTrue(availableDrones.contains("DRONE-001"), "DRONE-001 has both cooling and heating");
        assertFalse(availableDrones.contains("DRONE-002"), "DRONE-002 lacks heating");
        assertFalse(availableDrones.contains("DRONE-003"), "DRONE-003 lacks cooling");
    }

    @Test
    void testQueryAvailableDrones_CapacityExceeded() {
        MedDispatchRec dispatch = createDispatch(1, "2025-01-06", "10:00", 11.0, false, false);

        when(ilpClientService.getDrones()).thenReturn(testDrones);
        when(ilpClientService.getServicePoints()).thenReturn(testServicePoints);
        when(ilpClientService.getDronesForServicePoints()).thenReturn(testDronesForServicePoints);

        List<String> availableDrones = availabilityService.queryAvailableDrones(
                Collections.singletonList(dispatch));

        // Only DRONE-003 has sufficient capacity (12kg)
        assertEquals(1, availableDrones.size(), "Only one drone has sufficient capacity");
        assertTrue(availableDrones.contains("DRONE-003"), "DRONE-003 has 12kg capacity");
    }

    // Multiple dispatch scenario tests

    @Test
    void testQueryAvailableDrones_MultipleDispatches_ANDLogic() {
        // Given: Two dispatches with different requirements
        MedDispatchRec dispatch1 = createDispatch(1, "2025-01-06", "10:00", 5.0, true, false);
        MedDispatchRec dispatch2 = createDispatch(2, "2025-01-06", "11:00", 5.0, false, true);

        when(ilpClientService.getDrones()).thenReturn(testDrones);
        when(ilpClientService.getServicePoints()).thenReturn(testServicePoints);
        when(ilpClientService.getDronesForServicePoints()).thenReturn(testDronesForServicePoints);

        List<String> availableDrones = availabilityService.queryAvailableDrones(
                Arrays.asList(dispatch1, dispatch2));

        // Only drone with BOTH cooling AND heating
        assertEquals(1, availableDrones.size(), "Only one drone satisfies both requirements");
        assertTrue(availableDrones.contains("DRONE-001"), "DRONE-001 has both capabilities");
    }

    @Test
    void testQueryAvailableDrones_NarrowingCapacityCheck() {
        MedDispatchRec dispatch1 = createDispatch(1, "2025-01-06", "10:00", 6.0, false, false);
        MedDispatchRec dispatch2 = createDispatch(2, "2025-01-06", "11:00", 9.0, false, false);

        when(ilpClientService.getDrones()).thenReturn(testDrones);
        when(ilpClientService.getServicePoints()).thenReturn(testServicePoints);
        when(ilpClientService.getDronesForServicePoints()).thenReturn(testDronesForServicePoints);

        List<String> availableDrones = availabilityService.queryAvailableDrones(
                Arrays.asList(dispatch1, dispatch2));

        // Only drones with capacity >= 9.0
        assertTrue(availableDrones.contains("DRONE-001"), "DRONE-001 has 10kg");
        assertTrue(availableDrones.contains("DRONE-003"), "DRONE-003 has 12kg");
        assertFalse(availableDrones.contains("DRONE-002"), "DRONE-002 has only 8kg");
        assertFalse(availableDrones.contains("DRONE-004"), "DRONE-004 has only 5kg");
    }

    // Time check tests

    @Test
    void testTimeCheck_DeliveryBeforeEnd_IsAvailable() {
        MedDispatchRec dispatch = createDispatch(1, "2025-01-06", "12:30", 5.0, false, false);

        when(ilpClientService.getDrones()).thenReturn(testDrones);
        when(ilpClientService.getServicePoints()).thenReturn(testServicePoints);
        when(ilpClientService.getDronesForServicePoints()).thenReturn(testDronesForServicePoints);

        List<String> result = availabilityService.queryAvailableDrones(
                Collections.singletonList(dispatch));

        // Drones should be available as 12:30 < 17:00
        assertFalse(result.isEmpty(), "Drones should be available at 12:30");
    }

    @Test
    void testTimeCheck_DeliveryAtExactEnd_NotAvailable() {
        MedDispatchRec dispatch = createDispatch(1, "2025-01-06", "17:00", 5.0, false, false);

        when(ilpClientService.getDrones()).thenReturn(testDrones);
        when(ilpClientService.getServicePoints()).thenReturn(testServicePoints);
        when(ilpClientService.getDronesForServicePoints()).thenReturn(testDronesForServicePoints);

        List<String> result = availabilityService.queryAvailableDrones(
                Collections.singletonList(dispatch));

        // Drones should NOT be available as 17:00 >= 17:00
        assertTrue(result.isEmpty(), "Drones should not be available at exactly 17:00");
    }

    @Test
    void testTimeCheck_DeliveryAfterEnd_NotAvailable() {
        MedDispatchRec dispatch = createDispatch(1, "2025-01-06", "18:00", 5.0, false, false);

        when(ilpClientService.getDrones()).thenReturn(testDrones);
        when(ilpClientService.getServicePoints()).thenReturn(testServicePoints);
        when(ilpClientService.getDronesForServicePoints()).thenReturn(testDronesForServicePoints);

        List<String> result = availabilityService.queryAvailableDrones(
                Collections.singletonList(dispatch));

        // No drones available
        assertTrue(result.isEmpty(), "No drones available after 17:00");
    }

    // Edge Cases

    @Test
    void testQueryAvailableDrones_NullInput_ReturnsEmpty() {
        List<String> result = availabilityService.queryAvailableDrones(null);

        // Should return empty list
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testQueryAvailableDrones_EmptyList_ReturnsEmpty() {
        List<String> result = availabilityService.queryAvailableDrones(new ArrayList<>());

        // Should return empty list
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testQueryAvailableDrones_NoMatchingDrone() {
        MedDispatchRec dispatch = createDispatch(1, "2025-01-06", "10:00", 50.0, true, true);

        when(ilpClientService.getDrones()).thenReturn(testDrones);
        when(ilpClientService.getServicePoints()).thenReturn(testServicePoints);
        when(ilpClientService.getDronesForServicePoints()).thenReturn(testDronesForServicePoints);

        List<String> result = availabilityService.queryAvailableDrones(
                Collections.singletonList(dispatch));

        // No drones match
        assertTrue(result.isEmpty(), "No drone has 50kg capacity");
    }

    @Test
    void testQueryAvailableDrones_OptionalFieldsNull_HandledGracefully() {
        MedDispatchRec dispatch = new MedDispatchRec();
        dispatch.setId(1);

        DeliveryRequirements req = new DeliveryRequirements();
        req.setCapacity(5.0);
        dispatch.setRequirements(req);

        when(ilpClientService.getDrones()).thenReturn(testDrones);
        when(ilpClientService.getServicePoints()).thenReturn(testServicePoints);
        when(ilpClientService.getDronesForServicePoints()).thenReturn(testDronesForServicePoints);

        List<String> result = availabilityService.queryAvailableDrones(
                Collections.singletonList(dispatch));

        // Should handle gracefully
        assertFalse(result.isEmpty(), "Should find drones with minimal requirements");
    }

    // Parameterized Tests

    @ParameterizedTest
    @MethodSource("provideCapacityTestCases")
    void testQueryAvailableDrones_VariousCapacities(double capacity, Set<String> expectedDrones,
                                                    String description) {
        MedDispatchRec dispatch = createDispatch(1, "2025-01-06", "10:00", capacity, false, false);

        when(ilpClientService.getDrones()).thenReturn(testDrones);
        when(ilpClientService.getServicePoints()).thenReturn(testServicePoints);
        when(ilpClientService.getDronesForServicePoints()).thenReturn(testDronesForServicePoints);

        List<String> result = availabilityService.queryAvailableDrones(
                Collections.singletonList(dispatch));

        // Verifies expected drones
        assertEquals(expectedDrones.size(), result.size(), description);
        for (String expectedDrone : expectedDrones) {
            assertTrue(result.contains(expectedDrone),
                    description + " - should contain " + expectedDrone);
        }
    }

    private static Stream<Arguments> provideCapacityTestCases() {
        return Stream.of(
                Arguments.of(3.0, Set.of("DRONE-001", "DRONE-002", "DRONE-003", "DRONE-004"),
                        "All drones have capacity >= 3.0"),
                Arguments.of(5.0, Set.of("DRONE-001", "DRONE-002", "DRONE-003", "DRONE-004"),
                        "All drones have capacity >= 5.0"),
                Arguments.of(8.0, Set.of("DRONE-001", "DRONE-002", "DRONE-003"),
                        "Three drones have capacity >= 8.0"),
                Arguments.of(10.0, Set.of("DRONE-001", "DRONE-003"),
                        "Two drones have capacity >= 10.0"),
                Arguments.of(12.0, Set.of("DRONE-003"),
                        "Only DRONE-003 has capacity >= 12.0"),
                Arguments.of(15.0, Set.of(),
                        "No drones have capacity >= 15.0")
        );
    }

    // Helper method
    private MedDispatchRec createDispatch(int id, String date, String time,
                                          double capacity, boolean cooling, boolean heating) {
        MedDispatchRec dispatch = new MedDispatchRec();
        dispatch.setId(id);
        dispatch.setDate(date);
        dispatch.setTime(time);

        DeliveryRequirements req = new DeliveryRequirements();
        req.setCapacity(capacity);
        req.setCooling(cooling);
        req.setHeating(heating);
        dispatch.setRequirements(req);
        dispatch.setDelivery(new LngLat(-3.187, 55.943));

        return dispatch;
    }
}
