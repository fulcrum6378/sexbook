package com.mahdiparastesh.mbcounter

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Process
import android.util.DisplayMetrics
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import java.util.*
import kotlin.system.exitProcess

class Fun {
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var c: Context
        lateinit var sp: SharedPreferences
        var dm = DisplayMetrics()
        var night = false


        fun init(that: AppCompatActivity) {
            c = that.applicationContext
            sp = that.getPreferences(Context.MODE_PRIVATE)
            dm = that.resources.displayMetrics
            night = c.resources.getBoolean(R.bool.night)
        }

        fun dp(px: Int) = (dm.density * px.toFloat()).toInt()

        fun now() = Calendar.getInstance().timeInMillis

        fun explode(
            c: Context, v: View, dur: Long = 522,
            src: Int = R.drawable.circle_cp, alpha: Float = 1f, max: Float = 4f
        ) {
            if (v.parent !is ConstraintLayout) return
            val parent = v.parent as ConstraintLayout
            var ex = View(c).apply {
                background = ContextCompat.getDrawable(c, src)
                translationX = v.translationX
                translationY = v.translationY
                scaleX = v.scaleX
                scaleY = v.scaleY
                this.alpha = alpha
            }
            parent.addView(ex, parent.indexOfChild(v), ConstraintLayout.LayoutParams(0, 0).apply {
                topToTop = v.id; leftToLeft = v.id; rightToRight = v.id; bottomToBottom = v.id
            })

            var explode = AnimatorSet().setDuration(dur)
            var hide = ObjectAnimator.ofFloat(ex, "alpha", 0f)
            hide.startDelay = explode.duration / 4
            explode.apply {
                playTogether(
                    ObjectAnimator.ofFloat(ex, "scaleX", ex.scaleX * max),
                    ObjectAnimator.ofFloat(ex, "scaleY", ex.scaleY * max),
                    hide
                )
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        parent.removeView(ex)
                    }
                })
                start()
            }
        }

        fun color(res: Int) = ContextCompat.getColor(c, res)

        fun pdcf(c: Context, res: Int = R.color.CPDD) =
            PorterDuffColorFilter(ContextCompat.getColor(c, res), PorterDuff.Mode.SRC_IN)

        fun exit(that: AppCompatActivity) {
            that.moveTaskToBack(true)
            Process.killProcess(Process.myPid())
            exitProcess(1)
        }

        fun fixADButton(button: Button) = button.apply {
            setTextColor(color(R.color.mrvPopupButtons))
            //setBackgroundColor(color(R.color.CP))
        }

        fun z(n: Int): String {
            val s = n.toString()
            return if (s.length == 1) "0$s" else s
        }

        fun cutYear(y: Int) = y.toString().substring(2, 4)
    }
}
