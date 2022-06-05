package ir.mahdiparastesh.sexbook.mdtp.jdate;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

import java.util.Locale;

import ir.mahdiparastesh.sexbook.mdtp.Utils;

public class SimpleMonthView extends MonthView {

    public SimpleMonthView(Context context, AttributeSet attr, DatePickerController controller) {
        super(context, attr, controller);
    }

    public SimpleMonthView(Context context) {
        super(context);
    }

    @Override
    public void drawMonthDay(Canvas canvas, int year, int month, int day, int x, int y) {
        if (mSelectedDay == day)
            canvas.drawCircle(x, y - (MINI_DAY_NUMBER_TEXT_SIZE / 3f), DAY_SELECTED_CIRCLE_SIZE,
                    mSelectedCirclePaint);
        if (isHighlighted(year, month, day))
            mMonthNumPaint.setTypeface(Utils.mdtpFont(getContext(), true));
        else mMonthNumPaint.setTypeface(Utils.mdtpFont(getContext(), false));

        if (isOutOfRange(year, month, day))
            mMonthNumPaint.setColor(mDisabledDayTextColor);
        else if (mSelectedDay == day)
            mMonthNumPaint.setColor(mSelectedDayTextColor);
        else if (mHasToday && mToday == day)
            mMonthNumPaint.setColor(mTodayNumberColor);
        else
            mMonthNumPaint.setColor(isHighlighted(year, month, day)
                    ? mHighlightedDayTextColor : mDayTextColor);

        canvas.drawText(Utils.getPersianNumbers(String.format(Locale.getDefault(),
                "%d", day)), x, y, mMonthNumPaint);
    }
}
