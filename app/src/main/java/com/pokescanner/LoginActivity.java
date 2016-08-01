package com.pokescanner;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.LoginEvent;
import com.pokescanner.events.AuthLoadedEvent;
import com.pokescanner.loaders.AuthPTCLoader;
import com.pokescanner.loaders.AuthTokenLoader;
import com.pokescanner.objects.User;
import com.pokescanner.utils.UiUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmConfiguration;


public class LoginActivity extends AppCompatActivity {
    @BindView(R.id.etUsername) EditText etUsername;
    @BindView(R.id.etPassword) EditText etPassword;
    @BindView(R.id.tvCheckServer) TextView tvCheckServer;

    @BindView(R.id.Container) LinearLayout Container;
    @BindView(R.id.progressBar) ProgressBar progressBar;

    String username, password;

    Realm realm;
    int LOGIN_METHOD = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder(this)
                .name(Realm.DEFAULT_REALM_NAME)
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(realmConfiguration);

        realm = Realm.getDefaultInstance();
    }

    public void checkIfUserIsLoggedIn() {
        if (realm.where(User.class).findAll().size() != 0) {
            //Get our first user
            User user = realm.where(User.class).findFirst();
            if (user.getAuthType() == User.PTC) {
                //Lets enter his information
                username = user.getUsername();
                password = user.getPassword();
            }
            startMapIntent();
        }
    }

    @OnClick(R.id.btnLogin)
    public void btnPTCLogin() {
        //get our username and password value
        username = etUsername.getText().toString();
        password = etPassword.getText().toString();
        //begin to show the progress bar
        showProgressbar(true);
        UiUtils.hideKeyboard(etPassword);

        showToast(R.string.TRYING_PTC_LOGIN);
        LOGIN_METHOD = User.PTC;
        AuthPTCLoader authloader = new AuthPTCLoader(username, password);
        authloader.start();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAuthLoadedEvent(final AuthLoadedEvent event) {
        switch (event.getStatus()) {
            case AuthLoadedEvent.OK:
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        User user = new User(
                                username,
                                password,
                                event.getToken(),
                                LOGIN_METHOD,
                                User.STATUS_VALID);
                        realm.copyToRealmOrUpdate(user);
                        loginLog();
                        showToast(R.string.LOGIN_OK);
                        startMapIntent();

                    }
                });
                break;
            case AuthLoadedEvent.AUTH_FAILED:
                showProgressbar(false);
                showToast(R.string.AUTH_FAILED);
                break;
            case AuthLoadedEvent.SERVER_FAILED:
                showProgressbar(false);
                showToast(R.string.SERVER_FAILED);
                break;
        }

    }

    public void loginLog(){
        //This method is used to track which login method users are using
        // We're going to use to see if we should be improving the google login
        //or not (We are not storing any sort of login information via this method)
        if (!BuildConfig.DEBUG) {
            switch (LOGIN_METHOD) {
                case User.GOOGLE:
                    Answers.getInstance().logLogin(new LoginEvent()
                            .putMethod("GOOGLE")
                            .putSuccess(true));
                    break;
                case User.PTC:
                    Answers.getInstance().logLogin(new LoginEvent()
                            .putMethod("PTC")
                            .putSuccess(true));
                    break;
            }
        }
    }


    @OnClick(R.id.btnGoogleLogin)
    public void GoogleLogin(View view) {
        showToast(R.string.TRYING_GOOGLE_LOGIN);
        LOGIN_METHOD = User.GOOGLE;
        Intent intent = new Intent(this, GoogleLoginActivity.class);
        startActivityForResult(intent, 1300);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null || !data.hasExtra(GoogleLoginActivity.EXTRA_CODE)) {
            showToast(R.string.AUTH_FAILED);
            return;
        }
        String code = data.getStringExtra(GoogleLoginActivity.EXTRA_CODE);
        if (code != null) {
            AuthTokenLoader tokenLoader = new AuthTokenLoader(code);
            tokenLoader.start();
        } else {
            showToast(R.string.AUTH_FAILED);
        }
    }

    public void startMapIntent() {
        Intent intent = new Intent(this, MapsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @OnClick(R.id.tvCheckServer)
    public void showServerStatus() {
        String url = "http://ispokemongodownornot.com/";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    //if this value is true then lets hide the login and show the progress bar
    public void showProgressbar(boolean status) {
        if (status) {
            progressBar.setVisibility(View.VISIBLE);
            Container.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            Container.setVisibility(View.VISIBLE);
        }
    }
    //Shows a toast (Boiler plate stuff)
    public void showToast(int resString) {
        Toast.makeText(LoginActivity.this, getString(resString), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkIfUserIsLoggedIn();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        realm.close();
        super.onDestroy();
    }
}
