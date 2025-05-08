package com.airflow.scheduleservice.model;

public enum TaskType {
    PICKUP_FROM_AIRPORT,     // flight → warehouse
    DROP_OFF_AT_AIRPORT;    // warehouse → flight

    public static TaskType fromString(String s) {
        String norm = s.strip().toLowerCase();
        if (norm.contains("pickup"))   return PICKUP_FROM_AIRPORT;
        if (norm.contains("drop"))     return DROP_OFF_AT_AIRPORT;
        throw new IllegalArgumentException("Unknown task type: " + s);
    }
}
