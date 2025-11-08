package ir.mahdiparastesh.sexbook.view

import android.app.PendingIntent
import android.content.Context
import android.icu.util.Calendar
import android.icu.util.GregorianCalendar
import android.icu.util.TimeZone
import android.os.Build
import android.widget.EditText
import com.google.android.material.R
import ir.mahdiparastesh.mcdtp.date.DatePickerDialog
import ir.mahdiparastesh.mcdtp.time.TimePickerDialog
import ir.mahdiparastesh.sexbook.util.HumanistIranianCalendar
import kotlin.math.abs

/** Static UI-related utilities used everywhere */
object UiTools {
    // Latin + Cyrillic font: Balsamiq Sans

    const val MAX_BADGE_CHAR = 6
    val materialTheme = R.style.Theme_MaterialComponents_DayNight

    /** Specifies if vibration is enabled. */
    @Volatile
    var vib: Boolean? = null

    /**
     * A set of 13 colours to be set on a gradient displaying the Hue channel of the HSV color space.
     *
     * ```
     * val arHue = IntArray(13)
     * for (i in 0..12) arHue[i] = Color.HSVToColor(floatArrayOf(i * 30f, 1f, 1f))
     * ```
     */
    val hueWheelGradient = intArrayOf(
        -65536, -32768, -256, -8323328, -16711936, -16711808,
        -16711681, -16744193, -16776961, -8388353, -65281, -65408,
        -65536
    )


    /** Sets the options specific to Sexbook on this [ir.mahdiparastesh.mcdtp.date.DatePickerDialog]. */
    fun DatePickerDialog<*>.defaultOptions(): DatePickerDialog<*> {
        version = DatePickerDialog.Version.VERSION_1
        firstDayOfWeek = if (calendarType == HumanistIranianCalendar::class.java)
            Calendar.SATURDAY else Calendar.MONDAY
        doVibrate(vib == true)
        boldFont = ir.mahdiparastesh.sexbook.R.font.bold
        normalFont = ir.mahdiparastesh.sexbook.R.font.normal
        return this
    }

    /** Sets the options specific to Sexbook on this [ir.mahdiparastesh.mcdtp.time.TimePickerDialog]. */
    fun TimePickerDialog.defaultOptions(): TimePickerDialog {
        version = TimePickerDialog.Version.VERSION_2
        enableSeconds(true)
        doVibrate(vib == true)
        boldFont = ir.mahdiparastesh.sexbook.R.font.bold
        normalFont = ir.mahdiparastesh.sexbook.R.font.normal
        return this
    }

    fun Context.possessiveDeterminer(gender: Int): String = when (gender) {
        1 -> getString(ir.mahdiparastesh.sexbook.R.string.her)
        2 -> getString(ir.mahdiparastesh.sexbook.R.string.his)
        else -> getString(ir.mahdiparastesh.sexbook.R.string.their)
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

    /** Handles mutability flags for Notifications across different Android APIs. */
    fun ntfMutability(bb: Boolean = true): Int = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (bb) PendingIntent.FLAG_MUTABLE else PendingIntent.FLAG_IMMUTABLE
        else ->
            if (bb) PendingIntent.FLAG_UPDATE_CURRENT else PendingIntent.FLAG_IMMUTABLE
    }

    /**
     * Takes an input float between 0 and 1 and returns another float between 0 and 1
     * that gets larger the closer the input is to 0.5.
     */
    fun buldgingTransformer(value: Float): Float =
        0.5f - abs(0.5f - value)
}
