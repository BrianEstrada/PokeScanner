package com.pokescanner;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.SwitchCompat;
import android.view.Window;
import android.widget.CompoundButton;

import com.pokescanner.helper.Settings;


public class SettingsController {

    public static final String KEY_BOUNDING_BOX = "boundingBoxEnabled";

    public static void showSettingDialog(final Context context) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_settings);

        SwitchCompat showRange = (SwitchCompat) dialog.findViewById(R.id.showRange);
        showRange.setChecked(getSettings(context).isBoundingBoxEnabled());
        showRange.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, final boolean b) {
                saveSettings(context, new Settings(b));
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
            sharedPrefs.getBoolean(KEY_BOUNDING_BOX, false)
        );
    }

    public static void saveSettings(Context context, Settings settings) {
        context.getSharedPreferences(context.getString(R.string.shared_key), Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_BOUNDING_BOX, settings.isBoundingBoxEnabled())
            .commit();
    }
}
