package ir.mahdiparastesh.sexbook.mdtp.jdate;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Objects;

import ir.mahdiparastesh.sexbook.R;
import ir.mahdiparastesh.sexbook.mdtp.HapticFeedbackController;
import ir.mahdiparastesh.sexbook.mdtp.PersianCalendar;
import ir.mahdiparastesh.sexbook.mdtp.Utils;

public class DatePickerDialog extends DialogFragment implements
        OnClickListener, DatePickerController {

    private static final int UNINITIALIZED = -1;
    private static final int MONTH_AND_DAY_VIEW = 0;
    private static final int YEAR_VIEW = 1;

    private static final String KEY_SELECTED_YEAR = "year";
    private static final String KEY_SELECTED_MONTH = "month";
    private static final String KEY_SELECTED_DAY = "day";
    private static final String KEY_LIST_POSITION = "list_position";
    private static final String KEY_WEEK_START = "week_start";
    private static final String KEY_YEAR_START = "year_start";
    private static final String KEY_YEAR_END = "year_end";
    private static final String KEY_CURRENT_VIEW = "current_view";
    private static final String KEY_LIST_POSITION_OFFSET = "list_position_offset";
    private static final String KEY_MIN_DATE = "min_date";
    private static final String KEY_MAX_DATE = "max_date";
    private static final String KEY_HIGHLIGHTED_DAYS = "highlighted_days";
    private static final String KEY_SELECTABLE_DAYS = "selectable_days";
    private static final String KEY_THEME_DARK = "theme_dark";

    private static final int DEFAULT_START_YEAR = 1350;
    private static final int DEFAULT_END_YEAR = 1450;

    private static final int ANIMATION_DURATION = 300;
    private static final int ANIMATION_DELAY = 500;

    private final PersianCalendar mPersianCalendar = new PersianCalendar();
    private OnDateSetListener mCallBack;
    private final HashSet<OnDateChangedListener> mListeners = new HashSet<>();
    private DialogInterface.OnCancelListener mOnCancelListener;
    private DialogInterface.OnDismissListener mOnDismissListener;

    private AccessibleDateAnimator mAnimator;

    private LinearLayout mMonthAndDayView;
    private TextView mSelectedMonthTextView;
    private TextView mSelectedDayTextView;
    private TextView mYearView;
    private DayPickerView mDayPickerView;
    private YearPickerView mYearPickerView;

    private int mCurrentView = UNINITIALIZED;

    private int mWeekStart = PersianCalendar.SATURDAY;
    private int mMinYear = DEFAULT_START_YEAR;
    private int mMaxYear = DEFAULT_END_YEAR;
    private PersianCalendar mMinDate;
    private PersianCalendar mMaxDate;
    private PersianCalendar[] highlightedDays;
    private PersianCalendar[] selectableDays;
    private boolean mThemeDark;

    private HapticFeedbackController mHapticFeedbackController;

    private boolean mDelayAnimation = true;

    private String mDayPickerDescription;
    private String mSelectDay;
    private String mYearPickerDescription;
    private String mSelectYear;

    public interface OnDateSetListener {
        void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth);
    }

    public interface OnDateChangedListener {

        void onDateChanged();
    }

    public static DatePickerDialog newInstance(OnDateSetListener callBack, int year,
                                               int monthOfYear,
                                               int dayOfMonth) {
        DatePickerDialog ret = new DatePickerDialog();
        ret.initialize(callBack, year, monthOfYear, dayOfMonth);
        return ret;
    }

    public void initialize(OnDateSetListener callBack, int year, int monthOfYear, int dayOfMonth) {
        mCallBack = callBack;
        mPersianCalendar.setPersianDate(year, monthOfYear, dayOfMonth);
        mThemeDark = false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final AppCompatActivity that = (AppCompatActivity) getActivity();
        assert that != null;
        that.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        if (savedInstanceState != null) {
            mPersianCalendar.setPersianDate(
                    savedInstanceState.getInt(KEY_SELECTED_YEAR),
                    savedInstanceState.getInt(KEY_SELECTED_MONTH),
                    savedInstanceState.getInt(KEY_SELECTED_DAY)
            );
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_SELECTED_YEAR, mPersianCalendar.getPersianYear());
        outState.putInt(KEY_SELECTED_MONTH, mPersianCalendar.getPersianMonth());
        outState.putInt(KEY_SELECTED_DAY, mPersianCalendar.getPersianDay());
        outState.putInt(KEY_WEEK_START, mWeekStart);
        outState.putInt(KEY_YEAR_START, mMinYear);
        outState.putInt(KEY_YEAR_END, mMaxYear);
        outState.putInt(KEY_CURRENT_VIEW, mCurrentView);
        int listPosition = -1;
        if (mCurrentView == MONTH_AND_DAY_VIEW) {
            listPosition = mDayPickerView.getMostVisiblePosition();
        } else if (mCurrentView == YEAR_VIEW) {
            listPosition = mYearPickerView.getFirstVisiblePosition();
            outState.putInt(KEY_LIST_POSITION_OFFSET, mYearPickerView.getFirstPositionOffset());
        }
        outState.putInt(KEY_LIST_POSITION, listPosition);
        outState.putSerializable(KEY_MIN_DATE, mMinDate);
        outState.putSerializable(KEY_MAX_DATE, mMaxDate);
        outState.putSerializable(KEY_HIGHLIGHTED_DAYS, highlightedDays);
        outState.putSerializable(KEY_SELECTABLE_DAYS, selectableDays);
        outState.putBoolean(KEY_THEME_DARK, mThemeDark);
    }

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(LayoutInflater inf, ViewGroup parent, Bundle savedInstanceState) {
        Objects.requireNonNull(getDialog()).getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        View view = inf.inflate(R.layout.mdtp_date_picker_dialog, null);
        final AppCompatActivity that = (AppCompatActivity) getActivity();
        assert that != null;
        mMonthAndDayView = view.findViewById(R.id.mdtp_date_picker_month_and_day);
        mMonthAndDayView.setOnClickListener(this);
        mSelectedMonthTextView = view.findViewById(R.id.mdtp_date_picker_month);
        mSelectedDayTextView = view.findViewById(R.id.mdtp_date_picker_day);
        mYearView = view.findViewById(R.id.mdtp_date_picker_year);

        Typeface font1 = Utils.mdtpFont(that, false);
        Button okButton = view.findViewById(R.id.mdtp_ok);
        Button cancelButton = view.findViewById(R.id.mdtp_cancel);
        okButton.setTypeface(font1);
        okButton.setTextColor(that.getResources().getColor(R.color.jdtp_button_color));
        cancelButton.setTypeface(font1);
        cancelButton.setTextColor(that.getResources().getColor(R.color.jdtp_button_color));
        mSelectedMonthTextView.setTypeface(font1);
        mSelectedDayTextView.setTypeface(font1);
        mYearView.setTypeface(font1);
        mYearView.setOnClickListener(this);

        int listPosition = -1;
        int listPositionOffset = 0;
        int currentView = MONTH_AND_DAY_VIEW;
        if (savedInstanceState != null) {
            mWeekStart = savedInstanceState.getInt(KEY_WEEK_START);
            mMinYear = savedInstanceState.getInt(KEY_YEAR_START);
            mMaxYear = savedInstanceState.getInt(KEY_YEAR_END);
            currentView = savedInstanceState.getInt(KEY_CURRENT_VIEW);
            listPosition = savedInstanceState.getInt(KEY_LIST_POSITION);
            listPositionOffset = savedInstanceState.getInt(KEY_LIST_POSITION_OFFSET);
            mMinDate = (PersianCalendar) savedInstanceState.getSerializable(KEY_MIN_DATE);
            mMaxDate = (PersianCalendar) savedInstanceState.getSerializable(KEY_MAX_DATE);
            highlightedDays = (PersianCalendar[]) savedInstanceState.getSerializable(KEY_HIGHLIGHTED_DAYS);
            selectableDays = (PersianCalendar[]) savedInstanceState.getSerializable(KEY_SELECTABLE_DAYS);
            mThemeDark = savedInstanceState.getBoolean(KEY_THEME_DARK);
        }
        mDayPickerView = new SimpleDayPickerView(that, this);
        mYearPickerView = new YearPickerView(that, this);

        Resources res = getResources();
        mDayPickerDescription = res.getString(R.string.mdtp_day_picker_description);
        mSelectDay = res.getString(R.string.mdtp_select_day);
        mYearPickerDescription = res.getString(R.string.mdtp_year_picker_description);
        mSelectYear = res.getString(R.string.mdtp_select_year);

        int bgColorResource = mThemeDark ? R.color.mdtp_date_picker_view_animator_dark_theme
                : R.color.mdtp_date_picker_view_animator;
        view.setBackgroundColor(ContextCompat.getColor(that, bgColorResource));

        mAnimator = view.findViewById(R.id.mdtp_animator);
        mAnimator.addView(mDayPickerView);
        mAnimator.addView(mYearPickerView);
        mAnimator.setDateMillis(mPersianCalendar.getTimeInMillis());
        Animation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(ANIMATION_DURATION);
        mAnimator.setInAnimation(animation);
        Animation animation2 = new AlphaAnimation(1.0f, 0.0f);
        animation2.setDuration(ANIMATION_DURATION);
        mAnimator.setOutAnimation(animation2);

        okButton.setOnClickListener(v -> {
            tryVibrate();
            if (mCallBack != null) {
                mCallBack.onDateSet(DatePickerDialog.this, mPersianCalendar.getPersianYear(),
                        mPersianCalendar.getPersianMonth(), mPersianCalendar.getPersianDay());
            }
            dismiss();
        });

        cancelButton.setOnClickListener(v -> {
            tryVibrate();
            getDialog().cancel();
        });
        cancelButton.setVisibility(isCancelable() ? View.VISIBLE : View.GONE);

        updateDisplay(false);
        setCurrentView(currentView);

        if (listPosition != -1) {
            if (currentView == MONTH_AND_DAY_VIEW)
                mDayPickerView.postSetSelection(listPosition);
            else if (currentView == YEAR_VIEW)
                mYearPickerView.postSetSelectionFromTop(listPosition, listPositionOffset);
        }

        mHapticFeedbackController = new HapticFeedbackController(that);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mHapticFeedbackController.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        mHapticFeedbackController.stop();
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);
        if (mOnCancelListener != null) mOnCancelListener.onCancel(dialog);
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (mOnDismissListener != null) {
            mOnDismissListener.onDismiss(dialog);
        }
    }

    private void setCurrentView(final int viewIndex) {
        switch (viewIndex) {
            case MONTH_AND_DAY_VIEW:
                ObjectAnimator pulseAnimator = Utils.getPulseAnimator(mMonthAndDayView,
                        0.9f, 1.05f);
                if (mDelayAnimation) {
                    pulseAnimator.setStartDelay(ANIMATION_DELAY);
                    mDelayAnimation = false;
                }
                mDayPickerView.onDateChanged();
                if (mCurrentView != viewIndex) {
                    mMonthAndDayView.setSelected(true);
                    mYearView.setSelected(false);
                    mAnimator.setDisplayedChild(MONTH_AND_DAY_VIEW);
                    mCurrentView = viewIndex;
                }
                pulseAnimator.start();

                String dayString = Utils.getPersianNumbers(mPersianCalendar.getPersianLongDate(getContext()));
                mAnimator.setContentDescription(mDayPickerDescription + ": " + dayString);
                Utils.tryAccessibilityAnnounce(mAnimator, mSelectDay);
                break;
            case YEAR_VIEW:
                pulseAnimator = Utils.getPulseAnimator(mYearView, 0.85f, 1.1f);
                if (mDelayAnimation) {
                    pulseAnimator.setStartDelay(ANIMATION_DELAY);
                    mDelayAnimation = false;
                }
                mYearPickerView.onDateChanged();
                if (mCurrentView != viewIndex) {
                    mMonthAndDayView.setSelected(false);
                    mYearView.setSelected(true);
                    mAnimator.setDisplayedChild(YEAR_VIEW);
                    mCurrentView = viewIndex;
                }
                pulseAnimator.start();

                String yearString = Utils.getPersianNumbers(
                        String.valueOf(mPersianCalendar.getPersianYear()));
                mAnimator.setContentDescription(mYearPickerDescription + ": " + yearString);
                Utils.tryAccessibilityAnnounce(mAnimator, mSelectYear);
                break;
        }
    }

    private void updateDisplay(boolean announce) {
        mSelectedMonthTextView.setText(Utils.getPersianNumbers(
                mPersianCalendar.getPersianMonthName(getContext())));
        mSelectedDayTextView.setText(Utils.getPersianNumbers(
                String.valueOf(mPersianCalendar.getPersianDay())));
        mYearView.setText(Utils.getPersianNumbers(
                String.valueOf(mPersianCalendar.getPersianYear())));

        mAnimator.setDateMillis(mPersianCalendar.getTimeInMillis());
        String monthAndDayText = Utils.getPersianNumbers(
                mPersianCalendar.getPersianMonthName(getContext()) + " " +
                        mPersianCalendar.getPersianDay()
        );
        mMonthAndDayView.setContentDescription(monthAndDayText);

        if (announce) Utils.tryAccessibilityAnnounce(mAnimator, Utils.
                getPersianNumbers(mPersianCalendar.getPersianLongDate(getContext())));
    }

    public void setThemeDark(boolean themeDark) {
        mThemeDark = themeDark;
    }

    @Override
    public boolean isThemeDark() {
        return mThemeDark;
    }

    @SuppressWarnings("unused")
    public void setFirstDayOfWeek(int startOfWeek) {
        if (startOfWeek < Calendar.SUNDAY || startOfWeek > Calendar.SATURDAY) {
            throw new IllegalArgumentException("Value must be between Calendar.SUNDAY and " +
                    "Calendar.SATURDAY");
        }
        mWeekStart = startOfWeek;
        if (mDayPickerView != null)
            mDayPickerView.onChange();
    }

    @SuppressWarnings("unused")
    public void setYearRange(int startYear, int endYear) {
        if (endYear < startYear) {
            throw new IllegalArgumentException("Year end must be larger than or equal to year start");
        }

        mMinYear = startYear;
        mMaxYear = endYear;
        if (mDayPickerView != null) mDayPickerView.onChange();
    }

    @SuppressWarnings("unused")
    public void setMinDate(PersianCalendar calendar) {
        mMinDate = calendar;
        if (mDayPickerView != null) mDayPickerView.onChange();
    }

    @Override
    public PersianCalendar getMinDate() {
        return mMinDate;
    }

    @SuppressWarnings("unused")
    public void setMaxDate(PersianCalendar calendar) {
        mMaxDate = calendar;

        if (mDayPickerView != null) mDayPickerView.onChange();
    }

    @Override
    public PersianCalendar getMaxDate() {
        return mMaxDate;
    }

    @SuppressWarnings("unused")
    public void setHighlightedDays(PersianCalendar[] highlightedDays) {
        Arrays.sort(highlightedDays);
        this.highlightedDays = highlightedDays;
    }

    @Override
    public PersianCalendar[] getHighlightedDays() {
        return highlightedDays;
    }

    @SuppressWarnings("unused")
    public void setSelectableDays(PersianCalendar[] selectableDays) {
        Arrays.sort(selectableDays);
        this.selectableDays = selectableDays;
    }

    @Override
    public PersianCalendar[] getSelectableDays() {
        return selectableDays;
    }

    @SuppressWarnings("unused")
    public void setOnDateSetListener(OnDateSetListener listener) {
        mCallBack = listener;
    }

    @SuppressWarnings("unused")
    public void setOnCancelListener(DialogInterface.OnCancelListener onCancelListener) {
        mOnCancelListener = onCancelListener;
    }

    public void setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
        mOnDismissListener = onDismissListener;
    }

    @SuppressWarnings("unused")
    private void adjustDayInMonthIfNeeded(int month, int year) {
    }

    @Override
    public void onClick(View v) {
        tryVibrate();
        if (v.getId() == R.id.mdtp_date_picker_year)
            setCurrentView(YEAR_VIEW);
        else if (v.getId() == R.id.mdtp_date_picker_month_and_day)
            setCurrentView(MONTH_AND_DAY_VIEW);
    }

    @Override
    public void onYearSelected(int year) {
        adjustDayInMonthIfNeeded(mPersianCalendar.getPersianMonth(), year);
        mPersianCalendar.setPersianDate(year, mPersianCalendar.getPersianMonth(),
                mPersianCalendar.getPersianDay());
        updatePickers();
        setCurrentView(MONTH_AND_DAY_VIEW);
        updateDisplay(true);
    }

    @Override
    public void onDayOfMonthSelected(int year, int month, int day) {
        mPersianCalendar.setPersianDate(year, month, day);
        updatePickers();
        updateDisplay(true);
    }

    private void updatePickers() {
        for (OnDateChangedListener listener : mListeners)
            listener.onDateChanged();
    }


    @Override
    public MonthAdapter.CalendarDay getSelectedDay() {
        return new MonthAdapter.CalendarDay(mPersianCalendar);
    }

    @Override
    public int getMinYear() {
        if (selectableDays != null) {
            return selectableDays[0].getPersianYear();
        }
        return mMinDate != null && mMinDate.getPersianYear() > mMinYear ? mMinDate.getPersianYear() : mMinYear;
    }

    @Override
    public int getMaxYear() {
        if (selectableDays != null) {
            return selectableDays[selectableDays.length - 1].getPersianYear();
        }
        return mMaxDate != null && mMaxDate.getPersianYear() < mMaxYear ? mMaxDate.getPersianYear() : mMaxYear;
    }

    @Override
    public int getFirstDayOfWeek() {
        return mWeekStart;
    }

    @Override
    public void registerOnDateChangedListener(OnDateChangedListener listener) {
        mListeners.add(listener);
    }

    @Override
    public void unregisterOnDateChangedListener(OnDateChangedListener listener) {
        mListeners.remove(listener);
    }

    @Override
    public void tryVibrate() {
        mHapticFeedbackController.tryVibrate();
    }
}
