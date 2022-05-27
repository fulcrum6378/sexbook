package ir.mahdiparastesh.sexbook.jdtp;

import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.view.View;

import ir.mahdiparastesh.sexbook.jdtp.utils.PersianCalendarUtils;

public class Utils {
    public static final int PULSE_ANIMATOR_DURATION = 544;

    public static void tryAccessibilityAnnounce(View view, CharSequence text) {
        if (view != null && text != null)
            view.announceForAccessibility(text);
    }

    public static int getDaysInMonth(int month, int year) {
        if (month < 6)
            return 31;
        else if (month < 11)
            return 30;
        else if (PersianCalendarUtils.isPersianLeapYear(year))
            return 30;
        else return 29;
    }

    public static ObjectAnimator getPulseAnimator(View labelToAnimate, float decreaseRatio,
                                                  float increaseRatio) {
        Keyframe k0 = Keyframe.ofFloat(0f, 1f),
                k1 = Keyframe.ofFloat(0.275f, decreaseRatio),
                k2 = Keyframe.ofFloat(0.69f, increaseRatio),
                k3 = Keyframe.ofFloat(1f, 1f);

        PropertyValuesHolder scaleX = PropertyValuesHolder.ofKeyframe(
                View.SCALE_X, k0, k1, k2, k3);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofKeyframe(
                View.SCALE_Y, k0, k1, k2, k3);
        ObjectAnimator pulseAnimator =
                ObjectAnimator.ofPropertyValuesHolder(labelToAnimate, scaleX, scaleY);
        pulseAnimator.setDuration(PULSE_ANIMATOR_DURATION);

        return pulseAnimator;
    }
}
