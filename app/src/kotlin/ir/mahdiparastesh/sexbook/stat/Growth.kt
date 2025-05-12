package ir.mahdiparastesh.sexbook.stat

import ir.mahdiparastesh.hellocharts.model.AbstractChartData
import ir.mahdiparastesh.hellocharts.model.LineChartData
import ir.mahdiparastesh.hellocharts.view.LineChartView
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.Settings

class Growth : ChartActivity<LineChartView>(R.layout.growth) {

    override suspend fun draw(): AbstractChartData {
        val hideUnsafe =
            c.sp.getBoolean(Settings.spHideUnsafePeople, true) && c.unsafe.isNotEmpty()

        val stb = sinceTheBeginning(c)
        val stars = ArrayList<Star>()
        for (x in c.summary!!.scores) {
            if (hideUnsafe && x.key in c.unsafe) continue

            val frames = ArrayList<Star.Frame>()
            for (month in stb)
                frames.add(Star.Frame(calcHistory(c, x.value, month, true), month))
            stars.add(Star(x.key, frames.toTypedArray()))
        }
        stars.sortWith(Star.Sort(1))
        stars.sortWith(Star.Sort())
        return LineChartData().setLines(LineFactory(stars))
    }

    override suspend fun render(data: AbstractChartData) {
        chartView.setLabelOffset(dp(20))
        chartView.lineChartData = data as LineChartData
    }
}
