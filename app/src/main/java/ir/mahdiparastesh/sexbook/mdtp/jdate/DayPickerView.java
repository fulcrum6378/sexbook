package ir.mahdiparastesh.sexbook.mdtp.jdate;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;

import androidx.annotation.NonNull;

import ir.mahdiparastesh.sexbook.mdtp.Utils;
import ir.mahdiparastesh.sexbook.mdtp.jdate.DatePickerDialog.OnDateChangedListener;
import ir.mahdiparastesh.sexbook.mdtp.PersianCalendar;

public abstract class DayPickerView extends ListView implements OnScrollListener,
        OnDateChangedListener {

    protected static final int GOTO_SCROLL_DURATION = 250;
    protected static final int SCROLL_CHANGE_DELAY = 40;
    public static final int LIST_TOP_OFFSET = -1;

    protected final float mFriction = 1.0f;
    protected Handler mHandler;

    protected final MonthAdapter.CalendarDay mSelectedDay = new MonthAdapter.CalendarDay();
    protected MonthAdapter mAdapter;
    protected final MonthAdapter.CalendarDay mTempDay = new MonthAdapter.CalendarDay();

    protected int mPreviousScrollState = OnScrollListener.SCROLL_STATE_IDLE;
    protected int mCurrentScrollState = OnScrollListener.SCROLL_STATE_IDLE;
    private DatePickerController mController;
    private boolean mPerformingScroll;

    public DayPickerView(Context c, AttributeSet attrs) {
        super(c, attrs);
        init();
    }

    public DayPickerView(Context c, DatePickerController controller) {
        super(c);
        init();
        setController(controller);
    }

    public void setController(DatePickerController controller) {
        mController = controller;
        mController.registerOnDateChangedListener(this);
        refreshAdapter();
        onDateChanged();
    }

    public void init() {
        mHandler = new Handler();
        setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        setDrawSelectorOnTop(false);
        setUpListView();
    }

    public void onChange() {
        refreshAdapter();
    }

    protected void refreshAdapter() {
        if (mAdapter == null) mAdapter = createMonthAdapter(getContext(), mController);
        else mAdapter.setSelectedDay(mSelectedDay);
        setAdapter(mAdapter);
    }

    public abstract MonthAdapter createMonthAdapter(Context c, DatePickerController controller);

    protected void setUpListView() {
        setCacheColorHint(0);
        setDivider(null);
        setItemsCanFocus(true);
        setFastScrollEnabled(false);
        setVerticalScrollBarEnabled(false);
        setOnScrollListener(this);
        setFadingEdgeLength(0);
        setFriction(ViewConfiguration.getScrollFriction() * mFriction);
    }

    public void goTo(MonthAdapter.CalendarDay day, boolean animate, boolean setSelected,
                     boolean forceScroll) {
        if (setSelected) mSelectedDay.set(day);

        mTempDay.set(day);
        final int position = (day.year - mController.getMinYear())
                * MonthAdapter.MONTHS_IN_YEAR + day.month;

        View child;
        int i = 0, top;
        do {
            child = getChildAt(i++);
            if (child == null)
                break;
            top = child.getTop();
        } while (top < 0);

        int selectedPosition;
        if (child != null) selectedPosition = getPositionForView(child);
        else selectedPosition = 0;

        if (setSelected) mAdapter.setSelectedDay(mSelectedDay);

        if (position != selectedPosition || forceScroll) {
            setMonthDisplayed();
            mPreviousScrollState = OnScrollListener.SCROLL_STATE_FLING;
            if (animate)
                smoothScrollToPositionFromTop(position, LIST_TOP_OFFSET, GOTO_SCROLL_DURATION);
            else
                postSetSelection(position);
        } else if (setSelected) setMonthDisplayed();
    }

    public void postSetSelection(final int position) {
        clearFocus();
        post(() -> setSelection(position));
        onScrollStateChanged(this, OnScrollListener.SCROLL_STATE_IDLE);
    }

    @Override
    public void onScroll(
            AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        MonthView child = (MonthView) view.getChildAt(0);
        if (child != null) mPreviousScrollState = mCurrentScrollState;
    }

    protected void setMonthDisplayed() {
        invalidateViews();
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        mScrollStateChangedRunnable.doScrollStateChange(scrollState);
    }

    protected final ScrollStateRunnable mScrollStateChangedRunnable = new ScrollStateRunnable();

    protected class ScrollStateRunnable implements Runnable {
        private int mNewState;

        public void doScrollStateChange(int scrollState) {
            mHandler.removeCallbacks(this);
            mNewState = scrollState;
            mHandler.postDelayed(this, SCROLL_CHANGE_DELAY);
        }

        @Override
        public void run() {
            mCurrentScrollState = mNewState;

            if (mNewState == OnScrollListener.SCROLL_STATE_IDLE
                    && mPreviousScrollState != OnScrollListener.SCROLL_STATE_IDLE
                    && mPreviousScrollState != OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                mPreviousScrollState = mNewState;
                int i = 0;
                View child = getChildAt(i);
                while (child != null && child.getBottom() <= 0) {
                    child = getChildAt(++i);
                }
                if (child == null)
                    return;
                int firstPosition = getFirstVisiblePosition();
                int lastPosition = getLastVisiblePosition();
                boolean scroll = firstPosition != 0 && lastPosition != getCount() - 1;
                final int top = child.getTop();
                final int bottom = child.getBottom();
                final int midpoint = getHeight() / 2;
                if (scroll && top < LIST_TOP_OFFSET) {
                    if (bottom > midpoint) smoothScrollBy(top, GOTO_SCROLL_DURATION);
                    else smoothScrollBy(bottom, GOTO_SCROLL_DURATION);
                }
            } else mPreviousScrollState = mNewState;
        }
    }

    public int getMostVisiblePosition() {
        final int firstPosition = getFirstVisiblePosition();
        final int height = getHeight();

        int maxDisplayedHeight = 0;
        int mostVisibleIndex = 0;
        int i = 0;
        int bottom = 0;
        while (bottom < height) {
            View child = getChildAt(i);
            if (child == null) {
                break;
            }
            bottom = child.getBottom();
            int displayedHeight = Math.min(bottom, height) - Math.max(0, child.getTop());
            if (displayedHeight > maxDisplayedHeight) {
                mostVisibleIndex = i;
                maxDisplayedHeight = displayedHeight;
            }
            i++;
        }
        return firstPosition + mostVisibleIndex;
    }

    @Override
    public void onDateChanged() {
        goTo(mController.getSelectedDay(), false, true, true);
    }

    private MonthAdapter.CalendarDay findAccessibilityFocus() {
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            if (child instanceof MonthView) {
                final MonthAdapter.CalendarDay focus = ((MonthView) child).getAccessibilityFocus();
                if (focus != null) return focus;
            }
        }

        return null;
    }

    private void restoreAccessibilityFocus(MonthAdapter.CalendarDay day) {
        if (day == null) return;

        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            if (child instanceof MonthView)
                if (((MonthView) child).restoreAccessibilityFocus(day))
                    return;
        }
    }

    @Override
    protected void layoutChildren() {
        final MonthAdapter.CalendarDay focusedDay = findAccessibilityFocus();
        super.layoutChildren();
        if (mPerformingScroll)
            mPerformingScroll = false;
        else restoreAccessibilityFocus(focusedDay);
    }

    @Override
    public void onInitializeAccessibilityEvent(@NonNull AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setItemCount(-1);
    }

    private static String getMonthAndYearString(Context c, MonthAdapter.CalendarDay day) {
        PersianCalendar mPersianCalendar = new PersianCalendar();
        mPersianCalendar.setPersianDate(day.year, day.month, day.day);

        String sb = "";
        sb += mPersianCalendar.getPersianMonthName(c);
        sb += " ";
        sb += mPersianCalendar.getPersianYear();
        return sb;
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(@NonNull AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_BACKWARD);
        info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD);
    }

    @Override
    public boolean performAccessibilityAction(int action, Bundle arguments) {
        if (action != AccessibilityNodeInfo.ACTION_SCROLL_FORWARD &&
                action != AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD) {
            return super.performAccessibilityAction(action, arguments);
        }

        int firstVisiblePosition = getFirstVisiblePosition();
        int month = firstVisiblePosition % 12;
        int year = firstVisiblePosition / 12 + mController.getMinYear();
        MonthAdapter.CalendarDay day = new MonthAdapter.CalendarDay(year, month, 1);

        if (action == AccessibilityNodeInfo.ACTION_SCROLL_FORWARD) {
            day.month++;
            if (day.month == 12) {
                day.month = 0;
                day.year++;
            }
        } else {
            View firstVisibleView = getChildAt(0);
            if (firstVisibleView != null && firstVisibleView.getTop() >= -1) {
                day.month--;
                if (day.month == -1) {
                    day.month = 11;
                    day.year--;
                }
            }
        }

        Utils.tryAccessibilityAnnounce(this, Utils.getPersianNumbers(
                getMonthAndYearString(getContext(), day)));
        goTo(day, true, false, true);
        mPerformingScroll = true;
        return true;
    }
}
