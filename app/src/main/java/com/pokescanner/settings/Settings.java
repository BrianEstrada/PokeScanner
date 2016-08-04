package com.pokescanner.settings;

import android.content.Context;

import com.pokescanner.utils.SettingsUtil;

import org.json.JSONException;
import org.json.JSONObject;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Settings extends RealmObject{

    @PrimaryKey
    int key;

    boolean updatesEnabled;
    boolean boundingBoxEnabled;
    boolean drivingModeEnabled;
    boolean forceEnglishNames;
    boolean enableLowMemory;
    int scanValue;
    int serverRefresh;
    int scale;
    int mapRefresh;
    String lastUsername;
    boolean useOldMapMarker;
    boolean shuffleIcons;
    boolean showLuredPokemon;
    boolean neutralGymsEnabled;
    boolean yellowGymsEnabled;
    boolean blueGymsEnabled;
    boolean redGymsEnabled;
    int guardPokemonMinCp;
    int guardPokemonMaxCp;
    boolean luredPokestopsEnabled;
    boolean normalPokestopsEnabled;

    //Called if no settings are found
    public Settings() {
        super();
    }

    //Used when the app is loaded for the first time
    public Settings(String mode) {
        super();
        if(mode.equals("new")) {
            key = 1;
            updatesEnabled = true;
            boundingBoxEnabled = false;
            drivingModeEnabled = false;
            forceEnglishNames = false;
            enableLowMemory = true;
            scanValue = 4;
            serverRefresh = 3;
            scale = 2;
            mapRefresh = 2;
            lastUsername = "";
            useOldMapMarker = false;
            shuffleIcons = false;
            showLuredPokemon = true;
            neutralGymsEnabled = true;
            yellowGymsEnabled = true;
            blueGymsEnabled = true;
            redGymsEnabled = true;
            guardPokemonMinCp = 1;
            guardPokemonMaxCp = 1999;
            luredPokestopsEnabled = true;
            normalPokestopsEnabled = true;
        }
    }

    //This constructor is used in onder to get a non-updating realm object
    public Settings(Settings settings) {
        this.key = settings.key;
        this.updatesEnabled = settings.updatesEnabled;
        this.boundingBoxEnabled = settings.boundingBoxEnabled;
        this.drivingModeEnabled = settings.forceEnglishNames;
        this.enableLowMemory = settings.enableLowMemory;
        this.scanValue = settings.scanValue;
        this.serverRefresh = settings.serverRefresh;
        this.scale = settings.scale;
        this.mapRefresh = settings.mapRefresh;
        this.lastUsername = settings.lastUsername;
        this.useOldMapMarker = settings.useOldMapMarker;
        this.shuffleIcons = settings.shuffleIcons;
        this.showLuredPokemon = settings.showLuredPokemon;
        this.neutralGymsEnabled = settings.neutralGymsEnabled;
        this.yellowGymsEnabled = settings.yellowGymsEnabled;
        this.blueGymsEnabled = settings.blueGymsEnabled;
        this.redGymsEnabled = settings.redGymsEnabled;
        this.guardPokemonMinCp = settings.guardPokemonMinCp;
        this.guardPokemonMaxCp = settings.guardPokemonMaxCp;
        this.luredPokestopsEnabled = settings.luredPokestopsEnabled;
        this.normalPokestopsEnabled = settings.normalPokestopsEnabled;
    }

    public void save(Context context) {
        SettingsUtil.saveSettings(context, this);
    }

    public static Settings get(Context context) {
        return SettingsUtil.getSettings(context);
    }

    public JSONObject toJSONObject() throws JSONException{
        JSONObject result = new JSONObject();
        result.put(SettingsUtil.ENABLE_UPDATES, updatesEnabled);
        result.put(SettingsUtil.KEY_BOUNDING_BOX, boundingBoxEnabled);
        result.put(SettingsUtil.DRIVING_MODE, drivingModeEnabled);
        result.put(SettingsUtil.ENABLE_LOW_MEMORY, enableLowMemory);
        result.put(SettingsUtil.SCAN_VALUE, scanValue);
        result.put(SettingsUtil.SERVER_REFRESH_RATE, serverRefresh);
        result.put(SettingsUtil.POKEMON_ICON_SCALE, scale);
        result.put(SettingsUtil.MAP_REFRESH_RATE, mapRefresh);
        result.put(SettingsUtil.LAST_USERNAME, lastUsername);
        result.put(SettingsUtil.KEY_OLD_MARKER, useOldMapMarker);
        result.put(SettingsUtil.SHUFFLE_ICONS, shuffleIcons);
        result.put(SettingsUtil.SHOW_LURED_POKEMON, showLuredPokemon);
        result.put(SettingsUtil.SHOW_NEUTRAL_GYMS, neutralGymsEnabled);
        result.put(SettingsUtil.SHOW_YELLOW_GYMS, yellowGymsEnabled);
        result.put(SettingsUtil.SHOW_BLUE_GYMS, blueGymsEnabled);
        result.put(SettingsUtil.SHOW_RED_GYMS, redGymsEnabled);
        result.put(SettingsUtil.GUARD_MIN_CP, guardPokemonMinCp);
        result.put(SettingsUtil.GUARD_MAX_CP, guardPokemonMaxCp);
        result.put(SettingsUtil.SHOW_LURED_POKESTOPS, luredPokestopsEnabled);
        result.put(SettingsUtil.SHOW_NORMAL_POKESTOPS, normalPokestopsEnabled);
        return result;
    }
}
