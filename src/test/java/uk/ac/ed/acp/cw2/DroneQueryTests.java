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
import uk.ac.ed.acp.cw2.dto.QueryAttribute;
import uk.ac.ed.acp.cw2.model.Drone;
import uk.ac.ed.acp.cw2.model.DroneCapability;
import uk.ac.ed.acp.cw2.service.IlpClientService;
import uk.ac.ed.acp.cw2.service.QueryService;

import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Comprehensive tests for QueryService (CORRECTED).
 * Coverage:
 * Drone IDs are STRINGS
 * All query methods return List<String>
 * droneDetails accepts String ID
 * Cooling/heating queries
 * Single and multiple attribute queries
 * Numeric operators (=, !=, <, >, <=, >=)
 */
@ExtendWith(MockitoExtension.class)
class DroneQueryTests {

    @Mock
    private IlpClientService ilpClientService;

    @InjectMocks
    private QueryService queryService;

    private List<Drone> testDrones;

    @BeforeEach
    void setUp() {
        setupTestData();
    }

    private void setupTestData() {
        // Drone "DRONE-001": Full capability
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

        // Drone "DRONE-002": Cooling only
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

        // Drone "DRONE-003": Heating only
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

        // Drone "DRONE-004": Basic
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
    }

    // getDronesWithCooling Tests

    @Test
    void testGetDronesWithCooling_True_ReturnsStringIds() {
        when(ilpClientService.getDrones()).thenReturn(testDrones);

        List<String> result = queryService.getDronesWithCooling(true);

        // Should return String IDs
        assertEquals(2, result.size(), "Should find 2 drones with cooling");
        assertTrue(result.contains("DRONE-001"), "DRONE-001 has cooling");
        assertTrue(result.contains("DRONE-002"), "DRONE-002 has cooling");
        assertFalse(result.contains("DRONE-003"), "DRONE-003 has no cooling");
    }

    @Test
    void testGetDronesWithCooling_False_ReturnsNonCoolingDrones() {
        when(ilpClientService.getDrones()).thenReturn(testDrones);

        List<String> result = queryService.getDronesWithCooling(false);

        // Should return String IDs
        assertEquals(2, result.size(), "Should find 2 drones without cooling");
        assertTrue(result.contains("DRONE-003"), "DRONE-003 has no cooling");
        assertTrue(result.contains("DRONE-004"), "DRONE-004 has no cooling");
    }

    @Test
    void testGetDronesWithCooling_EmptyList_ReturnsEmpty() {
        when(ilpClientService.getDrones()).thenReturn(Collections.emptyList());

        List<String> result = queryService.getDronesWithCooling(true);

        // Should return empty
        assertTrue(result.isEmpty());
    }

    // getDroneById Tests

    @Test
    void testGetDroneById_ValidStringId_ReturnsDrone() {
        when(ilpClientService.getDrones()).thenReturn(testDrones);

        Drone result = queryService.getDroneById("DRONE-002");

        assertNotNull(result, "Should find DRONE-002");
        assertEquals("DRONE-002", result.getId(), "ID should match");
        assertEquals("Drone Beta", result.getName(), "Name should match");
    }

    @Test
    void testGetDroneById_InvalidId_ReturnsNull() {
        when(ilpClientService.getDrones()).thenReturn(testDrones);

        Drone result = queryService.getDroneById("DRONE-999");

        // Should return null
        assertNull(result, "Should return null for non-existent ID");
    }

    @Test
    void testGetDroneById_EmptyList_ReturnsNull() {
        when(ilpClientService.getDrones()).thenReturn(Collections.emptyList());

        Drone result = queryService.getDroneById("DRONE-001");

        // Should return null
        assertNull(result);
    }

    // queryByPath Tests

    @Test
    void testQueryByPath_Capacity_ReturnsStringIds() {
        when(ilpClientService.getDrones()).thenReturn(testDrones);

        List<String> result = queryService.queryByPath("capacity", "10.0");

        assertEquals(1, result.size(), "Should find 1 drone with capacity 10.0");
        assertTrue(result.contains("DRONE-001"), "DRONE-001 has capacity 10.0");
    }

    @Test
    void testQueryByPath_Cooling_BooleanMatch() {
        when(ilpClientService.getDrones()).thenReturn(testDrones);

        List<String> result = queryService.queryByPath("cooling", "true");

        // Should return String IDs
        assertEquals(2, result.size(), "Should find 2 drones with cooling");
        assertTrue(result.contains("DRONE-001"));
        assertTrue(result.contains("DRONE-002"));
    }

    @Test
    void testQueryByPath_Name_StringMatch() {
        when(ilpClientService.getDrones()).thenReturn(testDrones);

        List<String> result = queryService.queryByPath("name", "Drone Alpha");

        // Should return String ID
        assertEquals(1, result.size());
        assertTrue(result.contains("DRONE-001"));
    }

    @Test
    void testQueryByPath_Id_StringMatch() {
        when(ilpClientService.getDrones()).thenReturn(testDrones);

        List<String> result = queryService.queryByPath("id", "DRONE-002");

        // Should find drone
        assertEquals(1, result.size());
        assertTrue(result.contains("DRONE-002"));
    }

    // query (POST) tests

    @Test
    void testQuery_SingleCondition_ReturnsStringIds() {
        QueryAttribute qa = new QueryAttribute();
        qa.setAttribute("capacity");
        qa.setOperator("=");
        qa.setValue("8.0");

        when(ilpClientService.getDrones()).thenReturn(testDrones);

        List<String> result = queryService.query(Collections.singletonList(qa));

        // Should return String ID
        assertEquals(1, result.size());
        assertTrue(result.contains("DRONE-002"), "DRONE-002 has capacity 8.0");
    }

    @Test
    void testQuery_MultipleConditions_ANDLogic() {
        QueryAttribute qa1 = new QueryAttribute();
        qa1.setAttribute("cooling");
        qa1.setOperator("=");
        qa1.setValue("true");

        QueryAttribute qa2 = new QueryAttribute();
        qa2.setAttribute("capacity");
        qa2.setOperator(">");
        qa2.setValue("7.0");

        when(ilpClientService.getDrones()).thenReturn(testDrones);

        List<String> result = queryService.query(Arrays.asList(qa1, qa2));

        // Should return String IDs
        assertEquals(2, result.size(), "Should find DRONE-001 and DRONE-002");
        assertTrue(result.contains("DRONE-001"));
        assertTrue(result.contains("DRONE-002"));
    }

    @Test
    void testQuery_LessThanOperator() {
        QueryAttribute qa = new QueryAttribute();
        qa.setAttribute("capacity");
        qa.setOperator("<");
        qa.setValue("10.0");

        when(ilpClientService.getDrones()).thenReturn(testDrones);

        List<String> result = queryService.query(Collections.singletonList(qa));

        // Should return String IDs
        assertEquals(2, result.size());
        assertTrue(result.contains("DRONE-002"), "DRONE-002 has capacity 8.0");
        assertTrue(result.contains("DRONE-004"), "DRONE-004 has capacity 5.0");
    }

    @Test
    void testQuery_GreaterThanOperator() {
        QueryAttribute qa = new QueryAttribute();
        qa.setAttribute("maxMoves");
        qa.setOperator(">");
        qa.setValue("2000");

        when(ilpClientService.getDrones()).thenReturn(testDrones);

        List<String> result = queryService.query(Collections.singletonList(qa));

        // Should return String ID
        assertEquals(1, result.size());
        assertTrue(result.contains("DRONE-003"), "DRONE-003 has 2500 maxMoves");
    }

    @Test
    void testQuery_NotEqualsOperator() {
        QueryAttribute qa = new QueryAttribute();
        qa.setAttribute("cooling");
        qa.setOperator("!=");
        qa.setValue("true");

        when(ilpClientService.getDrones()).thenReturn(testDrones);

        List<String> result = queryService.query(Collections.singletonList(qa));

        // Should return String IDs
        assertEquals(2, result.size());
        assertTrue(result.contains("DRONE-003"));
        assertTrue(result.contains("DRONE-004"));
    }

    @Test
    void testQuery_ComplexMultiCondition() {
        QueryAttribute qa1 = new QueryAttribute();
        qa1.setAttribute("capacity");
        qa1.setOperator(">=");
        qa1.setValue("8.0");

        QueryAttribute qa2 = new QueryAttribute();
        qa2.setAttribute("costPerMove");
        qa2.setOperator("<=");
        qa2.setValue("0.01");

        QueryAttribute qa3 = new QueryAttribute();
        qa3.setAttribute("heating");
        qa3.setOperator("=");
        qa3.setValue("true");

        when(ilpClientService.getDrones()).thenReturn(testDrones);

        List<String> result = queryService.query(Arrays.asList(qa1, qa2, qa3));

        // Should return String IDs
        assertEquals(2, result.size());
        assertTrue(result.contains("DRONE-001"));
        assertTrue(result.contains("DRONE-003"));
    }

    // Parameterized Tests

    @ParameterizedTest
    @MethodSource("provideNumericOperatorTests")
    void testQuery_NumericOperators(String attribute, String operator,
                                    String value, Set<String> expectedIds,
                                    String description) {
        QueryAttribute qa = new QueryAttribute();
        qa.setAttribute(attribute);
        qa.setOperator(operator);
        qa.setValue(value);

        when(ilpClientService.getDrones()).thenReturn(testDrones);

        List<String> result = queryService.query(Collections.singletonList(qa));

        // Verifies expected String IDs
        assertEquals(expectedIds.size(), result.size(), description);
        for (String expectedId : expectedIds) {
            assertTrue(result.contains(expectedId),
                    description + " - should contain " + expectedId);
        }
    }

    private static Stream<Arguments> provideNumericOperatorTests() {
        return Stream.of(
                Arguments.of("capacity", "=", "10.0", Set.of("DRONE-001"),
                        "Exact capacity match"),
                Arguments.of("capacity", "<", "9.0", Set.of("DRONE-002", "DRONE-004"),
                        "Capacity less than 9.0"),
                Arguments.of("capacity", ">", "9.0", Set.of("DRONE-001", "DRONE-003"),
                        "Capacity greater than 9.0"),
                Arguments.of("maxMoves", ">=", "2000", Set.of("DRONE-001", "DRONE-003"),
                        "MaxMoves >= 2000"),
                Arguments.of("maxMoves", "<=", "1500", Set.of("DRONE-002", "DRONE-004"),
                        "MaxMoves <= 1500"),
                Arguments.of("costPerMove", "<", "0.015", Set.of("DRONE-001", "DRONE-003"),
                        "CostPerMove < 0.015")
        );
    }

    @ParameterizedTest
    @MethodSource("provideBooleanQueryTests")
    void testQuery_BooleanAttributes(String attribute, String value,
                                     Set<String> expectedIds, String description) {
        QueryAttribute qa = new QueryAttribute();
        qa.setAttribute(attribute);
        qa.setOperator("=");
        qa.setValue(value);

        when(ilpClientService.getDrones()).thenReturn(testDrones);

        List<String> result = queryService.query(Collections.singletonList(qa));

        // Verifies expected String IDs
        assertEquals(expectedIds.size(), result.size(), description);
        assertTrue(result.containsAll(expectedIds), description);
    }

    private static Stream<Arguments> provideBooleanQueryTests() {
        return Stream.of(
                Arguments.of("cooling", "true", Set.of("DRONE-001", "DRONE-002"),
                        "Drones with cooling"),
                Arguments.of("cooling", "false", Set.of("DRONE-003", "DRONE-004"),
                        "Drones without cooling"),
                Arguments.of("heating", "true", Set.of("DRONE-001", "DRONE-003"),
                        "Drones with heating"),
                Arguments.of("heating", "false", Set.of("DRONE-002", "DRONE-004"),
                        "Drones without heating")
        );
    }

    @Test
    void testQuery_InvalidOperator_ReturnsEmpty() {
        QueryAttribute qa = new QueryAttribute();
        qa.setAttribute("capacity");
        qa.setOperator("~~");
        qa.setValue("10.0");

        when(ilpClientService.getDrones()).thenReturn(testDrones);
        List<String> result = queryService.query(Collections.singletonList(qa));

        // Should return empty
        assertTrue(result.isEmpty(), "Invalid operator should return no matches");
    }

    @Test
    void testQuery_EmptyQueryList_ReturnsAllDrones() {
        when(ilpClientService.getDrones()).thenReturn(testDrones);

        List<String> result = queryService.query(Collections.emptyList());

        // Empty query should return all drones
        assertEquals(4, result.size());
        assertTrue(result.contains("DRONE-001"));
        assertTrue(result.contains("DRONE-002"));
        assertTrue(result.contains("DRONE-003"));
        assertTrue(result.contains("DRONE-004"));
    }
}
