package ir.mahdiparastesh.sexbook.data

import ir.mahdiparastesh.sexbook.mdtp.Utils
import ir.mahdiparastesh.sexbook.more.BaseActivity

class Filter(var year: Int, var month: Int, var items: ArrayList<Int>) {
    fun put(item: Int) {
        items.add(item)
    }

    fun title(c: BaseActivity) =
        "${Utils.localSymbols(c, c.calType()).months[month]} $year : {${items.size}}"
}
