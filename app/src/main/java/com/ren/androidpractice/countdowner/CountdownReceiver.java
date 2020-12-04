package com.ren.androidpractice.countdowner;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.Set;


public class CountdownReceiver extends BroadcastReceiver {
    private static final String channelId = "CountdownService";
    private static final String channelName = "App:CountdownService";
    private static final String TAG = "CountdownApp::BroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        Set<String> strings = bundle.keySet();
        for (String str:strings) {
            Log.v(TAG,"intent param ::"+ str + " value: "+bundle.get(str));
        }

        Intent intentClick = new Intent(context, MainActivity.class);
        intentClick.putExtra("Source", "from-notification");
        PendingIntent pendingIntentClick = PendingIntent.getBroadcast(context, 0,
                intentClick, PendingIntent.FLAG_ONE_SHOT);

        Intent cleanNotification = new Intent("clear_notification");
        PendingIntent clean = PendingIntent.getBroadcast(context,0,
                cleanNotification,PendingIntent.FLAG_ONE_SHOT);

        String time_delta = "";
        String message = "";
        if ("countdowner main activity".equals(intent.getStringExtra("name"))) {
            time_delta = intent.getStringExtra("DisplayStr");
            message = context.getString(R.string.service_broadcast_receiver_message);
        } else if ("countdowner service".equals(intent.getStringExtra("name"))) {
            time_delta = intent.getStringExtra("time_delta");
            message = context.getString(R.string.service_broadcast_receiver_message);
        } else {
            message = context.getString(R.string.error_broadcast_receiver);
        }
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new NotificationCompat.Builder(context, channelId).setContentTitle("CountdownApp")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
                .setContentText(message)
                .setShowWhen(true)
                .setWhen(System.currentTimeMillis())
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setDefaults(NotificationCompat.DEFAULT_LIGHTS)
                .setOngoing(false)    //可以滑动删除
                .setContentIntent(pendingIntentClick)
                .setDeleteIntent(clean)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .build();


        if("clear_notification".equals(intent.getAction())){
            manager.cancel(233);
        }else{
            manager.notify(233, notification);
        }
        Log.v(TAG, "ALARMED! "+message);

//        Intent i = new Intent(context, CountdownService.class);
//        i.putExtra("Alarm-hour", intent.getIntExtra("Alarm-hour", 8));
//        i.putExtra("Alarm-minute", intent.getIntExtra("Alarm-minute", 0));
//        i.putExtra("Start-by","CountdownBroadcastReceiver");
//        context.startService(i);
    }


}
