package com.pokescanner.service;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Brian on 7/26/2016.
 */

public class ServerPollReceiver extends BroadcastReceiver {
    public static final int REQUEST_CODE = 12345;

    public void cancelAlarm(Context context) {
        Intent intent = new Intent(context, ServerPollReceiver.class);
        final PendingIntent pIntent = PendingIntent.getBroadcast(context, ServerPollReceiver.REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pIntent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("Something");
        if (intent.getAction() != null) {
            if (intent.getAction().equals("STOP_ACTION")) {
                NotificationManager mNotifyMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                mNotifyMgr.cancel(213);
                cancelAlarm(context);
            }
        }else{
            Intent i = new Intent(context, PokeService.class);
            context.startService(i);
        }

    }
}
