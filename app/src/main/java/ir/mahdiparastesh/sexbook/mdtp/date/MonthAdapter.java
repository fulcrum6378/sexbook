package ir.mahdiparastesh.sexbook.mdtp.date;

import android.annotation.SuppressLint;
import android.content.Context;
import android.icu.util.Calendar;
import android.icu.util.TimeZone;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import ir.mahdiparastesh.sexbook.mdtp.Utils;
import ir.mahdiparastesh.sexbook.mdtp.date.MonthView.OnDayClickListener;

public abstract class MonthAdapter<CAL extends Calendar>
        extends RecyclerView.Adapter<MonthAdapter.MonthViewHolder<CAL>> implements OnDayClickListener<CAL> {

    protected final DatePickerController<CAL> mController;

    private CalendarDay<CAL> mSelectedDay;

    protected static final int MONTHS_IN_YEAR = 12;

    public static class CalendarDay<CAL extends Calendar> {
        private Class<CAL> mCalendarType;
        private CAL calendar;
        int year;
        int month;
        int day;
        TimeZone mTimeZone;

        public CalendarDay(TimeZone timeZone, Class<CAL> calendarType) {
            mTimeZone = timeZone;
            mCalendarType = calendarType;
            setTime(System.currentTimeMillis());
        }

        public CalendarDay(long timeInMillis, TimeZone timeZone, Class<CAL> calendarType) {
            mTimeZone = timeZone;
            mCalendarType = calendarType;
            setTime(timeInMillis);
        }

        public CalendarDay(CAL calendar, TimeZone timeZone, Class<CAL> calendarType) {
            mTimeZone = timeZone;
            mCalendarType = calendarType;
            year = calendar.get(Calendar.YEAR);
            month = calendar.get(Calendar.MONTH);
            day = calendar.get(Calendar.DAY_OF_MONTH);
        }

        @SuppressWarnings("unused")
        public CalendarDay(int year, int month, int day) {
            setDay(year, month, day);
        }

        public CalendarDay(int year, int month, int day, TimeZone timezone) {
            mTimeZone = timezone;
            setDay(year, month, day);
        }

        public void set(CalendarDay<CAL> date) {
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
            if (calendar == null) calendar = Utils.createCalendar(mCalendarType, mTimeZone);
            calendar.setTimeInMillis(timeInMillis);
            month = calendar.get(Calendar.MONTH);
            year = calendar.get(Calendar.YEAR);
            day = calendar.get(Calendar.DAY_OF_MONTH);
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
    }

    public MonthAdapter(DatePickerController<CAL> controller) {
        mController = controller;
        init();
        setSelectedDay(mController.getSelectedDay());
        setHasStableIds(true);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setSelectedDay(CalendarDay<CAL> day) {
        mSelectedDay = day;
        notifyDataSetChanged();
    }

    @SuppressWarnings("unused")
    public CalendarDay<CAL> getSelectedDay() {
        return mSelectedDay;
    }

    protected void init() {
        mSelectedDay = new CalendarDay<>(
                System.currentTimeMillis(), mController.getTimeZone(), mController.getCalendarType());
    }

    @Override
    @NonNull
    public MonthViewHolder<CAL> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        MonthView<CAL> v = createMonthView(parent.getContext());
        // Set up the new view
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        v.setLayoutParams(params);
        v.setClickable(true);
        v.setOnDayClickListener(this);

        return new MonthViewHolder<>(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MonthViewHolder<CAL> holder, int position) {
        holder.bind(position, mController, mSelectedDay);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        CAL endDate = mController.getEndDate();
        CAL startDate = mController.getStartDate();
        int endMonth = endDate.get(Calendar.YEAR) * MONTHS_IN_YEAR + endDate.get(Calendar.MONTH);
        int startMonth = startDate.get(Calendar.YEAR) * MONTHS_IN_YEAR + startDate.get(Calendar.MONTH);
        return endMonth - startMonth + 1;
    }

    public abstract MonthView<CAL> createMonthView(Context context);

    @Override
    public void onDayClick(MonthView<CAL> view, CalendarDay<CAL> day) {
        if (day != null) onDayTapped(day);
    }

    /**
     * Maintains the same hour/min/sec but moves the day to the tapped day.
     */
    protected void onDayTapped(CalendarDay<CAL> day) {
        mController.tryVibrate();
        mController.onDayOfMonthSelected(day.year, day.month, day.day);
        setSelectedDay(day);
    }

    static class MonthViewHolder<CAL extends Calendar> extends RecyclerView.ViewHolder {

        public MonthViewHolder(MonthView itemView) {
            super(itemView);

        }

        void bind(int position, DatePickerController<CAL> mController, CalendarDay<CAL> selectedCalendarDay) {
            final int month = (position + mController.getStartDate().get(Calendar.MONTH)) % MONTHS_IN_YEAR;
            final int year = (position + mController.getStartDate().get(Calendar.MONTH)) / MONTHS_IN_YEAR + mController.getMinYear();

            int selectedDay = -1;
            if (isSelectedDayInMonth(selectedCalendarDay, year, month))
                selectedDay = selectedCalendarDay.day;

            //noinspection unchecked
            ((MonthView<CAL>) itemView).setMonthParams(selectedDay, year, month, mController.getFirstDayOfWeek());
            this.itemView.invalidate();
        }

        private boolean isSelectedDayInMonth(CalendarDay<CAL> selectedDay, int year, int month) {
            return selectedDay.year == year && selectedDay.month == month;
        }
    }
}
