package com.airflow.scheduleservice.model;

public enum Priority { LOW, MEDIUM, HIGH;
    public static Priority fromString(String s) {
        try {
            return Priority.valueOf(s.strip().toUpperCase());
        } catch (Exception e) {
            return MEDIUM;
        }
    }
}

