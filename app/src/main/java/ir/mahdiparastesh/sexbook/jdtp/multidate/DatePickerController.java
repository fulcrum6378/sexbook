package ir.mahdiparastesh.sexbook.jdtp.multidate;

import java.util.ArrayList;

import ir.mahdiparastesh.sexbook.jdtp.utils.PersianCalendar;

public interface DatePickerController {

    void onYearSelected(int year);

    void onDaysOfMonthSelected(ArrayList<PersianCalendar> selectedDays);

    void registerOnDateChangedListener(MultiDatePickerDialog.OnDateChangedListener listener);

    void unregisterOnDateChangedListener(MultiDatePickerDialog.OnDateChangedListener listener);

    ArrayList<PersianCalendar> getSelectedDays();

    void setSelectedDays(ArrayList<PersianCalendar> selectedDays);

    boolean isThemeDark();

    PersianCalendar[] getHighlightedDays();

    PersianCalendar[] getSelectableDays();

    int getFirstDayOfWeek();

    int getMinYear();

    int getMaxYear();

    int getSelectedYear();

    PersianCalendar getMinDate();

    PersianCalendar getMaxDate();

    void tryVibrate();
}
