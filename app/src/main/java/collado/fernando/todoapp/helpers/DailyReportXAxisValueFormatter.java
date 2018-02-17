package collado.fernando.todoapp.helpers;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

/**
 * Created by root on 17/02/18.
 */

public class DailyReportXAxisValueFormatter implements IAxisValueFormatter {

    private String[] _date;

    public DailyReportXAxisValueFormatter(String[] date){
        this._date = date;
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        String date = _date[(int) value];
        return date.substring(8,10) + "-" + date.substring(5,7) + "-" + date.substring(0,4);
    }

}
