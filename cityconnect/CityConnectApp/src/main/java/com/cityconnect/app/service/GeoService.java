package com.cityconnect.app.service;

import com.cityconnect.app.model.Infrastructure;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GeoService {
    public List<Infrastructure> findNearby(List<Infrastructure> all, double lat, double lon, double radiusKm) {
        List<Infrastructure> nearby = new ArrayList<>();
        for (Infrastructure infra : all) {
            double dist = haversine(lat, lon, infra.getLatitude(), infra.getLongitude());
            if (dist <= radiusKm) {
                nearby.add(infra);
            }
        }
        return nearby;
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(dLon/2) * Math.sin(dLon/2);
        return R * (2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)));
    }
}
