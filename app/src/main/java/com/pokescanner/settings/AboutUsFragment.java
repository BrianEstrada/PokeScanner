package com.pokescanner.settings;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.pokescanner.R;

public class AboutUsFragment extends PreferenceFragment {

    private Context mContext;
    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_about_us, container, false);
        mContext = getActivity();
        setupToolbar();
        ((Button) rootView.findViewById(R.id.donateButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.me/brianestrada"));
                startActivity(i);
            }
        });
        return rootView;
    }

    private void setupToolbar() {
        AppCompatPreferenceActivity activity = (AppCompatPreferenceActivity) getActivity();
        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.aboutUsToolbar);
        activity.setSupportActionBar(toolbar);

        ActionBar bar = activity.getSupportActionBar();
        bar.setHomeButtonEnabled(true);
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setDisplayShowTitleEnabled(true);
        bar.setHomeAsUpIndicator(R.drawable.back_button);
        bar.setTitle(com.pokescanner.R.string.about_us);
    }
}
