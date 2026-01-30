package uk.ac.ed.acp.cw2.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.ac.ed.acp.cw2.model.LngLat;
import java.util.List;

public class Delivery {

    @JsonProperty("deliveryId")
    private Integer deliveryId;

    @JsonProperty("flightPath")
    private List<LngLat> flightPath;

    public Delivery() {}

    public Delivery(Integer deliveryId, List<LngLat> flightPath) {
        this.deliveryId = deliveryId;
        this.flightPath = flightPath;
    }

    public Integer getDeliveryId() { return deliveryId; }
    public void setDeliveryId(Integer deliveryId) { this.deliveryId = deliveryId; }
    public List<LngLat> getFlightPath() { return flightPath; }
    public void setFlightPath(List<LngLat> flightPath) { this.flightPath = flightPath; }
}
