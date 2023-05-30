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
import androidx.annotation.IdRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import ir.mahdiparastesh.mcdtp.date.DatePickerDialog
import ir.mahdiparastesh.mcdtp.time.TimePickerDialog
import ir.mahdiparastesh.sexbook.more.BaseActivity
import ir.mahdiparastesh.sexbook.more.BaseActivity.Companion.night
import ir.mahdiparastesh.sexbook.more.HumanistIranianCalendar

/** Static functions and utilities used everywhere. */
object Fun {
    // Latin + Cyrillic Font: Balsamiq Sans

    const val DATABASE = "sexbook.db"
    const val INSTA = "https://www.instagram.com/"
    const val SORT_BY_NAME = 0
    const val SORT_BY_SUM = 1
    const val SORT_BY_AGE = 2
    const val SORT_BY_HEIGHT = 3
    const val SORT_BY_LAST = 4
    // private const val ADMOB = "com.google.android.gms.ads.MobileAds"

    /** Specifies if vibration is enabled. */
    var vib: Boolean? = null

    /** The number of all the available sex types. */
    const val sexTypesCount = 5

    /** @return the current timestamp */
    fun now() = System.currentTimeMillis()

    /** Creates and executes an explosion effect on this View. */
    fun View.explode(
        c: BaseActivity, dur: Long = 522, @DrawableRes src: Int = R.drawable.button_light,
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

    /**
     * Fills a String with a number and zeroes before it.
     * E.g. 2 -> "02"
     *
     * @param n number
     */
    fun z(n: Int): String {
        val s = n.toString()
        return if (s.length == 1) "0$s" else s
    }

    /**
     * Switches visibility of a View between VISIBLE and GONE
     * @param b false for GONE, defaults to true: VISIBLE
     * @return b
     */
    fun View.vis(b: Boolean = true): Boolean {
        visibility = if (b) View.VISIBLE else View.GONE
        return b
    }

    /** Proper implementation of Vibration in across different supported APIs. */
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

    /** @return human-readable date from this Calendar */
    fun Calendar.fullDate() = "${z(this[Calendar.YEAR])}.${z(this[Calendar.MONTH] + 1)}" +
            ".${z(this[Calendar.DAY_OF_MONTH])}"

    /** @return a Calendar set on this timestamp */
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

    /** @return a random colour for a chart item */
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

    /** Sets the options specific to Sexbook on this DatePickerDialog. */
    fun DatePickerDialog<*>.defaultOptions(c: BaseActivity): DatePickerDialog<*> {
        version = DatePickerDialog.Version.VERSION_1
        accentColor = c.themeColor(com.google.android.material.R.attr.colorPrimary)
        firstDayOfWeek = if (calendarType == HumanistIranianCalendar::class.java)
            Calendar.SATURDAY else Calendar.MONDAY
        doVibrate(vib == true)
        setOkColor(c.themeColor(com.google.android.material.R.attr.colorOnSecondary))
        setCancelColor(c.themeColor(com.google.android.material.R.attr.colorOnSecondary))
        boldFont = R.font.bold
        normalFont = R.font.normal
        return this
    }

    /** Sets the options specific to Sexbook on this TimePickerDialog. */
    fun TimePickerDialog.defaultOptions(c: BaseActivity): TimePickerDialog {
        version = TimePickerDialog.Version.VERSION_2
        accentColor = c.themeColor(com.google.android.material.R.attr.colorPrimary)
        enableSeconds(true)
        doVibrate(vib == true)
        setOkColor(c.themeColor(com.google.android.material.R.attr.colorOnSecondary))
        setCancelColor(c.themeColor(com.google.android.material.R.attr.colorOnSecondary))
        boldFont = R.font.bold
        normalFont = R.font.normal
        return this
    }

    /** @return an Array of all the available sex types */
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

    /** @return a ArrayList of the IDs of the allowed sex types based on shared preferences */
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

    /** Listens for the time when a View is completely loaded and then executes "func". */
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

    fun sort(@IdRes menuItemId: Int): Any = when (menuItemId) {
        R.id.sortByName -> SORT_BY_NAME
        R.id.sortBySum -> SORT_BY_SUM
        R.id.sortByAge -> SORT_BY_AGE
        R.id.sortByHeight -> SORT_BY_HEIGHT
        R.id.sortByLast -> SORT_BY_LAST
        R.id.sortAsc -> true
        R.id.sortDsc -> false
        else -> throw IllegalArgumentException("Unsupported menu item id!")
    }

    fun findSortMenuItemId(sortBy: Int) = when (sortBy) {
        SORT_BY_NAME -> R.id.sortByName
        SORT_BY_SUM -> R.id.sortBySum
        SORT_BY_AGE -> R.id.sortByAge
        SORT_BY_HEIGHT -> R.id.sortByHeight
        SORT_BY_LAST -> R.id.sortByLast
        else -> throw IllegalArgumentException("Invalid sorting method!")
    }

    /**
     * Data class that indicates a sex type.
     * @param name visible name
     * @param icon visible icon from drawable resources
     */
    data class SexType(val name: String, @DrawableRes val icon: Int)
}
