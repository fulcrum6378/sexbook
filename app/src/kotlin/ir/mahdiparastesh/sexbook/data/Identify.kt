package ir.mahdiparastesh.sexbook.data

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.pm.PackageManager
import android.icu.util.Calendar
import android.icu.util.GregorianCalendar
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ir.mahdiparastesh.mcdtp.McdtpUtils
import ir.mahdiparastesh.mcdtp.date.DatePickerDialog
import ir.mahdiparastesh.sexbook.Fun.defaultOptions
import ir.mahdiparastesh.sexbook.Fun.fullDate
import ir.mahdiparastesh.sexbook.Fun.shake
import ir.mahdiparastesh.sexbook.Fun.toGregorian
import ir.mahdiparastesh.sexbook.Fun.vis
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.Settings
import ir.mahdiparastesh.sexbook.databinding.IdentifyBinding
import ir.mahdiparastesh.sexbook.more.Act
import ir.mahdiparastesh.sexbook.more.BaseActivity
import ir.mahdiparastesh.sexbook.more.BaseDialog
import ir.mahdiparastesh.sexbook.more.MaterialMenu
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.experimental.and
import kotlin.experimental.or

class Identify() : BaseDialog() {
    constructor(crush: Crush?) : this() {
        Companion.crush = crush
    }

    companion object {
        const val TAG = "identify"
        const val BUNDLE_CRUSH_KEY = "crush_key"
        const val DISABLED_ALPHA = 0.7f
        var crush: Crush? = null
    }

    private lateinit var b: IdentifyBinding
    private var isBirthSet = false
    private var isFirstSet = false
    private var bir: Calendar? = null
    private var fir: Calendar? = null

    @SuppressLint("NewApi")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        b = IdentifyBinding.inflate(c.layoutInflater)
        ContextCompat.getColorStateList(c, R.color.chip)
            .also { b.notifyBirth.trackTintList = it }

        // Gender
        b.gender.adapter = ArrayAdapter(
            c, R.layout.spinner_white, c.resources.getStringArray(R.array.genders)
        ).apply { setDropDownViewResource(R.layout.spinner_dd) }
        b.gender.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
            override fun onItemSelected(a: AdapterView<*>?, v: View?, i: Int, l: Long) {
                b.gender.alpha = if (i == 0) DISABLED_ALPHA else 1f
            }
        }

        // Default Values
        bir = crush?.bCalendar(c)
        fir = crush?.fCalendar(c)
        if (crush != null) {
            b.fName.setText(crush!!.fName)
            b.mName.setText(crush!!.mName)
            b.lName.setText(crush!!.lName)
            b.gender.setSelection((crush!!.status and Crush.STAT_GENDER).toInt())
            b.fiction.isChecked = crush!!.fiction().also { onFictionChanged(it) }
            if (bir != null) {
                b.birth.text = bir!!.fullDate()
                isBirthSet = true
            }
            if (crush!!.height != -1f)
                b.height.setText(crush!!.height.toString())
            b.address.setText(crush!!.address)
            if (fir != null) {
                b.firstMet.text = fir!!.fullDate()
                isFirstSet = true
            }
            b.instagram.setText(crush!!.insta)
            b.notifyBirth.isChecked = crush!!.notifyBirth()
        }
        if (bir == null) {
            bir = if (c.sp.getBoolean(
                    Settings.spGregorianForBirthdays, Settings.spGregorianForBirthdaysDef
                )
            ) GregorianCalendar()
            else c.calType().getDeclaredConstructor().newInstance()
            b.birth.alpha = DISABLED_ALPHA
            b.birth.isLongClickable = false
        }
        if (fir == null) {
            fir = c.calType().getDeclaredConstructor().newInstance()
            b.firstMet.alpha = DISABLED_ALPHA
            b.firstMet.isLongClickable = false
        }

        // Fiction
        b.fiction.setOnCheckedChangeListener { _, isChecked -> onFictionChanged(isChecked) }

        // Birthday
        b.birth.setOnClickListener {
            DatePickerDialog.newInstance({ _, year, month, day ->
                bir!!.set(Calendar.YEAR, year)
                bir!!.set(Calendar.MONTH, month)
                bir!!.set(Calendar.DAY_OF_MONTH, day)
                bir = McdtpUtils.trimToMidnight(bir)
                isBirthSet = true
                b.birth.text = bir!!.fullDate()
                b.birth.alpha = 1f
                b.birth.isLongClickable = true
            }, bir).defaultOptions().show(c.supportFragmentManager, "birth")
        }

        // First Met
        b.firstMet.setOnClickListener {
            DatePickerDialog.newInstance({ _, year, month, day ->
                fir!!.set(Calendar.YEAR, year)
                fir!!.set(Calendar.MONTH, month)
                fir!!.set(Calendar.DAY_OF_MONTH, day)
                fir = McdtpUtils.trimToMidnight(fir)
                isFirstSet = true
                b.firstMet.text = fir!!.fullDate()
                b.firstMet.alpha = 1f
                b.firstMet.isLongClickable = true
            }, fir).defaultOptions().show(c.supportFragmentManager, "first_met")
        }

        // Notify Birth
        val needsNtfPerm = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ActivityCompat.checkSelfPermission(
                    c, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
        if (needsNtfPerm && crush?.notifyBirth() == true) reqNotificationPerm(c)
        b.notifyBirth.setOnCheckedChangeListener { _, isChecked ->
            if (!needsNtfPerm && isChecked) reqNotificationPerm(c)
            b.notifyBirth.alpha = if (isChecked) 1f else DISABLED_ALPHA
        } // changing isChecked programmatically won't invoke the listener!
        b.notifyBirth.alpha = if (b.notifyBirth.isChecked) 1f else DISABLED_ALPHA

        // Date Pickers: Long Click
        View.OnLongClickListener { v ->
            MaterialMenu(c, v, R.menu.clear_date, Act().apply {
                this[R.id.clearDate] = {
                    if (v == b.birth) {
                        isBirthSet = false
                        b.birth.setText(R.string.birth)
                        b.birth.alpha = DISABLED_ALPHA
                        b.birth.isLongClickable = false
                    } else {
                        isFirstSet = false
                        b.firstMet.setText(R.string.firstMet)
                        b.firstMet.alpha = DISABLED_ALPHA
                        b.firstMet.isLongClickable = false
                    }
                }
            }).show(); true
        }.also {
            b.birth.setOnLongClickListener(it)
            b.firstMet.setOnLongClickListener(it)
        }

        val crushKey = requireArguments().getString(BUNDLE_CRUSH_KEY)!!
        return MaterialAlertDialogBuilder(c).apply {
            setTitle("${c.getString(R.string.identify)}: ${crush?.key ?: crushKey}")
            setView(b.root)
            setPositiveButton(R.string.save) { _, _ ->
                val endBir = bir!!.toGregorian() // "this" is returned when it is already Gregorian
                val endFir = fir!!.toGregorian()
                val inserted = Crush(
                    crush?.key ?: crushKey,
                    b.fName.text.toString().ifBlank { null },
                    b.mName.text.toString().ifBlank { null },
                    b.lName.text.toString().ifBlank { null },
                    b.gender.selectedItemPosition.toByte() or
                            (if (b.fiction.isChecked) Crush.STAT_FICTION else 0) or
                            (if (b.notifyBirth.isChecked) Crush.STAT_NOTIFY_BIRTH else 0) or
                            (crush?.let { it.status and Crush.STAT_INACTIVE } ?: 0),
                    if (isBirthSet) "${endBir[Calendar.YEAR]}.${endBir[Calendar.MONTH] + 1}." +
                            "${endBir[Calendar.DAY_OF_MONTH]}" else null,
                    if (b.height.text.toString() != "")
                        b.height.text.toString().toFloat() else -1f,
                    /*TODO*/0,
                    b.address.text.toString().ifBlank { null },
                    if (isFirstSet) "${endFir[Calendar.YEAR]}.${endFir[Calendar.MONTH] + 1}." +
                            "${endFir[Calendar.DAY_OF_MONTH]}" else null,
                    b.instagram.text.toString().ifBlank { null },
                )
                CoroutineScope(Dispatchers.IO).launch {
                    if (crush == null) c.m.dao.cInsert(inserted)
                    else c.m.dao.cUpdate(inserted)
                    withContext(Dispatchers.Main) {
                        c.m.onCrushChanged(c, inserted, if (crush == null) 0 else 1)
                    }
                }
                c.shake()
            }
            setNegativeButton(R.string.discard, null)
            setNeutralButton(R.string.clear) { ad1, _ ->
                if (crush == null) return@setNeutralButton
                MaterialAlertDialogBuilder(c).apply {
                    setTitle(c.getString(R.string.crushClear, crush!!.key))
                    setMessage(R.string.crushClearSure)
                    setPositiveButton(R.string.yes) { _, _ ->
                        CoroutineScope(Dispatchers.IO).launch {
                            c.m.dao.cDelete(crush!!)
                            withContext(Dispatchers.Main) {
                                c.m.onCrushChanged(c, crush!!, 2)
                            }
                        }
                        c.shake()
                        ad1.dismiss()
                    }
                    setNegativeButton(R.string.no, null)
                }.show()
                c.shake()
            }
            setCancelable(true)
        }.create()
    }

    @RequiresApi(33)
    private fun reqNotificationPerm(c: BaseActivity) {
        ActivityCompat.requestPermissions(c, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 0)
    }

    private fun onFictionChanged(bb: Boolean) {
        b.birth.vis(!bb)
        b.birthSep.vis(!bb)
        b.firstMetSep.vis(!bb)
        b.instagram.vis(!bb)
        b.instagramSep.vis(!bb)
        b.notifyBirth.vis(!bb)
    }
}
