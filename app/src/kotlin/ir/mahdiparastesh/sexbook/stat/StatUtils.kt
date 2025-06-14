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
import ir.mahdiparastesh.sexbook.stat.StatUtils.randomHue
import ir.mahdiparastesh.sexbook.util.LongSparseArrayExt.toArrayList
import ir.mahdiparastesh.sexbook.util.NumberUtils.calendar
import ir.mahdiparastesh.sexbook.util.NumberUtils.roundToNearestHundredth
import ir.mahdiparastesh.sexbook.util.NumberUtils.show

object StatUtils {

    const val POINT_LABEL_OFFSET_IN_DP = 24
    val hues: IntRange = 0..359

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
            for (h in history)
                if (h.time in (newest + 1)..statTill)
                    newest = h.time
            if (newest != 0L) end = newest.calendar(c)
        }

        // find the beginning
        if (c.sp.getBoolean(Settings.spStatSinceCb, false)) {
            val statSinc = c.sp.getLong(Settings.spStatSince, Long.MIN_VALUE)
            for (h in history) if (h.time in statSinc until oldest) oldest = h.time
        } else
            for (h in history) if (h.time < oldest) oldest = h.time

        val beg = oldest.calendar(c)
        val list = arrayListOf<String>()
        val yDist = end[Calendar.YEAR] - beg[Calendar.YEAR]
        val months = McdtpUtils.localSymbols(c, c.calType()).shortMonths
        for (y in 0 until (yDist + 1)) {
            var start = 0
            var finish = 11
            if (y == 0) start = beg[Calendar.MONTH]
            if (y == yDist) finish = end[Calendar.MONTH]
            for (m in start..finish)
                list.add(monthKey(months[m], beg[Calendar.YEAR] + y))
        }
        return list
    }

    /** Filters a month of sex records out of a list. */
    fun sumTimeFrames(
        c: Sexbook,
        orgasms: ArrayList<Summary.Orgasm>,
        frames: List<String>,
        cumulative: Boolean = false
    ): LinkedHashMap<String, Float> {

        val map = LinkedHashMap<String, Float>()
        for (frame in frames) map[frame] = 0f
        val monthNames = McdtpUtils.localSymbols(c, c.calType()).shortMonths
        for (orgasm in orgasms) {
            val lm = orgasm.time.calendar(c)
            val yea = lm[Calendar.YEAR]
            val mon = lm[Calendar.MONTH]
            val monthKey = monthKey(monthNames[mon], yea)
            map[monthKey] = map[monthKey]!! + orgasm.value
        }
        if (cumulative) {
            var previous = 0f
            var current = 0f
            for (key in map.keys) {
                current = map[key]!! + previous
                map[key] = current.roundToNearestHundredth()
                previous = current
            }
        }
        return map
    }

    fun monthKey(shortMonth: String, year: Int) = "$shortMonth $year"

    fun randomHue(): Int = hues.random()
}

class ColumnFactory(
    c: BaseActivity,
    map: LinkedHashMap<String, Float>,
    hasLabelsOnlyForSelected: Boolean = false
) : ArrayList<Column>(
    map.map {
        Column(
            listOf(
                SubColumnValue(it.value)
                    .setLabel("${it.key} (${it.value.show()})")
                    .setColor(c.chartColour)
            )
        )
            .setHasLabels(true)
            .setHasLabelsOnlyForSelected(hasLabelsOnlyForSelected)
    }
)

class LineFactory(stars: List<Timeline>) : ArrayList<Line>(
    stars.let { stars ->
        val firstColours = (0..35).map { it * 10 }.shuffled()
        var i: Int
        var hue: Int
        var colour: Int

        stars.mapIndexed { s, star ->
            i = -1
            colour = if (star.colour != null)
                star.colour
            else {
                hue = if (s < firstColours.size)  // 36 first stars
                    firstColours[s]
                else
                    randomHue()
                Color.HSVToColor(255, floatArrayOf(hue.toFloat(), 1f, 1f))
            }

            Line(star.line.map { line ->
                i++
                PointValue(i.toFloat(), line.value)
                    .setLabel("${star.name} : ${line.key} (${line.value.show()})")
            })
                .setColor(colour)
                .setCubic(true)
                .setHasLabelsOnlyForSelected(true)
        }
    }
)

/**
 * A timeline of [Report]s with a specific person
 *
 * @param name of the person
 * @param line points in the timeline
 * @param sum total number of [Report]s
 * @param colour a special colour for this person if exists
 */
data class Timeline(
    val name: String,
    val line: LinkedHashMap<String, Float>,
    val sum: Float,
    @ColorInt val colour: Int? = null,
)
