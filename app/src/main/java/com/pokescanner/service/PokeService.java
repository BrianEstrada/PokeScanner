package com.pokescanner.service;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.pokescanner.R;

/**
 * Created by Brian on 7/26/2016.
 */
public class PokeService extends IntentService {
    Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    long[] Vibration = new long[]{1000,1000};

    public PokeService() {
        super("PokeScanner");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Intent stopRecieve = new Intent();
        stopRecieve.setAction("STOP_ACTION");
        PendingIntent pendingIntentStop = PendingIntent.getBroadcast(this, 0, stopRecieve, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.icon)
                .addAction(new NotificationCompat.Action(R.drawable.ic_settings_black_24dp,"Cancel Service",pendingIntentStop))
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.icon))
                .setContentTitle("Poke Scanner")
                .setVibrate(Vibration)
                .setSound(alarmSound)
                .setLights(Color.RED, 3000, 3000)
                .setContentText("Hello World!");
        int mNotificationId = 213;

        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        mNotifyMgr.notify(mNotificationId, mBuilder.build());
    }
}