package ir.mahdiparastesh.sexbook

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.get
import ir.mahdiparastesh.sexbook.Fun.Companion.calendar
import ir.mahdiparastesh.sexbook.Fun.Companion.defCalendar
import ir.mahdiparastesh.sexbook.Fun.Companion.fullDate
import ir.mahdiparastesh.sexbook.Fun.Companion.stylise
import ir.mahdiparastesh.sexbook.Main.Action.RELOAD
import ir.mahdiparastesh.sexbook.data.Database.DbFile
import ir.mahdiparastesh.sexbook.databinding.SettingsBinding
import ir.mahdiparastesh.sexbook.more.BaseActivity
import ir.mahdiparastesh.sexbook.more.LocalDatePicker
import ir.mahdiparastesh.sexbook.more.SpinnerAdap
import kotlinx.coroutines.runBlocking
import java.io.File

class Settings : BaseActivity() {
    private lateinit var b: SettingsBinding
    private lateinit var calendarTypes: Array<String>
    private var changed = false

    companion object {
        const val spCalType = "calendarType"
        const val spDefPlace = "defaultPlace"
        const val spStatSince = "statisticiseSince"
        const val spStatSinceCb = "statisticiseSinceCb"
        // Beware of the numerical fields; go to Exporter$Companion.replace() for modifications.

        @SuppressLint("SdCardPath")
        const val spPath = "/data/data/ir.mahdiparastesh.sexbook/shared_prefs/"
        const val spName = "settings"

        fun migrateSp() {
            File("${spPath}ir.mahdiparastesh.sexbook.Settings.xml")
                .apply { if (exists()) renameTo(spFile()) }
        }

        fun spFile() = File("${spPath}${spName}.xml")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = SettingsBinding.inflate(layoutInflater)
        setContentView(b.root)
        toolbar(b.toolbar, R.string.stTitle)


        // Fonts
        for (l in 0 until b.ll.childCount) {
            val first = (b.ll[l] as ConstraintLayout)[0]
            if (first is TextView) first.typeface = font1
        }
        b.stStatSinceDate.typeface = font1

        // Calendar Type
        calendarTypes = resources.getStringArray(R.array.calendarTypes)
        b.stCalendarType.adapter = SpinnerAdap(this, calendarTypes.toList())
        b.stCalendarType.setSelection(sp.getInt(spCalType, 0))
        b.stCalendarType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
            override fun onItemSelected(av: AdapterView<*>?, v: View?, i: Int, l: Long) {
                if (sp.getInt(spCalType, 0) == i) return
                changed = true
                sp.edit().apply {
                    putInt(spCalType, i)
                    apply()
                }
            }
        }

        // Statisticise Since
        b.stStatSinceDateCb.isChecked = sp.getBoolean(spStatSinceCb, false)
        b.stStatSinceDateCb.setOnCheckedChangeListener { _, isChecked ->
            sp.edit().putBoolean(spStatSinceCb, isChecked).apply()
        }
        val statSinc = sp.getLong(spStatSince, 0).calendar()
        b.stStatSinceDate.text =
            if (!sp.contains(spStatSince)) "..." else statSinc.fullDate(this)
        b.stStatSince.setOnClickListener {
            LocalDatePicker(
                this, "stat", if (!sp.contains(spStatSince)) Fun.now().calendar() else statSinc
            ) { _, time ->
                val cal = time.defCalendar()
                b.stStatSinceDate.text = cal.fullDate(this)
                sp.edit().putLong(spStatSince, time).apply()
            }
        }

        // Removal
        b.stReset.setOnClickListener {
            AlertDialog.Builder(this).apply {
                setTitle(R.string.stReset)
                setMessage(R.string.stResetSure)
                setPositiveButton(R.string.yes) { _, _ ->
                    runBlocking { spFile().also { if (it.exists()) it.delete() } }
                    initSp()
                    changed = true
                    recreate()
                }
                setNegativeButton(R.string.no, null)
                setCancelable(true)
            }.show().stylise(this)
        }
        b.stTruncate.setOnClickListener {
            AlertDialog.Builder(this).apply {
                setTitle(R.string.stTruncate)
                setMessage(R.string.stTruncateSure)
                setPositiveButton(R.string.yes) { _, _ ->
                    DbFile(DbFile.Triple.MAIN).delete()
                    DbFile(DbFile.Triple.SHARED_MEMORY).delete()
                    DbFile(DbFile.Triple.WRITE_AHEAD_LOG).delete()
                    changed = true
                }
                setNegativeButton(R.string.no, null)
                setCancelable(true)
            }.show().stylise(this)
        }
    }

    override fun onBackPressed() {
        if (changed) {
            finish()
            startActivity(Intent(this, Main::class.java).setAction(RELOAD.s))
        } else super.onBackPressed()
    }
}
