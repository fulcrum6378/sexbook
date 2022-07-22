package ir.mahdiparastesh.sexbook.mdtp.date;

import android.content.Context;
import android.icu.util.Calendar;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Locale;
import java.util.Objects;

import ir.mahdiparastesh.sexbook.mdtp.GravitySnapHelper;
import ir.mahdiparastesh.sexbook.mdtp.Utils;
import ir.mahdiparastesh.sexbook.mdtp.date.DatePickerDialog.OnDateChangedListener;

@SuppressWarnings("unchecked")
public abstract class DayPickerView<CAL extends Calendar> extends RecyclerView implements OnDateChangedListener {

    protected Context mContext;

    // highlighted time
    protected MonthAdapter.CalendarDay<CAL> mSelectedDay;
    protected MonthAdapter<CAL> mAdapter;

    protected MonthAdapter.CalendarDay<CAL> mTempDay;

    private OnPageListener pageListener;
    private DatePickerController<CAL> mController;

    public interface OnPageListener {
        void onPageChanged(int position);
    }

    public DayPickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        DatePickerDialog.ScrollOrientation scrollOrientation = DatePickerDialog.ScrollOrientation.HORIZONTAL;
        init(context, scrollOrientation);
    }

    public DayPickerView(Context context, DatePickerController<CAL> controller) {
        super(context);
        init(context, controller.getScrollOrientation());
        setController(controller);
    }

    protected void setController(DatePickerController<CAL> controller) {
        mController = controller;
        mController.registerOnDateChangedListener(this);
        mSelectedDay = new MonthAdapter.CalendarDay<>(
                mController.getTimeZone(), mController.getCalendarType());
        mTempDay = new MonthAdapter.CalendarDay<>(
                mController.getTimeZone(), mController.getCalendarType());
        refreshAdapter();
    }

    public void init(Context context, DatePickerDialog.ScrollOrientation scrollOrientation) {
        @RecyclerView.Orientation
        int layoutOrientation = scrollOrientation == DatePickerDialog.ScrollOrientation.VERTICAL
                ? RecyclerView.VERTICAL
                : RecyclerView.HORIZONTAL;
        LinearLayoutManager linearLayoutManager =
                new LinearLayoutManager(context, layoutOrientation, false);
        setLayoutManager(linearLayoutManager);
        setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        setVerticalScrollBarEnabled(false);
        setHorizontalScrollBarEnabled(false);
        setClipChildren(false);

        mContext = context;
        setUpRecyclerView(scrollOrientation);
    }

    protected void setUpRecyclerView(DatePickerDialog.ScrollOrientation scrollOrientation) {
        setVerticalScrollBarEnabled(false);
        setFadingEdgeLength(0);
        int gravity = scrollOrientation == DatePickerDialog.ScrollOrientation.VERTICAL
                ? Gravity.TOP : Gravity.START;
        GravitySnapHelper helper = new GravitySnapHelper(gravity, position -> {
            // Leverage the fact that the SnapHelper figures out which position is shown and
            // pass this on to our PageListener after the snap has happened
            if (pageListener != null) pageListener.onPageChanged(position);
        });
        helper.attachToRecyclerView(this);
    }

    public void onChange() {
        refreshAdapter();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        final MonthAdapter.CalendarDay<CAL> focusedDay = findAccessibilityFocus();
        restoreAccessibilityFocus(focusedDay);
    }

    protected void refreshAdapter() {
        if (mAdapter == null) mAdapter = createMonthAdapter(mController);
        else {
            mAdapter.setSelectedDay(mSelectedDay);
            if (pageListener != null) pageListener.onPageChanged(getMostVisiblePosition());
        }
        // refresh the view with the new parameters
        setAdapter(mAdapter);
    }

    public abstract MonthAdapter<CAL> createMonthAdapter(DatePickerController<CAL> controller);

    public void setOnPageListener(@Nullable OnPageListener pageListener) {
        this.pageListener = pageListener;
    }

    @Nullable
    @SuppressWarnings("unused")
    public OnPageListener getOnPageListener() {
        return pageListener;
    }

    /**
     * This moves to the specified time in the view. If the time is not already
     * in range it will move the list so that the first of the month containing
     * the time is at the top of the view. If the new time is already in view
     * the list will not be scrolled unless forceScroll is true. This time may
     * optionally be highlighted as selected as well.
     */
    @SuppressWarnings("UnusedReturnValue")
    public boolean goTo(MonthAdapter.CalendarDay<CAL> day, boolean animate, boolean setSelected,
                        boolean forceScroll) {
        // Set the selected day
        if (setSelected) mSelectedDay.set(day);

        mTempDay.set(day);
        int minMonth = mController.getStartDate().get(Calendar.MONTH);
        final int position = (day.year - mController.getMinYear())
                * MonthAdapter.MONTHS_IN_YEAR + day.month - minMonth;

        View child;
        int i = 0;
        int top;
        // Find a child that's completely in the view
        do {
            child = getChildAt(i++);
            if (child == null) {
                break;
            }
            top = child.getTop();
        } while (top < 0);

        // Compute the first and last position visible
        int selectedPosition = child != null ? getChildAdapterPosition(child) : 0;
        if (setSelected) mAdapter.setSelectedDay(mSelectedDay);

        if (position != selectedPosition || forceScroll) {
            if (animate) {
                smoothScrollToPosition(position);
                if (pageListener != null) pageListener.onPageChanged(position);
                return true;
            } else postSetSelection(position);
        }
        return false;
    }

    public void postSetSelection(final int position) {
        clearFocus();
        post(() -> {
            ((LinearLayoutManager) Objects.requireNonNull(getLayoutManager()))
                    .scrollToPositionWithOffset(position, 0);
            restoreAccessibilityFocus(mSelectedDay);
            if (pageListener != null) pageListener.onPageChanged(position);
        });
    }

    public int getMostVisiblePosition() {
        return getChildAdapterPosition(Objects.requireNonNull(getMostVisibleMonth()));
    }

    public @Nullable
    MonthView<CAL> getMostVisibleMonth() {
        boolean verticalScroll = mController.getScrollOrientation() == DatePickerDialog.ScrollOrientation.VERTICAL;
        final int maxSize = verticalScroll ? getHeight() : getWidth();
        int maxDisplayedSize = 0;
        int i = 0;
        int size = 0;
        MonthView<CAL> mostVisibleMonth = null;

        while (size < maxSize) {
            View child = getChildAt(i);
            if (child == null) break;

            size = verticalScroll ? child.getBottom() : child.getRight();
            int endPosition = verticalScroll ? child.getTop() : child.getLeft();
            int displayedSize = Math.min(size, maxSize) - Math.max(0, endPosition);
            if (displayedSize > maxDisplayedSize) {
                mostVisibleMonth = (MonthView<CAL>) child;
                maxDisplayedSize = displayedSize;
            }
            i++;
        }
        return mostVisibleMonth;
    }

    public int getCount() {
        return mAdapter.getItemCount();
    }

    /**
     * This should only be called when the DayPickerView is visible, or when it has already been
     * requested to be visible
     */
    @Override
    public void onDateChanged() {
        goTo(mController.getSelectedDay(), false, true, true);
    }

    /**
     * Attempts to return the date that has accessibility focus.
     */
    private MonthAdapter.CalendarDay<CAL> findAccessibilityFocus() {
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            if (child instanceof MonthView) {
                final MonthAdapter.CalendarDay<CAL> focus = ((MonthView<CAL>) child).getAccessibilityFocus();
                if (focus != null) return focus;
            }
        }

        return null;
    }

    /**
     * Attempts to restore accessibility focus to a given date. No-op if
     * {@code day} is {@code null}.
     */
    @SuppressWarnings("UnusedReturnValue")
    private boolean restoreAccessibilityFocus(MonthAdapter.CalendarDay<CAL> day) {
        if (day == null) return false;

        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            if (child instanceof MonthView)
                if (((MonthView<CAL>) child).restoreAccessibilityFocus(day))
                    return true;
        }

        return false;
    }

    @Override
    public void onInitializeAccessibilityEvent(@NonNull AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setItemCount(-1);
    }

    void accessibilityAnnouncePageChanged() {
        MonthView<CAL> mv = getMostVisibleMonth();
        if (mv != null) {
            String monthYear = getMonthAndYearString(mContext,
                    mv.mMonth, mv.mYear, mController.getLocale(), mController.getCalendarType());
            Utils.tryAccessibilityAnnounce(this, monthYear);
        }
    }

    private static <CAL extends Calendar> String getMonthAndYearString(
            Context c, int month, int year, Locale locale, Class<CAL> calendarType) {
        CAL calendar = Utils.createCalendar(calendarType);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.YEAR, year);
        return new LocalDateFormat(c, calendarType, "MMMM yyyy", locale).format(calendar);
    }
}
