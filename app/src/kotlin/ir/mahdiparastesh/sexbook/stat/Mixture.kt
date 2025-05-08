package ir.mahdiparastesh.sexbook.stat

import androidx.core.util.isNotEmpty
import ir.mahdiparastesh.hellocharts.model.AbstractChartData
import ir.mahdiparastesh.hellocharts.model.ColumnChartData
import ir.mahdiparastesh.hellocharts.view.AbstractChartView
import ir.mahdiparastesh.sexbook.databinding.MixtureBinding
import ir.mahdiparastesh.sexbook.util.LongSparseArrayExt.filter
import ir.mahdiparastesh.sexbook.view.SexType

class Mixture : ChartActivity<MixtureBinding>() {
    override val b by lazy { MixtureBinding.inflate(layoutInflater) }
    override val chartView: AbstractChartView get() = b.main

    override fun requirements() = c.reports.isNotEmpty()

    override suspend fun draw(): AbstractChartData {
        val data = ArrayList<Pair<String, Float>>()
        val history = arrayListOf<Summary.Orgasm>()
        val allowedTypes = SexType.allowedOnes(c.sp)
        for (o in c.reports.let {
            if (allowedTypes.size < SexType.count)
                it.filter { r -> r.type in allowedTypes && r.ogsm }
            else it.filter { r -> r.ogsm } // do not simplify
        }) history.add(Summary.Orgasm(o.time, 1f))
        sinceTheBeginning(c)
            .forEach { data.add(Pair(it, calcHistory(c, history, it))) }
        return ColumnChartData().setColumns(ColumnFactory(this, data, true))
    }

    override suspend fun render(data: AbstractChartData) {
        b.main.columnChartData = data as ColumnChartData
    }
}
