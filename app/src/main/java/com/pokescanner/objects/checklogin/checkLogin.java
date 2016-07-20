package com.pokescanner.objects.checklogin;

import com.pokegoapi.auth.PTCLogin;
import com.pokegoapi.exceptions.LoginFailedException;

import org.greenrobot.eventbus.EventBus;

import POGOProtos.Networking.EnvelopesOuterClass;
import okhttp3.OkHttpClient;

/**
 * Created by Brian on 7/19/2016.
 */
public class checkLogin extends Thread {
    String username,password;

    public checkLogin(String username,String password) {
        this.password = password;
        this.username = username;
    }

    @Override
    public void run() {
        OkHttpClient client = new OkHttpClient();
        PTCLogin ptcLogin = new PTCLogin(client);
        try {
            EnvelopesOuterClass.Envelopes.RequestEnvelope.AuthInfo auth = ptcLogin.login(username,password);
            EventBus.getDefault().post(new LoginLoadedEvent(auth));
        } catch (LoginFailedException e) {
            EventBus.getDefault().post(new LoginLoadedEvent(null));
            e.printStackTrace();
        }
    }
}
