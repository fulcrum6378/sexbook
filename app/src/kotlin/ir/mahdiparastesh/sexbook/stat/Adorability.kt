package ir.mahdiparastesh.sexbook.stat

import ir.mahdiparastesh.hellocharts.model.AbstractChartData
import ir.mahdiparastesh.hellocharts.model.LineChartData
import ir.mahdiparastesh.hellocharts.view.AbstractChartView
import ir.mahdiparastesh.sexbook.Settings
import ir.mahdiparastesh.sexbook.databinding.AdorabilityBinding

class Adorability : ChartActivity<AdorabilityBinding>() {
    override val b by lazy { AdorabilityBinding.inflate(layoutInflater) }
    override val chartView: AbstractChartView get() = b.main

    override suspend fun draw(): AbstractChartData {
        val hideUnsafe =
            sp.getBoolean(Settings.spHideUnsafePeople, true) && m.unsafe.isNotEmpty()

        val stb = sinceTheBeginning(this)
        val stars = ArrayList<Star>()
        for (x in m.summary!!.scores) {
            if (hideUnsafe && x.key in m.unsafe) continue

            val scores = ArrayList<Star.Frame>()
            for (month in stb)
                scores.add(Star.Frame(calcHistory(this, x.value, month), month))
            stars.add(Star(x.key, scores.toTypedArray()))
        }
        // empty columns should not be removed or all the points will jump to the beginning!
        stars.sortWith(Star.Sort(1))
        stars.sortWith(Star.Sort())
        return LineChartData().setLines(LineFactory(this@Adorability, stars))
    }

    override suspend fun render(data: AbstractChartData) {
        b.main.lineChartData = data as LineChartData
        b.main.isViewportCalculationEnabled = false // never do it before setLineChatData
        // it cannot be zoomed out.
    }
}
