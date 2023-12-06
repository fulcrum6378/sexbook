package ir.mahdiparastesh.sexbook.stat

import android.icu.util.Calendar
import android.os.Bundle
import androidx.annotation.MainThread
import androidx.core.view.isInvisible
import androidx.viewbinding.ViewBinding
import ir.mahdiparastesh.hellocharts.model.AbstractChartData
import ir.mahdiparastesh.hellocharts.model.Column
import ir.mahdiparastesh.hellocharts.model.Line
import ir.mahdiparastesh.hellocharts.model.PointValue
import ir.mahdiparastesh.hellocharts.model.SubColumnValue
import ir.mahdiparastesh.hellocharts.view.AbstractChartView
import ir.mahdiparastesh.mcdtp.McdtpUtils
import ir.mahdiparastesh.sexbook.Fun.calendar
import ir.mahdiparastesh.sexbook.Fun.randomColor
import ir.mahdiparastesh.sexbook.Settings
import ir.mahdiparastesh.sexbook.data.Report
import ir.mahdiparastesh.sexbook.more.BaseActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class ChartActivity<L> : BaseActivity() where L : ViewBinding {
    protected abstract val b: L
    private var job: Job? = null
    abstract val chartView: AbstractChartView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(b.root)
        if (night())
            window.decorView.setBackgroundColor(themeColor(com.google.android.material.R.attr.colorPrimary))
        if (!requirements()) {
            onBackPressed(); return; }

        job = CoroutineScope(Dispatchers.IO).launch {
            val drawn = draw()
            withContext(Dispatchers.Main) {
                render(drawn)
                chartView.isInvisible = false
            }
            job = null
        }
    }

    open fun requirements() = m.onani != null && m.summary != null

    abstract suspend fun draw(): AbstractChartData

    @MainThread
    abstract suspend fun render(data: AbstractChartData)

    @Suppress("OVERRIDE_DEPRECATION")
    override fun onBackPressed() {
        job?.cancel()
        @Suppress("DEPRECATION") super.onBackPressed()
    }

    fun sinceTheBeginning(c: BaseActivity, mOnani: ArrayList<Report>): List<String> {
        // Find the ending
        var end = c.calType().getDeclaredConstructor().newInstance()
        var oldest = end.timeInMillis
        if (c.sp.getBoolean(Settings.spStatUntilCb, false)) {
            val statTill = c.sp.getLong(Settings.spStatUntil, Long.MAX_VALUE)
            var newest = 0L
            for (h in mOnani) if (h.time in (newest + 1)..statTill) newest = h.time
            if (newest != 0L) end = newest.calendar(c)
        }

        // Find the beginning
        if (c.sp.getBoolean(Settings.spStatSinceCb, false)) {
            val statSinc = c.sp.getLong(Settings.spStatSince, Long.MIN_VALUE)
            for (h in mOnani) if (h.time in statSinc until oldest) oldest = h.time
        } else for (h in mOnani) if (h.time < oldest) oldest = h.time

        val beg = oldest.calendar(c)
        val list = arrayListOf<String>()
        val yDist = end[Calendar.YEAR] - beg[Calendar.YEAR]
        for (y in 0 until (yDist + 1)) {
            var start = 0
            var finish = 11
            if (y == 0) start = beg[Calendar.MONTH]
            if (y == yDist) finish = end[Calendar.MONTH]
            for (m in start..finish) list.add(
                "${McdtpUtils.localSymbols(c.c, c.calType()).shortMonths[m]} " +
                        "${beg[Calendar.YEAR] + y}"
            )
        }
        return list
    }

    fun calcHistory(
        c: BaseActivity, list: ArrayList<Summary.Orgasm>, month: String,
        growing: Boolean = false
    ): Float {
        var value = 0f
        val split = month.split(" ")
        val months = McdtpUtils.localSymbols(c, c.calType()).shortMonths
        for (i in list) {
            val lm = i.time.calendar(c)
            val yea = lm[Calendar.YEAR]
            val mon = lm[Calendar.MONTH]
            if (months.indexOf(split[0]) == mon && split[1].toInt() == yea) value += i.value
            if (growing && (split[1].toInt() > yea ||
                        (split[1].toInt() == yea && months.indexOf(split[0]) > mon))
            ) value += i.value
        }
        return value
    }

    class ColumnFactory(c: BaseActivity, list: ArrayList<Pair<String, Float>>) :
        ArrayList<Column>(list.map {
            Column(
                listOf(
                    SubColumnValue(it.second)
                        .setLabel("${it.first} (${it.second})")
                        .setColor(
                            if (!c.night()) c.themeColor(com.google.android.material.R.attr.colorPrimary)
                            else c.color(ir.mahdiparastesh.sexbook.R.color.CPV_LIGHT)
                        )
                )
            ).setHasLabels(true)
        })

    class LineFactory(c: BaseActivity, stars: List<Star>) : ArrayList<Line>(stars.map {
        Line(it.frames.mapIndexed { i, frame ->
            PointValue(i.toFloat(), frame.score)
                .setLabel("${it.name} : ${frame.month} (${frame.score})")
        })
            .setColor(c.randomColor())
            .setCubic(true)
            .setHasLabelsOnlyForSelected(true)
    })

    class Star(val name: String, val frames: Array<Frame>) {
        class Sort(private val by: Int = 0) : Comparator<Star> {
            override fun compare(a: Star, b: Star) = when (by) {
                1 -> a.name.compareTo(b.name)
                else -> b.frames.sumOf { (it.score * 100f).toInt() } -
                        a.frames.sumOf { (it.score * 100f).toInt() }
            }
        }

        data class Frame(val score: Float, val month: String)
    }
}