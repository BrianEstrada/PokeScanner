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
import com.pokescanner.objects.FilterItem;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;

import io.realm.Realm;

/**
 * Created by Brian on 7/21/2016.
 */
public class PokemonListLoader {

    public static ArrayList<FilterItem> getPokelist(Context context) throws IOException {
        Realm realm = Realm.getDefaultInstance();
        if (realm.where(FilterItem.class).findAll().size() == 151) {
            return new ArrayList<>(realm.copyFromRealm(
                    realm.where(FilterItem.class)
                            .findAll()
                            .sort("Number")));
        }else
        {
            InputStream is = context.getAssets().open("pokemons.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String bufferString = new String(buffer);
            Gson gson = new Gson();
            Type listType = new TypeToken<ArrayList<FilterItem>>() {}.getType();
            final ArrayList<FilterItem> filterItems = gson.fromJson(bufferString, listType);
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.copyToRealmOrUpdate(filterItems);
                }
            });
            return filterItems;
        }
    }

    public static ArrayList<FilterItem> getFilteredList() {
        Realm realm = Realm.getDefaultInstance();
        ArrayList returnArray =  new ArrayList<>(realm.copyFromRealm(
                realm.where(FilterItem.class)
                .equalTo("filtered",true)
                .findAll()
                .sort("Number")));
        realm.close();
        return returnArray;
    }
    public static void savePokeList(final ArrayList<FilterItem> pokelist) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealmOrUpdate(pokelist);
            }
        });
        realm.close();
    }

}
