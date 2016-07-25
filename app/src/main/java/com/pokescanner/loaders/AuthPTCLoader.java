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

import com.pokegoapi.auth.PTCLogin;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokescanner.events.AuthLoadedEvent;

import org.greenrobot.eventbus.EventBus;

import POGOProtos.Networking.Envelopes.RequestEnvelopeOuterClass;
import okhttp3.OkHttpClient;

/**
 * Created by Brian on 7/22/2016.
 */
public class AuthPTCLoader extends Thread {
    String username,password;

    public AuthPTCLoader(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public void run() {
        try {
            OkHttpClient client = new OkHttpClient();
            PTCLogin ptcLogin = new PTCLogin(client);
            RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo auth = ptcLogin.login(username, password);
            if (auth.hasToken()) {
                EventBus.getDefault().post(new AuthLoadedEvent(AuthLoadedEvent.OK));
            }else{
                EventBus.getDefault().post(new AuthLoadedEvent(AuthLoadedEvent.AUTH_FAILED));
            }
        } catch (LoginFailedException e) {
            EventBus.getDefault().post(new AuthLoadedEvent(AuthLoadedEvent.SERVER_FAILED));
            e.printStackTrace();
        }
    }
}
