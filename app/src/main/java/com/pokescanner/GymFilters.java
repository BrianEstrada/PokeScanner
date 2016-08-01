package com.pokescanner;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.SwitchCompat;
import android.view.Window;
import android.widget.CompoundButton;

import com.pokescanner.helper.GymFilter;

import org.florescu.android.rangeseekbar.RangeSeekBar;

public class GymFilters
{
    private static final String SHOW_NEUTRAL_GYMS = "showNeutralGyms";
    private static final String SHOW_YELLOW_GYMS = "showYellowGyms";
    private static final String SHOW_BLUE_GYMS = "showBlueGyms";
    private static final String SHOW_RED_GYMS = "showRedGyms";
    private static final String GUARD_MIN_CP = "guardPokemonMinCp";
    private static final String GUARD_MAX_CP = "guardPokemonMaxCp";

    public static void showGymFiltersDialog(final Context context)
    {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_gym_filters);

        //Initialize view references
        SwitchCompat neutralGymsSwitch = (SwitchCompat) dialog.findViewById(R.id.showNeutralGyms);
        SwitchCompat yellowGymsSwitch = (SwitchCompat) dialog.findViewById(R.id.showYellowTeam);
        SwitchCompat blueGymsSwitch = (SwitchCompat) dialog.findViewById(R.id.showBlueTeam);
        SwitchCompat redGymsSwitch = (SwitchCompat) dialog.findViewById(R.id.showRedTeam);
        RangeSeekBar cpSeekBar = (RangeSeekBar) dialog.findViewById(R.id.guardPokemonCpSeekbar);
        cpSeekBar.setRangeValues(1, 2000);

        //Load saved filters
        GymFilter currentGymFilter = getGymFilter(context);
        neutralGymsSwitch.setChecked(currentGymFilter.isNeutralGymsEnabled());
        yellowGymsSwitch.setChecked(currentGymFilter.isYellowGymsEnabled());
        blueGymsSwitch.setChecked(currentGymFilter.isBlueGymsEnabled());
        redGymsSwitch.setChecked(currentGymFilter.isRedGymsEnabled());
        cpSeekBar.setSelectedMinValue(currentGymFilter.getGuardPokemonMinCp());
        cpSeekBar.setSelectedMaxValue(currentGymFilter.getGuardPokemonMaxCp());

        //Attach listeners to save filters if changed
        neutralGymsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b)
            {
                GymFilter.getGymFilter(context).toBuilder().neutralGymsEnabled(b).build().saveGymFilter(context);
            }
        });

        yellowGymsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b)
            {
                GymFilter.getGymFilter(context).toBuilder().yellowGymsEnabled(b).build().saveGymFilter(context);
            }
        });

        blueGymsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b)
            {
                GymFilter.getGymFilter(context).toBuilder().blueGymsEnabled(b).build().saveGymFilter(context);
            }
        });

        redGymsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b)
            {
                GymFilter.getGymFilter(context).toBuilder().redGymsEnabled(b).build().saveGymFilter(context);
            }
        });

        cpSeekBar.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener()
        {
            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar bar, Object minValue, Object maxValue)
            {
                int selectedMinValue = (Integer) minValue;
                int selectedMaxValue = (Integer) maxValue;
                GymFilter.getGymFilter(context).toBuilder().guardPokemonMinCp(selectedMinValue).guardPokemonMaxCp(selectedMaxValue).build().saveGymFilter(context);
            }
        });

        dialog.show();
    }

    public static GymFilter getGymFilter(Context context)
    {
        SharedPreferences sharedPrefs = context.getSharedPreferences(context.getString(R.string.shared_key), Context.MODE_PRIVATE);
        return new GymFilter(
                sharedPrefs.getBoolean(SHOW_NEUTRAL_GYMS, true),
                sharedPrefs.getBoolean(SHOW_YELLOW_GYMS, true),
                sharedPrefs.getBoolean(SHOW_BLUE_GYMS, true),
                sharedPrefs.getBoolean(SHOW_RED_GYMS, true),
                sharedPrefs.getInt(GUARD_MIN_CP, 1),
                sharedPrefs.getInt(GUARD_MAX_CP, 999)
        );
    }

    public static void saveGymFilter(Context context, GymFilter currentGymFilter)
    {
        context.getSharedPreferences(context.getString(R.string.shared_key), Context.MODE_PRIVATE)
                .edit()
                .putBoolean(SHOW_NEUTRAL_GYMS, currentGymFilter.isNeutralGymsEnabled())
                .putBoolean(SHOW_YELLOW_GYMS, currentGymFilter.isYellowGymsEnabled())
                .putBoolean(SHOW_BLUE_GYMS, currentGymFilter.isBlueGymsEnabled())
                .putBoolean(SHOW_RED_GYMS, currentGymFilter.isRedGymsEnabled())
                .putInt(GUARD_MIN_CP, currentGymFilter.getGuardPokemonMinCp())
                .putInt(GUARD_MAX_CP, currentGymFilter.getGuardPokemonMaxCp())
                .apply();
    }
}
