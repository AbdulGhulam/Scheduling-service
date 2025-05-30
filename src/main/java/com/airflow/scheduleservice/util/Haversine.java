package com.airflow.scheduleservice.util;

public class Haversine {
    private static final int EARTH_RADIUS_KM = 6371;

    public static double distanceKm(
            double lat1, double lon1,
            double lat2, double lon2) {

        double dLat  = Math.toRadians(lat2 - lat1);
        double dLon  = Math.toRadians(lon2 - lon1);
        double a = Math.pow(Math.sin(dLat / 2), 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.pow(Math.sin(dLon / 2),2);
        return 2 * EARTH_RADIUS_KM * Math.asin(Math.sqrt(a));
    }
}
