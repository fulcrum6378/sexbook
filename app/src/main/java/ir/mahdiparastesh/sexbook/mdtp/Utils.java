package ir.mahdiparastesh.sexbook.mdtp;

import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.AttrRes;
import androidx.core.content.ContextCompat;

import java.util.Calendar;

import ir.mahdiparastesh.sexbook.R;
import ir.mahdiparastesh.sexbook.mdtp.utils.PersianCalendarUtils;

@SuppressWarnings("WeakerAccess")
public class Utils {
    public static final int PULSE_ANIMATOR_DURATION = 544;

    // Alpha level for time picker selection.
    public static final int SELECTED_ALPHA = 255;
    public static final int SELECTED_ALPHA_THEME_DARK = 255;
    // Alpha level for fully opaque.
    public static final int FULL_ALPHA = 255;

    /**
     * Try to speak the specified text, for accessibility. Only available on JB or later.
     *
     * @param text Text to announce.
     */
    public static void tryAccessibilityAnnounce(View view, CharSequence text) {
        if (view != null && text != null) view.announceForAccessibility(text);
    }

    public static ObjectAnimator getPulseAnimator(View labelToAnimate, float decreaseRatio,
                                                  float increaseRatio) {
        Keyframe k0 = Keyframe.ofFloat(0f, 1f);
        Keyframe k1 = Keyframe.ofFloat(0.275f, decreaseRatio);
        Keyframe k2 = Keyframe.ofFloat(0.69f, increaseRatio);
        Keyframe k3 = Keyframe.ofFloat(1f, 1f);

        PropertyValuesHolder scaleX = PropertyValuesHolder.ofKeyframe(
                View.SCALE_X, k0, k1, k2, k3);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofKeyframe(
                View.SCALE_Y, k0, k1, k2, k3);
        ObjectAnimator pulseAnimator =
                ObjectAnimator.ofPropertyValuesHolder(labelToAnimate, scaleX, scaleY);
        pulseAnimator.setDuration(PULSE_ANIMATOR_DURATION);

        return pulseAnimator;
    }

    @SuppressWarnings("unused")
    public static int dpToPx(float dp, Resources resources) {
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
        return (int) px;
    }

    public static int darkenColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] = hsv[2] * 0.8f; // value component
        return Color.HSVToColor(hsv);
    }

    public static int getAccentColorFromThemeIfAvailable(Context context) {
        TypedValue typedValue = new TypedValue();
        // First, try the android:colorAccent
        context.getTheme().resolveAttribute(android.R.attr.colorAccent, typedValue, true);
        return typedValue.data;
        // Next, try colorAccent from support lib
    }

    public static boolean isDarkTheme(Context context, boolean current) {
        return resolveBoolean(context, R.attr.mdtp_theme_dark, current);
    }

    private static boolean resolveBoolean(Context context, @AttrRes int attr, boolean fallback) {
        TypedArray a = context.getTheme().obtainStyledAttributes(new int[]{attr});
        try {
            return a.getBoolean(0, fallback);
        } finally {
            a.recycle();
        }
    }

    public static Calendar trimToMidnight(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
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
}
