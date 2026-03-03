package eus.elkarmedia.apnea;

import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays intra-session detail for a single night of sleep.
 *
 * Shows a combined chart with:
 * - Left Y axis: body position over time (encoded as integer values)
 * - Right Y axis: decibel level over time
 * - X axis: time in seconds from start of session
 *
 * Follows the Single Responsibility Principle: only visualizes sample data.
 */
public class SleepDetailActivity extends AppCompatActivity {

    public static final String EXTRA_SLEEP_ID = "sleep_id";

    private SleepDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep_detail);

        setTitle(R.string.sleep_detail);

        long sleepId = getIntent().getLongExtra(EXTRA_SLEEP_ID, -1);
        if (sleepId == -1) {
            finish();
            return;
        }

        dbHelper = new SleepDbHelper(this);
        List<SleepSample> samples = loadSamples(sleepId);

        if (samples.isEmpty()) {
            finish();
            return;
        }

        LineChart chart = findViewById(R.id.detailChart);
        configureChart(chart, samples);
    }

    private List<SleepSample> loadSamples(long sleepId) {
        return dbHelper.getSamplesForSleep(dbHelper.getReadableDatabase(), sleepId);
    }

    private void configureChart(LineChart chart, List<SleepSample> samples) {
        chart.getAxisLeft().setDrawGridLines(false);
        chart.getXAxis().setDrawGridLines(false);
        chart.getAxisRight().setEnabled(true);
        chart.getAxisRight().setDrawGridLines(false);
        chart.getDescription().setEnabled(false);

        List<Entry> positionEntries = buildPositionEntries(samples);
        List<Entry> decibelEntries = buildDecibelEntries(samples);

        List<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(createPositionDataSet(positionEntries));
        dataSets.add(createDecibelDataSet(decibelEntries));

        chart.setData(new LineData(dataSets));
        chart.invalidate();
    }

    private List<Entry> buildPositionEntries(List<SleepSample> samples) {
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < samples.size(); i++) {
            entries.add(new Entry(i, samples.get(i).getPosition()));
        }
        return entries;
    }

    private List<Entry> buildDecibelEntries(List<SleepSample> samples) {
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < samples.size(); i++) {
            entries.add(new Entry(i, (float) samples.get(i).getDecibels()));
        }
        return entries;
    }

    private LineDataSet createPositionDataSet(List<Entry> entries) {
        LineDataSet dataSet = new LineDataSet(entries, getString(R.string.position));
        dataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        dataSet.setColor(Color.BLUE);
        dataSet.setDrawCircles(false);
        dataSet.setLineWidth(1.5f);
        dataSet.setDrawValues(false);
        return dataSet;
    }

    private LineDataSet createDecibelDataSet(List<Entry> entries) {
        LineDataSet dataSet = new LineDataSet(entries, getString(R.string.sound_level));
        dataSet.setAxisDependency(YAxis.AxisDependency.RIGHT);
        dataSet.setColor(Color.RED);
        dataSet.setDrawCircles(false);
        dataSet.setLineWidth(1.5f);
        dataSet.setDrawValues(false);
        return dataSet;
    }

    @Override
    protected void onDestroy() {
        if (dbHelper != null) {
            dbHelper.close();
        }
        super.onDestroy();
    }
}
