package collado.fernando.todoapp.activities;

import android.support.v7.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.RelativeLayout;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import collado.fernando.todoapp.R;
import collado.fernando.todoapp.helpers.DailyReportXAxisValueFormatter;
import collado.fernando.todoapp.models.Stat;

/**
 * Created by Fernando on 11/02/18.
 */

public class Stats extends AppCompatActivity{

    private LineChart mChart;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stats);

        Intent intent = getIntent();
        ArrayList<Stat> stats = intent.getParcelableArrayListExtra("stats");


        Log.d("STAAAAAAAATS", stats.toString());



        List<Entry> entries = new ArrayList<Entry>();
        String[] dates = new String[stats.size()];
        int i = 0;
        for(Stat stat : stats){
            Float relative = ( (float) stat.getDone() / stat.getTotal()) * 100;
            String statDate = stat.getDate();
            dates[i] = statDate;

            try {

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd");
                Date date = sdf.parse(statDate);
                entries.add(new Entry(i, relative));
            }
            catch (ParseException e){
                Log.d("INVALID_DATE", statDate);
            }
            i++;
        }

        printLinearChart(entries, dates);

    }

    private void printLinearChart(List<Entry> entries,  String[] dates){
        mChart = (LineChart) findViewById(R.id.daily_stats_chart);
        //LimitLine llXAxis = new LimitLine();

        XAxis xAxis = mChart.getXAxis();
        xAxis.setLabelRotationAngle(90f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);

        YAxis leftAxis = mChart.getAxisLeft();
        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);

        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(100f);

        Legend legend = mChart.getLegend();
        legend.setEnabled(true);
        legend.setPosition(Legend.LegendPosition.ABOVE_CHART_CENTER);
        legend.setForm(Legend.LegendForm.CIRCLE);

        Log.d("DAAAAAATEs", dates[0] + "      " + dates[1]);
        xAxis.setValueFormatter(new DailyReportXAxisValueFormatter(dates));

        LineDataSet dataSet = new LineDataSet(entries, "Percentage of tasks done per day");
        LineData lineData = new LineData(dataSet);
        mChart.setData(lineData);
        mChart.invalidate(); // refresh
    }
}