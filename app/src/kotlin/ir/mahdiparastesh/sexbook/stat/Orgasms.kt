package ir.mahdiparastesh.sexbook.stat

import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toolbar
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.core.util.isNotEmpty
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import ir.mahdiparastesh.hellocharts.model.AbstractChartData
import ir.mahdiparastesh.hellocharts.model.ColumnChartData
import ir.mahdiparastesh.hellocharts.view.AbstractChartView
import ir.mahdiparastesh.hellocharts.view.ColumnChartView
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.Sexbook
import ir.mahdiparastesh.sexbook.ctrl.Summary
import ir.mahdiparastesh.sexbook.databinding.OrgasmsBinding
import ir.mahdiparastesh.sexbook.stat.base.MultiChartActivity
import ir.mahdiparastesh.sexbook.util.ChartTimeframeLength
import ir.mahdiparastesh.sexbook.util.ColumnFactory
import ir.mahdiparastesh.sexbook.util.LongSparseArrayExt.filter
import ir.mahdiparastesh.sexbook.util.StatUtils
import ir.mahdiparastesh.sexbook.view.SexType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.getValue

class Orgasms : MultiChartActivity() {
    val b: OrgasmsBinding by lazy { OrgasmsBinding.inflate(layoutInflater) }
    val vm: Model by viewModels()
    private val jobs: ArrayList<Job> = arrayListOf()

    companion object {

        fun history(c: Sexbook): ArrayList<Summary.Orgasm> {
            val history = arrayListOf<Summary.Orgasm>()
            val allowedTypes = SexType.allowedOnes(c.sp)
            for (o in c.reports.let {
                if (allowedTypes.size < SexType.count)
                    it.filter { r -> r.type in allowedTypes && r.orgasmed() }
                else it.filter { r -> r.orgasmed() }  // do not simplify
            }) history.add(Summary.Orgasm(o.time, 1f))
            return history
        }
    }

    class Model : ViewModel() {
        var currentPage: Int? = null
        var chartTimeframe: Int = 0
        var timeSeries: List<String>? = null

        fun timeframeLength() = ChartTimeframeLength.entries[chartTimeframe]
    }

    override val toolbar: Toolbar get() = b.toolbar
    override var vmChartType: Int = ChartType.TIME_SERIES.ordinal
    override var vmChartTimeframe: Int
        get() = vm.chartTimeframe
        set(value) {
            vm.chartTimeframe = value
            vm.timeSeries = null
        }
    override val helpMessage: Int get() = R.string.orgasmsHelp

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureToolbar(b.toolbar, R.string.orgasmsTitle)
        b.pager.registerOnPageChangeCallback(onPageChanged)
        b.indicator.attachTo(b.pager)
    }

    override fun requirements() = c.reports.isNotEmpty()
    override fun getRootView(): View = b.root

    private val onPageChanged = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            vm.currentPage = position
        }
    }

    override fun createNewChart(reset: Boolean) {
        if (b.pager.adapter == null || reset) b.pager.adapter = OrgasmsAdapter()
        if (vm.currentPage != null) b.pager.setCurrentItem(vm.currentPage!!, false)
    }

    override fun createChartView(): AbstractChartView =
        ColumnChartView(ContextThemeWrapper(c, R.style.statChart))

    override fun passDataToChartView(chartView: AbstractChartView, data: AbstractChartData) {
        (chartView as ColumnChartView).columnChartData = data as ColumnChartData
    }

    inner class OrgasmsAdapter : FragmentStateAdapter(this@Orgasms) {
        override fun getItemCount(): Int = 1
        override fun createFragment(i: Int): Fragment = when (i) {
            // 1 ->
            else -> Mixture()
        }  // You cannot use anonymous objects here. Even if you could, it would be a bad idea.
    }

    override fun onDestroy() {
        for (job in jobs) job.cancel()
        b.pager.unregisterOnPageChangeCallback(onPageChanged)
        super.onDestroy()
    }


    abstract class OrgasmsFragment : Fragment() {
        protected val c: Orgasms by lazy { activity as Orgasms }
        protected lateinit var root: FrameLayout
        private lateinit var chartView: AbstractChartView
        private var myJob: Job? = null

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
        ): View = FrameLayout(c).also { root = it }


        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            // create and add the chart view
            chartView = c.createChartView()
            root.addView(
                chartView,
                FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
            chartView.setViewportChangeListener {
                c.b.pager.isUserInputEnabled = chartView.zoomLevel == 1f
            }

            // prepare the diagram
            myJob = CoroutineScope(Dispatchers.IO).launch {
                val data = statisticise()

                withContext(Dispatchers.Main) {
                    c.passDataToChartView(chartView, data)
                    c.b.loading.isVisible = false
                    chartView.isInvisible = false
                }
                myJob?.also { c.jobs.remove(it) }
                myJob = null
            }
            myJob?.also { c.jobs.add(it) }
        }

        override fun onResume() {
            super.onResume()
            c.b.toolbar.subtitle = getString(subtitleStrRes)
        }

        @get:StringRes
        abstract val subtitleStrRes: Int

        abstract suspend fun statisticise(): AbstractChartData
    }

    class Mixture : OrgasmsFragment() {

        override val subtitleStrRes: Int = R.string.sum

        override suspend fun statisticise(): AbstractChartData {
            val timeframeLength = c.vm.timeframeLength()
            if (c.vm.timeSeries == null)
                c.vm.timeSeries = StatUtils.timeSeries(c.c, c.vm.timeframeLength())
            return ColumnChartData().setColumns(
                ColumnFactory(
                    c, StatUtils.sumTimeframes(
                        c.c,
                        history(c.c),
                        c.vm.timeSeries!!,
                        timeframeLength
                    ),
                    true
                )
            )
        }
    }
}
