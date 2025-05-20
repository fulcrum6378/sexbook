package ir.mahdiparastesh.sexbook.stat

import ir.mahdiparastesh.hellocharts.model.AbstractChartData
import ir.mahdiparastesh.hellocharts.model.LineChartData
import ir.mahdiparastesh.hellocharts.view.LineChartView
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.Settings

class Growth : ChartActivity<LineChartView>(R.layout.growth) {

    override suspend fun draw(): AbstractChartData {
        val lines = ArrayList<Timeline>()
        val frames = StatUtils.timeSeries(c)
        val hideUnsafe =
            c.sp.getBoolean(Settings.spHideUnsafePeople, true) && c.unsafe.isNotEmpty()
        for (x in c.summary!!.scores) {
            if (hideUnsafe && x.key in c.unsafe) continue
            lines.add(Timeline(x.key, StatUtils.sumTimeFrames(c, x.value, frames, true)))
        }
        return LineChartData().setLines(LineFactory(lines))
    }

    override suspend fun render(data: AbstractChartData) {
        chartView.setLabelOffset(dp(StatUtils.POINT_LABEL_OFFSET_IN_DP))
        chartView.lineChartData = data as LineChartData
    }
}
