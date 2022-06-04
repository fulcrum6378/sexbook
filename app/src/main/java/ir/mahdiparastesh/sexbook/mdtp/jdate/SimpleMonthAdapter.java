package ir.mahdiparastesh.sexbook.mdtp.jdate;

import android.content.Context;

public class SimpleMonthAdapter extends MonthAdapter {

    public SimpleMonthAdapter(Context c, DatePickerController controller) {
        super(c, controller);
    }

    @Override
    public MonthView createMonthView(Context c) {
        return new SimpleMonthView(c, null, mController);
    }
}
