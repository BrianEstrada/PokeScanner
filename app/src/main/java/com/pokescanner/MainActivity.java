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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.multidex.MultiDex;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.pokegoapi.auth.PTCLogin;
import com.pokegoapi.exceptions.LoginFailedException;

import POGOProtos.Networking.Envelopes.RequestEnvelopeOuterClass;
import io.fabric.sdk.android.Fabric;
import okhttp3.OkHttpClient;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func0;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    EditText etUsername;
    EditText etPassword;
    TextView tvTitle;

    LinearLayout Container;
    ProgressBar progressBar;

    String username,password;
    Button btnRegister;
    Button btnLogin;
    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MultiDex.install(this);
        super.onCreate(savedInstanceState);
        if (!BuildConfig.DEBUG) {
            Fabric.with(this, new Crashlytics());
        }
        setContentView(R.layout.activity_main);

        sharedPref = getSharedPreferences(getString(R.string.shared_key), Context.MODE_PRIVATE);

        //If we are logged in this value will be true
        boolean login = sharedPref.getBoolean("login",false);
        //if this value is true then lets go to the map
        if (login) {
            startMapIntent();
        }

        //get our views
        btnLogin = (Button) findViewById(R.id.btnLogin);
        etUsername = (EditText) findViewById(R.id.etUsername);
        etPassword = (EditText) findViewById(R.id.etPassword);
        tvTitle = (TextView) findViewById(R.id.tvTitle);

        Container = (LinearLayout) findViewById(R.id.Container);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        btnRegister = (Button) findViewById(R.id.btnRegister);
        btnRegister.setMovementMethod(LinkMovementMethod.getInstance());

        btnLogin.setOnClickListener(this);

        //finally we are going to ask for permission to the GPS
        getPermissions();
    }

    @Override
    public void onClick(View view) {
        //get our username and password value
        username = etUsername.getText().toString();
        password = etPassword.getText().toString();
        //begin to show the progress bar
        showProgressbar(true);
        CheckLogin().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(MainActivity.this, "Bad login (Servers might be down)", Toast.LENGTH_SHORT).show();
                        showProgressbar(false);
                    }

                    @Override
                    public void onNext(RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo authInfo) {
                        if (authInfo != null) {
                            if (authInfo.hasToken()) {
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putBoolean("login", true);
                                editor.putString("username", etUsername.getText().toString());
                                editor.putString("password", etPassword.getText().toString());
                                editor.commit();

                                startMapIntent();
                                Toast.makeText(MainActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }
    public Observable<RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo> CheckLogin() {
        return Observable.defer(new Func0<Observable<RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo>>() {
            @Override public Observable<RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo> call() {
                try {
                    OkHttpClient client = new OkHttpClient();
                    PTCLogin ptcLogin = new PTCLogin(client);
                    RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo auth = ptcLogin.login(username,password);
                    System.out.println(auth);
                    return Observable.just(auth);
                } catch (LoginFailedException e) {
                    showProgressbar(false);
                    Toast.makeText(MainActivity.this, "Bad login (Servers might be down)", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                    return null;
                }
            }
        });
    }

    public void startMapIntent() {
        if (doWeHavePermission()) {
            Intent intent = new Intent(this, MapsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }else
        {
            getPermissions();
        }
    }

    //if this value is true then lets hide the login and show the progress bar
    public void showProgressbar(boolean status) {
        if (status)
        {
            progressBar.setVisibility(View.VISIBLE);
            Container.setVisibility(View.GONE);
        }else
        {
            progressBar.setVisibility(View.GONE);
            Container.setVisibility(View.VISIBLE);
        }
    }
    //Permission Stuff
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
                    Toast.makeText(this, "Thanks for the permission!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
