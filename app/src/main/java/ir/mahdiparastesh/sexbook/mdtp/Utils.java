package ir.mahdiparastesh.sexbook.mdtp;

import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.icu.text.DateFormatSymbols;
import android.icu.util.Calendar;
import android.icu.util.GregorianCalendar;
import android.icu.util.TimeZone;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import java.util.Locale;

import ir.mahdiparastesh.sexbook.R;

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
        context.getTheme().resolveAttribute(android.R.attr.colorAccent, typedValue, true);
        return typedValue.data;
    }

    public static <CAL extends Calendar> CAL trimToMidnight(CAL calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
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

    public static boolean night(Context c) {
        return (c.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES;
    }

    @SuppressWarnings("unchecked")
    public static Class<? extends Calendar> createCalendarType(String name) {
        try {
            return (Class<? extends Calendar>) Class.forName(name);
        } catch (ClassNotFoundException e) {
            return GregorianCalendar.class;
        }
    }

    @NonNull
    public static <CAL extends Calendar> CAL createCalendar(
            Class<CAL> type, @Nullable TimeZone tz) {
        try {
            CAL ins = type.newInstance();
            if (tz != null) ins.setTimeZone(tz);
            return ins;
        } catch (IllegalAccessException | InstantiationException e) {
            //noinspection unchecked
            return (CAL) new GregorianCalendar();
        }
    }

    public static <CAL extends Calendar> CAL createCalendar(
            Class<CAL> type) {
        return createCalendar(type, null);
    }

    public static DateFormatSymbols localSymbols( // only this one needs globalisation.
            Context c, Class<? extends Calendar> calendarType, Locale locale) {
        DateFormatSymbols symbols = DateFormatSymbols.getInstance(locale);
        switch (calendarType.getSimpleName()) {
            case "HumanistIranianCalendar":
                symbols.setMonths(c.getResources().getStringArray(R.array.persianMonths));
                symbols.setShortMonths(c.getResources().getStringArray(R.array.shortPersianMonths));
                break;
            case "IndianCalendar":
                symbols.setMonths(c.getResources().getStringArray(R.array.indianMonths));
                symbols.setShortMonths(c.getResources().getStringArray(R.array.shortIndianMonths));
                break;
        }
        return symbols;
    }

    public static DateFormatSymbols localSymbols(
            Context c, Class<? extends Calendar> calendarType) {
        return localSymbols(c, calendarType, Locale.getDefault());
    }

    public static <CAL extends Calendar> String accessibilityDate(Context c, CAL calendar) {
        return calendar.get(Calendar.DAY_OF_MONTH) + " " +
                Utils.localSymbols(c, calendar.getClass())
                        .getMonths()[calendar.get(Calendar.MONTH)]
                + " " + calendar.get(Calendar.YEAR);
    }
}
