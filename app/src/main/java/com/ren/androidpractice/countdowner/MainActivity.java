package com.ren.androidpractice.countdowner;

import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity{
    private static final String TAG = "CountdownApp";
    private static final String EXIT_ACTION = "CountdownAppExit";
    private final ExitReceiver exitReceiver = new ExitReceiver();
    //    private Date test_date = null;
    private boolean handler_closed = false;
    private Handler ClockHandler = new Handler() {
        public void handleMessage(Message msg) {
//            Log.d(TAG, "msg.what:" + msg.what);
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    setTextClock(false);
                    break;
                case 2:
                    handler_closed = true;
                    toggle_notices();
                    break;
                case 3:
                    setTextClock(true);
                    break;
                default:
                    Toast.makeText(MainActivity.this, R.string.error_clock_handler, Toast.LENGTH_SHORT).show();
                    finish();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Set<String> set = savedInstanceState.keySet();
//        for(String str:set){
//            Log.d(TAG,"bundled state key:"+str+" value:"+savedInstanceState);
//        }
        setContentView(R.layout.activity_main);
        Locale.setDefault(Locale.CHINESE);
        initTitle();
        initSwitch();
        checkTimeZone();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "CountdownService";
            String channelName = "App:CountdownService";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            createNotificationChannel(channelId, channelName, importance);
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(EXIT_ACTION);
        registerReceiver(exitReceiver, filter);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        new Thread() {
            public void run() {
                while (!handler_closed) {
                    try {
                        Thread.sleep(1000);
                        ClockHandler.sendEmptyMessage(1);
                    } catch (Exception e) {
                        ClockHandler.sendEmptyMessage(4);
                        Log.e(TAG, e.toString());
                    }
                }
            }
        }.start();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(view ->
                Snackbar.make(view, "An App made by Jason Ren", BaseTransientBottomBar.LENGTH_LONG)
                .setAction("Action", null).show());

    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createNotificationChannel(String channelId, String channelName, int importance) {
        NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
        NotificationManager notificationManager = (NotificationManager) getSystemService(
                NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);
        Log.i(TAG,"Android version > 8, create channel name:"+channelName+" for notification display");
    }

    private void initTitle(){
        String title = getResources().getString(R.string.title_cn);
        String title_on_app = String.format(title, getString(R.string.countdown_name));
        TextView tv = (TextView) findViewById(R.id.notice);
        tv.setText(title_on_app);
    }

    private void initSwitch() {
        Switch switcher = (Switch) findViewById(R.id.toggle_sys_broadcast);
        Button but = findViewById(R.id.choose_time);

        switcher.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Intent i = new Intent(MainActivity.this, CountdownService.class);
            if (isChecked) {
                i.putExtra("switch-on", true);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(i);
                } else {
                    startService(i);
                }
                but.setEnabled(true);
                but.setOnClickListener(new View.OnClickListener() {
                    final Calendar calendar = Calendar.getInstance(Locale.CHINA);

                    @Override
                    public void onClick(View v) {
                        new TimePickerDialog(MainActivity.this, (view, hourOfDay, minute) -> {
                            Intent i1 = new Intent(MainActivity.this, CountdownService.class);
                            i1.putExtra("Alarm-hour", hourOfDay);
                            i1.putExtra("Alarm-minute", minute);
                            i1.putExtra("Start-by","CountdownMainActivity");
                            startService(i1);
                            Log.i(TAG, "Change alarm time to: " + hourOfDay + ":" + minute);
//                                    Log.i(TAG,""+i.getExtras().getInt("Alarm-hour"));
                        }
                                , calendar.get(Calendar.HOUR_OF_DAY)
                                , calendar.get(Calendar.MINUTE)+2, true).show();
                    }
                });
            } else {
                i.putExtra("switch-on", false);
                but.setEnabled(false);
                stopService(i);
            }
        });
    }

    private void checkTimeZone(){
        TimeZone timezone = TimeZone.getDefault();
        if (!timezone.getID().contains("Asia/Shanghai")) {
            Toast.makeText(MainActivity.this, R.string.error_timezone, Toast.LENGTH_SHORT).show();
        }
    }

    private Date getTestDate() {
        String format = getResources().getString(R.string.format_date_cn_desc);
        SimpleDateFormat pre_sdf = new SimpleDateFormat(format, Locale.SIMPLIFIED_CHINESE);
        pre_sdf.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        try {
            return pre_sdf.parse(getResources().getString(R.string.test_date));
        } catch (ParseException e) {
            ClockHandler.sendEmptyMessage(4);
            Log.e(TAG, e.toString());
        }
        return new GregorianCalendar(2020,1,9,
                8,0,0).getTime();
    }

    private void toggle_notices() {
        TextView notice_review = findViewById(R.id.noticereview);
        notice_review.setText(getResources().getString(R.string.test_finished));
        notice_review.setTextColor(getResources().getColor(R.color.passedText));
    }

    private void setTextClock(boolean sendIntentToService) {
        Date date = new Date(System.currentTimeMillis());
        Date test_date = getTestDate();
//        Log.d(TAG, "test_date: " + test_date.toString() + " System date: " + date.toString());
        TextClock tc = findViewById(R.id.textClock);
        long delta = test_date.getTime() - date.getTime();
        Log.d(TAG, "Time delta: " + delta);
        if (delta <= 0) {
            ClockHandler.sendEmptyMessage(2);   //期末考试已经结束，不再设置clockhandler
            handler_closed = true;
        } else {
            long day = delta / (24 * 60 * 60 * 1000);
            long hour = (delta / (60 * 60 * 1000) - day * 24);
            long minute = ((delta / (60 * 1000)) - day * 24 * 60 - hour * 60);
            long sec = (delta / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - minute * 60);
            int id = 0;
            if (day <= 0) {
                if (hour <= 0) {
                    if (minute <= 0) {
                        id = R.string.format_countdown_s;
                    } else {
                        id = R.string.format_countdown_cn_ms;
                    }
                } else {
                    id = R.string.format_countdown_cn_hms;
                }
            } else {
                id = R.string.format_countdown_cn_dhms;
            }
            SimpleDateFormat sdf = new SimpleDateFormat(getResources().getString(id),
                    Locale.getDefault());
            //sdf.setTimeZone();
            if (tc.is24HourModeEnabled()) {
                tc.setFormat24Hour(sdf.format(delta));
            } else {
                tc.setFormat12Hour(sdf.format(delta));
            }
            Log.i(TAG, "Displays: " + tc.getText().toString());
            if(sendIntentToService){
                Intent i = new Intent(this, CountdownService.class);
                i.putExtra("name","countdowner main activity");
                i.putExtra("DisplayStr",tc.getText().toString());
                startService(i);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
        Log.d(TAG, "item:" + item.getItemId());
        switch (item.getItemId()) {
            case R.id.action_show_author:
                String author_str = getResources().getString(R.string.author_more_info);
                final AlertDialog.Builder normalDialog =
                        new AlertDialog.Builder(MainActivity.this);
                normalDialog.setTitle(R.string.author_info_title).setMessage(author_str).setNeutralButton(R.string.button_return,
                        (dialog, which) -> dialog.dismiss()).show();
                break;
            case R.id.action_quit_app:
                new AlertDialog.Builder(this).setTitle(R.string.confirm_exit)
                        .setPositiveButton(R.string.button_confirm, (dialog, which) -> {
                            sendBroadcast(new Intent("CountdownAppExit"));
                            handler_closed = true;
                        })
                        .setNegativeButton(R.string.button_cancel, (dialog, which) -> dialog.dismiss()).show();
                break;
            default:
                return true;
        }
        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_quit_app) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    public void onBackPressed() {
        new AlertDialog.Builder(this).setTitle(R.string.confirm_exit)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton(R.string.button_confirm, (dialog, which) -> {
                    sendBroadcast(new Intent("CountdownAppExit"));
                    handler_closed = true;
                })
                .setNegativeButton(R.string.button_cancel, (dialog, which) -> dialog.dismiss()).show();
    }

    public void onDestroy() {
        super.onDestroy();
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancelAll();
        unregisterReceiver(exitReceiver);
    }

    class ExitReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v(TAG,"Exit signal received!");
            //收到广播时，finish
            MainActivity.this.finish();
        }
    }
}
