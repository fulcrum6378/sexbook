package ir.mahdiparastesh.sexbook.list

import android.annotation.SuppressLint
import android.content.Context
import android.icu.util.Calendar
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
import ir.mahdiparastesh.sexbook.Fun
import ir.mahdiparastesh.sexbook.Fun.Companion.calType
import ir.mahdiparastesh.sexbook.Fun.Companion.vis
import ir.mahdiparastesh.sexbook.Main
import ir.mahdiparastesh.sexbook.Model
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.data.Place
import ir.mahdiparastesh.sexbook.data.Report
import ir.mahdiparastesh.sexbook.data.Work
import ir.mahdiparastesh.sexbook.databinding.ItemReportBinding
import ir.mahdiparastesh.sexbook.more.*
import ir.mahdiparastesh.sexbook.more.BaseActivity.Companion.dp
import ir.mahdiparastesh.sexbook.more.BaseActivity.Companion.night
import java.util.*
import kotlin.collections.ArrayList

class ReportAdap(val c: Main, val autoExpand: Boolean = false) :
    RecyclerView.Adapter<ReportAdap.MyViewHolder>(),
    TimePickerDialog.OnTimeSetListener {

    var clockHeight = dp(48)
    val expansion = BooleanArray(itemCount) { autoExpand }
    val places = c.m.places.value?.sortedWith(Place.Sort(Place.Sort.NAME))

    class MyViewHolder(val b: ItemReportBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val b = ItemReportBinding.inflate(c.layoutInflater, parent, false)

        // Fonts
        b.date.typeface = c.font1Bold
        b.ampm.typeface = c.font1

        // Date & Time
        if (b.clock.height != 0) clockHeight = b.clock.height
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
        b.placeMark.setColorFilter(c.color(R.color.spnFilterMark))
        if (places != null) b.place.adapter =
            SpinnerAdap(c, ArrayList(places.map { it.name }).apply { add(0, "") })

        return MyViewHolder(b)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(h: MyViewHolder, i: Int) {
        val itm = c.m.visOnani.value!![i]

        // Date & Time
        var cal = Calendar.getInstance()
        cal.timeInMillis = itm.time
        h.b.clockHour.rotation = rotateHour(cal[Calendar.HOUR_OF_DAY])
        h.b.clockMin.rotation = rotateMin(cal[Calendar.MINUTE])
        h.b.date.text = compileDate(c, itm.time)
        if (!itm.estimated) h.b.clock.setOnClickListener {
            TimePickerDialog.newInstance(
                this, cal[Calendar.HOUR_OF_DAY], cal[Calendar.MINUTE], false
            ).apply {
                isThemeDark = night
                version = TimePickerDialog.Version.VERSION_2
                accentColor = c.color(R.color.CP)
                setOkColor(c.color(R.color.mrvPopupButtons))
                setCancelColor(c.color(R.color.mrvPopupButtons))
                show(
                    c.supportFragmentManager,
                    "edit${globalPos(c.m, h.layoutPosition)}"
                )
            }
        } else h.b.clock.setOnClickListener(null)
        if (!itm.estimated) h.b.date.setOnClickListener {
            LocalDatePicker(c, "$tagEdit${globalPos(c.m, h.layoutPosition)}", cal) { view, time ->
                if (c.m.onani.value == null || view.tag == null || view.tag!!.length <= 4)
                    return@LocalDatePicker
                val pos = view.tag!!.substring(4).toInt()
                if (c.m.onani.value!!.size > pos && view.tag!!.substring(0, 4) == tagEdit) {
                    c.m.onani.value!![pos].time = time
                    Work(c, Work.UPDATE_ONE, listOf(c.m.onani.value!![pos], pos, 0)).start()
                }
            }
        } else h.b.date.setOnClickListener(null)
        h.b.ampm.text =
            c.resources.getText(if (cal[Calendar.HOUR_OF_DAY] > 12) R.string.PM else R.string.AM)

        // Name
        h.b.name.setText(itm.name)
        if (!itm.estimated) {
            var crushes = arrayListOf<String>()
            if (c.m.summary.value != null)
                crushes = ArrayList(c.m.summary.value!!.scores.keys)
            h.b.name.setAdapter(
                ArrayAdapter(c, android.R.layout.simple_dropdown_item_1line, crushes)
            )
        }
        h.b.name.isEnabled = !itm.estimated
        val nameWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, r: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, r: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) {
                c.m.visOnani.value!![h.layoutPosition].apply {
                    if (name != h.b.name.text.toString()) {
                        name = h.b.name.text.toString()
                        update(this, h.layoutPosition)
                    }
                }
            }
        }
        if (!itm.estimated) h.b.name.addTextChangedListener(nameWatcher)
        else h.b.name.removeTextChangedListener(nameWatcher)

        // Type
        h.b.type.setSelection(itm.type.toInt(), true)
        h.b.type.onItemSelectedListener =
            if (!itm.estimated) object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(adapterView: AdapterView<*>?) {}
                override fun onItemSelected(a: AdapterView<*>?, v: View?, i: Int, l: Long) {
                    c.m.visOnani.value!![h.layoutPosition].apply {
                        if (type != i.toByte()) {
                            type = i.toByte()
                            update(this, h.layoutPosition)
                        }
                    }
                }
            } else null
        h.b.type.isEnabled = !itm.estimated

        // Overflow
        if (!itm.estimated) {
            h.b.root.setOnClickListener { turnOverflow(h.layoutPosition, h.b) }
            turnOverflow(i, h.b, expansion[i])
        } else {
            h.b.root.setOnClickListener(null)
            vis(h.b.desc, false)
        }

        // Descriptions
        h.b.desc.setText(if (!itm.estimated) itm.desc else "")
        val descWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, r: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, r: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) {
                c.m.visOnani.value!![h.layoutPosition].apply {
                    if (desc != h.b.desc.text.toString()) {
                        desc = h.b.desc.text.toString()
                        update(this, h.layoutPosition)
                    }
                }
            }
        }
        if (!itm.estimated) h.b.desc.addTextChangedListener(descWatcher)
        else h.b.desc.removeTextChangedListener(descWatcher)
        h.b.desc.isEnabled = !itm.estimated

        // Place
        var placeTouched = false
        if (!itm.estimated)
            h.b.place.setOnTouchListener { _, _ -> placeTouched = true; false }
        else h.b.place.setOnTouchListener(null)
        h.b.place.onItemSelectedListener =
            if (!itm.estimated) object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(av: AdapterView<*>?) {}
                override fun onItemSelected(av: AdapterView<*>?, v: View?, i: Int, l: Long) {
                    if (c.m.places.value == null || !placeTouched) return
                    val id = if (i == 0) -1L else c.m.places.value!![i - 1].id
                    c.m.visOnani.value!![h.layoutPosition].apply {
                        if (plac != id) {
                            plac = id
                            update(this, h.layoutPosition)
                        }
                    }
                }
            } else null
        h.b.place.setSelection(
            if (itm.plac == -1L) 0 else placePos(itm.plac, c.m.places.value!!) + 1, true
        )
        if (itm.estimated) {
            vis(h.b.place)
            vis(h.b.placeMark)
        }
        h.b.place.isEnabled = !itm.estimated

        // Long Click
        val longClick = if (!itm.estimated) View.OnLongClickListener { v ->
            MaterialMenu(c, v, R.menu.report, Act().apply {
                this[R.id.lcExpand] = {
                    turnOverflow(h.layoutPosition, h.b)
                }
                this[R.id.lcAccurate] = {
                    c.m.visOnani.value!![h.layoutPosition].apply {
                        if (acur != !it.isChecked) {
                            acur = !it.isChecked
                            update(this, h.layoutPosition)
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
                    c.m.visOnani.value!![h.layoutPosition].acur
                if (expansion[h.layoutPosition]) menu.findItem(R.id.lcExpand).title =
                    c.resources.getString(R.string.collapse)
            }.show()
            true
        } else null
        h.b.root.setOnLongClickListener(longClick)
        h.b.clock.setOnLongClickListener(longClick)
        h.b.date.setOnLongClickListener(longClick)
        h.b.name.setOnLongClickListener(longClick)
        h.b.root.isClickable = !itm.estimated
        h.b.root.isLongClickable = !itm.estimated
        h.b.root.alpha = if (!itm.estimated) 1f else estimatedAlpha
    }

    override fun getItemCount() = c.m.visOnani.value!!.size

    override fun onTimeSet(view: TimePickerDialog, hourOfDay: Int, minute: Int, second: Int) {
        if (c.m.onani.value == null || view.tag == null || view.tag!!.length <= 4) return
        val pos = view.tag!!.substring(4).toInt()
        if (c.m.onani.value!!.size > pos) when (view.tag!!.substring(0, 4)) {
            tagEdit -> {
                var calc = Calendar.getInstance()
                calc.timeInMillis = c.m.onani.value!![pos].time
                calc[Calendar.HOUR_OF_DAY] = hourOfDay
                calc[Calendar.MINUTE] = minute
                calc[Calendar.SECOND] = second
                c.m.onani.value!![pos].time = calc.timeInMillis
                Work(c, Work.UPDATE_ONE, listOf(c.m.onani.value!![pos], pos, 0)).start()
            }
        }
    }

    fun turnOverflow(i: Int, b: ItemReportBinding, expand: Boolean = !expansion[i]) {
        expansion[i] = expand
        vis(b.desc, expand)
        vis(b.place, expand)
        vis(b.placeMark, expand)
    }

    fun update(updated: Report, nominalPos: Int) {
        if (c.m.onani.value == null) return
        val pos = globalPos(c.m, nominalPos)
        if (c.m.onani.value!!.size <= pos || pos < 0) return
        c.m.onani.value!![pos] = updated
        Work(c, Work.UPDATE_ONE, listOf(c.m.onani.value!![pos], pos, 1)).start()
    }

    companion object {
        const val tagEdit = "edit"
        const val estimatedAlpha = 0.6f

        fun compileDate(c: Context, time: Long): String {
            val lm = Calendar.getInstance().apply { timeInMillis = time }
            if (calType() == Fun.CalendarType.JALALI) {
                val jal = Jalali(lm)
                return "${c.resources.getStringArray(R.array.jMonths)[jal.M]} ${jal.D}"
            }
            return "${c.resources.getStringArray(R.array.months)[lm.get(Calendar.MONTH)]} " +
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
                if (m.onani.value!![o].id == m.visOnani.value!![pos].id)
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
