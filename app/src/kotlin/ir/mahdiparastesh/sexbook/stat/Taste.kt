package ir.mahdiparastesh.sexbook.stat

import ir.mahdiparastesh.hellocharts.model.AbstractChartData
import ir.mahdiparastesh.hellocharts.model.PieChartData
import ir.mahdiparastesh.hellocharts.model.SliceValue
import ir.mahdiparastesh.hellocharts.view.AbstractChartView
import ir.mahdiparastesh.sexbook.Fun.show
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.data.Crush
import ir.mahdiparastesh.sexbook.databinding.TasteBinding
import kotlin.experimental.and
import kotlin.math.roundToInt

class Taste : ChartActivity<TasteBinding>() {
    override val b by lazy { TasteBinding.inflate(layoutInflater) }
    override val chartView: AbstractChartView get() = b.main

    override suspend fun draw(): AbstractChartData {
        val genders = resources.getStringArray(R.array.genders)
            .apply { this[0] = getString(R.string.unspecified) }
        val stats = hashMapOf<Byte, Double>()
        for (g in genders.indices) stats[g.toByte()] = 0.0
        val crushKeys = m.people?.map { it.key } ?: listOf()
        for (agent in m.summary!!.scores.keys) {
            val addable = m.summary!!.scores[agent]!!.sumOf { it.value.toDouble() }
            // `sumOf()` only accepts Double values!
            if (agent in crushKeys) {
                val g = (m.people!!.find { it.key == agent }!!.status and Crush.STAT_GENDER)
                try {
                    stats[g] = stats[g]!! + addable
                } catch (_: NullPointerException) {
                    throw Exception(g.toString())
                }
            } else stats[0] = stats[0]!! + addable
        }

        val data = arrayListOf<SliceValue>()
        val sumOfAll = m.onani?.size?.toFloat() ?: 0f
        for (g in genders.indices) {
            val score = stats[g.toByte()]!!.toFloat()
            if (score == 0f) continue
            data.add(SliceValue(score, color(R.color.CPV_LIGHT)).apply {
                setLabel("${genders[g]}: ${score.show()} (${((100f / sumOfAll) * score).roundToInt()}%)")
            })
        }
        return PieChartData(data).apply { setHasLabels(true) }
    }

    override suspend fun render(data: AbstractChartData) {
        b.main.pieChartData = data as PieChartData
    }
}
