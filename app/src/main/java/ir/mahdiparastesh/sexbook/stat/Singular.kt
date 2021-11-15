package ir.mahdiparastesh.sexbook.stat

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.lifecycle.ViewModelProvider
import com.anychart.AnyChart
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.anychart.enums.Anchor
import com.anychart.enums.HoverMode
import com.anychart.enums.Position
import com.anychart.enums.TooltipPositionMode
import com.google.android.material.switchmaterial.SwitchMaterial
import ir.mahdiparastesh.sexbook.Fun
import ir.mahdiparastesh.sexbook.Fun.Companion.c
import ir.mahdiparastesh.sexbook.Model
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.data.Crush
import ir.mahdiparastesh.sexbook.data.Report
import ir.mahdiparastesh.sexbook.data.Work
import ir.mahdiparastesh.sexbook.databinding.SingularBinding
import ir.mahdiparastesh.sexbook.more.Jalali
import java.util.*
import kotlin.collections.ArrayList

@Suppress("UNCHECKED_CAST")
class Singular : AppCompatActivity() {
    private lateinit var b: SingularBinding
    private lateinit var m: Model

    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = SingularBinding.inflate(layoutInflater)
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
            background(if (Fun.night) "#3A3A3A" else "#FFFFFF")
            b.main.setChart(this)
        }

        // Night Mode
        if (Fun.night) {
            window.decorView.setBackgroundColor(Fun.color(R.color.CP))
            b.identifyIV.colorFilter = Fun.pdcf(R.color.CP)
        }

        // Identification
        b.identify.setOnClickListener {
            val layout = layoutInflater.inflate(R.layout.identify, null, false) as ScrollView
            val ll = layout[0] as LinearLayout
            val fName = ll[0] as EditText
            val lName = ll[1] as EditText
            val masc = ll[2] as SwitchMaterial
            val height = ll[3] as EditText
            val instagram = ll[4] as EditText
            AlertDialog.Builder(this).apply {
                setTitle("${resources.getString(R.string.identify)}: ${m.crush.value}")
                setView(layout)
                setPositiveButton(R.string.ok) { _, _ ->
                    val inserted = Crush(
                        m.crush.value!!,
                        fName.text.toString(),
                        lName.text.toString(),
                        masc.isChecked,
                        height.text.toString().toShort(),
                        instagram.text.toString()
                    )
                    Work(Work.C_INSERT_ONE, listOf(inserted)).start()
                }
                setNegativeButton(R.string.cancel, null)
                setCancelable(true)
            }.create().apply {
                show()
                Fun.fixADButton(getButton(AlertDialog.BUTTON_POSITIVE))
                Fun.fixADButton(getButton(AlertDialog.BUTTON_NEGATIVE))
            }
        }
    }

    companion object {
        fun sinceTheBeginning(mOnani: ArrayList<Report>): List<String> {
            val now = Calendar.getInstance()
            var oldest = now.timeInMillis
            for (h in mOnani) if (h.time < oldest) oldest = h.time
            val beg = Calendar.getInstance().apply { timeInMillis = oldest }
            val list = arrayListOf<String>()
            if (Fun.calType() != Fun.CalendarType.JALALI) {
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
            }
            return list.toList()
        }

        fun calcHistory(list: ArrayList<Summary.Erection>, month: String): Float {
            var value = 0f
            val split = month.split(" ")
            val months = c.resources.getStringArray(
                when (Fun.calType()) {
                    Fun.CalendarType.JALALI -> R.array.jMonths
                    else -> R.array.months
                }
            )
            for (i in list) {
                var lm = Calendar.getInstance().apply { timeInMillis = i.time }
                if (Fun.calType() == Fun.CalendarType.JALALI) {
                    val jal = Jalali(lm)
                    if (months.indexOf(split[0]) == jal.M && split[1].toInt() == jal.Y)
                        value += i.value
                    continue
                }
                if (months.indexOf(split[0]) == lm[Calendar.MONTH]
                    && split[1].toInt() == lm[Calendar.YEAR]
                ) value += i.value
            }
            return value
        }
    }
}
