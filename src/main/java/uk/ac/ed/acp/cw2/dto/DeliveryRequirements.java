package uk.ac.ed.acp.cw2.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Delivery requirements nested within MedDispatchRec.
 * Only capacity is required while all other fields are optional.
 * Cooling and heating are mutually exclusive.
 */
public class DeliveryRequirements {

    @JsonProperty("capacity")
    private Double capacity;

    @JsonProperty("cooling")
    private Boolean cooling;

    @JsonProperty("heating")
    private Boolean heating;

    @JsonProperty("maxCost")
    private Double maxCost;

    public DeliveryRequirements() {
    }

    public Double getCapacity() {
        return capacity;
    }

    public void setCapacity(Double capacity) {
        this.capacity = capacity;
    }

    // Get cooling requirement (defaults to false if not specified)
    public Boolean getCooling() {
        return cooling != null ? cooling : false;
    }

    public void setCooling(Boolean cooling) {
        this.cooling = cooling;
    }

    // Get heating requirement (defaults to false if not specified)
    public Boolean getHeating() {
        return heating != null ? heating : false;
    }

    public void setHeating(Boolean heating) {
        this.heating = heating;
    }

    // Get maxCost

    public Double getMaxCost() {
        return maxCost;
    }

    public void setMaxCost(Double maxCost) {
        this.maxCost = maxCost;
    }
}
