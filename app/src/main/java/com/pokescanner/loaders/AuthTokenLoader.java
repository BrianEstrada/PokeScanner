package com.pokescanner.loaders;

import com.google.gson.Gson;
import com.pokegoapi.auth.GoogleUserCredentialProvider;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import com.pokescanner.events.AuthLoadedEvent;
import com.pokescanner.objects.GoogleAuthToken;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Brian on 7/30/2016.
 */
public class AuthTokenLoader extends Thread {
    String code;

    public AuthTokenLoader(String code){
        System.out.println("Starting code");
        System.out.println(code);
        this.code = code;
    }

    @Override
    public void run() {
        try {
            OkHttpClient client = new OkHttpClient();
            GoogleUserCredentialProvider loginProvider = new GoogleUserCredentialProvider(client);

            RequestBody body = new FormBody.Builder()
                    .add("code", code)
                    .add("client_id", GoogleUserCredentialProvider.CLIENT_ID)
                    .add("client_secret", GoogleUserCredentialProvider.SECRET)
                    .add("redirect_uri", "http://127.0.0.1:9004")
                    .add("grant_type", "authorization_code")
                    .build();

            Request req = new Request.Builder()
                    .url(GoogleUserCredentialProvider.OAUTH_TOKEN_ENDPOINT)
                    .method("POST", body)
                    .build();

            Response response = client.newCall(req).execute();

            String STR = response.body().string();

            GoogleAuthToken token = new Gson().fromJson(STR, GoogleAuthToken.class);

            GoogleUserCredentialProvider realLogin = new GoogleUserCredentialProvider(client,token.getRefreshToken());

            if (realLogin.getAuthInfo().hasToken()) {
                EventBus.getDefault().post(new AuthLoadedEvent(AuthLoadedEvent.OK,token));
            }else{
                EventBus.getDefault().post(new AuthLoadedEvent(AuthLoadedEvent.AUTH_FAILED));
            }

        } catch (LoginFailedException e) {
            EventBus.getDefault().post(new AuthLoadedEvent(AuthLoadedEvent.AUTH_FAILED));
            e.printStackTrace();
        } catch (RemoteServerException e) {
            EventBus.getDefault().post(new AuthLoadedEvent(AuthLoadedEvent.SERVER_FAILED));
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
