package ir.mahdiparastesh.sexbook.stat

import android.graphics.Color
import android.os.Bundle
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.databinding.PopularityBinding
import ir.mahdiparastesh.sexbook.more.BaseActivity
import lecho.lib.hellocharts.model.Line
import lecho.lib.hellocharts.model.LineChartData
import lecho.lib.hellocharts.model.PointValue

class Popularity : BaseActivity() {
    private lateinit var b: PopularityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = PopularityBinding.inflate(layoutInflater)
        setContentView(b.root)
        if (night()) window.decorView.setBackgroundColor(color(R.color.CP))

        if (m.onani.value == null || m.summary.value == null) {
            onBackPressed(); return; }
        val stb = Singular.sinceTheBeginning(c, m.onani.value!!)
        var stars = ArrayList<Star>()
        for (x in m.summary.value!!.scores) {
            if (Summary.isUnknown(x.key)) continue
            val scores = ArrayList<Star.Frame>()
            for (month in stb)
                scores.add(Star.Frame(Singular.calcHistory(c, x.value, month), month))
            stars.add(Star(x.key, scores.toTypedArray()))
        }
        stars.sortWith(Star.Sort(1))
        stars.sortWith(Star.Sort())

        b.main.lineChartData = LineChartData().setLines(LineFactory(this, stars))
        b.main.isViewportCalculationEnabled = false
    }

    class Star(val name: String, val frames: Array<Frame>) {
        class Sort(val by: Int = 0) : Comparator<Star> {
            override fun compare(a: Star, b: Star) = when (by) {
                1 -> a.name.compareTo(b.name)
                else -> b.frames.sumOf { (it.score * 100f).toInt() } -
                        a.frames.sumOf { (it.score * 100f).toInt() }
            }
        }

        data class Frame(val score: Float, val month: String)
    }

    class LineFactory(c: BaseActivity, stars: List<Star>) : ArrayList<Line>(stars.map {
        Line(it.frames.mapIndexed { i, frame ->
            PointValue(i.toFloat(), frame.score)
                .setLabel("${it.name} : ${frame.month} (${frame.score})")
        })
            .setColor(
                arrayListOf(
                    Color.BLUE, Color.RED, Color.CYAN, Color.GREEN,
                    if (c.night()) Color.WHITE else Color.BLACK
                ).random()
            )
            .setCubic(true)
            .setHasLabelsOnlyForSelected(true)
    })
}
