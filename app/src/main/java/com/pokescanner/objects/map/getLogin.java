package com.pokescanner.objects.map;

import android.os.Handler;

import com.google.android.gms.maps.model.LatLng;
import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.map.Map;
import com.pokegoapi.api.map.MapObjects;
import com.pokegoapi.auth.PTCLogin;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokescanner.PokemonLoadedEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import POGOProtos.Networking.EnvelopesOuterClass;
import okhttp3.OkHttpClient;

/**
 * Created by Brian on 7/19/2016.
 */
public class getLogin extends Thread {
    LatLng location;
    String username,password;

    private boolean started = false;
    private Handler handler = new Handler();
    ArrayList<LatLng> tempList = new ArrayList<>();

    public getLogin(LatLng latLng, String username, String password) {
        this.location = latLng;
        this.username = username;
        this.password = password;
    }

    @Override
    public void run() {
        double dist = 00.001500;
        double lat = location.latitude;
        double longi = location.longitude;

        tempList.add(new LatLng(lat, longi));

        tempList.add(new LatLng(lat, longi - dist));
        tempList.add(new LatLng(lat, longi + dist));

        tempList.add(new LatLng(lat - dist, longi));
        tempList.add(new LatLng(lat + dist, longi));

        final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(tempList.size());

        for (final LatLng lng: tempList) {
            executor.schedule(new Runnable() {
                @Override
                public void run() {
                    loadPokemons(lng);
                }
            }, 5, TimeUnit.SECONDS);
        }
    }

    public void loadPokemons(LatLng lng) {
        try {
            OkHttpClient client = new OkHttpClient();
            PTCLogin ptcLogin = new PTCLogin(client);
            EnvelopesOuterClass.Envelopes.RequestEnvelope.AuthInfo auth = ptcLogin.login(username, password);
            System.out.println(auth);
            PokemonGo go = new PokemonGo(auth, client);

            System.out.println(go.getPlayerProfile());
            
            go.setLatitude(lng.latitude);
            go.setLongitude(lng.longitude);

            Map map = new Map(go);
            MapObjects objects = map.getMapObjects(go.getLatitude(), go.getLongitude());
            EventBus.getDefault().post(new PokemonLoadedEvent(objects));
        } catch (LoginFailedException e) {
            EventBus.getDefault().post(new PokemonLoadedEvent(null));
            e.printStackTrace();
        }
    }
}
