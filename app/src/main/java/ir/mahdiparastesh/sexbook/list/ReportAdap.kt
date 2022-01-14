package ir.mahdiparastesh.sexbook.list

import android.text.Editable
import android.text.SpannableString
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.view.ContextThemeWrapper
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.forEach
import androidx.recyclerview.widget.RecyclerView
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
import ir.mahdiparastesh.sexbook.Fun
import ir.mahdiparastesh.sexbook.Fun.Companion.c
import ir.mahdiparastesh.sexbook.Fun.Companion.calType
import ir.mahdiparastesh.sexbook.Fun.Companion.color
import ir.mahdiparastesh.sexbook.Fun.Companion.dp
import ir.mahdiparastesh.sexbook.Fun.Companion.font1
import ir.mahdiparastesh.sexbook.Fun.Companion.night
import ir.mahdiparastesh.sexbook.Main
import ir.mahdiparastesh.sexbook.Model
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.data.Report
import ir.mahdiparastesh.sexbook.data.Work
import ir.mahdiparastesh.sexbook.databinding.ItemReportBinding
import ir.mahdiparastesh.sexbook.jdtp.utils.PersianCalendar
import ir.mahdiparastesh.sexbook.more.CustomTypefaceSpan
import ir.mahdiparastesh.sexbook.more.Jalali
import ir.mahdiparastesh.sexbook.more.TypeAdap
import java.util.*
import kotlin.collections.ArrayList

import ir.mahdiparastesh.sexbook.jdtp.date.DatePickerDialog as JalaliDatePickerDialog

class ReportAdap(val c: Main) : RecyclerView.Adapter<ReportAdap.MyViewHolder>(),
    DatePickerDialog.OnDateSetListener,
    TimePickerDialog.OnTimeSetListener,
    JalaliDatePickerDialog.OnDateSetListener {

    var clockHeight = dp(48)
    val tagEdit = "edit"

    class MyViewHolder(val b: ItemReportBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val b = ItemReportBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        // Fonts
        b.date.typeface = Fun.font1Bold
        b.ampm.typeface = font1

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
        b.type.adapter = TypeAdap()

        return MyViewHolder(b)
    }

    override fun onBindViewHolder(h: MyViewHolder, i: Int) {

        // Date & Time
        var cal = Calendar.getInstance()
        cal.timeInMillis = c.m.visOnani.value!![i].time
        h.b.clockHour.rotation = rotateHour(cal[Calendar.HOUR_OF_DAY])
        h.b.clockMin.rotation = rotateMin(cal[Calendar.MINUTE])
        h.b.date.text = compileDate(c.m.visOnani.value!![i].time)
        h.b.clock.setOnClickListener {
            TimePickerDialog.newInstance(
                this, cal[Calendar.HOUR_OF_DAY], cal[Calendar.MINUTE], false
            ).apply {
                isThemeDark = night
                version = TimePickerDialog.Version.VERSION_2
                accentColor = color(R.color.CP)
                setOkColor(color(R.color.mrvPopupButtons))
                setCancelColor(color(R.color.mrvPopupButtons))
                show(
                    c.supportFragmentManager,
                    "edit${allPos(c.m, h.layoutPosition)}"
                )
            }
        }
        h.b.date.setOnClickListener {
            if (calType() == Fun.CalendarType.GREGORY) DatePickerDialog.newInstance(
                this, cal[Calendar.YEAR], cal[Calendar.MONTH], cal[Calendar.DAY_OF_MONTH]
            ).apply {
                isThemeDark = night
                version = DatePickerDialog.Version.VERSION_2
                accentColor = color(R.color.CP)
                setOkColor(color(R.color.mrvPopupButtons))
                setCancelColor(color(R.color.mrvPopupButtons))
                show(
                    c.supportFragmentManager,
                    "$tagEdit${allPos(c.m, h.layoutPosition)}"
                )
            } else {
                val jal = Jalali(cal)
                JalaliDatePickerDialog.newInstance(this, jal.Y, jal.M, jal.D).apply {
                    isThemeDark = night
                    show(
                        c.supportFragmentManager,
                        "$tagEdit${allPos(c.m, h.layoutPosition)}"
                    )
                }
            }
        }
        h.b.ampm.text =
            c.resources.getText(if (cal[Calendar.HOUR_OF_DAY] > 12) R.string.PM else R.string.AM)

        // Name
        h.b.name.setText(c.m.visOnani.value!![i].name)
        var crushes = arrayListOf<String>()
        if (c.m.summary.value != null)
            crushes = ArrayList(c.m.summary.value!!.scores.keys)
        h.b.name.setAdapter(
            ArrayAdapter(c, android.R.layout.simple_dropdown_item_1line, crushes)
        )
        h.b.name.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, r: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, r: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) {
                c.m.visOnani.value!![h.layoutPosition].apply {
                    if (name != h.b.name.text.toString()) {
                        name = h.b.name.text.toString()
                        update(c.m, this, h.layoutPosition)
                    }
                }
            }
        })

        // Type
        h.b.type.setSelection(c.m.visOnani.value!![i].type.toInt(), true)
        h.b.type.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
            override fun onItemSelected(a: AdapterView<*>?, v: View?, i: Int, l: Long) {
                c.m.visOnani.value!![h.layoutPosition].apply {
                    if (type != i.toByte()) {
                        type = i.toByte()
                        update(c.m, this, h.layoutPosition)
                    }
                }
            }
        }

        // Descriptions
        h.b.desc.setText(c.m.visOnani.value!![i].desc)
        h.b.root.setOnClickListener {
            Fun.vis(h.b.desc, h.b.desc.visibility == View.GONE)
        }
        h.b.desc.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, r: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, r: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) {
                c.m.visOnani.value!![h.layoutPosition].apply {
                    if (desc != h.b.desc.text.toString()) {
                        desc = h.b.desc.text.toString()
                        update(c.m, this, h.layoutPosition)
                    }
                }
            }
        })

        // Long Click
        val longClick = View.OnLongClickListener { v ->
            var popup = PopupMenu(ContextThemeWrapper(c, R.style.AppTheme), v)
            popup.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.lcAccurate -> {
                        c.m.visOnani.value!![h.layoutPosition].apply {
                            if (acur != !it.isChecked) {
                                acur = !it.isChecked
                                update(c.m, this, h.layoutPosition)
                            }
                        }
                        true
                    }
                    R.id.lcDelete -> {
                        if (c.m.onani.value == null) return@setOnMenuItemClickListener true
                        val aPos = allPos(c.m, h.layoutPosition)
                        Work(Work.DELETE_ONE, listOf(c.m.onani.value!![aPos], aPos)).start()
                        true
                    }
                    else -> false
                }
            }
            popup.inflate(R.menu.report)
            popup.show()
            popup.menu.forEach {
                it.title = SpannableString(it.title).apply {
                    setSpan(
                        CustomTypefaceSpan("", font1, Fun.dm.density * 16f), 0,
                        length, SpannableString.SPAN_INCLUSIVE_INCLUSIVE
                    )
                }
            }
            popup.menu.findItem(R.id.lcAccurate).isChecked =
                c.m.visOnani.value!![h.layoutPosition].acur
            true
        }
        h.b.root.setOnLongClickListener(longClick)
        h.b.clock.setOnLongClickListener(longClick)
        h.b.date.setOnLongClickListener(longClick)
        h.b.name.setOnLongClickListener(longClick)
    }

    override fun getItemCount() = c.m.visOnani.value!!.size

    override fun onDateSet(view: DatePickerDialog, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        if (c.m.onani.value == null || view.tag == null || view.tag!!.length <= 4) return
        val pos = view.tag!!.substring(4).toInt()
        if (c.m.onani.value!!.size > pos) when (view.tag!!.substring(0, 4)) {
            tagEdit -> {
                var calc = Calendar.getInstance()
                calc.timeInMillis = c.m.onani.value!![pos].time
                calc[Calendar.YEAR] = year
                calc[Calendar.MONTH] = monthOfYear
                calc[Calendar.DAY_OF_MONTH] = dayOfMonth
                c.m.onani.value!![pos].time = calc.timeInMillis
                Work(Work.UPDATE_ONE, listOf(c.m.onani.value!![pos], pos, 0)).start()
            }
        }
    }

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
                Work(Work.UPDATE_ONE, listOf(c.m.onani.value!![pos], pos, 0)).start()
            }
        }
    }

    // Repaired version of https://github.com/mohamad-amin/PersianMaterialDateTimePicker
    override fun onDateSet(
        view: JalaliDatePickerDialog, year: Int, monthOfYear: Int, dayOfMonth: Int
    ) {
        if (c.m.onani.value == null || view.tag == null || view.tag!!.length <= 4) return
        val pos = view.tag!!.substring(4).toInt()
        if (c.m.onani.value!!.size > pos) when (view.tag!!.substring(0, 4)) {
            tagEdit -> {
                PersianCalendar().apply {
                    timeInMillis = c.m.onani.value!![pos].time
                    setPersianDate(year, monthOfYear, dayOfMonth)
                    c.m.onani.value!![pos].time = timeInMillis
                }
                Work(Work.UPDATE_ONE, listOf(c.m.onani.value!![pos], pos, 0)).start()
            }
        }
    }

    fun update(m: Model, updated: Report, nominalPos: Int) {
        if (m.onani.value == null) return
        val pos = allPos(m, nominalPos)
        if (m.onani.value!!.size <= pos || pos < 0) return
        m.onani.value!![pos] = updated
        Work(Work.UPDATE_ONE, listOf(m.onani.value!![pos], pos, 1)).start()
    }

    companion object {
        fun compileDate(time: Long): String {
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

        fun allPos(m: Model, pos: Int): Int {
            var index = -1
            for (o in m.onani.value!!.indices)
                if (m.onani.value!![o].id == m.visOnani.value!![pos].id)
                    index = o
            return index
        }


        class Sort : Comparator<Report> {
            override fun compare(a: Report, b: Report) = a.time.compareTo(b.time)
        }
    }
}
