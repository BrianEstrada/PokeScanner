package com.pokescanner.helpers;

import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.map.Map;
import com.pokegoapi.api.map.Pokemon.CatchablePokemon;
import com.pokegoapi.auth.PTCLogin;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;

import POGOProtos.Networking.Envelopes.RequestEnvelopeOuterClass;
import okhttp3.OkHttpClient;

/**
 * Created by Brian on 7/21/2016.
 */
public class getPokemans extends Thread {
    @Override
    public void run() {
        try {
            OkHttpClient client = new OkHttpClient();
            PTCLogin ptcLogin = new PTCLogin(client);
            RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo auth = ptcLogin.login("throwaway562", "throwaway562");

            PokemonGo go = new PokemonGo(auth, client);
            System.out.println(go.getPlayerProfile());
            go.setLongitude(33.9766900);
            go.setLatitude(-118.0453900);
            Map map = new Map(go);

            for(CatchablePokemon pokemon: map.getCatchablePokemon())
            {
                System.out.println(pokemon.getPokemonId());
            }
        } catch (RemoteServerException e) {
            e.printStackTrace();
        } catch (LoginFailedException e) {
            e.printStackTrace();
        }
    }
}
