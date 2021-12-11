package ir.mahdiparastesh.sexbook.jdtp.date;

import ir.mahdiparastesh.sexbook.jdtp.utils.PersianCalendar;

public interface DatePickerController {

    void onYearSelected(int year);

    void onDayOfMonthSelected(int year, int month, int day);

    void registerOnDateChangedListener(DatePickerDialog.OnDateChangedListener listener);

    void unregisterOnDateChangedListener(DatePickerDialog.OnDateChangedListener listener);

    MonthAdapter.CalendarDay getSelectedDay();

    boolean isThemeDark();

    PersianCalendar[] getHighlightedDays();

    PersianCalendar[] getSelectableDays();

    int getFirstDayOfWeek();

    int getMinYear();

    int getMaxYear();

    PersianCalendar getMinDate();

    PersianCalendar getMaxDate();

    void tryVibrate();
}
