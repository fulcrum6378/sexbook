package ir.mahdiparastesh.sexbook.jdtp.time;

import static ir.mahdiparastesh.sexbook.Fun.font1;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.ActionBar.LayoutParams;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;
import java.util.Locale;

import ir.mahdiparastesh.sexbook.R;
import ir.mahdiparastesh.sexbook.jdtp.HapticFeedbackController;
import ir.mahdiparastesh.sexbook.jdtp.Utils;
import ir.mahdiparastesh.sexbook.jdtp.time.RadialPickerLayout.OnValueSelectedListener;
import ir.mahdiparastesh.sexbook.jdtp.utils.LanguageUtils;

public class TimePickerDialog extends DialogFragment implements OnValueSelectedListener {
    private static final String KEY_HOUR_OF_DAY = "hour_of_day";
    private static final String KEY_MINUTE = "minute";
    private static final String KEY_IS_24_HOUR_VIEW = "is_24_hour_view";
    private static final String KEY_TITLE = "dialog_title";
    private static final String KEY_CURRENT_ITEM_SHOWING = "current_item_showing";
    private static final String KEY_IN_KB_MODE = "in_kb_mode";
    private static final String KEY_TYPED_TIMES = "typed_times";
    private static final String KEY_DARK_THEME = "dark_theme";

    public static final int HOUR_INDEX = 0;
    public static final int MINUTE_INDEX = 1;
    public static final int AMPM_INDEX = 2;
    public static final int ENABLE_PICKER_INDEX = 3;
    public static final int AM = 0, PM = 1;

    private static final int PULSE_ANIMATOR_DELAY = 300;

    private OnTimeSetListener mCallback;
    private DialogInterface.OnCancelListener mOnCancelListener;
    private DialogInterface.OnDismissListener mOnDismissListener;
    private HapticFeedbackController mHapticFeedbackController;

    private Button mOkButton;
    private TextView mHourView;
    private TextView mHourSpaceView;
    private TextView mMinuteView;
    private TextView mMinuteSpaceView;
    private TextView mAmPmTextView;
    private View mAmPmHitSpace;
    private RadialPickerLayout mTimePicker;

    private int mSelectedColor;
    private int mUnselectedColor;
    private String mAmText;
    private String mPmText;

    private boolean mAllowAutoAdvance;
    private int mInitialHourOfDay;
    private int mInitialMinute;
    private boolean mIs24HourMode;
    private String mTitle;
    private boolean mThemeDark;

    private char mPlaceholderText;
    private String mDoublePlaceholderText;
    private String mDeletedKeyFormat;
    private boolean mInKbMode;
    private ArrayList<Integer> mTypedTimes;
    private Node mLegalTimesTree;
    private int mAmKeyCode;
    private int mPmKeyCode;

    private String mHourPickerDescription;
    private String mSelectHours;
    private String mMinutePickerDescription;
    private String mSelectMinutes;

    public interface OnTimeSetListener {
        void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute);
    }

    public TimePickerDialog() {

    }

    public static TimePickerDialog newInstance(OnTimeSetListener callback, int hourOfDay,
                                               int minute, boolean is24HourMode) {
        TimePickerDialog ret = new TimePickerDialog();
        ret.initialize(callback, hourOfDay, minute, is24HourMode);
        return ret;
    }

    public void initialize(OnTimeSetListener callback, int hourOfDay, int minute,
                           boolean is24HourMode) {
        mCallback = callback;
        mInitialHourOfDay = hourOfDay;
        mInitialMinute = minute;
        mIs24HourMode = is24HourMode;
        mInKbMode = false;
        mTitle = "";
        mThemeDark = false;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setThemeDark(boolean dark) {
        mThemeDark = dark;
    }

    public boolean isThemeDark() {
        return mThemeDark;
    }

    public void setOnTimeSetListener(OnTimeSetListener callback) {
        mCallback = callback;
    }

    public void setOnCancelListener(DialogInterface.OnCancelListener onCancelListener) {
        mOnCancelListener = onCancelListener;
    }

    public void setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
        mOnDismissListener = onDismissListener;
    }

    public void setStartTime(int hourOfDay, int minute) {
        mInitialHourOfDay = hourOfDay;
        mInitialMinute = minute;
        mInKbMode = false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null && savedInstanceState.containsKey(KEY_HOUR_OF_DAY)
                && savedInstanceState.containsKey(KEY_MINUTE)
                && savedInstanceState.containsKey(KEY_IS_24_HOUR_VIEW)) {
            mInitialHourOfDay = savedInstanceState.getInt(KEY_HOUR_OF_DAY);
            mInitialMinute = savedInstanceState.getInt(KEY_MINUTE);
            mIs24HourMode = savedInstanceState.getBoolean(KEY_IS_24_HOUR_VIEW);
            mInKbMode = savedInstanceState.getBoolean(KEY_IN_KB_MODE);
            mTitle = savedInstanceState.getString(KEY_TITLE);
            mThemeDark = savedInstanceState.getBoolean(KEY_DARK_THEME);
        }
    }

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        assert getDialog() != null;
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        View view = inflater.inflate(R.layout.jdtp_time_picker_dialog, null);
        final AppCompatActivity that = (AppCompatActivity) getActivity();
        assert that != null;
        KeyboardListener keyboardListener = new KeyboardListener();
        view.findViewById(R.id.time_picker_dialog).setOnKeyListener(keyboardListener);

        Resources res = getResources();
        mHourPickerDescription = res.getString(R.string.jdtp_hour_picker_description);
        mSelectHours = res.getString(R.string.jdtp_select_hours);
        mMinutePickerDescription = res.getString(R.string.jdtp_minute_picker_description);
        mSelectMinutes = res.getString(R.string.jdtp_select_minutes);
        mSelectedColor = ContextCompat.getColor(that, R.color.jdtp_white);
        mUnselectedColor = ContextCompat.getColor(that, R.color.jdtp_accent_color_focused);

        mHourView = view.findViewById(R.id.hours);
        mHourView.setOnKeyListener(keyboardListener);
        mHourSpaceView = view.findViewById(R.id.hour_space);
        mMinuteSpaceView = view.findViewById(R.id.minutes_space);
        mMinuteView = view.findViewById(R.id.minutes);
        mMinuteView.setOnKeyListener(keyboardListener);
        mAmPmTextView = view.findViewById(R.id.ampm_label);
        mOkButton = view.findViewById(R.id.ok);
        Button mCancelButton = view.findViewById(R.id.cancel);
        mOkButton.setTypeface(font1);
        mCancelButton.setTypeface(font1);
        mHourView.setTypeface(font1);
        mMinuteView.setTypeface(font1);
        mAmPmTextView.setTypeface(font1);
        mAmPmTextView.setOnKeyListener(keyboardListener);
        mAmText = "قبل‌ازظهر";
        mPmText = "بعدازظهر";

        mHapticFeedbackController = new HapticFeedbackController(getActivity());

        mTimePicker = view.findViewById(R.id.time_picker);
        mTimePicker.setOnValueSelectedListener(this);
        mTimePicker.setOnKeyListener(keyboardListener);
        mTimePicker.initialize(getActivity(), mHapticFeedbackController, mInitialHourOfDay,
                mInitialMinute, mIs24HourMode);

        int currentItemShowing = HOUR_INDEX;
        if (savedInstanceState != null &&
                savedInstanceState.containsKey(KEY_CURRENT_ITEM_SHOWING)) {
            currentItemShowing = savedInstanceState.getInt(KEY_CURRENT_ITEM_SHOWING);
        }
        setCurrentItemShowing(currentItemShowing, false, true, true);
        mTimePicker.invalidate();

        mHourView.setOnClickListener(v -> {
            setCurrentItemShowing(HOUR_INDEX, true, false, true);
            tryVibrate();
        });
        mMinuteView.setOnClickListener(v -> {
            setCurrentItemShowing(MINUTE_INDEX, true, false, true);
            tryVibrate();
        });

        mOkButton.setOnClickListener(v -> {
            if (mInKbMode && isTypedTimeFullyLegal())
                finishKbMode(false);
            else tryVibrate();
            if (mCallback != null) {
                mCallback.onTimeSet(mTimePicker, mTimePicker.getHours(), mTimePicker.getMinutes());
            }
            dismiss();
        });
        mOkButton.setOnKeyListener(keyboardListener);

        mCancelButton.setOnClickListener(v -> {
            tryVibrate();
            getDialog().cancel();
        });
        mCancelButton.setVisibility(isCancelable() ? View.VISIBLE : View.GONE);

        mAmPmHitSpace = view.findViewById(R.id.ampm_hitspace);
        if (mIs24HourMode) {
            mAmPmTextView.setVisibility(View.GONE);

            RelativeLayout.LayoutParams paramsSeparator = new RelativeLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            paramsSeparator.addRule(RelativeLayout.CENTER_IN_PARENT);
            TextView separatorView = view.findViewById(R.id.separator);
            separatorView.setLayoutParams(paramsSeparator);
        } else {
            mAmPmTextView.setVisibility(View.VISIBLE);
            updateAmPmDisplay(mInitialHourOfDay < 12 ? AM : PM);
            mAmPmHitSpace.setOnClickListener(v -> {
                tryVibrate();
                int amOrPm = mTimePicker.getIsCurrentlyAmOrPm();
                if (amOrPm == AM) amOrPm = PM;
                else if (amOrPm == PM) amOrPm = AM;
                updateAmPmDisplay(amOrPm);
                mTimePicker.setAmOrPm(amOrPm);
            });
        }

        mAllowAutoAdvance = true;
        setHour(mInitialHourOfDay, true);
        setMinute(mInitialMinute);


        mDoublePlaceholderText = res.getString(R.string.jdtp_time_placeholder);
        mDeletedKeyFormat = res.getString(R.string.jdtp_deleted_key);
        mPlaceholderText = mDoublePlaceholderText.charAt(0);
        mAmKeyCode = mPmKeyCode = -1;
        generateLegalTimesTree();
        if (mInKbMode) {
            if (savedInstanceState != null)
                mTypedTimes = savedInstanceState.getIntegerArrayList(KEY_TYPED_TIMES);
            tryStartingKbMode(-1);
            mHourView.invalidate();
        } else if (mTypedTimes == null)
            mTypedTimes = new ArrayList<>();

        TextView timePickerHeader = view.findViewById(R.id.time_picker_header);
        if (!mTitle.isEmpty()) {
            timePickerHeader.setVisibility(TextView.VISIBLE);
            timePickerHeader.setText(mTitle);
        }

        mTimePicker.setTheme(that.getApplicationContext(), mThemeDark);

        int circleBackground = ContextCompat.getColor(that, R.color.jdtp_circle_background);
        int backgroundColor = ContextCompat.getColor(that, R.color.jdtp_background_color);
        int darkBackgroundColor = ContextCompat.getColor(that, R.color.jdtp_light_gray);

        int lightGray = ContextCompat.getColor(that, R.color.jdtp_light_gray);

        mTimePicker.setBackgroundColor(mThemeDark ? lightGray : circleBackground);
        view.findViewById(R.id.time_picker_dialog).setBackgroundColor(mThemeDark ? darkBackgroundColor : backgroundColor);

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
        if (mOnDismissListener != null) mOnDismissListener.onDismiss(dialog);
    }

    public void tryVibrate() {
        mHapticFeedbackController.tryVibrate();
    }

    private void updateAmPmDisplay(int amOrPm) {
        if (amOrPm == AM) {
            mAmPmTextView.setText(mAmText);
            Utils.tryAccessibilityAnnounce(mTimePicker, mAmText);
            mAmPmHitSpace.setContentDescription(mAmText);
        } else if (amOrPm == PM) {
            mAmPmTextView.setText(mPmText);
            Utils.tryAccessibilityAnnounce(mTimePicker, mPmText);
            mAmPmHitSpace.setContentDescription(mPmText);
        } else {
            mAmPmTextView.setText(mDoublePlaceholderText);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (mTimePicker != null) {
            outState.putInt(KEY_HOUR_OF_DAY, mTimePicker.getHours());
            outState.putInt(KEY_MINUTE, mTimePicker.getMinutes());
            outState.putBoolean(KEY_IS_24_HOUR_VIEW, mIs24HourMode);
            outState.putInt(KEY_CURRENT_ITEM_SHOWING, mTimePicker.getCurrentItemShowing());
            outState.putBoolean(KEY_IN_KB_MODE, mInKbMode);
            if (mInKbMode) outState.putIntegerArrayList(KEY_TYPED_TIMES, mTypedTimes);
            outState.putString(KEY_TITLE, mTitle);
            outState.putBoolean(KEY_DARK_THEME, mThemeDark);
        }
    }

    @Override
    public void onValueSelected(int pickerIndex, int newValue, boolean autoAdvance) {
        if (pickerIndex == HOUR_INDEX) {
            setHour(newValue, false);
            String announcement = String.format(Locale.getDefault(), "%d", newValue);
            if (mAllowAutoAdvance && autoAdvance) {
                setCurrentItemShowing(MINUTE_INDEX, true, true, false);
                announcement += ". " + mSelectMinutes;
            } else mTimePicker.setContentDescription(mHourPickerDescription + ": " + newValue);

            Utils.tryAccessibilityAnnounce(mTimePicker, announcement);
        } else if (pickerIndex == MINUTE_INDEX) {
            setMinute(newValue);
            mTimePicker.setContentDescription(mMinutePickerDescription + ": " + newValue);
        } else if (pickerIndex == AMPM_INDEX)
            updateAmPmDisplay(newValue);
        else if (pickerIndex == ENABLE_PICKER_INDEX) {
            if (!isTypedTimeFullyLegal()) mTypedTimes.clear();
            finishKbMode(true);
        }
    }

    private void setHour(int value, boolean announce) {
        String format;
        if (mIs24HourMode) format = "%02d";
        else {
            format = "%d";
            value = value % 12;
            if (value == 0) value = 12;
        }

        String text = LanguageUtils.getPersianNumbers(String.format(Locale.getDefault(), format, value));
        mHourView.setText(text);
        mHourSpaceView.setText(text);
        if (announce) Utils.tryAccessibilityAnnounce(mTimePicker, text);
    }

    private void setMinute(int value) {
        if (value == 60) value = 0;
        CharSequence text = LanguageUtils.getPersianNumbers(String.format(Locale.getDefault(),
                "%02d", value));
        Utils.tryAccessibilityAnnounce(mTimePicker, text);
        mMinuteView.setText(text);
        mMinuteSpaceView.setText(text);
    }


    private void setCurrentItemShowing(int index, boolean animateCircle, boolean delayLabelAnimate,
                                       boolean announce) {
        mTimePicker.setCurrentItemShowing(index, animateCircle);

        TextView labelToAnimate;
        if (index == HOUR_INDEX) {
            int hours = mTimePicker.getHours();
            if (!mIs24HourMode) {
                hours = hours % 12;
            }
            mTimePicker.setContentDescription(mHourPickerDescription + ": " + hours);
            if (announce) {
                Utils.tryAccessibilityAnnounce(mTimePicker, mSelectHours);
            }
            labelToAnimate = mHourView;
        } else {
            int minutes = mTimePicker.getMinutes();
            mTimePicker.setContentDescription(mMinutePickerDescription + ": " + minutes);
            if (announce) {
                Utils.tryAccessibilityAnnounce(mTimePicker, mSelectMinutes);
            }
            labelToAnimate = mMinuteView;
        }

        int hourColor = (index == HOUR_INDEX) ? mSelectedColor : mUnselectedColor;
        int minuteColor = (index == MINUTE_INDEX) ? mSelectedColor : mUnselectedColor;
        mHourView.setTextColor(hourColor);
        mMinuteView.setTextColor(minuteColor);

        ObjectAnimator pulseAnimator = Utils.getPulseAnimator(labelToAnimate, 0.85f, 1.1f);
        if (delayLabelAnimate)
            pulseAnimator.setStartDelay(PULSE_ANIMATOR_DELAY);
        pulseAnimator.start();
    }


    private boolean processKeyUp(int keyCode) {
        if (keyCode == KeyEvent.KEYCODE_ESCAPE || keyCode == KeyEvent.KEYCODE_BACK) {
            if (isCancelable()) dismiss();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_TAB) {
            if (mInKbMode) {
                if (isTypedTimeFullyLegal()) finishKbMode(true);
                return true;
            }
        } else if (keyCode == KeyEvent.KEYCODE_ENTER) {
            if (mInKbMode) {
                if (!isTypedTimeFullyLegal()) return true;
                finishKbMode(false);
            }
            if (mCallback != null)
                mCallback.onTimeSet(mTimePicker, mTimePicker.getHours(), mTimePicker.getMinutes());
            dismiss();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_DEL) {
            if (mInKbMode) {
                if (!mTypedTimes.isEmpty()) {
                    int deleted = deleteLastTypedKey();
                    String deletedKeyStr;
                    if (deleted == getAmOrPmKeyCode(AM)) {
                        deletedKeyStr = mAmText;
                    } else if (deleted == getAmOrPmKeyCode(PM)) {
                        deletedKeyStr = mPmText;
                    } else {
                        deletedKeyStr = String.format(Locale.getDefault(), "%d", getValFromKeyCode(deleted));
                    }
                    Utils.tryAccessibilityAnnounce(mTimePicker,
                            String.format(mDeletedKeyFormat, deletedKeyStr));
                    updateDisplay(true);
                }
            }
        } else if (keyCode == KeyEvent.KEYCODE_0 || keyCode == KeyEvent.KEYCODE_1
                || keyCode == KeyEvent.KEYCODE_2 || keyCode == KeyEvent.KEYCODE_3
                || keyCode == KeyEvent.KEYCODE_4 || keyCode == KeyEvent.KEYCODE_5
                || keyCode == KeyEvent.KEYCODE_6 || keyCode == KeyEvent.KEYCODE_7
                || keyCode == KeyEvent.KEYCODE_8 || keyCode == KeyEvent.KEYCODE_9
                || (!mIs24HourMode &&
                (keyCode == getAmOrPmKeyCode(AM) || keyCode == getAmOrPmKeyCode(PM)))) {
            if (!mInKbMode) {
                if (mTimePicker == null) return true;
                mTypedTimes.clear();
                tryStartingKbMode(keyCode);
                return true;
            }

            if (addKeyIfLegal(keyCode)) {
                updateDisplay(false);
            }
            return true;
        }
        return false;
    }


    private void tryStartingKbMode(int keyCode) {
        if (mTimePicker.trySettingInputEnabled(false) &&
                (keyCode == -1 || addKeyIfLegal(keyCode))) {
            mInKbMode = true;
            mOkButton.setEnabled(false);
            updateDisplay(false);
        }
    }

    private boolean addKeyIfLegal(int keyCode) {


        if ((mIs24HourMode && mTypedTimes.size() == 4) ||
                (!mIs24HourMode && isTypedTimeFullyLegal())) {
            return false;
        }

        mTypedTimes.add(keyCode);
        if (!isTypedTimeLegalSoFar()) {
            deleteLastTypedKey();
            return false;
        }

        int val = getValFromKeyCode(keyCode);
        Utils.tryAccessibilityAnnounce(mTimePicker, String.format(Locale.getDefault(), "%d", val));

        if (isTypedTimeFullyLegal()) {
            if (!mIs24HourMode && mTypedTimes.size() <= 3) {
                mTypedTimes.add(mTypedTimes.size() - 1, KeyEvent.KEYCODE_0);
                mTypedTimes.add(mTypedTimes.size() - 1, KeyEvent.KEYCODE_0);
            }
            mOkButton.setEnabled(true);
        }

        return true;
    }


    private boolean isTypedTimeLegalSoFar() {
        Node node = mLegalTimesTree;
        for (int keyCode : mTypedTimes) {
            node = node.canReach(keyCode);
            if (node == null) {
                return false;
            }
        }
        return true;
    }


    private boolean isTypedTimeFullyLegal() {
        if (mIs24HourMode) {


            int[] values = getEnteredTime(null);
            return (values[0] >= 0 && values[1] >= 0 && values[1] < 60);
        } else {


            return (mTypedTimes.contains(getAmOrPmKeyCode(AM)) ||
                    mTypedTimes.contains(getAmOrPmKeyCode(PM)));
        }
    }

    private int deleteLastTypedKey() {
        int deleted = mTypedTimes.remove(mTypedTimes.size() - 1);
        if (!isTypedTimeFullyLegal()) {
            mOkButton.setEnabled(false);
        }
        return deleted;
    }


    private void finishKbMode(boolean updateDisplays) {
        mInKbMode = false;
        if (!mTypedTimes.isEmpty()) {
            int[] values = getEnteredTime(null);
            mTimePicker.setTime(values[0], values[1]);
            if (!mIs24HourMode) {
                mTimePicker.setAmOrPm(values[2]);
            }
            mTypedTimes.clear();
        }
        if (updateDisplays) {
            updateDisplay(false);
            mTimePicker.trySettingInputEnabled(true);
        }
    }


    private void updateDisplay(boolean allowEmptyDisplay) {
        if (!allowEmptyDisplay && mTypedTimes.isEmpty()) {
            int hour = mTimePicker.getHours();
            int minute = mTimePicker.getMinutes();
            setHour(hour, true);
            setMinute(minute);
            if (!mIs24HourMode) {
                updateAmPmDisplay(hour < 12 ? AM : PM);
            }
            setCurrentItemShowing(mTimePicker.getCurrentItemShowing(), true, true, true);
            mOkButton.setEnabled(true);
        } else {
            Boolean[] enteredZeros = {false, false};
            int[] values = getEnteredTime(enteredZeros);
            String hourFormat = enteredZeros[0] ? "%02d" : "%2d";
            String minuteFormat = (enteredZeros[1]) ? "%02d" : "%2d";
            String hourStr = (values[0] == -1) ? mDoublePlaceholderText :
                    String.format(hourFormat, values[0]).replace(' ', mPlaceholderText);
            String minuteStr = (values[1] == -1) ? mDoublePlaceholderText :
                    String.format(minuteFormat, values[1]).replace(' ', mPlaceholderText);
            mHourView.setText(LanguageUtils.getPersianNumbers(hourStr));
            mHourSpaceView.setText(LanguageUtils.getPersianNumbers(hourStr));
            mHourView.setTextColor(mUnselectedColor);
            mMinuteView.setText(LanguageUtils.getPersianNumbers(minuteStr));
            mMinuteSpaceView.setText(LanguageUtils.getPersianNumbers(minuteStr));
            mMinuteView.setTextColor(mUnselectedColor);
            if (!mIs24HourMode) {
                updateAmPmDisplay(values[2]);
            }
        }
    }

    private static int getValFromKeyCode(int keyCode) {
        if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9)
            return keyCode - KeyEvent.KEYCODE_0;
        else return -1;
    }


    private int[] getEnteredTime(Boolean[] enteredZeros) {
        int amOrPm = -1, startIndex = 1;
        if (!mIs24HourMode && isTypedTimeFullyLegal()) {
            int keyCode = mTypedTimes.get(mTypedTimes.size() - 1);
            if (keyCode == getAmOrPmKeyCode(AM))
                amOrPm = AM;
            else if (keyCode == getAmOrPmKeyCode(PM))
                amOrPm = PM;
            startIndex = 2;
        }
        int minute = -1, hour = -1;
        for (int i = startIndex; i <= mTypedTimes.size(); i++) {
            int val = getValFromKeyCode(mTypedTimes.get(mTypedTimes.size() - i));
            if (i == startIndex)
                minute = val;
            else if (i == startIndex + 1) {
                minute += 10 * val;
                if (enteredZeros != null && val == 0) {
                    enteredZeros[1] = true;
                }
            } else if (i == startIndex + 2)
                hour = val;
            else if (i == startIndex + 3) {
                hour += 10 * val;
                if (enteredZeros != null && val == 0)
                    enteredZeros[0] = true;
            }
        }
        return new int[]{hour, minute, amOrPm};
    }


    private int getAmOrPmKeyCode(int amOrPm) {
        if (mAmKeyCode == -1 || mPmKeyCode == -1) {
            KeyCharacterMap kcm = KeyCharacterMap.load(KeyCharacterMap.VIRTUAL_KEYBOARD);
            char amChar;
            char pmChar;
            for (int i = 0; i < Math.max(mAmText.length(), mPmText.length()); i++) {
                amChar = "AM".toLowerCase(Locale.getDefault()).charAt(i);
                pmChar = "PM".toLowerCase(Locale.getDefault()).charAt(i);
                if (amChar != pmChar) {
                    KeyEvent[] events = kcm.getEvents(new char[]{amChar, pmChar});
                    if (events != null && events.length == 4) {
                        mAmKeyCode = events[0].getKeyCode();
                        mPmKeyCode = events[2].getKeyCode();
                    }
                    break;
                }
            }
        }
        if (amOrPm == AM) return mAmKeyCode;
        else if (amOrPm == PM) return mPmKeyCode;
        return -1;
    }

    private void generateLegalTimesTree() {
        int k0 = KeyEvent.KEYCODE_0;
        int k1 = KeyEvent.KEYCODE_1;
        int k2 = KeyEvent.KEYCODE_2;
        int k3 = KeyEvent.KEYCODE_3;
        int k4 = KeyEvent.KEYCODE_4;
        int k5 = KeyEvent.KEYCODE_5;
        int k6 = KeyEvent.KEYCODE_6;
        int k7 = KeyEvent.KEYCODE_7;
        int k8 = KeyEvent.KEYCODE_8;
        int k9 = KeyEvent.KEYCODE_9;

        mLegalTimesTree = new Node();
        if (mIs24HourMode) {
            Node minuteFirstDigit = new Node(k0, k1, k2, k3, k4, k5);

            Node minuteSecondDigit = new Node(k0, k1, k2, k3, k4, k5, k6, k7, k8, k9);
            minuteFirstDigit.addChild(minuteSecondDigit);

            Node firstDigit = new Node(k0, k1);
            mLegalTimesTree.addChild(firstDigit);

            Node secondDigit = new Node(k0, k1, k2, k3, k4, k5);
            firstDigit.addChild(secondDigit);
            secondDigit.addChild(minuteFirstDigit);

            Node thirdDigit = new Node(k6, k7, k8, k9);
            secondDigit.addChild(thirdDigit);

            secondDigit = new Node(k6, k7, k8, k9);
            firstDigit.addChild(secondDigit);
            secondDigit.addChild(minuteFirstDigit);

            firstDigit = new Node(k2);
            mLegalTimesTree.addChild(firstDigit);

            secondDigit = new Node(k0, k1, k2, k3);
            firstDigit.addChild(secondDigit);
            secondDigit.addChild(minuteFirstDigit);

            secondDigit = new Node(k4, k5);
            firstDigit.addChild(secondDigit);
            secondDigit.addChild(minuteSecondDigit);

            firstDigit = new Node(k3, k4, k5, k6, k7, k8, k9);
            mLegalTimesTree.addChild(firstDigit);
            firstDigit.addChild(minuteFirstDigit);
        } else {
            Node ampm = new Node(getAmOrPmKeyCode(AM), getAmOrPmKeyCode(PM));

            Node firstDigit = new Node(k1);
            mLegalTimesTree.addChild(firstDigit);
            firstDigit.addChild(ampm);

            Node secondDigit = new Node(k0, k1, k2);
            firstDigit.addChild(secondDigit);
            secondDigit.addChild(ampm);

            Node thirdDigit = new Node(k0, k1, k2, k3, k4, k5);
            secondDigit.addChild(thirdDigit);
            thirdDigit.addChild(ampm);

            Node fourthDigit = new Node(k0, k1, k2, k3, k4, k5, k6, k7, k8, k9);
            thirdDigit.addChild(fourthDigit);
            fourthDigit.addChild(ampm);

            thirdDigit = new Node(k6, k7, k8, k9);
            secondDigit.addChild(thirdDigit);
            thirdDigit.addChild(ampm);

            secondDigit = new Node(k3, k4, k5);
            firstDigit.addChild(secondDigit);

            thirdDigit = new Node(k0, k1, k2, k3, k4, k5, k6, k7, k8, k9);
            secondDigit.addChild(thirdDigit);
            thirdDigit.addChild(ampm);

            firstDigit = new Node(k2, k3, k4, k5, k6, k7, k8, k9);
            mLegalTimesTree.addChild(firstDigit);
            firstDigit.addChild(ampm);

            secondDigit = new Node(k0, k1, k2, k3, k4, k5);
            firstDigit.addChild(secondDigit);

            thirdDigit = new Node(k0, k1, k2, k3, k4, k5, k6, k7, k8, k9);
            secondDigit.addChild(thirdDigit);
            thirdDigit.addChild(ampm);
        }
    }

    private static class Node {
        private final int[] mLegalKeys;
        private final ArrayList<Node> mChildren;

        public Node(int... legalKeys) {
            mLegalKeys = legalKeys;
            mChildren = new ArrayList<>();
        }

        public void addChild(Node child) {
            mChildren.add(child);
        }

        public boolean containsKey(int key) {
            for (int mLegalKey : mLegalKeys) if (mLegalKey == key) return true;
            return false;
        }

        public Node canReach(int key) {
            for (Node child : mChildren) if (child.containsKey(key)) return child;
            return null;
        }
    }

    private class KeyboardListener implements OnKeyListener {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getAction() == KeyEvent.ACTION_UP)
                return processKeyUp(keyCode);
            return false;
        }
    }
}
