package ir.mahdiparastesh.sexbook

import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.Dimension
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.checkbox.MaterialCheckBox
import ir.mahdiparastesh.sexbook.Fun.calendar
import ir.mahdiparastesh.sexbook.Fun.defaultOptions
import ir.mahdiparastesh.sexbook.Fun.fullDate
import ir.mahdiparastesh.sexbook.Fun.shake
import ir.mahdiparastesh.sexbook.Main.Action.RELOAD
import ir.mahdiparastesh.sexbook.data.Database.DbFile
import ir.mahdiparastesh.sexbook.databinding.SettingsBinding
import ir.mahdiparastesh.sexbook.mdtp.Utils
import ir.mahdiparastesh.sexbook.mdtp.date.DatePickerDialog
import ir.mahdiparastesh.sexbook.more.BaseActivity

class Settings : BaseActivity() {
    private lateinit var b: SettingsBinding
    private val calendarTypes: Array<String> by lazy { resources.getStringArray(R.array.calendarTypes) }
    private var changed = false

    companion object {
        const val spCalType = "calendarType" // def 0
        const val spDefPlace = "defaultPlace"
        const val spStatSince = "statisticiseSince"
        const val spStatSinceCb = "statisticiseSinceCb" // def false
        const val spStatInclude = "statisticiseInclude" // + s; def true
        const val spNotifyBirthDaysBefore = "notifyBirthDaysBefore" // def 3 TODO
        // Beware of the numerical fields; go to Exporter$Companion.replace() for modifications.

        // Hidden
        const val spPrefersMasculine = "prefersMasculine"
        const val spPrefersOrgType = "prefersOrgType"
        const val spLastNotifiedBirthAt = "lastNotifiedBirthAt"

        const val spName = "settings"
        const val notifyBirthAfterLastTime = 3600000L * 6L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = SettingsBinding.inflate(layoutInflater)
        setContentView(b.root)
        toolbar(b.toolbar, R.string.stTitle)

        // Calendar Type
        b.stCalendarType.adapter =
            ArrayAdapter(this@Settings, R.layout.spinner, calendarTypes.toList())
                .apply { setDropDownViewResource(R.layout.spinner_dd) }
        b.stCalendarType.setSelection(sp.getInt(spCalType, 0))
        b.stCalendarType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
            override fun onItemSelected(av: AdapterView<*>?, v: View?, i: Int, l: Long) {
                if (sp.getInt(spCalType, 0) == i) return
                sp.edit().putInt(spCalType, i).apply()
                c.shake()
                changed = true
            }
        }

        // Statisticise Since
        b.stStatSinceDateCb.isChecked = sp.getBoolean(spStatSinceCb, false)
        b.stStatSinceDateCb.setOnCheckedChangeListener { _, isChecked ->
            sp.edit().putBoolean(spStatSinceCb, isChecked).apply()
            c.shake()
        }
        b.stStatSinceDate.text =
            if (!sp.contains(spStatSince)) "..."
            else sp.getLong(spStatSince, 0).calendar(this).fullDate()
        b.stStatSince.setOnClickListener {
            var cal =
                if (!sp.contains(spStatSince)) Fun.now().calendar(this)
                else sp.getLong(spStatSince, 0).calendar(this)
            DatePickerDialog.newInstance({ _, year, month, day ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, month)
                cal.set(Calendar.DAY_OF_MONTH, day)
                cal = Utils.trimToMidnight(cal)
                b.stStatSinceDate.text = cal.fullDate()
                sp.edit().putLong(spStatSince, cal.timeInMillis).apply()
            }, cal).defaultOptions(this).show(supportFragmentManager, "stat_since")
        }

        // Sex Type Exclusion
        var prevLlId: Int? = null
        val sexTypes = Fun.sexTypes(c)
        for (s in sexTypes.indices) {
            val sex = sexTypes[s]
            val cbId = View.generateViewId()
            b.sexTypes.addView(ImageView(this@Settings).apply {
                val size = dp(32)
                layoutParams = ConstraintLayout.LayoutParams(size, size).apply {
                    topToBottom = prevLlId ?: b.sexTypesTitle.id
                    endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                    val marV = dp(12)
                    setMargins(0, marV, 0, marV)
                    marginEnd = dp(10)
                }
                prevLlId = View.generateViewId()
                id = prevLlId!!
                setImageResource(sex.icon)
                colorFilter = pdcf()
                labelFor = cbId
            })

            b.sexTypes.addView(MaterialCheckBox(this@Settings).apply {
                text = getString(R.string.stSexTypeInclude, sex.name)
                textAlignment = TextView.TEXT_ALIGNMENT_VIEW_START
                typeface = ResourcesCompat.getFont(c, R.font.normal)
                setTextSize(Dimension.SP, 16f)
                id = cbId
                isChecked = sp.getBoolean(spStatInclude + s, true)
                setOnCheckedChangeListener { _, bb ->
                    sp.edit().putBoolean(spStatInclude + s, bb).apply()
                    c.shake()
                }
            }, ConstraintLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topToTop = prevLlId!!
                bottomToBottom = prevLlId!!
                endToStart = prevLlId!!
                startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            })
        }

        // Removal
        b.stReset.setOnClickListener {
            AlertDialog.Builder(this).apply {
                setTitle(R.string.stReset)
                setMessage(R.string.stResetSure)
                setPositiveButton(R.string.yes) { _, _ ->
                    sp.edit().apply { for (k in sp.all.keys) remove(k) }.apply()
                    // spinners and checkboxes saved their instance and after recreation and setting
                    // their values, they saved their values into SP. After assigning their
                    // "saveEnabled" to "false", it worked like a charm!
                    c.shake()
                    recreate()
                    changed = true
                }
                setNegativeButton(R.string.no, null)
                setCancelable(true)
            }.show()
            c.shake()
        }
        b.stTruncate.setOnClickListener {
            AlertDialog.Builder(this).apply {
                setTitle(R.string.stTruncate)
                setMessage(R.string.stTruncateSure)
                setPositiveButton(R.string.yes) { _, _ ->
                    DbFile(DbFile.Triple.MAIN).delete()
                    DbFile(DbFile.Triple.SHARED_MEMORY).delete()
                    DbFile(DbFile.Triple.WRITE_AHEAD_LOG).delete()
                    c.shake()
                    changed = true
                }
                setNegativeButton(R.string.no, null)
                setCancelable(true)
            }.show()
            c.shake()
        }
    }

    override fun onBackPressed() {
        if (changed) {
            finish()
            startActivity(Intent(this, Main::class.java).setAction(RELOAD.s))
        } else super.onBackPressed()
    }
}
