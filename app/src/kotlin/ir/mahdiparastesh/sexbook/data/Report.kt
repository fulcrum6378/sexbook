package ir.mahdiparastesh.sexbook.data

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
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

    @Ignore // for estimation
    constructor(time: Long, name: String, type: Byte, plac: Long)
            : this(time, name, type, null, false, plac, true, -127) {
        guess = true
    }

    @Ignore // for GsonAdapter
    constructor() : this(0L, null, 0, null, true, -1L, true, -127)

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

    class GsonAdapter : TypeAdapter<Report>() {
        override fun write(w: JsonWriter, o: Report) {
            w.beginObject()
            w.name("id").value(o.id)
            w.name("time").value(o.time)
            if (!o.name.isNullOrBlank()) w.name("name").value(o.name)
            w.name("type").value(o.type)
            if (!o.desc.isNullOrBlank()) w.name("desc").value(o.desc)
            if (!o.accu) w.name("accu").value(false)
            if (o.plac != -1L) w.name("plac").value(o.plac)
            if (!o.ogsm) w.name("ogsm").value(false)
            if (o.frtn != (-127).toByte()) w.name("frtn").value(o.frtn)
            w.endObject()
        }

        override fun read(r: JsonReader): Report {
            val o = Report()
            r.beginObject()
            while (r.hasNext()) when (r.nextName()) {
                "id" -> o.id = r.nextLong()
                "time" -> o.time = r.nextLong()
                "name" -> o.name = r.nextString()
                "type" -> o.type = r.nextInt().toByte()
                "desc" -> o.desc = r.nextString()
                "accu" -> o.accu = r.nextBoolean()
                "plac" -> o.plac = r.nextLong()
                "ogsm" -> o.ogsm = r.nextBoolean()
                "frtn" -> o.frtn = r.nextInt().toByte()
            }
            r.endObject()
            return o
        }
    }
}
