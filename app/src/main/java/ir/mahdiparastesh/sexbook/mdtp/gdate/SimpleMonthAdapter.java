package ir.mahdiparastesh.sexbook.mdtp.gdate;

import android.content.Context;

public class SimpleMonthAdapter extends MonthAdapter {

    public SimpleMonthAdapter(DatePickerController controller) {
        super(controller);
    }

    @Override
    public MonthView createMonthView(Context context) {
        return new SimpleMonthView(context, null, mController);
    }
}
