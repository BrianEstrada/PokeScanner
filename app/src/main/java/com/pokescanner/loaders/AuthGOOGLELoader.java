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

import com.pokegoapi.auth.GoogleLogin;
import com.pokegoapi.exceptions.LoginFailedException;

import org.greenrobot.eventbus.EventBus;

import POGOProtos.Networking.Envelopes.RequestEnvelopeOuterClass;
import okhttp3.OkHttpClient;

public class AuthGOOGLELoader extends Thread {
    String username,password;

    public AuthGOOGLELoader(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public void run() {
        try {
            OkHttpClient client = new OkHttpClient();
            GoogleLogin googleLogin = new GoogleLogin(client);
            RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo auth = googleLogin.login(username,password);
            System.out.println(auth);
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
