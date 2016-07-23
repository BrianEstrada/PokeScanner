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

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.multidex.MultiDex;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.pokescanner.helper.PokemonListLoader;
import com.pokescanner.loaders.MapObjectsLoadedEvent;
import com.pokescanner.loaders.MapObjectsLoader;
import com.pokescanner.objects.FilterItem;
import com.pokescanner.objects.Gym;
import com.pokescanner.objects.MenuItem;
import com.pokescanner.objects.PokeStop;
import com.pokescanner.objects.Pokemons;
import com.pokescanner.objects.User;
import com.pokescanner.recycler.MenuRecycler;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import POGOProtos.Map.Fort.FortDataOuterClass;
import POGOProtos.Map.Pokemon.MapPokemonOuterClass;
import io.realm.Realm;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

import static com.pokescanner.helper.Generation.getCorners;
import static com.pokescanner.helper.Generation.hexagonal_number;
import static com.pokescanner.helper.Generation.makeHexScanMap;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnCameraChangeListener {

    Button button;
    ProgressBar progressBar;
    private GoogleMap mMap;

    LocationManager locationManager;
    Location currentLocation;
    ImageButton imageButton;

    User user;
    Realm realm;

    List<LatLng> scanMap = new ArrayList<>();
    ArrayList<FilterItem> filterItems = new ArrayList<>();
    PokemonListLoader pokemonListLoader;
    SharedPreferences sharedPreferences;
    private MapObjectsLoader mapObjectsLoader;

    int pos = 1;
    //Used for determining Scan status
    boolean SCANNING_STATUS = false;
    //Default size for our scan grid
    int scanValue = 5;
    //Used for our refreshing of the map
    Subscription refresher;

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
        sharedPreferences = getSharedPreferences(getString(R.string.shared_key),Context.MODE_PRIVATE);
        scanValue = sharedPreferences.getInt("scanvalue",5);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Start our location manager so we can center our map
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //This class is used to load and save our filters
        pokemonListLoader = new PokemonListLoader(this);

        try {
            //let's try and load our filters
            filterItems.addAll(pokemonListLoader.getPokelist());
        } catch (IOException e) {
            showToast(R.string.ERROR_FILTERS);
            e.printStackTrace();
        }

        imageButton = (ImageButton) findViewById(R.id.imageButton);
        button = (Button) findViewById(R.id.btnSearch);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PokeScan();
            }
        });

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMenu();
            }
        });
    }
    public void PokeScan() {
        if (SCANNING_STATUS) {
            stopPokeScan();
        }else
        {
            pos = 1;
            //Our refresh rate to Milliseconds
            int millis = SettingsController.getSettings(this).getServerRefresh() * 1000;
            showProgressbar(true);
            progressBar.setProgress(0);
            scanMap = makeHexScanMap(mMap.getCameraPosition().target, scanValue, 1, new ArrayList<LatLng>());
            mapObjectsLoader = new MapObjectsLoader(user, scanMap,millis);
            mapObjectsLoader.start();
        }
    }
    private void stopPokeScan() {
        try{
            mapObjectsLoader.interrupt();
            mapObjectsLoader.join(500);
            showProgressbar(false);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void mapObjectsLoaded(MapObjectsLoadedEvent event) {
        progressBar.setProgress((pos* 100)/scanMap.size());

        final Collection<MapPokemonOuterClass.MapPokemon> collectionPokemon = event.getMapObjects().getCatchablePokemons();
        final Collection<FortDataOuterClass.FortData> collectionGyms = event.getMapObjects().getGyms();
        final Collection<FortDataOuterClass.FortData> collectionPokeStops = event.getMapObjects().getPokestops();

        if ((collectionPokemon != null) && (collectionGyms != null) && (collectionPokeStops != null)) {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm)
                {
                    for (MapPokemonOuterClass.MapPokemon pokemonOut: collectionPokemon)
                        realm.copyToRealmOrUpdate(new Pokemons(pokemonOut));

                    for (FortDataOuterClass.FortData gymOut : collectionGyms)
                        realm.copyToRealmOrUpdate(new Gym(gymOut));

                    for(FortDataOuterClass.FortData pokestopOut : collectionPokeStops)
                        realm.copyToRealmOrUpdate(new PokeStop(pokestopOut));
                }
            });
        }else {
            showToast(R.string.SERVER_FAILED);
        }

        if (pos==(scanMap.size())) {
            showProgressbar(false);
        }
        
        pos++;
    }
    public void showToast(int resString) {
        Toast.makeText(MapsActivity.this, getString(resString), Toast.LENGTH_SHORT).show();
    }
    public void createBoundingBox() {
        if (scanMap.size()>0) {
            //To create a circle we need to get the corners
            List<LatLng> corners = getCorners(scanMap);
            //Once we have the corners lets create two locations
            Location location = new Location("");
            //set the latitude/longitude
            location.setLatitude(corners.get(0).latitude);
            location.setLongitude(corners.get(0).longitude);

            Location location1 = new Location("");
            //set the laditude/longitude
            location1.setLatitude(scanMap.get(0).latitude);
            location1.setLongitude(scanMap.get(0).longitude);

            float distance = location.distanceTo(location1);

            mMap.addCircle(new CircleOptions().center(scanMap.get(0)).radius(distance));
            //mMap.addPolygon(new PolygonOptions().addAll(getCorners(scanMap)));
        }
    }
    public void createMarkerList() {
        if(BuildConfig.DEBUG){
            for (LatLng temp: scanMap) {
                mMap.addMarker(new MarkerOptions().position(temp));
            }
        }
    }
    @Override
    @SuppressWarnings({"MissingPermission"})
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (doWeHavePermission()) {
            //Set our map stuff
            mMap.setMyLocationEnabled(true);
            mMap.setOnCameraChangeListener(this);

            //Let's find our location and set it!
            Criteria criteria = new Criteria();
            String provider = locationManager.getBestProvider(criteria, true);
            currentLocation = locationManager.getLastKnownLocation(provider);

            //Center camera function
            centerCamera();
            startRefresher();
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
            button.setText(getString(R.string.cancel));
            SCANNING_STATUS = true;
        } else {
            progressBar.setVisibility(View.INVISIBLE);
            button.setText(getString(R.string.scan));
            SCANNING_STATUS = false;
        }
    }
    //I don't think we're using this
    public boolean isGPSEnabled() {
        LocationManager cm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return cm.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }
    //Menu Related Functions
    public void showMenu() {
        ArrayList<MenuItem> items = new ArrayList<>();

        items.add(new MenuItem(getString(R.string.search_radius),0,null));
        items.add(new MenuItem(getString(R.string.filter),1,null));
        items.add(new MenuItem(getString(R.string.settings),2,null));
        items.add(new MenuItem(getString(R.string.logout),3,null));

        final RecyclerView.Adapter mAdapter;
        RecyclerView.LayoutManager mLayoutManager;

        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        dialog.setContentView(R.layout.dialog_menu);


        final RecyclerView invoicesRecycler = (RecyclerView) dialog.findViewById(R.id.recycler);
        invoicesRecycler.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        invoicesRecycler.setLayoutManager(mLayoutManager);

        mAdapter = new MenuRecycler(items, new MenuRecycler.onItemClickListener() {
            @Override
            public void onItemClick(MenuItem item) {
                switch(item.getAction()){
                    case 0:
                        searchRadiusDialog();
                        dialog.dismiss();
                        break;
                    case 1:
                        startDialogActivity();
                        dialog.dismiss();
                        break;
                    case 2:
                        dialog.dismiss();
                        SettingsController.showSettingDialog(MapsActivity.this);
                        break;
                    case 3:
                        logOut();
                        break;
                }

            }
        });

        invoicesRecycler.setAdapter(mAdapter);

        dialog.show();
    }
    public void searchRadiusDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_search_radius);

        final SeekBar seekBar = (SeekBar) dialog.findViewById(R.id.seekBar);
        Button btnSave = (Button) dialog.findViewById(R.id.btnAccept);
        Button btnCancel = (Button) dialog.findViewById(R.id.btnCancel);
        final TextView tvNumber = (TextView) dialog.findViewById(R.id.tvNumber);
        final TextView tvEstimate = (TextView) dialog.findViewById(R.id.tvEstimate);
        tvNumber.setText(String.valueOf(scanValue));
        tvEstimate.setText(getString(R.string.timeEstimate) + " " + getTimeEstimate(scanValue));
        seekBar.setProgress(scanValue);
        seekBar.setMax(12);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                tvNumber.setText(String.valueOf(i));
                tvEstimate.setText(getString(R.string.timeEstimate) + " " + getTimeEstimate(i));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int saveValue = seekBar.getProgress();
                if (saveValue == 0) {
                    //We don't want a value of 0, No one likes 0 :{
                    scanValue = 1;
                } else {
                    scanValue = saveValue;
                }
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("scanvalue",scanValue);
                editor.commit();
                dialog.dismiss();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
    public String getTimeEstimate(int val) {
        int calculatedValue = hexagonal_number(val) * SettingsController.getSettings(this).getServerRefresh() * 1000;
        long millis = calculatedValue;
        DateTime dt = new DateTime(millis);
        DateTimeFormatter fmt = DateTimeFormat.forPattern("mm:ss");
        return fmt.print(dt);
    }
    public void startDialogActivity() {
        Intent filterIntent = new Intent(MapsActivity.this,FilterActivity.class);
        startActivity(filterIntent);
    }
    public void reloadFilters() {
        try {
            filterItems.clear();
            filterItems.addAll(pokemonListLoader.getPokelist());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void logOut() {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.where(User.class).findAll().deleteAllFromRealm();
                Intent intent = new Intent(MapsActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });
    }
    //Map related Functions
    public void refreshMap() {
        realm.beginTransaction();
        mMap.clear();
        createMapObjects();
        ArrayList<Pokemons> pokemons = new ArrayList<Pokemons>(realm.copyFromRealm(realm.where(Pokemons.class).findAll()));
        for (int i = 0; i < pokemons.size(); i++) {
            Pokemons pokemon = pokemons.get(i);
            if (pokemon.getDate().isAfter(new Instant())) {
                if (realm.copyFromRealm(realm.where(FilterItem.class).equalTo("Number",pokemon.getNumber()).findFirst()).isFiltered()) {
                } else {
                    mMap.addMarker(pokemon.getMarker(this));
                }
            } else {
                realm.where(Pokemons.class).equalTo("encounterid", pokemon.getEncounterid()).findAll().deleteAllFromRealm();
            }
        }
        ArrayList<Gym> gyms = new ArrayList<Gym>(realm.copyFromRealm(realm.where(Gym.class).findAll()));
        for (int i = 0;i < gyms.size(); i++)
        {
            Gym gym = gyms.get(i);
            mMap.addMarker(gym.getMarker(this));
        }
        ArrayList<PokeStop> pokestops = new ArrayList<PokeStop>(realm.copyFromRealm(realm.where(PokeStop.class).findAll()));
        for (int i = 0;i < pokestops.size(); i++)
        {
            PokeStop pokestop = pokestops.get(i);
            mMap.addMarker(pokestop.getMarker(this));
        }
        realm.commitTransaction();
    }
    public void createMapObjects() {
        if (SettingsController.getSettings(this).isBoundingBoxEnabled()) {
            createBoundingBox();
        }
        //createMarkerList();
    }
    public void startRefresher() {
        final DateTime date = new DateTime();
        //If our Subscription is subscribed (As in is running)
        //Then lets unsubscribe them so we can create a new one
        if (refresher != null) {
            if (!refresher.isUnsubscribed()) {
                refresher.unsubscribe();
            }
        }
        //Using RX java we setup an interval to refresh the map
        refresher = Observable.interval(SettingsController.getSettings(this).getMapRefresh(), TimeUnit.SECONDS, AndroidSchedulers.mainThread())
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        Interval interval = new Interval(date, new DateTime());
                        long time = interval.toDurationMillis() / 1000;
                        System.out.println("Refreshing " + String.valueOf(time));
                        refreshMap();
                    }
                });
    }
    public boolean doWeHavePermission() {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
    }
    @Override
    protected void onResume() {
        System.out.println("Resume");
        super.onResume();
        realm = Realm.getDefaultInstance();
        reloadFilters();
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
