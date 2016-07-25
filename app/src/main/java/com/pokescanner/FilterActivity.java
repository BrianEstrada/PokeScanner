package com.pokescanner;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.pokescanner.objects.FilterItem;
import com.pokescanner.recycler.FilterRecyclerAdapter;

import java.util.ArrayList;

import io.realm.Case;
import io.realm.Realm;

/**
 * Created by Brian on 7/22/2016.
 */
public class FilterActivity extends AppCompatActivity implements TextWatcher {
    ArrayList<FilterItem> filterItems = new ArrayList<>();
    RecyclerView.Adapter mAdapter;
    EditText etSearch;
    RecyclerView filterRecycler;
    Realm realm;
    Button btnCancel;
    Button btnSave;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        realm = Realm.getDefaultInstance();


        filterRecycler = (RecyclerView) findViewById(R.id.filterRecycler);

        btnCancel = (Button) findViewById(R.id.btnCancel);
        btnSave = (Button) findViewById(R.id.btnSave);

        etSearch = (EditText) findViewById(R.id.etSearch);

        loadFilters();
        setupRecycler();

        etSearch.addTextChangedListener(this);
    }

    public void loadFilters() {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                filterItems.clear();
                filterItems.addAll(realm.copyFromRealm(realm.where(FilterItem.class).findAll().sort("Number")));
            }
        });
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


        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishActivity();
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
