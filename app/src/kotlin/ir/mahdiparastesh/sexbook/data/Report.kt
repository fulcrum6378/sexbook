package ir.mahdiparastesh.sexbook.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import ir.mahdiparastesh.mcdtp.McdtpUtils
import ir.mahdiparastesh.sexbook.Main
import ir.mahdiparastesh.sexbook.Settings
import ir.mahdiparastesh.sexbook.Sexbook
import ir.mahdiparastesh.sexbook.ctrl.Dao
import ir.mahdiparastesh.sexbook.stat.Intervals
import ir.mahdiparastesh.sexbook.stat.Mixture
import ir.mahdiparastesh.sexbook.view.SexType

/**
 * A `Report` is any kind of sexual activity that is reported at a specific time and space,
 * whether or not the user had orgasmed during this session
 * (that's why it shouldn't be name `Orgasm`).
 */
@Entity
class Report(

    /** A variable ID which will not be exported in JSON */
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L,

    /** Timestamp of this sexual activity in milliseconds, as accurate as possible */
    @ColumnInfo(index = true)
    var time: Long,

    /**
     * Name(s) of other person(s) engaged
     * Multiple names can be distinguished by [analyse] using specific conventions that the user
     * can enter:
     *
     * - A and B
     * - A & B
     * - A, B and C
     * - A + B
     *
     * Finally the name(s) will be used to instantiate [Crush]es.
     */
    var name: String? = null,

    /** Type of the sexual activity (an ID from [SexType]) */
    @ColumnInfo(defaultValue = "1")
    var type: Byte = 1,

    /** The unique ID of the [Place] where the sexual activity occurred */
    @ColumnInfo(index = true, defaultValue = "-1")
    var place: Long = -1L,

    /** Arbitrary descriptions */
    var description: String? = null,

    /** Is the [time] value accurate? */
    @ColumnInfo(defaultValue = "1")
    var accurate: Boolean = true,

    /**
     * Did the user orgasm during this sexual activity?
     *
     * [Mixture], [Intervals] and [Dao.whenWasTheLastTime] require this.
     * [Main.summarize] requires this if `spStatNonOrgasm` is checked in [Settings].
     */
    @ColumnInfo(defaultValue = "1")
    var orgasmed: Boolean = true,
) {

    constructor(
        time: Long,
        name: String?,
        type: Byte,
        place: Long,
        description: String?,
        accurate: Boolean,
        orgasmed: Boolean
    ) : this(0L, time, name, type, place, description, accurate, orgasmed)

    @delegate:Ignore
    @delegate:Transient
    val guess: Boolean by lazy { id < 0L }

    @Ignore
    @Transient
    var analysis: List<String>? = null

    @Ignore  // for estimation
    constructor(id: Long, time: Long, name: String, type: Byte, place: Long)
            : this(id, time, name, type, place, null, false, true)

    /*@Ignore  // for GsonAdapter
    constructor() : this(
        0L, null, 1, -1L, null, true, true
    )*/

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
            if (o.place != -1L) w.name("plac").value(o.place)
            if (!o.description.isNullOrBlank()) w.name("desc").value(o.description)
            if (!o.accurate) w.name("accu").value(false)
            if (!o.orgasmed) w.name("ogsm").value(false)
            w.endObject()
        }

        override fun read(r: JsonReader): Report {
            val o = Report(0, 0)
            r.beginObject()
            while (r.hasNext()) when (r.nextName()) {
                "time" -> o.time = r.nextLong()
                "name" -> o.name = r.nextString()
                "type" -> o.type = r.nextInt().toByte()
                "plac" -> o.place = r.nextLong()
                "desc" -> o.description = r.nextString()
                "accu" -> o.accurate = r.nextBoolean()
                "ogsm" -> o.orgasmed = r.nextBoolean()
                else -> r.skipValue()
            }
            r.endObject()
            return o
        }
    }
}
