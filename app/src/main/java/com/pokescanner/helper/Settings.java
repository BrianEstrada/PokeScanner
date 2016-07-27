package com.pokescanner.helper;

import android.content.Context;

import com.pokescanner.utils.SettingsUtil;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Settings {
    boolean updatesEnabled;
    boolean boundingBoxEnabled;
    boolean lockGpsEnabled;
    int scanValue;
    int serverRefresh;
    int scale;
    int mapRefresh;
    String lastUsername;
    boolean showOnlyLured;
    boolean gymsEnabled;
    boolean pokestopsEnabled;
    boolean useOldMapMarker;

    public void save(Context context) {
        SettingsUtil.saveSettings(context, this);
    }

    public static Settings get(Context context) {
        return SettingsUtil.getSettings(context);
    }
}
