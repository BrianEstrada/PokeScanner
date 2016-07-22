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

package com.pokescanner.scanner;

import com.google.android.gms.maps.model.LatLng;
import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.map.Map;
import com.pokegoapi.api.map.Pokemon.CatchablePokemon;
import com.pokegoapi.auth.PTCLogin;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import com.pokescanner.objects.Pokemons;
import com.pokescanner.objects.User;

import java.util.ArrayList;
import java.util.List;

import POGOProtos.Networking.Envelopes.RequestEnvelopeOuterClass;
import io.realm.Realm;
import okhttp3.OkHttpClient;

/**
 * Created by Brian on 7/22/2016.
 */
public class Scanner extends Thread {
    OkHttpClient client;
    ArrayList<LatLng> scanMap;
    int SLEEP_TIME;
    User user;
    Realm realm;

    public Scanner(ArrayList<LatLng> scanMap, int SLEEP_TIME) {
        realm = Realm.getDefaultInstance();
        this.scanMap = scanMap;
        client = new OkHttpClient();
        this.SLEEP_TIME = SLEEP_TIME;
        user = realm.where(User.class).findFirst();
    }

    @Override
    public void run() {
        try {
            for (LatLng loc: scanMap) {
                RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo authInfo = new PTCLogin(client).login(user.getUsername(), user.getPassword());
                PokemonGo go = new PokemonGo(authInfo, client);
                Thread.sleep(SLEEP_TIME);
                go.setLongitude(loc.longitude);
                go.setLatitude(loc.latitude);
                Map map = new Map(go);

                final List<CatchablePokemon> catchablePokemon = map.getCatchablePokemon();

                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        for (CatchablePokemon pokemon: catchablePokemon) {
                            realm.copyToRealmOrUpdate(toPokemons(pokemon));
                        }
                    }
                });

            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (RemoteServerException e) {
            e.printStackTrace();
        } catch (LoginFailedException e) {
            e.printStackTrace();
        }
    }

    public Pokemons toPokemons(CatchablePokemon catchablePokemon) {
        Pokemons pokemons = new Pokemons();
        pokemons.setEncounterid(catchablePokemon.getEncounterId());
        pokemons.setName(catchablePokemon.getPokemonId().toString());
        pokemons.setExpires(catchablePokemon.getExpirationTimestampMs());
        pokemons.setNumber(catchablePokemon.getPokemonId().getNumber());
        pokemons.setLaditude(catchablePokemon.getLatitude());
        pokemons.setLongitude(catchablePokemon.getLongitude());
        return pokemons;
    }
}
