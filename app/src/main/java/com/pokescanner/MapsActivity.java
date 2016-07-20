package com.pokescanner;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
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
import com.pokegoapi.api.map.MapObjects;
import com.pokescanner.objects.map.getLogin;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.joda.time.Interval;

import java.util.ArrayList;
import java.util.Collection;

import POGOProtos.Map.Pokemon.MapPokemonOuterClass;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {
    Button button;
    ProgressBar progressBar;
    private GoogleMap mMap;
    LocationManager locationManager;
    Location currentLocation;
    String username, password;
    SharedPreferences sharedPref;
    Collection<MapPokemonOuterClass.MapPokemon> pokemons = new ArrayList<>();

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
            relog();
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
                scanForPokemans();
            }
        });
    }

    public void relog() {
        getPreferences(Context.MODE_PRIVATE).edit().clear().commit();

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @SuppressWarnings({"ResourceType"})
    public void scanForPokemans() {
        if (isGPSEnabled()) {
            /*
            Criteria criteria = new Criteria();
            String provider = locationManager.getBestProvider(criteria, true);
            currentLocation = locationManager.getLastKnownLocation(provider);
            centerCamera();
            */
            LatLng latLng = new LatLng(mMap.getCameraPosition().target.latitude, mMap.getCameraPosition().target.longitude);

            if (username != null && password != null) {
                if (currentLocation != null) {
                    enableProgress(true);
                    getLogin login = new getLogin(latLng, username, password);
                    login.start();
                }
            }
        } else {
            Toast.makeText(MapsActivity.this, "Cannot get Location is your GPS on?", Toast.LENGTH_SHORT).show();
        }
    }

    public void refreshMap() {
        mMap.clear();
        for (MapPokemonOuterClass.MapPokemon pokemon : pokemons) {

            String uri = "p" + pokemon.getPokemonIdValue();
            int resourceID = getResources().getIdentifier(uri, "drawable", getPackageName());
            Bitmap image = BitmapFactory.decodeResource(getResources(), resourceID);

            DateTime oldDate = new DateTime(pokemon.getExpirationTimestampMs());
            Interval interval = new Interval(new Instant(), oldDate);


            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(pokemon.getLatitude(), pokemon.getLongitude()))
                    .icon(BitmapDescriptorFactory.fromBitmap(image))
                    .title(pokemon.getPokemonId().toString())
                    .snippet("Expires: " + String.valueOf(interval.toDurationMillis() / 1000) + "s"));
        }
    }

    public void addPokemon(MapPokemonOuterClass.MapPokemon pokemonadd) {
        boolean dup = false;
        for (MapPokemonOuterClass.MapPokemon pokemon : pokemons) {
            if (pokemon.getEncounterId() == pokemonadd.getEncounterId()) {
                dup = true;
                break;
            }
        }
        if (!dup) {
            pokemons.add(pokemonadd);
        }
    }

    public void enableProgress(boolean status) {
        if (status) {
            progressBar.setVisibility(View.VISIBLE);
            button.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            button.setVisibility(View.VISIBLE);
        }
    }

    public void centerCamera() {
        if (currentLocation != null) {
            LatLng target = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            CameraPosition position = this.mMap.getCameraPosition();

            CameraPosition.Builder builder = new CameraPosition.Builder();
            builder.zoom(15);
            builder.target(target);

            this.mMap.animateCamera(CameraUpdateFactory.newCameraPosition(builder.build()));
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoadedScan(PokemonLoadedEvent event) {
        if (event.getMapObjects() != null) {
            MapObjects mapObjects = event.getMapObjects();
            pokemons.addAll(mapObjects.getCatchablePokemons());
            refreshMap();
        } else {
            enableProgress(true);
        }
    }

    @Override
    @SuppressWarnings({"ResourceType"})
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setMyLocationEnabled(true);

        if (doWeHavePermission() && isGPSEnabled()) {
            Criteria criteria = new Criteria();
            String provider = locationManager.getBestProvider(criteria, true);
            currentLocation = locationManager.getLastKnownLocation(provider);

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }

        centerCamera();
    }

    public boolean doWeHavePermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean isGPSEnabled() {
        LocationManager cm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return cm.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    @Override
    public void onLocationChanged(Location location) {
        if (mMap != null) {
            if (location.distanceTo(currentLocation) > 10) {
                currentLocation = location;
                //centerCamera();
            }
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
