package eus.elkarmedia.apnea;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private final static int LEFT = 0;
    private final static int RIGHT = 1;
    private final static int BACK = 2;
    private final static int STOMACH = 3;

    private SensorManager sensorManager;

    private TextView statusTextView, leftTextView, rightTextView, backTextView, stomachTextView, totalsTextView;
    private double left = 0, right = 0, back = 0, stomach = 0, total = 0, lastUpdateX = 0,
            lastUpdateY = 0, lastUpdateZ = 0;
    private int leftCount = 0, rightCount = 0, backCount = 0, stomachCount = 0, totalCount = 0,
            totalVibrate = 0, totalSound = 0;
    private String countTimePct = "%1$d / %2$s  (%3$.2f %%)";
    private Calendar lastUpdate = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        lastUpdate.setTimeInMillis(0);

        //Instantiate textViews
        statusTextView = (TextView) findViewById(R.id.statusTextView);
        leftTextView = (TextView) findViewById(R.id.leftTextView);
        rightTextView = (TextView) findViewById(R.id.rightTextView);
        backTextView = (TextView) findViewById(R.id.backTextView);
        stomachTextView = (TextView) findViewById(R.id.stomachTextView);
        totalsTextView = (TextView) findViewById(R.id.totalsTextView);

        showTimes();

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
    }

    private void showTimes() {
        String leftString = String.format(countTimePct,leftCount,getHoursMinutesSeconds(left),
                total != 0 ? (left/total*100) : 0.0);
        String rightString = String.format(countTimePct,rightCount,getHoursMinutesSeconds(right),
                total != 0 ? (right/total*100) : 0.0);
        String backString = String.format(countTimePct,backCount,getHoursMinutesSeconds(back),
                total != 0 ? (back/total*100) : 0.0);
        String stomachString = String.format(countTimePct,stomachCount,getHoursMinutesSeconds(stomach),
                total != 0 ? (stomach/total*100) : 0.0);
        String totalsString = String.format(countTimePct,totalCount,getHoursMinutesSeconds(total),100.0);

        leftTextView.setText(leftString);
        rightTextView.setText(rightString);
        backTextView.setText(backString);
        stomachTextView.setText(stomachString);
        totalsTextView.setText(totalsString);
    }

    private String getHoursMinutesSeconds(double time) {
        long millis = Double.valueOf(time).longValue();
        return String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)), // The change is in this line
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
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
        int id = item.getItemId();
        switch(id) {
            case R.id.action_play:
                if (Build.VERSION.SDK_INT >= 19) {
                    sensorManager.registerListener(this,
                            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                            SensorManager.SENSOR_DELAY_UI,1000000);
                    statusTextView.setText(R.string.started);
                } else {
                    sensorManager.registerListener(this,
                            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                            SensorManager.SENSOR_DELAY_NORMAL);
                    statusTextView.setText(R.string.started);
                }
                return true;
            case R.id.action_pause:
                statusTextView.setText(R.string.paused);
                return true;
            case R.id.action_stop:
                statusTextView.setText(R.string.stopped);
                sensorManager.unregisterListener(this);
                return true;
            case R.id.action_settings:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Calendar oneSecondAgo = Calendar.getInstance();
        oneSecondAgo.add(Calendar.SECOND,-1);
        if (lastUpdate.before(oneSecondAgo)) {
            total+=1000;
            showTimes();
            Log.d("time","segundua pasata");
            switch(getLastPosition()) {
                case RIGHT:
                    right+=1000;
                    break;
                case LEFT:
                    left+=1000;
                    break;
                case BACK:
                    back+=1000;
                    break;
                case STOMACH:
                    stomach+=1000;
                    break;
                default:
                    Log.d("sensor","unknown body position");
            }
            lastUpdate = oneSecondAgo;
            lastUpdate.add(Calendar.SECOND,1);
            lastUpdateX = event.values[0];
            lastUpdateY = event.values[1];
            lastUpdateZ = event.values[2];
        }
        Log.d("sensor", Arrays.toString(event.values));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private int getLastPosition() {
        double absX = Math.abs(lastUpdateX);
        double absY = Math.abs(lastUpdateY);
        double absZ = Math.abs(lastUpdateZ);
        if (absX >= absY && absX >= absZ) {
            //zutik dago
            return -1;
        } else if (absY >= absX && absY >= absZ) {
            //correct
            if (lastUpdateY > 0) {
                return RIGHT;
            } else {
                return LEFT;
            }
        } else if (absZ >= absX && absZ >= absY) {
            //back or stomach
            if (lastUpdateZ > 0) {
                return BACK;
            } else {
                return STOMACH;
            }
        } else {
            return -1;
        }
    }

    private int getPosition(double y, double z) {
        if (y > 6) {
            return RIGHT;
        } else if (y < -6) {
            return LEFT;
        } else if (z > 6) {
            return BACK;
        } else if (z < -6) {
            return STOMACH;
        } else {
            return -1;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        // unregister listener
        super.onPause();
    }
}
