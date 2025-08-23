package ir.mahdiparastesh.sexbook.util

import android.graphics.Color
import android.icu.util.Calendar
import androidx.annotation.ColorInt
import androidx.annotation.IdRes
import ir.mahdiparastesh.hellocharts.model.Column
import ir.mahdiparastesh.hellocharts.model.Line
import ir.mahdiparastesh.hellocharts.model.PointValue
import ir.mahdiparastesh.hellocharts.model.SubColumnValue
import ir.mahdiparastesh.mcdtp.McdtpUtils
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.Sexbook
import ir.mahdiparastesh.sexbook.base.BaseActivity
import ir.mahdiparastesh.sexbook.ctrl.Summary
import ir.mahdiparastesh.sexbook.data.Report
import ir.mahdiparastesh.sexbook.page.Settings
import ir.mahdiparastesh.sexbook.util.LongSparseArrayExt.toArrayList
import ir.mahdiparastesh.sexbook.util.NumberUtils.calendar
import ir.mahdiparastesh.sexbook.util.NumberUtils.roundToNearestHundredth
import ir.mahdiparastesh.sexbook.util.NumberUtils.show
import ir.mahdiparastesh.sexbook.util.StatUtils.randomHue

object StatUtils {

    const val POINT_LABEL_OFFSET_IN_DP = 24
    val hues: IntRange = 0..359

    /** Creates a list of months of the recorded sexual history. */
    fun timeSeries(
        c: Sexbook,
        itemLength: ChartTimeframeLength = ChartTimeframeLength.MONTHLY,
        history: Iterable<Report> = c.reports.toArrayList(),
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

        // create timeframes
        val beg = oldest.calendar(c)
        val list = arrayListOf<String>()
        val yDist = end[Calendar.YEAR] - beg[Calendar.YEAR]
        when (itemLength) {
            ChartTimeframeLength.MONTHLY -> {
                val months = McdtpUtils.localSymbols(c, c.calType()).shortMonths
                for (y in 0 until (yDist + 1)) {
                    var start = 0
                    var finish = 11
                    if (y == 0) start = beg[Calendar.MONTH]
                    if (y == yDist) finish = end[Calendar.MONTH]
                    for (m in start..finish)
                        list.add(monthKey(months[m], beg[Calendar.YEAR] + y))
                }
            }
            ChartTimeframeLength.YEARLY ->
                for (y in 0 until (yDist + 1))
                    list.add((beg[Calendar.YEAR] + y).toString())
        }
        return list
    }

    /** Organises a list of Orgasms according to the given timesframes. */
    fun sumTimeframes(
        c: Sexbook,
        orgasms: ArrayList<Summary.Orgasm>,
        timeframes: List<String>,
        timeframeLength: ChartTimeframeLength,
        cumulative: Boolean = false
    ): LinkedHashMap<String, Float> {

        val map = LinkedHashMap<String, Float>()
        for (timeframe in timeframes) map[timeframe] = 0f
        when (timeframeLength) {
            ChartTimeframeLength.MONTHLY -> {
                val monthNames = McdtpUtils.localSymbols(c, c.calType()).shortMonths
                for (orgasm in orgasms) {
                    val cal = orgasm.time.calendar(c)
                    val yea = cal[Calendar.YEAR]
                    val mon = cal[Calendar.MONTH]
                    val key = monthKey(monthNames[mon], yea)
                    if (key !in map) continue  // because of statSince and/or statUntil
                    map[key] = map[key]!! + orgasm.value
                }
            }
            ChartTimeframeLength.YEARLY ->
                for (orgasm in orgasms) {
                    val cal = orgasm.time.calendar(c)
                    val key = cal[Calendar.YEAR].toString()
                    if (key !in map) continue  // because of statSince and/or statUntil
                    map[key] = map[key]!! + orgasm.value
                }
        }
        if (cumulative) {
            var previous = 0f
            var current: Float
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

enum class ChartTimeframeLength(@IdRes val menuId: Int) {
    MONTHLY(R.id.chartMonthly),
    YEARLY(R.id.chartYearly),
}
