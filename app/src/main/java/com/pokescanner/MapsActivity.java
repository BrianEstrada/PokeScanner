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
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.multidex.MultiDex;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.pokescanner.events.AppUpdateEvent;
import com.pokescanner.events.ForceLogoutEvent;
import com.pokescanner.events.ForceRefreshEvent;
import com.pokescanner.events.PublishProgressEvent;
import com.pokescanner.events.RestartRefreshEvent;
import com.pokescanner.helper.CustomMapFragment;
import com.pokescanner.helper.GymFilter;
import com.pokescanner.helper.PokemonListLoader;
import com.pokescanner.helper.Settings;
import com.pokescanner.loaders.MapObjectsLoader;
import com.pokescanner.objects.FilterItem;
import com.pokescanner.objects.Gym;
import com.pokescanner.objects.PokeStop;
import com.pokescanner.objects.Pokemons;
import com.pokescanner.objects.User;
import com.pokescanner.updater.AppUpdateDialog;
import com.pokescanner.updater.AppUpdateLoader;
import com.pokescanner.utils.LocationUtils;
import com.pokescanner.utils.SettingsUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.joda.time.Instant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

import static com.pokescanner.helper.Generation.getCorners;
import static com.pokescanner.helper.Generation.makeHexScanMap;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnCameraChangeListener {

    FloatingActionButton button;
    ProgressBar progressBar;
    private GoogleMap mMap;
    ImageButton btnSettings;
    Toolbar toolbar;
    RelativeLayout main;

    LocationManager locationManager;
    Location currentLocation;
    CameraPosition currentCameraPos;

    User user;
    Realm realm;

    List<LatLng> scanMap = new ArrayList<>();
    ArrayList<FilterItem> filterItems = new ArrayList<>();

    private Map<Pokemons, Marker> pokemonsMarkerMap = new HashMap<Pokemons, Marker>();
    private ArrayList<Marker> locationMarkers = new ArrayList<>();
    Circle mBoundingBox = null;

    PokemonListLoader pokemonListLoader;
    SharedPreferences sharedPreferences;
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

        realm = Realm.getDefaultInstance();

        //So if our realm has no users then we'll send our user back to the login screen
        //otherwise set our user and move on!
        if (realm.where(User.class).findAll().size() != 0) {
            user = realm.copyFromRealm(realm.where(User.class).findFirst());
        } else {
            Toast.makeText(MapsActivity.this, "No login!", Toast.LENGTH_SHORT).show();
            logOut();
        }

        //Load our shared prefs for our scan value
        sharedPreferences = getSharedPreferences(getString(R.string.shared_key), Context.MODE_PRIVATE);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (CustomMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Start our location manager so we can center our map
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //This class is used to load and save our filters
        pokemonListLoader = new PokemonListLoader(this);
        //load our main layout
        main = (RelativeLayout) findViewById(R.id.main);

        try {
            //let's try and load our filters
            filterItems.addAll(pokemonListLoader.getPokelist());
        } catch (IOException e) {
            showToast(R.string.ERROR_FILTERS);
            e.printStackTrace();
        }

        button = (FloatingActionButton) findViewById(R.id.btnSearch);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PokeScan();
            }
        });

        btnSettings = (ImageButton) findViewById(R.id.btnSettings);
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent settingsIntent = new Intent(MapsActivity.this, SettingsActivity.class);
                startActivity(settingsIntent);
            }
        });
    }

    public void PokeScan() {
        if (SCANNING_STATUS) {
            stopPokeScan();
        } else {
            pos = 1;
            //Load our scan value
            int scanValue = sharedPreferences.getInt(SettingsUtil.SCAN_VALUE, 5);
            //Our refresh rate to Milliseconds
            int millis = SettingsUtil.getSettings(this).getServerRefresh() * 1000;

            showProgressbar(true);
            progressBar.setProgress(0);

            LatLng pos = mMap.getCameraPosition().target;

            if (SettingsUtil.getSettings(this).isLockGpsEnabled() && centerCamera()) {
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

    private void stopPokeScan() {
        try {
            mapObjectsLoader.interrupt();
            mapObjectsLoader.join(500);
            showProgressbar(false);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void showToast(int resString) {
        Toast.makeText(MapsActivity.this, getString(resString), Toast.LENGTH_SHORT).show();
    }

    public void createBoundingBoxPreview() {
        if (currentCameraPos != null) {
            if (mBoundingBox != null) {
                mBoundingBox.remove();
            }
            int scanDist = Settings.get(this).getScanValue();
            mBoundingBox = mMap.addCircle(new CircleOptions()
                    .center(currentCameraPos.target)
                    .radius(scanDist * 150)
                    .strokeWidth(5)
                    .strokeColor(Color.parseColor("#80d22d2d")));
        }
    }

    public void createBoundingBox() {
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
            //mMap.addPolygon(new PolygonOptions().addAll(getCorners(scanMap)));
        }
    }

    @Override
    @SuppressWarnings({"MissingPermission"})
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (LocationUtils.doWeHaveGPSandLOC(this)) {
            //Set our map stuff
            mMap.setMyLocationEnabled(true);
            mMap.setOnCameraChangeListener(this);
            //Let's find our location and set it!
            mMap.getUiSettings().setMapToolbarEnabled(true);
            mMap.getUiSettings().setCompassEnabled(false);
            //Center camera function
            centerCamera();
            startRefresher();
        }
    }

    @SuppressWarnings({"MissingPermission"})
    public boolean centerCamera() {
        if (LocationUtils.doWeHaveGPSandLOC(this)) {
            Criteria criteria = new Criteria();
            String provider = locationManager.getBestProvider(criteria, true);
            currentLocation = locationManager.getLastKnownLocation(provider);

            if (currentLocation != null) {
                LatLng target = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                CameraPosition position = this.mMap.getCameraPosition();

                CameraPosition.Builder builder = new CameraPosition.Builder();
                builder.zoom(15);
                builder.target(target);

                this.mMap.animateCamera(CameraUpdateFactory.newCameraPosition(builder.build()));
                return true;
            }
            showToast(R.string.CAMERA_CENTER_FAILED);
            return false;
        }
        showToast(R.string.CAMERA_CENTER_FAILED);
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

    public void reloadFilters() {
        filterItems.clear();
        filterItems.addAll(pokemonListLoader.getFilteredList());
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

            //load our array
            ArrayList<Pokemons> pokemons = new ArrayList<Pokemons>(realm.copyFromRealm(realm.where(Pokemons.class).findAll()));

            //get our icon scale from our settings
            int scale = SettingsUtil.getSettings(this).getScale();

            //Okay so we're going to fix the annoying issue where the markers were being constantly redrawn
            for (int i = 0; i < pokemons.size(); i++) {
                //Get our pokemon from the list
                Pokemons pokemon = pokemons.get(i);
                //Is our pokemon contained within the bounds of the camera?
                if (curScreen.contains(new LatLng(pokemon.getLatitude(), pokemon.getLongitude()))) {
                    //If yes then has he expired?
                    if (pokemon.getDate().isAfter(new Instant())) {
                        if (filterItems.contains(new FilterItem(pokemon.getNumber()))) {
                            if (pokemonsMarkerMap.containsKey(pokemon))
                            {
                                Marker marker = pokemonsMarkerMap.get(pokemon);
                                if (marker != null)
                                {
                                    marker.remove();
                                    pokemonsMarkerMap.remove(pokemon);
                                }
                            }
                            System.out.println("Filtered");
                        } else {
                            //Okay finally is he contained within our hashmap?
                            if (pokemonsMarkerMap.containsKey(pokemon)) {
                                //Well if he is then lets pull out our marker.
                                Marker marker = pokemonsMarkerMap.get(pokemon);
                                //Update our icon
                                marker.setIcon(BitmapDescriptorFactory.fromBitmap(pokemon.getBitmap(this, scale)));
                                //Update the snippet
                                marker.setSnippet(pokemon.getExpireTime());
                                //Was our marker window open when we updated?
                                if (marker.isInfoWindowShown()) {
                                    //Alright lets redraw it!
                                    marker.showInfoWindow();
                                }
                            } else {
                                //If our pokemon wasn't in our hashmap lets add him
                                pokemonsMarkerMap.put(pokemon, mMap.addMarker(pokemon.getMarker(this, scale)));
                            }
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
            }
        }
    }

    public void refreshGyms() {
        //The the map bounds
        if (mMap != null) {
            LatLngBounds curScreen = mMap.getProjection().getVisibleRegion().latLngBounds;

            //Before we refresh we want to remove the old markets so lets do that first
            for (Marker marker : locationMarkers) {
                marker.remove();
            }
            //Clear our array
            locationMarkers.clear();
            //Once we refresh our markers lets go ahead and load our pokemans
            ArrayList<Gym> gyms = new ArrayList<Gym>(realm.copyFromRealm(realm.where(Gym.class).findAll()));
            ArrayList<PokeStop> pokestops = new ArrayList<PokeStop>(realm.copyFromRealm(realm.where(PokeStop.class).findAll()));

            if (SettingsUtil.getSettings(MapsActivity.this).isGymsEnabled()) {
                for (int i = 0; i < gyms.size(); i++) {
                    Gym gym = gyms.get(i);
                    LatLng pos = new LatLng(gym.getLatitude(), gym.getLongitude());
                    if (curScreen.contains(pos) && !shouldGymBeRemoved(gym)) {
                        locationMarkers.add(mMap.addMarker(gym.getMarker(this)));
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
                            locationMarkers.add(mMap.addMarker(pokestop.getMarker(this)));
                        }
                    }
                }
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
            if (SCANNING_STATUS) {
                createBoundingBox();
            } else {
                if (Settings.get(this).isBoundingBoxEnabled()) {
                    createBoundingBoxPreview();
                }
            }
        } else {
            if (mBoundingBox != null) {
                mBoundingBox.remove();
                mBoundingBox = null;
            }
        }
        //createMarkerList();
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
                        refreshGyms();
                    }
                });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void forceRefreshEvent(ForceRefreshEvent event) {
        refreshGyms();
        refreshMap();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRestartRefreshEvent(RestartRefreshEvent event) {
        System.out.println(Settings.get(this).getServerRefresh());
        refreshGyms();
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

    @Subscribe (threadMode = ThreadMode.MAIN)
    public void onAppUpdateEvent(AppUpdateEvent event) {
        switch (event.getStatus()) {
            case AppUpdateEvent.OK:
                new AppUpdateDialog(MapsActivity.this, event.getAppUpdate());
                break;
            case AppUpdateEvent.FAILED:
                showToast(R.string.update_check_failed);
                break;
        }
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
        reloadFilters();
        new AppUpdateLoader().start();
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

    public void popupMenu() {
        PopupMenu popup = new PopupMenu(MapsActivity.this, btnSettings);
        popup.getMenuInflater().inflate(R.menu.main_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_settings:
                        Intent settingsIntent = new Intent(MapsActivity.this, SettingsActivity.class);
                        startActivity(settingsIntent);
                        break;
                    case R.id.action_logout:
                        logOut();
                        break;
                }
                return true;
            }
        });
        popup.show();
    }
}
