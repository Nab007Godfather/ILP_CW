package uk.ac.ed.acp.cw2.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import uk.ac.ed.acp.cw2.model.LngLat;

public class DistanceRequest {

    @NotNull @Valid
    @JsonProperty("position1")
    private LngLat position1;

    @NotNull @Valid
    @JsonProperty("position2")
    private LngLat position2;

    public DistanceRequest() {}

    public LngLat getPosition1() { return position1; }
    public void setPosition1(LngLat position1) { this.position1 = position1; }
    public LngLat getPosition2() { return position2; }
    public void setPosition2(LngLat position2) { this.position2 = position2; }

    public boolean isValid() {
        return position1 != null && position2 != null &&
                position1.isValid() && position2.isValid();
    }
}
