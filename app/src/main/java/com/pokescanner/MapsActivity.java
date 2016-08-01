package com.pokescanner;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.design.widget.FloatingActionButton;
import android.support.multidex.MultiDex;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;
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
import com.pokescanner.events.ForceLogOutEvent;
import com.pokescanner.events.ForceRefreshEvent;
import com.pokescanner.events.InterruptedExecptionEvent;
import com.pokescanner.events.LoginFailedExceptionEvent;
import com.pokescanner.events.RemoteServerExceptionEvent;
import com.pokescanner.events.RestartRefreshEvent;
import com.pokescanner.events.ScanCircleEvent;
import com.pokescanner.exceptions.NoCameraPositionException;
import com.pokescanner.exceptions.NoMapException;
import com.pokescanner.helper.CustomMapFragment;
import com.pokescanner.helper.GymFilter;
import com.pokescanner.helper.PokeDistanceSorter;
import com.pokescanner.helper.PokemonListLoader;
import com.pokescanner.loaders.MultiAccountLoader;
import com.pokescanner.objects.FilterItem;
import com.pokescanner.objects.Gym;
import com.pokescanner.objects.PokeStop;
import com.pokescanner.objects.Pokemons;
import com.pokescanner.objects.User;
import com.pokescanner.recycler.ListViewRecyclerAdapter;
import com.pokescanner.settings.Settings;
import com.pokescanner.settings.SettingsActivity;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

import static com.pokescanner.helper.Generation.getCorners;
import static com.pokescanner.helper.Generation.makeHexScanMap;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnCameraChangeListener,
        OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    @BindView(R.id.btnSearch)
    FloatingActionButton button;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    private GoogleMap mMap;
    @BindView(R.id.btnSettings)
    ImageButton btnSettings;
    @BindView(R.id.main)
    RelativeLayout main;
    @BindView(R.id.btnSataliteMode)
    com.github.clans.fab.FloatingActionButton btnSataliteMode;
    @BindView(R.id.floatActionMenu)
    FloatingActionMenu floatingActionMenu;


    LocationManager locationManager;

    User user;
    Realm realm;

    private GoogleApiClient mGoogleApiClient;
    List<LatLng> scanMap = new ArrayList<>();

    private Map<Pokemons, Marker> pokemonsMarkerMap = new HashMap<>();
    private Map<Gym, Marker> gymMarkerMap = new HashMap<>();
    private Map<PokeStop, Marker> pokestopMarkerMap = new HashMap<>();
    private ArrayList<Circle> circleArray = new ArrayList<>();

    Circle mBoundingBox = null;

    int pos = 1;
    //Used for determining Scan status
    boolean SCANNING_STATUS = false;
    boolean LIST_MODE = false;
    //Used for our refreshing of the map
    Subscription pokeonRefresher;
    Subscription gymstopRefresher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MultiDex.install(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        ButterKnife.bind(this);
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder(this)
                .name(Realm.DEFAULT_REALM_NAME)
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(realmConfiguration);
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

    public LatLng getCameraLocation() throws NoMapException, NoCameraPositionException {
        if (mMap != null) {
            if (mMap.getCameraPosition() != null) {
                return mMap.getCameraPosition().target;
            }
            throw new NoCameraPositionException();
        }
        throw new NoMapException();
    }

    @SuppressWarnings({"MissingPermission"})
    public LatLng getCurrentLocation() {
        if (PermissionUtils.doWeHaveGPSandLOC(this)) {
            if (mGoogleApiClient.isConnected()) {
                Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                if (location != null) {
                    return new LatLng(location.getLatitude(), location.getLongitude());
                }
                return null;
            }
            return null;
        }
        return null;
    }

    @OnClick(R.id.btnSearch)
    public void PokeScan() {
        if (SCANNING_STATUS) {
            stopPokeScan();
        } else {
            //Progress Bar Related Stuff
            pos = 1;
            progressBar.setProgress(0);
            int scanValue = Settings.get(this).getScanValue();
            showProgressbar(true);
            //get our camera position
            LatLng scanPosition = null;

            //Try to get the camera position
            try {
                scanPosition = getCameraLocation();
            } catch (NoMapException | NoCameraPositionException e) {
                showToast(R.string.SCAN_FAILED);
                e.printStackTrace();
            }

            if (SettingsUtil.getSettings(MapsActivity.this).isDrivingModeEnabled() && moveCameraToCurrentPosition()) {
                scanPosition = getCurrentLocation();
            }

            if (scanPosition != null) {
                scanMap = makeHexScanMap(scanPosition, scanValue, 1, new ArrayList<LatLng>());
                if (scanMap != null) {
                    //Pull our users from the realm
                    ArrayList<User> users = new ArrayList<>(realm.copyFromRealm(realm.where(User.class).findAll()));

                    MultiAccountLoader.setSleepTime(UiUtils.BASE_DELAY);
                    //Set our map
                    MultiAccountLoader.setScanMap(scanMap);
                    //Set our users
                    MultiAccountLoader.setUsers(users);
                    //Begin our threads???
                    MultiAccountLoader.startThreads();
                } else {
                    showToast(R.string.SCAN_FAILED);
                    showProgressbar(false);
                }
            } else {
                showToast(R.string.SCAN_FAILED);
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
        MultiAccountLoader.cancelAllThreads();
        if (!MultiAccountLoader.areThreadsRunning()) {
            showProgressbar(false);
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

    public void showProgressbar(boolean status) {
        if (status) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            progressBar.setVisibility(View.VISIBLE);
            button.setImageDrawable(ContextCompat.getDrawable(MapsActivity.this, R.drawable.ic_pause_white_24dp));
            SCANNING_STATUS = true;
        } else {
            removeCircleArray();
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            progressBar.setVisibility(View.INVISIBLE);
            button.setImageDrawable(ContextCompat.getDrawable(MapsActivity.this, R.drawable.ic_track_changes_white_24dp));
            SCANNING_STATUS = false;
        }
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

        if (!MultiAccountLoader.areThreadsRunning()) {
            System.out.println("Threads");
            showProgressbar(false);
        }

        if (!LIST_MODE) {
            if (mMap != null) {
                LatLngBounds curScreen = mMap.getProjection().getVisibleRegion().latLngBounds;
                createMapObjects();

                //If in driving mode, move camera to current location
                if (SettingsUtil.getSettings(MapsActivity.this).isDrivingModeEnabled())
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
                                    //UNTESTED
                                    if (marker != null) {
                                        marker = pokemon.updateMarker(marker, this);
                                    }
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
    }

    public void refreshGymsAndPokestops() {
        if (!LIST_MODE) {
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
    }

    public void createBoundingBox() {
        if (SCANNING_STATUS) {
            if (scanMap.size() > 0) {
                removeBoundingBox();

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

                mBoundingBox = mMap.addCircle(new CircleOptions()
                        .center(loc)
                        .strokeColor(Color.parseColor("#80d22d2d"))
                        .radius(distance));
            }
        } else {
            LatLng currentCameraPos = null;
            try {
                currentCameraPos = getCameraLocation();

                if (currentCameraPos != null) {

                    removeBoundingBox();

                    int scanDist = Settings.get(this).getScanValue();

                    LatLng center = currentCameraPos;

                    if (SettingsUtil.getSettings(this).isDrivingModeEnabled()) {
                        LatLng latLng = getCurrentLocation();
                        if (latLng != null) {
                            center = new LatLng(latLng.latitude, latLng.longitude);
                        }
                    }

                    mBoundingBox = mMap.addCircle(new CircleOptions()
                            .center(center)
                            .radius(scanDist * 100)
                            .strokeWidth(5)
                            .strokeColor(Color.parseColor("#80d22d2d")));
                }
            } catch (NoMapException e) {
                e.printStackTrace();
            } catch (NoCameraPositionException e) {
                e.printStackTrace();
            }
        }
    }

    public void removeBoundingBox() {
        if (mBoundingBox != null)
            mBoundingBox.remove();
    }

    public void removeCircleArray() {
        if (circleArray != null)
        {
            for(Circle circle: circleArray) {
                circle.remove();
            }

            circleArray.clear();
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
            removeBoundingBox();
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
    public void createCircle(ScanCircleEvent event) {
        if (event.pos != null)
        {
            CircleOptions circleOptions = new CircleOptions()
                    .radius(70)
                    .strokeWidth(0)
                    .fillColor(ResourcesCompat.getColor(getResources(),R.color.colorPrimaryTransparent,null))
                    .center(event.pos);
            circleArray.add(mMap.addCircle(circleOptions));

            float progress = (float) pos++ * 100 / scanMap.size();
            progressBar.setProgress((int) progress);
            if((int) progress == 100) {
                showProgressbar(false);
            }
        }
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
    public void onThreadInterruptedEvent(InterruptedExecptionEvent event) {
        showToast(R.string.ERROR_THREAD);
        showProgressbar(false);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRemoteServerFailedEvent(RemoteServerExceptionEvent event) {
        showToast(R.string.ERROR_SERVER);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUserAuthFailedEvent(LoginFailedExceptionEvent event) {
        showToast(R.string.ERROR_LOGIN);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onForceLogOutEvent(ForceLogOutEvent event) {
        showToast(R.string.ERROR_LOGIN);
        logOut();
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
        createBoundingBox();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {

    }

    @Override

    public void onConnected(Bundle connectionHint) {
    }

    @Override
    public void onConnectionSuspended(int cause) {

    }

    @OnClick(R.id.btnListMode)
    public void listModeDialog() {
        floatingActionMenu.close(false);
        //CREATE TWO LIST (THIS IS A GHETTO SOLUTION BOYS)
        ArrayList<Pokemons> pokelist = new ArrayList<>(realm.copyFromRealm(realm.where(Pokemons.class).findAll()));
        ArrayList<Pokemons> listout = new ArrayList<>();

        LatLng latlng = getCurrentLocation();

        if (latlng == null) {
            try {
                latlng = getCameraLocation();
            } catch (NoMapException e) {
                e.printStackTrace();
            } catch (NoCameraPositionException e) {
                e.printStackTrace();
            }
        }

        if (latlng != null) {
            Location location = new Location("");
            location.setLatitude(latlng.latitude);
            location.setLongitude(latlng.longitude);
            //Write distance to pokemons
            for (int i = 0; i < pokelist.size(); i++) {
                Pokemons pokemons = pokelist.get(i);
                //IF OUR POKEMANS IS FILTERED WE AINT SHOWIN HIM
                if (!PokemonListLoader.getFilteredList().contains(new FilterItem(pokemons.getNumber()))) {
                    //DO MATH
                    Location temp = new Location("");

                    temp.setLatitude(pokemons.getLatitude());
                    temp.setLongitude(pokemons.getLongitude());

                    double distance = location.distanceTo(temp);
                    pokemons.setDistance(distance);

                    //ADD OUR POKEMANS TO OUR OUT LIST
                    listout.add(pokemons);
                }
            }
        }

        Collections.sort(listout,new PokeDistanceSorter());

        if (listout.size() > 0) {
            LayoutInflater inflater = getLayoutInflater();
            final View dialoglayout = inflater.inflate(R.layout.dialog_list_view, null);
            final AlertDialog builder = new AlertDialog.Builder(this).create();
            builder.setView(dialoglayout);


            RecyclerView recyclerView = (RecyclerView) dialoglayout.findViewById(R.id.recyclerListView);
            recyclerView.setHasFixedSize(true);
            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(mLayoutManager);

            RecyclerView.Adapter mAdapter = new ListViewRecyclerAdapter(listout, new ListViewRecyclerAdapter.OnClickListener() {
                @Override
                public void onClick(Pokemons pokemons) {
                    if (mMap != null) {
                        LatLng pos = new LatLng(pokemons.getLatitude(), pokemons.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 20));
                        builder.dismiss();
                    }
                }
            });

            recyclerView.setAdapter(mAdapter);

            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    LIST_MODE = false;
                }
            });

            builder.show();
            LIST_MODE = true;

            //Were going to clear the map to reduce lag
            pokemonsMarkerMap = new ArrayMap<Pokemons, Marker>();
            pokestopMarkerMap = new ArrayMap<PokeStop, Marker>();
            gymMarkerMap = new ArrayMap<Gym, Marker>();
            mMap.clear();
        }
    }
    @OnClick(R.id.btnAddressSearch)
    public void searchAddressDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View dialoglayout = inflater.inflate(R.layout.dialog_search_address, null);
        final AlertDialog builder = new AlertDialog.Builder(this).create();

        final EditText etAddress = (EditText) dialoglayout.findViewById(R.id.etAddress);
        Button btnSearch = (Button) dialoglayout.findViewById(R.id.btnSearch);
        Button btnCancel = (Button) dialoglayout.findViewById(R.id.btnCancel);


        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String addy = etAddress.getText().toString();
                if (addy.length() > 0) {
                    Geocoder geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());
                    try {
                        List<Address> addresses = geocoder.getFromLocationName(addy, 10);
                        if (addresses != null) {
                            if (addresses.size() > 0) {
                                Address address = addresses.get(0);
                                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                                builder.dismiss();
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    etAddress.setError("Cannot be empty");
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                builder.dismiss();
            }
        });

        builder.setView(dialoglayout);
        builder.show();
        floatingActionMenu.close(false);
    }
    @OnClick(R.id.btnSataliteMode)
    public void toggleMapType() {
        if(mMap != null) {
            if(mMap.getMapType() == GoogleMap.MAP_TYPE_NORMAL) {
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                btnSataliteMode.setImageResource(R.drawable.ic_map_white_24dp);
            }
            else {
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                btnSataliteMode.setImageResource(R.drawable.ic_satellite_white_24dp);
            }
        }
        floatingActionMenu.close(true);
    }
    @OnClick(R.id.btnCenterCamera)
    public void onLocateMeClick() {
        moveCameraToCurrentPosition();
        floatingActionMenu.close(true);
    }
    @Override
    @SuppressWarnings({"MissingPermission"})
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        System.out.println("Map ready");
        if (PermissionUtils.doWeHaveGPSandLOC(this)) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMapToolbarEnabled(false);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            moveCameraToCurrentPosition();
        }
        mMap.setOnCameraChangeListener(this);
        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
                Pokemons deletePokemon = null;
                if (pokemonsMarkerMap.containsValue(marker)) {
                    for (Map.Entry<Pokemons, Marker> e : pokemonsMarkerMap.entrySet()) {
                        if (e.getValue().equals(marker)) {
                            deletePokemon = e.getKey();
                        }
                    }
                    if (deletePokemon != null) {
                        final Pokemons finalDeletePokemon = deletePokemon;
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                if (realm.where(Pokemons.class).equalTo("encounterid", finalDeletePokemon.getEncounterid()).findAll().deleteAllFromRealm()) {
                                    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                                    v.vibrate(50);
                                    pokemonsMarkerMap.get(finalDeletePokemon).remove();
                                    pokemonsMarkerMap.remove(finalDeletePokemon);
                                }
                            }
                        });
                    }
                }
            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {

            }
        });
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
                        MarkerDetails.showMarkerDetailsDialog(MapsActivity.this, markerKey);
                    } else {
                        setAdapterAndListener(markerKey);
                        marker.showInfoWindow();
                    }
                }
                return false;
            }
        });
        startRefresher();
    }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        cleanEntireMap();
    }
    public boolean moveCameraToCurrentPosition() {
        LatLng GPS_LOCATION = getCurrentLocation();
        if (GPS_LOCATION != null) {
            if (mMap != null) {
                this.mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(GPS_LOCATION, 15));
                return true;
            } else {
                return false;
            }
        } else {
            //Try again after half a second
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    moveCameraToCurrentPosition();
                }
            }, 500);
        }
        return false;
    }
    @OnClick(R.id.btnClear)
    public void cleanPokemon(){
        if (mMap != null) {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.where(Pokemons.class).findAll().deleteAllFromRealm();

                    pokemonsMarkerMap = new ArrayMap<Pokemons, Marker>();

                    mMap.clear();
                    showToast(R.string.cleared_map);
                }
            });

            forceRefreshEvent(new ForceRefreshEvent());
        }
    }
    @OnLongClick(R.id.btnClear)
    public boolean cleanEntireMap(){
        if (mMap != null) {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.where(Pokemons.class).findAll().deleteAllFromRealm();
                    realm.where(Gym.class).findAll().deleteAllFromRealm();
                    realm.where(PokeStop.class).findAll().deleteAllFromRealm();

                    pokemonsMarkerMap = new ArrayMap<Pokemons, Marker>();
                    pokestopMarkerMap = new ArrayMap<PokeStop, Marker>();
                    gymMarkerMap = new ArrayMap<Gym, Marker>();

                    mMap.clear();
                    showToast(R.string.cleared_map);

                }
            });
            forceRefreshEvent(new ForceRefreshEvent());
        }
        return true;
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
                if (markerKey instanceof Pokemons) {
                    snippet.setText(MapsActivity.this.getText(R.string.expires_in) + DrawableUtils.getExpireTime(((Pokemons) markerKey).getExpires()));
                } else {
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

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
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
