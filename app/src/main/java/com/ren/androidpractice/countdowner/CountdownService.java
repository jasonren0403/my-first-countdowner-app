package com.ren.androidpractice.countdowner;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.Set;
import java.util.TimeZone;

public class CountdownService extends Service {
    private static final String TAG = "CountdownApp::Service";
    private NotificationManager notificationManager;
    private static final String notificationId = "CountdownService";
    private static final String notificationName = "App:CountdownService";

    public CountdownService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(notificationId, notificationName, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }
        Notification notification = new NotificationCompat.Builder(this, notificationId).setDefaults(Notification.DEFAULT_SOUND)
                .setContentTitle("CountdownerApp:start")
                .setContentText("start").build();

        startForeground(1, notification);


//        Toast.makeText(getApplicationContext(),R.string.service_init_start_cn,Toast.LENGTH_SHORT).show();
        Log.v(TAG, "Service created on onCreate()");
    }


    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent i = new Intent(this, CountdownReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);

        manager.cancel(pi);
        Log.v(TAG, "Service stopped");

        Toast.makeText(getApplicationContext(), R.string.service_stop, Toast.LENGTH_SHORT).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String delta = "";
        int minute, hr;
        String hour = "", min = "";
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent i = new Intent(this, CountdownReceiver.class);
        if ("countdowner main activity".equals(intent.getStringExtra("name"))) {
            delta = intent.getStringExtra("DisplayStr");
            i.putExtra("time_delta", delta);
            i.putExtra("name", "countdowner service");
            PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
            manager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), AlarmManager.INTERVAL_DAY, pi);
        } else {
            Notification notification = new NotificationCompat.Builder(this, notificationId)
                    .setContentTitle("CountdownerApp:start")
                    .setContentText("start").build();
            startForeground(1, notification);

            minute = intent.getIntExtra("Alarm-minute", 0);
            hr = intent.getIntExtra("Alarm-hour", 8);
            if (minute == 0) min = "00";
            else if (0 < minute && minute < 10) {
                min = "0" + minute;
            } else {
                min = "" + minute;
            }
            if (hr == 0) hour = "00";
            else if (0 < hr && hr < 10) {
                hour = "0" + hr;
            } else {
                hour = "" + hr;
            }
            i.putExtra("Alarm-minute", minute);
            i.putExtra("Alarm-hour", hr);
            Log.v(TAG, String.format("Service started on a command,flags:%d,Intent:%s", flags,intent.toString()));
            Bundle bundle = intent.getExtras();
            Set<String> strings = bundle.keySet();
            for (String str:strings) {
                Log.v(TAG,"intent param ::"+ str + " value: "+bundle.get(str));
            }
            Log.d(TAG, "minute: " + minute + " hour:" + hr);
            i.putExtra("name", "countdowner service");
            //todo:想办法获得MainActivity当前的time_delta值

            PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
            long offset_secs = hr * minute * 60;
            long current = System.currentTimeMillis();
            long zero = current/(1000*3600*24)*(1000*3600*24) - TimeZone.getDefault().getRawOffset();
//            manager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), AlarmManager.INTERVAL_DAY, pi);
            String res = getResources().getString(R.string.service_start);
            manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, zero + offset_secs*1000, pi);
            String message = String.format(res, hour, min);
            Log.d(TAG, message);
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        }

        return super.onStartCommand(intent, flags, startId);
    }


}
