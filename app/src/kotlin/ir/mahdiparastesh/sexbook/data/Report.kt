package ir.mahdiparastesh.sexbook.data

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity
class Report(
    var time: Long,
    var name: String?,
    var type: Byte,
    var desc: String?,
    var accu: Boolean,
    var plac: Long,
) {
    @PrimaryKey(autoGenerate = true)
    var id = 0L

    @Ignore
    @Transient
    var guess: Boolean = false

    @Ignore
    constructor(time: Long, name: String, type: Byte, plac: Long)
            : this(time, name, type, null, false, plac) {
        guess = true
    }

    override fun equals(other: Any?): Boolean = if (other !is Report) false else id == other.id
    override fun hashCode(): Int = id.hashCode()

    class Sort : Comparator<Report> {
        override fun compare(a: Report, b: Report) = a.time.compareTo(b.time)
    }
}
