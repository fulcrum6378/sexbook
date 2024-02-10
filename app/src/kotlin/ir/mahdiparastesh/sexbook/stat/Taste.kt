package ir.mahdiparastesh.sexbook.stat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ArrayRes
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import ir.mahdiparastesh.hellocharts.model.PieChartData
import ir.mahdiparastesh.hellocharts.model.SliceValue
import ir.mahdiparastesh.sexbook.Fun.show
import ir.mahdiparastesh.sexbook.Fun.sumOf
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.data.Crush
import ir.mahdiparastesh.sexbook.databinding.TasteBinding
import ir.mahdiparastesh.sexbook.databinding.TasteFragmentBinding
import ir.mahdiparastesh.sexbook.base.BaseActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.experimental.and
import kotlin.math.roundToInt

class Taste : BaseActivity() {
    private val b by lazy { TasteBinding.inflate(layoutInflater) }
    private val jobs: ArrayList<Job> = arrayListOf()

    val index = arrayListOf<Pair<Crush, Float>>()
    val pieColour by lazy { color(R.color.CPV_LIGHT) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(b.root)
        if (night())
            window.decorView.setBackgroundColor(themeColor(com.google.android.material.R.attr.colorPrimary))
        if (m.people == null || m.summary == null) {
            onBackPressed(); return; }

        CoroutineScope(Dispatchers.IO).launch {
            var orgasms: ArrayList<Summary.Orgasm>
            var sum: Float
            for (p in m.people!!) {
                orgasms = m.summary!!.scores[p.key] ?: continue
                sum = orgasms.sumOf { it.value }
                index.add(p to sum)
            }

            withContext(Dispatchers.Main) {
                b.root.adapter = object : FragmentStateAdapter(this@Taste) {
                    override fun getItemCount(): Int = 9
                    override fun createFragment(i: Int): Fragment = when (i) {
                        1 -> SkinColourTaste()
                        2 -> EyeColourTaste()
                        3 -> EyeShapeTaste()
                        4 -> FaceShapeTaste()
                        5 -> FatTaste()
                        6 -> MuscleTaste()
                        7 -> BreastsTaste()
                        8 -> PenisTaste()
                        else -> GenderTaste()
                    }
                }
            }
        }
    }

    abstract class TasteFragment : Fragment() {
        protected val c: Taste by lazy { activity as Taste }
        protected lateinit var b: TasteFragmentBinding
        private var myJob: Job? = null
        private var sumOfAll = 0f

        @get:ArrayRes
        abstract val modes: Int

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
        ): View = TasteFragmentBinding.inflate(layoutInflater, container, false)
            .apply { b = this }.root

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            // Title
            val arModes = resources.getStringArray(modes)
            b.title.text = c.getString(R.string.taste) + ": " +
                    (if (modes == R.array.genders)
                        c.getString(R.string.gender)
                    else arModes[0].substring(0..(arModes[0].length - 2)))

            // Prepare the diagram
            myJob = CoroutineScope(Dispatchers.IO).launch {
                arModes[0] = getString(R.string.unspecified)
                val stats = hashMapOf<Byte, Float>()
                for (mode in arModes.indices) stats[mode.toByte()] = 0f
                for (p in c.index) {
                    if (!crushFilter(p.first)) continue
                    val mode = crushProperty(p.first)
                    stats[mode] = stats[mode]!! + p.second
                    sumOfAll += p.second
                }
                stats[0] = stats[0]!! + c.m.summary!!.unknown
                sumOfAll += c.m.summary!!.unknown

                val data = arrayListOf<SliceValue>()
                for (mode in arModes.indices) {
                    val score = stats[mode.toByte()]!!
                    if (score == 0f) continue
                    data.add(SliceValue(score, c.pieColour).apply {
                        setLabel(
                            "${arModes[mode]}: ${score.show()}" +
                                    " (${((100f / sumOfAll) * score).roundToInt()}%)"
                        )
                    })
                }

                withContext(Dispatchers.Main) {
                    b.main.pieChartData = PieChartData(data).apply { setHasLabels(true) }
                    b.main.isInvisible = false
                }
                myJob?.also { c.jobs.remove(it) }
                myJob = null
            }
            myJob?.also { c.jobs.add(it) }
        }

        abstract fun crushFilter(cr: Crush): Boolean
        abstract fun crushProperty(cr: Crush): Byte
    }

    class GenderTaste : TasteFragment() {
        override val modes: Int = R.array.genders
        override fun crushFilter(cr: Crush): Boolean = true
        override fun crushProperty(cr: Crush): Byte =
            cr.status and Crush.STAT_GENDER
    }

    class SkinColourTaste : TasteFragment() {
        override val modes: Int = R.array.bodySkinColour
        override fun crushFilter(cr: Crush): Boolean = true
        override fun crushProperty(cr: Crush): Byte =
            ((cr.body and Crush.BODY_SKIN_COLOUR.first) shr Crush.BODY_SKIN_COLOUR.second).toByte()
    }

    class EyeColourTaste : TasteFragment() {
        override val modes: Int = R.array.bodyEyeColour
        override fun crushFilter(cr: Crush): Boolean = true
        override fun crushProperty(cr: Crush): Byte =
            ((cr.body and Crush.BODY_EYE_COLOUR.first) shr Crush.BODY_EYE_COLOUR.second).toByte()
    }

    class EyeShapeTaste : TasteFragment() {
        override val modes: Int = R.array.bodyEyeShape
        override fun crushFilter(cr: Crush): Boolean = true
        override fun crushProperty(cr: Crush): Byte =
            ((cr.body and Crush.BODY_EYE_SHAPE.first) shr Crush.BODY_EYE_SHAPE.second).toByte()
    }

    class FaceShapeTaste : TasteFragment() {
        override val modes: Int = R.array.bodyFaceShape
        override fun crushFilter(cr: Crush): Boolean = true
        override fun crushProperty(cr: Crush): Byte =
            ((cr.body and Crush.BODY_FACE_SHAPE.first) shr Crush.BODY_FACE_SHAPE.second).toByte()
    }

    class FatTaste : TasteFragment() {
        override val modes: Int = R.array.bodyFat
        override fun crushFilter(cr: Crush): Boolean = true
        override fun crushProperty(cr: Crush): Byte =
            ((cr.body and Crush.BODY_FAT.first) shr Crush.BODY_FAT.second).toByte()
    }

    class MuscleTaste : TasteFragment() {
        override val modes: Int = R.array.bodyMuscle
        override fun crushFilter(cr: Crush): Boolean = true
        override fun crushProperty(cr: Crush): Byte =
            ((cr.body and Crush.BODY_MUSCLE.first) shr Crush.BODY_MUSCLE.second).toByte()
    }

    class BreastsTaste : TasteFragment() {
        override val modes: Int = R.array.bodyBreasts
        override fun crushFilter(cr: Crush): Boolean =
            (cr.status and Crush.STAT_GENDER).let { it != 2.toByte() && it != 4.toByte() }

        override fun crushProperty(cr: Crush): Byte =
            ((cr.body and Crush.BODY_BREASTS.first) shr Crush.BODY_BREASTS.second).toByte()
    }

    class PenisTaste : TasteFragment() {
        override val modes: Int = R.array.bodyPenis
        override fun crushFilter(cr: Crush): Boolean =
            (cr.status and Crush.STAT_GENDER).let { it != 1.toByte() && it != 4.toByte() }

        override fun crushProperty(cr: Crush): Byte =
            ((cr.body and Crush.BODY_PENIS.first) shr Crush.BODY_PENIS.second).toByte()
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun onBackPressed() {
        for (job in jobs) job.cancel()
        @Suppress("DEPRECATION") super.onBackPressed()
    }
}
