package ir.mahdiparastesh.sexbook.stat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import ir.mahdiparastesh.sexbook.Model
import ir.mahdiparastesh.sexbook.databinding.SumCloudBinding
import com.anychart.chart.common.dataentry.CategoryValueDataEntry

import com.anychart.chart.common.dataentry.DataEntry

import com.anychart.scales.OrdinalColor

import com.anychart.AnyChart

import com.anychart.charts.TagCloud

class SumCloud : Fragment() {
    private lateinit var b: SumCloudBinding
    private lateinit var m: Model

    override fun onCreateView(inf: LayoutInflater, parent: ViewGroup?, state: Bundle?): View {
        b = SumCloudBinding.inflate(layoutInflater, parent, false)
        m = ViewModelProvider(this, Model.Factory()).get("Model", Model::class.java)

        AnyChart.tagCloud().apply {
            //title("World Population")
            OrdinalColor.instantiate().also { oc ->
                oc.colors(arrayOf("#26959f", "#f18126", "#3b8ad8", "#60727b", "#e24b26"))
                colorScale(oc)
            }
            angles(arrayOf(-90.0, 0.0, 90.0))
            colorRange().enabled(true)
            colorRange().colorLineSize(15.0)
            data(ArrayList<DataEntry>().apply {
                for (s in m.summary.value!!.scores) add(
                    CategoryValueDataEntry(
                        s.key, "", Summary.sumErections(s.value).toDouble()
                    )
                )
            })
            b.root.setChart(this)
        }

        return b.root
    }
}
