package ir.mahdiparastesh.sexbook.mdtp.date;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.customview.widget.ExploreByTouchHelper;

import java.security.InvalidParameterException;
import java.util.List;
import java.util.Locale;

import ir.mahdiparastesh.sexbook.R;
import ir.mahdiparastesh.sexbook.mdtp.Utils;
import ir.mahdiparastesh.sexbook.mdtp.date.MonthAdapter.CalendarDay;

public abstract class MonthView<CAL extends Calendar> extends View {

    protected static final int DEFAULT_SELECTED_DAY = -1;
    protected static final int DEFAULT_WEEK_START = Calendar.SUNDAY; // TODO
    protected static final int DEFAULT_NUM_DAYS = 7;
    protected static final int DEFAULT_NUM_ROWS = 6;
    protected static final int MAX_NUM_ROWS = 6;

    private static final int SELECTED_CIRCLE_ALPHA = 255;

    protected static final int DAY_SEPARATOR_WIDTH = 1;
    protected static int MINI_DAY_NUMBER_TEXT_SIZE;
    protected static int MONTH_LABEL_TEXT_SIZE;
    protected static int MONTH_DAY_LABEL_TEXT_SIZE;
    protected static int MONTH_HEADER_SIZE;
    protected static int MONTH_HEADER_SIZE_V2;
    protected static int DAY_SELECTED_CIRCLE_SIZE;
    protected static int DAY_HIGHLIGHT_CIRCLE_SIZE;
    protected static int DAY_HIGHLIGHT_CIRCLE_MARGIN;

    protected final DatePickerController<CAL> mController;

    // affects the padding on the sides of this view
    protected final int mEdgePadding;

    protected Paint mMonthNumPaint;
    protected Paint mMonthTitlePaint;
    protected Paint mSelectedCirclePaint;
    protected Paint mMonthDayLabelPaint;

    private final StringBuilder mStringBuilder;

    protected int mMonth;
    protected int mYear;
    protected int mWidth;
    protected final int mRowHeight;
    protected boolean mHasToday = false;
    protected int mSelectedDay = -1;
    protected int mToday = DEFAULT_SELECTED_DAY;
    protected int mWeekStart = DEFAULT_WEEK_START;
    protected final int mNumDays = DEFAULT_NUM_DAYS;
    protected int mNumCells = mNumDays;

    private final CAL mCalendar;
    protected final CAL mDayLabelCalendar;
    private final MonthViewTouchHelper mTouchHelper;
    protected int mNumRows = DEFAULT_NUM_ROWS;
    protected OnDayClickListener<CAL> mOnDayClickListener;
    private final boolean mLockAccessibilityDelegate;

    protected final int mDayTextColor;
    protected final int mSelectedDayTextColor;
    protected final int mMonthDayTextColor;
    protected final int mTodayNumberColor;
    protected final int mHighlightedDayTextColor;
    protected final int mDisabledDayTextColor;
    protected final int mMonthTitleColor;

    private LocalDateFormat weekDayLabelFormatter;

    public MonthView(Context context) {
        this(context, null, null);
    }

    public MonthView(Context context, AttributeSet attr, DatePickerController<CAL> controller) {
        super(context, attr);
        mController = controller;
        Resources res = context.getResources();

        mDayLabelCalendar = Utils.createCalendar(mController.getCalendarType(), mController.getTimeZone());
        mCalendar = Utils.createCalendar(mController.getCalendarType(), mController.getTimeZone());

        mDayTextColor = ContextCompat.getColor(context, R.color.mdtp_date_picker_text_normal);
        mMonthDayTextColor = ContextCompat.getColor(context, R.color.mdtp_date_picker_month_day);
        mDisabledDayTextColor = ContextCompat.getColor(context, R.color.mdtp_date_picker_text_disabled);
        mHighlightedDayTextColor = ContextCompat.getColor(context, R.color.mdtp_date_picker_text_highlighted);
        mSelectedDayTextColor = ContextCompat.getColor(context, R.color.mdtp_calendar_selected_day_text);
        mTodayNumberColor = ContextCompat.getColor(context, R.color.mdtp_calendar_today_number);
        mMonthTitleColor = ContextCompat.getColor(context, R.color.mdtp_white);

        mStringBuilder = new StringBuilder(50);

        MINI_DAY_NUMBER_TEXT_SIZE = res.getDimensionPixelSize(R.dimen.mdtp_day_number_size);
        MONTH_LABEL_TEXT_SIZE = res.getDimensionPixelSize(R.dimen.mdtp_month_label_size);
        MONTH_DAY_LABEL_TEXT_SIZE = res.getDimensionPixelSize(R.dimen.mdtp_month_day_label_text_size);
        MONTH_HEADER_SIZE = res.getDimensionPixelOffset(R.dimen.mdtp_month_list_item_header_height);
        MONTH_HEADER_SIZE_V2 = res.getDimensionPixelOffset(R.dimen.mdtp_month_list_item_header_height_v2);
        DAY_SELECTED_CIRCLE_SIZE = mController.getVersion() == DatePickerDialog.Version.VERSION_1
                ? res.getDimensionPixelSize(R.dimen.mdtp_day_number_select_circle_radius)
                : res.getDimensionPixelSize(R.dimen.mdtp_day_number_select_circle_radius_v2);
        DAY_HIGHLIGHT_CIRCLE_SIZE = res
                .getDimensionPixelSize(R.dimen.mdtp_day_highlight_circle_radius);
        DAY_HIGHLIGHT_CIRCLE_MARGIN = res
                .getDimensionPixelSize(R.dimen.mdtp_day_highlight_circle_margin);

        if (mController.getVersion() == DatePickerDialog.Version.VERSION_1) {
            mRowHeight = (res.getDimensionPixelOffset(R.dimen.mdtp_date_picker_view_animator_height)
                    - getMonthHeaderSize()) / MAX_NUM_ROWS;
        } else {
            mRowHeight = (res.getDimensionPixelOffset(R.dimen.mdtp_date_picker_view_animator_height_v2)
                    - getMonthHeaderSize() - MONTH_DAY_LABEL_TEXT_SIZE * 2) / MAX_NUM_ROWS;
        }

        mEdgePadding = mController.getVersion() == DatePickerDialog.Version.VERSION_1
                ? 0
                : context.getResources().getDimensionPixelSize(R.dimen.mdtp_date_picker_view_animator_padding_v2);

        // Set up accessibility components.
        mTouchHelper = getMonthViewTouchHelper();
        ViewCompat.setAccessibilityDelegate(this, mTouchHelper);
        ViewCompat.setImportantForAccessibility(this, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES);
        mLockAccessibilityDelegate = true;

        // Sets up any standard paints that will be used
        initView();
    }

    protected MonthViewTouchHelper getMonthViewTouchHelper() {
        return new MonthViewTouchHelper(this);
    }

    @Override
    public void setAccessibilityDelegate(AccessibilityDelegate delegate) {
        // Workaround for a JB MR1 issue where accessibility delegates on
        // top-level ListView items are overwritten.
        if (!mLockAccessibilityDelegate) {
            super.setAccessibilityDelegate(delegate);
        }
    }

    public void setOnDayClickListener(OnDayClickListener<CAL> listener) {
        mOnDayClickListener = listener;
    }

    @Override
    public boolean dispatchHoverEvent(@NonNull MotionEvent event) {
        // First right-of-refusal goes the touch exploration helper.
        return mTouchHelper.dispatchHoverEvent(event) || super.dispatchHoverEvent(event);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            final int day = getDayFromLocation(event.getX(), event.getY());
            if (day >= 0) onDayClick(day);
        }
        return true;
    }

    /**
     * Sets up the text and style properties for painting. Override this if you
     * want to use a different paint.
     */
    protected void initView() {
        mMonthTitlePaint = new Paint();
        if (mController.getVersion() == DatePickerDialog.Version.VERSION_1)
            mMonthTitlePaint.setFakeBoldText(true);
        mMonthTitlePaint.setAntiAlias(true);
        mMonthTitlePaint.setTextSize(MONTH_LABEL_TEXT_SIZE);
        mMonthTitlePaint.setTypeface(Utils.mdtpMonthTitleFont(getContext()));
        mMonthTitlePaint.setColor(mDayTextColor);
        mMonthTitlePaint.setTextAlign(Align.CENTER);
        mMonthTitlePaint.setStyle(Style.FILL);

        mSelectedCirclePaint = new Paint();
        mSelectedCirclePaint.setFakeBoldText(true);
        mSelectedCirclePaint.setAntiAlias(true);
        mSelectedCirclePaint.setColor(mTodayNumberColor);
        mSelectedCirclePaint.setTextAlign(Align.CENTER);
        mSelectedCirclePaint.setStyle(Style.FILL);
        mSelectedCirclePaint.setAlpha(SELECTED_CIRCLE_ALPHA);

        mMonthDayLabelPaint = new Paint();
        mMonthDayLabelPaint.setAntiAlias(true);
        mMonthDayLabelPaint.setTextSize(MONTH_DAY_LABEL_TEXT_SIZE);
        mMonthDayLabelPaint.setColor(mMonthDayTextColor);
        mMonthDayLabelPaint.setTypeface(Utils.mdtpDayOfWeekFont(getContext()));
        mMonthDayLabelPaint.setStyle(Style.FILL);
        mMonthDayLabelPaint.setTextAlign(Align.CENTER);
        mMonthDayLabelPaint.setFakeBoldText(true);

        mMonthNumPaint = new Paint();
        mMonthNumPaint.setAntiAlias(true);
        mMonthNumPaint.setTextSize(MINI_DAY_NUMBER_TEXT_SIZE);
        mMonthNumPaint.setStyle(Style.FILL);
        mMonthNumPaint.setTextAlign(Align.CENTER);
        mMonthNumPaint.setFakeBoldText(false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawMonthTitle(canvas);
        drawMonthDayLabels(canvas);
        drawMonthNums(canvas);
    }

    private int mDayOfWeekStart = 0;

    /**
     * Sets all the parameters for displaying this week. The only required
     * parameter is the week number. Other parameters have a default value and
     * will only update if a new value is included, except for focus month,
     * which will always default to no focus month if no value is passed in.
     */
    public void setMonthParams(int selectedDay, int year, int month, int weekStart) {
        if (month == -1 && year == -1)
            throw new InvalidParameterException("You must specify month and year for this view");

        mSelectedDay = selectedDay;

        // Allocate space for caching the day numbers and focus values
        mMonth = month;
        mYear = year;

        // Figure out what day today is
        //final Time today = new Time(Time.getCurrentTimezone());
        //today.setToNow();
        final CAL today = Utils.createCalendar(mController.getCalendarType(), mController.getTimeZone());
        mHasToday = false;
        mToday = -1;

        mCalendar.set(Calendar.MONTH, mMonth);
        mCalendar.set(Calendar.YEAR, mYear);
        mCalendar.set(Calendar.DAY_OF_MONTH, 1);
        mDayOfWeekStart = mCalendar.get(Calendar.DAY_OF_WEEK);

        if (weekStart != -1) {
            mWeekStart = weekStart;
        } else {
            mWeekStart = mCalendar.getFirstDayOfWeek();
        }

        mNumCells = mCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int i = 0; i < mNumCells; i++) {
            final int day = i + 1;
            if (sameDay(day, today)) {
                mHasToday = true;
                mToday = day;
            }
        }
        mNumRows = calculateNumRows();

        // Invalidate cached accessibility information.
        mTouchHelper.invalidateRoot();
    }

    @SuppressWarnings("unused")
    public void setSelectedDay(int day) {
        mSelectedDay = day;
    }

    private int calculateNumRows() {
        int offset = findDayOffset();
        int dividend = (offset + mNumCells) / mNumDays;
        int remainder = (offset + mNumCells) % mNumDays;
        return (dividend + (remainder > 0 ? 1 : 0));
    }

    private boolean sameDay(int day, CAL today) {
        return mYear == today.get(Calendar.YEAR) &&
                mMonth == today.get(Calendar.MONTH) &&
                day == today.get(Calendar.DAY_OF_MONTH);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec),
                mRowHeight * mNumRows + getMonthHeaderSize());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mWidth = w;
        mTouchHelper.invalidateRoot();
    }

    public int getMonth() {
        return mMonth;
    }

    public int getYear() {
        return mYear;
    }

    public int getMonthHeight() {
        int scaleFactor = mController.getVersion() == DatePickerDialog.Version.VERSION_1 ? 2 : 3;
        return getMonthHeaderSize() - MONTH_DAY_LABEL_TEXT_SIZE * scaleFactor;
    }

    public int getCellWidth() {
        return (mWidth - mEdgePadding * 2) / mNumDays;
    }

    public int getEdgePadding() {
        return mEdgePadding;
    }

    protected int getMonthHeaderSize() {
        return mController.getVersion() == DatePickerDialog.Version.VERSION_1
                ? MONTH_HEADER_SIZE
                : MONTH_HEADER_SIZE_V2;
    }

    @NonNull
    private String getMonthAndYearString() {
        Locale locale = mController.getLocale();
        String pattern = DateFormat.getBestDateTimePattern(locale, "MMMM yyyy");

        LocalDateFormat formatter = new LocalDateFormat(
                getContext(), mController.getCalendarType(), pattern, locale);
        formatter.setTimeZone(mController.getTimeZone());
        formatter.applyLocalizedPattern(pattern);
        mStringBuilder.setLength(0);
        return formatter.format(mCalendar);
    }

    protected void drawMonthTitle(Canvas canvas) {
        int x = mWidth / 2;
        int y = mController.getVersion() == DatePickerDialog.Version.VERSION_1
                ? (getMonthHeaderSize() - MONTH_DAY_LABEL_TEXT_SIZE) / 2
                : getMonthHeaderSize() / 2 - MONTH_DAY_LABEL_TEXT_SIZE;
        canvas.drawText(getMonthAndYearString(), x, y, mMonthTitlePaint);
    }

    protected void drawMonthDayLabels(Canvas canvas) {
        int y = getMonthHeaderSize() - (MONTH_DAY_LABEL_TEXT_SIZE / 2);
        int dayWidthHalf = (mWidth - mEdgePadding * 2) / (mNumDays * 2);

        for (int i = 0; i < mNumDays; i++) {
            int x = (2 * i + 1) * dayWidthHalf + mEdgePadding;

            int calendarDay = (i + mWeekStart) % mNumDays;
            mDayLabelCalendar.set(Calendar.DAY_OF_WEEK, calendarDay);
            String weekString = getWeekDayLabel(mDayLabelCalendar);
            canvas.drawText(weekString, x, y, mMonthDayLabelPaint);
        }
    }

    protected void drawMonthNums(Canvas canvas) {
        int y = (((mRowHeight + MINI_DAY_NUMBER_TEXT_SIZE) / 2) - DAY_SEPARATOR_WIDTH)
                + getMonthHeaderSize();
        final int dayWidthHalf = (mWidth - mEdgePadding * 2) / (mNumDays * 2);
        int j = findDayOffset();
        for (int dayNumber = 1; dayNumber <= mNumCells; dayNumber++) {
            final int x = (2 * j + 1) * dayWidthHalf + mEdgePadding;
            drawMonthDay(canvas, mYear, mMonth, dayNumber, x, y);

            j++;
            if (j == mNumDays) {
                j = 0;
                y += mRowHeight;
            }
        }
    }

    public abstract void drawMonthDay(Canvas canvas, int year, int month, int day, int x, int y);

    protected int findDayOffset() {
        return (mDayOfWeekStart < mWeekStart ? (mDayOfWeekStart + mNumDays) : mDayOfWeekStart)
                - mWeekStart;
    }


    public int getDayFromLocation(float x, float y) {
        final int day = getInternalDayFromLocation(x, y);
        if (day < 1 || day > mNumCells) {
            return -1;
        }
        return day;
    }

    protected int getInternalDayFromLocation(float x, float y) {
        int dayStart = mEdgePadding;
        if (x < dayStart || x > mWidth - mEdgePadding) {
            return -1;
        }
        // Selection is (x - start) / (pixels/day) == (x -s) * day / pixels
        int row = (int) (y - getMonthHeaderSize()) / mRowHeight;
        int column = (int) ((x - dayStart) * mNumDays / (mWidth - dayStart - mEdgePadding));

        int day = column - findDayOffset() + 1;
        day += row * mNumDays;
        return day;
    }

    private void onDayClick(int day) {
        // If the min / max date are set, only process the click if it's a valid selection.
        if (mController.isOutOfRange(mYear, mMonth, day)) return;

        if (mOnDayClickListener != null) mOnDayClickListener.onDayClick(this,
                new CalendarDay<>(mYear, mMonth, day, mController.getTimeZone()));

        // This is a no-op if accessibility is turned off.
        mTouchHelper.sendEventForVirtualView(day, AccessibilityEvent.TYPE_VIEW_CLICKED);
    }

    protected boolean isHighlighted(int year, int month, int day) {
        return mController.isHighlighted(year, month, day);
    }

    private String getWeekDayLabel(CAL day) {
        Locale locale = mController.getLocale();
        if (weekDayLabelFormatter == null)
            weekDayLabelFormatter = new LocalDateFormat(
                    getContext(), mController.getCalendarType(), "EEEEE", locale);
        return weekDayLabelFormatter.format(day);
    }

    public CalendarDay<CAL> getAccessibilityFocus() {
        final int day = mTouchHelper.getAccessibilityFocusedVirtualViewId();
        if (day >= 0)
            return new CalendarDay<>(mYear, mMonth, day, mController.getTimeZone());
        return null;
    }

    public void clearAccessibilityFocus() {
        mTouchHelper.clearFocusedVirtualView();
    }

    public boolean restoreAccessibilityFocus(CalendarDay<CAL> day) {
        if ((day.year != mYear) || (day.month != mMonth) || (day.day > mNumCells)) {
            return false;
        }
        mTouchHelper.setFocusedVirtualView(day.day);
        return true;
    }

    protected class MonthViewTouchHelper extends ExploreByTouchHelper {
        private static final String DATE_FORMAT = "dd MMMM yyyy";

        private final Rect mTempRect = new Rect();
        private final CAL mTempCalendar =
                Utils.createCalendar(mController.getCalendarType(), mController.getTimeZone());

        MonthViewTouchHelper(View host) {
            super(host);
        }

        void setFocusedVirtualView(int virtualViewId) {
            getAccessibilityNodeProvider(MonthView.this).performAction(
                    virtualViewId, AccessibilityNodeInfoCompat.ACTION_ACCESSIBILITY_FOCUS, null);
        }

        void clearFocusedVirtualView() {
            final int focusedVirtualView = getAccessibilityFocusedVirtualViewId();
            if (focusedVirtualView != ExploreByTouchHelper.INVALID_ID) {
                getAccessibilityNodeProvider(MonthView.this).performAction(
                        focusedVirtualView,
                        AccessibilityNodeInfoCompat.ACTION_CLEAR_ACCESSIBILITY_FOCUS,
                        null);
            }
        }

        @Override
        protected int getVirtualViewAt(float x, float y) {
            final int day = getDayFromLocation(x, y);
            if (day >= 0) return day;
            return ExploreByTouchHelper.INVALID_ID;
        }

        @Override
        protected void getVisibleVirtualViews(List<Integer> virtualViewIds) {
            for (int day = 1; day <= mNumCells; day++) virtualViewIds.add(day);
        }

        @Override
        protected void onPopulateEventForVirtualView(int virtualViewId, @NonNull AccessibilityEvent event) {
            event.setContentDescription(getItemDescription(virtualViewId));
        }

        @Override
        protected void onPopulateNodeForVirtualView(int virtualViewId,
                                                    @NonNull AccessibilityNodeInfoCompat node) {
            getItemBounds(virtualViewId, mTempRect);

            node.setContentDescription(getItemDescription(virtualViewId));
            node.setBoundsInParent(mTempRect);
            node.addAction(AccessibilityNodeInfo.ACTION_CLICK);

            // Flag non-selectable dates as disabled
            node.setEnabled(!mController.isOutOfRange(mYear, mMonth, virtualViewId));

            if (virtualViewId == mSelectedDay) {
                node.setSelected(true);
            }

        }

        @Override
        protected boolean onPerformActionForVirtualView(int virtualViewId, int action, Bundle arguments) {
            if (action == AccessibilityNodeInfo.ACTION_CLICK) {
                onDayClick(virtualViewId);
                return true;
            }
            return false;
        }

        void getItemBounds(int day, Rect rect) {
            final int offsetX = mEdgePadding;
            final int offsetY = getMonthHeaderSize();
            final int cellHeight = mRowHeight;
            final int cellWidth = ((mWidth - (2 * mEdgePadding)) / mNumDays);
            final int index = ((day - 1) + findDayOffset());
            final int row = (index / mNumDays);
            final int column = (index % mNumDays);
            final int x = (offsetX + (column * cellWidth));
            final int y = (offsetY + (row * cellHeight));

            rect.set(x, y, (x + cellWidth), (y + cellHeight));
        }

        CharSequence getItemDescription(int day) {
            mTempCalendar.set(mYear, mMonth, day);
            return DateFormat.format(DATE_FORMAT, mTempCalendar.getTimeInMillis());
        }
    }

    public interface OnDayClickListener<CAL extends Calendar> {
        void onDayClick(MonthView<CAL> view, CalendarDay<CAL> day);
    }
}
