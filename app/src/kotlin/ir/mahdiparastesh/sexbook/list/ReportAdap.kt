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
import ir.mahdiparastesh.sexbook.PageSex
import ir.mahdiparastesh.sexbook.Places
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.Settings
import ir.mahdiparastesh.sexbook.base.BaseActivity
import ir.mahdiparastesh.sexbook.base.BaseActivity.Companion.night
import ir.mahdiparastesh.sexbook.data.Crush
import ir.mahdiparastesh.sexbook.data.Place
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
        val r = c.m.visOnani.getOrNull(i)?.let { c.m.onani?.getOrNull(it) } ?: return

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
                    .show(c.supportFragmentManager, "edit${h.layoutPosition}")
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
                    val ii = view.tag!!.substring(4).toInt()
                    val gPos = c.m.visOnani[ii]
                    if (c.m.onani!!.size > gPos && view.tag!!.substring(0, 4) == tagEdit) {
                        c.m.onani!![gPos].time = cal.timeInMillis
                        update(ii, true)
                    }
                }, cal).defaultOptions()
                    // .setOnDismissListener { dialogDismissed() }
                    .show(c.supportFragmentManager, "$tagEdit${h.layoutPosition}")
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
                c.m.onani!![c.m.visOnani[h.layoutPosition]].apply {
                    if (name != h.b.name.text.toString()) {
                        if (!Crush.statsCleared) {
                            c.m.people?.forEach { it.resetStats() }
                            Crush.statsCleared = true
                        }
                        analysis = null
                        name = h.b.name.text.toString()
                        update(h.layoutPosition)
                    }
                }
            }
        } else null)

        // Type
        h.b.type.setSelection(r.type.toInt(), true)
        h.b.type.onItemSelectedListener =
            if (!r.guess) object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(adapterView: AdapterView<*>?) {}
                override fun onItemSelected(a: AdapterView<*>?, v: View?, i: Int, l: Long) {
                    c.m.onani!![c.m.visOnani[h.layoutPosition]].apply {
                        if (type != i.toByte()) {
                            type = i.toByte()
                            update(h.layoutPosition)
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
                c.m.onani!![c.m.visOnani[h.layoutPosition]].apply {
                    if (desc != h.b.desc.text.toString()) {
                        desc = h.b.desc.text.toString()
                        update(h.layoutPosition)
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
            if (c.m.onani != null) MaterialMenu(c, v, R.menu.report, Act().apply {
                val ii = h.layoutPosition

                this[R.id.lcExpand] = {
                    turnOverflow(ii, h.b)
                }
                this[R.id.lcAccurate] = {
                    c.m.onani!![c.m.visOnani[ii]].apply {
                        if (accu != !it.isChecked) {
                            accu = !it.isChecked
                            update(ii)
                        }
                    }
                }
                this[R.id.lcOrgasmed] = {
                    c.m.onani!![c.m.visOnani[ii]].apply {
                        if (ogsm != !it.isChecked) {
                            ogsm = !it.isChecked
                            update(ii)
                        }
                    }
                }
                this[R.id.lcDelete] = {
                    CoroutineScope(Dispatchers.IO).launch {
                        val gPos = c.m.visOnani[ii]
                        c.m.dao.rDelete(c.m.onani!![gPos])
                        c.m.onani!!.removeAt(gPos)
                        if (c.m.onani!!.isNotEmpty()) c.m.visOnani.removeAt(ii)
                        else f.resetAllReports()
                        LastOrgasm.updateAll(c)

                        withContext(Dispatchers.Main) {
                            notifyAnyChange(false)
                            notifyItemRemoved(ii)
                            notifyItemRangeChanged(ii, itemCount - ii)
                            f.updateFilterSpinner()
                        }
                    }
                }
            }).apply {
                menu.findItem(R.id.lcAccurate).isChecked =
                    c.m.onani!![c.m.visOnani[h.layoutPosition]].accu
                menu.findItem(R.id.lcOrgasmed).isChecked =
                    c.m.onani!![c.m.visOnani[h.layoutPosition]].ogsm
                if (expansion[h.layoutPosition]) menu.findItem(R.id.lcExpand).title =
                    c.resources.getString(R.string.collapse)
            }.show()
            true
        } else null
        h.b.root.setOnLongClickListener(longClick)
        h.b.clock.setOnLongClickListener(longClick)
        h.b.date.setOnLongClickListener(longClick)
        h.b.root.isClickable = !r.guess
        h.b.root.isLongClickable = !r.guess
        h.b.root.alpha = if (!r.guess) 1f else estimatedAlpha
    }

    override fun getItemCount() = c.m.visOnani.size

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onTimeSet(view: TimePickerDialog, hourOfDay: Int, minute: Int, second: Int) {
        if (c.m.onani == null || view.tag == null || view.tag!!.length <= 4) return
        val i = view.tag!!.substring(4).toInt()
        val gPos = c.m.visOnani[i]
        if (c.m.onani!!.size > gPos) when (view.tag!!.substring(0, 4)) {
            tagEdit -> {
                val calc = c.m.onani!![gPos].time.calendar(c)
                calc[Calendar.HOUR_OF_DAY] = hourOfDay
                calc[Calendar.MINUTE] = minute
                calc[Calendar.SECOND] = second
                c.m.onani!![gPos].time = calc.timeInMillis
                update(i, true)
            }
        }
    }

    inner class CrushSuggester :
        ArrayAdapter<String>(c, android.R.layout.simple_dropdown_item_1line, c.m.summaryCrushes()) {
        fun update() {
            clear()
            addAll(c.m.summaryCrushes())
        }
    }

    /** Opens or closes overflow part of a Report item, containing descriptions and Place. */
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
            c.m.onani!![c.m.visOnani[i!!]].apply {
                if (plac != id) {
                    plac = id
                    update(i!!)
                }
            }
        }
    }

    /**
     * Writes changes to the database.
     *
     * @param i nominal position (in visOnani)
     * @param dateTimeChanged set to true if date or time of Report is changed
     */
    private fun update(i: Int, dateTimeChanged: Boolean = false) {
        if (c.m.onani == null) return
        val gPos = c.m.visOnani[i]
        if (c.m.onani!!.size <= gPos || gPos < 0) return

        CoroutineScope(Dispatchers.IO).launch {
            c.m.dao.rUpdate(c.m.onani!![gPos])
            LastOrgasm.doUpdateAll(c)
            withContext(Dispatchers.Main) {
                if (c.m.onani == null) {
                    f.resetAllReports(); return@withContext; }

                // ONLY if date or time have been changed...
                if (!dateTimeChanged) return@withContext
                val ym = c.m.onani!![gPos].time.calendar(c).createFilterYm()
                if (f.filters.getOrNull(c.m.listFilter)
                        ?.let { ym.first == it.year && ym.second == it.month } == true
                ) { // report is still in this month
                    notifyItemChanged(i)
                    c.m.visOnani.sortBy { c.m.onani!![it].time }
                    val newPos = c.m.visOnani.indexOf(gPos)
                    f.b.rv.smoothScrollToPosition(newPos)
                    c.m.visOnani[i] = c.m.onani!!.indexOf(c.m.onani!![gPos])
                    c.m.visOnani[newPos] = gPos
                    notifyItemMoved(i, newPos)
                } else { // report moved to another month or is missing
                    notifyItemRemoved(i)
                    notifyItemRangeChanged(i, itemCount - i)
                    f.resetAllReports(gPos)
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

        fun placePos(id: Long, list: List<Place>): Int {
            var index = -1
            for (i in list.indices)
                if (list[i].id == id)
                    index = i
            return index
        }
    }
}
