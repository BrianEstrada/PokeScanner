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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.pokescanner.helper.PokemonListLoader;
import com.pokescanner.loaders.MapObjectsLoadedEvent;
import com.pokescanner.loaders.MapObjectsLoader;
import com.pokescanner.objects.FilterItem;
import com.pokescanner.objects.MenuItem;
import com.pokescanner.objects.Pokemons;
import com.pokescanner.objects.User;
import com.pokescanner.recycler.FilterRecyclerAdapter;
import com.pokescanner.recycler.MenuRecycler;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.joda.time.Instant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import POGOProtos.Map.Pokemon.MapPokemonOuterClass;
import io.realm.Realm;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnCameraChangeListener {

    Button button;
    ProgressBar progressBar;
    private GoogleMap mMap;

    LocationManager locationManager;
    Location currentLocation;
    String username, password;
    ImageButton imageButton;

    User user;
    Realm realm;
    RecyclerView.Adapter mAdapter;

    ArrayList<LatLng> scanMap = new ArrayList<>();

    int pos = 0;
    final int SLEEP_TIME = 2000;
    int scanValue = 4;
    boolean boundingBox = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MultiDex.install(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        realm = Realm.getDefaultInstance();

        if  (realm.where(User.class).findAll().size() != 0) {
            user = realm.copyFromRealm(realm.where(User.class).findFirst());
        }else
        {
            Toast.makeText(MapsActivity.this, "No login!", Toast.LENGTH_SHORT).show();
            logOut();
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

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
        pos = 0;
        progressBar.setProgress(0);
        if (mMap != null)
            mMap.clear();
        createHexScanMap(mMap.getCameraPosition().target, scanValue);

        MapObjectsLoader mapObjectsLoader = new MapObjectsLoader(user,scanMap,SLEEP_TIME);
        //mapObjectsLoader.start();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void mapObjectsLoaded(MapObjectsLoadedEvent event) {
        progressBar.setProgress(pos);

        if (pos==(Math.pow(scanValue,2)-1)) {
            showProgressbar(false);
        }

        final Collection<MapPokemonOuterClass.MapPokemon> collectionPokemon = event.getMapObjects().getCatchablePokemons();

        if (collectionPokemon != null) {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    for (MapPokemonOuterClass.MapPokemon pokemonOut: collectionPokemon)
                    {
                        realm.copyToRealmOrUpdate(new Pokemons(pokemonOut));
                    }
                }
            });
        }else {
            showToast(R.string.SERVER_FAILED);
        }
        pos++;
    }

    public void showToast(int resString) {
        Toast.makeText(MapsActivity.this, getString(resString), Toast.LENGTH_SHORT).show();
    }

    public void createBoundingBox() {
        if (scanMap.size() == Math.pow(scanValue,2)) {
            int adjusted = scanValue - 1;
            mMap.addPolygon(new PolygonOptions()
                    .add(scanMap.get(0))
                    .add(scanMap.get(scanValue - 1))
                    .add(scanMap.get(scanMap.size() - 1))
                    .add(scanMap.get(scanMap.size() - adjusted - 1))
            );
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
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.deleteAll();
                Intent intent = new Intent(MapsActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }
    //Menu Related Functions
    public void showMenu() {
        ArrayList<MenuItem> items = new ArrayList<>();

        items.add(new MenuItem("Search Radius",0,null));
        items.add(new MenuItem("Pokemon Filters",1,null));
        items.add(new MenuItem("Settings",2,null));
        items.add(new MenuItem("Log Out",3,null));

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
                        filterDialog();
                        dialog.dismiss();
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
        seekBar.setProgress(scanValue);
        seekBar.setMax(12);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                System.out.println(i);
                tvNumber.setText(String.valueOf(i));
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
                if (saveValue == 0 || saveValue == 1) {
                    scanValue = 2;
                }else {
                    scanValue = saveValue;
                }
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
    public void filterDialog(){

        final PokemonListLoader pokemonListLoader = new PokemonListLoader(this);
        try {
            final ArrayList<FilterItem> filterItems = pokemonListLoader.getPokelist();

            final Dialog dialog = new Dialog(this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_blacklist);

            Button btnAll = (Button) dialog.findViewById(R.id.btnAll);
            Button btnNone = (Button) dialog.findViewById(R.id.btnNone);
            Button btnSave = (Button) dialog.findViewById(R.id.btnSave);
            RecyclerView filterRecycler = (RecyclerView) dialog.findViewById(R.id.filterRecycler);

            RecyclerView.LayoutManager mLayoutManager;
            filterRecycler.setHasFixedSize(true);
            mLayoutManager = new LinearLayoutManager(this);
            filterRecycler.setLayoutManager(mLayoutManager);

            mAdapter = new FilterRecyclerAdapter(filterItems, new FilterRecyclerAdapter.onCheckedListener() {
                @Override
                public void onChecked(FilterItem filterItem) {
                    filterItems.set(filterItem.getNumber(),filterItem);
                }
            });

            btnAll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    for (int i = 0;i<filterItems.size();i++)
                    {
                        filterItems.get(i).setFiltered(true);
                    }
                    mAdapter.notifyDataSetChanged();
                }
            });

            btnNone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    for (int i = 0;i<filterItems.size();i++)
                    {
                        filterItems.get(i).setFiltered(false);
                    }
                    mAdapter.notifyDataSetChanged();
                }
            });

            btnSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    pokemonListLoader.savePokeList(filterItems);
                    dialog.dismiss();
                }
            });

            filterRecycler.setAdapter(mAdapter);
            dialog.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                    mMap.addMarker(pokemon.getMarker(this));

                }else
                {
                    realm.where(Pokemons.class).equalTo("encounterid",pokemon.getEncounterid()).findAll().deleteAllFromRealm();
                }
        }
        realm.commitTransaction();
    }
    public void createMapObjects() {
        if (boundingBox)
            createBoundingBox();
        createMarkerList();
    }

    // Call with layer_count initially 1
    // REQUIRES: not empty scanMap, layer_count > 0, loc is the starting loc
    public void HexScanMapHelp(LatLng loc, int steps, int layer_count) {
        // Base case is do nothing
        if (steps > 0) {
            if (layer_count == 1) {
                // Add in the point, no translation since 1st layer
                scanMap.add(loc);
            } else {
                double distance = 173.2; // in meters
                // add a point that is distance due north
                scanMap.add(translate(loc, 0.0, distance));
                // go south-east
                for (int i = 0; i < layer_count - 1; i++) {
                    LatLng prev = scanMap.get(scanMap.size() - 1);
                    LatLng next = translate(prev, 120.0, distance);
                    scanMap.add(next);
                }
                // go due south
                for (int i = 0; i < layer_count - 1; i++) {
                    LatLng prev = scanMap.get(scanMap.size() - 1);
                    LatLng next = translate(prev, 180.0, distance);
                    scanMap.add(next);
                }
                // go south-west
                for (int i = 0; i < layer_count - 1; i++) {
                    LatLng prev = scanMap.get(scanMap.size() - 1);
                    LatLng next = translate(prev, 240.0, distance);
                    scanMap.add(next);
                }
                // go north-west
                for (int i = 0; i < layer_count - 1; i++) {
                    LatLng prev = scanMap.get(scanMap.size() - 1);
                    LatLng next = translate(prev, 300.0, distance);
                    scanMap.add(next);
                }
                // go due north
                for (int i = 0; i < layer_count - 1; i++) {
                    LatLng prev = scanMap.get(scanMap.size() - 1);
                    LatLng next = translate(prev, 0.0, distance);
                    scanMap.add(next);
                }
                // go north-east
                for (int i = 0; i < layer_count - 2; i++) {
                    LatLng prev = scanMap.get(scanMap.size() - 1);
                    LatLng next = translate(prev, 60.0, distance);
                    scanMap.add(next);
                }
            }
            HexScanMapHelp(scanMap.get(hexagonal_number(layer_count -1)), steps - 1, layer_count + 1);
        }


    }

    // Takes in distance in meters, bearing in degrees
    public LatLng translate(LatLng cur, double bearing, double distance) {
        double earth = 6378.1; // Radius of Earth in km
        double rad_bear = Math.toRadians(bearing);
        double dist_km = distance/1000;
        double lat1 = Math.toRadians(cur.latitude);
        double lon1 = Math.toRadians(cur.longitude);
        double lat2 =  Math.asin( Math.sin(lat1) * Math.cos(dist_km/earth) +
                Math.cos(lat1) * Math.sin(dist_km/earth) * Math.cos(rad_bear));
        double lon2 = lon1 + Math.atan2(Math.sin(rad_bear) * Math.sin(dist_km/earth) * Math.cos(lat1),
                Math.cos(dist_km/earth) - Math.sin(lat1) * Math.sin(lat2));
        lat2 = Math.toDegrees(lat2);
        lon2 = Math.toDegrees(lon2);
        return new LatLng(lat2, lon2);
    }

    public int hexagonal_number(int n) {
        return (n == 0) ? 0 : 3 * n * (n - 1) + 1;
    }

    public void createHexScanMap(LatLng loc, int gridsize) {
        // Clear previous scan map
        scanMap.clear();

        HexScanMapHelp(loc, gridsize, 1);
    }

    public void createScanMap(LatLng loc, int gridsize) {
        int gridNumber = gridsize;
        //Make our grid size an odd number (evens don't have centers :P)
        if ((gridsize % 2) == 0) {
            gridNumber = gridNumber - 1;
        }
        //clear the previous scan map
        scanMap.clear();
        //Our distance number is the number we'll offset the GPS by
        //when creating a grid
        double dist = 00.002000;
        //to find the middle of the grid we need to find the middle number
        int middleNumber = ((gridNumber - 1) / 2);
        System.out.println("Grid Size: "+gridsize +"Adjusted Size: " +  gridNumber + "Middle Number; "+ middleNumber);
        double lat = loc.latitude + (dist * -middleNumber);
        double lon = loc.longitude + (dist * -middleNumber);
        //this is the GPS offset we're going to use
        for (int i = 0; i < gridNumber; i++) {
            for (int j = 0; j < gridNumber; j++) {
                double newLat = (lat + (dist * i));
                double newLon = (lon + (dist * j));
                LatLng temp = new LatLng(newLat, newLon);
                scanMap.add(temp);
            }
        }
    }
    public void startRefresher() {
        Observable.interval(3, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        refreshMap();
                        System.out.println("Refreshing Map");
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
        super.onResume();
        realm = Realm.getDefaultInstance();
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
