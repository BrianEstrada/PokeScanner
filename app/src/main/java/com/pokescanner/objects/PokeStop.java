package com.pokescanner.objects;

import android.content.Context;
import android.graphics.Bitmap;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.pokescanner.SettingsController;
import com.pokescanner.helper.DrawableUtils;
import com.pokescanner.helper.Settings;

import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import POGOProtos.Map.Fort.FortDataOuterClass;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PokeStop extends RealmObject
{
    double latitude, longitude;
    @PrimaryKey
    String id;
    @Index
    String activePokemonName;
    boolean hasLureInfo;
    long lureExpiryTimestamp;
    long activePokemonNo;
    public PokeStop()
    {
    }

    public PokeStop(FortDataOuterClass.FortData pokestopData)
    {
        setLatitude(pokestopData.getLatitude());
        setLongitude(pokestopData.getLongitude());
        setId(pokestopData.getId());
        setHasLureInfo(pokestopData.hasLureInfo());
        setLureExpiryTimestamp(pokestopData.getLureInfo().getLureExpiresTimestampMs());
        setActivePokemonNo(pokestopData.getLureInfo().getActivePokemonId().getNumber());
        setActivePokemonName(pokestopData.getLureInfo().getActivePokemonId().toString());
    }

    public DateTime getExpiryTime()
    {
        return new DateTime(getLureExpiryTimestamp());
    }

    public MarkerOptions getMarker(Context context)
    {
        String uri = "";
        String snippetMessage  = "";
        String timeout = "";
        if(hasLureInfo) //There is a lure active at the pokestop
        {
            if(getExpiryTime().isAfter(new Instant()))  //The lure is currently active
            {
                uri = "stop_lure";
                Interval interval = new Interval(new Instant(), getExpiryTime());
                DateTime dt = new DateTime(interval.toDurationMillis());
                DateTimeFormatter fmt = DateTimeFormat.forPattern("mm:ss");
                timeout = fmt.print(dt);

                String activePokemonName = getActivePokemonName();
                activePokemonName = activePokemonName.substring(0, 1).toUpperCase() + activePokemonName.substring(1).toLowerCase();
                snippetMessage = "Lure Expires: " + timeout + " | Current Lure Pokémon: " + activePokemonName;
            }
            else
                uri = "stop";
        }
        else
            uri = "stop";
        int resourceID = context.getResources().getIdentifier(uri, "drawable", context.getPackageName());

        LatLng position = new LatLng(getLatitude(), getLongitude());
        Bitmap out = DrawableUtils.writeTextOnDrawable(resourceID, timeout, 3, context);

        MarkerOptions pokestopMarker = new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(out))
                .position(position)
                .title("Pokéstop")
                .snippet(snippetMessage);
        return pokestopMarker;
    }

    public MarkerOptions getMarker2(Context context)
    {
        String uri = "";
        String snippetMessage  = "";
        String timeout = "";
        if(hasLureInfo) //There is a lure active at the pokestop
        {
            if(getExpiryTime().isAfter(new Instant()))  //The lure is currently active
            {
                //uri = "stop_lure";
                Interval interval = new Interval(new Instant(), getExpiryTime());
                DateTime dt = new DateTime(interval.toDurationMillis());
                DateTimeFormatter fmt = DateTimeFormat.forPattern("mm:ss");
                timeout = fmt.print(dt);

                String activePokemonName = getActivePokemonName();
                activePokemonName = activePokemonName.substring(0, 1).toUpperCase() + activePokemonName.substring(1).toLowerCase();
                snippetMessage = "Current Lure Pokémon: " + activePokemonName + " | Expires: " + timeout;

                uri = "p" + getActivePokemonNo();
            }
            else
                uri = "stop";
        }
        else
            uri = "stop";
        int resourceID = context.getResources().getIdentifier(uri, "drawable", context.getPackageName());

        LatLng position = new LatLng(getLatitude(), getLongitude());
        Bitmap out = DrawableUtils.writeTextOnDrawable(resourceID, timeout, 3, context);

        MarkerOptions pokestopMarker = new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(out))
                .position(position)
                .title("Pokéstop")
                .snippet(snippetMessage);
        return pokestopMarker;
    }
}
