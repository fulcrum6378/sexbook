package ir.mahdiparastesh.sexbook.stat

import android.os.Bundle
import ir.mahdiparastesh.hellocharts.model.Line
import ir.mahdiparastesh.hellocharts.model.LineChartData
import ir.mahdiparastesh.hellocharts.model.PointValue
import ir.mahdiparastesh.sexbook.Fun.randomColor
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.databinding.AdorabilityBinding
import ir.mahdiparastesh.sexbook.more.BaseActivity

class Adorability : BaseActivity() {
    private lateinit var b: AdorabilityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = AdorabilityBinding.inflate(layoutInflater)
        setContentView(b.root)
        if (night()) window.decorView.setBackgroundColor(color(R.color.CP))

        if (m.onani.value == null || m.summary == null) {
            @Suppress("DEPRECATION") onBackPressed(); return; }
        val stb = Singular.sinceTheBeginning(this, m.onani.value!!)
        val stars = ArrayList<Star>()
        for (x in m.summary!!.scores) {
            if (Summary.isUnknown(x.key)) continue
            val scores = ArrayList<Star.Frame>()
            for (month in stb)
                scores.add(Star.Frame(Singular.calcHistory(this, x.value, month), month))
            stars.add(Star(x.key, scores.toTypedArray()))
        }
        stars.sortWith(Star.Sort(1))
        stars.sortWith(Star.Sort())

        b.main.lineChartData = LineChartData().setLines(LineFactory(this, stars))
        b.main.isViewportCalculationEnabled = false // never do it before setLineChatData
        // it cannot be zoomed out.
    }

    class Star(val name: String, val frames: Array<Frame>) {
        class Sort(private val by: Int = 0) : Comparator<Star> {
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
            .setColor(c.randomColor())
            .setCubic(true)
            .setHasLabelsOnlyForSelected(true)
    })
}
