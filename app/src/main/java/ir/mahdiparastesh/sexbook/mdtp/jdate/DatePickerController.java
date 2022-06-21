package ir.mahdiparastesh.sexbook.mdtp.jdate;

import ir.mahdiparastesh.sexbook.mdtp.PersianCalendar;

public interface DatePickerController {

    void onYearSelected(int year);

    void onDayOfMonthSelected(int year, int month, int day);

    void registerOnDateChangedListener(DatePickerDialog.OnDateChangedListener listener);

    @SuppressWarnings("unused")
    void unregisterOnDateChangedListener(DatePickerDialog.OnDateChangedListener listener);

    MonthAdapter.CalendarDay getSelectedDay();

    PersianCalendar[] getHighlightedDays();

    PersianCalendar[] getSelectableDays();

    int getFirstDayOfWeek();

    int getMinYear();

    int getMaxYear();

    PersianCalendar getMinDate();

    PersianCalendar getMaxDate();

    void tryVibrate();
}
