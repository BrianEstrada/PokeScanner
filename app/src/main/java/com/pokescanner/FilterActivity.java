package com.pokescanner;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;

import com.pokescanner.helper.PokemonListLoader;
import com.pokescanner.objects.FilterItem;
import com.pokescanner.recycler.FilterRecyclerAdapter;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Case;
import io.realm.Realm;

/**
 * Created by Brian on 7/22/2016.
 */
public class FilterActivity extends AppCompatActivity implements TextWatcher {
    @BindView(R.id.etSearch) EditText etSearch;
    @BindView(R.id.filterRecycler) RecyclerView filterRecycler;
    @BindView(R.id.btnNone) Button btnNone;
    @BindView(R.id.btnAll) Button btnAll;

    ArrayList<FilterItem> filterItems = new ArrayList<>();
    RecyclerView.Adapter mAdapter;
    Realm realm;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);
        ButterKnife.bind(this);

        realm = Realm.getDefaultInstance();

        etSearch.addTextChangedListener(this);

        try {
            filterItems = PokemonListLoader.getPokelist(this);
        } catch (IOException e) {
            e.printStackTrace();
        }

        setupRecycler();
    }

    public void setupRecycler(){
        RecyclerView.LayoutManager mLayoutManager;
        filterRecycler.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        filterRecycler.setLayoutManager(mLayoutManager);

        mAdapter = new FilterRecyclerAdapter(filterItems, new FilterRecyclerAdapter.onCheckedListener() {
            @Override
            public void onChecked(final FilterItem filterItem) {
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        realm.copyToRealmOrUpdate(filterItem);
                    }
                });
            }
        });


        filterRecycler.setAdapter(mAdapter);

    }

    @Override
    public void onBackPressed() {
        finishActivity();
    }

    public void finishActivity(){
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealmOrUpdate(filterItems);
            }
        });
        finish();
    }


    @OnClick(R.id.btnNone)
    public void selectNoneButton(){
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                filterItems.clear();
                filterItems.addAll(realm.copyFromRealm(realm.where(FilterItem.class)
                        .findAll()));
                for (FilterItem filterItem: filterItems) {
                    filterItem.setFiltered(false);
                }
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    @OnClick(R.id.btnAll)
    public void selectAllButton(){
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                filterItems.clear();
                filterItems.addAll(realm.copyFromRealm(realm.where(FilterItem.class)
                        .findAll()));
                for (FilterItem filterItem: filterItems) {
                    filterItem.setFiltered(true);
                }
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

    @Override
    public void onTextChanged(final CharSequence charSequence, int i, int i1, int i2) {
        if (charSequence.length() > 0) {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    filterItems.clear();
                    filterItems.addAll(realm.copyFromRealm(realm.where(FilterItem.class)
                            .contains("Name",charSequence.toString(), Case.INSENSITIVE)
                            .findAll()));
                    mAdapter.notifyDataSetChanged();
                }
            });
        }else {
            filterItems.clear();
            filterItems.addAll(realm.copyFromRealm(realm.where(FilterItem.class).findAll()));
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {
    }

    @Override
    protected void onResume() {
        realm = Realm.getDefaultInstance();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        realm.close();
        super.onDestroy();
    }
}
