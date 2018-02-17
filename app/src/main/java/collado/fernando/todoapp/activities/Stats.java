package collado.fernando.todoapp.activities;

import android.support.v7.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;

import java.util.ArrayList;

import collado.fernando.todoapp.R;
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

        /*


        mChart = (LineChart) findViewById(R.id.daily_stats_chart);
        mChart.setDrawGridBackground(true);
        mChart.setPinchZoom(true);


        // MyMarkerView mv = new MyMarkerView(this, R.layout.custom_marker_view);
        // mv.setChartView(mChart); // For bounds control
        // mChart.setMarker(mv); // Set the marker to the chart


        LimitLine llXAxis = new LimitLine(10f, "Index 10");
        llXAxis.setLineWidth(4f);
        llXAxis.enableDashedLine(10f, 10f, 0f);
        llXAxis.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
        llXAxis.setTextSize(10f);

        XAxis xAxis = mChart.getXAxis();
        xAxis.enableGridDashedLine(10f, 10f, 0f);
        */

    }
}
