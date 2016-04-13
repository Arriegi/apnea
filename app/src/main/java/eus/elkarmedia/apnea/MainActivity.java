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
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private final static int LEFT = 0;
    private final static int RIGHT = 1;
    private final static int BACK = 2;
    private final static int STOMACH = 3;
    private final static int UP = 4;

    private final static int STARTED = 1;
    private final static int PAUSED = 2;
    private final static int STOPPED = 3;

    private final static int PAUSE = 60*10*1000;

    private SensorManager sensorManager;

    private TextView statusTextView, leftTextView, rightTextView, backTextView, stomachTextView,
            getUpTextView, totalsTextView;
    private double left = 0, right = 0, back = 0, stomach = 0, up = 0, total = 0, lastUpdateX = 0,
            lastUpdateY = 0, lastUpdateZ = 0;
    private int leftCount = 0, rightCount = 0, backCount = 0, stomachCount = 0, upCount = 0,
            totalCount = 0, totalVibrate = 0, totalSound = 0, status = STOPPED;
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
        getUpTextView = (TextView) findViewById(R.id.getUpTextView);
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
        String upString = String.format(countTimePct,upCount,getHoursMinutesSeconds(up),
                total != 0 ? (up/total*100) : 0.0);
        String totalsString = String.format(countTimePct,totalCount,getHoursMinutesSeconds(total),100.0);

        leftTextView.setText(leftString);
        rightTextView.setText(rightString);
        backTextView.setText(backString);
        stomachTextView.setText(stomachString);
        getUpTextView.setText(upString);
        totalsTextView.setText(totalsString);
    }

    private String getHoursMinutesSeconds(double time) {
        long millis = Double.valueOf(time).longValue();
        return String.format(getResources().getConfiguration().locale,"%02d:%02d:%02d",
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
                if (status == STOPPED) {
                    initializeSleep();
                    showTimes();
                }
                status = STARTED;
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
                status = PAUSED;
                statusTextView.setText(R.string.paused);
                sensorManager.unregisterListener(this);
                return true;
            case R.id.action_stop:
                status = STOPPED;
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
        int lastPosition = getLastPosition();
        int currentPosition = getPosition(event.values[0],event.values[1],event.values[2]);
        boolean hasChangedPosition = lastPosition != currentPosition;
        if (lastUpdate.before(oneSecondAgo)) {
            showTimes();
            total+=1000;
            switch(currentPosition) {
                case RIGHT:
                    if (hasChangedPosition) rightCount++;
                    right+=1000;
                    break;
                case LEFT:
                    if (hasChangedPosition) leftCount++;
                    left+=1000;
                    break;
                case BACK:
                    if (hasChangedPosition) backCount++;
                    back+=1000;
                    break;
                case STOMACH:
                    if (hasChangedPosition) stomachCount++;
                    stomach+=1000;
                    break;
                case UP:
                    if (hasChangedPosition) upCount++;
                    up+=1000;
                    break;
                default:
                    Log.d("sensor","unknown body position");
            }
            if (hasChangedPosition) totalCount++;
            //Update last... for next second
            lastUpdate = oneSecondAgo;
            lastUpdate.add(Calendar.SECOND,1);
            lastUpdateX = event.values[0];
            lastUpdateY = event.values[1];
            lastUpdateZ = event.values[2];
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private int getLastPosition() {
        if (lastUpdateX == 0 && lastUpdateY == 0 && lastUpdateZ == 0) {
            return -1;
        }
        double absX = Math.abs(lastUpdateX);
        double absY = Math.abs(lastUpdateY);
        double absZ = Math.abs(lastUpdateZ);
        if (absX >= absY && absX >= absZ) {
            //zutik dago
            return UP;
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

    private int getPosition(double x, double y, double z) {
        /*if (y > 6) {
            return RIGHT;
        } else if (y < -6) {
            return LEFT;
        } else if (z > 6) {
            return BACK;
        } else if (z < -6) {
            return STOMACH;
        } else {
            return -1;
        }*/
        double absX = Math.abs(x);
        double absY = Math.abs(y);
        double absZ = Math.abs(z);
        if (absX >= absY && absX >= absZ) {
            //zutik dago
            return UP;
        } else if (absY >= absX && absY >= absZ) {
            //correct
            if (y > 0) {
                return RIGHT;
            } else {
                return LEFT;
            }
        } else if (absZ >= absX && absZ >= absY) {
            //back or stomach
            if (z > 0) {
                return BACK;
            } else {
                return STOMACH;
            }
        } else {
            return -1;
        }
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
        lastUpdate = Calendar.getInstance();
        lastUpdate.setTimeInMillis(0);
        lastUpdateX = 0;
        lastUpdateY = 0;
        lastUpdateZ = 0;
        status = STOPPED;
        totalVibrate = 0;
        totalSound = 0;
    }

}
