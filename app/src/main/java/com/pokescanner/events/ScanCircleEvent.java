package com.pokescanner.events;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Brian on 7/31/2016.
 */
public class ScanCircleEvent {
    public LatLng pos;

    public ScanCircleEvent(LatLng pos) {
        this.pos = pos;
    }
}
