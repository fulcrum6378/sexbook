package ir.mahdiparastesh.sexbook.more

import android.icu.util.Calendar
import androidx.fragment.app.DialogFragment
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import ir.mahdiparastesh.sexbook.Fun
import ir.mahdiparastesh.sexbook.Fun.CalendarType
import ir.mahdiparastesh.sexbook.Fun.Companion.calType
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.jdtp.utils.PersianCalendar

class LocalDatePicker(
    c: BaseActivity, tag: String, default: Calendar,
    listener: (view: DialogFragment, time: Long) -> Unit,
) {
    init {
        if (calType() == CalendarType.JALALI) {
            val jal = Jalali(default)
            // Repaired version of https://github.com/mohamad-amin/PersianMaterialDateTimePicker
            ir.mahdiparastesh.sexbook.jdtp.date.DatePickerDialog.newInstance(
                { view, year, monthOfYear, dayOfMonth ->
                    val cal = PersianCalendar()
                    cal.timeInMillis = default.timeInMillis
                    cal.setPersianDate(year, monthOfYear, dayOfMonth)
                    listener(view, cal.timeInMillis)
                }, jal.Y, jal.M, jal.D
            ).apply {
                isThemeDark = BaseActivity.night
                show(c.supportFragmentManager, tag)
            }
        } else DatePickerDialog.newInstance(
            { view, year, monthOfYear, dayOfMonth ->
                val cal = Fun.calendar(default.timeInMillis)
                cal[Calendar.YEAR] = year
                cal[Calendar.MONTH] = monthOfYear
                cal[Calendar.DAY_OF_MONTH] = dayOfMonth
                listener(view, cal.timeInMillis)
            },
            default[Calendar.YEAR],
            default[Calendar.MONTH],
            default[Calendar.DAY_OF_MONTH]
        ).apply {
            isThemeDark = BaseActivity.night
            version = DatePickerDialog.Version.VERSION_2
            accentColor = c.color(R.color.CP)
            setOkColor(c.color(R.color.mrvPopupButtons))
            setCancelColor(c.color(R.color.mrvPopupButtons))
            show(c.supportFragmentManager, tag)
        }
    }
}
