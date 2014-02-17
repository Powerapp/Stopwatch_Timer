package com.angelicag.app;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;

public class TimerService extends Service implements Handler.Callback {
    private static final int NOTIFICATION_ID = 1000;
    private static final int COUNTDOWN_TIMER_MSG = 2000;
    private static final int TIMER_START_VALUE = 30000;
    private final LocalBinder mLocalBinder = new LocalBinder();
    private TimerCallback mTimerCallback;
    private Notification mNotification;
    NotificationCompat.Builder builder;
    private long mTimerMsLeft;
    private Handler mHandler;
    private long mStartTime;
    private Ringtone timerAlarm;
    private boolean timerRunning;


    public TimerService() {
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (msg.what == COUNTDOWN_TIMER_MSG) {
            long now = SystemClock.elapsedRealtime();
            mTimerMsLeft = TIMER_START_VALUE - (now - mStartTime);
            if (mTimerMsLeft >= 0 && timerRunning) {
                notifyTimerCallback();
                //Notification
                builder.setContentText(getString(R.string.notification_message, mTimerMsLeft / 1000));
                startForeground(NOTIFICATION_ID, builder.build());
                //-----
                mHandler.sendEmptyMessageDelayed(COUNTDOWN_TIMER_MSG, 100);

            } else {

                onTimerFinished();
                timerRunning = false;
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
    public void showNotification() {

        builder = new NotificationCompat.Builder(this)
                .setContentTitle("TIMER!!!!")
                .setContentText("STARTAD!")
                .setSmallIcon(R.drawable.ic_launcher);

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("page", 0);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

// Creates the PendingIntent
        PendingIntent notifyIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        intent,
                        0);
// Puts the PendingIntent into the notification builder
        builder.setContentIntent(notifyIntent);


        // intent triggered, you can add other intent for other actions
       /* Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("page", 0);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pIntent = PendingIntent.getActivity(TimerService.this,
                1,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

         // this is it, we'll build the notification!
        // in the addAction method, if you don't want any icon, just set the first param to 0

        builder = new NotificationCompat.Builder(this)

                .setContentTitle("Timer!")
                .setContentText("Timer Started")
                .setSmallIcon(R.drawable.ic_launcher);
                //.setContentIntent(pIntent)

               // .addAction(0, "Remind", pIntent);

        builder.setContentIntent(pIntent);
*/
    }

    //-----------------------------------------------------------------------------------------------

    public boolean isTimerRunning() {
        return timerRunning;
    }

    public class LocalBinder extends Binder {
        public TimerService getService() {
            return TimerService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mLocalBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new Handler(getMainLooper(), this);
    }

    public void startTimer() {
        startService(new Intent(this, getClass()));
        startTimeInForeground();
        showNotification();
    }

    private void startTimeInForeground() {
        // TODO Kontrollera ifall notification redan är satt!!!
        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentTitle(getString(R.string.timer_notification_title));
        mNotification = builder.getNotification();
        startForeground(NOTIFICATION_ID, mNotification);

        timerRunning = true;
        mStartTime = SystemClock.elapsedRealtime();
        mTimerMsLeft = TIMER_START_VALUE;
        mHandler.sendEmptyMessage(COUNTDOWN_TIMER_MSG);

    }

    public void stopTimer() {
        timerRunning = false;

    }

    public void stopAlarm() {
        timerAlarm.stop();

    }

    public void onTimerFinished() {

        if (mTimerMsLeft <= 0) {
            Uri alarm = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            timerAlarm = RingtoneManager.getRingtone(getApplicationContext(), alarm);
            timerAlarm.play();

            Runnable mMyRunnable = new Runnable() {
                @Override
                public void run() {
                    timerAlarm.stop();
                }
            };

            Handler mStopAlarmHandler = new Handler();
            mStopAlarmHandler.postDelayed(mMyRunnable, 30000);

        }
    }

    public long getTimerValue() {
        return mTimerMsLeft;
    }

    private void notifyTimerCallback() {
        if (mTimerCallback != null) {
            mTimerCallback.onTimerValueChanged(mTimerMsLeft);
        }
    }

    public void setTimerCallback(TimerCallback timerCallback) {
        mTimerCallback = timerCallback;
    }

    public interface TimerCallback {
        void onTimerValueChanged(long timerValue);
    }
}
