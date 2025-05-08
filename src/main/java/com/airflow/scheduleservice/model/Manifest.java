package com.airflow.scheduleservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalTime;

public class Manifest {

    @JsonProperty("manifest_id")
    public String manifestId;
    @JsonProperty("company_name")
    public String companyName;
    @JsonProperty("dispatcher_name")
    public String dispatcherName;
    @JsonProperty("WareHouse_Id")
    public String warehouseId;
    @JsonProperty("warehouse_location")
    public Location warehouseLocation;
    @JsonProperty("location_name")
    public String locationName;
    @JsonProperty("task_type")
    public String taskTypeRaw;          // “pickup” / “drop off” in JSON
    @JsonProperty("flight_number")
    public String flightNumber;
    @JsonProperty("flight_arrival_time")
    public String flightArrival;        // HH:mm
    @JsonProperty("flight_departure_time")
    public String flightDeparture;      // HH:mm

    @JsonProperty("priority")
    public Priority priority;

    public String getManifestId() {
        return manifestId;
    }

    public void setManifestId(String manifestId) {
        this.manifestId = manifestId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getDispatcherName() {
        return dispatcherName;
    }

    public void setDispatcherName(String dispatcherName) {
        this.dispatcherName = dispatcherName;
    }

    public String getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(String warehouseId) {
        this.warehouseId = warehouseId;
    }

    public Location getWarehouseLocation() {
        return warehouseLocation;
    }

    public void setWarehouseLocation(Location warehouseLocation) {
        this.warehouseLocation = warehouseLocation;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getTaskTypeRaw() {
        return taskTypeRaw;
    }

    public void setTaskTypeRaw(String taskTypeRaw) {
        this.taskTypeRaw = taskTypeRaw;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
    }

    public String getFlightArrival() {
        return flightArrival;
    }

    public void setFlightArrival(String flightArrival) {
        this.flightArrival = flightArrival;
    }

    public String getFlightDeparture() {
        return flightDeparture;
    }

    public void setFlightDeparture(String flightDeparture) {
        this.flightDeparture = flightDeparture;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public TaskType taskType() {
        return taskTypeRaw.toLowerCase().contains("pickup")
                ? TaskType.PICKUP_FROM_AIRPORT
                : TaskType.DROP_OFF_AT_AIRPORT;
    }

    public LocalTime arrivalTime()  { return LocalTime.parse(flightArrival);  }
    public LocalTime departureTime(){ return LocalTime.parse(flightDeparture);}

    @Override public String toString() { return manifestId + " (" + taskType() + ")"; }
}
