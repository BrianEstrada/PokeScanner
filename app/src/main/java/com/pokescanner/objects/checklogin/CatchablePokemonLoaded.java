package com.pokescanner.objects.checklogin;

import java.util.Collection;

import POGOProtos.Map.Pokemon.MapPokemonOuterClass;

/**
 * Created by Brian on 7/20/2016.
 */
public class CatchablePokemonLoaded {
    Collection<MapPokemonOuterClass.MapPokemon> pokemonsOut;

    public CatchablePokemonLoaded(Collection<MapPokemonOuterClass.MapPokemon> pokemonsOut) {
        this.pokemonsOut = pokemonsOut;
    }

    public Collection<MapPokemonOuterClass.MapPokemon> getPokemonsOut() {
        return pokemonsOut;
    }

    public void setPokemonsOut(Collection<MapPokemonOuterClass.MapPokemon> pokemonsOut) {
        this.pokemonsOut = pokemonsOut;
    }
}
