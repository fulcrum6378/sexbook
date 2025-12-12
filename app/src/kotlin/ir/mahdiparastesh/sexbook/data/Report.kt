package ir.mahdiparastesh.sexbook.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import ir.mahdiparastesh.mcdtp.McdtpUtils
import ir.mahdiparastesh.sexbook.Sexbook
import ir.mahdiparastesh.sexbook.ctrl.Dao
import ir.mahdiparastesh.sexbook.page.Main
import ir.mahdiparastesh.sexbook.page.Settings
import ir.mahdiparastesh.sexbook.stat.Intervals
import ir.mahdiparastesh.sexbook.stat.Mixture
import ir.mahdiparastesh.sexbook.view.SexType
import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.experimental.xor

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

    /** Qualities of this sexual event */
    @ColumnInfo(defaultValue = "1")
    var qualities: Short = 1,
) {

    companion object {

        /**
         * Quality: Did the user orgasm during this sexual activity?
         *
         * [Mixture], [Intervals] and [Dao.whenWasTheLastTime] require this.
         * [Main.summarize] requires this if `spStatNonOrgasm` is checked in [Settings].
         */
        private val QUAL_ORGASMED = 0b1.toShort() to 0  // 1

        /**
         * Quality: How pleasant was this sexual activity?
         *
         * - 0 => Undefined
         * - 1 => Painful
         * - 2 => No pleasure
         * - 3 => A bit pleasant
         * - 4 => Pleasant
         * - 5 => Highly pleasant
         * - 6 => Ultra-pleasant
         * - (7): empty
         */
        private val QUAL_PLEASURE = (0b111 shl 1).toShort() to 1  // 2

        /**
         * Quality: How energetic was this sexual activity?
         *
         * - 0 => Undefined
         * - 1 => Low energy
         * - 2 => Normal energy
         * - 3 => Highly energetic
         * - 4 => Explosive
         * - (5,6,7): empty
         */
        private val QUAL_ENERGY = (0b111 shl 4).toShort() to 4  // 112

        /**
         * Quality: How baffling was this sexual activity?
         *
         * - 0 => Undefined
         * - 1 => Not baffling
         * - 2 => A bit baffling
         * - 3 => Moderately baffling
         * - 4 => Highly baffling
         * - (5,6,7): empty
         */
        private val QUAL_BAFFLEMENT = (0b111 shl 7).toShort() to 7  // 896

        /**
         * Quality: How draining was this sexual activity?
         *
         * - 0 => Undefined
         * - 1 => Premature ejaculation
         * - 2 => Normal ejaculation
         * - 3 => Deep ejaculation
         */
        private val QUAL_EJACULATION = (0b11 shl 10).toShort() to 10  // 3072
    }

    constructor(
        time: Long,
        name: String?,
        type: Byte,
        place: Long,
        description: String?,
        accurate: Boolean,
        qualities: Short
    ) : this(0L, time, name, type, place, description, accurate, qualities)

    @delegate:Ignore
    @delegate:Transient
    val guess: Boolean by lazy { id < 0L }

    @Ignore
    @Transient
    var analysis: List<String>? = null

    @Ignore  // for estimation
    constructor(id: Long, time: Long, name: String, type: Byte, place: Long)
            : this(id, time, name, type, place, null, false, 1)

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

    fun orgasmed() = (qualities and QUAL_ORGASMED.first) == 1.toShort()

    fun orgasmed(value: Boolean) {
        qualities =
            if (value) (qualities or QUAL_ORGASMED.first)
            else (qualities xor QUAL_ORGASMED.first)
    }

    fun pleasure(): Int = (qualities and QUAL_PLEASURE.first).toInt() shr QUAL_PLEASURE.second

    fun pleasure(value: Int) {
        qualities = (qualities and QUAL_PLEASURE.first.toInt().inv().toShort()) or
                (value shl QUAL_PLEASURE.second).toShort()
    }

    fun energy(): Int = (qualities and QUAL_ENERGY.first).toInt() shr QUAL_ENERGY.second

    fun energy(value: Int) {
        qualities = (qualities and QUAL_ENERGY.first.toInt().inv().toShort()) or
                (value shl QUAL_ENERGY.second).toShort()
    }

    fun bafflement(): Int = (qualities and QUAL_BAFFLEMENT.first).toInt() shr QUAL_BAFFLEMENT.second

    fun bafflement(value: Int) {
        qualities = (qualities and QUAL_BAFFLEMENT.first.toInt().inv().toShort()) or
                (value shl QUAL_BAFFLEMENT.second).toShort()
    }

    fun ejaculation(): Int =
        (qualities and QUAL_EJACULATION.first).toInt() shr QUAL_EJACULATION.second

    fun ejaculation(value: Int) {
        qualities = (qualities and QUAL_EJACULATION.first.toInt().inv().toShort()) or
                (value shl QUAL_EJACULATION.second).toShort()
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
            if (o.qualities != 1.toShort()) w.name("qual").value(o.qualities)
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
                "qual" -> o.qualities = r.nextInt().toShort()
                // obsolete:
                "ogsm" -> o.qualities = if (r.nextBoolean()) 1 else 0
                else -> r.skipValue()
            }
            r.endObject()
            return o
        }
    }
}
