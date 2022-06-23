package ir.mahdiparastesh.sexbook.mdtp.date;

import android.content.Context;
import android.icu.text.DateFormatSymbols;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.accessibility.AccessibilityEvent;
import android.widget.ViewAnimator;

import java.util.Calendar;

import ir.mahdiparastesh.sexbook.mdtp.Utils;
import ir.mahdiparastesh.sexbook.more.PersianCalendar;

public class AccessibleDateAnimator extends ViewAnimator {
    private long mDateMillis;

    public AccessibleDateAnimator(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setDateMillis(long dateMillis) {
        mDateMillis = dateMillis;
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            // Clear the event's current text so that only the current date will be spoken.
            event.getText().clear();
            String dateString;
            if (Utils.isGregorian(getContext())) {
                int flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR |
                        DateUtils.FORMAT_SHOW_WEEKDAY;

                dateString = DateUtils.formatDateTime(getContext(), mDateMillis, flags);
            } else {// Persian
                PersianCalendar mPersianCalendar = new PersianCalendar();
                mPersianCalendar.setTimeInMillis(mDateMillis);
                dateString = DateFormatSymbols.getInstance().getMonths()[mPersianCalendar.get(Calendar.MONTH)]
                        + " " + mPersianCalendar.get(Calendar.YEAR);
            }
            event.getText().add(dateString);
            return true;
        }
        return super.dispatchPopulateAccessibilityEvent(event);
    }
}
