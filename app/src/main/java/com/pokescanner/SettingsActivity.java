package com.pokescanner;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatDialog;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.pokescanner.helper.Settings;
import com.pokescanner.objects.Gym;
import com.pokescanner.objects.PokeStop;
import com.pokescanner.objects.Pokemons;
import com.pokescanner.objects.User;
import com.pokescanner.utils.SettingsUtil;
import com.pokescanner.utils.UiUtils;

import io.realm.Realm;

public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    SharedPreferences sharedPreferences;
    Preference scan_dialog,gym_filter,pokemon_filter;
    Preference clear_pokemon,clear_gyms,clear_pokestops;
    Preference logout;
    Realm realm;
    int scanValue;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.registerOnSharedPreferenceChangeListener(this);
        Settings settings = SettingsUtil.getSettings(this);

        preferences.edit()
                .putBoolean(SettingsUtil.KEY_BOUNDING_BOX,settings.isBoundingBoxEnabled())
                .putBoolean(SettingsUtil.SCAN_PREVIEW,settings.isBoundingPreviewEnabled())
                .putBoolean(SettingsUtil.SHOW_ONLY_LURED,settings.isShowOnlyLured())
                .putBoolean(SettingsUtil.SHOW_GYMS,settings.isGymsEnabled())
                .putBoolean(SettingsUtil.SHOW_POKESTOPS,settings.isPokestopsEnabled())
                .putBoolean(SettingsUtil.KEY_LOCK_GPS,settings.isLockGpsEnabled())
                .putString(SettingsUtil.SERVER_REFRESH_RATE,String.valueOf(settings.getServerRefresh()))
                .putString(SettingsUtil.MAP_REFRESH_RATE,String.valueOf(settings.getMapRefresh()))
                .putString(SettingsUtil.POKEMON_ICON_SCALE,String.valueOf(settings.getScale()))
                .putString(SettingsUtil.LAST_USERNAME,settings.getLastUsername())
                .commit();

        addPreferencesFromResource(R.xml.settings);

        realm = Realm.getDefaultInstance();

        scan_dialog = (Preference) getPreferenceManager().findPreference("scan_dialog");
        scan_dialog.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                searchRadiusDialog();
                return true;
            }
        });

        gym_filter = (Preference) getPreferenceManager().findPreference("gym_filter");
        gym_filter.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                GymFilters.showGymFiltersDialog(SettingsActivity.this);
                return true;
            }
        });

        pokemon_filter = (Preference) getPreferenceManager().findPreference("pokemon_filter");
        pokemon_filter.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Intent filterIntent = new Intent(SettingsActivity.this,FilterActivity.class);
                startActivity(filterIntent);
                return true;
            }
        });

        clear_gyms = (Preference) getPreferenceManager().findPreference("clear_gyms");
        clear_gyms.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        if(realm.where(Gym.class).findAll().deleteAllFromRealm())
                        {
                            Toast.makeText(SettingsActivity.this, getString(R.string.gyms_cleared), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                return true;
            }
        });

        clear_pokemon = (Preference) getPreferenceManager().findPreference("clear_pokemon");
        clear_pokemon.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        if(realm.where(Pokemons.class).findAll().deleteAllFromRealm())
                        {
                            Toast.makeText(SettingsActivity.this, getString(R.string.pokemon_cleared), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                return true;
            }
        });

        clear_pokestops = (Preference) getPreferenceManager().findPreference("clear_pokestops");
        clear_pokestops.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        if(realm.where(PokeStop.class).findAll().deleteAllFromRealm())
                        {
                            Toast.makeText(SettingsActivity.this, getString(R.string.pokestops_cleared), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                return true;
            }
        });

        logout = (Preference) getPreferenceManager().findPreference("logout");
        logout.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                logOut();
                return true;
            }
        });
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        System.out.println(sharedPreferences.getAll().toString());
        SettingsUtil.saveSettings(this,new Settings(
                sharedPreferences.getBoolean(SettingsUtil.KEY_BOUNDING_BOX, false),
                sharedPreferences.getBoolean(SettingsUtil.SCAN_PREVIEW,false),
                sharedPreferences.getBoolean(SettingsUtil.KEY_LOCK_GPS, false),
                Integer.valueOf(sharedPreferences.getString(SettingsUtil.SCAN_VALUE,"4")),
                Integer.valueOf(sharedPreferences.getString(SettingsUtil.SERVER_REFRESH_RATE, "1")),
                Integer.valueOf(sharedPreferences.getString(SettingsUtil.POKEMON_ICON_SCALE, "2")),
                Integer.valueOf(sharedPreferences.getString(SettingsUtil.MAP_REFRESH_RATE, "2")),
                sharedPreferences.getString(SettingsUtil.LAST_USERNAME, ""),
                sharedPreferences.getBoolean(SettingsUtil.SHOW_ONLY_LURED, true),
                sharedPreferences.getBoolean(SettingsUtil.SHOW_GYMS, true),
                sharedPreferences.getBoolean(SettingsUtil.SHOW_POKESTOPS, true)
        ));
    }

    @SuppressWarnings("ConstantConditions")
    public void searchRadiusDialog() {
        sharedPreferences = getSharedPreferences(getString(R.string.shared_key), Context.MODE_PRIVATE);
        scanValue = sharedPreferences.getInt(SettingsUtil.SCAN_VALUE,4);

        final AppCompatDialog dialog = new AppCompatDialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_search_radius);

        final SeekBar seekBar = (SeekBar) dialog.findViewById(R.id.seekBar);
        Button btnSave = (Button) dialog.findViewById(R.id.btnAccept);
        Button btnCancel = (Button) dialog.findViewById(R.id.btnCancel);
        final TextView tvNumber = (TextView) dialog.findViewById(R.id.tvNumber);
        final TextView tvEstimate = (TextView) dialog.findViewById(R.id.tvEstimate);
        tvNumber.setText(String.valueOf(scanValue));
        tvEstimate.setText(getString(R.string.timeEstimate) + " " + UiUtils.getSearchTime(scanValue,this));
        seekBar.setProgress(scanValue);
        seekBar.setMax(12);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                tvNumber.setText(String.valueOf(i));
                tvEstimate.setText(getString(R.string.timeEstimate) + " " + UiUtils.getSearchTime(i,SettingsActivity.this));
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
                int saveValue = seekBar.getProgress();
                if (saveValue == 0) {
                    scanValue = 1;
                } else {
                    scanValue = saveValue;
                }
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt(SettingsUtil.SCAN_VALUE,saveValue);
                editor.apply();
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

    public void logOut() {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.where(User.class).findAll().deleteAllFromRealm();
                realm.where(PokeStop.class).findAll().deleteAllFromRealm();
                realm.where(Pokemons.class).findAll().deleteAllFromRealm();
                realm.where(Gym.class).findAll().deleteAllFromRealm();
                Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        realm = Realm.getDefaultInstance();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        realm.close();
        super.onDestroy();
    }
}
