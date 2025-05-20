package ir.mahdiparastesh.sexbook.stat

import android.view.View
import android.view.ViewGroup
import android.widget.Spinner
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.util.isNotEmpty
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModel
import ir.mahdiparastesh.hellocharts.model.AbstractChartData
import ir.mahdiparastesh.hellocharts.model.LineChartData
import ir.mahdiparastesh.hellocharts.model.PieChartData
import ir.mahdiparastesh.hellocharts.view.AbstractChartView
import ir.mahdiparastesh.hellocharts.view.LineChartView
import ir.mahdiparastesh.hellocharts.view.PieChartView
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.Settings
import ir.mahdiparastesh.sexbook.databinding.AdorabilityBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Adorability : MultiChartActivity(), SingleChartActivity {
    val b: AdorabilityBinding by lazy { AdorabilityBinding.inflate(layoutInflater) }
    val vm: Model by viewModels()
    lateinit var chartView: AbstractChartView

    class Model : ViewModel() {
        var chartType: Int = 1
    }

    override var vmChartType: Int
        get() = vm.chartType
        set(value) {
            vm.chartType = value
        }

    override fun requirements(): Boolean = c.reports.isNotEmpty() && c.summary != null
    override fun getRootView(): View = b.root
    override var job: Job? = null
    override val chartType: Spinner get() = b.chartType

    override fun createNewChart() {

        // create and add/replace the chart view
        if (::chartView.isInitialized) b.root.removeView(chartView)
        chartView = createChartView()
        b.root.addView(
            chartView, 1,
            ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0).apply {
                topToBottom = R.id.title
                bottomToTop = R.id.chartType
            })

        // prepare data and then pass it to the chart view
        job = CoroutineScope(Dispatchers.IO).launch {
            val data = prepareData()
            withContext(Dispatchers.Main) {
                drawChart(data)
                b.loading.isVisible = false
                chartView.isInvisible = false
            }
            job = null
        }
    }

    override suspend fun prepareData(): AbstractChartData {
        val lines = ArrayList<Timeline>()
        val frames = StatUtils.timeSeries(c)
        val hideUnsafe =
            c.sp.getBoolean(Settings.spHideUnsafePeople, true) && c.unsafe.isNotEmpty()
        for (x in c.summary!!.scores) {
            if (hideUnsafe && x.key in c.unsafe) continue
            lines.add(Timeline(x.key, StatUtils.sumTimeFrames(c, x.value, frames)))
        }
        return LineChartData().setLines(LineFactory(lines))
    }

    override suspend fun drawChart(data: AbstractChartData) {
        when (vm.chartType) {
            ChartType.COMPOSITIONAL.ordinal ->
                (chartView as PieChartView).pieChartData = data as PieChartData
            ChartType.TIME_SERIES.ordinal -> {
                chartView.setLabelOffset(dp(StatUtils.POINT_LABEL_OFFSET_IN_DP))
                (chartView as LineChartView).lineChartData = data as LineChartData
                chartView.isViewportCalculationEnabled = false // never do it before setLineChatData
                // it cannot be zoomed out.
            }
        }
    }
}
