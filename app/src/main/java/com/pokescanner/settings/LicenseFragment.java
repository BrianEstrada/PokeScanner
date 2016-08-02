package com.pokescanner.settings;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pokescanner.R;

public class LicenseFragment extends PreferenceFragment {

    private Context mContext;
    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_license, container, false);
        mContext = getActivity();
        setupToolbar();
        return rootView;
    }

    private void setupToolbar() {
        AppCompatPreferenceActivity activity = (AppCompatPreferenceActivity) getActivity();
        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.licenseToolbar);
        activity.setSupportActionBar(toolbar);

        ActionBar bar = activity.getSupportActionBar();
        bar.setHomeButtonEnabled(true);
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setDisplayShowTitleEnabled(true);
        bar.setHomeAsUpIndicator(R.drawable.back_button);
        bar.setTitle("License");
    }

}