package com.pokescanner;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.pokescanner.events.ForceRefreshEvent;
import com.pokescanner.events.RestartRefreshEvent;
import com.pokescanner.helper.Settings;
import com.pokescanner.objects.Gym;
import com.pokescanner.objects.PokeStop;
import com.pokescanner.objects.Pokemons;

import org.greenrobot.eventbus.EventBus;

import io.realm.Realm;


public class SettingsController {

    private static final String KEY_BOUNDING_BOX = "boundingBoxEnabled";
    private static final String SERVER_REFRESH_RATE = "serverRefreshRate";
    private static final String MAP_REFRESH_RATE = "mapRefreshRate";
    private static final String POKEMON_ICON_SCALE = "pokemonIconScale";
    private static final String LAST_USERNAME = "lastUsername";
    public static final String SHOW_ONLY_LURED = "showOnlyLured";
    public static final String SHOW_GYMS = "showGyms";
    public static final String SHOW_POKESTOPS = "showPokestops";

    public static void showSettingDialog(final Context context) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_settings);

        Settings settings = getSettings(context);
        int mapRefresh = settings.getMapRefresh();
        int serverRefresh = settings.getServerRefresh();
        int iconScale = settings.getScale();

        SwitchCompat showRange = (SwitchCompat) dialog.findViewById(R.id.showRange);

        Button btnClearPokemon = (Button) dialog.findViewById(R.id.btnClearPokemon);
        Button btnClearGyms = (Button) dialog.findViewById(R.id.btnClearGyms);

        SeekBar seekMap = (SeekBar) dialog.findViewById(R.id.seekMap);
        SeekBar seekServer = (SeekBar) dialog.findViewById(R.id.seekServer);
        SeekBar seekScale = (SeekBar) dialog.findViewById(R.id.seekScale);

        final TextView tvMapNumber = (TextView) dialog.findViewById(R.id.tvMapNumber);
        final TextView tvServerNumber = (TextView) dialog.findViewById(R.id.tvServerNumber);
        final TextView tvScaleNumber = (TextView) dialog.findViewById(R.id.tvScaleSpace);

        SwitchCompat onlyLured = (SwitchCompat) dialog.findViewById(R.id.onlyLured);
        SwitchCompat showGyms = (SwitchCompat) dialog.findViewById(R.id.showGyms);
        SwitchCompat showPokeStops = (SwitchCompat) dialog.findViewById(R.id.showPokestops);

        showGyms.setChecked(settings.isGymsEnabled());
        showGyms.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b)
            {
                Settings.get(context).toBuilder().gymsEnabled(b).build().save(context);
                forceRefresh();
            }
        });

        showPokeStops.setChecked(settings.isPokestopsEnabled());
        showPokeStops.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b)
            {
                Settings.get(context).toBuilder().pokestopsEnabled(b).build().save(context);
                forceRefresh();
            }
        });

        showRange.setChecked(settings.isBoundingBoxEnabled());
        showRange.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, final boolean b) {
                Settings.get(context).toBuilder().boundingBoxEnabled(b).build().save(context);
                forceRefresh();
            }
        });

        onlyLured.setChecked(settings.isShowOnlyLured());
        onlyLured.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Settings.get(context).toBuilder().showOnlyLured(b).build().save(context);
                forceRefresh();
            }
        });

        tvMapNumber.setText(String.valueOf(mapRefresh)+"s");
        tvServerNumber.setText(String.valueOf(serverRefresh)+"s");
        tvScaleNumber.setText(String.valueOf(iconScale));

        seekMap.setProgress(mapRefresh-1);
        seekServer.setProgress(serverRefresh-1);
        seekScale.setProgress(iconScale-1);

        seekMap.setMax(4);
        seekServer.setMax(4);
        seekScale.setMax(2);

        seekMap.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                tvMapNumber.setText(String.valueOf(i+1)+"s");
                Settings.get(context).toBuilder().mapRefresh(i + 1).build().save(context);

                if (EventBus.getDefault().hasSubscriberForEvent(RestartRefreshEvent.class)) {
                    EventBus.getDefault().post(new RestartRefreshEvent());
                }
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
                Settings.get(context).toBuilder().serverRefresh(i + 1).build().save(context);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekScale.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                tvScaleNumber.setText(String.valueOf(i+1));
                Settings.get(context).toBuilder().scale(i + 1).build().save(context);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        dialog.show();

        btnClearGyms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Realm realm = Realm.getDefaultInstance();
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        Toast.makeText(context, context.getString(R.string.gyms_cleared), Toast.LENGTH_SHORT).show();
                        realm.where(Gym.class).findAll().deleteAllFromRealm();
                        realm.where(PokeStop.class).findAll().deleteAllFromRealm();
                    }
                });
            }
        });

        btnClearPokemon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Realm realm = Realm.getDefaultInstance();
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        Toast.makeText(context, context.getString(R.string.pokemon_cleared), Toast.LENGTH_SHORT).show();
                        realm.where(Pokemons.class).findAll().deleteAllFromRealm();
                    }
                });
            }
        });

    }

    public static Settings getSettings(Context context) {
        SharedPreferences sharedPrefs = context.getSharedPreferences(
            context.getString(R.string.shared_key),
            Context.MODE_PRIVATE
        );
        return new Settings(
            sharedPrefs.getBoolean(KEY_BOUNDING_BOX, false),
            sharedPrefs.getInt(SERVER_REFRESH_RATE, 3),
            sharedPrefs.getInt(POKEMON_ICON_SCALE, 2),
            sharedPrefs.getInt(MAP_REFRESH_RATE, 3),
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
}
