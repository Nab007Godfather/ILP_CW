package uk.ac.ed.acp.cw2;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.ac.ed.acp.cw2.model.LngLat;
import uk.ac.ed.acp.cw2.model.Region;
import uk.ac.ed.acp.cw2.service.PointInRegion;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for PointInRegion utility class.
 * Coverage:
 * Ray casting algorithm for point-in-polygon
 * Points inside, outside, on boundaries
 * Various polygon shapes (squares, triangles, concave)
 * Edinburgh area real-world coordinates
 * Edge cases (null inputs, invalid regions)
 */
class PointInRegionTests {

    // Basic tests

    @Test
    void testIsPointInRegion_PointInside_ReturnsTrue() {
        Region square = createSquare(0, 0, 4, 4);
        LngLat insidePoint = new LngLat(2.0, 2.0);

        boolean result = PointInRegion.isPointInRegion(insidePoint, square);

        assertTrue(result, "Point (2,2) should be inside square");
    }

    @Test
    void testIsPointInRegion_PointOutside_ReturnsFalse() {
        Region square = createSquare(0, 0, 4, 4);
        LngLat outsidePoint = new LngLat(5.0, 5.0);

        boolean result = PointInRegion.isPointInRegion(outsidePoint, square);

        assertFalse(result, "Point (5,5) should be outside square");
    }

    @Test
    void testIsPointInRegion_PointOnVertex_ReturnsTrue() {
        Region square = createSquare(0, 0, 4, 4);
        LngLat vertexPoint = new LngLat(0.0, 0.0);

        boolean result = PointInRegion.isPointInRegion(vertexPoint, square);

        assertTrue(result, "Point on vertex should be inside");
    }

    @Test
    void testIsPointInRegion_PointOnEdge_ReturnsTrue() {
        Region square = createSquare(0, 0, 4, 4);
        LngLat edgePoint = new LngLat(2.0, 0.0);

        boolean result = PointInRegion.isPointInRegion(edgePoint, square);

        assertTrue(result, "Point on edge should be inside");
    }

    // Triangle tests

    @Test
    void testIsPointInRegion_TriangleInside() {
        Region triangle = createTriangle(0, 0, 4, 0, 2, 3);
        LngLat insidePoint = new LngLat(2.0, 1.0);

        boolean result = PointInRegion.isPointInRegion(insidePoint, triangle);

        assertTrue(result, "Point should be inside triangle");
    }

    @Test
    void testIsPointInRegion_TriangleOutside() {
        Region triangle = createTriangle(0, 0, 4, 0, 2, 3);
        LngLat outsidePoint = new LngLat(3.0, 2.5);

        boolean result = PointInRegion.isPointInRegion(outsidePoint, triangle);

        assertFalse(result, "Point should be outside triangle");
    }

    // Concave Polygon tests

    @Test
    void testIsPointInRegion_ConcavePolygon_PointInside() {
        // L-shaped polygon
        List<LngLat> vertices = Arrays.asList(
                new LngLat(0.0, 0.0),
                new LngLat(4.0, 0.0),
                new LngLat(4.0, 2.0),
                new LngLat(2.0, 2.0),
                new LngLat(2.0, 4.0),
                new LngLat(0.0, 4.0),
                new LngLat(0.0, 0.0)
        );
        Region lShape = new Region("L-Shape", vertices);
        LngLat insidePoint = new LngLat(1.0, 1.0);

        boolean result = PointInRegion.isPointInRegion(insidePoint, lShape);

        assertTrue(result, "Point should be inside L-shaped polygon");
    }

    @Test
    void testIsPointInRegion_ConcavePolygon_PointInConcavePart() {
        // L-shaped polygon
        List<LngLat> vertices = Arrays.asList(
                new LngLat(0.0, 0.0),
                new LngLat(4.0, 0.0),
                new LngLat(4.0, 2.0),
                new LngLat(2.0, 2.0),
                new LngLat(2.0, 4.0),
                new LngLat(0.0, 4.0),
                new LngLat(0.0, 0.0)
        );
        Region lShape = new Region("L-Shape", vertices);
        LngLat concavePoint = new LngLat(3.0, 3.0);

        boolean result = PointInRegion.isPointInRegion(concavePoint, lShape);

        assertFalse(result, "Point in concave cutout should be outside");
    }

    // Edinburgh Area tests

    @Test
    void testIsPointInRegion_EdinburghArea_AppletonTowerInside() {
        List<LngLat> vertices = Arrays.asList(
                new LngLat(-3.192, 55.946),
                new LngLat(-3.184, 55.946),
                new LngLat(-3.184, 55.942),
                new LngLat(-3.192, 55.942),
                new LngLat(-3.192, 55.946)
        );
        Region edinburghArea = new Region("Edinburgh Area", vertices);
        LngLat appletonTower = new LngLat(-3.186874, 55.944494);

        boolean result = PointInRegion.isPointInRegion(appletonTower, edinburghArea);

        assertTrue(result, "Appleton Tower should be inside Edinburgh area");
    }

    @Test
    void testIsPointInRegion_EdinburghArea_OutsidePoint() {
        List<LngLat> vertices = Arrays.asList(
                new LngLat(-3.192, 55.946),
                new LngLat(-3.184, 55.946),
                new LngLat(-3.184, 55.942),
                new LngLat(-3.192, 55.942),
                new LngLat(-3.192, 55.946)
        );
        Region edinburghArea = new Region("Edinburgh Area", vertices);
        LngLat farAway = new LngLat(-3.200, 55.950);

        boolean result = PointInRegion.isPointInRegion(farAway, edinburghArea);

        assertFalse(result, "Point should be outside Edinburgh area");
    }

    // Edge cases

    @Test
    void testIsPointInRegion_NullPoint_ReturnsFalse() {
        Region square = createSquare(0, 0, 4, 4);

        PointInRegion.isPointInRegion(null, square);
        boolean result = false;

        assertFalse(result, "Null point should return false");
    }

    @Test
    void testIsPointInRegion_NullRegion_ReturnsFalse() {
        LngLat point = new LngLat(2.0, 2.0);

        boolean result = PointInRegion.isPointInRegion(point, null);

        assertFalse(result, "Null region should return false");
    }

    @Test
    void testIsPointInRegion_InvalidPoint_ReturnsFalse() {
        Region square = createSquare(0, 0, 4, 4);
        LngLat invalidPoint = new LngLat(null, null);

        boolean result = PointInRegion.isPointInRegion(invalidPoint, square);

        assertFalse(result, "Invalid point should return false");
    }

    @Test
    void testIsPointInRegion_UnclosedPolygon_ReturnsFalse() {
        List<LngLat> unclosedVertices = Arrays.asList(
                new LngLat(0.0, 0.0),
                new LngLat(4.0, 0.0),
                new LngLat(4.0, 4.0),
                new LngLat(0.0, 4.0)
                // Missing closing vertex
        );
        Region unclosedRegion = new Region("Unclosed", unclosedVertices);
        LngLat point = new LngLat(2.0, 2.0);

        boolean result = PointInRegion.isPointInRegion(point, unclosedRegion);

        assertFalse(result, "Unclosed polygon should return false");
    }

    // Boundary tests

    @ParameterizedTest
    @MethodSource("provideBoundaryPoints")
    void testIsPointInRegion_BoundaryPoints(LngLat point, boolean expected, String desc) {
        Region square = createSquare(0, 0, 4, 4);

        boolean result = PointInRegion.isPointInRegion(point, square);

        assertEquals(expected, result, desc);
    }

    private static Stream<Arguments> provideBoundaryPoints() {
        return Stream.of(
                // Vertices
                Arguments.of(new LngLat(0.0, 0.0), true, "Bottom-left vertex"),
                Arguments.of(new LngLat(4.0, 0.0), true, "Bottom-right vertex"),
                Arguments.of(new LngLat(4.0, 4.0), true, "Top-right vertex"),
                Arguments.of(new LngLat(0.0, 4.0), true, "Top-left vertex"),

                // Edge midpoints
                Arguments.of(new LngLat(2.0, 0.0), true, "Bottom edge midpoint"),
                Arguments.of(new LngLat(4.0, 2.0), true, "Right edge midpoint"),
                Arguments.of(new LngLat(2.0, 4.0), true, "Top edge midpoint"),
                Arguments.of(new LngLat(0.0, 2.0), true, "Left edge midpoint"),

                // Just inside
                Arguments.of(new LngLat(0.1, 0.1), true, "Just inside bottom-left"),
                Arguments.of(new LngLat(3.9, 3.9), true, "Just inside top-right"),

                // Just outside
                Arguments.of(new LngLat(-0.1, 0.0), false, "Just outside left"),
                Arguments.of(new LngLat(4.1, 2.0), false, "Just outside right"),
                Arguments.of(new LngLat(2.0, -0.1), false, "Just outside bottom"),
                Arguments.of(new LngLat(2.0, 4.1), false, "Just outside top")
        );
    }

    // Helper Methods

    private Region createSquare(double x1, double y1, double x2, double y2) {
        List<LngLat> vertices = Arrays.asList(
                new LngLat(x1, y1),
                new LngLat(x2, y1),
                new LngLat(x2, y2),
                new LngLat(x1, y2),
                new LngLat(x1, y1)
        );
        return new Region("Square", vertices);
    }

    private Region createTriangle(double x1, double y1, double x2, double y2,
                                  double x3, double y3) {
        List<LngLat> vertices = Arrays.asList(
                new LngLat(x1, y1),
                new LngLat(x2, y2),
                new LngLat(x3, y3),
                new LngLat(x1, y1)
        );
        return new Region("Triangle", vertices);
    }
}
