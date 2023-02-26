package ir.mahdiparastesh.sexbook.stat

import android.os.Bundle
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.databinding.GrowthBinding
import ir.mahdiparastesh.sexbook.more.BaseActivity
import ir.mahdiparastesh.sexbook.stat.Adorability.Star
import lecho.lib.hellocharts.model.LineChartData

class Growth : BaseActivity() {
    private lateinit var b: GrowthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = GrowthBinding.inflate(layoutInflater)
        setContentView(b.root)
        if (night()) window.decorView.setBackgroundColor(color(R.color.CP))

        if (m.onani.value == null || m.summary == null) {
            @Suppress("DEPRECATION") onBackPressed(); return; }
        val stb = Singular.sinceTheBeginning(this, m.onani.value!!)
        val stars = ArrayList<Star>()
        for (x in m.summary!!.scores) {
            if (Summary.isUnknown(x.key)) continue
            val frames = ArrayList<Star.Frame>()
            for (month in stb)
                frames.add(Star.Frame(Singular.calcHistory(this, x.value, month, true), month))
            stars.add(Star(x.key, frames.toTypedArray()))
        }
        stars.sortWith(Star.Sort(1))
        stars.sortWith(Star.Sort())

        b.main.lineChartData = LineChartData().setLines(Adorability.LineFactory(this, stars))
    }
}
