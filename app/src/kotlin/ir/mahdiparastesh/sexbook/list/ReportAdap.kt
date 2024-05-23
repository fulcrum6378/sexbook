package ir.mahdiparastesh.sexbook.list

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.icu.util.Calendar
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import ir.mahdiparastesh.mcdtp.McdtpUtils
import ir.mahdiparastesh.mcdtp.date.DatePickerDialog
import ir.mahdiparastesh.mcdtp.time.TimePickerDialog
import ir.mahdiparastesh.sexbook.Fun.calendar
import ir.mahdiparastesh.sexbook.Fun.createFilterYm
import ir.mahdiparastesh.sexbook.Fun.defaultOptions
import ir.mahdiparastesh.sexbook.Main
import ir.mahdiparastesh.sexbook.Model
import ir.mahdiparastesh.sexbook.PageSex
import ir.mahdiparastesh.sexbook.Places
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.Settings
import ir.mahdiparastesh.sexbook.base.BaseActivity
import ir.mahdiparastesh.sexbook.base.BaseActivity.Companion.night
import ir.mahdiparastesh.sexbook.data.Place
import ir.mahdiparastesh.sexbook.data.Report
import ir.mahdiparastesh.sexbook.databinding.ItemReportBinding
import ir.mahdiparastesh.sexbook.more.Act
import ir.mahdiparastesh.sexbook.more.AnyViewHolder
import ir.mahdiparastesh.sexbook.more.LastOrgasm
import ir.mahdiparastesh.sexbook.more.MaterialMenu
import ir.mahdiparastesh.sexbook.more.RecyclerViewItemEvent
import ir.mahdiparastesh.sexbook.more.TypeAdap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DateFormatSymbols
import java.util.Collections

class ReportAdap(
    private val c: Main, private val f: PageSex, private val autoExpand: Boolean = false
) : RecyclerView.Adapter<AnyViewHolder<ItemReportBinding>>(),
    TimePickerDialog.OnTimeSetListener {

    private var clockHeight = c.resources.getDimension(R.dimen.clockSize)
    private var expansion = arExpansion()
    val places = c.m.places
        ?.sortedWith(Place.Sort(Place.Sort.NAME))
        ?.filter { it.name?.isNotBlank() == true } // throws NullPointerException when empty!
        ?: arrayListOf()
    private val clockBg: Drawable by lazy { ContextCompat.getDrawable(c, R.drawable.clock_bg)!! }
    private val etIcon: Drawable by lazy {
        ContextCompat.getDrawable(c, R.drawable.estimation)!!.mutate().apply {
            if (c.night()) colorFilter =
                c.themePdcf(com.google.android.material.R.attr.colorSecondary)
        }
    }
    var crushSuggester = CrushSuggester()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            AnyViewHolder<ItemReportBinding> {
        val b = ItemReportBinding.inflate(c.layoutInflater, parent, false)

        // Date & Time
        val pointHeight = clockHeight * perh(b.point)
        val hourHeight = clockHeight * perh(b.clockHour)
        val minuteHeight = clockHeight * perh(b.clockMin)
        b.clockHour.apply {
            pivotX = (clockHeight * perw(b.clockHour)) / 2f
            pivotY = hourHeight - (pointHeight / 2f)
        }
        b.clockMin.apply {
            pivotX = (clockHeight * perw(b.clockMin)) / 2f
            pivotY = minuteHeight - (pointHeight / 2f)
        }

        // Name
        b.name.setAdapter(crushSuggester)
        if (c.m.summary == null) b.name.setOnFocusChangeListener { view, bb ->
            if (c.m.summary != null)
                (view as TextView).onFocusChangeListener = null
            else if (bb) c.summarize()
        }

        // Type
        b.type.adapter = TypeAdap(c)

        // Place
        b.place.adapter = ArrayAdapter(c, R.layout.spinner_yellow, ArrayList(places.map { it.name })
            .apply { add(0, if (places.isEmpty()) c.getString(R.string.placeHint) else "") }
        ).apply { setDropDownViewResource(R.layout.spinner_dd) }
        b.place.onItemSelectedListener = OnPlaceSelectedListener()

        return AnyViewHolder(b)
    }

    /** In order to avoid data from jumping into other items, remove all TextWatchers from
     * all EditTexts, then set their texts, then re-implement new listeners. */
    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(h: AnyViewHolder<ItemReportBinding>, i: Int) {
        val r = c.m.visOnani.getOrNull(i) ?: return

        // Date & Time
        h.b.date.text = compileDate(c, r.time)
        if (!r.guess) {
            val cal = r.time.calendar(c)
            h.b.clockHour.rotation = rotateHour(cal[Calendar.HOUR_OF_DAY])
            h.b.clockMin.rotation = rotateMin(cal[Calendar.MINUTE])
            h.b.clock.setOnClickListener {
                if (c.m.onani == null) return@setOnClickListener
                TimePickerDialog.newInstance(
                    this, cal[Calendar.HOUR_OF_DAY], cal[Calendar.MINUTE], cal[Calendar.SECOND]
                ).defaultOptions()
                    //.setOnDismissListener { dialogDismissed() }
                    .show(c.supportFragmentManager, "edit${globalPos(c.m, h.layoutPosition)}")
                // mayShowAd()
            }
            h.b.date.setOnClickListener {
                if (c.m.onani == null) return@setOnClickListener
                DatePickerDialog.newInstance({ view, year, month, day ->
                    if (c.m.onani == null || view.tag == null || view.tag!!.length <= 4)
                        return@newInstance
                    cal.set(Calendar.YEAR, year)
                    cal.set(Calendar.MONTH, month)
                    cal.set(Calendar.DAY_OF_MONTH, day)
                    val pos = view.tag!!.substring(4).toInt()
                    if (c.m.onani!!.size > pos && view.tag!!.substring(0, 4) == tagEdit) {
                        c.m.onani!![pos].time = cal.timeInMillis
                        syncDb(pos, true)
                    }
                }, cal).defaultOptions()
                    // .setOnDismissListener { dialogDismissed() }
                    .show(c.supportFragmentManager, "$tagEdit${globalPos(c.m, h.layoutPosition)}")
                // mayShowAd()
            }
            h.b.ampm.text =
                DateFormatSymbols().amPmStrings[if (cal[Calendar.HOUR_OF_DAY] < 12) 0 else 1]
        } else {
            h.b.clock.setOnClickListener(null)
            h.b.date.setOnClickListener(null)
        }
        for (tim in arrayOf(h.b.clockHour, h.b.clockMin, h.b.point, h.b.ampm))
            tim.isVisible = !r.guess
        h.b.clock.background = if (!r.guess) clockBg else etIcon
        if (r.guess && c.night()) h.b.clock.background = h.b.clock.background
            .apply { colorFilter = c.themePdcf(com.google.android.material.R.attr.colorSecondary) }

        // Name
        h.b.name.setText(r.name)
        h.b.name.isEnabled = !r.guess
        h.b.name.setTextWatcher(if (!r.guess) object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, r: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, r: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) {
                c.m.visOnani[h.layoutPosition].apply {
                    if (name != h.b.name.text.toString()) {
                        name = h.b.name.text.toString()
                        updateStatic(this, h.layoutPosition)
                    }
                    analysis = null
                }
            }
        } else null)

        // Type
        h.b.type.setSelection(r.type.toInt(), true)
        h.b.type.onItemSelectedListener =
            if (!r.guess) object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(adapterView: AdapterView<*>?) {}
                override fun onItemSelected(a: AdapterView<*>?, v: View?, i: Int, l: Long) {
                    c.m.visOnani[h.layoutPosition].apply {
                        if (type != i.toByte()) {
                            type = i.toByte()
                            updateStatic(this, h.layoutPosition)
                        }
                    }
                    c.sp.edit().putInt(Settings.spPrefersOrgType, i).apply()
                }
            } else null
        h.b.type.isEnabled = !r.guess

        // Overflow
        if (!r.guess) {
            h.b.root.setOnClickListener { turnOverflow(h.layoutPosition, h.b) }
            turnOverflow(i, h.b, expansion[i])
        } else {
            h.b.root.setOnClickListener(null)
            h.b.desc.isVisible = false
        }

        // Descriptions
        h.b.desc.setTextWatcher(null)
        h.b.desc.setText(if (!r.guess) r.desc else "")
        h.b.desc.setTextWatcher(if (!r.guess) object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, r: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, r: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) {
                c.m.visOnani[h.layoutPosition].apply {
                    if (desc != h.b.desc.text.toString()) {
                        desc = h.b.desc.text.toString()
                        updateStatic(this, h.layoutPosition)
                    }
                }
            }
        } else null)
        h.b.desc.isEnabled = !r.guess

        // Place
        (h.b.place.onItemSelectedListener as? OnPlaceSelectedListener)?.i =
            if (!r.guess) i else null
        if (!r.guess && places.isEmpty())
            h.b.place.setOnTouchListener { _, _ -> c.goTo(Places::class); false }
        else h.b.place.setOnTouchListener(null)
        h.b.place.setSelection(
            if (r.plac == -1L || places.isEmpty()) 0
            else placePos(r.plac, places) + 1, true
        )
        if (r.guess) h.b.place.isVisible = true
        h.b.place.isEnabled = !r.guess

        // Long Click
        val longClick = if (!r.guess) View.OnLongClickListener { v ->
            if (c.m.onani == null || c.m.visOnani.size <= h.layoutPosition)
                return@OnLongClickListener true
            MaterialMenu(c, v, R.menu.report, Act().apply {
                this[R.id.lcExpand] = {
                    turnOverflow(h.layoutPosition, h.b)
                }
                this[R.id.lcAccurate] = {
                    c.m.visOnani[h.layoutPosition].apply {
                        if (accu != !it.isChecked) {
                            accu = !it.isChecked
                            updateStatic(this, h.layoutPosition)
                        }
                    }
                }
                this[R.id.lcOrgasmed] = {
                    c.m.visOnani[h.layoutPosition].apply {
                        if (ogsm != !it.isChecked) {
                            ogsm = !it.isChecked
                            updateStatic(this, h.layoutPosition)
                        }
                    }
                }
                this[R.id.lcDelete] = {
                    if (c.m.onani != null) {
                        val aPos = globalPos(c.m, h.layoutPosition)
                        CoroutineScope(Dispatchers.IO).launch {
                            c.m.dao.rDelete(c.m.onani!![aPos])
                            LastOrgasm.updateAll(c)
                            withContext(Dispatchers.Main) {
                                if (c.m.onani != null) {
                                    val nominalPos = c.m.visOnani.indexOf(c.m.onani!![aPos])
                                    if (nominalPos != -1) {
                                        c.m.visOnani.removeAt(nominalPos)
                                        notifyItemRemoved(nominalPos)
                                        notifyItemRangeChanged(nominalPos, itemCount)
                                    } else f.resetAllReports()

                                    c.m.onani!!.remove(c.m.onani!![aPos])
                                    if (c.m.onani!!.isNotEmpty())
                                        f.filters.getOrNull(c.m.listFilter)?.map?.remove(aPos)
                                    else f.filters = f.createFilters(c.m.onani!!)
                                    f.updateFilterSpinner()
                                    notifyAnyChange(false)
                                } else f.resetAllReports()
                            }
                        }
                    }
                }
            }).apply {
                menu.findItem(R.id.lcAccurate).isChecked = c.m.visOnani[h.layoutPosition].accu
                menu.findItem(R.id.lcOrgasmed).isChecked = c.m.visOnani[h.layoutPosition].ogsm
                if (expansion[h.layoutPosition]) menu.findItem(R.id.lcExpand).title =
                    c.resources.getString(R.string.collapse)
            }.show()
            true
        } else null
        h.b.root.setOnLongClickListener(longClick)
        h.b.clock.setOnLongClickListener(longClick)
        h.b.date.setOnLongClickListener(longClick)
        //h.b.name.setOnLongClickListener(longClick)
        h.b.root.isClickable = !r.guess
        h.b.root.isLongClickable = !r.guess
        h.b.root.alpha = if (!r.guess) 1f else estimatedAlpha
    }

    override fun getItemCount() = c.m.visOnani.size

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onTimeSet(view: TimePickerDialog, hourOfDay: Int, minute: Int, second: Int) {
        if (c.m.onani == null || view.tag == null || view.tag!!.length <= 4) return
        val pos = view.tag!!.substring(4).toInt()
        if (c.m.onani!!.size > pos) when (view.tag!!.substring(0, 4)) {
            tagEdit -> {
                val calc = c.m.onani!![pos].time.calendar(c)
                calc[Calendar.HOUR_OF_DAY] = hourOfDay
                calc[Calendar.MINUTE] = minute
                calc[Calendar.SECOND] = second
                c.m.onani!![pos].time = calc.timeInMillis
                syncDb(pos, true)
            }
        }
    }

    inner class CrushSuggester :
        ArrayAdapter<String>(c, android.R.layout.simple_dropdown_item_1line, c.m.summaryCrushes()) {
        fun update() {
            clear()
            addAll(c.m.summaryCrushes())
            //notifyDataSetChanged()
            //Toast.makeText(c, c.m.summary?.scores?.size.toString(), Toast.LENGTH_LONG).show()
        }
    }

    private fun turnOverflow(i: Int, b: ItemReportBinding, expand: Boolean = !expansion[i]) {
        expansion[i] = expand
        b.desc.isVisible = expand
        b.place.isVisible = expand
    }

    inner class OnPlaceSelectedListener : AdapterView.OnItemSelectedListener,
        RecyclerViewItemEvent {
        private var firstSelect = true
        override var i: Int? = null
        override fun onNothingSelected(av: AdapterView<*>?) {}
        override fun onItemSelected(av: AdapterView<*>?, v: View?, pos: Int, l: Long) {
            if (i == null) return
            if (firstSelect) {
                firstSelect = false; return; }
            // if "places" is empty, nothing ever can be selected, also onNothingSelected doesn't work!

            val id = if (pos == 0) -1L else places[pos - 1].id
            c.m.visOnani[i!!].apply {
                if (plac != id) {
                    plac = id
                    updateStatic(this, i!!)
                }
            }
        }
    }

    fun updateStatic(updated: Report, nominalPos: Int) {
        if (c.m.onani == null) return
        val pos = globalPos(c.m, nominalPos)
        if (c.m.onani!!.size <= pos || pos < 0) return
        c.m.onani!![pos] = updated
        syncDb(pos, false)
    }

    private fun syncDb(pos: Int, dateTimeChanged: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            c.m.dao.rUpdate(c.m.onani!![pos])
            LastOrgasm.doUpdateAll(c)
            withContext(Dispatchers.Main) {
                if (c.m.onani == null) {
                    f.resetAllReports(); return@withContext; }

                val nominalPos = c.m.visOnani.indexOf(c.m.onani!![pos])
                if (nominalPos != -1) c.m.visOnani[nominalPos] = c.m.onani!![pos]

                // in addition, if date or time have been changed...
                if (!dateTimeChanged) return@withContext
                val ym = c.m.onani!![pos].time.calendar(c).createFilterYm()
                if (nominalPos != -1 && f.filters.getOrNull(c.m.listFilter)
                        ?.let { ym.first == it.year && ym.second == it.month } == true
                ) { // report is still in this month
                    notifyItemChanged(nominalPos)
                    Collections.sort(c.m.visOnani, Report.Sort())
                    val newPos = c.m.visOnani.indexOf(c.m.onani!![pos])
                    notifyItemMoved(nominalPos, newPos)
                    f.b.rv.smoothScrollToPosition(newPos)
                    f.filters.getOrNull(c.m.listFilter)?.map?.apply {
                        this[nominalPos] = c.m.onani!!.indexOf(c.m.visOnani[nominalPos])
                        this[newPos] = pos
                    }
                } else { // report moved to another month or is missing
                    notifyItemRemoved(nominalPos)
                    notifyItemRangeChanged(nominalPos, itemCount)
                    f.resetAllReports(pos)
                }
            }
        }
    }

    private fun arExpansion() = BooleanArray(itemCount) { autoExpand }

    fun notifyAnyChange(reset: Boolean) {
        if (reset) {
            expansion = arExpansion()
            return; }
        val oldExp = expansion
        expansion = arExpansion()
        for (i in oldExp.indices)
            if (expansion.size > i)
                expansion[i] = oldExp[i]
    }

    /*private var refrainFromAd = 0
    private var showingDialog = false
    private fun mayShowAd() {
        showingDialog = true
        if (refrainFromAd == 0) {
            c.loadInterstitial("ca-app-pub-9457309151954418/4827392445") { !c.showingAd && !showingDialog }
            refrainFromAd += PageSex.DISMISSAL_REFRAIN_FROM_AD_TIMES
        }
    }

    private fun dialogDismissed() {
        showingDialog = false
        c.showInterstitial()
    }*/

    companion object {
        const val tagEdit = "edit"
        const val estimatedAlpha = 0.75f

        fun compileDate(c: BaseActivity, time: Long): String {
            val calType = c.calType()
            val lm = calType.getDeclaredConstructor().newInstance().apply { timeInMillis = time }
            return "${McdtpUtils.localSymbols(c.c, calType).shortMonths[lm.get(Calendar.MONTH)]} " +
                    "${lm.get(Calendar.DAY_OF_MONTH)}"
        }

        fun perw(v: View) =
            (v.layoutParams as ConstraintLayout.LayoutParams).matchConstraintPercentWidth

        fun perh(v: View) =
            (v.layoutParams as ConstraintLayout.LayoutParams).matchConstraintPercentHeight

        fun rotateHour(h: Int) = (h - (if (h > 12) 12 else 0)) * 30f

        fun rotateMin(m: Int) = m * 6f

        fun globalPos(m: Model, pos: Int): Int {
            var index = -1
            for (o in m.onani!!.indices)
                if (m.onani!![o].id == m.visOnani[pos].id)
                    index = o
            return index
        }

        fun placePos(id: Long, list: List<Place>): Int {
            var index = -1
            for (i in list.indices)
                if (list[i].id == id)
                    index = i
            return index
        }
    }
}
