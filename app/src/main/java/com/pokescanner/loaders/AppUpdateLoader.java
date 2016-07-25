package com.pokescanner.loaders;

import com.pokescanner.BuildConfig;
import com.pokescanner.events.AppUpdateEvent;
import com.pokescanner.helper.SemVer;
import com.pokescanner.objects.AppUpdate;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AppUpdateLoader extends Thread {

    @Override
    public void run() {
        OkHttpClient httpClient = new OkHttpClient();
        String apiEndpoint = "https://api.github.com/repos/BrianEstrada/PokeScanner/releases/latest";
        Request request = new Request.Builder()
                .url(apiEndpoint)
                .build();

        try {
            Response response = httpClient.newCall(request).execute();
            JSONObject latestRelease = new JSONObject(response.body().string());
            JSONObject releaseAssets = latestRelease.getJSONArray("assets").getJSONObject(0);

            AppUpdate update = new AppUpdate(releaseAssets.getString("browser_download_url"), latestRelease.getString("tag_name"), latestRelease.getString("body"));

            SemVer currentVersion = SemVer.parse(BuildConfig.VERSION_NAME);
            SemVer remoteVersion = SemVer.parse(update.getVersion());

            //Fuck java for not supporting operator overloading
            if (currentVersion.compareTo(remoteVersion) == -1) {
                //current version is smaller than remote version
                if (EventBus.getDefault().hasSubscriberForEvent(AppUpdateEvent.class)) {
                    EventBus.getDefault().post(new AppUpdateEvent(AppUpdateEvent.OK, update));
                }
            }

        } catch (JSONException | IOException e) {
            if (EventBus.getDefault().hasSubscriberForEvent(AppUpdateEvent.class)) {
                EventBus.getDefault().post(new AppUpdateEvent(AppUpdateEvent.FAILED));
            }
            e.printStackTrace();
        }
    }
}
