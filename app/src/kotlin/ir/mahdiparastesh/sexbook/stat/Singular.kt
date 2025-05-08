package ir.mahdiparastesh.sexbook.stat

import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import ir.mahdiparastesh.hellocharts.model.AbstractChartData
import ir.mahdiparastesh.hellocharts.model.ColumnChartData
import ir.mahdiparastesh.hellocharts.view.AbstractChartView
import ir.mahdiparastesh.sexbook.ctrl.Identify
import ir.mahdiparastesh.sexbook.databinding.SingularBinding
import ir.mahdiparastesh.sexbook.util.LongSparseArrayExt.filter

class Singular : ChartActivity<SingularBinding>() {
    override val b by lazy { SingularBinding.inflate(layoutInflater) }
    override val chartView: AbstractChartView get() = b.main
    val mm: MyModel by viewModels()

    companion object {
        const val EXTRA_CRUSH_KEY = "crush_key"
    }

    class MyModel : ViewModel() {
        var crushKey: String? = null
        var history: ArrayList<Summary.Orgasm>? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (night) b.identifyIV.colorFilter = themePdcf()

        b.title.text = mm.crushKey
        b.identify.setOnClickListener { Identify.create<Singular>(this@Singular, mm.crushKey!!) }
    }

    override fun requirements(): Boolean {
        mm.crushKey = intent.getStringExtra(EXTRA_CRUSH_KEY)
        mm.history = c.summary?.scores?.get(mm.crushKey)
        return super.requirements() && mm.crushKey != null && mm.history != null
    }

    override suspend fun draw(): AbstractChartData {
        val data = ArrayList<Pair<String, Float>>()
        sinceTheBeginning(c, c.reports.filter {
            if (it.analysis == null) it.analyse()
            mm.crushKey in it.analysis!!
        }).forEach { data.add(Pair(it, calcHistory(c, mm.history!!, it))) }
        return ColumnChartData().setColumns(ColumnFactory(this, data))
    }

    override suspend fun render(data: AbstractChartData) {
        b.main.columnChartData = data as ColumnChartData
    }
}
