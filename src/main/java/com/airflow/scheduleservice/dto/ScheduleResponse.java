// src/main/java/com/airflow/scheduleservice/dto/ScheduleResponse.java
package com.airflow.scheduleservice.dto;

import java.util.List;

public record ScheduleResponse(
        List<TruckPlan> plans,
        List<NotDoable> notDoable
) {}
