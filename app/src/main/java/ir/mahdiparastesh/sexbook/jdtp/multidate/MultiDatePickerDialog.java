package ir.mahdiparastesh.sexbook.jdtp.multidate;

import static ir.mahdiparastesh.sexbook.Fun.font1;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.res.Resources;
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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Objects;

import ir.mahdiparastesh.sexbook.R;
import ir.mahdiparastesh.sexbook.jdtp.HapticFeedbackController;
import ir.mahdiparastesh.sexbook.jdtp.Utils;
import ir.mahdiparastesh.sexbook.jdtp.date.AccessibleDateAnimator;
import ir.mahdiparastesh.sexbook.jdtp.utils.LanguageUtils;
import ir.mahdiparastesh.sexbook.jdtp.utils.PersianCalendar;

public class MultiDatePickerDialog extends DialogFragment implements
        OnClickListener, DatePickerController {

    private static final int UNINITIALIZED = -1;
    private static final int MONTH_AND_DAY_VIEW = 0;
    private static final int YEAR_VIEW = 1;

    private static final String KEY_SELECTED_DAYS = "selectedDays";
    private static final String KEY_LIST_POSITION = "list_position";
    private static final String KEY_WEEK_START = "week_start";
    private static final String KEY_YEAR_START = "year_start";
    private static final String KEY_YEAR_END = "year_end";
    private static final String KEY_CURRENT_VIEW = "current_view";
    private static final String KEY_LIST_POSITION_OFFSET = "list_position_offset";
    private static final String KEY_MIN_DATE = "min_date";
    private static final String KEY_MAX_DATE = "max_date";
    private static final String KEY_SELECTED_YEAR = "selected_year";
    private static final String KEY_HIGHLIGHTED_DAYS = "highlighted_days";
    private static final String KEY_SELECTABLE_DAYS = "selectable_days";
    private static final String KEY_THEME_DARK = "theme_dark";

    private static final int DEFAULT_START_YEAR = 1350;
    private static final int DEFAULT_END_YEAR = 1450;

    private static final int ANIMATION_DURATION = 300;
    private static final int ANIMATION_DELAY = 500;

    private final ArrayList<PersianCalendar> mSelectedDaysCalendars = new ArrayList<>();
    private OnDateSetListener mCallBack;
    private final HashSet<OnDateChangedListener> mListeners = new HashSet<>();
    private DialogInterface.OnCancelListener mOnCancelListener;
    private DialogInterface.OnDismissListener mOnDismissListener;

    private AccessibleDateAnimator mAnimator;

    private TextView mDayOfWeekView;
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
    private int mSelectedYear;
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
        void onDateSet(MultiDatePickerDialog view, ArrayList<PersianCalendar> selectedDays);
    }

    public interface OnDateChangedListener {

        void onDateChanged();
    }


    public MultiDatePickerDialog() {

    }

    public static MultiDatePickerDialog newInstance(OnDateSetListener callBack, @Nullable ArrayList<PersianCalendar> selectedDays) {
        MultiDatePickerDialog ret = new MultiDatePickerDialog();
        ret.initialize(callBack, selectedDays);
        return ret;
    }

    public void initialize(OnDateSetListener callBack, @Nullable ArrayList<PersianCalendar> selectedDays) {
        mCallBack = callBack;
        if (selectedDays != null)
            setSelectedDays(selectedDays);
        else
            mSelectedDaysCalendars.add(new PersianCalendar());
        mSelectedYear = mSelectedDaysCalendars.get(mSelectedDaysCalendars.size() - 1).getPersianYear();
        mThemeDark = false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final AppCompatActivity that = (AppCompatActivity) getActivity();
        assert that != null;
        that.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        if (savedInstanceState != null) {
            mSelectedDaysCalendars.clear();
            mSelectedDaysCalendars.addAll((ArrayList<PersianCalendar>)
                    savedInstanceState.getSerializable(KEY_SELECTED_DAYS));
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(KEY_SELECTED_DAYS, mSelectedDaysCalendars);
        outState.putInt(KEY_WEEK_START, mWeekStart);
        outState.putInt(KEY_YEAR_START, mMinYear);
        outState.putInt(KEY_YEAR_END, mMaxYear);
        outState.putInt(KEY_CURRENT_VIEW, mCurrentView);
        int listPosition = -1;
        if (mCurrentView == MONTH_AND_DAY_VIEW)
            listPosition = mDayPickerView.getMostVisiblePosition();
        else if (mCurrentView == YEAR_VIEW) {
            listPosition = mYearPickerView.getFirstVisiblePosition();
            outState.putInt(KEY_LIST_POSITION_OFFSET, mYearPickerView.getFirstPositionOffset());
        }
        outState.putInt(KEY_LIST_POSITION, listPosition);
        outState.putSerializable(KEY_MIN_DATE, mMinDate);
        outState.putSerializable(KEY_MAX_DATE, mMaxDate);
        outState.putSerializable(KEY_SELECTED_YEAR, mSelectedYear);
        outState.putSerializable(KEY_HIGHLIGHTED_DAYS, highlightedDays);
        outState.putSerializable(KEY_SELECTABLE_DAYS, selectableDays);
        outState.putBoolean(KEY_THEME_DARK, mThemeDark);
    }

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(LayoutInflater inf, ViewGroup parent, Bundle savedInstanceState) {
        assert getDialog() != null;
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        View view = inf.inflate(R.layout.jdtp_date_picker_dialog, null);
        final AppCompatActivity that = (AppCompatActivity) getActivity();
        assert that != null;
        mMonthAndDayView = view.findViewById(R.id.date_picker_month_and_day);
        mMonthAndDayView.setOnClickListener(this);
        mSelectedMonthTextView = view.findViewById(R.id.date_picker_month);
        mSelectedMonthTextView.setTypeface(font1);
        mSelectedDayTextView = view.findViewById(R.id.date_picker_day);
        mSelectedDayTextView.setTypeface(font1);
        mYearView = view.findViewById(R.id.date_picker_year);
        mYearView.setTypeface(font1);
        mYearView.setOnClickListener(this);

        int listPosition = -1;
        int listPositionOffset = 0;
        int currentView = MONTH_AND_DAY_VIEW;
        if (savedInstanceState != null) {
            mWeekStart = savedInstanceState.getInt(KEY_WEEK_START);
            mMinYear = savedInstanceState.getInt(KEY_YEAR_START);
            mMaxYear = savedInstanceState.getInt(KEY_YEAR_END);
            mSelectedYear = savedInstanceState.getInt(KEY_SELECTED_YEAR);
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
        mDayPickerDescription = res.getString(R.string.jdtp_day_picker_description);
        mSelectDay = res.getString(R.string.jdtp_select_day);
        mYearPickerDescription = res.getString(R.string.jdtp_year_picker_description);
        mSelectYear = res.getString(R.string.jdtp_select_year);

        int bgColorResource = mThemeDark ? R.color.jdtp_date_picker_view_animator_dark_theme
                : R.color.jdtp_date_picker_view_animator;
        view.setBackgroundColor(ContextCompat.getColor(that, bgColorResource));

        mAnimator = view.findViewById(R.id.animator);
        mAnimator.addView(mDayPickerView);
        mAnimator.addView(mYearPickerView);
        mAnimator.setDateMillis(mSelectedDaysCalendars.get(mSelectedDaysCalendars.size() - 1).getTimeInMillis());
        Animation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(ANIMATION_DURATION);
        mAnimator.setInAnimation(animation);
        Animation animation2 = new AlphaAnimation(1.0f, 0.0f);
        animation2.setDuration(ANIMATION_DURATION);
        mAnimator.setOutAnimation(animation2);

        Button okButton = view.findViewById(R.id.ok);
        okButton.setOnClickListener(v -> {
            tryVibrate();
            if (mCallBack != null)
                mCallBack.onDateSet(MultiDatePickerDialog.this, mSelectedDaysCalendars);
            dismiss();
        });
        okButton.setTypeface(font1);

        Button cancelButton = view.findViewById(R.id.cancel);
        cancelButton.setOnClickListener(v -> {
            tryVibrate();
            Objects.requireNonNull(getDialog()).cancel();
        });
        cancelButton.setTypeface(font1);
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
        if (mOnCancelListener != null) {
            mOnCancelListener.onCancel(dialog);
        }
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
                ObjectAnimator pulseAnimator = Utils.getPulseAnimator(mMonthAndDayView, 0.9f,
                        1.05f);
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

                String dayString = LanguageUtils.getPersianNumbers(mSelectedDaysCalendars.get(mSelectedDaysCalendars.size() - 1).getPersianLongDate());
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

                String yearString = LanguageUtils.
                        getPersianNumbers(String.valueOf(mSelectedDaysCalendars.get(mSelectedDaysCalendars.size() - 1).getPersianYear()));
                mAnimator.setContentDescription(mYearPickerDescription + ": " + yearString);
                Utils.tryAccessibilityAnnounce(mAnimator, mSelectYear);
                break;
        }
    }

    private void updateDisplay(boolean announce) {
        if (mSelectedDaysCalendars.size() == 0) {
            return;
        }
        PersianCalendar target = mSelectedDaysCalendars.get(mSelectedDaysCalendars.size() - 1);
        if (mDayOfWeekView != null) {
            mDayOfWeekView.setText(target.getPersianWeekDayName());
        }

        mSelectedMonthTextView.setText(LanguageUtils.
                getPersianNumbers(target.getPersianMonthName()));
        mSelectedDayTextView.setText(LanguageUtils.
                getPersianNumbers(String.valueOf(target.getPersianDay())));
        mYearView.setText(LanguageUtils.
                getPersianNumbers(String.valueOf(mSelectedYear)));

        long millis = target.getTimeInMillis();
        mAnimator.setDateMillis(millis);
        String monthAndDayText = LanguageUtils.getPersianNumbers(
                target.getPersianMonthName() + " " +
                        target.getPersianDay()
        );
        mMonthAndDayView.setContentDescription(monthAndDayText);

        if (announce) {
            String fullDateText = LanguageUtils.
                    getPersianNumbers(target.getPersianLongDate());
            Utils.tryAccessibilityAnnounce(mAnimator, fullDateText);
        }
    }

    public void setThemeDark(boolean themeDark) {
        mThemeDark = themeDark;
    }

    @Override
    public boolean isThemeDark() {
        return mThemeDark;
    }

    public void setFirstDayOfWeek(int startOfWeek) {
        if (startOfWeek < Calendar.SUNDAY || startOfWeek > Calendar.SATURDAY)
            throw new IllegalArgumentException("Value must be between Calendar.SUNDAY and " +
                    "Calendar.SATURDAY");
        mWeekStart = startOfWeek;
        if (mDayPickerView != null) mDayPickerView.onChange();
    }

    public void setYearRange(int startYear, int endYear) {
        if (endYear < startYear)
            throw new IllegalArgumentException("Year end must be larger than or equal to year start");

        mMinYear = startYear;
        mMaxYear = endYear;
        if (mDayPickerView != null) {
            mDayPickerView.onChange();
        }
    }

    public void setMinDate(PersianCalendar calendar) {
        mMinDate = calendar;
        if (mDayPickerView != null) mDayPickerView.onChange();
    }

    @Override
    public PersianCalendar getMinDate() {
        return mMinDate;
    }

    public void setMaxDate(PersianCalendar calendar) {
        mMaxDate = calendar;
        if (mDayPickerView != null) mDayPickerView.onChange();
    }

    @Override
    public PersianCalendar getMaxDate() {
        return mMaxDate;
    }

    public void setHighlightedDays(PersianCalendar[] highlightedDays) {
        Arrays.sort(highlightedDays);
        this.highlightedDays = highlightedDays;
    }

    @Override
    public PersianCalendar[] getHighlightedDays() {
        return highlightedDays;
    }

    public void setSelectableDays(PersianCalendar[] selectableDays) {
        Arrays.sort(selectableDays);
        this.selectableDays = selectableDays;
    }

    @Override
    public PersianCalendar[] getSelectableDays() {
        return selectableDays;
    }

    public void setOnDateSetListener(OnDateSetListener listener) {
        mCallBack = listener;
    }

    public void setOnCancelListener(DialogInterface.OnCancelListener onCancelListener) {
        mOnCancelListener = onCancelListener;
    }

    public void setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
        mOnDismissListener = onDismissListener;
    }

    //private void adjustDayInMonthIfNeeded(int month, int year) { }

    @Override
    public void onClick(View v) {
        tryVibrate();
        if (v.getId() == R.id.date_picker_year)
            setCurrentView(YEAR_VIEW);
        else if (v.getId() == R.id.date_picker_month_and_day)
            setCurrentView(MONTH_AND_DAY_VIEW);
    }

    @Override
    public void onYearSelected(int year) {
        mSelectedYear = year;
        //adjustDayInMonthIfNeeded(mSelectedDaysCalendars.get(mSelectedDaysCalendars.size() - 1).getPersianMonth(), year);
        if (mSelectedDaysCalendars.size() == 1) {
            mSelectedDaysCalendars.get(0).setPersianDate(year
                    , mSelectedDaysCalendars.get(0).getPersianMonth(),
                    mSelectedDaysCalendars.get(mSelectedDaysCalendars.size() - 1).getPersianDay());
        }
        updatePickers();
        setCurrentView(MONTH_AND_DAY_VIEW);
        updateDisplay(true);
    }

    @Override
    public void onDaysOfMonthSelected(ArrayList<PersianCalendar> selectedDays) {
        mSelectedYear = selectedDays.get(selectedDays.size() - 1).getPersianYear();
        updatePickers();
        updateDisplay(true);
    }

    private void updatePickers() {
        for (OnDateChangedListener listener : mListeners) listener.onDateChanged();
    }


    @Override
    public ArrayList<PersianCalendar> getSelectedDays() {
        return mSelectedDaysCalendars;
    }

    @Override
    public void setSelectedDays(ArrayList<PersianCalendar> selectedDays) {
        mSelectedDaysCalendars.clear();
        mSelectedDaysCalendars.addAll(selectedDays);
    }

    @Override
    public int getMinYear() {
        if (selectableDays != null)
            return selectableDays[0].getPersianYear();
        return mMinDate != null && mMinDate.getPersianYear() > mMinYear ? mMinDate.getPersianYear() : mMinYear;
    }

    @Override
    public int getMaxYear() {
        if (selectableDays != null)
            return selectableDays[selectableDays.length - 1].getPersianYear();
        return mMaxDate != null && mMaxDate.getPersianYear() < mMaxYear ? mMaxDate.getPersianYear() : mMaxYear;
    }

    @Override
    public int getSelectedYear() {
        return mSelectedYear;
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
