package com.pokescanner.helpers;

import com.pokegoapi.api.map.Pokemon.CatchablePokemon;

import java.util.List;

/**
 * Created by Brian on 7/20/2016.
 */
public class PokemonCollection {
    List<CatchablePokemon> pokemons;

    public PokemonCollection(List<CatchablePokemon> pokemons) {
        this.pokemons = pokemons;
    }

    public List<CatchablePokemon> getPokemons() {
        return pokemons;
    }

    public void setPokemons(List<CatchablePokemon> pokemons) {
        this.pokemons = pokemons;
    }
}
