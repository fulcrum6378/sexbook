package ir.mahdiparastesh.sexbook.mdtp.gdate;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public interface DatePickerController {

    void onYearSelected(int year);

    void onDayOfMonthSelected(int year, int month, int day);

    void registerOnDateChangedListener(DatePickerDialog.OnDateChangedListener listener);

    @SuppressWarnings("unused")
    void unregisterOnDateChangedListener(DatePickerDialog.OnDateChangedListener listener);

    MonthAdapter.CalendarDay getSelectedDay();

    boolean isThemeDark();

    int getAccentColor();

    boolean isHighlighted(int year, int month, int day);

    int getFirstDayOfWeek();

    int getMinYear();

    int getMaxYear();

    Calendar getStartDate();

    Calendar getEndDate();

    boolean isOutOfRange(int year, int month, int day);

    void tryVibrate();

    TimeZone getTimeZone();

    Locale getLocale();

    DatePickerDialog.Version getVersion();

    DatePickerDialog.ScrollOrientation getScrollOrientation();
}
