package uk.ac.ed.acp.cw2.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

// Represents a restricted area or no-fly zone
// A no-fly zone has limits.upper = -1 (cannot fly at any altitude)
// A restricted zone has specific altitude limits

public class RestrictedArea {

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("limits")
    private AltitudeLimits limits;

    @JsonProperty("vertices")
    private List<LngLat> vertices;

    public RestrictedArea() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AltitudeLimits getLimits() {
        return limits;
    }

    public void setLimits(AltitudeLimits limits) {
        this.limits = limits;
    }

    public List<LngLat> getVertices() {
        return vertices;
    }

    public void setVertices(List<LngLat> vertices) {
        this.vertices = vertices;
    }

    // Checking if this is a complete no-fly zone
    public boolean isNoFlyZone() {
        return limits != null && limits.getUpper() == -1;
    }

    // Nested class representing altitude limits
    public static class AltitudeLimits {

        @JsonProperty("lower")
        private Integer lower;

        @JsonProperty("upper")
        private Integer upper;

        public AltitudeLimits() {
        }

        public Integer getLower() {
            return lower;
        }

        public void setLower(Integer lower) {
            this.lower = lower;
        }

        public Integer getUpper() {
            return upper;
        }

        public void setUpper(Integer upper) {
            this.upper = upper;
        }
    }

    @Override
    public String toString() {
        return String.format("RestrictedArea{id=%d, name='%s', noFly=%b}",
                id, name, isNoFlyZone());
    }
}
