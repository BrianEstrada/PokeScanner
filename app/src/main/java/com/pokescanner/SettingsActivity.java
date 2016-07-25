package com.pokescanner;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import com.pokescanner.helper.Settings;
import com.pokescanner.utils.SettingsUtil;

public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.registerOnSharedPreferenceChangeListener(this);
        Settings settings = SettingsUtil.getSettings(this);

        preferences.edit()
                .putBoolean(SettingsUtil.KEY_BOUNDING_BOX,settings.isBoundingBoxEnabled())
                .putBoolean(SettingsUtil.SHOW_ONLY_LURED,settings.isShowOnlyLured())
                .putBoolean(SettingsUtil.SHOW_GYMS,settings.isGymsEnabled())
                .putBoolean(SettingsUtil.SHOW_POKESTOPS,settings.isPokestopsEnabled())
                .putBoolean(SettingsUtil.KEY_LOCK_GPS,settings.isLockGpsEnabled())
                .putString(SettingsUtil.SERVER_REFRESH_RATE,String.valueOf(settings.getServerRefresh()))
                .putString(SettingsUtil.MAP_REFRESH_RATE,String.valueOf(settings.getMapRefresh()))
                .putString(SettingsUtil.POKEMON_ICON_SCALE,String.valueOf(settings.getScale()))
                .putString(SettingsUtil.LAST_USERNAME,settings.getLastUsername())
                .commit();

        addPreferencesFromResource(R.xml.settings);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        System.out.println(sharedPreferences.getAll().toString());
        SettingsUtil.saveSettings(this,new Settings(
                sharedPreferences.getBoolean(SettingsUtil.KEY_BOUNDING_BOX, false),
                sharedPreferences.getBoolean(SettingsUtil.KEY_LOCK_GPS, false),
                Integer.valueOf(sharedPreferences.getString(SettingsUtil.SERVER_REFRESH_RATE, "1")),
                Integer.valueOf(sharedPreferences.getString(SettingsUtil.POKEMON_ICON_SCALE, "2")),
                Integer.valueOf(sharedPreferences.getString(SettingsUtil.MAP_REFRESH_RATE, "2")),
                sharedPreferences.getString(SettingsUtil.LAST_USERNAME, ""),
                sharedPreferences.getBoolean(SettingsUtil.SHOW_ONLY_LURED, true),
                sharedPreferences.getBoolean(SettingsUtil.SHOW_GYMS, true),
                sharedPreferences.getBoolean(SettingsUtil.SHOW_POKESTOPS, true)
        ));
        SettingsUtil.forceRefresh();
        SettingsUtil.restartRefresh();
    }
}
