package collado.fernando.todoapp.activities;

import android.support.v7.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

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

    private LineChart lChart;
    private PieChart pChart;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stats);

        Intent intent = getIntent();
        ArrayList<Stat> stats = intent.getParcelableArrayListExtra("stats");

        List<Entry> entries = new ArrayList<Entry>();

        String[] dates = new String[stats.size()];
        int i = 0;

        int total = 0;
        int done = 0;

        for(Stat stat : stats){
            Float relative = ( (float) stat.getDone() / stat.getTotal()) * 100;

            dates[i] = stat.getDate();
            total += stat.getTotal();
            done += stat.getDone();

            entries.add(new Entry(i, relative));

            i++;
        }

        printLinearChart(entries, dates);
        printPieChart(getPieData(total, done));

    }

    private PieData getPieData(int total, int done){

        List<PieEntry> pieEntries = new ArrayList<PieEntry>();

        PieEntry pieEntryDone = new PieEntry(done, "Tasks done");
        PieEntry pieEntryTotal = new PieEntry(total-done, "Tasks not done");

        pieEntries.add(pieEntryDone);
        pieEntries.add(pieEntryTotal);

        PieDataSet dataSet = new PieDataSet(pieEntries, "");
        dataSet.setColors(ColorTemplate.JOYFUL_COLORS);
        dataSet.setSliceSpace(3f);

        return new PieData(dataSet);

    }

    private void printPieChart(PieData pieData){
        pChart = (PieChart) findViewById(R.id.total_stats_pie);
        pChart.setUsePercentValues(true);
        pChart.getDescription().setEnabled(false);
        pChart.setHoleRadius(58f);
        pChart.setRotationEnabled(true);

        Legend l = pChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(false);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(0f);
        l.setYOffset(0f);

        pieData.setValueTextSize(11f);

        pChart.setData(pieData);

    }

    private void printLinearChart(List<Entry> entries,  String[] dates){
        lChart = (LineChart) findViewById(R.id.daily_stats_chart);
        lChart.getDescription().setEnabled(false);
        //LimitLine llXAxis = new LimitLine();

        XAxis xAxis = lChart.getXAxis();
        xAxis.setLabelRotationAngle(290f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);

        YAxis leftAxis = lChart.getAxisLeft();
        YAxis rightAxis = lChart.getAxisRight();
        rightAxis.setEnabled(false);

        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(100f);

        Legend legend = lChart.getLegend();
        legend.setEnabled(true);
        legend.setPosition(Legend.LegendPosition.ABOVE_CHART_CENTER);
        legend.setForm(Legend.LegendForm.CIRCLE);

        xAxis.setValueFormatter(new DailyReportXAxisValueFormatter(dates));

        LineDataSet dataSet = new LineDataSet(entries, "Percentage of tasks done per day");
        LineData lineData = new LineData(dataSet);
        lChart.setData(lineData);
        lChart.invalidate(); // refresh
    }
}