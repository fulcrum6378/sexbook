package ir.mahdiparastesh.sexbook.mdtp.gdate;

import android.os.Parcelable;
import androidx.annotation.NonNull;

import java.util.Calendar;

@SuppressWarnings("WeakerAccess")
public interface DateRangeLimiter extends Parcelable {

    default int getMinYear() {
        return getStartDate().get(Calendar.YEAR);
    }

    default int getMaxYear() {
        return getEndDate().get(Calendar.YEAR);
    }

    @NonNull Calendar getStartDate();

    @NonNull Calendar getEndDate();

    boolean isOutOfRange(int year, int month, int day);

    @NonNull Calendar setToNearestDate(@NonNull Calendar day);
}
