package ir.mahdiparastesh.sexbook.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import ir.mahdiparastesh.sexbook.view.SexType

/**
 * A `Guess` demonstrate a timeframe where a specific kind of sexual activity happened
 * over and over again with a specific amount of frequency.
 *
 * This feature is quite useful for creating statistics.
 * It can create many repetitive [Report] instances which are NOT inserted in the database.
 * Beware of the messes it can create!
 */
@Entity
class Guess(
    /** A variable unimportant ID */
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L,

    /** Name(s) of other person(s) engaged; exactly like [Report.name] */
    var name: String? = null,

    /** The beginning of the timeframe (a timestamp of milliseconds) */
    @ColumnInfo(defaultValue = "-1")
    var since: Long = -1L,

    /** The ending of the timeframe (a timestamp of milliseconds) */
    @ColumnInfo(defaultValue = "-1")
    var until: Long = -1L,

    /** The frequency of [Report] instances PER DAY */
    @ColumnInfo(defaultValue = "0.0")
    var frequency: Float = 0f,

    /** Type of the sexual activity (an ID from [SexType]) */
    @ColumnInfo(defaultValue = "1")
    var type: Byte = 1,

    /** The unique ID of the [Place] where the sexual activities occurred */
    @ColumnInfo(defaultValue = "-1")
    var place: Long = -1L,

    /** Arbitrary descriptions */
    var description: String? = null,

    /** Should this [Guess] be applied? (the user can disable it) */
    @ColumnInfo(defaultValue = "1")
    var active: Boolean = true,
) {

    fun checkValid() = active && since > -1L && until > -1L && frequency > 0 && until > since

    class Sort : Comparator<Guess> {
        override fun compare(a: Guess, b: Guess) = a.since.compareTo(b.since)
    }

    class GsonAdapter : TypeAdapter<Guess>() {
        override fun write(w: JsonWriter, o: Guess) {
            w.beginObject()
            if (!o.name.isNullOrBlank()) w.name("name").value(o.name)
            if (o.since != -1L) w.name("since").value(o.since)
            if (o.until != -1L) w.name("until").value(o.until)
            if (o.frequency != 0f) w.name("frequency").value(o.frequency)
            w.name("type").value(o.type)
            if (o.place != -1L) w.name("place").value(o.place)
            if (!o.description.isNullOrBlank())
                w.name("description").value(o.description)
            if (!o.active) w.name("active").value(false)
            w.endObject()
        }

        override fun read(r: JsonReader): Guess {
            val o = Guess()
            r.beginObject()
            while (r.hasNext()) when (r.nextName()) {
                "name" -> o.name = r.nextString()
                "since", "sinc" -> o.since = r.nextLong()
                "until", "till" -> o.until = r.nextLong()
                "frequency", "freq" -> o.frequency = r.nextDouble().toFloat()
                "type" -> o.type = r.nextInt().toByte()
                "place", "plac" -> o.place = r.nextLong()
                "description", "desc" -> o.description = r.nextString()
                "active", "able" -> o.active = r.nextBoolean()
                else -> r.skipValue()
            }
            r.endObject()
            return o
        }
    }
}
