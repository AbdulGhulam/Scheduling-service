// src/main/java/com/airflow/scheduleservice/dto/NotDoable.java
package com.airflow.scheduleservice.dto;

public record NotDoable(
        String manifestId,
        String reason
) {}
