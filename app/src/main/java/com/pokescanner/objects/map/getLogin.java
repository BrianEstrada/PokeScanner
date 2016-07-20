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
import java.util.Date;

import POGOProtos.Networking.EnvelopesOuterClass;
import okhttp3.OkHttpClient;

/**
 * Created by Brian on 7/19/2016.
 */
public class getLogin extends Thread {
    LatLng location;
    String username, password;

    private boolean started = false;
    private double[] lat = {0, 0, -00.001500, 00.001500}, lon = {-00.001500, 00.001500, 0, 0};
    private int pos = 0;
    private Handler handler = new Handler();

    public getLogin(LatLng latLng, String username, String password) {
        this.location = latLng;
        this.username = username;
        this.password = password;
    }

    @Override
    public void run() {
        if (pos >= 4) return;
        LatLng loc = new LatLng(location.latitude + lat[pos], lcation.longitude + lon[pos++]);
        loadPokemons(loc);
        handler.postDelayed(this, 5000);
    }

    public void loadPokemons(LatLng lng) {
        try {
            System.out.print("Running");
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
