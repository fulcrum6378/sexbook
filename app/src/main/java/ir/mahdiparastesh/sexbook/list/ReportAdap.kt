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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import ir.mahdiparastesh.sexbook.Fun.calendar
import ir.mahdiparastesh.sexbook.Fun.defaultOptions
import ir.mahdiparastesh.sexbook.Fun.vis
import ir.mahdiparastesh.sexbook.Main
import ir.mahdiparastesh.sexbook.Model
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.Settings
import ir.mahdiparastesh.sexbook.data.Place
import ir.mahdiparastesh.sexbook.data.Report
import ir.mahdiparastesh.sexbook.data.Work
import ir.mahdiparastesh.sexbook.databinding.ItemReportBinding
import ir.mahdiparastesh.sexbook.mdtp.Utils
import ir.mahdiparastesh.sexbook.mdtp.date.DatePickerDialog
import ir.mahdiparastesh.sexbook.mdtp.time.TimePickerDialog
import ir.mahdiparastesh.sexbook.more.*
import ir.mahdiparastesh.sexbook.more.BaseActivity.Companion.night
import java.text.DateFormatSymbols

class ReportAdap(val c: Main, private val autoExpand: Boolean = false) :
    RecyclerView.Adapter<AnyViewHolder<ItemReportBinding>>(),
    TimePickerDialog.OnTimeSetListener {

    private var clockHeight = c.resources.getDimension(R.dimen.clockSize)
    private var expansion = arExpansion()
    val places = c.m.places.value?.sortedWith(Place.Sort(Place.Sort.NAME))
    private val clockBg: Drawable by lazy { ContextCompat.getDrawable(c, R.drawable.clock_bg)!! }
    private val etIcon: Drawable by lazy {
        ContextCompat.getDrawable(c, R.drawable.estimation)!!.mutate().apply {
            if (c.night()) colorFilter = c.pdcf(R.color.mrvClock)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): AnyViewHolder<ItemReportBinding> {
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

        // Type
        b.type.adapter = TypeAdap(c)

        // Place
        if (places != null)
            b.place.adapter = ArrayAdapter(c, R.layout.spinner,
                ArrayList(places.map { it.name }).apply { add(0, "") })
                .apply { setDropDownViewResource(R.layout.spinner_dd) }

        return AnyViewHolder(b)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(h: AnyViewHolder<ItemReportBinding>, i: Int) {
        val r = c.m.visOnani.getOrNull(i) ?: return

        // Date & Time
        h.b.date.text = compileDate(c, r.time)
        if (r.isReal) {
            val cal = r.time.calendar(c)
            h.b.clockHour.rotation = rotateHour(cal[Calendar.HOUR_OF_DAY])
            h.b.clockMin.rotation = rotateMin(cal[Calendar.MINUTE])
            h.b.clock.setOnClickListener {
                if (c.m.onani.value == null) return@setOnClickListener
                TimePickerDialog.newInstance(
                    this, cal[Calendar.HOUR_OF_DAY], cal[Calendar.MINUTE]
                ).apply {
                    version = TimePickerDialog.Version.VERSION_2
                    accentColor = c.color(R.color.CP)
                    setOkColor(c.color(R.color.dialogText))
                    setCancelColor(c.color(R.color.dialogText))
                    // setOnDismissListener { dialogDismissed() }
                    show(c.supportFragmentManager, "edit${globalPos(c.m, h.layoutPosition)}")
                }
                // mayShowAd()
            }
            h.b.date.setOnClickListener {
                if (c.m.onani.value == null) return@setOnClickListener
                DatePickerDialog.newInstance({ view, year, month, day ->
                    if (c.m.onani.value == null || view.tag == null || view.tag!!.length <= 4)
                        return@newInstance
                    cal.set(Calendar.YEAR, year)
                    cal.set(Calendar.MONTH, month)
                    cal.set(Calendar.DAY_OF_MONTH, day)
                    val pos = view.tag!!.substring(4).toInt()
                    if (c.m.onani.value!!.size > pos && view.tag!!.substring(0, 4) == tagEdit) {
                        c.m.onani.value!![pos].time = cal.timeInMillis
                        Work(c, Work.UPDATE_ONE, listOf(c.m.onani.value!![pos], pos, 0)).start()
                    }
                }, cal).defaultOptions(c)
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
        for (tim in arrayOf(h.b.clockHour, h.b.clockMin, h.b.point, h.b.ampm)) tim.vis(r.isReal)
        h.b.clock.background = if (r.isReal) clockBg else etIcon
        if (!r.isReal && c.night()) h.b.clock.background = h.b.clock.background
            .apply { colorFilter = c.pdcf(R.color.mrvClock) }

        // Name
        h.b.name.setText(r.name)
        if (r.isReal) {
            var crushes = arrayListOf<String>()
            if (c.m.summary != null)
                crushes = ArrayList(c.m.summary!!.scores.keys)
            h.b.name.setAdapter(
                ArrayAdapter(c, android.R.layout.simple_dropdown_item_1line, crushes)
            )
        }
        h.b.name.isEnabled = r.isReal
        val nameWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, r: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, r: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) {
                c.m.visOnani[h.layoutPosition].apply {
                    if (name != h.b.name.text.toString()) {
                        name = h.b.name.text.toString()
                        updateStatic(this, h.layoutPosition)
                    }
                }
            }
        }
        if (r.isReal) h.b.name.addTextChangedListener(nameWatcher)
        else h.b.name.removeTextChangedListener(nameWatcher)

        // Type
        h.b.type.setSelection(r.type.toInt(), true)
        h.b.type.onItemSelectedListener =
            if (r.isReal) object : AdapterView.OnItemSelectedListener {
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
        h.b.type.isEnabled = r.isReal

        // Overflow
        if (r.isReal) {
            h.b.root.setOnClickListener { turnOverflow(h.layoutPosition, h.b) }
            turnOverflow(i, h.b, expansion[i])
        } else {
            h.b.root.setOnClickListener(null)
            h.b.desc.vis(false)
        }

        // Descriptions
        h.b.desc.setText(if (r.isReal) r.desc else "")
        val descWatcher = object : TextWatcher {
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
        }
        if (r.isReal) h.b.desc.addTextChangedListener(descWatcher)
        else h.b.desc.removeTextChangedListener(descWatcher)
        h.b.desc.isEnabled = r.isReal

        // Place
        var placeTouched = false
        if (r.isReal)
            h.b.place.setOnTouchListener { _, _ -> placeTouched = true; false }
        else h.b.place.setOnTouchListener(null)
        h.b.place.onItemSelectedListener =
            if (r.isReal) object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(av: AdapterView<*>?) {}
                override fun onItemSelected(av: AdapterView<*>?, v: View?, i: Int, l: Long) {
                    if (places == null || !placeTouched) return
                    val id = if (i == 0) -1L else places[i - 1].id
                    c.m.visOnani[h.layoutPosition].apply {
                        if (plac != id) {
                            plac = id
                            updateStatic(this, h.layoutPosition)
                        }
                    }
                }
            } else null
        h.b.place.setSelection(
            if (r.plac == -1L || places == null) 0
            else placePos(r.plac, places) + 1, true
        )
        if (!r.isReal) h.b.place.vis()
        h.b.place.isEnabled = r.isReal

        // Long Click
        val longClick = if (r.isReal) View.OnLongClickListener { v ->
            if (c.m.onani.value == null || c.m.visOnani.size <= h.layoutPosition)
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
                this[R.id.lcDelete] = {
                    if (c.m.onani.value != null) {
                        val aPos = globalPos(c.m, h.layoutPosition)
                        Work(c, Work.DELETE_ONE, listOf(c.m.onani.value!![aPos], aPos)).start()
                    }
                }
            }).apply {
                menu.findItem(R.id.lcAccurate).isChecked =
                    c.m.visOnani[h.layoutPosition].accu
                if (expansion[h.layoutPosition]) menu.findItem(R.id.lcExpand).title =
                    c.resources.getString(R.string.collapse)
            }.show()
            true
        } else null
        h.b.root.setOnLongClickListener(longClick)
        h.b.clock.setOnLongClickListener(longClick)
        h.b.date.setOnLongClickListener(longClick)
        //h.b.name.setOnLongClickListener(longClick)
        h.b.root.isClickable = r.isReal
        h.b.root.isLongClickable = r.isReal
        h.b.root.alpha = if (r.isReal) 1f else estimatedAlpha
    }

    override fun getItemCount() = c.m.visOnani.size

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onTimeSet(view: TimePickerDialog, hourOfDay: Int, minute: Int, second: Int) {
        if (c.m.onani.value == null || view.tag == null || view.tag!!.length <= 4) return
        val pos = view.tag!!.substring(4).toInt()
        if (c.m.onani.value!!.size > pos) when (view.tag!!.substring(0, 4)) {
            tagEdit -> {
                val calc = c.m.onani.value!![pos].time.calendar(c)
                calc[Calendar.HOUR_OF_DAY] = hourOfDay
                calc[Calendar.MINUTE] = minute
                calc[Calendar.SECOND] = second
                c.m.onani.value!![pos].time = calc.timeInMillis
                Work(c, Work.UPDATE_ONE, listOf(c.m.onani.value!![pos], pos, 0)).start()
            }
        }
    }

    private fun turnOverflow(i: Int, b: ItemReportBinding, expand: Boolean = !expansion[i]) {
        expansion[i] = expand
        b.desc.vis(expand)
        b.place.vis(expand)
    }

    fun updateStatic(updated: Report, nominalPos: Int) {
        if (c.m.onani.value == null) return
        val pos = globalPos(c.m, nominalPos)
        if (c.m.onani.value!!.size <= pos || pos < 0) return
        c.m.onani.value!![pos] = updated
        Work(c, Work.UPDATE_ONE, listOf(c.m.onani.value!![pos], pos, 1)).start()
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
            val lm = calType.newInstance().apply { timeInMillis = time }
            return "${Utils.localSymbols(c.c, calType).shortMonths[lm.get(Calendar.MONTH)]} " +
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
            for (o in m.onani.value!!.indices)
                if (m.onani.value!![o].id == m.visOnani[pos].id)
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

    // Don't migrate to Java!
    class Sort : Comparator<Report> {
        override fun compare(a: Report, b: Report) = a.time.compareTo(b.time)
    }
}
