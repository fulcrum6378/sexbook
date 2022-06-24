package ir.mahdiparastesh.sexbook.mdtp.date;

import android.content.Context;
import android.icu.util.Calendar;
import android.util.AttributeSet;
import android.view.accessibility.AccessibilityEvent;
import android.widget.ViewAnimator;

import ir.mahdiparastesh.sexbook.mdtp.Utils;

public class AccessibleDateAnimator extends ViewAnimator {
    private Calendar mCalendar;

    public AccessibleDateAnimator(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public <CAL extends Calendar> void setCalendar(CAL calendar) {
        mCalendar = calendar;
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            // Clear the event's current text so that only the current date will be spoken.
            event.getText().clear();
            event.getText().add(Utils.accessibilityDate(getContext(), mCalendar));
            return true;
        }
        return super.dispatchPopulateAccessibilityEvent(event);
    }
}
