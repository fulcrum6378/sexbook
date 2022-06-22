package ir.mahdiparastesh.sexbook.mdtp;

import static ir.mahdiparastesh.sexbook.Main.getJdtpArabicNumbers;

import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.View;

import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;
import java.util.Calendar;

import ir.mahdiparastesh.sexbook.Fun;
import ir.mahdiparastesh.sexbook.R;
import ir.mahdiparastesh.sexbook.Settings;

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

    public static String getPersianNumbers(String string) {
        if (getJdtpArabicNumbers()) {
            string = string.replace("0", "۰");
            string = string.replace("1", "١");
            string = string.replace("2", "۲");
            string = string.replace("3", "۳");
            string = string.replace("4", "۴");
            string = string.replace("5", "۵");
            string = string.replace("6", "۶");
            string = string.replace("7", "۷");
            string = string.replace("8", "۸");
            string = string.replace("9", "۹");
        }
        return string;
    }

    public static void getPersianNumbers(ArrayList<String> strings) {
        for (int i = 0; i < strings.size(); i++)
            strings.set(i, getPersianNumbers(strings.get(i)));
    }

    public static String getLatinNumbers(String string) {
        if (getJdtpArabicNumbers()) {
            string = string.replace("۰", "0");
            string = string.replace("١", "1");
            string = string.replace("۲", "2");
            string = string.replace("۳", "3");
            string = string.replace("۴", "4");
            string = string.replace("۵", "5");
            string = string.replace("۶", "6");
            string = string.replace("۷", "7");
            string = string.replace("۸", "8");
            string = string.replace("۹", "9");
        }
        return string;
    }

    public static Typeface mdtpFont(Context c, boolean bold) {
        return ResourcesCompat.getFont(c, bold ? R.font.bold : R.font.normal);
    }

    public static Typeface mdtpAmPmFont(Context c) {
        return Typeface.create("sans-serif", Typeface.NORMAL);
    }

    public static Typeface mdtpTimeCircleFont(Context c, boolean innerCircleIn24HM) {
        return Typeface.create("sans-serif", Typeface.NORMAL);
    }

    public static Typeface mdtpMonthTitleFont(Context c) {
        return Typeface.create("sans-serif", Typeface.BOLD);
    }

    public static Typeface mdtpDayOfWeekFont(Context c) {
        return Typeface.create("sans-serif-light", Typeface.BOLD);
    }

    public static Typeface mdtpDayOfMonth(Context c, boolean isHighlighted) {
        return Typeface.create(Typeface.DEFAULT, isHighlighted ? Typeface.BOLD : Typeface.NORMAL);
    }


    public static boolean isGregorian(Context c) {
        return CalendarType.values()[
                c.getSharedPreferences(Settings.spName, Context.MODE_PRIVATE).getInt(Settings.spCalType, 0)
                ] == CalendarType.GREGORIAN;
    }

    public static boolean night(Context c) {
        return (c.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES;
    }
}
