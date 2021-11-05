package ir.mahdiparastesh.sexbook.adap

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import ir.mahdiparastesh.sexbook.Fun.Companion.color
import ir.mahdiparastesh.sexbook.Fun.Companion.dm
import ir.mahdiparastesh.sexbook.Fun.Companion.dp
import ir.mahdiparastesh.sexbook.Fun.Companion.night
import ir.mahdiparastesh.sexbook.Main
import ir.mahdiparastesh.sexbook.Main.Companion.handler
import ir.mahdiparastesh.sexbook.Main.Companion.saveOnBlur
import ir.mahdiparastesh.sexbook.Main.Companion.scrollOnFocus
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.data.Report
import ir.mahdiparastesh.sexbook.data.Work
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
import java.util.*

class ReportAdap(
    val c: Context,
    val list: List<Report>,
    val that: AppCompatActivity,
    val allMasturbation: ArrayList<Report>?
) : RecyclerView.Adapter<ReportAdap.MyViewHolder>(), DatePickerDialog.OnDateSetListener,
    TimePickerDialog.OnTimeSetListener {
    var clockHeight = dp(48)
    val tagEdit = "edit"

    class MyViewHolder(val l: ConstraintLayout) : RecyclerView.ViewHolder(l)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        var l = LayoutInflater.from(parent.context)
            .inflate(R.layout.report, parent, false) as ConstraintLayout
        val clock = l.getChildAt(clockPos) as ConstraintLayout
        val clockHour = clock.getChildAt(clockHourPos) as View
        val clockMin = clock.getChildAt(clockMinPos) as View
        val point = clock.getChildAt(pointPos) as View
        val ampm = l.getChildAt(ampmPos) as TextView
        val date = l.getChildAt(datePos) as TextView
        val notes = l.getChildAt(notesPos) as EditText

        // IDs
        clock.id = View.generateViewId()
        clockHour.id = View.generateViewId()
        clockMin.id = View.generateViewId()
        point.id = View.generateViewId()
        date.id = View.generateViewId()
        notes.id = View.generateViewId()

        // Fix Constraints
        var clockHourLP = clockHour.layoutParams as ConstraintLayout.LayoutParams
        clockHourLP.bottomToBottom = point.id
        clockHour.layoutParams = clockHourLP
        var clockMinLP = clockMin.layoutParams as ConstraintLayout.LayoutParams
        clockMinLP.bottomToBottom = point.id
        clockMin.layoutParams = clockMinLP
        var dateLP = date.layoutParams as ConstraintLayout.LayoutParams
        dateLP.startToEnd = clock.id
        dateLP.topToTop = clock.id
        dateLP.bottomToBottom = clock.id
        date.layoutParams = dateLP
        var ampmLP = ampm.layoutParams as ConstraintLayout.LayoutParams
        ampmLP.startToEnd = clock.id
        ampmLP.bottomToBottom = clock.id
        ampm.layoutParams = ampmLP
        var notesLP = notes.layoutParams as ConstraintLayout.LayoutParams
        notesLP.topToBottom = clock.id
        notes.layoutParams = notesLP

        // Background
        l.background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            colors = intArrayOf(color(R.color.mrvBG1), color(R.color.mrvBG2), color(R.color.mrvBG3))
            cornerRadius = dm.density * 12
        }

        // Fonts
        if (Main.dateFont != null) date.setTypeface(Main.dateFont, Typeface.BOLD)

        // Date & Time
        if (clock.height != 0) clockHeight = clock.height
        val pointHeight = clockHeight * perh(point)
        val hourHeight = clockHeight * perh(clockHour)
        val minuteHeight = clockHeight * perh(clockMin)
        clockHour.apply {
            pivotX = (clockHeight * perw(clockHour)) / 2f
            pivotY = hourHeight - (pointHeight / 2f)
        }
        clockMin.apply {
            pivotX = (clockHeight * perw(clockMin)) / 2f
            pivotY = minuteHeight - (pointHeight / 2f)
        }

        saveOnBlur = true
        scrollOnFocus = true
        return MyViewHolder(l)
    }

    override fun onBindViewHolder(h: MyViewHolder, i: Int) {
        val clock = h.l.getChildAt(clockPos) as ConstraintLayout
        val clockHour = clock.getChildAt(clockHourPos) as View
        val clockMin = clock.getChildAt(clockMinPos) as View
        //val point = clock.getChildAt(pointPos) as View
        val ampm = h.l.getChildAt(ampmPos) as TextView
        val date = h.l.getChildAt(datePos) as TextView
        val notes = h.l.getChildAt(notesPos) as EditText

        // Date & Time
        var cal = Calendar.getInstance()
        cal.timeInMillis = list[i].time
        clockHour.rotation = rotateHour(cal[Calendar.HOUR_OF_DAY])
        clockMin.rotation = rotateMin(cal[Calendar.MINUTE])
        date.text = compileDate(c, list[i].time)
        clock.setOnClickListener {
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
        date.setOnClickListener {
            DatePickerDialog.newInstance(
                this, cal[Calendar.YEAR], cal[Calendar.MONTH], cal[Calendar.DAY_OF_MONTH]
            ).apply {
                isThemeDark = night
                version = DatePickerDialog.Version.VERSION_2
                accentColor = color(R.color.CP)
                setOkColor(color(R.color.mrvPopupButtons))
                setCancelColor(color(R.color.mrvPopupButtons))
                show(that.supportFragmentManager, "$tagEdit${allPos(h, list, allMasturbation)}")
            }
        }
        ampm.text =
            c.resources.getText(if (cal[Calendar.HOUR_OF_DAY] > 12) R.string.PM else R.string.AM)

        // Notes
        notes.setText(list[i].name)
        notes.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                saveET(c, notes, allPos(h, list, allMasturbation), allMasturbation)
            }
        })
        /*notes.setOnFocusChangeListener { _, b ->
            if (!b && saveOnBlur && allMasturbation != null) saveET(
                c, notes, allPos(h, list, allMasturbation), allMasturbation
            )
            if (b && scrollOnFocus) handler.obtainMessage(Work.SCROLL, h.l.top - dp(5))
        }*/

        // Long Click
        val longClick = View.OnLongClickListener { p0 ->
            var popup = PopupMenu(c, p0)
            popup.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.lcDelete -> {
                        if (allMasturbation == null) return@setOnMenuItemClickListener true
                        val aPos = allPos(h, list, allMasturbation)
                        Work(
                            c, handler, Work.DELETE_ONE, listOf(allMasturbation[aPos], aPos)
                        ).start()
                        true
                    }
                    else -> false
                }
            }
            popup.inflate(R.menu.longclick)
            popup.show()
            true
        }
        h.l.setOnLongClickListener(longClick)
        clock.setOnLongClickListener(longClick)
        date.setOnLongClickListener(longClick)
        notes.setOnLongClickListener(longClick)
    }

    override fun getItemCount() = list.size

    override fun onDateSet(view: DatePickerDialog?, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        if (allMasturbation == null) return
        if (view != null && view.tag != null && view.tag!!.length > 4) {
            val pos = view.tag!!.substring(4).toInt()
            if (allMasturbation.size > pos) when (view.tag!!.substring(0, 4)) {
                tagEdit -> {
                    var calc = Calendar.getInstance()
                    calc.timeInMillis = allMasturbation[pos].time
                    calc[Calendar.YEAR] = year
                    calc[Calendar.MONTH] = monthOfYear
                    calc[Calendar.DAY_OF_MONTH] = dayOfMonth
                    allMasturbation[pos].time = calc.timeInMillis
                    Work(
                        c, handler, Work.UPDATE_ONE, listOf(allMasturbation[pos], pos, 0)
                    ).start()
                }
            }
        }
    }

    override fun onTimeSet(view: TimePickerDialog?, hourOfDay: Int, minute: Int, second: Int) {
        if (allMasturbation == null) return
        if (view != null && view.tag != null && view.tag!!.length > 4) {
            val pos = view.tag!!.substring(4).toInt()
            if (allMasturbation.size > pos) when (view.tag!!.substring(0, 4)) {
                tagEdit -> {
                    var calc = Calendar.getInstance()
                    calc.timeInMillis = allMasturbation[pos].time
                    calc[Calendar.HOUR_OF_DAY] = hourOfDay
                    calc[Calendar.MINUTE] = minute
                    calc[Calendar.SECOND] = second
                    allMasturbation[pos].time = calc.timeInMillis
                    Work(
                        c, handler, Work.UPDATE_ONE, listOf(allMasturbation[pos], pos, 0)
                    ).start()
                }
            }
        }
    }

    companion object {
        const val clockPos = 0
        const val clockHourPos = 0
        const val clockMinPos = 1
        const val pointPos = 2
        const val ampmPos = 1
        const val datePos = 2
        const val notesPos = 3

        fun compileDate(c: Context, time: Long): String {
            val lm = Calendar.getInstance()
            lm.timeInMillis = time
            return "${c.resources.getStringArray(R.array.months)[lm.get(Calendar.MONTH)]} " +
                    "${lm.get(Calendar.DAY_OF_MONTH)}"
        }

        fun perw(v: View) =
            (v.layoutParams as ConstraintLayout.LayoutParams).matchConstraintPercentWidth

        fun perh(v: View) =
            (v.layoutParams as ConstraintLayout.LayoutParams).matchConstraintPercentHeight

        fun rotateHour(h: Int) = (h - (if (h > 12) 12 else 0)) * 30f

        fun rotateMin(m: Int) = m * 6f

        fun saveET(c: Context, et: EditText, pos: Int, allMasturbation: ArrayList<Report>?) {
            if (allMasturbation == null) return
            if (allMasturbation.size <= pos || pos < 0) return
            if (allMasturbation[pos].name == et.text.toString()) return
            allMasturbation[pos].name = et.text.toString()
            Work(
                c, handler, Work.UPDATE_ONE,
                listOf(allMasturbation[pos], pos, 1)
            ).start()
        }

        fun allPos(
            h: RecyclerView.ViewHolder, list: List<Report>, allMasturbation: ArrayList<Report>?
        ) = allMasturbation!!.indexOf(list[h.layoutPosition])

        fun allPos(list: List<Report>, nominalPos: Int, allMasturbation: ArrayList<Report>?) =
            allMasturbation!!.indexOf(list[nominalPos])


        class Sort : Comparator<Report> {
            override fun compare(a: Report, b: Report) = a.time.compareTo(b.time)
        }
    }
}