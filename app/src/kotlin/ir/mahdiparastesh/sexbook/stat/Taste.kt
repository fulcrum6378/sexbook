package ir.mahdiparastesh.sexbook.stat

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.annotation.ArrayRes
import androidx.annotation.MainThread
import androidx.annotation.StringRes
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
import ir.mahdiparastesh.hellocharts.view.LineChartView
import ir.mahdiparastesh.hellocharts.view.PieChartView
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.base.BaseActivity
import ir.mahdiparastesh.sexbook.data.Crush
import ir.mahdiparastesh.sexbook.databinding.StatFragmentBinding
import ir.mahdiparastesh.sexbook.databinding.TasteBinding
import ir.mahdiparastesh.sexbook.util.NumberUtils.show
import ir.mahdiparastesh.sexbook.util.NumberUtils.sumOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.experimental.and
import kotlin.math.roundToInt
import kotlin.reflect.KClass

class Taste : BaseActivity() {
    lateinit var b: TasteBinding
    private val jobs: ArrayList<Job> = arrayListOf()
    val vm: Model by viewModels()
    private var spnChartTypeTouched = false

    class Model : ViewModel() {
        var currentPage: Int? = null
        var chartType: Int = 0
        var crushSumIndex: HashMap<Crush, Float>? = null
        var timeSeries: List<String>? = null
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (c.summary == null) {
            onBackPressed(); return; }

        b = TasteBinding.inflate(layoutInflater)
        setContentView(b.root)
        if (night) window.decorView.setBackgroundColor(
            themeColor(com.google.android.material.R.attr.colorPrimary)
        )
        b.pager.registerOnPageChangeCallback(onPageChanged)

        // chart types
        b.chartType.adapter = ArrayAdapter(
            c, R.layout.spinner_yellow, resources.getStringArray(R.array.tasteChartTypes)
        ).apply { setDropDownViewResource(R.layout.spinner_dd) }
        b.chartType.setSelection(vm.chartType)
        b.chartType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                if (!spnChartTypeTouched) {
                    spnChartTypeTouched = true
                    return; }

                vm.chartType = position
                loadPages()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // prepare initial data and then load the pages
        if (vm.crushSumIndex == null) CoroutineScope(Dispatchers.IO).launch {
            vm.crushSumIndex = hashMapOf()
            var orgasms: ArrayList<Summary.Orgasm>
            var sum: Float
            for (p in c.people.values) {
                orgasms = c.summary!!.scores[p.key] ?: continue
                sum = orgasms.sumOf { it.value }
                if (sum == 0f) continue
                vm.crushSumIndex!![p] = sum
            }

            withContext(Dispatchers.Main) { loadPages() }
        } else loadPages()
    }

    private val onPageChanged = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            vm.currentPage = position
        }
    }

    private fun loadPages() {
        b.pager.adapter = TasteAdapter()
        if (vm.currentPage != null) b.pager.setCurrentItem(vm.currentPage!!, false)
    }

    inner class TasteAdapter : FragmentStateAdapter(this@Taste) {
        override fun getItemCount(): Int = 12
        override fun createFragment(i: Int): Fragment = when (i) {
            1 -> SkinColourTaste()
            2 -> HairColourTaste()
            3 -> EyeColourTaste()
            4 -> EyeShapeTaste()
            5 -> FaceShapeTaste()
            6 -> FatTaste()
            7 -> MuscleTaste()
            8 -> BreastsTaste()
            9 -> PenisTaste()
            10 -> HeightTaste()
            11 -> AgeTaste()
            else -> GenderTaste()
        }
    }

    override fun onDestroy() {
        b.pager.unregisterOnPageChangeCallback(onPageChanged)
        super.onDestroy()
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun onBackPressed() {
        for (job in jobs) job.cancel()
        @Suppress("DEPRECATION") super.onBackPressed()
    }


    abstract class TasteFragment : Fragment() {
        protected val c: Taste by lazy { activity as Taste }
        protected lateinit var b: StatFragmentBinding
        private lateinit var chartView: AbstractChartView
        private var myJob: Job? = null
        protected val counts = hashMapOf<Short, Float>()
        protected val progress = hashMapOf<Short, ArrayList<Summary.Orgasm>>()
        protected var sumOfAll = 0f
        protected val unspecifiedQualityColour: Int by lazy {
            if (!c.night) Color.BLACK else Color.WHITE
        }

        abstract fun crushProperty(cr: Crush): Short

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
        ): View = StatFragmentBinding.inflate(layoutInflater, container, false)
            .also { b = it }.root

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            // create and add the chart view
            chartView = ChartType.entries[c.vm.chartType].view.java
                .constructors.find { it.parameterCount == 1 }!!
                .newInstance(ContextThemeWrapper(c, R.style.statChart)) as AbstractChartView
            b.root.addView(chartView, 1)
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
                    when (c.vm.chartType) {
                        ChartType.COMPOSITIONAL.ordinal ->
                            (chartView as PieChartView).pieChartData = data as PieChartData
                        ChartType.TIME_SERIES.ordinal -> {
                            (chartView as LineChartView).lineChartData = data as LineChartData
                            chartView.setLabelOffset(c.dp(20))
                        }
                    }
                    b.loading.isVisible = false
                    chartView.isInvisible = false
                }
                myJob?.also { c.jobs.remove(it) }
                myJob = null
            }
            myJob?.also { c.jobs.add(it) }
        }

        @MainThread
        abstract fun preAnalysis()
        abstract suspend fun statisticise(): AbstractChartData

        protected fun createSliceValue(score: Float, mode: Int, division: String): SliceValue =
            SliceValue(score, preferredColour(mode) ?: c.chartColour).setLabel(
                "$division: ${score.show()} (${((100f / sumOfAll) * score).roundToInt()}%)"
            )

        open fun preferredColour(mode: Int): Int? = null
    }

    abstract class QualitativeTasteFragment(
        private val isFiltered: Boolean = false
    ) : TasteFragment() {
        private lateinit var arModes: Array<String>

        @get:ArrayRes
        abstract val modes: Int
        open fun crushFilter(cr: Crush): Boolean = true

        @SuppressLint("SetTextI18n")
        override fun preAnalysis() {
            arModes = resources.getStringArray(modes)
            b.title.text = getString(R.string.taste) + ": " +
                    (if (modes == R.array.genders)
                        getString(R.string.gender)
                    else arModes[0].substring(0..(arModes[0].length - 2)))
            arModes[0] = getString(R.string.unspecified)
            for (mode in arModes.indices) counts[mode.toShort()] = 0f
        }

        override suspend fun statisticise(): AbstractChartData {
            when (c.vm.chartType) {

                ChartType.COMPOSITIONAL.ordinal -> {
                    for (p in c.vm.crushSumIndex!!.entries) {
                        if (isFiltered && !crushFilter(p.key)) continue
                        val mode = crushProperty(p.key)
                        counts[mode] = counts[mode]!! + p.value
                        sumOfAll += p.value
                    }

                    val data = arrayListOf<SliceValue>()
                    for (mode in arModes.indices) {
                        val score = counts[mode.toShort()]!!
                        if (score == 0f) continue
                        data.add(createSliceValue(score, mode, arModes[mode]))
                    }
                    return PieChartData(data).setHasLabels(true)
                }

                ChartType.TIME_SERIES.ordinal -> {
                    if (c.vm.timeSeries == null) c.vm.timeSeries = StatUtils.timeSeries(c.c)
                    var cr: Crush? = null
                    var modeCode: Short
                    for ((crushKey, orgasms) in c.c.summary!!.scores.entries) {
                        cr = c.c.people[crushKey] ?: continue
                        modeCode = crushProperty(cr)
                        if (modeCode !in progress) progress[modeCode] = arrayListOf()
                        progress[modeCode]!!.addAll(orgasms)
                    }
                    val stars = ArrayList<Star>()
                    for (mode in arModes.indices) {
                        if (mode.toShort() !in progress) continue
                        val frames = ArrayList<Star.Frame>()
                        for (month in c.vm.timeSeries!!) frames.add(
                            Star.Frame(
                                StatUtils.sumTimeFrame(c.c, progress[mode.toShort()]!!, month),
                                month
                            )
                        )
                        stars.add(Star(arModes[mode], frames, preferredColour(mode.toInt())))
                    }
                    stars.sortWith(Star.Sort(1))
                    stars.sortWith(Star.Sort())
                    return LineChartData().setLines(LineFactory(stars))
                }

                else -> throw IllegalArgumentException("ChartType not implemented!")
            }
        }
    }

    class GenderTaste : QualitativeTasteFragment() {
        override val modes: Int = R.array.genders
        override fun crushProperty(cr: Crush): Short =
            (cr.status and Crush.STAT_GENDER).toShort()

        override fun preferredColour(mode: Int): Int? = when (mode) {
            0 -> unspecifiedQualityColour
            1 -> 0xFFFF0037
            2 -> 0xFF0095FF
            3 -> 0xFF7300FF
            4 -> 0xFFDDFF00
            else -> throw IllegalArgumentException("Unknown gender code: $mode")
        }.toInt()
    }

    class SkinColourTaste : QualitativeTasteFragment() {
        override val modes: Int = R.array.bodySkinColour
        override fun crushProperty(cr: Crush): Short =
            ((cr.body and Crush.BODY_SKIN_COLOUR.first) shr Crush.BODY_SKIN_COLOUR.second).toShort()

        override fun preferredColour(mode: Int): Int? = when (mode) {
            0 -> 0xFF777777
            1 -> 0xFF633F37
            2 -> 0xFFAB7A5F
            3 -> 0xFFC0A07A
            4 -> 0xFFE5C3AE
            5 -> 0xFFF7E1D6
            6 -> 0xFFF1C9CD
            else -> throw IllegalArgumentException("Unknown skin colour code: $mode")
        }.toInt()
    }

    class HairColourTaste : QualitativeTasteFragment() {
        override val modes: Int = R.array.bodyHairColour
        override fun crushProperty(cr: Crush): Short =
            ((cr.body and Crush.BODY_HAIR_COLOUR.first) shr Crush.BODY_HAIR_COLOUR.second).toShort()

        override fun preferredColour(mode: Int): Int? = when (mode) {
            0 -> 0xFF777777
            1 -> 0xFF000000
            2 -> 0xFF6D4730
            3 -> 0xFFFBE7A1
            4 -> 0xFFC66531
            5 -> 0xFF052F9F
            else -> throw IllegalArgumentException("Unknown hair colour code: $mode")
        }.toInt()
    }

    class EyeColourTaste : QualitativeTasteFragment() {
        override val modes: Int = R.array.bodyEyeColour
        override fun crushProperty(cr: Crush): Short =
            ((cr.body and Crush.BODY_EYE_COLOUR.first) shr Crush.BODY_EYE_COLOUR.second).toShort()

        override fun preferredColour(mode: Int): Int? = when (mode) {
            0 -> unspecifiedQualityColour
            1 -> 0xFF5D301D
            2 -> 0xFF8F5929
            3 -> 0xFF947B3E
            4 -> 0xFF868254
            5 -> 0xFF798FB0
            6 -> 0xFF54427A
            else -> throw IllegalArgumentException("Unknown eye colour code: $mode")
        }.toInt()
    }

    class EyeShapeTaste : QualitativeTasteFragment() {
        override val modes: Int = R.array.bodyEyeShape
        override fun crushProperty(cr: Crush): Short =
            ((cr.body and Crush.BODY_EYE_SHAPE.first) shr Crush.BODY_EYE_SHAPE.second).toShort()
    }

    class FaceShapeTaste : QualitativeTasteFragment() {
        override val modes: Int = R.array.bodyFaceShape
        override fun crushProperty(cr: Crush): Short =
            ((cr.body and Crush.BODY_FACE_SHAPE.first) shr Crush.BODY_FACE_SHAPE.second).toShort()
    }

    class FatTaste : QualitativeTasteFragment() {
        override val modes: Int = R.array.bodyFat
        override fun crushProperty(cr: Crush): Short =
            ((cr.body and Crush.BODY_FAT.first) shr Crush.BODY_FAT.second).toShort()
    }

    class MuscleTaste : QualitativeTasteFragment() {
        override val modes: Int = R.array.bodyMuscle
        override fun crushProperty(cr: Crush): Short =
            ((cr.body and Crush.BODY_MUSCLE.first) shr Crush.BODY_MUSCLE.second).toShort()
    }

    class BreastsTaste : QualitativeTasteFragment(true) {
        override val modes: Int = R.array.bodyBreasts
        override fun crushFilter(cr: Crush): Boolean =
            (cr.status and Crush.STAT_GENDER).let { it != 2.toByte() && it != 4.toByte() }

        override fun crushProperty(cr: Crush): Short =
            ((cr.body and Crush.BODY_BREASTS.first) shr Crush.BODY_BREASTS.second).toShort()
    }

    class PenisTaste : QualitativeTasteFragment(true) {
        override val modes: Int = R.array.bodyPenis
        override fun crushFilter(cr: Crush): Boolean =
            (cr.status and Crush.STAT_GENDER).let { it != 1.toByte() && it != 4.toByte() }

        override fun crushProperty(cr: Crush): Short =
            ((cr.body and Crush.BODY_PENIS.first) shr Crush.BODY_PENIS.second).toShort()
    }

    abstract class QuantitativeTasteFragment : TasteFragment() {
        @get:StringRes
        abstract val topic: Int

        @SuppressLint("SetTextI18n")
        override fun preAnalysis() {
            b.title.text = getString(R.string.taste) + ": " + getString(topic)
        }

        override suspend fun statisticise(): AbstractChartData {
            when (c.vm.chartType) {

                ChartType.COMPOSITIONAL.ordinal -> {
                    for (p in c.vm.crushSumIndex!!.entries) {
                        val div = crushProperty(p.key)
                        if (div !in counts)
                            counts[div] = p.value
                        else
                            counts[div] = counts[div]!! + p.value
                        sumOfAll += p.value
                    }

                    val data = arrayListOf<SliceValue>()
                    for ((div, score) in counts.toSortedMap()) {
                        if (score == 0f) continue
                        data.add(
                            createSliceValue(
                                score,
                                div.toInt(),
                                if (div != 0.toShort()) "${div.toInt() * 10}s"
                                else getString(R.string.unspecified)
                            )
                        )
                    }
                    return PieChartData(data).setHasLabels(true)
                }

                ChartType.TIME_SERIES.ordinal -> {
                    // TODO
                    return LineChartData.generateDummyData()
                }

                else -> throw IllegalArgumentException("ChartType not implemented!")
            }
        }
    }

    class HeightTaste : QuantitativeTasteFragment() {
        override val topic: Int = R.string.height
        override fun crushProperty(cr: Crush): Short =
            (if (cr.height == -1f) 0 else (cr.height / 10f).toInt()).toShort()
    }

    class AgeTaste : QuantitativeTasteFragment() {
        override val topic: Int = R.string.age
        override fun crushProperty(cr: Crush): Short {
            if (cr.birth.isNullOrBlank()) return 0.toShort()
            val year = cr.birth!!.split("/")[0]
            if (year.isEmpty()) return 0.toShort()
            return (year.toInt() / 10).toShort()
        }
    }

    enum class ChartType(val view: KClass<out AbstractChartView>) {
        COMPOSITIONAL(PieChartView::class),
        TIME_SERIES(LineChartView::class)
    }
}
