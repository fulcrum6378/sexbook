package ir.mahdiparastesh.sexbook.jdtp.time;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

import androidx.core.content.ContextCompat;

import ir.mahdiparastesh.sexbook.R;

public class CircleView extends View {
    private final Paint mPaint = new Paint();
    private boolean mIs24HourMode;
    private int mCircleColor;
    private int mDotColor;
    private float mCircleRadiusMultiplier;
    private float mAmPmCircleRadiusMultiplier;
    private boolean mIsInitialized;

    private boolean mDrawValuesReady;
    private int mXCenter;
    private int mYCenter;
    private int mCircleRadius;

    public CircleView(Context c) {
        super(c);

        mCircleColor = ContextCompat.getColor(c, R.color.jdtp_circle_color);
        mDotColor = ContextCompat.getColor(c, R.color.jdtp_numbers_text_color);
        mPaint.setAntiAlias(true);

        mIsInitialized = false;
    }

    public void initialize(Context c, boolean is24HourMode) {
        if (mIsInitialized) return;

        Resources res = c.getResources();
        mIs24HourMode = is24HourMode;
        if (is24HourMode) {
            mCircleRadiusMultiplier = Float.parseFloat(
                    res.getString(R.string.jdtp_circle_radius_multiplier_24HourMode));
        } else {
            mCircleRadiusMultiplier = Float.parseFloat(
                    res.getString(R.string.jdtp_circle_radius_multiplier));
            mAmPmCircleRadiusMultiplier =
                    Float.parseFloat(res.getString(R.string.jdtp_ampm_circle_radius_multiplier));
        }

        mIsInitialized = true;
    }

    void setTheme(Context c, boolean dark) {
        if (dark) {
            mCircleColor = ContextCompat.getColor(c, R.color.jdtp_circle_background_dark_theme);
            mDotColor = ContextCompat.getColor(c, R.color.jdtp_white);
        } else {
            mCircleColor = ContextCompat.getColor(c, R.color.jdtp_circle_color);
            mDotColor = ContextCompat.getColor(c, R.color.jdtp_numbers_text_color);
        }
    }


    @Override
    public void onDraw(Canvas canvas) {
        int viewWidth = getWidth();
        if (viewWidth == 0 || !mIsInitialized) return;

        if (!mDrawValuesReady) {
            mXCenter = getWidth() / 2;
            mYCenter = getHeight() / 2;
            mCircleRadius = (int) (Math.min(mXCenter, mYCenter) * mCircleRadiusMultiplier);

            if (!mIs24HourMode)
                mYCenter -= (int) (mCircleRadius * mAmPmCircleRadiusMultiplier) * 0.75;

            mDrawValuesReady = true;
        }

        mPaint.setColor(mCircleColor);
        canvas.drawCircle(mXCenter, mYCenter, mCircleRadius, mPaint);

        mPaint.setColor(mDotColor);
        canvas.drawCircle(mXCenter, mYCenter, 4, mPaint);
    }
}
