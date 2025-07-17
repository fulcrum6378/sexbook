package ir.mahdiparastesh.sexbook.stat

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toolbar
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import ir.mahdiparastesh.hellocharts.model.AbstractChartData
import ir.mahdiparastesh.hellocharts.model.ColumnChartData
import ir.mahdiparastesh.hellocharts.view.ColumnChartView
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.ctrl.Identify
import ir.mahdiparastesh.sexbook.ctrl.Summary
import ir.mahdiparastesh.sexbook.databinding.SingularBinding
import ir.mahdiparastesh.sexbook.stat.base.OneChartActivity
import ir.mahdiparastesh.sexbook.util.ChartTimeframeLength
import ir.mahdiparastesh.sexbook.util.ColumnFactory
import ir.mahdiparastesh.sexbook.util.LongSparseArrayExt.filter
import ir.mahdiparastesh.sexbook.util.NumberUtils
import ir.mahdiparastesh.sexbook.util.StatUtils

class Singular : OneChartActivity<ColumnChartView>(), Toolbar.OnMenuItemClickListener {
    val b: SingularBinding by lazy { SingularBinding.inflate(layoutInflater) }
    private var lastIdentifyCreation = 0L

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
        configureToolbar(b.toolbar, R.string.identify)
        b.toolbar.title = vm.crushKey
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        b.toolbar.inflateMenu(R.menu.singular)
        b.toolbar.setOnMenuItemClickListener(this)
        return true
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.identify -> {
                if ((NumberUtils.now() - lastIdentifyCreation <= 1000L)) return false
                Identify.create<Singular>(this@Singular, vm.crushKey!!)
                lastIdentifyCreation = NumberUtils.now()
            }
        }
        return true
    }

    override suspend fun drawChart(data: AbstractChartData) {
        chartView.columnChartData = data as ColumnChartData
    }
}
