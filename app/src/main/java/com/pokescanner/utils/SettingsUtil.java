package com.pokescanner.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.pokescanner.R;
import com.pokescanner.events.ForceRefreshEvent;
import com.pokescanner.events.RestartRefreshEvent;
import com.pokescanner.helper.Settings;

import org.greenrobot.eventbus.EventBus;


public class SettingsUtil {

    static final String KEY_BOUNDING_BOX = "boundingBoxEnabled";
    static final String SHOW_ONLY_LURED = "showOnlyLured";
    static final String SHOW_GYMS = "showGyms";
    static final String SHOW_POKESTOPS = "showPokestops";
    static final String KEY_LOCK_GPS = "lockGpsEnabled";

    static final String SERVER_REFRESH_RATE = "serverRefreshRate";
    static final String MAP_REFRESH_RATE = "mapRefreshRate";
    static final String POKEMON_ICON_SCALE = "pokemonIconScale";

    static final String LAST_USERNAME = "lastUsername";

    public static Settings getSettings(Context context) {
        SharedPreferences sharedPrefs = context.getSharedPreferences(
            context.getString(R.string.shared_key),
            Context.MODE_PRIVATE
        );
        return new Settings(
            sharedPrefs.getBoolean(KEY_BOUNDING_BOX, false),
            sharedPrefs.getBoolean(KEY_LOCK_GPS, false),
            sharedPrefs.getInt(SERVER_REFRESH_RATE, 1),
            sharedPrefs.getInt(POKEMON_ICON_SCALE, 2),
            sharedPrefs.getInt(MAP_REFRESH_RATE, 2),
            sharedPrefs.getString(LAST_USERNAME, ""),
            sharedPrefs.getBoolean(SHOW_ONLY_LURED, true),
            sharedPrefs.getBoolean(SHOW_GYMS, true),
            sharedPrefs.getBoolean(SHOW_POKESTOPS, true)
        );
    }

    public static void saveSettings(Context context, Settings settings) {
        context.getSharedPreferences(context.getString(R.string.shared_key), Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_BOUNDING_BOX, settings.isBoundingBoxEnabled())
            .putBoolean(KEY_LOCK_GPS, settings.isLockGpsEnabled())
            .putInt(SERVER_REFRESH_RATE, settings.getServerRefresh())
            .putInt(MAP_REFRESH_RATE, settings.getMapRefresh())
            .putInt(POKEMON_ICON_SCALE, settings.getScale())
            .putString(LAST_USERNAME, settings.getLastUsername())
            .putBoolean(SHOW_ONLY_LURED, settings.isShowOnlyLured())
            .putBoolean(SHOW_GYMS, settings.isGymsEnabled())
            .putBoolean(SHOW_POKESTOPS, settings.isPokestopsEnabled())
            .apply();
    }

    public static void forceRefresh() {
        if (EventBus.getDefault().hasSubscriberForEvent(ForceRefreshEvent.class)) {
            EventBus.getDefault().post(new ForceRefreshEvent());
        }
    }

    public static void restartRefresh() {
        if (EventBus.getDefault().hasSubscriberForEvent(ForceRefreshEvent.class)) {
            EventBus.getDefault().post(new RestartRefreshEvent());
        }
    }
}
