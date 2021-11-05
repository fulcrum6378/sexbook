package ir.mahdiparastesh.sexbook.data

import android.content.Context
import ir.mahdiparastesh.sexbook.R
import java.util.*

class Filter(var year: Int, var month: Int, var items: ArrayList<Int>) {
    fun put(item: Int) {
        items.add(item)
    }

    fun titleInShamsi(c: Context) =
        "${c.resources.getStringArray(R.array.jMonthsFull)[month]} $year : {${items.size}}"

    fun title(c: Context) =
        "${c.resources.getStringArray(R.array.monthsFull)[month]} $year : {${items.size}}"
}
