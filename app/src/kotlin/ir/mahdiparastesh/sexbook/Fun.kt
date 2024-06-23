package ir.mahdiparastesh.sexbook

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.icu.util.Calendar
import android.icu.util.GregorianCalendar
import android.icu.util.TimeZone
import android.os.Build
import android.os.CountDownTimer
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import ir.mahdiparastesh.mcdtp.date.DatePickerDialog
import ir.mahdiparastesh.mcdtp.time.TimePickerDialog
import ir.mahdiparastesh.sexbook.base.BaseActivity
import ir.mahdiparastesh.sexbook.base.BaseActivity.Companion.night
import ir.mahdiparastesh.sexbook.more.HumanistIranianCalendar
import java.text.DecimalFormat

/** Static functions and utilities used everywhere. */
object Fun {
    // Latin + Cyrillic Font: Balsamiq Sans

    const val DATABASE = "sexbook.db"
    const val INSTA = "https://www.instagram.com/"
    const val SORT_BY_NAME = 0
    const val SORT_BY_SUM = 1
    const val SORT_BY_AGE = 2
    const val SORT_BY_HEIGHT = 3
    const val SORT_BY_BEGINNING = 4
    const val SORT_BY_LAST = 5
    const val SORT_BY_FIRST = 6
    const val MAX_BADGE_CHAR = 6
    const val A_DAY = 86400000L
    val materialTheme = com.google.android.material.R.style.Theme_MaterialComponents_DayNight
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
        vib.vibrate(VibrationEffect.createOneShot(dur, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    /** @return human-readable date from this Calendar */
    fun Calendar.fullDate() = "${z(this[Calendar.YEAR])}.${z(this[Calendar.MONTH] + 1)}" +
            ".${z(this[Calendar.DAY_OF_MONTH])}"

    /** @return a Calendar set on this timestamp */
    fun Long.calendar(c: BaseActivity): Calendar =
        c.calType().getDeclaredConstructor().newInstance().apply { timeInMillis = this@calendar }

    fun Calendar.createFilterYm() = Pair(this[Calendar.YEAR], this[Calendar.MONTH])

    fun Long.defCalendar(c: BaseActivity): Calendar =
        c.calType().getDeclaredConstructor().newInstance().apply {
            timeInMillis = this@defCalendar
            this[Calendar.HOUR_OF_DAY] = 0
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
    fun DatePickerDialog<*>.defaultOptions(): DatePickerDialog<*> {
        version = DatePickerDialog.Version.VERSION_1
        firstDayOfWeek = if (calendarType == HumanistIranianCalendar::class.java)
            Calendar.SATURDAY else Calendar.MONDAY
        doVibrate(vib == true)
        boldFont = R.font.bold
        normalFont = R.font.normal
        return this
    }

    /** Sets the options specific to Sexbook on this TimePickerDialog. */
    fun TimePickerDialog.defaultOptions(): TimePickerDialog {
        version = TimePickerDialog.Version.VERSION_2
        enableSeconds(true)
        doVibrate(vib == true)
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

    fun sort(@IdRes menuItemId: Int): Any? = when (menuItemId) {
        R.id.sortByName -> SORT_BY_NAME
        R.id.sortBySum -> SORT_BY_SUM
        R.id.sortByAge -> SORT_BY_AGE
        R.id.sortByHeight -> SORT_BY_HEIGHT
        R.id.sortByBeginning -> SORT_BY_BEGINNING
        R.id.sortByLast -> SORT_BY_LAST
        R.id.sortByFirst -> SORT_BY_FIRST
        R.id.sortAsc -> true
        R.id.sortDsc -> false
        else -> null
    }

    fun findSortMenuItemId(sortBy: Int) = when (sortBy) {
        SORT_BY_NAME -> R.id.sortByName
        SORT_BY_SUM -> R.id.sortBySum
        SORT_BY_AGE -> R.id.sortByAge
        SORT_BY_HEIGHT -> R.id.sortByHeight
        SORT_BY_BEGINNING -> R.id.sortByBeginning
        SORT_BY_LAST -> R.id.sortByLast
        SORT_BY_FIRST -> R.id.sortByFirst
        else -> throw IllegalArgumentException("Invalid sorting method!")
    }

    /** @return another instance of Calendar having the same date in the default calendar type */
    fun GregorianCalendar.toDefaultType(c: BaseActivity): Calendar {
        return c.calType().getDeclaredConstructor().newInstance()
            .apply { this@apply.timeInMillis = this@toDefaultType.timeInMillis }
    }

    fun Float.show(): String =
        if (this % 1 > 0) DecimalFormat("#.##").format(this) else toInt().toString()

    inline fun <T> Iterable<T>.sumOf(selector: (T) -> Float): Float {
        var sum = 0f
        for (element in this) sum += selector(element)
        return sum
    }

    fun Context.possessiveDeterminer(gender: Int): String = when (gender) {
        1 -> getString(R.string.her)
        2 -> getString(R.string.his)
        else -> getString(R.string.their)
    }

    /** 1=>year / 2=>month / 3=>day   4=>hour : 5=>minute : 6=>second */
    fun validateDateTime(raw: String): String {
        if (raw.isBlank()) return "0000/00/00 00:00:00"
        var put = ""
        var field = 1
        for (ch in raw) if (!ch.isDigit()) field++
        if (field > 6) field = 6
        if (field != 6) {
            for (f in 6 downTo (field + 1)) put = when (f) {
                2 -> "/00"
                3 -> "/00"
                4 -> " 00"
                else -> ":00"
            } + put
        }

        var digitCount = 0
        for (ch in raw.reversed())
            if (ch.isDigit()) {
                if (field != 1 && digitCount >= 2) continue
                put = ch + put
                digitCount++
            } else { // `field` always be >=2 on valid inputs
                //if (digitCount == 0) continue
                if (field != 1 && digitCount < 2)
                    repeat(2 - digitCount) { put = "0$put" }
                digitCount = 0

                if (field != 1) put = when (field) {
                    2, 3 -> '/'
                    4 -> ' '
                    /*5, 6*/ else -> ':'
                } + put
                field--
                if (field == 0) break
            }
        if (field == 1 && digitCount < 4)
            repeat(4 - digitCount) { put = "0$put" }
        return put
    }

    fun compressDateTime(full: String): String? {
        if (full.isBlank()) return null
        var put = ""
        var field = 6
        var cur = full.length
        var hitNonZero = false
        var num: Int
        while (cur != 0) {
            if (field != 1) {
                num = full.substring(cur - 2, cur).toInt()
                if (num != 0) {
                    put = num.toString() + put
                    hitNonZero = true
                }
                if (hitNonZero) put = full[cur - 3] + put
                cur -= 3
            } else {
                num = full.substring(0, cur).toInt()
                if (num != 0) {
                    put = num.toString() + put
                    hitNonZero = true
                }
                cur = 0
            }
            field--
        }
        return if (hitNonZero) put else null
    }

    fun compDateTimeToCalendar(comp: String, tz: TimeZone? = null): GregorianCalendar {
        val cal = GregorianCalendar(1970, 0, 1, 0, 0, 0)
        cal[Calendar.MILLISECOND] = 0
        tz?.also { cal.timeZone = it }
        var field = 1
        var beg = 0
        var end = 0
        var sub: String
        for (ch in "$comp ") {
            if (ch.isDigit()) {
                end++
                continue; }
            sub = comp.substring(beg, end)
            if (sub.isNotEmpty()) cal.set(
                when (field) {
                    1 -> Calendar.YEAR
                    2 -> Calendar.MONTH
                    3 -> Calendar.DAY_OF_MONTH
                    4 -> Calendar.HOUR
                    5 -> Calendar.MINUTE
                    /*6*/ else -> Calendar.SECOND
                }, sub.toInt() - (if (field == 2) 1 else 0)
            )
            end++
            beg = end
            field++
        }
        return cal
    }

    /**
     * Data class that indicates a sex type.
     * @param name visible name
     * @param icon visible icon from drawable resources
     */
    data class SexType(val name: String, @DrawableRes val icon: Int)
}
