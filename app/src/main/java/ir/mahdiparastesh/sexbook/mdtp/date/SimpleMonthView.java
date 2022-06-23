package ir.mahdiparastesh.sexbook.mdtp.date;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.icu.util.Calendar;
import android.util.AttributeSet;

import ir.mahdiparastesh.sexbook.mdtp.Utils;

@SuppressLint("ViewConstructor")
public class SimpleMonthView<CAL extends Calendar> extends MonthView<CAL> {

    public SimpleMonthView(Context context, AttributeSet attr, DatePickerController<CAL> controller) {
        super(context, attr, controller);
    }

    @Override
    public void drawMonthDay(Canvas canvas, int year, int month, int day, int x, int y) {
        if (mSelectedDay == day)
            canvas.drawCircle(x, y - (MINI_DAY_NUMBER_TEXT_SIZE / 3f), DAY_SELECTED_CIRCLE_SIZE,
                    mSelectedCirclePaint);

        if (isHighlighted(year, month, day) && mSelectedDay != day) {
            canvas.drawCircle(x, y + MINI_DAY_NUMBER_TEXT_SIZE - DAY_HIGHLIGHT_CIRCLE_MARGIN,
                    DAY_HIGHLIGHT_CIRCLE_SIZE, mSelectedCirclePaint);
            mMonthNumPaint.setTypeface(Utils.mdtpDayOfMonth(getContext(), true));
        } else mMonthNumPaint.setTypeface(Utils.mdtpDayOfMonth(getContext(), false));

        if (mController.isOutOfRange(year, month, day)) {
            mMonthNumPaint.setColor(mDisabledDayTextColor);
        } else if (mSelectedDay == day) {
            mMonthNumPaint.setTypeface(Utils.mdtpDayOfMonth(getContext(), true));
            mMonthNumPaint.setColor(mSelectedDayTextColor);
        } else if (mHasToday && mToday == day) {
            mMonthNumPaint.setColor(mTodayNumberColor);
        } else {
            mMonthNumPaint.setColor(isHighlighted(year, month, day) ? mHighlightedDayTextColor : mDayTextColor);
        }

        canvas.drawText(String.format(mController.getLocale(), "%d", day), x, y, mMonthNumPaint);
    }
}
