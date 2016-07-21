package com.pokescanner;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.map.Map;
import com.pokegoapi.api.map.Pokemon.CatchablePokemon;
import com.pokegoapi.auth.PTCLogin;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;

import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.joda.time.Interval;

import java.util.ArrayList;
import java.util.List;

import POGOProtos.Networking.Envelopes.RequestEnvelopeOuterClass;
import okhttp3.OkHttpClient;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    Button button;
    ProgressBar progressBar;
    private GoogleMap mMap;

    LocationManager locationManager;
    Location currentLocation;
    String username, password;

    SharedPreferences sharedPref;

    ArrayList<LatLng> scanMap = new ArrayList<>();
    ArrayList<CatchablePokemon> pokemons = new ArrayList<>();

    double dist = 00.002000;
    final int SLEEP_TIME = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        sharedPref = getSharedPreferences(getString(R.string.shared_key), Context.MODE_PRIVATE);

        boolean login = sharedPref.getBoolean("login", false);

        if (login) {
            username = sharedPref.getString("username", null);
            password = sharedPref.getString("password", null);
        } else {
            Toast.makeText(MapsActivity.this, "No login!", Toast.LENGTH_SHORT).show();
            logOut();
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        button = (Button) findViewById(R.id.btnSearch);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PokeScan();
            }
        });
    }

    public void PokeScan() {
        showProgressbar(true);
        createScanMap(mMap.getCameraPosition().target);
        new loadPokemon().execute();
    }

    @Override
    @SuppressWarnings({"MissingPermission"})
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (doWeHavePermission()) {
            Criteria criteria = new Criteria();
            String provider = locationManager.getBestProvider(criteria, true);
            currentLocation = locationManager.getLastKnownLocation(provider);
            mMap.setMyLocationEnabled(true);
            centerCamera();
        }
    }

    public void centerCamera() {
        if (currentLocation != null && doWeHavePermission()) {
            LatLng target = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            CameraPosition position = this.mMap.getCameraPosition();

            CameraPosition.Builder builder = new CameraPosition.Builder();
            builder.zoom(15);
            builder.target(target);

            this.mMap.animateCamera(CameraUpdateFactory.newCameraPosition(builder.build()));
        }
    }

    public void showProgressbar(boolean status) {
        if (status) {
            progressBar.setVisibility(View.VISIBLE);
            button.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            button.setVisibility(View.VISIBLE);
        }
    }

    public boolean isGPSEnabled() {
        LocationManager cm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return cm.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public void logOut() {
        getPreferences(Context.MODE_PRIVATE).edit().clear().commit();

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void refreshMap() {
        mMap.clear();
        for (CatchablePokemon pokemon : pokemons) {


            String uri = "p" + pokemon.getPokemonId().getNumber();
            int resourceID = getResources().getIdentifier(uri, "drawable", getPackageName());
            Bitmap image = BitmapFactory.decodeResource(getResources(), resourceID);

            DateTime oldDate = new DateTime(pokemon.getExpirationTimestampMs());
            Interval interval;
            if (oldDate.isAfter(new Instant())) {
                interval = new Interval(new Instant(), oldDate);
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(pokemon.getLatitude(), pokemon.getLongitude()))
                        .icon(BitmapDescriptorFactory.fromBitmap(image))
                        .title(pokemon.getPokemonId().toString())
                        .snippet("Expires: " + String.valueOf(interval.toDurationMillis() / 1000) + "s"));
            }else
            {
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(pokemon.getLatitude(), pokemon.getLongitude()))
                        .icon(BitmapDescriptorFactory.fromBitmap(image))
                        .title(pokemon.getPokemonId().toString()));
            }
        }

    }

    public void createScanMap(LatLng loc) {
        //Clear our map
        scanMap.clear();
        double lat = loc.latitude + (dist * -2);
        double lon = loc.longitude + (dist * -2);
        //this is the GPS offset we're going to use

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                double newLat = (lat + (dist * i));
                double newLon = (lon + (dist * j));
                LatLng temp = new LatLng(newLat, newLon);
                scanMap.add(temp);
            }
        }
        System.out.println(scanMap.size());
    }

    public boolean doWeHavePermission() {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }


    private class loadPokemon extends AsyncTask<String, List<CatchablePokemon>, String> {
        int pos = 0;

        @Override
        protected String doInBackground(String... params) {
            try {
                OkHttpClient client = new OkHttpClient();
                RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo authInfo = new PTCLogin(client).login("throwaway562", "throwaway562");
                PokemonGo go = new PokemonGo(authInfo, client);
                for (LatLng loc : scanMap) {
                    try {
                        go.setLongitude(loc.longitude);
                        go.setLatitude(loc.latitude);
                        Map map = new Map(go);
                        publishProgress(map.getCatchablePokemon());
                        Thread.sleep(SLEEP_TIME);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (RemoteServerException e) {
                        e.printStackTrace();
                    } catch (LoginFailedException e) {
                        e.printStackTrace();
                    }
                }
            } catch (LoginFailedException e) {
                e.printStackTrace();
            }
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            showProgressbar(false);
        }

        @Override
        protected void onPreExecute() {
            showProgressbar(true);
        }

        @Override
        protected void onProgressUpdate(List<CatchablePokemon>... objects) {
            progressBar.setProgress(pos * 4);
            if (objects.length < 1) return;
            List<CatchablePokemon> object = objects[0];
            pokemons.addAll(object);
            refreshMap();
            pos++;
        }
    }
}
