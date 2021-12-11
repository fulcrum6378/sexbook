package ir.mahdiparastesh.sexbook

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Typeface
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import java.util.*

class Fun {
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var c: Context
        lateinit var sp: SharedPreferences
        lateinit var font1: Typeface
        lateinit var font1Bold: Typeface
        var dm = DisplayMetrics()
        var night = false
        var dirRtl = false


        fun init(that: AppCompatActivity, root: ViewGroup) {
            c = that.applicationContext
            sp = that.getSharedPreferences(
                c.resources.getString(R.string.stSP), Context.MODE_PRIVATE
            )
            dm = that.resources.displayMetrics
            night = c.resources.getBoolean(R.bool.night)
            dirRtl = c.resources.getBoolean(R.bool.dirRtl)
            root.layoutDirection =
                if (!dirRtl) ViewGroup.LAYOUT_DIRECTION_LTR else ViewGroup.LAYOUT_DIRECTION_RTL
            if (!::font1.isInitialized) font1 = font()
            if (!::font1Bold.isInitialized) font1Bold = font(true)
        }

        fun dp(px: Int) = (dm.density * px.toFloat()).toInt()

        fun now() = Calendar.getInstance().timeInMillis

        fun explode(
            v: View, dur: Long = 522, src: Int = R.drawable.button_1,
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

        fun color(res: Int) = ContextCompat.getColor(c, res)

        fun pdcf(res: Int = R.color.CPDD) =
            PorterDuffColorFilter(ContextCompat.getColor(c, res), PorterDuff.Mode.SRC_IN)

        fun fixADButton(button: Button) = button.apply {
            setTextColor(color(R.color.mrvPopupButtons))
            //setBackgroundColor(color(R.color.CP))
            typeface = font1
        }

        fun z(n: Int): String {
            val s = n.toString()
            return if (s.length == 1) "0$s" else s
        }

        fun calType() = CalendarType.values()[sp.getInt(Settings.spCalType, 0)]

        fun vis(v: View, b: Boolean = true): Boolean {
            v.visibility = if (b) View.VISIBLE else View.GONE
            return b
        }

        fun font(bold: Boolean = false): Typeface = Typeface.createFromAsset(
            c.assets, if (!bold) c.resources.getString(R.string.font1)
            else c.resources.getString(R.string.font1Bold)
        )
    }

    enum class CalendarType { GREGORY, JALALI }
}
