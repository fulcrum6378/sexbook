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
    private var crushKey: String? = null
    private var crush: Crush? = null
    private var history: ArrayList<Summary.Orgasm>? = null

    companion object {
        const val EXTRA_CRUSH_KEY = "crush_key"
        var handler: Handler? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (night()) b.identifyIV.colorFilter = themePdcf()

        handler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    Work.C_VIEW_ONE -> crush = msg.obj as Crush?
                    Work.C_INSERT_ONE, Work.C_UPDATE_ONE, Work.C_DELETE_ONE -> {
                        PageLove.changed = true
                        Work(c, Work.C_VIEW_ONE, listOf(crushKey!!), handler).start()
                    }
                }
            }
        }
        crush = m.liefde.value?.find { it.key == crushKey }
        b.identify.setOnClickListener {
            Identify(crush, handler).apply {
                arguments = Bundle().apply { putString(Identify.BUNDLE_CRUSH_KEY, crushKey) }
                show(supportFragmentManager, Identify.TAG)
            }
        }
    }

    override fun requirements(): Boolean {
        crushKey = intent.getStringExtra(EXTRA_CRUSH_KEY)
        history = m.summary!!.scores[crushKey]
        return super.requirements() && crushKey != null && history != null
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
