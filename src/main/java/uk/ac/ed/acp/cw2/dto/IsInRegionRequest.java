package uk.ac.ed.acp.cw2.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import uk.ac.ed.acp.cw2.model.LngLat;
import uk.ac.ed.acp.cw2.model.Region;

public class IsInRegionRequest {

    @NotNull @Valid
    @JsonProperty("position")
    private LngLat position;

    @NotNull @Valid
    @JsonProperty("region")
    private Region region;

    public IsInRegionRequest() {}

    public LngLat getPosition() { return position; }
    public void setPosition(LngLat position) { this.position = position; }
    public Region getRegion() { return region; }
    public void setRegion(Region region) { this.region = region; }

    public boolean isValid() {
        return position != null && position.isValid() &&
                region != null && region.isValid();
    }
}
