package ir.mahdiparastesh.sexbook.stat

import android.os.Bundle
import androidx.annotation.MainThread
import androidx.core.view.isInvisible
import androidx.viewbinding.ViewBinding
import ir.mahdiparastesh.hellocharts.model.AbstractChartData
import ir.mahdiparastesh.hellocharts.model.Line
import ir.mahdiparastesh.hellocharts.model.PointValue
import ir.mahdiparastesh.hellocharts.view.AbstractChartView
import ir.mahdiparastesh.sexbook.Fun.randomColor
import ir.mahdiparastesh.sexbook.more.BaseActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Suppress("DEPRECATION")
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

    open fun requirements() = m.onani.value != null && m.summary != null

    abstract suspend fun draw(): AbstractChartData

    @MainThread
    abstract suspend fun render(data: AbstractChartData)

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        job?.cancel()
        super.onBackPressed()
    }

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

    class LineFactory(c: BaseActivity, stars: List<Star>) : ArrayList<Line>(stars.map {
        Line(it.frames.mapIndexed { i, frame ->
            PointValue(i.toFloat(), frame.score)
                .setLabel("${it.name} : ${frame.month} (${frame.score})")
        })
            .setColor(c.randomColor())
            .setCubic(true)
            .setHasLabelsOnlyForSelected(true)
    })
}