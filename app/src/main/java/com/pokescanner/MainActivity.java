package com.pokescanner;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.pokescanner.objects.checklogin.LoginLoadedEvent;
import com.pokescanner.objects.checklogin.checkLogin;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import io.fabric.sdk.android.*;
import io.fabric.sdk.android.BuildConfig;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    EditText etUsername;
    EditText etPassword;
    TextView tvTitle;

    LinearLayout Container;
    ProgressBar progressBar;

    String username,password;

    Button btnLogin;
    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!BuildConfig.DEBUG) {
            Fabric.with(this, new Crashlytics());
        }
        setContentView(R.layout.activity_main);

        sharedPref = getSharedPreferences(getString(R.string.shared_key), Context.MODE_PRIVATE);

        boolean login = sharedPref.getBoolean("login",false);

        if (login) {
            startMapIntent();
        }

        btnLogin = (Button) findViewById(R.id.btnLogin);
        etUsername = (EditText) findViewById(R.id.etUsername);
        etPassword = (EditText) findViewById(R.id.etPassword);
        tvTitle = (TextView) findViewById(R.id.tvTitle);

        Container = (LinearLayout) findViewById(R.id.Container);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        btnLogin.setOnClickListener(this);

        getPermissions();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoginLoadedEvent(LoginLoadedEvent event) {
        if (event.getAuthInfo() != null)
        {
            if (event.getAuthInfo().hasToken()) {
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean("login",true);
                editor.putString("username",etUsername.getText().toString());
                editor.putString("password",etPassword.getText().toString());
                editor.commit();

                startMapIntent();
            }else
            {
                Toast.makeText(MainActivity.this, "Bad Login", Toast.LENGTH_SHORT).show();
                showProgress(false);
            }
        }else
        {
            Toast.makeText(MainActivity.this, "Servers might be down.", Toast.LENGTH_SHORT).show();
            showProgress(false);
        }
    }
    @Override
    public void onClick(View view) {
        username = etUsername.getText().toString();
        password = etPassword.getText().toString();

        checkLogin checklogin = new checkLogin(username,password);
        checklogin.start();

        showProgress(true);
    }

    public void showProgress(boolean status) {
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

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

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
