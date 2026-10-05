package ir.mahdiparastesh.sexbook.stat.base

import android.os.Bundle
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.ProgressBar
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import ir.mahdiparastesh.hellocharts.gesture.ContainerScrollType
import ir.mahdiparastesh.hellocharts.view.AbstractChartView
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.base.BaseActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Subclass of [BaseActivity] which can display only one chart and has only one page. */
abstract class OneChartActivity<ChartView> : ChartActivity(),
    SingleChartActivity where ChartView : AbstractChartView {

    override var job: Job? = null
    protected val chartView: ChartView by lazy { findViewById<ChartView>(R.id.main) }
    private val loading: ProgressBar by lazy { findViewById(R.id.loading) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prepareChart()
    }

    fun prepareChart() {
        loading.isVisible = true
        chartView.isInvisible = true
        job = CoroutineScope(Dispatchers.IO).launch {
            val data = prepareData()
            withContext(Dispatchers.Main) {

                chartView
                    .setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL)
                chartView.minimumWidth = chartItemsLength()
                    ?.let { (it * dm.density * chartItemsWidthDp()).toInt() }
                    ?: dm.widthPixels
                if (chartView.minimumWidth < dm.widthPixels)
                    chartView.minimumWidth = dm.widthPixels

                drawChart(data)
                loading.isVisible = false
                chartView.isInvisible = false

                val hsv = chartView.parent as HorizontalScrollView
                hsv.post { hsv.fullScroll(View.FOCUS_RIGHT) }
            }
            job = null
        }
    }

    open fun chartItemsWidthDp(): Float = 22f

    abstract fun chartItemsLength(): Int?

    override fun onDestroy() {
        job?.cancel()
        super.onDestroy()
    }
}
