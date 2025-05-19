package ir.mahdiparastesh.sexbook.stat

import android.os.Bundle
import android.widget.TextView
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModel
import ir.mahdiparastesh.hellocharts.model.AbstractChartData
import ir.mahdiparastesh.hellocharts.model.ColumnChartData
import ir.mahdiparastesh.hellocharts.view.ColumnChartView
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.ctrl.Identify
import ir.mahdiparastesh.sexbook.util.LongSparseArrayExt.filter

class Singular : ChartActivity<ColumnChartView>(R.layout.singular) {

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

        findViewById<TextView>(R.id.title).text = mm.crushKey
        findViewById<ConstraintLayout>(R.id.identify).setOnClickListener {
            Identify.create<Singular>(this@Singular, mm.crushKey!!)
        }
    }

    override fun requirements(): Boolean {
        mm.crushKey = intent.getStringExtra(EXTRA_CRUSH_KEY)
        mm.history = c.summary?.scores?.get(mm.crushKey)
        return super.requirements() && mm.crushKey != null && mm.history != null
    }

    override suspend fun draw(): AbstractChartData {
        val data = ArrayList<Pair<String, Float>>()
        StatUtils.sinceTheBeginning(c, c.reports.filter {
            if (it.analysis == null) it.analyse()
            mm.crushKey in it.analysis!!
        }).forEach { data.add(Pair(it, StatUtils.calcHistory(c, mm.history!!, it))) }
        return ColumnChartData().setColumns(ColumnFactory(this, data))
    }

    override suspend fun render(data: AbstractChartData) {
        chartView.columnChartData = data as ColumnChartData
    }
}
