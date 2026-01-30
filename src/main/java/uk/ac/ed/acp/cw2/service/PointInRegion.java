package uk.ac.ed.acp.cw2.service;

import uk.ac.ed.acp.cw2.model.LngLat;
import uk.ac.ed.acp.cw2.model.Region;

import java.util.List;

// Point-in-polygon algorithm using ray casting

public class PointInRegion {

    private PointInRegion() {
    }

    // Checking if a point is inside a region
    public static boolean isPointInRegion(LngLat point, Region region) {
        if (point == null || !point.isValid()) {
            return false;
        }

        if (region == null || !region.isValid()) {
            return false;
        }

        List<LngLat> vertices = region.getVertices();

        // Ray casting algorithm
        int intersections = 0;
        int n = vertices.size() - 1; // Exclude duplicate closing vertex

        for (int i = 0; i < n; i++) {
            LngLat v1 = vertices.get(i);
            LngLat v2 = vertices.get(i + 1);

            // Checks if point is on the edge
            if (pointOnSegment(point, v1, v2)) {
                return true; // On boundary means inside
            }

            // Checks if ray intersects this edge
            if (rayIntersectsSegment(point, v1, v2)) {
                intersections++;
            }
        }

        // Odd number of intersections = inside
        return (intersections % 2) == 1;
    }

    // Checking if a horizontal ray from point intersects a line segment
    private static boolean rayIntersectsSegment(LngLat point, LngLat v1, LngLat v2) {
        double px = point.getLng();
        double py = point.getLat();
        double x1 = v1.getLng();
        double y1 = v1.getLat();
        double x2 = v2.getLng();
        double y2 = v2.getLat();

        // If segment is horizontal, no intersection
        if (y1 == y2) {
            return false;
        }

        // Checks if point's y is within segment's y range
        if (py < Math.min(y1, y2) || py > Math.max(y1, y2)) {
            return false;
        }

        // Calculates x-coordinate of intersection using line equation
        double xIntersection = x1 + (py - y1) * (x2 - x1) / (y2 - y1);

        return xIntersection >= px;
    }

    // Checks if a point lies exactly on a line segment.

    private static boolean pointOnSegment(LngLat p, LngLat a, LngLat b) {
        if (a == null || b == null || p == null) {
            return false;
        }

        // Calculates cross product to check if collinear
        double cross = (p.getLat() - a.getLat()) * (b.getLng() - a.getLng()) -
                (p.getLng() - a.getLng()) * (b.getLat() - a.getLat());

        // If not collinear, point is not on segment
        if (Math.abs(cross) > 1e-9) {
            return false;
        }

        // Checks if point is within segment bounds using dot product
        double dot = (p.getLng() - a.getLng()) * (b.getLng() - a.getLng()) +
                (p.getLat() - a.getLat()) * (b.getLat() - a.getLat());

        if (dot < -1e-12) {
            return false; // Point is before segment start
        }

        double lenSq = (b.getLng() - a.getLng()) * (b.getLng() - a.getLng()) +
                (b.getLat() - a.getLat()) * (b.getLat() - a.getLat());

        return dot <= lenSq + 1e-12; // Point is within or at segment end
    }
}
