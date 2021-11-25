package ir.mahdiparastesh.sexbook.stat

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.anychart.AnyChart
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.anychart.data.Set
import com.anychart.enums.*
import com.anychart.graphics.vector.Stroke
import ir.mahdiparastesh.sexbook.Fun
import ir.mahdiparastesh.sexbook.Model
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.databinding.PopularityBinding
import kotlin.Comparator
import kotlin.collections.ArrayList

class Popularity : AppCompatActivity() {
    private lateinit var b: PopularityBinding
    private lateinit var m: Model

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = PopularityBinding.inflate(layoutInflater)
        m = ViewModelProvider(this, Model.Factory()).get("Model", Model::class.java)
        setContentView(b.root)
        Fun.init(this)
        if (Fun.night) window.decorView.setBackgroundColor(Fun.color(R.color.CP))

        if (m.onani.value == null || m.summary.value == null) {
            onBackPressed(); return; }
        val stb = Singular.sinceTheBeginning(m.onani.value!!)
        var stars = ArrayList<Star>()
        for (x in m.summary.value!!.scores) {
            if (Summary.isUnknown(x.key)) continue
            val scores = ArrayList<Float>()
            for (month in stb)
                scores.add(Singular.calcHistory(x.value, month))
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

            val set = Set.instantiate().apply { data(data) }
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
            background(if (Fun.night) "#3A3A3A" else "#FFFFFF")
            b.main.setChart(this)
        }
    }

    class Frame constructor(x: String, values: List<Number>) :
        ValueDataEntry(x, values[0]) {
        init {
            for (i in 1 until values.size) setValue("value$i", values[i])
        }
    }

    class Star(val name: String, val scores: Array<Float>) {
        class Sort(val by: Int = 0) : Comparator<Star> {
            override fun compare(a: Star, b: Star) = when (by) {
                1 -> a.name.compareTo(b.name)
                else -> (b.scores.sum() - a.scores.sum()).toInt()
            }
        }
    }
}
