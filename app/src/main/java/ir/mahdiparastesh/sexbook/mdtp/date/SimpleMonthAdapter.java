package ir.mahdiparastesh.sexbook.mdtp.date;

import android.content.Context;
import android.icu.util.Calendar;

public class SimpleMonthAdapter<CAL extends Calendar> extends MonthAdapter<CAL> {

    public SimpleMonthAdapter(DatePickerController<CAL> controller) {
        super(controller);
    }

    @Override
    public MonthView<CAL> createMonthView(Context context) {
        return new SimpleMonthView<>(context, null, mController);
    }
}
