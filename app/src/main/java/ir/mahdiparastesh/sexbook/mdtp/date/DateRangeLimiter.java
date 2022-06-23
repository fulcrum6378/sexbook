package ir.mahdiparastesh.sexbook.mdtp.date;

import android.icu.util.Calendar;
import android.os.Parcelable;

import androidx.annotation.NonNull;

@SuppressWarnings("WeakerAccess")
public interface DateRangeLimiter<CAL extends Calendar> extends Parcelable {

    default int getMinYear() {
        return getStartDate().get(Calendar.YEAR);
    }

    default int getMaxYear() {
        return getEndDate().get(Calendar.YEAR);
    }

    @NonNull
    CAL getStartDate();

    @NonNull
    CAL getEndDate();

    boolean isOutOfRange(int year, int month, int day);

    @NonNull
    CAL setToNearestDate(@NonNull CAL day);
}
