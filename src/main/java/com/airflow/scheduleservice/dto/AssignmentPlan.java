package com.airflow.scheduleservice.dto;

import java.util.List;

public record AssignmentPlan(
        String    manifestId,
        int       sequence,
        String    taskType,
        String    flightNumber,
        String    warehouseId,
        List<Stop> legs
) {}
