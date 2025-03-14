package ir.mahdiparastesh.sexbook.stat

import ir.mahdiparastesh.hellocharts.model.AbstractChartData
import ir.mahdiparastesh.hellocharts.model.LineChartData
import ir.mahdiparastesh.hellocharts.view.AbstractChartView
import ir.mahdiparastesh.sexbook.Settings
import ir.mahdiparastesh.sexbook.databinding.GrowthBinding

class Growth : ChartActivity<GrowthBinding>() {
    override val b by lazy { GrowthBinding.inflate(layoutInflater) }
    override val chartView: AbstractChartView get() = b.main

    override suspend fun draw(): AbstractChartData {
        val hideUnsafe =
            c.sp.getBoolean(Settings.spHideUnsafePeople, true) && c.unsafe.isNotEmpty()

        val stb = sinceTheBeginning(this)
        val stars = ArrayList<Star>()
        for (x in c.summary!!.scores) {
            if (hideUnsafe && x.key in c.unsafe) continue

            val frames = ArrayList<Star.Frame>()
            for (month in stb)
                frames.add(Star.Frame(calcHistory(this, x.value, month, true), month))
            stars.add(Star(x.key, frames.toTypedArray()))
        }
        stars.sortWith(Star.Sort(1))
        stars.sortWith(Star.Sort())
        return LineChartData().setLines(LineFactory(stars))
    }

    override suspend fun render(data: AbstractChartData) {
        b.main.setLabelOffset(dp(20))
        b.main.lineChartData = data as LineChartData
    }
}
