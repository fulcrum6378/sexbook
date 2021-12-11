package ir.mahdiparastesh.sexbook.stat

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.anychart.AnyChart
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.enums.*
import com.anychart.graphics.vector.Stroke
import ir.mahdiparastesh.sexbook.Fun
import ir.mahdiparastesh.sexbook.Model
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.databinding.GrowthBinding
import ir.mahdiparastesh.sexbook.stat.Popularity.Frame
import ir.mahdiparastesh.sexbook.stat.Popularity.Star

class Growth : AppCompatActivity() {
    private lateinit var b: GrowthBinding
    private lateinit var m: Model

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = GrowthBinding.inflate(layoutInflater)
        m = ViewModelProvider(this, Model.Factory()).get("Model", Model::class.java)
        setContentView(b.root)
        Fun.init(this, b.root)
        if (Fun.night) window.decorView.setBackgroundColor(Fun.color(R.color.CP))

        if (m.onani.value == null || m.summary.value == null) {
            onBackPressed(); return; }
        val stb = Singular.sinceTheBeginning(m.onani.value!!)
        var stars = ArrayList<Star>()
        for (x in m.summary.value!!.scores) {
            if (Summary.isUnknown(x.key)) continue
            val scores = ArrayList<Float>()
            for (month in stb)
                scores.add(Singular.calcHistory(x.value, month, true))
            stars.add(Star(x.key, scores.toTypedArray()))
        }
        stars.sortWith(Star.Sort(1))
        stars.sortWith(Star.Sort())

        val data = ArrayList<DataEntry>()
        for (s in stb.indices) {
            val thisMonthsScores = ArrayList<Float>()
            stars.forEach { thisMonthsScores.add(it.scores[s]) }
            data.add(Frame(stb[s], thisMonthsScores))
        }

        AnyChart.line().apply {
            animation(true)
            crosshair().enabled(true)
            crosshair()
                .yLabel(true) // DO: yStroke
                .yStroke(null as Stroke?, null, null, null as String?, null as String?)
            tooltip().positionMode(TooltipPositionMode.POINT)

            val set = com.anychart.data.Set.instantiate().apply { data(data) }
            var ss = 0
            stars.forEach {
                line(
                    set.mapAs("{ x: 'x', value: 'value${if (ss > 0) "$ss" else ""}' }")
                ).name(it.name)
                hovered().markers().apply {
                    enabled(true)
                    type(MarkerType.CIRCLE)
                    size(4.0)
                }
                tooltip().apply {
                    position("right")
                    anchor(Anchor.LEFT_CENTER)
                    offsetX(5.0)
                    offsetY(5.0)
                }
                ss++
            }
            legend().apply { // Names of the Stars
                enabled(true)
                fontSize(13.0)
                padding(0.0, 0.0, 11.0, 0.0)
                position(Orientation.TOP)
                positionMode(LegendPositionMode.OUTSIDE)
            }
            background(resources.getString(R.string.anyChartBG))
            b.main.setChart(this)
        }
    }
}
