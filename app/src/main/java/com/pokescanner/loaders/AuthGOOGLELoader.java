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

import com.google.gson.Gson;
import com.pokegoapi.auth.GoogleAuthTokenJson;
import com.pokegoapi.auth.GoogleLogin;
import com.pokescanner.events.AuthLoadedEvent;
import com.pokescanner.objects.GoogleAuthToken;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;

import POGOProtos.Networking.Envelopes.RequestEnvelopeOuterClass;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AuthGOOGLELoader extends Thread {
    String code;

    public AuthGOOGLELoader(String code) {
        this.code = code;
    }

    @Override
    public void run() {
        try {
            OkHttpClient client = new OkHttpClient();
            GoogleLogin googleLogin = new GoogleLogin(client);

            RequestBody body = new FormBody.Builder()
                    .add("code", code)
                    .add("client_id", GoogleLogin.CLIENT_ID)
                    .add("client_secret", GoogleLogin.SECRET)
                    .add("redirect_uri", "http://127.0.0.1:9004")
                    .add("grant_type", "authorization_code")
                    .build();
            Request req = new Request.Builder()
                    .url(GoogleLogin.OAUTH_TOKEN_ENDPOINT)
                    .method("POST", body)
                    .build();
            Response response = client.newCall(req).execute();

            GoogleAuthTokenJson token = new Gson().fromJson(response.body().string(), GoogleAuthTokenJson.class);
            RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo auth = googleLogin.login(token.getAccess_token());

            System.out.println(token.toString());

            if (auth.hasToken()) {
                EventBus.getDefault().post(new AuthLoadedEvent(AuthLoadedEvent.OK,new GoogleAuthToken(token)));
            }else{
                EventBus.getDefault().post(new AuthLoadedEvent(AuthLoadedEvent.AUTH_FAILED,null));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
