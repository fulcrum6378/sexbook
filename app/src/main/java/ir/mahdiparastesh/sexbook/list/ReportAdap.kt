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
import ir.mahdiparastesh.sexbook.Fun.Companion.night
import ir.mahdiparastesh.sexbook.Main
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

class ReportAdap(
    val list: List<Report>,
    val that: Main,
    val allMasturbation: ArrayList<Report>? = that.m.onani.value
) : RecyclerView.Adapter<ReportAdap.MyViewHolder>(),
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
        b.ampm.typeface = Fun.font1

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
        cal.timeInMillis = list[i].time
        h.b.clockHour.rotation = rotateHour(cal[Calendar.HOUR_OF_DAY])
        h.b.clockMin.rotation = rotateMin(cal[Calendar.MINUTE])
        h.b.date.text = compileDate(list[i].time)
        h.b.clock.setOnClickListener {
            TimePickerDialog.newInstance(
                this, cal[Calendar.HOUR_OF_DAY], cal[Calendar.MINUTE], false
            ).apply {
                isThemeDark = night
                version = TimePickerDialog.Version.VERSION_2
                accentColor = color(R.color.CP)
                setOkColor(color(R.color.mrvPopupButtons))
                setCancelColor(color(R.color.mrvPopupButtons))
                show(that.supportFragmentManager, "edit${allPos(h, list, allMasturbation)}")
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
                show(that.supportFragmentManager, "$tagEdit${allPos(h, list, allMasturbation)}")
            } else {
                val jal = Jalali(cal)
                JalaliDatePickerDialog.newInstance(this, jal.Y, jal.M, jal.D).apply {
                    isThemeDark = night
                    show(that.supportFragmentManager, "$tagEdit${allPos(h, list, allMasturbation)}")
                }
            }
        }
        h.b.ampm.text =
            c.resources.getText(if (cal[Calendar.HOUR_OF_DAY] > 12) R.string.PM else R.string.AM)

        // Name
        h.b.name.setText(list[i].name)
        var crushes = arrayListOf<String>()
        if (that.m.summary.value != null)
            crushes = ArrayList(that.m.summary.value!!.scores.keys)
        h.b.name.setAdapter(
            ArrayAdapter(that, android.R.layout.simple_dropdown_item_1line, crushes)
        )
        h.b.name.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, r: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, r: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) {
                saveET(h.b.name, allPos(h, list, allMasturbation), allMasturbation)
            }
        })

        // Type
        h.b.type.setSelection(list[i].type.toInt(), true)
        h.b.type.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
            override fun onItemSelected(a: AdapterView<*>?, v: View?, i: Int, l: Long) {
                if (allMasturbation == null) return
                val pos = allPos(h, list, allMasturbation)
                if (allMasturbation.size <= pos || pos < 0) return
                if (allMasturbation[pos].type == i.toByte()) return
                allMasturbation[pos].type = i.toByte()
                Work(Work.UPDATE_ONE, listOf(allMasturbation[pos], pos, 1)).start()
            }
        }

        // Long Click
        val longClick = View.OnLongClickListener { v ->
            var popup = PopupMenu(ContextThemeWrapper(c, R.style.AppTheme), v)
            popup.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.lcDescriptions -> {
                        // TODO
                        true
                    }
                    R.id.lcDelete -> {
                        if (allMasturbation == null) return@setOnMenuItemClickListener true
                        val aPos = allPos(h, list, allMasturbation)
                        Work(Work.DELETE_ONE, listOf(allMasturbation[aPos], aPos)).start()
                        true
                    }
                    // TODO: Estimation
                    else -> false
                }
            }
            popup.inflate(R.menu.sex_longclick)
            popup.show()
            popup.menu.forEach {
                val mNewTitle = SpannableString(it.title)
                mNewTitle.setSpan(
                    CustomTypefaceSpan("", Fun.font1, Fun.dm.density * 16f), 0,
                    mNewTitle.length, SpannableString.SPAN_INCLUSIVE_INCLUSIVE
                )
                it.title = mNewTitle
            }
            true
        }
        h.b.root.setOnLongClickListener(longClick)
        h.b.clock.setOnLongClickListener(longClick)
        h.b.date.setOnLongClickListener(longClick)
        h.b.name.setOnLongClickListener(longClick)
    }

    override fun getItemCount() = list.size

    override fun onDateSet(view: DatePickerDialog, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        if (allMasturbation == null || view.tag == null || view.tag!!.length <= 4) return
        val pos = view.tag!!.substring(4).toInt()
        if (allMasturbation.size > pos) when (view.tag!!.substring(0, 4)) {
            tagEdit -> {
                var calc = Calendar.getInstance()
                calc.timeInMillis = allMasturbation[pos].time
                calc[Calendar.YEAR] = year
                calc[Calendar.MONTH] = monthOfYear
                calc[Calendar.DAY_OF_MONTH] = dayOfMonth
                allMasturbation[pos].time = calc.timeInMillis
                Work(Work.UPDATE_ONE, listOf(allMasturbation[pos], pos, 0)).start()
            }
        }
    }

    override fun onTimeSet(view: TimePickerDialog, hourOfDay: Int, minute: Int, second: Int) {
        if (allMasturbation == null || view.tag == null || view.tag!!.length <= 4) return
        val pos = view.tag!!.substring(4).toInt()
        if (allMasturbation.size > pos) when (view.tag!!.substring(0, 4)) {
            tagEdit -> {
                var calc = Calendar.getInstance()
                calc.timeInMillis = allMasturbation[pos].time
                calc[Calendar.HOUR_OF_DAY] = hourOfDay
                calc[Calendar.MINUTE] = minute
                calc[Calendar.SECOND] = second
                allMasturbation[pos].time = calc.timeInMillis
                Work(Work.UPDATE_ONE, listOf(allMasturbation[pos], pos, 0)).start()
            }
        }
    }

    // Repaired version of https://github.com/mohamad-amin/PersianMaterialDateTimePicker
    override fun onDateSet(
        view: JalaliDatePickerDialog, year: Int, monthOfYear: Int, dayOfMonth: Int
    ) {
        if (allMasturbation == null || view.tag == null || view.tag!!.length <= 4) return
        val pos = view.tag!!.substring(4).toInt()
        if (allMasturbation.size > pos) when (view.tag!!.substring(0, 4)) {
            tagEdit -> {
                PersianCalendar().apply {
                    setPersianDate(year, monthOfYear, dayOfMonth)
                    allMasturbation[pos].time = timeInMillis
                }
                Work(Work.UPDATE_ONE, listOf(allMasturbation[pos], pos, 0)).start()
            }
        }
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

        fun saveET(et: EditText, pos: Int, allMasturbation: ArrayList<Report>?) {
            if (allMasturbation == null) return
            if (allMasturbation.size <= pos || pos < 0) return
            if (allMasturbation[pos].name == et.text.toString()) return
            allMasturbation[pos].name = et.text.toString()
            Work(Work.UPDATE_ONE, listOf(allMasturbation[pos], pos, 1)).start()
        }

        fun allPos(
            h: RecyclerView.ViewHolder, list: List<Report>, allMasturbation: ArrayList<Report>?
        ) = allMasturbation!!.indexOf(list[h.layoutPosition])


        class Sort : Comparator<Report> {
            override fun compare(a: Report, b: Report) = a.time.compareTo(b.time)
        }
    }
}
