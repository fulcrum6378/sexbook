package ir.mahdiparastesh.sexbook

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import ir.mahdiparastesh.sexbook.Fun.calendar
import ir.mahdiparastesh.sexbook.Fun.createFilterYm
import ir.mahdiparastesh.sexbook.Fun.explode
import ir.mahdiparastesh.sexbook.Fun.shake
import ir.mahdiparastesh.sexbook.base.BaseActivity.Companion.night
import ir.mahdiparastesh.sexbook.base.BasePage
import ir.mahdiparastesh.sexbook.data.Report
import ir.mahdiparastesh.sexbook.databinding.PageSexBinding
import ir.mahdiparastesh.sexbook.list.ReportAdap
import ir.mahdiparastesh.sexbook.more.LastOrgasm
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class PageSex : BasePage() {
    lateinit var b: PageSexBinding
    var filters: List<Report.Filter> = listOf()

    companion object {
        const val PREV_RECORDS_REQUIRED_TO_USE_THE_SAME_NAME = 5
        //const val MAX_ADDED_REPORTS_TO_SHOW_AD = 5
        //const val DISMISSAL_REFRAIN_FROM_AD_TIMES = 3
    }

    override fun onCreateView(inf: LayoutInflater, parent: ViewGroup?, state: Bundle?): View =
        PageSexBinding.inflate(layoutInflater, parent, false).apply { b = this }.root

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Spinner of filters
        b.spnFilter.setOnTouchListener { _, _ -> spnFilterTouched = true; false }
        b.spnFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(av: AdapterView<*>?) {}
            override fun onItemSelected(av: AdapterView<*>?, v: View?, i: Int, l: Long) {
                if (spnFilterTouched) applyFilter(i, false)
            }
        }

        // "Add" button
        if (c.night()) b.addIV.colorFilter = c.themePdcf()
        b.add.setOnClickListener { add() }

        if (c.m.onani != null) prepareList()
    }

    override fun prepareList() {
        resetAllReports(
            c.intentViewId?.let { id ->
                c.m.findGlobalIndexOfReport(id).let { if (it != -1) it else null }
            })
        c.intentViewId = null
        c.load()
    }

    var spnFilterTouched = false
    fun resetAllReports(toGlobalIndexOfItem: Int? = null) {
        if (c.m.onani == null) return
        //Log.println(Log.ASSERT, "ZOEY", "resetAllReports to index $toGlobalIndexOfItem")
        filters = createFilters(c.m.onani!!)
        //Log.println(Log.ASSERT, "ZOEY", "resetAllReports ${filters.size} filters")

        // which month to show?
        var newFilter = if (c.m.listFilter == -1) (filters.size - 1) else c.m.listFilter
        toGlobalIndexOfItem?.also { gIndex ->
            val toFilter = c.m.onani!![gIndex].time.calendar(c).createFilterYm()
            val fIndex =
                filters.indexOfFirst { it.year == toFilter.first && it.month == toFilter.second }
            if (fIndex != -1) newFilter = fIndex
        }

        // application...
        applyFilter(newFilter, true, toGlobalIndexOfItem != null)
        updateFilterSpinner()

        // scroll to the edited item position...
        toGlobalIndexOfItem?.also { gIndex ->
            val pos = c.m.visOnani.indexOf(c.m.onani!![gIndex])
            if (pos != -1) b.rv.smoothScrollToPosition(pos)
        }
    }

    fun updateFilterSpinner() {
        b.spnFilter.adapter = ArrayAdapter(
            c, R.layout.spinner_yellow,
            List(filters.size) { f -> "${f + 1}. ${filters[f].title(c)}" }
        ).apply { setDropDownViewResource(R.layout.spinner_dd) }
        spnFilterTouched = false
        b.spnFilter.setSelection(c.m.listFilter, true)
    }

    fun createFilters(reports: List<Report>): List<Report.Filter> {
        //Log.println(Log.ASSERT, "ZOEY", "createFilters ${reports.size} reports")
        val filters = arrayListOf<Report.Filter>()
        for (r in reports.indices) {
            val ym = reports[r].time.calendar(c).createFilterYm()
            var filterExists = false
            for (f in filters.indices)
                if (filters[f].year == ym.first && filters[f].month == ym.second) {
                    filterExists = true
                    filters[f] = filters[f].apply { put(r) }
                }
            if (!filterExists)
                filters.add(Report.Filter(ym.first, ym.second, ArrayList<Int>().apply { add(r) }))
        }
        return filters.toList()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun applyFilter(i: Int, causedByResetAllReports: Boolean, willScrollToItem: Boolean = false) {
        //Log.println(Log.ASSERT, "ZOEY", "applyFilter $i")
        if (c.m.listFilter == i && c.m.listFilter > -1 && !causedByResetAllReports) return
        c.m.visOnani.clear()
        //Log.println(Log.ASSERT, "ZOEY", "visOnani cleared")
        if (c.m.onani == null) return // if onani is null, empty visOnani.
        c.m.listFilter = i

        // Fill visOnani...
        if (filters.isEmpty()) for (o in c.m.onani!!) c.m.visOnani.add(o)
        else {
            for (o in filters[c.m.listFilter].map)
                if (c.m.onani!!.size > o)
                    c.m.visOnani.add(c.m.onani!![o])
            Collections.sort(c.m.visOnani, Report.Sort())
        }
        //Log.println(Log.ASSERT, "ZOEY", "visOnani filled ${c.m.visOnani.size}")

        // Update the adapter and scroll to position...
        if (b.rv.adapter == null) b.rv.adapter = ReportAdap(c, this) else {
            (b.rv.adapter!! as ReportAdap).notifyAnyChange(true)
            b.rv.adapter!!.notifyDataSetChanged()
        }
        if (!willScrollToItem) b.rv.scrollToPosition(c.m.visOnani.size - 1)
    }

    // private var addedToShowAd = 0
    private var adding = false
    fun add() {
        if (adding) return
        adding = true
        CoroutineScope(Dispatchers.IO).launch {
            var name = ""
            c.m.onani?.size?.also { total ->
                if (total < PREV_RECORDS_REQUIRED_TO_USE_THE_SAME_NAME) return@also
                name = c.m.onani!![total - 1].name ?: ""
                if (name == "") return@also
                for (r in (total - 2) downTo (total - PREV_RECORDS_REQUIRED_TO_USE_THE_SAME_NAME))
                    if (!name.equals(c.m.onani?.get(r)?.name, true)) { // don't use ".."
                        name = ""; break; }
            }
            val newOne = Report(
                Fun.now(), name, c.sp.getInt(Settings.spPrefersOrgType, 1).toByte(),
                "", true, c.sp.getLong(Settings.spDefPlace, -1L), true, -127
            )
            newOne.id = c.m.dao.rInsert(newOne)
            LastOrgasm.updateAll(c)
            val firstRecordEver = c.m.onani == null
            if (firstRecordEver) c.m.onani = ArrayList()
            c.m.onani!!.add(newOne)
            adding = false

            withContext(Dispatchers.Main) {
                val ym = newOne.time.calendar(c).createFilterYm()
                if (filters.indexOfFirst { it.year == ym.first && it.month == ym.second }
                    == c.m.listFilter && c.m.listFilter >= 0 && !firstRecordEver) {
                    // add to the bottom of the recycler view
                    c.m.visOnani.add(newOne)
                    Collections.sort(c.m.visOnani, Report.Sort())
                    (b.rv.adapter as ReportAdap?)?.notifyAnyChange(false)
                    val thePos = c.m.visOnani.indexOf(newOne)
                    b.rv.adapter?.notifyItemInserted(thePos)
                    filters.getOrNull(c.m.listFilter)?.map
                        ?.add(thePos, c.m.onani!!.size - 1)
                    updateFilterSpinner()
                } else // go to/create a new month
                    resetAllReports(c.m.onani!!.size - 1)

                b.add.explode(c)
            }
        }
        c.c.shake()
        /*addedToShowAd++
        if (addedToShowAd >= MAX_ADDED_REPORTS_TO_SHOW_AD)
            c.loadInterstitial("ca-app-pub-9457309151954418/9505004058") {
                addedToShowAd = 0
                true
            }*/
    }
}
