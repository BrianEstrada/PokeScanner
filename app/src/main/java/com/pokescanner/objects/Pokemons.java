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
import com.google.android.gms.maps.model.MarkerOptions;
import com.pokescanner.helper.DrawableUtils;

import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

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

    public DateTime getDate() {
        return new DateTime(getExpires());
    }
    public MarkerOptions getMarker(Context context,int scale) {
        String uri = "p" + getNumber();
        int resourceID = context.getResources().getIdentifier(uri, "drawable", context.getPackageName());


        Interval interval;
        //Find our interval
        interval = new Interval(new Instant(), getDate());
        //turn our interval into MM:SS
        DateTime dt = new DateTime(interval.toDurationMillis());
        DateTimeFormatter fmt = DateTimeFormat.forPattern("mm:ss");
        String timeOut = fmt.print(dt);
        //set our location
        LatLng position = new LatLng(getLatitude(), getLongitude());

        Bitmap out = DrawableUtils.writeTextOnDrawable(resourceID,timeOut,scale,context);

        String name = getName();
        name = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();

        String snippetmessage = "Expires: " + timeOut;

        MarkerOptions pokeIcon = new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(out))
                .position(position)
                .title(name)
                .snippet(snippetmessage);

        return pokeIcon;
    }
}
