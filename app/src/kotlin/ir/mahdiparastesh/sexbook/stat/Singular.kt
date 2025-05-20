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

    val vm: Model by viewModels()

    companion object {
        const val EXTRA_CRUSH_KEY = "crush_key"
    }

    class Model : ViewModel() {
        var crushKey: String? = null
        var history: ArrayList<Summary.Orgasm>? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        findViewById<TextView>(R.id.title).text = vm.crushKey
        findViewById<ConstraintLayout>(R.id.identify).setOnClickListener {
            Identify.create<Singular>(this@Singular, vm.crushKey!!)
        }
    }

    override fun requirements(): Boolean {
        vm.crushKey = intent.getStringExtra(EXTRA_CRUSH_KEY)
        vm.history = c.summary?.scores?.get(vm.crushKey)
        return super.requirements() && vm.crushKey != null && vm.history != null
    }

    override suspend fun draw(): AbstractChartData {
        return ColumnChartData().setColumns(
            ColumnFactory(
                this, StatUtils.sumTimeFrames(
                    c, vm.history!!, StatUtils.timeSeries(
                        c, c.reports.filter {
                            if (it.analysis == null) it.analyse()
                            vm.crushKey in it.analysis!!
                        }
                    )
                )
            )
        )
    }

    override suspend fun render(data: AbstractChartData) {
        chartView.columnChartData = data as ColumnChartData
    }
}
