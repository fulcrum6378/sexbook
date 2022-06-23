package ir.mahdiparastesh.sexbook.stat

import android.icu.util.Calendar
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import ir.mahdiparastesh.sexbook.Fun.calendar
import ir.mahdiparastesh.sexbook.Fun.defaultOptions
import ir.mahdiparastesh.sexbook.Fun.fullDate
import ir.mahdiparastesh.sexbook.PageLove
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.Settings
import ir.mahdiparastesh.sexbook.data.Crush
import ir.mahdiparastesh.sexbook.data.Report
import ir.mahdiparastesh.sexbook.data.Work
import ir.mahdiparastesh.sexbook.databinding.IdentifyBinding
import ir.mahdiparastesh.sexbook.databinding.SingularBinding
import ir.mahdiparastesh.sexbook.mdtp.Utils
import ir.mahdiparastesh.sexbook.mdtp.date.DatePickerDialog
import ir.mahdiparastesh.sexbook.more.BaseActivity
import lecho.lib.hellocharts.model.Column
import lecho.lib.hellocharts.model.ColumnChartData
import lecho.lib.hellocharts.model.SubcolumnValue

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
        sinceTheBeginning(this, m.onani.value!!)
            .forEach { data.add(Pair(it, calcHistory(this, history, it))) }

        // Handler
        handler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    Work.C_VIEW_ONE -> crush = msg.obj as Crush?
                    Work.C_INSERT_ONE, Work.C_UPDATE_ONE, Work.C_DELETE_ONE -> {
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
        b.identify.setOnClickListener { identify(this, crush, handler) }

        // TODO: Implement Mass Rename
    }

    override fun onDestroy() {
        super.onDestroy()
        handler = null
    }

    companion object {
        var handler: Handler? = null

        fun sinceTheBeginning(c: BaseActivity, mOnani: ArrayList<Report>): List<String> {
            val now = c.calType().newInstance()
            var oldest = now.timeInMillis
            if (c.sp.getBoolean(Settings.spStatSinceCb, false)) {
                val statSinc = c.sp.getLong(Settings.spStatSince, 0L)
                for (h in mOnani) if (h.time in statSinc until oldest) oldest = h.time
            } else for (h in mOnani) if (h.time < oldest) oldest = h.time
            val symbols = Utils.localSymbols(c.c, c.calType())

            val beg = oldest.calendar(c)
            val list = arrayListOf<String>()
            val yDist = now[Calendar.YEAR] - beg[Calendar.YEAR]
            for (y in 0 until (yDist + 1)) {
                var start = 0
                var end = 11
                if (y == 0) start = beg[Calendar.MONTH]
                if (y == yDist) end = now[Calendar.MONTH]
                for (m in start..end) list.add(
                    "${symbols.shortMonths[m]} ${beg[Calendar.YEAR] + y}"
                )
            }
            return list
        }

        fun calcHistory(
            c: BaseActivity, list: ArrayList<Summary.Erection>, month: String,
            growing: Boolean = false
        ): Float {
            var value = 0f
            val split = month.split(" ")
            val months = Utils.localSymbols(c, c.calType()).shortMonths
            for (i in list) {
                var lm = i.time.calendar(c)
                var yea: Int = lm[Calendar.YEAR]
                var mon: Int = lm[Calendar.MONTH]
                if (months.indexOf(split[0]) == mon && split[1].toInt() == yea) value += i.value
                if (growing && (split[1].toInt() > yea ||
                            (split[1].toInt() == yea && months.indexOf(split[0]) > mon))
                ) value += i.value
            }
            return value
        }

        fun identify(c: BaseActivity, crush: Crush?, handler: Handler? = null) {
            val bi = IdentifyBinding.inflate(c.layoutInflater, null, false)
            AppCompatResources.getColorStateList(c, R.color.chip_normal).also {
                bi.masc.trackTintList = it
                bi.notifyBirth.trackTintList = it
            }

            // Default Values
            var isBirthSet = false
            val bir = Calendar.getInstance()
            if (crush != null) {
                bi.fName.setText(crush.fName)
                bi.mName.setText(crush.mName)
                bi.lName.setText(crush.lName)
                bi.masc.isChecked = crush.masc
                if (crush.height != -1f)
                    bi.height.setText(crush.height.toString())
                crush.bYear.toInt().let { if (it != -1) bir[Calendar.YEAR] = it }
                crush.bMonth.toInt().let { if (it != -1) bir[Calendar.MONTH] = it }
                crush.bDay.toInt().let { if (it != -1) bir[Calendar.DAY_OF_MONTH] = it }
                if (crush.hasFullBirth()) {
                    bi.birth.text = bir.fullDate()
                    isBirthSet = true
                }
                bi.location.setText(crush.locat)
                bi.instagram.setText(crush.insta)
                bi.notifyBirth.isChecked = crush.notifyBirth
            } else {
                bi.masc.isChecked = c.sp.getBoolean(Settings.spPrefersMasculine, false)
            }

            // Masculine
            bi.masc.setOnCheckedChangeListener { _, isChecked ->
                c.sp.edit().putBoolean(Settings.spPrefersMasculine, isChecked).apply()
            }

            // Birth
            bi.birth.setOnClickListener {
                DatePickerDialog.newInstance({ _, time ->
                    isBirthSet = true
                    bir.timeInMillis = time
                    bi.birth.text = bir.fullDate()
                }, bir).defaultOptions(c).show(c.supportFragmentManager, "birth")
            }

            AlertDialog.Builder(c).apply {
                setTitle("${c.getString(R.string.identify)}: ${crush?.key ?: c.m.crush}")
                setView(bi.root)
                setPositiveButton(R.string.save) { _, _ ->
                    val inserted = Crush(
                        c.m.crush!!,
                        bi.fName.text.toString().ifEmpty { null },
                        bi.mName.text.toString().ifEmpty { null },
                        bi.lName.text.toString().ifEmpty { null },
                        bi.masc.isChecked,
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
                setNeutralButton(R.string.clear) { ad1, _ ->
                    if (crush == null) return@setNeutralButton
                    AlertDialog.Builder(c).apply {
                        setTitle(c.getString(R.string.crushClear, crush.key))
                        setMessage(R.string.crushClearSure)
                        setPositiveButton(R.string.yes) { _, _ ->
                            Work(c, Work.C_DELETE_ONE, listOf(crush), handler).start()
                            ad1.dismiss()
                        }
                        setNegativeButton(R.string.no, null)
                    }.show()
                }
                setCancelable(true)
            }.show()
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
