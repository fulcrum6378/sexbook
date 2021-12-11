package ir.mahdiparastesh.sexbook.jdtp.time;

import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

import androidx.core.content.ContextCompat;

import ir.mahdiparastesh.sexbook.R;
import ir.mahdiparastesh.sexbook.jdtp.Utils;

public class RadialSelectorView extends View {
    private static final int SELECTED_ALPHA = Utils.SELECTED_ALPHA;
    private static final int SELECTED_ALPHA_THEME_DARK = Utils.SELECTED_ALPHA_THEME_DARK;
    private static final int FULL_ALPHA = Utils.FULL_ALPHA;

    private final Paint mPaint = new Paint();

    private boolean mIsInitialized;
    private boolean mDrawValuesReady;

    private float mCircleRadiusMultiplier;
    private float mAmPmCircleRadiusMultiplier;
    private float mInnerNumbersRadiusMultiplier;
    private float mOuterNumbersRadiusMultiplier;
    private float mNumbersRadiusMultiplier;
    private float mSelectionRadiusMultiplier;
    private float mAnimationRadiusMultiplier;
    private boolean mIs24HourMode;
    private boolean mHasInnerCircle;
    private int mSelectionAlpha;

    private int mXCenter;
    private int mYCenter;
    private int mCircleRadius;
    private float mTransitionMidRadiusMultiplier;
    private float mTransitionEndRadiusMultiplier;
    private int mLineLength;
    private int mSelectionRadius;
    private InvalidateUpdateListener mInvalidateUpdateListener;

    private int mSelectionDegrees;
    private double mSelectionRadians;
    private boolean mForceDrawDot;

    public RadialSelectorView(Context c) {
        super(c);
        mIsInitialized = false;
    }

    public void initialize(Context c, boolean is24HourMode, boolean hasInnerCircle,
                           boolean disappearsOut, int selectionDegrees, boolean isInnerCircle) {
        if (mIsInitialized) return;

        Resources res = c.getResources();

        int accentColor = ContextCompat.getColor(c, R.color.jdtp_accent_color);
        mPaint.setColor(accentColor);
        mPaint.setAntiAlias(true);
        mSelectionAlpha = SELECTED_ALPHA;

        mIs24HourMode = is24HourMode;
        if (is24HourMode) mCircleRadiusMultiplier = Float.parseFloat(
                res.getString(R.string.jdtp_circle_radius_multiplier_24HourMode));
        else {
            mCircleRadiusMultiplier = Float.parseFloat(
                    res.getString(R.string.jdtp_circle_radius_multiplier));
            mAmPmCircleRadiusMultiplier = Float.parseFloat(
                    res.getString(R.string.jdtp_ampm_circle_radius_multiplier));
        }

        mHasInnerCircle = hasInnerCircle;
        if (hasInnerCircle) {
            mInnerNumbersRadiusMultiplier = Float.parseFloat(
                    res.getString(R.string.jdtp_numbers_radius_multiplier_inner));
            mOuterNumbersRadiusMultiplier = Float.parseFloat(
                    res.getString(R.string.jdtp_numbers_radius_multiplier_outer));
        } else mNumbersRadiusMultiplier = Float.parseFloat(
                res.getString(R.string.jdtp_numbers_radius_multiplier_normal));
        mSelectionRadiusMultiplier =
                Float.parseFloat(res.getString(R.string.jdtp_selection_radius_multiplier));

        mAnimationRadiusMultiplier = 1;
        mTransitionMidRadiusMultiplier = 1f + (0.05f * (disappearsOut ? -1 : 1));
        mTransitionEndRadiusMultiplier = 1f + (0.3f * (disappearsOut ? 1 : -1));
        mInvalidateUpdateListener = new InvalidateUpdateListener();

        setSelection(selectionDegrees, isInnerCircle, false);
        mIsInitialized = true;
    }

    void setTheme(Context c, boolean themeDark) {
        int color;
        if (themeDark) {
            color = ContextCompat.getColor(c, R.color.jdtp_red);
            mSelectionAlpha = SELECTED_ALPHA_THEME_DARK;
        } else {
            color = ContextCompat.getColor(c, R.color.jdtp_accent_color);
            mSelectionAlpha = SELECTED_ALPHA;
        }
        mPaint.setColor(color);
    }

    public void setSelection(int selectionDegrees, boolean isInnerCircle, boolean forceDrawDot) {
        mSelectionDegrees = selectionDegrees;
        mSelectionRadians = selectionDegrees * Math.PI / 180;
        mForceDrawDot = forceDrawDot;

        if (mHasInnerCircle) {
            if (isInnerCircle)
                mNumbersRadiusMultiplier = mInnerNumbersRadiusMultiplier;
            else mNumbersRadiusMultiplier = mOuterNumbersRadiusMultiplier;
        }
    }

    @Override
    public boolean hasOverlappingRendering() {
        return false;
    }

    public void setAnimationRadiusMultiplier(float animationRadiusMultiplier) {
        mAnimationRadiusMultiplier = animationRadiusMultiplier;
    }

    public int getDegreesFromCoordinates(float pointX, float pointY, boolean forceLegal,
                                  final Boolean[] isInnerCircle) {
        if (!mDrawValuesReady) return -1;

        double hypotenuse = Math.sqrt((pointY - mYCenter) * (pointY - mYCenter) +
                (pointX - mXCenter) * (pointX - mXCenter));
        if (mHasInnerCircle) {
            if (forceLegal) {
                int innerNumberRadius = (int) (mCircleRadius * mInnerNumbersRadiusMultiplier);
                int distanceToInnerNumber = (int) Math.abs(hypotenuse - innerNumberRadius);
                int outerNumberRadius = (int) (mCircleRadius * mOuterNumbersRadiusMultiplier);
                int distanceToOuterNumber = (int) Math.abs(hypotenuse - outerNumberRadius);

                isInnerCircle[0] = (distanceToInnerNumber <= distanceToOuterNumber);
            } else {
                int minAllowedHypotenuseForInnerNumber =
                        (int) (mCircleRadius * mInnerNumbersRadiusMultiplier) - mSelectionRadius;
                int maxAllowedHypotenuseForOuterNumber =
                        (int) (mCircleRadius * mOuterNumbersRadiusMultiplier) + mSelectionRadius;
                int halfwayHypotenusePoint = (int) (mCircleRadius *
                        ((mOuterNumbersRadiusMultiplier + mInnerNumbersRadiusMultiplier) / 2));

                if (hypotenuse >= minAllowedHypotenuseForInnerNumber &&
                        hypotenuse <= halfwayHypotenusePoint)
                    isInnerCircle[0] = true;
                else if (hypotenuse <= maxAllowedHypotenuseForOuterNumber &&
                        hypotenuse >= halfwayHypotenusePoint)
                    isInnerCircle[0] = false;
                else return -1;
            }
        } else if (!forceLegal) {
            int distanceToNumber = (int) Math.abs(hypotenuse - mLineLength);
            int maxAllowedDistance = (int) (mCircleRadius * (1 - mNumbersRadiusMultiplier));
            if (distanceToNumber > maxAllowedDistance) return -1;
        }


        float opposite = Math.abs(pointY - mYCenter);
        double radians = Math.asin(opposite / hypotenuse);
        int degrees = (int) (radians * 180 / Math.PI);

        boolean rightSide = (pointX > mXCenter);
        boolean topSide = (pointY < mYCenter);
        if (rightSide && topSide) {
            degrees = 90 - degrees;
        } else if (rightSide)
            degrees = 90 + degrees;
        else if (!topSide)
            degrees = 270 - degrees;
        else degrees = 270 + degrees;
        return degrees;
    }

    @Override
    public void onDraw(Canvas canvas) {
        int viewWidth = getWidth();
        if (viewWidth == 0 || !mIsInitialized) return;

        if (!mDrawValuesReady) {
            mXCenter = getWidth() / 2;
            mYCenter = getHeight() / 2;
            mCircleRadius = (int) (Math.min(mXCenter, mYCenter) * mCircleRadiusMultiplier);

            if (!mIs24HourMode) {
                int amPmCircleRadius = (int) (mCircleRadius * mAmPmCircleRadiusMultiplier);
                mYCenter -= amPmCircleRadius * 0.75;
            }

            mSelectionRadius = (int) (mCircleRadius * mSelectionRadiusMultiplier);
            mDrawValuesReady = true;
        }

        mLineLength = (int) (mCircleRadius * mNumbersRadiusMultiplier
                * mAnimationRadiusMultiplier);
        int pointX = mXCenter + (int) (mLineLength * Math.sin(mSelectionRadians));
        int pointY = mYCenter - (int) (mLineLength * Math.cos(mSelectionRadians));

        mPaint.setAlpha(mSelectionAlpha);
        canvas.drawCircle(pointX, pointY, mSelectionRadius, mPaint);

        if (mForceDrawDot | mSelectionDegrees % 30 != 0) {
            mPaint.setAlpha(FULL_ALPHA);
            canvas.drawCircle(pointX, pointY, (mSelectionRadius * 2 / 7f), mPaint);
        } else {
            int lineLength = mLineLength;
            lineLength -= mSelectionRadius;
            pointX = mXCenter + (int) (lineLength * Math.sin(mSelectionRadians));
            pointY = mYCenter - (int) (lineLength * Math.cos(mSelectionRadians));
        }


        mPaint.setAlpha(255);
        mPaint.setStrokeWidth(1);
        canvas.drawLine(mXCenter, mYCenter, pointX, pointY, mPaint);
    }

    public ObjectAnimator getDisappearAnimator() {
        if (!mIsInitialized || !mDrawValuesReady) return null;

        Keyframe kf0, kf1, kf2;
        float midwayPoint = 0.2f;
        int duration = 500;

        kf0 = Keyframe.ofFloat(0f, 1);
        kf1 = Keyframe.ofFloat(midwayPoint, mTransitionMidRadiusMultiplier);
        kf2 = Keyframe.ofFloat(1f, mTransitionEndRadiusMultiplier);
        PropertyValuesHolder radiusDisappear = PropertyValuesHolder.ofKeyframe(
                "animationRadiusMultiplier", kf0, kf1, kf2);

        kf0 = Keyframe.ofFloat(0f, 1f);
        kf1 = Keyframe.ofFloat(1f, 0f);
        PropertyValuesHolder fadeOut = PropertyValuesHolder.ofKeyframe("alpha", kf0, kf1);

        ObjectAnimator disappearAnimator = ObjectAnimator.ofPropertyValuesHolder(
                this, radiusDisappear, fadeOut).setDuration(duration);
        disappearAnimator.addUpdateListener(mInvalidateUpdateListener);

        return disappearAnimator;
    }

    public ObjectAnimator getReappearAnimator() {
        if (!mIsInitialized || !mDrawValuesReady) return null;

        Keyframe kf0, kf1, kf2, kf3;
        float midwayPoint = 0.2f;
        int duration = 500;

        float delayMultiplier = 0.25f;
        float transitionDurationMultiplier = 1f;
        float totalDurationMultiplier = transitionDurationMultiplier + delayMultiplier;
        int totalDuration = (int) (duration * totalDurationMultiplier);
        float delayPoint = (delayMultiplier * duration) / totalDuration;
        midwayPoint = 1 - (midwayPoint * (1 - delayPoint));

        kf0 = Keyframe.ofFloat(0f, mTransitionEndRadiusMultiplier);
        kf1 = Keyframe.ofFloat(delayPoint, mTransitionEndRadiusMultiplier);
        kf2 = Keyframe.ofFloat(midwayPoint, mTransitionMidRadiusMultiplier);
        kf3 = Keyframe.ofFloat(1f, 1);
        PropertyValuesHolder radiusReappear = PropertyValuesHolder.ofKeyframe(
                "animationRadiusMultiplier", kf0, kf1, kf2, kf3);

        kf0 = Keyframe.ofFloat(0f, 0f);
        kf1 = Keyframe.ofFloat(delayPoint, 0f);
        kf2 = Keyframe.ofFloat(1f, 1f);
        PropertyValuesHolder fadeIn = PropertyValuesHolder.ofKeyframe("alpha", kf0, kf1, kf2);

        ObjectAnimator reappearAnimator = ObjectAnimator.ofPropertyValuesHolder(
                this, radiusReappear, fadeIn).setDuration(totalDuration);
        reappearAnimator.addUpdateListener(mInvalidateUpdateListener);
        return reappearAnimator;
    }

    private class InvalidateUpdateListener implements AnimatorUpdateListener {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            RadialSelectorView.this.invalidate();
        }
    }
}
