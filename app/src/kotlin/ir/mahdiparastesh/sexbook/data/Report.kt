package ir.mahdiparastesh.sexbook.data

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import ir.mahdiparastesh.mcdtp.McdtpUtils
import ir.mahdiparastesh.sexbook.more.BaseActivity

@Entity
class Report(
    var time: Long,
    var name: String?,
    var type: Byte,
    var desc: String?,
    var accu: Boolean,
    var plac: Long,
    var ogsm: Boolean, // Mixture, Intervals and whenWasTheLastTime() require this
    var frtn: Byte, // fortuna
) {
    @PrimaryKey(autoGenerate = true)
    var id = 0L

    @Ignore
    @Transient
    var guess: Boolean = false

    @Ignore
    constructor(time: Long, name: String, type: Byte, plac: Long)
            : this(time, name, type, null, false, plac, true, -127) {
        guess = true
    }

    override fun equals(other: Any?): Boolean = if (other !is Report) false else id == other.id
    override fun hashCode(): Int = id.hashCode()

    class Sort : Comparator<Report> {
        override fun compare(a: Report, b: Report) = a.time.compareTo(b.time)
    }

    /** Helper class for filtering sex records by month. */
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

        override fun hashCode(): Int {
            var result = year
            result = 31 * result + month
            return result
        }
    }
}
