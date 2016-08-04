package com.pokescanner.loaders;

import com.pokegoapi.auth.PtcCredentialProvider;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import com.pokescanner.objects.User;

import java.util.List;

import io.realm.Realm;
import okhttp3.OkHttpClient;

/**
 * Created by Brian on 7/31/2016.
 */
public class AuthSingleAccountLoader extends Thread {
    User user;

    public AuthSingleAccountLoader(User user){
        this.user = user;
    }

    @Override
    public void run() {
        Realm realm = Realm.getDefaultInstance();

        user.setStatus(User.STATUS_UNKNOWN);

        realm.beginTransaction();
        realm.copyToRealmOrUpdate(user);
        realm.commitTransaction();

        OkHttpClient client = new OkHttpClient();
        try {
            PtcCredentialProvider ptcCredentialProvider = new PtcCredentialProvider(client, user.getUsername(), user.getPassword());
            if (ptcCredentialProvider.getAuthInfo().hasToken()) {
                user.setStatus(User.STATUS_VALID);
            } else {
                user.setStatus(User.STATUS_INVALID);
            }
        } catch (RemoteServerException | LoginFailedException e) {
            user.setStatus(User.STATUS_INVALID);
            e.printStackTrace();
        }


        realm.beginTransaction();
        realm.copyToRealmOrUpdate(user);
        realm.commitTransaction();

        realm.close();
    }
}
