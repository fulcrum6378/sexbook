package ir.mahdiparastesh.sexbook.stat

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ArrayRes
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ir.mahdiparastesh.hellocharts.model.PieChartData
import ir.mahdiparastesh.hellocharts.model.SliceValue
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.base.BaseActivity
import ir.mahdiparastesh.sexbook.base.BaseDialog
import ir.mahdiparastesh.sexbook.data.Crush
import ir.mahdiparastesh.sexbook.databinding.ChartPieFragmentBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.experimental.and
import kotlin.math.roundToInt

class CrushesStat : BaseDialog() {
    companion object {
        const val BUNDLE_WHICH_LIST = "which_list"
        const val TAG = "crushes_stat"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val pager = ViewPager2(c)
        pager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = 10
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
                else -> GenderStat()
            }.apply { arguments = this@CrushesStat.requireArguments() }
        }

        isCancelable = true
        return MaterialAlertDialogBuilder(c).apply {
            setTitle(R.string.crushesStat)
            setView(pager)
        }.create()
    }

    abstract class CrshStatFragment : Fragment() {
        protected val c: BaseActivity by lazy { activity as BaseActivity }
        protected lateinit var b: ChartPieFragmentBinding
        private val pieColour by lazy { c.color(R.color.CPV_LIGHT) }

        @get:ArrayRes
        abstract val modes: Int
        abstract fun crushFilter(cr: Crush): Boolean
        abstract fun crushProperty(cr: Crush): Byte

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
        ): View = ChartPieFragmentBinding.inflate(layoutInflater, container, false)
            .apply { b = this }.root

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            val whichList = requireArguments().getInt(BUNDLE_WHICH_LIST)
            val list = if (whichList == 0) c.m.people!! else c.m.liefde!!

            // set the title
            val arModes = resources.getStringArray(modes)
            b.title.text =
                if (modes == R.array.genders) c.getString(R.string.gender)
                else arModes[0].substring(0..(arModes[0].length - 2))

            // prepare the diagram
            CoroutineScope(Dispatchers.IO).launch {
                arModes[0] = getString(R.string.unspecified)
                val stats = hashMapOf<Byte, Int>()
                for (mode in arModes.indices) stats[mode.toByte()] = 0
                for (p in list) {
                    if (!crushFilter(p)) continue
                    val mode = crushProperty(p)
                    stats[mode] = stats[mode]!! + 1
                }

                val data = arrayListOf<SliceValue>()
                for (mode in arModes.indices) {
                    val score = stats[mode.toByte()]!!
                    if (score == 0) continue
                    data.add(SliceValue(score.toFloat(), pieColour).apply {
                        setLabel(
                            "${arModes[mode]}: $score" +
                                    " (${((100f / list.size) * score).roundToInt()}%)"
                        )
                    })
                }

                withContext(Dispatchers.Main) {
                    b.main.pieChartData = PieChartData(data).apply { setHasLabels(true) }
                    b.main.isInvisible = false
                }
            }
        }
    }

    class GenderStat : CrshStatFragment() {
        override val modes: Int = R.array.genders
        override fun crushFilter(cr: Crush): Boolean = true
        override fun crushProperty(cr: Crush): Byte =
            cr.status and Crush.STAT_GENDER
    }

    class SkinColourStat : CrshStatFragment() {
        override val modes: Int = R.array.bodySkinColour
        override fun crushFilter(cr: Crush): Boolean = true
        override fun crushProperty(cr: Crush): Byte =
            ((cr.body and Crush.BODY_SKIN_COLOUR.first) shr Crush.BODY_SKIN_COLOUR.second).toByte()
    }

    class HairColourStat : CrshStatFragment() {
        override val modes: Int = R.array.bodyHairColour
        override fun crushFilter(cr: Crush): Boolean = true
        override fun crushProperty(cr: Crush): Byte =
            ((cr.body and Crush.BODY_HAIR_COLOUR.first) shr Crush.BODY_HAIR_COLOUR.second).toByte()
    }

    class EyeColourStat : CrshStatFragment() {
        override val modes: Int = R.array.bodyEyeColour
        override fun crushFilter(cr: Crush): Boolean = true
        override fun crushProperty(cr: Crush): Byte =
            ((cr.body and Crush.BODY_EYE_COLOUR.first) shr Crush.BODY_EYE_COLOUR.second).toByte()
    }

    class EyeShapeStat : CrshStatFragment() {
        override val modes: Int = R.array.bodyEyeShape
        override fun crushFilter(cr: Crush): Boolean = true
        override fun crushProperty(cr: Crush): Byte =
            ((cr.body and Crush.BODY_EYE_SHAPE.first) shr Crush.BODY_EYE_SHAPE.second).toByte()
    }

    class FaceShapeStat : CrshStatFragment() {
        override val modes: Int = R.array.bodyFaceShape
        override fun crushFilter(cr: Crush): Boolean = true
        override fun crushProperty(cr: Crush): Byte =
            ((cr.body and Crush.BODY_FACE_SHAPE.first) shr Crush.BODY_FACE_SHAPE.second).toByte()
    }

    class FatStat : CrshStatFragment() {
        override val modes: Int = R.array.bodyFat
        override fun crushFilter(cr: Crush): Boolean = true
        override fun crushProperty(cr: Crush): Byte =
            ((cr.body and Crush.BODY_FAT.first) shr Crush.BODY_FAT.second).toByte()
    }

    class MuscleStat : CrshStatFragment() {
        override val modes: Int = R.array.bodyMuscle
        override fun crushFilter(cr: Crush): Boolean = true
        override fun crushProperty(cr: Crush): Byte =
            ((cr.body and Crush.BODY_MUSCLE.first) shr Crush.BODY_MUSCLE.second).toByte()
    }

    class BreastsStat : CrshStatFragment() {
        override val modes: Int = R.array.bodyBreasts
        override fun crushFilter(cr: Crush): Boolean =
            (cr.status and Crush.STAT_GENDER).let { it != 2.toByte() && it != 4.toByte() }

        override fun crushProperty(cr: Crush): Byte =
            ((cr.body and Crush.BODY_BREASTS.first) shr Crush.BODY_BREASTS.second).toByte()
    }

    class PenisStat : CrshStatFragment() {
        override val modes: Int = R.array.bodyPenis
        override fun crushFilter(cr: Crush): Boolean =
            (cr.status and Crush.STAT_GENDER).let { it != 1.toByte() && it != 4.toByte() }

        override fun crushProperty(cr: Crush): Byte =
            ((cr.body and Crush.BODY_PENIS.first) shr Crush.BODY_PENIS.second).toByte()
    }
}