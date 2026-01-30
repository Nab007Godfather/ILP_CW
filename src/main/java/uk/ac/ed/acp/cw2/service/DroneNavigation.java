package uk.ac.ed.acp.cw2.service;

import uk.ac.ed.acp.cw2.model.LngLat;

/**
 * Provides static utility methods for:
 * calculating Euclidean distance between positions
 * checking if two positions are "close" (< 0.00015 degrees)
 * calculating next position based on angle and move distance
 */
public class DroneNavigation {

    public static final double STEP = 0.00015;

    public static final double CLOSE_THRESHOLD = 0.00015;

    // Allowed compass directions (16 directions and 22.5° apart)
    public static final double[] ALLOWED_ANGLES = {
            0, 22.5, 45, 67.5, 90, 112.5, 135, 157.5,
            180, 202.5, 225, 247.5, 270, 292.5, 315, 337.5
    };

    private DroneNavigation() {
    }

    public static double euclideanDistance(LngLat a, LngLat b) {
        validatePoint(a);
        validatePoint(b);

        double dx = a.getLng() - b.getLng();
        double dy = a.getLat() - b.getLat();

        return Math.sqrt(dx * dx + dy * dy);
    }

    public static boolean isClose(LngLat a, LngLat b) {
        double distance = euclideanDistance(a, b);
        return distance < CLOSE_THRESHOLD;
    }

    // Calculating the next position after moving in a given direction.

    public static LngLat nextPosition(LngLat start, Double angle) {
        validatePoint(start);

        if (angle == null) {
            throw new IllegalArgumentException("Angle is required");
        }

        if (!isAllowedAngle(angle)) {
            throw new IllegalArgumentException(
                    "Angle must be one of the 16 compass directions (multiples of 22.5°)"
            );
        }

        // Converting to radians
        double radians = Math.toRadians(angle);

        // Calculating displacement
        double dx = STEP * Math.cos(radians);
        double dy = STEP * Math.sin(radians);

        // Creating new position
        Double newLng = start.getLng() + dx;
        Double newLat = start.getLat() + dy;

        return new LngLat(newLng, newLat, start.getAlt());
    }

    // Checking if an angle is one of the allowed compass directions
    private static boolean isAllowedAngle(double angle) {
        for (double allowed : ALLOWED_ANGLES) {
            if (Math.abs(allowed - angle) < 1e-9) {
                return true;
            }
        }
        return false;
    }

    // Validating that a point has valid coordinates
    private static void validatePoint(LngLat point) {
        if (point == null) {
            throw new IllegalArgumentException("Point cannot be null");
        }
        if (point.getLat() == null || point.getLng() == null) {
            throw new IllegalArgumentException("Latitude and longitude must be provided");
        }
        if (Double.isNaN(point.getLat()) || Double.isNaN(point.getLng())) {
            throw new IllegalArgumentException("Latitude and longitude must be valid numbers");
        }
        if (Double.isInfinite(point.getLat()) || Double.isInfinite(point.getLng())) {
            throw new IllegalArgumentException("Latitude and longitude cannot be infinite");
        }
    }
}
