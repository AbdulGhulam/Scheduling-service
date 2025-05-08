// src/main/java/com/airflow/scheduleservice/dto/ScheduleEvent.java
package com.airflow.scheduleservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public class ScheduleEvent {
    private String type;                // e.g. "truck_breakdown"
    private String truckId;             // for truck_* events
    private String warehouseId;         // for dock_unavailable
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startTime;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endTime;      // or repair-until
    private Integer delayMinutes;       // for jams, closures, gate changes
    private Integer expectedRepairDurationMinutes; // for breakdowns

    // Jackson requires a no-args ctor
    public ScheduleEvent() {}

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTruckId() {
        return truckId;
    }

    public void setTruckId(String truckId) {
        this.truckId = truckId;
    }

    public String getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(String warehouseId) {
        this.warehouseId = warehouseId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Integer getDelayMinutes() {
        return delayMinutes;
    }

    public void setDelayMinutes(Integer delayMinutes) {
        this.delayMinutes = delayMinutes;
    }

    public Integer getExpectedRepairDurationMinutes() {
        return expectedRepairDurationMinutes;
    }

    public void setExpectedRepairDurationMinutes(Integer expectedRepairDurationMinutes) {
        this.expectedRepairDurationMinutes = expectedRepairDurationMinutes;
    }
}
