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

import com.google.android.gms.maps.model.LatLng;
import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.map.Map;
import com.pokegoapi.auth.GoogleLogin;
import com.pokegoapi.auth.PTCLogin;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import com.pokescanner.objects.User;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import POGOProtos.Networking.Envelopes.RequestEnvelopeOuterClass;
import okhttp3.OkHttpClient;

public class MapObjectsLoader extends Thread{
    User user;
    List<LatLng> scanMap;
    int SLEEP_TIME;

    public MapObjectsLoader(User user, List<LatLng> scanMap, int SLEEP_TIME) {
        this.user = user;
        this.scanMap = scanMap;
        this.SLEEP_TIME = SLEEP_TIME;
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
                authInfo = new GoogleLogin(client).login(user.getUsername(), user.getPassword());
            }else {
                //if PTC do that
                authInfo = new PTCLogin(client).login(user.getUsername(), user.getPassword());
            }
            //Once we have our token and stuff lets call our pokemonGo instance
            PokemonGo go = new PokemonGo(authInfo, client);

            //Finally when we load our instance we're going to go through our scanmap
            for (LatLng loc: scanMap) {
                //Set the location for each looop
                go.setLongitude(loc.longitude);
                go.setLatitude(loc.latitude);

                //Call the map
                Map map = new Map(go);

                //Send it off to the map thread
                EventBus.getDefault().post(new MapObjectsLoadedEvent(map.getMapObjects()));

                //Time 2 wait
                Thread.sleep(SLEEP_TIME);
            }
        } catch (InterruptedException e) {
            EventBus.getDefault().post(new MapObjectsLoadedEvent(null));
            e.printStackTrace();
        } catch (RemoteServerException e) {
            EventBus.getDefault().post(new MapObjectsLoadedEvent(null));
            e.printStackTrace();
        } catch (LoginFailedException e) {
            EventBus.getDefault().post(new MapObjectsLoadedEvent(null));
            e.printStackTrace();
        }
    }
}
