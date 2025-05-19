package ir.mahdiparastesh.sexbook.stat

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.annotation.ArrayRes
import androidx.annotation.MainThread
import androidx.annotation.StringRes
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import ir.mahdiparastesh.hellocharts.model.LineChartData
import ir.mahdiparastesh.hellocharts.model.PieChartData
import ir.mahdiparastesh.hellocharts.model.SliceValue
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.base.BaseActivity
import ir.mahdiparastesh.sexbook.data.Crush
import ir.mahdiparastesh.sexbook.databinding.ChartPieFragmentBinding
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

class Taste : BaseActivity() {
    private lateinit var b: TasteBinding
    private val jobs: ArrayList<Job> = arrayListOf()
    var chartType: Int = 0
    val crushSumIndex = hashMapOf<Crush, Float>()

    enum class ChartType { COMPOSITIONAL, TIME_SERIES }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (c.summary == null) {
            onBackPressed(); return; }

        b = TasteBinding.inflate(layoutInflater)
        setContentView(b.root)
        if (night) window.decorView.setBackgroundColor(
            themeColor(com.google.android.material.R.attr.colorPrimary)
        )

        // chart types
        b.chartType.adapter = ArrayAdapter(
            c, R.layout.spinner_yellow, resources.getStringArray(R.array.tasteChartTypes)
        ).apply { setDropDownViewResource(R.layout.spinner_dd) }
        b.chartType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                chartType = position
                // TODO reload
            }
        }

        // prepare initial data
        CoroutineScope(Dispatchers.IO).launch {
            var orgasms: ArrayList<Summary.Orgasm>
            var sum: Float
            for (p in c.people.values) {
                orgasms = c.summary!!.scores[p.key] ?: continue
                sum = orgasms.sumOf { it.value }
                if (sum == 0f) continue
                crushSumIndex[p] = sum
            }

            withContext(Dispatchers.Main) {
                b.pager.adapter = object : FragmentStateAdapter(this@Taste) {
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
            }
        }

        // testing a line chart todo
        val stb = StatUtils.sinceTheBeginning(c)
        val stars = ArrayList<Star>()
        val genders = resources.getStringArray(R.array.genders)
        val genderOrgasms = hashMapOf<Short, ArrayList<Summary.Orgasm>>()
        var cr: Crush? = null
        var genderCode: Short
        for ((crushKey, orgasms) in c.summary!!.scores.entries) {
            cr = c.people[crushKey] ?: continue
            genderCode = (cr.status and Crush.STAT_GENDER).toShort()
            if (genderCode !in genderOrgasms) genderOrgasms[genderCode] = arrayListOf()
            genderOrgasms[genderCode]!!.addAll(orgasms)
        }
        for (g in genders.indices) {
            if (g.toShort() !in genderOrgasms) continue
            val scores = ArrayList<Star.Frame>()
            for (month in stb) scores.add(
                Star.Frame(StatUtils.calcHistory(c, genderOrgasms[g.toShort()]!!, month), month)
            )
            stars.add(Star(genders[g], scores.toTypedArray()))
        }
        // empty columns should not be removed or all the points will jump to the beginning!
        stars.sortWith(Star.Sort(1))
        stars.sortWith(Star.Sort())
        b.lineChart.lineChartData = LineChartData().setLines(LineFactory(stars))
    }

    abstract class TasteFragment : Fragment() {
        protected val c: Taste by lazy { activity as Taste }
        protected lateinit var b: ChartPieFragmentBinding
        private var myJob: Job? = null
        protected val counts = hashMapOf<Short, Float>()
        protected var sumOfAll = 0f

        abstract fun crushProperty(cr: Crush): Short

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
        ): View = ChartPieFragmentBinding.inflate(layoutInflater, container, false)
            .also { b = it }.root

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            // set the title and miscellaneous stuff
            preAnalysis()

            // prepare the diagram
            myJob = CoroutineScope(Dispatchers.IO).launch {
                sumOfAll += c.c.summary!!.unknown
                counts[0] = c.c.summary!!.unknown
                val data = statisticise()

                withContext(Dispatchers.Main) {
                    b.main.pieChartData = PieChartData(data).apply { setHasLabels(true) }
                    b.main.isInvisible = false
                }
                myJob?.also { c.jobs.remove(it) }
                myJob = null
            }
            myJob?.also { c.jobs.add(it) }
        }

        @MainThread
        abstract fun preAnalysis()
        abstract suspend fun statisticise(): ArrayList<SliceValue>

        protected fun createSliceValue(score: Float, division: String): SliceValue =
            SliceValue(score, c.chartColour).setLabel(
                "$division: ${score.show()} (${((100f / sumOfAll) * score).roundToInt()}%)"
            )
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

        override suspend fun statisticise(): ArrayList<SliceValue> {
            for (p in c.crushSumIndex.entries) {
                if (isFiltered && !crushFilter(p.key)) continue
                val mode = crushProperty(p.key)
                counts[mode] = counts[mode]!! + p.value
                sumOfAll += p.value
            }

            val data = arrayListOf<SliceValue>()
            for (mode in arModes.indices) {
                val score = counts[mode.toShort()]!!
                if (score == 0f) continue
                data.add(createSliceValue(score, arModes[mode]))
            }
            return data
        }
    }

    class GenderTaste : QualitativeTasteFragment() {
        override val modes: Int = R.array.genders
        override fun crushProperty(cr: Crush): Short =
            (cr.status and Crush.STAT_GENDER).toShort()
    }

    class SkinColourTaste : QualitativeTasteFragment() {
        override val modes: Int = R.array.bodySkinColour
        override fun crushProperty(cr: Crush): Short =
            ((cr.body and Crush.BODY_SKIN_COLOUR.first) shr Crush.BODY_SKIN_COLOUR.second).toShort()
    }

    class HairColourTaste : QualitativeTasteFragment() {
        override val modes: Int = R.array.bodyHairColour
        override fun crushProperty(cr: Crush): Short =
            ((cr.body and Crush.BODY_HAIR_COLOUR.first) shr Crush.BODY_HAIR_COLOUR.second).toShort()
    }

    class EyeColourTaste : QualitativeTasteFragment() {
        override val modes: Int = R.array.bodyEyeColour
        override fun crushProperty(cr: Crush): Short =
            ((cr.body and Crush.BODY_EYE_COLOUR.first) shr Crush.BODY_EYE_COLOUR.second).toShort()
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

        override suspend fun statisticise(): ArrayList<SliceValue> {
            for (p in c.crushSumIndex.entries) {
                val div = crushProperty(p.key)
                if (div !in counts)
                    counts[div] = p.value
                else
                    counts[div] = counts[div]!! + p.value
                sumOfAll += p.value
            }

            val data = arrayListOf<SliceValue>()
            for ((div, score) in counts.toSortedMap()) data.add(
                createSliceValue(
                    score,
                    if (div != 0.toShort()) "${div.toInt() * 10}s"
                    else getString(R.string.unspecified)
                )
            )
            return data
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


    @Suppress("OVERRIDE_DEPRECATION")
    override fun onBackPressed() {
        for (job in jobs) job.cancel()
        @Suppress("DEPRECATION") super.onBackPressed()
    }
}
