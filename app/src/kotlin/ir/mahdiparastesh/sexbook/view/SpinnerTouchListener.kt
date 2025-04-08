package ir.mahdiparastesh.sexbook.view

import android.util.Log
import android.view.MotionEvent
import android.view.View

/** A touch listener that doesn't regard scrolling touch events as clicks */
class SpinnerTouchListener : View.OnTouchListener {
    override fun onTouch(v: View, me: MotionEvent): Boolean {
        if (me.action != MotionEvent.ACTION_UP)
            return true
        /*Log.d("ESPINELA", "top: ${me.y} >= ${v.top} (${me.y >= v.top})")
        Log.d("ESPINELA", "bottom: ${me.y} <= ${v.bottom} (${me.y <= v.bottom})")
        Log.d("ESPINELA", "left: ${me.x} >= ${v.left} (${me.x >= v.left})")
        Log.d("ESPINELA", "right: ${me.x} <= ${v.right} (${me.x <= v.right})")*/
        if (me.y < 0 || me.y > v.height || me.x < 0 || me.x > v.width)
            return true
        v.performClick()
        return true
    }
}

/** A customisable touch listener that doesn't regard scrolling touch events as clicks */
class CustomSpinnerTouchListener(private val func: () -> Unit) : View.OnTouchListener {
    override fun onTouch(v: View, me: MotionEvent): Boolean {
        func()
        if (me.action != MotionEvent.ACTION_UP)
            return true
        /*Log.d("ESPINELA", "top: ${me.y} >= ${v.top} (${me.y >= v.top})")
        Log.d("ESPINELA", "bottom: ${me.y} <= ${v.bottom} (${me.y <= v.bottom})")
        Log.d("ESPINELA", "left: ${me.x} >= ${v.left} (${me.x >= v.left})")
        Log.d("ESPINELA", "right: ${me.x} <= ${v.right} (${me.x <= v.right})")*/
        if (me.y < 0 || me.y > v.height || me.x < 0 || me.x > v.width)
            return true
        v.performClick()
        return true
    }
}
