package ir.mahdiparastesh.sexbook.stat

import android.os.Bundle
import ir.mahdiparastesh.hellocharts.model.ColumnChartData
import ir.mahdiparastesh.sexbook.Fun
import ir.mahdiparastesh.sexbook.databinding.MixtureBinding
import ir.mahdiparastesh.sexbook.more.BaseActivity

class Mixture : BaseActivity() {
    private lateinit var b: MixtureBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = MixtureBinding.inflate(layoutInflater)
        setContentView(b.root)

        if (m.onani.value == null) {
            @Suppress("DEPRECATION") onBackPressed(); return; }
        val data = ArrayList<Pair<String, Float>>()
        val history = arrayListOf<Summary.Erection>()
        val allowedTypes = Fun.allowedSexTypes(sp)
        for (o in m.onani.value!!.let {
            if (allowedTypes.size < Fun.sexTypesCount) it.filter { r -> r.type in allowedTypes } else it
        }) history.add(Summary.Erection(o.time, 1f))
        Singular.sinceTheBeginning(this, m.onani.value!!)
            .forEach { data.add(Pair(it, Singular.calcHistory(this, history, it))) }
        b.main.columnChartData = ColumnChartData().setColumns(Singular.ColumnFactory(this, data))
    }
}
