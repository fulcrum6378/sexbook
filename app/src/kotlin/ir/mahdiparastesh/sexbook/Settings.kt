package ir.mahdiparastesh.sexbook

import android.content.pm.PackageManager
import android.icu.util.Calendar
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.Dimension
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.addTextChangedListener
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ir.mahdiparastesh.mcdtp.McdtpUtils
import ir.mahdiparastesh.mcdtp.date.DatePickerDialog
import ir.mahdiparastesh.sexbook.Fun.calendar
import ir.mahdiparastesh.sexbook.Fun.defaultOptions
import ir.mahdiparastesh.sexbook.Fun.fullDate
import ir.mahdiparastesh.sexbook.Fun.shake
import ir.mahdiparastesh.sexbook.Main.Action.RELOAD
import ir.mahdiparastesh.sexbook.data.Database.DbFile
import ir.mahdiparastesh.sexbook.databinding.SettingsBinding
import ir.mahdiparastesh.sexbook.more.Act
import ir.mahdiparastesh.sexbook.more.BaseActivity
import ir.mahdiparastesh.sexbook.more.CalendarManager
import ir.mahdiparastesh.sexbook.more.LastOrgasm
import ir.mahdiparastesh.sexbook.more.MaterialMenu

class Settings : BaseActivity() {
    private lateinit var b: SettingsBinding
    private val calendarTypes: Array<String> by lazy { resources.getStringArray(R.array.calendarTypes) }
    private val emptyDate: String by lazy { getString(R.string.emptyDate) }
    private var calManager: CalendarManager? = null
    private var changed = false

    /** Beware of the numerical fields; go to Exporter.replace() for modifications. */
    companion object {
        const val spName = "settings"
        const val notifyBirthAfterLastTime = 3600000L * 6L

        // Via Settings
        const val spCalType = "calendarType" // def 0
        const val spStatSinceCb = "statisticiseSinceCb" // def false
        const val spStatSince = "statisticiseSince"
        const val spStatUntilCb = "statisticiseUntilCb" // def false
        const val spStatUntil = "statisticiseUntil"
        const val spStatInclude = "statisticiseInclude" // + s; def true
        const val spStatOnlyCrushes = "statisticiseOnlyCrushes" // def false
        const val spVibration = "vibration" // def true
        const val spCalOutput = "calendarOutput" // def false
        const val spGregorianForBirthdays = "useGregorianForBirthdays"
        const val spGregorianForBirthdaysDef = true
        const val spPauseBirthdaysNtf = "pauseBirthdayNotifications" // def false
        const val spNotifyBirthDaysBefore = "notifyBirthDaysBefore"
        const val spNotifyBirthDaysBeforeDef = 3

        // Via other places
        const val spDefPlace = "defaultPlace"
        const val spPageLoveSortBy = "page_love_sort_by" // def 0
        const val spPageLoveSortAsc = "page_love_sort_ascending" // def true

        // Automatic and hidden
        const val spPrefersOrgType = "prefersOrgType"
        const val spLastNotifiedBirthAt = "lastNotifiedBirthAt"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = SettingsBinding.inflate(layoutInflater)
        setContentView(b.root)
        toolbar(b.toolbar, R.string.stTitle)

        // Calendar Type
        b.stCalendarType.adapter =
            ArrayAdapter(this@Settings, R.layout.spinner_yellow, calendarTypes.toList())
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
        if (sp.contains(spStatSince)) {
            b.stStatSinceDateCb.isEnabled = true
            b.stStatSinceDateCb.isChecked = sp.getBoolean(spStatSinceCb, false)
        }
        b.stStatSinceDateCb.setOnCheckedChangeListener { _, isChecked ->
            sp.edit().putBoolean(spStatSinceCb, isChecked).apply()
            c.shake()
        }
        b.stStatSinceDate.text =
            if (!sp.contains(spStatSince)) emptyDate
            else sp.getLong(spStatSince, 0).calendar(this).fullDate()
        b.stStatSince.setOnClickListener {
            var cal = sp.getLong(spStatSince, Fun.now()).calendar(this)
            DatePickerDialog.newInstance({ _, year, month, day ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, month)
                cal.set(Calendar.DAY_OF_MONTH, day)
                cal = McdtpUtils.trimToMidnight(cal)
                if (sp.contains(spStatUntil) && cal.timeInMillis >
                    sp.getLong(spStatUntil, 0/*IMPOSSIBLE*/)
                ) {
                    Toast.makeText(c, R.string.statSinceIllogical, Toast.LENGTH_LONG).show()
                    return@newInstance; }
                b.stStatSinceDate.text = cal.fullDate()
                sp.edit()
                    .putLong(spStatSince, cal.timeInMillis)
                    .putBoolean(spStatSinceCb, true)
                    .apply()
                b.stStatSinceDateCb.isEnabled = true
                b.stStatSinceDateCb.isChecked = true
            }, cal).defaultOptions().show(supportFragmentManager, "stat_since")
        }

        // Statisticise Until
        if (sp.contains(spStatUntil)) {
            b.stStatUntilDateCb.isEnabled = true
            b.stStatUntilDateCb.isChecked = sp.getBoolean(spStatUntilCb, false)
        }
        b.stStatUntilDateCb.setOnCheckedChangeListener { _, isChecked ->
            sp.edit().putBoolean(spStatUntilCb, isChecked).apply()
            c.shake()
        }
        b.stStatUntilDate.text =
            if (!sp.contains(spStatUntil)) emptyDate
            else sp.getLong(spStatUntil, 0).calendar(this).fullDate()
        b.stStatUntil.setOnClickListener {
            var cal = sp.getLong(spStatUntil, Fun.now()).calendar(this)
            DatePickerDialog.newInstance({ _, year, month, day ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, month)
                cal.set(Calendar.DAY_OF_MONTH, day)
                cal = McdtpUtils.trimToMidnight(cal)
                if (sp.contains(spStatSince) && cal.timeInMillis <
                    sp.getLong(spStatSince, 0/*IMPOSSIBLE*/)
                ) {
                    Toast.makeText(c, R.string.statUntilIllogical, Toast.LENGTH_LONG).show()
                    return@newInstance; }
                b.stStatUntilDate.text = cal.fullDate()
                sp.edit()
                    .putLong(spStatUntil, cal.timeInMillis)
                    .putBoolean(spStatUntilCb, true)
                    .apply()
                b.stStatUntilDateCb.isEnabled = true
                b.stStatUntilDateCb.isChecked = true
            }, cal).defaultOptions().show(supportFragmentManager, "stat_until")
        }

        // Statisticise Range: Long Click
        View.OnLongClickListener { v ->
            MaterialMenu(this, v, R.menu.clear_date, Act().apply {
                this[R.id.clearDate] = {
                    if (v == b.stStatSince) {
                        sp.edit().remove(spStatSince).putBoolean(spStatSinceCb, false).apply()
                        b.stStatSinceDateCb.isChecked = false
                        b.stStatSinceDateCb.isEnabled = false
                        b.stStatSinceDate.text = emptyDate
                    } else {
                        sp.edit().remove(spStatUntil).putBoolean(spStatUntilCb, false).apply()
                        b.stStatUntilDateCb.isChecked = false
                        b.stStatUntilDateCb.isEnabled = false
                        b.stStatUntilDate.text = emptyDate
                    }
                }
            }).show(); true
        }.also {
            b.stStatSince.setOnLongClickListener(it)
            b.stStatUntil.setOnLongClickListener(it)
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
                colorFilter = themePdcf()
                labelFor = cbId
            })

            b.sexTypes.addView(MaterialCheckBox(this@Settings).apply {
                text = getString(R.string.stSexTypeInclude, sex.name)
                textAlignment = TextView.TEXT_ALIGNMENT_VIEW_START
                typeface = ResourcesCompat.getFont(c, R.font.normal)
                setTextSize(Dimension.SP, 16f)
                setPaddingRelative(
                    resources.getDimension(R.dimen.stItemPadH).toInt(), 0, 0, 0
                )
                id = cbId
                isChecked = sp.getBoolean(spStatInclude + s, true)
                setOnCheckedChangeListener { _, bb ->
                    sp.edit().putBoolean(spStatInclude + s, bb).apply()
                    c.shake()
                }
            }, ConstraintLayout.LayoutParams(0, -2).apply {
                topToTop = prevLlId!!
                bottomToBottom = prevLlId!!
                endToStart = prevLlId!!
                startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            })
        }

        // Statisticise only crushes
        b.stStatOnlyCrushes.isChecked = sp.getBoolean(spStatOnlyCrushes, false)
        b.stStatOnlyCrushes.setOnCheckedChangeListener { _, isChecked ->
            sp.edit().putBoolean(spStatOnlyCrushes, isChecked).apply()
            c.shake()
        }

        // Vibration
        b.stVibration.isChecked = sp.getBoolean(spVibration, true)
        b.stVibration.setOnCheckedChangeListener { _, isChecked ->
            Fun.vib = isChecked
            sp.edit().putBoolean(spVibration, isChecked).apply()
            c.shake()
        }

        // Birthdays
        b.stCalOutput.isChecked = sp.getBoolean(spCalOutput, false)
        b.stCalOutput.setOnCheckedChangeListener { _, isChecked ->
            if (!CalendarManager.checkPerm(this))
                CalendarManager.askPerm(this)
            else turnCalendar(isChecked)
            c.shake()
        }
        b.stUseGregorianForBirthdays.isChecked =
            sp.getBoolean(spGregorianForBirthdays, spGregorianForBirthdaysDef)
        b.stUseGregorianForBirthdays.setOnCheckedChangeListener { _, isChecked ->
            sp.edit().putBoolean(spGregorianForBirthdays, isChecked).apply()
            c.shake()
        }
        b.stPauseBirthdaysNtf.isChecked = sp.getBoolean(spPauseBirthdaysNtf, false)
        b.stPauseBirthdaysNtf.setOnCheckedChangeListener { _, isChecked ->
            sp.edit().putBoolean(spPauseBirthdaysNtf, isChecked).apply()
            c.shake()
        }
        b.stNotifyBirthDaysBefore
            .setText(sp.getInt(spNotifyBirthDaysBefore, spNotifyBirthDaysBeforeDef).toString())
        b.stNotifyBirthDaysBefore.addTextChangedListener {
            sp.edit().putInt(
                spNotifyBirthDaysBefore, try {
                    it.toString().toInt()
                } catch (_: NumberFormatException) {
                    return@addTextChangedListener
                }
            ).apply()
        }

        // Removal
        b.stReset.setOnClickListener {
            MaterialAlertDialogBuilder(this).apply {
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
            MaterialAlertDialogBuilder(this).apply {
                setTitle(R.string.stTruncate)
                setMessage(R.string.stTruncateSure)
                setPositiveButton(R.string.yes) { _, _ ->
                    DbFile(DbFile.Triple.MAIN).delete()
                    DbFile(DbFile.Triple.SHARED_MEMORY).delete()
                    DbFile(DbFile.Triple.WRITE_AHEAD_LOG).delete()
                    LastOrgasm.updateAll(c)
                    c.shake()
                    changed = true
                }
                setNegativeButton(R.string.no, null)
                setCancelable(true)
            }.show()
            c.shake()
        }
    }

    override fun onRequestPermissionsResult(code: Int, arr: Array<out String>, res: IntArray) {
        super.onRequestPermissionsResult(code, arr, res)
        if (code == CalendarManager.reqCode) {
            if (res.isNotEmpty() && res[0] == PackageManager.PERMISSION_GRANTED)
                turnCalendar(true)
            else b.stCalOutput.isChecked = false
        }
    }

    @Deprecated("Deprecated in Java")
    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        if (changed) goTo(Main::class, true) { action = RELOAD.s }
        else super.onBackPressed()
    }


    /** In the both cases, requires WRITE_CALENDAR permission. */
    private fun turnCalendar(on: Boolean) {
        sp.edit().putBoolean(spCalOutput, on).apply()
        calManager = CalendarManager(this, m.liefde.value)
        if (!on) calManager?.terminate()
    }
}
