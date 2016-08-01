package com.pokescanner.objects;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by Brian on 7/30/2016.
 */
public class PokeCluster implements ClusterItem {
        private final LatLng mPosition;

        public PokeCluster(double lat, double lng) {
            mPosition = new LatLng(lat, lng);
        }

        @Override
        public LatLng getPosition() {
            return mPosition;
        }
}