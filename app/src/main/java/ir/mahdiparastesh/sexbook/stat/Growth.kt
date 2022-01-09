package ir.mahdiparastesh.sexbook.stat

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import ir.mahdiparastesh.sexbook.Fun
import ir.mahdiparastesh.sexbook.Model
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.databinding.GrowthBinding
import ir.mahdiparastesh.sexbook.stat.Popularity.Star
import lecho.lib.hellocharts.model.LineChartData

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
            val frames = ArrayList<Star.Frame>()
            for (month in stb)
                frames.add(Star.Frame(Singular.calcHistory(x.value, month, true), month))
            stars.add(Star(x.key, frames.toTypedArray()))
        }
        stars.sortWith(Star.Sort(1))
        stars.sortWith(Star.Sort())

        b.main.lineChartData = LineChartData().setLines(Popularity.LineFactory(stars))
    }
}
