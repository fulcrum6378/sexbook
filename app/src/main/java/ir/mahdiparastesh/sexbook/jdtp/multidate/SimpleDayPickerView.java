package ir.mahdiparastesh.sexbook.jdtp.multidate;

import android.content.Context;
import android.util.AttributeSet;

public class SimpleDayPickerView extends DayPickerView {

    public SimpleDayPickerView(Context c, AttributeSet attrs) {
        super(c, attrs);
    }

    public SimpleDayPickerView(Context c, DatePickerController controller) {
        super(c, controller);
    }

    @Override
    public MonthAdapter createMonthAdapter(Context c, DatePickerController controller) {
        return new SimpleMonthAdapter(c, controller);
    }
}
