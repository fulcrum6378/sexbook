package ir.mahdiparastesh.sexbook.stat

import android.Manifest
import android.content.pm.PackageManager
import android.icu.util.Calendar
import android.icu.util.GregorianCalendar
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import androidx.annotation.RequiresApi
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ir.mahdiparastesh.hellocharts.model.Column
import ir.mahdiparastesh.hellocharts.model.ColumnChartData
import ir.mahdiparastesh.hellocharts.model.SubColumnValue
import ir.mahdiparastesh.mcdtp.McdtpUtils
import ir.mahdiparastesh.mcdtp.date.DatePickerDialog
import ir.mahdiparastesh.sexbook.Fun.calendar
import ir.mahdiparastesh.sexbook.Fun.defaultOptions
import ir.mahdiparastesh.sexbook.Fun.fullDate
import ir.mahdiparastesh.sexbook.Fun.shake
import ir.mahdiparastesh.sexbook.PageLove
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.Settings
import ir.mahdiparastesh.sexbook.data.Crush
import ir.mahdiparastesh.sexbook.data.Report
import ir.mahdiparastesh.sexbook.data.Work
import ir.mahdiparastesh.sexbook.databinding.IdentifyBinding
import ir.mahdiparastesh.sexbook.databinding.SingularBinding
import ir.mahdiparastesh.sexbook.more.BaseActivity

class Singular : BaseActivity() {
    private lateinit var b: SingularBinding
    private var crush: Crush? = null

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = SingularBinding.inflate(layoutInflater)
        setContentView(b.root)

        if (m.onani.value == null || m.summary == null || m.crush == null) {
            onBackPressed(); return; }
        val data = ArrayList<Pair<String, Float>>()
        val history = m.summary!!.scores[m.crush]
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
            window.decorView.setBackgroundColor(themeColor(com.google.android.material.R.attr.colorPrimary))
            b.identifyIV.colorFilter = themePdcf()
        }

        // Identification
        Work(c, Work.C_VIEW_ONE, listOf(m.crush!!), handler).start()
        b.identify.setOnClickListener { identify(this, crush, handler) }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler = null
    }

    companion object {
        var handler: Handler? = null

        fun sinceTheBeginning(c: BaseActivity, mOnani: ArrayList<Report>): List<String> {
            // Find the ending
            var end = c.calType().getDeclaredConstructor().newInstance()
            var oldest = end.timeInMillis
            if (c.sp.getBoolean(Settings.spStatUntilCb, false)) {
                val statTill = c.sp.getLong(Settings.spStatUntil, 0L)
                var newest = 0L
                for (h in mOnani) if (h.time in (newest + 1)..statTill) newest = h.time
                if (newest != 0L) end = newest.calendar(c)
            }

            // Find the beginning
            if (c.sp.getBoolean(Settings.spStatSinceCb, false)) {
                val statSinc = c.sp.getLong(Settings.spStatSince, 0L)
                for (h in mOnani) if (h.time in statSinc until oldest) oldest = h.time
            } else for (h in mOnani) if (h.time < oldest) oldest = h.time

            val beg = oldest.calendar(c)
            val list = arrayListOf<String>()
            val yDist = end[Calendar.YEAR] - beg[Calendar.YEAR]
            for (y in 0 until (yDist + 1)) {
                var start = 0
                var finish = 11
                if (y == 0) start = beg[Calendar.MONTH]
                if (y == yDist) finish = end[Calendar.MONTH]
                for (m in start..finish) list.add(
                    "${McdtpUtils.localSymbols(c.c, c.calType()).shortMonths[m]} " +
                            "${beg[Calendar.YEAR] + y}"
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
            val months = McdtpUtils.localSymbols(c, c.calType()).shortMonths
            for (i in list) {
                val lm = i.time.calendar(c)
                val yea = lm[Calendar.YEAR]
                val mon = lm[Calendar.MONTH]
                if (months.indexOf(split[0]) == mon && split[1].toInt() == yea) value += i.value
                if (growing && (split[1].toInt() > yea ||
                            (split[1].toInt() == yea && months.indexOf(split[0]) > mon))
                ) value += i.value
            }
            return value
        }

        fun identify(c: BaseActivity, crush: Crush?, handler: Handler? = null) {
            val oldCrush = crush?.copy()
            val bi = IdentifyBinding.inflate(c.layoutInflater)
            AppCompatResources.getColorStateList(c, R.color.chip).also {
                bi.masc.trackTintList = it
                bi.notifyBirth.trackTintList = it
            }

            // Default Values
            var isBirthSet = false
            var bir = crush?.calendar()
            if (crush != null) {
                bi.fName.setText(crush.fName)
                bi.mName.setText(crush.mName)
                bi.lName.setText(crush.lName)
                bi.masc.isChecked = crush.masc
                if (crush.height != -1f)
                    bi.height.setText(crush.height.toString())
                if (bir != null) {
                    bi.birth.text = bir.fullDate()
                    isBirthSet = true
                }
                bi.location.setText(crush.locat)
                bi.instagram.setText(crush.insta)
                bi.notifyBirth.isChecked = crush.notifyBirth
            } else {
                bi.masc.isChecked = c.sp.getBoolean(Settings.spPrefersMasculine, false)
            }
            if (bir == null) bir = GregorianCalendar()

            // Masculine
            bi.masc.setOnCheckedChangeListener { _, isChecked ->
                c.sp.edit().putBoolean(Settings.spPrefersMasculine, isChecked).apply()
            }

            // Birth
            bi.birth.setOnClickListener {
                DatePickerDialog.newInstance({ _, year, month, day ->
                    bir!!.set(Calendar.YEAR, year)
                    bir!!.set(Calendar.MONTH, month)
                    bir!!.set(Calendar.DAY_OF_MONTH, day)
                    bir = McdtpUtils.trimToMidnight(bir)
                    isBirthSet = true
                    bi.birth.text = bir!!.fullDate()
                }, bir).defaultOptions().show(c.supportFragmentManager, "birth")
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ActivityCompat.checkSelfPermission(
                    c, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (crush?.notifyBirth == true) reqNotificationPerm(c)
                else bi.notifyBirth.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) reqNotificationPerm(c)
                }
            }

            MaterialAlertDialogBuilder(c).apply {
                setTitle("${c.getString(R.string.identify)}: ${crush?.key ?: c.m.crush}")
                setView(bi.root)
                setPositiveButton(R.string.save) { _, _ ->
                    val inserted = Crush(
                        crush?.key ?: c.m.crush!!,
                        bi.fName.text.toString().ifEmpty { null },
                        bi.mName.text.toString().ifEmpty { null },
                        bi.lName.text.toString().ifEmpty { null },
                        bi.masc.isChecked,
                        if (bi.height.text.toString() != "")
                            bi.height.text.toString().toFloat() else -1f,
                        if (isBirthSet) bir!![Calendar.YEAR].toShort() else -1,
                        if (isBirthSet) bir!![Calendar.MONTH].toByte() else -1,
                        if (isBirthSet) bir!![Calendar.DAY_OF_MONTH].toByte() else -1,
                        bi.location.text.toString().ifEmpty { null },
                        bi.instagram.text.toString().ifEmpty { null },
                        bi.notifyBirth.isChecked
                    )
                    Work(
                        c, if (crush == null) Work.C_INSERT_ONE else Work.C_UPDATE_ONE,
                        listOf<Any?>(inserted, oldCrush), handler
                    ).start()
                    c.shake()
                }
                setNegativeButton(R.string.discard, null)
                setNeutralButton(R.string.clear) { ad1, _ ->
                    if (crush == null) return@setNeutralButton
                    MaterialAlertDialogBuilder(c).apply {
                        setTitle(c.getString(R.string.crushClear, crush.key))
                        setMessage(R.string.crushClearSure)
                        setPositiveButton(R.string.yes) { _, _ ->
                            Work(c, Work.C_DELETE_ONE, listOf(crush, null), handler).start()
                            c.shake()
                            ad1.dismiss()
                        }
                        setNegativeButton(R.string.no, null)
                    }.show()
                    c.shake()
                }
                setCancelable(true)
            }.show()
        }

        @RequiresApi(33)
        private fun reqNotificationPerm(c: BaseActivity) {
            ActivityCompat.requestPermissions(c, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 0)
        }
    }

    class ColumnFactory(c: BaseActivity, list: ArrayList<Pair<String, Float>>) :
        ArrayList<Column>(list.map {
            Column(
                listOf(
                    SubColumnValue(it.second)
                        .setLabel("${it.first} (${it.second})")
                        .setColor(
                            if (!c.night()) c.themeColor(com.google.android.material.R.attr.colorPrimary)
                            else c.color(R.color.CPV_LIGHT)
                        )
                )
            ).setHasLabels(true)
        })
}
