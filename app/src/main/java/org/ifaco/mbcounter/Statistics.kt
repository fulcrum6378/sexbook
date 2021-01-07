package org.ifaco.mbcounter

import android.content.res.Configuration
import android.graphics.Typeface
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.anychart.AnyChart
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.anychart.enums.Anchor
import com.anychart.enums.HoverMode
import com.anychart.enums.Position
import com.anychart.enums.TooltipPositionMode
import org.ifaco.mbcounter.Fun.Companion.c
import org.ifaco.mbcounter.Fun.Companion.detectNight
import org.ifaco.mbcounter.Fun.Companion.night
import org.ifaco.mbcounter.Main.Companion.allMasturbation
import org.ifaco.mbcounter.Main.Companion.selectedCrush
import org.ifaco.mbcounter.Main.Companion.sumResult
import org.ifaco.mbcounter.databinding.StatisticsBinding
import java.util.*
import kotlin.collections.ArrayList

@Suppress("UNCHECKED_CAST")
class Statistics : AppCompatActivity() {
    lateinit var b: StatisticsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = StatisticsBinding.inflate(layoutInflater)
        setContentView(b.root)
        Fun.init(this)

        detectNight(resources.configuration)?.let { setNight(it) }


        if (allMasturbation == null || sumResult == null) onBackPressed()
        selectedCrush?.let { crush ->
            val data: MutableList<DataEntry> = ArrayList()
            val history = sumResult?.scores!![crush]
            listOf(
                "May 2020", "June 2020", "July 2020", "August 2020", "September 2020",
                "October 2020", "November 2020", "December 2020", "January 2021"
            ).forEach { data.add(ValueDataEntry(it, calcHistory(history!!, it))) }

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
                title(selectedCrush)
                yScale().minimum(0.0)
                yAxis(0).labels().format("{%Value}{groupsSeparator: }")
                tooltip().positionMode(TooltipPositionMode.POINT)
                interactivity().hoverMode(HoverMode.BY_X)
                //xAxis(0).title("Month")
                //yAxis(0).title("Masturbation")
                b.main.setChart(this)
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Fun.dm = resources.displayMetrics
        detectNight(newConfig)?.let { setNight(it) }
        Main.dateFont = Typeface.createFromAsset(assets, "franklin_gothic.ttf")
    }


    fun calcHistory(list: ArrayList<Summary.Erection>, month: String): Float {
        var value = 0f
        val months = c.resources.getStringArray(R.array.calendarFull)
        val split = month.split(" ")
        for (i in list) {
            var lm = Calendar.getInstance()
            lm.timeInMillis = i.time
            if (months.indexOf(split[0]) == lm.get(Calendar.MONTH)
                && split[1].toInt() == lm.get(Calendar.YEAR)
            ) value += i.value
        }
        return value
    }

    fun setNight(n: Boolean = true) {
        night = n
        /*window.decorView.setBackgroundColor(
            ContextCompat.getColor(c, if (!n) R.color.body else R.color.bodyN)
        )*/
    }
}