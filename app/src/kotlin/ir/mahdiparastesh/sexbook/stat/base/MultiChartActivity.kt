package ir.mahdiparastesh.sexbook.stat.base

import android.os.Build
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.Menu
import android.view.MenuItem
import android.widget.Toolbar
import androidx.annotation.IdRes
import androidx.annotation.MainThread
import ir.mahdiparastesh.hellocharts.model.AbstractChartData
import ir.mahdiparastesh.hellocharts.model.LineChartData
import ir.mahdiparastesh.hellocharts.model.PieChartData
import ir.mahdiparastesh.hellocharts.view.AbstractChartView
import ir.mahdiparastesh.hellocharts.view.LineChartView
import ir.mahdiparastesh.hellocharts.view.PieChartView
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.base.BaseActivity
import ir.mahdiparastesh.sexbook.util.ChartTimeframeLength
import ir.mahdiparastesh.sexbook.util.StatUtils
import kotlin.reflect.KClass

/** Subclass of [BaseActivity] that contains multiple [ChartType]s. */
abstract class MultiChartActivity : ChartActivity(), Toolbar.OnMenuItemClickListener {

    abstract val toolbar: Toolbar
    abstract var vmChartType: Int
    abstract var vmChartTimeframe: Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNewChart(false)
    }

    /** @param reset true if it is invoked by the user, false if it's invoked onCreate */
    @MainThread
    abstract fun createNewChart(reset: Boolean)

    protected fun createChartView() = ChartType.entries[vmChartType].view.java
        .constructors.find { it.parameterCount == 1 }!!
        .newInstance(
            ContextThemeWrapper(c, R.style.statChart)
        ) as AbstractChartView

    fun passDataToChartView(chartView: AbstractChartView, data: AbstractChartData) {
        when (vmChartType) {
            ChartType.COMPOSITIONAL.ordinal ->
                (chartView as PieChartView).pieChartData = data as PieChartData
            ChartType.TIME_SERIES.ordinal, ChartType.CUMULATIVE_TIME_SERIES.ordinal -> {
                chartView.setLabelOffset(dp(StatUtils.POINT_LABEL_OFFSET_IN_DP))
                (chartView as LineChartView).lineChartData = data as LineChartData
                chartView.isViewportCalculationEnabled = false // never do it before setLineChatData
                // it cannot be zoomed out.
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        toolbar.inflateMenu(R.menu.multi_chart)
        toolbar.setOnMenuItemClickListener(this)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
            menu?.setGroupDividerEnabled(true)
        menu?.findItem(R.id.chartOptions)?.subMenu?.apply {
            getItem(vmChartType).isChecked = true
            getItem(ChartType.entries.size + vmChartTimeframe).isChecked = true
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        val chartType = ChartType.entries.indexOfFirst { it.menuId == item.itemId }
        val chartTimeframe = ChartTimeframeLength.entries.indexOfFirst { it.menuId == item.itemId }
        if (chartType != -1 || chartTimeframe != -1) {
            item.isChecked = true
            if (chartType != -1) vmChartType = chartType
            if (chartTimeframe != -1) vmChartTimeframe = chartTimeframe
            createNewChart(true)
            return true
        }
        return false
    }


    enum class ChartType(val view: KClass<out AbstractChartView>, @IdRes val menuId: Int) {
        COMPOSITIONAL(PieChartView::class, R.id.chartCompositional),
        TIME_SERIES(LineChartView::class, R.id.chartTimeSeries),
        CUMULATIVE_TIME_SERIES(LineChartView::class, R.id.chartGrowth),
    }
}
