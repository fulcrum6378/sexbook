package ir.mahdiparastesh.sexbook.stat

import android.graphics.Color
import android.icu.util.Calendar
import androidx.annotation.ColorInt
import ir.mahdiparastesh.hellocharts.model.Column
import ir.mahdiparastesh.hellocharts.model.Line
import ir.mahdiparastesh.hellocharts.model.PointValue
import ir.mahdiparastesh.hellocharts.model.SubColumnValue
import ir.mahdiparastesh.mcdtp.McdtpUtils
import ir.mahdiparastesh.sexbook.Settings
import ir.mahdiparastesh.sexbook.Sexbook
import ir.mahdiparastesh.sexbook.base.BaseActivity
import ir.mahdiparastesh.sexbook.data.Report
import ir.mahdiparastesh.sexbook.util.LongSparseArrayExt.toArrayList
import ir.mahdiparastesh.sexbook.util.NumberUtils.calendar

object StatUtils {

    /** Creates a list of months of the recorded sexual history. */
    fun timeSeries(
        c: Sexbook, history: Iterable<Report> = c.reports.toArrayList()
    ): List<String> {

        // find the ending
        var end = c.calType().getDeclaredConstructor().newInstance()
        var oldest = end.timeInMillis
        if (c.sp.getBoolean(Settings.spStatUntilCb, false)) {
            val statTill = c.sp.getLong(Settings.spStatUntil, Long.MAX_VALUE)
            var newest = 0L
            for (h in history) if (h.time in (newest + 1)..statTill) newest = h.time
            if (newest != 0L) end = newest.calendar(c)
        }

        // find the beginning
        if (c.sp.getBoolean(Settings.spStatSinceCb, false)) {
            val statSinc = c.sp.getLong(Settings.spStatSince, Long.MIN_VALUE)
            for (h in history) if (h.time in statSinc until oldest) oldest = h.time
        } else for (h in history) if (h.time < oldest) oldest = h.time

        val beg = oldest.calendar(c)
        val list = arrayListOf<String>()
        val yDist = end[Calendar.YEAR] - beg[Calendar.YEAR]
        for (y in 0 until (yDist + 1)) {
            var start = 0
            var finish = 11
            if (y == 0) start = beg[Calendar.MONTH]
            if (y == yDist) finish = end[Calendar.MONTH]
            for (m in start..finish) list.add(
                "${McdtpUtils.localSymbols(c, c.calType()).shortMonths[m]} " +
                        "${beg[Calendar.YEAR] + y}"
            )
        }
        return list
    }

    /** Filters a month of sex records out of a list. */
    fun sumTimeFrame(
        c: Sexbook, list: ArrayList<Summary.Orgasm>, month: String,
        growing: Boolean = false
    ): Float {
        var value = 0f
        val split = month.split(" ")
        val months = McdtpUtils.localSymbols(c, c.calType()).shortMonths
        for (i in list) {
            val lm = i.time.calendar(c)
            val yea = lm[Calendar.YEAR]
            val mon = lm[Calendar.MONTH]
            if (months.indexOf(split[0]) == mon && split[1].toInt() == yea) value += i.value
            if (growing && (split[1].toInt() > yea ||
                        (split[1].toInt() == yea && months.indexOf(split[0]) > mon))
            ) value += i.value
        }
        return value
    }
}

class ColumnFactory(
    c: BaseActivity,
    list: ArrayList<Pair<String, Float>>,
    hasLabelsOnlyForSelected: Boolean = false
) : ArrayList<Column>(
    list.map {
        Column(
            listOf(
                SubColumnValue(it.second)
                    .setLabel("${it.first} (${it.second})")
                    .setColor(c.chartColour)
            )
        )
            .setHasLabels(true)
            .setHasLabelsOnlyForSelected(hasLabelsOnlyForSelected)
    }
)

class LineFactory(stars: List<Star>) : ArrayList<Line>(
    stars.map {
        Line(it.frames.mapIndexed { i, frame ->
            PointValue(i.toFloat(), frame.score)
                .setLabel("${it.name} : ${frame.month} (${frame.score})")
        })
            .setColor(
                it.colour
                    ?: Color.HSVToColor(255, floatArrayOf((0..359).random().toFloat(), 1f, 1f))
            )
            .setCubic(true)
            .setHasLabelsOnlyForSelected(true)
    }
)

class Star(
    val name: String,
    val frames: List<Frame>,
    @ColorInt val colour: Int? = null
) {

    class Sort(private val by: Int = 0) : Comparator<Star> {
        override fun compare(a: Star, b: Star) = when (by) {
            1 -> a.name.compareTo(b.name)
            else -> b.frames.sumOf { (it.score * 100f).toInt() } -
                    a.frames.sumOf { (it.score * 100f).toInt() }
        }
    }

    data class Frame(val score: Float, val month: String)
}
