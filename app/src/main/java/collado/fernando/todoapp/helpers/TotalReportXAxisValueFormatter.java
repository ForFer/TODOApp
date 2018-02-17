package collado.fernando.todoapp.helpers;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

/**
 * Created by Fernando on 17/02/18.
 */

public class TotalReportXAxisValueFormatter implements IAxisValueFormatter {

    private String[] _date;

    public TotalReportXAxisValueFormatter(String[] date){
        this._date = date;
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        return _date[(int) value];
    }
}
