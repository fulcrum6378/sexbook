package ir.mahdiparastesh.sexbook.data

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

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

    constructor() : this(
        null, -1L, -1L, 0f, 1, null, -1L, true
    )

    fun checkValid() = able && sinc > -1L && till > -1L && freq > 0 && till > sinc

    class Sort : Comparator<Guess> {
        override fun compare(a: Guess, b: Guess) = a.sinc.compareTo(b.sinc)
    }

    class GsonAdapter : TypeAdapter<Guess>() {
        override fun write(w: JsonWriter, o: Guess) {
            w.beginObject()
            if (!o.crsh.isNullOrBlank()) w.name("crsh").value(o.crsh)
            if (o.sinc != -1L) w.name("sinc").value(o.sinc)
            if (o.till != -1L) w.name("till").value(o.till)
            if (o.freq != 0f) w.name("freq").value(o.freq)
            w.name("type").value(o.type)
            if (!o.desc.isNullOrBlank()) w.name("desc").value(o.desc)
            if (o.plac != -1L) w.name("plac").value(o.plac)
            if (!o.able) w.name("able").value(false)
            w.endObject()
        }

        override fun read(r: JsonReader): Guess {
            val o = Guess()
            r.beginObject()
            while (r.hasNext()) when (r.nextName()) {
                "crsh" -> o.crsh = r.nextString()
                "sinc" -> o.sinc = r.nextLong()
                "till" -> o.till = r.nextLong()
                "freq" -> o.freq = r.nextDouble().toFloat()
                "type" -> o.type = r.nextInt().toByte()
                "desc" -> o.desc = r.nextString()
                "plac" -> o.plac = r.nextLong()
                "able" -> o.able = r.nextBoolean()
                else -> r.skipValue()
            }
            r.endObject()
            return o
        }
    }
}
