package ir.mahdiparastesh.sexbook.data

import ir.mahdiparastesh.sexbook.Fun
import ir.mahdiparastesh.sexbook.R
import ir.mahdiparastesh.sexbook.more.BaseActivity

class Filter(var year: Int, var month: Int, var items: ArrayList<Int>) {
    fun put(item: Int) {
        items.add(item)
    }

    fun title(c: BaseActivity) = "${
        c.resources.getStringArray(
            when (c.calType()) {
                Fun.CalendarType.JALALI -> R.array.jMonthsFull
                else -> R.array.monthsFull
            }
        )[month]
    } $year : {${items.size}}"
}
