package ir.mahdiparastesh.sexbook.util

import android.icu.util.Calendar
import ir.mahdiparastesh.sexbook.Sexbook

/** Static number-related utilities used everywhere */
object NumberUtils {

    const val A_DAY = 86400000L
    const val DISABLED_ALPHA = 0.7f

    /** @return current timestamp in milliseconds */
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

    /** @return human-readable date from this [Calendar] */
    fun Calendar.fullDate() = "${z(this[Calendar.YEAR])}.${z(this[Calendar.MONTH] + 1)}" +
            ".${z(this[Calendar.DAY_OF_MONTH])}"

    /** @return a [Calendar] set on this timestamp */
    fun Long.calendar(c: Sexbook): Calendar =
        c.calType().getDeclaredConstructor().newInstance().apply { timeInMillis = this@calendar }

    fun Calendar.createFilterYm() = Pair(this[Calendar.YEAR], this[Calendar.MONTH])

    fun Long.defCalendar(c: Sexbook): Calendar =
        c.calType().getDeclaredConstructor().newInstance().apply {
            timeInMillis = this@defCalendar
            this[Calendar.HOUR_OF_DAY] = 0
            this[Calendar.MINUTE] = 0
            this[Calendar.SECOND] = 0
            this[Calendar.MILLISECOND] = 0
        }

    @Suppress("ReplaceJavaStaticMethodWithKotlinAnalog")
    fun Float.roundToNearestHundredth() =
        Math.round(this * 100f).toFloat() / 100f

    fun Float.show(): String =
        if (this % 1 > 0) toString() else toInt().toString()
    // DecimalFormat("#.##").format(this)
}
