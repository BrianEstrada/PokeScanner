package com.pokescanner.settings;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;

import com.pokescanner.R;
import com.pokescanner.utils.SettingsUtil;

import org.florescu.android.rangeseekbar.RangeSeekBar;

public class GymFilters
{
    public static void showGymCpFilterDialog(final Context context)
    {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_gym_filters);

        //Initialize view references
        RangeSeekBar cpSeekBar = (RangeSeekBar) dialog.findViewById(R.id.guardPokemonCpSeekbar);
        cpSeekBar.setRangeValues(1, 2000);

        //Load saved filters
        Settings currentSettings = SettingsUtil.getSettings(context);
        cpSeekBar.setSelectedMinValue(currentSettings.getGuardPokemonMinCp());
        cpSeekBar.setSelectedMaxValue(currentSettings.getGuardPokemonMaxCp());

        cpSeekBar.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener()
        {
            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar bar, Object minValue, Object maxValue)
            {
                int selectedMinValue = (Integer) minValue;
                int selectedMaxValue = (Integer) maxValue;
                SettingsUtil.getSettings(context).toBuilder().guardPokemonMinCp(selectedMinValue).guardPokemonMaxCp(selectedMaxValue).build().save(context);
            }
        });

        dialog.show();
    }
}
