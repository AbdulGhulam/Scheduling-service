package com.airflow.scheduleservice.service;

import com.airflow.scheduleservice.model.Location;
import com.airflow.scheduleservice.util.Haversine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

//@Component
public class DistanceService {
    private final double averageSpeedMph;

    public DistanceService(@Value("${scheduler.averageSpeedMph}") double mph) {
        this.averageSpeedMph = mph;
    }

    public int travelMinutes(Location a, Location b) {
        var km = Haversine.distanceKm(
                a.latitude(), a.longitude(),
                b.latitude(), b.longitude());
        double miles = km * 0.621371;
        return (int) Math.round(miles / averageSpeedMph * 60);
    }
}
