package ir.mahdiparastesh.sexbook.stat

import ir.mahdiparastesh.hellocharts.model.AbstractChartData
import ir.mahdiparastesh.hellocharts.model.Line
import ir.mahdiparastesh.hellocharts.model.LineChartData
import ir.mahdiparastesh.hellocharts.model.PointValue
import ir.mahdiparastesh.hellocharts.view.AbstractChartView
import ir.mahdiparastesh.sexbook.Fun.calendar
import ir.mahdiparastesh.sexbook.Fun.fullDate
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.databinding.IntervalsBinding

class Intervals : ChartActivity<IntervalsBinding>() {
    override val b by lazy { IntervalsBinding.inflate(layoutInflater) }
    override val chartView: AbstractChartView get() = b.main

    override suspend fun draw(): AbstractChartData {
        val points = arrayListOf<PointValue>()
        var prev: Long? = null
        var i = -1f
        for (org in m.onani.value!!) {
            i += 1f
            if (prev == null) {
                prev = org.time
                continue; }
            val delay = (org.time - prev) / 3600000
            points.add(
                PointValue(i, delay.toFloat()).setLabel(
                    org.time.calendar(this).fullDate() + getString(R.string.afterNHours, delay)
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
