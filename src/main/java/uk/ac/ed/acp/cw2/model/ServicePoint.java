package uk.ac.ed.acp.cw2.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a drone service point (base station).
 * Service points are where drones:
 * start their delivery runs
 * return after completing deliveries
 * get recharged/serviced
 */

public class ServicePoint {

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("location")
    private LngLat location;

    public ServicePoint() {
    }

    public ServicePoint(Integer id, String name, LngLat location) {
        this.id = id;
        this.name = name;
        this.location = location;
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

    public LngLat getLocation() {
        return location;
    }

    public void setLocation(LngLat location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return String.format("ServicePoint{id=%d, name='%s', location=%s}",
                id, name, location);
    }
}
