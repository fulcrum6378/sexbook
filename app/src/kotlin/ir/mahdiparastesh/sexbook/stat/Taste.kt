package ir.mahdiparastesh.sexbook.stat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.MainThread
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import ir.mahdiparastesh.hellocharts.model.AbstractChartData
import ir.mahdiparastesh.hellocharts.model.PieChartData
import ir.mahdiparastesh.hellocharts.model.SliceValue
import ir.mahdiparastesh.hellocharts.view.AbstractChartView
import ir.mahdiparastesh.sexbook.Fun.show
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.data.Crush
import ir.mahdiparastesh.sexbook.databinding.TasteBinding
import ir.mahdiparastesh.sexbook.databinding.TasteFragmentBinding
import ir.mahdiparastesh.sexbook.more.BaseActivity
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (night())
            window.decorView.setBackgroundColor(themeColor(com.google.android.material.R.attr.colorPrimary))
        if (m.onani == null || m.summary == null) {
            onBackPressed(); return; }

        b.root.adapter = TasteAdapter(this)
    }

    private inner class TasteAdapter(c: FragmentActivity) : FragmentStateAdapter(c) {
        override fun getItemCount(): Int = 2
        override fun createFragment(i: Int): Fragment = when (i) {
            1 -> GenderTaste()
            else -> GenderTaste()
        }
    }

    abstract class TasteFragment : Fragment() {
        protected val c: Taste by lazy { activity as Taste }
        protected lateinit var b: TasteFragmentBinding
        private var myJob: Job? = null

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
        ): View = TasteFragmentBinding.inflate(layoutInflater, container, false)
            .apply { b = this }.root

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            myJob = CoroutineScope(Dispatchers.IO).launch {
                val drawn = draw()
                withContext(Dispatchers.Main) {
                    b.main.pieChartData = drawn as PieChartData
                    b.main.isInvisible = false
                }
                myJob?.also { c.jobs.remove(it) }
                myJob = null
            }
            myJob?.also { c.jobs.add(it) }
        }

        abstract suspend fun draw(): AbstractChartData
    }

    class GenderTaste : TasteFragment() {
        override suspend fun draw(): AbstractChartData {
            val genders = resources.getStringArray(R.array.genders)
                .apply { this[0] = getString(R.string.unspecified) }
            val stats = hashMapOf<Byte, Double>()
            for (g in genders.indices) stats[g.toByte()] = 0.0
            val crushKeys = c.m.people?.map { it.key } ?: listOf()
            var sumOfAll = 0.0
            for (agent in c.m.summary!!.scores.keys) {
                val addable = c.m.summary!!.scores[agent]!!.sumOf { it.value.toDouble() }
                // `sumOf()` only accepts Double values!
                if (agent in crushKeys) {
                    val g = (c.m.people!!.find { it.key == agent }!!.status and Crush.STAT_GENDER)
                    try {
                        stats[g] = stats[g]!! + addable
                    } catch (_: NullPointerException) {
                        throw Exception(g.toString())
                    }
                } else stats[0] = stats[0]!! + addable
                sumOfAll += addable
            }

            val data = arrayListOf<SliceValue>()
            for (g in genders.indices) {
                val score = stats[g.toByte()]!!.toFloat()
                if (score == 0f) continue
                data.add(SliceValue(score, c.color(R.color.CPV_LIGHT)).apply {
                    setLabel(
                        "${genders[g]}: ${score.show()}" +
                                " (${((100f / sumOfAll) * score).roundToInt()}%)"
                    )
                })
            }
            return PieChartData(data).apply { setHasLabels(true) }
        }
    }

    /*class SkinColourTaste : TasteFragment() {
    }*/

    @Suppress("OVERRIDE_DEPRECATION")
    override fun onBackPressed() {
        for (job in jobs) job.cancel()
        @Suppress("DEPRECATION") super.onBackPressed()
    }
}
