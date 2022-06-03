package ir.mahdiparastesh.sexbook.data

import ir.mahdiparastesh.sexbook.Fun
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.more.BaseActivity
import java.text.DateFormatSymbols

class Filter(var year: Int, var month: Int, var items: ArrayList<Int>) {
    fun put(item: Int) {
        items.add(item)
    }

    fun title(c: BaseActivity) = "${
        when (c.calType()) {
            Fun.CalendarType.JALALI -> c.resources.getStringArray(R.array.jMonthsFull)[month]
            else -> DateFormatSymbols().months[month]
        }
    } $year : {${items.size}}"
}
