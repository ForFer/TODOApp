package collado.fernando.todoapp.activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;

import java.util.ArrayList;

import collado.fernando.todoapp.R;
import collado.fernando.todoapp.helpers.DBHelper;
import collado.fernando.todoapp.models.Stat;

/**
 * Created by Fernando on 11/02/18.
 */

public class Stats extends Activity {

    private LineChart mChart;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ArrayList<Stat> stats = getIntent().getParcelableArrayListExtra("stats");
        setContentView(R.layout.stats);

        mChart = (LineChart) findViewById(R.id.daily_stats_chart);
        mChart.setDrawGridBackground(true);
        mChart.setPinchZoom(true);

        /*
        MyMarkerView mv = new MyMarkerView(this, R.layout.custom_marker_view);
        mv.setChartView(mChart); // For bounds control
        mChart.setMarker(mv); // Set the marker to the chart
        */

        LimitLine llXAxis = new LimitLine(10f, "Index 10");
        llXAxis.setLineWidth(4f);
        llXAxis.enableDashedLine(10f, 10f, 0f);
        llXAxis.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
        llXAxis.setTextSize(10f);

        XAxis xAxis = mChart.getXAxis();
        xAxis.enableGridDashedLine(10f, 10f, 0f);


    }
}
