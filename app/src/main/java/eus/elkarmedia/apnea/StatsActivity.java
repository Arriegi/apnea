package eus.elkarmedia.apnea;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.util.ArrayList;
import java.util.List;

import static eus.elkarmedia.apnea.R.string.back;

/**
 * Displays historical sleep statistics across all sessions.
 *
 * The chart shows position percentages (left Y axis) and decibel levels (right
 * Y axis).
 * Tapping a specific session point opens SleepDetailActivity for intra-night
 * analysis.
 */
public class StatsActivity extends AppCompatActivity implements OnChartValueSelectedListener {

    private SleepDbHelper dbHelper = null;
    private final List<Long> sleepIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        setTitle(R.string.action_stats);

        dbHelper = new SleepDbHelper(this);
        List<Sleep> sleeps = dbHelper.getSleeps(dbHelper.getReadableDatabase());

        LineChart chart = (LineChart) findViewById(R.id.chart);
        chart.getAxisLeft().setDrawGridLines(false);
        chart.getXAxis().setDrawGridLines(false);
        chart.getAxisRight().setEnabled(true);
        chart.getAxisRight().setDrawGridLines(false);
        chart.getXAxis().setAxisMinimum(0);
        chart.getXAxis().setAxisMaximum(sleeps.size());
        chart.setOnChartValueSelectedListener(this);

        List<Entry> entriesBack = new ArrayList<Entry>();
        List<Entry> entriesLeft = new ArrayList<Entry>();
        List<Entry> entriesRight = new ArrayList<Entry>();
        List<Entry> entriesStomach = new ArrayList<Entry>();
        List<Entry> entriesUp = new ArrayList<Entry>();
        List<Entry> entriesAvgDb = new ArrayList<Entry>();
        List<Entry> entriesMaxDb = new ArrayList<Entry>();

        for (int i = 0; i < sleeps.size(); i++) {
            Sleep sleep = sleeps.get(i);
            if (sleep.getTotal() == 0) {
                dbHelper.deleteSleep(sleep);
                continue;
            }
            sleepIds.add(sleep.getId());
            int index = sleepIds.size() - 1;
            long backPct = sleep.getBack() * 100 / sleep.getTotal();
            entriesBack.add(new Entry(index, backPct));
            long rightPct = sleep.getRight() * 100 / sleep.getTotal();
            entriesRight.add(new Entry(index, rightPct));
            long leftPct = sleep.getLeft() * 100 / sleep.getTotal();
            entriesLeft.add(new Entry(index, leftPct));
            long stomachPct = sleep.getStomach() * 100 / sleep.getTotal();
            entriesStomach.add(new Entry(index, stomachPct));
            long upPct = sleep.getUp() * 100 / sleep.getTotal();
            entriesUp.add(new Entry(index, upPct));
            entriesAvgDb.add(new Entry(index, (float) sleep.getAvgDecibels()));
            entriesMaxDb.add(new Entry(index, (float) sleep.getMaxDecibels()));
        }
        addEmptyEntryIfNeeded(entriesBack);
        addEmptyEntryIfNeeded(entriesRight);
        addEmptyEntryIfNeeded(entriesLeft);
        addEmptyEntryIfNeeded(entriesStomach);
        addEmptyEntryIfNeeded(entriesUp);
        addEmptyEntryIfNeeded(entriesAvgDb);
        addEmptyEntryIfNeeded(entriesMaxDb);

        List<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();

        LineDataSet setComp1 = new LineDataSet(entriesBack, getString(back));
        setComp1.setAxisDependency(YAxis.AxisDependency.LEFT);
        setComp1.setColor(Color.BLACK);
        LineDataSet setComp2 = new LineDataSet(entriesRight, getString(R.string.right_side));
        setComp2.setAxisDependency(YAxis.AxisDependency.LEFT);
        setComp2.setColor(Color.BLUE);
        LineDataSet setComp3 = new LineDataSet(entriesLeft, getString(R.string.left_side));
        setComp3.setAxisDependency(YAxis.AxisDependency.LEFT);
        setComp3.setColor(Color.GREEN);
        LineDataSet setComp4 = new LineDataSet(entriesStomach, getString(R.string.stomach));
        setComp4.setAxisDependency(YAxis.AxisDependency.LEFT);
        setComp4.setColor(Color.RED);
        LineDataSet setComp5 = new LineDataSet(entriesUp, getString(R.string.getUp));
        setComp5.setAxisDependency(YAxis.AxisDependency.LEFT);
        setComp5.setColor(Color.MAGENTA);
        LineDataSet setAvgDb = new LineDataSet(entriesAvgDb, getString(R.string.avg_decibels));
        setAvgDb.setAxisDependency(YAxis.AxisDependency.RIGHT);
        setAvgDb.setColor(Color.CYAN);
        setAvgDb.setLineWidth(2f);
        LineDataSet setMaxDb = new LineDataSet(entriesMaxDb, getString(R.string.max_decibels));
        setMaxDb.setAxisDependency(YAxis.AxisDependency.RIGHT);
        setMaxDb.setColor(Color.YELLOW);
        setMaxDb.setLineWidth(2f);

        dataSets.add(setComp1);
        dataSets.add(setComp2);
        dataSets.add(setComp3);
        dataSets.add(setComp4);
        dataSets.add(setComp5);
        dataSets.add(setAvgDb);
        dataSets.add(setMaxDb);

        LineData data = new LineData(dataSets);
        chart.setData(data);
        chart.invalidate();

        Toast.makeText(this, R.string.tap_to_detail, Toast.LENGTH_LONG).show();
    }

    private void addEmptyEntryIfNeeded(List<Entry> entries) {
        if (entries.isEmpty()) {
            entries.add(new Entry(0, 0));
        }
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        int index = (int) e.getX();
        if (index >= 0 && index < sleepIds.size()) {
            openSleepDetail(sleepIds.get(index));
        }
    }

    @Override
    public void onNothingSelected() {
        // No action needed
    }

    private void openSleepDetail(long sleepId) {
        Intent intent = new Intent(this, SleepDetailActivity.class);
        intent.putExtra(SleepDetailActivity.EXTRA_SLEEP_ID, sleepId);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_stats, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_clear_stats) {
            androidx.fragment.app.FragmentManager fm = getSupportFragmentManager();
            ClearStatsDialogFragment dialogFragment = new ClearStatsDialogFragment();
            dialogFragment.show(fm, "Sample Fragment");
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }
}
