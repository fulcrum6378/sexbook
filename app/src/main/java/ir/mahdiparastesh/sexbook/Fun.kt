package ir.mahdiparastesh.sexbook

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.initialization.AdapterStatus
import com.google.android.gms.ads.initialization.InitializationStatus
import ir.mahdiparastesh.sexbook.more.BaseActivity
import ir.mahdiparastesh.sexbook.more.BaseActivity.Companion.night
import ir.mahdiparastesh.sexbook.more.Jalali
import java.util.*

class Fun {
    companion object {
        private const val ADMOB = "com.google.android.gms.ads.MobileAds"

        fun now() = Calendar.getInstance().timeInMillis

        fun View.explode(
            c: Context, dur: Long = 522, @DrawableRes src: Int = R.drawable.button_1,
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
                ex, parent.indexOfChild(this),
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

        fun Calendar.fullDate(c: BaseActivity) = when (c.calType()) {
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

        fun InitializationStatus.isReady(): Boolean = if (adapterStatusMap.containsKey(ADMOB))
            adapterStatusMap[ADMOB]?.initializationState == AdapterStatus.State.READY
        else false

        fun adaptiveBanner(c: BaseActivity, unitId: String) = AdView(c).apply {
            id = R.id.adBanner
            adSize = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
                c, (c.dm.widthPixels / c.dm.density).toInt()
            )
            adUnitId = unitId
        }

        fun adaptiveBannerLp() = ConstraintLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID }

        fun AlertDialog.stylise(c: BaseActivity) {
            // Don't move this function to BaseActivity
            getButton(AlertDialog.BUTTON_POSITIVE)?.apply {
                setTextColor(c.color(R.color.mrvPopupButtons))
                //setBackgroundColor(color(R.color.CP))
                typeface = c.font1
            }
            getButton(AlertDialog.BUTTON_NEGATIVE)?.apply {
                setTextColor(c.color(R.color.mrvPopupButtons))
                //setBackgroundColor(color(R.color.CP))
                typeface = c.font1
            }
            getButton(AlertDialog.BUTTON_NEUTRAL)?.apply {
                setTextColor(c.color(R.color.mrvPopupButtons))
                //setBackgroundColor(color(R.color.CP))
                typeface = c.font1
            }
        }
    }

    @Suppress("unused")
    enum class CalendarType { GREGORIAN, JALALI }
}
