package ir.mahdiparastesh.sexbook.stat

import ir.mahdiparastesh.hellocharts.model.AbstractChartData
import ir.mahdiparastesh.hellocharts.model.ColumnChartData
import ir.mahdiparastesh.hellocharts.view.AbstractChartView
import ir.mahdiparastesh.sexbook.Fun
import ir.mahdiparastesh.sexbook.databinding.MixtureBinding

class Mixture : ChartActivity<MixtureBinding>() {
    override val b by lazy { MixtureBinding.inflate(layoutInflater) }
    override val chartView: AbstractChartView get() = b.main

    override fun requirements() = c.reports.isNotEmpty()

    override suspend fun draw(): AbstractChartData {
        val data = ArrayList<Pair<String, Float>>()
        val history = arrayListOf<Summary.Orgasm>()
        val allowedTypes = Fun.allowedSexTypes(c.sp)
        for (o in c.reports.values.let {
            if (allowedTypes.size < Fun.sexTypesCount)
                it.filter { r -> r.type in allowedTypes && r.ogsm }
            else it.filter { r -> r.ogsm } // do not simplify
        }) history.add(Summary.Orgasm(o.time, 1f))
        sinceTheBeginning(this)
            .forEach { data.add(Pair(it, calcHistory(this, history, it))) }
        return ColumnChartData().setColumns(ColumnFactory(this, data, true))
    }

    override suspend fun render(data: AbstractChartData) {
        b.main.columnChartData = data as ColumnChartData
    }
}
