package ir.mahdiparastesh.sexbook.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Guess(
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
}
