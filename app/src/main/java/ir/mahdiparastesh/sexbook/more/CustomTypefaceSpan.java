package ir.mahdiparastesh.sexbook.more;

import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.style.TypefaceSpan;

public class CustomTypefaceSpan extends TypefaceSpan {
    private final Typeface newType;
    private final float textSize;

    public CustomTypefaceSpan(String family, Typeface type, float size) {
        super(family);
        newType = type;
        textSize = size;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        applyCustomTypeFace(ds, newType, textSize);
    }

    @Override
    public void updateMeasureState(TextPaint paint) {
        applyCustomTypeFace(paint, newType, textSize);
    }

    private static void applyCustomTypeFace(Paint paint, Typeface tf, float ts) {
        int oldStyle;
        Typeface old = paint.getTypeface();
        if (old == null) oldStyle = 0;
        else oldStyle = old.getStyle();

        int fake = oldStyle & ~tf.getStyle();
        if ((fake & Typeface.BOLD) != 0)
            paint.setFakeBoldText(true);

        if ((fake & Typeface.ITALIC) != 0)
            paint.setTextSkewX(-0.25f);

        paint.setTextSize(ts);
        paint.setTypeface(tf);
    }
}
