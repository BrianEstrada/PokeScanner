package com.pokescanner.helper;

import com.pokescanner.objects.Pokemons;

import java.util.Comparator;

/**
 * Created by Brian on 7/28/2016.
 */
public class PokeDistanceSorter implements Comparator<Pokemons> {
    @Override
    public int compare(Pokemons pokemons, Pokemons t1) {
        int returnVal = 0;

        if(pokemons.getDistance() < t1.getDistance()){
            returnVal =  -1;
        }else if(pokemons.getDistance() > t1.getDistance()){
            returnVal =  1;
        }else if(pokemons.getDistance() == t1.getDistance()){
            returnVal =  0;
        }
        return returnVal;
    }
}
