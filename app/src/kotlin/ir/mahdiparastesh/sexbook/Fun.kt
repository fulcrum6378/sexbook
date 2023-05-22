package ir.mahdiparastesh.sexbook

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.icu.util.Calendar
import android.os.*
import android.view.View
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import ir.mahdiparastesh.mcdtp.date.DatePickerDialog
import ir.mahdiparastesh.mcdtp.time.TimePickerDialog
import ir.mahdiparastesh.sexbook.more.BaseActivity
import ir.mahdiparastesh.sexbook.more.BaseActivity.Companion.night
import ir.mahdiparastesh.sexbook.more.HumanistIranianCalendar

object Fun {
    // Latin + Cyrillic Font: Balsamiq Sans

    const val DATABASE = "sexbook.db"
    const val INSTA = "https://www.instagram.com/"
    var vib: Boolean? = null
    // private const val ADMOB = "com.google.android.gms.ads.MobileAds"

    fun now() = System.currentTimeMillis()

    fun View.explode(
        c: BaseActivity, dur: Long = 522, @DrawableRes src: Int = R.drawable.button,
        alpha: Float = 1f, max: Float = 4f
    ) {
        if (parent !is ConstraintLayout) return
        val parent = parent as ConstraintLayout
        val ex = View(c).apply {
            background = ContextCompat.getDrawable(c, src)
            this.alpha = alpha
        }
        parent.addView(
            ex, parent.indexOfChild(this),
            ConstraintLayout.LayoutParams(0, 0).apply {
                topToTop = id; leftToLeft = id; rightToRight = id; bottomToBottom = id
            })

        val explode = AnimatorSet().setDuration(dur)
        val hide = ObjectAnimator.ofFloat(ex, View.ALPHA, 0f)
        hide.startDelay = explode.duration / 4
        explode.apply {
            playTogether(
                ObjectAnimator.ofFloat(ex, View.SCALE_X, ex.scaleX * max),
                ObjectAnimator.ofFloat(ex, View.SCALE_Y, ex.scaleY * max),
                hide
            )
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
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
        if (vib == null) vib = getSharedPreferences(Settings.spName, Context.MODE_PRIVATE)
            .getBoolean(Settings.spVibration, true)
        if (!vib!!) return
        val vib = (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        else getSystemService(Context.VIBRATOR_SERVICE) as Vibrator)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            vib.vibrate(VibrationEffect.createOneShot(dur, VibrationEffect.DEFAULT_AMPLITUDE))
        else vib.vibrate(dur)
    }

    fun Calendar.fullDate() = "${z(this[Calendar.YEAR])}.${z(this[Calendar.MONTH] + 1)}" +
            ".${z(this[Calendar.DAY_OF_MONTH])}"

    fun Long.calendar(c: BaseActivity): Calendar =
        c.calType().newInstance().apply { timeInMillis = this@calendar }

    fun Calendar.createFilterYm() = Pair(this[Calendar.YEAR], this[Calendar.MONTH])

    fun Long.defCalendar(c: BaseActivity): Calendar = c.calType().newInstance().apply {
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

    /*fun InitializationStatus.isReady(): Boolean = if (adapterStatusMap.containsKey(ADMOB))
        adapterStatusMap[ADMOB]?.initializationState == AdapterStatus.State.READY
    else false

    fun adaptiveBanner(c: BaseActivity, unitId: String) = AdView(c).apply {
        id = R.id.adBanner
        setAdSize(
            AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
                c, (c.dm.widthPixels / c.dm.density).toInt()
            )
        )
        adUnitId = unitId
    }

    fun adaptiveBannerLp() = ConstraintLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
    ).apply { bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID }*/

    fun DatePickerDialog<*>.defaultOptions(c: BaseActivity): DatePickerDialog<*> {
        version = DatePickerDialog.Version.VERSION_1
        accentColor = c.color(R.color.CP)
        firstDayOfWeek = if (calendarType == HumanistIranianCalendar::class.java)
            Calendar.THURSDAY else Calendar.SUNDAY
        doVibrate(vib == true)
        setOkColor(c.color(R.color.dialogText))
        setCancelColor(c.color(R.color.dialogText))
        boldFont = R.font.bold
        normalFont = R.font.normal
        return this
    }

    fun TimePickerDialog.defaultOptions(c: BaseActivity): TimePickerDialog {
        version = TimePickerDialog.Version.VERSION_2
        accentColor = c.color(R.color.CP)
        enableSeconds(true)
        doVibrate(vib == true)
        setOkColor(c.color(R.color.dialogText))
        setCancelColor(c.color(R.color.dialogText))
        boldFont = R.font.bold
        normalFont = R.font.normal
        return this
    }

    const val sexTypesCount = 5
    fun sexTypes(c: Context): Array<SexType> {
        val names = c.resources.getStringArray(R.array.types)
        return arrayOf(
            SexType(names[0], R.drawable.wet_dream),
            SexType(names[1], R.drawable.masturbation),
            SexType(names[2], R.drawable.oral_sex),
            SexType(names[3], R.drawable.anal_sex),
            SexType(names[4], R.drawable.vaginal_sex),
        )
    }

    fun allowedSexTypes(sp: SharedPreferences) = arrayListOf<Byte>().apply {
        for (s in 0 until sexTypesCount)
            if (sp.getBoolean(Settings.spStatInclude + s, true))
                add(s.toByte())
        if (isEmpty()) addAll((0 until sexTypesCount).map { it.toByte() })
    }

    fun Float.tripleRound(): Float {
        val int = toInt()
        return when {
            (this - int) < 0.33334f -> int.toFloat()
            (this - int) > 0.66666f -> int + 1f
            else -> int.toFloat() + 0.5f
        }
    }

    fun View.onLoad(func: () -> Unit) {
        object : CountDownTimer(5000, 50) {
            override fun onFinish() {}
            override fun onTick(millisUntilFinished: Long) {
                if (height == 0) return
                cancel()
                func()
            }
        }.start()
    }

    data class SexType(val name: String, @DrawableRes val icon: Int)
}
