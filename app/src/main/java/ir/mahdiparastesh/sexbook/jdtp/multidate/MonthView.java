package ir.mahdiparastesh.sexbook.jdtp.multidate;

import static ir.mahdiparastesh.sexbook.more.BaseActivity.jdtpFont;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.os.Bundle;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import ir.mahdiparastesh.sexbook.R;
import ir.mahdiparastesh.sexbook.jdtp.Utils;
import ir.mahdiparastesh.sexbook.jdtp.multidate.MonthAdapter.CalendarDay;
import ir.mahdiparastesh.sexbook.jdtp.utils.LanguageUtils;
import ir.mahdiparastesh.sexbook.jdtp.utils.PersianCalendar;

public abstract class MonthView extends View {

    public static final String VIEW_PARAMS_HEIGHT = "height";
    public static final String VIEW_PARAMS_MONTH = "month";
    public static final String VIEW_PARAMS_YEAR = "year";
    public static final String VIEW_PARAMS_SELECTED_DAY = "selected_day";
    public static final String VIEW_PARAMS_SELECTED_DAYS = "selected_days";
    public static final String VIEW_PARAMS_WEEK_START = "week_start";

    protected final static int MIN_HEIGHT = 10;
    protected static final int DEFAULT_SELECTED_DAY = -1;
    protected static final int DEFAULT_WEEK_START = Calendar.SATURDAY;
    protected static final int DEFAULT_NUM_DAYS = 7;
    protected static final int DEFAULT_NUM_ROWS = 6;
    protected static final int MAX_NUM_ROWS = 6;

    private static final int SELECTED_CIRCLE_ALPHA = 255;

    protected final static int DAY_SEPARATOR_WIDTH = 1;
    protected static int MINI_DAY_NUMBER_TEXT_SIZE;
    protected static int MONTH_LABEL_TEXT_SIZE;
    protected static int MONTH_DAY_LABEL_TEXT_SIZE;
    protected static int MONTH_HEADER_SIZE;
    protected static int DAY_SELECTED_CIRCLE_SIZE;

    protected DatePickerController mController;

    protected final int mEdgePadding = 0;
    protected Paint mMonthNumPaint;
    protected Paint mMonthTitlePaint;
    protected Paint mSelectedCirclePaint;
    protected Paint mMonthDayLabelPaint;
    private final StringBuilder mStringBuilder;

    protected int mMonth, mYear, mWidth, mRowHeight;
    protected boolean mHasToday = false;
    protected int mSelectedDay = -1;
    protected ArrayList<Integer> mSelectedDays = new ArrayList<>();
    protected int mToday = DEFAULT_SELECTED_DAY;
    protected int mWeekStart = DEFAULT_WEEK_START;
    protected final int mNumDays = DEFAULT_NUM_DAYS;
    protected int mNumCells = mNumDays;

    private final PersianCalendar mPersianCalendar;
    protected final PersianCalendar mDayLabelCalendar;
    private final MonthViewTouchHelper mTouchHelper;

    protected int mNumRows = DEFAULT_NUM_ROWS;

    protected OnDayClickListener mOnDayClickListener;
    private final boolean mLockAccessibilityDelegate;

    protected final int mDayTextColor, mSelectedDayTextColor, mMonthDayTextColor, mTodayNumberColor,
            mHighlightedDayTextColor, mDisabledDayTextColor, mMonthTitleColor;
    private final float rightSpace = 30;

    public MonthView(Context c) {
        this(c, null, null);
    }

    public MonthView(Context c, AttributeSet attr, DatePickerController controller) {
        super(c, attr);
        mController = controller;
        Resources res = c.getResources();

        mDayLabelCalendar = new PersianCalendar();
        mPersianCalendar = new PersianCalendar();


        boolean darkTheme = mController != null && mController.isThemeDark();
        if (darkTheme) {
            mDayTextColor = ContextCompat.getColor(c,
                    R.color.jdtp_date_picker_text_normal_dark_theme);
            mMonthDayTextColor = ContextCompat.getColor(c,
                    R.color.jdtp_date_picker_month_day_dark_theme);
            mDisabledDayTextColor = ContextCompat.getColor(c,
                    R.color.jdtp_date_picker_text_disabled_dark_theme);
            mHighlightedDayTextColor = ContextCompat.getColor(c,
                    R.color.jdtp_date_picker_text_highlighted_dark_theme);
        } else {
            mDayTextColor = ContextCompat.getColor(c,
                    R.color.jdtp_date_picker_text_normal);
            mMonthDayTextColor = ContextCompat.getColor(c,
                    R.color.jdtp_date_picker_month_day);
            mDisabledDayTextColor = ContextCompat.getColor(c,
                    R.color.jdtp_date_picker_text_disabled);
            mHighlightedDayTextColor = ContextCompat.getColor(c,
                    R.color.jdtp_date_picker_text_highlighted);
        }
        mSelectedDayTextColor = ContextCompat.getColor(c, R.color.jdtp_white);
        mTodayNumberColor = ContextCompat.getColor(c, R.color.jdtp_accent_color);
        mMonthTitleColor = ContextCompat.getColor(c, R.color.jdtp_white);

        mStringBuilder = new StringBuilder(50);

        MINI_DAY_NUMBER_TEXT_SIZE = res.getDimensionPixelSize(R.dimen.jdtp_day_number_size);
        MONTH_LABEL_TEXT_SIZE = res.getDimensionPixelSize(R.dimen.jdtp_month_label_size);
        MONTH_DAY_LABEL_TEXT_SIZE = res.getDimensionPixelSize(R.dimen.jdtp_month_day_label_text_size);
        MONTH_HEADER_SIZE = res.getDimensionPixelOffset(R.dimen.jdtp_month_list_item_header_height);
        DAY_SELECTED_CIRCLE_SIZE = res
                .getDimensionPixelSize(R.dimen.jdtp_day_number_select_circle_radius);

        mRowHeight = (res.getDimensionPixelOffset(R.dimen.jdtp_date_picker_view_animator_height)
                - getMonthHeaderSize()) / MAX_NUM_ROWS;

        mTouchHelper = getMonthViewTouchHelper();
        ViewCompat.setAccessibilityDelegate(this, mTouchHelper);
        ViewCompat.setImportantForAccessibility(this, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES);
        mLockAccessibilityDelegate = true;

        initView();
    }

    public void setDatePickerController(DatePickerController controller) {
        mController = controller;
    }

    protected MonthViewTouchHelper getMonthViewTouchHelper() {
        return new MonthViewTouchHelper(this);
    }

    @Override
    public void setAccessibilityDelegate(AccessibilityDelegate delegate) {
        if (!mLockAccessibilityDelegate) super.setAccessibilityDelegate(delegate);
    }

    public void setOnDayClickListener(OnDayClickListener listener) {
        mOnDayClickListener = listener;
    }

    @Override
    public boolean dispatchHoverEvent(@NonNull MotionEvent event) {
        if (mTouchHelper.dispatchHoverEvent(event)) return true;
        return super.dispatchHoverEvent(event);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            final int day = getDayFromLocation(event.getX(), event.getY());
            if (day >= 0) onDayClick(day);
        }
        return true;
    }

    protected void initView() {
        mMonthTitlePaint = new Paint();
        mMonthTitlePaint.setFakeBoldText(true);
        mMonthTitlePaint.setAntiAlias(true);
        mMonthTitlePaint.setTextSize(MONTH_LABEL_TEXT_SIZE);
        mMonthTitlePaint.setTypeface(jdtpFont);
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
        mMonthDayLabelPaint.setTypeface(jdtpFont);
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

    @SuppressWarnings({"ConstantConditions", "unchecked"})
    public void setMonthParams(HashMap<String, Object> params) {
        if (!params.containsKey(VIEW_PARAMS_MONTH) && !params.containsKey(VIEW_PARAMS_YEAR))
            throw new InvalidParameterException("You must specify month and year for this view");
        setTag(params);
        if (params.containsKey(VIEW_PARAMS_HEIGHT)) {
            mRowHeight = (int) params.get(VIEW_PARAMS_HEIGHT);
            if (mRowHeight < MIN_HEIGHT) mRowHeight = MIN_HEIGHT;
        }
        if (params.containsKey(VIEW_PARAMS_SELECTED_DAY))
            mSelectedDay = (int) params.get(VIEW_PARAMS_SELECTED_DAY);
        if (params.containsKey(VIEW_PARAMS_SELECTED_DAYS)) {
            mSelectedDays = (ArrayList<Integer>) params.get(VIEW_PARAMS_SELECTED_DAYS);
        }
        mMonth = (int) params.get(VIEW_PARAMS_MONTH);
        mYear = (int) params.get(VIEW_PARAMS_YEAR);

        final PersianCalendar today = new PersianCalendar();
        mHasToday = false;
        mToday = -1;

        mPersianCalendar.setPersianDate(mYear, mMonth, 1);
        mDayOfWeekStart = mPersianCalendar.get(Calendar.DAY_OF_WEEK);

        if (params.containsKey(VIEW_PARAMS_WEEK_START))
            mWeekStart = (int) params.get(VIEW_PARAMS_WEEK_START);
        else mWeekStart = Calendar.SATURDAY;

        mNumCells = Utils.getDaysInMonth(mMonth, mYear);
        for (int i = 0; i < mNumCells; i++) {
            final int day = i + 1;
            if (sameDay(day, today)) {
                mHasToday = true;
                mToday = day;
            }
        }
        mNumRows = calculateNumRows();
        mTouchHelper.invalidateRoot();
    }

    public void setSelectedDay(int day) {
        mSelectedDay = day;
    }

    public void reuse() {
        mNumRows = DEFAULT_NUM_ROWS;
        requestLayout();
    }

    private int calculateNumRows() {
        int offset = findDayOffset();
        int dividend = (offset + mNumCells) / mNumDays;
        int remainder = (offset + mNumCells) % mNumDays;
        return (dividend + (remainder > 0 ? 1 : 0));
    }

    private boolean sameDay(int day, PersianCalendar today) {
        return mYear == today.getPersianYear() &&
                mMonth == today.getPersianMonth() &&
                day == today.getPersianDay();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), mRowHeight * mNumRows
                + getMonthHeaderSize() + 5);
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


    protected int getMonthHeaderSize() {
        return MONTH_HEADER_SIZE;
    }

    private String getMonthAndYearString() {
        mStringBuilder.setLength(0);
        return LanguageUtils.getPersianNumbers(
                mPersianCalendar.getPersianMonthName() + " " + mPersianCalendar.getPersianYear());
    }

    protected void drawMonthTitle(Canvas canvas) {
        int x = mWidth / 2;
        int y = (getMonthHeaderSize() - MONTH_DAY_LABEL_TEXT_SIZE) / 2;
        canvas.drawText(getMonthAndYearString(), x, y, mMonthTitlePaint);
    }

    protected void drawMonthDayLabels(Canvas canvas) {
        int y = getMonthHeaderSize() - (MONTH_DAY_LABEL_TEXT_SIZE / 2);
        int dayWidthHalf = mWidth / (mNumDays * 2);
        float firstX = (2 * (mNumDays - 1) + 1) * dayWidthHalf + rightSpace;
        for (int i = 0; i < mNumDays; i++) {
            int calendarDay = (i + mWeekStart) % mNumDays;
            int x = (int) (firstX - (2 * i + 1) * dayWidthHalf + mEdgePadding);
            mDayLabelCalendar.set(Calendar.DAY_OF_WEEK, calendarDay);
            String localWeekDisplayName = mDayLabelCalendar.getPersianWeekDayName();
            String weekString = localWeekDisplayName.substring(0, 1);
            canvas.drawText(weekString, x, y, mMonthDayLabelPaint);
        }
    }

    protected void drawMonthNums(Canvas canvas) {
        int y = (((mRowHeight + MINI_DAY_NUMBER_TEXT_SIZE) / 2) - DAY_SEPARATOR_WIDTH)
                + getMonthHeaderSize();
        final float dayWidthHalf = mWidth / (mNumDays * 2.0f);
        int j = findDayOffset();
        float firstX = (2 * (mNumDays - 1) + 1) * dayWidthHalf + rightSpace;
        for (int dayNumber = 1; dayNumber <= mNumCells; dayNumber++) {
            final int x = (int) (firstX - (((2 * j + 1) * dayWidthHalf + mEdgePadding)));

            int yRelativeToDay = (mRowHeight + MINI_DAY_NUMBER_TEXT_SIZE) / 2 - DAY_SEPARATOR_WIDTH;

            final int startX = (int) (x - dayWidthHalf);
            final int stopX = (int) (x + dayWidthHalf);
            final int startY = y - yRelativeToDay;
            final int stopY = startY + mRowHeight;

            drawMonthDay(canvas, mYear, mMonth, dayNumber, x, y, startX, stopX, startY, stopY);

            j++;
            if (j == mNumDays) {
                j = 0;
                y += mRowHeight;
            }
        }
    }

    public abstract void drawMonthDay(Canvas canvas, int year, int month, int day,
                                      int x, int y, int startX, int stopX, int startY, int stopY);

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
        if (x < dayStart || x > mWidth - mEdgePadding) return -1;
        int row = (int) (y - getMonthHeaderSize()) / mRowHeight;
        int column = (int) (mNumDays - ((x - dayStart) * mNumDays)
                / (mWidth - dayStart - mEdgePadding));
        int day = column - findDayOffset() + 1;
        day += row * mNumDays;
        return day;
    }

    private void onDayClick(int day) {
        if (isOutOfRange(mYear, mMonth, day)) return;
        if (mOnDayClickListener != null)
            mOnDayClickListener.onDayClick(this, new CalendarDay(mYear, mMonth, day));
        mTouchHelper.sendEventForVirtualView(day, AccessibilityEvent.TYPE_VIEW_CLICKED);
    }

    protected boolean isOutOfRange(int year, int month, int day) {
        if (mController.getSelectableDays() != null)
            return !isSelectable(year, month, day);

        if (isBeforeMin(year, month, day)) return true;
        else return isAfterMax(year, month, day);
    }

    private boolean isSelectable(int year, int month, int day) {
        PersianCalendar[] selectableDays = mController.getSelectableDays();
        for (PersianCalendar c : selectableDays) {
            if (year < c.getPersianYear())
                break;
            if (year > c.getPersianYear())
                continue;
            if (month < c.getPersianMonth())
                break;
            if (month > c.getPersianMonth())
                continue;
            if (day < c.getPersianDay())
                break;
            if (day > c.getPersianDay())
                continue;
            return true;
        }
        return false;
    }

    private boolean isBeforeMin(int year, int month, int day) {
        if (mController == null) return false;
        PersianCalendar minDate = mController.getMinDate();
        if (minDate == null) return false;

        if (year < minDate.getPersianYear())
            return true;
        else if (year > minDate.getPersianYear())
            return false;

        if (month < minDate.getPersianMonth())
            return true;
        else if (month > minDate.getPersianMonth())
            return false;

        return day < minDate.getPersianDay();
    }

    private boolean isAfterMax(int year, int month, int day) {
        if (mController == null) return false;
        PersianCalendar maxDate = mController.getMaxDate();
        if (maxDate == null) return false;

        if (year > maxDate.getPersianYear())
            return true;
        else if (year < maxDate.getPersianYear())
            return false;

        if (month > maxDate.getPersianMonth())
            return true;
        else if (month < maxDate.getPersianMonth())
            return false;
        return day > maxDate.getPersianMonth();
    }

    protected boolean isHighlighted(int year, int month, int day) {
        PersianCalendar[] highlightedDays = mController.getHighlightedDays();
        if (highlightedDays == null) {
            return false;
        }
        for (PersianCalendar c : highlightedDays) {
            if (year < c.getPersianYear())
                break;
            if (year > c.getPersianYear())
                continue;
            if (month < c.getPersianMonth())
                break;
            if (month > c.getPersianMonth())
                continue;
            if (day < c.getPersianDay())
                break;
            if (day > c.getPersianDay())
                continue;
            return true;
        }
        return false;
    }

    public CalendarDay getAccessibilityFocus() {
        final int day = mTouchHelper.getAccessibilityFocusedVirtualViewId();
        if (day >= 0) return new CalendarDay(mYear, mMonth, day);
        return null;
    }

    public void clearAccessibilityFocus() {
        mTouchHelper.clearFocusedVirtualView();
    }

    public boolean restoreAccessibilityFocus(CalendarDay day) {
        if ((day.year != mYear) || (day.month != mMonth) || (day.day > mNumCells)) {
            return false;
        }
        mTouchHelper.setFocusedVirtualView(day.day);
        return true;
    }

    protected class MonthViewTouchHelper extends ExploreByTouchHelper {

        private final Rect mTempRect = new Rect();
        private final PersianCalendar mTempCalendar = new PersianCalendar();

        public MonthViewTouchHelper(View host) {
            super(host);
        }

        public void setFocusedVirtualView(int virtualViewId) {
            getAccessibilityNodeProvider(MonthView.this).performAction(
                    virtualViewId, AccessibilityNodeInfoCompat.ACTION_ACCESSIBILITY_FOCUS,
                    null);
        }

        public void clearFocusedVirtualView() {
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
        protected void onPopulateEventForVirtualView(int virtualViewId, AccessibilityEvent event) {
            event.setContentDescription(getItemDescription(virtualViewId));
        }

        @Override
        protected void onPopulateNodeForVirtualView(int virtualViewId,
                                                    AccessibilityNodeInfoCompat node) {
            getItemBounds(virtualViewId, mTempRect);

            node.setContentDescription(getItemDescription(virtualViewId));
            node.setBoundsInParent(mTempRect);
            node.addAction(AccessibilityNodeInfo.ACTION_CLICK);

            if (virtualViewId == mSelectedDay) node.setSelected(true);
        }

        @Override
        protected boolean onPerformActionForVirtualView(int virtualViewId, int action, Bundle args) {
            if (action == AccessibilityNodeInfo.ACTION_CLICK) {
                onDayClick(virtualViewId);
                return true;
            }
            return false;
        }

        protected void getItemBounds(int day, Rect rect) {
            // offsetX => mEdgePadding
            final int offsetY = getMonthHeaderSize();
            final int cellHeight = mRowHeight;
            final int cellWidth = mWidth / mNumDays;
            final int index = ((day - 1) + findDayOffset());
            final int row = (index / mNumDays);
            final int column = (index % mNumDays);
            final int x = (mEdgePadding + (column * cellWidth));
            final int y = (offsetY + (row * cellHeight));

            rect.set(x, y, (x + cellWidth), (y + cellHeight));
        }

        protected CharSequence getItemDescription(int day) {
            mTempCalendar.setPersianDate(mYear, mMonth, day);
            final String date = LanguageUtils.getPersianNumbers(mTempCalendar.getPersianLongDate());

            if (day == mSelectedDay)
                return getContext().getString(R.string.jdtp_item_is_selected, date);
            return date;
        }
    }

    interface OnDayClickListener {
        void onDayClick(MonthView view, CalendarDay day);
    }
}
