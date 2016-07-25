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
        Bitmap out = DrawableUtils.writeTextOnDrawable(resourceID, "Gym", 2, context);

        String guardPokemonName = getGuardPokemonName();
        long gymPoints = getPoints();
        guardPokemonName = guardPokemonName.substring(0, 1).toUpperCase() + guardPokemonName.substring(1).toLowerCase();

        MarkerOptions gymMarker = new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(out))
                .position(position)
                .title("Gym")
                .snippet("Guarded by : " + guardPokemonName + "\nPoints: " + gymPoints);
        return gymMarker;
    }
}
