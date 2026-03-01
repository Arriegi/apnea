package eus.elkarmedia.apnea;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private final static int STARTED = 1;
    private final static int PAUSED = 2;
    private final static int STOPPED = 3;

    private SensorManager sensorManager;
    private PowerManager.WakeLock lock;

    private SleepDbHelper dbHelper;

    private TextView statusTextView, leftTextView, rightTextView, backTextView, stomachTextView,
            getUpTextView, totalsTextView, totalAlarmsTextView;
    private long left = 0, right = 0, back = 0, stomach = 0, up = 0, total = 0;
    private double lastUpdateX = 0, lastUpdateY = 0, lastUpdateZ = 0;
    private int leftCount = 0, rightCount = 0, backCount = 0, stomachCount = 0, upCount = 0,
            totalCount = 0, streak = 0, pauseLeft = 0;

    private int status = STOPPED;
    private String countTimePct = "%1$d / %2$s  (%3$.2f %%)";
    private long lastUpdate = 0;
    private Timer pauseTimer = new Timer();

    private AlarmsManager alarmsManager;

    private static int TIME_TO_VIBRATE_IN_SECONDS;
    private static int TIME_TO_SOUND_IN_SECONDS;
    private static int MAX_VOLUME_STREAK_IN_SECONDS;
    private static int PAUSE;
    private static boolean ALARM_STOMACH;
    private static String ORIENTATION;
    private static int TIME_TO_START_NOTICING;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        lastUpdate = SystemClock.elapsedRealtime();

        checkPermissions();

        dbHelper = new SleepDbHelper(this);

        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        lock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "apnea:wakelock");

        // Instantiate textViews
        statusTextView = (TextView) findViewById(R.id.statusTextView);
        leftTextView = (TextView) findViewById(R.id.leftTextView);
        rightTextView = (TextView) findViewById(R.id.rightTextView);
        backTextView = (TextView) findViewById(R.id.backTextView);
        stomachTextView = (TextView) findViewById(R.id.stomachTextView);
        getUpTextView = (TextView) findViewById(R.id.getUpTextView);
        totalsTextView = (TextView) findViewById(R.id.totalsTextView);
        totalAlarmsTextView = (TextView) findViewById(R.id.totalAlarmsTextView);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        alarmsManager = new AlarmsManager(this);

        showTimes();
    }

    @Override
    protected void onResume() {
        super.onResume();
        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);
        updateVarsFromPreferences();
        alarmsManager.setMaxVolumeStreakInSeconds(MAX_VOLUME_STREAK_IN_SECONDS);
    }

    private void showTimes() {
        String leftString = String.format(countTimePct, leftCount, getHoursMinutesSeconds(left),
                total != 0 ? (left * 100 / total) : 0.0);
        String rightString = String.format(countTimePct, rightCount, getHoursMinutesSeconds(right),
                total != 0 ? (right * 100 / total) : 0.0);
        String backString = String.format(countTimePct, backCount, getHoursMinutesSeconds(back),
                total != 0 ? (back * 100 / total) : 0.0);
        String stomachString = String.format(countTimePct, stomachCount, getHoursMinutesSeconds(stomach),
                total != 0 ? (stomach * 100 / total) : 0.0);
        String upString = String.format(countTimePct, upCount, getHoursMinutesSeconds(up),
                total != 0 ? (up * 100 / total) : 0.0);
        String totalsString = String.format(countTimePct, totalCount, getHoursMinutesSeconds(total), 100.0);

        leftTextView.setText(leftString);
        rightTextView.setText(rightString);
        backTextView.setText(backString);
        stomachTextView.setText(stomachString);
        getUpTextView.setText(upString);
        totalsTextView.setText(totalsString);

        Resources res = getResources();
        String text = String.format(res.getString(R.string.totalAlarms), alarmsManager.getTotalVibrate(),
                alarmsManager.getTotalSound());
        totalAlarmsTextView.setText(text);
    }

    private String getHoursMinutesSeconds(double time) {
        return TimeFormatter.formatMillis(time);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_play) {
            startFlightMode();
            alarmsManager.setHasToNotice(false);
            stopPause();
            if (status == STOPPED) {
                initializeSleep();
                showTimes();
            }
            startListeningSensor();
            startSleepTrackingService();
            statusTextView.setText(R.string.sleeping);
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    alarmsManager.setHasToNotice(true);
                    statusTextView.setText(R.string.started);
                }
            }, TIME_TO_START_NOTICING);
            return true;
        } else if (id == R.id.action_pause) {
            if (status == PAUSED) {
                stopPause();
                startPause();
                return true;
            }
            alarmsManager.stopSounds();
            alarmsManager.stopVibrating();
            status = PAUSED;
            statusTextView.setText(R.string.paused);
            Log.d("JON", "stop listening sensor");
            sensorManager.unregisterListener(this);
            stopSleepTrackingService();
            startPause();
            return true;
        } else if (id == R.id.action_stop) {
            stopFlightMode();
            alarmsManager.stopSounds();
            alarmsManager.stopVibrating();
            stopPause();
            Sleep newSleep = new Sleep(0, left, leftCount, right, rightCount, back, backCount,
                    stomach, stomachCount, up, upCount, (left + right + back + stomach + up),
                    (leftCount + rightCount + stomachCount + upCount + backCount), 0);
            status = STOPPED;
            statusTextView.setText(R.string.stopped);
            Log.d("JON", "stop listening sensor");
            sensorManager.unregisterListener(this);
            stopSleepTrackingService();
            if (lock.isHeld())
                lock.release();
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            newSleep.storeOnDB(db);
            //saveSleepOnCloud();
            alarmsManager.setHasToNotice(false);
            return true;
        } else if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_stats) {
            Intent intent = new Intent(this, StatsActivity.class);
            startActivity(intent);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void startFlightMode() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Intent panelIntent = new Intent(android.provider.Settings.Panel.ACTION_WIFI);
                startActivity(panelIntent);
            } else {
                WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                wifiManager.setWifiEnabled(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopFlightMode() {
        try {

        } catch (Exception e) {

        }
    }

    private void startListeningSensor() {
        Log.d("JON", "start listening sensor");
        status = STARTED;
        if (!lock.isHeld())
            lock.acquire(99999999L);
        if (Build.VERSION.SDK_INT >= 19) {
            sensorManager.registerListener(this,
                    sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                    SensorManager.SENSOR_DELAY_UI, 1000000);
            statusTextView.setText(R.string.started);
        } else {
            sensorManager.registerListener(this,
                    sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                    SensorManager.SENSOR_DELAY_NORMAL);
            statusTextView.setText(R.string.started);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.d("JON", "sensor event changed");
        int lastPosition = getLastPosition();
        int currentPosition = PostureCalculator.getPosition(event.values[0], event.values[1], event.values[2],
                ORIENTATION);
        boolean hasChangedPosition = lastPosition != currentPosition;
        long now = SystemClock.elapsedRealtime();
        long lapsed = (now - lastUpdate);
        if (lapsed > 1000) {
            Log.d("JON", "time lapsed +1s");
            showTimes();
            total += lapsed;
            if (currentPosition == PostureCalculator.RIGHT) {
                alarmsManager.stopSounds();
                alarmsManager.stopVibrating();
                if (hasChangedPosition)
                    rightCount++;
                right += lapsed;
            } else if (currentPosition == PostureCalculator.LEFT) {
                alarmsManager.stopSounds();
                alarmsManager.stopVibrating();
                if (hasChangedPosition)
                    leftCount++;
                left += lapsed;
            } else if (currentPosition == PostureCalculator.BACK) {
                if (hasChangedPosition)
                    backCount++;
                back += lapsed;
            } else if (currentPosition == PostureCalculator.STOMACH) {
                if (hasChangedPosition)
                    stomachCount++;
                stomach += lapsed;
            } else if (currentPosition == PostureCalculator.UP) {
                alarmsManager.stopSounds();
                alarmsManager.stopVibrating();
                if (hasChangedPosition)
                    upCount++;
                up += lapsed;
            } else {
                Log.d("sensor", "unknown body position");
            }
            if (hasChangedPosition) {
                totalCount++;
                streak = 1;
            } else {
                streak++;
            }
            if (streak > TIME_TO_VIBRATE_IN_SECONDS + 1 && streak < TIME_TO_SOUND_IN_SECONDS &&
                    (currentPosition == PostureCalculator.BACK
                            || (ALARM_STOMACH && currentPosition == PostureCalculator.STOMACH))) {
                alarmsManager.vibrate();
            }
            if (streak > TIME_TO_SOUND_IN_SECONDS &&
                    (currentPosition == PostureCalculator.BACK
                            || (ALARM_STOMACH && currentPosition == PostureCalculator.STOMACH))) {
                alarmsManager.sound();
            }
            // Update last... for next second
            lastUpdate = now;
            lastUpdateX = event.values[0];
            lastUpdateY = event.values[1];
            lastUpdateZ = event.values[2];
        }
    }

    private void startPause() {
        pauseTimer = new Timer();
        pauseTimer.schedule(new UpdatePauseTime(), 0, 1000);
    }

    private void stopPause() {
        pauseLeft = PAUSE;
        pauseTimer.cancel();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private int getLastPosition() {
        if (lastUpdateX == 0 && lastUpdateY == 0 && lastUpdateZ == 0) {
            return -1;
        }
        return PostureCalculator.getPosition(lastUpdateX, lastUpdateY, lastUpdateZ, ORIENTATION);
    }

    private void initializeSleep() {
        total = 0;
        totalCount = 0;
        up = 0;
        upCount = 0;
        back = 0;
        backCount = 0;
        left = 0;
        leftCount = 0;
        right = 0;
        rightCount = 0;
        stomach = 0;
        stomachCount = 0;
        lastUpdate = SystemClock.elapsedRealtime();
        lastUpdateX = 0;
        lastUpdateY = 0;
        lastUpdateZ = 0;
        status = STOPPED;
        alarmsManager.resetCounters();
    }

    private void updateVarsFromPreferences() {
        // Load shared preferences or defaults
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        TIME_TO_VIBRATE_IN_SECONDS = Integer.valueOf(prefs.getString("seconds_to_vibrate", "3"));
        TIME_TO_SOUND_IN_SECONDS = Integer.valueOf(prefs.getString("seconds_to_sound", "12"));
        MAX_VOLUME_STREAK_IN_SECONDS = Integer.valueOf(prefs.getString("volume_raise", "4"));
        PAUSE = Integer.valueOf(prefs.getString("pause_in_minutes", "10")) * 60;
        ALARM_STOMACH = prefs.getBoolean("alarm_stomach_bool", false);
        TIME_TO_START_NOTICING = Integer.valueOf(prefs.getString("minutes_to_start", "0")) * 60 * 1000;
        ORIENTATION = prefs.getString("device_orientation", "Left");
        if (status != PAUSED) {
            pauseLeft = PAUSE;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        alarmsManager.stopSounds();
        alarmsManager.stopVibrating();
        stopSleepTrackingService();
        Log.d("JON", "stop listening sensor");
        sensorManager.unregisterListener(this);
        dbHelper.close();
        alarmsManager.restoreInitialVolume();
        if (lock.isHeld()) {
            lock.release();
        }
    }

    class UpdatePauseTime extends TimerTask {

        private String twoDigitString(int number) {
            if (number == 0) {
                return "00";
            }
            if (number / 10 == 0) {
                return "0" + number;
            }
            return String.valueOf(number);
        }

        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (pauseLeft == 0) {
                        startListeningSensor();
                        startSleepTrackingService();
                        stopPause();
                    } else {
                        int minutes = (pauseLeft % 3600) / 60;
                        int seconds = pauseLeft % 60;
                        statusTextView.setText(getString(R.string.paused_for,
                                twoDigitString(minutes) + ":" + twoDigitString(seconds)));
                        pauseLeft--;
                    }
                }
            });
        }
    }

    private void startSleepTrackingService() {
        Intent serviceIntent = new Intent(this, SleepTrackingService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }

    private void stopSleepTrackingService() {
        Intent serviceIntent = new Intent(this, SleepTrackingService.class);
        stopService(serviceIntent);
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(
                    android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[] { android.Manifest.permission.POST_NOTIFICATIONS }, 101);
            }
        }
    }
}
