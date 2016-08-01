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
public class AuthAccountsLoader extends Thread {

    public AuthAccountsLoader(){}

    @Override
    public void run() {
            Realm realm = Realm.getDefaultInstance();
            List<User> users = realm.copyFromRealm(realm.where(User.class).findAll());

            for (User user: users) {
                user.setStatus(User.STATUS_UNKNOWN);
            }

            realm.beginTransaction();
            realm.copyToRealmOrUpdate(users);
            realm.commitTransaction();

            OkHttpClient client = new OkHttpClient();
            for (User user: users) {
                try {
                    PtcCredentialProvider ptcCredentialProvider = new PtcCredentialProvider(client, user.getUsername(), user.getPassword());
                    sleep(300);
                    if (ptcCredentialProvider.getAuthInfo().hasToken()) {
                        user.setStatus(User.STATUS_VALID);
                    } else {
                        user.setStatus(User.STATUS_INVALID);
                    }
                } catch (RemoteServerException | LoginFailedException e) {
                    user.setStatus(User.STATUS_INVALID);
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        realm.beginTransaction();
        realm.copyToRealmOrUpdate(users);
        realm.commitTransaction();

        realm.close();
    }
}
