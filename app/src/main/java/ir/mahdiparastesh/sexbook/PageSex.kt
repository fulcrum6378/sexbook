package ir.mahdiparastesh.sexbook

import android.annotation.SuppressLint
import android.content.Intent
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
import ir.mahdiparastesh.sexbook.Fun.createFilterYm
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
    private var filters: List<Filter> = listOf()

    companion object {
        /*const val MAX_ADDED_REPORTS_TO_SHOW_AD = 5
        const val DISMISSAL_REFRAIN_FROM_AD_TIMES = 3*/
        var handler = MutableLiveData<Handler?>(null)
    }

    override fun onCreateView(inf: LayoutInflater, parent: ViewGroup?, state: Bundle?): View =
        PageSexBinding.inflate(layoutInflater, parent, false).apply { b = this }.root

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        handler.value = object : Handler(Looper.getMainLooper()) {
            @Suppress("UNCHECKED_CAST")
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    Work.VIEW_ALL -> {
                        c.m.onani.value = msg.obj as ArrayList<Report>?
                        receivedData()
                    }
                    Work.VIEW_ONE -> if (msg.obj != null) when (msg.arg1) {
                        Work.ADD_NEW_ITEM -> (msg.obj as Report).also { report ->
                            if (c.m.onani.value == null) c.m.onani.value = ArrayList()
                            c.m.onani.value!!.add(report)

                            val ym = report.time.calendar(c).createFilterYm()
                            if (filters.indexOfFirst { it.year == ym.first && it.month == ym.second }
                                == c.m.listFilter) { // add at the bottom of the same month
                                c.m.visOnani.add(report)
                                Collections.sort(c.m.visOnani, ReportAdap.Sort())
                                (b.rv.adapter!! as ReportAdap).notifyAnyChange(false)
                                val thePos = c.m.visOnani.indexOf(report)
                                filters.getOrNull(c.m.listFilter)?.map
                                    ?.add(thePos, c.m.onani.value!!.size - 1)
                                b.rv.adapter?.notifyItemInserted(thePos)
                            } else // go to/create a new month
                                resetAllReports(c.m.onani.value!!.size - 1)

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
                    }

                    Work.UPDATE_ONE -> if (c.m.onani.value != null) {
                        val nominalPos = c.m.visOnani.indexOf(c.m.onani.value!![msg.arg1])
                        if (nominalPos != -1)
                            c.m.visOnani[nominalPos] = c.m.onani.value!![msg.arg1]

                        // In addition, if date or time have been changed...
                        if (msg.arg2 == 0) {
                            val ym = c.m.onani.value!![msg.arg1].time.calendar(c).createFilterYm()
                            if (filters[c.m.listFilter].let { ym.first == it.year && ym.second == it.month }
                                && nominalPos != -1) { // report is still in this month
                                b.rv.adapter?.notifyItemChanged(nominalPos)
                                Collections.sort(c.m.visOnani, ReportAdap.Sort())
                                val newPos = c.m.visOnani.indexOf(c.m.onani.value!![msg.arg1])
                                b.rv.adapter?.notifyItemMoved(nominalPos, newPos)
                                b.rv.smoothScrollToPosition(newPos)
                                filters.getOrNull(c.m.listFilter)?.map?.apply {
                                    this[nominalPos] =
                                        c.m.onani.value!!.indexOf(c.m.visOnani[nominalPos])
                                    this[newPos] = msg.arg1
                                }
                            } else { // report moved to another month or is missing
                                b.rv.adapter?.notifyItemRemoved(nominalPos)
                                b.rv.adapter?.notifyItemRangeChanged(
                                    nominalPos, b.rv.adapter!!.itemCount
                                )
                                resetAllReports(msg.arg1)
                            }
                        }
                    } else resetAllReports()

                    Work.DELETE_ONE -> if (c.m.onani.value != null) {
                        val nominalPos = c.m.visOnani.indexOf(c.m.onani.value!![msg.arg1])
                        if (nominalPos != -1) {
                            c.m.visOnani.removeAt(nominalPos)
                            b.rv.adapter?.notifyItemRemoved(nominalPos)
                            b.rv.adapter?.notifyItemRangeChanged(
                                nominalPos, b.rv.adapter!!.itemCount
                            )
                        } else resetAllReports()

                        c.m.onani.value!!.remove(c.m.onani.value!![msg.arg1])
                        filters.getOrNull(c.m.listFilter)?.map?.remove(msg.arg1)
                        (b.rv.adapter!! as ReportAdap).notifyAnyChange(false)
                    } else resetAllReports()

                    Work.SCROLL -> b.rv.smoothScrollBy(0, msg.obj as Int)
                    Work.SPECIAL_ADD -> add()
                }
            }
        }
        messages.clear()

        // Spinner Filter
        //b.spnFilterMark.setColorFilter(c.color(R.color.spnFilterMark))
        b.spnFilter.setOnTouchListener { _, _ -> spnFilterTouched = true; false }
        b.spnFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(av: AdapterView<*>?) {}
            override fun onItemSelected(av: AdapterView<*>?, v: View?, i: Int, l: Long) {
                if (spnFilterTouched) applyFilter(i)
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
        resetAllReports(c.intentToGlobalIndexOfItem)
        c.intentToGlobalIndexOfItem = null
        c.load()
    }

    var spnFilterTouched = false
    fun resetAllReports(toGlobalIndexOfItem: Int? = null) {
        filters = createFilters(c.m.onani.value!!)

        // Which month to show?
        var newFilter = filters.size - 1
        if (newFilter == -1) newFilter = 0
        toGlobalIndexOfItem?.also { gIndex ->
            val toFilter = c.m.onani.value!![gIndex].time.calendar(c).createFilterYm()
            val fIndex =
                filters.indexOfFirst { it.year == toFilter.first && it.month == toFilter.second }
            if (fIndex != -1) newFilter = fIndex
        }
        applyFilter(newFilter, toGlobalIndexOfItem == null)

        // Prepare the bottom spinner...
        b.spnFilter.adapter = ArrayAdapter(
            c, R.layout.spinner,
            filters.mapIndexed { f, _ -> "${f + 1}. ${filters[f].title(c)}" }
        ).apply { setDropDownViewResource(R.layout.spinner_dd) }
        spnFilterTouched = false
        b.spnFilter.setSelection(c.m.listFilter, true)

        // Scroll to the edited item position...
        toGlobalIndexOfItem?.also { gIndex ->
            val pos = c.m.visOnani.indexOf(c.m.onani.value!![gIndex])
            if (pos != -1) b.rv.smoothScrollToPosition(pos)
        }
    }

    private fun createFilters(reports: ArrayList<Report>): List<Filter> {
        val filters = arrayListOf<Filter>()
        for (r in reports.indices) {
            val name = reports[r].time.calendar(c).createFilterYm()
            var filterExists = false
            for (f in filters.indices)
                if (filters[f].year == name.first && filters[f].month == name.second) {
                    filterExists = true
                    filters[f] = filters[f].apply { put(r) }
                }
            if (!filterExists)
                filters.add(Filter(name.first, name.second, ArrayList<Int>().apply { add(r) }))
        }
        return filters.toList()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun applyFilter(i: Int, scrollDown: Boolean = true) {
        if (c.m.onani.value == null) {
            c.m.visOnani.clear(); return; }
        if (c.m.listFilter == i) return
        c.m.listFilter = i
        c.m.visOnani.clear()

        // Fill visOnani
        if (filters.isEmpty()) for (o in c.m.onani.value!!) c.m.visOnani.add(o)
        else {
            for (o in filters[c.m.listFilter].map)
                if (c.m.onani.value!!.size > o)
                    c.m.visOnani.add(c.m.onani.value!![o])
            Collections.sort(c.m.visOnani, ReportAdap.Sort())
        }

        if (b.rv.adapter == null) {
            c.summarize()
            b.rv.adapter = ReportAdap(c)
        } else {
            (b.rv.adapter!! as ReportAdap).notifyAnyChange(true)
            b.rv.adapter!!.notifyDataSetChanged()
        }
        if (scrollDown) b.rv.scrollToPosition(c.m.visOnani.size - 1)
    }

    // private var addedToShowAd = 0
    private var adding = false
    fun add() {
        if (adding) return
        if (filters.isNotEmpty()) applyFilter(filters.size - 1) // go to the new page
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
