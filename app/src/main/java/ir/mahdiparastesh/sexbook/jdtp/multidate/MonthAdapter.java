package ir.mahdiparastesh.sexbook.jdtp.multidate;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.HashMap;

import ir.mahdiparastesh.sexbook.jdtp.multidate.MonthView.OnDayClickListener;
import ir.mahdiparastesh.sexbook.jdtp.utils.PersianCalendar;

public abstract class MonthAdapter extends BaseAdapter implements OnDayClickListener {

    private final Context mContext;
    protected final DatePickerController mController;

    private final ArrayList<PersianCalendar> mSelectedDays;

    protected static final int MONTHS_IN_YEAR = 12;

    public static class CalendarDay {
        private PersianCalendar mPersianCalendar;
        int year;
        int month;
        int day;

        public CalendarDay() {
            setTime(System.currentTimeMillis());
        }

        public CalendarDay(long timeInMillis) {
            setTime(timeInMillis);
        }

        public CalendarDay(PersianCalendar calendar) {
            year = calendar.getPersianYear();
            month = calendar.getPersianMonth();
            day = calendar.getPersianDay();
        }

        public CalendarDay(int year, int month, int day) {
            setDay(year, month, day);
        }

        public void set(CalendarDay date) {
            year = date.year;
            month = date.month;
            day = date.day;
        }

        public void setDay(int year, int month, int day) {
            this.year = year;
            this.month = month;
            this.day = day;
        }

        private void setTime(long timeInMillis) {
            if (mPersianCalendar == null) {
                mPersianCalendar = new PersianCalendar();
            }
            mPersianCalendar.setTimeInMillis(timeInMillis);
            month = mPersianCalendar.getPersianMonth();
            year = mPersianCalendar.getPersianYear();
            day = mPersianCalendar.getPersianDay();
        }

        public boolean same(CalendarDay date) {
            return date.day == day && date.year == year && date.month == month;
        }

        public int getYear() {
            return year;
        }

        public int getMonth() {
            return month;
        }

        public int getDay() {
            return day;
        }

        public PersianCalendar getPersianCalendar() {
            if (mPersianCalendar == null) {
                mPersianCalendar = new PersianCalendar();
                mPersianCalendar.setPersianDate(year, month, day);
            }
            return mPersianCalendar;
        }
    }

    public MonthAdapter(Context c, DatePickerController controller) {
        mContext = c;
        mController = controller;
        mSelectedDays = mController.getSelectedDays();
    }

    @Override
    public int getCount() {
        return ((mController.getMaxYear() - mController.getMinYear()) + 1) * MONTHS_IN_YEAR;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MonthView v;
        HashMap<String, Object> drawingParams = null;
        if (convertView != null) {
            v = (MonthView) convertView;
            drawingParams = (HashMap<String, Object>) v.getTag();
        } else {
            v = createMonthView(mContext);
            LayoutParams params = new LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            v.setLayoutParams(params);
            v.setClickable(true);
            v.setOnDayClickListener(this);
        }
        if (drawingParams == null) drawingParams = new HashMap<>();
        drawingParams.clear();

        final int month = position % MONTHS_IN_YEAR;
        final int year = position / MONTHS_IN_YEAR + mController.getMinYear();

        ArrayList<Integer> days = new ArrayList<>();
        for (PersianCalendar persianCalendar : mSelectedDays)
            if (isSelectedDayInMonth(new CalendarDay(persianCalendar), year, month))
                days.add(persianCalendar.getPersianDay());

        v.reuse();

        drawingParams.put(MonthView.VIEW_PARAMS_SELECTED_DAYS, days);
        drawingParams.put(MonthView.VIEW_PARAMS_YEAR, year);
        drawingParams.put(MonthView.VIEW_PARAMS_MONTH, month);
        drawingParams.put(MonthView.VIEW_PARAMS_WEEK_START, mController.getFirstDayOfWeek());
        v.setMonthParams(drawingParams);
        v.invalidate();
        return v;
    }

    public abstract MonthView createMonthView(Context c);

    private boolean isSelectedDayInMonth(CalendarDay selectedDay, int year, int month) {
        return selectedDay.year == year && selectedDay.month == month;
    }


    @Override
    public void onDayClick(MonthView view, CalendarDay day) {
        if (day != null) onDayTapped(day);
    }

    protected void onDayTapped(CalendarDay day) {
        mController.tryVibrate();
        notifySelectedDays(day);
        notifyDataSetChanged();
        mController.onDaysOfMonthSelected(mSelectedDays);
    }

    private void notifySelectedDays(CalendarDay day) {
        PersianCalendar toRemove = null;
        for (PersianCalendar calendarDay : mSelectedDays)
            if (day.same(new CalendarDay(calendarDay))) {
                toRemove = calendarDay;
                break;
            }

        if (mSelectedDays.size() > 1 && toRemove != null)
            mSelectedDays.remove(toRemove);
        else {
            mSelectedDays.add(day.getPersianCalendar());
            mSelectedDays.sort((o1, o2) ->
                    o1.getTimeInMillis() > o2.getTimeInMillis() ? 1 : 0);
        }
    }
}
