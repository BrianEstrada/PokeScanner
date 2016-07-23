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
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

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
    double longitude,laditude;

    public Pokemons() {}

    public Pokemons(MapPokemonOuterClass.MapPokemon pokemonIn){
            setEncounterid(pokemonIn.getEncounterId());
            setName(pokemonIn.getPokemonId().toString());
            setExpires(pokemonIn.getExpirationTimestampMs());
            setNumber(pokemonIn.getPokemonId().getNumber());
            setLaditude(pokemonIn.getLatitude());
            setLongitude(pokemonIn.getLongitude());
    }

    public DateTime getDate() {
        return new DateTime(getExpires());
    }
    public MarkerOptions getMarker(Context context) {
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
        LatLng position = new LatLng(getLaditude(), getLongitude());

        Bitmap out = writeTextOnDrawable(resourceID,timeOut,2,context);

        String name = getName();
        name = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();

        MarkerOptions pokeIcon = new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(out))
                .position(position)
                .title(name)
                .snippet(timeOut);

        return pokeIcon;
    }

    private Bitmap writeTextOnDrawable(int drawableId, String text, int scale,Context context) {
        Bitmap bm = BitmapFactory.decodeResource(context.getResources(), drawableId)
                .copy(Bitmap.Config.ARGB_8888, true);
        bm = Bitmap.createScaledBitmap(bm,bm.getWidth()/scale,bm.getHeight()/scale,false);

        Typeface tf = Typeface.create("Helvetica", Typeface.BOLD);

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLACK);
        paint.setTypeface(tf);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(convertToPixels(context, 11));

        Rect textRect = new Rect();
        paint.getTextBounds(text, 0, text.length(), textRect);

        Canvas canvas = new Canvas(bm);

        //If the text is bigger than the canvas , reduce the font size
        if(textRect.width() >= (canvas.getWidth() - 4))     //the padding on either sides is considered as 4, so as to appropriately fit in the text
            paint.setTextSize(convertToPixels(context, 7));        //Scaling needs to be used for different dpi's

        //Calculate the positions
        int xPos = (canvas.getWidth() / 2) - 2;     //-2 is for regulating the x position offset

        //"- ((paint.descent() + paint.ascent()) / 2)" is the distance from the baseline to the center.
        int yPos = (int) ((canvas.getHeight() / 2) - ((paint.descent() + paint.ascent()) / 2)) ;

        canvas.drawText(text, xPos, yPos-convertToPixels(context,16), paint);

        return  bm;
    }

    public static int convertToPixels(Context context, int nDP) {
        final float conversionScale = context.getResources().getDisplayMetrics().density;

        return (int) ((nDP * conversionScale) + 0.5f) ;

    }
}
