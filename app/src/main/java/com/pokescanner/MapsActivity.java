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
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.multidex.MultiDex;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.pokescanner.events.ForceLogoutEvent;
import com.pokescanner.events.ForceRefreshEvent;
import com.pokescanner.events.PublishProgressEvent;
import com.pokescanner.events.RestartRefreshEvent;
import com.pokescanner.helper.CustomMapFragment;
import com.pokescanner.helper.GymFilter;
import com.pokescanner.helper.PokemonListLoader;
import com.pokescanner.helper.Settings;
import com.pokescanner.loaders.MapObjectsLoader;
import com.pokescanner.objects.Gym;
import com.pokescanner.objects.PokeStop;
import com.pokescanner.objects.Pokemons;
import com.pokescanner.objects.User;
import com.pokescanner.utils.DrawableUtils;
import com.pokescanner.utils.MarkerDetails;
import com.pokescanner.utils.PermissionUtils;
import com.pokescanner.utils.SettingsUtil;
import com.pokescanner.utils.UiUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import io.realm.Realm;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

import static com.pokescanner.helper.Generation.getCorners;
import static com.pokescanner.helper.Generation.makeHexScanMap;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnCameraChangeListener,
        OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    @BindView(R.id.btnSearch) FloatingActionButton button;
    @BindView(R.id.progressBar) ProgressBar progressBar;
    private GoogleMap mMap;
    @BindView(R.id.btnSettings) ImageButton btnSettings;
    @BindView(R.id.main) RelativeLayout main;

    LocationManager locationManager;
    Location currentLocation;
    CameraPosition currentCameraPos;

    User user;
    Realm realm;

    private GoogleApiClient mGoogleApiClient;
    List<LatLng> scanMap = new ArrayList<>();

    private Map<Pokemons, Marker> pokemonsMarkerMap = new HashMap<Pokemons, Marker>();
    private Map<Gym, Marker> gymMarkerMap = new HashMap<Gym, Marker>();
    private Map<PokeStop, Marker> pokestopMarkerMap = new HashMap<PokeStop, Marker>();
    Circle mBoundingBox = null;

    private MapObjectsLoader mapObjectsLoader;

    int pos = 1;
    //Used for determining Scan status
    boolean SCANNING_STATUS = false;
    //Used for our refreshing of the map
    Subscription pokeonRefresher, gymstopRefresher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MultiDex.install(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        ButterKnife.bind(this);

        realm = Realm.getDefaultInstance();

        //So if our realm has no users then we'll send our user back to the login screen
        //otherwise set our user and move on!
        if (realm.where(User.class).findAll().size() != 0) {
            user = realm.copyFromRealm(realm.where(User.class).findFirst());
        } else {
            Toast.makeText(MapsActivity.this, "No login!", Toast.LENGTH_SHORT).show();
            logOut();
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (CustomMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Start our location manager so we can center our map
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        try {
            PokemonListLoader.populatePokemonList(this);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    @OnClick(R.id.btnClear)
    public void clearMap() {
        if (mMap != null) {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {

                    realm.where(Pokemons.class).findAll().deleteAllFromRealm();
                    realm.where(Gym.class).findAll().deleteAllFromRealm();
                    realm.where(PokeStop.class).findAll().deleteAllFromRealm();
                    mMap.clear();
                    showToast(R.string.cleared_map);
                }
            });
        }
    }
    @OnClick(R.id.btnSearch)
    public void PokeScan() {
        if (SCANNING_STATUS) {
            stopPokeScan();
        } else {
            pos = 1;
            //Load our scan value
            int scanValue = Settings.get(this).getScanValue();
            //Our refresh rate to Milliseconds
            int millis = SettingsUtil.getSettings(this).getServerRefresh() * 1000;
            showProgressbar(true);
            progressBar.setProgress(0);
            LatLng pos = mMap.getCameraPosition().target;
            if(mGoogleApiClient.isConnected())
                currentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            if ((SettingsUtil.getSettings(this).isLockGpsEnabled() ||
                    SettingsUtil.getSettings(MapsActivity.this).isDrivingModeEnabled())
                    && moveCameraToCurrentPosition()) {
                pos = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            }

            scanMap = makeHexScanMap(pos, scanValue, 1, new ArrayList<LatLng>());
            if (scanMap != null) {
                mapObjectsLoader = new MapObjectsLoader(user, scanMap, millis, this);
                mapObjectsLoader.start();
            } else {
                showToast(R.string.ERROR_GENERATING_GRID);
                showProgressbar(false);
            }
        }
    }
    @OnClick(R.id.btnSettings)
    public void onSettingsClick() {
        Intent settingsIntent = new Intent(MapsActivity.this, SettingsActivity.class);
        startActivity(settingsIntent);
    }
    private void stopPokeScan() {
        try {
            mapObjectsLoader.interrupt();
            mapObjectsLoader.join();
            showProgressbar(false);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @OnLongClick(R.id.btnSearch)
    public boolean onLongClickSearch() {
        SettingsUtil.searchRadiusDialog(this);
        return true;
    }

    public void showToast(int resString) {
        Toast.makeText(MapsActivity.this, getString(resString), Toast.LENGTH_SHORT).show();
    }

    @Override
    @SuppressWarnings({"MissingPermission"})
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (PermissionUtils.doWeHaveGPSandLOC(this)) {
            //Set our map stuff
            mMap.setMyLocationEnabled(true);
            mMap.setOnCameraChangeListener(this);
            //Let's find our location and set it!
            mMap.getUiSettings().setMapToolbarEnabled(false);
            mMap.getUiSettings().setZoomControlsEnabled(false);
            mMap.getUiSettings().setCompassEnabled(true);
            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    Object markerKey = null;
                    for (Map.Entry<Pokemons, Marker> pokemonsMarkerEntry : pokemonsMarkerMap.entrySet()) {
                        if (pokemonsMarkerEntry.getValue().equals(marker)) {
                            markerKey = pokemonsMarkerEntry.getKey();
                            break;
                        }
                    }
                    if (markerKey == null) {
                        for (Map.Entry<Gym, Marker> gymMarkerEntry : gymMarkerMap.entrySet()) {
                            if (gymMarkerEntry.getValue().equals(marker)) {
                                markerKey = gymMarkerEntry.getKey();
                                break;
                            }
                        }
                    }
                    if (markerKey == null) {
                        for (Map.Entry<PokeStop, Marker> pokeStopMarkerEntry : pokestopMarkerMap.entrySet()) {
                            if (pokeStopMarkerEntry.getValue().equals(marker)) {
                                markerKey = pokeStopMarkerEntry.getKey();
                                break;
                            }
                        }
                    }
                    if (markerKey != null) {
                        if (!Settings.get(MapsActivity.this).isUseOldMapMarker()) {
                            removeAdapterAndListener();
                            MarkerDetails.showMarkerDetailsDialog(MapsActivity.this, markerKey, currentLocation);
                        } else {
                            setAdapterAndListener(markerKey);
                            marker.showInfoWindow();
                        }
                    }
                    return false;
                }
            });

            moveCameraToCurrentPosition();
            startRefresher();
        }
    }

    @SuppressWarnings({"MissingPermission"})
    public boolean moveCameraToCurrentPosition() {
        if (PermissionUtils.doWeHaveGPSandLOC(this)) {
            if(mGoogleApiClient.isConnected())
                currentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                createBoundingBox();
            if (currentLocation != null) {
                LatLng target = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                CameraPosition.Builder builder = new CameraPosition.Builder();
                builder.zoom(15);
                builder.target(target);

                if(mMap != null)
                {
                    this.mMap.animateCamera(CameraUpdateFactory.newCameraPosition(builder.build()));
                    return true;
                }
                else
                    return false;
            }
            else
            {
                //Try again after half a second
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        moveCameraToCurrentPosition();
                    }
                }, 500);
            }
        }
        return false;
    }

    public void showProgressbar(boolean status) {
        if (status) {
            progressBar.setVisibility(View.VISIBLE);
            button.setImageDrawable(ContextCompat.getDrawable(MapsActivity.this, R.drawable.ic_pause_white_24dp));
            SCANNING_STATUS = true;
        } else {
            progressBar.setVisibility(View.INVISIBLE);
            button.setImageDrawable(ContextCompat.getDrawable(MapsActivity.this, R.drawable.ic_track_changes_white_24dp));
            SCANNING_STATUS = false;
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        if (PermissionUtils.doWeHaveGPSandLOC(this)) {
            currentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {

    }

    public void logOut() {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.where(User.class).findAll().deleteAllFromRealm();
                realm.where(PokeStop.class).findAll().deleteAllFromRealm();
                realm.where(Pokemons.class).findAll().deleteAllFromRealm();
                realm.where(Gym.class).findAll().deleteAllFromRealm();
                Intent intent = new Intent(MapsActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });
    }

    //Map related Functions
    public void refreshMap() {
        if (mMap != null) {
            LatLngBounds curScreen = mMap.getProjection().getVisibleRegion().latLngBounds;
            //We use this to check when our map object loader is done loading anything
            //If is done loading then we set our progress bar off
            //It's a quick fix in the future we should implement a listener inside the thread.
            if (mapObjectsLoader != null) {
                if (mapObjectsLoader.getState().equals(Thread.State.TERMINATED)) {
                    showProgressbar(false);
                }
            }
            createMapObjects();

            //If in driving mode, move camera to current location
            if(SettingsUtil.getSettings(MapsActivity.this).isDrivingModeEnabled())
                moveCameraToCurrentPosition();

            //Load our Pokemon Array
            ArrayList<Pokemons> pokemons = new ArrayList<Pokemons>(realm.copyFromRealm(realm.where(Pokemons.class).findAll()));
            //Okay so we're going to fix the annoying issue where the markers were being constantly redrawn
            for (int i = 0; i < pokemons.size(); i++) {
                //Get our pokemon from the list
                Pokemons pokemon = pokemons.get(i);
                //Is our pokemon contained within the bounds of the camera?
                if (curScreen.contains(new LatLng(pokemon.getLatitude(), pokemon.getLongitude()))) {
                    //If yes then has he expired?
                    //This isnt worded right it should say isNotExpired (Will fix later)
                    if (pokemon.isExpired()) {
                        if (UiUtils.isPokemonFiltered(pokemon) ||
                                UiUtils.isPokemonExpiredFiltered(pokemon, this)) {
                            if (pokemonsMarkerMap.containsKey(pokemon)) {
                                Marker marker = pokemonsMarkerMap.get(pokemon);
                                if (marker != null) {
                                    marker.remove();
                                    pokemonsMarkerMap.remove(pokemon);
                                }
                            }
                        } else {
                            //Okay finally is he contained within our hashmap?
                            if (pokemonsMarkerMap.containsKey(pokemon)) {
                                //Well if he is then lets pull out our marker.
                                Marker marker = pokemonsMarkerMap.get(pokemon);
                                //Update the marker
                                marker = pokemon.updateMarker(marker, this);
                            } else {
                                //If our pokemon wasn't in our hashmap lets add him
                                pokemonsMarkerMap.put(pokemon, mMap.addMarker(pokemon.getMarker(this)));
                            }
                        }
                    } else {
                        //If our pokemon expired lets remove the marker
                        if (pokemonsMarkerMap.get(pokemon) != null)
                            pokemonsMarkerMap.get(pokemon).remove();
                        //Then remove the pokemon
                        pokemonsMarkerMap.remove(pokemon);
                        //Finally lets remove him from our realm.
                        realm.beginTransaction();
                        realm.where(Pokemons.class).equalTo("encounterid", pokemon.getEncounterid()).findAll().deleteAllFromRealm();
                        realm.commitTransaction();
                    }
                } else {
                    //If our pokemon expired lets remove the marker
                    if (pokemonsMarkerMap.get(pokemon) != null)
                        pokemonsMarkerMap.get(pokemon).remove();
                    //Then remove the pokemon
                    pokemonsMarkerMap.remove(pokemon);
                }
            }
        }
    }
    public void refreshGymsAndPokestops() {
        //The the map bounds
        if (mMap != null) {
            LatLngBounds curScreen = mMap.getProjection().getVisibleRegion().latLngBounds;

            //Before we refresh we want to remove the old markers so lets do that first
            for (Map.Entry<Gym, Marker> gymMarker : gymMarkerMap.entrySet())
                gymMarker.getValue().remove();
            for (Map.Entry<PokeStop, Marker> pokestopMarker : pokestopMarkerMap.entrySet())
                pokestopMarker.getValue().remove();

            //Clear the hashmaps
            gymMarkerMap.clear();
            pokestopMarkerMap.clear();

            //Once we refresh our markers lets go ahead and load our pokemans
            ArrayList<Gym> gyms = new ArrayList<Gym>(realm.copyFromRealm(realm.where(Gym.class).findAll()));
            ArrayList<PokeStop> pokestops = new ArrayList<PokeStop>(realm.copyFromRealm(realm.where(PokeStop.class).findAll()));

            if (SettingsUtil.getSettings(MapsActivity.this).isGymsEnabled()) {
                for (int i = 0; i < gyms.size(); i++) {
                    Gym gym = gyms.get(i);
                    LatLng pos = new LatLng(gym.getLatitude(), gym.getLongitude());
                    if (curScreen.contains(pos) && !shouldGymBeRemoved(gym)) {
                        Marker marker = mMap.addMarker(gym.getMarker(this));
                        gymMarkerMap.put(gym, marker);
                    }
                }
            }

            boolean showAllStops = !Settings.get(this).isShowOnlyLured();

            if (SettingsUtil.getSettings(MapsActivity.this).isPokestopsEnabled()) {
                for (int i = 0; i < pokestops.size(); i++) {
                    PokeStop pokestop = pokestops.get(i);
                    LatLng pos = new LatLng(pokestop.getLatitude(), pokestop.getLongitude());
                    if (curScreen.contains(pos)) {
                        if (pokestop.isHasLureInfo() || showAllStops) {
                            Marker marker = mMap.addMarker(pokestop.getMarker(this));
                            pokestopMarkerMap.put(pokestop, marker);
                        }
                    }
                }
            }
        }
    }
    public void createBoundingBox() {
        if (SCANNING_STATUS) {
            if (scanMap.size() > 0) {
                if (mBoundingBox != null)
                    mBoundingBox.remove();

                LatLng loc = scanMap.get(0);

                //To create a circle we need to get the corners
                List<LatLng> corners = getCorners(scanMap);
                //Once we have the corners lets create two locations
                Location location = new Location("");
                //set the latitude/longitude
                location.setLatitude(corners.get(0).latitude);
                location.setLongitude(corners.get(0).longitude);

                Location location1 = new Location("");
                //set the laditude/longitude
                location1.setLatitude(loc.latitude);
                location1.setLongitude(loc.longitude);

                float distance = location.distanceTo(location1);

                mBoundingBox = mMap.addCircle(new CircleOptions().center(loc).radius(distance));
            }
        } else {
            if (currentCameraPos != null) {
                if (mBoundingBox != null) {
                    mBoundingBox.remove();
                }
                int scanDist = Settings.get(this).getScanValue();
                LatLng center = currentCameraPos.target;
                if (SettingsUtil.getSettings(this).isLockGpsEnabled()) {
                    center = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                }
                mBoundingBox = mMap.addCircle(new CircleOptions()
                        .center(center)
                        .radius(scanDist * 150)
                        .strokeWidth(5)
                        .strokeColor(Color.parseColor("#80d22d2d")));
            }
        }
    }
    public boolean shouldGymBeRemoved(Gym gym) {
        GymFilter currentGymFilter = GymFilter.getGymFilter(MapsActivity.this);
        int guardPokemonCp = gym.getGuardPokemonCp();
        int minCp = currentGymFilter.getGuardPokemonMinCp();
        int maxCp = currentGymFilter.getGuardPokemonMaxCp();
        if (!((guardPokemonCp >= minCp) && (guardPokemonCp <= maxCp)) && (guardPokemonCp != 0))
            return true;
        int ownedByTeamValue = gym.getOwnedByTeamValue();
        switch (ownedByTeamValue) {
            case 0:
                if (!currentGymFilter.isNeutralGymsEnabled())
                    return true;
                break;
            case 1:
                if (!currentGymFilter.isBlueGymsEnabled())
                    return true;
                break;
            case 2:
                if (!currentGymFilter.isRedGymsEnabled())
                    return true;
                break;
            case 3:
                if (!currentGymFilter.isYellowGymsEnabled())
                    return true;
                break;
        }
        return false;
    }
    public void createMapObjects() {
        if (SettingsUtil.getSettings(this).isBoundingBoxEnabled()) {
            createBoundingBox();
        } else {
            if (mBoundingBox != null) {
                mBoundingBox.remove();
                mBoundingBox = null;
            }
        }
    }
    public void startRefresher() {
        if (pokeonRefresher != null)
            pokeonRefresher.unsubscribe();
        if (gymstopRefresher != null)
            gymstopRefresher.unsubscribe();

        //Using RX java we setup an interval to refresh the map
        pokeonRefresher = Observable.interval(SettingsUtil.getSettings(this).getMapRefresh(), TimeUnit.SECONDS, AndroidSchedulers.mainThread())
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        //System.out.println("Refreshing Pokemons");
                        refreshMap();
                    }
                });

        gymstopRefresher = Observable.interval(30, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        //System.out.println("Refreshing Gyms");
                        refreshGymsAndPokestops();
                    }
                });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void forceRefreshEvent(ForceRefreshEvent event) {
        refreshGymsAndPokestops();
        refreshMap();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRestartRefreshEvent(RestartRefreshEvent event) {
        System.out.println(Settings.get(this).getServerRefresh());
        refreshGymsAndPokestops();
        refreshMap();
        startRefresher();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPublishProgressEvent(PublishProgressEvent event) {
        if (event.getProgress() != -1) {
            float progress = (float) event.getProgress() * 100 / scanMap.size();
            progressBar.setProgress((int) progress);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onForceLogoutEvent(ForceLogoutEvent event) {
        showToast(R.string.LOGOUT_ERROR);
        logOut();
    }

    public boolean doWeHavePermission() {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (pokemonsMarkerMap != null)
            pokemonsMarkerMap.clear();
        if (mMap != null)
            mMap.clear();
        forceRefreshEvent(new ForceRefreshEvent());
        onRestartRefreshEvent(new RestartRefreshEvent());
        realm = Realm.getDefaultInstance();
    }

    @Override
    public void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        realm.close();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        pokeonRefresher.unsubscribe();
        gymstopRefresher.unsubscribe();
        super.onPause();
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        currentCameraPos = cameraPosition;
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {

    }

    private void setAdapterAndListener(final Object markerKey) {
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(final Marker marker) {
                LinearLayout info = new LinearLayout(MapsActivity.this);
                info.setOrientation(LinearLayout.VERTICAL);

                TextView title = new TextView(MapsActivity.this);
                title.setTextColor(Color.BLACK);
                title.setGravity(Gravity.CENTER);
                title.setTypeface(null, Typeface.BOLD);
                title.setText(marker.getTitle());

                TextView snippet = new TextView(MapsActivity.this);
                snippet.setTextColor(Color.GRAY);
                snippet.setGravity(Gravity.CENTER);
                if(markerKey instanceof Pokemons){
                    snippet.setText(MapsActivity.this.getText(R.string.expires_in) + DrawableUtils.getExpireTime(((Pokemons) markerKey).getExpires()));
                }else{
                    snippet.setText(marker.getSnippet());
                }

                TextView navigate = new TextView(MapsActivity.this);
                navigate.setTextColor(Color.GRAY);
                navigate.setGravity(Gravity.CENTER);
                navigate.setText(getText(R.string.click_open_in_gmaps));

                info.addView(title);
                info.addView(snippet);
                info.addView(navigate);

                return info;
            }
        });

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener(){
            @Override
            public void onInfoWindowClick(Marker marker) {
                Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + marker.getPosition().latitude + "," + marker.getPosition().longitude + "(" + marker.getTitle() + ")");
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                if (mapIntent.resolveActivity(MapsActivity.this.getPackageManager()) != null) {
                    MapsActivity.this.startActivity(mapIntent);
                }
            }
        });
    }

    private void removeAdapterAndListener() {
        mMap.setInfoWindowAdapter(null);
        mMap.setOnInfoWindowClickListener(null);
    }
}
