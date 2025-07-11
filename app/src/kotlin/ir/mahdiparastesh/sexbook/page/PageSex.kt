package ir.mahdiparastesh.sexbook.page

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.util.contains
import androidx.core.util.isEmpty
import androidx.core.util.set
import androidx.core.view.isVisible
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.base.BasePage
import ir.mahdiparastesh.sexbook.ctrl.LastOrgasm
import ir.mahdiparastesh.sexbook.data.Report
import ir.mahdiparastesh.sexbook.databinding.PageSexBinding
import ir.mahdiparastesh.sexbook.list.ReportAdap
import ir.mahdiparastesh.sexbook.util.LongSparseArrayExt.iterator
import ir.mahdiparastesh.sexbook.util.NumberUtils
import ir.mahdiparastesh.sexbook.util.NumberUtils.calendar
import ir.mahdiparastesh.sexbook.util.NumberUtils.createFilterYm
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.max

/**
 * This Fragment lists and controls the [Report] table in the database.
 * It classifies [Report]s by their months and lets the user jump between months via a Spinner
 * called `spnFilter`.
 */
class PageSex : BasePage() {
    lateinit var b: PageSexBinding
    var filters: List<Report.Filter> = listOf()
    private var spnFilterTouched = false
    private var anGrowShrinkForAdd: AnimatorSet? = null
    private val growShrinkScale = 1.5f

    override fun onCreateView(inf: LayoutInflater, parent: ViewGroup?, state: Bundle?): View =
        PageSexBinding.inflate(layoutInflater, parent, false).also { b = it }.root

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //Log.d("ZOEY", "PageSex::onViewCreated")

        // Spinner of filters
        b.spnFilter.setOnTouchListener { _, _ -> spnFilterTouched = true; false }
        b.spnFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(av: AdapterView<*>?) {}
            override fun onItemSelected(av: AdapterView<*>?, v: View?, i: Int, l: Long) {
                if (spnFilterTouched) applyFilter(i, false)
            }
        }

        // "Add" button
        if (c.night) b.addIV.colorFilter = c.themePdcf()
        b.add.setOnClickListener { add() }

        if (c.c.dbLoaded) prepareList()
    }

    override fun prepareList() {
        super.prepareList()
        //Log.d("ZOEY", "PageSex::prepareList()")
        reset(c.intentViewId)
        c.intentViewId = null

        b.empty.isVisible = c.c.reports.isEmpty()
        if (c.c.reports.isEmpty()) anGrowShrinkForAdd = AnimatorSet().apply {
            duration = 1000L
            playTogether(
                ObjectAnimator.ofFloat(
                    b.add, View.SCALE_X, 1f, growShrinkScale
                ).apply {
                    repeatMode = ValueAnimator.REVERSE
                    repeatCount = ValueAnimator.INFINITE
                },
                ObjectAnimator.ofFloat(
                    b.add, View.SCALE_Y, 1f, growShrinkScale
                ).apply {
                    repeatMode = ValueAnimator.REVERSE
                    repeatCount = ValueAnimator.INFINITE
                }
            )
            start()
        }

        if (!c.vm.loaded) c.load()
    }

    /** Resets the filters, reloads a list and scrolls to an item if necessary. */
    fun reset(scrollToId: Long? = null) {
        filters = createFilters()

        // which month to show?
        var curFilter = if (c.vm.listFilter == -1) (filters.size - 1) else c.vm.listFilter
        if (scrollToId != null && scrollToId in c.c.reports) {
            val toFilter = c.c.reports[scrollToId]!!.time.calendar(c.c).createFilterYm()
            val fIndex =
                filters.indexOfFirst { it.year == toFilter.first && it.month == toFilter.second }
            if (fIndex != -1) curFilter = fIndex
        }

        // application...
        applyFilter(curFilter, true, scrollToId != null)
        updateFilterSpinner()

        // scroll to the edited item position...
        if (scrollToId != null) {
            val pos = c.vm.visReports.indexOf(scrollToId)
            if (pos != -1) b.rv.smoothScrollToPosition(pos)
        }
    }

    /** Creates monthly filters for [Report] instances. */
    private fun createFilters(): List<Report.Filter> {
        val filters = arrayListOf<Report.Filter>()
        for ((id, r) in c.c.reports.iterator()) {
            val ym = r.time.calendar(c.c).createFilterYm()
            var filterExists = false
            for (f in filters.indices)
                if (filters[f].year == ym.first && filters[f].month == ym.second) {
                    filterExists = true
                    filters[f].put(id)
                }
            if (!filterExists)
                filters.add(Report.Filter(ym.first, ym.second, arrayListOf(id)))
        }
        return filters.sortedBy { (it.year * 100) + it.month }
    }

    /**
     * Chooses a monthly Filter and applies it to the current page,
     * so that this page displays only one month of all [Report] instances.
     */
    @SuppressLint("NotifyDataSetChanged")
    fun applyFilter(i: Int, causedByResetAllReports: Boolean, willScrollToItem: Boolean = false) {
        if (c.vm.listFilter == i && c.vm.listFilter > -1 && !causedByResetAllReports) return
        c.vm.listFilter = i
        c.vm.visReports =
            if (filters.isNotEmpty()) filters[c.vm.listFilter].map
            else arrayListOf()
        c.vm.sortVisReports(c.c)

        // update the adapter
        if (b.rv.adapter == null) b.rv.adapter = ReportAdap(c, this) else {
            (b.rv.adapter!! as ReportAdap).notifyAnyChange(true)
            b.rv.adapter!!.notifyDataSetChanged()
        }

        // scroll to position
        if (!willScrollToItem) b.rv.scrollToPosition(c.vm.visReports.size - 1)
    }

    /** Updates the bottom spinner according to the current monthly Filter. */
    fun updateFilterSpinner() {
        b.spnFilter.adapter = ArrayAdapter(
            c, R.layout.spinner_yellow,
            List(filters.size) { f -> "${f + 1}. ${filters[f].title(c.c)}" }
        ).apply { setDropDownViewResource(R.layout.spinner_dd) }
        spnFilterTouched = false
        b.spnFilter.setSelection(c.vm.listFilter, true)
    }

    /** Adds a new [Report] instance and performs subsequent necessary actions. */
    fun add() {
        CoroutineScope(Dispatchers.IO).launch {

            // determine default values based on previous Report instances
            var name: String? = null
            var type: Byte? = null
            var plac: Long? = null
            val lastIndex = c.c.reports.size() - 1
            for (r in lastIndex downTo max(0, lastIndex - 3)) {
                val report = c.c.reports.valueAt(r)

                // one-time definitions
                if (r == lastIndex) {
                    type = report.type
                    plac = report.place
                }

                // detect a regularly repeated monoamorous crush (3^: 5th after 4 repetitions)
                if (name == null) {
                    if (report.name.isNullOrBlank())
                        break  // the last Report has no name; so it's useless.

                    name = report.name
                    // remember the name of the last Report.
                } else {
                    if (!name.equals(report.name, true)) {
                        name = null
                        break
                        // forget the one-time or inadequately-repeated name and break the loop.
                    }
                    // the name has been repeated, so continue looping.
                    // (do not copy older Strings which might have different character cases.)
                }
            }

            // create a Report instance and insert it in the ViewModel and the Database
            val newOne = Report(
                time = NumberUtils.now(),
                name = name,
                type = type ?: 1.toByte(),
                description = null,
                accurate = true,
                place = plac ?: -1L,
                orgasmed = true
            )
            newOne.id = c.c.dao.rInsert(newOne)
            LastOrgasm.updateAll(c.c)
            c.c.reports[newOne.id] = newOne

            withContext(Dispatchers.Main) {
                val ym = newOne.time.calendar(c.c).createFilterYm()
                if (c.vm.listFilter >= 0 &&
                    filters.indexOfFirst { it.year == ym.first && it.month == ym.second } == c.vm.listFilter
                ) {
                    // add to the bottom of the RecyclerView
                    c.vm.visReports.add(newOne.id)
                    c.vm.sortVisReports(c.c)
                    (b.rv.adapter as ReportAdap?)?.apply {
                        notifyAnyChange(false)
                        notifyItemInserted(c.vm.visReports.indexOf(newOne.id))
                    }
                    updateFilterSpinner()
                } else {
                    // go to/create a new month
                    reset(newOne.id)
                }

                c.explosionEffect(b.add)
                b.empty.isVisible = false
                if (anGrowShrinkForAdd != null) {
                    anGrowShrinkForAdd?.cancel()
                    anGrowShrinkForAdd = null
                    b.add.scaleX = 1f
                    b.add.scaleY = 1f
                }
            }
        }
        c.shake()
    }
}
