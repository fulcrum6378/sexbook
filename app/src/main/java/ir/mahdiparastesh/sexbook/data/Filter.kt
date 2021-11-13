package ir.mahdiparastesh.sexbook.data

import android.content.Context
import ir.mahdiparastesh.sexbook.Fun
import ir.mahdiparastesh.sexbook.R
import java.util.*

class Filter(var year: Int, var month: Int, var items: ArrayList<Int>) {
    fun put(item: Int) {
        items.add(item)
    }

    fun title(c: Context) = "${
        c.resources.getStringArray(
            when (Fun.calType()) {
                Fun.CalendarType.JALALI -> R.array.jMonthsFull
                else -> R.array.monthsFull
            }
        )[month]
    } $year : {${items.size}}"
}
