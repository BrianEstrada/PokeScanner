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



package com.pokescanner.objects;

import android.content.Context;
import android.graphics.Bitmap;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.pokescanner.utils.DrawableUtils;

import org.joda.time.DateTime;
import org.joda.time.Instant;

import POGOProtos.Map.Pokemon.MapPokemonOuterClass;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * Created by Brian on 7/21/2016.
 */
@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Pokemons  extends RealmObject{
    int Number;
    @Index
    String Name;
    @PrimaryKey
    long encounterid;
    long expires;
    double longitude,latitude;

    public Pokemons() {}

    public Pokemons(MapPokemonOuterClass.MapPokemon pokemonIn){
            setEncounterid(pokemonIn.getEncounterId());
            setName(pokemonIn.getPokemonId().toString());
            setExpires(pokemonIn.getExpirationTimestampMs());
            setNumber(pokemonIn.getPokemonId().getNumber());
            setLatitude(pokemonIn.getLatitude());
            setLongitude(pokemonIn.getLongitude());
    }

    public int getResourceID(Context context) {
        return DrawableUtils.getResourceID(getNumber(),context);
    }
    public boolean isExpired() {
        //Create a date
        DateTime expires = new DateTime(getExpires());
        //If this date is after the current time then it has not expired!
        if (expires.isAfter(new Instant())) {
            return true;
        }else {
            return false;
        }
    }

    public MarkerOptions getMarker(Context context) {
        int resourceID = getResourceID(context);
        //Find our interval
        String timeOut = DrawableUtils.getExpireTime(getExpires());
        //set our location
        LatLng position = new LatLng(getLatitude(), getLongitude());


        MarkerOptions pokeIcon = new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(out))
                .position(position);
        return pokeIcon;
    }

    public String getFormalName() {
        String name = getName();
        return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
    }

    public Marker updateMarker(Marker marker,Context context) {

        String expires = DrawableUtils.getExpireTime(getExpires());


        marker.setIcon(BitmapDescriptorFactory.fromBitmap(newbit));

        return marker;
    }
}
