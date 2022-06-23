package ir.mahdiparastesh.sexbook.mdtp.date;

import android.content.Context;
import android.icu.util.Calendar;
import android.util.AttributeSet;

public class SimpleDayPickerView<CAL extends Calendar> extends DayPickerView<CAL> {

    public SimpleDayPickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SimpleDayPickerView(Context context, DatePickerController<CAL> controller) {
        super(context, controller);
    }

    @Override
    public MonthAdapter<CAL> createMonthAdapter(DatePickerController<CAL> controller) {
        return new SimpleMonthAdapter<>(controller);
    }
}
