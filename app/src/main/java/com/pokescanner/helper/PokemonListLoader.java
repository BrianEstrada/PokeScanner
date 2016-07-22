package com.pokescanner.helper;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pokescanner.objects.pokemon.Pokemons;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Created by Brian on 7/21/2016.
 */
public class PokemonListLoader {
    Context context;

    public PokemonListLoader(Context context) {
        this.context = context;
    }

    public ArrayList<Pokemons> getPokelist() throws IOException {
        InputStream is = context.getAssets().open("pokemons.json");
        int size = is.available();
        byte[] buffer = new byte[size];
        is.read(buffer);
        is.close();
        String bufferString = new String(buffer);
        Gson gson = new Gson();
        Type listType = new TypeToken<ArrayList<Pokemons>>() {}.getType();
        return gson.fromJson(bufferString,listType);
    }
}
