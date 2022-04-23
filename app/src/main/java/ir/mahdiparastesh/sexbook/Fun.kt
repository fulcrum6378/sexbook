package ir.mahdiparastesh.sexbook

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.os.*
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.viewbinding.ViewBinding
import ir.mahdiparastesh.sexbook.more.BaseActivity
import ir.mahdiparastesh.sexbook.more.BaseActivity.Companion.night
import ir.mahdiparastesh.sexbook.more.Jalali
import java.util.*

class Fun {
    companion object {
        fun now() = Calendar.getInstance().timeInMillis

        fun View.explode(
            c: Context, dur: Long = 522, src: Int = R.drawable.button_1,
            alpha: Float = 1f, max: Float = 4f
        ) {
            if (parent !is ConstraintLayout) return
            val parent = parent as ConstraintLayout
            var ex = View(c).apply {
                background = ContextCompat.getDrawable(c, src)
                translationX = translationX
                translationY = translationY
                scaleX = scaleX
                scaleY = scaleY
                this.alpha = alpha
            }
            parent.addView(
                ex,
                parent.indexOfChild(this),
                ConstraintLayout.LayoutParams(0, 0).apply {
                    topToTop = id; leftToLeft = id; rightToRight = id; bottomToBottom = id
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

        fun z(n: Int): String {
            val s = n.toString()
            return if (s.length == 1) "0$s" else s
        }

        fun calType() = CalendarType.values()[BaseActivity.sp.getInt(Settings.spCalType, 0)]

        fun View.vis(b: Boolean = true): Boolean {
            visibility = if (b) View.VISIBLE else View.GONE
            return b
        }

        @Suppress("DEPRECATION")
        @JvmStatic
        fun Context.shake(dur: Long = 48L) {
            val vib = (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
            else getSystemService(Context.VIBRATOR_SERVICE) as Vibrator)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                vib.vibrate(VibrationEffect.createOneShot(dur, VibrationEffect.DEFAULT_AMPLITUDE))
            else vib.vibrate(dur)
        }

        fun Calendar.fullDate() = when (calType()) {
            CalendarType.JALALI -> {
                val j = Jalali(this)
                "${z(j.Y)}.${z(j.M + 1)}.${z(j.D)}"
            }
            else -> "${z(this[Calendar.YEAR])}.${z(this[Calendar.MONTH] + 1)}" +
                    ".${z(this[Calendar.DAY_OF_MONTH])}"
        }

        fun Long.calendar(): Calendar =
            Calendar.getInstance().apply { timeInMillis = this@calendar }

        fun Long.defCalendar(): Calendar = Calendar.getInstance().apply {
            timeInMillis = this@defCalendar
            this[Calendar.HOUR] = 0
            this[Calendar.MINUTE] = 0
            this[Calendar.SECOND] = 0
            this[Calendar.MILLISECOND] = 0
        }

        fun BaseActivity.randomColor() = arrayListOf(
            Color.BLUE, Color.RED, Color.CYAN, Color.GREEN, Color.MAGENTA,
            if (night()) Color.WHITE else Color.BLACK
        ).random()

        fun ViewBinding.setOnLoadListener(func: () -> Unit): CountDownTimer =
            object : CountDownTimer(5000, 50) {
                override fun onFinish() {}
                override fun onTick(millisUntilFinished: Long) {
                    if (root.width <= 0) return
                    func()
                    this.cancel()
                }
            }.start()
    }

    enum class CalendarType { GREGORY, JALALI }
}
