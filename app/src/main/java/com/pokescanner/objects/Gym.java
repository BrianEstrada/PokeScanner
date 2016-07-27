package com.pokescanner.objects;

import android.content.Context;
import android.graphics.Bitmap;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.pokescanner.utils.DrawableUtils;

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
public class Gym extends RealmObject
{
    int ownedByTeamValue;
    double latitude,longitude;
    int guardPokemonNo;
    int guardPokemonCp;
    long points;
    boolean inBattle;
    @Index
    String guardPokemonName;
    @PrimaryKey
    String id;

    public Gym()
    {
    }

    public Gym(FortDataOuterClass.FortData gymData)
    {
        setOwnedByTeamValue(gymData.getOwnedByTeamValue());
        setLatitude(gymData.getLatitude());
        setLongitude(gymData.getLongitude());
        setGuardPokemonNo(gymData.getGuardPokemonId().getNumber());
        setGuardPokemonName(gymData.getGuardPokemonId().toString());
        setId(gymData.getId());
        setPoints(gymData.getGymPoints());
        setInBattle(gymData.getIsInBattle());
        setGuardPokemonCp(gymData.getGuardPokemonCp());
    }

    public MarkerOptions getMarker(Context context) {
        LatLng position = new LatLng(getLatitude(), getLongitude());

        String guardPokemonName = getGuardPokemonName();
        long gymPoints = getPoints();
        guardPokemonName = guardPokemonName.substring(0, 1).toUpperCase() + guardPokemonName.substring(1).toLowerCase();

        int level = 0;
        if (gymPoints < 2000) level = 1;
        else if (gymPoints < 4000) level = 2;
        else if (gymPoints < 8000) level = 3;
        else if (gymPoints < 12000) level = 4;
        else if (gymPoints < 16000) level = 5;
        else if (gymPoints < 20000) level = 6;
        else if (gymPoints < 30000) level = 7;
        else if (gymPoints < 40000) level = 8;
        else if (gymPoints < 50000) level = 9;
        else level = 10;

        MarkerOptions gymMarker = new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(getBitmap(context)))
                .position(position)
                .title(getTitle())
                .snippet("Level: " + level + " | Points: " + gymPoints);
        return gymMarker;
    }

    public Bitmap getBitmap(Context context)
    {
        String uri = "gym" + getOwnedByTeamValue();
        int resourceID = context.getResources().getIdentifier(uri, "drawable", context.getPackageName());
        Bitmap out = DrawableUtils.getBitmapFromView(resourceID, "", context);
        return out;
    }

    public String getTitle()
    {
        int ownedBy = getOwnedByTeamValue();
        if(ownedBy == 0)
            return "Neutral";
        else if(ownedBy == 1)
            return "Mystic";
        else if(ownedBy == 2)
            return "Valor";
        else
            return "Instinct";
    }
}
