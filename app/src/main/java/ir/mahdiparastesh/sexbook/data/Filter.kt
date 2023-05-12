package ir.mahdiparastesh.sexbook.data

import ir.mahdiparastesh.mcdtp.McdtpUtils
import ir.mahdiparastesh.sexbook.more.BaseActivity

@Suppress("EqualsOrHashCode")
class Filter(val year: Int, val month: Int, var map: ArrayList<Int>) {
    fun put(item: Int) {
        map.add(item)
    }

    fun title(c: BaseActivity) =
        "${McdtpUtils.localSymbols(c, c.calType()).months[month]} $year : {${map.size}}"

    override operator fun equals(other: Any?): Boolean {
        if (other == null || other !is Filter) return false
        return year == other.year && month == other.month
    }
}
