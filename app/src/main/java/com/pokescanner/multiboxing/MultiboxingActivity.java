package com.pokescanner.multiboxing;

import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.pokescanner.R;
import com.pokescanner.loaders.AuthAccountsLoader;
import com.pokescanner.loaders.AuthSingleAccountLoader;
import com.pokescanner.objects.User;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmConfiguration;

public class MultiboxingActivity extends AppCompatActivity{

    @BindView(R.id.rvMultiboxingAccountList)
    RecyclerView userRecycler;
    private ArrayList<User> userList;
    private MultiboxingAdapter userAdapter;

    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiboxing);
        ButterKnife.bind(this);

        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder(this)
                .name(Realm.DEFAULT_REALM_NAME)
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(realmConfiguration);
        realm = Realm.getDefaultInstance();

        realm.addChangeListener(new RealmChangeListener<Realm>() {
            @Override
            public void onChange(Realm element) {
                loadAccounts();
            }
        });

        userList = new ArrayList<User>();

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        userRecycler.setLayoutManager(mLayoutManager);

        userAdapter = new MultiboxingAdapter(this, userList, new MultiboxingAdapter.accountRemovalListener() {
            @Override
            public void onRemove(User user) {
                removeAccount(user);
            }
        });

        userRecycler.setAdapter(userAdapter);
    }

    private void removeAccount(final User user) {
        int realmSize = realm.where(User.class).findAll().size();
        if (realmSize != 1) {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    if (realm.where(User.class).equalTo("username", user.getUsername())
                            .findAll().deleteAllFromRealm()) {
                        int index = userList.indexOf(user);
                        userList.remove(index);
                        userAdapter.notifyItemRemoved(index);
                    }
                }
            });
        }
    }


    private void loadAccounts(){
        userList.clear();
        userList.addAll(realm.copyFromRealm(realm.where(User.class).findAll()));
        userAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        realm = Realm.getDefaultInstance();
        loadAccounts();
        refreshAccounts();
    }


    @OnClick(R.id.btnAddAccount)
    public void addAccountDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_add_account,null);
        final AlertDialog builder = new AlertDialog.Builder(this).create();

        final TextView etUsername = (TextView) view.findViewById(R.id.etAddUsername);
        final TextView etPassword = (TextView) view.findViewById(R.id.etAddPassword);

        Button btnAdd = (Button) view.findViewById(R.id.btnOk);
        Button btnCancel = (Button) view.findViewById(R.id.btnCancel);


        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = etUsername.getText().toString();
                String password = etPassword.getText().toString();

                User user = new User(username,password,null,User.PTC,User.STATUS_UNKNOWN);

                realm.beginTransaction();
                realm.copyToRealmOrUpdate(user);
                realm.commitTransaction();

                AuthSingleAccountLoader singleloader = new AuthSingleAccountLoader(user);
                singleloader.start();

                builder.dismiss();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                builder.dismiss();
            }
        });



        builder.setView(view);
        builder.show();
    }

    @OnClick(R.id.btnRefresh)
    public void refreshAccounts() {
        new AuthAccountsLoader().start();
    }

    @Override
    protected void onDestroy() {
        realm.close();
        super.onDestroy();
    }
}
