package collado.fernando.todoapp.activities;

import android.support.v7.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import collado.fernando.todoapp.R;
import collado.fernando.todoapp.helpers.DBHelper;
import collado.fernando.todoapp.helpers.StringXAxisValueFormatter;
import collado.fernando.todoapp.models.Stat;
import collado.fernando.todoapp.models.Task;

/**
 * Created by Fernando on 11/02/18.
 */

public class Stats extends AppCompatActivity{

    private static String[] TAGS;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stats);

        DBHelper db = new DBHelper(this);

        TAGS = db.getAllTags();

        // Calculate an int array that has in the [0,TAGS.length] positions
        // the total per TAG tasks assigned, in the [TAGS.length,endOfArray] positions
        // the tasks done
        // Example
        // Tag  Personal Exercise Study
        // [0,2]   3        2       4
        // [3,5]   1        2       3

        int tagsSize = TAGS.length;
        int []tags = new int[tagsSize*2];
        Map<String, ArrayList<Task>> tasks_by_day = db.getAllTasks();
        for(Map.Entry<String,ArrayList<Task>> entry : tasks_by_day.entrySet()){
            ArrayList<Task> tasks = entry.getValue();
            for(Task task : tasks){
                int index = getIndexFromTag(task.getTag());
                tags[index > -1 ? index : 0] += 1;
                if(task.isDone()) tags[index+tagsSize] += 1;
            }
        }

        ArrayList<Stat> stats = db.getAllStats();
        List<Entry> entries = new ArrayList<>();

        String[] dates = new String[stats.size()];
        int i = 0;

        int total = 0;
        int done = 0;

        // Creates entries for the absolute and relative charts
        for(Stat stat : stats){
            Float relative = ( (float) stat.getDone() / stat.getTotal()) * 100;

            String date = stat.getDate();
            dates[i] = date.substring(8,10) + "-" + date.substring(5,7) + "-" + date.substring(0,4);

            total += stat.getTotal();
            done += stat.getDone();

            entries.add(new Entry(i, relative));

            i++;
        }

        printLinearChart(entries, dates);
        printPieChart(getPieData(total, done));
        printTotalBarChart(total, done);
        printAbsoluteByTagBarChart(Arrays.copyOfRange(tags, 0, TAGS.length));
        printRelativeByTagBarChart(tags);
    }

    private int getIndexFromTag(String tag){
        /**
         * Get index of Tag from the TAGS String array
         */
        for (int i=0;i<TAGS.length;i++) {
            if (TAGS[i].equals(tag)) {
                return i;
            }
        }
        return -1;
    }

    private PieData getPieData(int total, int done){
        /**
         * Create PieData from total and done tasks
         */
        List<PieEntry> pieEntries = new ArrayList<>();

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
        /**
         * Handles PieChart creation and settings
         */
        PieChart pChart = (PieChart) findViewById(R.id.total_stats_pie);
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
        /**
         * Handles LinearChart creation and settings
         */
        LineChart lChart = (LineChart) findViewById(R.id.daily_stats_chart);
        lChart.getDescription().setEnabled(false);

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

        xAxis.setValueFormatter(new StringXAxisValueFormatter(dates));

        LineDataSet dataSet = new LineDataSet(entries, "Percentage of tasks done per day");
        LineData lineData = new LineData(dataSet);
        lChart.setData(lineData);
        lChart.invalidate(); // refresh
    }

    private void printTotalBarChart(int total, int done){
        /**
         * Handles BarChart creation and settings
         */
        BarChart bChart = (BarChart) findViewById(R.id.total_bar_chart);
        bChart.getDescription().setEnabled(false);

        List<BarEntry> barEntries = new ArrayList<>();

        BarEntry barEntryTotal = new BarEntry(0, total, "Total");
        BarEntry barEntryDone = new BarEntry(1, done,"Done");

        barEntries.add(barEntryDone);
        barEntries.add(barEntryTotal);

        barChartFromData(bChart, barEntries, new String[] { "Total", "Done" });
    }

    private void printAbsoluteByTagBarChart(int[] tags){
        /**
         * Handles (absolute data) BarChart creation and settings
         */
        BarChart bChart = (BarChart) findViewById(R.id.tag_bar_chart);
        bChart.getDescription().setEnabled(false);
        bChart.getAxisLeft().setAxisMaximum(100);

        List<BarEntry> barEntries = new ArrayList<>();
        int total = 0;
        for(int i=0; i<TAGS.length; i++){
            total += tags[i];
        }
        for(int i=0; i<TAGS.length; i++){
            float tagValue = 0;
            if(total>0) tagValue = (float)tags[i]/total * 100;
            BarEntry barEntryTag = new BarEntry(i, tagValue, TAGS[i]);
            barEntries.add(barEntryTag);
        }

        barChartFromData(bChart, barEntries, TAGS);
    }

    private void printRelativeByTagBarChart(int[] tags){
        /**
        * Handles (relative data) BarChart creation and settings
        */
        BarChart bChart = (BarChart) findViewById(R.id.relative_tag_bar_chart);
        bChart.getDescription().setEnabled(false);
        bChart.getAxisLeft().setAxisMaximum(100);

        List<BarEntry> barEntries = new ArrayList<>();
        int tagSize = TAGS.length;

        for(int i=0; i<tagSize; i++){
            float tagValue = 0;
            if(tags[i] != 0){
                tagValue = (float)tags[i+tagSize]/tags[i] * 100;
            }
            BarEntry barEntryTag = new BarEntry(i, tagValue, TAGS[i]);
            barEntries.add(barEntryTag);
        }

        barChartFromData(bChart, barEntries, TAGS);
    }

    private void barChartFromData(BarChart bChart, List<BarEntry> barEntries, String[] xAxisValues) {

        BarDataSet dataSet = new BarDataSet(barEntries, "");

        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);

        XAxis xAxis = bChart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelCount(TAGS.length);
        if(TAGS.length>8) xAxis.setLabelRotationAngle(-60f);
        xAxis.setValueFormatter(new StringXAxisValueFormatter(xAxisValues));

        YAxis leftAxis = bChart.getAxisLeft();
        YAxis rightAxis = bChart.getAxisRight();
        rightAxis.setEnabled(false);

        leftAxis.setAxisMinimum(0f);

        bChart.getLegend().setEnabled(false);

        BarData barData = new BarData(dataSet);
        barData.setValueTextSize(10f);
        barData.setBarWidth(0.9f);

        bChart.setData(barData);
        bChart.invalidate();
    }
}