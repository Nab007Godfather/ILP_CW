package uk.ac.ed.acp.cw2.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import uk.ac.ed.acp.cw2.model.LngLat;

public class NextPositionRequest {

    @NotNull @Valid
    @JsonProperty("start")
    private LngLat start;

    @NotNull
    @JsonProperty("angle")
    private Double angle;

    public NextPositionRequest() {}

    public LngLat getStart() { return start; }

    public void setStart(LngLat start) { this.start = start; }
    public Double getAngle() { return angle; }
    public void setAngle(Double angle) { this.angle = angle; }

    public boolean isValid() {
        return start != null && start.isValid() &&
                angle != null && !angle.isNaN() && !angle.isInfinite();
    }
}
