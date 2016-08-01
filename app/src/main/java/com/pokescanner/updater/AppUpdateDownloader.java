package com.pokescanner.updater;

/**
 * Created by Brian on 7/29/2016.
 */
public class AppUpdateDownloader extends Thread{
    String URL;

    public AppUpdateDownloader(String URL) {
        this.URL = URL;
    }


}
