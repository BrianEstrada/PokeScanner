package com.pokescanner.updater;

import android.support.v7.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.pokescanner.R;
import com.pokescanner.SplashScreenActivity;

import java.io.File;

import io.fabric.sdk.android.BuildConfig;

public class AppUpdateDialog {
    public AppUpdateDialog(final Context context, final AppUpdate update) {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(context)
                .setTitle(R.string.update_available_title)
                .setMessage(context.getString(R.string.app_name) + " " + update.getVersion() + " " + context.getString(R.string.update_available_long) + "\n\n" + context.getString(R.string.changes) + "\n\n" + update.getChangelog())
                .setIcon(R.mipmap.ic_launcher)
                .setPositiveButton(context.getString(R.string.update), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        downloadAndInstallAppUpdate(context, update);
                    }
                })
                .setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                            if (context instanceof SplashScreenActivity)
                                ((SplashScreenActivity) context).goToLoginScreen();
                    }
                })
                .setCancelable(false);

        dialog.show();
    }

    void downloadAndInstallAppUpdate(Context context, AppUpdate update) {
        try {
            String destination = context.getExternalFilesDir(null) + "/";
            String fileName = "update.apk";
            destination += fileName;
            final Uri uri = Uri.parse("file://" + destination);

            //Delete update file if exists
            File file = new File(destination);
            if (file.exists()) file.delete();


            //set downloadmanager
            String url = update.getAssetUrl();
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setTitle(context.getString(R.string.updating_pokescanner));

            //set destination
            request.setDestinationUri(uri);

            // get download service and enqueue file
            final DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            final long downloadId = manager.enqueue(request);

            //set BroadcastReceiver to install app when .apk is downloaded
            BroadcastReceiver onComplete = new BroadcastReceiver() {
                public void onReceive(Context ctxt, Intent intent) {
                    if (!BuildConfig.DEBUG)
                        Answers.getInstance().logCustom(new CustomEvent("AppSelfUpdate"));
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
