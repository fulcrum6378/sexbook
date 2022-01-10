package ir.mahdiparastesh.sexbook

import android.annotation.SuppressLint
import android.os.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import ir.mahdiparastesh.sexbook.data.Filter
import ir.mahdiparastesh.sexbook.data.Report
import ir.mahdiparastesh.sexbook.data.Work
import ir.mahdiparastesh.sexbook.databinding.PageSexBinding
import ir.mahdiparastesh.sexbook.list.ReportAdap
import ir.mahdiparastesh.sexbook.more.Jalali
import ir.mahdiparastesh.sexbook.more.MessageInbox
import ir.mahdiparastesh.sexbook.more.SpinnerAdap
import java.util.*
import kotlin.collections.ArrayList

class PageSex(val c: Main) : Fragment() {
    private lateinit var b: PageSexBinding

    companion object {
        var handler = MutableLiveData<Handler?>(null)
        var filters: ArrayList<Filter>? = null
        var listFilter = 0
        val messages = MessageInbox(handler)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(inf: LayoutInflater, parent: ViewGroup?, state: Bundle?): View {
        b = PageSexBinding.inflate(layoutInflater, parent, false)

        // Handler
        handler.value = object : Handler(Looper.getMainLooper()) {
            @Suppress("UNCHECKED_CAST")
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    Work.VIEW_ALL -> if (msg.obj != null) {
                        c.m.onani.value = msg.obj as ArrayList<Report>
                        resetAllMasturbations()
                        c.load()
                    }
                    Work.VIEW_ONE -> if (msg.obj != null) when (msg.arg1) {
                        Work.ADD_NEW_ITEM -> {
                            if (c.m.onani.value == null) c.m.onani.value = ArrayList()
                            c.m.onani.value!!.add(msg.obj as Report)
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
                    Work.UPDATE_ONE -> if (c.m.onani.value != null) {
                        if (c.m.visOnani.value!!.contains(c.m.onani.value!![msg.arg1])) {
                            val nominalPos = c.m.visOnani.value!!
                                .indexOf(c.m.onani.value!![msg.arg1])
                            c.m.visOnani.value!![nominalPos] = c.m.onani.value!![msg.arg1]
                            if (msg.arg2 == 0) b.rv.adapter?.notifyItemChanged(nominalPos)
                        }
                        if (msg.arg2 == 0) resetAllMasturbations()

                    }
                    Work.DELETE_ONE -> if (c.m.onani.value != null && c.m.visOnani.value!!
                            .contains(c.m.onani.value!![msg.arg1])
                    ) {
                        val nominalPos = c.m.visOnani.value!!
                            .indexOf(c.m.onani.value!![msg.arg1])
                        c.m.visOnani.value!!.remove(c.m.onani.value!![msg.arg1])
                        c.m.onani.value!!.remove(c.m.onani.value!![msg.arg1])
                        filters?.let {
                            if (it.size > listFilter) it[listFilter].items =
                                it[listFilter].items.apply { remove(msg.arg1) }
                        }
                        if (c.m.visOnani.value!!.size > 0) {
                            b.rv.adapter?.notifyItemRemoved(nominalPos)
                            b.rv.adapter?.notifyItemRangeChanged(nominalPos, b.rv.adapter!!.itemCount)
                        } else resetAllMasturbations()
                    } else resetAllMasturbations()
                    Work.SCROLL -> b.rv.smoothScrollBy(0, msg.obj as Int)
                    Work.SPECIAL_ADD -> add()
                }
            }
        }
        messages.clear()

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
        b.add.setOnClickListener { add() }

        Work(Work.VIEW_ALL).start()
        return b.root
    }

    var spnFilterTouched = false
    var filteredOnce = false
    fun resetAllMasturbations() {
        Collections.sort(c.m.onani.value!!, ReportAdap.Companion.Sort())
        filters = filter(c.m.onani.value!!)
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

    @SuppressLint("NotifyDataSetChanged")
    fun filterList(i: Int = listFilter) {
        if (c.m.onani.value == null) {
            c.m.visOnani.value!!.clear(); return; }
        listFilter = i
        c.m.visOnani.value!!.clear()
        if (filters == null) for (o in c.m.onani.value!!) c.m.visOnani.value!!.add(o)
        else if (!filters.isNullOrEmpty())
            for (o in filters!![listFilter].items)
                if (c.m.onani.value!!.size > o)
                    c.m.visOnani.value!!.add(c.m.onani.value!![o])
        if (b.rv.adapter == null) {
            Main.summarize(c.m)
            b.rv.adapter = ReportAdap(c)
        } else b.rv.adapter!!.notifyDataSetChanged()
    }

    private var adding = false
    fun add() {
        if (adding) return
        if (filters != null) filterList(filters!!.size - 1)
        adding = true
        Work(Work.INSERT_ONE, listOf(Report(Fun.now(), "", 1, "", -1L))).start()
        object : CountDownTimer(Work.TIMEOUT, Work.TIMEOUT) {
            override fun onTick(p0: Long) {}
            override fun onFinish() {
                adding = false; }
        }.start()
        Fun.shake(c)
    }
}
