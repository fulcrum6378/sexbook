package ir.mahdiparastesh.sexbook.stat

import android.os.Bundle
import ir.mahdiparastesh.hellocharts.model.AbstractChartData
import ir.mahdiparastesh.hellocharts.model.ColumnChartData
import ir.mahdiparastesh.hellocharts.view.AbstractChartView
import ir.mahdiparastesh.sexbook.data.Crush
import ir.mahdiparastesh.sexbook.data.Identify
import ir.mahdiparastesh.sexbook.databinding.SingularBinding

class Singular : ChartActivity<SingularBinding>() {
    override val b by lazy { SingularBinding.inflate(layoutInflater) }
    override val chartView: AbstractChartView get() = b.main
    private var crushKey: String? = null
    var crush: Crush? = null
    private var history: ArrayList<Summary.Orgasm>? = null

    companion object {
        const val EXTRA_CRUSH_KEY = "crush_key"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (night()) b.identifyIV.colorFilter = themePdcf()

        crush = m.people?.find { it.key == crushKey }
        b.title.text = crushKey
        b.identify.setOnClickListener {
            Identify(this@Singular, crush).apply {
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
        sinceTheBeginning(this, m.onani!!)
            .forEach { data.add(Pair(it, calcHistory(this, history!!, it))) }
        return ColumnChartData().setColumns(ColumnFactory(this, data))
    }

    override suspend fun render(data: AbstractChartData) {
        b.main.columnChartData = data as ColumnChartData
    }
}
