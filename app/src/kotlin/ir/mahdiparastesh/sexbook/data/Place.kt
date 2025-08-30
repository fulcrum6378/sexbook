package ir.mahdiparastesh.sexbook.data

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

/**
 * A `Place` is where some sexual activities have ever occurred and can have an arbitrary name.
 *
 * Later on, we can add a feature that displays these places on the map by assigning [latitude] and
 * [longitude] numbers.
 */
@Entity
class Place(
    /** A unique ID which must be synchronised with [Report.place] and [Guess.place] */
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L,

    /** A unique name */
    var name: String? = null,

    /** Currently unused */
    var latitude: Double? = null,

    /** Currently unused */
    var longitude: Double? = null,
) {

    @Ignore
    @Transient
    var sum = -1L

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

    class GsonAdapter : TypeAdapter<Place>() {
        override fun write(w: JsonWriter, o: Place) {
            w.beginObject()
            w.name("id").value(o.id)  // MUST NECESSARILY BE EXPORTED
            w.name("name").value(o.name ?: "")
            if (o.latitude != null && o.latitude != -1.0
                && o.longitude != null && o.longitude != -1.0
            ) w.name("location").value("${o.latitude},${o.longitude}")
            w.endObject()
        }

        override fun read(r: JsonReader): Place {
            val o = Place()
            r.beginObject()
            while (r.hasNext()) when (r.nextName()) {
                "id" -> o.id = r.nextLong()
                "name" -> o.name = r.nextString()
                "location" -> r.nextString().split(",").also {
                    if (it[0] != "null") o.latitude = it[0].toDouble()
                    if (it[1] != "null") o.longitude = it[1].toDouble()
                }
                else -> r.skipValue()
            }
            r.endObject()
            return o
        }
    }
}
