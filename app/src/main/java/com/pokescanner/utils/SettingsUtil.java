package com.pokescanner.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatDialog;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.pokescanner.R;
import com.pokescanner.helper.Settings;


public class SettingsUtil {

    public static final String ENABLE_UPDATES = "updatesEnabled";
    public static final String KEY_BOUNDING_BOX = "boundingBoxEnabled";
    public static final String SHOW_ONLY_LURED = "showOnlyLured";
    public static final String SHOW_GYMS = "showGyms";
    public static final String SHOW_POKESTOPS = "showPokestops";
    public static final String SHOW_LURED_POKEMON = "showLuredPokemon";
    public static final String KEY_LOCK_GPS = "lockGpsEnabled";
    public static final String KEY_OLD_MARKER = "useOldMapMarker";
    public static final String DRIVING_MODE = "drivingModeEnabled";

    public static final String SERVER_REFRESH_RATE = "serverRefreshRate";
    public static final String MAP_REFRESH_RATE = "mapRefreshRate";
    public static final String POKEMON_ICON_SCALE = "pokemonIconScale";
    public static final String SCAN_VALUE = "scanValue";

    public static final String LAST_USERNAME = "lastUsername";

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
        SharedPreferences sharedPrefs = context.getSharedPreferences(
            context.getString(R.string.shared_key),
            Context.MODE_PRIVATE
        );
        return new Settings(
            sharedPrefs.getBoolean(ENABLE_UPDATES,true),
            sharedPrefs.getBoolean(KEY_BOUNDING_BOX, false),
            sharedPrefs.getBoolean(KEY_LOCK_GPS, false),
            sharedPrefs.getBoolean(DRIVING_MODE, false),
            sharedPrefs.getInt(SCAN_VALUE, 4),
            sharedPrefs.getInt(SERVER_REFRESH_RATE, 1),
            sharedPrefs.getInt(POKEMON_ICON_SCALE, 2),
            sharedPrefs.getInt(MAP_REFRESH_RATE, 2),
            sharedPrefs.getString(LAST_USERNAME, ""),
            sharedPrefs.getBoolean(SHOW_ONLY_LURED, false),
            sharedPrefs.getBoolean(SHOW_GYMS, true),
            sharedPrefs.getBoolean(SHOW_POKESTOPS, true),
                sharedPrefs.getBoolean(SHOW_LURED_POKEMON, true),
            sharedPrefs.getBoolean(KEY_OLD_MARKER, false)
        );
    }

    public static void saveSettings(Context context, Settings settings) {
        context.getSharedPreferences(context.getString(R.string.shared_key), Context.MODE_PRIVATE)
            .edit()
            .putBoolean(ENABLE_UPDATES,settings.isUpdatesEnabled())
            .putBoolean(KEY_BOUNDING_BOX, settings.isBoundingBoxEnabled())
            .putBoolean(KEY_LOCK_GPS, settings.isLockGpsEnabled())
            .putBoolean(DRIVING_MODE, settings.isDrivingModeEnabled())
            .putInt(SCAN_VALUE,settings.getScanValue())
            .putInt(SERVER_REFRESH_RATE, settings.getServerRefresh())
            .putInt(MAP_REFRESH_RATE, settings.getMapRefresh())
            .putInt(POKEMON_ICON_SCALE, settings.getScale())
            .putString(LAST_USERNAME, settings.getLastUsername())
            .putBoolean(SHOW_ONLY_LURED, settings.isShowOnlyLured())
            .putBoolean(SHOW_GYMS, settings.isGymsEnabled())
            .putBoolean(SHOW_POKESTOPS, settings.isPokestopsEnabled())
                .putBoolean(SHOW_LURED_POKEMON, settings.isShowLuredPokemon())
            .putBoolean(KEY_OLD_MARKER, settings.isUseOldMapMarker())
            .apply();
    }
}
