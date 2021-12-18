package ir.mahdiparastesh.sexbook.jdtp.date;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;

import ir.mahdiparastesh.sexbook.R;

public class TextViewWithCircularIndicator extends AppCompatTextView {

    private static final int SELECTED_CIRCLE_ALPHA = 255;

    final Paint mCirclePaint = new Paint();

    private final int mCircleColor;
    private final String mItemIsSelectedText;

    private boolean mDrawCircle;

    public TextViewWithCircularIndicator(Context c, AttributeSet attrs) {
        super(c, attrs);
        Resources res = c.getResources();
        mCircleColor = ContextCompat.getColor(c, R.color.jdtp_accent_color);
        int mRadius = res.getDimensionPixelOffset(R.dimen.jdtp_month_select_circle_radius);
        mItemIsSelectedText = c.getResources().getString(R.string.jdtp_item_is_selected);

        init();
    }

    private void init() {
        mCirclePaint.setFakeBoldText(true);
        mCirclePaint.setAntiAlias(true);
        mCirclePaint.setColor(mCircleColor);
        mCirclePaint.setTextAlign(Align.CENTER);
        mCirclePaint.setStyle(Style.FILL);
        mCirclePaint.setAlpha(SELECTED_CIRCLE_ALPHA);
    }

    public void drawIndicator(boolean drawCircle) {
        mDrawCircle = drawCircle;
    }

    @Override
    public void onDraw(@NonNull Canvas canvas) {
        if (mDrawCircle) {
            final int width = getWidth();
            final int height = getHeight();
            int radius = Math.min(width, height) / 2;
            canvas.drawCircle(width / 2f, height / 2f, radius, mCirclePaint);
        }
        setSelected(mDrawCircle);
        super.onDraw(canvas);
    }

    /*@Override
    public CharSequence getContentDescription() {
        String itemText = LanguageUtils.getPersianNumbers(getText().toString());
        if (mDrawCircle)
            return String.format(mItemIsSelectedText, itemText);
        else return itemText;
    }*/
}
