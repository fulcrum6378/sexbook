package ir.mahdiparastesh.sexbook.jdtp.date;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.BaseAdapter;

import java.util.HashMap;

import ir.mahdiparastesh.sexbook.jdtp.date.MonthView.OnDayClickListener;
import ir.mahdiparastesh.sexbook.jdtp.utils.PersianCalendar;

public abstract class MonthAdapter extends BaseAdapter implements OnDayClickListener {
    private final Context mContext;
    protected final DatePickerController mController;

    private CalendarDay mSelectedDay;

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

        /*public int getYear() {
            return year;
        }

        public int getMonth() {
            return month;
        }

        public int getDay() {
            return day;
        }*/
    }

    public MonthAdapter(Context c, DatePickerController controller) {
        mContext = c;
        mController = controller;
        init();
        setSelectedDay(mController.getSelectedDay());
    }

    public void setSelectedDay(CalendarDay day) {
        mSelectedDay = day;
        notifyDataSetChanged();
    }

    /*public CalendarDay getSelectedDay() {
        return mSelectedDay;
    }*/

    protected void init() {
        mSelectedDay = new CalendarDay(System.currentTimeMillis());
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
        HashMap<String, Integer> drawingParams = null;
        if (convertView != null) {
            v = (MonthView) convertView;
            drawingParams = (HashMap<String, Integer>) v.getTag();
        } else {
            v = createMonthView(mContext);
            LayoutParams params = new LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            v.setLayoutParams(params);
            v.setClickable(true);
            v.setOnDayClickListener(this);
        }
        if (drawingParams == null)
            drawingParams = new HashMap<>();
        drawingParams.clear();

        final int month = position % MONTHS_IN_YEAR;
        final int year = position / MONTHS_IN_YEAR + mController.getMinYear();

        int selectedDay = -1;
        if (isSelectedDayInMonth(year, month))
            selectedDay = mSelectedDay.day;
        v.reuse();

        drawingParams.put(MonthView.VIEW_PARAMS_SELECTED_DAY, selectedDay);
        drawingParams.put(MonthView.VIEW_PARAMS_YEAR, year);
        drawingParams.put(MonthView.VIEW_PARAMS_MONTH, month);
        drawingParams.put(MonthView.VIEW_PARAMS_WEEK_START, mController.getFirstDayOfWeek());
        v.setMonthParams(drawingParams);
        v.invalidate();
        return v;
    }

    public abstract MonthView createMonthView(Context c);

    private boolean isSelectedDayInMonth(int year, int month) {
        return mSelectedDay.year == year && mSelectedDay.month == month;
    }

    @Override
    public void onDayClick(CalendarDay day) {
        if (day != null) onDayTapped(day);
    }

    protected void onDayTapped(CalendarDay day) {
        mController.tryVibrate();
        mController.onDayOfMonthSelected(day.year, day.month, day.day);
        setSelectedDay(day);
    }
}
