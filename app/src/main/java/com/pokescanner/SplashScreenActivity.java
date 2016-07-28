package com.pokescanner;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.multidex.MultiDex;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.pokescanner.events.AppUpdateEvent;
import com.pokescanner.helper.Settings;
import com.pokescanner.updater.AppUpdateLoader;
import com.pokescanner.updater.AppUpdateDialog;
import com.pokescanner.utils.PermissionUtils;
import com.zl.reik.dilatingdotsprogressbar.DilatingDotsProgressBar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.fabric.sdk.android.Fabric;
import io.realm.Realm;
import io.realm.RealmConfiguration;

public class SplashScreenActivity extends AppCompatActivity
{
    private final static int LOCATION_PERMISSION_REQUESTED = 1400;
    private final static int STORAGE_PERMISSION_REQUESTED = 1300;
    private final static int SPLASH_TIME_OUT = 3000;

    private Context mContext;
    @BindView(R.id.splashProgress) DilatingDotsProgressBar splashProgress;
    @BindView(R.id.splashRootView) RelativeLayout rootView;
    @BindView(R.id.tvSplashVersion) TextView splashVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        MultiDex.install(this);
        super.onCreate(savedInstanceState);
        //Crashlytics initialization
        if (!BuildConfig.DEBUG)
            Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_splash_screen);
        ButterKnife.bind(this);
        mContext = SplashScreenActivity.this;


        //Realm initialization
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder(this)
                .name(Realm.DEFAULT_REALM_NAME)
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(realmConfiguration);
    }

    @Override
    protected void onResume() {
        //Move this to on resume allows the user to close the window
        //and for this to still work if he returns
        super.onResume();
        //Start the progress indicator
        splashProgress.show();
        loadVersionNumber();
        checkRequirementsAndInitialize();
    }

    private void loadVersionNumber()
    {
        try
        {
            PackageInfo pInfo = pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            splashVersion.setText("V"+ version + " ALPHA");
        }
        catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void checkRequirementsAndInitialize()
    {
        if(isConnectedToTheInternet())
        {
            if(checkGooglePlayServicesAvailable())
            {
                if(getLocationPermission())
                {
                    if (isLocationServicesEnabled())
                    {
                        Settings currentSettings = Settings.get(mContext);
                        if (currentSettings.isUpdatesEnabled())
                            new AppUpdateLoader().start();
                        else
                            goToLoginScreen();
                    }
                    else
                        displayErrorDialog(getString(R.string.enable_location_services));
                }
            }
        }
        else
            displayErrorDialog(getString(R.string.no_internet));
    }

    private void displayErrorDialog(String message)
    {
        AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
        dialog.setMessage(message);
        dialog.setCancelable(false);
        dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                checkRequirementsAndInitialize();
            }
        });
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                checkRequirementsAndInitialize();
            }
        });
        dialog.show();
    }

    private boolean isConnectedToTheInternet()
    {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

    private boolean checkGooglePlayServicesAvailable()
    {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(mContext);
        if(result != ConnectionResult.SUCCESS)
        {
            if(googleAPI.isUserResolvableError(result))
            {
                Dialog errorDialog = googleAPI.getErrorDialog(this, result, 1);
                errorDialog.setCancelable(false);
                errorDialog.setCanceledOnTouchOutside(false);
                errorDialog.show();
            }
            else
                Toast.makeText(mContext, googleAPI.getErrorString(result), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean getLocationPermission()
    {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,}, LOCATION_PERMISSION_REQUESTED);
            return false;
        }
        return true;
    }

    private boolean isLocationServicesEnabled()
    {
        LocationManager lm = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;
        gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        return gps_enabled && network_enabled;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAppUpdateEvent(AppUpdateEvent event)
    {
        switch (event.getStatus())
        {
            case AppUpdateEvent.OK:
                if (PermissionUtils.doWeHaveReadWritePermission(this))
                    new AppUpdateDialog(mContext, event.getAppUpdate());
                else
                    getReadWritePermission();
                break;
            case AppUpdateEvent.FAILED:
                showToast(R.string.update_check_failed);
                goToLoginScreen();
                break;
            case AppUpdateEvent.UPTODATE:
                goToLoginScreen();
                break;
        }
    }

    public void goToLoginScreen()
    {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                splashProgress.hide();
                Intent i = new Intent(SplashScreenActivity.this, LoginActivity.class);
                startActivity(i);
                finish();
            }
        }, SPLASH_TIME_OUT);
    }

    public void showToast(int resString) {
        Toast.makeText(mContext, getString(resString), Toast.LENGTH_SHORT).show();
    }

    public void getReadWritePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
                dialog.setCancelable(false)
                .setMessage(R.string.Permission_Required_Auto_Updater)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ActivityCompat.requestPermissions(SplashScreenActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_REQUESTED);
                    }
                })
                .show();
        } else {
            ActivityCompat.requestPermissions(SplashScreenActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_REQUESTED);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {

            case LOCATION_PERMISSION_REQUESTED: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showToast(R.string.PERMISSION_OK);
                    checkRequirementsAndInitialize();
                }else {
                    // Permission request was denied.
                    displayErrorDialog(getString(R.string.requires_location_services));
                }
            }
            break;
            case STORAGE_PERMISSION_REQUESTED:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    showToast(R.string.PERMISSION_OK);
                    new AppUpdateLoader().start();
                }
                else
                {
                    Toast.makeText(mContext, R.string.update_canceled, Toast.LENGTH_SHORT).show();
                    goToLoginScreen();
                }
                break;
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
        super.onDestroy();
    }

}
