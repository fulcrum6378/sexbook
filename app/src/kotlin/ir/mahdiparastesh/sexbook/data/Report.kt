package ir.mahdiparastesh.sexbook.data

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import ir.mahdiparastesh.mcdtp.McdtpUtils
import ir.mahdiparastesh.sexbook.Sexbook

@Entity
class Report(
    @PrimaryKey(autoGenerate = true)
    var id: Long,
    var time: Long,
    var name: String?,
    var type: Byte,
    var desc: String?,
    var accu: Boolean,
    var plac: Long,
    /** Mixture, Intervals and whenWasTheLastTime() require this.
     * Main::summarize() requires this if "spStatNonOrgasm" is checked in Settings. */
    var ogsm: Boolean,
) {

    constructor(
        time: Long,
        name: String?,
        type: Byte,
        desc: String?,
        accu: Boolean,
        plac: Long,
        ogsm: Boolean
    ) : this(0L, time, name, type, desc, accu, plac, ogsm)

    @delegate:Ignore
    @delegate:Transient
    val guess: Boolean by lazy { id < 0L }

    @Ignore
    @Transient
    var analysis: List<String>? = null

    @Ignore // for estimation
    constructor(id: Long, time: Long, name: String, type: Byte, plac: Long)
            : this(id, time, name, type, null, false, plac, true)

    @Ignore // for GsonAdapter
    constructor() : this(0L, null, 0, null, true, -1L, true)

    override fun equals(other: Any?): Boolean = if (other !is Report) false else id == other.id
    override fun hashCode(): Int = id.hashCode()

    fun analyse() {
        analysis = if (name.isNullOrBlank())
            listOf()
        else
            name!!
                .replace(" and ", " + ")
                .replace(" & ", " + ")
                .replace(", ", " + ")
                .split(" + ")
                .map { it.trim() }
    }

    /** Helper class for filtering sex records by month */
    class Filter(val year: Int, val month: Int, var map: ArrayList<Long>) {

        fun put(item: Long) {
            map.add(item)
        }

        fun title(c: Sexbook) =
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
            w.name("time").value(o.time)
            if (!o.name.isNullOrBlank()) w.name("name").value(o.name)
            w.name("type").value(o.type)
            if (!o.desc.isNullOrBlank()) w.name("desc").value(o.desc)
            if (!o.accu) w.name("accu").value(false)
            if (o.plac != -1L) w.name("plac").value(o.plac)
            if (!o.ogsm) w.name("ogsm").value(false)
            w.endObject()
        }

        override fun read(r: JsonReader): Report {
            val o = Report()
            r.beginObject()
            while (r.hasNext()) when (r.nextName()) {
                "time" -> o.time = r.nextLong()
                "name" -> o.name = r.nextString()
                "type" -> o.type = r.nextInt().toByte()
                "desc" -> o.desc = r.nextString()
                "accu" -> o.accu = r.nextBoolean()
                "plac" -> o.plac = r.nextLong()
                "ogsm" -> o.ogsm = r.nextBoolean()
                else -> r.skipValue()
            }
            r.endObject()
            return o
        }
    }
}
