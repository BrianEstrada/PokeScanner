package com.pokescanner;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.widget.SwitchCompat;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.pokescanner.events.RestartRefreshEvent;
import com.pokescanner.helper.ExpirationFilter;
import com.pokescanner.helper.GymFilter;

import org.florescu.android.rangeseekbar.RangeSeekBar;
import org.greenrobot.eventbus.EventBus;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class ExpirationFilters
{
    private static final String MIN_SEC = "pokemonExpirationMinSec";

    public static void showExpirationFiltersDialog(final Context context)
    {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_expirtation_filters);

        final TextView tvExpirationMin = (TextView) dialog.findViewById(R.id.tvExpirationMin);

        //Initialize view references
        SeekBar expirationSeekBar = (SeekBar) dialog.findViewById(R.id.pokemonExpirationSeekbar);
        expirationSeekBar.setMax(900);

        //Load saved filters
        ExpirationFilter currentExpirationFilter = getFilter(context);
        expirationSeekBar.setProgress(currentExpirationFilter.getPokemonExpirationMinSec());
        tvExpirationMin.setText(formatTime(currentExpirationFilter.getPokemonExpirationMinSec()));

        expirationSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int value, boolean b) {
                ExpirationFilter.getFilter(context).toBuilder().pokemonExpirationMinSec(value).build().saveFilter(context);
                tvExpirationMin.setText(formatTime(value));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener()
        {
            @Override
            public void onCancel(DialogInterface dialogInterface)
            {
                EventBus.getDefault().post(new RestartRefreshEvent());
            }
        });

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener()
        {
            @Override
            public void onDismiss(DialogInterface dialogInterface)
            {
                EventBus.getDefault().post(new RestartRefreshEvent());
            }
        });

        dialog.show();
    }

    public static ExpirationFilter getFilter(Context context)
    {
        SharedPreferences sharedPrefs = context.getSharedPreferences(context.getString(R.string.shared_key), Context.MODE_PRIVATE);
        return new ExpirationFilter(
                sharedPrefs.getInt(MIN_SEC, 900)
        );
    }

    public static void saveFilter(Context context, ExpirationFilter currentExpirationFilter)
    {
        context.getSharedPreferences(context.getString(R.string.shared_key), Context.MODE_PRIVATE)
                .edit()
                .putInt(MIN_SEC, currentExpirationFilter.getPokemonExpirationMinSec())
                .apply();
    }

    private static String formatTime(int sec){
        LocalTime localTime = new LocalTime(0, 0); // midnight
        localTime = localTime.plusSeconds(sec);
        return DateTimeFormat.forPattern("mm:ss").print(localTime);
    }
}
