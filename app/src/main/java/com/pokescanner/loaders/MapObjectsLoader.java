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

package com.pokescanner.loaders;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;
import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.map.Map;
import com.pokegoapi.api.map.MapObjects;
import com.pokegoapi.auth.GoogleLogin;
import com.pokegoapi.auth.PTCLogin;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import com.pokescanner.events.ForceLogoutEvent;
import com.pokescanner.events.PublishProgressEvent;
import com.pokescanner.objects.Gym;
import com.pokescanner.objects.PokeStop;
import com.pokescanner.objects.Pokemons;
import com.pokescanner.objects.User;

import org.greenrobot.eventbus.EventBus;

import java.util.Collection;
import java.util.List;

import POGOProtos.Map.Fort.FortDataOuterClass;
import POGOProtos.Map.Pokemon.MapPokemonOuterClass;
import POGOProtos.Networking.Envelopes.RequestEnvelopeOuterClass;
import io.realm.Realm;
import okhttp3.OkHttpClient;

public class MapObjectsLoader extends Thread{
    User user;
    List<LatLng> scanMap;
    int SLEEP_TIME;

    private Realm realm;

    Context context;

    public MapObjectsLoader(User user, List<LatLng> scanMap, int SLEEP_TIME,Context context) {
        this.user = user;
        this.scanMap = scanMap;
        this.SLEEP_TIME = SLEEP_TIME;
        this.context = context;
    }

    @Override
    public void run() {
        try {
            //Create our client so we can call our objects
            OkHttpClient client = new OkHttpClient();
            //declare our user so we can get our pokemon go instance
            RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo authInfo;
            //this is ugly and I should feel bad.
            //Okay so we're going to check our user object
            //Are we using a google login or a PTC login?
            if (user.getAuthType() == User.GOOGLE) {
                //If google do this
                System.out.println(user);
                authInfo = new GoogleLogin(client).login(user.getToken().getId_token());
            }else {
                //if PTC do that
                System.out.println(user);
                authInfo = new PTCLogin(client).login(user.getUsername(), user.getPassword());
            }
            //Once we have our token and stuff lets call our pokemonGo instance
            PokemonGo go = new PokemonGo(authInfo, client);

            //Finally when we load our instance we're going to go through our scanmap
            int pos = 1;
            for (LatLng loc: scanMap) {
                //Set the location for each looop
                go.setLongitude(loc.longitude);
                go.setLatitude(loc.latitude);

                //Call the map
                Map map = new Map(go);

                //Start uploading info to realm.
                checkForPurge();

                MapObjects event = map.getMapObjects();

                final Collection<MapPokemonOuterClass.MapPokemon> collectionPokemon = event.getCatchablePokemons();
                final Collection<FortDataOuterClass.FortData> collectionGyms = event.getGyms();
                final Collection<FortDataOuterClass.FortData> collectionPokeStops = event.getPokestops();

                realm = Realm.getDefaultInstance();
                final int progress = pos;
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

                        if (EventBus.getDefault().hasSubscriberForEvent(PublishProgressEvent.class))
                        {
                            EventBus.getDefault().post(new PublishProgressEvent(progress));
                        }
                    }
                });
                //Time 2 wait
                pos++;
                Thread.sleep(SLEEP_TIME);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (RemoteServerException e) {
            EventBus.getDefault().post(new ForceLogoutEvent());
            e.printStackTrace();
        } catch (LoginFailedException e) {
            EventBus.getDefault().post(new ForceLogoutEvent());
            e.printStackTrace();
        }
    }

    public void checkForPurge() {

    }
}
