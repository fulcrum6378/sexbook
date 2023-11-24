package ir.mahdiparastesh.sexbook.stat

import ir.mahdiparastesh.hellocharts.model.AbstractChartData
import ir.mahdiparastesh.hellocharts.model.Line
import ir.mahdiparastesh.hellocharts.model.LineChartData
import ir.mahdiparastesh.hellocharts.model.PointValue
import ir.mahdiparastesh.hellocharts.view.AbstractChartView
import ir.mahdiparastesh.sexbook.Fun.calendar
import ir.mahdiparastesh.sexbook.Fun.fullDate
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.Settings
import ir.mahdiparastesh.sexbook.databinding.IntervalsBinding

class Intervals : ChartActivity<IntervalsBinding>() {
    override val b by lazy { IntervalsBinding.inflate(layoutInflater) }
    override val chartView: AbstractChartView get() = b.main

    override suspend fun draw(): AbstractChartData {
        val points = arrayListOf<PointValue>()
        var prev: Long? = null
        var i = -1f
        val minima: Long =
            if (!sp.getBoolean(Settings.spStatSinceCb, false)) Long.MIN_VALUE
            else sp.getLong(Settings.spStatSince, Long.MIN_VALUE)
        val maxima: Long =
            if (!sp.getBoolean(Settings.spStatUntilCb, false)) Long.MAX_VALUE
            else sp.getLong(Settings.spStatUntil, Long.MAX_VALUE)
        for (org in m.onani.value!!) {
            if (prev == null || org.time < minima || org.time > maxima) {
                prev = org.time
                continue; }
            i += 1f
            val delay = (org.time - prev) / 3600000f
            points.add(
                PointValue(i, delay).setLabel(
                    org.time.calendar(this).fullDate() +
                            getString(R.string.afterNHours, delay.toInt())
                )
            )
            prev = org.time
        }
        val line = Line(points)
            .setColor(
                if (!night()) themeColor(com.google.android.material.R.attr.colorPrimary)
                else color(R.color.CPV_LIGHT)
            )
            .setHasLabelsOnlyForSelected(true)
        return LineChartData().setLines(listOf(line))
    }

    override suspend fun render(data: AbstractChartData) {
        b.main.lineChartData = data as LineChartData
        b.main.isViewportCalculationEnabled = false
    }

    override fun requirements() = m.onani.value != null
}
