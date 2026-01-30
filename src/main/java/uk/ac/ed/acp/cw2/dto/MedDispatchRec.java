package uk.ac.ed.acp.cw2.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.ac.ed.acp.cw2.model.LngLat;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Medicine dispatch record representing a delivery order
 * Required fields: id, requirements.capacity
 * Optional fields: date, time, cooling, heating, maxCost
 */
public class MedDispatchRec {

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("date")
    private String date;

    @JsonProperty("time")
    private String time;

    @JsonProperty("requirements")
    private DeliveryRequirements requirements;

    @JsonProperty("delivery")
    private LngLat delivery;

    public MedDispatchRec() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public DeliveryRequirements getRequirements() {
        return requirements;
    }

    public void setRequirements(DeliveryRequirements requirements) {
        this.requirements = requirements;
    }

    public LngLat getDelivery() {
        return delivery;
    }

    public void setDelivery(LngLat delivery) {
        this.delivery = delivery;
    }

    // Parsing date string to LocalDate
    public LocalDate getLocalDate() {
        return date != null ? LocalDate.parse(date) : null;
    }

    // Parsing time string to LocalTime
    public LocalTime getLocalTime() {
        return time != null ? LocalTime.parse(time) : null;
    }

    @Override
    public String toString() {
        return String.format("MedDispatchRec{id=%d, date=%s, time=%s}", id, date, time);
    }
}
