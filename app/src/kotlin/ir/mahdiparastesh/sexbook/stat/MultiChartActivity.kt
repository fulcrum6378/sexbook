package ir.mahdiparastesh.sexbook.stat

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.annotation.MainThread
import ir.mahdiparastesh.hellocharts.model.AbstractChartData
import ir.mahdiparastesh.hellocharts.model.LineChartData
import ir.mahdiparastesh.hellocharts.model.PieChartData
import ir.mahdiparastesh.hellocharts.view.AbstractChartView
import ir.mahdiparastesh.hellocharts.view.LineChartView
import ir.mahdiparastesh.hellocharts.view.PieChartView
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.base.BaseActivity
import kotlin.reflect.KClass

/** Subclass of [BaseActivity] that contains multiple [ChartType]s. */
abstract class MultiChartActivity : ChartActivity() {

    abstract var vmChartType: Int
    abstract val chartType: Spinner
    private var spnChartTypeTouched = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // chart types
        chartType.adapter = ArrayAdapter(
            c, R.layout.spinner_yellow, resources.getStringArray(R.array.tasteChartTypes)
        ).apply { setDropDownViewResource(R.layout.spinner_dd) }
        chartType.setSelection(vmChartType)
        chartType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                if (!spnChartTypeTouched) {
                    spnChartTypeTouched = true
                    return; }

                vmChartType = position
                createNewChart()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        createNewChart()
    }

    @MainThread
    abstract fun createNewChart()

    protected fun createChartView() = ChartType.entries[vmChartType].view.java
        .constructors.find { it.parameterCount == 1 }!!
        .newInstance(ContextThemeWrapper(c, R.style.statChart)) as AbstractChartView

    fun passDataToChartView(chartView: AbstractChartView, data: AbstractChartData) {
        when (vmChartType) {
            ChartType.COMPOSITIONAL.ordinal ->
                (chartView as PieChartView).pieChartData = data as PieChartData
            ChartType.TIME_SERIES.ordinal, ChartType.CUMULATIVE_TIME_SERIES.ordinal -> {
                chartView.setLabelOffset(dp(StatUtils.POINT_LABEL_OFFSET_IN_DP))
                (chartView as LineChartView).lineChartData = data as LineChartData
                chartView.isViewportCalculationEnabled = false // never do it before setLineChatData
                // it cannot be zoomed out.
            }
        }
    }


    enum class ChartType(val view: KClass<out AbstractChartView>) {
        COMPOSITIONAL(PieChartView::class),
        TIME_SERIES(LineChartView::class),
        CUMULATIVE_TIME_SERIES(LineChartView::class),
    }
}
