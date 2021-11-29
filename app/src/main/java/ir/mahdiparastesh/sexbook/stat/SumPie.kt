package ir.mahdiparastesh.sexbook.stat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.anychart.AnyChart
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.anychart.enums.Align
import com.anychart.enums.LegendLayout
import ir.mahdiparastesh.sexbook.Fun
import ir.mahdiparastesh.sexbook.Model
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.databinding.SumPieBinding

class SumPie : Fragment() {
    private lateinit var b: SumPieBinding
    private lateinit var m: Model

    override fun onCreateView(inf: LayoutInflater, parent: ViewGroup?, state: Bundle?): View {
        b = SumPieBinding.inflate(layoutInflater, parent, false)
        m = ViewModelProvider(this, Model.Factory()).get("Model", Model::class.java)

        AnyChart.pie().apply {
            // title("Fruits imported in 2015 (in kg)")
            data(ArrayList<DataEntry>().apply {
                for (s in m.summary.value!!.scores)
                    add(ValueDataEntry(s.key, Summary.sumErections(s.value)))
            })
            labels().position("outside")
            legend()
                .position("center-bottom")
                .itemsLayout(LegendLayout.HORIZONTAL)
                .align(Align.CENTER)
            background(resources.getString(R.string.anyChartADBG))
            b.root.setChart(this)
        }
        /*pie.setOnClickListener(object : ListenersInterface.OnClickListener(arrayOf("x", "value")) {
            override fun onClick(event: Event) {
                Toast.makeText(
                    this@PieChartActivity,
                    event.getData().get("x").toString() + ":" + event.getData().get("value"),
                    Toast.LENGTH_SHORT
                ).show()
            }
        })*/
        return b.root
    }
}
