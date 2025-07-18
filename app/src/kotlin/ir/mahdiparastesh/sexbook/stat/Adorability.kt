package ir.mahdiparastesh.sexbook.stat

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toolbar
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.util.isNotEmpty
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModel
import ir.mahdiparastesh.hellocharts.model.AbstractChartData
import ir.mahdiparastesh.hellocharts.model.LineChartData
import ir.mahdiparastesh.hellocharts.model.PieChartData
import ir.mahdiparastesh.hellocharts.model.SliceValue
import ir.mahdiparastesh.hellocharts.view.AbstractChartView
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.databinding.AdorabilityBinding
import ir.mahdiparastesh.sexbook.page.Settings
import ir.mahdiparastesh.sexbook.stat.base.MultiChartActivity
import ir.mahdiparastesh.sexbook.stat.base.SingleChartActivity
import ir.mahdiparastesh.sexbook.util.ChartTimeframeLength
import ir.mahdiparastesh.sexbook.util.LineFactory
import ir.mahdiparastesh.sexbook.util.NumberUtils.show
import ir.mahdiparastesh.sexbook.util.StatUtils
import ir.mahdiparastesh.sexbook.util.Timeline
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
        var chartTimeframe: Int = 0
    }

    override val toolbar: Toolbar get() = b.toolbar
    override var vmChartType: Int
        get() = vm.chartType
        set(value) {
            vm.chartType = value
        }
    override var vmChartTimeframe: Int
        get() = vm.chartTimeframe
        set(value) {
            vm.chartTimeframe = value
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureToolbar(b.toolbar, R.string.adorability)
    }

    override fun requirements(): Boolean = c.reports.isNotEmpty() && c.summary != null
    override fun getRootView(): View = b.root
    override var job: Job? = null

    override fun createNewChart(reset: Boolean) {
        b.loading.isVisible = true

        // create and add/replace the chart view
        if (::chartView.isInitialized) b.root.removeView(chartView)
        chartView = createChartView()
        b.root.addView(
            chartView, 2,
            ConstraintLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0
            ).apply {
                topToBottom = R.id.toolbar
                bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
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

        // filters
        val statOnlyCrushes = c.sp.getBoolean(Settings.spStatOnlyCrushes, false)
                && c.liefde.isNotEmpty()
        val hideUnsafe = c.hideUnsafe()

        when (vm.chartType) {

            ChartType.COMPOSITIONAL.ordinal -> {
                val data = arrayListOf<SliceValue>()
                c.summary?.scores?.entries?.sortedBy { it.value.sum }?.forEach { x ->
                    if (hideUnsafe && x.key in c.unsafe) return@forEach
                    if (statOnlyCrushes && x.key !in c.liefde) return@forEach

                    val score = x.value.sum
                    data.add(
                        SliceValue(score, chartColour)
                            .apply { setLabel("${x.key} {${score.show()}}") })
                }
                return PieChartData(data).apply {
                    setHasLabelsOnlyForSelected(true)  // setHasLabels(true)
                }
            }

            ChartType.TIME_SERIES.ordinal, ChartType.CUMULATIVE_TIME_SERIES.ordinal -> {
                val lines = ArrayList<Timeline>()
                val timeframeLength = ChartTimeframeLength.entries[vm.chartTimeframe]
                val timeframes = StatUtils.timeSeries(c, timeframeLength)

                val cumulative = vm.chartType == ChartType.CUMULATIVE_TIME_SERIES.ordinal
                for (x in c.summary!!.scores) {
                    if (hideUnsafe && x.key in c.unsafe) continue
                    if (statOnlyCrushes && x.key !in c.liefde) continue
                    lines.add(
                        Timeline(
                            x.key,
                            StatUtils.sumTimeframes(
                                c, x.value.orgasms, timeframes, timeframeLength, cumulative
                            ),
                            x.value.sum
                        )
                    )
                }
                lines.sortByDescending { it.sum }
                //Log.d("MARTINA", Gson().toJson(lines.map { it.name }))
                return LineChartData().setLines(LineFactory(lines))
            }

            else -> throw IllegalArgumentException("ChartType not implemented!")
        }
    }

    override suspend fun drawChart(data: AbstractChartData) {
        passDataToChartView(chartView, data)
    }
}
