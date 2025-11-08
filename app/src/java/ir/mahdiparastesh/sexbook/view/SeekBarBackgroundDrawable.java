package ir.mahdiparastesh.sexbook.view;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Translated from the AndroidUtils project by James Fenn:
 * https://github.com/fennifith/AndroidUtils/blob/main/androidutils/src/main/java/
 * me/jfenn/androidutils/seekbar/SeekBarBackgroundDrawable.java
 */
public class SeekBarBackgroundDrawable extends Drawable {

    private Drawable drawable;
    private float height;
    private Paint paint;

    public SeekBarBackgroundDrawable(Drawable drawable, float height) {
        this.drawable = drawable;
        this.height = height;
        paint = new Paint();
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        Bitmap bitmap = Bitmap.createBitmap(getBounds().width(), getBounds().height(),
                Bitmap.Config.RGB_565);
        drawable.setBounds(0, 0, getBounds().width(), getBounds().height());
        drawable.draw(new Canvas(bitmap));

        Rect bounds = getBounds();
        canvas.clipRect(new Rect(
                bounds.left,
                (int) (bounds.centerY() - height / 2),
                bounds.right,
                (int) (bounds.centerY() + height / 2)
        ));

        canvas.drawBitmap(bitmap, 0, 0, paint);
    }

    @Override
    public void setAlpha(int alpha) {
        paint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSPARENT;
    }
}
