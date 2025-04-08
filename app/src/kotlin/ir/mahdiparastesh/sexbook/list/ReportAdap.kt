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
import ir.mahdiparastesh.sexbook.Fun.dbValue
import ir.mahdiparastesh.sexbook.Fun.defaultOptions
import ir.mahdiparastesh.sexbook.Main
import ir.mahdiparastesh.sexbook.PageSex
import ir.mahdiparastesh.sexbook.Places
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.Settings
import ir.mahdiparastesh.sexbook.base.BaseActivity
import ir.mahdiparastesh.sexbook.data.Crush
import ir.mahdiparastesh.sexbook.data.Place
import ir.mahdiparastesh.sexbook.data.Report
import ir.mahdiparastesh.sexbook.databinding.ItemReportBinding
import ir.mahdiparastesh.sexbook.misc.LastOrgasm
import ir.mahdiparastesh.sexbook.view.AnyViewHolder
import ir.mahdiparastesh.sexbook.view.CustomSpinnerTouchListener
import ir.mahdiparastesh.sexbook.view.MaterialMenu
import ir.mahdiparastesh.sexbook.view.RecyclerViewItemEvent
import ir.mahdiparastesh.sexbook.view.SexType
import ir.mahdiparastesh.sexbook.view.SpinnerTouchListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DateFormatSymbols

class ReportAdap(
    private val c: Main, private val f: PageSex, private val autoExpand: Boolean = false
) : RecyclerView.Adapter<AnyViewHolder<ItemReportBinding>>() {

    /*init {
        setHasStableIds(true)
    }*/

    private var clockHeight = c.resources.getDimension(R.dimen.clockSize)
    private var expansion = arExpansion()
    val places = c.c.places
        .sortedWith(Place.Sort(Place.Sort.NAME))
        .filter { it.name?.isNotBlank() == true }  // throws NullPointerException when empty!
    private val clockBg: Drawable by lazy { ContextCompat.getDrawable(c, R.drawable.clock_bg)!! }
    private val etIcon: Drawable by lazy {
        ContextCompat.getDrawable(c, R.drawable.estimation)!!.mutate().apply {
            if (c.night) colorFilter =
                c.themePdcf(com.google.android.material.R.attr.colorSecondary)
        }
    }
    var crushSuggester = CrushSuggester()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            AnyViewHolder<ItemReportBinding> {
        val b = ItemReportBinding.inflate(c.layoutInflater, parent, false)

        // date & time
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

        // name
        b.name.setAdapter(crushSuggester)
        if (c.c.summary == null) b.name.setOnFocusChangeListener { view, bb ->
            if (c.c.summary != null)
                (view as TextView).onFocusChangeListener = null
            else if (bb) c.summarize()
        }

        // SexType
        b.type.adapter = SexType.Adapter(c)
        b.type.onItemSelectedListener = OnTypeSelectedListener()
        b.type.setOnTouchListener(SpinnerTouchListener())

        // Place
        b.place.adapter = ArrayAdapter(
            c, R.layout.spinner_yellow, ArrayList(places.map { it.name })
                .apply { add(0, if (places.isEmpty()) c.getString(R.string.placeHint) else "") }
        ).apply { setDropDownViewResource(R.layout.spinner_dd) }
        b.place.onItemSelectedListener = OnPlaceSelectedListener()

        return AnyViewHolder(b)
    }

    /**
     * In order to avoid data from jumping into other items, remove all [TextWatcher]s from
     * all EditTexts, then set their texts, then re-implement new listeners.
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(h: AnyViewHolder<ItemReportBinding>, i: Int) {
        //Log.d("ESPINELA", "$i")
        val r = c.mm.visReports.getOrNull(i)?.let { c.c.reports[it] } ?: return

        // date & time
        h.b.date.text = compileDate(c, r.time)
        if (!r.guess) {
            val cal = r.time.calendar(c)
            h.b.clockHour.rotation = rotateHour(cal[Calendar.HOUR_OF_DAY])
            h.b.clockMin.rotation = rotateMin(cal[Calendar.MINUTE])
            h.b.clock.setOnClickListener { // TODO move to inner class?
                TimePickerDialog.newInstance({ _, hourOfDay, minute, second ->
                    val calc = r.time.calendar(c)
                    calc[Calendar.HOUR_OF_DAY] = hourOfDay
                    calc[Calendar.MINUTE] = minute
                    calc[Calendar.SECOND] = second
                    r.time = calc.timeInMillis
                    update(r.id, dateTimeChanged = true)
                }, cal[Calendar.HOUR_OF_DAY], cal[Calendar.MINUTE], cal[Calendar.SECOND])
                    .defaultOptions()
                    .show(c.supportFragmentManager, "timepicker")
            }
            h.b.date.setOnClickListener {
                DatePickerDialog.newInstance({ _, year, month, day ->
                    cal.set(Calendar.YEAR, year)
                    cal.set(Calendar.MONTH, month)
                    cal.set(Calendar.DAY_OF_MONTH, day)
                    r.time = cal.timeInMillis
                    update(r.id, dateTimeChanged = true)
                }, cal)
                    .defaultOptions()
                    .show(c.supportFragmentManager, "datepicker")
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
        if (r.guess && c.night) h.b.clock.background = h.b.clock.background
            .apply { colorFilter = c.themePdcf(com.google.android.material.R.attr.colorSecondary) }

        // name
        h.b.name.setTextWatcher(null) // NEVER REMOVE THIS!!!
        h.b.name.setText(r.name)
        h.b.name.isEnabled = !r.guess
        h.b.name.setTextWatcher(if (!r.guess) object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, r: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, r: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val dbValue = h.b.name.dbValue()
                if (r.name != dbValue) {
                    if (!Crush.statsCleared) {
                        c.c.people.values.forEach { it.resetStats() }
                        Crush.statsCleared = true
                    }
                    r.analysis = null
                    r.name = dbValue
                    update(r.id)
                }
            }
        } else null)

        // type
        h.b.type.setSelection(r.type.toInt(), true)
        (h.b.type.onItemSelectedListener as? OnTypeSelectedListener)?.o =
            if (!r.guess) r else null
        h.b.type.isEnabled = !r.guess

        // more
        h.b.more.setOnClickListener { v -> more(v, h, r) }

        // overflow
        if (!r.guess) {
            h.b.root.setOnClickListener { turnOverflow(h.layoutPosition, h.b) }
            turnOverflow(i, h.b, expansion[i])
        } else {
            h.b.root.setOnClickListener(null)
            h.b.desc.isVisible = false
        }

        // descriptions
        h.b.desc.setTextWatcher(null)
        h.b.desc.setText(if (!r.guess) r.desc else "")
        h.b.desc.setTextWatcher(if (!r.guess) object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, r: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, r: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val dbValue = h.b.desc.dbValue()
                if (r.desc != dbValue) {
                    r.desc = dbValue
                    update(r.id)
                }
                h.b.descIcon.isVisible = dbValue != null
            }
        } else null)
        h.b.desc.isEnabled = !r.guess
        h.b.descIcon.isVisible = !r.guess && !r.desc.isNullOrBlank()

        // Place
        (h.b.place.onItemSelectedListener as? OnPlaceSelectedListener)?.o =
            if (!r.guess) r else null
        h.b.place.setOnTouchListener(
            if (!r.guess && places.isEmpty())
                CustomSpinnerTouchListener(onClick = { c.goTo(Places::class) })
            else
                SpinnerTouchListener()
        )
        h.b.place.setSelection(
            if (r.plac == -1L || places.isEmpty()) 0
            else placePos(r.plac, places) + 1, true
        )
        if (r.guess) h.b.place.isVisible = true
        h.b.place.isEnabled = !r.guess

        // long click
        val longClick =
            if (!r.guess) View.OnLongClickListener { v -> more(v, h, r); true } else null
        h.b.root.setOnLongClickListener(longClick)
        h.b.clock.setOnLongClickListener(longClick)
        h.b.date.setOnLongClickListener(longClick)
        h.b.root.isClickable = !r.guess
        h.b.root.isLongClickable = !r.guess
        h.b.root.alpha = if (!r.guess) 1f else estimatedAlpha
    }

    override fun getItemCount() = c.mm.visReports.size

    /*override fun getItemId(i: Int): Long =
        c.mm.visReports.getOrNull(i)?.let { c.c.reports[it]?.id } ?: RecyclerView.NO_ID*/

    inner class CrushSuggester : ArrayAdapter<String>(
        c, android.R.layout.simple_dropdown_item_1line, c.c.summaryCrushes()
    ) {
        fun update() {
            clear()
            addAll(c.c.summaryCrushes())
        }
    }

    private fun more(v: View, h: AnyViewHolder<ItemReportBinding>, r: Report) {
        MaterialMenu(
            c, v, R.menu.report,
            R.id.lcExpand to {
                turnOverflow(h.layoutPosition, h.b)
            },
            R.id.lcAccurate to {
                if (r.accu != !it.isChecked) {
                    r.accu = !it.isChecked
                    update(r.id)
                }
            },
            R.id.lcOrgasmed to {
                if (r.ogsm != !it.isChecked) {
                    r.ogsm = !it.isChecked
                    update(r.id, orgasmChanged = true)
                }
            },
            R.id.lcDelete to {
                CoroutineScope(Dispatchers.IO).launch {
                    c.c.dao.rDelete(r)
                    c.c.reports.remove(r.id)
                    if (c.c.reports.isNotEmpty()) c.mm.visReports.remove(r.id)
                    LastOrgasm.updateAll(c.c)

                    withContext(Dispatchers.Main) {
                        val ii = h.layoutPosition
                        notifyAnyChange(false)
                        notifyItemRemoved(ii)
                        if (c.c.reports.isEmpty()) f.reset()
                        f.updateFilterSpinner()
                    }
                }
            }
        ).apply {
            menu.findItem(R.id.lcAccurate).isChecked = r.accu
            menu.findItem(R.id.lcOrgasmed).isChecked = r.ogsm
            if (expansion[h.layoutPosition]) menu.findItem(R.id.lcExpand).title =
                c.resources.getString(R.string.collapse)
        }.show()
    }

    /** Opens or closes overflow part of a Report item, containing descriptions and Place. */
    private fun turnOverflow(i: Int, b: ItemReportBinding, expand: Boolean = !expansion[i]) {
        expansion[i] = expand
        b.desc.isVisible = expand
        b.place.isVisible = expand
    }


    inner class OnTypeSelectedListener : AdapterView.OnItemSelectedListener,
        RecyclerViewItemEvent<Report> {
        override var o: Report? = null

        override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        override fun onItemSelected(a: AdapterView<*>?, v: View?, pos: Int, l: Long) {
            val r = o ?: return
            if (r.type != pos.toByte()) {
                r.type = pos.toByte()
                update(r.id)
            }
            c.c.sp.edit().putInt(Settings.spPrefersOrgType, pos).apply()
            // TODO determine regarding the type of the latest orgasm
        }
    }

    inner class OnPlaceSelectedListener : AdapterView.OnItemSelectedListener,
        RecyclerViewItemEvent<Report> {
        override var o: Report? = null
        private var firstSelect = true

        override fun onNothingSelected(av: AdapterView<*>?) {}
        override fun onItemSelected(av: AdapterView<*>?, v: View?, pos: Int, l: Long) {
            val r = o ?: return
            if (firstSelect) {
                firstSelect = false; return; }
            // if "places" is empty, nothing ever can be selected, also onNothingSelected doesn't work!

            val pid = if (pos == 0) -1L else places[pos - 1].id
            if (r.plac != pid) {
                r.plac = pid
                update(r.id)
            }
        }
    }

    /** Writes changes to the database. */
    private fun update(
        id: Long,
        dateTimeChanged: Boolean = false,
        orgasmChanged: Boolean = false
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            c.c.dao.rUpdate(c.c.reports[id]!!)

            if (dateTimeChanged || orgasmChanged) LastOrgasm.doUpdateAll(c.c)

            // ONLY if date or time have been changed...
            if (dateTimeChanged) withContext(Dispatchers.Main) {
                val oldPos = c.mm.visReports.indexOf(id)
                val ym = c.c.reports[id]!!.time.calendar(c).createFilterYm()
                if (f.filters.getOrNull(c.mm.listFilter)
                        ?.let { ym.first == it.year && ym.second == it.month } == true
                ) {
                    // Report is still in this month
                    c.mm.sortVisReports(c.c)
                    val newPos = c.mm.visReports.indexOf(id)
                    if (oldPos != newPos) {
                        notifyItemMoved(oldPos, newPos)
                        f.b.rv.smoothScrollToPosition(newPos)
                    }
                    notifyItemChanged(newPos)
                } else {
                    // Report is moved to another month or is missing
                    notifyItemRemoved(oldPos)
                    f.reset(id)
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

    companion object {
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

        fun placePos(id: Long, list: List<Place>): Int {
            var index = -1
            for (i in list.indices)
                if (list[i].id == id)
                    index = i
            return index
        }
    }
}
