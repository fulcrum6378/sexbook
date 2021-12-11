package ir.mahdiparastesh.sexbook.jdtp.multidate;

import static ir.mahdiparastesh.sexbook.Fun.font1;
import static ir.mahdiparastesh.sexbook.Fun.font1Bold;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

import java.util.Locale;

import ir.mahdiparastesh.sexbook.jdtp.utils.LanguageUtils;

public class SimpleMonthView extends MonthView {

    public SimpleMonthView(Context c, AttributeSet attr, DatePickerController controller) {
        super(c, attr, controller);
    }

    @Override
    public void drawMonthDay(Canvas canvas, int year, int month, int day,
                             int x, int y, int startX, int stopX, int startY, int stopY) {
        boolean flag = false;
        for (int selectedDays : mSelectedDays)
            if (day == selectedDays) {
                canvas.drawCircle(x, y - (MINI_DAY_NUMBER_TEXT_SIZE / 3f),
                        DAY_SELECTED_CIRCLE_SIZE,
                        mSelectedCirclePaint);
                flag = true;
                break;
            }

        if (isHighlighted(year, month, day))
            mMonthNumPaint.setTypeface(font1Bold);
        else mMonthNumPaint.setTypeface(font1);

        if (isOutOfRange(year, month, day))
            mMonthNumPaint.setColor(mDisabledDayTextColor);
        else if (flag)
            mMonthNumPaint.setColor(mSelectedDayTextColor);
        else if (mHasToday && mToday == day)
            mMonthNumPaint.setColor(mTodayNumberColor);
        else
            mMonthNumPaint.setColor(isHighlighted(year, month, day)
                    ? mHighlightedDayTextColor : mDayTextColor);

        canvas.drawText(LanguageUtils.getPersianNumbers(String.format(
                Locale.getDefault(), "%d", day)), x, y, mMonthNumPaint);
    }
}
