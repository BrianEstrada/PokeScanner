package com.pokescanner.objects;

import android.content.Context;
import android.graphics.Bitmap;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.pokescanner.helper.DrawableUtils;

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
        String uri = "gym" + getOwnedByTeamValue();
        int resourceID = context.getResources().getIdentifier(uri, "drawable", context.getPackageName());

        LatLng position = new LatLng(getLatitude(), getLongitude());
        Bitmap out = DrawableUtils.writeTextOnDrawable(resourceID, "", 3, context);

        String guardPokemonName = getGuardPokemonName();
        long gymPoints = getPoints();
        guardPokemonName = guardPokemonName.substring(0, 1).toUpperCase() + guardPokemonName.substring(1).toLowerCase();

        String teamname;
        if (ownedByTeamValue == 1) teamname = "Mystic";
        else if (ownedByTeamValue == 2) teamname = "Valor";
        else if (ownedByTeamValue == 3) teamname = "Instinct";
        else teamname = "Neutral";

        int level;
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
                .icon(BitmapDescriptorFactory.fromBitmap(out))
                .position(position)
                .title(teamname)
                .snippet("Level: " + level + " | Points: " + gymPoints);
        return gymMarker;
    }
}
