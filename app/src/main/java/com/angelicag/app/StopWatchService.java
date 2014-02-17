package com.angelicag.app;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;

public class StopWatchService extends Service implements Handler.Callback {

    private static final long FIVEHUNDRED_MILLIS = 200;
    private static final int UPDATE_STOPWATCH_VALUE = 1001;
    private long mStartTimeStopwatch;
    private Handler mStopwatchHandler;
    private boolean mStopwatchIsRunning = false;
    private long mLastTime;
    private StopwatchCallback mStopwatchCallback;
    private final LocalBinder mLocalBinder = new LocalBinder();
    private long elapsed;

    //Notification
    private static final int NOTIFICATION_ID = 2002;
    NotificationCompat.Builder builder;
    private String ns;

    public class LocalBinder extends Binder {
        public StopWatchService getService() {
            return StopWatchService.this;
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (msg.what == UPDATE_STOPWATCH_VALUE) {
            updateStopwatchValue();
            notifyStopWatchCallback(elapsed);


            if (mStopwatchIsRunning) {
                //Notification
                builder.setContentText(getString(R.string.stopwacth_notification_label, elapsed / 1000));
                startForeground(NOTIFICATION_ID, builder.build());
                //-----
                mStopwatchHandler.sendEmptyMessageDelayed(UPDATE_STOPWATCH_VALUE, FIVEHUNDRED_MILLIS);

            }
        }
        return false;
    }

    //----------------------------------------NOTIFICATION----------------------------------------------

    public class NotificationReceiver extends Activity {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void createNotification() {

        builder = new NotificationCompat.Builder(this)
                .setContentTitle("STOPWATCH!!!!")
                .setContentText("STARTAD!")
                .setSmallIcon(R.drawable.ic_launcher);

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("page", 1);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);


// Creates the PendingIntent
        PendingIntent notifyIntent =
                PendingIntent.getActivity(
                        this,
                        1,
                        intent,
                        0);

// Puts the PendingIntent into the notification builder
        builder.setContentIntent(notifyIntent);


        /*
        // define sound URI, the sound to be played when there's a notification
        //Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // intent triggered, you can add other intent for other actions
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("page", 1);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pIntent = PendingIntent.getActivity(StopWatchService.this,
                2,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // this is it, we'll build the notification!
        // in the addAction method, if you don't want any icon, just set the first param to 0

        builder = new NotificationCompat.Builder(this)

                .setContentTitle("Stopwatch!")
                .setContentText("Stopwatch Started")
                .setSmallIcon(R.drawable.ic_launcher);
        //.setContentIntent(pIntent)

        // .addAction(0, "Remind", pIntent);

        builder.setContentIntent(pIntent);
*/
    }

    //-----------------------------------------------------------------------------------------------


    @Override
    public IBinder onBind(Intent intent) {
        return mLocalBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mStopwatchHandler = new Handler(getMainLooper(), this);
    }

    private void startStopwatchInForeground() {
        // TODO Kontrollera ifall notification redan är satt!!!
        /*Notification.Builder builder = new Notification.Builder(this);
        builder.setContentTitle(getString(R.string.timer_notification_title));
        mNotification = builder.getNotification();
        startForeground(NOTIFICATION_ID, mNotification);*/


        mStopwatchIsRunning = true;

        if (mStartTimeStopwatch == 0) {
            mStartTimeStopwatch = SystemClock.elapsedRealtime();
        } else {
            mStartTimeStopwatch = SystemClock.elapsedRealtime() - mLastTime;
        }
        updateStopwatchValue();
        //showStopWatchNotification();
        mStopwatchHandler.sendEmptyMessageDelayed(UPDATE_STOPWATCH_VALUE, FIVEHUNDRED_MILLIS);

    }

    public void startStopwatch() {
        startService(new Intent(this, getClass()));
        startStopwatchInForeground();
        createNotification();
    }

    public void stopStopwatch() {
        mStopwatchIsRunning = false;
        mLastTime = SystemClock.elapsedRealtime() - mStartTimeStopwatch;
    }


    public void resetStopwatch() {

        mStartTimeStopwatch = SystemClock.elapsedRealtime();
        mStopwatchIsRunning = false;
        if (!mStopwatchIsRunning) {
            mLastTime = 0;
        }
    }


    private void updateStopwatchValue() {
        elapsed = SystemClock.elapsedRealtime() - mStartTimeStopwatch;
    }

    private void notifyStopWatchCallback(long elapsed) {
        if (mStopwatchCallback != null) {
            mStopwatchCallback.onStopwatchValueChanged(elapsed);
        }

    }

    public void setStopwatchCallback(StopwatchCallback stopwatchCallback) {
        mStopwatchCallback = stopwatchCallback;
    }

    public interface StopwatchCallback {
        void onStopwatchValueChanged(long stopwatchValue);
    }

}


