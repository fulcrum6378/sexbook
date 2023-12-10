package ir.mahdiparastesh.sexbook.data

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

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

    class GsonAdapter : TypeAdapter<Place>() {
        override fun write(w: JsonWriter, o: Place) {
            w.beginObject()
            w.name("id").value(o.id)
            w.name("name").value(o.name ?: "")
            if (o.latitude != -1.0 && o.longitude != -1.0)
                w.name("location").value("${o.latitude},${o.longitude}")
            w.endObject()
        }

        override fun read(r: JsonReader): Place {
            val o = Place()
            r.beginObject()
            while (r.hasNext()) when (r.nextName()) {
                "id" -> o.id = r.nextLong()
                "name" -> o.name = r.nextString()
                "location" -> r.nextString().split(",").also {
                    o.latitude = it[0].toDouble()
                    o.longitude = it[1].toDouble()
                }
                "latitude" -> o.latitude = r.nextDouble() // compatibility
                "longitude" -> o.longitude = r.nextDouble() // compatibility
            }
            r.endObject()
            return o
        }
    }
}
