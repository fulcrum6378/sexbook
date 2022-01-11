package ir.mahdiparastesh.sexbook.stat

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import ir.mahdiparastesh.sexbook.Fun
import ir.mahdiparastesh.sexbook.Model
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.databinding.PopularityBinding
import lecho.lib.hellocharts.model.Line
import lecho.lib.hellocharts.model.LineChartData
import lecho.lib.hellocharts.model.PointValue

class Popularity : AppCompatActivity() {
    private lateinit var b: PopularityBinding
    private lateinit var m: Model

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = PopularityBinding.inflate(layoutInflater)
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
            val scores = ArrayList<Star.Frame>()
            for (month in stb)
                scores.add(Star.Frame(Singular.calcHistory(x.value, month), month))
            stars.add(Star(x.key, scores.toTypedArray()))
        }
        stars.sortWith(Star.Sort(1))
        stars.sortWith(Star.Sort())

        b.main.lineChartData = LineChartData().setLines(LineFactory(stars))
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

    class LineFactory(stars: List<Star>) : ArrayList<Line>(stars.map {
        Line(it.frames.mapIndexed { i, frame ->
            PointValue(i.toFloat(), frame.score)
                .setLabel("${it.name} : ${frame.month} (${frame.score})")
        })
            .setColor(
                arrayListOf(
                    Color.BLUE, Color.RED, Color.CYAN, Color.GREEN,
                    if (Fun.night) Color.WHITE else Color.BLACK
                ).random()
            )
            .setCubic(true)
            .setHasLabelsOnlyForSelected(true)
    })
}
