package ir.mahdiparastesh.sexbook.more

import android.content.DialogInterface
import androidx.fragment.app.DialogFragment
import ir.mahdiparastesh.sexbook.mdtp.gdate.DatePickerDialog
import ir.mahdiparastesh.sexbook.Fun.CalendarType
import ir.mahdiparastesh.sexbook.Fun.Companion.calendar
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.mdtp.PersianCalendar
import ir.mahdiparastesh.sexbook.more.BaseActivity.Companion.night
import java.util.*

class LocalDatePicker(
    c: BaseActivity, tag: String, default: Calendar,
    dismissal: (DialogInterface) -> Unit = {},
    listener: (view: DialogFragment, time: Long) -> Unit
) {
    init {
        if (c.calType() == CalendarType.JALALI) {
            val jal = Jalali(default)
            // Repaired version of https://github.com/mohamad-amin/PersianMaterialDateTimePicker
            ir.mahdiparastesh.sexbook.mdtp.jdate.DatePickerDialog.newInstance(
                { view, year, monthOfYear, dayOfMonth ->
                    val cal = PersianCalendar()
                    cal.timeInMillis = default.timeInMillis
                    cal.setPersianDate(year, monthOfYear, dayOfMonth)
                    listener(view, cal.timeInMillis)
                }, jal.Y, jal.M, jal.D
            ).apply {
                setOnDismissListener(dismissal)
                isThemeDark = c.night()
                show(c.supportFragmentManager, tag)
            }
        } else DatePickerDialog.newInstance(
            { view, year, monthOfYear, dayOfMonth ->
                val cal = default.timeInMillis.calendar()
                cal[Calendar.YEAR] = year
                cal[Calendar.MONTH] = monthOfYear
                cal[Calendar.DAY_OF_MONTH] = dayOfMonth
                listener(view, cal.timeInMillis)
            },
            default[Calendar.YEAR],
            default[Calendar.MONTH],
            default[Calendar.DAY_OF_MONTH]
        ).apply {
            isThemeDark = c.night()
            version = DatePickerDialog.Version.VERSION_2
            accentColor = c.color(R.color.CP)
            setOkColor(c.color(R.color.mrvPopupButtons))
            setCancelColor(c.color(R.color.mrvPopupButtons))
            setOnDismissListener(dismissal)
            show(c.supportFragmentManager, tag)
        }
    }
}
