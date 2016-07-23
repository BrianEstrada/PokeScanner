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

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.multidex.MultiDex;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.pokescanner.loaders.AuthGOOGLELoader;
import com.pokescanner.loaders.AuthLoadedEvent;
import com.pokescanner.loaders.AuthPTCLoader;
import com.pokescanner.objects.User;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import io.fabric.sdk.android.Fabric;
import io.realm.Realm;
import io.realm.RealmConfiguration;

public class MainActivity extends AppCompatActivity {
    EditText etUsername;
    EditText etPassword;
    TextView tvTitle;

    LinearLayout container;
    ProgressDialog pDialog;

    String username, password;
    Button btnLogin;
    Realm realm;
    int LOGIN_METHOD = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MultiDex.install(this);
        super.onCreate(savedInstanceState);
        if (!BuildConfig.DEBUG) {
            Fabric.with(this, new Crashlytics());
        }
        setContentView(R.layout.activity_main);

        checkIfUserIsLoggedIn();
        initViews();
        getPermissions();
    }

    /**
     * This method checks if the user has previously logged in. If it has,
     * it will open up the MapsActivity.
     */
    private void checkIfUserIsLoggedIn() {
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder(this)
                .name(Realm.DEFAULT_REALM_NAME)
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(realmConfiguration);

        realm = Realm.getDefaultInstance();

        if (realm.where(User.class).findAll().size() != 0) {
            startMapIntent();
        }
    }

    /**
     * This method initializes all the views and assigns them to variables as well as assigns
     * the onClickListener to the login button.
     */
    private void initViews() {
        btnLogin = (Button) findViewById(R.id.btnLogin);
        etUsername = (EditText) findViewById(R.id.etUsername);
        etPassword = (EditText) findViewById(R.id.etPassword);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        container = (LinearLayout) findViewById(R.id.container);

        /**
         * The login button will check whether a PTC account or Gmail account is being used,
         * and will therefore log the user in using the appropriate approach.
         */
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                username = etUsername.getText().toString();
                password = etPassword.getText().toString();
                showProgressbar(true);
                if (isEmailValid(username)) {
                    showToast(R.string.TRYING_GOOGLE_LOGIN);
                    LOGIN_METHOD = User.GOOGLE;
                    AuthGOOGLELoader authGOOGLELoader = new AuthGOOGLELoader(username, password);
                    authGOOGLELoader.start();
                } else {
                    showToast(R.string.TRYING_PTC_LOGIN);
                    LOGIN_METHOD = User.PTC;
                    AuthPTCLoader authloader = new AuthPTCLoader(username, password);
                    authloader.start();
                }
            }
        });
    }

    /**
     * This method will wait to see if the user credentials are authenticated. If they are, it will
     * store the password and data to the database (using realm) and start the MapActivity.
     * Events are fired off after authloader.start() is called.
     * @param event the status of the user authentication
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAuthLoadedEvent(AuthLoadedEvent event) {
        showProgressbar(false);
        switch (event.getStatus()) {
            case AuthLoadedEvent.OK:
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        User user = new User(1, username, password, LOGIN_METHOD);
                        realm.copyToRealmOrUpdate(user);
                        startMapIntent();
                        showToast(R.string.LOGIN_OK);
                    }
                });
                break;
            case AuthLoadedEvent.AUTH_FAILED:
                showToast(R.string.AUTH_FAILED);
                break;
            case AuthLoadedEvent.SERVER_FAILED:
                showToast(R.string.SERVER_FAILED);
                break;
        }

    }


    /**
     * This method will launch the MapActivity if the user has approved permissions. If not,
     * the app will request that the user allow the app the permissions.
     */
    public void startMapIntent() {
        if (doWeHavePermission()) {
            Intent intent = new Intent(this, MapsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } else {
            getPermissions();
        }
    }

    /**
     * Displays a loading dialog. It will hide the EditText fields while active.
     * @param status a boolean that determines if the dialog should be visible. True: Visible Progress Dialog.
     *               False: Hide the Progress Dialog and display the EditText fields.
     */
    public void showProgressbar(boolean status) {
        if (status) {
            container.setVisibility(View.INVISIBLE);
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Logging In...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        } else {
            pDialog.dismiss();
            container.setVisibility(View.VISIBLE);
        }
    }

    /**
     * The following methods check whether the app has permissions enabled or not.
     */
    public boolean doWeHavePermission() {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public void getPermissions() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION}, 1400);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1400: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showToast(R.string.PERMISSION_OK);
                }
            }
        }
    }

    /**
     * Miscellaneous methods
     */

    boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }


    public void showToast(int resString) {
        Toast.makeText(MainActivity.this, getString(resString), Toast.LENGTH_SHORT).show();
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
