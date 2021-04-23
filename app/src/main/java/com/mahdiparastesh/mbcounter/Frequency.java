package com.mahdiparastesh.mbcounter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import com.anychart.AnyChart;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.core.cartesian.series.Line;
import com.anychart.data.Mapping;
import com.anychart.data.Set;
import com.anychart.enums.Anchor;
import com.anychart.enums.MarkerType;
import com.anychart.enums.TooltipPositionMode;
import com.anychart.graphics.vector.Stroke;
import com.mahdiparastesh.mbcounter.data.Report;
import com.mahdiparastesh.mbcounter.databinding.FrequencyBinding;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Frequency extends AppCompatActivity {
    FrequencyBinding b;
    Model m;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = FrequencyBinding.inflate(getLayoutInflater());
        m = new ViewModelProvider(this, new Model.Factory()).get("Model", Model.class);
        setContentView(b.getRoot());
        Fun.Companion.init(this);


        if (m.getOnani().getValue() == null) {
            onBackPressed();
            return;
        }

        Cartesian cartesian = AnyChart.line();
        cartesian.animation(true);
        cartesian.padding(10d, 20d, 5d, 20d);
        /*cartesian.crosshair().enabled(true);
        cartesian.crosshair()
                .yLabel(true)
                .yStroke((Stroke) null, null, null, (String) null, (String) null);*/
        //cartesian.tooltip().positionMode(TooltipPositionMode.POINT);
        //cartesian.title("Trend of Sales of the Most Popular Products of ACME Corp.");

        List<DataEntry> seriesData = new ArrayList<>();
        int x = 1;
        for (Report r : m.getOnani().getValue()) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(r.time);
            seriesData.add(new ValueDataEntry(x + ". ", cal.get(Calendar.HOUR_OF_DAY)));
            x++;
        }

        Set set = Set.instantiate();
        set.data(seriesData);
        Mapping series1Mapping = set.mapAs("{ x: 'x', value: 'value' }");

        Line series1 = cartesian.line(series1Mapping);
        series1.name("Brandy");
        series1.hovered().markers().enabled(true);
        series1.hovered().markers()
                .type(MarkerType.CIRCLE)
                .size(4d);
        series1.tooltip()
                .position("right")
                .anchor(Anchor.LEFT_CENTER)
                .offsetX(5d)
                .offsetY(5d);
        cartesian.legend().enabled(true);
        cartesian.legend().fontSize(13d);
        cartesian.legend().padding(0d, 0d, 10d, 0d);

        b.main.setChart(cartesian);
    }
}
