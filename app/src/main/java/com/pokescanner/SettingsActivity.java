package com.pokescanner;

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

import com.pokescanner.events.AppUpdateEvent;
import com.pokescanner.helper.Settings;
import com.pokescanner.objects.Gym;
import com.pokescanner.objects.PokeStop;
import com.pokescanner.objects.Pokemons;
import com.pokescanner.objects.User;
import com.pokescanner.updater.AppUpdateDialog;
import com.pokescanner.updater.AppUpdateLoader;
import com.pokescanner.utils.PermissionUtils;
import com.pokescanner.utils.SettingsUtil;
import com.pokescanner.utils.UiUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import io.realm.Realm;

public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    SharedPreferences preferences;
    Preference scan_dialog,gym_filter,pokemon_filter,expiration_filter;
    Preference clear_pokemon,clear_gyms,clear_pokestops;
    Preference logout,update;
    Realm realm;
    int scanValue;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.registerOnSharedPreferenceChangeListener(this);
        Settings settings = SettingsUtil.getSettings(this);

        preferences.edit()
                .putBoolean(SettingsUtil.ENABLE_UPDATES,settings.isUpdatesEnabled())
                .putBoolean(SettingsUtil.KEY_BOUNDING_BOX,settings.isBoundingBoxEnabled())
                .putString(SettingsUtil.SCAN_VALUE,String.valueOf(settings.getScanValue()))
                .putBoolean(SettingsUtil.SHOW_ONLY_LURED,settings.isShowOnlyLured())
                .putBoolean(SettingsUtil.SHOW_GYMS,settings.isGymsEnabled())
                .putBoolean(SettingsUtil.SHOW_POKESTOPS,settings.isPokestopsEnabled())
                .putBoolean(SettingsUtil.SHOW_LURED_POKEMON,settings.isShowLuredPokemon())
                .putBoolean(SettingsUtil.KEY_LOCK_GPS,settings.isLockGpsEnabled())
                .putString(SettingsUtil.SERVER_REFRESH_RATE,String.valueOf(settings.getServerRefresh()))
                .putString(SettingsUtil.MAP_REFRESH_RATE,String.valueOf(settings.getMapRefresh()))
                .putString(SettingsUtil.POKEMON_ICON_SCALE,String.valueOf(settings.getScale()))
                .putString(SettingsUtil.LAST_USERNAME,settings.getLastUsername())
                .putBoolean(SettingsUtil.SHUFFLE_ICONS,settings.isShuffleIcons())
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

        expiration_filter = (Preference) getPreferenceManager().findPreference("expiration_filter");
        expiration_filter.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                ExpirationFilters.showExpirationFiltersDialog(SettingsActivity.this);
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

        update = (Preference) getPreferenceManager().findPreference("update");
        update.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                new AppUpdateLoader().start();
                return true;
            }
        });
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAppUpdateEvent(AppUpdateEvent event) {
        switch (event.getStatus()) {
            case AppUpdateEvent.OK:
                if (PermissionUtils.doWeHaveReadWritePermission(this)) {
                    new AppUpdateDialog(SettingsActivity.this, event.getAppUpdate());
                }
                break;
            case AppUpdateEvent.FAILED:
                Toast.makeText(SettingsActivity.this, getString(R.string.update_check_failed), Toast.LENGTH_SHORT).show();
                break;
        }
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        System.out.println(sharedPreferences.getAll().toString());
        SettingsUtil.saveSettings(this,new Settings(
                sharedPreferences.getBoolean(SettingsUtil.ENABLE_UPDATES,true),
                sharedPreferences.getBoolean(SettingsUtil.KEY_BOUNDING_BOX, false),
                sharedPreferences.getBoolean(SettingsUtil.KEY_LOCK_GPS, false),
                Integer.valueOf(sharedPreferences.getString(SettingsUtil.SCAN_VALUE,"4")),
                Integer.valueOf(sharedPreferences.getString(SettingsUtil.SERVER_REFRESH_RATE, "1")),
                Integer.valueOf(sharedPreferences.getString(SettingsUtil.POKEMON_ICON_SCALE, "2")),
                Integer.valueOf(sharedPreferences.getString(SettingsUtil.MAP_REFRESH_RATE, "2")),
                sharedPreferences.getString(SettingsUtil.LAST_USERNAME, ""),
                sharedPreferences.getBoolean(SettingsUtil.SHOW_ONLY_LURED, true),
                sharedPreferences.getBoolean(SettingsUtil.SHOW_GYMS, true),
                sharedPreferences.getBoolean(SettingsUtil.SHOW_POKESTOPS, true),
                sharedPreferences.getBoolean(SettingsUtil.SHOW_LURED_POKEMON, true),
                sharedPreferences.getBoolean(SettingsUtil.SHUFFLE_ICONS, false)
        ));
    }

    @SuppressWarnings("ConstantConditions")
    public void searchRadiusDialog() {
        scanValue = Integer.valueOf(preferences.getString(SettingsUtil.SCAN_VALUE,"4"));

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
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(SettingsUtil.SCAN_VALUE,String.valueOf(scanValue));
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
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
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
