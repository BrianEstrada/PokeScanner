package com.pokescanner.utils;

import android.content.Context;
import android.support.v7.app.AppCompatDialog;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.pokescanner.R;
import com.pokescanner.settings.Settings;

import io.realm.Realm;


public class SettingsUtil {

    public static final String ENABLE_UPDATES = "updatesEnabled";
    public static final String KEY_BOUNDING_BOX = "boundingBoxEnabled";
    public static final String ENABLE_LOW_MEMORY = "enableLowMemory";
    public static final String SHOW_LURED_POKEMON = "showLuredPokemon";
    public static final String KEY_LOCK_GPS = "lockGpsEnabled";
    public static final String KEY_OLD_MARKER = "useOldMapMarker";
    public static final String DRIVING_MODE = "drivingModeEnabled";
    public static final String FORCE_ENGLISH_NAMES = "forceEnglishNames";
    public static final String SERVER_REFRESH_RATE = "serverRefreshRate";
    public static final String MAP_REFRESH_RATE = "mapRefreshRate";
    public static final String POKEMON_ICON_SCALE = "pokemonIconScale";
    public static final String SCAN_VALUE = "scanValue";
    public static final String LAST_USERNAME = "lastUsername";
    public static final String SHOW_NEUTRAL_GYMS = "showNeutralGyms";
    public static final String SHOW_YELLOW_GYMS = "showYellowGyms";
    public static final String SHOW_BLUE_GYMS = "showBlueGyms";
    public static final String SHOW_RED_GYMS = "showRedGyms";
    public static final String GUARD_MIN_CP = "guardPokemonMinCp";
    public static final String GUARD_MAX_CP = "guardPokemonMaxCp";
    public static final String SHUFFLE_ICONS = "shuffleIcons";
    public static final String SHOW_LURED_POKESTOPS = "showLuredPokestops";
    public static final String SHOW_NORMAL_POKESTOPS = "showNormalPokestops";

    public static void searchRadiusDialog(final Context context) {
        int scanValue = Settings.get(context).getScanValue();

        final AppCompatDialog dialog = new AppCompatDialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_search_radius);

        final SeekBar seekBar = (SeekBar) dialog.findViewById(R.id.seekBar);
        Button btnSave = (Button) dialog.findViewById(R.id.btnAccept);
        Button btnCancel = (Button) dialog.findViewById(R.id.btnCancel);
        final TextView tvNumber = (TextView) dialog.findViewById(R.id.tvNumber);
        final TextView tvEstimate = (TextView) dialog.findViewById(R.id.tvEstimate);
        tvNumber.setText(String.valueOf(scanValue));
        tvEstimate.setText(context.getString(R.string.timeEstimate) + " " + UiUtils.getSearchTime(scanValue,context));
        seekBar.setProgress(scanValue);
        seekBar.setMax(12);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                tvNumber.setText(String.valueOf(i));
                tvEstimate.setText(context.getString(R.string.timeEstimate) + " " + UiUtils.getSearchTime(i,context));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int scanOut = 4;
                int saveValue = seekBar.getProgress();
                if (saveValue == 0) {
                    scanOut = 1;
                } else {
                    scanOut = saveValue;
                }
                context.getSharedPreferences(context.getString(R.string.shared_key), Context.MODE_PRIVATE)
                        .edit()
                        .putInt(SCAN_VALUE,scanOut)
                        .apply();
                dialog.dismiss();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public static Settings getSettings(Context context) {
        Realm realm = Realm.getDefaultInstance();
        Settings currentSettings = realm.where(Settings.class).findFirst();
        realm.close();
        if(currentSettings == null)
            currentSettings = new Settings("new");
        return new Settings(currentSettings);
        /*SharedPreferences sharedPrefs = context.getSharedPreferences(
            context.getString(R.string.shared_key),
            Context.MODE_PRIVATE
        );
        return new Settings(
            sharedPrefs.getBoolean(ENABLE_UPDATES,true),
            sharedPrefs.getBoolean(KEY_BOUNDING_BOX, false),
            sharedPrefs.getBoolean(DRIVING_MODE, false),
            sharedPrefs.getBoolean(FORCE_ENGLISH_NAMES,false),
            sharedPrefs.getBoolean(ENABLE_LOW_MEMORY,true),
            sharedPrefs.getInt(SCAN_VALUE, 4),
            sharedPrefs.getInt(SERVER_REFRESH_RATE, 3),
            sharedPrefs.getInt(POKEMON_ICON_SCALE, 2),
            sharedPrefs.getInt(MAP_REFRESH_RATE, 2),
            sharedPrefs.getString(LAST_USERNAME, ""),
            sharedPrefs.getBoolean(KEY_OLD_MARKER, false),
            sharedPrefs.getBoolean(SHUFFLE_ICONS, true),
            sharedPrefs.getBoolean(SHOW_LURED_POKEMON, true),
            sharedPrefs.getBoolean(SHOW_NEUTRAL_GYMS, true),
            sharedPrefs.getBoolean(SHOW_YELLOW_GYMS, true),
            sharedPrefs.getBoolean(SHOW_BLUE_GYMS, true),
            sharedPrefs.getBoolean(SHOW_RED_GYMS, true),
            sharedPrefs.getInt(GUARD_MIN_CP, 0),
            sharedPrefs.getInt(GUARD_MAX_CP, 1999),
            sharedPrefs.getBoolean(SHOW_LURED_POKESTOPS, true),
            sharedPrefs.getBoolean(SHOW_NORMAL_POKESTOPS, true)
        );*/
    }

    public static void saveSettings(Context context, final Settings settings) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealmOrUpdate(settings);
            }
        });
        realm.close();
        /*
        context.getSharedPreferences(context.getString(R.string.shared_key), Context.MODE_PRIVATE)
            .edit()
            .putBoolean(ENABLE_UPDATES,settings.isUpdatesEnabled())
            .putBoolean(KEY_BOUNDING_BOX, settings.isBoundingBoxEnabled())
            .putBoolean(DRIVING_MODE, settings.isDrivingModeEnabled())
            .putBoolean(FORCE_ENGLISH_NAMES,settings.isForceEnglishNames())
            .putBoolean(ENABLE_LOW_MEMORY,settings.isEnableLowMemory())
            .putInt(SCAN_VALUE,settings.getScanValue())
            .putInt(SERVER_REFRESH_RATE, settings.getServerRefresh())
            .putInt(POKEMON_ICON_SCALE, settings.getScale())
            .putInt(MAP_REFRESH_RATE, settings.getMapRefresh())
            .putString(LAST_USERNAME, settings.getLastUsername())
            .putBoolean(KEY_OLD_MARKER, settings.isUseOldMapMarker())
            .putBoolean(SHUFFLE_ICONS, settings.isShuffleIcons())
            .putBoolean(SHOW_LURED_POKEMON, settings.isShowLuredPokemon())
            .putBoolean(SHOW_NEUTRAL_GYMS, settings.isNeutralGymsEnabled())
            .putBoolean(SHOW_YELLOW_GYMS, settings.isYellowGymsEnabled())
            .putBoolean(SHOW_BLUE_GYMS, settings.isBlueGymsEnabled())
            .putBoolean(SHOW_RED_GYMS, settings.isRedGymsEnabled())
            .putInt(GUARD_MIN_CP, settings.getGuardPokemonMinCp())
            .putInt(GUARD_MAX_CP, settings.getGuardPokemonMaxCp())
            .putBoolean(SHOW_NORMAL_POKESTOPS, settings.isLuredPokestopsEnabled())
            .putBoolean(SHOW_NORMAL_POKESTOPS, settings.isNormalPokestopsEnabled())
            .apply(); */
    }
}
