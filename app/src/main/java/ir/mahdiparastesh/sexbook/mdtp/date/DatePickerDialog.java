package ir.mahdiparastesh.sexbook.mdtp.date;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.icu.text.DateFormatSymbols;
import android.icu.util.Calendar;
import android.icu.util.GregorianCalendar;
import android.icu.util.TimeZone;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.core.content.ContextCompat;

import java.time.temporal.WeekFields;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;

import ir.mahdiparastesh.sexbook.R;
import ir.mahdiparastesh.sexbook.mdtp.HapticFeedbackController;
import ir.mahdiparastesh.sexbook.mdtp.Utils;

@SuppressWarnings("unchecked")
public class DatePickerDialog<CAL extends Calendar> extends AppCompatDialogFragment implements
        OnClickListener, DatePickerController<CAL> {

    public enum Version {VERSION_1, VERSION_2}

    public enum ScrollOrientation {HORIZONTAL, VERTICAL}

    private static final int UNINITIALIZED = -1;
    private static final int MONTH_AND_DAY_VIEW = 0;
    private static final int YEAR_VIEW = 1;

    private static final String KEY_SELECTED_YEAR = "year";
    private static final String KEY_SELECTED_MONTH = "month";
    private static final String KEY_SELECTED_DAY = "day";
    private static final String KEY_LIST_POSITION = "list_position";
    private static final String KEY_WEEK_START = "week_start";
    private static final String KEY_CURRENT_VIEW = "current_view";
    private static final String KEY_LIST_POSITION_OFFSET = "list_position_offset";
    private static final String KEY_HIGHLIGHTED_DAYS = "highlighted_days";
    private static final String KEY_ACCENT = "accent";
    private static final String KEY_VIBRATE = "vibrate";
    private static final String KEY_DISMISS = "dismiss";
    private static final String KEY_AUTO_DISMISS = "auto_dismiss";
    private static final String KEY_DEFAULT_VIEW = "default_view";
    private static final String KEY_TITLE = "title";
    private static final String KEY_OK_COLOR = "ok_color";
    private static final String KEY_CANCEL_COLOR = "cancel_color";
    private static final String KEY_VERSION = "version";
    private static final String KEY_TIMEZONE = "timezone";
    private static final String KEY_DATERANGELIMITER = "daterangelimiter";
    private static final String KEY_SCROLL_ORIENTATION = "scrollorientation";
    private static final String KEY_LOCALE = "locale";
    private static final String KEY_CALENDAR_TYPE = "calendar_type";

    private static final int ANIMATION_DURATION = 300;
    private static final int ANIMATION_DELAY = 500;

    private static LocalDateFormat YEAR_FORMAT;
    private static LocalDateFormat MONTH_FORMAT;
    private static LocalDateFormat DAY_FORMAT;
    private static LocalDateFormat VERSION_2_FORMAT;

    private Class<? extends Calendar> mCalendarType;
    private CAL mCalendar;
    private TimeZone mTimezone;
    private Locale mLocale = Locale.getDefault();
    private OnDateSetListener mCallBack;
    private final HashSet<OnDateChangedListener> mListeners = new HashSet<>();
    private DialogInterface.OnCancelListener mOnCancelListener;
    private DialogInterface.OnDismissListener mOnDismissListener;

    private AccessibleDateAnimator mAnimator;

    private TextView mDatePickerHeaderView;
    private LinearLayout mMonthAndDayView;
    private TextView mSelectedMonthTextView;
    private TextView mSelectedDayTextView;
    private TextView mYearView;
    private DayPickerGroup<CAL> mDayPickerView;
    private YearPickerView<CAL> mYearPickerView;

    private int mCurrentView = UNINITIALIZED;

    private int mWeekStart;
    private String mTitle;
    private HashSet<Calendar> highlightedDays = new HashSet<>();
    private Integer mAccentColor = null;
    private boolean mVibrate = true;
    private boolean mDismissOnPause = false;
    private boolean mAutoDismiss = false;
    private int mDefaultView = MONTH_AND_DAY_VIEW;
    private Integer mOkColor = null;
    private Integer mCancelColor = null;
    private Version mVersion = Version.VERSION_2;
    private ScrollOrientation mScrollOrientation;
    private DefaultDateRangeLimiter<CAL> mDefaultLimiter;
    private DateRangeLimiter<CAL> mDateRangeLimiter;

    private HapticFeedbackController mHapticFeedbackController;

    private boolean mDelayAnimation = true;

    // Accessibility strings.
    private String mDayPickerDescription;
    private String mSelectDay;
    private String mYearPickerDescription;
    private String mSelectYear;

    public interface OnDateSetListener {
        //void onDateSet(DatePickerDialog<?> view, int year, int monthOfYear, int dayOfMonth);
        void onDateSet(DatePickerDialog<?> view, long time);
    }

    protected interface OnDateChangedListener {
        void onDateChanged();
    }


    public DatePickerDialog(Class<CAL> calendarType) {
        // Empty constructor required for dialog fragment.
        mCalendarType = calendarType;
        mCalendar = (CAL) Utils.createCalendar(mCalendarType);
    }

    @SuppressWarnings("unused")
    public static <CAL extends Calendar> DatePickerDialog<CAL> newInstance(
            OnDateSetListener callBack, int year, int monthOfYear, int dayOfMonth,
            Class<CAL> calendarType) {
        DatePickerDialog<CAL> ret = new DatePickerDialog<>(calendarType);
        ret.initialize(callBack, year, monthOfYear, dayOfMonth);
        return ret;
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    public static <CAL extends Calendar> DatePickerDialog<CAL> newInstance(OnDateSetListener callback) {
        return (DatePickerDialog<CAL>) DatePickerDialog.newInstance(callback, new GregorianCalendar());
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    public static <CAL extends Calendar> DatePickerDialog<CAL> newInstance(
            OnDateSetListener callback, CAL initialSelection) {
        DatePickerDialog<CAL> ret = new DatePickerDialog<>((Class<CAL>) initialSelection.getClass());
        ret.initialize(callback, initialSelection);
        return ret;
    }

    public void initialize(OnDateSetListener callBack, int year, int monthOfYear, int dayOfMonth) {
        CAL cal = (CAL) Utils.createCalendar(mCalendarType, getTimeZone());
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, monthOfYear);
        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        this.initialize(callBack, cal);
    }

    public void initialize(OnDateSetListener callBack, CAL initialSelection) {
        mCallBack = callBack;
        mCalendar = Utils.trimToMidnight((CAL) initialSelection.clone());
        mWeekStart = mCalendar.getFirstDayOfWeek();
        mScrollOrientation = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Activity activity = requireActivity();
        activity.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setStyle(AppCompatDialogFragment.STYLE_NO_TITLE, 0);
        mCurrentView = UNINITIALIZED;

        YEAR_FORMAT = new LocalDateFormat(activity, mCalendarType, "yyyy", Locale.getDefault());
        MONTH_FORMAT = new LocalDateFormat(activity, mCalendarType, "MMM", Locale.getDefault());
        DAY_FORMAT = new LocalDateFormat(activity, mCalendarType, "dd", Locale.getDefault());
        setTimeZone(mCalendar.getTimeZone());
        mDefaultLimiter = new DefaultDateRangeLimiter<>(activity);
        mDateRangeLimiter = mDefaultLimiter;

        if (savedInstanceState != null) {
            mCalendar.set(Calendar.YEAR, savedInstanceState.getInt(KEY_SELECTED_YEAR));
            mCalendar.set(Calendar.MONTH, savedInstanceState.getInt(KEY_SELECTED_MONTH));
            mCalendar.set(Calendar.DAY_OF_MONTH, savedInstanceState.getInt(KEY_SELECTED_DAY));
            mDefaultView = savedInstanceState.getInt(KEY_DEFAULT_VIEW);
        }
        VERSION_2_FORMAT = new LocalDateFormat(activity, mCalendarType,
                DateFormat.getBestDateTimePattern(mLocale, "EEEMMMdd"), mLocale);
        VERSION_2_FORMAT.setTimeZone(getTimeZone());
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_CALENDAR_TYPE, mCalendarType.getSimpleName());
        outState.putInt(KEY_SELECTED_YEAR, mCalendar.get(Calendar.YEAR));
        outState.putInt(KEY_SELECTED_MONTH, mCalendar.get(Calendar.MONTH));
        outState.putInt(KEY_SELECTED_DAY, mCalendar.get(Calendar.DAY_OF_MONTH));
        outState.putInt(KEY_WEEK_START, mWeekStart);
        outState.putInt(KEY_CURRENT_VIEW, mCurrentView);
        int listPosition = -1;
        if (mCurrentView == MONTH_AND_DAY_VIEW) {
            listPosition = mDayPickerView.getMostVisiblePosition();
        } else if (mCurrentView == YEAR_VIEW) {
            listPosition = mYearPickerView.getFirstVisiblePosition();
            outState.putInt(KEY_LIST_POSITION_OFFSET, mYearPickerView.getFirstPositionOffset());
        }
        outState.putInt(KEY_LIST_POSITION, listPosition);
        outState.putSerializable(KEY_HIGHLIGHTED_DAYS, highlightedDays);
        if (mAccentColor != null) outState.putInt(KEY_ACCENT, mAccentColor);
        outState.putBoolean(KEY_VIBRATE, mVibrate);
        outState.putBoolean(KEY_DISMISS, mDismissOnPause);
        outState.putBoolean(KEY_AUTO_DISMISS, mAutoDismiss);
        outState.putInt(KEY_DEFAULT_VIEW, mDefaultView);
        outState.putString(KEY_TITLE, mTitle);
        if (mOkColor != null) outState.putInt(KEY_OK_COLOR, mOkColor);
        if (mCancelColor != null) outState.putInt(KEY_CANCEL_COLOR, mCancelColor);
        outState.putSerializable(KEY_VERSION, mVersion);
        outState.putSerializable(KEY_SCROLL_ORIENTATION, mScrollOrientation);
        outState.putSerializable(KEY_TIMEZONE, mTimezone);
        outState.putParcelable(KEY_DATERANGELIMITER, mDateRangeLimiter);
        outState.putSerializable(KEY_LOCALE, mLocale);
        outState.putSerializable(KEY_CALENDAR_TYPE, mCalendarType.toString());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        int listPosition = -1;
        int listPositionOffset = 0;
        int currentView = mDefaultView;
        if (mScrollOrientation == null)
            mScrollOrientation = mVersion == Version.VERSION_1
                    ? ScrollOrientation.VERTICAL
                    : ScrollOrientation.HORIZONTAL;
        if (savedInstanceState != null) {
            mCalendarType = Utils.calendarTypeFromSimpleName(savedInstanceState.getString(KEY_CALENDAR_TYPE));
            mWeekStart = savedInstanceState.getInt(KEY_WEEK_START);
            currentView = savedInstanceState.getInt(KEY_CURRENT_VIEW);
            listPosition = savedInstanceState.getInt(KEY_LIST_POSITION);
            listPositionOffset = savedInstanceState.getInt(KEY_LIST_POSITION_OFFSET);
            //noinspection unchecked
            highlightedDays = (HashSet<Calendar>) savedInstanceState.getSerializable(KEY_HIGHLIGHTED_DAYS);
            if (savedInstanceState.containsKey(KEY_ACCENT))
                mAccentColor = savedInstanceState.getInt(KEY_ACCENT);
            mVibrate = savedInstanceState.getBoolean(KEY_VIBRATE);
            mDismissOnPause = savedInstanceState.getBoolean(KEY_DISMISS);
            mAutoDismiss = savedInstanceState.getBoolean(KEY_AUTO_DISMISS);
            mTitle = savedInstanceState.getString(KEY_TITLE);
            if (savedInstanceState.containsKey(KEY_OK_COLOR))
                mOkColor = savedInstanceState.getInt(KEY_OK_COLOR);
            if (savedInstanceState.containsKey(KEY_CANCEL_COLOR))
                mCancelColor = savedInstanceState.getInt(KEY_CANCEL_COLOR);
            mVersion = (Version) savedInstanceState.getSerializable(KEY_VERSION);
            mScrollOrientation = (ScrollOrientation) savedInstanceState.getSerializable(KEY_SCROLL_ORIENTATION);
            mTimezone = (TimeZone) savedInstanceState.getSerializable(KEY_TIMEZONE);
            mDateRangeLimiter = savedInstanceState.getParcelable(KEY_DATERANGELIMITER);

            setLocale((Locale) savedInstanceState.getSerializable(KEY_LOCALE));
            if (mDateRangeLimiter instanceof DefaultDateRangeLimiter)
                mDefaultLimiter = (DefaultDateRangeLimiter<CAL>) mDateRangeLimiter;
            else mDefaultLimiter = new DefaultDateRangeLimiter<>(getContext());
        }

        mDefaultLimiter.setController(this);

        int viewRes = mVersion == Version.VERSION_1 ? R.layout.mdtp_date_picker_dialog : R.layout.mdtp_date_picker_dialog_v2;
        View view = inflater.inflate(viewRes, container, false);
        // All options have been set at this point: round the initial selection if necessary
        mCalendar = (CAL) mDateRangeLimiter.setToNearestDate(mCalendar);

        mDatePickerHeaderView = view.findViewById(R.id.mdtp_date_picker_header);
        mMonthAndDayView = view.findViewById(R.id.mdtp_date_picker_month_and_day);
        mMonthAndDayView.setOnClickListener(this);
        mSelectedMonthTextView = view.findViewById(R.id.mdtp_date_picker_month);
        mSelectedDayTextView = view.findViewById(R.id.mdtp_date_picker_day);
        mYearView = view.findViewById(R.id.mdtp_date_picker_year);
        mYearView.setOnClickListener(this);

        final Activity activity = requireActivity();
        mDayPickerView = new DayPickerGroup<>(activity, this);
        mYearPickerView = new YearPickerView<>(activity, this);

        Resources res = getResources();
        mDayPickerDescription = res.getString(R.string.mdtp_day_picker_description);
        mSelectDay = res.getString(R.string.mdtp_select_day);
        mYearPickerDescription = res.getString(R.string.mdtp_year_picker_description);
        mSelectYear = res.getString(R.string.mdtp_select_year);

        int bgColorResource = R.color.mdtp_date_picker_view_animator;
        int bgColor = ContextCompat.getColor(activity, bgColorResource);
        view.setBackgroundColor(bgColor);

        mAnimator = view.findViewById(R.id.mdtp_animator);
        mAnimator.addView(mDayPickerView);
        mAnimator.addView(mYearPickerView);
        mAnimator.setDateMillis(mCalendar.getTimeInMillis());
        Animation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(ANIMATION_DURATION);
        mAnimator.setInAnimation(animation);
        Animation animation2 = new AlphaAnimation(1.0f, 0.0f);
        animation2.setDuration(ANIMATION_DURATION);
        mAnimator.setOutAnimation(animation2);

        Typeface font1 = Utils.mdtpFont(activity, false);
        Button okButton = view.findViewById(R.id.mdtp_ok);
        okButton.setOnClickListener(v -> {
            tryVibrate();
            notifyOnDateListener();
            dismiss();
        });
        okButton.setTypeface(font1);
        okButton.setText(android.R.string.ok);

        Button cancelButton = view.findViewById(R.id.mdtp_cancel);
        cancelButton.setOnClickListener(v -> {
            tryVibrate();
            if (getDialog() != null) getDialog().cancel();
        });
        cancelButton.setTypeface(font1);
        cancelButton.setText(android.R.string.cancel);
        cancelButton.setVisibility(isCancelable() ? View.VISIBLE : View.GONE);

        // If an accent color has not been set manually, get it from the context
        if (mAccentColor == null)
            mAccentColor = Utils.getAccentColorFromThemeIfAvailable(activity);
        if (mDatePickerHeaderView != null)
            mDatePickerHeaderView.setBackgroundColor(Utils.darkenColor(mAccentColor));
        view.findViewById(R.id.mdtp_day_picker_selected_date_layout).setBackgroundColor(mAccentColor);

        // Buttons can have a different color
        if (mOkColor == null) mOkColor = mAccentColor;
        okButton.setTextColor(mOkColor);

        if (mCancelColor == null) mCancelColor = mAccentColor;
        cancelButton.setTextColor(mCancelColor);

        if (getDialog() == null)
            view.findViewById(R.id.mdtp_done_background).setVisibility(View.GONE);

        updateDisplay(false);
        setCurrentView(currentView);

        if (listPosition != -1) {
            if (currentView == MONTH_AND_DAY_VIEW)
                mDayPickerView.postSetSelection(listPosition);
            else if (currentView == YEAR_VIEW)
                mYearPickerView.postSetSelectionFromTop(listPosition, listPositionOffset);
        }

        mHapticFeedbackController = new HapticFeedbackController(activity);
        return view;
    }

    @Override
    public void onConfigurationChanged(@NonNull final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        ViewGroup viewGroup = (ViewGroup) getView();
        if (viewGroup != null) {
            viewGroup.removeAllViewsInLayout();
            View view = onCreateView(requireActivity().getLayoutInflater(), viewGroup, null);
            viewGroup.addView(view);
        }
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
        if (mDismissOnPause) dismiss();
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);
        if (mOnCancelListener != null) mOnCancelListener.onCancel(dialog);
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (mOnDismissListener != null) mOnDismissListener.onDismiss(dialog);
    }

    private void setCurrentView(final int viewIndex) {
        switch (viewIndex) {
            case MONTH_AND_DAY_VIEW:
                if (mVersion == Version.VERSION_1) {
                    ObjectAnimator pulseAnimator = Utils.getPulseAnimator(mMonthAndDayView, 0.9f,
                            1.05f);
                    if (mDelayAnimation) {
                        pulseAnimator.setStartDelay(ANIMATION_DELAY);
                        mDelayAnimation = false;
                    }
                    if (mCurrentView != viewIndex) {
                        mMonthAndDayView.setSelected(true);
                        mYearView.setSelected(false);
                        mAnimator.setDisplayedChild(MONTH_AND_DAY_VIEW);
                        mCurrentView = viewIndex;
                    }
                    mDayPickerView.onDateChanged();
                    pulseAnimator.start();
                } else {
                    if (mCurrentView != viewIndex) {
                        mMonthAndDayView.setSelected(true);
                        mYearView.setSelected(false);
                        mAnimator.setDisplayedChild(MONTH_AND_DAY_VIEW);
                        mCurrentView = viewIndex;
                    }
                    mDayPickerView.onDateChanged();
                }

                int flags = DateUtils.FORMAT_SHOW_DATE;
                String dayString = DateUtils.formatDateTime(
                        getActivity(), mCalendar.getTimeInMillis(), flags);
                mAnimator.setContentDescription(mDayPickerDescription + ": " + dayString);
                Utils.tryAccessibilityAnnounce(mAnimator, mSelectDay);
                break;
            case YEAR_VIEW:
                if (mVersion == Version.VERSION_1) {
                    ObjectAnimator pulseAnimator = Utils.getPulseAnimator(mYearView, 0.85f, 1.1f);
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
                } else {
                    mYearPickerView.onDateChanged();
                    if (mCurrentView != viewIndex) {
                        mMonthAndDayView.setSelected(false);
                        mYearView.setSelected(true);
                        mAnimator.setDisplayedChild(YEAR_VIEW);
                        mCurrentView = viewIndex;
                    }
                }

                CharSequence yearString = YEAR_FORMAT.format(mCalendar);
                mAnimator.setContentDescription(mYearPickerDescription + ": " + yearString);
                Utils.tryAccessibilityAnnounce(mAnimator, mSelectYear);
                break;
        }
    }

    private void updateDisplay(boolean announce) {
        mYearView.setText(YEAR_FORMAT.format(mCalendar));

        if (mVersion == Version.VERSION_1) {
            if (mDatePickerHeaderView != null) {
                if (mTitle != null) mDatePickerHeaderView.setText(mTitle);
                else mDatePickerHeaderView.setText(
                        DateFormatSymbols.getInstance(mLocale)
                                .getWeekdays()[mCalendar.get(Calendar.DAY_OF_WEEK)]);
            }
            mSelectedMonthTextView.setText(MONTH_FORMAT.format(mCalendar));
            mSelectedDayTextView.setText(DAY_FORMAT.format(mCalendar));
        }

        if (mVersion == Version.VERSION_2) {
            mSelectedDayTextView.setText(VERSION_2_FORMAT.format(mCalendar));
            if (mTitle != null) mDatePickerHeaderView.setText(mTitle.toUpperCase(mLocale));
            else mDatePickerHeaderView.setVisibility(View.GONE);
        }

        // Accessibility.
        long millis = mCalendar.getTimeInMillis();
        mAnimator.setDateMillis(millis);
        int flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NO_YEAR;
        mMonthAndDayView.setContentDescription(
                DateUtils.formatDateTime(getActivity(), millis, flags));

        if (announce) {
            flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR;
            String fullDateText = DateUtils.formatDateTime(getActivity(), millis, flags);
            Utils.tryAccessibilityAnnounce(mAnimator, fullDateText);
        }
    }

    @SuppressWarnings("unused")
    public void doVibrate(boolean vibrate) {
        mVibrate = vibrate;
    }

    @SuppressWarnings("unused")
    public void dismissOnPause(boolean dismissOnPause) {
        mDismissOnPause = dismissOnPause;
    }

    @SuppressWarnings("unused")
    public void autoDismiss(boolean autoDismiss) {
        mAutoDismiss = autoDismiss;
    }

    @SuppressWarnings("unused")
    public void setAccentColor(String color) {
        mAccentColor = Color.parseColor(color);
    }

    public void setAccentColor(@ColorInt int color) {
        mAccentColor = Color.argb(255, Color.red(color), Color.green(color), Color.blue(color));
    }

    @SuppressWarnings("unused")
    public void setOkColor(String color) {
        mOkColor = Color.parseColor(color);
    }

    @SuppressWarnings("unused")
    public void setOkColor(@ColorInt int color) {
        mOkColor = Color.argb(255, Color.red(color), Color.green(color), Color.blue(color));
    }

    @SuppressWarnings("unused")
    public void setCancelColor(String color) {
        mCancelColor = Color.parseColor(color);
    }

    @SuppressWarnings("unused")
    public void setCancelColor(@ColorInt int color) {
        mCancelColor = Color.argb(255, Color.red(color), Color.green(color), Color.blue(color));
    }

    @Override
    public int getAccentColor() {
        return mAccentColor;
    }

    @SuppressWarnings("unused")
    public void showYearPickerFirst(boolean yearPicker) {
        mDefaultView = yearPicker ? YEAR_VIEW : MONTH_AND_DAY_VIEW;
    }

    @SuppressWarnings("unused")
    public void setFirstDayOfWeek(int startOfWeek) {
        if (startOfWeek < Calendar.SUNDAY || startOfWeek > Calendar.SATURDAY) {
            throw new IllegalArgumentException("Value must be between Calendar.SUNDAY and " +
                    "Calendar.SATURDAY");
        }
        mWeekStart = startOfWeek;
        if (mDayPickerView != null) mDayPickerView.onChange();
    }

    @SuppressWarnings("unused")
    public void setYearRange(int startYear, int endYear) {
        mDefaultLimiter.setYearRange(startYear, endYear);
        if (mDayPickerView != null) mDayPickerView.onChange();
    }

    @SuppressWarnings("unused")
    public void setMinDate(CAL calendar) {
        mDefaultLimiter.setMinDate(calendar);
        if (mDayPickerView != null) mDayPickerView.onChange();
    }

    @SuppressWarnings("unused")
    public CAL getMinDate() {
        return mDefaultLimiter.getMinDate();
    }

    @SuppressWarnings("unused")
    public void setMaxDate(CAL calendar) {
        mDefaultLimiter.setMaxDate(calendar);
        if (mDayPickerView != null) mDayPickerView.onChange();
    }

    @SuppressWarnings("unused")
    public CAL getMaxDate() {
        return mDefaultLimiter.getMaxDate();
    }

    @SuppressWarnings("unused")
    public void setHighlightedDays(CAL[] highlightedDays) {
        for (CAL highlightedDay : highlightedDays)
            this.highlightedDays.add(Utils.trimToMidnight((CAL) highlightedDay.clone()));
        if (mDayPickerView != null) mDayPickerView.onChange();
    }

    @SuppressWarnings("unused")
    public CAL[] getHighlightedDays() {
        if (highlightedDays.isEmpty()) return null;
        CAL[] output = (CAL[]) highlightedDays.toArray(new Calendar[0]);
        Arrays.sort(output);
        return output;
    }

    @Override
    public boolean isHighlighted(int year, int month, int day) {
        Calendar date = Utils.createCalendar(mCalendarType, getTimeZone());
        date.set(Calendar.YEAR, year);
        date.set(Calendar.MONTH, month);
        date.set(Calendar.DAY_OF_MONTH, day);
        Utils.trimToMidnight(date);
        return highlightedDays.contains(date);
    }

    @SuppressWarnings("unused")
    public void setSelectableDays(CAL[] selectableDays) {
        mDefaultLimiter.setSelectableDays(selectableDays);
        if (mDayPickerView != null) mDayPickerView.onChange();
    }

    @SuppressWarnings("unused")
    public CAL[] getSelectableDays() {
        return mDefaultLimiter.getSelectableDays();
    }

    @SuppressWarnings("unused")
    public void setDisabledDays(CAL[] disabledDays) {
        mDefaultLimiter.setDisabledDays(disabledDays);
        if (mDayPickerView != null) mDayPickerView.onChange();
    }

    @SuppressWarnings("unused")
    public CAL[] getDisabledDays() {
        return mDefaultLimiter.getDisabledDays();
    }

    @SuppressWarnings("unused")
    public void setDateRangeLimiter(DateRangeLimiter<CAL> dateRangeLimiter) {
        mDateRangeLimiter = dateRangeLimiter;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public void setVersion(Version version) {
        mVersion = version;
    }

    public Version getVersion() {
        return mVersion;
    }

    @SuppressWarnings("unused")
    public void setScrollOrientation(ScrollOrientation orientation) {
        mScrollOrientation = orientation;
    }

    public ScrollOrientation getScrollOrientation() {
        return mScrollOrientation;
    }

    public void setTimeZone(TimeZone timeZone) {
        mTimezone = timeZone;
        mCalendar.setTimeZone(timeZone);
        YEAR_FORMAT.setTimeZone(timeZone);
        MONTH_FORMAT.setTimeZone(timeZone);
        DAY_FORMAT.setTimeZone(timeZone);
    }

    @SuppressWarnings("WeakerAccess")
    public void setLocale(Locale locale) {
        mLocale = locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            mWeekStart = WeekFields.of(mLocale).getFirstDayOfWeek().getValue();
        YEAR_FORMAT = new LocalDateFormat(getContext(), mCalendarType, "yyyy", locale);
        MONTH_FORMAT = new LocalDateFormat(getContext(), mCalendarType, "MMM", locale);
        DAY_FORMAT = new LocalDateFormat(getContext(), mCalendarType, "dd", locale);
    }

    @Override
    public Locale getLocale() {
        return mLocale;
    }

    @SuppressWarnings("unused")
    public void setOnDateSetListener(OnDateSetListener listener) {
        mCallBack = listener;
    }

    @SuppressWarnings("unused")
    public DatePickerDialog<CAL> setOnCancelListener(DialogInterface.OnCancelListener onCancelListener) {
        mOnCancelListener = onCancelListener;
        return this;
    }

    @SuppressWarnings("unused")
    public DatePickerDialog<CAL> setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
        mOnDismissListener = onDismissListener;
        return this;
    }

    @SuppressWarnings("unused")
    public OnDateSetListener getOnDateSetListener() {
        return mCallBack;
    }

    // If the newly selected month / year does not contain the currently selected day number,
    // change the selected day number to the last day of the selected month or year.
    //      e.g. Switching from Mar to Apr when Mar 31 is selected -> Apr 30
    //      e.g. Switching from 2012 to 2013 when Feb 29, 2012 is selected -> Feb 28, 2013
    private Calendar adjustDayInMonthIfNeeded(CAL calendar) {
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        if (day > daysInMonth) {
            calendar.set(Calendar.DAY_OF_MONTH, daysInMonth);
        }
        return mDateRangeLimiter.setToNearestDate(calendar);
    }

    @Override
    public void onClick(View v) {
        tryVibrate();
        if (v.getId() == R.id.mdtp_date_picker_year) {
            setCurrentView(YEAR_VIEW);
        } else if (v.getId() == R.id.mdtp_date_picker_month_and_day) {
            setCurrentView(MONTH_AND_DAY_VIEW);
        }
    }

    @Override
    public void onYearSelected(int year) {
        mCalendar.set(Calendar.YEAR, year);
        mCalendar = (CAL) adjustDayInMonthIfNeeded(mCalendar);
        updatePickers();
        setCurrentView(MONTH_AND_DAY_VIEW);
        updateDisplay(true);
    }

    @Override
    public void onDayOfMonthSelected(int year, int month, int day) {
        mCalendar.set(Calendar.YEAR, year);
        mCalendar.set(Calendar.MONTH, month);
        mCalendar.set(Calendar.DAY_OF_MONTH, day);
        updatePickers();
        updateDisplay(true);
        if (mAutoDismiss) {
            notifyOnDateListener();
            dismiss();
        }
    }

    private void updatePickers() {
        for (OnDateChangedListener listener : mListeners) listener.onDateChanged();
    }


    @Override
    public MonthAdapter.CalendarDay<CAL> getSelectedDay() {
        return new MonthAdapter.CalendarDay<>(mCalendar, getTimeZone(), (Class<CAL>) getCalendarType());
    }

    @Override
    public CAL getStartDate() {
        return mDateRangeLimiter.getStartDate();
    }

    @Override
    public CAL getEndDate() {
        return mDateRangeLimiter.getEndDate();
    }

    @Override
    public int getMinYear() {
        return mDateRangeLimiter.getMinYear();
    }

    @Override
    public int getMaxYear() {
        return mDateRangeLimiter.getMaxYear();
    }


    @Override
    public boolean isOutOfRange(int year, int month, int day) {
        return mDateRangeLimiter.isOutOfRange(year, month, day);
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
        if (mVibrate) mHapticFeedbackController.tryVibrate();
    }

    @Override
    public TimeZone getTimeZone() {
        return mTimezone == null ? TimeZone.getDefault() : mTimezone;
    }

    public void notifyOnDateListener() {
        if (mCallBack != null)
            mCallBack.onDateSet(DatePickerDialog.this, mCalendar.getTimeInMillis());
    }

    @Override
    public Class<CAL> getCalendarType() {
        return (Class<CAL>) mCalendarType;
    }
}
