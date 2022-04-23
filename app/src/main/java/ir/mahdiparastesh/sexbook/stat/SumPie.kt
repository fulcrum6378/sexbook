package ir.mahdiparastesh.sexbook.stat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.databinding.SumPieBinding
import ir.mahdiparastesh.sexbook.more.BaseActivity
import lecho.lib.hellocharts.model.PieChartData
import lecho.lib.hellocharts.model.SliceValue

class SumPie : Fragment() {
    val c: BaseActivity by lazy { activity as BaseActivity }
    private lateinit var b: SumPieBinding

    override fun onCreateView(inf: LayoutInflater, parent: ViewGroup?, state: Bundle?): View {
        b = SumPieBinding.inflate(layoutInflater, parent, false)

        val data = arrayListOf<SliceValue>()
        c.m.summary.value!!.scores.entries.sortedBy {
            it.value.sumOf { s -> s.value.toDouble() }.toFloat()
        }.forEach {
            data.add(SliceValue(
                it.value.sumOf { s -> s.value.toDouble() }.toFloat(),
                c.color(R.color.CPD)
            ).apply { setLabel(it.key) })
        }
        b.root.pieChartData = PieChartData(data).apply {
            setHasLabelsOnlyForSelected(true) //setHasLabels(true)
        }

        return b.root
    }
}
