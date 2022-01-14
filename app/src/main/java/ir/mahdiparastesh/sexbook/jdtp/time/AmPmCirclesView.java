package ir.mahdiparastesh.sexbook.jdtp.time;

import static ir.mahdiparastesh.sexbook.more.BaseActivity.jdtpFont;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.view.View;

import androidx.core.content.ContextCompat;

import ir.mahdiparastesh.sexbook.R;
import ir.mahdiparastesh.sexbook.jdtp.Utils;

public class AmPmCirclesView extends View {
    private static final int SELECTED_ALPHA = Utils.SELECTED_ALPHA;
    private static final int SELECTED_ALPHA_THEME_DARK = Utils.SELECTED_ALPHA_THEME_DARK;

    private final Paint mPaint = new Paint();
    private int mSelectedAlpha;
    private int mTouchedColor;
    private int mUnselectedColor;
    private int mAmPmTextColor;
    private int mAmPmSelectedTextColor;
    private int mSelectedColor;
    private float mCircleRadiusMultiplier;
    private float mAmPmCircleRadiusMultiplier;
    private String mAmText;
    private String mPmText;
    private boolean mIsInitialized;

    private static final int AM = TimePickerDialog.AM, PM = TimePickerDialog.PM;

    private boolean mDrawValuesReady;
    private int mAmPmCircleRadius;
    private int mAmXCenter;
    private int mPmXCenter;
    private int mAmPmYCenter;
    private int mAmOrPm;
    private int mAmOrPmPressed;

    public AmPmCirclesView(Context c) {
        super(c);
        mIsInitialized = false;
    }

    public void initialize(Context c, int amOrPm) {
        if (mIsInitialized) return;

        Resources res = c.getResources();
        mUnselectedColor = ContextCompat.getColor(c, R.color.jdtp_white);
        mSelectedColor = ContextCompat.getColor(c, R.color.jdtp_accent_color);
        mTouchedColor = ContextCompat.getColor(c, R.color.jdtp_accent_color_dark);
        mAmPmTextColor = ContextCompat.getColor(c, R.color.jdtp_ampm_text_color);
        mAmPmSelectedTextColor = ContextCompat.getColor(c, R.color.jdtp_white);
        mSelectedAlpha = SELECTED_ALPHA;
        mPaint.setTypeface(jdtpFont);
        mPaint.setAntiAlias(true);
        mPaint.setTextAlign(Align.CENTER);

        mCircleRadiusMultiplier =
                Float.parseFloat(res.getString(R.string.jdtp_circle_radius_multiplier));
        mAmPmCircleRadiusMultiplier =
                Float.parseFloat(res.getString(R.string.jdtp_ampm_circle_radius_multiplier));
        mAmText = getContext().getString(R.string.am_label);
        mPmText = getContext().getString(R.string.pm_label);

        setAmOrPm(amOrPm);
        mAmOrPmPressed = -1;

        mIsInitialized = true;
    }

    void setTheme(Context c, boolean themeDark) {
        if (themeDark) {
            mUnselectedColor = ContextCompat.getColor(c,
                    R.color.jdtp_circle_background_dark_theme);
            mSelectedColor = ContextCompat.getColor(c, R.color.jdtp_red);
            mAmPmTextColor = ContextCompat.getColor(c, R.color.jdtp_white);
            mSelectedAlpha = SELECTED_ALPHA_THEME_DARK;
        } else {
            mUnselectedColor = ContextCompat.getColor(c, R.color.jdtp_white);
            mSelectedColor = ContextCompat.getColor(c, R.color.jdtp_accent_color);
            mAmPmTextColor = ContextCompat.getColor(c, R.color.jdtp_ampm_text_color);
            mSelectedAlpha = SELECTED_ALPHA;
        }
    }

    public void setAmOrPm(int amOrPm) {
        mAmOrPm = amOrPm;
    }

    public void setAmOrPmPressed(int amOrPmPressed) {
        mAmOrPmPressed = amOrPmPressed;
    }

    public int getIsTouchingAmOrPm(float xCor, float yCor) {
        if (!mDrawValuesReady) return -1;

        int squaredYDistance = (int) ((yCor - mAmPmYCenter) * (yCor - mAmPmYCenter));

        int distanceToAmCenter = (int) Math.sqrt((xCor - mAmXCenter)
                * (xCor - mAmXCenter) + squaredYDistance);
        if (distanceToAmCenter <= mAmPmCircleRadius) return AM;

        int distanceToPmCenter = (int) Math.sqrt((xCor - mPmXCenter)
                * (xCor - mPmXCenter) + squaredYDistance);
        if (distanceToPmCenter <= mAmPmCircleRadius) return PM;

        return -1;
    }

    @Override
    public void onDraw(Canvas canvas) {
        int viewWidth = getWidth();
        if (viewWidth == 0 || !mIsInitialized) return;

        if (!mDrawValuesReady) {
            int layoutXCenter = getWidth() / 2;
            int layoutYCenter = getHeight() / 2;
            int circleRadius = (int) (Math.min(layoutXCenter, layoutYCenter)
                    * mCircleRadiusMultiplier);
            mAmPmCircleRadius = (int) (circleRadius * mAmPmCircleRadiusMultiplier);
            layoutYCenter += mAmPmCircleRadius * 0.75;
            int textSize = mAmPmCircleRadius * 3 / 4;
            mPaint.setTextSize(textSize);

            mAmPmYCenter = layoutYCenter - mAmPmCircleRadius / 2 + circleRadius;
            mAmXCenter = layoutXCenter - circleRadius + mAmPmCircleRadius;
            mPmXCenter = layoutXCenter + circleRadius - mAmPmCircleRadius;

            mDrawValuesReady = true;
        }

        int amColor = mUnselectedColor;
        int amAlpha = 255;
        int amTextColor = mAmPmTextColor;
        int pmColor = mUnselectedColor;
        int pmAlpha = 255;
        int pmTextColor = mAmPmTextColor;

        if (mAmOrPm == AM) {
            amColor = mSelectedColor;
            amAlpha = mSelectedAlpha;
            amTextColor = mAmPmSelectedTextColor;
        } else if (mAmOrPm == PM) {
            pmColor = mSelectedColor;
            pmAlpha = mSelectedAlpha;
            pmTextColor = mAmPmSelectedTextColor;
        }
        if (mAmOrPmPressed == AM) {
            amColor = mTouchedColor;
            amAlpha = mSelectedAlpha;
        } else if (mAmOrPmPressed == PM) {
            pmColor = mTouchedColor;
            pmAlpha = mSelectedAlpha;
        }

        mPaint.setColor(amColor);
        mPaint.setAlpha(amAlpha);
        canvas.drawCircle(mAmXCenter, mAmPmYCenter, mAmPmCircleRadius, mPaint);
        mPaint.setColor(pmColor);
        mPaint.setAlpha(pmAlpha);
        canvas.drawCircle(mPmXCenter, mAmPmYCenter, mAmPmCircleRadius, mPaint);

        mPaint.setColor(amTextColor);
        int textYCenter = mAmPmYCenter - (int) (mPaint.descent() + mPaint.ascent()) / 2;
        canvas.drawText(mAmText, mAmXCenter, textYCenter, mPaint);
        mPaint.setColor(pmTextColor);
        canvas.drawText(mPmText, mPmXCenter, textYCenter, mPaint);
        mPaint.setTypeface(jdtpFont);
    }
}
