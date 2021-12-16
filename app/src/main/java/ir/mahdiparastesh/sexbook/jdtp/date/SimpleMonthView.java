package ir.mahdiparastesh.sexbook.jdtp.date;

import static ir.mahdiparastesh.sexbook.Fun.font1;
import static ir.mahdiparastesh.sexbook.Fun.font1Bold;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

import java.util.Locale;

import ir.mahdiparastesh.sexbook.jdtp.utils.LanguageUtils;

public class SimpleMonthView extends MonthView {
    final DatePickerController controller;

    public SimpleMonthView(Context c, AttributeSet attr, DatePickerController controller) {
        super(c, attr, controller);
        this.controller = controller;
    }

    @Override
    public void drawMonthDay(Canvas canvas, int year, int month, int day,
                             int x, int y, int startX, int stopX, int startY, int stopY) {
        if (mSelectedDay == day) {
            canvas.drawCircle(x, y - (MINI_DAY_NUMBER_TEXT_SIZE / 3f), DAY_SELECTED_CIRCLE_SIZE,
                    mSelectedCirclePaint);
        }
        if (isHighlighted(year, month, day))
            mMonthNumPaint.setTypeface(font1Bold);
        else mMonthNumPaint.setTypeface(font1);

        if (isOutOfRange(year, month, day))
            mMonthNumPaint.setColor(mDisabledDayTextColor);
        else if (mSelectedDay == day)
            mMonthNumPaint.setColor(mSelectedDayTextColor);
        else if (mHasToday && mToday == day)
            mMonthNumPaint.setColor(mTodayNumberColor);
        else
            mMonthNumPaint.setColor(isHighlighted(year, month, day)
                    ? mHighlightedDayTextColor : mDayTextColor);

        canvas.drawText(LanguageUtils.getPersianNumbers(String.format(Locale.getDefault(),
                "%d", day)), x, y, mMonthNumPaint);
    }
}
