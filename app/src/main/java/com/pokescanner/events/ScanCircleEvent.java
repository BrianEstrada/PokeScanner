package com.pokescanner.events;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Brian on 7/31/2016.
 */
public class ScanCircleEvent {
    public LatLng pos;
    public int color;

    public ScanCircleEvent(LatLng pos) {
        this.pos = pos;
    }

    public ScanCircleEvent(LatLng pos,int color) {
        this.pos = pos;
        this.color = color;
    }
}
