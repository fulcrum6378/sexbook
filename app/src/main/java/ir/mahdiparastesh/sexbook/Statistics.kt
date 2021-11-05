package ir.mahdiparastesh.sexbook

import android.graphics.Typeface
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.anychart.AnyChart
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.anychart.enums.Anchor
import com.anychart.enums.HoverMode
import com.anychart.enums.Position
import com.anychart.enums.TooltipPositionMode
import ir.mahdiparastesh.sexbook.Fun.Companion.c
import ir.mahdiparastesh.sexbook.data.Report
import ir.mahdiparastesh.sexbook.databinding.StatisticsBinding
import ir.mahdiparastesh.sexbook.more.Jalali
import java.util.*
import kotlin.collections.ArrayList

@Suppress("UNCHECKED_CAST")
class Statistics : AppCompatActivity() {
    private lateinit var b: StatisticsBinding
    private lateinit var m: Model

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = StatisticsBinding.inflate(layoutInflater)
        m = ViewModelProvider(this, Model.Factory()).get("Model", Model::class.java)
        setContentView(b.root)
        Fun.init(this)

        if (m.onani.value == null || m.summary.value == null || m.crush.value == null) {
            onBackPressed(); return; }
        val data: MutableList<DataEntry> = ArrayList()
        val history = m.summary.value!!.scores[m.crush.value]
        sinceTheBeginning(m.onani.value!!)
            .forEach { data.add(ValueDataEntry(it, calcHistory(history!!, it))) }

        AnyChart.column().apply {
            column(data).fill("#FFD422").stroke("#FFD422")
                .tooltip()
                .titleFormat("{%X}")
                .position(Position.CENTER_BOTTOM)
                .anchor(Anchor.CENTER_BOTTOM)
                .offsetX(0.0)
                .offsetY(5.0)
                .format("{%Value}{groupsSeparator: }")
            animation(true)
            title(m.crush.value)
            yScale().minimum(0.0)
            yAxis(0).labels().format("{%Value}{groupsSeparator: }")
            tooltip().positionMode(TooltipPositionMode.POINT)
            interactivity().hoverMode(HoverMode.BY_X)
            //xAxis(0).title("Month")
            //yAxis(0).title("Masturbation")
            background(if (Fun.night) "#3A3A3A" else "#FFFFFF")
            b.main.setChart(this)
        }
        Main.dateFont = Typeface.createFromAsset(assets, "franklin_gothic.ttf")
    }

    companion object {
        fun sinceTheBeginning(mOnani: ArrayList<Report>): List<String> {
            val now = Calendar.getInstance()
            var oldest = now.timeInMillis
            for (h in mOnani) if (h.time < oldest) oldest = h.time
            val beg = Calendar.getInstance().apply { timeInMillis = oldest }
            val list = arrayListOf<String>()
            if (!c.resources.getBoolean(R.bool.jalali)) {
                val yDist = now[Calendar.YEAR] - beg[Calendar.YEAR]
                for (y in 0 until (yDist + 1)) {
                    var start = 0
                    var end = 11
                    if (y == 0) start = beg[Calendar.MONTH]
                    if (y == yDist) end = now[Calendar.MONTH]
                    for (m in start..end) list.add(
                        "${c.resources.getStringArray(R.array.months)[m]} ${beg[Calendar.YEAR] + y}"
                    )
                }
                return list.toList()
            } else {
                val jBeg = Jalali(beg)
                val jNow = Jalali(now)
                val yDist = jNow.Y - jBeg.Y
                for (y in 0 until (yDist + 1)) {
                    var start = 0
                    var end = 11
                    if (y == 0) start = jBeg.M
                    if (y == yDist) end = jNow.M
                    for (m in start..end) list.add(
                        "${c.resources.getStringArray(R.array.jMonths)[m]} ${jBeg.Y + y}"
                    )
                }
                return list.toList()
            }
        }

        fun calcHistory(list: ArrayList<Summary.Erection>, month: String): Float {
            var value = 0f
            val split = month.split(" ")
            if (!c.resources.getBoolean(R.bool.jalali)) {
                val months = c.resources.getStringArray(R.array.months)
                for (i in list) {
                    var lm = Calendar.getInstance().apply { timeInMillis = i.time }
                    if (months.indexOf(split[0]) == lm.get(Calendar.MONTH)
                        && split[1].toInt() == lm.get(Calendar.YEAR)
                    ) value += i.value
                }
            } else {
                val months = c.resources.getStringArray(R.array.jMonths)
                for (i in list) {
                    var lm = Jalali(Calendar.getInstance().apply { timeInMillis = i.time })
                    if (months.indexOf(split[0]) == lm.M && split[1].toInt() == lm.Y)
                        value += i.value
                }
            }
            return value
        }
    }
}
