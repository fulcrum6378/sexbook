package ir.mahdiparastesh.sexbook

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.pm.PackageManager
import android.icu.util.Calendar
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.Dimension
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ir.mahdiparastesh.mcdtp.McdtpUtils
import ir.mahdiparastesh.mcdtp.date.DatePickerDialog
import ir.mahdiparastesh.sexbook.base.BaseActivity
import ir.mahdiparastesh.sexbook.base.BaseDialog
import ir.mahdiparastesh.sexbook.data.Crush
import ir.mahdiparastesh.sexbook.ctrl.Database.DbFile
import ir.mahdiparastesh.sexbook.databinding.SettingsBinding
import ir.mahdiparastesh.sexbook.list.BNtfCrushAdap
import ir.mahdiparastesh.sexbook.misc.CalendarManager
import ir.mahdiparastesh.sexbook.misc.LastOrgasm
import ir.mahdiparastesh.sexbook.util.NumberUtils
import ir.mahdiparastesh.sexbook.util.NumberUtils.calendar
import ir.mahdiparastesh.sexbook.util.NumberUtils.fullDate
import ir.mahdiparastesh.sexbook.view.EasyPopupMenu
import ir.mahdiparastesh.sexbook.view.SexType
import ir.mahdiparastesh.sexbook.view.UiTools
import ir.mahdiparastesh.sexbook.view.UiTools.defaultOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Settings : BaseActivity() {
    private lateinit var b: SettingsBinding
    val mm: MyModel by viewModels()
    private val calendarTypes: Array<String> by lazy { resources.getStringArray(R.array.calendarTypes) }
    private val emptyDate: String by lazy { getString(R.string.emptyDate) }
    var bNtfCrushAdap: BNtfCrushAdap? = null

    /**
     * Beware of the numerical fields;
     * Go to [ir.mahdiparastesh.sexbook.ctrl.Exporter.replace] for modifications.
     */
    companion object {
        const val spName = "settings"
        const val notifyBirthAfterLastTime = 3600000L * 12L
        const val B_NTF_CRUSHES_TAG = "b_ntf_crushes"

        // via Settings
        const val spCalType = "calendarType" // Int, def 0
        const val spStatSinceCb = "statisticiseSinceCb" // Boolean, def false
        const val spStatSince = "statisticiseSince" // Long
        const val spStatUntilCb = "statisticiseUntilCb" // Boolean, def false
        const val spStatUntil = "statisticiseUntil" // Long
        const val spStatInclude = "statisticiseInclude" // + s; Boolean, def true
        const val spStatOnlyCrushes = "statisticiseOnlyCrushes" // Boolean, def false
        const val spStatNonOrgasm = "statNonOrgasm" // Boolean, def true
        const val spHideUnsafePeople = "hideUnsafe" // Boolean, def true
        const val spVibration = "vibration" // Boolean, def true
        const val spCalOutput = "calendarOutput" // Boolean, def false
        const val spPauseBirthdaysNtf = "pauseBirthdayNotifications" // Boolean, def false
        const val spNotifyBirthDaysBefore = "notifyBirthDaysBefore" // Int
        const val spNotifyBirthDaysBeforeDef = 3

        // via other places
        const val spDefPlace = "defaultPlace" // Long
        const val spPageLoveSortBy = "page_love_sort_by" // Int, def 0
        const val spPageLoveSortAsc = "page_love_sort_ascending" // Boolean, def true
        const val spPeopleSortBy = "people_sort_by" // Int, def 0
        const val spPeopleSortAsc = "people_sort_ascending" // Boolean, def true

        // automatic and hidden
        const val spLastNotifiedBirthAt = "lastNotifiedBirthAt" // Long
    }

    class MyModel : ViewModel() {
        lateinit var bNtfCrushes: ArrayList<String>

        fun sortBNtfCrushes(c: Sexbook) {
            bNtfCrushes.sortWith(Crush.Sort(c, spPeopleSortBy, spPeopleSortAsc))
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = SettingsBinding.inflate(layoutInflater)
        setContentView(b.root)
        toolbar(b.toolbar, R.string.stTitle)

        // Calendar Type
        b.stCalendarType.adapter =
            ArrayAdapter(this@Settings, R.layout.spinner_yellow, calendarTypes.toList())
                .apply { setDropDownViewResource(R.layout.spinner_dd) }
        b.stCalendarType.setSelection(c.sp.getInt(spCalType, 0))
        b.stCalendarType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
            override fun onItemSelected(av: AdapterView<*>?, v: View?, i: Int, l: Long) {
                if (c.sp.getInt(spCalType, 0) == i) return
                c.sp.edit().putInt(spCalType, i).apply()
                shake()
                Main.changed = true
            }
        }

        // Statisticise Since
        if (c.sp.contains(spStatSince)) {
            b.stStatSinceDateCb.isEnabled = true
            b.stStatSinceDateCb.isChecked = c.sp.getBoolean(spStatSinceCb, false)
        }
        b.stStatSinceDateCb.setOnCheckedChangeListener { _, isChecked ->
            c.sp.edit().putBoolean(spStatSinceCb, isChecked).apply()
            shake()
        }
        b.stStatSinceDate.text =
            if (!c.sp.contains(spStatSince)) emptyDate
            else c.sp.getLong(spStatSince, 0).calendar(this).fullDate()
        b.stStatSince.setOnClickListener {
            var cal = c.sp.getLong(spStatSince, NumberUtils.now()).calendar(this)
            DatePickerDialog.newInstance({ _, year, month, day ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, month)
                cal.set(Calendar.DAY_OF_MONTH, day)
                cal = McdtpUtils.trimToMidnight(cal)
                if (c.sp.contains(spStatUntil) && cal.timeInMillis >
                    c.sp.getLong(spStatUntil, 0/*IMPOSSIBLE*/)
                ) {
                    Toast.makeText(
                        c, R.string.statSinceIllogical, Toast.LENGTH_LONG
                    ).show()
                    return@newInstance; }
                b.stStatSinceDate.text = cal.fullDate()
                c.sp.edit()
                    .putLong(spStatSince, cal.timeInMillis)
                    .putBoolean(spStatSinceCb, true)
                    .apply()
                b.stStatSinceDateCb.isEnabled = true
                b.stStatSinceDateCb.isChecked = true
            }, cal).defaultOptions().show(supportFragmentManager, "stat_since")
        }

        // Statisticise Until
        if (c.sp.contains(spStatUntil)) {
            b.stStatUntilDateCb.isEnabled = true
            b.stStatUntilDateCb.isChecked = c.sp.getBoolean(spStatUntilCb, false)
        }
        b.stStatUntilDateCb.setOnCheckedChangeListener { _, isChecked ->
            c.sp.edit().putBoolean(spStatUntilCb, isChecked).apply()
            shake()
        }
        b.stStatUntilDate.text =
            if (!c.sp.contains(spStatUntil)) emptyDate
            else c.sp.getLong(spStatUntil, 0).calendar(this).fullDate()
        b.stStatUntil.setOnClickListener {
            var cal = c.sp.getLong(spStatUntil, NumberUtils.now()).calendar(this)
            DatePickerDialog.newInstance({ _, year, month, day ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, month)
                cal.set(Calendar.DAY_OF_MONTH, day)
                cal = McdtpUtils.trimToMidnight(cal)
                if (c.sp.contains(spStatSince) && cal.timeInMillis <
                    c.sp.getLong(spStatSince, 0/*IMPOSSIBLE*/)
                ) {
                    Toast.makeText(
                        c, R.string.statUntilIllogical, Toast.LENGTH_LONG
                    ).show()
                    return@newInstance; }
                b.stStatUntilDate.text = cal.fullDate()
                c.sp.edit()
                    .putLong(spStatUntil, cal.timeInMillis)
                    .putBoolean(spStatUntilCb, true)
                    .apply()
                b.stStatUntilDateCb.isEnabled = true
                b.stStatUntilDateCb.isChecked = true
            }, cal).defaultOptions().show(supportFragmentManager, "stat_until")
        }

        // Statisticise Range: Long Click
        View.OnLongClickListener { v ->
            EasyPopupMenu(
                this, v, R.menu.clear_date,
                R.id.clearDate to {
                    if (v == b.stStatSince) {
                        c.sp.edit().remove(spStatSince).putBoolean(spStatSinceCb, false).apply()
                        b.stStatSinceDateCb.isChecked = false
                        b.stStatSinceDateCb.isEnabled = false
                        b.stStatSinceDate.text = emptyDate
                    } else {
                        c.sp.edit().remove(spStatUntil).putBoolean(spStatUntilCb, false).apply()
                        b.stStatUntilDateCb.isChecked = false
                        b.stStatUntilDateCb.isEnabled = false
                        b.stStatUntilDate.text = emptyDate
                    }
                }
            ).show(); true
        }.also {
            b.stStatSince.setOnLongClickListener(it)
            b.stStatUntil.setOnLongClickListener(it)
        }

        // filtering by sex type
        var prevLlId: Int? = null
        val sexTypes = SexType.all(c)
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
                id = prevLlId
                setImageResource(sex.icon)
                colorFilter = themePdcf()
                labelFor = cbId
            })

            b.sexTypes.addView(MaterialCheckBox(this@Settings).apply {
                text = getString(R.string.stSexTypeInclude, sex.name)
                textAlignment = TextView.TEXT_ALIGNMENT_VIEW_START
                typeface = resources.getFont(R.font.normal)
                setTextSize(Dimension.SP, 16f)
                setPaddingRelative(
                    resources.getDimension(R.dimen.stItemPadH).toInt(), 0, 0, 0
                )
                id = cbId
                isChecked = c.sp.getBoolean(spStatInclude + s, true)
                setOnCheckedChangeListener { _, bb ->
                    c.sp.edit().putBoolean(spStatInclude + s, bb).apply()
                    shake()
                }
            }, ConstraintLayout.LayoutParams(0, -2).apply {
                topToTop = prevLlId!!
                bottomToBottom = prevLlId
                endToStart = prevLlId
                startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            })
        }

        // Other statistical tweaks
        b.stStatOnlyCrushes.isChecked = c.sp.getBoolean(spStatOnlyCrushes, false)
        b.stStatOnlyCrushes.setOnCheckedChangeListener { _, isChecked ->
            c.sp.edit().putBoolean(spStatOnlyCrushes, isChecked).apply()
            shake()
        }
        b.stStatNonOrgasm.isChecked = c.sp.getBoolean(spStatNonOrgasm, true)
        b.stStatNonOrgasm.setOnCheckedChangeListener { _, isChecked ->
            c.sp.edit().putBoolean(spStatNonOrgasm, isChecked).apply()
            shake()
        }

        // Vibration
        b.stHideUnsafePeople.isChecked = c.sp.getBoolean(spHideUnsafePeople, true)
        b.stHideUnsafePeople.setOnCheckedChangeListener { _, isChecked ->
            c.sp.edit().putBoolean(spHideUnsafePeople, isChecked).apply()
            shake()
        }
        b.stVibration.isChecked = c.sp.getBoolean(spVibration, true)
        b.stVibration.setOnCheckedChangeListener { _, isChecked ->
            UiTools.vib = isChecked
            c.sp.edit().putBoolean(spVibration, isChecked).apply()
            shake()
        }

        // Birthdays
        b.stCalOutput.isChecked = c.sp.getBoolean(spCalOutput, false)
        b.stCalOutput.setOnCheckedChangeListener { _, isChecked ->
            if (!CalendarManager.checkPerm(c))
                CalendarManager.askPerm(this)
            else turnCalendar(isChecked)
            shake()
        }
        b.stPauseBirthdaysNtf.isChecked = c.sp.getBoolean(spPauseBirthdaysNtf, false)
        b.stPauseBirthdaysNtf.setOnCheckedChangeListener { _, isChecked ->
            c.sp.edit().putBoolean(spPauseBirthdaysNtf, isChecked).apply()
            shake()
        }
        b.stNotifyBirthDaysBefore
            .setText(c.sp.getInt(spNotifyBirthDaysBefore, spNotifyBirthDaysBeforeDef).toString())
        b.stNotifyBirthDaysBefore.addTextChangedListener {
            c.sp.edit().putInt(
                spNotifyBirthDaysBefore, try {
                    it.toString().toInt()
                } catch (_: NumberFormatException) {
                    return@addTextChangedListener
                }
            ).apply()
        }
        CoroutineScope(Dispatchers.IO).launch {
            mm.bNtfCrushes = ArrayList(
                c.people.values.filter { it.notifyBirth() }.map { it.key }
            )
            mm.sortBNtfCrushes(c)
            withContext(Dispatchers.Main) {
                b.stBNtfCrushes.setOnClickListener {
                    BNtfCrushes().show(supportFragmentManager, B_NTF_CRUSHES_TAG)
                }
            }
        }

        // Removal
        b.stReset.setOnClickListener {
            MaterialAlertDialogBuilder(this).apply {
                setTitle(R.string.stReset)
                setMessage(R.string.stResetSure)
                setPositiveButton(R.string.yes) { _, _ ->
                    c.sp.edit().apply { for (k in c.sp.all.keys) remove(k) }.apply()
                    // spinners and checkboxes saved their instance and after recreation and setting
                    // their values, they saved their values into c.sp. After assigning their
                    // "saveEnabled" to "false", it worked like a charm!
                    shake()
                    recreate()
                    Main.changed = true
                }
                setNegativeButton(R.string.no, null)
                setCancelable(true)
            }.show()
            shake()
        }
        b.stTruncate.setOnClickListener {
            MaterialAlertDialogBuilder(this).apply {
                setTitle(R.string.stTruncate)
                setMessage(R.string.stTruncateSure)
                setPositiveButton(R.string.yes) { _, _ ->
                    DbFile(DbFile.Triple.MAIN).delete()
                    DbFile(DbFile.Triple.SHARED_MEMORY).delete()
                    DbFile(DbFile.Triple.WRITE_AHEAD_LOG).delete()
                    LastOrgasm.doUpdateAll(c)
                    shake()
                    Main.changed = true
                }
                setNegativeButton(R.string.no, null)
                setCancelable(true)
            }.show()
            shake()
        }
    }

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun onRequestPermissionsResult(code: Int, arr: Array<out String>, res: IntArray) {
        super.onRequestPermissionsResult(code, arr, res)
        if (code == CalendarManager.reqCode) {
            if (res.isNotEmpty() && res[0] == PackageManager.PERMISSION_GRANTED)
                turnCalendar(true)
            else b.stCalOutput.isChecked = false
        }
    }


    /** In the both cases, requires WRITE_CALENDAR permission. */
    private fun turnCalendar(on: Boolean) {
        c.sp.edit().putBoolean(spCalOutput, on).apply()
        CoroutineScope(Dispatchers.IO).launch {
            if (on) CalendarManager.initialise(this@Settings)
            else CalendarManager.destroy(c)
        }
    }

    class BNtfCrushes : BaseDialog<Settings>() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            isCancelable = true
            return MaterialAlertDialogBuilder(c).apply {
                setTitle(resources.getString(R.string.stBNtfCrushes))
                setView(RecyclerView(c).apply {
                    layoutManager = LinearLayoutManager(c)
                    setPadding(0, c.dp(12), 0, 0)
                    c.bNtfCrushAdap = BNtfCrushAdap(c)
                    adapter = c.bNtfCrushAdap
                })
                setOnDismissListener { c.bNtfCrushAdap = null }
            }.create()
        }
    }
}
