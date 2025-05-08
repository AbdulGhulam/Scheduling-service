package com.airflow.scheduleservice.dto;

import com.airflow.scheduleservice.model.Location;

import java.time.LocalTime;

public record Leg(
        Location startLocation,
        Location   endLocation,
        String     description,
        LocalTime arrivalAtEndLocation,
        LocalTime  departureFromEndLocation
) {}
