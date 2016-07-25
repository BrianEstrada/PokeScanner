package com.pokescanner.service;

import android.app.IntentService;
import android.content.Intent;

/**
 * Created by Brian on 7/23/2016.
 */
public class ScanService extends IntentService {

    public ScanService() {
        super(null);
    }
    public ScanService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }
}
