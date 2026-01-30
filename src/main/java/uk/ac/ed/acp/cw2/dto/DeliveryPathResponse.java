package uk.ac.ed.acp.cw2.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class DeliveryPathResponse {

    @JsonProperty("totalCost")
    private Double totalCost;

    @JsonProperty("totalMoves")
    private Integer totalMoves;

    @JsonProperty("dronePaths")
    private List<DronePath> dronePaths;

    public DeliveryPathResponse() {}

    public DeliveryPathResponse(Double totalCost, Integer totalMoves, List<DronePath> dronePaths) {
        this.totalCost = totalCost;
        this.totalMoves = totalMoves;
        this.dronePaths = dronePaths;
    }

    public Double getTotalCost() { return totalCost; }
    public void setTotalCost(Double totalCost) { this.totalCost = totalCost; }
    public Integer getTotalMoves() { return totalMoves; }
    public void setTotalMoves(Integer totalMoves) { this.totalMoves = totalMoves; }
    public List<DronePath> getDronePaths() { return dronePaths; }
    public void setDronePaths(List<DronePath> dronePaths) { this.dronePaths = dronePaths; }
}
