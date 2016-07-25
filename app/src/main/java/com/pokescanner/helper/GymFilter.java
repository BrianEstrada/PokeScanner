package com.pokescanner.helper;

import android.content.Context;

import com.pokescanner.GymFilters;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class GymFilter
{
    boolean neutralGymsEnabled;
    boolean yellowGymsEnabled;
    boolean blueGymsEnabled;
    boolean redGymsEnabled;
    int guardPokemonMinCp;
    int guardPokemonMaxCp;

    public void saveGymFilter(Context context)
    {
        GymFilters.saveGymFilter(context, this);
    }

    public static GymFilter getGymFilter(Context context)
    {
        return GymFilters.getGymFilter(context);
    }
}
