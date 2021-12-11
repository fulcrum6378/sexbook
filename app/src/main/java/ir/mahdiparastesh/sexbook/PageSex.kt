package ir.mahdiparastesh.sexbook

import android.annotation.SuppressLint
import android.os.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import ir.mahdiparastesh.sexbook.Fun.Companion.c
import ir.mahdiparastesh.sexbook.data.Filter
import ir.mahdiparastesh.sexbook.data.Report
import ir.mahdiparastesh.sexbook.data.Work
import ir.mahdiparastesh.sexbook.databinding.PageSexBinding
import ir.mahdiparastesh.sexbook.list.ReportAdap
import ir.mahdiparastesh.sexbook.more.Jalali
import ir.mahdiparastesh.sexbook.more.SpinnerAdap
import java.util.*
import kotlin.collections.ArrayList

class PageSex(val that: Main) : Fragment() {
    private lateinit var b: PageSexBinding
    private lateinit var m: Model
    private var adding = false
    var adapter: ReportAdap? = null

    companion object {
        lateinit var handler: Handler
        var reports = ArrayList<Report>()
        var filters: ArrayList<Filter>? = null
        var listFilter = 0

        fun handling() = ::handler.isInitialized
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(inf: LayoutInflater, parent: ViewGroup?, state: Bundle?): View {
        b = PageSexBinding.inflate(layoutInflater, parent, false)
        m = ViewModelProvider(this, Model.Factory()).get("Model", Model::class.java)


        // Handler
        handler = object : Handler(Looper.getMainLooper()) {
            @Suppress("UNCHECKED_CAST")
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    Work.VIEW_ALL -> if (msg.obj != null) {
                        m.onani.value = msg.obj as ArrayList<Report>
                        resetAllMasturbations()
                        that.load()
                    }
                    Work.VIEW_ONE -> if (msg.obj != null) when (msg.arg1) {
                        Work.ADD_NEW_ITEM -> {
                            if (m.onani.value == null) m.onani.value = ArrayList()
                            m.onani.value!!.add(msg.obj as Report)
                            resetAllMasturbations()
                            adding = false
                            Fun.explode(b.add)
                        }
                    }
                    Work.INSERT_ONE -> if (msg.obj != null)
                        Work(Work.VIEW_ONE, listOf(msg.obj as Long, Work.ADD_NEW_ITEM)).start()
                    Work.REPLACE_ALL -> {
                        Toast.makeText(c, R.string.importDone, Toast.LENGTH_LONG).show()
                        Work(Work.VIEW_ALL).start()
                    }
                    Work.UPDATE_ONE -> {
                        if (m.onani.value != null) {
                            if (reports.contains(m.onani.value!![msg.arg1])) {
                                val nominalPos = reports.indexOf(m.onani.value!![msg.arg1])
                                reports[nominalPos] = m.onani.value!![msg.arg1]
                                if (msg.arg2 == 0) adapter?.notifyItemChanged(nominalPos)
                            }
                            if (msg.arg2 == 0) resetAllMasturbations()
                        }
                    }
                    Work.DELETE_ONE -> if (m.onani.value != null) {
                        if (reports.contains(m.onani.value!![msg.arg1])) {
                            val nominalPos = reports.indexOf(m.onani.value!![msg.arg1])
                            reports.remove(m.onani.value!![msg.arg1])
                            m.onani.value!!.remove(m.onani.value!![msg.arg1])
                            filters?.let {
                                if (it.size > listFilter) it[listFilter].items =
                                    it[listFilter].items.apply { remove(msg.arg1) }
                            }
                            adapter?.notifyItemRemoved(nominalPos)
                        }
                        resetAllMasturbations()
                    }
                    Work.SCROLL -> b.rv.smoothScrollBy(0, msg.obj as Int)
                }
            }
        }

        // Filter
        b.spnFilterMark.setColorFilter(Fun.color(R.color.spnFilterMark))
        b.spnFilter.setOnTouchListener { _, _ -> spnFilterTouched = true; false }
        b.spnFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(av: AdapterView<*>?) {}
            override fun onItemSelected(av: AdapterView<*>?, v: View?, i: Int, l: Long) {
                if (spnFilterTouched) filterList(i)
            }
        }

        // Add
        if (Fun.night) Fun.pdcf().apply { b.addIV.colorFilter = this }
        b.add.setOnClickListener {
            if (adding) return@setOnClickListener
            if (filters != null) filterList(filters!!.size - 1)
            adding = true
            Work(Work.INSERT_ONE, listOf(Report(Fun.now(), "", 1, "", -1L))).start()
            object : CountDownTimer(Work.TIMEOUT, Work.TIMEOUT) {
                override fun onTick(p0: Long) {}
                override fun onFinish() {
                    adding = false; }
            }
        }

        Work(Work.VIEW_ALL).start()
        return b.root
    }

    var spnFilterTouched = false
    var filteredOnce = false
    fun resetAllMasturbations() {
        Collections.sort(m.onani.value!!, ReportAdap.Companion.Sort())
        filters = filter(m.onani.value!!)
        val maxPage = filters!!.size - 1
        if (listFilter == -1) listFilter++
        filterList(
            if (filters!!.size > 0 && (!filteredOnce || listFilter > maxPage))
                maxPage else listFilter
        )
        if (filters != null) {
            val titles = ArrayList<String>().apply {
                for (f in filters!!.indices) add(filters!![f].title(c))
            }
            b.spnFilter.adapter = SpinnerAdap(titles)
            spnFilterTouched = false
            b.spnFilter.setSelection(listFilter, true)
        }
        filteredOnce = true
    }

    fun filter(reports: ArrayList<Report>): ArrayList<Filter> {
        val filters: ArrayList<Filter> = ArrayList()
        for (r in reports.indices) {
            val lm = Calendar.getInstance().apply { timeInMillis = reports[r].time }
            var ym = arrayOf(lm[Calendar.YEAR], lm[Calendar.MONTH])
            if (Fun.calType() == Fun.CalendarType.JALALI)
                Jalali(lm).apply { ym = arrayOf(Y, M) }
            var filterExists = false
            for (f in filters.indices) if (filters[f].year == ym[0] && filters[f].month == ym[1]) {
                filterExists = true
                filters[f] = filters[f].apply { put(r) }
            }
            if (!filterExists)
                filters.add(Filter(ym[0], ym[1], ArrayList<Int>().apply { add(r) }))
        }
        return filters
    }

    fun filterList(i: Int = listFilter) {
        if (m.onani.value == null) {
            reports.clear(); return; }
        listFilter = i
        reports.clear()
        if (filters == null) for (o in m.onani.value!!) reports.add(o)
        else if (!filters.isNullOrEmpty())
            for (o in filters!![listFilter].items)
                if (m.onani.value!!.size > o)
                    reports.add(m.onani.value!![o])
        if (adapter == null) {
            Main.summarize(m)
            adapter = ReportAdap(reports, that)
            b.rv.adapter = adapter
        } else adapter!!.notifyDataSetChanged()
    }
}
