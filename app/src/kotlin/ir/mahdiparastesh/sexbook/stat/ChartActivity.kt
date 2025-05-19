package ir.mahdiparastesh.sexbook.stat

import android.os.Bundle
import android.widget.ProgressBar
import androidx.annotation.MainThread
import androidx.core.util.isNotEmpty
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import ir.mahdiparastesh.hellocharts.model.AbstractChartData
import ir.mahdiparastesh.hellocharts.view.AbstractChartView
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.base.BaseActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class ChartActivity<ChartView>(private val layoutRes: Int) : BaseActivity()
        where ChartView : AbstractChartView {

    private var job: Job? = null
    protected val chartView: ChartView by lazy { findViewById<ChartView>(R.id.main) }
    protected val loading: ProgressBar by lazy { findViewById(R.id.loading) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutRes)
        if (night) window.decorView.setBackgroundColor(
            themeColor(com.google.android.material.R.attr.colorPrimary)
        )
        if (!requirements()) {
            onBackPressed(); return; }

        job = CoroutineScope(Dispatchers.IO).launch {
            val drawn = draw()
            withContext(Dispatchers.Main) {
                render(drawn)
                loading.isVisible = false
                chartView.isInvisible = false
            }
            job = null
        }
    }

    open fun requirements() = c.reports.isNotEmpty() && c.summary != null

    abstract suspend fun draw(): AbstractChartData

    @MainThread
    abstract suspend fun render(data: AbstractChartData)

    @Suppress("OVERRIDE_DEPRECATION")
    override fun onBackPressed() {
        job?.cancel()
        @Suppress("DEPRECATION") super.onBackPressed()
    }
}