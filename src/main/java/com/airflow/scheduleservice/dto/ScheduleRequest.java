// src/main/java/com/airflow/scheduleservice/dto/ScheduleRequest.java
package com.airflow.scheduleservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.airflow.scheduleservice.model.Manifest;
import com.airflow.scheduleservice.model.Truck;
import com.airflow.scheduleservice.model.Warehouse;
import java.time.LocalDateTime;
import java.util.List;

public record ScheduleRequest(
        List<Manifest> manifests,
        List<Truck> trucks,
        List<Warehouse> warehouses,
        @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime currentTime,
        List<ScheduleEvent> events
) {}
