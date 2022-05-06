package ir.mahdiparastesh.sexbook.stat

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.get
import ir.mahdiparastesh.sexbook.Fun
import ir.mahdiparastesh.sexbook.Fun.Companion.calendar
import ir.mahdiparastesh.sexbook.Fun.Companion.fullDate
import ir.mahdiparastesh.sexbook.PageLove
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.Settings
import ir.mahdiparastesh.sexbook.data.Crush
import ir.mahdiparastesh.sexbook.data.Report
import ir.mahdiparastesh.sexbook.data.Work
import ir.mahdiparastesh.sexbook.databinding.IdentifyBinding
import ir.mahdiparastesh.sexbook.databinding.SingularBinding
import ir.mahdiparastesh.sexbook.more.BaseActivity
import ir.mahdiparastesh.sexbook.more.Jalali
import ir.mahdiparastesh.sexbook.more.LocalDatePicker
import lecho.lib.hellocharts.model.Column
import lecho.lib.hellocharts.model.ColumnChartData
import lecho.lib.hellocharts.model.SubcolumnValue
import java.util.*

@Suppress("UNCHECKED_CAST")
class Singular : BaseActivity() {
    private lateinit var b: SingularBinding
    private var crush: Crush? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = SingularBinding.inflate(layoutInflater)
        setContentView(b.root)

        if (m.onani.value == null || m.summary.value == null || m.crush == null) {
            onBackPressed(); return; }
        val data = ArrayList<Pair<String, Float>>()
        val history = m.summary.value!!.scores[m.crush]
        if (history == null) {
            onBackPressed(); return; }
        sinceTheBeginning(c, m.onani.value!!)
            .forEach { data.add(Pair(it, calcHistory(c, history, it))) }

        // Handler
        handler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    Work.C_VIEW_ONE -> crush = msg.obj as Crush?
                    Work.C_INSERT_ONE, Work.C_UPDATE_ONE -> {
                        PageLove.changed = true
                        Work(c, Work.C_VIEW_ONE, listOf(m.crush!!), handler).start()
                    }
                }
            }
        }

        b.main.columnChartData = ColumnChartData().setColumns(ColumnFactory(this, data))

        // Night Mode
        if (night()) {
            window.decorView.setBackgroundColor(color(R.color.CP))
            b.identifyIV.colorFilter = pdcf(R.color.CP)
        }

        // Identification
        Work(c, Work.C_VIEW_ONE, listOf(m.crush!!), handler).start()
        b.identify.setOnClickListener {
            val bi = IdentifyBinding.inflate(layoutInflater, null, false)

            // Fonts
            for (l in 0 until bi.ll.childCount)
                if (bi.ll[l] is TextView)
                    (bi.ll[l] as TextView).typeface = font1

            // Default Values
            var isBirthSet = false
            val bir = Calendar.getInstance()
            if (crush != null) {
                bi.fName.setText(crush!!.fName)
                bi.lName.setText(crush!!.lName)
                bi.masc.isChecked = crush!!.masc
                bi.real.isChecked = crush!!.real
                if (crush!!.height != -1f)
                    bi.height.setText(crush!!.height.toString())
                crush!!.bYear.toInt().let { if (it != -1) bir[Calendar.YEAR] = it }
                crush!!.bMonth.toInt().let { if (it != -1) bir[Calendar.MONTH] = it }
                crush!!.bDay.toInt().let { if (it != -1) bir[Calendar.DAY_OF_MONTH] = it }
                if (crush!!.hasFullBirth()) {
                    bi.birth.text = bir.fullDate()
                    isBirthSet = true
                }
                bi.location.setText(crush!!.locat)
                bi.instagram.setText(crush!!.insta)
                bi.notifyBirth.isChecked = crush!!.notifyBirth
            }

            // Birth
            bi.birth.setOnClickListener {
                LocalDatePicker(this@Singular, "birth", bir) { _, time ->
                    isBirthSet = true
                    bir.timeInMillis = time
                    bi.birth.text = bir.fullDate()
                }
            }

            AlertDialog.Builder(this).apply {
                setTitle("${resources.getString(R.string.identify)}: ${m.crush}")
                setView(bi.root)
                setPositiveButton(R.string.save) { _, _ ->
                    val inserted = Crush(
                        m.crush!!,
                        bi.fName.text.toString().ifEmpty { null },
                        bi.lName.text.toString().ifEmpty { null },
                        bi.masc.isChecked,
                        bi.real.isChecked,
                        if (bi.height.text.toString() != "")
                            bi.height.text.toString().toFloat() else -1f,
                        if (isBirthSet) bir[Calendar.YEAR].toShort() else -1,
                        if (isBirthSet) bir[Calendar.MONTH].toByte() else -1,
                        if (isBirthSet) bir[Calendar.DAY_OF_MONTH].toByte() else -1,
                        bi.location.text.toString().ifEmpty { null },
                        bi.instagram.text.toString().ifEmpty { null },
                        bi.notifyBirth.isChecked
                    )
                    Work(
                        c, if (crush == null) Work.C_INSERT_ONE else Work.C_UPDATE_ONE,
                        listOf(inserted), handler
                    ).start()
                }
                setNegativeButton(R.string.discard, null)
                setCancelable(true)
            }.create().apply {
                show()
                fixADButton(getButton(AlertDialog.BUTTON_POSITIVE))
                fixADButton(getButton(AlertDialog.BUTTON_NEGATIVE))
            }
        }

        // TODO: Implement Mass Rename
    }

    override fun onDestroy() {
        super.onDestroy()
        handler = null
    }

    companion object {
        var handler: Handler? = null

        fun sinceTheBeginning(c: Context, mOnani: ArrayList<Report>): List<String> {
            val now = Calendar.getInstance()
            var oldest = now.timeInMillis
            if (sp.getBoolean(Settings.spStatSinceCb, false)) {
                val statSinc = sp.getLong(Settings.spStatSince, 0L)
                for (h in mOnani) if (h.time in statSinc until oldest) oldest = h.time
            } else for (h in mOnani) if (h.time < oldest) oldest = h.time
            val beg = oldest.calendar()
            val list = arrayListOf<String>()
            if (Fun.calType() != Fun.CalendarType.JALALI) {
                val yDist = now[Calendar.YEAR] - beg[Calendar.YEAR]
                for (y in 0 until (yDist + 1)) {
                    var start = 0
                    var end = 11
                    if (y == 0) start = beg[Calendar.MONTH]
                    if (y == yDist) end = now[Calendar.MONTH]
                    for (m in start..end) list.add(
                        "${c.resources.getStringArray(R.array.months)[m]} ${beg[Calendar.YEAR] + y}"
                    )
                }
            } else {
                val jBeg = Jalali(beg)
                val jNow = Jalali(now)
                val yDist = jNow.Y - jBeg.Y
                for (y in 0 until (yDist + 1)) {
                    var start = 0
                    var end = 11
                    if (y == 0) start = jBeg.M
                    if (y == yDist) end = jNow.M
                    for (m in start..end) list.add(
                        "${c.resources.getStringArray(R.array.jMonths)[m]} ${jBeg.Y + y}"
                    )
                }
            }
            return list
        }

        fun calcHistory(
            c: Context, list: ArrayList<Summary.Erection>, month: String, growing: Boolean = false
        ): Float {
            var value = 0f
            val split = month.split(" ")
            val months = c.resources.getStringArray(
                when (Fun.calType()) {
                    Fun.CalendarType.JALALI -> R.array.jMonths
                    else -> R.array.months
                }
            )
            for (i in list) {
                var lm = i.time.calendar()
                var yea: Int
                var mon: Int
                if (Fun.calType() == Fun.CalendarType.JALALI) Jalali(lm).apply {
                    yea = Y
                    mon = M
                } else {
                    yea = lm[Calendar.YEAR]
                    mon = lm[Calendar.MONTH]
                }
                if (months.indexOf(split[0]) == mon && split[1].toInt() == yea) value += i.value
                if (growing && (split[1].toInt() > yea ||
                            (split[1].toInt() == yea && months.indexOf(split[0]) > mon))
                ) value += i.value
            }
            return value
        }
    }

    class ColumnFactory(c: Singular, list: ArrayList<Pair<String, Float>>) :
        ArrayList<Column>(list.map {
            Column(
                listOf(
                    SubcolumnValue(it.second)
                        .setLabel("${it.first} (${it.second})")
                        .setColor(c.color(if (!c.night()) R.color.CP else R.color.CPD))
                )
            ).setHasLabels(true)
        })
}
