package ir.mahdiparastesh.sexbook.more;

import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.style.TypefaceSpan;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

public class CustomTypefaceSpan extends TypefaceSpan {
    private final Typeface newType;
    private final float textSize;
    private Integer textColour = null;

    public CustomTypefaceSpan(Typeface type, float size, @Nullable @ColorInt Integer colour) {
        super("");
        newType = type;
        textSize = size;
        if (colour != null) textColour = colour;
    }

    private static void applyCustomTypeFace(Paint paint, Typeface tf, float ts,
                                            @Nullable @ColorInt Integer tc) {
        paint.setTextSize(ts);
        paint.setTypeface(tf);
        if (tc != null) paint.setColor(tc);
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        applyCustomTypeFace(ds, newType, textSize, textColour);
    }

    @Override
    public void updateMeasureState(TextPaint paint) {
        applyCustomTypeFace(paint, newType, textSize, textColour);
    }
}
