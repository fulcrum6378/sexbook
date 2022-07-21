package ir.mahdiparastesh.sexbook

import android.annotation.SuppressLint
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import ir.mahdiparastesh.sexbook.Fun.calendar
import ir.mahdiparastesh.sexbook.Fun.explode
import ir.mahdiparastesh.sexbook.Fun.shake
import ir.mahdiparastesh.sexbook.data.Filter
import ir.mahdiparastesh.sexbook.data.Report
import ir.mahdiparastesh.sexbook.data.Work
import ir.mahdiparastesh.sexbook.databinding.PageSexBinding
import ir.mahdiparastesh.sexbook.list.ReportAdap
import ir.mahdiparastesh.sexbook.more.BaseActivity.Companion.night
import ir.mahdiparastesh.sexbook.more.Delay
import ir.mahdiparastesh.sexbook.more.MessageInbox
import java.util.*

class PageSex : Fragment() {
    val c: Main by lazy { activity as Main }
    private lateinit var b: PageSexBinding
    val messages = MessageInbox(handler)
    private var filters: ArrayList<Filter>? = null

    companion object {
        // const val MAX_ADDED_REPORTS_TO_SHOW_AD = 5
        // const val DISMISSAL_REFRAIN_FROM_AD_TIMES = 3
        var handler = MutableLiveData<Handler?>(null)
    }

    override fun onCreateView(inf: LayoutInflater, parent: ViewGroup?, state: Bundle?): View =
        PageSexBinding.inflate(layoutInflater, parent, false).apply { b = this }.root

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Handler
        handler.value = object : Handler(Looper.getMainLooper()) {
            @Suppress("UNCHECKED_CAST")
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    Work.VIEW_ALL -> {
                        c.m.onani.value = msg.obj as ArrayList<Report>?
                        receivedData()
                    }
                    Work.VIEW_ONE -> if (msg.obj != null) when (msg.arg1) {
                        Work.ADD_NEW_ITEM -> {
                            if (c.m.onani.value == null) c.m.onani.value = ArrayList()
                            c.m.onani.value!!.add(msg.obj as Report)
                            resetAllMasturbations()
                            adding = false
                            b.add.explode(c)
                        }
                    }
                    Work.INSERT_ONE -> if (msg.obj != null)
                        Work(c, Work.VIEW_ONE, listOf(msg.obj as Long, Work.ADD_NEW_ITEM)).start()
                    Work.REPLACE_ALL -> {
                        Toast.makeText(c, R.string.importDone, Toast.LENGTH_LONG).show()
                        c.finish()
                        c.startActivity(Intent(c, Main::class.java))
                        // c.recreate() // Work(c, Work.VIEW_ALL).start()
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
                            if (it.size > c.m.listFilter) it[c.m.listFilter].items =
                                it[c.m.listFilter].items.apply { remove(msg.arg1) }
                        }
                        if (c.m.visOnani.value!!.size > 0) {
                            b.rv.adapter?.notifyItemRemoved(nominalPos)
                            b.rv.adapter?.notifyItemRangeChanged(
                                nominalPos,
                                b.rv.adapter!!.itemCount
                            )
                        } else resetAllMasturbations()
                    } else resetAllMasturbations()
                    Work.SCROLL -> b.rv.smoothScrollBy(0, msg.obj as Int)
                    Work.SPECIAL_ADD -> add()
                }
            }
        }
        messages.clear()

        // Filter
        //b.spnFilterMark.setColorFilter(c.color(R.color.spnFilterMark))
        b.spnFilter.setOnTouchListener { _, _ -> spnFilterTouched = true; false }
        b.spnFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(av: AdapterView<*>?) {}
            override fun onItemSelected(av: AdapterView<*>?, v: View?, i: Int, l: Long) {
                if (spnFilterTouched) filterList(i)
            }
        }

        // Add
        if (c.night()) b.addIV.colorFilter = c.pdcf()
        b.add.setOnClickListener { add() }

        if (c.m.onani.value == null) Work(c, Work.VIEW_ALL).start()
        else receivedData()
    }

    fun receivedData() {
        c.instillGuesses()
        resetAllMasturbations()
        c.load()
    }

    var spnFilterTouched = false
    fun resetAllMasturbations() {
        Collections.sort(c.m.onani.value!!, ReportAdap.Sort())
        filters = filter(c.m.onani.value!!)
        if (c.m.listFilter == -1) c.m.listFilter++

        val maxPage = filters!!.size - 1
        filterList(
            if (filters!!.size > 0 && (!c.m.filteredOnce || c.m.listFilter > maxPage))
                maxPage else c.m.listFilter
        )
        if (filters != null) {
            val titles = ArrayList<String>().apply {
                for (f in filters!!.indices) add("${f + 1}. ${filters!![f].title(c)}")
            }
            b.spnFilter.adapter = ArrayAdapter(c, R.layout.spinner, titles)
                .apply { setDropDownViewResource(R.layout.spinner_dd) }
            spnFilterTouched = false
            b.spnFilter.setSelection(c.m.listFilter, true)
        }
        c.m.filteredOnce = true
    }

    private fun filter(reports: ArrayList<Report>): ArrayList<Filter> {
        val filters: ArrayList<Filter> = ArrayList()
        for (r in reports.indices) {
            val lm = reports[r].time.calendar(c)
            val ym = arrayOf(lm[Calendar.YEAR], lm[Calendar.MONTH])
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
    fun filterList(i: Int = c.m.listFilter) {
        if (c.m.onani.value == null) {
            c.m.visOnani.value!!.clear(); return; }
        c.m.listFilter = i
        c.m.visOnani.value!!.clear()
        if (filters == null) for (o in c.m.onani.value!!) c.m.visOnani.value!!.add(o)
        else if (!filters.isNullOrEmpty())
            for (o in filters!![c.m.listFilter].items)
                if (c.m.onani.value!!.size > o)
                    c.m.visOnani.value!!.add(c.m.onani.value!![o])
        if (b.rv.adapter == null) {
            c.summarize()
            b.rv.adapter = ReportAdap(c)
        } else {
            (b.rv.adapter!! as ReportAdap).notifyAnyChange()
            b.rv.adapter!!.notifyDataSetChanged()
        }
        b.rv.scrollToPosition(c.m.visOnani.value!!.size - 1)
    }

    // private var addedToShowAd = 0
    private var adding = false
    fun add() {
        if (adding) return
        if (filters != null) filterList(filters!!.size - 1)
        adding = true
        val newOne = Report(
            Fun.now(), "", c.sp.getInt(Settings.spPrefersOrgType, 1).toByte(),
            "", true, c.sp.getLong(Settings.spDefPlace, -1L)
        )
        Work(c.c, Work.INSERT_ONE, listOf(newOne)).start()
        Delay { adding = false }
        c.c.shake()
        /*addedToShowAd++
        if (addedToShowAd >= MAX_ADDED_REPORTS_TO_SHOW_AD)
            c.loadInterstitial("ca-app-pub-9457309151954418/9505004058") {
                addedToShowAd = 0
                true
            }*/
    }
}
