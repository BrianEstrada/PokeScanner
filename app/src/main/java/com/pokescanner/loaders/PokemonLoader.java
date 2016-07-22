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
import com.pokegoapi.api.map.Pokemon.CatchablePokemon;
import com.pokegoapi.auth.GoogleLogin;
import com.pokegoapi.auth.PTCLogin;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import com.pokescanner.objects.User;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import POGOProtos.Networking.Envelopes.RequestEnvelopeOuterClass;
import okhttp3.OkHttpClient;

public class PokemonLoader extends Thread{
    User user;
    ArrayList<LatLng> scanMap;
    int SLEEP_TIME;

    public PokemonLoader(User user, ArrayList<LatLng> scanMap, int SLEEP_TIME) {
        this.user = user;
        this.scanMap = scanMap;
        this.SLEEP_TIME = SLEEP_TIME;
    }

    @Override
    public void run() {
        try {
            OkHttpClient client = new OkHttpClient();
            RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo authInfo;
            //this is ugly and I should feel bad.
            if (user.getAuthType() == User.GOOGLE) {
                authInfo = new GoogleLogin(client).login(user.getUsername(), user.getPassword());
            }else {
                authInfo = new PTCLogin(client).login(user.getUsername(), user.getPassword());
            }
            PokemonGo go = new PokemonGo(authInfo, client);

            for (LatLng loc: scanMap) {
                go.setLongitude(loc.longitude);
                go.setLatitude(loc.latitude);
                Map map = new Map(go);

                final List<CatchablePokemon> catchablePokemon = map.getCatchablePokemon();
                EventBus.getDefault().post(new PokemonLoadedEvent(catchablePokemon));
                Thread.sleep(SLEEP_TIME);
            }
        } catch (InterruptedException e) {
            EventBus.getDefault().post(new PokemonLoadedEvent(null));
            e.printStackTrace();
        } catch (RemoteServerException e) {
            EventBus.getDefault().post(new PokemonLoadedEvent(null));
            e.printStackTrace();
        } catch (LoginFailedException e) {
            EventBus.getDefault().post(new PokemonLoadedEvent(null));
            e.printStackTrace();
        }
    }
}
