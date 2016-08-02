package com.pokescanner.settings;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.pokescanner.LoginActivity;
import com.pokescanner.R;
import com.pokescanner.multiboxing.MultiboxingActivity;
import com.pokescanner.objects.Gym;
import com.pokescanner.objects.PokeStop;
import com.pokescanner.objects.Pokemons;
import com.pokescanner.objects.User;

import java.util.List;

import io.realm.Realm;

public class SettingsActivity extends AppCompatPreferenceActivity {

    private Realm realm;
    private Context mContext;

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.settings_headers, target);

        setContentView(R.layout.settings_page);
        Toolbar toolbar = (Toolbar) findViewById(R.id.settingsToolbar);
        setSupportActionBar(toolbar);

        ActionBar bar = getSupportActionBar();
        bar.setHomeButtonEnabled(true);
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setDisplayShowTitleEnabled(true);
        bar.setHomeAsUpIndicator(R.drawable.back_button);
        bar.setTitle("Settings");
    }

    @Override
    public void onHeaderClick(Header header, int position) {
        super.onHeaderClick(header, position);
        if(header.id == R.id.logoutHeader)
            logOut();
        else if(header.id == R.id.multiboxingHeader)
        {
            Intent i = new Intent(mContext, MultiboxingActivity.class);
            startActivity(i);
        }
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void logOut() {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.where(User.class).findAll().deleteAllFromRealm();
                realm.where(PokeStop.class).findAll().deleteAllFromRealm();
                realm.where(Pokemons.class).findAll().deleteAllFromRealm();
                realm.where(Gym.class).findAll().deleteAllFromRealm();
                Intent intent = new Intent(mContext, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        realm = Realm.getDefaultInstance();
        mContext = SettingsActivity.this;
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        realm.close();
        super.onDestroy();
    }
}
