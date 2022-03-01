package ir.mahdiparastesh.sexbook

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.icu.util.Calendar
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import ir.mahdiparastesh.sexbook.more.BaseActivity
import ir.mahdiparastesh.sexbook.more.Jalali

class Fun {
    companion object {
        fun now() = Calendar.getInstance().timeInMillis

        fun explode(
            c: Context, v: View, dur: Long = 522, src: Int = R.drawable.button_1,
            alpha: Float = 1f, max: Float = 4f
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

        fun z(n: Int): String {
            val s = n.toString()
            return if (s.length == 1) "0$s" else s
        }

        fun calType() = CalendarType.values()[BaseActivity.sp.getInt(Settings.spCalType, 0)]

        fun vis(v: View, b: Boolean = true): Boolean {
            v.visibility = if (b) View.VISIBLE else View.GONE
            return b
        }

        @Suppress("DEPRECATION")
        fun shake(c: Context, dur: Long = 60L) {
            (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                (c.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
            else c.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator)
                .vibrate(VibrationEffect.createOneShot(dur, VibrationEffect.DEFAULT_AMPLITUDE))
        }

        fun fullDate(cal: Calendar) = when (calType()) {
            CalendarType.JALALI -> {
                val j = Jalali(cal)
                "${z(j.Y)}.${z(j.M + 1)}.${z(j.D)}"
            }
            else -> "${z(cal[Calendar.YEAR])}.${z(cal[Calendar.MONTH] + 1)}" +
                    ".${z(cal[Calendar.DAY_OF_MONTH])}"
        }

        fun calendar(time: Long): Calendar = Calendar.getInstance().apply { timeInMillis = time }

        fun defCalendar(time: Long): Calendar = Calendar.getInstance().apply {
            timeInMillis = time
            this[Calendar.HOUR] = 0
            this[Calendar.MINUTE] = 0
            this[Calendar.SECOND] = 0
            this[Calendar.MILLISECOND] = 0
        }
    }

    enum class CalendarType { GREGORY, JALALI }
}
