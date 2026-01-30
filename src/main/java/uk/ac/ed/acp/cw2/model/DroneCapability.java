package uk.ac.ed.acp.cw2.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the operational capabilities of a drone:
 * matching drones to delivery requirements
 * calculating costs
 * validating flight feasibility
 */
public class DroneCapability {

    @JsonProperty("cooling")
    private Boolean cooling;

    @JsonProperty("heating")
    private Boolean heating;

    @JsonProperty("capacity")
    private Double capacity;

    @JsonProperty("maxMoves")
    private Integer maxMoves;

    @JsonProperty("costPerMove")
    private Double costPerMove;

    @JsonProperty("costInitial")
    private Double costInitial;

    @JsonProperty("costFinal")
    private Double costFinal;

    public DroneCapability() {
    }

    public Boolean getCooling() {
        return cooling;
    }

    public void setCooling(Boolean cooling) {
        this.cooling = cooling;
    }

    public Boolean getHeating() {
        return heating;
    }

    public void setHeating(Boolean heating) {
        this.heating = heating;
    }

    public Double getCapacity() {
        return capacity;
    }

    public void setCapacity(Double capacity) {
        this.capacity = capacity;
    }

    public Integer getMaxMoves() {
        return maxMoves;
    }

    public void setMaxMoves(Integer maxMoves) {
        this.maxMoves = maxMoves;
    }

    public Double getCostPerMove() {
        return costPerMove;
    }

    public void setCostPerMove(Double costPerMove) {
        this.costPerMove = costPerMove;
    }

    public Double getCostInitial() {
        return costInitial;
    }

    public void setCostInitial(Double costInitial) {
        this.costInitial = costInitial;
    }

    public Double getCostFinal() {
        return costFinal;
    }

    public void setCostFinal(Double costFinal) {
        this.costFinal = costFinal;
    }
}
