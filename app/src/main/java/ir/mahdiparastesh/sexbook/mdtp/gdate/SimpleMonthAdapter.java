package ir.mahdiparastesh.sexbook.mdtp.gdate;

import android.content.Context;

/**
 * An adapter for a list of {@link SimpleMonthView} items.
 */
public class SimpleMonthAdapter extends MonthAdapter {

    public SimpleMonthAdapter(DatePickerController controller) {
        super(controller);
    }

    @Override
    public MonthView createMonthView(Context context) {
        return new SimpleMonthView(context, null, mController);
    }
}
