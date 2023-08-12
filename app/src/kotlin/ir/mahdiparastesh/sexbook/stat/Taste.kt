package ir.mahdiparastesh.sexbook.stat

import ir.mahdiparastesh.hellocharts.model.AbstractChartData
import ir.mahdiparastesh.hellocharts.model.PieChartData
import ir.mahdiparastesh.hellocharts.model.SliceValue
import ir.mahdiparastesh.hellocharts.view.AbstractChartView
import ir.mahdiparastesh.sexbook.Fun.tripleRound
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.databinding.TasteBinding

class Taste : ChartActivity<TasteBinding>() {
    override val b by lazy { TasteBinding.inflate(layoutInflater) }
    override val chartView: AbstractChartView get() = b.main

    override suspend fun draw(): AbstractChartData {
        val data = arrayListOf<SliceValue>()
        val genders = resources.getStringArray(R.array.genders)
            .apply { this[0] = getString(R.string.unspecified) }
        for (g in genders.indices) {
            val byt = g.toByte()
            var score = m.liefde.value?.filter { it.gender == byt }
                ?.sumOf { it.sum(m)!!.toDouble() }?.toFloat() ?: 0f
            if (g == 0) {
                // TODO m.summary!!.scores.keys
                score += 0
            }
            data.add(
                SliceValue(score, color(R.color.CPV_LIGHT))
                    .apply { setLabel("${genders[g]} {${score.tripleRound()}}") })
        }
        return PieChartData(data).apply {
            setHasLabelsOnlyForSelected(true)
        }
    }

    override suspend fun render(data: AbstractChartData) {
        b.main.pieChartData = data as PieChartData
    }
}
