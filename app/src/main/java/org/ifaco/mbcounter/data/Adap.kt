package org.ifaco.mbcounter.data

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.blure.complexview.ComplexView
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
import org.ifaco.mbcounter.Fun.Companion.color
import org.ifaco.mbcounter.Fun.Companion.dm
import org.ifaco.mbcounter.Fun.Companion.dp
import org.ifaco.mbcounter.Fun.Companion.night
import org.ifaco.mbcounter.Main
import org.ifaco.mbcounter.Main.Companion.handler
import org.ifaco.mbcounter.Main.Companion.saveOnBlur
import org.ifaco.mbcounter.Main.Companion.scrollOnFocus
import org.ifaco.mbcounter.R
import java.util.*

class Adap(
    val c: Context,
    val list: List<Report>,
    val that: AppCompatActivity,
    val allMasturbation: ArrayList<Report>?
) : RecyclerView.Adapter<Adap.MyViewHolder>(), DatePickerDialog.OnDateSetListener,
    TimePickerDialog.OnTimeSetListener {
    var clockHeight = dp(48)
    val tagEdit = "edit"

    class MyViewHolder(val l: ComplexView) : RecyclerView.ViewHolder(l)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        var l = LayoutInflater.from(parent.context)
            .inflate(R.layout.report, parent, false) as ComplexView
        val cl = l.getChildAt(clPos) as ConstraintLayout
        val clock = cl.getChildAt(clockPos) as ConstraintLayout
        val clockHour = clock.getChildAt(clockHourPos) as View
        val clockMin = clock.getChildAt(clockMinPos) as View
        val point = clock.getChildAt(pointPos) as View
        val ampm = cl.getChildAt(ampmPos) as TextView
        val date = cl.getChildAt(datePos) as TextView
        val notes = cl.getChildAt(notesPos) as EditText

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
        cl.background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            colors = intArrayOf(color(R.color.mrvBG1), color(R.color.mrvBG2), color(R.color.mrvBG3))
            cornerRadius = dm.density * 15
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
        val cl = h.l.getChildAt(clPos) as ConstraintLayout
        val clock = cl.getChildAt(clockPos) as ConstraintLayout
        val clockHour = clock.getChildAt(clockHourPos) as View
        val clockMin = clock.getChildAt(clockMinPos) as View
        //val point = clock.getChildAt(pointPos) as View
        val ampm = cl.getChildAt(ampmPos) as TextView
        val date = cl.getChildAt(datePos) as TextView
        val notes = cl.getChildAt(notesPos) as EditText

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
        notes.setText(list[i].notes)
        notes.setOnFocusChangeListener { _, b ->
            if (!b && saveOnBlur && allMasturbation != null) saveET(
                c, notes, allPos(h, list, allMasturbation), allMasturbation
            )
            if (b && scrollOnFocus) handler.obtainMessage(Work.SCROLL, h.l.top - dp(5))
        }

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
        cl.setOnLongClickListener(longClick)
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
                        c, handler, Work.UPDATE_ONE, listOf(allMasturbation[pos], pos, 2)
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
                        c, handler, Work.UPDATE_ONE, listOf(allMasturbation[pos], pos, 2)
                    ).start()
                }
            }
        }
    }

    companion object {
        const val clPos = 0
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

        fun saveET(
            c: Context, et: EditText, pos: Int, allMasturbation: ArrayList<Report>?,
            exitAfterwards: Boolean = false
        ) {
            if (allMasturbation == null) return
            if (allMasturbation.size <= pos || pos < 0) return
            if (allMasturbation[pos].notes == et.text.toString()) return
            allMasturbation[pos].notes = et.text.toString()
            Work(
                c, handler, Work.UPDATE_ONE,
                listOf(allMasturbation[pos], pos, if (exitAfterwards) 1 else 0)
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