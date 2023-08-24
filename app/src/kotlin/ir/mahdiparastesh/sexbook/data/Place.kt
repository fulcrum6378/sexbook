package ir.mahdiparastesh.sexbook.data

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity
class Place @Ignore constructor(
    var name: String?,
    var latitude: Double,
    var longitude: Double,
) {
    @PrimaryKey(autoGenerate = true)
    var id = 0L

    @Ignore
    @Transient
    var sum = -1L

    constructor() : this(null, -1.0, -1.0)

    class Sort(var by: Byte) : Comparator<Place> {
        override fun compare(a: Place, b: Place): Int = when (by) {
            NAME -> (a.name ?: "").compareTo(b.name ?: "")
            else -> (b.sum - a.sum).toInt()
        }

        companion object {
            const val SUM: Byte = 0
            const val NAME: Byte = 1
        }
    }
}