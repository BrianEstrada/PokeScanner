package com.pokescanner;

import com.pokegoapi.api.map.MapObjects;

/**
 * Created by Brian on 7/19/2016.
 */
public class PokemonLoadedEvent {
    MapObjects mapObjects;

    public PokemonLoadedEvent(MapObjects mapObjects) {
        this.mapObjects = mapObjects;
    }

    public MapObjects getMapObjects() {
        return mapObjects;
    }

    public void setMapObjects(MapObjects mapObjects) {
        this.mapObjects = mapObjects;
    }
}
