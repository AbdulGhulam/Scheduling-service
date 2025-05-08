package com.airflow.scheduleservice.model;

import com.airflow.scheduleservice.dto.Stop;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Truck {
    @JsonProperty("truckId")
    public String id;

    @JsonProperty("truck_start")
    public String startRaw;    // e.g. 04:00AM
    @JsonProperty("truck_end")
    public String endRaw;

    @JsonProperty("truck_home_warehouseId")
    public String homeWarehouseId;

    @JsonProperty("isSpare")
    public boolean spare = false;   // default false; optional in JSON

    private final List<com.airflow.scheduleservice.dto.Stop> stops = new ArrayList<>();

    // Getters & setters
    public String getId()                     { return id; }
    public void setId(String id)              { this.id = id; }

    public String getStartRaw()               { return startRaw; }
    public void setStartRaw(String s)         { this.startRaw = s; }

    public String getEndRaw()                 { return endRaw; }
    public void setEndRaw(String e)           { this.endRaw = e; }

    public String getHomeWarehouseId()        { return homeWarehouseId; }
    public void setHomeWarehouseId(String h)  { this.homeWarehouseId = h; }

    public boolean isSpare()                  { return spare; }
    public void setSpare(boolean s)           { this.spare = s; }

    public List<com.airflow.scheduleservice.dto.Stop> getStops() {
        return stops;
    }

    // Helpers
    private static final DateTimeFormatter[] FMTS = {
            DateTimeFormatter.ofPattern("H:mm"),
            DateTimeFormatter.ofPattern("h:mma")
    };

    public LocalTime shiftStart() {
        for (var fmt : FMTS) {
            try { return LocalTime.parse(startRaw.toUpperCase(), fmt); }
            catch (Exception ignored) {}
        }
        throw new IllegalArgumentException("Bad start: " + startRaw);
    }
    public LocalTime shiftEnd() {
        for (var fmt : FMTS) {
            try { return LocalTime.parse(endRaw.toUpperCase(), fmt); }
            catch (Exception ignored) {}
        }
        throw new IllegalArgumentException("Bad end: " + endRaw);
    }

    public com.airflow.scheduleservice.dto.Stop getLastStop() {
        if (stops.isEmpty()) return null;
        return stops.get(stops.size() - 1);
    }
}
