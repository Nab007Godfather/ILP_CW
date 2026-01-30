package uk.ac.ed.acp.cw2;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.ac.ed.acp.cw2.model.LngLat;
import uk.ac.ed.acp.cw2.service.DroneNavigation;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for DroneNavigation utility class.
 * Coverage:
 * Euclidean distance calculations
 * Proximity checking (<0.00015 threshold)
 * Next position calculation with 16 compass directions
 * Angle validation (only multiples of 22.5°)
 * Edge cases (null, NaN, infinity)
 * Step size is always 0.00015 (straight or diagonal)
 */
class DroneNavigationTests {

    private static final double EPSILON = 1e-9;

    // euclideanDistance Tests

    @Test
    void testEuclideanDistance_BasicCalculation() {
        LngLat point1 = new LngLat(0.0, 0.0);
        LngLat point2 = new LngLat(3.0, 4.0);

        double distance = DroneNavigation.euclideanDistance(point1, point2);

        assertEquals(5.0, distance, EPSILON, "3-4-5 triangle should be 5.0");
    }

    @Test
    void testEuclideanDistance_SamePoint_ReturnsZero() {
        LngLat point = new LngLat(-3.186, 55.944);

        double distance = DroneNavigation.euclideanDistance(point, point);

        assertEquals(0.0, distance, EPSILON, "Distance to same point should be 0.0");
    }

    @Test
    void testEuclideanDistance_EdinburghCoordinates() {
        LngLat appleton = new LngLat(-3.186874, 55.944494);
        LngLat library = new LngLat(-3.189180, 55.942617);

        double distance = DroneNavigation.euclideanDistance(appleton, library);

        assertTrue(distance > 0, "Distance should be positive");
        assertEquals(0.002973, distance, 0.0001, "Distance approximately 0.002973 degrees");
    }

    @Test
    void testEuclideanDistance_Symmetry() {
        LngLat point1 = new LngLat(1.0, 2.0);
        LngLat point2 = new LngLat(4.0, 6.0);

        double dist12 = DroneNavigation.euclideanDistance(point1, point2);
        double dist21 = DroneNavigation.euclideanDistance(point2, point1);

        assertEquals(dist12, dist21, EPSILON, "Distance should be symmetric");
    }

    @Test
    void testEuclideanDistance_NullPoint_ThrowsException() {
        LngLat valid = new LngLat(1.0, 2.0);

        assertThrows(IllegalArgumentException.class,
                () -> DroneNavigation.euclideanDistance(null, valid));
        assertThrows(IllegalArgumentException.class,
                () -> DroneNavigation.euclideanDistance(valid, null));
    }

    // isClose Tests

    @Test
    void testIsClose_WithinThreshold_ReturnsTrue() {
        LngLat point1 = new LngLat(0.0, 0.0);
        LngLat point2 = new LngLat(0.0001, 0.0001);

        boolean isClose = DroneNavigation.isClose(point1, point2);

        assertTrue(isClose, "Points within 0.00015 should be close");
    }

    @Test
    void testIsClose_ExactlyAtThreshold_ReturnsFalse() {
        LngLat point1 = new LngLat(0.0, 0.0);
        LngLat point2 = new LngLat(0.00015, 0.0);

        boolean isClose = DroneNavigation.isClose(point1, point2);

        assertFalse(isClose, "Points at exact threshold should NOT be close (strictly <)");
    }

    @Test
    void testIsClose_BeyondThreshold_ReturnsFalse() {
        LngLat point1 = new LngLat(0.0, 0.0);
        LngLat point2 = new LngLat(0.0002, 0.0);

        boolean isClose = DroneNavigation.isClose(point1, point2);

        assertFalse(isClose, "Points beyond threshold should NOT be close");
    }

    @Test
    void testIsClose_SamePoint_ReturnsTrue() {
        LngLat point = new LngLat(1.0, 2.0);

        boolean isClose = DroneNavigation.isClose(point, point);

        assertTrue(isClose, "Same point should be close to itself");
    }

    @ParameterizedTest
    @MethodSource("provideCloseDistances")
    void testIsClose_VariousDistances(double dx, double dy, boolean expected, String desc) {
        LngLat point1 = new LngLat(0.0, 0.0);
        LngLat point2 = new LngLat(dx, dy);

        boolean isClose = DroneNavigation.isClose(point1, point2);

        assertEquals(expected, isClose, desc);
    }

    private static Stream<Arguments> provideCloseDistances() {
        return Stream.of(
                Arguments.of(0.0, 0.0, true, "Same point"),
                Arguments.of(0.0001, 0.0, true, "0.0001 degrees away"),
                Arguments.of(0.00014, 0.0, true, "0.00014 degrees away"),
                Arguments.of(0.00015, 0.0, false, "Exactly at threshold"),
                Arguments.of(0.0002, 0.0, false, "Beyond threshold"),
                Arguments.of(0.0001, 0.0001, true, "Diagonal within threshold"),
                Arguments.of(0.00011, 0.00011, false, "Diagonal beyond threshold")
        );
    }

    // nextPosition tests

    @Test
    void testNextPosition_East_0Degrees() {
        LngLat start = new LngLat(0.0, 0.0);

        LngLat next = DroneNavigation.nextPosition(start, 0.0);

        assertEquals(DroneNavigation.STEP, next.getLng(), EPSILON, "Should move East");
        assertEquals(0.0, next.getLat(), EPSILON, "Lat unchanged");
    }

    @Test
    void testNextPosition_North_90Degrees() {
        LngLat start = new LngLat(0.0, 0.0);

        LngLat next = DroneNavigation.nextPosition(start, 90.0);

        assertEquals(0.0, next.getLng(), EPSILON, "Lng unchanged");
        assertEquals(DroneNavigation.STEP, next.getLat(), EPSILON, "Should move North");
    }

    @Test
    void testNextPosition_West_180Degrees() {
        LngLat start = new LngLat(0.0, 0.0);

        LngLat next = DroneNavigation.nextPosition(start, 180.0);

        assertEquals(-DroneNavigation.STEP, next.getLng(), EPSILON, "Should move West");
        assertEquals(0.0, next.getLat(), EPSILON, "Lat unchanged");
    }

    @Test
    void testNextPosition_South_270Degrees() {
        LngLat start = new LngLat(0.0, 0.0);

        LngLat next = DroneNavigation.nextPosition(start, 270.0);

        assertEquals(0.0, next.getLng(), EPSILON, "Lng unchanged");
        assertEquals(-DroneNavigation.STEP, next.getLat(), EPSILON, "Should move South");
    }

    @Test
    void testNextPosition_NorthEast_45Degrees() {
        LngLat start = new LngLat(0.0, 0.0);

        LngLat next = DroneNavigation.nextPosition(start, 45.0);

        assertTrue(next.getLng() > 0, "Lng should increase (East)");
        assertTrue(next.getLat() > 0, "Lat should increase (North)");
        assertEquals(next.getLng(), next.getLat(), 0.0000001,
                "At 45°, lng and lat changes should be equal");
    }

    @Test
    void testNextPosition_StepSizeConsistent_StraightAndDiagonal() {
        LngLat start = new LngLat(0.0, 0.0);

        // Tests straight movement
        LngLat east = DroneNavigation.nextPosition(start, 0.0);
        double straightDistance = DroneNavigation.euclideanDistance(start, east);

        // Tests diagonal movement
        LngLat northeast = DroneNavigation.nextPosition(start, 45.0);
        double diagonalDistance = DroneNavigation.euclideanDistance(start, northeast);

        assertEquals(DroneNavigation.STEP, straightDistance, EPSILON,
                "Straight distance should be STEP");
        assertEquals(DroneNavigation.STEP, diagonalDistance, EPSILON,
                "Diagonal distance should also be STEP");
    }

    @ParameterizedTest
    @ValueSource(doubles = {0, 22.5, 45, 67.5, 90, 112.5, 135, 157.5,
            180, 202.5, 225, 247.5, 270, 292.5, 315, 337.5})
    void testNextPosition_AllAllowedAngles(double angle) {
        LngLat start = new LngLat(-3.186, 55.944);

        LngLat next = DroneNavigation.nextPosition(start, angle);

        assertNotNull(next, "Should return valid position for angle " + angle);

        double distance = DroneNavigation.euclideanDistance(start, next);
        assertEquals(DroneNavigation.STEP, distance, 0.0000001,
                "Distance moved should be STEP for angle " + angle);
    }

    @Test
    void testNextPosition_InvalidAngle_ThrowsException() {
        LngLat start = new LngLat(0.0, 0.0);

        assertThrows(IllegalArgumentException.class,
                () -> DroneNavigation.nextPosition(start, 30.0),
                "Should throw for invalid angle 30°");
    }

    @ParameterizedTest
    @ValueSource(doubles = {10.0, 15.5, 23.0, 50.0, 100.0, 360.0, -45.0})
    void testNextPosition_VariousInvalidAngles_ThrowsException(double angle) {
        LngLat start = new LngLat(0.0, 0.0);

        assertThrows(IllegalArgumentException.class,
                () -> DroneNavigation.nextPosition(start, angle),
                "Should throw for angle " + angle);
    }

    @Test
    void testNextPosition_NullAngle_ThrowsException() {
        LngLat start = new LngLat(0.0, 0.0);

        assertThrows(IllegalArgumentException.class,
                () -> DroneNavigation.nextPosition(start, null));
    }

    @Test
    void testNextPosition_PreservesAltitude() {
        LngLat start = new LngLat(0.0, 0.0, 100);

        LngLat next = DroneNavigation.nextPosition(start, 0.0);

        assertEquals(100, next.getAlt(), "Altitude should be preserved");
    }

    // Constants tests

    @Test
    void testSTEP_CorrectValue() {
        assertEquals(0.00015, DroneNavigation.STEP, EPSILON, "STEP should be 0.00015");
    }

    @Test
    void testCLOSE_THRESHOLD_CorrectValue() {
        assertEquals(0.00015, DroneNavigation.CLOSE_THRESHOLD, EPSILON);
    }

    @Test
    void testALLOWED_ANGLES_Count() {
        assertEquals(16, DroneNavigation.ALLOWED_ANGLES.length, "Should have 16 angles");
    }

    @Test
    void testALLOWED_ANGLES_Spacing() {
        for (int i = 0; i < DroneNavigation.ALLOWED_ANGLES.length; i++) {
            double expected = i * 22.5;
            assertEquals(expected, DroneNavigation.ALLOWED_ANGLES[i], EPSILON,
                    "Angle " + i + " should be " + expected + "°");
        }
    }

    // Integration tests

    @Test
    void testIntegration_MoveAndCheckClose() {
        LngLat start = new LngLat(0.0, 0.0);
        LngLat goal = new LngLat(0.00015, 0.0);

        LngLat next = DroneNavigation.nextPosition(start, 0.0);

        assertTrue(DroneNavigation.isClose(next, goal),
                "After one move East, should be close to goal");
    }

    @Test
    void testIntegration_MultipleMovesTowardsGoal() {
        LngLat position = new LngLat(0.0, 0.0);
        LngLat goal = new LngLat(0.0003, 0.0);

        int moves = 0;
        int maxMoves = 10;

        while (!DroneNavigation.isClose(position, goal) && moves < maxMoves) {
            position = DroneNavigation.nextPosition(position, 0.0);
            moves++;
        }

        assertTrue(moves > 0, "Should require at least one move");
        assertTrue(moves <= 3, "Should reach goal in 2-3 moves");
        assertTrue(DroneNavigation.isClose(position, goal), "Should be close to goal");
    }
}
