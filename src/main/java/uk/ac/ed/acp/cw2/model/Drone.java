package uk.ac.ed.acp.cw2.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

// Represents a drone with its capabilities and specifications

@JsonIgnoreProperties(ignoreUnknown = true)
public class Drone {

    @JsonProperty("id")
    private String id;  // CRITICAL: Drone ID is String, not Integer

    @JsonProperty("name")
    private String name;

    @JsonProperty("capability")
    private DroneCapability capability;

    public Drone() {}

    public Drone(String id, String name, DroneCapability capability) {
        this.id = id;
        this.name = name;
        this.capability = capability;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public DroneCapability getCapability() { return capability; }
    public void setCapability(DroneCapability capability) { this.capability = capability; }

    @Override
    public String toString() {
        return String.format("Drone{id='%s', name='%s', capability=%s}", id, name, capability);
    }

}
