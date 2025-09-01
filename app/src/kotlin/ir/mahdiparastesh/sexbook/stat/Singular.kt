package ir.mahdiparastesh.sexbook.stat

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toolbar
import androidx.activity.viewModels
import androidx.core.util.valueIterator
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
import ir.mahdiparastesh.sexbook.util.StatUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Singular : OneChartActivity<ColumnChartView>(), Toolbar.OnMenuItemClickListener {
    val b: SingularBinding by lazy { SingularBinding.inflate(layoutInflater) }
    private var tbMenu: Menu? = null

    override fun getRootView(): View = b.root

    val vm: Model by viewModels()

    companion object {
        const val EXTRA_CRUSH_KEY = "crush_key"
    }

    class Model : ViewModel() {
        var crushKey: String? = null
        var history: ArrayList<Summary.Orgasm>? = null
        val otherPartners: ArrayList<OtherPartners.Item> = arrayListOf()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureToolbar(b.toolbar, R.string.identify)
        b.toolbar.title = vm.crushKey

        // check if there are Other Partners
        CoroutineScope(Dispatchers.Default).launch {
            val crk = vm.crushKey
            val map = hashMapOf<String, OtherPartners.Item>()
            for (r in c.reports.valueIterator()) {
                if (r.guess || r.analysis?.none { it.equals(crk, true) } != false)
                    continue
                r.analysis!!.filter {
                    !it.equals(crk, true)
                }.forEach { other ->
                    val otherLC = other.lowercase()
                    if (!map.containsKey(otherLC)) {
                        var identified: String? = null
                        for (p in c.people.keys)
                            if (p.equals(other, true)) {
                                identified = p
                                break
                            }
                        map[otherLC] = OtherPartners.Item(identified ?: other, 1)
                    } else
                        map[otherLC]!!.times += 1
                }
            }
            vm.otherPartners.clear()
            vm.otherPartners.addAll(map.values)
            vm.otherPartners.sortBy { it.name }
            vm.otherPartners.sortByDescending { it.times }

            if (tbMenu != null && map.isNotEmpty()) withContext(Dispatchers.Main) {
                tbMenu?.findItem(R.id.otherPartners)?.isVisible = true
            }
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        b.toolbar.inflateMenu(R.menu.singular)
        b.toolbar.setOnMenuItemClickListener(this)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        tbMenu = menu!!
        if (vm.otherPartners.isNotEmpty()) menu.findItem(R.id.otherPartners)?.isVisible = true
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.identify -> Identify.create<Singular>(this, vm.crushKey!!)
            R.id.otherPartners -> OtherPartners.create(this, vm.crushKey!!)
        }
        return true
    }

    override suspend fun drawChart(data: AbstractChartData) {
        chartView.columnChartData = data as ColumnChartData
    }
}
