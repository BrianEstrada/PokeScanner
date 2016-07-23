package com.pokescanner;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.SwitchCompat;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.pokescanner.helper.Settings;


public class SettingsController {

    public static final String KEY_BOUNDING_BOX = "boundingBoxEnabled";
    public static final String SERVER_REFRESH_RATE = "serverRefreshRate";
    public static final String MAP_REFRESH_RATE = "mapRefreshRate";

    static int mapRefresh = 3;
    static int serverRefresh = 3;
    static boolean boundingBox = false;

    //
    // THIS IS NOT FINISHED!!!
    //

    public static void showSettingDialog(final Context context) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_settings);

        Settings settings = getSettings(context);
        mapRefresh = settings.getMapRefresh();
        serverRefresh = settings.getServerRefresh();
        boundingBox = settings.isBoundingBoxEnabled();

        SwitchCompat showRange = (SwitchCompat) dialog.findViewById(R.id.showRange);

        SeekBar seekMap = (SeekBar) dialog.findViewById(R.id.seekMap);
        SeekBar seekServer = (SeekBar) dialog.findViewById(R.id.seekServer);

        final TextView tvMapNumber = (TextView) dialog.findViewById(R.id.tvMapNumber);
        final TextView tvServerNumber = (TextView) dialog.findViewById(R.id.tvServerNumber);

        showRange.setChecked(getSettings(context).isBoundingBoxEnabled());
        showRange.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, final boolean b) {
                boundingBox = b;
                saveSettings(context, new Settings(boundingBox,serverRefresh,mapRefresh));
            }
        });

        tvMapNumber.setText(String.valueOf(mapRefresh)+"s");
        tvServerNumber.setText(String.valueOf(serverRefresh)+"s");

        seekMap.setProgress(mapRefresh-1);
        seekServer.setProgress(serverRefresh-1);

        seekMap.setMax(4);
        seekServer.setMax(4);

        seekMap.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                tvMapNumber.setText(String.valueOf(i+1)+"s");
                mapRefresh = i + 1;
                saveSettings(context, new Settings(boundingBox,serverRefresh,mapRefresh));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekServer.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                tvServerNumber.setText(String.valueOf(i+1)+"s");
                serverRefresh = i + 1;
                saveSettings(context, new Settings(boundingBox,serverRefresh,mapRefresh));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

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
            sharedPrefs.getBoolean(KEY_BOUNDING_BOX, false),
                sharedPrefs.getInt(SERVER_REFRESH_RATE,3),
                sharedPrefs.getInt(MAP_REFRESH_RATE,3)
        );
    }

    public static void saveSettings(Context context, Settings settings) {
        context.getSharedPreferences(context.getString(R.string.shared_key), Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_BOUNDING_BOX, settings.isBoundingBoxEnabled())
                .putInt(SERVER_REFRESH_RATE,settings.getServerRefresh())
                .putInt(MAP_REFRESH_RATE,settings.getMapRefresh())
            .commit();
    }
}
