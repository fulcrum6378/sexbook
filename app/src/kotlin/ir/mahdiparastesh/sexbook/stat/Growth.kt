package ir.mahdiparastesh.sexbook.stat

import android.view.View
import androidx.core.util.isNotEmpty
import ir.mahdiparastesh.hellocharts.model.AbstractChartData
import ir.mahdiparastesh.hellocharts.model.LineChartData
import ir.mahdiparastesh.hellocharts.view.LineChartView
import ir.mahdiparastesh.sexbook.Settings
import ir.mahdiparastesh.sexbook.databinding.GrowthBinding

class Growth : OneChartActivity<LineChartView>() {
    private val b: GrowthBinding by lazy { GrowthBinding.inflate(layoutInflater) }

    override fun requirements(): Boolean = c.reports.isNotEmpty() && c.summary != null
    override fun getRootView(): View = b.root

    override suspend fun prepareData(): AbstractChartData {
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

    override suspend fun drawChart(data: AbstractChartData) {
        chartView.setLabelOffset(dp(StatUtils.POINT_LABEL_OFFSET_IN_DP))
        chartView.lineChartData = data as LineChartData
    }
}
