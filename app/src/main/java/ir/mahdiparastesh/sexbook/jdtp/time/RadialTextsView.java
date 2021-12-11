package ir.mahdiparastesh.sexbook.jdtp.time;

import static ir.mahdiparastesh.sexbook.Fun.font1;

import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Typeface;
import android.view.View;

import androidx.core.content.ContextCompat;

import ir.mahdiparastesh.sexbook.R;
import ir.mahdiparastesh.sexbook.jdtp.utils.LanguageUtils;

public class RadialTextsView extends View {
    private final Paint mPaint = new Paint();
    private final Paint mSelectedPaint = new Paint();

    private boolean mDrawValuesReady;
    private boolean mIsInitialized;

    private int selection = -1;

    private String[] mTexts;
    private String[] mInnerTexts;
    private boolean mIs24HourMode;
    private boolean mHasInnerCircle;
    private float mCircleRadiusMultiplier;
    private float mAmPmCircleRadiusMultiplier;
    private float mNumbersRadiusMultiplier;
    private float mInnerNumbersRadiusMultiplier;
    private float mTextSizeMultiplier;
    private float mInnerTextSizeMultiplier;

    private int mXCenter;
    private int mYCenter;
    private float mCircleRadius;
    private boolean mTextGridValuesDirty;
    private float mTextSize;
    private float mInnerTextSize;
    private float[] mTextGridHeights;
    private float[] mTextGridWidths;
    private float[] mInnerTextGridHeights;
    private float[] mInnerTextGridWidths;

    private float mAnimationRadiusMultiplier;
    private float mTransitionMidRadiusMultiplier;
    private float mTransitionEndRadiusMultiplier;
    ObjectAnimator mDisappearAnimator;
    ObjectAnimator mReappearAnimator;
    private InvalidateUpdateListener mInvalidateUpdateListener;
    private final Context c;

    public RadialTextsView(Context c) {
        super(c);
        this.c = c;
        mIsInitialized = false;
    }

    public void initialize(Resources res, String[] texts, String[] innerTexts,
                           boolean is24HourMode, boolean disappearsOut) {
        if (mIsInitialized) return;

        int numbersTextColor = ContextCompat.getColor(c, R.color.jdtp_numbers_text_color);
        mPaint.setColor(numbersTextColor);
        mPaint.setAntiAlias(true);
        mPaint.setTextAlign(Align.CENTER);

        int selectedTextColor = ContextCompat.getColor(c, R.color.jdtp_white);
        mSelectedPaint.setColor(selectedTextColor);
        mSelectedPaint.setAntiAlias(true);
        mSelectedPaint.setTypeface(font1);
        mSelectedPaint.setTextAlign(Align.CENTER);

        mTexts = texts;
        mInnerTexts = innerTexts;
        mIs24HourMode = is24HourMode;
        mHasInnerCircle = (innerTexts != null);

        if (is24HourMode)
            mCircleRadiusMultiplier = Float.parseFloat(
                    res.getString(R.string.jdtp_circle_radius_multiplier_24HourMode));
        else {
            mCircleRadiusMultiplier = Float.parseFloat(
                    res.getString(R.string.jdtp_circle_radius_multiplier));
            mAmPmCircleRadiusMultiplier =
                    Float.parseFloat(res.getString(R.string.jdtp_ampm_circle_radius_multiplier));
        }

        mTextGridHeights = new float[7];
        mTextGridWidths = new float[7];
        if (mHasInnerCircle) {
            mNumbersRadiusMultiplier = Float.parseFloat(
                    res.getString(R.string.jdtp_numbers_radius_multiplier_outer));
            mTextSizeMultiplier = Float.parseFloat(
                    res.getString(R.string.jdtp_text_size_multiplier_outer));
            mInnerNumbersRadiusMultiplier = Float.parseFloat(
                    res.getString(R.string.jdtp_numbers_radius_multiplier_inner));
            mInnerTextSizeMultiplier = Float.parseFloat(
                    res.getString(R.string.jdtp_text_size_multiplier_inner));

            mInnerTextGridHeights = new float[7];
            mInnerTextGridWidths = new float[7];
        } else {
            mNumbersRadiusMultiplier = Float.parseFloat(
                    res.getString(R.string.jdtp_numbers_radius_multiplier_normal));
            mTextSizeMultiplier = Float.parseFloat(
                    res.getString(R.string.jdtp_text_size_multiplier_normal));
        }

        mAnimationRadiusMultiplier = 1;
        mTransitionMidRadiusMultiplier = 1f + (0.05f * (disappearsOut ? -1 : 1));
        mTransitionEndRadiusMultiplier = 1f + (0.3f * (disappearsOut ? 1 : -1));
        mInvalidateUpdateListener = new InvalidateUpdateListener();

        mTextGridValuesDirty = true;
        mIsInitialized = true;
    }

    void setTheme(Context c, boolean themeDark) {
        int textColor;
        if (themeDark) textColor = ContextCompat.getColor(c, R.color.jdtp_white);
        else textColor = ContextCompat.getColor(c, R.color.jdtp_numbers_text_color);
        mPaint.setColor(textColor);
    }

    protected void setSelection(int selection) {
        this.selection = selection;
    }

    @Override
    public boolean hasOverlappingRendering() {
        return false;
    }

    public void setAnimationRadiusMultiplier(float animationRadiusMultiplier) {
        mAnimationRadiusMultiplier = animationRadiusMultiplier;
        mTextGridValuesDirty = true;
    }

    @Override
    public void onDraw(Canvas canvas) {
        int viewWidth = getWidth();
        if (viewWidth == 0 || !mIsInitialized) {
            return;
        }

        if (!mDrawValuesReady) {
            mXCenter = getWidth() / 2;
            mYCenter = getHeight() / 2;
            mCircleRadius = Math.min(mXCenter, mYCenter) * mCircleRadiusMultiplier;
            if (!mIs24HourMode) {
                float amPmCircleRadius = mCircleRadius * mAmPmCircleRadiusMultiplier;
                mYCenter -= amPmCircleRadius * 0.75;
            }

            mTextSize = mCircleRadius * mTextSizeMultiplier;
            if (mHasInnerCircle) {
                mInnerTextSize = mCircleRadius * mInnerTextSizeMultiplier;
            }
            renderAnimations();

            mTextGridValuesDirty = true;
            mDrawValuesReady = true;
        }

        if (mTextGridValuesDirty) {
            float numbersRadius =
                    mCircleRadius * mNumbersRadiusMultiplier * mAnimationRadiusMultiplier;

            calculateGridSizes(numbersRadius, mXCenter, mYCenter,
                    mTextSize, mTextGridHeights, mTextGridWidths);
            if (mHasInnerCircle) {
                float innerNumbersRadius =
                        mCircleRadius * mInnerNumbersRadiusMultiplier * mAnimationRadiusMultiplier;
                calculateGridSizes(innerNumbersRadius, mXCenter, mYCenter,
                        mInnerTextSize, mInnerTextGridHeights, mInnerTextGridWidths);
            }
            mTextGridValuesDirty = false;
        }

        drawTexts(canvas, mTextSize, font1, mTexts, mTextGridWidths, mTextGridHeights);
        if (mHasInnerCircle) drawTexts(canvas, mInnerTextSize, font1, mInnerTexts,
                mInnerTextGridWidths, mInnerTextGridHeights);
    }

    private void calculateGridSizes(float numbersRadius, float xCenter, float yCenter,
                                    float textSize, float[] textGridHeights, float[] textGridWidths) {

        float offset1 = numbersRadius;
        float offset2 = numbersRadius * ((float) Math.sqrt(3)) / 2f;
        float offset3 = numbersRadius / 2f;
        mPaint.setTextSize(textSize);
        mSelectedPaint.setTextSize(textSize);
        yCenter -= (mPaint.descent() + mPaint.ascent()) / 2;

        textGridHeights[0] = yCenter - offset1;
        textGridWidths[0] = xCenter - offset1;
        textGridHeights[1] = yCenter - offset2;
        textGridWidths[1] = xCenter - offset2;
        textGridHeights[2] = yCenter - offset3;
        textGridWidths[2] = xCenter - offset3;
        textGridHeights[3] = yCenter;
        textGridWidths[3] = xCenter;
        textGridHeights[4] = yCenter + offset3;
        textGridWidths[4] = xCenter + offset3;
        textGridHeights[5] = yCenter + offset2;
        textGridWidths[5] = xCenter + offset2;
        textGridHeights[6] = yCenter + offset1;
        textGridWidths[6] = xCenter + offset1;
    }

    private void drawTexts(Canvas canvas, float textSize, Typeface typeface, String[] texts,
                           float[] textGridWidths, float[] textGridHeights) {
        mPaint.setTextSize(textSize);
        mPaint.setTypeface(typeface);
        LanguageUtils.getPersianNumbers(texts);
        canvas.drawText(texts[0], textGridWidths[3], textGridHeights[0],
                Integer.parseInt(texts[0]) == selection ? mSelectedPaint : mPaint);
        canvas.drawText(texts[1], textGridWidths[4], textGridHeights[1],
                Integer.parseInt(texts[1]) == selection ? mSelectedPaint : mPaint);
        canvas.drawText(texts[2], textGridWidths[5], textGridHeights[2],
                Integer.parseInt(texts[2]) == selection ? mSelectedPaint : mPaint);
        canvas.drawText(texts[3], textGridWidths[6], textGridHeights[3],
                Integer.parseInt(texts[3]) == selection ? mSelectedPaint : mPaint);
        canvas.drawText(texts[4], textGridWidths[5], textGridHeights[4],
                Integer.parseInt(texts[4]) == selection ? mSelectedPaint : mPaint);
        canvas.drawText(texts[5], textGridWidths[4], textGridHeights[5],
                Integer.parseInt(texts[5]) == selection ? mSelectedPaint : mPaint);
        canvas.drawText(texts[6], textGridWidths[3], textGridHeights[6],
                Integer.parseInt(texts[6]) == selection ? mSelectedPaint : mPaint);
        canvas.drawText(texts[7], textGridWidths[2], textGridHeights[5],
                Integer.parseInt(texts[7]) == selection ? mSelectedPaint : mPaint);
        canvas.drawText(texts[8], textGridWidths[1], textGridHeights[4],
                Integer.parseInt(texts[8]) == selection ? mSelectedPaint : mPaint);
        canvas.drawText(texts[9], textGridWidths[0], textGridHeights[3],
                Integer.parseInt(texts[9]) == selection ? mSelectedPaint : mPaint);
        canvas.drawText(texts[10], textGridWidths[1], textGridHeights[2],
                Integer.parseInt(texts[10]) == selection ? mSelectedPaint : mPaint);
        canvas.drawText(texts[11], textGridWidths[2], textGridHeights[1],
                Integer.parseInt(texts[11]) == selection ? mSelectedPaint : mPaint);
    }

    private void renderAnimations() {
        Keyframe kf0, kf1, kf2, kf3;
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

        mDisappearAnimator = ObjectAnimator.ofPropertyValuesHolder(
                this, radiusDisappear, fadeOut).setDuration(duration);
        mDisappearAnimator.addUpdateListener(mInvalidateUpdateListener);

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

        mReappearAnimator = ObjectAnimator.ofPropertyValuesHolder(
                this, radiusReappear, fadeIn).setDuration(totalDuration);
        mReappearAnimator.addUpdateListener(mInvalidateUpdateListener);
    }

    public ObjectAnimator getDisappearAnimator() {
        if (!mIsInitialized || !mDrawValuesReady || mDisappearAnimator == null) return null;

        return mDisappearAnimator;
    }

    public ObjectAnimator getReappearAnimator() {
        if (!mIsInitialized || !mDrawValuesReady || mReappearAnimator == null) return null;

        return mReappearAnimator;
    }

    private class InvalidateUpdateListener implements AnimatorUpdateListener {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            RadialTextsView.this.invalidate();
        }
    }
}
