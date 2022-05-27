package ir.mahdiparastesh.sexbook.jdtp.date;

import android.content.Context;
import android.util.AttributeSet;
import android.view.accessibility.AccessibilityEvent;
import android.widget.ViewAnimator;

import ir.mahdiparastesh.sexbook.jdtp.utils.LanguageUtils;
import ir.mahdiparastesh.sexbook.jdtp.utils.PersianCalendar;

public class AccessibleDateAnimator extends ViewAnimator {
    private long mDateMillis;

    public AccessibleDateAnimator(Context c, AttributeSet attrs) {
        super(c, attrs);
    }

    public void setDateMillis(long dateMillis) {
        mDateMillis = dateMillis;
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            event.getText().clear();
            PersianCalendar mPersianCalendar = new PersianCalendar();
            mPersianCalendar.setTimeInMillis(mDateMillis);
            String dateString = LanguageUtils.getPersianNumbers(
                    mPersianCalendar.getPersianMonthName(getContext()) + " " +
                            mPersianCalendar.getPersianYear()
            );
            event.getText().add(dateString);
            return true;
        }
        return super.dispatchPopulateAccessibilityEvent(event);
    }
}
