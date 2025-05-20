package ir.mahdiparastesh.sexbook.stat

import android.os.Bundle
import android.widget.ProgressBar
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import ir.mahdiparastesh.hellocharts.view.AbstractChartView
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.base.BaseActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Subclass of [BaseActivity] which can display only one chart and has only one page. */
abstract class OneChartActivity<ChartView>() : ChartActivity(),
    SingleChartActivity where ChartView : AbstractChartView {

    override var job: Job? = null
    protected val chartView: ChartView by lazy { findViewById<ChartView>(R.id.main) }
    private val loading: ProgressBar by lazy { findViewById(R.id.loading) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        job = CoroutineScope(Dispatchers.IO).launch {
            val data = prepareData()
            withContext(Dispatchers.Main) {
                drawChart(data)
                loading.isVisible = false
                chartView.isInvisible = false
            }
            job = null
        }
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun onBackPressed() {
        job?.cancel()
        @Suppress("DEPRECATION") super.onBackPressed()
    }
}