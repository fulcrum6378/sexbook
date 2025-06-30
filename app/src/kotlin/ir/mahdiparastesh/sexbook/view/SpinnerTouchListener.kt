package ir.mahdiparastesh.sexbook.view

import android.view.MotionEvent
import android.view.View

/**
 * A touch listener that doesn't regard scrolling touch events as clicks
 *
 * Note: this listener cannot handle tooltips.
 */
class SpinnerTouchListener : View.OnTouchListener {

    override fun onTouch(v: View, me: MotionEvent): Boolean {
        if (me.action != MotionEvent.ACTION_UP)
            return true
        if (me.y < 0 || me.y > v.height || me.x < 0 || me.x > v.width)
            return true
        v.performClick()
        return true
    }
}

/** A customisable touch listener that doesn't regard scrolling touch events as clicks */
class CustomSpinnerTouchListener(
    private val onClick: (() -> Unit)? = null,
    private val onTouch: (() -> Unit)? = null,
) : View.OnTouchListener {

    override fun onTouch(v: View, me: MotionEvent): Boolean {
        onTouch?.also { it() }
        if (me.action != MotionEvent.ACTION_UP)
            return true
        if (me.y < 0 || me.y > v.height || me.x < 0 || me.x > v.width)
            return true
        onClick?.also { it() }
            ?: v.performClick()
        return true
    }
}
