package ir.mahdiparastesh.sexbook.stat

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toolbar
import androidx.activity.viewModels
import androidx.annotation.MainThread
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import ir.mahdiparastesh.hellocharts.model.AbstractChartData
import ir.mahdiparastesh.hellocharts.model.LineChartData
import ir.mahdiparastesh.hellocharts.model.PieChartData
import ir.mahdiparastesh.hellocharts.model.SliceValue
import ir.mahdiparastesh.hellocharts.view.AbstractChartView
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.ctrl.Summary
import ir.mahdiparastesh.sexbook.data.Crush
import ir.mahdiparastesh.sexbook.databinding.TasteBinding
import ir.mahdiparastesh.sexbook.stat.base.CrushAgeChart
import ir.mahdiparastesh.sexbook.stat.base.CrushAttrChart
import ir.mahdiparastesh.sexbook.stat.base.CrushBreastsChart
import ir.mahdiparastesh.sexbook.stat.base.CrushEyeColourChart
import ir.mahdiparastesh.sexbook.stat.base.CrushEyeShapeChart
import ir.mahdiparastesh.sexbook.stat.base.CrushFaceShapeChart
import ir.mahdiparastesh.sexbook.stat.base.CrushFatChart
import ir.mahdiparastesh.sexbook.stat.base.CrushFictionalityChart
import ir.mahdiparastesh.sexbook.stat.base.CrushFirstMetChart
import ir.mahdiparastesh.sexbook.stat.base.CrushGenderChart
import ir.mahdiparastesh.sexbook.stat.base.CrushHairColourChart
import ir.mahdiparastesh.sexbook.stat.base.CrushHeightChart
import ir.mahdiparastesh.sexbook.stat.base.CrushMuscleChart
import ir.mahdiparastesh.sexbook.stat.base.CrushPenisChart
import ir.mahdiparastesh.sexbook.stat.base.CrushQualitativeChart
import ir.mahdiparastesh.sexbook.stat.base.CrushQuantitativeChart
import ir.mahdiparastesh.sexbook.stat.base.CrushSkinColourChart
import ir.mahdiparastesh.sexbook.stat.base.MultiChartActivity
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
import kotlin.math.roundToInt

class Taste : MultiChartActivity() {
    val b: TasteBinding by lazy { TasteBinding.inflate(layoutInflater) }
    val vm: Model by viewModels()
    private val jobs: ArrayList<Job> = arrayListOf()

    class Model : ViewModel() {
        var currentPage: Int? = null
        var chartType: Int = 0
        var chartTimeframe: Int = 0
        var crushSumIndex: HashMap<Crush, Float>? = null
        var timeSeries: List<String>? = null

        fun timeframeLength() = ChartTimeframeLength.entries[chartTimeframe]
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
            vm.timeSeries = null
        }
    override val helpMessage: Int get() = R.string.tasteHelp

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureToolbar(b.toolbar, R.string.taste)
        b.pager.registerOnPageChangeCallback(onPageChanged)
        b.indicator.attachTo(b.pager)
    }

    override fun requirements() = c.summary != null
    override fun getRootView(): View = b.root

    private val onPageChanged = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            vm.currentPage = position
        }
    }

    override fun createNewChart(reset: Boolean) {
        if (b.pager.adapter == null || reset) b.pager.adapter = TasteAdapter()
        if (vm.currentPage != null) b.pager.setCurrentItem(vm.currentPage!!, false)
    }

    inner class TasteAdapter : FragmentStateAdapter(this@Taste) {
        override fun getItemCount(): Int = 14
        override fun createFragment(i: Int): Fragment = when (i) {
            // qualities:
            1 -> SkinColourTaste()
            2 -> HairColourTaste()
            3 -> EyeColourTaste()
            4 -> EyeShapeTaste()
            5 -> FaceShapeTaste()
            6 -> FatTaste()
            7 -> MuscleTaste()
            8 -> BreastsTaste()
            9 -> PenisTaste()
            // quantities:
            10 -> HeightTaste()
            11 -> AgeTaste()
            12 -> FirstMetTaste()
            13 -> FictionalityTaste()  // quality
            else -> GenderTaste()  // quality
        }  // You cannot use anonymous objects here. Even if you could, it would be a bad idea.
    }

    /** Only used for [ChartType.COMPOSITIONAL] */
    fun indexCrushSums() {
        if (vm.crushSumIndex != null) return
        else vm.crushSumIndex = hashMapOf()

        var sum: Float
        for (p in c.people.values) {
            sum = c.summary!!.scores[p.key]?.sum ?: continue
            if (sum == 0f) continue
            vm.crushSumIndex!![p] = sum
        }
    }

    override fun onDestroy() {
        for (job in jobs) job.cancel()
        b.pager.unregisterOnPageChangeCallback(onPageChanged)
        super.onDestroy()
    }


    abstract class TasteFragment : Fragment(), CrushAttrChart {
        protected val c: Taste by lazy { activity as Taste }
        protected lateinit var root: FrameLayout
        private lateinit var chartView: AbstractChartView
        private var myJob: Job? = null

        /** Only used for [ChartType.COMPOSITIONAL] */
        protected val counts = hashMapOf<Short, Float>()
        protected var sumOfAll = 0f

        /** Only used for [ChartType.TIME_SERIES] and [ChartType.CUMULATIVE_TIME_SERIES] */
        protected val records = hashMapOf<Short, ArrayList<Summary.Orgasm>>()

        override val unspecifiedQualityColour: Int by lazy {
            if (!c.night) Color.BLACK else Color.WHITE
        }

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

            // set the title and miscellaneous stuff
            preAnalysis()

            // prepare the diagram
            myJob = CoroutineScope(Dispatchers.IO).launch {
                if (c.vm.chartType == ChartType.COMPOSITIONAL.ordinal) {
                    sumOfAll += c.c.summary!!.unknown
                    counts[0] = c.c.summary!!.unknown
                }
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
            c.b.toolbar.subtitle = subtitle
        }

        protected lateinit var subtitle: String

        @MainThread
        protected abstract fun preAnalysis()

        abstract suspend fun statisticise(): AbstractChartData

        /** Only used for [ChartType.TIME_SERIES] and [ChartType.CUMULATIVE_TIME_SERIES] */
        fun indexRecords() {
            if (c.vm.timeSeries == null)
                c.vm.timeSeries = StatUtils.timeSeries(c.c, c.vm.timeframeLength())
            var cr: Crush?
            var propertyValue: Short
            for ((crushKey, score) in c.c.summary!!.scores.entries) {
                cr = c.c.people[crushKey] ?: continue
                if (isFiltered && !crushFilter(cr)) continue
                propertyValue = crushProperty(cr)
                if (propertyValue !in records) records[propertyValue] = arrayListOf()
                records[propertyValue]!!.addAll(score.orgasms)
            }
        }

        protected fun createSliceValue(score: Float, mode: Int, division: String): SliceValue =
            SliceValue(score, preferredColour(mode) ?: c.chartColour).setLabel(
                "$division: ${score.show()} (${((100f / sumOfAll) * score).roundToInt()}%)"
            )
    }


    abstract class QualitativeTasteFragment : TasteFragment(), CrushQualitativeChart {
        private lateinit var arModes: Array<String>

        @SuppressLint("SetTextI18n")
        override fun preAnalysis() {
            arModes = resources.getStringArray(modes)
            subtitle =
                (if (modes == R.array.genders)
                    getString(R.string.gender)
                else arModes[0].substring(0..(arModes[0].length - 2)))
            arModes[0] = getString(R.string.unspecified)
            for (mode in arModes.indices) counts[mode.toShort()] = 0f
        }

        override suspend fun statisticise(): AbstractChartData {
            when (c.vm.chartType) {

                ChartType.COMPOSITIONAL.ordinal -> {
                    c.indexCrushSums()
                    for ((crush, score) in c.vm.crushSumIndex!!) {
                        if (isFiltered && !crushFilter(crush)) continue
                        val mode = crushProperty(crush)
                        counts[mode] = counts[mode]!! + score
                        sumOfAll += score
                    }

                    val data = arrayListOf<SliceValue>()
                    for ((mode, score) in counts) if (score > 0f) data.add(
                        createSliceValue(score, mode.toInt(), arModes[mode.toInt()])
                    )
                    return PieChartData(data).setHasLabels(true)
                }

                ChartType.TIME_SERIES.ordinal, ChartType.CUMULATIVE_TIME_SERIES.ordinal -> {
                    indexRecords()
                    val lines = ArrayList<Timeline>()
                    val cumulative = c.vm.chartType == ChartType.CUMULATIVE_TIME_SERIES.ordinal
                    for ((div, orgasms) in records) lines.add(
                        Timeline(
                            arModes[div.toInt()],
                            StatUtils.sumTimeframes(
                                c.c,
                                orgasms,
                                c.vm.timeSeries!!,
                                c.vm.timeframeLength(),
                                cumulative
                            ),
                            0f,  // no sorting here
                            preferredColour(div.toInt())
                        )
                    )
                    return LineChartData().setLines(LineFactory(lines))
                }

                else -> throw IllegalArgumentException("ChartType not implemented!")
            }
        }
    }

    class GenderTaste : QualitativeTasteFragment(), CrushGenderChart

    class SkinColourTaste : QualitativeTasteFragment(), CrushSkinColourChart

    class HairColourTaste : QualitativeTasteFragment(), CrushHairColourChart

    class EyeColourTaste : QualitativeTasteFragment(), CrushEyeColourChart

    class EyeShapeTaste : QualitativeTasteFragment(), CrushEyeShapeChart

    class FaceShapeTaste : QualitativeTasteFragment(), CrushFaceShapeChart

    class FatTaste : QualitativeTasteFragment(), CrushFatChart

    class MuscleTaste : QualitativeTasteFragment(), CrushMuscleChart

    class BreastsTaste : QualitativeTasteFragment(), CrushBreastsChart

    class PenisTaste : QualitativeTasteFragment(), CrushPenisChart

    class FictionalityTaste : QualitativeTasteFragment(), CrushFictionalityChart


    abstract class QuantitativeTasteFragment : TasteFragment(), CrushQuantitativeChart {

        @SuppressLint("SetTextI18n")
        override fun preAnalysis() {
            subtitle = getString(topic)
        }

        override suspend fun statisticise(): AbstractChartData {
            when (c.vm.chartType) {

                ChartType.COMPOSITIONAL.ordinal -> {
                    c.indexCrushSums()
                    for ((crush, score) in c.vm.crushSumIndex!!) {
                        if (isFiltered && !crushFilter(crush)) continue
                        val div = crushProperty(crush)
                        if (div !in counts)
                            counts[div] = score
                        else
                            counts[div] = counts[div]!! + score
                        sumOfAll += score
                    }

                    val data = arrayListOf<SliceValue>()
                    if (sumOfAll != 0f) for ((div, score) in counts.toSortedMap()) data.add(
                        createSliceValue(
                            score,
                            div.toInt(),
                            if (div != 0.toShort()) divisionName(div.toInt())
                            else getString(R.string.unspecified)
                        )
                    )
                    return PieChartData(data).setHasLabels(true)
                }

                ChartType.TIME_SERIES.ordinal, ChartType.CUMULATIVE_TIME_SERIES.ordinal -> {
                    indexRecords()
                    val lines = ArrayList<Timeline>()
                    val cumulative = c.vm.chartType == ChartType.CUMULATIVE_TIME_SERIES.ordinal
                    for ((div, orgasms) in records) lines.add(
                        Timeline(
                            if (div != 0.toShort()) divisionName(div.toInt())
                            else getString(R.string.unspecified),
                            StatUtils.sumTimeframes(
                                c.c,
                                orgasms,
                                c.vm.timeSeries!!,
                                c.vm.timeframeLength(),
                                cumulative
                            ),
                            0f,  // no sorting here
                            preferredColour(div.toInt())
                        )
                    )
                    return LineChartData().setLines(LineFactory(lines))
                }

                else -> throw IllegalArgumentException("ChartType not implemented!")
            }
        }
    }

    class HeightTaste : QuantitativeTasteFragment(), CrushHeightChart

    class AgeTaste : QuantitativeTasteFragment(), CrushAgeChart

    class FirstMetTaste : QuantitativeTasteFragment(), CrushFirstMetChart
}
