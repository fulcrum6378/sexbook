package ir.mahdiparastesh.sexbook.stat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ir.mahdiparastesh.hellocharts.model.PieChartData
import ir.mahdiparastesh.hellocharts.model.SliceValue
import ir.mahdiparastesh.sexbook.Fun.tripleRound
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.databinding.SumPieBinding
import ir.mahdiparastesh.sexbook.more.BaseActivity

class SumPie : Fragment() {
    val c: BaseActivity by lazy { activity as BaseActivity }
    private lateinit var b: SumPieBinding

    override fun onCreateView(inf: LayoutInflater, parent: ViewGroup?, state: Bundle?): View =
        SumPieBinding.inflate(layoutInflater, parent, false).apply { b = this }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val data = arrayListOf<SliceValue>()
        c.m.summary?.scores?.entries?.sortedBy {
            it.value.sumOf { s -> s.value.toDouble() }.toFloat()
        }?.forEach {
            val score = it.value.sumOf { s -> s.value.toDouble() }.toFloat()
            data.add(SliceValue(score, c.color(R.color.CPD))
                .apply { setLabel("${it.key} {${score.tripleRound()}}") })
        }
        b.root.pieChartData = PieChartData(data).apply {
            setHasLabelsOnlyForSelected(true) // setHasLabels(true)
        }
    }
}
