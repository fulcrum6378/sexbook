package ir.mahdiparastesh.sexbook.data

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.icu.util.Calendar
import android.icu.util.GregorianCalendar
import android.os.Build
import android.os.Handler
import android.view.View
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ir.mahdiparastesh.mcdtp.McdtpUtils
import ir.mahdiparastesh.mcdtp.date.DatePickerDialog
import ir.mahdiparastesh.sexbook.Fun.defaultOptions
import ir.mahdiparastesh.sexbook.Fun.fullDate
import ir.mahdiparastesh.sexbook.Fun.shake
import ir.mahdiparastesh.sexbook.Fun.toGregorian
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.Settings
import ir.mahdiparastesh.sexbook.databinding.IdentifyBinding
import ir.mahdiparastesh.sexbook.more.Act
import ir.mahdiparastesh.sexbook.more.BaseActivity
import ir.mahdiparastesh.sexbook.more.MaterialMenu

@SuppressLint("NewApi")
class Identify(c: BaseActivity, crush: Crush?, handler: Handler? = null) {
    private val DISABLED_ALPHA = 0.7f

    init {
        val oldCrush = crush?.copy()
        val bi = IdentifyBinding.inflate(c.layoutInflater)
        AppCompatResources.getColorStateList(c, R.color.chip)
            .also { bi.notifyBirth.trackTintList = it }

        // Gender
        bi.gender.adapter = ArrayAdapter(
            c, R.layout.spinner, c.resources.getStringArray(R.array.genders)
        ).apply { setDropDownViewResource(R.layout.spinner_dd) }

        // Default Values
        var isBirthSet = false
        var bir = crush?.bCalendar(c)
        val birIsGrg = c.sp.getBoolean(
            Settings.spUseGregorianForBirthdays, Settings.spUseGregorianForBirthdaysDef
        )
        var isFirstSet = false
        var fir = crush?.fCalendar(c)
        if (crush != null) {
            bi.fName.setText(crush.fName)
            bi.mName.setText(crush.mName)
            bi.lName.setText(crush.lName)
            bi.gender.setSelection(crush.gender.toInt() + 1)
            if (bir != null) {
                bi.birth.text = bir.fullDate()
                isBirthSet = true
            }
            if (crush.height != -1f)
                bi.height.setText(crush.height.toString())
            bi.address.setText(crush.address)
            bi.instagram.setText(crush.insta)
            if (fir != null) {
                bi.firstMet.text = fir.fullDate()
                isFirstSet = true
            }
            bi.notifyBirth.isChecked = crush.notifyBirth
        }
        if (bir == null) {
            bir =
                if (birIsGrg) GregorianCalendar()
                else c.calType().getDeclaredConstructor().newInstance()
            bi.birth.alpha = DISABLED_ALPHA
            bi.birth.isLongClickable = false
        }
        if (fir == null) {
            fir = c.calType().getDeclaredConstructor().newInstance()
            bi.firstMet.alpha = DISABLED_ALPHA
            bi.firstMet.isLongClickable = false
        }

        // Birthday
        bi.birth.setOnClickListener {
            DatePickerDialog.newInstance({ _, year, month, day ->
                bir!!.set(Calendar.YEAR, year)
                bir!!.set(Calendar.MONTH, month)
                bir!!.set(Calendar.DAY_OF_MONTH, day)
                bir = McdtpUtils.trimToMidnight(bir)
                isBirthSet = true
                bi.birth.text = bir!!.fullDate()
                bi.birth.alpha = 1f
                bi.birth.isLongClickable = true
            }, bir).defaultOptions().show(c.supportFragmentManager, "birth")
        }

        // First Met
        bi.firstMet.setOnClickListener {
            DatePickerDialog.newInstance({ _, year, month, day ->
                fir!!.set(Calendar.YEAR, year)
                fir!!.set(Calendar.MONTH, month)
                fir!!.set(Calendar.DAY_OF_MONTH, day)
                fir = McdtpUtils.trimToMidnight(fir)
                isFirstSet = true
                bi.firstMet.text = fir!!.fullDate()
                bi.firstMet.alpha = 1f
                bi.firstMet.isLongClickable = true
            }, fir).defaultOptions().show(c.supportFragmentManager, "first_met")
        }

        // Notify Birth
        val needsNtfPerm = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ActivityCompat.checkSelfPermission(
                    c, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
        if (needsNtfPerm && crush?.notifyBirth == true) reqNotificationPerm(c)
        bi.notifyBirth.setOnCheckedChangeListener { _, isChecked ->
            if (!needsNtfPerm && isChecked) reqNotificationPerm(c)
            bi.notifyBirth.alpha = if (isChecked) 1f else DISABLED_ALPHA
        } // changing isChecked programmatically won't invoke the listener!
        bi.notifyBirth.alpha = if (bi.notifyBirth.isChecked) 1f else DISABLED_ALPHA

        // Date Pickers: Long Click
        View.OnLongClickListener { v ->
            MaterialMenu(c, v, R.menu.clear_date, Act().apply {
                this[R.id.clearDate] = {
                    if (v == bi.birth) {
                        isBirthSet = false
                        bi.birth.setText(R.string.birth)
                        bi.birth.alpha = DISABLED_ALPHA
                        bi.birth.isLongClickable = false
                    } else {
                        isFirstSet = false
                        bi.firstMet.setText(R.string.firstMet)
                        bi.firstMet.alpha = DISABLED_ALPHA
                        bi.firstMet.isLongClickable = false
                    }
                }
            }).show(); true
        }.also {
            bi.birth.setOnLongClickListener(it)
            bi.firstMet.setOnLongClickListener(it)
        }

        MaterialAlertDialogBuilder(c).apply {
            setTitle("${c.getString(R.string.identify)}: ${crush?.key ?: c.m.crush}")
            setView(bi.root)
            setPositiveButton(R.string.save) { _, _ ->
                val endBir = bir!!.toGregorian() // "this" is returned when it is already Gregorian
                val endFir = fir!!.toGregorian()
                val inserted = Crush(
                    crush?.key ?: c.m.crush!!,
                    bi.fName.text.toString().ifBlank { null },
                    bi.mName.text.toString().ifBlank { null },
                    bi.lName.text.toString().ifBlank { null },
                    (bi.gender.selectedItemPosition - 1).toByte(),
                    if (isBirthSet) "${endBir[Calendar.YEAR]}.${endBir[Calendar.MONTH] + 1}." +
                            "${endBir[Calendar.DAY_OF_MONTH]}" else null,
                    if (bi.height.text.toString() != "")
                        bi.height.text.toString().toFloat() else -1f,
                    bi.address.text.toString().ifBlank { null },
                    bi.instagram.text.toString().ifBlank { null },
                    if (isFirstSet) "${endFir[Calendar.YEAR]}.${endFir[Calendar.MONTH] + 1}." +
                            "${endFir[Calendar.DAY_OF_MONTH]}" else null,
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
