package uk.ac.ed.acp.cw2.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

// Evaluates which drones are available at which service points

public class DroneForServicePoint {

    @JsonProperty("servicePointId")
    private Integer servicePointId;

    @JsonProperty("drones")
    private List<DroneAvailability> drones;

    public DroneForServicePoint() {
    }

    public Integer getServicePointId() {
        return servicePointId;
    }

    public void setServicePointId(Integer servicePointId) {
        this.servicePointId = servicePointId;
    }

    public List<DroneAvailability> getDrones() {
        return drones;
    }

    public void setDrones(List<DroneAvailability> drones) {
        this.drones = drones;
    }

    // Nested class representing a drone's availability at a certain service point
    public static class DroneAvailability {

        @JsonProperty("id")
        private String id;

        @JsonProperty("availability")
        private List<DayAvailability> availability;

        public DroneAvailability() {
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public List<DayAvailability> getAvailability() {
            return availability;
        }

        public void setAvailability(List<DayAvailability> availability) {
            this.availability = availability;
        }
    }

    @Override
    public String toString() {
        return String.format("DroneForServicePoint{servicePointId=%d, drones=%d}",
                servicePointId, drones != null ? drones.size() : 0);
    }
}
