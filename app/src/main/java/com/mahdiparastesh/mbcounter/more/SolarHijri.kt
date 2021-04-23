package com.mahdiparastesh.mbcounter.more

import java.util.*

@Suppress("PropertyName")
class SolarHijri(cl: Calendar) {
    var Y = 0
    var M = 0
    var D = 0

    init {
        val y = cl[Calendar.YEAR]
        val m = cl[Calendar.MONTH]
        val d = cl[Calendar.DAY_OF_MONTH]
        var l = if (isLeapYear(y)) 1 else 0
        val clPl = Calendar.getInstance()
        clPl[y - 1, 0] = 1
        var pl = if (isLeapYear(clPl[Calendar.YEAR])) 1 else 0
        when (m) {
            0 -> if (d <= 20 - pl) {
                Y = y - 622
                M = m + 9
                D = d + (10 + pl)
            } else {
                Y = y - 622
                M = m + 10
                D = d - (20 - pl)
            }
            1 -> if (d <= 19 - pl) {
                Y = y - 622
                M = m + 9
                D = d + (11 + pl)
            } else {
                Y = y - 622
                M = m + 10
                D = d - (19 - pl)
            }
            2 -> if (d <= 20 - l) {
                Y = y - 622
                M = m + 9
                D = d + (9 + l + pl)
            } else {
                Y = y - 621
                M = m - 2
                D = d - (20 - l)
            }
            3 -> if (d <= 20 - l) {
                Y = y - 621
                M = m - 3
                D = d + (11 + l)
            } else {
                Y = y - 621
                M = m - 2
                D = d - (20 - l)
            }
            4, 5 -> if (d <= 21 - l) {
                Y = y - 621
                M = m - 3
                D = d + (10 + l)
            } else {
                Y = y - 621
                M = m - 2
                D = d - (21 - l)
            }
            6, 7, 8 -> if (d <= 22 - l) {
                Y = y - 621
                M = m - 3
                D = d + (9 + l)
            } else {
                Y = y - 621
                M = m - 2
                D = d - (22 - l)
            }
            9 -> if (d <= 22 - l) {
                Y = y - 621
                M = m - 3
                D = d + (8 + l)
            } else {
                Y = y - 621
                M = m - 2
                D = d - (22 - l)
            }
            10 -> if (d <= 21 - l) {
                Y = y - 621
                M = m - 3
                D = d + (9 + l)
            } else {
                Y = y - 621
                M = m - 2
                D = d - (21 - l)
            }
            11 -> if (d <= 21 - l) {
                Y = y - 621
                M = m - 3
                D = d + (9 + l)
            } else {
                Y = y - 621
                M = m - 2
                D = d - (21 - l)
            }
        }
    }

    fun isLeapYear(year: Int): Boolean {
        val cal = Calendar.getInstance()
        cal[Calendar.YEAR] = year
        return cal.getActualMaximum(Calendar.DAY_OF_YEAR) > 365
    }
}