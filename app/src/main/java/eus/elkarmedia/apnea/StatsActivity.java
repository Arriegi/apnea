package eus.elkarmedia.apnea;

import android.app.FragmentManager;
import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.List;

import static eus.elkarmedia.apnea.R.string.back;

public class StatsActivity extends AppCompatActivity {

    private SleepDbHelper dbHelper = null;

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
        chart.getAxisRight().setEnabled(false);
        chart.getXAxis().setAxisMinimum(0);
        chart.getXAxis().setAxisMaximum(sleeps.size());

        List<Entry> entriesBack = new ArrayList<Entry>();
        List<Entry> entriesLeft = new ArrayList<Entry>();
        List<Entry> entriesRight = new ArrayList<Entry>();
        List<Entry> entriesStomach = new ArrayList<Entry>();
        List<Entry> entriesUp = new ArrayList<Entry>();

        for (int i = 0; i < sleeps.size(); i++) {
            // turn your data into Entry objects
            Sleep sleep = sleeps.get(i);
            if (sleep.getTotal() == 0) {
                dbHelper.deleteSleep(sleep);
                continue;
            }
            long back = sleep.getTotal() == 0 ? 0 : sleep.getBack() * 100 / sleep.getTotal();
            entriesBack.add(new Entry(i, back));
            long right = sleep.getTotal() == 0 ? 0 : sleep.getRight() * 100 / sleep.getTotal();
            entriesRight.add(new Entry(i, right));
            long left = sleep.getTotal() == 0 ? 0 : sleep.getLeft() * 100 / sleep.getTotal();
            entriesLeft.add(new Entry(i, left));
            long stomach = sleep.getTotal() == 0 ? 0 : sleep.getStomach() * 100 / sleep.getTotal();
            entriesStomach.add(new Entry(i, stomach));
            long up = sleep.getTotal() == 0 ? 0 : sleep.getUp() * 100 / sleep.getTotal();
            entriesUp.add(new Entry(i, up));
        }
        if (entriesBack.size() == 0) {
            entriesBack.add(new Entry(0,0));
        }
        if (entriesRight.size() == 0) {
            entriesRight.add(new Entry(0,0));
        }
        if (entriesLeft.size() == 0) {
            entriesLeft.add(new Entry(0,0));
        }
        if (entriesStomach.size() == 0) {
            entriesStomach.add(new Entry(0,0));
        }
        if (entriesUp.size() == 0) {
            entriesUp.add(new Entry(0,0));
        }
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

        dataSets.add(setComp1);
        dataSets.add(setComp2);
        dataSets.add(setComp3);
        dataSets.add(setComp4);
        dataSets.add(setComp5);

        LineData data = new LineData(dataSets);
        chart.setData(data);
        chart.invalidate();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_stats, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_clear_stats:
                FragmentManager fm = getFragmentManager();
                ClearStatsDialogFragment dialogFragment = new ClearStatsDialogFragment ();
                dialogFragment.show(fm, "Sample Fragment");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }
}
