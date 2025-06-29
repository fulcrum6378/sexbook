package ir.mahdiparastesh.sexbook.stat

import android.view.View
import androidx.core.util.isNotEmpty
import ir.mahdiparastesh.hellocharts.model.AbstractChartData
import ir.mahdiparastesh.hellocharts.model.Line
import ir.mahdiparastesh.hellocharts.model.LineChartData
import ir.mahdiparastesh.hellocharts.model.PointValue
import ir.mahdiparastesh.hellocharts.view.LineChartView
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.Settings
import ir.mahdiparastesh.sexbook.databinding.IntervalsBinding
import ir.mahdiparastesh.sexbook.stat.base.OneChartActivity
import ir.mahdiparastesh.sexbook.util.LongSparseArrayExt.toArrayList
import ir.mahdiparastesh.sexbook.util.NumberUtils.calendar
import ir.mahdiparastesh.sexbook.util.NumberUtils.fullDate
import ir.mahdiparastesh.sexbook.view.SexType

class Intervals : OneChartActivity<LineChartView>() {
    val b: IntervalsBinding by lazy { IntervalsBinding.inflate(layoutInflater) }
    private lateinit var allowedSexTypes: List<Byte>

    override fun requirements() = c.reports.isNotEmpty()
    override fun getRootView(): View = b.root

    override suspend fun prepareData(): AbstractChartData {
        val points = arrayListOf<PointValue>()
        var prev: Long? = null
        var i = -1f
        val minima: Long =
            if (!c.sp.getBoolean(Settings.spStatSinceCb, false)) Long.MIN_VALUE
            else c.sp.getLong(Settings.spStatSince, Long.MIN_VALUE)
        val maxima: Long =
            if (!c.sp.getBoolean(Settings.spStatUntilCb, false)) Long.MAX_VALUE
            else c.sp.getLong(Settings.spStatUntil, Long.MAX_VALUE)
        allowedSexTypes = SexType.allowedOnes(c.sp)
        for (org in c.reports.toArrayList().sortedBy { it.time }) {
            if (!org.orgasmed) continue
            if (prev == null || org.time < minima || org.time > maxima || org.type !in allowedSexTypes) {
                prev = org.time
                continue; }
            i += 1f
            val delay = (org.time - prev) / 3600000f
            val iDelay = delay.toInt()
            points.add(
                PointValue(i, delay).setLabel(
                    org.time.calendar(c).fullDate() +
                            getString(R.string.after) +
                            resources.getQuantityString(R.plurals.hour, iDelay, iDelay)
                )
            )
            prev = org.time
        }
        val line = Line(points)
            .setColor(chartColour)
            .setHasLabelsOnlyForSelected(true)
        //.setHasLines(false)
        return LineChartData().setLines(listOf(line))
    }

    override suspend fun drawChart(data: AbstractChartData) {
        chartView.setLabelOffset(dp(StatUtils.POINT_LABEL_OFFSET_IN_DP))
        chartView.lineChartData = data as LineChartData
        chartView.isViewportCalculationEnabled = false
    }
}
