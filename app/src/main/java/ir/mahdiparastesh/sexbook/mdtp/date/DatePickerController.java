package ir.mahdiparastesh.sexbook.mdtp.date;

import android.icu.util.Calendar;
import android.icu.util.TimeZone;

import java.util.Locale;

public interface DatePickerController<CAL extends Calendar> {

    void onYearSelected(int year);

    void onDayOfMonthSelected(int year, int month, int day);

    void registerOnDateChangedListener(DatePickerDialog.OnDateChangedListener listener);

    @SuppressWarnings("unused")
    void unregisterOnDateChangedListener(DatePickerDialog.OnDateChangedListener listener);

    MonthAdapter.CalendarDay<CAL> getSelectedDay();

    int getAccentColor();

    boolean isHighlighted(int year, int month, int day);

    int getFirstDayOfWeek();

    int getMinYear();

    int getMaxYear();

    CAL getStartDate();

    CAL getEndDate();

    boolean isOutOfRange(int year, int month, int day);

    void tryVibrate();

    TimeZone getTimeZone();

    Locale getLocale();

    DatePickerDialog.Version getVersion();

    DatePickerDialog.ScrollOrientation getScrollOrientation();

    Class<CAL> getCalendarType();
}
