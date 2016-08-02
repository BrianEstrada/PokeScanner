package com.pokescanner;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.multidex.MultiDex;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.pokescanner.helper.PokeDistanceSorter;
import com.pokescanner.helper.PokemonListLoader;
import com.pokescanner.objects.FilterItem;
import com.pokescanner.objects.Pokemons;
import com.pokescanner.recycler.ListViewRecyclerAdapter;
import com.pokescanner.utils.PermissionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by Brian on 8/1/2016.
 */
public class ListViewActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    @BindView(R.id.recyclerListView)
    RecyclerView recyclerListView;

    ArrayList<Pokemons> pokemon = new ArrayList<>();
    ArrayList<Pokemons> pokemonRecycler = new ArrayList<>();
    LatLng cameraLocation;
    GoogleApiClient mGoogleApiClient;
    Subscription pokemonSubscriber;
    RecyclerView.Adapter mAdapter;
    Realm realm;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        MultiDex.install(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view);
        ButterKnife.bind(this);

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            if (extras.containsKey("cameraLocation")) {
                double location[] = extras.getDoubleArray("cameraLocation");
                if (location != null) {
                    cameraLocation = new LatLng(location[0], location[1]);
                }

            }
        }

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        realm = Realm.getDefaultInstance();

        setupRecycler();
        refresh();
    }

    public void setupRecycler() {
        recyclerListView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerListView.setLayoutManager(mLayoutManager);

        mAdapter = new ListViewRecyclerAdapter(pokemonRecycler);

        recyclerListView.setAdapter(mAdapter);
    }

    public void refreshList() {
        pokemon = new ArrayList<>(realm.copyFromRealm(realm.where(Pokemons.class).findAll()));
        pokemonRecycler = new ArrayList<>();

        LatLng latlng = getCurrentLocation();

        if (latlng == null) {
            latlng = cameraLocation;
        }

        if (latlng != null) {
            Location location = new Location("");
            location.setLatitude(latlng.latitude);
            location.setLongitude(latlng.longitude);
            //Write distance to pokemons
            for (int i = 0; i < pokemon.size(); i++) {
                Pokemons pokemons = pokemon.get(i);
                //IF OUR POKEMANS IS FILTERED WE AINT SHOWIN HIM
                if (!PokemonListLoader.getFilteredList().contains(new FilterItem(pokemons.getNumber()))) {
                    //DO MATH
                    Location temp = new Location("");

                    temp.setLatitude(pokemons.getLatitude());
                    temp.setLongitude(pokemons.getLongitude());

                    double distance = location.distanceTo(temp);
                    pokemons.setDistance(distance);

                    //ADD OUR POKEMANS TO OUR OUT LIST
                    pokemonRecycler.add(pokemons);
                }
            }
        }
        Collections.sort(pokemonRecycler,new PokeDistanceSorter());
        mAdapter.notifyDataSetChanged();
    }

    public void refresh() {
        if (pokemonSubscriber != null)
            pokemonSubscriber.unsubscribe();

        //Using RX java we setup an interval to refresh the map
        pokemonSubscriber = Observable.interval(1, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        System.out.println("Refresh");
                        refreshList();
                    }
                });
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

    @Override
    protected void onResume() {
        super.onResume();
        realm = Realm.getDefaultInstance();
    }

    @Override
    protected void onDestroy() {
        realm.close();
        super.onDestroy();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }
}
