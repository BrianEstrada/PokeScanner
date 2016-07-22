/*
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */





package com.pokescanner.helper;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pokescanner.objects.Pokemons;

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
