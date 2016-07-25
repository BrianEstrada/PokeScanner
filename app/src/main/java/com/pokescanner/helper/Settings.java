package com.pokescanner.helper;

import android.content.Context;

import com.pokescanner.SettingsController;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Settings {
    boolean boundingBoxEnabled;
    int serverRefresh;
    int scale;
    int mapRefresh;
    String lastUsername;
    boolean showOnlyLured;
    boolean gymsEnabled;
    boolean pokestopsEnabled;
    boolean showLuredPokemon;

    public void save(Context context) {
        SettingsController.saveSettings(context, this);
    }

    public static Settings get(Context context) {
        return SettingsController.getSettings(context);
    }
}
