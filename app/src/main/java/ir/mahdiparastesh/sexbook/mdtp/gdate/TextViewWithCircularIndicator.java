package ir.mahdiparastesh.sexbook.mdtp.gdate;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import ir.mahdiparastesh.sexbook.R;

public class TextViewWithCircularIndicator extends androidx.appcompat.widget.AppCompatTextView {

    private static final int SELECTED_CIRCLE_ALPHA = 255;

    final Paint mCirclePaint = new Paint();

    private int mCircleColor;
    private final String mItemIsSelectedText;

    private boolean mDrawCircle;

    public TextViewWithCircularIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        mCircleColor = ContextCompat.getColor(context, R.color.mdtp_accent_color);
        mItemIsSelectedText = context.getResources().getString(R.string.mdtp_item_is_selected);

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

    public void setAccentColor(int color, boolean darkMode) {
        mCircleColor = color;
        mCirclePaint.setColor(mCircleColor);
        setTextColor(createTextColor(color, darkMode));
    }

    private ColorStateList createTextColor(int accentColor, boolean darkMode) {
        int[][] states = new int[][]{
                new int[]{android.R.attr.state_pressed}, // pressed
                new int[]{android.R.attr.state_selected}, // selected
                new int[]{}
        };
        int[] colors = new int[]{
                accentColor,
                Color.WHITE,
                darkMode ? Color.WHITE : Color.BLACK
        };
        return new ColorStateList(states, colors);
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

    @SuppressLint("GetContentDescriptionOverride")
    @Override
    public CharSequence getContentDescription() {
        CharSequence itemText = getText();
        if (mDrawCircle)
            return String.format(mItemIsSelectedText, itemText);
        else return itemText;
    }
}
