package ir.mahdiparastesh.sexbook.mdtp.date;

import android.icu.util.Calendar;
import android.icu.util.TimeZone;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashSet;
import java.util.TreeSet;

import ir.mahdiparastesh.sexbook.mdtp.Utils;

@SuppressWarnings("unchecked")
class DefaultDateRangeLimiter<CAL extends Calendar> implements DateRangeLimiter<CAL> {
    private static final int YEAR_RANGE_RADIUS = 100;

    private Class<CAL> mCalendarType;
    private transient DatePickerController<CAL> mController;
    private int mMinYear;
    private int mMaxYear;
    private CAL mMinDate;
    private CAL mMaxDate;
    private TreeSet<CAL> selectableDays = new TreeSet<>();
    private HashSet<CAL> disabledDays = new HashSet<>();

    DefaultDateRangeLimiter(Class<CAL> calendarType) {
        mCalendarType = calendarType;
        int year = Utils.createCalendar(calendarType).get(Calendar.YEAR);
        mMinYear = year - YEAR_RANGE_RADIUS;
        mMaxYear = year + YEAR_RANGE_RADIUS;
    }

    @SuppressWarnings({"unchecked", "WeakerAccess"})
    public DefaultDateRangeLimiter(Parcel in) {
        mMinYear = in.readInt();
        mMaxYear = in.readInt();
        mMinDate = (CAL) in.readSerializable();
        mMaxDate = (CAL) in.readSerializable();
        selectableDays = (TreeSet<CAL>) in.readSerializable();
        disabledDays = (HashSet<CAL>) in.readSerializable();
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(mMinYear);
        out.writeInt(mMaxYear);
        out.writeSerializable(mMinDate);
        out.writeSerializable(mMaxDate);
        out.writeSerializable(selectableDays);
        out.writeSerializable(disabledDays);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @SuppressWarnings("WeakerAccess")
    public static final Parcelable.Creator<DefaultDateRangeLimiter<? extends Calendar>> CREATOR
            = new Parcelable.Creator<DefaultDateRangeLimiter<? extends Calendar>>() {
        public DefaultDateRangeLimiter<? extends Calendar> createFromParcel(Parcel in) {
            return new DefaultDateRangeLimiter<>(in);
        }

        public DefaultDateRangeLimiter<? extends Calendar>[] newArray(int size) {
            return new DefaultDateRangeLimiter<?>[size];
        }
    };

    void setSelectableDays(@NonNull CAL[] days) {
        for (CAL selectableDay : days)
            this.selectableDays.add(Utils.trimToMidnight((CAL) selectableDay.clone()));
    }

    void setDisabledDays(@NonNull CAL[] days) {
        for (CAL disabledDay : days)
            this.disabledDays.add(Utils.trimToMidnight((CAL) disabledDay.clone()));
    }

    void setMinDate(@NonNull CAL calendar) {
        mMinDate = Utils.trimToMidnight((CAL) calendar.clone());
    }

    void setMaxDate(@NonNull CAL calendar) {
        mMaxDate = Utils.trimToMidnight((CAL) calendar.clone());
    }

    void setController(@NonNull DatePickerController<CAL> controller) {
        mController = controller;
    }

    void setYearRange(int startYear, int endYear) {
        if (endYear < startYear)
            throw new IllegalArgumentException("Year end must be larger than or equal to year start");

        mMinYear = startYear;
        mMaxYear = endYear;
    }

    @Nullable
    CAL getMinDate() {
        return mMinDate;
    }

    @Nullable
    CAL getMaxDate() {
        return mMaxDate;
    }

    @Nullable
    CAL[] getSelectableDays() {
        return selectableDays.isEmpty() ? null : (CAL[]) selectableDays.toArray(new Calendar[0]);
    }

    @Nullable
    CAL[] getDisabledDays() {
        return disabledDays.isEmpty() ? null : (CAL[]) disabledDays.toArray(new Calendar[0]);
    }

    @Override
    public int getMinYear() {
        if (!selectableDays.isEmpty()) return selectableDays.first().get(Calendar.YEAR);
        // Ensure no years can be selected outside of the given minimum date
        return mMinDate != null && mMinDate.get(Calendar.YEAR) > mMinYear ? mMinDate.get(Calendar.YEAR) : mMinYear;
    }

    @Override
    public int getMaxYear() {
        if (!selectableDays.isEmpty()) return selectableDays.last().get(Calendar.YEAR);
        // Ensure no years can be selected outside of the given maximum date
        return mMaxDate != null && mMaxDate.get(Calendar.YEAR) < mMaxYear ? mMaxDate.get(Calendar.YEAR) : mMaxYear;
    }

    @Override
    public @NonNull
    CAL getStartDate() {
        if (!selectableDays.isEmpty()) return (CAL) selectableDays.first().clone();
        if (mMinDate != null) return (CAL) mMinDate.clone();
        TimeZone timeZone = mController == null ? TimeZone.getDefault() : mController.getTimeZone();
        CAL output = Utils.createCalendar(mCalendarType, timeZone);
        output.set(Calendar.YEAR, mMinYear);
        output.set(Calendar.DAY_OF_MONTH, 1);
        output.set(Calendar.MONTH, Calendar.JANUARY);
        return output;
    }

    @Override
    public @NonNull
    CAL getEndDate() {
        if (!selectableDays.isEmpty()) return (CAL) selectableDays.last().clone();
        if (mMaxDate != null) return (CAL) mMaxDate.clone();
        TimeZone timeZone = mController == null ? TimeZone.getDefault() : mController.getTimeZone();
        CAL output = Utils.createCalendar(mCalendarType, timeZone);
        output.set(Calendar.YEAR, mMaxYear);
        output.set(Calendar.DAY_OF_MONTH, 31);
        output.set(Calendar.MONTH, Calendar.DECEMBER);
        return output;
    }

    /**
     * @return true if the specified year/month/day are within the selectable days or the range set by minDate and maxDate.
     * If one or either have not been set, they are considered as Integer.MIN_VALUE and
     * Integer.MAX_VALUE.
     */
    @Override
    public boolean isOutOfRange(int year, int month, int day) {
        TimeZone timezone = mController == null ? TimeZone.getDefault() : mController.getTimeZone();
        CAL date = Utils.createCalendar(mCalendarType, timezone);
        date.set(Calendar.YEAR, year);
        date.set(Calendar.MONTH, month);
        date.set(Calendar.DAY_OF_MONTH, day);
        return isOutOfRange(date);
    }

    private boolean isOutOfRange(@NonNull CAL calendar) {
        Utils.trimToMidnight(calendar);
        return isDisabled(calendar) || !isSelectable(calendar);
    }

    private boolean isDisabled(@NonNull CAL c) {
        return disabledDays.contains(Utils.trimToMidnight(c)) || isBeforeMin(c) || isAfterMax(c);
    }

    private boolean isSelectable(@NonNull CAL c) {
        return selectableDays.isEmpty() || selectableDays.contains(Utils.trimToMidnight(c));
    }

    private boolean isBeforeMin(@NonNull CAL calendar) {
        return mMinDate != null && calendar.before(mMinDate) || calendar.get(Calendar.YEAR) < mMinYear;
    }

    private boolean isAfterMax(@NonNull CAL calendar) {
        return mMaxDate != null && calendar.after(mMaxDate) || calendar.get(Calendar.YEAR) > mMaxYear;
    }

    @Override
    public @NonNull
    CAL setToNearestDate(@NonNull CAL calendar) {
        if (!selectableDays.isEmpty()) {
            CAL newCalendar = null;
            CAL higher = selectableDays.ceiling(calendar);
            CAL lower = selectableDays.lower(calendar);

            if (higher == null && lower != null) newCalendar = lower;
            else if (lower == null && higher != null) newCalendar = higher;

            if (newCalendar != null || higher == null) {
                newCalendar = newCalendar == null ? calendar : newCalendar;
                TimeZone timeZone = mController == null ? TimeZone.getDefault() : mController.getTimeZone();
                newCalendar.setTimeZone(timeZone);
                return (CAL) newCalendar.clone();
            }

            long highDistance = Math.abs(higher.getTimeInMillis() - calendar.getTimeInMillis());
            long lowDistance = Math.abs(calendar.getTimeInMillis() - lower.getTimeInMillis());

            if (lowDistance < highDistance) return (CAL) lower.clone();
            else return (CAL) higher.clone();
        }

        if (!disabledDays.isEmpty()) {
            CAL forwardDate = isBeforeMin(calendar) ? getStartDate() : (CAL) calendar.clone();
            CAL backwardDate = isAfterMax(calendar) ? getEndDate() : (CAL) calendar.clone();
            while (isDisabled(forwardDate) && isDisabled(backwardDate)) {
                forwardDate.add(Calendar.DAY_OF_MONTH, 1);
                backwardDate.add(Calendar.DAY_OF_MONTH, -1);
            }
            if (!isDisabled(backwardDate)) return backwardDate;
            if (!isDisabled(forwardDate)) return forwardDate;
        }

        TimeZone timezone = mController == null ? TimeZone.getDefault() : mController.getTimeZone();
        if (isBeforeMin(calendar)) {
            if (mMinDate != null) return (CAL) mMinDate.clone();
            CAL output = Utils.createCalendar(mCalendarType, timezone);
            output.set(Calendar.YEAR, mMinYear);
            output.set(Calendar.MONTH, Calendar.JANUARY);
            output.set(Calendar.DAY_OF_MONTH, 1);
            return Utils.trimToMidnight(output);
        }

        if (isAfterMax(calendar)) {
            if (mMaxDate != null) return (CAL) mMaxDate.clone();
            CAL output = Utils.createCalendar(mCalendarType, timezone);
            output.set(Calendar.YEAR, mMaxYear);
            output.set(Calendar.MONTH, Calendar.DECEMBER);
            output.set(Calendar.DAY_OF_MONTH, 31);
            return Utils.trimToMidnight(output);
        }

        return calendar;
    }
}