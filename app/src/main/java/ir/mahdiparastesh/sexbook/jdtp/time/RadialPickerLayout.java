package ir.mahdiparastesh.sexbook.jdtp.time;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.icu.text.DateFormat;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.util.Calendar;
import java.util.Locale;

import ir.mahdiparastesh.sexbook.R;
import ir.mahdiparastesh.sexbook.jdtp.HapticFeedbackController;
import ir.mahdiparastesh.sexbook.jdtp.utils.LanguageUtils;

public class RadialPickerLayout extends FrameLayout implements OnTouchListener {
    private final int TOUCH_SLOP;
    private final int TAP_TIMEOUT;

    private static final int VISIBLE_DEGREES_STEP_SIZE = 30;
    private static final int HOUR_VALUE_TO_DEGREES_STEP_SIZE = VISIBLE_DEGREES_STEP_SIZE;
    private static final int MINUTE_VALUE_TO_DEGREES_STEP_SIZE = 6;
    private static final int HOUR_INDEX = TimePickerDialog.HOUR_INDEX;
    private static final int MINUTE_INDEX = TimePickerDialog.MINUTE_INDEX;
    private static final int AMPM_INDEX = TimePickerDialog.AMPM_INDEX;
    private static final int ENABLE_PICKER_INDEX = TimePickerDialog.ENABLE_PICKER_INDEX;
    private static final int AM = TimePickerDialog.AM;
    private static final int PM = TimePickerDialog.PM;

    private int mLastValueSelected;

    private HapticFeedbackController mHapticFeedbackController;
    private OnValueSelectedListener mListener;
    private boolean mTimeInitialized;
    private int mCurrentHoursOfDay;
    private int mCurrentMinutes;
    private boolean mIs24HourMode;
    private boolean mHideAmPm;
    private int mCurrentItemShowing;

    private final CircleView mCircleView;
    private final AmPmCirclesView mAmPmCirclesView;
    private final RadialTextsView mHourRadialTextsView;
    private final RadialTextsView mMinuteRadialTextsView;
    private final RadialSelectorView mHourRadialSelectorView;
    private final RadialSelectorView mMinuteRadialSelectorView;
    private final View mGrayBox;

    private int[] mSnapPrefer30sMap;
    private boolean mInputEnabled;
    private int mIsTouchingAmOrPm = -1;
    private boolean mDoingMove;
    private boolean mDoingTouch;
    private int mDownDegrees;
    private float mDownX;
    private float mDownY;
    private final AccessibilityManager mAccessibilityManager;

    private AnimatorSet mTransition;
    private final Handler mHandler = new Handler();

    public interface OnValueSelectedListener {
        void onValueSelected(int pickerIndex, int newValue, boolean autoAdvance);
    }

    public RadialPickerLayout(Context c, AttributeSet attrs) {
        super(c, attrs);

        setOnTouchListener(this);
        ViewConfiguration vc = ViewConfiguration.get(c);
        TOUCH_SLOP = vc.getScaledTouchSlop();
        TAP_TIMEOUT = ViewConfiguration.getTapTimeout();
        mDoingMove = false;

        mCircleView = new CircleView(c);
        addView(mCircleView);

        mAmPmCirclesView = new AmPmCirclesView(c);
        addView(mAmPmCirclesView);

        mHourRadialSelectorView = new RadialSelectorView(c);
        addView(mHourRadialSelectorView);
        mMinuteRadialSelectorView = new RadialSelectorView(c);
        addView(mMinuteRadialSelectorView);

        mHourRadialTextsView = new RadialTextsView(c);
        addView(mHourRadialTextsView);
        mMinuteRadialTextsView = new RadialTextsView(c);
        addView(mMinuteRadialTextsView);

        preparePrefer30sMap();
        mLastValueSelected = -1;
        mInputEnabled = true;

        mGrayBox = new View(c);
        mGrayBox.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mGrayBox.setBackgroundColor(ContextCompat.getColor(c, R.color.jdtp_transparent_black));
        mGrayBox.setVisibility(View.INVISIBLE);
        addView(mGrayBox);

        mAccessibilityManager = (AccessibilityManager) c.getSystemService(Context.ACCESSIBILITY_SERVICE);

        mTimeInitialized = false;
    }

    public void setOnValueSelectedListener(OnValueSelectedListener listener) {
        mListener = listener;
    }

    public void initialize(Context c, HapticFeedbackController hapticFeedbackController,
                           int initialHoursOfDay, int initialMinutes, boolean is24HourMode) {
        if (mTimeInitialized) return;

        mHapticFeedbackController = hapticFeedbackController;
        mIs24HourMode = is24HourMode;
        mHideAmPm = mAccessibilityManager.isTouchExplorationEnabled() || mIs24HourMode;

        mCircleView.initialize(c, mHideAmPm);
        mCircleView.invalidate();
        if (!mHideAmPm) {
            mAmPmCirclesView.initialize(c, initialHoursOfDay < 12 ? AM : PM);
            mAmPmCirclesView.invalidate();
        }

        Resources res = c.getResources();
        int[] hours = {12, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
        int[] hours_24 = {0, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23};
        int[] minutes = {0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55};
        String[] hoursTexts = new String[12];
        String[] innerHoursTexts = new String[12];
        String[] minutesTexts = new String[12];
        for (int i = 0; i < 12; i++) {
            hoursTexts[i] = LanguageUtils.getPersianNumbers(
                    is24HourMode ? String.format(Locale.getDefault(), "%02d", hours_24[i]) : String.format(Locale.getDefault(), "%d", hours[i])
            );
            innerHoursTexts[i] = LanguageUtils.getPersianNumbers(String.format(Locale.getDefault(), "%d", hours[i]));
            minutesTexts[i] = LanguageUtils.getPersianNumbers(String.format(Locale.getDefault(), "%02d", minutes[i]));
        }
        mHourRadialTextsView.initialize(res,
                hoursTexts, (is24HourMode ? innerHoursTexts : null), mHideAmPm, true);
        mHourRadialTextsView.setSelection(is24HourMode ? initialHoursOfDay : initialHoursOfDay % 12);
        mHourRadialTextsView.invalidate();
        mMinuteRadialTextsView.initialize(res, minutesTexts, null, mHideAmPm, false);
        mMinuteRadialTextsView.setSelection(initialMinutes);
        mMinuteRadialTextsView.invalidate();


        setValueForItem(HOUR_INDEX, initialHoursOfDay);
        setValueForItem(MINUTE_INDEX, initialMinutes);
        int hourDegrees = (initialHoursOfDay % 12) * HOUR_VALUE_TO_DEGREES_STEP_SIZE;
        mHourRadialSelectorView.initialize(c, mHideAmPm, is24HourMode, true,
                hourDegrees, isHourInnerCircle(initialHoursOfDay));
        int minuteDegrees = initialMinutes * MINUTE_VALUE_TO_DEGREES_STEP_SIZE;
        mMinuteRadialSelectorView.initialize(c, mHideAmPm, false, false,
                minuteDegrees, false);

        mTimeInitialized = true;
    }

    void setTheme(Context c, boolean themeDark) {
        mCircleView.setTheme(c, themeDark);
        mAmPmCirclesView.setTheme(c, themeDark);
        mHourRadialTextsView.setTheme(c, themeDark);
        mMinuteRadialTextsView.setTheme(c, themeDark);
        mHourRadialSelectorView.setTheme(c, themeDark);
        mMinuteRadialSelectorView.setTheme(c, themeDark);
    }

    public void setTime(int hours, int minutes) {
        setItem(HOUR_INDEX, hours);
        setItem(MINUTE_INDEX, minutes);
    }

    private void setItem(int index, int value) {
        if (index == HOUR_INDEX) {
            setValueForItem(HOUR_INDEX, value);
            int hourDegrees = (value % 12) * HOUR_VALUE_TO_DEGREES_STEP_SIZE;
            mHourRadialSelectorView.setSelection(hourDegrees, isHourInnerCircle(value), false);
            mHourRadialSelectorView.invalidate();
            mHourRadialTextsView.setSelection(value);
            mHourRadialTextsView.invalidate();
        } else if (index == MINUTE_INDEX) {
            setValueForItem(MINUTE_INDEX, value);
            int minuteDegrees = value * MINUTE_VALUE_TO_DEGREES_STEP_SIZE;
            mMinuteRadialSelectorView.setSelection(minuteDegrees, false, false);
            mMinuteRadialSelectorView.invalidate();
            mMinuteRadialTextsView.setSelection(value);
            mHourRadialTextsView.invalidate();
        }
    }

    private boolean isHourInnerCircle(int hourOfDay) {

        return mIs24HourMode && (hourOfDay <= 12 && hourOfDay != 0);
    }

    public int getHours() {
        return mCurrentHoursOfDay;
    }

    public int getMinutes() {
        return mCurrentMinutes;
    }

    private int getCurrentlyShowingValue() {
        int currentIndex = getCurrentItemShowing();
        if (currentIndex == HOUR_INDEX)
            return mCurrentHoursOfDay;
        else if (currentIndex == MINUTE_INDEX)
            return mCurrentMinutes;
        else return -1;
    }

    public int getIsCurrentlyAmOrPm() {
        if (mCurrentHoursOfDay < 12) return AM;
        else if (mCurrentHoursOfDay < 24) return PM;
        return -1;
    }

    private void setValueForItem(int index, int value) {
        if (index == HOUR_INDEX)
            mCurrentHoursOfDay = value;
        else if (index == MINUTE_INDEX)
            mCurrentMinutes = value;
        else if (index == AMPM_INDEX) {
            if (value == AM)
                mCurrentHoursOfDay = mCurrentHoursOfDay % 12;
            else if (value == PM)
                mCurrentHoursOfDay = (mCurrentHoursOfDay % 12) + 12;
        }
    }

    public void setAmOrPm(int amOrPm) {
        mAmPmCirclesView.setAmOrPm(amOrPm);
        mAmPmCirclesView.invalidate();
        setValueForItem(AMPM_INDEX, amOrPm);
    }

    private void preparePrefer30sMap() {
        mSnapPrefer30sMap = new int[361];

        int snappedOutputDegrees = 0;
        int count = 1;
        int expectedCount = 8;
        for (int degrees = 0; degrees < 361; degrees++) {
            mSnapPrefer30sMap[degrees] = snappedOutputDegrees;
            if (count == expectedCount) {
                snappedOutputDegrees += 6;
                if (snappedOutputDegrees == 360)
                    expectedCount = 7;
                else if (snappedOutputDegrees % 30 == 0)
                    expectedCount = 14;
                else expectedCount = 4;
                count = 1;
            } else count++;
        }
    }

    private int snapPrefer30s(int degrees) {
        if (mSnapPrefer30sMap == null) {
            return -1;
        }
        return mSnapPrefer30sMap[degrees];
    }

    private static int snapOnly30s(int degrees, int forceHigherOrLower) {
        int stepSize = HOUR_VALUE_TO_DEGREES_STEP_SIZE;
        int floor = (degrees / stepSize) * stepSize;
        int ceiling = floor + stepSize;
        if (forceHigherOrLower == 1)
            degrees = ceiling;
        else if (forceHigherOrLower == -1) {
            if (degrees == floor)
                floor -= stepSize;
            degrees = floor;
        } else {
            if ((degrees - floor) < (ceiling - degrees))
                degrees = floor;
            else degrees = ceiling;
        }
        return degrees;
    }

    private int reselectSelector(int degrees, boolean isInnerCircle,
                                 boolean forceToVisibleValue, boolean forceDrawDot) {
        if (degrees == -1) return -1;
        int currentShowing = getCurrentItemShowing();

        int stepSize;
        boolean allowFineGrained = !forceToVisibleValue && (currentShowing == MINUTE_INDEX);
        if (allowFineGrained)
            degrees = snapPrefer30s(degrees);
        else
            degrees = snapOnly30s(degrees, 0);

        RadialSelectorView radialSelectorView;
        if (currentShowing == HOUR_INDEX) {
            radialSelectorView = mHourRadialSelectorView;
            stepSize = HOUR_VALUE_TO_DEGREES_STEP_SIZE;
        } else {
            radialSelectorView = mMinuteRadialSelectorView;
            stepSize = MINUTE_VALUE_TO_DEGREES_STEP_SIZE;
        }
        radialSelectorView.setSelection(degrees, isInnerCircle, forceDrawDot);
        radialSelectorView.invalidate();


        if (currentShowing == HOUR_INDEX) {
            if (mIs24HourMode) {
                if (degrees == 0 && isInnerCircle)
                    degrees = 360;
                else if (degrees == 360 && !isInnerCircle)
                    degrees = 0;
            } else if (degrees == 0) {
                degrees = 360;
            }
        } else if (degrees == 360 && currentShowing == MINUTE_INDEX) {
            degrees = 0;
        }

        int value = degrees / stepSize;

        if (currentShowing == HOUR_INDEX && mIs24HourMode && !isInnerCircle && degrees != 0)
            value += 12;

        if (getCurrentItemShowing() == HOUR_INDEX) {
            mHourRadialTextsView.setSelection(value);
            mHourRadialTextsView.invalidate();
        } else if (getCurrentItemShowing() == MINUTE_INDEX) {
            mMinuteRadialTextsView.setSelection(value);
            mMinuteRadialTextsView.invalidate();
        }

        return value;
    }

    private int getDegreesFromCoordinates(float pointX, float pointY, boolean forceLegal,
                                          final Boolean[] isInnerCircle) {
        int currentItem = getCurrentItemShowing();
        if (currentItem == HOUR_INDEX) {
            return mHourRadialSelectorView.getDegreesFromCoordinates(
                    pointX, pointY, forceLegal, isInnerCircle);
        } else if (currentItem == MINUTE_INDEX) {
            return mMinuteRadialSelectorView.getDegreesFromCoordinates(
                    pointX, pointY, forceLegal, isInnerCircle);
        } else {
            return -1;
        }
    }

    public int getCurrentItemShowing() {
        if (mCurrentItemShowing != HOUR_INDEX && mCurrentItemShowing != MINUTE_INDEX) return -1;
        return mCurrentItemShowing;
    }

    public void setCurrentItemShowing(int index, boolean animate) {
        if (index != HOUR_INDEX && index != MINUTE_INDEX) return;

        int lastIndex = getCurrentItemShowing();
        mCurrentItemShowing = index;

        if (animate && (index != lastIndex)) {
            ObjectAnimator[] anim = new ObjectAnimator[4];
            if (index == MINUTE_INDEX) {
                anim[0] = mHourRadialTextsView.getDisappearAnimator();
                anim[1] = mHourRadialSelectorView.getDisappearAnimator();
                anim[2] = mMinuteRadialTextsView.getReappearAnimator();
                anim[3] = mMinuteRadialSelectorView.getReappearAnimator();
            } else {
                anim[0] = mHourRadialTextsView.getReappearAnimator();
                anim[1] = mHourRadialSelectorView.getReappearAnimator();
                anim[2] = mMinuteRadialTextsView.getDisappearAnimator();
                anim[3] = mMinuteRadialSelectorView.getDisappearAnimator();
            }
            if (mTransition != null && mTransition.isRunning())
                mTransition.end();
            mTransition = new AnimatorSet();
            mTransition.playTogether(anim);
            mTransition.start();
        } else {
            int hourAlpha = (index == HOUR_INDEX) ? 255 : 0;
            int minuteAlpha = (index == MINUTE_INDEX) ? 255 : 0;
            mHourRadialTextsView.setAlpha(hourAlpha);
            mHourRadialSelectorView.setAlpha(hourAlpha);
            mMinuteRadialTextsView.setAlpha(minuteAlpha);
            mMinuteRadialSelectorView.setAlpha(minuteAlpha);
        }

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        final float eventX = event.getX();
        final float eventY = event.getY();
        int degrees;
        int value;
        final Boolean[] isInnerCircle = new Boolean[1];
        isInnerCircle[0] = false;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!mInputEnabled) return true;
                mDownX = eventX;
                mDownY = eventY;

                mLastValueSelected = -1;
                mDoingMove = false;
                mDoingTouch = true;
                if (!mHideAmPm)
                    mIsTouchingAmOrPm = mAmPmCirclesView.getIsTouchingAmOrPm(eventX, eventY);
                else mIsTouchingAmOrPm = -1;
                if (mIsTouchingAmOrPm == AM || mIsTouchingAmOrPm == PM) {
                    mHapticFeedbackController.tryVibrate();
                    mDownDegrees = -1;
                    mHandler.postDelayed(() -> {
                        mAmPmCirclesView.setAmOrPmPressed(mIsTouchingAmOrPm);
                        mAmPmCirclesView.invalidate();
                    }, TAP_TIMEOUT);
                } else {
                    boolean forceLegal = mAccessibilityManager.isTouchExplorationEnabled();
                    mDownDegrees = getDegreesFromCoordinates(eventX, eventY,
                            forceLegal, isInnerCircle);
                    if (mDownDegrees != -1) {
                        mHapticFeedbackController.tryVibrate();
                        mHandler.postDelayed(() -> {
                            mDoingMove = true;
                            int value1 = reselectSelector(mDownDegrees, isInnerCircle[0],
                                    false, true);
                            mLastValueSelected = value1;
                            mListener.onValueSelected(getCurrentItemShowing(), value1, false);
                        }, TAP_TIMEOUT);
                    }
                }
                return true;
            case MotionEvent.ACTION_MOVE:
                if (!mInputEnabled) return true;

                float dY = Math.abs(eventY - mDownY), dX = Math.abs(eventX - mDownX);
                if (!mDoingMove && dX <= TOUCH_SLOP && dY <= TOUCH_SLOP) break;
                if (mIsTouchingAmOrPm == AM || mIsTouchingAmOrPm == PM) {
                    mHandler.removeCallbacksAndMessages(null);
                    int isTouchingAmOrPm = mAmPmCirclesView.getIsTouchingAmOrPm(eventX, eventY);
                    if (isTouchingAmOrPm != mIsTouchingAmOrPm) {
                        mAmPmCirclesView.setAmOrPmPressed(-1);
                        mAmPmCirclesView.invalidate();
                        mIsTouchingAmOrPm = -1;
                    }
                    break;
                }

                if (mDownDegrees == -1) break;

                mDoingMove = true;
                mHandler.removeCallbacksAndMessages(null);
                degrees = getDegreesFromCoordinates(eventX, eventY, true, isInnerCircle);
                if (degrees != -1) {
                    value = reselectSelector(degrees, isInnerCircle[0], false, true);
                    if (value != mLastValueSelected) {
                        mHapticFeedbackController.tryVibrate();
                        mLastValueSelected = value;
                        mListener.onValueSelected(getCurrentItemShowing(), value, false);
                    }
                }
                return true;
            case MotionEvent.ACTION_UP:
                if (!mInputEnabled) {
                    mListener.onValueSelected(ENABLE_PICKER_INDEX, 1, false);
                    return true;
                }

                mHandler.removeCallbacksAndMessages(null);
                mDoingTouch = false;
                if (mIsTouchingAmOrPm == AM || mIsTouchingAmOrPm == PM) {
                    int isTouchingAmOrPm = mAmPmCirclesView.getIsTouchingAmOrPm(eventX, eventY);
                    mAmPmCirclesView.setAmOrPmPressed(-1);
                    mAmPmCirclesView.invalidate();

                    if (isTouchingAmOrPm == mIsTouchingAmOrPm) {
                        mAmPmCirclesView.setAmOrPm(isTouchingAmOrPm);
                        if (getIsCurrentlyAmOrPm() != isTouchingAmOrPm) {
                            mListener.onValueSelected(AMPM_INDEX, mIsTouchingAmOrPm,
                                    false);
                            setValueForItem(AMPM_INDEX, isTouchingAmOrPm);
                        }
                    }
                    mIsTouchingAmOrPm = -1;
                    break;
                }

                if (mDownDegrees != -1) {
                    degrees = getDegreesFromCoordinates(eventX, eventY, mDoingMove, isInnerCircle);
                    if (degrees != -1) {
                        value = reselectSelector(degrees, isInnerCircle[0], !mDoingMove,
                                false);

                        if (getCurrentItemShowing() == HOUR_INDEX && !mIs24HourMode) {
                            int amOrPm = getIsCurrentlyAmOrPm();
                            if (amOrPm == AM && value == 12)
                                value = 0;
                            else if (amOrPm == PM && value != 12)
                                value += 12;
                        }
                        setValueForItem(getCurrentItemShowing(), value);
                        mListener.onValueSelected(getCurrentItemShowing(), value, true);
                    }
                }
                mDoingMove = false;
                return true;
            default:
                break;
        }
        return false;
    }

    public boolean trySettingInputEnabled(boolean inputEnabled) {
        if (mDoingTouch && !inputEnabled) return false;
        mInputEnabled = inputEnabled;
        mGrayBox.setVisibility(inputEnabled ? View.INVISIBLE : View.VISIBLE);
        return true;
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(@NonNull AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_BACKWARD);
        info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD);
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            event.getText().clear();
            Calendar time = Calendar.getInstance();
            time.set(Calendar.HOUR, getHours());
            time.set(Calendar.MINUTE, getMinutes());
            long millis = time.getTimeInMillis();
            int flags = DateUtils.FORMAT_SHOW_TIME;
            if (mIs24HourMode) flags |= DateUtils.FORMAT_24HOUR;
            String timeString = LanguageUtils.getPersianNumbers(
                    DateUtils.formatDateTime(getContext(), millis, flags));
            event.getText().add(timeString);
            return true;
        }
        return super.dispatchPopulateAccessibilityEvent(event);
    }

    @Override
    public boolean performAccessibilityAction(int action, Bundle arguments) {
        if (super.performAccessibilityAction(action, arguments)) return true;

        int changeMultiplier = 0;
        if (action == AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
            changeMultiplier = 1;
        else if (action == AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD)
            changeMultiplier = -1;
        if (changeMultiplier != 0) {
            int value = getCurrentlyShowingValue(), stepSize = 0,
                    currentItemShowing = getCurrentItemShowing();
            if (currentItemShowing == HOUR_INDEX) {
                stepSize = HOUR_VALUE_TO_DEGREES_STEP_SIZE;
                value %= 12;
            } else if (currentItemShowing == MINUTE_INDEX)
                stepSize = MINUTE_VALUE_TO_DEGREES_STEP_SIZE;

            int degrees = value * stepSize;
            degrees = snapOnly30s(degrees, changeMultiplier);
            value = degrees / stepSize;
            int maxValue, minValue = 0;
            if (currentItemShowing == HOUR_INDEX) {
                if (mIs24HourMode)
                    maxValue = 23;
                else {
                    maxValue = 12;
                    minValue = 1;
                }
            } else maxValue = 55;

            if (value > maxValue) value = minValue;
            else if (value < minValue) value = maxValue;

            setItem(currentItemShowing, value);
            mListener.onValueSelected(currentItemShowing, value, false);
            return true;
        }
        return false;
    }
}
