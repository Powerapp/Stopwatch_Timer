package com.angelicag.app;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class MainActivity extends Activity implements ActionBar.TabListener, Handler.Callback, TimerService.TimerCallback, StopWatchService.StopwatchCallback {


    //Fragmenten
    ViewPager mViewPager;
    public static final int TIMER_POSITION = 0;
    public static final int STOPWATCH_POSITION = 1;
    private PlaceholderFragment mTimerFragment;
    private PlaceholderFragment mStopwatchFragment;

    //Variabel som håller värdet för vilken tab som ska visas i onResume
    private int pageID = 0;

    //Till laplisten
    private ListView lapList;
    private TextView value;
    private ArrayList<String> lapArray;
    private ArrayAdapter<String> adapter;
    SectionsPagerAdapter mSectionsPagerAdapter;

    //servicen
    private TimerService mTimerService;
    private TimerServiceConnection mTimerServiceConnection;

    private StopWatchService mStopwatchService;
    private StopWatchServiceConnection mStopwatchServiceConnection;
    private int sectionNumber = -1;

    public MainActivity() {
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        mTimerFragment = PlaceholderFragment.newInstance(0);
        mStopwatchFragment = PlaceholderFragment.newInstance(1);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {

            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }


    }

    //Hämtar värdet för vilken tab som ska visas, från .addAction "Eventet" i notifikationen
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
       // pageID = intent.getIntExtra("page",0);
        sectionNumber = intent.getIntExtra("page", -1);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (sectionNumber!= -1) {
           ((ViewPager)findViewById(R.id.pager)).setCurrentItem(sectionNumber);
        }

        mTimerServiceConnection = new TimerServiceConnection();
        bindService(new Intent(this, TimerService.class),
                mTimerServiceConnection, BIND_AUTO_CREATE);

        mStopwatchServiceConnection = new StopWatchServiceConnection();
        bindService(new Intent(this, StopWatchService.class),
                mStopwatchServiceConnection, BIND_AUTO_CREATE);

    }

    @Override
    protected void onPause() {
        super.onPause();

        mTimerService.setTimerCallback(null);
        unbindService(mTimerServiceConnection);

        mStopwatchService.setStopwatchCallback(null);
        unbindService(mStopwatchServiceConnection);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {


        getMenuInflater().inflate(R.menu.main, menu);

        lapList = (ListView) findViewById(R.id.laplist);
        value = (TextView) findViewById(R.id.stopwatch_value);
        lapArray = new ArrayList<String>();

        adapter = new ArrayAdapter<String>(getApplicationContext(),
                android.R.layout.simple_list_item_1, lapArray);
        lapList.setAdapter(adapter);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_settings) {

            Intent intent = new Intent(SettingsActivity.ACTION_SETTINGS);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }


    public void startStopwatch(View view) {
        if (mStopwatchService != null) {
            mStopwatchService.startStopwatch();

        }
    }

    public void stopStopwatch(View view) {
        if (mStopwatchService != null) {
            mStopwatchService.stopStopwatch();

        }
    }

    public void resetStopwatch(View view) {
        mStopwatchService.resetStopwatch();
        lapArray.clear();
        adapter.notifyDataSetChanged();
        mStopwatchFragment.updateStopwatchValue(0);

    }

    //varvlistan
    public void lapStopwatch(View view) {
        lapArray.add(value.getText().toString());
        adapter.notifyDataSetChanged();
    }

    public void startTimer(View view) {
        Button timerButton = (Button) view;

        if (mTimerService != null) {
            if (!mTimerService.isTimerRunning()) {
                timerButton.setText(getString(R.string.stop_timer_label));
                mTimerService.startTimer();
            } else {
                timerButton.setText(getString(R.string.start_timer_button_label));
                mTimerService.stopTimer();
            }

        }
    }

    public void stopAlarm(View view) {
        Button startTimer = (Button) findViewById(R.id.start_timer_button);
        startTimer.setText(getString(R.string.start_timer_button_label));
        mTimerService.stopTimer();
        mTimerService.stopAlarm();
    }

    @Override
    public boolean handleMessage(Message msg) {
        return false;
    }


    class StopWatchServiceConnection implements ServiceConnection {
        @Override

        public void onServiceConnected(ComponentName name, IBinder service) {
            mStopwatchService = ((StopWatchService.LocalBinder) service).getService();
            mStopwatchService.setStopwatchCallback(MainActivity.this);
            mStopwatchFragment.setStartStopwatchButtonEnabled(true);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mStopwatchFragment.setStartStopwatchButtonEnabled(false);
            mStopwatchService = null;
        }
    }

    class TimerServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mTimerService = ((TimerService.LocalBinder) service).getService();
            mTimerService.setTimerCallback(MainActivity.this);
            mTimerFragment.setTimerButtonEnabled(true);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mTimerFragment.setTimerButtonEnabled(false);
            mTimerService = null;
        }
    }

    @Override
    public void onTimerValueChanged(long timerValue) {
        mTimerFragment.updateTimerValue(timerValue);
    }

    @Override
    public void onStopwatchValueChanged(long stopwatchValue) {
        mStopwatchFragment.updateStopwatchValue(stopwatchValue);
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case TIMER_POSITION:
                    return mTimerFragment;
                case STOPWATCH_POSITION:
                    return mStopwatchFragment;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case TIMER_POSITION:
                    return getString(R.string.title_section2).toUpperCase(l);
                case STOPWATCH_POSITION:
                    return getString(R.string.title_section1).toUpperCase(l);
            }
            return null;
        }
    }

    public static class PlaceholderFragment extends Fragment {


        private static final String ARG_SECTION_NUMBER = "section_number";
        private TextView mTimerLabel;
        private TextView mStopwatchLabel;
        private Button mTimerButton;
        private Button mStartStopwatchButton;


        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            Bundle args = getArguments();
            int sectionNumber = args.getInt(ARG_SECTION_NUMBER);
            switch (sectionNumber) {
                case TIMER_POSITION:
                    View rootView = inflater.inflate(R.layout.timer_layout, container, false);
                    mTimerButton = (Button) rootView.findViewById(R.id.start_timer_button);
                    mTimerLabel = (TextView) rootView.findViewById(R.id.timer_value);
                    return rootView;
                case STOPWATCH_POSITION:
                    View stopwatchView = inflater.inflate(R.layout.stopwatch_layout, container, false);
                    mStartStopwatchButton = (Button) stopwatchView.findViewById(R.id.start_stopwatch_button);
                    mStopwatchLabel = (TextView) stopwatchView.findViewById(R.id.stopwatch_value);
                    return stopwatchView;
            }
            return null;
        }

        public void setTimerButtonEnabled(boolean enabled) {
            if (mTimerButton != null) {
                mTimerButton.setEnabled(enabled);
            }
        }

        public void updateTimerValue(long timerValue) {
            if (mTimerLabel != null) {
                mTimerLabel.setText(String.valueOf(timerValue / 1000));
            }

        }

        public void setStartStopwatchButtonEnabled(boolean enabled) {
            if (mStartStopwatchButton != null) {
                mStartStopwatchButton.setEnabled(enabled);
            }
        }

        public void updateStopwatchValue(long stopwatchValue) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

            CharSequence timeForStopwatch = simpleDateFormat.format(new Date(stopwatchValue));

            if (mStopwatchLabel != null) {
                mStopwatchLabel.setText(String.valueOf(timeForStopwatch));
            }

        }
    }

}