package ir.mahdiparastesh.sexbook.stat

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModel
import ir.mahdiparastesh.hellocharts.model.AbstractChartData
import ir.mahdiparastesh.hellocharts.model.ColumnChartData
import ir.mahdiparastesh.hellocharts.view.ColumnChartView
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.ctrl.Identify
import ir.mahdiparastesh.sexbook.databinding.SingularBinding
import ir.mahdiparastesh.sexbook.stat.base.OneChartActivity
import ir.mahdiparastesh.sexbook.util.LongSparseArrayExt.filter

class Singular : OneChartActivity<ColumnChartView>() {
    val b: SingularBinding by lazy { SingularBinding.inflate(layoutInflater) }

    override fun getRootView(): View = b.root

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
        vm.history = c.summary?.scores?.get(vm.crushKey)?.orgasms
        return vm.crushKey != null && vm.history != null
    }

    override suspend fun prepareData(): AbstractChartData {
        val timeframeLength = ChartTimeframeLength.MONTHLY
        return ColumnChartData().setColumns(
            ColumnFactory(
                this, StatUtils.sumTimeframes(
                    c, vm.history!!,
                    StatUtils.timeSeries(
                        c, timeframeLength, c.reports.filter {
                            if (it.analysis == null) it.analyse()
                            vm.crushKey in it.analysis!!
                        }
                    ),
                    timeframeLength
                )
            )
        )
    }

    override suspend fun drawChart(data: AbstractChartData) {
        chartView.columnChartData = data as ColumnChartData
    }
}
