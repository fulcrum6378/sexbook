package ir.mahdiparastesh.sexbook.stat

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import ir.mahdiparastesh.hellocharts.model.AbstractChartData
import ir.mahdiparastesh.hellocharts.model.ColumnChartData
import ir.mahdiparastesh.hellocharts.view.AbstractChartView
import ir.mahdiparastesh.sexbook.PageLove
import ir.mahdiparastesh.sexbook.data.Crush
import ir.mahdiparastesh.sexbook.data.Identify
import ir.mahdiparastesh.sexbook.data.Work
import ir.mahdiparastesh.sexbook.databinding.SingularBinding

class Singular : ChartActivity<SingularBinding>() {
    override val b by lazy { SingularBinding.inflate(layoutInflater) }
    override val chartView: AbstractChartView get() = b.main
    private var crush: Crush? = null
    private var history: ArrayList<Summary.Erection>? = null

    companion object {
        var handler: Handler? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Handler
        handler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    Work.C_VIEW_ONE -> crush = msg.obj as Crush?
                    Work.C_INSERT_ONE, Work.C_UPDATE_ONE, Work.C_DELETE_ONE -> {
                        PageLove.changed = true
                        Work(c, Work.C_VIEW_ONE, listOf(m.crush!!), handler).start()
                    }
                }
            }
        }

        // Styles
        if (night()) b.identifyIV.colorFilter = themePdcf()

        // Identification
        Work(c, Work.C_VIEW_ONE, listOf(m.crush!!), handler).start()
        b.identify.setOnClickListener { Identify(this, crush, handler) }
    }

    override fun requirements(): Boolean {
        history = m.summary!!.scores[m.crush]
        return super.requirements() && m.crush != null && history != null
    }

    override suspend fun draw(): AbstractChartData {
        val data = ArrayList<Pair<String, Float>>()
        sinceTheBeginning(this, m.onani.value!!)
            .forEach { data.add(Pair(it, calcHistory(this, history!!, it))) }
        return ColumnChartData().setColumns(ColumnFactory(this, data))
    }

    override suspend fun render(data: AbstractChartData) {
        b.main.columnChartData = data as ColumnChartData
    }

    override fun onDestroy() {
        super.onDestroy()
        handler = null
    }
}
