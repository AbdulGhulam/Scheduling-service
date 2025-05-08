package com.airflow.scheduleservice.dto;

import com.airflow.scheduleservice.model.Location;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalTime;

public class Stop {
    private final Location startLocation;
    private final Location endLocation;
    private final String description;

    @JsonFormat(pattern = "HH:mm:ss")
    private final LocalTime departureAtStartLocation;

    @JsonFormat(pattern = "HH:mm:ss")
    private final LocalTime arrivalAtEndLocation;

    @JsonFormat(pattern = "HH:mm:ss")
    private final LocalTime departureAtEndLocation;

    public Stop(
            Location startLocation,
            Location endLocation,
            String description,
            LocalTime departureAtStartLocation,
            LocalTime arrivalAtEndLocation,
            LocalTime departureAtEndLocation
    ) {
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.description = description;
        this.departureAtStartLocation = departureAtStartLocation;
        this.arrivalAtEndLocation = arrivalAtEndLocation;
        this.departureAtEndLocation = departureAtEndLocation;
    }

    public Location startLocation() {
        return startLocation;
    }

    public Location endLocation() {
        return endLocation;
    }

    public String description() {
        return description;
    }

    public LocalTime departureAtStartLocation() {
        return departureAtStartLocation;
    }

    public LocalTime arrivalAtEndLocation() {
        return arrivalAtEndLocation;
    }

    public LocalTime departureAtEndLocation() {
        return departureAtEndLocation;
    }

    // Add getters needed for proper JSON serialization
    public Location getStartLocation() {
        return startLocation;
    }

    public Location getEndLocation() {
        return endLocation;
    }

    public String getDescription() {
        return description;
    }

    public LocalTime getDepartureAtStartLocation() {
        return departureAtStartLocation;
    }

    public LocalTime getArrivalAtEndLocation() {
        return arrivalAtEndLocation;
    }

    public LocalTime getDepartureAtEndLocation() {
        return departureAtEndLocation;
    }
}