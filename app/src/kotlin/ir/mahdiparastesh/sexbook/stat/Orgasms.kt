package ir.mahdiparastesh.sexbook.stat

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toolbar
import androidx.activity.viewModels
import androidx.annotation.ColorInt
import androidx.annotation.IdRes
import androidx.core.util.isNotEmpty
import androidx.lifecycle.ViewModel
import ir.mahdiparastesh.hellocharts.model.AbstractChartData
import ir.mahdiparastesh.hellocharts.model.ColumnChartData
import ir.mahdiparastesh.hellocharts.model.ComboLineColumnChartData
import ir.mahdiparastesh.hellocharts.model.Line
import ir.mahdiparastesh.hellocharts.model.LineChartData
import ir.mahdiparastesh.hellocharts.model.PointValue
import ir.mahdiparastesh.hellocharts.view.ComboLineColumnChartView
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.Sexbook
import ir.mahdiparastesh.sexbook.ctrl.Summary
import ir.mahdiparastesh.sexbook.data.Report
import ir.mahdiparastesh.sexbook.databinding.OrgasmsBinding
import ir.mahdiparastesh.sexbook.stat.base.OneChartActivity
import ir.mahdiparastesh.sexbook.util.ChartTimeframeLength
import ir.mahdiparastesh.sexbook.util.ColumnFactory
import ir.mahdiparastesh.sexbook.util.LongSparseArrayExt.filter
import ir.mahdiparastesh.sexbook.util.NumberUtils.show
import ir.mahdiparastesh.sexbook.util.StatUtils
import ir.mahdiparastesh.sexbook.view.HelpDialog
import ir.mahdiparastesh.sexbook.view.SexType

class Orgasms : OneChartActivity<ComboLineColumnChartView>(), Toolbar.OnMenuItemClickListener {
    val b: OrgasmsBinding by lazy { OrgasmsBinding.inflate(layoutInflater) }
    val vm: Model by viewModels()

    companion object {

        fun mixture(
            c: Sexbook, func: (report: Report) -> Float = { 1f }
        ): ArrayList<Summary.Orgasm> {
            val history = arrayListOf<Summary.Orgasm>()
            val allowedTypes = SexType.allowedOnes(c.sp)
            for (o in c.reports.let {
                if (allowedTypes.size < SexType.count)
                    it.filter { r -> r.type in allowedTypes && r.orgasmed() }
                else it.filter { r -> r.orgasmed() }  // do not simplify
            }) history.add(Summary.Orgasm(o.time, func(o)))
            return history
        }
    }

    class Model : ViewModel() {
        var chartSubject: Int = 0
        var chartTimeframe: Int = 0
        var timeSeries: List<String>? = null

        fun subject() = ChartSubject.entries[chartSubject]
        fun timeframeLength() = ChartTimeframeLength.entries[chartTimeframe]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureToolbar(b.toolbar, R.string.orgasmsTitle)
    }

    override fun requirements() = c.reports.isNotEmpty()
    override fun getRootView(): View = b.root

    override suspend fun prepareData(): AbstractChartData {
        val timeframeLength = vm.timeframeLength()
        if (vm.timeSeries == null)
            vm.timeSeries = StatUtils.timeSeries(c, vm.timeframeLength())

        val chartData = ComboLineColumnChartData()
        val sums = StatUtils.sumTimeframes(
            c, mixture(c), vm.timeSeries!!, timeframeLength
        )
        chartData.columnChartData = ColumnChartData().setColumns(
            ColumnFactory(this, sums, true)
        )
        val subject = vm.subject()
        val scores = StatUtils.sumTimeframes(
            c, mixture(c) { o ->
                when (vm.chartSubject) {
                    ChartSubject.ENERGY.ordinal ->
                        o.energy()
                    ChartSubject.BAFFLEMENT.ordinal ->
                        o.bafflement() - (if (o.bafflement() == 0) 0 else 1)
                    ChartSubject.EJACULATION.ordinal ->
                        o.ejaculation()
                    else ->
                        o.pleasure() - (if (o.pleasure() == 0) 0 else 2)
                }.toFloat()
            },
            vm.timeSeries!!, timeframeLength
        )
        val pointValues = arrayListOf<PointValue>()
        for ((i, score) in scores.values.withIndex()) {
            if (score > 0) pointValues.add(
                PointValue(
                    i.toFloat(), when (vm.chartSubject) {
                        ChartSubject.ENERGY.ordinal -> score / 4f
                        ChartSubject.BAFFLEMENT.ordinal -> score / 4f
                        ChartSubject.EJACULATION.ordinal -> score / 3f
                        else -> score / 4f  // pleasure
                    }
                )
                    .setLabel(score.show())
            )
        }
        val line = Line(pointValues)
            .setColor(subject.color)
            .setCubic(true)
            .setHasLabelsOnlyForSelected(true)
        chartData.lineChartData = LineChartData().setLines(listOf(line))
        return chartData
    }

    override suspend fun drawChart(data: AbstractChartData) {
        chartView.setLabelOffset(dp(StatUtils.POINT_LABEL_OFFSET_IN_DP))
        chartView.comboLineColumnChartData = data as ComboLineColumnChartData
        chartView.setZoomLevel(5f, 0f, 0f)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        b.toolbar.inflateMenu(R.menu.orgasms)
        b.toolbar.setOnMenuItemClickListener(this)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.setGroupDividerEnabled(true)
        menu?.findItem(R.id.chartOptions)?.subMenu?.apply {
            getItem(vm.chartSubject).isChecked = true
            getItem(ChartSubject.entries.size + vm.chartTimeframe).isChecked = true
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (item.itemId == R.id.help) HelpDialog.create(this, R.string.orgasmsHelp)

        val chartSubject = ChartSubject.entries.indexOfFirst { it.menuId == item.itemId }
        val chartTimeframe = ChartTimeframeLength.entries.indexOfFirst { it.menuId == item.itemId }
        if (chartSubject != -1 || chartTimeframe != -1) {
            item.isChecked = true
            if (chartSubject != -1) vm.chartSubject = chartSubject
            if (chartTimeframe != -1) {
                vm.chartTimeframe = chartTimeframe
                vm.timeSeries = null
            }
            prepareChart()
            return true
        }
        return false
    }

    enum class ChartSubject(@field:IdRes val menuId: Int, @field:ColorInt val color: Int) {
        PLEASURE(R.id.chartPleasure, 0xFF26E013.toInt()),
        ENERGY(R.id.chartEnergy, 0xFFFF0404.toInt()),
        BAFFLEMENT(R.id.chartBafflement, 0xFF2307D3.toInt()),
        EJACULATION(R.id.chartEjaculation, 0xFF0DF3E5.toInt()),
    }
}
