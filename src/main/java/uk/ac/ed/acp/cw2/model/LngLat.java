package uk.ac.ed.acp.cw2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

// Represents a geographic position with longitude, latitude and altitude (optional)
public class LngLat {

    @NotNull(message = "Longitude cannot be null")
    @JsonProperty("lng")
    private Double lng;

    @NotNull(message = "Latitude cannot be null")
    @JsonProperty("lat")
    private Double lat;

    @JsonProperty("alt")
    private Integer alt;

    public LngLat() {
    }

    public LngLat(Double lng, Double lat) {
        this.lng = lng;
        this.lat = lat;
        this.alt = null;
    }

    public LngLat(Double lng, Double lat, Integer alt) {
        this.lng = lng;
        this.lat = lat;
        this.alt = alt;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Integer getAlt() {
        return alt;
    }

    public void setAlt(Integer alt) {
        this.alt = alt;
    }

    // Checking if a position has valid coordinates
    public boolean isValid() {
        return lng != null && lat != null &&
                !lng.isNaN() && !lng.isInfinite() &&
                !lat.isNaN() && !lat.isInfinite();
    }

    @Override
    public String toString() {
        return String.format("LngLat(%.6f, %.6f)", lng, lat);
    }

}
