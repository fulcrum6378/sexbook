package ir.mahdiparastesh.sexbook.view;

import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.view.Gravity;

/**
 * Translated from the AndroidUtils project by James Fenn:
 * https://github.com/fennifith/AndroidUtils/blob/main/androidutils/src/main/java/
 * me/jfenn/androidutils/seekbar/SeekBarDrawable.java
 */
public class SeekBarDrawable extends ClipDrawable {

    private float height;
    private Rect rect;

    public SeekBarDrawable(Drawable drawable, float height) {
        super(drawable, Gravity.START, ClipDrawable.HORIZONTAL);
        this.height = height;
    }

    @Override
    public void draw(Canvas canvas) {
        if (rect == null) {
            Rect bounds = getBounds();
            setBounds(rect = new Rect(
                    bounds.left,
                    (int) (bounds.centerY() - height / 2),
                    bounds.right,
                    (int) (bounds.centerY() + height / 2)
            ));
        }

        super.draw(canvas);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
