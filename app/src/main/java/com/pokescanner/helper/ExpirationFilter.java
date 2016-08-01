package com.pokescanner.helper;

import android.content.Context;

import com.pokescanner.ExpirationFilters;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ExpirationFilter
{
    int pokemonExpirationMinSec;

    public void saveFilter(Context context)
    {
        ExpirationFilters.saveFilter(context, this);
    }

    public static ExpirationFilter getFilter(Context context)
    {
        return ExpirationFilters.getFilter(context);
    }
}
