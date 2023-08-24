package ir.mahdiparastesh.sexbook.data

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity
class Guess @Ignore constructor(
    var crsh: String?,
    var sinc: Long,
    var till: Long,
    var freq: Float,
    var type: Byte,
    var desc: String?,
    var plac: Long,
    var able: Boolean,
) {
    @PrimaryKey(autoGenerate = true)
    var id = 0L

    constructor() : this(null, -1L, -1L, 0f, 1, null, -1L, true)

    fun checkValid() = sinc > -1L && till > -1L && freq > 0 && till > sinc

    class Sort : Comparator<Guess> {
        override fun compare(a: Guess, b: Guess) = a.sinc.compareTo(b.sinc)
    }
}
