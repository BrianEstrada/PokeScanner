package com.pokescanner.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.support.v4.content.ContextCompat;

/**
 * Created by Brian on 7/25/2016.
 */
public class LocationUtils {
    public static boolean doWeHavePermission(Context context) {
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
    public static boolean isGPSEnabled(Context context) {
        LocationManager cm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return cm.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public static boolean doWeHaveGPSandLOC(Context context) {
        return  doWeHavePermission(context) && isGPSEnabled(context);
    }
}
