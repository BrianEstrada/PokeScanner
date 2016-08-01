package com.pokescanner.updater;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.pokescanner.R;

import java.io.File;

import io.fabric.sdk.android.BuildConfig;

public class AppUpdateDialog {
    public static void downloadAndInstallAppUpdate(Context context, AppUpdate update) {
        try {
            String destination = context.getExternalFilesDir(null) + "/";
            String fileName = "update.apk";
            destination += fileName;
            final Uri uri = Uri.parse("file://" + destination);

            //Delete update file if exists
            File file = new File(destination);
            if (file.exists()) {
                file.delete();
            }


            //set downloadmanager
            String url = update.getAssetUrl();
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setTitle(context.getString(R.string.updating_pokescanner));

            //set destination
            request.setDestinationUri(uri);

            // get download service and enqueue file
            final DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            final long downloadId = manager.enqueue(request);

            if (!BuildConfig.DEBUG) {
                Answers.getInstance().logCustom(new CustomEvent("AppSelfUpdate"));
            }

            //set BroadcastReceiver to install app when .apk is downloaded
            BroadcastReceiver onComplete = new BroadcastReceiver() {
                public void onReceive(Context ctxt, Intent intent) {
                    Intent install = new Intent(Intent.ACTION_VIEW);
                    install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    install.setDataAndType(uri, manager.getMimeTypeForDownloadedFile(downloadId));
                    ctxt.startActivity(install);
                    ctxt.unregisterReceiver(this);
                }
            };
            //register receiver for when .apk download is complete
            context.registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        } catch (Exception e) {
            System.out.println("We have an error houston");
        }
    }
}
