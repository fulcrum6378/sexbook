package ir.mahdiparastesh.sexbook.more

import android.content.DialogInterface
import androidx.fragment.app.DialogFragment
import ir.mahdiparastesh.sexbook.Fun.CalendarType
import ir.mahdiparastesh.sexbook.Fun.calendar
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.mdtp.date.DatePickerDialog
import java.util.*

class LocalDatePicker(
    c: BaseActivity, tag: String, default: Calendar,
    dismissal: (DialogInterface) -> Unit = {},
    listener: (view: DialogFragment, time: Long) -> Unit
) {
    init {
        if (c.calType() == CalendarType.PERSIAN) {
            val jal = Jalali(default)
            // Repaired version of https://github.com/mohamad-amin/PersianMaterialDateTimePicker
            DatePickerDialog.newInstance(
                { view, year, monthOfYear, dayOfMonth ->
                    val cal = PersianCalendar()
                    cal.timeInMillis = default.timeInMillis
                    cal.set(year, monthOfYear, dayOfMonth)
                    listener(view, cal.timeInMillis)
                }, jal.Y, jal.M, jal.D, PersianCalendar::class.java
            ).apply {
                version = DatePickerDialog.Version.VERSION_1
                accentColor = c.color(R.color.CP)
                setOkColor(c.color(R.color.dialogText))
                setCancelColor(c.color(R.color.dialogText))
                setOnDismissListener(dismissal)
                show(c.supportFragmentManager, tag)
            }
        } else DatePickerDialog.newInstance(
            // Customised version of https://github.com/wdullaer/MaterialDateTimePicker
            { view, year, monthOfYear, dayOfMonth ->
                val cal = default.timeInMillis.calendar()
                cal[Calendar.YEAR] = year
                cal[Calendar.MONTH] = monthOfYear
                cal[Calendar.DAY_OF_MONTH] = dayOfMonth
                listener(view, cal.timeInMillis)
            },
            default[Calendar.YEAR],
            default[Calendar.MONTH],
            default[Calendar.DAY_OF_MONTH],
            android.icu.util.GregorianCalendar::class.java
        ).apply {
            version = DatePickerDialog.Version.VERSION_1
            accentColor = c.color(R.color.CP)
            setOkColor(c.color(R.color.dialogText))
            setCancelColor(c.color(R.color.dialogText))
            setOnDismissListener(dismissal)
            show(c.supportFragmentManager, tag)
        }
    }
}
