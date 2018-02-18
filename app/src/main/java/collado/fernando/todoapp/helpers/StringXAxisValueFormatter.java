package collado.fernando.todoapp.helpers;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

/**
 * Created by root on 17/02/18.
 */

public class StringXAxisValueFormatter implements IAxisValueFormatter {

    private String[] _date;

    public StringXAxisValueFormatter(String[] date){
        this._date = date;
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        return _date[(int) value];
    }

}
