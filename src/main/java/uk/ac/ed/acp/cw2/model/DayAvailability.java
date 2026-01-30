package uk.ac.ed.acp.cw2.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * Represents a drone's availability window for a specific day of the week
 * Used to check if a drone can handle a delivery at a specific date/time
 */
public class DayAvailability {

    @JsonProperty("dayOfWeek")
    private String dayOfWeek;

    @JsonProperty("from")
    private String from;

    @JsonProperty("until")
    private String until;

    public DayAvailability() {
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getUntil() {
        return until;
    }

    public void setUntil(String until) {
        this.until = until;
    }

    public LocalTime getFromTime() {
        return LocalTime.parse(from);
    }

    public LocalTime getUntilTime() {
        return LocalTime.parse(until);
    }

    public DayOfWeek getDayOfWeekEnum() {
        return DayOfWeek.valueOf(dayOfWeek.toUpperCase());
    }

    // Checking if a given time falls within this availability window
    public boolean isAvailableAt(LocalTime time) {
        LocalTime fromTime = getFromTime();
        LocalTime untilTime = getUntilTime();

        return !time.isBefore(fromTime) && !time.isAfter(untilTime);
    }

    @Override
    public String toString() {
        return String.format("%s: %s-%s", dayOfWeek, from, until);
    }
}
