package ir.mahdiparastesh.sexbook.stat

import androidx.core.util.isNotEmpty
import ir.mahdiparastesh.hellocharts.model.AbstractChartData
import ir.mahdiparastesh.hellocharts.model.ColumnChartData
import ir.mahdiparastesh.hellocharts.view.ColumnChartView
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.util.LongSparseArrayExt.filter
import ir.mahdiparastesh.sexbook.view.SexType

class Mixture : ChartActivity<ColumnChartView>(R.layout.mixture) {

    override fun requirements() = c.reports.isNotEmpty()

    override suspend fun draw(): AbstractChartData {
        val history = arrayListOf<Summary.Orgasm>()
        val allowedTypes = SexType.allowedOnes(c.sp)
        for (o in c.reports.let {
            if (allowedTypes.size < SexType.count)
                it.filter { r -> r.type in allowedTypes && r.ogsm }
            else it.filter { r -> r.ogsm }  // do not simplify
        }) history.add(Summary.Orgasm(o.time, 1f))

        return ColumnChartData().setColumns(
            ColumnFactory(
                this, StatUtils.sumTimeFrames(c, history, StatUtils.timeSeries(c)), true
            )
        )
    }

    override suspend fun render(data: AbstractChartData) {
        chartView.columnChartData = data as ColumnChartData
    }
}
