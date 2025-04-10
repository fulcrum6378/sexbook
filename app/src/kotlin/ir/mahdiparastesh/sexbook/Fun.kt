package ir.mahdiparastesh.sexbook

import android.content.Context
import android.icu.util.Calendar
import android.icu.util.GregorianCalendar
import android.icu.util.TimeZone
import android.os.CountDownTimer
import android.util.LongSparseArray
import android.view.View
import android.widget.EditText
import androidx.core.util.valueIterator
import ir.mahdiparastesh.mcdtp.date.DatePickerDialog
import ir.mahdiparastesh.mcdtp.time.TimePickerDialog
import ir.mahdiparastesh.sexbook.base.BaseActivity
import ir.mahdiparastesh.sexbook.misc.HumanistIranianCalendar
import java.text.DecimalFormat

/** Static fields and methods used everywhere */
object Fun {
    // Latin + Cyrillic font: Balsamiq Sans

    const val DATABASE = "sexbook.db"
    const val INSTA = "https://www.instagram.com/"
    const val MAX_BADGE_CHAR = 6
    const val A_DAY = 86400000L
    const val DISABLED_ALPHA = 0.7f
    val materialTheme = com.google.android.material.R.style.Theme_MaterialComponents_DayNight

    /** Specifies if vibration is enabled. */
    var vib: Boolean? = null

    /** @return the current timestamp */
    fun now() = System.currentTimeMillis()

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

    fun EditText.dbValue(): String? = text.ifBlank { null }?.toString()

    inline fun <T> LongSparseArray<T>.filter(predicate: (T) -> Boolean): ArrayList<T> {
        val al = arrayListOf<T>()
        for (item in valueIterator())
            if (predicate(item))
                al.add(item)
        return al
    }

    fun <T> LongSparseArray<T>.toArrayList(): ArrayList<T> {
        val al = arrayListOf<T>()
        for (item in valueIterator()) al.add(item)
        return al
    }

    fun <T> LongSparseArray<T>.iterator(): Iterator<Pair<Long, T>> =
        object : Iterator<Pair<Long, T>> {
            var index = 0

            override fun hasNext(): Boolean =
                index < size()

            override fun next(): Pair<Long, T> {
                val ret = Pair(keyAt(index), valueAt(index))
                index++
                return ret
            }
        }
}
