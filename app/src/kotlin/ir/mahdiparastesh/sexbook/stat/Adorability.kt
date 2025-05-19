package ir.mahdiparastesh.sexbook.stat

import ir.mahdiparastesh.hellocharts.model.AbstractChartData
import ir.mahdiparastesh.hellocharts.model.LineChartData
import ir.mahdiparastesh.hellocharts.view.LineChartView
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.Settings

class Adorability : ChartActivity<LineChartView>(R.layout.adorability) {

    override suspend fun draw(): AbstractChartData {
        val hideUnsafe =
            c.sp.getBoolean(Settings.spHideUnsafePeople, true) && c.unsafe.isNotEmpty()

        val stb = StatUtils.timeSeries(c)
        val stars = ArrayList<Star>()
        for (x in c.summary!!.scores) {
            if (hideUnsafe && x.key in c.unsafe) continue

            val scores = ArrayList<Star.Frame>()
            for (month in stb)
                scores.add(Star.Frame(StatUtils.sumTimeFrame(c, x.value, month), month))
            stars.add(Star(x.key, scores))
        }
        // empty columns should not be removed or all the points will jump to the beginning!
        stars.sortWith(Star.Sort(1))
        stars.sortWith(Star.Sort())
        return LineChartData().setLines(LineFactory(stars))
    }

    override suspend fun render(data: AbstractChartData) {
        chartView.setLabelOffset(dp(20))
        chartView.lineChartData = data as LineChartData
        chartView.isViewportCalculationEnabled = false // never do it before setLineChatData
        // it cannot be zoomed out.
    }
}
