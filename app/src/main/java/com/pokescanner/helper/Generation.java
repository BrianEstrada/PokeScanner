package com.pokescanner.helper;

import com.google.android.gms.maps.model.LatLng;

import java.util.Arrays;
import java.util.List;


public class Generation {
    private static boolean equal(double a, double b) {
        return Math.abs(a - b) < 0.000005;
    }

    public static List<LatLng> getCorners(List<LatLng> scanMap) {
        double minlat = 9001;
        double maxlat = -9001;
        double minlon = 9001;
        double maxlon = -9001;
        double toplat = 9001;
        double botlat = -9001;
        int mapSize = scanMap.size();
        for (int i = 0; i < mapSize; i++) {
            LatLng item = scanMap.get(i);
            minlat = Math.min(item.latitude, minlat);
            minlon = Math.min(item.longitude, minlon);
            maxlat = Math.max(item.latitude, maxlat);
            maxlon = Math.max(item.longitude, maxlon);
            if (equal(minlon, item.longitude)) {
                toplat = Math.min(toplat, item.latitude);
                botlat = Math.max(botlat, item.latitude);
            }
        }

        double midlon = minlon + (maxlon - minlon) / 2;

        return Arrays.asList(
            new LatLng(minlat, midlon),
            new LatLng(toplat, maxlon),
            new LatLng(botlat, maxlon),
            new LatLng(maxlat, midlon),
            new LatLng(botlat, minlon),
            new LatLng(toplat, minlon)
        );
    }

    // Call with layer_count initially 1
    // REQUIRES: not empty scanMap, layer_count > 0, loc is the starting loc
    public static List<LatLng> makeHexScanMap(LatLng loc, int steps, int layer_count, List<LatLng> scanMap) {
        // Base case is do nothing
        if (steps > 0) {
            if (layer_count == 1) {
                // Add in the point, no translation since 1st layer
                scanMap.add(loc);
            } else {
                double distance = 70; // in meters
                // add a point that is distance due north
                scanMap.add(translate(loc, 0.0, distance));
                // go south-east
                for (int i = 0; i < layer_count - 1; i++) {
                    LatLng prev = scanMap.get(scanMap.size() - 1);
                    LatLng next = translate(prev, 120.0, distance);
                    scanMap.add(next);
                }
                // go due south
                for (int i = 0; i < layer_count - 1; i++) {
                    LatLng prev = scanMap.get(scanMap.size() - 1);
                    LatLng next = translate(prev, 180.0, distance);
                    scanMap.add(next);
                }
                // go south-west
                for (int i = 0; i < layer_count - 1; i++) {
                    LatLng prev = scanMap.get(scanMap.size() - 1);
                    LatLng next = translate(prev, 240.0, distance);
                    scanMap.add(next);
                }
                // go north-west
                for (int i = 0; i < layer_count - 1; i++) {
                    LatLng prev = scanMap.get(scanMap.size() - 1);
                    LatLng next = translate(prev, 300.0, distance);
                    scanMap.add(next);
                }
                // go due north
                for (int i = 0; i < layer_count - 1; i++) {
                    LatLng prev = scanMap.get(scanMap.size() - 1);
                    LatLng next = translate(prev, 0.0, distance);
                    scanMap.add(next);
                }
                // go north-east
                for (int i = 0; i < layer_count - 2; i++) {
                    LatLng prev = scanMap.get(scanMap.size() - 1);
                    LatLng next = translate(prev, 60.0, distance);
                    scanMap.add(next);
                }
            }
            return makeHexScanMap(scanMap.get(hexagonal_number(layer_count - 1)), steps - 1, layer_count + 1, scanMap);
        } else {
            return scanMap;
        }
    }

    // Takes in distance in meters, bearing in degrees
    public static LatLng translate(LatLng cur, double bearing, double distance) {
        double earth = 6378.1; // Radius of Earth in km
        double rad_bear = Math.toRadians(bearing);
        double dist_km = distance/1000;
        double lat1 = Math.toRadians(cur.latitude);
        double lon1 = Math.toRadians(cur.longitude);
        double lat2 =  Math.asin( Math.sin(lat1) * Math.cos(dist_km/earth) +
                Math.cos(lat1) * Math.sin(dist_km/earth) * Math.cos(rad_bear));
        double lon2 = lon1 + Math.atan2(Math.sin(rad_bear) * Math.sin(dist_km/earth) * Math.cos(lat1),
                Math.cos(dist_km/earth) - Math.sin(lat1) * Math.sin(lat2));
        lat2 = Math.toDegrees(lat2);
        lon2 = Math.toDegrees(lon2);
        return new LatLng(lat2, lon2);
    }

    public static int hexagonal_number(int n) {
        return (n == 0) ? 0 : 3 * n * (n - 1) + 1;
    }
}
