package ir.mahdiparastesh.sexbook.stat

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.MainThread
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ir.mahdiparastesh.hellocharts.model.PieChartData
import ir.mahdiparastesh.hellocharts.model.SliceValue
import ir.mahdiparastesh.sexbook.People
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.base.BaseActivity
import ir.mahdiparastesh.sexbook.base.BaseDialog
import ir.mahdiparastesh.sexbook.databinding.CrshStatFragmentBinding
import ir.mahdiparastesh.sexbook.stat.base.CrushAgeChart
import ir.mahdiparastesh.sexbook.stat.base.CrushBreastsChart
import ir.mahdiparastesh.sexbook.stat.base.CrushChart
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

class CrushesStat : BaseDialog<BaseActivity>() {

    companion object {
        const val BUNDLE_WHICH_LIST = "which_list"
        const val TAG = "crushes_stat"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val pager = ViewPager2(c)
        pager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = 14
            override fun createFragment(i: Int): Fragment = when (i) {
                1 -> SkinColourStat()
                2 -> HairColourStat()
                3 -> EyeColourStat()
                4 -> EyeShapeStat()
                5 -> FaceShapeStat()
                6 -> FatStat()
                7 -> MuscleStat()
                8 -> BreastsStat()
                9 -> PenisStat()
                10 -> HeightStat()
                11 -> AgeStat()
                12 -> FirstMetStat()
                13 -> FictionalityStat()
                else -> GenderStat()
            }.apply { arguments = this@CrushesStat.requireArguments() }
        }

        isCancelable = true
        return MaterialAlertDialogBuilder(c).apply {
            setTitle(R.string.crushesStat)
            setView(pager)
        }.create()
    }

    /* ------------------------------------------------------ */

    abstract class CrshStatFragment : Fragment(), CrushChart {
        protected val c: BaseActivity by lazy { activity as BaseActivity }
        protected lateinit var b: CrshStatFragmentBinding
        protected val counts = hashMapOf<Short, Int>()

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
        ): View = CrshStatFragmentBinding.inflate(layoutInflater, container, false)
            .also { b = it }.root

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            val whichList = requireArguments().getInt(BUNDLE_WHICH_LIST)
            val list = if (whichList == 0) (c as People).vm.visPeople else c.c.liefde

            // set the title and miscellaneous stuff
            preAnalysis()

            // prepare the diagram
            CoroutineScope(Dispatchers.IO).launch {
                val data = statisticise(list)
                withContext(Dispatchers.Main) {
                    b.main.pieChartData = PieChartData(data).apply { setHasLabels(true) }
                    b.main.isInvisible = false
                }
            }
        }

        @MainThread
        abstract fun preAnalysis()
        abstract suspend fun statisticise(list: MutableList<String>): ArrayList<SliceValue>

        protected fun createSliceValue(score: Int, division: String, total: Int): SliceValue =
            SliceValue(score.toFloat(), c.chartColour).setLabel(
                "$division: $score (${((100f / total) * score).roundToInt()}%)"
            )
    }


    abstract class QualitativeStat : CrshStatFragment(), CrushQualitativeChart {
        private lateinit var arModes: Array<String>

        override fun preAnalysis() {
            arModes = resources.getStringArray(modes)
            b.title.text =
                if (modes == R.array.genders) c.getString(R.string.gender)
                else arModes[0].substring(0..(arModes[0].length - 2))
            arModes[0] = getString(R.string.unspecified)
        }

        override suspend fun statisticise(list: MutableList<String>): ArrayList<SliceValue> {
            for (mode in arModes.indices) counts[mode.toShort()] = 0
            for (person in list) {
                val p = c.c.people[person]!!
                if (isFiltered && !crushFilter(p)) continue
                val mode = crushProperty(p)
                counts[mode] = counts[mode]!! + 1
            }

            val data = arrayListOf<SliceValue>()
            for (mode in arModes.indices) {
                val score = counts[mode.toShort()]!!
                if (score == 0) continue
                data.add(createSliceValue(score, arModes[mode], list.size))
            }
            return data
        }
    }

    class GenderStat : QualitativeStat(), CrushGenderChart

    class SkinColourStat : QualitativeStat(), CrushSkinColourChart

    class HairColourStat : QualitativeStat(), CrushHairColourChart

    class EyeColourStat : QualitativeStat(), CrushEyeColourChart

    class EyeShapeStat : QualitativeStat(), CrushEyeShapeChart

    class FaceShapeStat : QualitativeStat(), CrushFaceShapeChart

    class FatStat : QualitativeStat(), CrushFatChart

    class MuscleStat : QualitativeStat(), CrushMuscleChart

    class BreastsStat : QualitativeStat(), CrushBreastsChart

    class PenisStat : QualitativeStat(), CrushPenisChart

    class FictionalityStat : QualitativeStat(), CrushFictionalityChart


    abstract class QuantitativeStat : CrshStatFragment(), CrushQuantitativeChart {

        override fun preAnalysis() {
            b.title.text = getString(topic)
        }

        override suspend fun statisticise(list: MutableList<String>): ArrayList<SliceValue> {
            for (person in list) {
                val p = c.c.people[person]!!
                val div = crushProperty(p)
                if (div !in counts) counts[div] = 1
                else counts[div] = counts[div]!! + 1
            }

            val data = arrayListOf<SliceValue>()
            for ((div, score) in counts.toSortedMap()) data.add(
                createSliceValue(
                    score, if (div != 0.toShort()) divisionName(div.toInt())
                    else getString(R.string.unspecified), list.size
                )
            )
            return data
        }
    }

    class HeightStat : QuantitativeStat(), CrushHeightChart

    class AgeStat : QuantitativeStat(), CrushAgeChart

    class FirstMetStat : QuantitativeStat(), CrushFirstMetChart
}
