package uk.ac.ed.acp.cw2.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class DronePath {

    @JsonProperty("droneId")
    private String droneId;

    @JsonProperty("deliveries")
    private List<Delivery> deliveries;

    public DronePath() {}

    public DronePath(String droneId, List<Delivery> deliveries) {
        this.droneId = droneId;
        this.deliveries = deliveries;
    }

    public String getDroneId() { return droneId; }
    public void setDroneId(String droneId) { this.droneId = droneId; }
    public List<Delivery> getDeliveries() { return deliveries; }
    public void setDeliveries(List<Delivery> deliveries) { this.deliveries = deliveries; }
}
