package ir.mahdiparastesh.sexbook.view;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.widget.SeekBar;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

/**
 * Translated from the AndroidUtils project by James Fenn:
 * https://github.com/fennifith/AndroidUtils/blob/main/androidutils/src/main/java/
 * me/jfenn/androidutils/seekbar/SeekBarUtils.java
 */
public class SeekBarUtils {

    /**
     * Apply a color to a SeekBar.
     *
     * @param seekbar The view to apply the color to.
     * @param color   The color to tint the view.
     */
    public static void setProgressBarColor(SeekBar seekbar, @ColorInt int color) {
        seekbar.getProgressDrawable().setColorFilter(color, PorterDuff.Mode.SRC_IN);
        seekbar.getThumb().setColorFilter(color, PorterDuff.Mode.SRC_IN);
    }

    /**
     * Apply a drawable to a seekbar's background track.
     *
     * @param seekbar     The seekbar to apply the drawable to.
     * @param drawable    The drawable to use as the track.
     * @param handleColor The color of the seekbar's handle.
     */
    public static void setProgressBarDrawable(
            SeekBar seekbar, @NonNull Drawable drawable, @ColorInt int handleColor, float height) {
        Drawable background = new SeekBarBackgroundDrawable(
                drawable.mutate().getConstantState().newDrawable(), height);
        background.setAlpha(127);

        LayerDrawable layers = new LayerDrawable(new Drawable[]{
                new SeekBarDrawable(drawable, height),
                background
        });

        layers.setId(0, android.R.id.progress);
        layers.setId(1, android.R.id.background);
        seekbar.setProgressDrawable(layers);
        seekbar.getThumb().setColorFilter(handleColor, PorterDuff.Mode.SRC_IN);
    }
}
