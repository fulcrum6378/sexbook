package com.mahdiparastesh.mbcounter

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.anychart.AnyChart
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.anychart.data.Set
import com.anychart.enums.Anchor
import com.anychart.enums.MarkerType
import com.anychart.enums.TooltipPositionMode
import com.anychart.graphics.vector.Stroke
import com.mahdiparastesh.mbcounter.databinding.PopularityBinding


class Popularity : AppCompatActivity() {
    private lateinit var b: PopularityBinding
    private lateinit var m: Model

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = PopularityBinding.inflate(layoutInflater)
        m = ViewModelProvider(this, Model.Factory()).get("Model", Model::class.java)
        setContentView(b.root)
        Fun.init(this)

        if (m.onani.value == null || m.summary.value == null) {
            onBackPressed(); return; }

        val cartesian = AnyChart.line().apply {
            animation(true)
            padding(10.0, 20.0, 5.0, 20.0)
            crosshair().enabled(true)
            crosshair()
                .yLabel(true) // DO: ystroke
                .yStroke(null as Stroke?, null, null, null as String?, null as String?)
            tooltip().positionMode(TooltipPositionMode.POINT)
            title("Trend of Sales of the Most Popular Products of ACME Corp.")
            yAxis(0).title("Number of Bottles Sold (thousands)")
            xAxis(0).labels().padding(5.0, 5.0, 5.0, 5.0)
        }

        val data = ArrayList<DataEntry>()
        Statistics.sinceTheBeginning(m.onani.value!!)
            //.forEach { data.add(Frame(it, )) }
        /*seriesData.add(Frame("1986", 3.6, 2.3, 2.8))
        seriesData.add(Frame("1987", 7.1, 4.0, 4.1))
        seriesData.add(Frame("1988", 8.5, 6.2, 5.1))
        seriesData.add(Frame("1989", 9.2, 11.8, 6.5))
        seriesData.add(Frame("1990", 10.1, 13.0, 12.5))
        seriesData.add(Frame("1991", 11.6, 13.9, 18.0))
        seriesData.add(Frame("1992", 16.4, 18.0, 21.0))
        seriesData.add(Frame("1993", 18.0, 23.3, 20.3))
        seriesData.add(Frame("1994", 13.2, 24.7, 19.2))
        seriesData.add(Frame("1995", 12.0, 18.0, 14.4))
        seriesData.add(Frame("1996", 3.2, 15.1, 9.2))
        seriesData.add(Frame("1997", 4.1, 11.3, 5.9))
        seriesData.add(Frame("1998", 6.3, 14.2, 5.2))
        seriesData.add(Frame("1999", 9.4, 13.7, 4.7))
        seriesData.add(Frame("2000", 11.5, 9.9, 4.2))
        seriesData.add(Frame("2001", 13.5, 12.1, 1.2))
        seriesData.add(Frame("2002", 14.8, 13.5, 5.4))
        seriesData.add(Frame("2003", 16.6, 15.1, 6.3))
        seriesData.add(Frame("2004", 18.1, 17.9, 8.9))
        seriesData.add(Frame("2005", 17.0, 18.9, 10.1))
        seriesData.add(Frame("2006", 16.6, 20.3, 11.5))
        seriesData.add(Frame("2007", 14.1, 20.7, 12.2))
        seriesData.add(Frame("2008", 15.7, 21.6, 10))
        seriesData.add(Frame("2009", 12.0, 22.5, 8.9))*/

        val set = Set.instantiate()
        set.data(data)
        val series1Mapping = set.mapAs("{ x: 'x', value: 'value' }")
        val series2Mapping = set.mapAs("{ x: 'x', value: 'value2' }")
        val series3Mapping = set.mapAs("{ x: 'x', value: 'value3' }")

        val series1 = cartesian.line(series1Mapping)
        series1.name("Brandy")
        series1.hovered().markers().enabled(true)
        series1.hovered().markers()
            .type(MarkerType.CIRCLE)
            .size(4.0)
        series1.tooltip()
            .position("right")
            .anchor(Anchor.LEFT_CENTER)
            .offsetX(5.0)
            .offsetY(5.0)

        val series2 = cartesian.line(series2Mapping)
        series2.name("Whiskey")
        series2.hovered().markers().enabled(true)
        series2.hovered().markers()
            .type(MarkerType.CIRCLE)
            .size(4.0)
        series2.tooltip()
            .position("right")
            .anchor(Anchor.LEFT_CENTER)
            .offsetX(5.0)
            .offsetY(5.0)

        val series3 = cartesian.line(series3Mapping)
        series3.name("Tequila")
        series3.hovered().markers().enabled(true)
        series3.hovered().markers()
            .type(MarkerType.CIRCLE)
            .size(4.0)
        series3.tooltip()
            .position("right")
            .anchor(Anchor.LEFT_CENTER)
            .offsetX(5.0)
            .offsetY(5.0)

        cartesian.legend().enabled(true)
        cartesian.legend().fontSize(13.0)
        cartesian.legend().padding(0.0, 0.0, 10.0, 0.0)

        b.main.setChart(cartesian)
    }

    private class Frame constructor(x: String, values: List<Number>) :
        ValueDataEntry(x, values[0]) {
        init {
            for (i in 1 until values.size + 1) setValue("value$i", values[i])
        }
    }
}
