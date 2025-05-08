package com.airflow.scheduleservice.service;

import com.airflow.scheduleservice.model.Location;
import com.google.maps.GeoApiContext;
import com.google.maps.DirectionsApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.TravelMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

@Component
public class GoogleDistanceService {
    private final GeoApiContext geoApiContext;
    private final ZoneId zoneId;

    public GoogleDistanceService(
            @Value("${google.maps.apiKey}") String apiKey,
            @Value("${scheduler.zoneId:America/Chicago}") String zoneId
    ) {
        this.geoApiContext = new GeoApiContext.Builder()
                .apiKey("AIzaSyBDJulT5VcbyLhWrGn6io1YeZLVYSfBEYU")
                .build();
        this.zoneId = ZoneId.of(zoneId);
    }

    /**
     * Fetches live travel time in minutes (rounded up) using Google Directions API with traffic.
     */
    public int travelMinutes(Location origin, Location destination) {
        try {
            String originStr = origin.latitude() + "," + origin.longitude();
            String destStr   = destination.latitude() + "," + destination.longitude();
            Instant now      = Instant.now();

            DirectionsResult result = DirectionsApi.newRequest(geoApiContext)
                    .mode(TravelMode.DRIVING)
                    .origin(originStr)
                    .destination(destStr)
                    .departureTime(now)
                    .await();

            long seconds = result.routes[0]
                    .legs[0]
                    .durationInTraffic
                    .inSeconds;

            // Round up to nearest minute
            return (int) Math.ceil(seconds / 60.0);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch travel time: " + e.getMessage(), e);
        }
    }

    /**
     * Calculates departure LocalDateTime so that arrival at destination matches targetArrival.
     * If the requested arrival is in the past (for testing), retries with the same clock-time tomorrow.
     */
    public LocalDateTime computeDepartureTime(
            Location origin,
            Location destination,
            LocalDateTime targetArrival
    ) {
        Instant arrivalTs = targetArrival.atZone(zoneId).toInstant();

        try {
            return computeWithArrival(origin, destination, arrivalTs);
        } catch (ApiException apiEx) {
            String msg = apiEx.getMessage();
            if (msg != null && msg.toLowerCase().contains("departure_time is in the past")) {
                // Retry for the same time on the next day
                Instant nextDay = arrivalTs.plus(1, ChronoUnit.DAYS);
                try {
                    return computeWithArrival(origin, destination, nextDay);
                } catch (Exception retryEx) {
                    throw new RuntimeException(
                            "Retry for next-day arrival also failed: " + retryEx.getMessage(), retryEx
                    );
                }
            }
            throw new RuntimeException("Failed to compute departure time: " + msg, apiEx);
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute departure time: " + e.getMessage(), e);
        }
    }

    /**
     * Internal helper that actually calls the Directions API for a given arrival Instant.
     */
    private LocalDateTime computeWithArrival(
            Location origin,
            Location destination,
            Instant arrivalTs
    ) throws Exception {
        String originStr = origin.latitude() + "," + origin.longitude();
        String destStr   = destination.latitude() + "," + destination.longitude();

        DirectionsResult result = DirectionsApi.newRequest(geoApiContext)
                .mode(TravelMode.DRIVING)
                .origin(originStr)
                .destination(destStr)
                .departureTime(arrivalTs)
                .await();

        long seconds = result.routes[0]
                .legs[0]
                .durationInTraffic
                .inSeconds;

        Instant departInstant = arrivalTs.minusSeconds(seconds);
        return LocalDateTime.ofInstant(departInstant, zoneId);
    }
}
