/*
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


package com.pokescanner;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.multidex.MultiDex;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.pokescanner.events.AppUpdateEvent;
import com.pokescanner.events.AuthLoadedEvent;
import com.pokescanner.helper.Settings;
import com.pokescanner.loaders.AuthGOOGLELoader;
import com.pokescanner.loaders.AuthPTCLoader;
import com.pokescanner.objects.User;
import com.pokescanner.updater.AppUpdateDialog;
import com.pokescanner.updater.AppUpdateLoader;
import com.pokescanner.utils.PermissionUtils;
import com.pokescanner.utils.UiUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.fabric.sdk.android.Fabric;
import io.realm.Realm;
import io.realm.RealmConfiguration;


public class LoginActivity extends AppCompatActivity {
    @BindView(R.id.etUsername) EditText etUsername;
    @BindView(R.id.etPassword) EditText etPassword;
    @BindView(R.id.tvCheckServer) TextView tvCheckServer;
    @BindView(R.id.tvVersionNumber) TextView tvVersionNumber;

    @BindView(R.id.main) LinearLayout main;
    @BindView(R.id.Container) LinearLayout Container;
    @BindView(R.id.progressBar) ProgressBar progressBar;

    String username, password;

    Realm realm;
    int LOGIN_METHOD = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MultiDex.install(this);
        super.onCreate(savedInstanceState);
        if (!BuildConfig.DEBUG) {
            Fabric.with(this, new Crashlytics());
        }
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder(this)
                .name(Realm.DEFAULT_REALM_NAME)
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(realmConfiguration);

        realm = Realm.getDefaultInstance();

        //Add version number to login
        loadVersionCode();

        //finally we are going to ask for permission to the GPS
        getLocationPermission();

        //Is Auto Update enabled?
        if (Settings.get(this).isUpdatesEnabled()) {
            //Lets go check for an update
            new AppUpdateLoader().start();
        }else {
            //If not lets find out if we have a login
            checkIfUserIsLoggedIn();
        }
    }

    public void checkIfUserIsLoggedIn() {
        if (realm.where(User.class).findAll().size() != 0) {
            User user = realm.where(User.class).findFirst();
            if (user.getAuthType() == User.PTC) {
                etUsername.setText(user.getUsername());
                etPassword.setText(user.getPassword());
                btnPTCLogin();
            } else {
                LOGIN_METHOD = User.GOOGLE;
                onAuthLoadedEvent(new AuthLoadedEvent(AuthLoadedEvent.OK, user.getToken()));
            }
        }
    }

    @OnClick(R.id.btnLogin)
    public void btnPTCLogin() {
        //get our username and password value
        username = etUsername.getText().toString();
        password = etPassword.getText().toString();
        //begin to show the progress bar
        showProgressbar(true);
        UiUtils.hideKeyboard(etPassword);


        showToast(R.string.TRYING_PTC_LOGIN);
        LOGIN_METHOD = User.PTC;
        AuthPTCLoader authloader = new AuthPTCLoader(username, password);
        authloader.start();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAuthLoadedEvent(final AuthLoadedEvent event) {
        switch (event.getStatus()) {
            case AuthLoadedEvent.OK:
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        User user = new User(1,
                                username,
                                password,
                                event.getToken(),
                                LOGIN_METHOD);
                        realm.copyToRealmOrUpdate(user);
                        startMapIntent();
                        showToast(R.string.LOGIN_OK);

                        LoginActivity context = LoginActivity.this;
                        Settings.get(context).toBuilder()
                                .lastUsername(user.getUsername())
                                .build().save(context);
                    }
                });
                break;
            case AuthLoadedEvent.AUTH_FAILED:
                showProgressbar(false);
                showToast(R.string.AUTH_FAILED);
                break;
            case AuthLoadedEvent.SERVER_FAILED:
                showProgressbar(false);
                showToast(R.string.SERVER_FAILED);
                break;
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAppUpdateEvent(AppUpdateEvent event) {
        switch (event.getStatus()) {
            case AppUpdateEvent.OK:
                if (PermissionUtils.doWeHaveReadWritePermission(this)) {
                    new AppUpdateDialog(LoginActivity.this, event.getAppUpdate());
                }else
                {
                    getReadWritePermission();
                }
                break;
            case AppUpdateEvent.FAILED:
                showToast(R.string.update_check_failed);
                break;
            case AppUpdateEvent.UPTODATE:
                checkIfUserIsLoggedIn();
                break;
        }
    }

    public void showToast(int resString) {
        Toast.makeText(LoginActivity.this, getString(resString), Toast.LENGTH_SHORT).show();
    }

    public void startMapIntent() {
        if (PermissionUtils.doWeHaveLocationPermission(this)) {
            Intent intent = new Intent(this, MapsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } else {
            getLocationPermission();
        }
    }

    @OnClick(R.id.tvCheckServer)
    public void showServerStatus() {
        String url = "http://ispokemongodownornot.com/";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    @OnClick(R.id.btnGoogleLogin)
    public void GoogleLogin(View view) {
        showToast(R.string.TRYING_GOOGLE_LOGIN);
        LOGIN_METHOD = User.GOOGLE;
        Intent intent = new Intent(this, GoogleLoginActivity.class);
        startActivityForResult(intent, 1300);
    }

    //if this value is true then lets hide the login and show the progress bar
    public void showProgressbar(boolean status) {
        if (status) {
            progressBar.setVisibility(View.VISIBLE);
            Container.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            Container.setVisibility(View.VISIBLE);
        }
    }

    public void getReadWritePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Snackbar.make(main,getString(R.string.Permission_Required_Auto_Updater),Snackbar.LENGTH_LONG).setAction("Ok", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ActivityCompat.requestPermissions(LoginActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE}, 1300);
                }
            }).show();
        } else {
            showToast(R.string.Get_Permission_Auto_Updater);
            ActivityCompat.requestPermissions(LoginActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE}, 1300);
        }
    }

    public void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,}, 1400);
        }
    }

    private void loadVersionCode() {
        try {
            PackageInfo pInfo = null;
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            tvVersionNumber.setText("Version: "+ version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1400: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showToast(R.string.PERMISSION_OK);
                }else {
                    // Permission request was denied.
                    Snackbar.make(main, getString(R.string.location_denied),
                            Snackbar.LENGTH_SHORT)
                            .show();
                }
            }
            break;
            case 1300:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    showToast(R.string.PERMISSION_OK);
                    new AppUpdateLoader().start();
                }else {
                    // Permission request was denied.
                    Snackbar.make(main, getString(R.string.auto_update_denied),
                            Snackbar.LENGTH_SHORT)
                            .show();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (data == null || !data.hasExtra(GoogleLoginActivity.EXTRA_CODE)) {
            showToast(R.string.AUTH_FAILED);
            return;
        }
        String code = data.getStringExtra(GoogleLoginActivity.EXTRA_CODE);
        if (code != null) {
            AuthGOOGLELoader authGOOGLELoader = new AuthGOOGLELoader(code);
            authGOOGLELoader.start();
        } else {
            showToast(R.string.AUTH_FAILED);
        }
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
    protected void onDestroy() {
        realm.close();
        super.onDestroy();
    }
}
